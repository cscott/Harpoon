// ToCanonicalTree.java, created Mon Mar 29  0:08:40 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.Derivation;
import harpoon.IR.Properties.Derivation.DList;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Util.Tuple;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Map;

/**
 * The <code>ToCanonicalTree</code> class translates tree code to 
 * canonical tree code (no ESEQ).  Based on the translator to canonical
 * form by Andrew Appel.  
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ToCanonicalTree.java,v 1.1.2.19 2000-01-09 00:21:56 duncan Exp $
 */
public class ToCanonicalTree implements Derivation, TypeMap {
    private Tree m_tree;
    private Derivation m_derivation;
    private TypeMap m_typeMap;

    /** Class constructor. 
     *
     * @param tf    The <code>TreeFactory</code> which will be used for all
     *              elements of the new <code>CanonicalTreeCode</code>.
     * @param code  The <code>TreeCode</code> which we wish to translate
     */
    public ToCanonicalTree(final TreeFactory tf, TreeCode code) { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().getName().equals("canonical-tree"));

	code.print(new java.io.PrintWriter(System.out)); 
	
    	final Map dT = new HashMap();
	final Map tT = new HashMap();
	System.err.println("Converting to canonical tree: " + code.getMethod()); 
	long time = System.currentTimeMillis(); 
	

	m_tree = translate(tf, code, dT, tT);
	m_derivation = new Derivation() {
	    public DList derivation(HCodeElement hce, Temp t) {
		Util.assert(hce!=null && t!=null);
		return (DList)dT.get(new Tuple(new Object[] { hce, t }));
	    }
	};
	m_typeMap = new TypeMap() {
	    public HClass typeMap(HCodeElement hce, Temp t) {
		Util.assert(t.tempFactory()==tf.tempFactory());
		Util.assert(hce!=null && t!=null);
		Object type = tT.get(t);   // Ignores hc parameter
		try { return (HClass)type; } 
		catch (ClassCastException cce) { 
		    throw (Error)((Error)type).fillInStackTrace();
		}
	    }
	};

