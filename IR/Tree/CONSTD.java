// CONSTD.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>CONSTD</code> objects are expressions which stand for a constant
 * 64-bit floating-point value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CONSTD.java,v 1.1.2.1 1999-01-15 00:50:27 cananian Exp $
 */
public class CONSTD extends CONST {
    /** The constant value of this <code>CONSTD</code> expression. */
    public final double value;
    /** Constructor. */
    public CONSTD(double value) { this.value = value; }

    public Number value() { return new Double(value); }

    public boolean isDoubleWord() { return true; }
    public boolean isFloatingPoint() { return true; }
}

