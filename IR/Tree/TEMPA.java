// TEMPA.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Temp;

/**
 * <code>TEMPA</code> objects are expressions which stand for the 
 * address value in a virtual register.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: TEMPA.java,v 1.1.2.2 1999-01-15 17:56:41 duncan Exp $
 */
public class TEMPA extends TEMP {
    private final boolean is64bitarch; // FIXME: make frame-dependent.
    /** Constructor. */
    public TEMPA(Temp temp, boolean is64bitarch) {
	super(temp); this.is64bitarch=is64bitarch;
    }

    public boolean isDoubleWord() { return is64bitarch; }
    public boolean isFloatingPoint() { return false; }
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

