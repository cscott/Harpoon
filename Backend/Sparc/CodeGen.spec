// CodeGen.spec, created Mon Jun 28 23:00:48 1999 by cananian -*- Java -*-
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.Uop;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Typed;
import harpoon.Temp.Temp;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <code>Sparc.CodeGen</code> is a code-generator for the Sparc architecture.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: CodeGen.spec,v 1.1.2.11 1999-11-04 10:19:50 andyb Exp $
 */
%%
    private Map origTempToNewTemp;
    private Instr root;
    private Instr last;
    private InstrFactory instrFactory;
    private final RegFileInfo regfile;

    public CodeGen(Frame frame) {
        super(frame);
        this.regfile = (RegFileInfo) frame.getRegFileInfo();
    }

    private Instr emit(Instr i) {
	if (root == null)
	    root = i;
        i.layout(last, null);
	last = i;
        return i;
    }

    // Lots of variations on emit() to make it a bit friendlier
    // to use in the patterns below.

    // The main variabtion - this is the only one which should
    // call emit(Instr) directly - all others should go
    // through this one.
    private Instr emit(HCodeElement root, String assem,
                       Temp[] dst, Temp[] src, boolean canFallThrough,
                       List targets) {
        return emit(new Instr(instrFactory, root, assem, dst, src,
                              canFallThrough, targets));
    }

    private Instr emit(HCodeElement root, String assem,
                       Temp[] dst, Temp[] src) {
        return emit(root, assem, dst, src, true, null);
    }

    private Instr emitMEM(HCodeElement root, String assem,
                          Temp[] dst, Temp[] src) {
        return emit(new InstrMEM(instrFactory, root, assem, dst, src));
    }

    private Instr emitDELAYSLOT(HCodeElement root) {
        return emit(new InstrDELAYSLOT(instrFactory, root));
    }

    private Instr emitCC(HCodeElement root, String assem,
                         Temp[] dst, Temp[] src) {
        return emit(new InstrCC(instrFactory, root, assem, dst, src));
    }

    private Instr emitLABEL(HCodeElement root, String assem, Label label) {
        return emit(new InstrLABEL(instrFactory, root, assem, label));
    }

    // AAA - FIX BELOW THIS LINE

    public Instr procFixup(HMethod hm, Instr instr, int stackspace,
                           Set usedRegisters) {
        return null;
    }

    private Temp makeTemp( Temp orig ) {
        return null;
    }

    // INNER CLASSES

    /** Sub-class to represent delay-slots.
     * <code>optimize()</code> uses this class information to determine that
     * it should rearrange code to try to eliminate these instructions.
     * @author C. Scott Ananian
     * @see Sparc.CodeGen#optimize
     */
    public class InstrDELAYSLOT extends Instr {
	// a nop to fill the delay slot
	public InstrDELAYSLOT(InstrFactory inf, HCodeElement source) {
	    super(inf, source, "\t nop \t ! delay slot \n");
	}
    }

    /** Sub-class to indicate dependencies on the condition code register
     *  for later optimization.  This prevents putting the <code>cmp</code>
     *  instruction in the delay-slot of its branch-conditional.
     */
    public class InstrCC extends Instr {
	//a new instr type to indicate dependency on the condition code reg
	public InstrCC(InstrFactory inf, HCodeElement source, String assem,
	          Temp[] dst, Temp[] src) {
	    super(inf, source, assem, dst, src);
	}
	/** FIXME: How does Instr represent LabelLists? */
    }

    /** Determine whether a constant can fit in the immediate field of
     *  a SPARC instruction. */
    static boolean is13bit(Number n) {
	if (n instanceof Double || n instanceof Float) return false;
	return ((-4096<=n.longValue()) && (n.longValue()<=4095));
    }

    /** Return proper suffix based on type. */
    static String suffix(Typed t) {
      String r="";
      if (Type.isDoubleWord(((Tree)t).getFactory(), t.type())) r+="d";
      if (Type.isFloatingPoint(t.type())) r+="f";
      return r;
    }

    /** Crunch <code>Bop</code> down to sparc instruction. */
    static String bop(int op) {
	switch(op) {
	case Bop.ADD: return "add";
	case Bop.AND: return "and";
	case Bop.OR:  return "or";
	case Bop.XOR: return "xor";
	case Bop.SHL: return "sll";
	case Bop.SHR: return "sra";
	case Bop.USHR:return "srl";
	}
	Util.assert(false);
        return null;
    }

    static boolean isCommutative(int op) { 
	return op==Bop.ADD || op==Bop.OR || op==Bop.AND || op==Bop.XOR;
    }

    static boolean isShift(int op) {
	return op==Bop.SHL || op==Bop.SHR || op==Bop.USHR;
    }
