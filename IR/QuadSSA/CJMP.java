// CJMP.java, created Wed Aug  5 07:07:32 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>CJMP</code> represents conditional branches.
 * succ[0] is if-false, succ[1] is if-true branch.
 * op is canonical.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CJMP.java,v 1.5 1998-08-24 19:30:00 cananian Exp $
 */

public abstract class CJMP extends Quad {
    /** Equality condition. */
    public static final int EQ = 0;
    /** Greater-than-or-equal-to. */
    public static final int GE = 1;
    /** Greater-than. */
    public static final int GT = 2;
    public int op;
    public Temp left, right;

    /** Creates a <code>CJMP</code>. */
    public CJMP(String sourcefile, int linenumber, 
		int op, Temp left, Temp right) {
        super(sourcefile, linenumber, 1, 2 /* two branch targets */);
	this.op = op;
	this.left = left;
	this.right= right;
    }
    CJMP(HCodeElement hce, int op, Temp left, Temp right) {
	this(hce.getSourceFile(), hce.getLineNumber(), op, left, right);
    }
    /** Swaps if-true and if-false targets. */
    public void invert() {
	Quad q = next[0];
	next[0] = next[1];
	next[1] = q;
    }
    String opString() {
	switch (op) {
	case EQ: return "=";
	case GE: return ">=";
	case GT: return ">";
	default: throw new Error("Illegal op.");
	}
    }
    /** Returns human-readable representation. */
    public String toString() {
	return "CJMP " + left + " " + opString() + " " + right +
	    " then " + next[1].getID() + " else " + next[0].getID();
    }
}
