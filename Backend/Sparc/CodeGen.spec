// CodeGen.spec, created Mon Jun 28 23:00:48 1999 by cananian -*- Java -*-
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrDIRECTIVE;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.DATA;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.EXP;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.METHOD;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.RETURN;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.THROW;
import harpoon.IR.Tree.UNOP;
import harpoon.IR.Tree.Uop;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Typed;
import harpoon.IR.Tree.PreciselyTyped;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.Label;
import harpoon.Temp.LabelList;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <code>Sparc.CodeGen</code> is a code-generator for the Sparc architecture.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: CodeGen.spec,v 1.1.2.20 2000-01-09 00:24:11 duncan Exp $
 */
%%
    private Instr root;
    private Instr last;
    private InstrFactory instrFactory;
    private final RegFileInfo regfile;
    private final TempBuilder tb;

    // short variable names for commonly used register temps
    private Temp r0, r1, r8, r9, r10, r11, SP, FP;

    private Map codeGenTempMap;

    public CodeGen(Frame frame) {
        super(frame);
        this.regfile = (RegFileInfo) frame.getRegFileInfo();
        this.tb = (TempBuilder) frame.getTempBuilder();
        r0 = regfile.getRegister(0);
        r1 = regfile.getRegister(1);
        r8 = regfile.getRegister(8);
        r9 = regfile.getRegister(9);
        r10 = regfile.getRegister(10);
        r11 = regfile.getRegister(11);
        SP = regfile.SP();
        FP = regfile.FP();
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

    // The main variation - this is the only one which should
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
  
    // emit instructions with targets and that can fall through
    private Instr emit(HCodeElement root, String assem,
                       Temp[] dst, Temp[] src, Label[] targets) {
        return emit(root, assem, dst, src, true, Arrays.asList(targets));
    }

    // emit instructions with targets and that cannot fall through to
    // the next instruction - i.e. absolute jumps.
    private Instr emitNoFall(HCodeElement root, String assem,
                             Temp[] dst, Temp[] src, Label[] targets) {
        return emit(root, assem, dst, src, false, Arrays.asList(targets));
    }

    private Instr emitNoFall(HCodeElement root, String assem,
                             Temp[] dst, Temp[] src, List targets) {
        return emit(root, assem, dst, src, false, targets);
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

    private Instr emitCC(HCodeElement root, String assem,
                         Temp[] dst, Temp[] src, Label[] targets) {
        return emit(new InstrCC(instrFactory, root, assem, dst, src,
                                true, Arrays.asList(targets)));
    }

    private Instr emitCCNoFall(HCodeElement root, String assem,
                               Temp[] dst, Temp[] src, Label[] targets) {
        return emit(new InstrCC(instrFactory, root, assem, dst, src,
                                false, Arrays.asList(targets)));
    }

    private Instr emitLABEL(HCodeElement root, String assem, Label label) {
        return emit(new InstrLABEL(instrFactory, root, assem, label));
    }

    private Instr emitDIRECTIVE(HCodeElement root, String directive) {
        return emit(new InstrDIRECTIVE(instrFactory, root, directive));
    }
  
    private Instr emitENTRY(HCodeElement root) {
        return emit(new InstrENTRY(instrFactory, root));
    }

    private Instr emitEXIT(HCodeElement root) {
        return emit(new InstrEXIT(instrFactory, root));
    }

    private Temp makeTemp(Typed ROOT, Temp orig, TempFactory tf) {
        Temp newTemp = (Temp) codeGenTempMap.get(orig);    
        if (newTemp == null) {
            newTemp = tb.makeTemp(ROOT, tf);
            codeGenTempMap.put(orig, newTemp);
        }
        return newTemp;
    }

    // AAA - To Do
    public Instr procFixup(HMethod hm, Instr instr, int stackspace,
                           Set usedRegisters) {
        return null;
    }

    // INNER CLASSES

    /** Sub-class to represent delay-slots.
     * <code>optimize()</code> uses this class information to determine that
     * it should rearrange code to try to eliminate these instructions.
     * !author C. Scott Ananian
     * !see Sparc.CodeGen#optimize
     */
    public class InstrDELAYSLOT extends Instr {
	// a nop to fill the delay slot
	public InstrDELAYSLOT(InstrFactory inf, HCodeElement source) {
	    super(inf, source, "nop  ! delay slot\n");
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
    
        public InstrCC(InstrFactory inf, HCodeElement source, String assem,
                       Temp[] dst, Temp[] src, boolean canFallThrough, 
                       List targets) {
            super(inf, source, assem, dst, src, canFallThrough, targets);
        }
    }

    private class InstrENTRY extends InstrDIRECTIVE {
        public InstrENTRY(InstrFactory inf, HCodeElement src) {
            super(inf, src, "!--method entry point--");
        }
    }

    private class InstrEXIT extends InstrDIRECTIVE {
        public InstrEXIT(InstrFactory inf, HCodeElement src) {
            super(inf, src, "!--method exit point--");
        }
    }

    /** Determine whether a constant can fit in the immediate field of
     *  a SPARC instruction. */
    static boolean is13bit(Number n) {
	if (n instanceof Double || n instanceof Float) return false;
	return ((-4096<=n.longValue()) && (n.longValue()<=4095));
    }

    static String storeSuffix(MEM mem) {
        String r = "";
        if (mem.isSmall()) {
            switch (mem.bitwidth()) {
            case 8: r += "b"; break;
            case 16: r += "h"; break;
            }
        } else {
            if (mem.isDoubleWord()) r += "d";
            /* should always use ld and ldd - as figures out floating
             * point by looking at registers in instruction.
             * if (mem.isFloatingPoint()) r += "f"; */
        }
        return r;
    }

    static String loadSuffix(MEM mem) {
        String r = "";
        if (mem.isSmall()) {
            r += (mem.signed()) ? "s" : "u";
            switch (mem.bitwidth()) {
            case 8: r += "b"; break;
            case 16: r += "h"; break;
            }
        } else {
            if (mem.isDoubleWord()) r += "d";
            /* should always use ld and ldd - as figures out floating 
             * point by looking at registers in instruction. 
             * if (mem.isFloatingPoint()) r += "f"; */
        }
        return r;
    }

    /** Crunch simple <code>Bop</code>'s down to sparc instruction. */
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
       this.instrFactory = inf;
       codeGenTempMap = new HashMap();
}%

%end with %{
       // What to execute at the end of the instruction
       // selection method

       Util.assert(root != null, "Should always generate some instrs");
       return root;
}%

ALIGN(n) %{
    emitDIRECTIVE( ROOT, "\t.align " + n);
}%

// BINOP checklist
//      	done		todo
// and: 	i,p,l
// xor: 	i,p,l
// or:  	i,p,l
// add: 	i,p,l,f,d
// "sub":	i,p,l,f,d		
// shl:		i,p,l		
// shr:		i,p,l		
// ushr:	i,p,l		
// mul:		i,p,l,f,d	
// div:		i,p,l,f,d		
// rem:		i,p,l		f,d
// cmplt:	i,p,l,f		d
// cmple:	i,p,l,f		d
// cmpeq:	i,p,l,f		d
// cmpge:	i,p,l,f		d
// cmpgt:	i,p,l,f		d

BINOP<i,p>(op, CONST(c), e)=r %pred %( isCommutative(op) && is13bit(c) )% %{
    emit (ROOT, bop(op)+" `s0, "+c+", `d0\n", 
                new Temp[] { r }, new Temp[] { e });
}%

BINOP<i,p>(op, e, CONST(c))=r %pred %( (isShift(op) || isCommutative(op)) && is13bit(c) )% %{
    emit (ROOT, bop(op)+" `s0, "+c+", `d0\n",
                new Temp[] { r }, new Temp[] { e });
}%

BINOP<l>(SHL, e1, e2)=r %{
    emit (ROOT, "mov `s0h, `d0\n", new Temp[] { r8 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0\n", new Temp[] { r9 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r10 }, new Temp[] { e2 });
    emit (ROOT, "call __ashldi3\n",
          new Temp[] { r1 }, new Temp[] { r8, r9, r10 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h\n", new Temp[] { r }, new Temp[] { r8 });
    emit (ROOT, "mov `s0, `d0l\n", new Temp[] { r }, new Temp[] { r9 });

}%

BINOP<l>(SHR, e1, e2)=r %{
    emit (ROOT, "mov `s0h, `d0\n", new Temp[] { r8 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0\n", new Temp[] { r9 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r10 }, new Temp[] { e2 });
    emit (ROOT, "call __ashrdi3\n",
          new Temp[] { r1 }, new Temp[] { r8, r9, r10 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h\n", new Temp[] { r }, new Temp[] { r8 });
    emit (ROOT, "mov `s0, `d0l\n", new Temp[] { r }, new Temp[] { r9 });

}%

BINOP<l>(USHR, e1, e2)=r %{
    emit (ROOT, "mov `s0h, `d0\n", new Temp[] { r8 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0\n", new Temp[] { r9 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r10 }, new Temp[] { e2 });
    emit (ROOT, "call __lshrdi3\n",
          new Temp[] { r1 }, new Temp[] { r8, r9, r10 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h\n", new Temp[] { r }, new Temp[] { r8 });
    emit (ROOT, "mov `s0, `d0l\n", new Temp[] { r }, new Temp[] { r9 });

}%

BINOP<i,p>(op, e1, e2)=r  %pred %( isShift(op) || isCommutative(op) )% %{
    emit (ROOT, bop(op)+" `s0, `s1, `d0\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(AND, e1, e2) = r %{
    emit (ROOT, "and `s0l, `s1l, `d0l\n", 
               new Temp[] { r }, new Temp[] { e1, e2 });
    emit (ROOT, "and `s0h, `s1h, `d0h\n",
               new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(XOR, e1, e2) = r %{
    emit (ROOT, "xor `s0l, `s1l, `d0l\n",
               new Temp[] { r }, new Temp[] { e1, e2 });
    emit (ROOT, "xor `s0h, `s1h, `d0h\n",
               new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(OR, e1, e2) = r %{
    emit (ROOT, "or `s0l, `s1l, `d0l\n",
               new Temp[] { r }, new Temp[] { e1, e2 });
    emit (ROOT, "or `s0h, `s1h, `d0h\n",
               new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(ADD, e1, e2) = r %{
    emitCC (ROOT, "addcc `s0l, `s1l, `d0l\n",
               new Temp[] { r }, new Temp[] { e1, e2 });
    emitCC (ROOT, "addx `s0h, `s1h, `d0h\n",
               new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<f,d>(ADD, e1, e2)=r %{
    String s = (ROOT.isDoubleWord()) ? "d" : "s";
    emit (ROOT, "fadd"+s+" `s0, `s1, `d0\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<i,p>(ADD, e1, UNOP(NEG, e2))=r /* subtraction */ %{
    emit (ROOT, "sub `s0, `s1, `d0\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(ADD, e1, UNOP(NEG, e2))=r %{
    emitCC (ROOT, "subcc `s0l, `s1l, `d0l\n", 
                new Temp[] { r }, new Temp[] { e1, e2 });
    emitCC (ROOT, "subx `s0h, `s1h, `d0h\n", 
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<f,d>(ADD, e1, UNOP(NEG, e2))=r %{
    String s = (ROOT.isDoubleWord()) ? "d" : "s";
    emit (ROOT, "fsub"+s+" `s0, `s1, `d0\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(MUL, e1, e2) = r %{
    emit (ROOT, "mov `s0h, `d0\n", new Temp[] { r8 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0\n", new Temp[] { r9 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0h, `d0\n", new Temp[] { r10 }, new Temp[] { e2 });
    emit (ROOT, "mov `s0l, `d0\n", new Temp[] { r11 }, new Temp[] { e2 });
    emit (ROOT, "call __muldi3\n",
          new Temp[] { r1 }, new Temp[] { r8, r9, r10, r11 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h\n", new Temp[] { r }, new Temp[] { r8 });
    emit (ROOT, "mov `s0, `d0l\n", new Temp[] { r }, new Temp[] { r9 });
}%

BINOP<i,p>(MUL, e1, e2) = r %{
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r8 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r9 }, new Temp[] { e2 });
    emit (ROOT, "call .mul\n", new Temp[] { r1 }, new Temp[] { r8, r9 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r }, new Temp[] { r8 });
}%

BINOP<f,d>(MUL, e1, e2)=r %{
    String s = (ROOT.isDoubleWord()) ? "d" : "s";
    emit (ROOT, "fmul"+s+" `s0, `s1, `d0\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(DIV, e1, e2) = r %{
    emit (ROOT, "mov `s0h, `d0\n", new Temp[] { r8 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0\n", new Temp[] { r9 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0h, `d0\n", new Temp[] { r10 }, new Temp[] { e2 });
    emit (ROOT, "mov `s0l, `d0\n", new Temp[] { r11 }, new Temp[] { e2 });
    emit (ROOT, "call __divdi3\n", 
          new Temp[] { r1 }, new Temp[] { r8, r9, r10, r11 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h\n", new Temp[] { r }, new Temp[] { r8 });
    emit (ROOT, "mov `s0, `d0l\n", new Temp[] { r }, new Temp[] { r9 });
}%

BINOP<i,p>(DIV, e1, e2) = r %{
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r8 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r9 }, new Temp[] { e2 });
    emit (ROOT, "call .div\n", new Temp[] { r1 }, new Temp[] { r8, r9 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r }, new Temp[] { r8 });
}%

BINOP<f,d>(DIV, e1, e2)=r %{
    String s = (ROOT.isDoubleWord()) ? "d" : "s";
    emit (ROOT, "fdiv"+s+" `s0, `s1, `d0\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(REM, e1, e2) = r %{
    emit (ROOT, "mov `s0h, `d0\n", new Temp[] { r8 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0\n", new Temp[] { r9 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0h, `d0\n", new Temp[] { r10 }, new Temp[] { e2 });
    emit (ROOT, "mov `s0l, `d0\n", new Temp[] { r11 }, new Temp[] { e2 });
    emit (ROOT, "call __moddi3\n",
          new Temp[] { r1 }, new Temp[] { r8, r9, r10, r11 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h\n", new Temp[] { r }, new Temp[] { r8 });
    emit (ROOT, "mov `s0, `d0l\n", new Temp[] { r }, new Temp[] { r9 });
}%

BINOP<i,p>(REM, e1, e2) = r %{
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r8 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r9 }, new Temp[] { e2 });
    emit (ROOT, "call .rem\n", new Temp[] { r1 }, new Temp[] { r8, r9 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r }, new Temp[] { r8 });
}%

BINOP(CMPLT, e1, e2) = r
%pred  %( ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, new Temp[] {});
    emit (ROOT, "cmp `s0, `s1\n" +
                "bge "+templabel+"\n", new Temp[] {}, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r } , new Temp[] {});
    emitLABEL(ROOT, templabel + ":", templabel);
}%

BINOP(CMPLE, e1, e2) = r
%pred  %( ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, null);
    emit (ROOT, "cmp `s0, `s1\n" +
                "bg "+templabel+"\n", new Temp[] {}, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r } , new Temp[] {});
    emitLABEL(ROOT, templabel + ":", templabel);
}%

BINOP(CMPEQ, e1, e2) = r
%pred  %( ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, new Temp[] {});
    emit (ROOT, "cmp `s0, `s1\n" +
                "bne "+templabel+"\n", new Temp[] {}, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r } , new Temp[] {});
    emitLABEL(ROOT, templabel + ":", templabel);
}%

BINOP(CMPGE, e1, e2) = r
%pred %( ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, new Temp[] {});
    emit (ROOT, "cmp `s0, `s1\n" +
                "bl "+templabel+"\n", new Temp[] {}, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r } , new Temp[] {});
    emitLABEL(ROOT, templabel + ":", templabel);
}%

BINOP(CMPGT, e1, e2) = r
%pred %( ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, new Temp[] {});
    emit (ROOT, "cmp `s0, `s1\n" +
                "ble "+templabel+"\n", new Temp[] {}, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r } , new Temp[] {});
    emitLABEL(ROOT, templabel + ":", templabel);
}%

BINOP(CMPLT, e1, e2) = r
%pred %( ROOT.operandType() == Type.LONG )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, null);
    emit (ROOT, "cmp `s0h, `s1h\n" +
                "bge "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "cmp `s0l, `s1l\n" +
                "bge "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPLE, e1, e2) = r
%pred %( ROOT.operandType() == Type.LONG )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, null);
    emit (ROOT, "cmp `s0h, `s1h\n" +
                "bg "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "cmp `s0l, `s1l\n" +
                "bg "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPEQ, e1, e2) = r
%pred %( ROOT.operandType() == Type.LONG )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, null);
    emit (ROOT, "cmp `s0h, `s1h\n" +
                "bne "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "cmp `s0l, `s1l\n" +
                "bne "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPGE, e1, e2) = r
%pred %( ROOT.operandType() == Type.LONG )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, null);
    emit (ROOT, "cmp `s0h, `s1h\n" +
                "bl "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "cmp `s0l, `s1l\n" +
                "bl "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPGT, e1, e2) = r
%pred %( ROOT.operandType() == Type.LONG )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, null);
    emit (ROOT, "cmp `s0h, `s1h\n" +
                "ble "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "cmp `s0l, `s1l\n" +
                "ble "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPLT, e1, e2) = r
%pred %( ROOT.operandType() == Type.FLOAT )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, null);
    emit (ROOT, "fcmps `s0, `s1\n" +
                "nop\n" +
                "fbge "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPLE, e1, e2) = r
%pred %( ROOT.operandType() == Type.FLOAT )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, null);
    emit (ROOT, "fcmps `s0, `s1\n" +
                "nop\n" +
                "fbg "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPEQ, e1, e2) = r
%pred %( ROOT.operandType() == Type.FLOAT )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, null);
    emit (ROOT, "fcmps `s0, `s1\n" +
                "nop\n" +
                "fbne "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPGE, e1, e2) = r
%pred %( ROOT.operandType() == Type.FLOAT )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, null);
    emit (ROOT, "fcmps `s0, `s1\n" +
                "nop\n" +
                "fbl "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPGT, e1, e2) = r
%pred %( ROOT.operandType() == Type.FLOAT )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0\n", new Temp[] { r }, null);
    emit (ROOT, "fcmps `s0, `s1\n" +
                "nop\n" +
                "fble "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

CALL(retval, retex, NAME(func), arglist, handler) 
%pred %( !ROOT.isTailCall )% 
%{
    /* AAA - Move paramaters into place, et al. */

    emitDIRECTIVE(ROOT, "\t! coming soon: CALL support\n");
}%

// true_label and false_label are harpoon.Temp.Labels, not Exps...
CJUMP(e, true_label, false_label) %{
    emitCC (ROOT, "cmp `s0, 0\n", null, new Temp[] { e });
    emitCC (ROOT, "bne " + true_label + "\n", null, null,
                  new Label[] { true_label }); 
    emitDELAYSLOT (ROOT);

    // should be able to optimize these away. 
    emitCCNoFall (ROOT, "ba " + false_label + "\n", null, null,
                        new Label[] { false_label }); 
    emitDELAYSLOT (ROOT);
}%

CONST<l,d>(c)=r %{
    long val = (ROOT.type() == Type.LONG)
               ? c.longValue()
               : Double.doubleToLongBits(c.floatValue());
    int low = (int)val;
    int high = (int)(val >> 32);
    emit (ROOT, "set " + low + ", `d0l\n", new Temp[] { r }, null);
    emit (ROOT, "set " + high + ", `d0h\n", new Temp[] { r }, null);
}%

CONST<i,f>(c)=r %{
    int val = (ROOT.type() == Type.INT)
              ? ROOT.value.intValue()
              : Float.floatToIntBits(ROOT.value.floatValue());
    emit (ROOT, "set "+val+", `d0\n", new Temp[] { r }, null);
}%

CONST<p>(c) = r %{
   emit (ROOT, "mov `s0, `d0 ! null\n", new Temp[]{ r }, new Temp[] { r0 });
}%

CONST<i>(0)=r %{
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r }, new Temp[] { r0 });
}%

CONST<l>(0)=r %{
    emit (ROOT, "mov `s0, `d0l\n", new Temp[] { r }, new Temp[] { r0 });
    emit (ROOT, "mov `s0, `d0h\n", new Temp[] { r }, new Temp[] { r0 });
}%

DATA(CONST<i>(exp)) %{
    String lo = "0x" + Integer.toHexString(exp.intValue());
    emitDIRECTIVE (ROOT, "\t.word " + lo + " ! " + exp);
}%

DATA(CONST<l>(exp)) %{
    long val = exp.longValue();
    String lo = "0x" + Integer.toHexString((int) val);
    String hi = "0x" + Integer.toHexString((int) (val >> 32));
    emitDIRECTIVE (ROOT, "\t.word " + hi + " ! " + exp);
    emitDIRECTIVE (ROOT, "\t.word " + lo + " ! " + exp);
}%

DATA(CONST<s:8,u:8>(exp)) %{
    String chardesc = (exp.intValue() >= 32 && exp.intValue() < 127
                       && exp.intValue() != 96 /* backquotes */
                       && exp.intValue() != 34 /* double quotes */) ?
                       ("\t! char "+((char)exp.intValue())) : "";
    emitDIRECTIVE(ROOT, "\t.byte "+exp+chardesc);
}%

DATA(CONST<s:16,u:16>(exp)) %{
    String chardesc = (exp.intValue() >= 32 && exp.intValue() < 127
                       && exp.intValue() != 96 /* backquotes */
                       && exp.intValue() != 34 /* double quotes */) ?
                       ("\t! char "+((char)exp.intValue())) : "";
    emitDIRECTIVE(ROOT, "\t.short "+exp+chardesc);
}% 

DATA(CONST<p>(exp)) %{
    emitDIRECTIVE(ROOT, "\t.word 0 ! should always be null pointer constant");
}%

DATA(CONST<l>(exp)) %{
    long l = exp.longValue();
    String lo = "0x" + Integer.toHexString((int)l);
    String hi = "0x" + Integer.toHexString((int)(l >> 32));
    emitDIRECTIVE(ROOT, "\t.word " + hi + " ! hi (" + exp + ")");
    emitDIRECTIVE(ROOT, "\t.word " + lo + " ! lo (" + exp + ")");
}%

DATA(NAME(l)) %{
    emitDIRECTIVE(ROOT, "\t.word " + l);
}%

EXP(e1) %{
    /* throw away temp e1 (not used) */
}%

JUMP(NAME(l)) %{
    emitNoFall (ROOT, "ba " + l + "\n", null, null, new Label[] { l }); 
    emitDELAYSLOT (ROOT);
}%

JUMP(BINOP(ADD, CONST<i>(c), e))
%pred %( is13bit(c) )%
%{
    List labelList = LabelList.toList (ROOT.targets);
    emitNoFall (ROOT, "jmpl `s0 + "+c+", %g0\n", 
                      null, new Temp[] { e }, labelList);
    emitDELAYSLOT (ROOT);
}%

JUMP(BINOP(ADD, e, CONST<i>(c)))
%pred %( is13bit(c) )%
%{
    List labelList = LabelList.toList (ROOT.targets);
    emitNoFall (ROOT, "jmpl `s0 + "+c+", %g0\n", 
                      null, new Temp[] { e }, labelList);
    emitDELAYSLOT (ROOT);
}%

JUMP(BINOP(ADD, e1, e2)) %{
    List labelList = LabelList.toList (ROOT.targets);
    emitNoFall (ROOT, "jmpl `s0 + `s1, %g0\n", 
                      null, new Temp[] { e1, e2 }, labelList);
    emitDELAYSLOT (ROOT);
}%

JUMP(e) %{
    List labelList = LabelList.toList (ROOT.targets);
    emitNoFall (ROOT, "jmpl `s0, %g0\n", null, new Temp[] { e }, labelList);
    emitDELAYSLOT (ROOT);
}%

LABEL(l) %{
    emitLABEL (ROOT, l.toString()+":\n", ((LABEL) ROOT).label);
}%

// ld* [rs + immed], rd
MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(BINOP(PLUS, CONST<i>(c), e1)) = r %{
    String srcsuff = (ROOT.isDoubleWord()) ? "h" : "";
    emit (ROOT, "ld"+loadSuffix(ROOT) + " [ `s0 + "+c+" ], `d0"+srcsuff + "\n",
                new Temp[] { r }, new Temp[] { e1 });
}%

// ld* [rs + immed], rd
MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(BINOP(PLUS, e1, CONST<i>(c))) = r %{
    String srcsuff = (ROOT.isDoubleWord()) ? "h" : "";
    emit (ROOT, "ld"+loadSuffix(ROOT) + " [ `s0 + "+c+" ], `d0"+srcsuff + "\n",
                new Temp[] { r }, new Temp[] { e1 });
}%

// ld* [rs0 + rs1], rd
MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(BINOP(PLUS, e1, e2)) = r %{
    String srcsuff = (ROOT.isDoubleWord()) ? "h" : "";
    emit (ROOT, "ld"+loadSuffix(ROOT) + " [ `s0 + `s1 ], `d0"+srcsuff + "\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

// ld* [immed], rd
MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(CONST<i>(c)) = r %{
    String srcsuff = (ROOT.isDoubleWord()) ? "h" : "";
    emit (ROOT, "ld"+loadSuffix(ROOT) + " ["+c+"], `d0"+srcsuff + "\n",
                new Temp[] { r }, null);
}%

// ld* [rs], rd
MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(e) = r %{
    String srcsuff = (ROOT.isDoubleWord()) ? "h" : "";
    emit (ROOT, "ld"+loadSuffix(ROOT) + " [`s0], `d0"+srcsuff + "\n",
                new Temp[] { r }, new Temp[] { e });
}%

METHOD(params) %{
    int loc = 0;
    emitENTRY(ROOT);

    // skip param[0],the explicit 'exceptional return address'
    for (int i = 1; i < params.length; i++) {
        if (tb.isTwoWord(params[i])) {
            if (loc < 6) { // first half in register
                emit (ROOT, "mov `s0, `d0h\n",
                            new Temp[] { params[i] },
                            new Temp[] { regfile.getRegister(24+loc) });
            } else { // on stack
                emit (ROOT, "ld [`s0 + "+4*(loc-6)+92+"], `d0h\n",
                            new Temp[] { params[i] },
                            new Temp[] { SP });
            }

            if (loc < 5) { // second half in register
                emit (ROOT, "mov `s0, `d0l\n",
                            new Temp[] { params[i] },
                            new Temp[] { regfile.getRegister(25+loc) });
            } else { // on stack
                emit (ROOT, "ld [`s0 + "+4*(loc-6)+92+"], `d0l\n",
                            new Temp[] { params[i] },
                            new Temp[] { SP });
            }
            loc += 2;
        } else {
            if (loc < 6) { // in register
                emit (ROOT, "mov `s0, `d0\n", 
                            new Temp[] { params[i] }, 
                            new Temp[] { regfile.getRegister(24+loc) });
            } else { // on stack
                emitMEM (ROOT, "ld [`s0 + "+4*(loc-6)+92+"], `d0\n",
                               new Temp[] { params[i] }, 
                               new Temp[] { SP });
            }
            loc++;
        }
    }
}%

// st* rs2, [ rs0 + rs1 ]
MOVE(MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(BINOP(ADD, e1, e2)), e3) %{
    MEM dst = (MEM)(((MOVE)ROOT).getDst());
    String suff = (dst.isDoubleWord()) ? "h" : "";
    emitMEM (ROOT, "st"+storeSuffix(dst) + " `s2"+suff + ", [`s0 + `s1]\n",
                   null, new Temp[] { e1, e2, e3 });
}%

// st* rs1, [ rs0 + immed ]
MOVE(MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(BINOP(ADD, e1, CONST<i>(c))), e2)
%pred %( is13bit(c) )%
%{
    MEM dst = (MEM)(((MOVE)ROOT).getDst());
    String suff = (dst.isDoubleWord()) ? "h" : "";
    emitMEM (ROOT, "st"+storeSuffix(dst) + " `s1"+suff + ", [ `s0 + "+c+" ]\n",
                   null, new Temp[] { e1, e2 });
}%

// st* rs1, [ rs0 + immed ]
MOVE(MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(BINOP(ADD, CONST<i>(c), e1)), e2)
%pred %( is13bit(c) )%
%{
    MEM dst = (MEM)(((MOVE)ROOT).getDst());
    String suff = (dst.isDoubleWord()) ? "h" : "";
    emitMEM (ROOT, "st"+storeSuffix(dst) + " `s1"+suff + ", [ `s0 + "+c+" ]\n",
                   null, new Temp[] { e1, e2 });
}%

// st* rs, [ immed ]
MOVE(MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(CONST<i>(c)), e) 
%pred %( is13bit(c) )%
%{
    MEM dst = (MEM)(((MOVE)ROOT).getDst());
    String suff = (dst.isDoubleWord()) ? "h" : "";
    emitMEM (ROOT, "st"+storeSuffix(dst) + " `s0"+suff + ", [ "+c+" ]\n",
                   null, new Temp[] { e });
}%

// st* rs1, [ rs0 ]
MOVE(MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(e1), e2) %{
    MEM dst = (MEM)(((MOVE)ROOT).getDst());
    String suff = (dst.isDoubleWord()) ? "h" : "";
    emitMEM (ROOT, "st"+storeSuffix(dst) + " `s1"+suff + ", [`s0]\n",
                   null, new Temp[] { e1, e2 });
}%

MOVE<i,p>(e1, CONST<i>(c)) %pred %( is13bit(c) )% %{
    emit (ROOT, "mov "+c+", `d0\n",
                new Temp[] { e1 }, null);
}%

MOVE<i,p>(e1, e2) %{ /* catch-all */
   emit (ROOT, "mov `s0, `d0\n", new Temp[] { e1 }, new Temp[] { e2 });
}%

MOVE<l>(e1, e2) %{ /* long (pair of int) register move */
    emit (ROOT, "mov `s0l, `d0l\n", new Temp[] { e1 }, new Temp[] { e2 });
    emit (ROOT, "mov `s0h, `d0h\n", new Temp[] { e1 }, new Temp[] { e2 });
}%

MOVE<f>(e1, e2) %{ /* floating-point register move */
    emit(ROOT, "fmovs `s0, `d0\n", new Temp[] { e1 }, new Temp[] { e2 });
}%

MOVE<d>(e1, e2) %{ /* double (pair of fp) register move */
    emit (ROOT, "fmovs `s0l, `d0l\n", new Temp[] { e1 }, new Temp[] { e2 });
    emit (ROOT, "fmovs `s0h, `d0h\n", new Temp[] { e1 }, new Temp[] { e2 });
}%

NAME(s)=r %{
    emit (ROOT, "set " + s + ", `d0\n", new Temp[] { r }, null);
}%

/* AAA - to do */
NATIVECALL(retval, func, arglist) %{
    emitDIRECTIVE(ROOT, "\t! coming soon: NATIVECALL support\n");
}%    

RETURN(val) %{
    // Assume for now that this is non-leaf.
    // procFixup will need to change these to %o0 and %o1 if it is leaf...
    if (tb.isTwoWord(val)) {
        emit (ROOT, "mov `s0h, `d0\n", 
                    new Temp[] { regfile.getRegister(24) }, /* %i0 */
                    new Temp[] { val });
        emit (ROOT, "mov `s0l, `d0\n",
                    new Temp[] { regfile.getRegister(25) }, /* %i1 */
                    new Temp[] { val });
    } else { 
        emit (ROOT, "mov `s0, `d0\n",
                    new Temp[] { regfile.getRegister(24) }, /* %i0 */
                    new Temp[] { val });
    }
    emitEXIT(ROOT);
}%

// Output those segments, ooo yea baby

SEGMENT(CLASS) %{
    emitDIRECTIVE(ROOT, "\t.data 1\t!.section class");
}%

SEGMENT(CODE) %{
    emitDIRECTIVE(ROOT, "\t.text 0\t!.section code");
}%

SEGMENT(GC) %{
    emitDIRECTIVE(ROOT, "\t.data 2\t!.section gc");
}%

SEGMENT(INIT_DATA) %{
    emitDIRECTIVE(ROOT, "\t.data 3\t!.section init_data");
}%

SEGMENT(STATIC_OBJECTS) %{
    emitDIRECTIVE(ROOT, "\t.data 4\t!.section static_objects");
}%

SEGMENT(STATIC_PRIMITIVES) %{
    emitDIRECTIVE(ROOT, "\t.data 5\t!.section static_primitives");
}%

SEGMENT(STRING_CONSTANTS) %{
    emitDIRECTIVE(ROOT, "\t.data 6\t!.section string_constants");
}%

SEGMENT(STRING_DATA) %{
    emitDIRECTIVE(ROOT, "\t.data 7\t!.section string_data");
}%

SEGMENT(REFLECTION_OBJECTS) %{
    emitDIRECTIVE(ROOT, "\t.data 8\t!.section reflection_objects");
}%

SEGMENT(REFLECTION_DATA) %{
    emitDIRECTIVE(ROOT, "\t.data 9\t!.section reflection_data");
}%

SEGMENT(TEXT) %{
    emitDIRECTIVE(ROOT, "\t.text  \t!.section text");
}%

TEMP<p,i,f,l,d>(id) = i %{
    i = makeTemp(ROOT, id, inf.tempFactory());
}%

THROW(val, handler) %{
    // again, assume non-leaf for now - might have to change registers
    // in procFixup
    emit (ROOT, "mov `s0, `d0\n", 
                new Temp[] { regfile.getRegister(24) }, /* %i0 */
                new Temp[] { val });
    emit (ROOT, "call _lookup\n",
                new Temp[] { }, /* AAA - need clobbers list */
                new Temp[] { }, /* AAA - need uses list */
                true, null);
    emitEXIT (ROOT);
}%

// unary operator checklist
// op		done		todo
// NEG		i,p,l,f		d
// NOT				i,p,l,f,d
// _2B		i,p		l,f,d
// _2C		i,p		l,f,d
// _2S		i,p		l,f,d
// _2I		i,p,l,f		l,f,d
// _2L		i,p		l,f,d
// _2F		i,p		l,f,d
// _2D				i,p,l,f,d

UNOP<i,p>(NEG, e)=r %{
    emit (ROOT, "sub `s0, `s1, `d0\n", new Temp[] { r }, new Temp[] { r0, e });
}%

UNOP<l>(NEG, e)=r %{
    emitCC (ROOT, "subcc `s0, `s1l, `d0l\n", 
                  new Temp[] { r }, new Temp[] { r0, e });
    emitCC (ROOT, "subx `s0, `s1h, `d0h\n", 
                  new Temp[] { r }, new Temp[] { r0, e });
}%

UNOP<f>(NEG, e)=r %{
    emit (ROOT, "fnegs `s0, `d0\n", new Temp[] { r }, new Temp[] { e });
}%

UNOP(_2B, e) = r
%pred %(ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    emit (ROOT, "sll `s0, 24, `d0\n", new Temp[] { r }, new Temp[] { e });
    emit (ROOT, "sra `s0, 24, `d0\n", new Temp[] { r }, new Temp[] { e });
}%

UNOP(_2C, e) = r
%pred %(ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    emit (ROOT, "sll `s0, 16, `d0\n", new Temp[] { r }, new Temp[] { e });
    emit (ROOT, "sra `s0, 16, `d0\n", new Temp[] { r }, new Temp[] { e });
}%

UNOP(_2S, e) = r
%pred %(ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    emit (ROOT, "sll `s0, 16, `d0\n", new Temp[] { r }, new Temp[] { e });
    emit (ROOT, "srl `s0, 16, `d0\n", new Temp[] { r }, new Temp[] { e });
}%

UNOP(_2I, e) = r
%pred %(ROOT.operandType() == Type.FLOAT )%
%{
    emit (ROOT, "fstoi `s0, `d0\n", new Temp[] { r }, new Temp[] { e });
}%

UNOP(_2I, e) = r
%pred %(ROOT.operandType() == Type.LONG )%
%{
    emit (ROOT, "mov `s0l, `d0\n", new Temp[] { r } , new Temp[] { e });
}%

UNOP(_2I, e) = r
%pred %(ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r } , new Temp[] { e });
}%

UNOP(_2L, e) = r
%pred %(ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    emit (ROOT, "mov `s0, `d0l\n", new Temp[] { r }, new Temp[] { e });
    emit (ROOT, "sra `s0, 31, `d0h\n", new Temp[] { r } , new Temp[] { e });
}%

UNOP(_2F, e) = r
%pred %(ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    /* AAA - this is broken. need to do loads and stores, can't
       move directly from fp to normal registers. */
    emit (ROOT, "fitos `s0, `d0\n", new Temp[] { r }, new Temp[] { e });
}%