	System.err.println("Total conversion time: " + ((System.currentTimeMillis()-time))); 
    }
    
    /** Returns the updated derivation information for the 
     *  specified <code>Temp</code>.  The <code>HCodeElement</code>
     *  parameter must be a <code>Tree</code> object in which the 
     *  <code>Temp</code> is found.
     */
    public DList derivation(HCodeElement hce, Temp t) {
	return m_derivation.derivation(hce, t);
    }

    /** Returns the root of the generated tree code */
    public Tree getTree() {
	return m_tree;
    }

    /** Returns the updated type information for the specified
     *  <code>Temp</code>.  The <code>HCode</code> paramter is
     *  ignored. */
    public HClass typeMap(HCodeElement hce, Temp t) {
	return m_typeMap.typeMap(hce, t);
    }

    // translate to canonical form
    private Tree translate(TreeFactory tf, TreeCode code, Map dT, Map tT) {
	CanonicalizingVisitor cv;    // Translates the TreeCode
	Stm                   root;  // The root of "code"
	TreeMap               tm;    // maps old trees to translated trees

	tm   = new TreeMap();
	cv   = new CanonicalizingVisitor(tf, tm, code, dT, tT);
	root = (Stm)code.getRootElement();

	// This visitor recursively visits all relevant nodes on its own
	root.accept(cv);

	for (int i=0; i<6; i++) { 
	    System.err.println("TIME, NUM: " + cv.times[i] + ", " +cv.nums[i]);
	}

	// Return the node which rootClone has been mapped to
	return tm.get(root);
    }

    // Visitor class to translate to canonical form
    class CanonicalizingVisitor extends TreeVisitor {
	private CloningTempMap ctm; 
	private Derivation  derivation;
	private TreeCode    code;
	private TreeFactory tf; 
	private TreeMap     treeMap;
	private TypeMap     typeMap;
	private Map         dT, tT;
	private java.util.Set visited = new java.util.HashSet();
	public long times[] = new long[10]; 
	public int nums[] = new int[10]; 
	

	public CanonicalizingVisitor(TreeFactory tf, TreeMap tm, 
				     TreeCode code, Map dT, Map tT) {
	    this.code       = code;
	    this.derivation = code;
	    this.treeMap    = tm;
	    this.dT         = dT;
	    this.tT         = tT;
	    this.tf         = tf;
	    this.typeMap    = code;
	    this.ctm        = new CloningTempMap
		(((Stm)code.getRootElement()).getFactory().tempFactory(), 
		 tf.tempFactory());
	}

	public void visit(Tree t) { 
	    throw new Error("No defaults here");
	}

	public void visit(MOVE s) {
	    long time = System.currentTimeMillis(); 
	    
	    if (!visited.add(s)) return;

	    //s.getSrc().accept(this);
	    if (s.getDst().kind()==TreeKind.ESEQ) {
		Util.assert(false, "Dangerous use of ESEQ");
		ESEQ eseq = (ESEQ)s.getDst();
		eseq.getExp().accept(this);
		eseq.getStm().accept(this);
		SEQ tmp = new SEQ
		    (tf, treeMap.get(eseq.getStm()), 
		     s, 
		     new MOVE
		     (tf, s, 
		      treeMap.get(eseq.getExp()), 
		      treeMap.get(s.getSrc())));
		tmp.accept(this);
		treeMap.map(s, treeMap.get(tmp));
	    }
	    else {
		treeMap.map(s, reorderMove(s));
	    }

	    times[0] += (System.currentTimeMillis()-time); 
	    nums[0]++; 
	}
      
	public void visit(SEQ s) {
	    //long time = System.currentTimeMillis(); 

	    if (!visited.add(s)) return;
	    s.getLeft().accept(this);
	    s.getRight().accept(this);
	    treeMap.map(s, seq(treeMap.get(s.getLeft()), treeMap.get(s.getRight())));
	}

	public void visit(DATA s) { 

	    long time = System.currentTimeMillis(); 

	    if (!visited.add(s)) return;
	    treeMap.map(s, reorderData(s));
	    times[1] += (System.currentTimeMillis()-time); 
	    nums[1]++; 
	}

	public void visit(Stm s) {
	    long time = System.currentTimeMillis(); 
	    if (!visited.add(s)) return;
	    treeMap.map(s, reorderStm(s));
	    times[2] += (System.currentTimeMillis()-time); 
	    nums[2]++; 
	}

	public void visit(Exp e) { 
	    long time = System.currentTimeMillis(); 
	    if (!visited.add(e)) { 
		return;
	    }
	    treeMap.map(e, reorderExp(e));
	    times[3] += (System.currentTimeMillis()-time); 
	    nums[3]++; 

	}

	public void visit(TEMP e) { 
	    long time = System.currentTimeMillis(); 
	    if (!visited.add(e)) return;
	    TEMP tNew = _MAP(e);
	    treeMap.map(e, reorderExp(tNew));
	    times[4] += (System.currentTimeMillis()-time); 
	    nums[4]++; 

	}

	public void visit(ESEQ e) { 
	    long time = System.currentTimeMillis(); 
	    if (!visited.add(e)) return;
	    e.getStm().accept(this);
	    e.getExp().accept(this);
	    treeMap.map(e, new ESEQ(tf, e, 
				    seq(treeMap.get(e.getStm()), 
					((ESEQ)treeMap.get(e.getExp())).getStm()),
				    ((ESEQ)treeMap.get(e.getExp())).getExp()));
	    times[5] += (System.currentTimeMillis()-time); 
	    nums[5]++; 
	}

	private Stm reorderData(DATA s) { 
	    ExpList kids = s.kids();
	    if (kids.head==null) return s.build(tf, kids);
	    else {
		StmExpList x = reorder(kids);
		return seq((x.stm), s.build(tf, x.exps));
	    }
	}
    
	private Stm reorderMove(MOVE m) { 
	    if (m.getDst().kind() == TreeKind.TEMP) { 
		TEMP tNew = _MAP((TEMP)m.getDst());
		StmExpList x = reorder(m.kids().tail);
		return seq(x.stm, m.build(tf, new ExpList(tNew, x.exps)));
	    }
	    else {
		StmExpList d = reorder(new ExpList(m.kids().head, null));
		StmExpList s = reorder(m.kids().tail);
		Util.assert(d.exps.tail==null && s.exps.tail==null);
		ExpList el = // combine src and dst exp lists.
		    new ExpList(d.exps.head, new ExpList(s.exps.head, null));
		return seq(s.stm, seq(d.stm, m.build(tf, el)));
	    }
	}

	private Stm reorderStm(Stm s) {
	    StmExpList x = reorder(s.kids());
	    return seq(x.stm, s.build(tf, x.exps));
	}
	
	private ESEQ reorderExp (Exp e) {
	    StmExpList x = reorder(e.kids());
	    return new ESEQ(tf, e, x.stm, e.build(tf, x.exps));
	}
	
        private StmExpList reorder(ExpList exps) {
	    if (exps==null) 
		return new StmExpList
		    (new EXP
		     (this.tf, code.getRootElement(), 
		      new CONST(this.tf, code.getRootElement(), 0)),
		     null);
	    else {
		Exp a = exps.head; a.accept(this);
		ESEQ aa = (ESEQ)treeMap.get(a);
		StmExpList bb = reorder(exps.tail);
		if (commute(bb.stm, aa.getExp()))
		    return new StmExpList(seq(aa.getStm(),bb.stm), 
					  new ExpList(aa.getExp(),bb.exps));
		else { // FIXME: must update DT for new Temp
		    Temp t = new Temp(tf.tempFactory());
		    return new StmExpList
			(seq
			 (aa.getStm(), 
			  seq
			  (new MOVE
			   (tf, aa, 
			    new TEMP(tf, aa, aa.getExp().type(), t),
			    aa.getExp()),
			   bb.stm)),
			 new ExpList(new TEMP(tf, aa, aa.getExp().type(), t), 
				     bb.exps));
		}
	    }
	}
	
	protected void updateDT(TEMP tOld, TEMP tNew) {
	    if (this.derivation.derivation(tOld, tOld.temp) != null) {
		Tuple hceT = new Tuple(new Object[] { tNew, tNew.temp });
		dT.put
		    (hceT, 
		     DList.clone(this.derivation.derivation(tOld, tOld.temp)));
		tT.put
		    (hceT, 
		     new Error("*** Derived pointers have no type"));
	    }
	    else {
		if (this.typeMap.typeMap(tOld, tOld.temp) != null) {
		    tT.put
			(new Tuple(new Object[] {tNew,tNew.temp}),
			 this.typeMap.typeMap(tNew, tOld.temp));
		}
	    }
	}

	Stm seq(Stm a, Stm b) {
	    if (isNop(a))      return b;
	    else if (isNop(b)) return a;
	    else return new SEQ(tf, a, a, b);
	}
    
	private TEMP _MAP(TEMP t) { 
	    return (TEMP)Tree.clone(tf, ctm, t); 
	}
    }
    
    static boolean commute(Stm a, Exp b) {
	return isNop(a) || 
	    (b.kind()==TreeKind.NAME) || 
	    (b.kind()==TreeKind.CONST);
    }

    static boolean isNop(Stm s) {
	return (s.kind()==TreeKind.EXP) && 
	    ((((EXP)s).getExp()).kind()==TreeKind.CONST);
    }
}

class StmExpList {
    Stm stm;
    ExpList exps;
    StmExpList(Stm s, ExpList e) {stm=s; exps=e;}
}

class TreeMap {
    private Map h = new HashMap();   
    void map(Tree t1, Tree t2) { h.put(t1, t2); }
    Exp get(Exp e) { return (Exp)h.get(e); }
    Stm get(Stm s) { return (Stm)h.get(s); }
}







