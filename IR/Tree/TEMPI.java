// TEMPI.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Temp;

/**
 * <code>TEMPI</code> objects are expressions which stand for the 
 * 32-bit integer value in a virtual register.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: TEMPI.java,v 1.1.2.1 1999-01-15 01:17:37 cananian Exp $
 */
public class TEMPI extends TEMP {
    /** Constructor. */
    public TEMPI(Temp temp) { super(temp); }

    public boolean isDoubleWord() { return false; }
    public boolean isFloatingPoint() { return false; }
}

