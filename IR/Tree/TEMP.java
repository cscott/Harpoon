// TEMP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Temp;

/**
 * <code>TEMP</code> objects are expressions which stand for a
 * value in a virtual register.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: TEMP.java,v 1.1.2.4 1999-01-15 17:56:41 duncan Exp $
 */
public abstract class TEMP extends Exp implements Typed {
    /** The <code>Temp</code> which this <code>TEMP</code> refers to. */
    public final Temp temp;
    /** Constructor. */
    protected TEMP(Temp temp) { this.temp=temp; }
    public ExpList kids() {return null;}
    public Exp build(ExpList kids) {return this;}

    // Typed interface:
    public abstract boolean isDoubleWord();
    public abstract boolean isFloatingPoint();
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

