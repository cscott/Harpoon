// MOVE.java, created Wed Aug  5 06:53:38 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;

/**
 * <code>MOVE</code> objects represent an assignment to a compiler temporary.
 * The source of the assignment must be another temporary.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MOVE.java,v 1.3 1998-09-08 14:38:38 cananian Exp $
 */

public class MOVE extends OPER {
    /** Creates a <code>MOVE</code> from a source and destination Temporary. */
    public MOVE(HCodeElement source,
	       Temp dst, Temp src) {
	super(source, "move", dst, new Temp[] { src });
    }

    /** Returns a human-readable representation. */
    public String toString() { 
	return dst.toString() + " = MOVE " + operands[0].toString();
    }
}
