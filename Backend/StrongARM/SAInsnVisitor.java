// SAInsnVisitor.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

/**
 * <code>SAInsnVisitor</code> is an base class for visiting 
 * the StrongARM instructions.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: SAInsnVisitor.java,v 1.1.2.1 1999-02-08 00:54:31 andyb Exp $
 */
public abstract class SAInsnVisitor {
    protected SAInsnVisitor() { }

    public abstract void visit(SAInsn sai);
    
/*    public void visit(ADC sai)      { visit((SAInsn)sai); }
    public void visit(ADD sai)      { visit((SAInsn)sai); }
    public void visit(AND sai)      { visit((SAInsn)sai); }
    public void visit(B sai)        { visit((SAInsn)sai); }
    public void visit(BL sai)       { visit((SAInsn)sai); }
    public void visit(BIC sai)      { visit((SAInsn)sai); }
    public void visit(CMN sai)      { visit((SAInsn)sai); }
    public void visit(CMP sai)      { visit((SAInsn)sai); }
    public void visit(EOR sai)      { visit((SAInsn)sai); }
    public void visit(LDM sai)      { visit((SAInsn)sai); }
    public void visit(LDR sai)      { visit((SAInsn)sai); }
    public void visit(LDRB sai)     { visit((SAInsn)sai); }
    public void visit(LDRH sai)     { visit((SAInsn)sai); }
    public void visit(LDRSB sai)    { visit((SAInsn)sai); }
    public void visit(LDRSH sai)    { visit((SAInsn)sai); }
    public void visit(MLA sai)      { visit((SAInsn)sai); }
    public void visit(MOV sai)      { visit((SAInsn)sai); }
    public void visit(MRS sai)      { visit((SAInsn)sai); }
    public void visit(MSR sai)      { visit((SAInsn)sai); }
    public void visit(MUL sai)      { visit((SAInsn)sai); }
    public void visit(MVN sai)      { visit((SAInsn)sai); }
    public void visit(ORR sai)      { visit((SAInsn)sai); }
    public void visit(RSB sai)      { visit((SAInsn)sai); }
    public void visit(RSC sai)      { visit((SAInsn)sai); }
    public void visit(SBC sai)      { visit((SAInsn)sai); }
    public void visit(SMLAL sai)    { visit((SAInsn)sai); }
    public void visit(SMULL sai)    { visit((SAInsn)sai); }
    public void visit(STM sai)      { visit((SAInsn)sai); }
    public void visit(STR sai)      { visit((SAInsn)sai); }
    public void visit(STRB sai)     { visit((SAInsn)sai); }
    public void visit(STRH sai)     { visit((SAInsn)sai); }
    public void visit(SWI sai)      { visit((SAInsn)sai); }
    public void visit(SWP sai)      { visit((SAInsn)sai); }
    public void visit(SWPB sai)     { visit((SAInsn)sai); }
    public void visit(TEQ sai)      { visit((SAInsn)sai); }
    public void visit(TST sai)      { visit((SAInsn)sai); }
    public void visit(UMLAL sai)    { visit((SAInsn)sai); }
    public void visit(UMULL sai)    { visit((SAInsn)sai); } */
}
