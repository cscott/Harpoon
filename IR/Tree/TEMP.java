// TEMP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Temp;

/**
 * <code>TEMP</code> objects are expressions which stand for the
 * 32-bit value in a virtual register.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: TEMP.java,v 1.1.2.2 1999-01-14 06:04:58 cananian Exp $
 */
public class TEMP extends Exp {
    /** The <code>Temp</code> which this <code>TEMP</code> refers to. */
    public final Temp temp;
    /** Constructor. */
    public TEMP(Temp temp) { this.temp=temp; }
    public ExpList kids() {return null;}
    public Exp build(ExpList kids) {return this;}
}

