// OperVisitor.java, created Sat Sep 19 04:49:05 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
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
 * @version $Id: OperVisitor.java,v 1.3 1998-10-11 02:37:57 cananian Exp $
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
    public void visit_dsub(OPER q) { visit_default(q); }
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
    public void visit_fsub(OPER q) { visit_default(q); }
    public void visit_i2b(OPER q) { visit_default(q); }
    public void visit_i2c(OPER q) { visit_default(q); }
    public void visit_i2d(OPER q) { visit_default(q); }
    public void visit_i2f(OPER q) { visit_default(q); }
    public void visit_i2l(OPER q) { visit_default(q); }
    public void visit_i2s(OPER q) { visit_default(q); }
    public void visit_iadd(OPER q) { visit_default(q); }
    public void visit_iand(OPER q) { visit_default(q); }
    public void visit_icmpeq(OPER q) { visit_default(q); }
    public void visit_icmpge(OPER q) { visit_default(q); }
    public void visit_icmpgt(OPER q) { visit_default(q); }
    public void visit_idiv(OPER q) { visit_default(q); }
    public void visit_imul(OPER q) { visit_default(q); }
    public void visit_ineg(OPER q) { visit_default(q); }
    public void visit_ior(OPER q) { visit_default(q); }
    public void visit_irem(OPER q) { visit_default(q); }
    public void visit_ishl(OPER q) { visit_default(q); }
    public void visit_ishr(OPER q) { visit_default(q); }
    public void visit_isub(OPER q) { visit_default(q); }
    public void visit_iushr(OPER q) { visit_default(q); }
    public void visit_ixor(OPER q) { visit_default(q); }
    public void visit_l2d(OPER q) { visit_default(q); }
    public void visit_l2f(OPER q) { visit_default(q); }
    public void visit_l2i(OPER q) { visit_default(q); }
    public void visit_ladd(OPER q) { visit_default(q); }
    public void visit_land(OPER q) { visit_default(q); }
    public void visit_lcmpeq(OPER q) { visit_default(q); }
    public void visit_lcmpge(OPER q) { visit_default(q); }
    public void visit_lcmpgt(OPER q) { visit_default(q); }
    public void visit_ldiv(OPER q) { visit_default(q); }
    public void visit_lmul(OPER q) { visit_default(q); }
    public void visit_lneg(OPER q) { visit_default(q); }
    public void visit_lor(OPER q) { visit_default(q); }
    public void visit_lrem(OPER q) { visit_default(q); }
    public void visit_lshl(OPER q) { visit_default(q); }
    public void visit_lshr(OPER q) { visit_default(q); }
    public void visit_lsub(OPER q) { visit_default(q); }
    public void visit_lushr(OPER q) { visit_default(q); }
    public void visit_lxor(OPER q) { visit_default(q); }
    public void visit_unknown(OPER q) 
    { throw new Error("Unknown OPER opcode"); }

    // package-scoped dispatch function.
    final void dispatch(OPER q) {
	String op = q.opcode.intern(); // allows use of == instead of equals()

	if (false) ;
	else if (op=="acmpeq") visit_acmpeq(q);
	else if (op=="d2f") visit_d2f(q);
	else if (op=="d2i") visit_d2i(q);
	else if (op=="d2l") visit_d2l(q);
	else if (op=="dadd") visit_dadd(q);
	else if (op=="dcmpeq") visit_dcmpeq(q);
	else if (op=="dcmpge") visit_dcmpge(q);
	else if (op=="dcmpgt") visit_dcmpgt(q);
	else if (op=="ddiv") visit_ddiv(q);
	else if (op=="dmul") visit_dmul(q);
	else if (op=="dneg") visit_dneg(q);
	else if (op=="drem") visit_drem(q);
	else if (op=="dsub") visit_dsub(q);
	else if (op=="f2d") visit_f2d(q);
	else if (op=="f2i") visit_f2i(q);
	else if (op=="f2l") visit_f2l(q);
	else if (op=="fadd") visit_fadd(q);
	else if (op=="fcmpeq") visit_fcmpeq(q);
	else if (op=="fcmpge") visit_fcmpge(q);
	else if (op=="fcmpgt") visit_fcmpgt(q);
	else if (op=="fdiv") visit_fdiv(q);
	else if (op=="fmul") visit_fmul(q);
	else if (op=="fneg") visit_fneg(q);
	else if (op=="frem") visit_frem(q);
	else if (op=="fsub") visit_fsub(q);
	else if (op=="i2b") visit_i2b(q);
	else if (op=="i2c") visit_i2c(q);
	else if (op=="i2d") visit_i2d(q);
	else if (op=="i2f") visit_i2f(q);
	else if (op=="i2l") visit_i2l(q);
	else if (op=="i2s") visit_i2s(q);
	else if (op=="iadd") visit_iadd(q);
	else if (op=="iand") visit_iand(q);
	else if (op=="icmpeq") visit_icmpeq(q);
	else if (op=="icmpge") visit_icmpge(q);
	else if (op=="icmpgt") visit_icmpgt(q);
	else if (op=="idiv") visit_idiv(q);
	else if (op=="imul") visit_imul(q);
	else if (op=="ineg") visit_ineg(q);
	else if (op=="ior") visit_ior(q);
	else if (op=="irem") visit_irem(q);
	else if (op=="ishl") visit_ishl(q);
	else if (op=="ishr") visit_ishr(q);
	else if (op=="isub") visit_isub(q);
	else if (op=="iushr") visit_iushr(q);
	else if (op=="ixor") visit_ixor(q);
	else if (op=="l2d") visit_l2d(q);
	else if (op=="l2f") visit_l2f(q);
	else if (op=="l2i") visit_l2i(q);
	else if (op=="ladd") visit_ladd(q);
	else if (op=="land") visit_land(q);
	else if (op=="lcmpeq") visit_lcmpeq(q);
	else if (op=="lcmpge") visit_lcmpge(q);
	else if (op=="lcmpgt") visit_lcmpgt(q);
	else if (op=="ldiv") visit_ldiv(q);
	else if (op=="lmul") visit_lmul(q);
	else if (op=="lneg") visit_lneg(q);
	else if (op=="lor") visit_lor(q);
	else if (op=="lrem") visit_lrem(q);
	else if (op=="lshl") visit_lshl(q);
	else if (op=="lshr") visit_lshr(q);
	else if (op=="lsub") visit_lsub(q);
	else if (op=="lushr") visit_lushr(q);
	else if (op=="lxor") visit_lxor(q);
	else visit_unknown(q);
    }
}
