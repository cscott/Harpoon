// CodeGen.spec, created Tue Jul 6 12:12:41 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.MIPS;

import harpoon.Backend.Maps.NameMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrEdge;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrJUMP;
import harpoon.IR.Assem.InstrMOVE;
import harpoon.IR.Assem.InstrCALL;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrDIRECTIVE;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Tree.TreeDerivation;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.Uop;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Typed;
import harpoon.IR.Tree.PreciselyTyped;
import harpoon.IR.Tree.ExpList;
import harpoon.Util.Util;
import harpoon.Temp.TempList;
import harpoon.Temp.Temp;
import harpoon.Temp.LabelList;
import harpoon.Temp.Label;

import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.INVOCATION;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.EXPR;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.OPER;
import harpoon.IR.Tree.RETURN;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.UNOP;
import harpoon.IR.Tree.SEQ;

import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.GenericMultiMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <code>MIPS.CodeGen</code> is a code-generator for the MIPS II architecture.
 * 
 * @see Kane, <U>MIPS Risc Architecture </U>
 * @author  Emmett Witchel <witchel@lcs.mit.edu>
 * @version $Id: CodeGen.spec,v 1.1.2.3 2000-06-26 20:18:02 witchel Exp $
 */
// All calling conventions and endian layout comes from observing cc
// on MIPS IRIX64 lion 6.2 03131016 IP19.  
// MIPS on IRIX is big endian.  This also applies to long longs and
// doubles (for which we are using software fp.  The most sinifigant
// 32 bit quantity is at the lower address (and in v0, as opposed to
// v1) 
%%

    // InstrFactory to generate Instrs from
    private InstrFactory instrFactory;
    

    /*final*/ RegFileInfo regfile;
    private Temp  v0, v1, a0, a1, a2, a3, t0, t1, t2, 
       t3, t4, t5, t6, t7, t8, t9, FP, SP, LR;
    Comparator regComp;
    private /*final*/ Temp call_use[];
    /* The maximum def set for a call */
    private Temp call_def_full[];
    /* The def set we use in our support functions */
    private  Temp call_def_builtin[];

    // whether to generate stabs debugging information in output (-g flag)
    // Currently broken on MIPS
    private static final boolean stabsDebugging=false;

    // NameMap for calling C functions.
    NameMap nameMap;

    // whether to generate a.out-style or elf-style segment directives
    private final boolean is_elf;
    // whether to use soft-float or hard-float calling convention.
    private final boolean soft_float = false; // skiffs use hard-float

    public CodeGen(Frame frame, boolean is_elf) {
       super(frame);
	last = null;
	this.regfile = (RegFileInfo) frame.getRegFileInfo();
	this.nameMap = frame.getRuntime().nameMap;
	this.is_elf = is_elf;
    v0 = regfile.V0;
    v1 = regfile.V1;
    a0 = regfile.A0;
    a1 = regfile.A1;
    a2 = regfile.A2;
    a3 = regfile.A3;
	t0 = regfile.T0;
    t1 = regfile.T1;
    t2 = regfile.T2;
    t3 = regfile.T3;
    t4 = regfile.T4;
    t5 = regfile.T5;
    t6 = regfile.T6;
    t7 = regfile.T7;
    t8 = regfile.T8;
    t9 = regfile.T9;
	FP = regfile.FP; // s8, but C on IRIX/MIPS doesn't use an fp
	SP = regfile.SP; // reg 29
	LR = regfile.LR; // reg 31
    call_use = new Temp[] {a0, a1, a2, a3, SP};
    call_def_full = new Temp[] {v0, v1, a0, a1, a2, a3, t0, t1, 
                                t2, t3, t4, t5, t6, t7, t8, t9, 
                                LR};
    call_def_builtin = new Temp[] {v0, v1, a0, a1, a2, a3, t0, t1, t2,
                                   LR};

	// allow sorting of registers so that stm and ldm work correctly.
	final Map regToNum = new HashMap();
	for (int i=0; i<regfile.reg.length; i++)
       regToNum.put(regfile.reg[i], new Integer(i));
	regComp = new Comparator() {
          public int compare(Object o1, Object o2) {
             Util.assert(regToNum.keySet().contains(o1),
                         o1+" not in regToNum's keys");
             Util.assert(regToNum.keySet().contains(o2),
                         o2+" not in regToNum's keys");
             return ((Integer)regToNum.get(o1)).intValue() -
                ((Integer)regToNum.get(o2)).intValue();
          }
       };
    }

    /** Sub-class to represent delay-slots.
     * <code>optimize()</code> uses this class information to determine that
     * it should rearrange code to try to eliminate these instructions.
     * @author Emmett Witchel
     */
    public class InstrDELAYSLOT extends Instr {
	// a nop to fill the delay slot
	public InstrDELAYSLOT(InstrFactory inf, HCodeElement source) {
	    super(inf, source, "nop  # delay slot");
	}
    }
    // I don't know why I need this, and would like to eliminate it
    private Temp[] New_And_Copy(Temp[] t) {
       Temp[] ret = new Temp[t.length];
       for(int i = 0; i < t.length; ++i) {
          ret[i] = t[i];
       }
       return ret;
    }
    private String GetLdSuffix(MEM root) {
       String suffix = "";
       if(root.isSmall()) {
          switch(root.bitwidth()) {
          case 8:
             if(root.signed())
                suffix = "b";
             else
                suffix = "bu";
             break;
          case 16:
             if(root.signed())
                suffix = "h";
             else
                suffix = "hu";
             break;
          default:
             Util.assert(false, "Constant offset memory load wants to load bitwidth that is not 8, 16");       
          }
       } else {
          suffix = "w";
       }
       return suffix;
    }
    private String GetStSuffix(MOVE root) {
       String suffix = "";
       if(((MEM)root.getDst()).isSmall()) {
          switch(((MEM)root.getDst()).bitwidth()) {
          case 8:
             suffix = "b";
             break;
          case 16:
             suffix = "h";
             break;
          default:
             Util.assert(false, "Constant offset memory load wants to load bitwidth that is not 8, 16, or 32");       
          }
       } else {
          suffix = "w";
       }
       return suffix;
    }

    /** The main Instr layer; nothing below this line should call
	emit(Instr) directly, unless they are constructing extensions
	of Instr.
    */
    private Instr emit(HCodeElement root, String assem,
                       Temp[] dst, Temp[] src,
                       boolean canFallThrough, List targets) {
       return emit(new Instr( instrFactory, root, assem,
                              dst, src, canFallThrough, targets));
    }		      

    /** Secondary emit layer; for primary usage by the other emit
	methods. 
    */
    private Instr emit2(HCodeElement root, String assem,
                        Temp[] dst, Temp[] src) {
	return emit(root, assem, dst, src, true, null);
    }

    /** Single dest Single source Emit Helper. */
    private Instr emit( HCodeElement root, String assem, 
		       Temp dst, Temp src) {
	return emit2(root, assem, new Temp[]{ dst }, new Temp[]{ src });
    }
    

    /** Single dest Two source Emit Helper. */
    private Instr emit( HCodeElement root, String assem, 
		       Temp dst, Temp src1, Temp src2) {
	return emit2(root, assem, new Temp[]{ dst },
			new Temp[]{ src1, src2 });
    }

    /** Null dest Null source Emit Helper. */
    private Instr emit( HCodeElement root, String assem ) {
	return emit2(root, assem, null, null);
    }

    /** Single dest Single source emit InstrMOVE helper */
    private Instr emitMOVE( HCodeElement root, String assem,
			   Temp dst, Temp src) {
	return emit(new InstrMOVE( instrFactory, root, assem+" # move",
			    new Temp[]{ dst },
			    new Temp[]{ src }));
    }			         

    /* Branching instruction emit helper. 
       Instructions emitted using this *can* fall through.
    */
    private Instr emit( HCodeElement root, String assem,
		       Temp[] dst, Temp[] src, Label[] targets ) {
        return emit(new Instr( instrFactory, root, assem,
			dst, src, true, Arrays.asList(targets)));
    }
    
    /* Branching instruction emit helper. 
       Instructions emitted using this *cannot* fall through.
    */
    private Instr emitNoFall( HCodeElement root, String assem,
		       Temp[] dst, Temp[] src, Label[] targets ) {
        return emit(new Instr( instrFactory, root, assem,
			dst, src, false, Arrays.asList(targets)));
    }

    /* Call instruction emit helper. 
       Instructions emitted using this *cannot* fall through.
    */
    private Instr emitCallNoFall( HCodeElement root, String assem,
		       Temp[] dst, Temp[] src, Label[] targets ) {
	List tlist = (targets==null?null:Arrays.asList(targets));
        return emit(new InstrCALL( instrFactory, root, assem,
				   dst, src, false, tlist));
    }

    private Instr emitNativeCall( HCodeElement root, String assem,
                                  Temp[] dst, Temp[] src, 
                                  boolean canFall, Label[] targets) {
       List tlist = (targets==null?null:Arrays.asList(targets));
       return emit(new InstrCALL( instrFactory, root, assem,
                                  dst, src, canFall, tlist));
    }

    /* InstrJUMP emit helper; automatically adds entry to
       label->branches map. */ 
    private Instr emitJUMP( HCodeElement root, String assem, Label l ) {
	Instr j = emit( new InstrJUMP( instrFactory, root, assem, l ));
	return j;
    }

    /* InstrLABEL emit helper. */
    private Instr emitLABEL( HCodeElement root, String assem, Label l ) {
	return emit( new InstrLABEL( instrFactory, root, assem, l ));
    }	
    /* InstrLABEL emit helper. */
    private Instr emitNoFallLABEL( HCodeElement root, String assem, Label l ) {
	return emit( InstrLABEL.makeNoFall( instrFactory, root, assem, l ));
    }	

    /* InstrDIRECTIVE emit helper. */
    private Instr emitDIRECTIVE( HCodeElement root, String assem ) {
	return emit( new InstrDIRECTIVE( instrFactory, root, assem ));
    }
    /* InstrDIRECTIVE emit helper. */
    private Instr emitNoFallDIRECTIVE( HCodeElement root, String assem ) {
	return emit( InstrDIRECTIVE.makeNoFall( instrFactory, root, assem ));
    }
    private Instr emitDELAYSLOT(HCodeElement root) {
        return emit(new InstrDELAYSLOT(instrFactory, root));
    }

    private boolean is16BitOffset(long val) {
	// addressing mode two takes a 12 bit unsigned offset, with
	// an additional bit in the instruction word indicating whether
	// to add or subtract this offset.  This means that there
	// are two representations for zero offset: +0 and -0.
	long absval = (val<0)?-val:val;
	return (absval&(~0xFFFF))==0;
    }
    private boolean is16BitOffset(Number n) {
	if (n instanceof Double || n instanceof Float) return false;
	else return is16BitOffset(n.longValue());
    }

    // helper for operand2 shifts
    private boolean is5BitShift(long val) {
	return (val>=0) && (val<=31);
    }
    private boolean is5BitShift(Number n) {
	if (n instanceof Double || n instanceof Float) return false;
	else return is5BitShift(n.longValue());
    }
    private boolean isShiftOp(int op) {
	switch (op) {
	case Bop.SHL: case Bop.SHR: case Bop.USHR: return true;
	default: return false;
	}
    }
    private String shiftOp2Str(int op) {
	switch (op) {
	case Bop.SHL: return "sll";
	case Bop.SHR: return "sra";
	case Bop.USHR: return "srl";
	default: throw new Error("Illegal shift operation");
	}
    }
    // helper for comparison operations
    private boolean isCmpOp(int op) {
	switch (op) {
	case Bop.CMPEQ: case Bop.CMPNE:
	case Bop.CMPGT: case Bop.CMPGE:
	case Bop.CMPLT: case Bop.CMPLE: return true;
	default: return false;
	}
    }
    // Given a comparison operation, return the implementation in MIPS
    // assembler
    private String cmpOp2AsStr(int op) {
	switch (op) {
	case Bop.CMPEQ: return "seq";
	case Bop.CMPNE: return "sne";
	case Bop.CMPGT: return "sgt";
	case Bop.CMPGE: return "sge";
	case Bop.CMPLE: return "sle";
	case Bop.CMPLT: return "slt";
	default: throw new Error("Illegal compare operation");
	}
    }
    // Given a comparison operation, return the corresponding branch
    // in MIPS assembler
    private String cmpOp2BrStr(int op) {
	switch (op) {
	case Bop.CMPEQ: return "beq";
	case Bop.CMPNE: return "bne";
	case Bop.CMPGT: return "bgt";
	case Bop.CMPGE: return "bge";
	case Bop.CMPLE: return "ble";
	case Bop.CMPLT: return "blt";
	default: throw new Error("Illegal compare operation");
	}
    }
    // helper for operand2 immediates
    private boolean isZero(int val) {
	return (val == 0);
    }
    private boolean isZero(Number n) {
	if (!(n instanceof Integer)) return false;
	else return isZero(n.intValue());
    }
    private int negate(Number n) {
	return -((Integer)n).intValue();
    }

    /** simple tuple class to wrap some bits of info about the call prologue */
    private class CallState {
      /** number of parameter bytes pushed on to the stack. */
      final int stackOffset;
      /** set of registers used by parameters to the call. */
      final List callUses;
      CallState(int stackOffset, List callUses) {
	this.stackOffset=stackOffset; this.callUses=callUses;
      }
      /** Append a stack-offset instruction to the actual call.
       *  We delay the stack-offset to the point where it is
       *  atomic with the call, so that the register allocator
       *  can't insert spill code between the stack adjustment
       *  and the call. (the spill code would fail horribly in
       *  that case, because the stack pointer won't be where it
       *  expects it to be.) */
      String prependSPOffset(String asmString) {
         // optimize for common case.
         // CSA: THIS ROUTINE NO LONGER NEEDED. we offset from FP now.
         if (true || stackOffset==0) return asmString;
         declare( SP, HClass.Void );
         return "subu $sp, $sp, "+stackOffset+"\n\t"+asmString;
      }
    }

    /** Declare Void types for a0, a1, a2, a3, t0, LR in prep for a call. */
    private void declareCALL() {
       declare(a0, HClass.Void);
       declare(a1, HClass.Void);
       declare(a2, HClass.Void);
       declare(a3, HClass.Void);
       declare(t0, HClass.Void);
       declare(LR, HClass.Void);
    }
    private void declareCALLDefFull() {
       declare(v0, HClass.Void);
       declare(v1, HClass.Void);
       declare(a0, HClass.Void);
       declare(a1, HClass.Void);
       declare(a2, HClass.Void);
       declare(a3, HClass.Void);
       declare(t0, HClass.Void);
       declare(t1, HClass.Void);
       declare(t2, HClass.Void);
       declare(t3, HClass.Void);
       declare(t4, HClass.Void);
       declare(t5, HClass.Void);
       declare(t6, HClass.Void);
       declare(t7, HClass.Void);
       declare(t8, HClass.Void);
       declare(t9, HClass.Void);
       declare(LR, HClass.Void);
    }
    private void declareCALLDefBuiltin() {
       declare(v0, HClass.Void);
       declare(v1, HClass.Void);
       declare(a0, HClass.Void);
       declare(a1, HClass.Void);
       declare(a2, HClass.Void);
       declare(a3, HClass.Void);
       declare(t0, HClass.Void);
       declare(t1, HClass.Void);
       declare(t2, HClass.Void);
       declare(LR, HClass.Void);
    }
    private void DoLLCall(HCodeElement root,
                          Temp i, Temp j, Temp k, String func_name) {
       declare( a3, HClass.Void );
       declare( a2, HClass.Int );
       declare( a1, HClass.Void );
       declare( a0, HClass.Void );
       // not certain an emitMOVE is legal with the l/h modifiers
       Util.assert(j instanceof TwoWordTemp);
       Util.assert(k instanceof TwoWordTemp);
       emit( root, "move `d0, `s0h", a0, j );
       emit( root, "move `d0, `s0l", a1, j );
       emit( root, "move `d0, `s0h", a2, k );
       emit( root, "move `d0, `s0l", a3, k );
       declareCALLDefBuiltin();
       emit2(root, "jal "+nameMap.c_function_name(func_name),
             // uses & stomps on these registers:
             call_def_builtin, call_use);
       Util.assert(i instanceof TwoWordTemp);
       emit( root, "move `d0h, `s0", i, v0 );
       emit( root, "move `d0l, `s0", i, v1 );
    }
    /** Source for long long arithmetic routines is in Flex_MIPS.S */
    private void DoLLShiftCall(HCodeElement root,
                               Temp i, Temp j, Temp k, String func_name) {
       declare( t0, HClass.Void );
       declare( t1, HClass.Void );
       declare( t2, HClass.Void );
       declare( a2, HClass.Int );
       declare( a1, HClass.Void );
       declare( a0, HClass.Void );
       // not certain an emitMOVE is legal with the l/h modifiers
       Util.assert(j instanceof TwoWordTemp);
       emit( root, "move `d0, `s0h", a0, j );
       emit( root, "move `d0, `s0l", a1, j );
       emitMOVE( root, "move `d0, `s0", a2, k );
       declareCALLDefBuiltin();
       emit2(root, "jal "+nameMap.c_function_name(func_name),
             // uses & stomps on these registers:
             call_def_builtin, call_use);
       Util.assert(i instanceof TwoWordTemp);
       emit( root, "move `d0h, `s0", i, v0 );
       emit( root, "move `d0l, `s0", i, v1 );
    }
    /** For now all float and double operations are in software.  Each
     routine has an assembly jacket function which now calls a C
     function.  Eventually, we can implement them in assembly using
     fewer registers if we want*/
    private void DoFCall(HCodeElement root,
                         Temp i, Temp j, Temp k, String func_name) {
       /* call auxillary fp routines */
       declare(a0, HClass.Float);
       declare(a1, HClass.Float);
       emitMOVE( root, "move `d0, `s0", a1, k );
       emitMOVE( root, "move `d0, `s0", a0, j );
       declareCALLDefFull();
       emit2( root, "jal "+nameMap.c_function_name(func_name),
              call_def_full, new Temp[] {a0,a1} );
       emitMOVE( root, "move `d0, `s0", i, v0 );
    }
    private void DoFCall(HCodeElement root,
                         Temp i, Temp j, String func_name) {
       /* call auxillary fp routines */
       declare(a0, HClass.Float);
       emitMOVE( root, "move `d0, `s0", a0, j );
       declareCALLDefFull();
       emit2( root, "jal "+nameMap.c_function_name(func_name),
              call_def_full, new Temp[] {a0,a1} );
       emitMOVE( root, "move `d0, `s0", i, v0 );
    }
    private void DoDCall(HCodeElement root,
                         Temp i, Temp j, Temp k, String func_name) {
       /* call auxillary fp routines */
       declare( a3, HClass.Void );
       declare( a2, HClass.Void );
       declare( a1, HClass.Void );
       declare( a0, HClass.Void );
       // not certain an emitMOVE is legal with the l/h modifiers
       Util.assert(j instanceof TwoWordTemp);
       Util.assert(k instanceof TwoWordTemp);
       emit( root, "move `d0, `s0h", a2, k );
       emit( root, "move `d0, `s0l", a3, k );
       emit( root, "move `d0, `s0h", a0, j );
       emit( root, "move `d0, `s0l", a1, j );
       declareCALLDefFull();
       emit2(root, "jal "+nameMap.c_function_name(func_name),
             // uses & stomps on these registers:
             call_def_full, call_use);
       Util.assert(i instanceof TwoWordTemp);
       emit( root, "move `d0h, `s0", i, v0 );
       emit( root, "move `d0l, `s0", i, v1 );
    }
    private void DoDCall(HCodeElement root,
                         Temp i, Temp j, String func_name) {
       /* call auxillary fp routines */
       declare( a1, HClass.Void );
       declare( a0, HClass.Void );
       // not certain an emitMOVE is legal with the l/h modifiers
       Util.assert(j instanceof TwoWordTemp);
       emit( root, "move `d0, `s0h", a0, j );
       emit( root, "move `d0, `s0l", a1, j );
       declareCALLDefFull();
       emit2(root, "jal "+nameMap.c_function_name(func_name),
             // uses & stomps on these registers:
             call_def_full, call_use);
       Util.assert(i instanceof TwoWordTemp);
       emit( root, "move `d0h, `s0", i, v0 );
       emit( root, "move `d0l, `s0", i, v1 );
    }

    private boolean isDoubleWord(Typed ty) {
       switch (ty.type()) {
       case Type.LONG: case Type.DOUBLE: return true;
       default: return false;
       }
    }
    private Temp getArgReg(int i) {
       Util.assert(i < 4 && i >= 0, "Bad arg reg " + i);
       switch(i) {
       case 0: return a0;
       case 1: return a1;
       case 2: return a2;
       case 3: return a3;
       }
       Util.assert(false);
       return null;
    }
    /** Helper for setting up registers/memory with the strongARM standard
     *  calling convention.  Returns the stack offset necessary,
     *  along with a set of registers used by the parameters. */
    private CallState emitCallPrologue(INVOCATION ROOT, 
                                       TempList tlist,
                                       TreeDerivation td) {
       ExpList elist = ROOT.getArgs();
       /** OUTPUT ARGUMENT ASSIGNMENTS IN REVERSE ORDER **/
       List callUses = new ArrayList(call_use.length);
       int stackOffset = 0;
       // reverse list and count # of words required
       TempList treverse=null;
       ExpList ereverse=null;
       int index=0;
       for(TempList tl=tlist; tl!=null; tl=tl.tail, elist=elist.tail) {
          treverse=new TempList(tl.head, treverse);
          ereverse=new ExpList(elist.head, ereverse);
          index+=isDoubleWord(elist.head) ? 2 : 1;
       }
       // add all used registers to callUses list.
       for (int i=0; i<call_use.length; ++i) {
          callUses.add(call_use[i]);
       }
       if(index > 4)
          stackOffset = 16 + (index - 4) * 4;
       index--; // so index points to 'register #' of last argument.

       elist=ereverse;
       for (TempList tl = treverse; tl != null; tl=tl.tail, elist=elist.tail) { 
          Temp temp = tl.head;
          if (isDoubleWord(elist.head)) {
             // arg takes up two words
             switch(index) {
             case 0: throw new Error("Not enough space!");
             case 1: case 2: case 3: // put in registers 
                // not certain an emitMOVE is legal with the l/h modifiers
                Temp rfirst = getArgReg(index--);
                declare(rfirst, HClass.Void);
                Temp rsecnd = getArgReg(index--);
                declare(rsecnd, HClass.Void);
		Util.assert(temp instanceof TwoWordTemp);
                emit( ROOT, "move `d0, `s0h", rfirst, temp );
                emit( ROOT, "move `d0, `s0l", rsecnd, temp );
                break;
             case 4: // spread between regs and stack
                stackOffset -= 4; index--;
                declare( SP, HClass.Void );
		Util.assert(temp instanceof TwoWordTemp);
                emit(new InstrMEM( instrFactory, ROOT,
                                   "sw `s0h, -" + stackOffset + "(`s1)",
                                   new Temp[] { SP },
                                   new Temp[]{ temp, SP })); 
                // not certain an emitMOVE is legal with the l/h modifiers
                Temp rthird = getArgReg(index--);
                declare( rthird, HClass.Void );
                emit( ROOT, "move `d0, `s0l", rthird, temp );
                break;
             default: // start putting args in memory
                stackOffset -= 4; index--;
                declare( SP, HClass.Void );
		Util.assert(temp instanceof TwoWordTemp);
                emit(new InstrMEM( instrFactory, ROOT,
                                   "sw `s0h, -" + stackOffset + "(`s1)",
                                   new Temp[]{ SP },
                                   new Temp[]{ temp, SP })); 
                stackOffset -= 4; index--;
                declare( SP, HClass.Void );
                emit(new InstrMEM( instrFactory, ROOT,
                                   "sw `s0l, -" + stackOffset + "(`s1)",
                                   new Temp[]{ SP },
                                   new Temp[]{ temp, SP }));
                break;
             }
          } else {
             // arg is one word
             if (index < 4) {
                Temp reg = getArgReg(index--); 
                declare( reg, td, elist.head );
                emitMOVE( ROOT, "move `d0, `s0", reg, temp);
             } else {
                stackOffset -= 4; index--;
                declare( SP, HClass.Void );
                emit(new InstrMEM(
                   instrFactory, ROOT,
                   "sw `s0, -" + stackOffset + "(`s1)",
                   new Temp[]{ SP },
                   new Temp[]{ temp, SP }));
             }
          }
       }
       Util.assert(index==-1);
       declareCALL();
       if (ROOT.getRetval()!=null) declare( v0, td, ROOT.getRetval() );
       return new CallState(stackOffset, callUses);
    }
    /** Make a handler stub. */
    private void emitHandlerStub(INVOCATION ROOT, Temp retex, Label handler) {
       declare( retex, frame.getLinker().forName("java.lang.Throwable"));
       emitMOVE ( ROOT, "move `d0, `s0", retex, v0 );
       emitJUMP ( ROOT, "bal "+handler+" # handler stub", handler);
    }
