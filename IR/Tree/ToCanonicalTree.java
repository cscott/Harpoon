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
 * @version $Id: ToCanonicalTree.java,v 1.1.2.11 1999-08-09 22:11:13 duncan Exp $
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
	Util.assert(tf.getParent().getName().equals("canonical-tree"));
	
    	final Map dT = new HashMap();
	final Map tT = new HashMap();

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
	root.visit(cv);

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
	    s.src.visit(this);

	    if (s.dst.kind()==TreeKind.ESEQ) {
		Util.assert(false, "Dangerous use of ESEQ");
		ESEQ eseq = (ESEQ)s.dst;
		eseq.exp.visit(this);
		eseq.stm.visit(this);
		SEQ tmp = new SEQ
		    (tf, treeMap.get(eseq.stm), 
		     s, 
		     new MOVE
		     (tf, s, 
		      treeMap.get(eseq.exp), 
		      treeMap.get(s.src)));
		tmp.visit(this);
		treeMap.map(s, treeMap.get(tmp));
	    }
	    else {
		treeMap.map(s, reorderMove(s));
	    }
	}
      
	public void visit(SEQ s) {
	    s.left.visit(this);
	    s.right.visit(this);
	    treeMap.map(s, seq(treeMap.get(s.left), treeMap.get(s.right)));
	}

	public void visit(Stm s) { 
	    treeMap.map(s, reorderStm(s));
	}

	public void visit(Exp e) { 
	    treeMap.map(e, reorderExp(e));
	}

	public void visit(TEMP e) { 
	    TEMP tNew = _MAP(e);
	    treeMap.map(e, reorderExp(tNew));
	}

	public void visit(ESEQ e) { 
	    e.stm.visit(this);
	    e.exp.visit(this);
	    treeMap.map(e, new ESEQ(tf, e, 
				    seq(treeMap.get(e.stm), 
					((ESEQ)treeMap.get(e.exp)).stm),
				    ((ESEQ)treeMap.get(e.exp)).exp));
	}

	private Stm reorderMove(MOVE m) { 
	    if (m.dst.kind() == TreeKind.TEMP) { 
		TEMP tNew = _MAP((TEMP)m.dst);
		StmExpList x = reorder(m.kids().tail);
		return seq(x.stm, m.build(tf, new ExpList(tNew, x.exps)));
	    }
	    else {
		StmExpList x = reorder(m.kids());
		return seq(x.stm, m.build(tf, x.exps));
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
		Exp a = exps.head; a.visit(this);
		ESEQ aa = (ESEQ)treeMap.get(a);
		StmExpList bb = reorder(exps.tail);
		if (commute(bb.stm, aa.exp))
		    return new StmExpList(seq(aa.stm,bb.stm), 
					  new ExpList(aa.exp,bb.exps));
		else { // FIXME: must update DT for new Temp
		    Temp t = new Temp(tf.tempFactory());
		    return new StmExpList
			(seq
			 (aa.stm, 
			  seq
			  (new MOVE
			   (tf, aa, 
			    new TEMP(tf, aa, Type.POINTER, t),
			    aa.exp),
			   bb.stm)),
			 new ExpList(new TEMP(tf, aa, Type.POINTER, t), 
				     bb.exps));
		}
	    }
	}
	
	
	protected void updateDT(TEMP tOld, TEMP tNew) {
	    if (this.derivation.derivation(tOld, tOld.temp) != null) {
		dT.put
		    (new Tuple(new Object[] { tNew, tNew.temp }),
		     DList.clone(this.derivation.derivation(tOld, tOld.temp)));
		dT.put
		    (tNew.temp,new Error("*** Derived pointers have no type"));
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
	    if (code.getFrame().isRegister(t.temp)) { 
		return (TEMP)t.build(tf, t.kids());
	    }
	    else { 
		Temp tmp = t.temp==null?null:this.ctm.tempMap(t.temp);
		return new TEMP(tf, t, t.type(), tmp);
	    }
	}
    }
    
    static boolean commute(Stm a, Exp b) {
	return isNop(a) || 
	    (b.kind()==TreeKind.NAME) || 
	    (b.kind()==TreeKind.CONST);
    }

    static boolean isNop(Stm s) {
	return (s.kind()==TreeKind.EXP) && 
	    ((((EXP)s).exp).kind()==TreeKind.CONST);
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







