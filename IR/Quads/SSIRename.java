// SSIRename.java, created Fri Aug 27 17:58:00 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Analysis.Place;
import harpoon.Analysis.Quads.QuadLiveness;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGraphable;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
import harpoon.Temp.WritableTempMap;
import harpoon.Util.Environment;
import harpoon.Util.HashEnvironment;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
/**
 * <code>SSIRename</code> is a new, improved, fast SSI-renaming
 * algorithm.  Detailed in the author's thesis.  This Java version
 * is hairy because of the big "efficiency-vs-immutable quads" fight.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SSIRename.java,v 1.1.2.8 2000-04-04 04:13:46 cananian Exp $
 */
class SSIRename {
    private static final boolean sort_phisig = false;
    /** Return a copy of the given quad graph properly converted to
     *  SSI form. */
    static ReturnTuple rename(final Code c, final QuadFactory nqf) {
	final SearchState S = new SearchState(c, nqf);
	return new ReturnTuple((Quad) S.old2new.get(c.getRootElement()),
			       S.varmap, S.old2new);
    }
    static class ReturnTuple {
	final Quad rootQuad; // new root element.
	final TempMap tempMap; // map old temps to new temps.
	final Map quadMap; // map old quads to new quads.
	ReturnTuple(Quad rootQuad, TempMap tempMap, Map quadMap) {
	    this.rootQuad=rootQuad; this.tempMap=tempMap; this.quadMap=quadMap;
	}
    }

    static class VarMap implements TempMap {
	final TempFactory tf;
	final Environment vm = new HashEnvironment();
	Temp get(Temp t) {
	    if (!vm.containsKey(t)) { vm.put(t, t.clone(tf)); }
	    return (Temp) vm.get(t);
	}
	Temp inc(Temp t) {
	    if (!vm.containsKey(t)) { vm.put(t, t.clone(tf)); }
	    else { vm.put(t, get(t).clone()); }
	    return get(t);
	}
	// environment interface.
	Stack s = new Stack();
	void beginScope() { s.push(vm.getMark()); }
	void endScope() { vm.undoToMark((Environment.Mark)s.pop()); }

	public Temp tempMap(Temp t) { return get(t); }
	VarMap(TempFactory tf) { this.tf = tf; }
    }

    static class SearchState {
	/** maps no-ssi quads to ssi quads */
	final Map old2new = new HashMap();
	/** shows where to place phi/sigs */
	final Place place;
	/** QuadFactory to use for new Quads. */
	final QuadFactory nqf;
	
	// algorithm state
	/** maps old variables to new variables */
	final VarMap varmap;
	/** edge stack to unroll dfs recursion. */
	final Stack We = new Stack();
	/** mark edges as we visit them */
	final Set marked = new HashSet();

	SearchState(HCode c, QuadFactory nqf) { 
	    this.place = new Place(c, new QuadLiveness(c));
	    this.varmap= new VarMap(nqf.tempFactory());
	    this.nqf   = nqf;

	    setup(c);

	    HCodeElement ROOT = c.getRootElement();
	    for (Iterator it=((CFGraphable)ROOT).edgeC().iterator();
		 it.hasNext(); )
		We.push(it.next());
	    
	    while (!We.isEmpty()) {
		Edge e = (Edge) We.pop();
		if (e==null) { varmap.endScope(); continue; }
		We.push(null); varmap.beginScope();
		search(e);
	    }

	    makePhiSig(c);

	    // now link edges
	    for (Iterator it=c.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		Quad fr = (Quad) old2new.get(q);
		for (int i = 0; i < q.nextLength(); i++) {
		    Edge e = q.nextEdge(i);
		    Quad to = (Quad) old2new.get(e.to());
		    Quad.addEdge(fr, e.which_succ(), to, e.which_pred());
		}
	    }
	}

	final Map lhs = new HashMap();//lhs of phi/sigma
	final Map rhs = new HashMap();//rhs of phi/sigma
	final Map arg = new HashMap();//uses in a sigma
	final Map sdf = new HashMap();//defs in a sigma
	void setup(HCode c) {
	    for (Iterator it=c.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		if (q instanceof PHI) {
		    Temp[] l = place.phiNeeded(q);
		    if (sort_phisig) Collections.sort(Arrays.asList(l));
		    Temp[][] r = new Temp[l.length][((PHI)q).arity()];
		    for (int i = 0; i < r.length; i++)
			for (int j = 0; j < r[i].length; j++)
			    r[i][j] = l[i];
		    lhs.put(q, l);
		    rhs.put(q, r);
		} else if (q instanceof SIGMA) {
		    Temp[] r = place.sigNeeded(q);
		    if (sort_phisig) Collections.sort(Arrays.asList(r));
		    Temp[][] l = new Temp[r.length][((SIGMA)q).arity()];
		    for (int i = 0; i < l.length; i++)
			for (int j = 0; j < l[i].length; j++)
			    l[i][j] = r[i];
		    lhs.put(q, l);
		    rhs.put(q, r);
		}
	    }
	}