/** Emit a fixup table entry */
    private void emitCallFixup(INVOCATION ROOT, Label retaddr, Label handler) {
      // this '1f' and '1:' business is taking advantage of a GNU
      // Assembly feature to avoid polluting the global name space with
      // local labels
      // these may need to be included in the previous instr to preserve
      // ordering semantics, but for now this way they indent properly
      emitDIRECTIVE( ROOT, !is_elf?".text 10":".data\n.section .flex.fixup, 1, 3, 4, 16");
      emitDIRECTIVE( ROOT, "\t.word "+retaddr+", "+handler+" # (retaddr, handler)");
      emitDIRECTIVE( ROOT, !is_elf?".text 0":".text\n.section .flex.code,  1, 7, 4, 16");
    }
    /** Finish up a CALL or NATIVECALL. */
    private void emitCallEpilogue(INVOCATION ROOT, boolean isNative,
				  Temp retval, HClass type, 
				  CallState cs) {
       // this will break if stackOffset > 255 (ie >63 args)
       Util.assert( cs.stackOffset < 256, 
                    "Update the spec file to handle large SP offsets");
       if (cs.stackOffset!=0) { // optimize for common case.
          declare ( SP, HClass.Void );
          emit( ROOT, "add `d0, `s0, " + cs.stackOffset, SP , SP );
       }
       if (ROOT.getRetval()==null) {
          // this is a void method.  don't bother to emit move.
       } else if (isNative && (!soft_float) &&
                  ROOT.getRetval().type()==Type.FLOAT) {
          // float retval passed in float registers.
          declare(retval, type); declare ( SP, HClass.Void );
          emitMOVE( ROOT, "move `d0, `s0", retval, v0 );
          //emit( ROOT, "s.s $f0, -4($sp)", SP, SP);
          //emit2(ROOT, "lw `d0, 4($sp)",new Temp[]{retval,SP},new Temp[]{SP});
       } else if (isNative && (!soft_float) &&
                  ROOT.getRetval().type()==Type.DOUBLE) {
          // double retval passed in float registers.
          // XXX eW why use 4 both times?
          declare(retval, type); declare ( SP, HClass.Void );
	  Util.assert(retval instanceof TwoWordTemp);
          emit( ROOT, "move `d0h, `s0", retval, v0 );
          emit( ROOT, "move `d0l, `s0", retval, v1 );
          //emit( ROOT, "s.d $f0, -8($sp)", SP, SP);
          //emit2(ROOT, "lw `d0l, 4($sp)",new Temp[]{retval,SP},new Temp[]{SP});
          //emit2(ROOT, "lw `d0h, 4($sp)",new Temp[]{retval,SP},new Temp[]{SP});
       } else if (ROOT.getRetval().isDoubleWord()) {
          // not certain an emitMOVE is legal with the l/h modifiers
          declare(retval, type);
	  Util.assert(retval instanceof TwoWordTemp);
          emit( ROOT, "move `d0h, `s0", retval, v0 );
          emit( ROOT, "move `d0l, `s0", retval, v1 );
       } else {
          declare(retval, type);
          emitMOVE( ROOT, "move `d0, `s0", retval, v0 );
       }  
    }

    // Mandated by CodeGen generic class: perform entry/exit
    public Instr procFixup(HMethod hm, Instr instr,
                           int stackspace, Set usedRegisters) {
       InstrFactory inf = instrFactory; // convenient abbreviation.
       Label methodlabel = nameMap.label(hm);
       // make list of callee-save registers we gotta save.
       StringBuffer reglist = new StringBuffer();

       Temp[] usedRegArray =
          (Temp[]) usedRegisters.toArray(new Temp[usedRegisters.size()]);
       Collections.sort(Arrays.asList(usedRegArray), regComp);
       int nregs=0;
       for (int i=0; i<usedRegArray.length; i++) {
          Temp rX = usedRegArray[i];
          Util.assert(regfile.isRegister(rX));
          if (rX.equals(v0)
              || rX.equals(v1)
              || rX.equals(a0)
              || rX.equals(a1)
              || rX.equals(a2)
              || rX.equals(a3)
              || rX.equals(t0)
              || rX.equals(t1)
              || rX.equals(t2)
              || rX.equals(t3)
              || rX.equals(t4)
              || rX.equals(t5)
              || rX.equals(t6)
              || rX.equals(t7)
              || rX.equals(t8)
              || rX.equals(t9))
             continue; // caller save registers.
          if (rX.equals(LR)||rX.equals(FP)||rX.equals(SP))
             continue; // always saved.
          reglist.append(rX.toString());
          reglist.append(", "); 
          nregs++;
       }
       // Make room for save area for arg registers & FP
       if(stackspace > 0) stackspace += 5;
       // XXX Needed for ra and FP (?)
       if(stackspace == 0) stackspace = 2;
       // find method entry/exit stubs
       Instr last=instr;
       for (Instr il = instr; il!=null; il=il.getNext()) {
          if (il instanceof InstrENTRY) { // entry stub.
             Instr in1 = new InstrDIRECTIVE(inf, il, ".align 2");
             Instr in2 = new InstrDIRECTIVE(inf, il, ".globl " +
                                            methodlabel.name);
             Instr in2a= new InstrDIRECTIVE(inf, il, ".ent " +
                                            methodlabel.name+",#function");
             //Instr in2b= new InstrDIRECTIVE(inf, il, ".set .fpoffset, "+
             // (-4*(4+nregs)));
             Instr in3 = new InstrLABEL(inf, il, methodlabel.name+":",
                                        methodlabel);
             Instr in4 = new Instr(inf, il, "move $30, $sp", null, null);
             Instr in5 = new Instr(inf, il, "sw $31, 0($sp)", null,
                                   null);
             Instr in6 = new Instr(inf, il, "sw $30, 4($sp)", null, null);
             // XXX missing code here to store reglist on stack
             // XXX hack to setup frame pointer
             String assem = "subu $sp, "+(stackspace*4);
             
             Instr in7 = new Instr(inf, il, assem, null, null);
             in7.layout(il, il.getNext());
             in6.layout(il, in7);
             in5.layout(il, in6);
             in4.layout(il, in5);
             in3.layout(il, in4);
             //in2b.layout(il,in3);
             in2a.layout(il,in3);
             in2.layout(il, in2a);
             in1.layout(il, in2);
             if (il==instr) instr=in1; // fixup root if necessary.
             if (stackspace==0) in7.remove(); // optimize
             il.remove(); il=in1;
          }
          if (il instanceof InstrEXIT) { // exit stub
             Instr in1 = new Instr(inf, il, "addu $sp, " + (stackspace*4),
                                   null, null);
             Instr in2 = new Instr(inf, il, "lw $30, 4($sp)",
                                   null, null);
             Instr in3 = new Instr(inf, il, "lw $31, 0($sp)",
                                   null, null);
             Instr in4 = new Instr(inf, il, "j  $31  # return",
                                   null, null);
             in1.layout(il.getPrev(), il);
             in2.layout(in1, il);
             in3.layout(in2, il);
             in4.layout(in3, il);
             il.remove(); 
             il=in3;
          }
          last=il;
       }
       // add a size directive to the end of the function to let gdb
       // know how long it is.
       if (last!=null) { // best be safe.
          // Instructions show up in reverse order
          Instr in1 = new InstrDIRECTIVE(inf, last, "\t.end " +
                                         methodlabel.name);
          // I can't find a way to make the MIPSpro assembler
          // understand this.  It complains about the symbols not
          // having aboslute value
          //Instr in2 = new InstrDIRECTIVE(inf, last, "\t.size " +
          //methodlabel.name + ", . - " +
          //methodlabel.name);
          in1.layout(last, last.getNext());
          //in2.layout(last, in1);
          last=in1;
       }
       // stabs debugging information:
       if (stabsDebugging && !hm.getDeclaringClass().isArray()) {
          int lineno=-1;
          for (Instr il = instr; il!=null; il=il.getNext())
             if (il.getLineNumber()!=lineno) {
                lineno = il.getLineNumber();
                Instr in1 = new InstrDIRECTIVE(inf, il, // line number
                                               "\t.stabd 68,0,"+lineno);
                in1.layout(il.getPrev(), il);
                if (il==instr) instr=in1;
             }
          Instr in1 = new InstrDIRECTIVE(inf, instr, // source path
                                         "\t.stabs \""+
                                         hm.getDeclaringClass().getPackage()
                                         .replace('.','/')+"/"+
                                         "\",100,0,0,"+methodlabel.name);
          Instr in2 = new InstrDIRECTIVE(inf, instr, // source file name
                                         "\t.stabs \""+instr.getSourceFile()+
                                         "\",100,0,0,"+methodlabel.name);
          Instr in3 = new InstrDIRECTIVE(inf, instr, // define void type
                                         "\t.stabs \"void:t19=19\",128,0,0,0"
             );
          Instr in4 = new InstrDIRECTIVE(inf, instr, // mark as function
                                         "\t.stabs \""+
                                         methodlabel.name.substring(1)+":F19"
                                         +"\",36,0,"+(nregs*4)+","+
                                         methodlabel.name);
          in1.layout(instr.getPrev(), instr);
          in2.layout(in1, instr);
          in3.layout(in2, instr);
          instr = in1;
          in4.layout(last, last.getNext());
          last = in4;
       }
       return instr;
    }

    // now define our little InstrENTRY and InstrEXIT sub-types.
    private class InstrENTRY extends Instr {
       public InstrENTRY(InstrFactory inf, HCodeElement src) {
          // defines SP, FP providing reaching defs for
          // InstrEXIT uses.
          super(inf, src, "# --method entry point--",
                new Temp[] { SP, FP }, null);
       }
    }
    private class InstrEXIT extends Instr {
       public InstrEXIT(InstrFactory inf, HCodeElement hce) {
          // uses SP, FP, making them live in whole
          // procedure (so register allocator doesn't stomp
          // on them!)
          super(inf, hce, "# --method exit point--", null, 
                new Temp[]{ SP, FP }, false, null);
       }
    }


