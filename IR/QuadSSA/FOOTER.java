// FOOTER.java, created Mon Sep  7 10:36:08 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Util.Util;
/**
 * <code>FOOTER</code> nodes are used to anchor the bottom end of the quad
 * graph.  They do not represent bytecode and are not executable. <p>
 * <code>RETURN</code> and <code>THROW</code> nodes should have a 
 * <code>FOOTER</code> node as their only successor.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FOOTER.java,v 1.4 1998-09-11 17:13:57 cananian Exp $
 * @see HEADER
 * @see RETURN
 * @see THROW
 */

public class FOOTER extends Quad {
    
    /** Creates a <code>FOOTER</code>. */
    public FOOTER(HCodeElement source) {
        super(source, 0/*no predecessors initially*/, 0/*no successors ever*/);
    }

    /** Grow the arity of a FOOTER by one. */
    void grow() {
	Edge[] nprev = new Edge[prev.length + 1];
	System.arraycopy(prev, 0, nprev, 0, prev.length);
	nprev[prev.length] = null;
	prev = nprev;
    }
    /** Attach a new Quad to this FOOTER. */
    public void attach(Quad q, int which_succ) {
	grow(); 
	addEdge(q, which_succ, this, prev.length-1);
    }

    public void accept(Visitor v) { v.visit(this); }

    /** Returns human-readable representation of this Quad. */
    public String toString() { return "FOOTER("+prev.length+")"; }
}
