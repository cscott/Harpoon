// Qop.java, created Mon Nov  9 22:36:21 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
/**
 * <code>Qop</code> is an enumerated type for the various kinds of
 * <code>OPER</code> opcodes.  The basic rationale is to use a
 * minimal set of opcodes to simplify later analysis.
 * <p>
 * Note that (x - y) is uniformly expressed as (x + (-y)), and that
 * (~x) is typically expressed (compiler-dependent) as (x ^ (-1)).
 * There is also a minimal set of comparison operations; all other
 * comparisons can be synthesized from the ones present.  Note that
 * floating-point (float/double) comparisons have special behaviors
 * when NaN is an operand.  Note also that one each of IAND/IOR and
 * LAND/LOR could be removed, but the translation involved too many
 * steps to be deemed worthwhile.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Qop.java,v 1.1.2.14 2001-06-17 22:33:36 cananian Exp $
 */
public abstract class Qop  {
    /** Compares references for equality. */
    public final static int ACMPEQ = 0;
    /** Converts a double to a float. */
    public final static int D2F = 1;
    /** Converts a double to an int. */
    public final static int D2I = 2;
    /** Converts a double to a long. */
    public final static int D2L = 3;
    /** Computes the sum of two double values. */
    public final static int DADD = 4;
    /** Evaluates to true if double values are equal and neither is NaN, 
     *  or false otherwise. */
    public final static int DCMPEQ = 5;
    /** Evaluates to true if first double value is greater than or
     *  equal to the second double value and neither is NaN, or false
     *  otherwise. */
    public final static int DCMPGE = 6;
    /** Evaluates to true if first double value is greater than the
     *  second double value and neither is NaN, or false otherwise. */
    public final static int DCMPGT = 7;
    /** Computes the quotient of two double values. */
    public final static int DDIV = 8;
    /** Computes the product of two double values. */
    public final static int DMUL = 9;
    /** Computes the negation of a double value. */
    public final static int DNEG = 10;
    /** Computes the remainder of two double values. */
    public final static int DREM = 11;
    /** Converts a float to a double. */
    public final static int F2D = 13;
    /** Converts a float to an int. */
    public final static int F2I = 14;
    /** Converts a float to a long. */
    public final static int F2L = 15;
    /** Computes the sum of two float values. */
    public final static int FADD = 16;
    /** Evaluates to true if the float values are equal and neither is NaN,
     *  or false otherwise. */
    public final static int FCMPEQ = 17;
    /** Evaluates to true if the first float value is greater than or equal
     *  to the second float value and neither is NaN, or false otherwise. */
    public final static int FCMPGE = 18;
    /** Evaluates to true if the first float value is greater than the
     *  second float value and neither is NaN, or false otherwise. */
    public final static int FCMPGT = 19;
    /** Computes the quotient of two float values. */
    public final static int FDIV = 20;
    /** Computes the product of two float values. */
    public final static int FMUL = 21;
    /** Computes the negation of a float value. */
    public final static int FNEG = 22;
    /** Computes the remainder of two float values. */
    public final static int FREM = 23;
    /** Converts an int to a byte.  Result is still int type, but it is
     *  truncated to 8 bits, then sign-extended. */
    public final static int I2B = 25;
    /** Converts an int to a character.  Result is still int type, but is
     *  truncated to 16 bits.  No sign extension. */
    public final static int I2C = 26;
    /** Converts an int to a double. */
    public final static int I2D = 27;
    /** Converts an int to a float. */
    public final static int I2F = 28;
    /** Converts an int to a long. */
    public final static int I2L = 29;
    /** Converts an int to a short.  Result is still int type, but is
     *  truncated to 16 bits, then sign extended. */
    public final static int I2S = 30;
    /** Computes the sum of two int values. */
    public final static int IADD = 31;
    /** Computes the binary-AND of two int values. */
    public final static int IAND = 32;
    /** Evaluates to true if the two int values are equal, or false
     *  otherwise. */
    public final static int ICMPEQ = 33;
    /** Evalutates to true if the first int value is greater than the
     *  second int value, or false otherwise. */
    public final static int ICMPGT = 35;
    /** Computes the int quotient of two int values. */
    public final static int IDIV = 36;
    /** Computes the product of two int values. */
    public final static int IMUL = 37;
    /** Computes the negation of an int value. */
    public final static int INEG = 38;
    /** Computes the binary-OR of two int values. */
    public final static int IOR = 39;
    /** Computes the remainder of two int values. */
    public final static int IREM = 40;
    /** Computes the value of the first int value shifted left by the number
     *  of bits specified in the low five bits of the second int value.
     *  That is, <code>( 1 << 34 ) == 4</code>. 
     *  Also, <code>(x << -1) == (x << 31)</code>.
     */
    public final static int ISHL = 41;
    /** Computes the value of the first int value shifted right with 
     *  sign extension by the number of bits specified in the low five bits
     *  of the second int value.
     *  That is, <code>( -4 >> 33 ) == -2</code>.
     *  Also, <code>(x >> -1) == (x >> 31)</code>.
     */
    public final static int ISHR = 42;
    /** Computes the value of the first int value shifted right without
     *  sign extension by the number of bits specified in the low five bits
     *  of the second int value.
     *  That is, <code>( -4 >>> 30) == 3</code>.
     *  Also, <code>(x >>> -1) == (x >>> 31)</code>.
     */
    public final static int IUSHR = 44;
    /** Computes the binary-XOR of two int values. */
    public final static int IXOR = 45;
    /** Converts a long to a double. */
    public final static int L2D = 46;
    /** Converts a long to a float. */
    public final static int L2F = 47;
    /** Converts a long to an int. */
    public final static int L2I = 48;
    /** Computes the sum of two long values. */
    public final static int LADD = 49;
    /** Computes the binary-AND of two long values. */
    public final static int LAND = 50;
    /** Evaluates to true if the two long values are equal, or 
     *  false otherwise. */
    public final static int LCMPEQ = 51;
    /** Evaluates to true if the first long value is greater than the 
     *  second, or false otherwise. */
    public final static int LCMPGT = 53;
    /** Computes the quotient of two long values. */
    public final static int LDIV = 54;
    /** Computes the product of two long values. */
    public final static int LMUL = 55;
    /** Computes the negation of a long value. */
    public final static int LNEG = 56;
    /** Computes the binary-OR of two long values. */
    public final static int LOR = 57;
    /** Computes the remainder of two long values. */
    public final static int LREM = 58;
    /** Computes the value of the first long value shifted left by the
     *  number of bits specified in the low six bits of the second 
     *  <b>int</b> value.
     *  That is, <code>(1 << 66) == 4</code>.
     *  Also, <code>(x << -1) == (x << 63)</code>.
     */
    public final static int LSHL = 59;
    /** Computes the value of the first long value shifted right with
     *  sign extension by the number of bits specified in the low six bits
     *  of the second <b>int</b> value.
     *  That is, <code>(-4 >> 65) == -2</code>.
     *  Also, <code>(x >> -1) == (x >> 63)</code>.
     */
    public final static int LSHR = 60;
    /** Computes the value of the first long value shifted right without
     *  sign extension by the number of bits specified in the low six bits
     *  of the second <b>int</b> value.
     *  That is, <code>(-4 >>> 62) == 3</code>.
     *  Also, <code>(x >>> -1) == (x >>> 63)</code>.
     */
    public final static int LUSHR = 62;
    /** Computes the binary-XOR of two long values. */
    public final static int LXOR = 63;