%%
%start with %{
	// *** METHOD PROLOGUE ***
	this.instrFactory = inf; // XXX this should probably move to superclass
}%
%end with %{
       // *** METHOD EPILOGUE *** 

}%
    /* this comment will be eaten by the .spec processor (unlike comments above) */
	
/* EXPRESSIONS */ 

/***********************************************************/
/* Arithmetic */

BINOP<p,i>(ADD, j, k) = i
%{
    emit( ROOT, "addu `d0, `s0, `s1", i, j, k);
}%
BINOP<p,i>(ADD, j, CONST<p,i>(c)) = i
%{
    emit( ROOT, "addu `d0, `s0, "+c, i, j);
}%

BINOP<l>(ADD, j, k) = i %extra<i>{ extra }
%{
    Util.assert(i instanceof TwoWordTemp);
   emit( ROOT, "addu `d0l, `s0l, `s1l", i, j, k);
   emit( ROOT, "sltu `d0, `s0l, `s1l", extra, i, k);
   emit( ROOT, "addu `d0h, `s0, `s1h", i, extra, j);
   emit( ROOT, "addu `d0h, `s0h, `s1h", i, i, k);
}%

BINOP<f>(ADD, j, k) = i %{
   DoFCall(ROOT, i,j,k,"__addsf3");
}%

