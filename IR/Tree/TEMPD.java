// TEMPD.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Temp;

/**
 * <code>TEMPD</code> objects are expressions which stand for the 
 * 64-bit floating-point value in a virtual register.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: TEMPD.java,v 1.1.2.2 1999-01-15 17:56:41 duncan Exp $
 */
public class TEMPD extends TEMP {
    /** Constructor. */
    public TEMPD(Temp temp) { super(temp); }

    public boolean isDoubleWord() { return true; }
    public boolean isFloatingPoint() { return true; }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

