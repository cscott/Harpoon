// OperVisitor.java, created Sat Sep 19 04:49:05 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

/**
 * An <code>OperVisitor</code> is a visitor pattern for opcodes of the OPER 
 * quad.  By default, all <code>visit_*</code> methods invoke
 * <code>visit_default</code>; this can (and should) be changed by
 * overriding particular <code>visit_*</code> methods.  The
 * <code>visit_unknown</code> method is your ticket to extending OPER's
 * set of opcodes.  By default it throws an <code>Error</code>, but you
 * can subclass OperVisitor, define visit methods for your new operations,
 * and then override <code>visit_unknown</code> to dispatch to your new
 * methods.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OperVisitor.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 */

public abstract class OperVisitor  {
    protected OperVisitor() { }
    
    public abstract void visit_default(OPER q);
    
    public void visit_acmpeq(OPER q) { visit_default(q); }
    public void visit_d2f(OPER q) { visit_default(q); }
    public void visit_d2i(OPER q) { visit_default(q); }
    public void visit_d2l(OPER q) { visit_default(q); }
    public void visit_dadd(OPER q) { visit_default(q); }
    public void visit_dcmpeq(OPER q) { visit_default(q); }
    public void visit_dcmpge(OPER q) { visit_default(q); }
    public void visit_dcmpgt(OPER q) { visit_default(q); }
    public void visit_ddiv(OPER q) { visit_default(q); }
    public void visit_dmul(OPER q) { visit_default(q); }
    public void visit_dneg(OPER q) { visit_default(q); }
    public void visit_drem(OPER q) { visit_default(q); }
    public void visit_f2d(OPER q) { visit_default(q); }
    public void visit_f2i(OPER q) { visit_default(q); }
    public void visit_f2l(OPER q) { visit_default(q); }
    public void visit_fadd(OPER q) { visit_default(q); }
    public void visit_fcmpeq(OPER q) { visit_default(q); }
    public void visit_fcmpge(OPER q) { visit_default(q); }
    public void visit_fcmpgt(OPER q) { visit_default(q); }
    public void visit_fdiv(OPER q) { visit_default(q); }
    public void visit_fmul(OPER q) { visit_default(q); }
    public void visit_fneg(OPER q) { visit_default(q); }
    public void visit_frem(OPER q) { visit_default(q); }
    public void visit_i2b(OPER q) { visit_default(q); }
    public void visit_i2c(OPER q) { visit_default(q); }
    public void visit_i2d(OPER q) { visit_default(q); }
    public void visit_i2f(OPER q) { visit_default(q); }
    public void visit_i2l(OPER q) { visit_default(q); }
    public void visit_i2s(OPER q) { visit_default(q); }
    public void visit_iadd(OPER q) { visit_default(q); }
    public void visit_iand(OPER q) { visit_default(q); }
    public void visit_icmpeq(OPER q) { visit_default(q); }
    public void visit_icmpgt(OPER q) { visit_default(q); }
    public void visit_idiv(OPER q) { visit_default(q); }
    public void visit_imul(OPER q) { visit_default(q); }
    public void visit_ineg(OPER q) { visit_default(q); }
    public void visit_ior(OPER q) { visit_default(q); }
    public void visit_irem(OPER q) { visit_default(q); }
    public void visit_ishl(OPER q) { visit_default(q); }
    public void visit_ishr(OPER q) { visit_default(q); }
    public void visit_iushr(OPER q) { visit_default(q); }
    public void visit_ixor(OPER q) { visit_default(q); }
    public void visit_l2d(OPER q) { visit_default(q); }
    public void visit_l2f(OPER q) { visit_default(q); }
    public void visit_l2i(OPER q) { visit_default(q); }
    public void visit_ladd(OPER q) { visit_default(q); }
    public void visit_land(OPER q) { visit_default(q); }
    public void visit_lcmpeq(OPER q) { visit_default(q); }
    public void visit_lcmpgt(OPER q) { visit_default(q); }
    public void visit_ldiv(OPER q) { visit_default(q); }
    public void visit_lmul(OPER q) { visit_default(q); }
    public void visit_lneg(OPER q) { visit_default(q); }
    public void visit_lor(OPER q) { visit_default(q); }
    public void visit_lrem(OPER q) { visit_default(q); }
    public void visit_lshl(OPER q) { visit_default(q); }
    public void visit_lshr(OPER q) { visit_default(q); }
    public void visit_lushr(OPER q) { visit_default(q); }
    public void visit_lxor(OPER q) { visit_default(q); }
    public void visit_unknown(OPER q) 
    { throw new RuntimeException("Unknown OPER opcode: "+q.opcode); }