BINOP<d>(ADD, j, k) = i %{
   DoDCall(ROOT, i, j, k, "__adddf3");
}%

    /*-------- SUBTRACT (which, in tree form, is (x + (-y) ) -------- */
BINOP<p,i>(ADD, j, UNOP<p,i>(NEG, k)) = i
%{
    emit( ROOT, "subu `d0, `s0, `s1", i, j, k);
}%
/* I am pretty sure I don't need a constant subtraction pattern, as
   addu a negative constant is just fine */ 
BINOP<p,i>(ADD, j, UNOP<p,i>(NEG, CONST<i>(c))) = i
%{
    emit( ROOT, "subu `d0, `s0, "+negate(c), i, j);
}%

    // okay, back to regular subtractions.
BINOP<l>(ADD, j, UNOP<l>(NEG, k)) = i %extra<i>{ extra }
%{
   emit( ROOT, "sltu `d0, `s0l, `s1l", extra, i, k);
   emit( ROOT, "subu `d0l, `s0l, `s1l", i, j, k);
   emit( ROOT, "subu `d0h, `s0h, `s1h", i, j, k);
   emit( ROOT, "subu `d0h, `s0h, `s1", i, i, extra);
}%

BINOP<f>(ADD, j, UNOP<l>(NEG, k)) = i %{
   DoFCall(ROOT, i, j, k, "__subsf3");
}%
BINOP<d>(ADD, j, UNOP<l>(NEG, k)) = i %{
   DoDCall(ROOT, i, j, k, "__subdf3");
}%