%%

%start with %{
       // State Variables which are initialized each
       // time we do instruction selection on another
       // bit of TreeCode

       root = null; 
       last = null;
       origTempToNewTemp = new HashMap();
       this.instrFactory = inf;
}%

%end with %{
       // What to execute at the end of the instruction
       // selection method

       Util.assert(root != null, "Should always generate some instrs");
       return root;
}%

// patterns with MOVE at their root.

//  32- and 64-bit integer and floating-point STOREs:

MOVE(MEM(CONST(c)), e) %pred %( is13bit(c) )% %{
 emitMEM (ROOT, "\t st"+suffix((Typed)ROOT.dst)+" `s0, ["+c+"]\n",
          null, new Temp[] { e });
}%

MOVE(MEM(BINOP(ADD, CONST(c), e1)), e2) %pred %( is13bit(c) )% %{
 emitMEM (ROOT, "\t st"+suffix((Typed)ROOT.dst)+" `s1, [`s0+"+c+"]\n",
	  null, new Temp[] { e1, e2 });
}%

MOVE(MEM(BINOP(ADD, e1, CONST(c))), e2) %pred %( is13bit(c) )% %{
 emitMEM (ROOT, "\t st"+suffix((Typed)ROOT.dst)+" `s1, [`s0+"+c+"]\n", 
          null, new Temp[] { e1, e2 });
}%

MOVE(MEM(BINOP(ADD, e1, e2)), e3) %{
 emitMEM (ROOT, "\t st"+suffix((Typed)ROOT.dst)+" `s2, [`s0+`s1]\n", 
          null, new Temp[] { e1, e2, e3 });
}% 

MOVE(MEM(e1), e2) %{
 emitMEM (ROOT, "\t st"+suffix((Typed)ROOT.dst)+" `s1, [`s0]\n", 
          null, new Temp[] { e1, e2 });
}%

// load small constants

MOVE(e1, CONST<i,p>(c)) %pred %( is13bit(c) )% %{
 emit (ROOT, "\t mov "+c+", `d0\n",
       new Temp[] { e1 }, null);
}%

// other MOVEs

MOVE<i,p>(e1, e2) %{ /* catch-all */
 emit (ROOT, "\t mov `s0, `d0\n",
       new Temp[] { e1 }, new Temp[] { e2 });
}%

MOVE<l>(e1, e2) %{ /* long (pair of int) register move */
 emit (ROOT, "\t mov `s0l, `d0l\n",
       new Temp[] { e1 }, new Temp[] { e2 });
 emit (ROOT, "\t mov `s0h, `d0h\n",
       new Temp[] { e1 }, new Temp[] { e2 });
}%

MOVE<f>(e1, e2) %{ /* floating-point register move */
 emit(ROOT, "\t fmovs `s0, `d0\n",
      new Temp[] { e1 }, new Temp[] { e2 });
}%

MOVE<d>(e1, e2) %{ /* double (pair of fp) register move */
 emit (ROOT, "\t fmovs `s0l, `d0l\n",
       new Temp[] { e1 }, new Temp[] { e2 });
 emit (ROOT, "\t fmovs `s0h, `d0h\n",
       new Temp[] { e1 }, new Temp[] { e2 });
}%

// patterns with EXP at their root.

