// HEADER.java, created Fri Aug  7 15:19:12 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>HEADER</code> nodes are used to anchor the top end of the
 * quad graph.  They do not represent bytecode.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HEADER.java,v 1.3 1998-09-03 01:21:56 cananian Exp $
 */

public class HEADER extends Quad {
    /** Creates a <code>HEADER</code>. */
    public HEADER() {
        super("---internal---",0, 0 /* no predecessors */, 1);
    }
    /** Returns human-readable representation. */
    public String toString() { return "HEADER"; }
}
