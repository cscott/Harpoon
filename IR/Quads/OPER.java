// OPER.java, created Wed Aug  5 06:47:58 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import harpoon.Analysis.Maps.ConstMap;
import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>OPER</code> objects represent arithmetic/logical operations,
 * including mathematical operators such as add and subtract, 
 * conversion operators such as double-to-int, and comparison
 * operators such as greater than and equals.
 * <p>
 * <code>OPER</code> quads never throw exceptions.  Any exception thrown
 * implicitly by the java bytecode opcode corresponding to an OPER is
 * rewritten as an explicit test and throw in the Quad IR.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OPER.java,v 1.1.2.4 1998-12-11 22:21:05 cananian Exp $
 */
public class OPER extends Quad {
    /** The <code>Temp</code> in which to store the result of the operation. */
    protected Temp dst;
    /** The operation to be performed, from the <code>Qop</code> class. */
    final protected int opcode;
    /** Operands of the operation, in left-to-right order. */
    protected Temp[] operands;
    /** Creates a <code>OPER</code>. */
    public OPER(HCodeElement source,
		int opcode, Temp dst, Temp[] operands) {
	super(source);
	this.opcode = opcode;
	this.dst = dst;
	this.operands = operands;
	// VERIFY legality of OPER.
	Util.assert(dst!=null && operands!=null);
	Util.assert(Qop.isValid(opcode));
	for (int i=0; i<operands.length; i++)
	    Util.assert(operands[i]!=null);
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> in which to store the result of the
     *  operation. */
    public Temp dst() { return dst; }
    /** Returns the operation to be performed, as an integer enumeration
     *  from the <code>Qop</code> class. */
    public int  opcode() { return opcode; }
    /** Returns an array of <code>Temp</code>s which are the operands
     *  of the operation. */
    public Temp[] operands()
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, operands); }
    /** Returns a specified element of the <code>operands</code> array. */
    public Temp operands(int i) { return operands[i]; }
    /** Returns the length of the <code>operands</code> array. */
    public int operandsLength() { return operands.length; }

    /** Returns the Temps used by this OPER. */
    public Temp[] use() 
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, operands); }
    /** Returns the Temps defined by this OPER. */
    public Temp[] def() { return new Temp[] { dst }; }

    public int kind() { return QuadKind.OPER; }

    public Quad rename(TempMap tm) {
	return new OPER(this, opcode, map(tm,dst), map(tm,operands));
    }
    /** Rename all used variables in this Quad according to a mapping. */
    void renameUses(TempMap tm) {
	for (int i=0; i<operands.length; i++)
	    operands[i] = tm.tempMap(operands[i]);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    /** Properly clone <code>operands[]</code> array. */
    public Object clone() {
	OPER q = (OPER) super.clone();
	q.operands = (Temp[]) operands.clone();
	return q;
    }

    public void visit(QuadVisitor v) { v.visit(this); }
    public void visit(OperVisitor v) { v.dispatch(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer(dst.toString());
	sb.append(" = OPER " + Qop.toString(opcode) + "(");
	for (int i=0; i<operands.length; i++) {
	    sb.append(operands[i].toString());
	    if (i<operands.length-1)
		sb.append(", ");
	}
	sb.append(')');
	return sb.toString();
    }

    // -------------------------------------------------------
    //   Evaluation functions.

    /** Determines the result type of an <code>OPER</code>. */
    public HClass evalType() {
	Method m = (Method) operMethods.get(Qop.toString(opcode));
	Util.assert(m!=null);
	return HClass.forClass(m.getReturnType());
    }
    /** Evaluates a constant value for the result of an <code>OPER</code>, 
     *  given constant values for the operands. */
    public Object evalValue(Object[] opValues) {
	Method m = (Method) operMethods.get(Qop.toString(opcode));
	Util.assert(m!=null);
	try {
	    return m.invoke(null, opValues);
	} catch (InvocationTargetException e) {
	    throw new Error("OPER evaluation threw "+e.getTargetException());
	} catch (IllegalAccessException e) {
	    Util.assert(false); return null;
	}
    }

    // private stuff.
    static private Hashtable operMethods = new Hashtable();
    static {
	Method[] m = Eval.class.getDeclaredMethods();
	for (int i=0; i<m.length; i++)
	    operMethods.put(m[i].getName(), m[i]);
    }

}
