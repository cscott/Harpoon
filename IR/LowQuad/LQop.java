// LQop.java, created Wed Jan 20 23:28:15 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HClass;
/**
 * <code>LQop</code> is an enumerated type for the various kinds of
 * <code>OPER</code> opcodes in <code>LowQuad</code> form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LQop.java,v 1.2 2002-02-25 21:04:38 cananian Exp $
 */
public abstract class LQop extends harpoon.IR.Quads.Qop {
    
    /** Evaluates to <code>true</code> if the two <code>POINTER</code>
     *  values are equal. */
    public final static int PCMPEQ = 100;
    /** Evaluates to <code>true</code> if the first <code>POINTER</code>
     *  value is greater than the second <code>POINTER</code> value. */
    public final static int PCMPGT = 101;
    /** Computes the sum of two <code>POINTER</code> values. */
    public final static int PADD = 102;
    /** Negates a <code>POINTER</code> value (used to compute differences). */
    public final static int PNEG = 103;

    public static boolean isValid(int v) {
	return harpoon.IR.Quads.Qop.isValid(v) ||
	    ((100<=v) && (v<=103));
    }

    public static String toString(int v) {
	switch(v) {
	case PCMPEQ: return "pcmpeq";
	case PCMPGT: return "pcmpgt";
	case PADD:   return "padd";
	case PNEG:   return "pneg";
	default:     return harpoon.IR.Quads.Qop.toString(v);
	}
    }
    public static int forString(String op) {
	Integer r = (Integer) h.get(op);
	if (r==null) return harpoon.IR.Quads.Qop.forString(op);
	else return r.intValue();
    }
    private static final java.util.Hashtable h = new java.util.Hashtable();
    static {
	h.put("pcmpeq", new Integer(PCMPEQ));
	h.put("pcmpgt", new Integer(PCMPGT));
	h.put("padd", new Integer(PADD));
	h.put("pneg", new Integer(PNEG));
    }

    /** Determines the result type of an <code>OPER</code>. */
    public static HClass resultType(int v) {
	switch(v) {
	case PCMPEQ:
	case PCMPGT:
	    return HClass.Boolean;
	case PADD:
	case PNEG:
	    throw new Error("I've no idea what to return in this case.");
	default:
	    return harpoon.IR.Quads.Qop.resultType(v);
	}
    }
    /** Evaluates a constant value for the result of an <code>OPER</code>,
     *  given constant values for the operands. */
    public static Object evaluation(int opc, Object[] opd) {
	switch(opc) {
	case PCMPEQ:
	case PCMPGT:
	case PADD:
	case PNEG:
	    throw new Error("No idea how to handle these.");
	default:
	    return harpoon.IR.Quads.Qop.evaluate(opc, opd);
	}
    }
}


