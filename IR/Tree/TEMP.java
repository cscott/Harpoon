// TEMP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * <code>TEMP</code> objects are expressions which stand for a
 * value in a virtual register.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: TEMP.java,v 1.1.2.5 1999-02-05 10:40:45 cananian Exp $
 */
public class TEMP extends Exp implements Typed {
    /** The <code>Temp</code> which this <code>TEMP</code> refers to. */
    public final Temp temp;
    /** The type of this <code>Temp</code> expression. */
    public final int type;
    /** Constructor. */
    protected TEMP(int type, Temp temp) {
	this.type=type; this.temp=temp;
	Util.assert(type==INT||type==FLOAT||type==LONG||type==DOUBLE||
		    type==POINTER);
    }
    public ExpList kids() {return null;}
    public Exp build(ExpList kids) {return this;}

    // Typed interface:
    public int type() { return type; }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

