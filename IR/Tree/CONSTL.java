// CONSTL.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>CONSTL</code> objects are expressions which stand for a constant
 * 64-bit integer value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CONSTL.java,v 1.1.2.1 1999-01-15 00:50:28 cananian Exp $
 */
public class CONSTL extends CONST {
    /** The constant value of this <code>CONSTL</code> expression. */
    public final long value;
    /** Constructor. */
    public CONSTL(long value) { this.value = value; }

    public Number value() { return new Long(value); }

    public boolean isDoubleWord() { return true; }
    public boolean isFloatingPoint() { return false; }
}

