// MEMF.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>MEMF</code> objects are expressions which stand for the contents of
 * a 32-bit floating-point value in memory starting at the address specified
 * by the subexpression.  Note that when <code>MEMF</code> is used as the left
 * child of a <code>MOVE</code> or <code>CALL</code>, it means "store," but
 * anywhere else it means "fetch."
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: MEMF.java,v 1.1.2.1 1999-01-15 00:19:34 cananian Exp $
 */
public class MEMF extends MEM {
    /** Constructor. */
    public MEMF(Exp exp) { super(exp); }
    public Exp build(ExpList kids) {
	return new MEMF(kids.head);
    }

    public boolean isDoubleWord() { return false; }
    public boolean isFloatingPoint() { return true; }
}

