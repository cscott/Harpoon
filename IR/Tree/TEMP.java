// TEMP.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * <code>TEMP</code> objects are expressions which stand for a
 * value in a virtual register.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: TEMP.java,v 1.1.2.8 1999-02-09 21:54:23 duncan Exp $
 */
public class TEMP extends Exp implements Typed {
    /** The <code>Temp</code> which this <code>TEMP</code> refers to. */
    public final Temp temp;
    /** The type of this <code>Temp</code> expression. */
    public final int type;
    /** Constructor. */
    protected TEMP(TreeFactory tf, HCodeElement source,
		   int type, Temp temp) {
	super(tf, source);
	this.type=type; this.temp=temp;
	Util.assert(Type.isValid(type) &&
		    temp!=null &&
		    temp.tempFactory() == tf.tempFactory());
    }
    public ExpList kids() {return null;}
    public Exp build(ExpList kids) {return this;}

    // Typed interface:
    public int type() { return type; }
    /** Returns <code>true</code> if the expression corresponds to a
     *  64-bit value. */
    public boolean isDoubleWord() { return Type.isDoubleWord(tf, type); }
    /** Returns <code>true</code> if the expression corresponds to a
     *  floating-point value. */
    public boolean isFloatingPoint() { return Type.isFloatingPoint(type); }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new TEMP(tf, this, this.type, map(ctm, this.temp));
    }
}


