// CONSTF.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>CONSTF</code> objects are expressions which stand for a constant
 * 32-bit floating-point value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CONSTF.java,v 1.1.2.2 1999-01-15 17:56:40 duncan Exp $
 */
public class CONSTF extends CONST {
    /** The constant value of this <code>CONSTF</code> expression. */
    public final float value;
    /** Constructor. */
    public CONSTF(float value) { this.value = value; }

    public Number value() { return new Float(value); }

    public boolean isDoubleWord() { return false; }
    public boolean isFloatingPoint() { return true; }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

