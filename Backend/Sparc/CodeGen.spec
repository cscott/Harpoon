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
 * @version $Id: CodeGen.spec,v 1.1.2.14 1999-11-29 09:31:52 andyb Exp $
 */
%%
    private Instr root;
    private Instr last;
    private InstrFactory instrFactory;
    private final RegFileInfo regfile;

    // short variable names for commonly used register temps
    private Temp r0, r1, r8, r9;

    public CodeGen(Frame frame) {
        super(frame);
        this.regfile = (RegFileInfo) frame.getRegFileInfo();
        r0 = regfile.getRegister(0);
        r1 = regfile.getRegister(1);
        r8 = regfile.getRegister(8);
        r9 = regfile.getRegister(9);
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

    private Instr emitDIRECTIVE(HCodeElement root, String directive) {
        return emit(new InstrDIRECTIVE(instrFactory, root, directive));
    }
  
    private Instr emitENTRY(HCodeElement root) {
        return emit(new InstrENTRY(instrFactory, root));
    }

    private Instr emitEXIT(HCodeElement root) {
        return emit(new InstrEXIT(instrFactory, root));
    }

    // AAA - FIX BELOW THIS LINE

    public Instr procFixup(HMethod hm, Instr instr, int stackspace,
                           Set usedRegisters) {
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
	    super(inf, source, "nop  @ delay slot\n");
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

    private class InstrENTRY extends InstrDIRECTIVE {
        public InstrENTRY(InstrFactory inf, HCodeElement src) {
            super(inf, src, "@--method entry point - AAA to fix--");
        }
    }

    private class InstrEXIT extends InstrDIRECTIVE {
        public InstrEXIT(InstrFactory inf, HCodeElement src) {
            super(inf, src, "@--method exit point - AAA to fix--");
        }
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
// "sub":	i,p,f,d		l
// shl:		i,p		l
// shr:		i,p		l
// ushr:	i,p		l
// mul:		i,p,f,d		l
// div:		i,p,f,d		l
// rem:		i,p		l,f,d
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
    emit (ROOT, "addcc `s0l, `s1l, `d0l\n",
               new Temp[] { r }, new Temp[] { e1, e2 });
    emit (ROOT, "addx `s0h, `s1h, `d0h\n",
               new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<f,d>(ADD, e1, e2)=r %{
    String s = suffix(ROOT);
    emit (ROOT, "fadd"+s+" `s0, `s1, `d0\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<i,p>(ADD, e1, UNOP(NEG, e2))=r /* subtraction */ %{
    emit (ROOT, "sub `s0, `s1, `d0\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<f,d>(ADD, e1, UNOP(NEG, e2))=r %{
    String s = suffix(ROOT);
    emit (ROOT, "fsub"+s+" `s0, `s1, `d0\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(MUL, e1, e2) = r %{
    /* AAA - to be implemented. */
}%

BINOP<i,p>(MUL, e1, e2) = r %{
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r8 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r9 }, new Temp[] { e2 });
    emit (ROOT, "call .mul\n", new Temp[] { r1 }, new Temp[] { r8, r9 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r }, new Temp[] { r8 });
}%

BINOP<f,d>(MUL, e1, e2)=r %{
    String s = suffix(ROOT);
    emit (ROOT, "fmul"+s+" `s0, `s1, `d0\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(DIV, e1, e2) = r %{
    /* AAA - to be implemented. */
}%

BINOP<i,p>(DIV, e1, e2) = r %{
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r8 }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r9 }, new Temp[] { e2 });
    emit (ROOT, "call .div\n", new Temp[] { r1 }, new Temp[] { r8, r9 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0\n", new Temp[] { r }, new Temp[] { r8 });
}%

BINOP<f,d>(DIV, e1, e2)=r %{
    String s = suffix(ROOT);
    emit (ROOT, "fdiv"+s+" `s0, `s1, `d0\n",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(REM, e1, e2) = r %{
    /* AAA - to be implemented. */
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
                "fbges "+templabel+"\n", null, new Temp[] { e1, e2 });
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
                "fbgs "+templabel+"\n", null, new Temp[] { e1, e2 });
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
                "fbnes "+templabel+"\n", null, new Temp[] { e1, e2 });
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
                "fbls "+templabel+"\n", null, new Temp[] { e1, e2 });
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
                "fbles "+templabel+"\n", null, new Temp[] { e1, e2 });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0\n", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

CALL(retval, retex, func, arglist, handler) %pred %( !ROOT.isTailCall )% %{
    emitDIRECTIVE(ROOT, "\t@ coming soon: CALL support\n");
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

/* AAA - SPARC book says that assembler will automatically do the
   right thing? need to check this out.

    if (is13bit(c)) {
        emit (ROOT, "set "+val+", `d0\n", new Temp[] {r}, new Temp[] {});
    } else {
        emit (ROOT, "sethi %hi("+val+"), `d0\n", new Temp[]{r}, null);
        emit (ROOT, "or `s0, %lo("+val+"), `d0\n",
              new Temp[] { r }, new Temp[] { r });
    }
*/

}%

CONST<p>(c) = r %{
   emit (ROOT, "mov `s0, `d0 @ null\n", new Temp[]{ r }, new Temp[] { r0 });
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
    emitDIRECTIVE (ROOT, "\t.word " + lo + " @ " + exp);
}%

DATA(CONST<l>(exp)) %{
    long val = exp.longValue();
    String lo = "0x" + Integer.toHexString((int) val);
    String hi = "0x" + Integer.toHexString((int) (val >> 32));
    emitDIRECTIVE (ROOT, "\t.word " + hi + " @ " + exp);
    emitDIRECTIVE (ROOT, "\t.word " + lo + " @ " + exp);
}%

DATA(CONST<s:8,u:8>(exp)) %{
    String chardesc = (exp.intValue() >= 32 && exp.intValue() < 127
                       && exp.intValue() != 96 /* backquotes */
                       && exp.intValue() != 34 /* double quotes */) ?
                       ("\t@ char "+((char)exp.intValue())) : "";
    emitDIRECTIVE(ROOT, "\t.byte "+exp+chardesc);
}%

DATA(CONST<s:16,u:16>(exp)) %{
    String chardesc = (exp.intValue() >= 32 && exp.intValue() < 127
                       && exp.intValue() != 96 /* backquotes */
                       && exp.intValue() != 34 /* double quotes */) ?
                       ("\t@ char "+((char)exp.intValue())) : "";
    emitDIRECTIVE(ROOT, "\t.short "+exp+chardesc);
}% 

DATA(CONST<p>(exp)) %{
    emitDIRECTIVE(ROOT, "\t.word 0 @ should always be null pointer constant");
}%

DATA(CONST<l>(exp)) %{
    long l = exp.longValue();
    String lo = "0x" + Integer.toHexString((int)l);
    String hi = "0x" + Integer.toHexString((int)(l >> 32));
    emitDIRECTIVE(ROOT, "\t.word " + hi + " @ hi (" + exp + ")");
    emitDIRECTIVE(ROOT, "\t.word " + lo + " @ lo (" + exp + ")");
}%

DATA(NAME(l)) %{
    emitDIRECTIVE(ROOT, "\t.word " + l);
}%

EXP(e1) %{
    /* throw away temp e1 (not used) */
}%

LABEL(l) %{
    emitLABEL (ROOT, l.toString()+":\n", ((LABEL) ROOT).label);
}%

/* AAA - look at these

MEM(BINOP(PLUS, CONST<l,i>(c), e1))=r %pred %( is13bit(c) )% %{
 emit (ROOT, "ld"+suffix(ROOT)+" [`s0+"+c+"], `d0\n",
       new Temp[] { r }, new Temp[] { e1 });
}%

MEM(BINOP(PLUS, e1, CONST<l,i>(c)))=r %pred %( is13bit(c) )% %{
 emit (ROOT, "ld"+suffix(ROOT)+" [`s0+"+c+"], `d0\n",
       new Temp[] { r }, new Temp[] { e1 });
}%

MEM(BINOP(PLUS, e1, e2))=r %{
 emit (ROOT, "ld"+suffix(ROOT)+" [`s0+`s1], `d0\n",
       new Temp[] { r }, new Temp[] { e1, e2 });
}%

MEM(CONST<l,i>(c))=r %pred %( is13bit(c) )% %{
 emit (ROOT, "ld ["+c+"], `d0\n",
       new Temp[] { r }, new Temp[] {});
}%

*/

MEM<i,p>(e)=r %{
    emit (ROOT, "ld [`s0], `d0\n", new Temp[] { r }, new Temp[] { e });
}%

MEM<l>(e)=r %{
    emit (ROOT, "ldd [`s0], `d0l\n", new Temp[] { r }, new Temp[] { e });
}%

MEM<f>(e)=r %{
    emit (ROOT, "ldf [`s0], `d0\n", new Temp[] { r } , new Temp[] { e });
}%

MEM<d>(e)=r %{
    emit (ROOT, "lddf [`s0], `d0l\n", new Temp[] { r }, new Temp[] { e });
}%

MEM<u:8>(e) = r %{
    emitMEM (ROOT, "ldub [`s0], `d0\n", new Temp[] { r }, new Temp[] { e });
}%

MEM<s:8>(e) = r %{
    emitMEM (ROOT, "ldsb [`s0], `d0\n", new Temp[] { r }, new Temp[] { e });
}%

MEM<u:16>(e) = r %{
    emitMEM (ROOT, "lduh [`s0], `d0\n", new Temp[] { r }, new Temp[] { e });
}%

MEM<s:16>(e) = r %{
    emitMEM (ROOT, "ldsh [`s0], `d0\n", new Temp[] { r }, new Temp[] { e });
}%

METHOD(params) %{
    emitENTRY(ROOT);
    /* AAA - move params into right place */
}%

MOVE(MEM<s:8, u:8>(e), src) %{
    emitMEM (ROOT, "stb `s1, [`s0]\n", null, new Temp[] { e, src });
}%

MOVE(MEM<s:16, u:16>(e), src) %{
    emitMEM (ROOT, "sth `s1, [`s0]\n", null, new Temp[] { e, src });
}%

MOVE(MEM<i,p>(e), src) %{
    emitMEM (ROOT, "st `s1, [`s0]\n", null, new Temp[] { e, src });
}%

MOVE(MEM<l>(e), src) %{
    emitMEM (ROOT, "std `s1l, [`s0]\n", null, new Temp[] { e, src });
}%

/* AAA - look at these 

MOVE(MEM(CONST<l,i>(c)), e) %pred %( is13bit(c) )% %{
 emitMEM (ROOT, "st"+suffix((Typed)ROOT.dst)+" `s0l, ["+c+"]\n",
          new Temp[] {}, new Temp[] { e });
}%

MOVE(MEM(BINOP(ADD, CONST<l,i>(c), e1)), e2) %pred %( is13bit(c) )% %{
 emitMEM (ROOT, "st"+suffix((Typed)ROOT.dst)+" `s1l, [`s0+"+c+"]\n",
          new Temp[] {}, new Temp[] { e1, e2 });
}%

MOVE(MEM(BINOP(ADD, e1, CONST<l,i>(c))), e2) %pred %( is13bit(c) )% %{
 emitMEM (ROOT, "st"+suffix((Typed)ROOT.dst)+" `s1l, [`s0+"+c+"]\n",
          new Temp[] {}, new Temp[] { e1, e2 });
}%

MOVE(MEM(BINOP(ADD, e1, e2)), e3) %{
 emitMEM (ROOT, "st"+suffix((Typed)ROOT.dst)+" `s2l, [`s0+`s1]\n",
          new Temp[] {}, new Temp[] { e1, e2, e3 });
}%

MOVE(MEM(e1), e2) %{
 emitMEM (ROOT, "st"+suffix((Typed)ROOT.dst)+" `s1l, [`s0]\n",
          new Temp[] {}, new Temp[] { e1, e2 });
}%

MOVE(e1, CONST<i>(c)) %pred %( is13bit(c) )% %{
 emit (ROOT, "mov "+c+", `d0\n",
       new Temp[] { e1 }, new Temp[] {});
}%

*/

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

/* AAA - again, I may be smoking something, but from reading
   the SPARC book, this is a bit much.
 emit (ROOT, "sethi %hi("+s+"), `d0\n",
       new Temp[]{r}, null);
 emit (ROOT, "or `s0, %lo("+s+"), `d0\n",
       new Temp[] { r }, new Temp[] { r });
*/
}%

NATIVECALL(retval, func, arglist) %{
    emitDIRECTIVE(ROOT, "\t@ coming soon: NATIVECALL support\n");
}%    

RETURN(val) %{
    /* AAA - put return value in right place */
    emitEXIT(ROOT);
}%

// Output those segments, ooo yea baby

SEGMENT(CLASS) %{
    emitDIRECTIVE(ROOT, "\t.data 1\t@.section class");
}%

SEGMENT(CODE) %{
    emitDIRECTIVE(ROOT, "\t.text 0\t@.section code");
}%

SEGMENT(GC) %{
    emitDIRECTIVE(ROOT, "\t.data 2\t@.section gc");
}%

SEGMENT(INIT_DATA) %{
    emitDIRECTIVE(ROOT, "\t.data 3\t@.section init_data");
}%

SEGMENT(STATIC_OBJECTS) %{
    emitDIRECTIVE(ROOT, "\t.data 4\t@.section static_objects");
}%

SEGMENT(STATIC_PRIMITIVES) %{
    emitDIRECTIVE(ROOT, "\t.data 5\t@.section static_primitives");
}%

SEGMENT(STRING_CONSTANTS) %{
    emitDIRECTIVE(ROOT, "\t.data 6\t@.section string_constants");
}%

SEGMENT(STRING_DATA) %{
    emitDIRECTIVE(ROOT, "\t.data 7\t@.section string_data");
}%

SEGMENT(REFLECTION_OBJECTS) %{
    emitDIRECTIVE(ROOT, "\t.data 8\t@.section reflection_objects");
}%

SEGMENT(REFLECTION_DATA) %{
    emitDIRECTIVE(ROOT, "\t.data 9\t@.section reflection_data");
}%

SEGMENT(TEXT) %{
    emitDIRECTIVE(ROOT, "\t.text  \t@.section text");
}%

TEMP<p,i,f,l,d>(id) = i %{
    i = frame.getTempBuilder().makeTemp(ROOT, inf.tempFactory());
}%

THROW(val, handler) %{
    /* AAA - lookup destination here */
    emitEXIT(ROOT);
}%

// unary operator checklist

UNOP<i,p>(NEG, e)=r %{
    emit (ROOT, "sub `s0, `s1, `d0\n", new Temp[] { r }, new Temp[] { r0, e });
}%

UNOP<l>(NEG, e)=r %{
    emit (ROOT, "subcc `s0, `s1l, `d0l\n", new Temp[] { r }, new Temp[] { r0, e });
    emit (ROOT, "subx `s0, `s1h, `d0h\n", new Temp[] { r }, new Temp[] { r0, e });
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

// OLD STUFF BELOW THIS LINE

JUMP(NAME(l)) %{
 emit (ROOT, "b "+l+"\n", null, null); /* AAA - target list?! */
 emitDELAYSLOT (ROOT);
}%

JUMP(BINOP(ADD, e1, e2)) %{
 emit (ROOT, "jmpl `s0+`s1, %g0\n", 
      null,  new Temp[] { e1, e2 }); /* AAA - target list?! */
 emitDELAYSLOT (ROOT);
}%

JUMP(e1) %{
 emit (ROOT, "jmpl `s0, %g0\n", 
       null, new Temp[] { e1 }); /* AAA - target list?! */
 emitDELAYSLOT (ROOT);
}%

// patterns with a CJUMP at their root.

CJUMP(e, true_label, false_label) %{
 emitCC (ROOT, "cmp `s0, 0\n", 
         null,  new Temp[] { e });
 emitCC (ROOT, "bne "+true_label+"\n", null, null); /*target!?*/
 emitDELAYSLOT (ROOT);

 /* the next two lines can hopefully be left out. */
 emitCC (ROOT, "ba "+false_label+"\n", null, null); /*target?!*/
 emitDELAYSLOT (ROOT);
}%
