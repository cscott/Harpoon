// Edge.java, created Wed Sep  9 20:53:22 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>Edge</code> objects connect <code>Quad</code> nodes in the
 * control-flow graph.  The <code>hashCode</code> and <code>equals</code>
 * methods of <code>Edge</code> have been implemented so that 
 * <code>Edges</code> can be used as hash table keys to associate analysis
 * data with control-flow edges.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Edge.java,v 1.1 1998-09-10 01:38:22 cananian Exp $
 */

public class Edge  {
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
    public Quad from() { return from; }
    /** Returns the destination vertex of this Edge. */
    public Quad to() { return to; }
    /** Returns the predecessor index of this Edge in <code>to</code>.
     *  <code>this.to().prevEdge(this.which_pred()) == this</code>. */
    public int which_pred() { return to_index; }
    /** Returns the successor index of this Edge in <code>from</code>.
     *  <code>this.from().nextEdge(this.which_succ()) == this</code>. */
    public int which_succ() { return from_index; }

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
	return (from.hashCode() ^ to.hashCode()) + from_index + to_index;
    }
}
