// CoalescingToNoSSA.java, created Thu Nov 23 14:52:08 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadRSSx;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.SWITCH;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.DisjointSet;
import harpoon.Util.Util;

import java.util.Iterator;
/**
 * <code>CoalescingToNoSSA</code> converts SSA, SSI, and RSSx forms
 * to No-SSA form, *coalescing* variables mentioned in phi and sigma
 * statements where possible instead of inserting moves.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CoalescingToNoSSA.java,v 1.1.2.1 2000-11-25 16:33:16 cananian Exp $
 */
public class CoalescingToNoSSA extends MethodMutator {
    
    /** Creates a <code>CoalescingToNoSSA</code>. */
    public CoalescingToNoSSA(HCodeFactory parent) {
        super(parent);
    }
    private static class MyNoSSA extends QuadNoSSA {
	private MyNoSSA(HMethod m) { super(m, null); }
	public static HCodeAndMaps cloneToNoSSA(harpoon.IR.Quads.Code c,
						HMethod m) {
	    MyNoSSA r = new MyNoSSA(m);
	    return r.cloneHelper(c, r);
	}
    }
    protected String mutateCodeName(String codeName) {
	Util.assert(codeName.equals(QuadSSA.codename) ||
		    codeName.equals(QuadSSI.codename) ||
		    codeName.equals(QuadRSSx.codename));
	return MyNoSSA.codename;
    }
    protected HCodeAndMaps cloneHCode(HCode hc, HMethod newmethod) {
	Util.assert(hc.getName().equals(QuadSSA.codename) ||
		    hc.getName().equals(QuadSSI.codename) ||
		    hc.getName().equals(QuadRSSx.codename));
	return MyNoSSA.cloneToNoSSA((harpoon.IR.Quads.Code)hc, newmethod);
    }
    protected HCode mutateHCode(HCodeAndMaps input) {
	Code hc = (Code) input.hcode();
	Util.assert(hc.getName().equals(QuadNoSSA.codename));
	QuadVisitor qv = new TransformVisitor(hc);
	Quad[] qa = (Quad[]) hc.getElements();
	for (int i=0; i<qa.length; i++)
	    qa[i].accept(qv);
	return hc;
    }
    private static class RenameVisitor extends QuadVisitor {
	public final DisjointSet ds = new DisjointSet();
	public final QuadInterferenceGraph qig;
	RenameVisitor(Code hc) {
	    this.qig = new QuadInterferenceGraph(hc);
	    for (Iterator it=hc.getElementsI(); it.hasNext(); )
		((Quad) it.next()).accept(this);
	}
	public void visit(Quad q) { /* do nothing */ }
	public void visit(PHI q) {
	    for (int i=0; i<q.numPhis(); i++)
		for (int j=0; j<q.arity(); j++)
		    if (!qig.isEdge(q.dst(i), q.src(i, j)))
			if (!ds.find(q.dst(i)).equals(ds.find(q.src(i, j))))
			    ds.union(q.dst(i), q.src(i, j));
	}
	public void visit(SIGMA q) {
	    for (int i=0; i<q.numSigmas(); i++)
		for (int j=0; j<q.arity(); j++)
		    if (!qig.isEdge(q.dst(i, j), q.src(i)))
			if (!ds.find(q.dst(i, j)).equals(ds.find(q.src(i))))
			    ds.union(q.dst(i, j), q.src(i));
	}
    }
    private static class TransformVisitor extends QuadVisitor {
	public final TempMap tm;
	TransformVisitor(Code hc) {
	    final DisjointSet ds = new RenameVisitor(hc).ds;
	    this.tm = new TempMap() {
		public Temp tempMap(Temp t) {
		    return (Temp) ds.find(t);
		}
	    };
	}
	private Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
	private Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	    Quad frm = (Quad) e.from(); int frm_succ = e.which_succ();
	    Quad to  = (Quad) e.to();   int to_pred = e.which_pred();
	    Quad.addEdge(frm, frm_succ, q, which_pred);
	    Quad.addEdge(q, which_succ, to, to_pred);
	    return to.prevEdge(to_pred);
	}
	public void visit(Quad q) { Quad.replace(q, q.rename(tm, tm)); }
	public void visit(HEADER q) { /* do nothing. */ }
	public void visit(FOOTER q) { /* do nothing. */ }
	public void visit(PHI q) {
	    QuadFactory qf = q.getFactory();
	    Quad nq = new PHI(qf, q, new Temp[0], q.arity());
	    Quad.replace(q, nq);
	    for (int i=0; i<q.numPhis(); i++)
		for (int j=0; j<q.arity(); j++) {
		    Temp d = map(q.dst(i));
		    Temp s = map(q.src(i, j));
		    if (!d.equals(s))
			addAt(nq.prevEdge(j), new MOVE(qf, q, d, s));
		}
	}
	public void visit(SIGMA q) { Util.assert(false); }
	private void dosigma(QuadFactory qf, SIGMA q, SIGMA nq) {
	    Quad.replace(q, nq);
	    for (int i=0; i<q.numSigmas(); i++)
		for (int j=0; j<q.arity(); j++) {
		    Temp d = map(q.dst(i, j));
		    Temp s = map(q.src(i));
		    if (!d.equals(s))
			addAt(nq.nextEdge(j), new MOVE(qf, q, d, s));
		}
	}
	public void visit(CJMP q) {
	    QuadFactory qf = q.getFactory();
	    dosigma(qf, q, new CJMP(qf, q, map(q.test()), new Temp[0]));
	}
	public void visit(SWITCH q) {
	    QuadFactory qf = q.getFactory();
	    dosigma(qf, q, new SWITCH(qf, q, map(q.index()), q.keys(),
				      new Temp[0]));
	}
	public void visit(TYPESWITCH q) {
	    QuadFactory qf = q.getFactory();
	    dosigma(qf, q, new TYPESWITCH(qf, q, map(q.index()), q.keys(),
					  new Temp[0], q.hasDefault()));
	}
	public void visit(CALL q) {
	    QuadFactory qf = q.getFactory();
	    dosigma(qf, q, new CALL(qf, q, q.method(), map(q.params()),
				    map(q.retval()), map(q.retex()),
				    q.isVirtual(), q.isTailCall(),
				    new Temp[0]));
	}
	private Temp map(Temp t) { return (t==null) ? null : tm.tempMap(t); }
	private Temp[] map(Temp[] ta) {
	    Temp[] r = new Temp[ta.length];
	    for (int i=0; i<r.length; i++)
		r[i] = tm.tempMap(ta[i]);
	    return r;
	}
    }
}
