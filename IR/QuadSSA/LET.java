// LET.java, created Wed Aug  5 06:53:38 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>LET</code> objects represent an assignment to a compiler temporary.
 * The source of the assignment can be either a constant or another
 * temporary.  It is roughly equivalent to the standard <code>MOVE</code>
 * pseudo-instruction.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LET.java,v 1.5 1998-08-24 19:30:01 cananian Exp $
 */

public class LET extends Quad {
    public Temp dst; // LeafTemp?
    public Leaf src;
    /** Creates a <code>LET</code> from a Leaf and a Temporary. */
    public LET(String sourcefile, int linenumber,
	       Temp dst, Leaf src) {
	super(sourcefile, linenumber);
	this.dst = dst;
	this.src = src;
    }
    LET(HCodeElement hce, Temp dst, Leaf src) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, src);
    }
    /** Returns a human-readable representation. */
    public String toString() { 
	return "LET " + dst + " <= " + src;
    }
}
