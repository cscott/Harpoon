// MEM.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>MEM</code> objects are expressions which stand for the contents of
 * a value in memory starting at the address specified by the
 * subexpression.  Note that when <code>MEM</code> is used as the left child
 * of a <code>MOVE</code> or <code>CALL</code>, it means "store," but
 * anywhere else it means "fetch."
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: MEM.java,v 1.1.2.2 1999-01-15 00:19:34 cananian Exp $
 */
public abstract class MEM extends Exp implements Typed {
    /** A subexpression evaluating to a memory reference. */
    public Exp exp;
    /** Constructor. */
    public MEM(Exp exp) { this.exp=exp; }
    public ExpList kids() {return new ExpList(exp,null);}
    public abstract Exp build(ExpList kids);

    // Typed interface:
    public abstract boolean isDoubleWord();
    public abstract boolean isFloatingPoint();
}

