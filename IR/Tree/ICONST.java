// ICONST.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>ICONST</code> objects are expressions which stand for a constant
 * 32-bit integer value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: ICONST.java,v 1.1.2.1 1999-01-14 05:54:59 cananian Exp $
 */
public class ICONST extends CONST {
    /** The constant value of this <code>ICONST</code> expression. */
    public final int value;
    /** Constructor. */
    public ICONST(int value) { this.value = value; }

    public Number value() { return new Integer(value); }
}

