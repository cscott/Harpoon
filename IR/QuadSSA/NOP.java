// NOP.java, created Tue Aug 25 03:01:12 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>NOP</code> nodes do nothing.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NOP.java,v 1.1 1998-08-25 07:47:15 cananian Exp $
 */

public class NOP extends Quad {
    
    /** Creates a <code>NOP</code>. */
    public NOP(String sourcefile, int linenumber) {
        super(sourcefile, linenumber);
    }
    NOP(HCodeElement hce) {
	this(hce.getSourceFile(), hce.getLineNumber());
    }
    public NOP() {
	this("---internal---", 0);
    }
    /** Returns human-readable representation. */
    public String toString() { return "NOP"; }
}
