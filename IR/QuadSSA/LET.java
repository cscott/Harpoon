// LET.java, created Wed Aug  5 06:53:38 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>LET</code> objects represent an assignment to a compiler temporary.
 * The source of the assignment must be another temporary.
 * It is roughly equivalent to the standard <code>MOVE</code>
 * pseudo-instruction.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LET.java,v 1.8 1998-08-24 21:07:59 cananian Exp $
 */

public class LET extends OPER {
    /** Creates a <code>LET</code> from a source and destination Temporary. */
    public LET(String sourcefile, int linenumber,
	       Temp dst, Temp src) {
	super(sourcefile, linenumber, "move", dst, new Temp[] { src });
    }
    LET(HCodeElement hce, Temp dst, Temp src) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, src);
    }
    /** Returns a human-readable representation. */
    public String toString() { 
	return "LET " + dst + " <= " + operands[0];
    }
}
