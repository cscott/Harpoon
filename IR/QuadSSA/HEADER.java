// HEADER.java, created Fri Aug  7 15:19:12 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>HEADER</code> nodes are used to anchor the top end of the
 * quad graph.  They do not represent bytecode.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HEADER.java,v 1.1 1998-08-08 00:43:22 cananian Exp $
 */

public class HEADER extends Quad {
    /** Creates a <code>HEADER</code>. */
    public HEADER() {
        super("---internal---",0);
    }
}
