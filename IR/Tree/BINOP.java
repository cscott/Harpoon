// BINOP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>BINOP</code> objects are expressions which stand for result of
 * applying some binary operator <i>o</i> to a pair of subexpressions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: BINOP.java,v 1.1.2.2 1999-01-15 17:56:39 duncan Exp $
 */
public class BINOP extends OPER {
    /** The subexpression of the left-hand side of the operator. */
    public Exp left;
    /** The subexpression of the right-hand side of the operator. */
    public Exp right;
    /** Constructor. */
    public BINOP(int binop, Exp left, Exp right) {
	super(binop); this.left=left; this.right=right; 
    }
    // FIXME: define binops.
    public ExpList kids() {return new ExpList(left, new ExpList(right,null));}
    public Exp build(ExpList kids) {
	return new BINOP(op, kids.head, kids.tail.head);
    }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