BINOP<p,i>(MUL, j, k) = i %{
    emit( ROOT, "mul `d0, `s0, `s1", i, j, k );
}%

BINOP<p,i>(MUL, j, CONST<p,i>(c)) = i %{
    emit( ROOT, "mul `d0, `s0, "+c, i, j );
}%

BINOP<l>(MUL, j, k) = i %{
   DoLLCall(ROOT, i, j, k, "__ll_mul");
}%

BINOP<f>(MUL, j, k) = i %{
   DoFCall(ROOT, i, j, k, "_mulsf3");
}%

BINOP<d>(MUL, j, k) = i %{
   DoDCall(ROOT, i, j, k, "__muldf3");
}%

BINOP<p,i>(DIV, j, k) = i %{
   emit( ROOT, "div `d0, `s0, `s1", i, j, k );
}%

BINOP<p,i>(DIV, j, CONST<p,i>(c)) = i %{
   emit( ROOT, "div `d0, `s0, "+c, i, j );
}%

BINOP<l>(DIV, j, k) = i %{
   DoLLCall(ROOT, i, j, k, "__ll_div");
}%

BINOP<f>(DIV, j, k) = i %{
   DoFCall(ROOT, i, j, k, "__divsf3");
}%

BINOP<d>(DIV, j, k) = i %{
   DoDCall(ROOT, i, j, k, "__divdf3");
}%

BINOP<i>(REM, j, k) = i %{
   emit( ROOT, "rem `d0, `s0, `s1", i, j, k );
}%

BINOP<i>(REM, j, CONST<p,i>(c)) = i %{
   emit( ROOT, "rem `d0, `s0, "+c, i, j );
}%

BINOP<l>(REM, j, k) = i %{
   DoLLCall(ROOT, i, j, k, "__ll_rem");
}%

UNOP<i,p>(NEG, arg) = i
%{
   emit( ROOT, "negu `d0, `s0", i, arg );
}% 
UNOP<f>(NEG, arg) = i
%{
   DoFCall(ROOT, i, arg, "__f_neg");
}% 
UNOP<d>(NEG, arg) = i
%{
   DoDCall(ROOT, i, arg, "__d_neg");
}% 
UNOP<l>(NEG, arg) = i %extra<i>{ extra }
%{
   emit( ROOT, "seq `d0, `s0l, 0", extra, arg );
   emit( ROOT, "negu `d0l, `s0l", i, arg );
   emit( ROOT, "not `d0h, `s0h", i, arg );
   emit( ROOT, "addu `d0h, `s0h, `s1", i, i, extra );
}% 

/***********************************************************/
/* Logical */

BINOP<p,i>(AND, j, k) = i
%{
    emit( ROOT, "and `d0, `s0, `s1", i, j, k );
}%
BINOP<p,i>(AND, j, CONST<p,i>(c)) = i
%{
    emit( ROOT, "and `d0, `s0, "+c, i, j );
}%
BINOP<l>(AND, j, k) = i %{
    emit( ROOT, "and `d0l, `s0l, `s1l", i, j, k );
    emit( ROOT, "and `d0h, `s0h, `s1h", i, j, k );
}%

BINOP<p,i>(OR, j, k) = i
%{
    emit( ROOT, "or `d0, `s0, `s1", i, j, k );
}%
BINOP<p,i>(OR, j, CONST<p,i>(c)) = i
%{
    emit( ROOT, "or `d0, `s0, "+c, i, j );
}%

BINOP<l>(OR, j, k) = i %{
    emit( ROOT, "or `d0l, `s0l, `s1l", i, j, k );
    emit( ROOT, "or `d0h, `s0h, `s1h", i, j, k );
}%

BINOP<p,i>(XOR, j, k) = i
%{
    emit( ROOT, "xor `d0, `s0, `s1", i, j, k );
}%
BINOP<p,i>(XOR, j, CONST<p,i>(c)) = i
%pred %( is16BitOffset(c) )%
%{
    emit( ROOT, "xor `d0, `s0, "+c, i, j );
}%
BINOP<l>(XOR, j, k) = i %{
    emit( ROOT, "xor `d0l, `s0l, `s1l", i, j, k );
    emit( ROOT, "xor `d0h, `s0h, `s1h", i, j, k );
}%

UNOP<i,p>(NOT, arg) = i
%{
    emit( ROOT, "not `d0, `s0", i, arg );
}% 

UNOP<l>(NOT, arg) = i 
%{
    emit( ROOT, "not `d0l, `s0l", i, arg );
    emit( ROOT, "not `d0h, `s0h", i, arg );
}% 

/***********************************************************/
/* Comparisons */

BINOP(cmpop, j, k) = i
%pred %( (ROOT.operandType()==Type.POINTER || ROOT.operandType()==Type.INT)
	 && isCmpOp(cmpop) )%
%{
   // Lets hear it for the MIPS assembler
   emit( ROOT, cmpOp2AsStr(cmpop) + " `d0, `s0, `s1 # cmpop", i, j, k );
}%

BINOP(CMPEQ, j, k) = i %extra<i>{ extra }
%pred %( ROOT.operandType()==Type.LONG )%
%{
    emit( ROOT, "xor `d0, `s0h, `s1h", extra, j, k );
    emit( ROOT, "xor `d0, `s0l, `s1l", i, j, k );
    emit( ROOT, "or  `d0, `s0,  `s1", i, i, extra );
    emit( ROOT, "sltiu `d0, `s0, 1", i, i );
}%
BINOP(CMPNE, j, k) = i %extra<i>{ extra } /* void*/
%pred %( ROOT.operandType()==Type.LONG )%
%{
    emit( ROOT, "xor `d0, `s0h, `s1h", extra, j, k );
    emit( ROOT, "xor `d0, `s0l, `s1l", i, j, k );
    emit( ROOT, "or  `d0, `s0,  `s1", i, i, extra );
    emit( ROOT, "sltu `d0, $0, `s0", i, i );
}%
BINOP(CMPLT, j, k) = i
%pred %( ROOT.operandType()==Type.LONG )%
%{
    emit( ROOT, "slt `d0, `s0h, `s1h", i, j, k );
    emit( ROOT, "bne `s0h, `s1h, 1f", i, j, k );
    emit( ROOT, "sltu `d0, `s0l, `s1l", i, j, k );
    emitNoFallLABEL( ROOT, "1:", new Label("1"));
}%
BINOP(CMPLE, j, k) = i
%pred %( ROOT.operandType()==Type.LONG )%
%{
    emit( ROOT, "slt `d0, `s0h, `s1h", i, j, k );
    emit( ROOT, "bne `s0h, `s1h, 1f", i, j, k );
    emit( ROOT, "sltu `d0, `s0l, `s1l", i, j, k );
    emitNoFallLABEL( ROOT, "1:", new Label("1"));
}%
BINOP(CMPGT, j, k) = i
%pred %( ROOT.operandType()==Type.LONG )%
%{
    emit( ROOT, "sgt `d0, `s0h, `s1h", i, j, k );
    emit( ROOT, "bne `s0h, `s1h, 1f", i, j, k );
    emit( ROOT, "sgtu `d0, `s0l, `s1l", i, j, k );
    emitNoFallLABEL( ROOT, "1:", new Label("1"));
}%
BINOP(CMPGE, j, k) = i
%pred %( ROOT.operandType()==Type.LONG )%
%{
    emit( ROOT, "sgt `d0, `s0h, `s1h", i, j, k );
    emit( ROOT, "bne `s0h, `s1h, 1f", i, j, k );
    emit( ROOT, "sgeu `d0, `s0l, `s1l", i, j, k );
    emitNoFallLABEL( ROOT, "1:", new Label("1"));
}%
  
BINOP(cmpop, j, k) = i
%pred %( ROOT.operandType()==Type.FLOAT && isCmpOp(cmpop) )%
%{
   DoFCall(ROOT, i, j, k, "_f_" + cmpOp2AsStr(cmpop));
}%

BINOP(cmpop, j, k) = i
%pred %( ROOT.operandType()==Type.DOUBLE && isCmpOp(cmpop) )%
%{
   DoDCall(ROOT, i, j, k, "_d_" + cmpOp2AsStr(cmpop));
}%

/***********************************************************/
/* Shift */

