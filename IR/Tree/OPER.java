// OPER.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;

/**
 * <code>OPER</code> objects are expressions which stand for the result
 * of applying some operator to subexpressions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: OPER.java,v 1.1.2.5 1999-02-05 12:02:46 cananian Exp $
 */
public abstract class OPER extends Exp implements Typed {
    /** An enumerated type encoding the operator.
     * @see Bop
     * @see Uop
     */
    public final int op;
    /** Type of the operands (not necessarily the result type). */
    public final int optype;

    public OPER(TreeFactory tf, HCodeElement source,
		int optype, int op) {
	super(tf, source);
	Util.assert(Type.isValid(optype));
	// subclass must verify validity of op.
	this.op = op; this.optype = optype;
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    // <code>Typed</code> interface.

    /** Return result type. */
    public abstract int type();
    /** Returns <code>true</code> if the expression corresponds to a
     *  64-bit value. */
    public boolean isDoubleWord() { return Type.isDoubleWord(tf, type()); }
    /** Returns <code>true</code> if the expression corresponds to a
     *  floating-point value. */
    public boolean isFloatingPoint() { return Type.isFloatingPoint(type()); }
    /** Return type of operands (not necessarily the result type). */
    public int operandType() { return optype; }
}