    /** Determines if a given <code>Qop</code> value is valid. */
    public static boolean isValid(int v) {
	return (0<=v) && (v<=LXOR);
    }

    /** Converts the enumerated <code>Qop</code> value to a descriptive
     *  string. */
    public static String toString(int v) {
	switch(v) {
        case ACMPEQ:	return "acmpeq";
        case D2F:	return "d2f";
        case D2I:	return "d2i";
        case D2L:	return "d2l";
        case DADD:	return "dadd";
        case DCMPEQ:	return "dcmpeq";
        case DCMPGE:	return "dcmpge";
        case DCMPGT:	return "dcmpgt";
        case DDIV:	return "ddiv";
        case DMUL:	return "dmul";
        case DNEG:	return "dneg";
        case DREM:	return "drem";
        case F2D:	return "f2d";
        case F2I:	return "f2i";
        case F2L:	return "f2l";
        case FADD:	return "fadd";
        case FCMPEQ:	return "fcmpeq";
        case FCMPGE:	return "fcmpge";
        case FCMPGT:	return "fcmpgt";
        case FDIV:	return "fdiv";
        case FMUL:	return "fmul";
        case FNEG:	return "fneg";
        case FREM:	return "frem";
        case I2B:	return "i2b";
        case I2C:	return "i2c";
        case I2D:	return "i2d";
        case I2F:	return "i2f";
        case I2L:	return "i2l";
        case I2S:	return "i2s";
        case IADD:	return "iadd";
        case IAND:	return "iand";
        case ICMPEQ:	return "icmpeq";
        case ICMPGT:	return "icmpgt";
        case IDIV:	return "idiv";
        case IMUL:	return "imul";
        case INEG:	return "ineg";
        case IOR:	return "ior";
        case IREM:	return "irem";
        case ISHL:	return "ishl";
        case ISHR:	return "ishr";
        case IUSHR:	return "iushr";
        case IXOR:	return "ixor";
        case L2D:	return "l2d";
        case L2F:	return "l2f";
        case L2I:	return "l2i";
        case LADD:	return "ladd";
        case LAND:	return "land";
        case LCMPEQ:	return "lcmpeq";
        case LCMPGT:	return "lcmpgt";
        case LDIV:	return "ldiv";
        case LMUL:	return "lmul";
        case LNEG:	return "lneg";
        case LOR:	return "lor";
        case LREM:	return "lrem";
        case LSHL:	return "lshl";
        case LSHR:	return "lshr";
        case LUSHR:	return "lushr";
        case LXOR:	return "lxor";
	default:        throw new RuntimeException("Unknown QOp type: "+v);
	}
    }
    /** Returns the enumerated <code>Qop</code> that corresponds to a given
     *  descriptive string. */
    public static int forString(String op) {
	Integer r = (Integer) h.get(op);
	if (r==null) throw new RuntimeException("Unknown QOp name: "+op);
	return r.intValue();
    }
    private static final java.util.Map h = new java.util.HashMap();
    static {
        h.put("acmpeq", new Integer(ACMPEQ));
        h.put("d2f", new Integer(D2F));
        h.put("d2i", new Integer(D2I));
        h.put("d2l", new Integer(D2L));
        h.put("dadd", new Integer(DADD));
        h.put("dcmpeq", new Integer(DCMPEQ));
        h.put("dcmpge", new Integer(DCMPGE));
        h.put("dcmpgt", new Integer(DCMPGT));
        h.put("ddiv", new Integer(DDIV));
        h.put("dmul", new Integer(DMUL));
        h.put("dneg", new Integer(DNEG));
        h.put("drem", new Integer(DREM));
        h.put("f2d", new Integer(F2D));
        h.put("f2i", new Integer(F2I));
        h.put("f2l", new Integer(F2L));
        h.put("fadd", new Integer(FADD));
        h.put("fcmpeq", new Integer(FCMPEQ));
        h.put("fcmpge", new Integer(FCMPGE));
        h.put("fcmpgt", new Integer(FCMPGT));
        h.put("fdiv", new Integer(FDIV));
        h.put("fmul", new Integer(FMUL));
        h.put("fneg", new Integer(FNEG));
        h.put("frem", new Integer(FREM));
        h.put("i2b", new Integer(I2B));
        h.put("i2c", new Integer(I2C));
        h.put("i2d", new Integer(I2D));
        h.put("i2f", new Integer(I2F));
        h.put("i2l", new Integer(I2L));
        h.put("i2s", new Integer(I2S));
        h.put("iadd", new Integer(IADD));
        h.put("iand", new Integer(IAND));
        h.put("icmpeq", new Integer(ICMPEQ));
        h.put("icmpgt", new Integer(ICMPGT));
        h.put("idiv", new Integer(IDIV));
        h.put("imul", new Integer(IMUL));
        h.put("ineg", new Integer(INEG));
        h.put("ior", new Integer(IOR));
        h.put("irem", new Integer(IREM));
        h.put("ishl", new Integer(ISHL));
        h.put("ishr", new Integer(ISHR));
        h.put("iushr", new Integer(IUSHR));
        h.put("ixor", new Integer(IXOR));
        h.put("l2d", new Integer(L2D));
        h.put("l2f", new Integer(L2F));
        h.put("l2i", new Integer(L2I));
        h.put("ladd", new Integer(LADD));
        h.put("land", new Integer(LAND));
        h.put("lcmpeq", new Integer(LCMPEQ));
        h.put("lcmpgt", new Integer(LCMPGT));
        h.put("ldiv", new Integer(LDIV));
        h.put("lmul", new Integer(LMUL));
        h.put("lneg", new Integer(LNEG));
        h.put("lor", new Integer(LOR));
        h.put("lrem", new Integer(LREM));
        h.put("lshl", new Integer(LSHL));
        h.put("lshr", new Integer(LSHR));
        h.put("lushr", new Integer(LUSHR));
        h.put("lxor", new Integer(LXOR));
    }
    ///////////////////////////////////
    // Evaluation functions.

