// CJMP.java, created Wed Aug  5 07:07:32 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>CJMP</code> represents conditional branches.<p>
 * <code>next[0]</code> is if-false, which is taken if 
 *                         the operand is equal to zero.
 * <code>next[1]</code> is if-true branch, taken when 
 *                         the operand is not equal to zero.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CJMP.java,v 1.12 1998-09-11 18:23:17 cananian Exp $
 */

public class CJMP extends Quad {
    public Temp test;

    /** Creates a <code>CJMP</code>. */
    public CJMP(HCodeElement source, Temp test) {
        super(source, 1, 2 /* two branch targets */);
	this.test = test;
    }

    /** Swaps if-true and if-false targets. */
    public void invert() {
	Edge iftrue = nextEdge(0);
	Edge iffalse= nextEdge(1);

	Quad.addEdge(this, 0, iffalse.to(), iffalse.which_pred());
	Quad.addEdge(this, 1, iftrue.to(), iftrue.which_pred());
    }
    /** Returns all the Temps used by this Quad.
     * @return the <code>test</code> field.
     */
    public Temp[] use() { return new Temp[] { test }; }

    public void visit(Visitor v) { v.visit(this); }

    /** Returns human-readable representation. */
    public String toString() {
	return "CJMP: if " + test + 
	    " then " + next(1).getID() + 
	    " else " + next(0).getID();
    }
}
