// DCONST.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>DCONST</code> objects are expressions which stand for a constant
 * 64-bit floating-point value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: DCONST.java,v 1.1.2.1 1999-01-14 05:54:58 cananian Exp $
 */
public class DCONST extends CONST {
    /** The constant value of this <code>DCONST</code> expression. */
    public final double value;
    /** Constructor. */
    public DCONST(double value) { this.value = value; }

    public Number value() { return new Double(value); }
}

