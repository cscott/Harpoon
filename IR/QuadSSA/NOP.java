// NOP.java, created Tue Aug 25 03:01:12 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>NOP</code> nodes do nothing.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NOP.java,v 1.5 1998-09-11 18:28:23 cananian Exp $
 */

public class NOP extends Quad {
    
    /** Creates a <code>NOP</code>. */
    public NOP(HCodeElement source) {
        super(source);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation. */
    public String toString() { return "NOP"; }
}