    /** Determines the result type of an <code>OPER</code>. */
    public static HClass resultType(int v) {
	switch(v) {
	    // boolean return type (input to CJMPs):
        case ACMPEQ:
        case DCMPEQ:
        case DCMPGE:
        case DCMPGT:
        case FCMPEQ:
        case FCMPGE:
        case FCMPGT:
        case ICMPEQ:
        case ICMPGT:
        case LCMPEQ:
        case LCMPGT:
	    return HClass.Boolean;
	    // float return type:
        case D2F:
        case FADD:
        case FDIV:
        case FMUL:
        case FNEG:
        case FREM:
        case I2F:
        case L2F:
	    return HClass.Float;
	    // int return type:
        case D2I:
        case F2I:
        case I2B:
        case I2C:
        case I2S:
        case IADD:
        case IAND:
        case IDIV:
        case IMUL:
        case INEG:
        case IOR:
        case IREM:
        case ISHL:
        case ISHR:
        case IUSHR:
        case IXOR:
        case L2I:
	    return HClass.Int;
	    // long return type:
        case D2L:
        case F2L:
        case I2L:
        case LADD:
        case LAND:
        case LDIV:
        case LMUL:
        case LNEG:
        case LOR:
        case LREM:
        case LSHL:
        case LSHR:
        case LUSHR:
        case LXOR:
	    return HClass.Long;
	    // double return type:
        case DADD:
        case DDIV:
        case DMUL:
        case DNEG:
        case DREM:
        case F2D:
        case I2D:
        case L2D:
	    return HClass.Double;
	default:        throw new RuntimeException("Unknown QOp type: "+v);
	}
    }
    /** Evaluates a constant value for the result of an <code>OPER</code>, 
     *  given constant values for the operands. */
    public static Object evaluate(int opc, Object[] opd) {
	switch(opc) {
	case ACMPEQ:	return _b( Eval.acmpeq	(opd[0], opd[1]));
	case D2F:	return _f( Eval.d2f	(_d(opd[0])));
	case D2I:	return _i( Eval.d2i	(_d(opd[0])));
	case D2L:	return _l( Eval.d2l	(_d(opd[0])));
	case DADD:	return _d( Eval.dadd	(_d(opd[0]), _d(opd[1])));
	case DCMPEQ:	return _b( Eval.dcmpeq	(_d(opd[0]), _d(opd[1])));
	case DCMPGE:	return _b( Eval.dcmpge	(_d(opd[0]), _d(opd[1])));
	case DCMPGT:	return _b( Eval.dcmpgt	(_d(opd[0]), _d(opd[1])));
	case DDIV:	return _d( Eval.ddiv	(_d(opd[0]), _d(opd[1])));
	case DMUL:	return _d( Eval.dmul	(_d(opd[0]), _d(opd[1])));
	case DNEG:	return _d( Eval.dneg	(_d(opd[0])));
	case DREM:	return _d( Eval.drem	(_d(opd[0]), _d(opd[1])));
	case F2D:	return _d( Eval.f2d	(_f(opd[0])));
	case F2I:	return _i( Eval.f2i	(_f(opd[0])));
	case F2L:	return _l( Eval.f2l	(_f(opd[0])));
	case FADD:	return _f( Eval.fadd	(_f(opd[0]), _f(opd[1])));
	case FCMPEQ:	return _b( Eval.fcmpeq	(_f(opd[0]), _f(opd[1])));
	case FCMPGE:	return _b( Eval.fcmpge	(_f(opd[0]), _f(opd[1])));
	case FCMPGT:	return _b( Eval.fcmpgt	(_f(opd[0]), _f(opd[1])));
	case FDIV:	return _f( Eval.fdiv	(_f(opd[0]), _f(opd[1])));
	case FMUL:	return _f( Eval.fmul	(_f(opd[0]), _f(opd[1])));
	case FNEG:	return _f( Eval.fneg	(_f(opd[0])));
	case FREM:	return _f( Eval.frem	(_f(opd[0]), _f(opd[1])));
	case I2B:	return _i( Eval.i2b	(_i(opd[0])));
	case I2C:	return _i( Eval.i2c	(_i(opd[0])));
	case I2D:	return _d( Eval.i2d	(_i(opd[0])));
	case I2F:	return _f( Eval.i2f	(_i(opd[0])));
	case I2L:	return _l( Eval.i2l	(_i(opd[0])));
	case I2S:	return _i( Eval.i2s	(_i(opd[0])));
	case IADD:	return _i( Eval.iadd	(_i(opd[0]), _i(opd[1])));
	case IAND:	return _i( Eval.iand	(_i(opd[0]), _i(opd[1])));
	case ICMPEQ:	return _b( Eval.icmpeq	(_i(opd[0]), _i(opd[1])));
	case ICMPGT:	return _b( Eval.icmpgt	(_i(opd[0]), _i(opd[1])));
	case IDIV:	return _i( Eval.idiv	(_i(opd[0]), _i(opd[1])));
	case IMUL:	return _i( Eval.imul	(_i(opd[0]), _i(opd[1])));
	case INEG:	return _i( Eval.ineg	(_i(opd[0])));
	case IOR:	return _i( Eval.ior	(_i(opd[0]), _i(opd[1])));
	case IREM:	return _i( Eval.irem	(_i(opd[0]), _i(opd[1])));
	case ISHL:	return _i( Eval.ishl	(_i(opd[0]), _i(opd[1])));
	case ISHR:	return _i( Eval.ishr	(_i(opd[0]), _i(opd[1])));
	case IUSHR:	return _i( Eval.iushr	(_i(opd[0]), _i(opd[1])));
	case IXOR:	return _i( Eval.ixor	(_i(opd[0]), _i(opd[1])));
	case L2D:	return _d( Eval.l2d	(_l(opd[0])));
	case L2F:	return _f( Eval.l2f	(_l(opd[0])));
	case L2I:	return _i( Eval.l2i	(_l(opd[0])));
	case LADD:	return _l( Eval.ladd	(_l(opd[0]), _l(opd[1])));
	case LAND:	return _l( Eval.land	(_l(opd[0]), _l(opd[1])));
	case LCMPEQ:	return _b( Eval.lcmpeq	(_l(opd[0]), _l(opd[1])));
	case LCMPGT:	return _b( Eval.lcmpgt	(_l(opd[0]), _l(opd[1])));
	case LDIV:	return _l( Eval.ldiv	(_l(opd[0]), _l(opd[1])));
	case LMUL:	return _l( Eval.lmul	(_l(opd[0]), _l(opd[1])));
	case LNEG:	return _l( Eval.lneg	(_l(opd[0])));
	case LOR:	return _l( Eval.lor	(_l(opd[0]), _l(opd[1])));
	case LREM:	return _l( Eval.lrem	(_l(opd[0]), _l(opd[1])));
	case LSHL:	return _l( Eval.lshl	(_l(opd[0]), _i(opd[1])));
	case LSHR:	return _l( Eval.lshr	(_l(opd[0]), _i(opd[1])));
	case LUSHR:	return _l( Eval.lushr	(_l(opd[0]), _i(opd[1])));
	case LXOR:	return _l( Eval.lxor	(_l(opd[0]), _l(opd[1])));
	default:        throw new RuntimeException("Unknown Op type: "+opc);
	}
    }
    // wrapper functions.
    private static Integer _i(int i)     { return new Integer(i); }
    private static Long    _l(long l)    { return new Long(l);    }
    private static Float   _f(float f)   { return new Float(f);   }
    private static Double  _d(double d)  { return new Double(d);  }
    private static Boolean _b(boolean b) { return new Boolean(b); }
    // unwrapper functions.
    private static int    _i(Object o) { return ((Integer)o).intValue(); }
    private static long   _l(Object o) { return ((Long)o)   .longValue(); }
    private static float  _f(Object o) { return ((Float)o)  .floatValue(); }
    private static double _d(Object o) { return ((Double)o) .doubleValue(); }
}