EXP(e1) %{
  /* throw away temp e1 (not used) */
}%

// patterns with JUMP at their root.

JUMP(NAME(l)) %{
 emit (ROOT, "\t b "+l+"\n", null, null); /* AAA - target list?! */
 emitDELAYSLOT (ROOT);
}%

JUMP(BINOP(ADD, e1, e2)) %{
 emit (ROOT, "\t jmpl `s0+`s1, %g0\n", 
      null,  new Temp[] { e1, e2 }); /* AAA - target list?! */
 emitDELAYSLOT (ROOT);
}%

JUMP(e1) %{
 emit (ROOT, "\t jmpl `s0, %g0\n", 
       null, new Temp[] { e1 }); /* AAA - target list?! */
 emitDELAYSLOT (ROOT);
}%

// patterns with a CJUMP at their root.

CJUMP(e, true_label, false_label) %{
 emitCC (ROOT, "\t cmp `s0, 0\n", 
         null,  new Temp[] { e });
 emitCC (ROOT, "\t bne "+true_label+"\n", null, null); /*target!?*/
 emitDELAYSLOT (ROOT);

 /* the next two lines can hopefully be left out. */
 emitCC (ROOT, "\t ba "+false_label+"\n", null, null); /*target?!*/
 emitDELAYSLOT (ROOT);
}%

// patterns with SEQ at their root should be handled by the generator magically
// labels.

LABEL(l) %{
 emitLABEL (ROOT, l.toString()+":\n", ((LABEL) ROOT).label);
}%

// expressions

CONST<i,l,p>(0)=r %{
 emit (ROOT, "\t mov %g0, `d0\n", new Temp[] {r}, null);
}%

CONST<i,p>(c)=r %{
 if (is13bit(c)) {
   emit (ROOT, "\t set "+c+", `d0\n", new Temp[] {r}, null);
 } else {
   emit (ROOT, "\t sethi %hi("+c+"), `d0\n", new Temp[]{r},null);
   emit (ROOT, "\t or `s0, %lo("+c+"), `d0\n",
         new Temp[] { r }, new Temp[] { r });
 }
}%

// FIXME: long and floating-point constants (change section?)

NAME(s)=r %{
 emit (ROOT, "\t sethi %hi("+s+"), `d0\n",
       new Temp[]{r}, null);
 emit (ROOT, "\t or `s0, %lo("+s+"), `d0\n",
       new Temp[] { r }, new Temp[] { r });
}%

/*TEMP(t)=r should be handled by c-g-g. */

BINOP<i,p>(op, CONST(c), e)=r %pred %( isCommutative(op) && is13bit(c) )% %{
 emit (ROOT, "\t "+bop(op)+" `s0, "+c+", `d0\n",
       new Temp[] { r }, new Temp[] { e });
}%

BINOP<i,p>(op, e, CONST(c))=r %pred %( (isShift(op) || isCommutative(op)) && is13bit(c) )% %{
 emit (ROOT, "\t "+bop(op)+" `s0, "+c+", `d0\n",
       new Temp[] { r }, new Temp[] { e });
}%

