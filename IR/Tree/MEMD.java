// MEMD.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>MEMD</code> objects are expressions which stand for the contents of
 * a 64-bit floating-point value in memory starting at the address specified
 * by the subexpression.  Note that when <code>MEMD</code> is used as the left
 * child of a <code>MOVE</code> or <code>CALL</code>, it means "store," but
 * anywhere else it means "fetch."
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: MEMD.java,v 1.1.2.1 1999-01-15 00:19:34 cananian Exp $
 */
public class MEMD extends MEM {
    /** Constructor. */
    public MEMD(Exp exp) { super(exp); }
    public Exp build(ExpList kids) {
	return new MEMD(kids.head);
    }

    public boolean isDoubleWord() { return true; }
    public boolean isFloatingPoint() { return true; }
}

