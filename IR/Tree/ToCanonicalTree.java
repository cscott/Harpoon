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
import java.util.Iterator; 
import java.util.Map;
import java.util.HashSet;

/**
 * The <code>ToCanonicalTree</code> class translates tree code to 
 * canonical tree code (no ESEQ).  Based on the translator to canonical
 * form by Andrew Appel.  
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ToCanonicalTree.java,v 1.1.2.22 2000-01-31 03:31:15 cananian Exp $
 */
public class ToCanonicalTree implements Derivation, TypeMap {
    private Tree m_tree;
    private Derivation m_derivation;

    /** Class constructor. 
     *
     * @param tf    The <code>TreeFactory</code> which will be used for all
     *              elements of the new <code>CanonicalTreeCode</code>.
     * @param code  The <code>TreeCode</code> which we wish to translate
     */
    public ToCanonicalTree(final TreeFactory tf, TreeCode code) { 
	Util.assert(tf instanceof Code.TreeFactory);
	Util.assert(((Code.TreeFactory)tf).getParent().getName().equals("canonical-tree"));

    	final Map dT = new HashMap();
	final Map tT = new HashMap();

	m_tree = translate(tf, code, dT, tT);
	m_derivation = new Derivation() {
	    public DList derivation(HCodeElement hce, Temp t) {
		Util.assert(hce!=null && t!=null);
		Tuple tuple = new Tuple(new Object[] { hce, t });
		if (dT.get(tuple)==null && tT.get(tuple)==null)
		    throw new TypeNotKnownException(hce, t);
		return (DList)dT.get(tuple);
	    }
	    public HClass typeMap(HCodeElement hce, Temp t) {
		Util.assert(hce!=null && t!=null);
		Tuple tuple = new Tuple(new Object[] { hce, t });
		if (dT.get(tuple)==null && tT.get(tuple)==null)
		    throw new TypeNotKnownException(hce, t);
		return (HClass) tT.get(tuple);
	    }
	};

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
	return m_derivation.typeMap(hce, t);
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

	public CanonicalizingVisitor(TreeFactory tf, TreeMap tm, 
				     TreeCode code, Map dT, Map tT) {
	    this.code       = code;
	    this.derivation = code;
	    this.treeMap    = tm;
	    this.dT         = dT;
	    this.tT         = tT;
	    this.tf         = tf;
	    this.ctm        = new CloningTempMap
		(((Stm)code.getRootElement()).getFactory().tempFactory(), 
		 tf.tempFactory());
	}


	public void visit(Tree t) { 
	    throw new Error("No defaults here");
	}

	public void visit(MOVE s) {
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

	}
      
	public void visit(SEQ s) {
	    if (!visited.add(s)) return;
	    s.getLeft().accept(this);
	    s.getRight().accept(this);
	    treeMap.map(s, seq(treeMap.get(s.getLeft()), treeMap.get(s.getRight())));
	}

	public void visit(DATUM s) { 
	    if (!visited.add(s)) return;
	    treeMap.map(s, reorderData(s));
	}

	public void visit(Stm s) {
	    if (!visited.add(s)) return;
	    treeMap.map(s, reorderStm(s));
	}

	public void visit(Exp e) {
	    if (!visited.add(e)) { 
		return;
	    }
	    treeMap.map(e, reorderExp(e));
	}

	public void visit(TEMP e) { 
	    if (!visited.add(e)) return;
	    TEMP tNew = _MAP(e);
	    treeMap.map(e, reorderExp(tNew));
	}

	public void visit(ESEQ e) { 
	    if (!visited.add(e)) return;
	    e.getStm().accept(this);
	    e.getExp().accept(this);
	    treeMap.map(e, new ESEQ(tf, e, 
				    seq(treeMap.get(e.getStm()), 
					((ESEQ)treeMap.get(e.getExp())).getStm()),
				    ((ESEQ)treeMap.get(e.getExp())).getExp()));
	}

	private Stm reorderData(DATUM s) { 
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
	    Tuple hceT = new Tuple(new Object[] { tNew, tNew.temp });
	    if (this.derivation.derivation(tOld, tOld.temp) != null) {
		dT.put
		    (hceT, 
		     DList.clone(this.derivation.derivation(tOld, tOld.temp)));
	    }
	    else {
		if (this.derivation.typeMap(tOld, tOld.temp) != null) {
		    tT.put
			(hceT,
			 this.derivation.typeMap(tNew, tOld.temp));
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