BINOP<p,i>(shiftop, j, k) = i 
%pred %( isShiftOp(shiftop) )%
%{
    // java lang spec says shift should occur according to
    // 'least significant five bits' of k; MIPS does this correctly
   emit( ROOT, shiftOp2Str(shiftop)+" `d0, `s0, `s1", i, j, k);
}%

BINOP<p,i>(shiftop, j, CONST(c)) = i 
%pred %( isShiftOp(shiftop) && is5BitShift(c) )%
%{
    emit( ROOT, shiftOp2Str(shiftop) + " `d0, `s0 "+c, i, j);
}%

BINOP<l>(SHL, j, k) = i %{
   DoLLShiftCall(ROOT, i, j, k, "__ll_lshift");
}%

BINOP<l>(SHR, j, k) = i %{
   DoLLShiftCall(ROOT, i, j, k, "__ull_rshift");
}%

BINOP<l>(USHR, j, k) = i %{
   DoLLShiftCall(ROOT, i, j, k, "__ll_rshift");
}%

/***********************************************************/
/* Constants */

CONST<l,d>(c) = i %{
    long val = (ROOT.type()==Type.LONG) ? ROOT.value.longValue()
	: Double.doubleToLongBits(ROOT.value.doubleValue());

    long loval = val & 0xFFFFFFFF;
    emit(new Instr( instrFactory, ROOT,
		    "li `d0l, " + loval, 
		    new Temp[]{ i }, null ));
    val>>>=32;
    emit(new Instr( instrFactory, ROOT,
		    "li `d0h, " + val, 
		    new Temp[]{ i }, null ));
}% 

CONST<f,i>(c) = i %{
    int val = (ROOT.type()==Type.INT) ? ROOT.value.intValue()
	: Float.floatToIntBits(ROOT.value.floatValue());
    emit(new Instr( instrFactory, ROOT,
		    "li `d0, " + val, 
		    new Temp[]{ i }, null ));
}%

CONST<p>(c) = i %{
    // the only CONST of type Pointer we should see is NULL
    Util.assert(c==null);
    emit(new Instr( instrFactory, ROOT,
                    "li `d0, 0",
                    new Temp[]{ i }, null ));
}%

/***********************************************************/
/* Memory Access */

MEM<s:8,u:8,s:16,u:16,i,p,f>(e) = i %{ 
   String suffix = GetLdSuffix(ROOT);
   emit(new InstrMEM(instrFactory, ROOT,
                     "l"+suffix+" `d0, 0(`s0)",
                     new Temp[]{ i }, new Temp[]{ e }));   
}%

MEM<s:8,u:8,s:16,u:16,p,i,f>(BINOP<p>(ADD, j, CONST<i,p>(c))) = i
%{
   String suffix = GetLdSuffix(ROOT);
   emit(new InstrMEM(instrFactory, ROOT,
                     "l"+suffix+" `d0, " + c + "(`s0)",
                     new Temp[]{ i }, new Temp[]{ j }));
}%
MEM<l,d>(e) = i %{
    emit(new InstrMEM(instrFactory, ROOT,
                      "lw `d0l, 4(`s0)",
                      new Temp[]{ i }, new Temp[]{ e }));
    emit(new InstrMEM(instrFactory, ROOT,
                      "lw `d0h, 0(`s0)",
                      new Temp[]{ i }, new Temp[]{ e }));
}%

MOVE(MEM<s:8,u:8,s:16,u:16,p,i,f>(dst), src)
%{
   String suffix = GetStSuffix(ROOT);
   emit(new InstrMEM(instrFactory, ROOT, "s" + suffix +" `s0, 0(`s1)",
                     null, new Temp[] {src, dst}));
}%

MOVE(MEM<s:8,u:8,s:16,u:16,p,i,f>(BINOP<p>(ADD, j, CONST<i,p>(c))), src)
%{ 
   String suffix = GetStSuffix(ROOT);
   emit(new InstrMEM(instrFactory, ROOT,
		      "s"+suffix+" `s0, "+c+"(`s1)",
		      null, new Temp[]{ src, j }));   
}%

MOVE(MEM<s:8,u:8,s:16,u:16,p,i,f>(BINOP<p>(ADD, j, CONST<i,p>(c))), NAME(src))
   %extra<i>{ extra } /*void*/
%{ 
   String suffix = GetStSuffix(ROOT);
   emit(new Instr( instrFactory, ROOT,
                   "la `d0, " + src,
                   new Temp[]{ extra }, null ));
   emit(new InstrMEM(instrFactory, ROOT, "s" + suffix +" `s0, "
                     + c + "(`s0)",
                     null, new Temp[]{ extra, j }));
}%

MOVE(MEM<s:8,u:8,s:16,u:16,p,i,f>(dst), NAME(src)) %extra<i>{ extra }/*void*/
%{ 
   String suffix = GetStSuffix(ROOT);
   emit(new Instr( instrFactory, ROOT,
                   "la `d0, " + src,
                   new Temp[]{ extra }, null ));
   emit(new InstrMEM(instrFactory, ROOT, "s" + suffix +" `s0, 0(`s1)",
                     null, new Temp[] {extra, dst}));
}%
MOVE(MEM<s:8,u:8,s:16,u:16,p,i,f>(NAME(dst)), src) %extra<i>{ extra }/*void*/
%{ 
   String suffix = GetStSuffix(ROOT);
   emit(new Instr( instrFactory, ROOT,
                   "la `d0, " + dst,
                   new Temp[]{ extra }, null ));
   emit(new InstrMEM(instrFactory, ROOT, "s" + suffix +" `s0, (`s1)",
                     null, new Temp[] {src, extra}));
}%

MOVE(MEM<s:8,u:8,s:16,u:16,p,i,f>(NAME(dst)), NAME(src))
%extra<i>{ extra0, extra1 }/*void*/
%{ 
   String suffix = GetStSuffix(ROOT);
   emit(new Instr( instrFactory, ROOT,
                   "la `d0, " + src,
                   new Temp[]{ extra0 }, null ));
   emit(new Instr( instrFactory, ROOT,
                   "la `d0, " + dst,
                   new Temp[]{ extra1 }, null ));
   emit(new InstrMEM(instrFactory, ROOT, "s" + suffix +" `s0, (`s1)",
                     null, new Temp[] {extra0, extra1}));
}%
MOVE(MEM<s:8,u:8,s:16,u:16,p,i,f>(NAME(dst)), CONST<i,p>(c)) 
   %extra<i>{ extra0, extra1 }/*void*/
%{ 
   String suffix = GetStSuffix(ROOT);
   emit(new Instr( instrFactory, ROOT,
                   "li `d0, " + c,
                   new Temp[]{ extra0 }, null ));
   emit(new Instr( instrFactory, ROOT,
                   "la `d0, " + dst,
                   new Temp[]{ extra1 }, null ));
   emit(new InstrMEM(instrFactory, ROOT, "s" + suffix +" `s0, (`s1)",
                     null, new Temp[] { extra0, extra1 }));
}%


// C compiler IRIX on mips places long longs big endian
MOVE(MEM<l,d>(dst), src) %{
   emit(new InstrMEM(instrFactory, ROOT, "sw `s0l, 4(`s1)",
                     null, new Temp[]{ src, dst }));   
   emit(new InstrMEM(instrFactory, ROOT, "sw `s0h, 0(`s1)",
                     null, new Temp[]{ src, dst }));   
}%

MOVE<p,i,f>(dst, src) %{
    declare( dst, code.getTreeDerivation(), ROOT.getSrc());
    emitMOVE( ROOT, "move `d0, `s0", dst, src );
}%

MOVE<d,l>(TEMP(dst), src) %{
    Util.assert( dst instanceof TwoWordTemp, "why is dst: "+dst + 
		 " a normal Temp? " + harpoon.IR.Tree.Print.print(ROOT));

    Util.assert(src instanceof TwoWordTemp, "why is src: "+src + 
		 " a normal Temp? " + harpoon.IR.Tree.Print.print(ROOT));

    declare( dst, code.getTreeDerivation(),  ROOT.getSrc() );
    // not certain an emitMOVE is legal with the l/h modifiers
    emit( ROOT, "move `d0l, `s0l", dst, src );
    emit( ROOT, "move `d0h, `s0h", dst, src );
}%



// Put a label in a register
NAME(id) = i %{
   emit(new Instr( instrFactory, ROOT,
                   "la `d0, " + id, 
                   new Temp[]{ i }, null ));
}%

MEM<f,i,p>(NAME(id)) = i %{
   emit(new Instr( instrFactory, ROOT,
                   "la `d0, " + id, 
                   new Temp[]{ i }, null ));
}%
MEM<d,l>(NAME(id)) = i %{

    emit(new Instr( instrFactory, ROOT,
		    "la `d0l, " + id, 
		    new Temp[]{ i }, null ));
    emit(new Instr( instrFactory, ROOT,
		    "la `d0h, " + id + "+4", 
		    new Temp[]{ i }, null ));
}%


TEMP(t) = i %{ i=t; /* this case is basically handled entirely by the CGG */ }%


