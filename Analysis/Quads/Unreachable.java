// Unreachable.java, created Wed Feb 24 20:03:06 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.PHI;

import harpoon.Util.Util;
import harpoon.Util.WorkSet;

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
 * @version $Id: Unreachable.java,v 1.1.2.2 2000-10-11 14:28:19 cananian Exp $
 */
public abstract class Unreachable  {

    /** Prunes unreachable code from a quad graph in-place. */
    public static final void prune(HEADER header) {
	Set reachable = (new ReachabilityVisitor(header)).reachableSet();
	(new PruningVisitor(reachable)).prune();
    }

    /** Class to do reachability analysis. */
    static private class ReachabilityVisitor extends LowQuadVisitor {
	final Set reachable = new HashSet();
	WorkSet todo = new WorkSet();
	ReachabilityVisitor(HEADER h) {
	    super(false); /* not strict low quad */
	    todo.add(h);
	    while (!todo.isEmpty())
		((Quad)todo.pop()).accept(this);

	    todo = null; // free space.
	}
	public Set reachableSet() { return reachable; }

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
	final Set reachable;
	PruningVisitor(Set reachable) {
	    super(false); /* not strict low quad */
	    this.reachable = reachable;
	}
	void prune() {
	    // dump the original live elements into a list.
	    List l = new ArrayList(reachable);
	    for (Iterator it=l.iterator(); it.hasNext(); )
		((Quad)it.next()).accept(this);
	}
	public void visit(Quad q) { /* do nothing. */ }
	public void visit(PHI q) {
	    // remove unused inputs to PHI.
	    for (int i=q.prevLength()-1; i>=0; i--)
		if (!reachable.contains(q.prev(i)))
		    q = q.shrink(i);
		
	    // if it shrinks too small, then remove it.
	    if (q.arity()==1) {
		Edge in = q.prevEdge(0), out = q.nextEdge(0);
		Quad.addEdge((Quad)in.from(), in.which_succ(),
			     (Quad)out.to(), out.which_pred());
	    } else // make sure new quad is marked as reachable
		reachable.add(q);
	}
	public void visit(FOOTER q) {
	    // remove unused inputs to footer.
	    for (int i=q.prevLength()-1; i>=0; i--)
		if (!reachable.contains(q.prev(i)))
		    q = q.remove(i);

	    // make sure new quad is marked as reachable.
	    reachable.add(q);
	}
    }
}
