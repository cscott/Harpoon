// HEADER.java, created Fri Aug  7 15:19:12 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>HEADER</code> nodes are used to anchor the top end of the
 * quad graph.  They do not represent bytecode.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HEADER.java,v 1.4 1998-09-08 14:38:38 cananian Exp $
 * @see FOOTER
 */

public class HEADER extends Quad {
    /** Creates a <code>HEADER</code>. */
    public HEADER(HCodeElement source) {
        super(source, 0 /* no predecessors */, 1);
    }
    /** Returns human-readable representation of this Quad. */
    public String toString() { return "HEADER"; }
}
