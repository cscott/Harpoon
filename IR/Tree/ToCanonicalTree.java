// ToCanonicalTree.java, created Mon Mar 29  0:08:40 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
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
 * @version $Id: ToCanonicalTree.java,v 1.3 2002-02-26 22:46:11 cananian Exp $
 */
public class ToCanonicalTree {
    private Tree m_tree;
    private DerivationGenerator m_dg = new DerivationGenerator();

    /** Class constructor. 
     *
     * @param tf    The <code>TreeFactory</code> which will be used for all
     *              elements of the new <code>CanonicalTreeCode</code>.
     * @param code  The <code>TreeCode</code> which we wish to translate
     */
    public ToCanonicalTree(final TreeFactory tf, TreeCode code) { 
	Util.ASSERT(tf instanceof Code.TreeFactory);
	Util.ASSERT(((Code.TreeFactory)tf).getParent().getName().equals("canonical-tree"));

	m_tree = translate(tf, code);
    }
    
    /** Returns a <code>TreeDerivation</code> object for the
     *  generated <code>Tree</code> form. */
    public TreeDerivation getTreeDerivation() { return new TreeDerivation() {
	public HClass typeMap(Exp exp) { return HClass.Void; }
	public DList derivation(Exp exp) { return null; }
    };
    }

    /** Returns the root of the generated tree code */
    public Tree getTree() {
	return m_tree;
    }

    // translate to canonical form
    private Tree translate(TreeFactory tf, TreeCode code) {
	CanonicalizingVisitor cv;    // Translates the TreeCode
	Stm                   root;  // The root of "code"
	TreeMap               tm;    // maps old trees to translated trees

	tm   = new TreeMap();
	cv   = new CanonicalizingVisitor(tf, tm, code);
	root = (Stm)code.getRootElement();

	// This visitor recursively visits all relevant nodes on its own
	root.accept(cv);

	// Return the node which rootClone has been mapped to
	return tm.get(root);
    }

    // Visitor class to translate to canonical form
    class CanonicalizingVisitor extends TreeVisitor {
	private CloningTempMap ctm; 
	private TreeCode    code;
	private TreeFactory tf; 
	private TreeMap     treeMap;
	private TreeDerivation oldDeriv;
	private java.util.Set visited = new java.util.HashSet();

	public CanonicalizingVisitor(TreeFactory tf, TreeMap tm, 
				     TreeCode code) {
	    this.code       = code;
	    this.treeMap    = tm;
	    this.tf         = tf;
	    this.oldDeriv   = code.getTreeDerivation();
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
		Util.ASSERT(false, "Dangerous use of ESEQ");
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

	public void visit(METHOD e) {
	    if (!visited.add(e)) return;
	    TEMP[] pOld = e.getParams();
	    TEMP[] pNew = new TEMP[pOld.length];
	    for(int i=0; i<pNew.length; i++) {
		pNew[i] = _MAP(pOld[i]);
	    }
	    treeMap.map(e, new METHOD(tf, e, e.getMethod(), e.getReturnType(),
				      pNew));
	}
	public void visit(CALL s) {
	    if (!visited.add(s)) return;
	    TEMP tNewV = (s.getRetval()==null)?null:_MAP(s.getRetval());
	    TEMP tNewX = _MAP(s.getRetex());
	    NAME handler = new NAME(tf, s.getHandler(), s.getHandler().label);
	    StmExpList x = reorder(s.kids());
	    CALL c = new CALL(tf, s, tNewV, tNewX, 
			      x.exps.head, x.exps.tail, handler,
			      s.isTailCall);
	    treeMap.map(s, c);
	}
	public void visit(NATIVECALL s) {
	    if (!visited.add(s)) return;
	    TEMP tNew = (s.getRetval()==null)?null:_MAP(s.getRetval());
	    StmExpList x = reorder(s.kids());
	    NATIVECALL nc = new NATIVECALL(tf, s, tNew,
					   x.exps.head, x.exps.tail);
	    treeMap.map(s, nc);
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
		StmExpList x = reorder(m.kids());
		Util.ASSERT(x.exps.tail==null);
		return seq(x.stm, new MOVE(tf, m, tNew, x.exps.head));
	    }
	    else {
		Util.ASSERT(m.getDst().kind() == TreeKind.MEM);
		StmExpList d = reorder(new ExpList(m.kids().head, null));
		StmExpList s = reorder(m.kids().tail);
		Util.ASSERT(d.exps.tail==null && s.exps.tail==null);
		MOVE nm = new MOVE(tf, m,
				   m.getDst().build(tf, d.exps),
				   s.exps.head);
		return seq(s.stm, seq(d.stm, nm));
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
		    (new EXPR
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
	
	protected void updateDT(Exp eOld, Exp eNew) {
	    HClass hc = oldDeriv.typeMap(eOld);
	    if (hc!=null) {
		if (eNew instanceof TEMP)
		    m_dg.putTypeAndTemp(eNew, hc, ((TEMP)eNew).temp);
		else
		    m_dg.putType(eNew, hc);
	    } else {
		m_dg.putDerivation(eNew, oldDeriv.derivation(eOld));
	    }
	}

	Stm seq(Stm a, Stm b) {
	    if (isNop(a))      return b;
	    else if (isNop(b)) return a;
	    else return new SEQ(tf, a, a, b);
	}
    
	private TEMP _MAP(TEMP t) { 
	    return (TEMP)t.rename(tf, ctm, new Tree.CloneCallback() {
		public Tree callback(Tree o, Tree n, TempMap tm) { return n; }
	    });
	}
    }
    
    static boolean commute(Stm a, Exp b) {
	return isNop(a) || 
	    (b.kind()==TreeKind.NAME) || 
	    (b.kind()==TreeKind.CONST);
    }

    static boolean isNop(Stm s) {
	return (s.kind()==TreeKind.EXPR) && 
	    ((((EXPR)s).getExp()).kind()==TreeKind.CONST);
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







