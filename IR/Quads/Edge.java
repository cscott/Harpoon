// Edge.java, created Wed Sep  9 20:53:22 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Util.ArrayFactory;
/**
 * <code>Edge</code> objects connect <code>Quad</code> nodes in the
 * control-flow graph.  The <code>hashCode</code> and <code>equals</code>
 * methods of <code>Edge</code> have been implemented so that 
 * <code>Edges</code> can be used as hash table keys to associate analysis
 * data with control-flow edges.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Edge.java,v 1.1.2.1 1998-12-01 12:36:42 cananian Exp $
 */

public class Edge implements HCodeEdge {
    Quad from, to;
    int from_index, to_index;
    
    /** Creates a <code>Edge</code>. */
    Edge(Quad from, int from_index, Quad to, int to_index) {
	this.from = from;
	this.from_index = from_index;
	this.to = to;
	this.to_index = to_index;
    }

    /** Returns the source vertex of this Edge. */
    public HCodeElement from() { return from; }
    /** Returns the destination vertex of this Edge. */
    public HCodeElement to() { return to; }
    /** Returns the predecessor index of this Edge in <code>to</code>.
     *  <code>this.to().prevEdge(this.which_pred()) == this</code>. */
    public int which_pred() { return to_index; }
    /** Returns the successor index of this Edge in <code>from</code>.
     *  <code>this.from().nextEdge(this.which_succ()) == this</code>. */
    public int which_succ() { return from_index; }

    /** Array factory: returns new <code>Edge[]</code>. */
    public static final ArrayFactory arrayFactory =
	new ArrayFactory() {
	    public Object[] newArray(int len) { return new Edge[len]; }
	};
    
    /** Compares two Edges for equality. */
    public boolean equals(Object obj) {
	if (obj instanceof Edge) {
	    Edge e = (Edge) obj;
	    if (e.from.equals(from) && e.to.equals(to) &&
		e.from_index == from_index && e.to_index == to_index)
		return true;
	}
	return false;
    }
    /** Returns a hash code value for this object. */
    public int hashCode() {
	// hashcode is independent of to_index so we
	// can remove inputs to phis without screwing up an edge mapping.
	// exit branches usually carry meaning, thus from_index *is*
	// included in the hashcode.
	return (from.hashCode() ^ to.hashCode()) + from_index;
    }

    /** Returns a human-readable representation of the Edge. */
    public String toString() {
	return "Edge " +
	    "from (#"+from().getID()+","+which_succ()+") " +
	    "to (#"+to().getID()+","+which_pred()+")";
    }
}
