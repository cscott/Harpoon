// UNOP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>UNOP</code> objects are expressions which stand for result of
 * applying some unary operator <i>o</i> to a subexpression.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: UNOP.java,v 1.1.2.1 1999-01-14 05:55:00 cananian Exp $
 */
public class UNOP extends OPER {
    /** The subexpression to be operated upon. */
    public Exp operand;
    /** Constructor. */
    public UNOP(int unop, Exp operand) {
	super(unop); this.operand = operand;
    }
    // FIXME: define unops.
    public ExpList kids() { return new ExpList(operand, null); }
    public Exp build(ExpList kids) {
	return new UNOP(op, kids.head);
    }
}

