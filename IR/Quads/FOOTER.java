// FOOTER.java, created Mon Sep  7 10:36:08 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>FOOTER</code> nodes are used to anchor the bottom end of the quad
 * graph.  They do not represent bytecode and are not executable. <p>
 * <code>RETURN</code> and <code>THROW</code> nodes should have a 
 * <code>FOOTER</code> node as their only successor.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FOOTER.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 * @see HEADER
 * @see RETURN
 * @see THROW
 */

public class FOOTER extends Quad {
    
    /** Creates a <code>FOOTER</code>. */
    public FOOTER(QuadFactory qf, HCodeElement source, int arity) {
        super(qf, source, arity/*num predecessors*/, 0/*no successors ever*/);
    }
    /** Returns the number of predecessors of this <Code>FOOTER</code>. */
    public int arity() { return prev.length; }

    /** Attach a new Quad to this <code>FOOTER</code> by replacing it.
     * @param q the new <code>Quad</code> to attach.
     * @param which_succ the edge leaving q to attach to the
     *                   <code>FOOTER</code>.
     * @return the new <code>FOOTER</code>. */
    public FOOTER attach(Quad q, int which_succ) {
	FOOTER f = resize(arity()+1);
	addEdge(q, which_succ, f, arity());
	return f;
    }
    /** Change the arity of this <code>FOOTER</code> by replacing it.
     *  The new edges if any are the highest numbered incoming
     *  edges and are left null. */
    public FOOTER resize(int new_arity) {
	FOOTER f = new FOOTER(qf, this, new_arity);
	for (int i=0; i<arity() && i<new_arity; i++)
	    Quad.addEdge(this.prev(i), this.prevEdge(i).which_succ(), f, i);
	return f;
    }

    /** Remove an attachment from this <code>FOOTER</code> by replacing
     *  the footer.
     * @param which_pred which incoming edge to remove.
     * @return the new <code>FOOTER</code>. */
    public FOOTER remove(int which_pred) {
	FOOTER f = new FOOTER(qf, this, arity()-1);
	for (int i=0, j=0; i<prev.length; i++)
	    if (i!=which_pred) {
		Edge e = prevEdge(i);
		Quad.addEdge((Quad)e.from(), e.which_succ(), f, j++);
	    }
	return f;
    }

    public int kind() { return QuadKind.FOOTER; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new FOOTER(qqf, this, prev.length);
    }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this <code>Quad</code>. */
    public String toString() { return "FOOTER("+arity()+")"; }
}