BINOP<i,p>(op, e1, e2)=r  %pred %( isShift(op) || isCommutative(op) )% %{
 emit (ROOT, "\t "+bop(op)+" `s0, `s1, `d0\n",
       new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<i,p>(ADD, e1, UNOP(NEG, e2))=r /* subtraction */ %{
 emit (ROOT, "\t sub `s0, `s1, `d0\n",
       new Temp[] { r }, new Temp[] { e1, e2 });
}%

/* FIXME: write integer MUL/DIV rules */
/* FIXME: write long rules */
/* floating-point binops: */

BINOP<f,d>(ADD, e1, e2)=r %{
 String s = (Type.isDoubleWord(((Tree)ROOT).getFactory(),((BINOP)ROOT).type()))?"d":"s";
 emit (ROOT, "\t fadd"+s+" `s0, `s1, `d0\n",
       new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<f,d>(ADD, e1, UNOP(NEG, e2))=r %{
 String s = (Type.isDoubleWord(((Tree)ROOT).getFactory(),((BINOP)ROOT).type()))?"d":"s";
 emit (ROOT, "\t fsub"+s+" `s0, `s1, `d0\n",
       new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<f,d>(MUL, e1, e2)=r %{
 String s = (Type.isDoubleWord(((Tree)ROOT).getFactory(),((BINOP)ROOT).type()))?"d":"s";
 emit (ROOT, "\t fmul"+s+" `s0, `s1, `d0\n",
       new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<f,d>(DIV, e1, e2)=r %{
 String s = (Type.isDoubleWord(((Tree)ROOT).getFactory(),((BINOP)ROOT).type()))?"d":"s";
 emit (ROOT, "\t fdiv"+s+" `s0, `s1, `d0\n",
       new Temp[] { r }, new Temp[] { e1, e2 });
}%

/* FIXME: finish floating-point rules */

UNOP<i,p>(NEG, e)=r %{
 emit (ROOT, "\t sub %g0, `s0, `d0\n",
       new Temp[] { r }, new Temp[] { e });
}%

UNOP<i,p>(_2B, e)=r %{ /* byte is 8-bit signed */
 emit (ROOT, "\t sll `s0, 24, `d0\n",
       new Temp[] { r }, new Temp[] { e });
 emit (ROOT, "\t sra `s0, 24, `d0\n",
       new Temp[] { r } , new Temp[] { r });
}%

UNOP<i,p>(_2S, e)=r %{ /* short is 16-bit signed */
 emit (ROOT, "\t sll `s0, 16, `d0\n",
       new Temp[] { r }, new Temp[] { e });
 emit (ROOT, "\t sra `s0, 16, `d0\n",
       new Temp[] { r } , new Temp[] { r });
}%

UNOP<i,p>(_2C, e)=r %{ /* character is 16-bit unsigned */
 emit (ROOT, "\t sll `s0, 16, `d0\n",
       new Temp[] { r }, new Temp[] { e });
 emit (ROOT, "\t srl `s0, 16, `d0\n",
       new Temp[] { r } , new Temp[] { r });
}%

UNOP<i,p>(_2L, e)=r %{ /* make 64-bit word. */
 emit (ROOT, "\t mov `s0, `d0l\n",
       new Temp[] { r }, new Temp[] { e });
 emit (ROOT, "\t sra `s0, 31, `d0h\n",
       new Temp[] { r }, new Temp[] { e });
}%

UNOP<i,p>(_2I, e)=r %{ /* do nothing */
 emit(ROOT, "\t mov `s0, `d0\n",
      new Temp[] { r } , new Temp[] { e });
}%
/* FIXME: finish UNOP rules */

// patterns with MEM at root.

MEM(BINOP(PLUS, CONST(c), e1))=r %pred %( is13bit(c) )% %{
 emit (ROOT, "\t ld"+suffix((Typed)ROOT.exp)+" [`s0+"+c+"], `d0\n",
       new Temp[] { r }, new Temp[] { e1 });
}%

MEM(BINOP(PLUS, e1, CONST(c)))=r %pred %( is13bit(c) )% %{
 emit (ROOT, "\t ld"+suffix((Typed)ROOT.exp)+" [`s0+"+c+"], `d0\n",
       new Temp[] { r }, new Temp[] { e1 });
}%

MEM(BINOP(PLUS, e1, e2))=r %{
 emit (ROOT, "\t ld"+suffix((Typed)ROOT.exp)+" [`s0+`s1], `d0\n",
       new Temp[] { r }, new Temp[] { e1, e2 });
}%

MEM(CONST(c))=r %pred %( is13bit(c) )% %{
 emit (ROOT, "\t ld ["+c+"], `d0\n",
       new Temp[] { r }, null);
}%

MEM(e)=r %{
 emit (ROOT, "\t ld [`s0], `d0\n",
       new Temp[] { r }, new Temp[] { e });
}%

/* FIXME: munch CALLs */
