// CONST.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>CONST</code> objects are expressions which stand for a constant
 * value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CONST.java,v 1.1.2.10 1999-06-28 18:49:16 duncan Exp $
 */
public class CONST extends Exp implements Typed {
    /** The constant value of this <code>CONST</code> expression. */
    public final Number value;
    /** The type of this <code>CONST</code> expression. */
    public final int type;

    public CONST(TreeFactory tf, HCodeElement source, int ival) {
	super(tf, source);
	this.type = INT; this.value = new Integer(ival);
    }
    public CONST(TreeFactory tf, HCodeElement source, long lval) {
	super(tf, source);
	this.type = LONG; this.value = new Long(lval);
    }
    public CONST(TreeFactory tf, HCodeElement source, float fval) {
	super(tf, source);
	this.type = FLOAT; this.value = new Float(fval);
    }
    public CONST(TreeFactory tf, HCodeElement source, double dval) {
	super(tf, source);
	this.type = DOUBLE; this.value = new Double(dval);
    }
    private CONST(TreeFactory tf, HCodeElement source, 
		  int type, Number value) {
        super(tf, source);
	this.type = type; this.value = value;
    }
    
    
    /** Return the constant value of this <code>CONST</code> expression. */
    public Number value() { return value; }

    public ExpList kids() {return null;}

    public int kind() { return TreeKind.CONST; }

    public Exp build(ExpList kids) {return this;}

    // Typed interface.
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
        return new CONST(tf, this, type, value);
    }

    public String toString() {
        return "CONST<"+Type.toString(type)+">("+value+")";
    }
}
