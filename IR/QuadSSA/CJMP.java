// CJMP.java, created Wed Aug  5 07:07:32 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>CJMP</code> represents conditional branches.
 * succ[0] is if-false, which is taken if the operand is equal to zero.
 * succ[1] is if-true branch, taken when the operand is not equal to zero.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CJMP.java,v 1.6 1998-08-26 22:01:39 cananian Exp $
 */

public abstract class CJMP extends Quad {
    public Temp test;

    /** Creates a <code>CJMP</code>. */
    public CJMP(String sourcefile, int linenumber, Temp test) {
        super(sourcefile, linenumber, 1, 2 /* two branch targets */);
	this.test = test;
    }
    CJMP(HCodeElement hce, Temp test) {
	this(hce.getSourceFile(), hce.getLineNumber(), test);
    }
    /** Swaps if-true and if-false targets. */
    public void invert() {
	Quad q = next[0];
	next[0] = next[1];
	next[1] = q;
    }
    /** Returns all the Temps used by this Quad.
     * @return the <code>test</code> field.
     */
    public Temp[] use() { return new Temp[] { test }; }
    /** Returns human-readable representation. */
    public String toString() {
	return "CJMP: if " + test + 
	    " then " + next[1].getID() + 
	    " else " + next[0].getID();
    }
}
