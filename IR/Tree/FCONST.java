// FCONST.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>FCONST</code> objects are expressions which stand for a constant
 * 32-bit floating-point value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: FCONST.java,v 1.1.2.1 1999-01-14 05:54:59 cananian Exp $
 */
public class FCONST extends CONST {
    /** The constant value of this <code>FCONST</code> expression. */
    public final float value;
    /** Constructor. */
    public FCONST(float value) { this.value = value; }

    public Number value() { return new Float(value); }
}

