// MEM.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>MEM</code> objects are expressions which stand for the contents of
 * a 32-bit value in memory starting at the address specified by the
 * subexpression.  Note that when <code>MEM</code> is used as the left child
 * of a <code>MOVE</code>, it means "store," but anywhere else it means
 * "fetch."
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: MEM.java,v 1.1.2.1 1999-01-14 05:54:59 cananian Exp $
 */
public class MEM extends Exp {
    /** A subexpression evaluating to a memory reference. */
    public Exp exp;
    /** Constructor. */
    public MEM(Exp exp) { this.exp=exp; }
    public ExpList kids() {return new ExpList(exp,null);}
    public Exp build(ExpList kids) {
	return new MEM(kids.head);
    }
}

