// Unreachable.java, created Wed Feb 24 20:03:06 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Maps.Derivation;

import harpoon.ClassFile.HCode;

import harpoon.IR.LowQuad.DerivationMap;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.PHI;

import harpoon.Temp.Temp;

import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
/**
 * <code>Unreachable</code> gets rid of unreachable code.
 * <b>CAUTION</b>: it modifies code in-place.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Unreachable.java,v 1.5 2003-03-11 00:58:47 cananian Exp $
 */
public abstract class Unreachable  {

    /** Prunes unreachable code from a quad graph in-place.  Also updates
     *  the derivation for the <code>HCode</code>, if present. */
    public static final void prune(harpoon.IR.Quads.Code hc) {
	// fetch or invalidate mutable derivation information.
	DerivationMap<Quad> dm = null;
	if (hc.getDerivation()!=null) {
	    harpoon.IR.LowQuad.Code c = (harpoon.IR.LowQuad.Code) hc;
	    if (c.getDerivation() instanceof DerivationMap)
		dm = (DerivationMap<Quad>) c.getDerivation();
	    else {
		//c.setDerivation(null); // clear derivation information.
		assert false; // can't invalidate, can't update, abort!
	    }
	}
	prune(hc.getRootElement(), dm);
    }
    /** Prunes unreachable code *without updating the derivation*. */
    public static final void prune(HEADER header) {
	prune(header, null);
    }
    /** private pruning method. */
    private static final void prune(HEADER header, DerivationMap<Quad> dm) {
	// okay, now find the unreachable code and prune it.
	Set<Quad> reachable = (new ReachabilityVisitor(header)).reachableSet();
	(new PruningVisitor(reachable, dm)).prune();
    }

    /** Class to do reachability analysis. */
    static private class ReachabilityVisitor extends LowQuadVisitor {
	final Set<Quad> reachable = new HashSet<Quad>();
	WorkSet<Quad> todo = new WorkSet<Quad>();
	ReachabilityVisitor(HEADER h) {
	    super(false); /* not strict low quad */
	    todo.add(h);
	    while (!todo.isEmpty())
		todo.pop().accept(this);

	    todo = null; // free space.
	}
	public Set<Quad> reachableSet() { return reachable; }

	public void visit(Quad q) {
	    reachable.add(q);
	    Quad[] nxt = q.next();
	    for (int i=0; i<nxt.length; i++)
		if (!reachable.contains(nxt[i]))
		    todo.add(nxt[i]);
	}
    }

    /** Class to do the pruning of unreachable edges. */
    static private class PruningVisitor extends LowQuadVisitor {
	final Set<Quad> reachable;
	final DerivationMap<Quad> dm;
	PruningVisitor(Set<Quad> reachable, DerivationMap<Quad> dm) {
	    super(false); /* not strict low quad */
	    this.reachable = reachable;
	    this.dm = dm;
	}
	void prune() {
	    // dump the original live elements into a list.
	    List<Quad> l = new ArrayList<Quad>(reachable);
	    for (Iterator<Quad> it=l.iterator(); it.hasNext(); )
		it.next().accept(this);
	}
	public void visit(Quad q) { /* do nothing. */ }
	public void visit(PHI q) {
	    // remove unused inputs to PHI.
	    PHI oldphi = q;
	    for (int i=q.prevLength()-1; i>=0; i--)
		if (!reachable.contains(q.prev(i)))
		    q = q.shrink(i);
	    if (oldphi != q) { // update derivation/make reachable
		if (dm!=null) updateDM(oldphi, q);
		reachable.add(q);
	    }
		
	    // if it shrinks too small, then remove it.
	    if (q.arity()==1) { // make arity-1 PHI into MOVEs.
		Edge in = q.prevEdge(0), out = q.nextEdge(0);
		Quad header = in.from();
		int which_succ = in.which_succ();
		for (int i=0; i<q.numPhis(); i++) {
		    MOVE m=new MOVE(q.getFactory(), q, q.dst(i), q.src(i, 0));
		    Quad.addEdge(header, which_succ, m, 0);
		    header = m; which_succ = 0;
		    reachable.add(m);
		    if (dm!=null) dm.update(q, q.dst(i), m, m.dst());
		}
		Quad.addEdge(header, which_succ,
			     (Quad)out.to(), out.which_pred());
	    }
	}
	public void visit(FOOTER q) {
	    // remove unused inputs to footer.
	    for (int i=q.prevLength()-1; i>=0; i--)
		if (!reachable.contains(q.prev(i)))
		    q = q.remove(i);

	    // make sure new quad is marked as reachable.
	    reachable.add(q);
	}

	private void updateDM(Quad oldq, Quad newq) {
	    // transfer derivations of temps defined in newq
	    Temp[] defs = newq.def();
	    for (int i=0; i<defs.length; i++)
		dm.update(oldq, defs[i], newq, defs[i]);
	    // clear out any left over derivations from oldq
	    defs = oldq.def();
	    for (int i=0; i<defs.length; i++)
		dm.remove(oldq, defs[i]);
	}
    }
}