UNOP(_2B, arg) = i %pred %( ROOT.operandType() == Type.INT )%
%{
    emit( ROOT, "sra `d0, `s0, 8", i, arg);
}%
UNOP(_2S, arg) = i %pred %( ROOT.operandType() == Type.INT )%
%{
    emit( ROOT, "sra `d0, `s0, 16", i, arg);
}%
UNOP(_2C, arg) = i %pred %( ROOT.operandType() == Type.INT )%
%{
    emit( ROOT, "sra `d0, `s0, 16", i, arg);
}%
UNOP(_2F, arg) = i %pred %( ROOT.operandType() == Type.INT )%
%{
   DoFCall(ROOT, i, arg, "__i2f");
}%
UNOP(_2D, arg) = i %pred %( ROOT.operandType() == Type.INT )%
%{
   DoDCall(ROOT, i, arg, "__i2d");
}%
UNOP(_2L, arg) = i %pred %( ROOT.operandType() == Type.INT )%
%{
    emit( ROOT, "move `d0l, `s0", i, arg );
    emit( ROOT, "sra `d0h, `s0, 31", i, arg );
}%
/* Trivial pointer to integer */
UNOP<p>(_2I, arg) = i %pred %( ROOT.operandType() == Type.POINTER )%
%{
    emitMOVE( ROOT, "move `d0, `s0", i, arg );
}%



UNOP(_2B, arg) = i %pred %( ROOT.operandType() == Type.LONG )%
%{
   Util.assert(false, "Spec file doesn't handle long-to-byte conversion directly");
}%
UNOP(_2C, arg) = i %pred %( ROOT.operandType() == Type.LONG )%
%{
   Util.assert(false, "Spec file doesn't handle long-to-char conversion directly");
}%
UNOP(_2S, arg) = i %pred %( ROOT.operandType() == Type.LONG )%
%{
   Util.assert(false, "Spec file doesn't handle long-to-short conversion directly");
}%
UNOP(_2I, arg) = i %pred %( ROOT.operandType() == Type.LONG )%
%{
    emit( ROOT, "move `d0, `s0l", i, arg );
}%


UNOP(_2B, arg) = i %pred %( ROOT.operandType() == Type.FLOAT )%
%{
   Util.assert(false, "Spec file doesn't handle float-to-byte conversion directly");
}%
UNOP(_2C, arg) = i %pred %( ROOT.operandType() == Type.FLOAT )%
%{
   Util.assert(false, "Spec file doesn't handle float-to-char conversion directly");
}%
UNOP(_2S, arg) = i %pred %( ROOT.operandType() == Type.FLOAT )%
%{
   Util.assert(false, "Spec file doesn't handle float-to-short conversion directly");
}%
UNOP(_2I, arg) = i %pred %( ROOT.operandType() == Type.FLOAT )%
%{
   DoFCall(ROOT, i, arg, "__f2i");
}%
UNOP(_2D, arg) = i %pred %( ROOT.operandType() == Type.FLOAT )%
%{
   DoDCall(ROOT, i, arg, "__f2d");
}%


UNOP(_2B, arg) = i %pred %( ROOT.operandType() == Type.DOUBLE )%
%{
    Util.assert(false, "Spec file doesn't handle double-to-byte conversion directly");
}%
UNOP(_2C, arg) = i %pred %( ROOT.operandType() == Type.DOUBLE )%
%{
   Util.assert(false, "Spec file doesn't handle double-to-char conversion directly");
}%
UNOP(_2S, arg) = i %pred %( ROOT.operandType() == Type.DOUBLE )%
%{
   Util.assert(false, "Spec file doesn't handle double-to-short conversion directly");
}%
UNOP(_2I, arg) = i %pred %( ROOT.operandType() == Type.DOUBLE )%
%{
   DoDCall(ROOT, i, arg, "__d2i");
}%


/***********************************************************/
/* Expressions */

EXPR(e) %{
			/* this is a statement that's just an
			   expression; just throw away 
			   calculated value */
}%

/***********************************************************/
/* Conditional jumps and jumps */

CJUMP(test, iftrue, iffalse) %{
   Instr j1 = emit( ROOT, "beq `s0, 0, `L0",
                    null, new Temp[]{ test },
                    new Label[]{ iftrue });
   // emitDELAYSLOT( ROOT );
   // Package the delay slot nop with the fall through branch so they
   // can both be eliminated together
   Instr j2 = emitJUMP( ROOT, "b `L0", iffalse );
   //Instr j2 = emitJUMP( ROOT, "b `L0; nop", iffalse );
}%

CJUMP(BINOP(cmpop, j, k), iftrue, iffalse)
%pred %( isCmpOp(cmpop) &&
	 ( ((BINOP) ROOT.getTest()).operandType()==Type.POINTER ||
	   ((BINOP) ROOT.getTest()).operandType()==Type.INT ) )%
%{
   emit( ROOT, cmpOp2BrStr(cmpop) + " `s0, `s1, `L0",
         null, new Temp[] { j , k }, new Label[] { iftrue });
   //emitDELAYSLOT( ROOT );
   // Package the delay slot nop with the fall through branch so they
   // can both be eliminated together
   emitJUMP( ROOT, "b `L0", iffalse );
   //emitJUMP( ROOT, "b `L0; nop", iffalse );
}%

CJUMP(BINOP(cmpop, j, CONST<i,p>(c)), iftrue, iffalse)
   %pred %( isCmpOp(cmpop) )%
%{
   Util.assert(((BINOP) ROOT.getTest()).operandType()!=Type.POINTER ||
               c == null, "Can not compare a pointer to anything but null\n");
   if(((BINOP) ROOT.getTest()).operandType() == Type.POINTER &&
      c == null) { 
      emit( ROOT, cmpOp2BrStr(cmpop) + " `s0, $0, `L0 # null",
            null, new Temp[] { j }, new Label[] { iftrue });
   } else {
      emit( ROOT, cmpOp2BrStr(cmpop) + " `s0, " + c + ", `L0 " 
            + (c==null ? (" # null ") : ""),
            null, new Temp[] { j }, new Label[] { iftrue });
   }
   //emitDELAYSLOT( ROOT );
   // Package the delay slot nop with the fall through branch so they
   // can both be eliminated together
   emitJUMP( ROOT, "b `L0", iffalse );
   //   emitJUMP( ROOT, "b `L0; nop", iffalse );
}%

JUMP(NAME(l)) %{ // direct jump
   emitNoFall (ROOT, "j " + l + "", null, null, new Label[] { l });
   //emitDELAYSLOT( ROOT );
}%

JUMP(e) %{
    List labelList = LabelList.toList( ROOT.targets );
    Instr j = 
       emit(new Instr( instrFactory, ROOT, 
                       "j `s0",
                       new Temp[]{ },
                       new Temp[]{ e },
                       false, labelList ) {
             public boolean hasModifiableTargets(){ 
                return false; 
             }
          });
    //emitDELAYSLOT( ROOT );
}%


LABEL(id) %{
    if (ROOT.exported) {
      emitLABEL( ROOT, "\t.globl "+ROOT.label+"\n"+
		    ROOT.label + ":", ROOT.label);
    } else {
      emitLABEL( ROOT, ROOT.label + ":", ROOT.label);
    }
}%

/***********************************************************/
/* Calls */

METHOD(params) %{
    // mark entry point.
    declare(SP, HClass.Void);
    declare(FP, HClass.Void);
    emit(new InstrENTRY( instrFactory, ROOT ));
    // move arguments to temporaries.
    int loc=0;
    // skip param[0], which is the explicit 'exceptional return address'
    for (int i=1; i<params.length; i++) {
       declare(params[i], code.getTreeDerivation(), ROOT.getParams(i));
       if (ROOT.getParams(i).isDoubleWord()) {
          if (loc<=2) { // both halves in registers
             // ack.  emitMOVE isn't working with long/double types.
             emit( ROOT, "move `d0l, `s0", params[i],regfile.reg[loc++]);
             emit( ROOT, "move `d0h, `s0", params[i],regfile.reg[loc++]);
          } else {
             // loc == 3 could mean on in register, one on stack, but
             // that is not what the C IRIX/MIPS compiler does.
             emit(new InstrMEM( instrFactory, ROOT,
                                "lw `d0h, "+(4*(loc++)+16)+"(`s0)",
                                new Temp[] {params[i]}, new Temp[] {FP}));
             emit(new InstrMEM( instrFactory, ROOT,
                                "lw `d0l, "+(4*(loc++)+16)+"(`s0)",
                                new Temp[] {params[i]}, new Temp[] {FP}));
          }
       } else { // single word.
          if (loc<4) { // in register
             emitMOVE( ROOT, "move `d0, `s0", params[i], regfile.reg[loc++]);
          } else { // on stack
             emit(new InstrMEM( instrFactory, ROOT,
                                "lw `d0, "+(4*(loc++)+16)+"(`s0)",
                                new Temp[] {params[i]}, new Temp[] {FP}));
          }
       }
    }
}%

THROW(val, handler) %{
   // ignore handler, as our runtime does clever things instead.
   declare( v0, code.getTreeDerivation(), ROOT.getRetex() );
   emitMOVE( ROOT, "move `d0, `s0", v0, val );
   declareCALLDefBuiltin();
   emit( ROOT, "jal "+nameMap.c_function_name("_lookup_handler")+
         " # hi mom ",
         call_def_builtin, // clobbers
		 call_use, true, null); 
   // mark exit point.
   emit(new InstrEXIT( instrFactory, ROOT ));
}%

// slow version when we don't know exactly which method we're calling.
CALL(retval, retex, func, arglist, handler)
   %pred %( !ROOT.isTailCall )%
