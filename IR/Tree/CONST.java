// CONST.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HDataElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

/**
 * <code>CONST</code> objects are expressions which stand for a constant
 * value.  <code>CONST</code>s should be used to represent numeric types 
 * only.  The one notable exception is the constant <code>null</code>,
 * which is represented by a <code>CONST</code> with <code>value == null</code>
 * and <code>type == POINTER</code>. 
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: CONST.java,v 1.1.2.19 1999-09-08 21:33:08 cananian Exp $
 */
public class CONST extends Exp implements PreciselyTyped, HDataElement {
    /** The constant value of this <code>CONST</code> expression. */
    public final Number value;
    /** The type of this <code>CONST</code> expression. */
    public final int type;

    // Force access through the PreciselyTyped interface, so we can assert
    // that type==SMALL.  
    private int bitwidth   = -1;
    private boolean signed = true;

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
    /** Creates a <code>CONST</code> representing the constant 
     * <code>null</code>. */
    public CONST(TreeFactory tf, HCodeElement source) { 
	super(tf, source);
	this.type  = POINTER; 
	this.value = null;
    }

    /** Creates a CONST with a precisely defined type.  
     *  For example, <code>CONST(tf, src, 8, true, 0xFF)</code> corresponds
     *  to the value <code>-1</code>, as a byte.  
     *  <code>CONST(tf, src, 8, true, -1)</code> also corresponds
     *  to the value <code>-1</code> as a byte.
     *  @param bitwidth    the width in bits of this <code>CONST</code>'s type.
     *                     Fails unless <code>0 <= bitwidth < 32</code>.
     *  @param signed      whether this <code>CONST</code> is signed
     *  @param val         the value of this <code>CONST</code>.  
     *                     Note that only the lower <code>bitwidth</code> bits
     *                     of this parameter will actually be used.  
     */
    public CONST(TreeFactory tf, HCodeElement source, 
		 int bitwidth, boolean signed, int val) { 
	super(tf, source);
	Util.assert((0<=bitwidth)&&(bitwidth<32), "Invalid bitwidth");
	this.type     = SMALL;
	this.bitwidth = bitwidth;
	this.signed   = signed;
	this.value    = new Integer(val & ((~0) >> (32-bitwidth)));
    }

    private CONST(TreeFactory tf, HCodeElement source, 
		  int type, Number value, int bitwidth, boolean signed) {
        super(tf, source);
	this.type = type; this.value = value;
	this.bitwidth = bitwidth; this.signed = signed;
	Util.assert(PreciseType.isValid(type));
    }
    
    /** Return the constant value of this <code>CONST</code> expression. */
    public Number value() { return value; }

    public ExpList kids() {return null;}

    public int kind() { return TreeKind.CONST; }

    public Exp build(ExpList kids) { return build(tf, kids); }

    public Exp build(TreeFactory tf, ExpList kids) {
	return new CONST(tf, this, type, value, bitwidth, signed);
    }

    // Typed interface.
    public int type() { Util.assert(PreciseType.isValid(type)); return type; }

    // PreciselyTyped interface.
    /** Returns the size of the expression, in bits.
     *  Only valid if the type of the expression is <code>SMALL</code>. */
    public int bitwidth() { Util.assert(type==SMALL); return bitwidth; } 
    /** Returns true if this is a signed expression, false otherwise.
     *  Only valid if the type of the expression is <code>SMALL</code>. */
    public boolean signed() { Util.assert(type==SMALL); return signed; } 
    
    /** Accept a visitor */
    public void visit(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) {
        return new CONST(tf, this, type, value, bitwidth, signed);
    }

    public String toString() {
        return "CONST<"+PreciseType.toString(type)+ 
	    ((type==SMALL) ? (":" +  bitwidth + (signed ? "sext" : "")) : "") +
	    ">("+value+")";
    }
}
