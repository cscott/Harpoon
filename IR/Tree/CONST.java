// CONST.java, created Wed Jan 13 21:14:57 1999 by cananian
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;

/**
 * <code>CONST</code> objects are expressions which stand for a constant
 * value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CONST.java,v 1.1.2.5 1999-02-05 11:48:46 cananian Exp $
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

    /** Return the constant value of this <code>CONST</code> expression. */
    public Number value() { return value; }

    public ExpList kids() {return null;}
    public Exp build(ExpList kids) {return this;}

    // Typed interface.
    public int type() { return type; }

    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }
}

