// JMP.java, created Wed Aug  5 07:07:11 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>JMP</code> represents unconditional branches.
 * No explicit target needed; the only successor will be the target.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: JMP.java,v 1.3 1998-08-20 22:43:21 cananian Exp $
 */

public class JMP extends Quad {
    /** Creates a <code>JMP</code>. */
    public JMP(String sourcefile, int linenumber) {
        super(sourcefile, linenumber);
    }
    /** Returns a human readable representation. */
    public String toString() { return "JMP "+next[0].getID(); }
}
