// LCONST.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

/**
 * <code>LCONST</code> objects are expressions which stand for a constant
 * 64-bit integer value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: LCONST.java,v 1.1.2.1 1999-01-14 05:54:59 cananian Exp $
 */
public class LCONST extends CONST {
    /** The constant value of this <code>LCONST</code> expression. */
    public final long value;
    /** Constructor. */
    public LCONST(long value) { this.value = value; }

    public Number value() { return new Long(value); }
}

