// Qop.java, created Mon Nov  9 22:36:21 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
/**
 * <code>Qop</code> is an enumerated type for the various kinds of
 * <code>OPER</code> opcodes.
 * 
 * @author  C. Scott Ananian <cananian@lesser-magoo.lcs.mit.edu>
 * @version $Id: Qop.java,v 1.1.2.5 1999-01-03 03:01:42 cananian Exp $
 */
public abstract class Qop  {
    public final static int ACMPEQ = 0;
    public final static int D2F = 1;
    public final static int D2I = 2;
    public final static int D2L = 3;
    public final static int DADD = 4;
    public final static int DCMPEQ = 5;
    public final static int DCMPGE = 6;
    public final static int DCMPGT = 7;
    public final static int DDIV = 8;
    public final static int DMUL = 9;
    public final static int DNEG = 10;
    public final static int DREM = 11;
    public final static int DSUB = 12;
    public final static int F2D = 13;
    public final static int F2I = 14;
    public final static int F2L = 15;
    public final static int FADD = 16;
    public final static int FCMPEQ = 17;
    public final static int FCMPGE = 18;
    public final static int FCMPGT = 19;
    public final static int FDIV = 20;
    public final static int FMUL = 21;
    public final static int FNEG = 22;
    public final static int FREM = 23;
    public final static int FSUB = 24;
    public final static int I2B = 25;
    public final static int I2C = 26;
    public final static int I2D = 27;
    public final static int I2F = 28;
    public final static int I2L = 29;
    public final static int I2S = 30;
    public final static int IADD = 31;
    public final static int IAND = 32;
    public final static int ICMPEQ = 33;
    public final static int ICMPGT = 35;
    public final static int IDIV = 36;
    public final static int IMUL = 37;
    public final static int INEG = 38;
    public final static int IOR = 39;
    public final static int IREM = 40;
    public final static int ISHL = 41;
    public final static int ISHR = 42;
    public final static int ISUB = 43;
    public final static int IUSHR = 44;
    public final static int IXOR = 45;
    public final static int L2D = 46;
    public final static int L2F = 47;
    public final static int L2I = 48;
    public final static int LADD = 49;
    public final static int LAND = 50;
    public final static int LCMPEQ = 51;
    public final static int LCMPGT = 53;
    public final static int LDIV = 54;
    public final static int LMUL = 55;
    public final static int LNEG = 56;
    public final static int LOR = 57;
    public final static int LREM = 58;
    public final static int LSHL = 59;
    public final static int LSHR = 60;
    public final static int LSUB = 61;
    public final static int LUSHR = 62;
    public final static int LXOR = 63;

    public static boolean isValid(int v) {
	return (0<=v) && (v<=LXOR);
    }

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
        case DSUB:	return "dsub";
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
        case FSUB:	return "fsub";
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
        case ISUB:	return "isub";
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
        case LSUB:	return "lsub";
        case LUSHR:	return "lushr";
        case LXOR:	return "lxor";
	default:        throw new RuntimeException("Unknown Op type: "+v);
	}
    }
    public static int forString(String op) {
	Integer r = (Integer) h.get(op);
	if (r==null) throw new RuntimeException("Unknown Op name: "+op);
	return r.intValue();
    }
    static java.util.Hashtable h = new java.util.Hashtable();
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
        h.put("dsub", new Integer(DSUB));
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
        h.put("fsub", new Integer(FSUB));
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
        h.put("isub", new Integer(ISUB));
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
        h.put("lsub", new Integer(LSUB));
        h.put("lushr", new Integer(LUSHR));
        h.put("lxor", new Integer(LXOR));
    }
    ///////////////////////////////////
    // Evaluation functions.

    /** Determines the result type of an <code>OPER</code>. */
    public static HClass resultType(int v) {
	switch(v) {
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
        case D2F:
        case FADD:
        case FDIV:
        case FMUL:
        case FNEG:
        case FREM:
        case FSUB:
        case I2F:
        case L2F:
	    return HClass.Float;
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
        case ISUB:
        case IUSHR:
        case IXOR:
        case L2I:
	    return HClass.Int;
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
        case LSUB:
        case LUSHR:
        case LXOR:
	    return HClass.Long;
        case DADD:
        case DDIV:
        case DMUL:
        case DNEG:
        case DREM:
        case DSUB:
        case F2D:
        case I2D:
        case L2D:
	    return HClass.Double;
	default:        throw new RuntimeException("Unknown Op type: "+v);
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
	case DSUB:	return _d( Eval.dsub	(_d(opd[0]), _d(opd[1])));
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
	case FSUB:	return _f( Eval.fsub	(_f(opd[0]), _f(opd[1])));
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
	case ISUB:	return _i( Eval.isub	(_i(opd[0]), _i(opd[1])));
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
	case LSUB:	return _l( Eval.lsub	(_l(opd[0]), _l(opd[1])));
	case LUSHR:	return _l( Eval.lushr	(_l(opd[0]), _i(opd[1])));
	case LXOR:	return _l( Eval.lxor	(_l(opd[0]), _l(opd[1])));
	default:        throw new RuntimeException("Unknown Op type: "+opc);
	}
    }
    private static Integer _i(int i)     { return new Integer(i); }
    private static Long    _l(long l)    { return new Long(l);    }
    private static Float   _f(float f)   { return new Float(f);   }
    private static Double  _d(double d)  { return new Double(d);  }
    private static Boolean _b(boolean b) { return new Boolean(b); }
   
    private static int    _i(Object o) { return ((Integer)o).intValue(); }
    private static long   _l(Object o) { return ((Long)o)   .longValue(); }
    private static float  _f(Object o) { return ((Float)o)  .floatValue(); }
    private static double _d(Object o) { return ((Double)o) .doubleValue(); }
}
