// MOVE.java, created Wed Aug  5 06:53:38 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;

/**
 * <code>MOVE</code> objects represent an assignment to a compiler temporary.
 * The source of the assignment must be another temporary.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MOVE.java,v 1.2 1998-09-03 06:14:00 cananian Exp $
 */

public class MOVE extends OPER {
    /** Creates a <code>MOVE</code> from a source and destination Temporary. */
    public MOVE(String sourcefile, int linenumber,
	       Temp dst, Temp src) {
	super(sourcefile, linenumber, "move", dst, new Temp[] { src });
    }
    MOVE(HCodeElement hce, Temp dst, Temp src) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, src);
    }
    /** Returns a human-readable representation. */
    public String toString() { 
	return dst.toString() + " = MOVE " + operands[0].toString();
    }
}
