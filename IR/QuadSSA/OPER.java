// OPER.java, created Wed Aug  5 06:47:58 1998
package harpoon.IR.QuadSSA;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Hashtable;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.ConstMap;
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
 * @version $Id: OPER.java,v 1.13 1998-09-11 18:23:17 cananian Exp $
 */

public class OPER extends Quad {
    /** The temp in which to store the result of the operation. */
    public Temp dst;
    /** The operation to be performed, as a string. */
    public String opcode;
    /** Operands of the operation, in left-to-right order. */
    public Temp[] operands;
    /** Creates a <code>OPER</code>. */
    public OPER(HCodeElement source,
		String opcode, Temp dst, Temp[] operands) {
	super(source);
	this.opcode = opcode;
	this.dst = dst;
	this.operands = operands;
    }

    /** Returns the Temps used by this OPER. */
    public Temp[] use() { return (Temp[]) operands.clone(); }
    /** Returns the Temps defined by this OPER. */
    public Temp[] def() { return new Temp[] { dst }; }

    public void visit(Visitor v) { v.visit(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer(dst.toString());
	sb.append(" = OPER " + opcode + "(");
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
	Method m = (Method) operMethods.get(opcode);
	Util.assert(m!=null);
	return HClass.forClass(m.getReturnType());
    }
    /** Evaluates a constant value for the result of an <code>OPER</code>, 
     *  given constant values for the operands. */
    public Object evalValue(Object[] opValues) {
	Method m = (Method) operMethods.get(opcode);
	Util.assert(m!=null);
	try {
	    return m.invoke(null, opValues);
	} catch (InvocationTargetException e) {
	    throw new Error("OPER evaluation threw "+e.getTargetException());
	} catch (IllegalAccessException e) {
	    Util.assert(false); return null;
	}
    }
    /** Evaluates a value for the result of an <code>OPER</code>, given
     *  a mapping from the operand <code>Temp</code>s to constant
     *  values. */
    public Object evalValue(HMethod m, ConstMap cm) {
	Object[] args = new Object[operands.length];
	for (int i=0; i<operands.length; i++) {
	    Util.assert(cm.isConst(m, operands[i]));
	    args[i] = cm.constMap(m, operands[i]);
	}
	return evalValue(args);
    }

    // private stuff.
    static private Hashtable operMethods = new Hashtable();
    static {
	Method[] m = Eval.class.getDeclaredMethods();
	for (int i=0; i<m.length; i++)
	    operMethods.put(m[i].getName(), m[i]);
    }

}
