// OperVisitor.java, created Sat Sep 19 04:49:05 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>OperVisitor</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: OperVisitor.java,v 1.1 1998-09-19 09:07:22 cananian Exp $
 */

public abstract class OperVisitor  {
    protected OPER oper;
    protected OperVisitor(OPER oper) { this.oper = oper; }
    
    public abstract void visit_default();
    
    public void visit_acmpeq() { visit_default(); }
    public void visit_d2f() { visit_default(); }
    public void visit_d2i() { visit_default(); }
    public void visit_d2l() { visit_default(); }
    public void visit_dadd() { visit_default(); }
    public void visit_dcmpeq() { visit_default(); }
    public void visit_dcmpge() { visit_default(); }
    public void visit_dcmpgt() { visit_default(); }
    public void visit_ddiv() { visit_default(); }
    public void visit_dmul() { visit_default(); }
    public void visit_dneg() { visit_default(); }
    public void visit_drem() { visit_default(); }
    public void visit_dsub() { visit_default(); }
    public void visit_f2d() { visit_default(); }
    public void visit_f2i() { visit_default(); }
    public void visit_f2l() { visit_default(); }
    public void visit_fadd() { visit_default(); }
    public void visit_fcmpeq() { visit_default(); }
    public void visit_fcmpge() { visit_default(); }
    public void visit_fcmpgt() { visit_default(); }
    public void visit_fdiv() { visit_default(); }
    public void visit_fmul() { visit_default(); }
    public void visit_fneg() { visit_default(); }
    public void visit_frem() { visit_default(); }
    public void visit_fsub() { visit_default(); }
    public void visit_i2b() { visit_default(); }
    public void visit_i2c() { visit_default(); }
    public void visit_i2d() { visit_default(); }
    public void visit_i2f() { visit_default(); }
    public void visit_i2l() { visit_default(); }
    public void visit_i2s() { visit_default(); }
    public void visit_iadd() { visit_default(); }
    public void visit_iand() { visit_default(); }
    public void visit_icmpeq() { visit_default(); }
    public void visit_icmpge() { visit_default(); }
    public void visit_icmpgt() { visit_default(); }
    public void visit_idiv() { visit_default(); }
    public void visit_imul() { visit_default(); }
    public void visit_ineg() { visit_default(); }
    public void visit_ior() { visit_default(); }
    public void visit_irem() { visit_default(); }
    public void visit_ishl() { visit_default(); }
    public void visit_ishr() { visit_default(); }
    public void visit_isub() { visit_default(); }
    public void visit_iushr() { visit_default(); }
    public void visit_ixor() { visit_default(); }
    public void visit_l2d() { visit_default(); }
    public void visit_l2f() { visit_default(); }
    public void visit_l2i() { visit_default(); }
    public void visit_ladd() { visit_default(); }
    public void visit_land() { visit_default(); }
    public void visit_lcmpeq() { visit_default(); }
    public void visit_lcmpge() { visit_default(); }
    public void visit_lcmpgt() { visit_default(); }
    public void visit_ldiv() { visit_default(); }
    public void visit_lmul() { visit_default(); }
    public void visit_lneg() { visit_default(); }
    public void visit_lor() { visit_default(); }
    public void visit_lrem() { visit_default(); }
    public void visit_lshl() { visit_default(); }
    public void visit_lshr() { visit_default(); }
    public void visit_lsub() { visit_default(); }
    public void visit_lushr() { visit_default(); }
    public void visit_lxor() { visit_default(); }
}