	final WritableTempMap wtm = new WritableTempMap() {
	    final Map backing = new HashMap();
	    public Temp tempMap(Temp t) { return (Temp) backing.get(t); }
	    public void associate(Temp o, Temp n) { backing.put(o,n); }
	};
	void search(Edge e) {
	    Util.assert(e.from() instanceof PHI ||
			e.from() instanceof SIGMA ||
			e.from() instanceof HEADER);
	    // handle dfs bookkeeping.
	    Util.assert(!marked.contains(e));
	    marked.add(e);
	    // setup 'from' state.
	    Quad from = (Quad) e.from();
	    if (from instanceof HEADER) {
		old2new.put(from, from.rename(nqf, null, null));
	    } else if (from instanceof PHI) {
		Temp[] l = (Temp[]) lhs.get(from);
		for (int i=0; i<l.length; i++)
		    l[i] = varmap.inc(l[i]);
	    } else if (from instanceof SIGMA) {
		Temp[][] l = (Temp[][]) lhs.get(from);
		int j = e.which_succ();
		for (int i=0; i<l.length; i++)
		    l[i][j] = varmap.inc(l[i][j]);
	    }
	    // fixup defs in CALL sigma appropriately
	    if (from instanceof CALL) {
		CALL Q = (CALL) from;
		Temp[] defs = (Temp[]) sdf.get(from);
		if (defs==null) { defs=new Temp[2]; sdf.put(from, defs); }
		// use of inc() below serves to kill any aliases on this path
		if (e.which_succ()==0 && Q.retval()!=null)
		    defs[0]=varmap.inc(Q.retval());
		if (e.which_succ()==1)
		    defs[1]=varmap.inc(Q.retex());
	    }
	    // now go on renaming inside basic block until we get to a phi
	    // or sigma.
	    for (Quad q; ; e=q.nextEdge(0)) {
		q = (Quad) e.to();
		if (q instanceof PHI) { /* update src */
		    Temp[][] r = (Temp[][]) rhs.get(q);
		    int j = e.which_pred();
		    for (int i=0; i < r.length; i++)
			r[i][j] = varmap.get(r[i][j]);
		    break;
		}
		if (q instanceof SIGMA) { /* update src */
		    // rhs of sigma comes before any defs in the sigma.
		    Temp[] r = (Temp[]) rhs.get(q);
		    for (int i=0; i < r.length; i++)
			r[i] = varmap.get(r[i]);
		    if (q instanceof CJMP)
			arg.put(q, varmap.get(((CJMP)q).test()));
		    else if (q instanceof SWITCH)
			arg.put(q, varmap.get(((SWITCH)q).index()));
		    else if (q instanceof CALL) {
			CALL Q = (CALL) q;
			Temp[] args = new Temp[Q.paramsLength()];
			for (int i=0; i<Q.paramsLength(); i++)
			    args[i] = varmap.get(Q.params(i));
			arg.put(q, args);
		    } else throw new Error("Ack!");
		    break;
		}
		/* else, rename src, then rename dst */
		Temp u[] = q.use(), d[] = q.def();
		for (int i=0; i<u.length; i++)
		    wtm.associate(u[i], varmap.get(u[i]));
		for (int i=0; i<d.length; i++)
		    varmap.inc(d[i]);
		Quad nq = q.rename(nqf, varmap, wtm);
		old2new.put(q, nq);
		if (q instanceof FOOTER) break;
	    }
	    for (Iterator it=((Quad)e.to()).succC().iterator();it.hasNext();) {
		e = (Edge) it.next();
		if (!marked.contains(e))
		    We.push(e);
	    }
	}
	void makePhiSig(HCode c) {
	    for (Iterator it=c.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		if (q instanceof PHI) {
		    Temp[] l = (Temp[]) lhs.get(q);
		    Temp[][] r = (Temp[][]) rhs.get(q);
		    Quad nq = new PHI(nqf, q, l, r, ((PHI)q).arity());
		    old2new.put(q, nq);
		} else if (q instanceof SIGMA) {
		    Temp[][] l = (Temp[][]) lhs.get(q);
		    Temp[] r = (Temp[]) rhs.get(q);
		    Quad nq;
		    if (q instanceof CJMP)
			nq = new CJMP(nqf, q, (Temp) arg.get(q), l, r);
		    else if (q instanceof SWITCH)
			nq = new SWITCH(nqf, q, (Temp) arg.get(q),
					((SWITCH)q).keys(), l, r);
		    else if (q instanceof CALL) {
			CALL Q = (CALL) q;
			Temp[] defs = (Temp[]) sdf.get(q);
			nq = new CALL(nqf, Q, Q.method(), (Temp[]) arg.get(q),
				      defs[0], defs[1],
				      Q.isVirtual(), Q.isTailCall(),
				      l, r);
		    } else throw new Error("Ack!");
		    old2new.put(q, nq);
		}
	    }
	}
    }
}
