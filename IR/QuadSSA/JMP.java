// JMP.java, created Wed Aug  5 07:07:11 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>JMP</code> represents unconditional branches.
 * No explicit target needed; the only successor will be the target.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: JMP.java,v 1.5 1998-09-03 01:21:56 cananian Exp $
 */

public class JMP extends Quad {
    /** Creates a <code>JMP</code>. */
    public JMP(String sourcefile, int linenumber) {
        super(sourcefile, linenumber, 1, 1 /* one branch target */);
    }
    JMP(HCodeElement hce) { 
	this(hce.getSourceFile(), hce.getLineNumber());
    }
    /** Returns a human readable representation. */
    public String toString() { return "JMP "+next[0].getID(); }
}
