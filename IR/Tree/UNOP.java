// UNOP.java, created Wed Jan 13 21:14:57 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>UNOP</code> objects are expressions which stand for result of
 * applying some unary operator <i>o</i> to a subexpression.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>, based on
 *          <i>Modern Compiler Implementation in Java</i> by Andrew Appel.
 * @version $Id: UNOP.java,v 1.3 2002-02-26 22:46:11 cananian Exp $
 * @see Uop
 */
public class UNOP extends OPER {
    /** Constructor.
     * @param unop Enumerated operation type, from <code>Uop</code>.
     */
    public UNOP(TreeFactory tf, HCodeElement source,
		int optype, int unop, Exp operand) {
	super(tf, source, optype, unop, 1);
	Util.ASSERT(operand!=null);
	this.setOperand(operand);
	Util.ASSERT(Uop.isValid(unop));
	Util.ASSERT(tf == operand.tf, "This and Operand must have same tree factory");
	if (unop==Uop.I2B || unop==Uop.I2C || unop==Uop.I2S)
	    Util.ASSERT(optype == Type.INT);/* these are special conversions */
	Util.ASSERT(operand.type()==optype, "operand and optype don't match");
    }

    /** Returns the subexpression to be operated upon. */
    public Exp getOperand() { return (Exp) getChild(0); }

    /** Sets the subexpression to be operated upon. */
    public void setOperand(Exp operand) { setChild(0, operand); }

    /** Returns an <code>int</code> identifying the TYPE that this
	unary operation returns.
	@see harpoon.IR.Tree.Uop 
    */
    public int type() {
	switch (op) {
	case Uop.I2B: case Uop.I2C: case Uop.I2S: case Uop._2I:
	    return INT;
	case Uop._2L:
	    return LONG;
	case Uop._2F:
	    return FLOAT;
	case Uop._2D:
	    return DOUBLE;
	default:
	    return optype;
	}
    }


    public static Object evalValue(TreeFactory tf, int op, 
				 int optype, Object left) {
	switch(op) {
	case Uop.NEG:
	    switch (optype) {
	    case Type.INT:      return _i(-(_i(left)));
	    case Type.LONG:     return _l(-(_l(left)));
	    case Type.FLOAT:    return _f(-(_f(left)));
	    case Type.DOUBLE:   return _d(-(_d(left)));
	    case Type.POINTER: 
		return Type.isDoubleWord(tf, optype) ?
		    (Object)_i(-(_i(left))) : (Object)_l(-(_l(left)));
	    }
	case Uop.NOT:
	    switch (optype) {
	    case Type.INT:      return _i(~(_i(left)));
	    case Type.LONG:     return _l(~(_l(left)));
	    case Type.FLOAT:
	    case Type.DOUBLE:
	    case Type.POINTER:
		throw new Error("Operation not supported");
	    }
	case Uop.I2B:
	    switch (optype) {
	    case Type.INT:      return _i((byte)_i(left));
	    default:
		throw new Error("Operation not supported");
	    }
	case Uop.I2C:
	    switch (optype) {
	    case Type.INT:      return _i((char)_i(left));
	    default:
		throw new Error("Operation not supported");
	    }
	case Uop.I2S: 
	    switch (optype) {
	    case Type.INT:      return _i((short)_i(left));
	    default:
		throw new Error("Operation not supported");
	    }
	case Uop._2I:
	    switch (optype) {
	    case Type.INT:      return left;
	    case Type.LONG:     return _i((int)_l(left));
	    case Type.FLOAT:    return _i((int)_f(left));
	    case Type.DOUBLE:   return _i((int)_d(left));
	    case Type.POINTER: 
		if (Type.isDoubleWord(tf, optype))
		    throw new Error("Operation not supported");
		else return _i((int)_i(left));
	    }
	case Uop._2L:
	    switch (optype) {
	    case Type.INT:      return _l((long)_i(left));
	    case Type.LONG:     return left;
	    case Type.FLOAT:    return _l((long)_f(left));
	    case Type.DOUBLE:   return _l((long)_d(left));
	    case Type.POINTER:  
		if (Type.isDoubleWord(tf, optype)) return left;
		else return _l((long)_i(left));
	    }
	case Uop._2F:
	    switch (optype) {
	    case Type.INT:      return _f((float)_i(left));
	    case Type.LONG:     return _f((float)_l(left));
	    case Type.FLOAT:    return left;
	    case Type.DOUBLE:   return _f((float)_d(left));
	    case Type.POINTER:  
		if (Type.isDoubleWord(tf, optype)) 
		    throw new Error("Operation not supported");
		else return _f((float)_i(left));
	    }
	case Uop._2D:
	    switch (optype) {
	    case Type.INT:      return _d((double)_i(left));
	    case Type.LONG:     return _d((double)_l(left));
	    case Type.FLOAT:    return _d((double)_f(left));
	    case Type.DOUBLE:   return left;
	    case Type.POINTER:  
		if (Type.isDoubleWord(tf, optype)) return left;
		else return _d((double)_i(left));
	    }
	default:
	    throw new Error("Unrecognized Uop");
	}
    }

    // wrapper functions.
    private static Byte    _b(byte b)    { return new Byte(b);    }  
    private static Integer _i(int i)     { return new Integer(i); }
    private static Long    _l(long l)    { return new Long(l);    }
    private static Float   _f(float f)   { return new Float(f);   }
    private static Double  _d(double d)  { return new Double(d);  }

    // unwrapper functions.
    private static byte   _b(Object o) { return ((Byte)o)   .byteValue(); } 
    private static int    _i(Object o) { return ((Integer)o).intValue(); }
    private static long   _l(Object o) { return ((Long)o)   .longValue(); }
    private static float  _f(Object o) { return ((Float)o)  .floatValue(); }
    private static double _d(Object o) { return ((Double)o) .doubleValue(); }

    public int kind() { return TreeKind.UNOP; }

    public Exp build(TreeFactory tf, ExpList kids) {
	Util.ASSERT(kids!=null && kids.tail==null);
	Util.ASSERT(tf == kids.head.tf);
	return new UNOP(tf, this, optype, op, kids.head);
    }
    /** Accept a visitor */
    public void accept(TreeVisitor v) { v.visit(this); }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
        return cb.callback(this,
			   new UNOP(tf, this, optype, op,
				    (Exp)getOperand().rename(tf, tm, cb)),
			   tm);
    }

    public String toString() {
        return "UNOP<" + Type.toString(optype) + ">(" + Uop.toString(op) +
                ", #" + getOperand().getID() + ")";
    }
}