%{
   CallState cs = emitCallPrologue(ROOT, arglist, code.getTreeDerivation());
   Label rlabel = new Label(), elabel = new Label();
   declareCALLDefFull();
   // call uses 'func' as `s0
   emitCallNoFall( ROOT, cs.prependSPOffset("jal `s0 "),
                   call_def_full,
                   (Temp[]) cs.callUses.toArray(new Temp[cs.callUses.size()]),
                   new Label[] { rlabel, elabel } );
   // make handler stub.
   emitLABEL( ROOT, elabel+":", elabel);
   emitHandlerStub(ROOT, retex, handler);
   // normal return
   emitLABEL( ROOT, rlabel+":", rlabel);
   emitCallEpilogue(ROOT, false, retval,
                    ((ROOT.getRetval()==null)?null:
                     code.getTreeDerivation().typeMap(ROOT.getRetval())), cs);
   // emit fixup table.
   emitCallFixup(ROOT, rlabel, elabel);
}%
// optimized version when we know exactly which method we're calling.
CALL(retval, retex, NAME(funcLabel), arglist, handler)
   %pred %( !ROOT.isTailCall )%
%{
   CallState cs = emitCallPrologue(ROOT, arglist, code.getTreeDerivation());
   Label rlabel = new Label(), elabel = new Label();
   declareCALLDefFull();
   emitCallNoFall( ROOT, cs.prependSPOffset("jal "+funcLabel ),
                   call_def_full,
                   (Temp[]) cs.callUses.toArray(new Temp[cs.callUses.size()]),
                   new Label[] { rlabel, elabel } );
   // make handler stub.
   emitLABEL( ROOT, elabel+":", elabel);
   emitHandlerStub(ROOT, retex, handler);
   // normal return
   emitLABEL( ROOT, rlabel+":", rlabel);
   emitCallEpilogue(ROOT, false, retval,
                    ((ROOT.getRetval()==null)?null:
                     code.getTreeDerivation().typeMap(ROOT.getRetval())), cs);
   // emit fixup table.
   emitCallFixup(ROOT, rlabel, elabel);
}%
// slow version when we don't know exactly which method we're calling.
NATIVECALL(retval, func, arglist) %{
   CallState cs = emitCallPrologue(ROOT, arglist,
                                   code.getTreeDerivation());
    // call uses 'func' as `s0
   cs.callUses.add(0, func);
   declareCALLDefFull();
   emitNativeCall( ROOT, cs.prependSPOffset("jal `s0"),
                   call_def_full,
                   (Temp[]) cs.callUses.toArray(new Temp[cs.callUses.size()]),
                   true, null);
   // clean up.
   emitCallEpilogue(ROOT, true, retval, 
                    ((ROOT.getRetval()==null)?null:
                     code.getTreeDerivation().typeMap(ROOT.getRetval())), cs);
}%
// optimized version when we know exactly which method we're calling.
// eW this is not optimized as the more general version does not need
// to be pessimized as it does on StrongARM.
NATIVECALL(retval, NAME(funcLabel), arglist) 
%{
   CallState cs = emitCallPrologue(ROOT, arglist,
                                   code.getTreeDerivation());
   declareCALLDefFull();
   emitNativeCall( ROOT, cs.prependSPOffset("jal " + funcLabel),
                   call_def_full,
                   (Temp[]) cs.callUses.toArray(new Temp[cs.callUses.size()]),
                   true, null);
   // clean up.
   emitCallEpilogue(ROOT, true, retval, 
                    ((ROOT.getRetval()==null)?null:
                     code.getTreeDerivation().typeMap(ROOT.getRetval())), cs);
}%

RETURN<i,f,p>(val) %{
   declare( v0, code.getTreeDerivation(),  ROOT.getRetval() );
   emitMOVE( ROOT, "move `d0, `s0", v0, val );
   // mark exit point.
   emit(new InstrEXIT( instrFactory, ROOT ));
}%

RETURN<l,d>(val) %{
   // these should really be InstrMOVEs!
   declare( v0, HClass.Void );
   declare( v1, HClass.Void );
   emit( ROOT, "move `d0, `s0h", v0, val);
   emit( ROOT, "move `d0, `s0l", v1, val);
   // mark exit point.
   emit(new InstrEXIT( instrFactory, ROOT ));
}%

/***********************************************************/
/* Data */

DATUM(CONST<i,f>(exp)) %{
   int i = (ROOT.getData().type()==Type.INT) ? exp.intValue()
      : Float.floatToIntBits(exp.floatValue());
   String lo = "0x"+Integer.toHexString(i);
   emitDIRECTIVE( ROOT, "\t.word "+lo+" # "+exp);
}%

DATUM(CONST<l,d>(exp)) %{
   long l = (ROOT.getData().type()==Type.LONG) ? exp.longValue()
      : Double.doubleToLongBits(exp.doubleValue());
   String lo = "0x"+Integer.toHexString((int)l);
   String hi = "0x"+Integer.toHexString((int)(l>>32));
   // doubles are stored in reverse order on the StrongARM.  No, I don't
   // know why.  
   if (ROOT.getData().type()==Type.LONG)
      emitDIRECTIVE( ROOT, "\t.word "+lo+" # lo("+exp+")");
   emitDIRECTIVE( ROOT, "\t.word "+hi+" # hi("+exp+")");
   if (ROOT.getData().type()==Type.DOUBLE)
      emitDIRECTIVE( ROOT, "\t.word "+lo+" # lo("+exp+")");
}%

DATUM(CONST<p>(exp)) %{
   emitDIRECTIVE( ROOT, "\t.word 0 # null pointer constant");
}%

DATUM(CONST<s:8,u:8>(exp)) %{
   String chardesc = (exp.intValue()>=32 && exp.intValue()<127 
                      && exp.intValue()!=96 /* backquotes cause problems */
                      && exp.intValue()!=34 /* so do double quotes */) ?
      ("\t# char "+((char)exp.intValue())) : "";
   emitDIRECTIVE( ROOT, "\t.byte "+exp+chardesc);
}%

DATUM(CONST<s:16,u:16>(exp)) %{
   String chardesc = (exp.intValue()>=32 && exp.intValue()<127
                      && exp.intValue()!=96 /* backquotes cause problems */
                      && exp.intValue()!=34 /* so do double quotes */) ?
      ("\t# char "+((char)exp.intValue())) : "";
   emitDIRECTIVE( ROOT, "\t.half "+exp+chardesc);
}%

DATUM(NAME(l)) %{
   emitDIRECTIVE( ROOT, "\t.word "+l);
}%

ALIGN(n) %{
   emitDIRECTIVE( ROOT, "\t.align "+n);
}%

SEGMENT(CLASS) %{
   emitDIRECTIVE( ROOT, !is_elf?".data 1":".data\n.section .flex.class, 1, 3, 4, 16");

}%

SEGMENT(CODE) %{
   // gas 2.7 does not support naming the code section...not
   // sure what to do about this yet...
   // emitDIRECTIVE( ROOT, !is_elf?".code 32":".section code");
   emitDIRECTIVE( ROOT, !is_elf?".text 0":".text\n.section .flex.code, 1, 7, 4, 16");
}%

SEGMENT(GC) %{
   emitDIRECTIVE( ROOT, !is_elf?".data 2":".data\n.section .flex.gc, 1, 3, 4, 16");
}%

SEGMENT(INIT_DATA) %{
   emitDIRECTIVE( ROOT, !is_elf?".data 3":".section .flex.init_data, 1, 3, 4, 16");
}%

SEGMENT(STATIC_OBJECTS) %{
   emitDIRECTIVE( ROOT, !is_elf?".data 4":".section .flex.static_objects, 1, 3, 4, 16");
}%

SEGMENT(STATIC_PRIMITIVES) %{
   emitDIRECTIVE( ROOT, !is_elf?".data 5":".section .flex.static_primitives, 1, 3, 4, 16");
}%

SEGMENT(STRING_CONSTANTS) %{
   emitDIRECTIVE( ROOT, !is_elf?".data 6":".section .flex.string_constants, 1, 3, 4, 16");
}%

SEGMENT(STRING_DATA) %{
   emitDIRECTIVE( ROOT, !is_elf?".data 7":".section .flex.string_data, 1, 3, 4, 16");
}%

SEGMENT(REFLECTION_OBJECTS) %{
   emitDIRECTIVE( ROOT, !is_elf?".data 8":".section .flex.reflection_objects, 1, 3, 4, 16");
}%

SEGMENT(REFLECTION_DATA) %{
   emitDIRECTIVE( ROOT, !is_elf?".data 9":".section .flex.reflection_data, 1, 3, 4, 16");
}%

SEGMENT(GC_INDEX) %{
   emitDIRECTIVE( ROOT, !is_elf?".data 10":".section .flex.gc_index, 1, 3, 4, 16");
}%

SEGMENT(TEXT) %{
   emitDIRECTIVE( ROOT, !is_elf?".text":".text\n.section .text, 1, 7, 4, 16");
}%

SEGMENT(ZERO_DATA) %{
   // gas 2.7 does not allow BSS subsections...use .comm and .lcomm
   // for the variables to be initialized to zero
   // emitDIRECTIVE( ROOT, ".bss   \t#.section zero");
   emitDIRECTIVE(ROOT, !is_elf?".bss":".section .flex.zero, 1, 3, 4, 16");
}%
// Local Variables:
// mode:java
// End:
