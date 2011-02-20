// CodeGen.spec, created Mon Jun 28 23:00:48 1999 by cananian -*- Java -*-
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HClass;
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
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.EXPR;
import harpoon.IR.Tree.INVOCATION;
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
import harpoon.Temp.TempList;
import harpoon.Temp.Label;
import harpoon.Temp.LabelList;
import harpoon.Util.Util;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <code>Sparc.CodeGen</code> is a code-generator for the Sparc architecture.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: CodeGen.spec,v 1.4 2002-04-10 03:03:51 cananian Exp $
 */
%%
    private InstrFactory instrFactory;
    private final RegFileInfo regfile;
    private final TempBuilder tb;

    // short variable names for commonly used register temps
    private Temp[] regg = new Temp[8];
    private Temp[] rego = new Temp[8];
    private Temp[] regl = new Temp[8];
    private Temp[] regi = new Temp[8];
    private Temp SP, FP;

    private Map codeGenTempMap;

    public CodeGen(Frame frame) {
        super(frame);
        this.regfile = (RegFileInfo) frame.getRegFileInfo();
        this.tb = (TempBuilder) frame.getTempBuilder();
	for (int i = 0; i < 8; i++) {
	    regg[i] = regfile.getRegister(i);
	    rego[i] = regfile.getRegister(i+8);
	    regl[i] = regfile.getRegister(i+16);
	    regi[i] = regfile.getRegister(i+24);
        }
        SP = regfile.SP();
        FP = regfile.FP();
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
    // the next instruction - i.e. absolute jumps without any sort
    // of return address stored somewhere.
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

    private void emitCallPrologue(HCodeElement ROOT, TempList arglist,
				  Label handler, boolean passhandler) {
        /* AAA - currently only deals with up to 6 arguments */
	TempList argrev = null;
	int wordsused = 0;	
        
        // count number of words required by arguments and
        // reverse the argument lists.
        for (TempList tl = arglist; tl != null; tl = tl.tail) {
	    argrev = new TempList(tl.head, argrev);
	    wordsused += tb.isTwoWord(tl.head) ? 2 : 1;
        }

	// Add one more word used - to pass exceptional return address
	if (passhandler) {
	    wordsused++;
	}

        // move arguments into place
        for (TempList tl = argrev; tl != null; tl = tl.tail) {
            Temp temp = tl.head;
	    if (tb.isTwoWord(temp)) {
		if (wordsused > 7) { /* two stack */
		    assert false : ("emitCallPrologue: too many arguments");
		} else if (wordsused == 7) { /* one reg, one stack */
		    assert false : ("emitCallPrologue: too many arguments");
		} else { /* two reg */
		    declare(rego[wordsused - 2], HClass.Void);
		    declare(rego[wordsused - 1], HClass.Void);
		    emit (ROOT, "mov `s0h, `d0",
				new Temp[] { rego[wordsused - 2] },
				new Temp[] { temp });
		    emit (ROOT, "mov `s0l, `d0",
				new Temp[] { rego[wordsused - 1] },
				new Temp[] { temp });
		}
		wordsused -= 2;
	    } else {
		if (wordsused > 6) { /* on stack */
		    assert false : ("emitCallPrologue: too many arguments");
		} else { /* in reg */
		    declare(rego[wordsused - 1], HClass.Void);
		    emit (ROOT, "mov `s0, `d0", 
				new Temp[] { rego[wordsused - 1] },
				new Temp[] { temp });
		}
		wordsused--;
	    }
        }

	// Put the excceptional return address in %o0
	if (passhandler) {
	    declare(rego[0], HClass.Void);
	    emit (ROOT, "set "+handler+", `d0",
		        new Temp[] { rego[0] },
		        new Temp[] { });
	    wordsused--;
	}

	assert wordsused == 0 : ("emitCallPrologue: all args not in place");
    }

    private void emitCallEpilogue(INVOCATION ROOT, Temp retval, HClass type) {
	/* AAA - need to adjust SP if args were put on stack */

        if (ROOT.getRetval() == null) {
	    // don't bother emiting move for void methods
        } else if (ROOT.getRetval().isDoubleWord()) {
	    declare(retval, type);
	    emit (ROOT, "mov `s0, `d0h",
			new Temp[] { retval },
			new Temp[] { rego[0] });
	    emit (ROOT, "mov `s0, `d0l",
			new Temp[] { retval },
			new Temp[] { rego[1] });
	} else {
	    declare(retval, type);
	    emit (ROOT, "mov `s0, `d0", 
			new Temp[] { retval }, 
			new Temp[] { rego[0] });
	}
    }

    private void emitCallFixupTable(HCodeElement ROOT, Label norm, Label exc) {
        emitDIRECTIVE (ROOT, ".text 10\t! .section fixup");
	emitDIRECTIVE (ROOT, "\t.word "+norm+", "+exc);
	emitDIRECTIVE (ROOT, ".text 0 \t! .section code");
    }

    private void emitHandlerStub(HCodeElement ROOT, Temp retex, Label handler) {
	if (tb.isTwoWord(retex)) {
	    declare(rego[0], HClass.Void);
	    declare(rego[1], HClass.Void);
	    emit (ROOT, "mov `s0h, `d0",
			new Temp[] { retex },
			new Temp[] { rego[0] });
	    emit (ROOT, "mov `s0l, `d0",
			new Temp[] { retex },
			new Temp[] { rego[1] });
	} else {
	    declare(rego[0], HClass.Void);
	    emit (ROOT, "mov `s0, `d0",
			new Temp[] { retex },
			new Temp[] { rego[0] });
	}
        emitNoFall (ROOT, "ba " + handler, null, null, new Label[] { handler });
    }

    public Instr procFixup(HMethod hm, Instr instr, int stackspace,
                           Set usedRegisters) {
	InstrFactory inf = instrFactory; // convenient abbreviation.
	Label methodlabel = frame.getRuntime().getNameMap().label(hm);

	for (Instr i = instr; i != null; i = i.getNext()) {
	    if (i instanceof InstrENTRY) { // entry stub
		Instr in1 = new InstrDIRECTIVE(inf, i, "\t.align 4");
		Instr in2 = new InstrDIRECTIVE(inf, i, "\t.global " +
					       methodlabel.name);
		Instr in3 = new InstrDIRECTIVE(inf, i, "\t.type " +
					       methodlabel.name +",#function");
		Instr in4 = new InstrLABEL(inf, i, methodlabel.name+":",
					   methodlabel);

		/* AAA - the 92 assumes no more than 6 args I think */
		int save_offset = 92 + 4 * stackspace;

		// save clobbers just about everything
		List save_clob = new ArrayList();
		save_clob.addAll(Arrays.asList(regi));
		save_clob.addAll(Arrays.asList(regl));
		save_clob.addAll(Arrays.asList(rego));
		Instr in5 = new Instr(inf, i, 
				      "save %sp, -" + save_offset + ", %sp",
			              (Temp[])save_clob.toArray(new Temp[24]), 
				      rego); 
		in5.layout(i, i.getNext());
		in4.layout(i, in5);
		in3.layout(i, in4);
		in2.layout(i, in3);
		in1.layout(i, in2);
		if (i == instr) instr = in1;
		i.remove(); i = in1;
	    }
	    if (i instanceof InstrEXIT) { // exit stub
		// ret == jmpl %i7 + 8, %g0
		Instr in1 = new Instr(inf, i, "ret", 
				      null, new Temp[] { regi[7] });

		List restore_clobbers = new ArrayList();
		restore_clobbers.addAll(Arrays.asList(regi));
		restore_clobbers.addAll(Arrays.asList(regl));
		restore_clobbers.addAll(Arrays.asList(rego));
		Instr in2 = new Instr(inf, i, "restore", 
				      (Temp[])restore_clobbers.toArray(new Temp[24]), 
				      regi);
		in1.layout(i.getPrev(), i);
		in2.layout(in1, i);
		i.remove();
		i = in2;
	    }
        }
	return instr;
    }

    // INNER CLASSES

    /** Sub-class to represent delay-slots.
     * <code>optimize()</code> uses this class information to determine that
     * it should rearrange code to try to eliminate these instructions.
     * @author C. Scott Ananian <cananian@alumni.princeton.edu>
     * @see Sparc.CodeGen#optimize
     */
    public class InstrDELAYSLOT extends Instr {
	// a nop to fill the delay slot
	public InstrDELAYSLOT(InstrFactory inf, HCodeElement source) {
	    super(inf, source, "nop  ! delay slot");
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
	assert false;
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

       this.instrFactory = inf;
       codeGenTempMap = new HashMap();
}%

%end with %{
       // What to execute at the end of the instruction
       // selection method
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
    emit (ROOT, bop(op)+" `s0, "+c+", `d0", 
                new Temp[] { r }, new Temp[] { e });
}%

BINOP<i,p>(op, e, CONST(c))=r %pred %( (isShift(op) || isCommutative(op)) && is13bit(c) )% %{
    emit (ROOT, bop(op)+" `s0, "+c+", `d0",
                new Temp[] { r }, new Temp[] { e });
}%

BINOP<l>(SHL, e1, e2)=r %{
    declare(rego[0], HClass.Void);
    declare(rego[1], HClass.Void);
    declare(rego[2], HClass.Int);
    emit (ROOT, "mov `s0h, `d0", new Temp[] { rego[0] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0", new Temp[] { rego[1] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0", new Temp[] { rego[2] }, new Temp[] { e2 });
    emit (ROOT, "call __ashldi3",
          new Temp[] { rego[0], rego[1] }, new Temp[] { rego[0], rego[1], rego[2] });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h", new Temp[] { r }, new Temp[] { rego[0] });
    emit (ROOT, "mov `s0, `d0l", new Temp[] { r }, new Temp[] { rego[1] });

}%

BINOP<l>(SHR, e1, e2)=r %{
    declare(rego[0], HClass.Void);
    declare(rego[1], HClass.Void);
    declare(rego[2], HClass.Int);
    emit (ROOT, "mov `s0h, `d0", new Temp[] { rego[0] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0", new Temp[] { rego[1] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0", new Temp[] { rego[2] }, new Temp[] { e2 });
    emit (ROOT, "call __ashrdi3",
          new Temp[] { rego[0], rego[1] }, new Temp[] { rego[0], rego[1], rego[2] });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h", new Temp[] { r }, new Temp[] { rego[0] });
    emit (ROOT, "mov `s0, `d0l", new Temp[] { r }, new Temp[] { rego[1] });

}%

BINOP<l>(USHR, e1, e2)=r %{
    declare(rego[0], HClass.Void);
    declare(rego[1], HClass.Void);
    declare(rego[2], HClass.Int);
    emit (ROOT, "mov `s0h, `d0", new Temp[] { rego[0] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0", new Temp[] { rego[1] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0", new Temp[] { rego[2] }, new Temp[] { e2 });
    emit (ROOT, "call __lshrdi3",
          new Temp[] { rego[0], rego[1] }, new Temp[] { rego[0], rego[1], rego[2] });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h", new Temp[] { r }, new Temp[] { rego[0] });
    emit (ROOT, "mov `s0, `d0l", new Temp[] { r }, new Temp[] { rego[1] });

}%

BINOP<i,p>(op, e1, e2)=r  %pred %( isShift(op) || isCommutative(op) )% %{
    emit (ROOT, bop(op)+" `s0, `s1, `d0",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(AND, e1, e2) = r %{
    emit (ROOT, "and `s0l, `s1l, `d0l", 
               new Temp[] { r }, new Temp[] { e1, e2 });
    emit (ROOT, "and `s0h, `s1h, `d0h",
               new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(XOR, e1, e2) = r %{
    emit (ROOT, "xor `s0l, `s1l, `d0l",
               new Temp[] { r }, new Temp[] { e1, e2 });
    emit (ROOT, "xor `s0h, `s1h, `d0h",
               new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(OR, e1, e2) = r %{
    emit (ROOT, "or `s0l, `s1l, `d0l",
               new Temp[] { r }, new Temp[] { e1, e2 });
    emit (ROOT, "or `s0h, `s1h, `d0h",
               new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(ADD, e1, e2) = r %{
    emitCC (ROOT, "addcc `s0l, `s1l, `d0l",
               new Temp[] { r }, new Temp[] { e1, e2 });
    emitCC (ROOT, "addx `s0h, `s1h, `d0h",
               new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<f,d>(ADD, e1, e2)=r %{
    String s = (ROOT.isDoubleWord()) ? "d" : "s";
    emit (ROOT, "fadd"+s+" `s0, `s1, `d0",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<i,p>(ADD, e1, UNOP(NEG, e2))=r /* subtraction */ %{
    emit (ROOT, "sub `s0, `s1, `d0",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(ADD, e1, UNOP(NEG, e2))=r %{
    emitCC (ROOT, "subcc `s0l, `s1l, `d0l", 
                new Temp[] { r }, new Temp[] { e1, e2 });
    emitCC (ROOT, "subx `s0h, `s1h, `d0h", 
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<f,d>(ADD, e1, UNOP(NEG, e2))=r %{
    String s = (ROOT.isDoubleWord()) ? "d" : "s";
    emit (ROOT, "fsub"+s+" `s0, `s1, `d0",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(MUL, e1, e2) = r %{
    declare(rego[0], HClass.Void);
    declare(rego[1], HClass.Void);
    declare(rego[2], HClass.Void);
    declare(rego[3], HClass.Void);
    emit (ROOT, "mov `s0h, `d0", new Temp[] { rego[0] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0", new Temp[] { rego[1] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0h, `d0", new Temp[] { rego[2] }, new Temp[] { e2 });
    emit (ROOT, "mov `s0l, `d0", new Temp[] { rego[3] }, new Temp[] { e2 });
    emit (ROOT, "call __muldi3",
          new Temp[] { rego[0], rego[1] }, new Temp[] { rego[0], rego[1], rego[2], rego[3] });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h", new Temp[] { r }, new Temp[] { rego[0] });
    emit (ROOT, "mov `s0, `d0l", new Temp[] { r }, new Temp[] { rego[1] });
}%

BINOP<i,p>(MUL, e1, e2) = r %{
    declare(rego[0], HClass.Int);
    declare(rego[1], HClass.Int);
    emit (ROOT, "mov `s0, `d0", new Temp[] { rego[0] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0", new Temp[] { rego[1] }, new Temp[] { e2 });
    emit (ROOT, "call .mul", new Temp[] { rego[0] }, new Temp[] { rego[0], rego[1] });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0", new Temp[] { r }, new Temp[] { rego[0] });
}%

BINOP<f,d>(MUL, e1, e2)=r %{
    String s = (ROOT.isDoubleWord()) ? "d" : "s";
    emit (ROOT, "fmul"+s+" `s0, `s1, `d0",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(DIV, e1, e2) = r %{
    declare(rego[0], HClass.Void);
    declare(rego[1], HClass.Void);
    declare(rego[2], HClass.Void);
    declare(rego[3], HClass.Void);
    emit (ROOT, "mov `s0h, `d0", new Temp[] { rego[0] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0", new Temp[] { rego[1] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0h, `d0", new Temp[] { rego[2] }, new Temp[] { e2 });
    emit (ROOT, "mov `s0l, `d0", new Temp[] { rego[3] }, new Temp[] { e2 });
    emit (ROOT, "call __divdi3", 
          new Temp[] { rego[0], rego[1] }, new Temp[] { rego[0], rego[1], rego[2], rego[3] });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h", new Temp[] { r }, new Temp[] { rego[0] });
    emit (ROOT, "mov `s0, `d0l", new Temp[] { r }, new Temp[] { rego[1] });
}%

BINOP<i,p>(DIV, e1, e2) = r %{
    declare(rego[0], HClass.Int);
    declare(rego[1], HClass.Int);
    emit (ROOT, "mov `s0, `d0", new Temp[] { rego[0] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0", new Temp[] { rego[1] }, new Temp[] { e2 });
    emit (ROOT, "call .div", new Temp[] { rego[0]}, new Temp[] { rego[0], rego[1] });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0", new Temp[] { r }, new Temp[] { rego[0] });
}%

BINOP<f,d>(DIV, e1, e2)=r %{
    String s = (ROOT.isDoubleWord()) ? "d" : "s";
    emit (ROOT, "fdiv"+s+" `s0, `s1, `d0",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

BINOP<l>(REM, e1, e2) = r %{
    declare(rego[0], HClass.Void);
    declare(rego[1], HClass.Void);
    declare(rego[2], HClass.Void);
    declare(rego[3], HClass.Void);
    emit (ROOT, "mov `s0h, `d0", new Temp[] { rego[0] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0l, `d0", new Temp[] { rego[1] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0h, `d0", new Temp[] { rego[2] }, new Temp[] { e2 });
    emit (ROOT, "mov `s0l, `d0", new Temp[] { rego[3] }, new Temp[] { e2 });
    emit (ROOT, "call __moddi3",
          new Temp[] { rego[0], rego[1] }, new Temp[] { rego[0], rego[1], rego[2], rego[3] });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0h", new Temp[] { r }, new Temp[] { rego[0] });
    emit (ROOT, "mov `s0, `d0l", new Temp[] { r }, new Temp[] { rego[1] });
}%

BINOP<i,p>(REM, e1, e2) = r %{
    declare(rego[0], HClass.Int);
    declare(rego[1], HClass.Int);
    emit (ROOT, "mov `s0, `d0", new Temp[] { rego[0] }, new Temp[] { e1 });
    emit (ROOT, "mov `s0, `d0", new Temp[] { rego[1] }, new Temp[] { e2 });
    emit (ROOT, "call .rem", new Temp[] { rego[0] }, new Temp[] { rego[0], rego[1] });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov `s0, `d0", new Temp[] { r }, new Temp[] { rego[0] });
}%

BINOP(CMPLT, e1, e2) = r
%pred  %( ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, new Temp[] {});
    emitCC (ROOT, "cmp `s0, `s1", new Temp[] {}, new Temp[] { e1, e2 });
    emitCC (ROOT, "bge "+templabel+"", 
		  new Temp[] {}, new Temp[] {}, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r } , new Temp[] {});
    emitLABEL(ROOT, templabel + ":", templabel);
}%

BINOP(CMPLE, e1, e2) = r
%pred  %( ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, null);
    emitCC (ROOT, "cmp `s0, `s1", new Temp[] {}, new Temp[] { e1, e2 });
    emitCC (ROOT, "bg "+templabel+"", 
		  new Temp[] {}, new Temp[] {}, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r } , new Temp[] {});
    emitLABEL(ROOT, templabel + ":", templabel);
}%

BINOP(CMPEQ, e1, e2) = r
%pred  %( ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, new Temp[] {});
    emitCC (ROOT, "cmp `s0, `s1", new Temp[] {}, new Temp[] { e1, e2 });
    emitCC (ROOT, "bne "+templabel+"", 
		  new Temp[] {}, new Temp[] {}, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r } , new Temp[] {});
    emitLABEL(ROOT, templabel + ":", templabel);
}%

BINOP(CMPGE, e1, e2) = r
%pred %( ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, new Temp[] {});
    emitCC (ROOT, "cmp `s0, `s1", new Temp[] {}, new Temp[] { e1, e2 });
    emitCC (ROOT, "bl "+templabel+"", 
		  new Temp[] {}, new Temp[] {}, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r } , new Temp[] {});
    emitLABEL(ROOT, templabel + ":", templabel);
}%

BINOP(CMPGT, e1, e2) = r
%pred %( ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, new Temp[] {});
    emitCC (ROOT, "cmp `s0, `s1", new Temp[] {}, new Temp[] { e1, e2 }); 
    emitCC (ROOT, "ble "+templabel+"", 
		  new Temp[] {}, new Temp[] {}, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r } , new Temp[] {});
    emitLABEL(ROOT, templabel + ":", templabel);
}%

BINOP(CMPLT, e1, e2) = r
%pred %( ROOT.operandType() == Type.LONG )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, null);
    emitCC (ROOT, "cmp `s0h, `s1h", null, new Temp[] { e1, e2});
    emitCC (ROOT, "bge "+templabel+"", null, null, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emitCC (ROOT, "cmp `s0l, `s1l", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "bge "+templabel+"", null, null, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPLE, e1, e2) = r
%pred %( ROOT.operandType() == Type.LONG )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, null);
    emitCC (ROOT, "cmp `s0h, `s1h", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "bg "+templabel+"", null, null, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emitCC (ROOT, "cmp `s0l, `s1l", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "bg "+templabel+"", null, null, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPEQ, e1, e2) = r
%pred %( ROOT.operandType() == Type.LONG )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, null);
    emitCC (ROOT, "cmp `s0h, `s1h", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "bne "+templabel+"", null, null, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emitCC (ROOT, "cmp `s0l, `s1l", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "bne "+templabel+"", null, null, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPGE, e1, e2) = r
%pred %( ROOT.operandType() == Type.LONG )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, null);
    emitCC (ROOT, "cmp `s0h, `s1h", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "bl "+templabel+"", null, null, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emitCC (ROOT, "cmp `s0l, `s1l", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "bl "+templabel+"", null, null, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPGT, e1, e2) = r
%pred %( ROOT.operandType() == Type.LONG )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, null);
    emitCC (ROOT, "cmp `s0h, `s1h", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "ble "+templabel+"", null, null, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emitCC (ROOT, "cmp `s0l, `s1l", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "ble "+templabel+"", null, null, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPLT, e1, e2) = r
%pred %( ROOT.operandType() == Type.FLOAT )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, null);
    emitCC (ROOT, "fcmps `s0, `s1", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "nop", null, null);
    emitCC (ROOT, "fbge "+templabel+"", null, null, new Label[] {templabel});
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPLE, e1, e2) = r
%pred %( ROOT.operandType() == Type.FLOAT )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, null);
    emitCC (ROOT, "fcmps `s0, `s1", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "nop", null, null);
    emitCC (ROOT, "fbg "+templabel+"", null, null, new Label[] {templabel});
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPEQ, e1, e2) = r
%pred %( ROOT.operandType() == Type.FLOAT )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, null);
    emitCC (ROOT, "fcmps `s0, `s1", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "nop", null, null);
    emitCC (ROOT, "fbne "+templabel+"", null, null, new Label[] {templabel});
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPGE, e1, e2) = r
%pred %( ROOT.operandType() == Type.FLOAT )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, null);
    emitCC (ROOT, "fcmps `s0, `s1", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "nop", null, null);
    emitCC (ROOT, "fbl "+templabel+"", null, null, new Label[] { templabel });
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

BINOP(CMPGT, e1, e2) = r
%pred %( ROOT.operandType() == Type.FLOAT )%
%{
    Label templabel = new Label();
    emit (ROOT, "mov 0, `d0", new Temp[] { r }, null);
    emitCC (ROOT, "fcmps `s0, `s1", null, new Temp[] { e1, e2 });
    emitCC (ROOT, "nop", null, null);
    emitCC (ROOT, "fble "+templabel+"", null, null, new Label[] {templabel});
    emitDELAYSLOT (ROOT);
    emit (ROOT, "mov 1, `d0", new Temp[] { r }, null);
    emitLABEL (ROOT, templabel + ":", templabel);
}%

CALL(retval, retex, NAME(func), arglist, handler) 
%pred %( !ROOT.isTailCall )% 
%{
    HClass type;
    Label exlabel = new Label();
    Label reglabel = new Label();

    if (ROOT.getRetval() == null)
	type = null;
    else
	type = code.getTreeDerivation().typeMap(ROOT.getRetval());

    // move the arguments into place
    emitCallPrologue(ROOT, arglist, handler, true);
   
    // do the call 
    emit (ROOT, "call "+func, 
                new Temp[] { rego[0], rego[1] }, /* AAA - do better clobbers */
		rego, /* AAA - do better uses */
		new Label[] { exlabel });
    emitDELAYSLOT (ROOT);

    emitCallEpilogue(ROOT, retval, type);
    emitNoFall (ROOT, "ba "+reglabel, null, null, new Label[] { reglabel });

    /* Need handler stub to make sure retex is in right place */
    emitLABEL (ROOT, exlabel+":", exlabel);
    emitHandlerStub (ROOT, retex, handler);

    emitLABEL (ROOT, reglabel+":", reglabel);

    /* Currently passing exceptional handler as first argument 
       to get the backend up and running - andyb

    // exceptional return handler
    emitLABEL (ROOT, elabel+":", elabel);
    emitHandlerStub(ROOT, retex, handler);

    // normal return handler
    emitLABEL (ROOT, rlabel+":", rlabel);
    emitCallEpilogue(ROOT, retval, type);

    // "fixup table"
    emitCallFixupTable (ROOT, rlabel, elabel);
    */
}%

CALL(retval, retex, func, arglist, handler)
%pred %( !ROOT.isTailCall )%
%{
    HClass type;
    Label exlabel = new Label();
    Label reglabel = new Label();


    if (ROOT.getRetval() == null)
        type = null;
    else
        type = code.getTreeDerivation().typeMap(ROOT.getRetval());

    // move the arguments into place
    emitCallPrologue(ROOT, arglist, handler, true);
  
    // do the call
    emit (ROOT, "call `s0",
                new Temp[] { rego[0], rego[1] }, /* AAA - do better clobbers */
                new Temp[] { func, rego[0], rego[1], rego[2], 
			     rego[3], rego[4], rego[5], rego[6], rego[7] }, 
		 	     /* AAA - need uses */
                new Label[] { exlabel });
    emitDELAYSLOT (ROOT);

    emitCallEpilogue(ROOT, retval, type);
    emitNoFall (ROOT, "ba "+reglabel, null, null, new Label[] { reglabel });

    emitLABEL (ROOT, exlabel+":", exlabel);
    emitHandlerStub (ROOT, retex, handler);

    emitLABEL (ROOT, reglabel+":", reglabel);

    /* Using first parameter hack for handling exceptions

    // exceptional return handler
    emitLABEL (ROOT, elabel+":", elabel);
    emitHandlerStub(ROOT, retex, handler);

    // normal return handler
    emitLABEL (ROOT, rlabel+":", rlabel);
    emitCallEpilogue(ROOT, retval, type);

    // "fixup table"
    emitCallFixupTable (ROOT, rlabel, elabel);
    */
}%

// true_label and false_label are harpoon.Temp.Labels, not Exps...
CJUMP(e, true_label, false_label) %{
    emitCC (ROOT, "cmp `s0, 0", null, new Temp[] { e });
    emitCC (ROOT, "bne " + true_label + "", null, null,
                  new Label[] { true_label }); 
    emitDELAYSLOT (ROOT);

    // should be able to optimize these away. 
    emitCCNoFall (ROOT, "ba " + false_label + "", null, null,
                        new Label[] { false_label }); 
    emitDELAYSLOT (ROOT);
}%

CONST<l,d>(c)=r %{
    // 'as' is smart - just use set, and it automagically generates
    // all of the necessary instructions
    long val = (ROOT.type() == Type.LONG)
               ? c.longValue()
               : Double.doubleToLongBits(c.floatValue());
    int low = (int)val;
    int high = (int)(val >> 32);
    emit (ROOT, "set " + low + ", `d0l", new Temp[] { r }, null);
    emit (ROOT, "set " + high + ", `d0h", new Temp[] { r }, null);
}%

CONST<i,f>(c)=r %{
    // 'as' is smart - just use set, and it automagically generates
    // all of the necessary instructions
    int val = (ROOT.type() == Type.INT)
              ? ROOT.value.intValue()
              : Float.floatToIntBits(ROOT.value.floatValue());
    emit (ROOT, "set "+val+", `d0", new Temp[] { r }, null);
}%

CONST<p>(c) = r %{
    declare(regg[0], HClass.Void);
    emit (ROOT, "mov `s0, `d0 ! null", new Temp[]{ r }, new Temp[] { regg[0] });
}%

CONST<i>(0)=r %{
    declare(regg[0], HClass.Void);
    emit (ROOT, "mov `s0, `d0", new Temp[] { r }, new Temp[] { regg[0] });
}%

CONST<l>(0)=r %{
    declare(regg[0], HClass.Void);
    emit (ROOT, "mov `s0, `d0l", new Temp[] { r }, new Temp[] { regg[0] });
    emit (ROOT, "mov `s0, `d0h", new Temp[] { r }, new Temp[] { regg[0] });
}%

DATUM(CONST<i>(exp)) %{
    String lo = "0x" + Integer.toHexString(exp.intValue());
    emitDIRECTIVE (ROOT, "\t.word " + lo + " ! " + exp);
}%

DATUM(CONST<l>(exp)) %{
    long val = exp.longValue();
    String lo = "0x" + Integer.toHexString((int) val);
    String hi = "0x" + Integer.toHexString((int) (val >> 32));
    emitDIRECTIVE (ROOT, "\t.word " + hi + " ! " + exp);
    emitDIRECTIVE (ROOT, "\t.word " + lo + " ! " + exp);
}%

DATUM(CONST<s:8,u:8>(exp)) %{
    String chardesc = (exp.intValue() >= 32 && exp.intValue() < 127
                       && exp.intValue() != 96 /* backquotes */
                       && exp.intValue() != 34 /* double quotes */) ?
                       ("\t! char "+((char)exp.intValue())) : "";
    emitDIRECTIVE(ROOT, "\t.byte "+exp+chardesc);
}%

DATUM(CONST<s:16,u:16>(exp)) %{
    String chardesc = (exp.intValue() >= 32 && exp.intValue() < 127
                       && exp.intValue() != 96 /* backquotes */
                       && exp.intValue() != 34 /* double quotes */) ?
                       ("\t! char "+((char)exp.intValue())) : "";
    emitDIRECTIVE(ROOT, "\t.short "+exp+chardesc);
}% 

DATUM(CONST<p>(exp)) %{
    emitDIRECTIVE(ROOT, "\t.word 0 ! should always be null pointer constant");
}%

DATUM(CONST<l>(exp)) %{
    long l = exp.longValue();
    String lo = "0x" + Integer.toHexString((int)l);
    String hi = "0x" + Integer.toHexString((int)(l >> 32));
    emitDIRECTIVE(ROOT, "\t.word " + hi + " ! hi (" + exp + ")");
    emitDIRECTIVE(ROOT, "\t.word " + lo + " ! lo (" + exp + ")");
}%

DATUM(NAME(l)) %{
    emitDIRECTIVE(ROOT, "\t.word " + l);
}%

EXPR(e1) %{
    /* throw away temp e1 (not used) */
}%

JUMP(NAME(l)) %{
    emitNoFall (ROOT, "ba " + l + "", null, null, new Label[] { l }); 
    emitDELAYSLOT (ROOT);
}%

JUMP(BINOP(ADD, CONST<i>(c), e))
%pred %( is13bit(c) )%
%{
    List labelList = LabelList.toList (ROOT.targets);
    emitNoFall (ROOT, "jmpl `s0 + "+c+", %g0", 
                      null, new Temp[] { e }, labelList);
    emitDELAYSLOT (ROOT);
}%

JUMP(BINOP(ADD, e, CONST<i>(c)))
%pred %( is13bit(c) )%
%{
    List labelList = LabelList.toList (ROOT.targets);
    emitNoFall (ROOT, "jmpl `s0 + "+c+", %g0", 
                      null, new Temp[] { e }, labelList);
    emitDELAYSLOT (ROOT);
}%

JUMP(BINOP(ADD, e1, e2)) %{
    List labelList = LabelList.toList (ROOT.targets);
    emitNoFall (ROOT, "jmpl `s0 + `s1, %g0", 
                      null, new Temp[] { e1, e2 }, labelList);
    emitDELAYSLOT (ROOT);
}%

JUMP(e) %{
    List labelList = LabelList.toList (ROOT.targets);
    emitNoFall (ROOT, "jmpl `s0, %g0", null, new Temp[] { e }, labelList);
    emitDELAYSLOT (ROOT);
}%

LABEL(l) %{
    emitLABEL (ROOT, l.toString()+":", ((LABEL) ROOT).label);
}%

// ld* [rs + immed], rd
MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(BINOP(PLUS, CONST<i>(c), e1)) = r %{
    String srcsuff = (ROOT.isDoubleWord()) ? "h" : "";
    emit (ROOT, "ld"+loadSuffix(ROOT) + " [ `s0 + "+c+" ], `d0"+srcsuff + "",
                new Temp[] { r }, new Temp[] { e1 });
}%

// ld* [rs + immed], rd
MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(BINOP(PLUS, e1, CONST<i>(c))) = r %{
    String srcsuff = (ROOT.isDoubleWord()) ? "h" : "";
    emit (ROOT, "ld"+loadSuffix(ROOT) + " [ `s0 + "+c+" ], `d0"+srcsuff + "",
                new Temp[] { r }, new Temp[] { e1 });
}%

// ld* [rs0 + rs1], rd
MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(BINOP(PLUS, e1, e2)) = r %{
    String srcsuff = (ROOT.isDoubleWord()) ? "h" : "";
    emit (ROOT, "ld"+loadSuffix(ROOT) + " [ `s0 + `s1 ], `d0"+srcsuff + "",
                new Temp[] { r }, new Temp[] { e1, e2 });
}%

// ld* [immed], rd
MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(CONST<i>(c)) = r %{
    String srcsuff = (ROOT.isDoubleWord()) ? "h" : "";
    emit (ROOT, "ld"+loadSuffix(ROOT) + " ["+c+"], `d0"+srcsuff + "",
                new Temp[] { r }, null);
}%

// ld* [rs], rd
MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(e) = r %{
    String srcsuff = (ROOT.isDoubleWord()) ? "h" : "";
    emit (ROOT, "ld"+loadSuffix(ROOT) + " [`s0], `d0"+srcsuff + "",
                new Temp[] { r }, new Temp[] { e });
}%

METHOD(params) %{
    int loc = 0;
    emitENTRY(ROOT);

    declare(SP, HClass.Void);
    // don't skip params[0], because we don't do any fancy stuff with it.
    for (int i = 0; i < params.length; i++) {
	declare(params[i], code.getTreeDerivation(), ROOT.getParams(i));
        if (tb.isTwoWord(params[i])) {
            if (loc < 6) { // first half in register
                emit (ROOT, "mov `s0, `d0h",
                            new Temp[] { params[i] },
                            new Temp[] { regi[loc] });
            } else { // on stack
                emit (ROOT, "ld [`s0 + "+4*(loc-6)+92+"], `d0h",
                            new Temp[] { params[i] },
                            new Temp[] { SP });
            }
	    loc++;
            if (loc < 6) { // second half in register
                emit (ROOT, "mov `s0, `d0l",
                            new Temp[] { params[i] },
                            new Temp[] { regi[loc] });
            } else { // on stack
                emit (ROOT, "ld [`s0 + "+4*(loc-6)+92+"], `d0l",
                            new Temp[] { params[i] },
                            new Temp[] { SP });
            }
            loc++;
        } else {
            if (loc < 6) { // in register
		assert params[i] != null;
                emit (ROOT, "mov `s0, `d0", 
                            new Temp[] { params[i] }, 
                            new Temp[] { regi[loc] });
            } else { // on stack
                emitMEM (ROOT, "ld [`s0 + "+4*(loc-6)+92+"], `d0",
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
    emitMEM (ROOT, "st"+storeSuffix(dst) + " `s2"+suff + ", [`s0 + `s1]",
                   null, new Temp[] { e1, e2, e3 });
}%

// st* rs1, [ rs0 + immed ]
MOVE(MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(BINOP(ADD, e1, CONST<i>(c))), e2)
%pred %( is13bit(c) )%
%{
    MEM dst = (MEM)(((MOVE)ROOT).getDst());
    String suff = (dst.isDoubleWord()) ? "h" : "";
    emitMEM (ROOT, "st"+storeSuffix(dst) + " `s1"+suff + ", [ `s0 + "+c+" ]",
                   null, new Temp[] { e1, e2 });
}%

// st* rs1, [ rs0 + immed ]
MOVE(MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(BINOP(ADD, CONST<i>(c), e1)), e2)
%pred %( is13bit(c) )%
%{
    MEM dst = (MEM)(((MOVE)ROOT).getDst());
    String suff = (dst.isDoubleWord()) ? "h" : "";
    emitMEM (ROOT, "st"+storeSuffix(dst) + " `s1"+suff + ", [ `s0 + "+c+" ]",
                   null, new Temp[] { e1, e2 });
}%

// st* rs, [ immed ]
MOVE(MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(CONST<i>(c)), e) 
%pred %( is13bit(c) )%
%{
    MEM dst = (MEM)(((MOVE)ROOT).getDst());
    String suff = (dst.isDoubleWord()) ? "h" : "";
    emitMEM (ROOT, "st"+storeSuffix(dst) + " `s0"+suff + ", [ "+c+" ]",
                   null, new Temp[] { e });
}%

// st* rs1, [ rs0 ]
MOVE(MEM<s:8,u:8,s:16,u:16,i,l,f,p,d>(e1), e2) %{
    MEM dst = (MEM)(((MOVE)ROOT).getDst());
    String suff = (dst.isDoubleWord()) ? "h" : "";
    emitMEM (ROOT, "st"+storeSuffix(dst) + " `s1"+suff + ", [`s0]",
                   null, new Temp[] { e1, e2 });
}%

MOVE<i,p>(TEMP(e1), CONST<i>(c)) %pred %( is13bit(c) )% %{
    declare(e1, code.getTreeDerivation(), ROOT.getSrc());
    emit (ROOT, "mov "+c+", `d0",
                new Temp[] { e1 }, null);
}%

MOVE<i,p>(TEMP(e1), e2) %{ /* catch-all */
   declare(e1, code.getTreeDerivation(), ROOT.getSrc());
   emit (ROOT, "mov `s0, `d0", new Temp[] { e1 }, new Temp[] { e2 });
}%

MOVE<l>(TEMP(e1), e2) %{ /* long (pair of int) register move */
    declare(e1, code.getTreeDerivation(), ROOT.getSrc());
    emit (ROOT, "mov `s0l, `d0l", new Temp[] { e1 }, new Temp[] { e2 });
    emit (ROOT, "mov `s0h, `d0h", new Temp[] { e1 }, new Temp[] { e2 });
}%

MOVE<f>(TEMP(e1), e2) %{ /* floating-point register move */
    declare(e1, code.getTreeDerivation(), ROOT.getSrc());
    emit(ROOT, "fmovs `s0, `d0", new Temp[] { e1 }, new Temp[] { e2 });
}%

MOVE<d>(TEMP(e1), e2) %{ /* double (pair of fp) register move */
    declare(e1, code.getTreeDerivation(), ROOT.getSrc());
    emit (ROOT, "fmovs `s0l, `d0l", new Temp[] { e1 }, new Temp[] { e2 });
    emit (ROOT, "fmovs `s0h, `d0h", new Temp[] { e1 }, new Temp[] { e2 });
}%

NAME(s)=r %{
    emit (ROOT, "set " + s + ", `d0", new Temp[] { r }, null);
}%

NATIVECALL(retval, func, arglist) %{
    /* AAA need to make uses better */
    HClass type;

    if (ROOT.getRetval() == null) {
	type = null;
    } else {
	type = code.getTreeDerivation().typeMap(ROOT.getRetval());
    }

    emitCallPrologue (ROOT, arglist, null, false);

    emit (ROOT, "call `s0",
		new Temp[] { rego[0] },
		new Temp[] { func, rego[0], rego[1], rego[2], rego[3], rego[4],
			     rego[5], rego[6], rego[7], rego[8] });
    emitDELAYSLOT (ROOT);

    emitCallEpilogue (ROOT, retval, type);
}%    

RETURN(val) %{
    // Assume for now that this is non-leaf.
    // procFixup will need to change these to %o0 and %o1 if it is leaf...
    if (tb.isTwoWord(val)) {
        declare(regi[0], HClass.Void);
	declare(regi[1], HClass.Void);
        emit (ROOT, "mov `s0h, `d0", 
                    new Temp[] { regi[0] }, /* %i0 */
                    new Temp[] { val });
        emit (ROOT, "mov `s0l, `d0",
                    new Temp[] { regi[1] }, /* %i1 */
                    new Temp[] { val });
    } else { 
	declare(regi[0], code.getTreeDerivation(), ROOT.getRetval());
        emit (ROOT, "mov `s0, `d0",
                    new Temp[] { regi[0] }, /* %i0 */
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

SEGMENT(GC_INDEX) %{
    emitDIRECTIVE(ROOT, "\t.data 10\t!.section gc_index");
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
    // like the StrongARM backend, the exception handling is done
    // by the caller.  we just have to make sure that we return to
    // the right place so that the caller knows to use the exception handler.

    // again, assume non-leaf for now - might have to change registers
    // in procFixup if we determine that it is a leaf procedure and
    // optimize for that

    // move exception value into correct registers
    if (tb.isTwoWord(val)) {
	declare(regi[0], HClass.Void);
	declare(regi[1], HClass.Void);
	emit (ROOT, "mov `s0h, `d0", 
		    new Temp[] { regi[0] }, 
		    new Temp[] { val });
	emit (ROOT, "mov `s0l, `d0",
		    new Temp[] { regi[1] },
		    new Temp[] { val });
    } else {
	declare(regi[0], code.getTreeDerivation(), ROOT.getRetex());
	emit (ROOT, "mov `s0, `d0", new Temp[] { regi[0] }, new Temp[] { val });
    }

    declare(regi[7], HClass.Void);
    // replace %i7 with the handler passed in %i0
    emit (ROOT, "mov `s0, `d0", new Temp[] { regi[7] }, new Temp[] { regi[0] });
    // retr returns to %i7 + 8, so subtract 8 to get to the right place
    emit (ROOT, "sub `s0, 8, `d0", 
		new Temp[] { regi[7] }, 
		new Temp[] { regi[7] });
    /*
    // The point of lookup is to set %i7 to the correct value for the 
    // returning jump
    emit (ROOT, "call _lookup",
                new Temp[] { regi[7] }, 
                new Temp[] { });
    */
    emitEXIT (ROOT);
}%

// unary operator checklist
// op		done		todo
// NEG		i,p,l,f		d
// NOT				i,p,l,f,d
// I2B		i
// I2C		i
// I2S		i
// _2I		i,p,l,f		l,f,d
// _2L		i,p		l,f,d
// _2F		i,p		l,f,d
// _2D				i,p,l,f,d

UNOP<i,p>(NEG, e)=r %{
    declare(regg[0], HClass.Int);
    emit (ROOT, "sub `s0, `s1, `d0", new Temp[] { r }, new Temp[] { regg[0], e });
}%

UNOP<l>(NEG, e)=r %{
    declare(regg[0], HClass.Int);
    emitCC (ROOT, "subcc `s0, `s1l, `d0l", 
                  new Temp[] { r }, new Temp[] { regg[0], e });
    emitCC (ROOT, "subx `s0, `s1h, `d0h", 
                  new Temp[] { r }, new Temp[] { regg[0], e });
}%

UNOP<f>(NEG, e)=r %{
    emit (ROOT, "fnegs `s0, `d0", new Temp[] { r }, new Temp[] { e });
}%

UNOP(I2B, e) = r
%pred %(ROOT.operandType() == Type.INT)%
%{
    emit (ROOT, "sll `s0, 24, `d0", new Temp[] { r }, new Temp[] { e });
    emit (ROOT, "sra `s0, 24, `d0", new Temp[] { r }, new Temp[] { e });
}%

UNOP(I2C, e) = r
%pred %(ROOT.operandType() == Type.INT)%
%{
    emit (ROOT, "sll `s0, 16, `d0", new Temp[] { r }, new Temp[] { e });
    emit (ROOT, "sra `s0, 16, `d0", new Temp[] { r }, new Temp[] { e });
}%

UNOP(I2S, e) = r
%pred %(ROOT.operandType() == Type.INT)%
%{
    emit (ROOT, "sll `s0, 16, `d0", new Temp[] { r }, new Temp[] { e });
    emit (ROOT, "srl `s0, 16, `d0", new Temp[] { r }, new Temp[] { e });
}%

UNOP(_2I, e) = r
%pred %(ROOT.operandType() == Type.FLOAT )%
%{
    emit (ROOT, "fstoi `s0, `d0", new Temp[] { r }, new Temp[] { e });
}%

UNOP(_2I, e) = r
%pred %(ROOT.operandType() == Type.LONG )%
%{
    emit (ROOT, "mov `s0l, `d0", new Temp[] { r } , new Temp[] { e });
}%

UNOP(_2I, e) = r
%pred %(ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    emit (ROOT, "mov `s0, `d0", new Temp[] { r } , new Temp[] { e });
}%

UNOP(_2L, e) = r
%pred %(ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    emit (ROOT, "mov `s0, `d0l", new Temp[] { r }, new Temp[] { e });
    emit (ROOT, "sra `s0, 31, `d0h", new Temp[] { r } , new Temp[] { e });
}%

UNOP(_2F, e) = r
%pred %(ROOT.operandType() == Type.INT || ROOT.operandType() == Type.POINTER)%
%{
    /* AAA - this is broken. need to do loads and stores, can't
       move directly from fp to normal registers. */
    emit (ROOT, "fitos `s0, `d0", new Temp[] { r }, new Temp[] { e });
}%
