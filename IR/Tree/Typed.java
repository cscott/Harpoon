// Typed.java, created Thu Jan 14 18:59:52 1999 by cananian
package harpoon.IR.Tree;

/**
 * The <code>Typed</code> interface allows access to type information for
 * <code>TEMP</code>, <code>MEM</code>, and <code>CONST</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Typed.java,v 1.1.2.1 1999-01-15 00:19:34 cananian Exp $
 */
public interface Typed  {
    /** Returns <code>true</code> if the expression corresponds to a 64-bit
     *  value. */
    public boolean isDoubleWord();
    /** Returns <code>true</code> if the expressions corresponds to a
     *  floating-point value. */
    public boolean isFloatingPoint();
}
