// UNOP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>UNOP</code> objects are expressions which stand for result of
 * applying some unary operator <i>o</i> to a subexpression.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: UNOP.java,v 1.1.2.6 1999-02-09 21:54:23 duncan Exp $
 * @see Uop
 */
public class UNOP extends OPER {
    /** The subexpression to be operated upon. */
    public Exp operand;
    /** Constructor. */
    public UNOP(TreeFactory tf, HCodeElement source,
		int optype, int unop, Exp operand) {
	super(tf, source, optype, unop);
	this.operand = operand;
	Util.assert(Uop.isValid(unop));
    }
    // Unops defined in harpoon.IR.Tree.Uop
    public int type() {
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
	    return optype;
	}
    }

    public ExpList kids() { return new ExpList(operand, null); }
    public Exp build(ExpList kids) {
	return new UNOP(tf, this, optype, op, kids.head);
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new UNOP(tf, this, optype, op, (Exp)operand.rename(tf, ctm));
    }
}

