// UNOP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;

/**
 * <code>UNOP</code> objects are expressions which stand for result of
 * applying some unary operator <i>o</i> to a subexpression.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: UNOP.java,v 1.1.2.4 1999-02-05 11:48:57 cananian Exp $
 * @see Uop
 */
public class UNOP extends OPER {
    /** The subexpression to be operated upon. */
    public Exp operand;
    /** Constructor. */
    public UNOP(TreeFactory tf, HCodeElement source,
		int type, int unop, Exp operand) {
	super(tf, source, type, unop);
	this.operand = operand;
	Util.assert(Uop.isValid(unop));
    }
    // Unops defined in harpoon.IR.Tree.Uop
    public int resultType() {
	switch(op) {
	case Uop._2B: case Uop._2C: case Uop._2S: case Uop._2I:
	    return INT;
	case Uop._2L:
	    return LONG;
	case Uop._2F:
	    return FLOAT;
	case Uop._2D:
	    return DOUBLE;
	default:
	    return type();
	}
    }

    public ExpList kids() { return new ExpList(operand, null); }
    public Exp build(ExpList kids) {
	return new UNOP(tf, this, type, op, kids.head);
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

