// MEMI.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>MEMI</code> objects are expressions which stand for the contents of
 * a 32-bit integer value in memory starting at the address specified by the
 * subexpression.  Note that when <code>MEMI</code> is used as the left child
 * of a <code>MOVE</code> or <code>CALL</code>, it means "store," but
 * anywhere else it means "fetch."
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: MEMI.java,v 1.1.2.2 1999-01-15 17:56:40 duncan Exp $
 */
public class MEMI extends MEM {
    /** Constructor. */
    public MEMI(Exp exp) { super(exp); }
    public Exp build(ExpList kids) {
	return new MEMI(kids.head);
    }

    public boolean isDoubleWord() { return false; }
    public boolean isFloatingPoint() { return false; }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