    // package-scoped dispatch function.
    final void dispatch(OPER q) {
	switch (q.opcode) {
	case Qop.ACMPEQ:	visit_acmpeq(q); break;
	case Qop.D2F:	visit_d2f(q); break;
	case Qop.D2I:	visit_d2i(q); break;
	case Qop.D2L:	visit_d2l(q); break;
	case Qop.DADD:	visit_dadd(q); break;
	case Qop.DCMPEQ:	visit_dcmpeq(q); break;
	case Qop.DCMPGE:	visit_dcmpge(q); break;
	case Qop.DCMPGT:	visit_dcmpgt(q); break;
	case Qop.DDIV:	visit_ddiv(q); break;
	case Qop.DMUL:	visit_dmul(q); break;
	case Qop.DNEG:	visit_dneg(q); break;
	case Qop.DREM:	visit_drem(q); break;
	case Qop.F2D:	visit_f2d(q); break;
	case Qop.F2I:	visit_f2i(q); break;
	case Qop.F2L:	visit_f2l(q); break;
	case Qop.FADD:	visit_fadd(q); break;
	case Qop.FCMPEQ:	visit_fcmpeq(q); break;
	case Qop.FCMPGE:	visit_fcmpge(q); break;
	case Qop.FCMPGT:	visit_fcmpgt(q); break;
	case Qop.FDIV:	visit_fdiv(q); break;
	case Qop.FMUL:	visit_fmul(q); break;
	case Qop.FNEG:	visit_fneg(q); break;
	case Qop.FREM:	visit_frem(q); break;
	case Qop.I2B:	visit_i2b(q); break;
	case Qop.I2C:	visit_i2c(q); break;
	case Qop.I2D:	visit_i2d(q); break;
	case Qop.I2F:	visit_i2f(q); break;
	case Qop.I2L:	visit_i2l(q); break;
	case Qop.I2S:	visit_i2s(q); break;
	case Qop.IADD:	visit_iadd(q); break;
	case Qop.IAND:	visit_iand(q); break;
	case Qop.ICMPEQ:	visit_icmpeq(q); break;
	case Qop.ICMPGT:	visit_icmpgt(q); break;
	case Qop.IDIV:	visit_idiv(q); break;
	case Qop.IMUL:	visit_imul(q); break;
	case Qop.INEG:	visit_ineg(q); break;
	case Qop.IOR:	visit_ior(q); break;
	case Qop.IREM:	visit_irem(q); break;
	case Qop.ISHL:	visit_ishl(q); break;
	case Qop.ISHR:	visit_ishr(q); break;
	case Qop.IUSHR:	visit_iushr(q); break;
	case Qop.IXOR:	visit_ixor(q); break;
	case Qop.L2D:	visit_l2d(q); break;
	case Qop.L2F:	visit_l2f(q); break;
	case Qop.L2I:	visit_l2i(q); break;
	case Qop.LADD:	visit_ladd(q); break;
	case Qop.LAND:	visit_land(q); break;
	case Qop.LCMPEQ:	visit_lcmpeq(q); break;
	case Qop.LCMPGT:	visit_lcmpgt(q); break;
	case Qop.LDIV:	visit_ldiv(q); break;
	case Qop.LMUL:	visit_lmul(q); break;
	case Qop.LNEG:	visit_lneg(q); break;
	case Qop.LOR:	visit_lor(q); break;
	case Qop.LREM:	visit_lrem(q); break;
	case Qop.LSHL:	visit_lshl(q); break;
	case Qop.LSHR:	visit_lshr(q); break;
	case Qop.LUSHR:	visit_lushr(q); break;
	case Qop.LXOR:	visit_lxor(q); break;
	default:	visit_unknown(q); break;
	}
    }
}
