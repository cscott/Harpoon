// CONSTI.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>CONSTI</code> objects are expressions which stand for a constant
 * 32-bit integer value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CONSTI.java,v 1.1.2.1 1999-01-15 00:50:28 cananian Exp $
 */
public class CONSTI extends CONST {
    /** The constant value of this <code>CONSTI</code> expression. */
    public final int value;
    /** Constructor. */
    public CONSTI(int value) { this.value = value; }

    public Number value() { return new Integer(value); }

    public boolean isDoubleWord() { return false; }
    public boolean isFloatingPoint() { return false; }
}

