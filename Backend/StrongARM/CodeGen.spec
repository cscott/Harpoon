// CodeGen.spec, created Tue Jul 6 12:12:41 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrMOVE;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrDIRECTIVE;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.Uop;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Typed;
import harpoon.IR.Tree.ExpList;
import harpoon.Backend.Generic.DefaultFrame;
import harpoon.Backend.Generic.Code;
import harpoon.Util.Util;
import harpoon.Temp.Temp;
import harpoon.Temp.LabelList;
import harpoon.Temp.Label;

import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.INVOCATION;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.EXP;
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

import java.util.Map;
import java.util.HashMap;

/**
 * <code>CodeGen</code> is a code-generator for the ARM architecture.
 * 
 * @see Jaggar, <U>ARM Architecture Reference Manual</U>
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CodeGen.spec,v 1.1.2.23 1999-08-07 00:43:53 pnkfelix Exp $
 */
%%


    // FIELDS
    // first = null OR first instr passed to emit(Instr)
    private Instr first;

    // last = null OR last instr passed to emit(Instr)
    private Instr last; 
    
    // InstrFactory to generate Instrs from
    private InstrFactory instrFactory;
    
    // Frame for instructions to access to get platform specific variables (Register Temps, etc) 
    private SAFrame frame;

    private Temp r0, r1, r2, r3;

    private TEMP param0;

    Map blMap;
    Map liMap;

    public CodeGen(SAFrame frame) {
	last = null;
	this.frame = frame;
	r0 = frame.getAllRegisters()[0];
	r1 = frame.getAllRegisters()[1];
	r2 = frame.getAllRegisters()[2];
	r3 = frame.getAllRegisters()[3];
	blMap = new HashMap();
	liMap = new HashMap();
    }

    private void emit(Instr i) {
	debug( "Emitting "+i.toString() );
	if (first == null) first = i;
	if (last == null) {
	    last = i;
	} else {
	    last.insertInstrAfter(last, i);
	    last = i;
	}
    }
    
    /** Single dest Single source Emit Helper. */
    private void emit( HCodeElement root, String assem, 
		       Temp dst, Temp src) {
	emit(new Instr( instrFactory, root, assem,
			new Temp[]{ dst },
			new Temp[]{ src }));
    }
    

    /** Single dest Two source Emit Helper. */
    private void emit( HCodeElement root, String assem, 
		       Temp dst, Temp src1, Temp src2) {
	emit(new Instr( instrFactory, root, assem,
			new Temp[]{ dst },
			new Temp[]{ src1, src2 }));
    }

    /** Null dest Null source Emit Helper. */
    private void emit( HCodeElement root, String assem ) {
	emit(new Instr( instrFactory, root, assem, null, null));
    }

    /** Single dest Single source EmitMOVE helper */
    private void emitMOVE( HCodeElement root, String assem,
			   Temp dst, Temp src) {
	emit(new InstrMOVE( instrFactory, root, assem,
			    new Temp[]{ dst },
			    new Temp[]{ src }));
    }			         


    private void emitLABEL( HCodeElement root, String assem, Label l ) {
	emit( new InstrLABEL( instrFactory, root, assem, l ));
    }	

    private void emitDIRECTIVE( HCodeElement root, String assem ) {
	emit( new InstrDIRECTIVE( instrFactory, root, assem ));
    }

    private Temp makeTemp() {
	    return new Temp(frame.tempFactory());
    }

    private TwoWordTemp makeTwoWordTemp() {
	    return new TwoWordTemp(frame.tempFactory());
    }
%%
%start with %{
       // *** METHOD PROLOGUE *** 
       this.instrFactory = inf;
}%
%end with %{
       // *** METHOD EPILOGUE *** 
       Util.assert(first != null, "Should always generate some instrs");
       return first;
}%
    /* this comment will be eaten by the .spec processor (unlike comments above) */
	
/* EXPRESSIONS */ 
BINOP<p,i>(ADD, j, k) = i %{		
    Temp i = makeTemp();		
    emit( ROOT, "add `d0, `s0, `s1", i, j, k);
}%

BINOP<l>(ADD, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "adds `d0l, `s0l, `s1l\n"+
		"adc  `d0h, `s0h, `s1h", i, j, k );
}%

BINOP<f>(ADD, j, k) = i %{
    /* call auxillary fp routines */
    Temp i = makeTemp();		
    emitMOVE( ROOT, "mov `d0, `s0", r1, k );
    emitMOVE( ROOT, "mov `d0, `s0", r0, j );
    emit( ROOT, "bl ___addsf" );
    emitMOVE( ROOT, "mov `d0, `s0", i, r0 );
}%

BINOP<d>(ADD, j, k) = i %{
    /* call auxillary fp routines */
    Temp i = makeTwoWordTemp();		
        // not certain an emitMOVE is legal with the l/h modifiers
    emitMOVE( ROOT, "mov `d0, `s0l", r2, k );
    emitMOVE( ROOT, "mov `d0, `s0h", r3, k );
    emitMOVE( ROOT, "mov `d0, `s0l", r0, j );
    emitMOVE( ROOT, "mov `d0, `s0h", r1, j );
    emit( ROOT, "bl ___adddf3" );
    emitMOVE( ROOT, "mov `d0l, `s0", i, r0 );
    emitMOVE( ROOT, "mov `d0h, `s0", i, r3 );
}%

BINOP<p,i>(AND, j, k) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "and `d0, `s0, `s1", i, j, k );
}%

BINOP<l>(AND, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "and `d0l, `s0l, `s1l", i, j, k );
    emit( ROOT, "and `d0h, `s0h, `s1h", i, j, k );
}%

BINOP<p,i>(CMPEQ, j, k) = i %{
    Temp i = makeTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit( ROOT, "cmp `s0, `s1\n"+
		"moveq `d0, #1\n"+
		"movne `d0, #0", i, j, k );
}%

BINOP<l>(CMPEQ, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit( ROOT, "cmp `s0l, `s1l\n"+
		"cmpeq `s0h, `s1h\n"+
		"moveq `d0, #1\n"+
		"movne `d0, #0", i, j, k );
}%
  
BINOP<f>(CMPEQ, j, k) = i %{
    Temp i = makeTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit(new Instr( instrFactory, ROOT, 
		    "mov `d1, `s1\n"+
		    "mov `d0, `s0\n"+
		    "bl ___eqsf2\n"+
		    "cmp `s2, #0\n"+
		    "moveq `d3, #1\n"+
		    "movne `d3, #0", 
		   new Temp[]{ r0, r1, i },
		   new Temp[]{ j, k, r0 }));
}%

BINOP<d>(CMPEQ, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit(new Instr(instrFactory, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___eqdf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n"+
		   "movne `d3, #0",
		   new Temp[]{ r0, r1, i },
		   new Temp[]{ j, k, r0 }));
}%

BINOP<p,i>(CMPGT, j, k) = i %{
    Temp i = makeTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit(ROOT, "cmp `s0, `s1\n"+	
	       "movgt `d0, #1\n"+	
	       "movne `d0, #0", i, j, k );
}%

BINOP<l>(CMPGT, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit( ROOT, "cmp `s0h, `s1h\n"+
		"cmpeq `s0l, `s1l\n"+
		"movgt `d0, #1\n"+
		"movle `d0, #0", i, j, k );
}%

BINOP<f>(CMPGT, j, k) = i %{
    Temp i = makeTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit(new Instr(instrFactory, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___gtsf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n"+
		   "movne `d3, #0",
		   new Temp[]{ r0, r1, i },
		   new Temp[]{ j, k, r0 }));
}%

BINOP<d>(CMPGT, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit(new Instr(instrFactory, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___gtdf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n"+
		   "movne `d3, #0",
		   new Temp[]{ r0, r1, i },
		   new Temp[]{ j, k, r0 }));
}%


BINOP<p,i>(CMPGE, j, k) = i %{
    Temp i = makeTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit( ROOT, "cmp `s0, `s1\n"+
		"movge `d0, #1\n"+
		"movlt `d0, #0", i, j, k );
}%

BINOP<l>(CMPGE, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit( ROOT, "cmp `s0h, `s1h\n"+
		"cmpeq `s0l, `s1l\n"+
		"movge `d0, #1\n"+
		"movlt `d0, #0", i, j, k );
}%

BINOP<f>(CMPGE, j, k) = i %{
    Temp i = makeTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit(new Instr(instrFactory, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___gesf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n"+
		   "movne `d3, #0",
		   new Temp[]{ r0, r1, i },
		   new Temp[]{ j, k, r0 }));
}%

BINOP<d>(CMPGE, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit(new Instr(instrFactory, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___gedf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n"+
		   "movne `d3, #0",
		   new Temp[]{ r0, r1, i },
		   new Temp[]{ j, k, r0 }));
}%


BINOP<p,i>(CMPLE, j, k) = i %{
    Temp i = makeTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit( ROOT, "cmp `s0, `s1\n"+
		"movle `d0, #1\n"+
		"movgt `d0, #0", i, j, k );
}%

BINOP<l>(CMPLE, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit( ROOT, "cmp `s0h, `s1h\n"+
		"cmpeq `s0l, `s1l\n"+
		"movle `d0, #1\n"+
		"movgt `d0, #0", i, j, k );
}%

BINOP<f>(CMPLE, j, k) = i %{
    Temp i = makeTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit(new Instr(instrFactory, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___lesf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n"+
		   "movne `d3, #0",
		   new Temp[]{ r0, r1, i },
		   new Temp[]{ j, k, r0 }));
}%

BINOP<d>(CMPLE, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit(new Instr(instrFactory, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___ledf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n"+
		   "movne `d3, #0",
		   new Temp[]{ r0, r1, i },
		   new Temp[]{ j, k, r0 }));
}%

BINOP<p,i>(CMPLT, j, k) = i %{
    Temp i = makeTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit( ROOT, "cmp `s0, `s1\n"+
		"movlt `d0, #1\n"+
		"movge `d0, #0", i, j, k );
}%

BINOP<l>(CMPLT, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit( ROOT, "cmp `s0h, `s1h\n"+
		"cmpeq `s0l, `s1l\n"+
		"movlt `d0, #1\n"+
		"movge `d0, #0", i, j, k );
}%

BINOP<f>(CMPLT, j, k) = i %{
    Temp i = makeTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit(new Instr(instrFactory, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___ltsf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n"+
		   "movne `d3, #0",
		   new Temp[]{ r0, r1, i },
		   new Temp[]{ j, k, r0 }));
}%

BINOP<d>(CMPLT, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    // don't move these into seperate Instrs; there's an implicit
    // dependency on the condition register so we don't want to risk
    // reordering them
    emit(new Instr(instrFactory, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___ltdf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n"+
		   "movne `d3, #0",
		   new Temp[]{ r0, r1, i },
		   new Temp[]{ j, k, r0 }));
}%

BINOP<p,i>(OR, j, k) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "orr `d0, `s0, `s1", i, j, k );
}%

BINOP<l>(OR, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "orr `d0l, `s0l, `s1l", i, j, k );
    emit( ROOT, "orr `d0h, `s0h, `s1h", i, j, k );
}%

BINOP<p,i>(SHL, j, k) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0 lsl `s1", i, j, k );
}%

BINOP<p,i>(SHR, j, k) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0 lsr `s1", i, j, k );
}%

BINOP<p,i>(USHR, j, k) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0 asr `s1", i, j, k );
}%

BINOP<p,i>(XOR, j, k) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "eor `d0, `s0, `s1", i, j, k );
}%

BINOP<l>(XOR, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "eor `d0l, `s0l, `s1l", i, j, k );
    emit( ROOT, "eor `d0h, `s0h, `s1h", i, j, k );
}%


CONST<f>(c) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Can't handle CONST of float type yet");
}%
CONST<d>(c) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Can't handle CONST of double type yet");
}%
CONST<l>(c) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Can't handle CONST of long type yet");
}% 

CONST<i>(c) = i %{
    Temp i = makeTemp();		
    int val = ((CONST)ROOT).value.intValue();
    boolean b0, b1, b2, b3;
    b0 = ((val & 0x000000FF) != 0);
    b1 = ((val & 0x0000FF00) != 0);
    b2 = ((val & 0x00FF0000) != 0);
    b3 = ((val & 0xFF000000) != 0);
    if (b0) { // start here
        emit(new Instr( instrFactory, ROOT, 
			"mov `d0, #"+(val & 0xFF000000), 
			new Temp[]{ i }, null));
	b0 = false;
    } else if (b1) {
        emit(new Instr( instrFactory, ROOT, 
			"mov `d0, #"+(val & 0x00FF0000), 
			new Temp[]{ i }, null));
	b1 = false;
    } else if (b2) {
        emit(new Instr( instrFactory, ROOT, 
			"mov `d0, #"+(val & 0x0000FF00), 
			new Temp[]{ i }, null));
	b2 = false;
    } else if (b3) {
        emit(new Instr( instrFactory, ROOT, 
			"mov `d0, #"+(val & 0x000000FF), 
			new Temp[]{ i }, null));
	b3 = false;
    } else {
        emit(new Instr( instrFactory, ROOT, 
			"mov `d0, #"+(val & 0x00000000), 
			new Temp[]{ i }, null));
    }

    if(b1) emit(new Instr( instrFactory, ROOT, 
			   "orr `d0, #"+(val & 0x00FF0000), 
			   new Temp[]{ i }, null));
    if(b2) emit(new Instr( instrFactory, ROOT, 
			   "orr `d0, #"+(val & 0x0000FF00), 
			   new Temp[]{ i }, null));
    if(b3) emit(new Instr( instrFactory, ROOT, 
			   "orr `d0, #"+(val & 0x000000FF), 
			   new Temp[]{ i }, null));
}%

/*
CONST<p>(c) = i %{
    // the only CONST of type Pointer we should see is NULL
    Temp i = makeTemp();
    int val = 0;
    emit(new Instr( instrFactory, ROOT, 
		    "mov `d0, #0", new Temp[]{ i }, null));
}%
*/

BINOP<p,i>(MUL, j, k) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mul `d0, `s0, `s1", i, j, k );
}%

BINOP<l>(MUL, j, k) = i %{
    // TODO: use the SMULL instruction instead	     
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "mov `d0, `s0l", r2, k );
    emit( ROOT, "mov `d0, `s0h", r3, k );
    emit( ROOT, "mov `d1, `s0l", r1, j );
    emit( ROOT, "mov `d0, `s0h", r0, j );
    emit( ROOT, "bl ___muldi3");
    emit( ROOT, "mov `d0l, `s0", i, r0 );
    emit( ROOT, "mov `d0h, `s0", i, r1 );
}%

BINOP<f>(MUL, j, k) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0", r1, k );
    emit( ROOT, "mov `d0, `s0", r0, j );
    emit( ROOT, "bl ___mulsf");
    emit( ROOT, "mov `d0, `s0", i, r0 );
}%

BINOP<d>(MUL, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "mov `d0, `s0l", r2, k );
    emit( ROOT, "mov `d0, `s0h", r3, k );
    emit( ROOT, "mov `d0, `s0l", r0, j );
    emit( ROOT, "mov `d0, `s0h", r1, j );
    emit( ROOT, "bl ___muldf3");
    emit( ROOT, "mov `d0l, `s0", i, r0 );
    emit( ROOT, "mov `d0h, `s0", i, r1 );
}%

BINOP<p,i>(DIV, j, k) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0", r1, k );
    emit( ROOT, "mov `d0, `s0", r0, j );
    emit( ROOT, "bl ___divsi3");
    emit( ROOT, "mov `d0, `s0", i, r0 );
}%

BINOP<l>(DIV, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "mov `d0, `s0l", r2, k );
    emit( ROOT, "mov `d0, `s0h", r3, k );
    emit( ROOT, "mov `d0, `s0l", r0, j );
    emit( ROOT, "mov `d0, `s0h", r1, j );
    emit( ROOT, "bl ___divdi3");
    emit( ROOT, "mov `d0l, `s0", i, r0 );
    emit( ROOT, "mov `d0h, `s0", i, r1 );
}%

BINOP<f>(DIV, j, k) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0", r1, k );
    emit( ROOT, "mov `d0, `s0", r0, j );
    emit( ROOT, "bl ___divsf3");
    emit( ROOT, "mov `d0, `s0", i, r0 );
}%

BINOP<d>(DIV, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "mov `d0, `s0l", r2, k );
    emit( ROOT, "mov `d0, `s0h", r3, k );
    emit( ROOT, "mov `d0, `s0l", r0, j );
    emit( ROOT, "mov `d0, `s0h", r1, j );
    emit( ROOT, "bl ___divdf3");
    emit( ROOT, "mov `d0l, `s0", i, r0 );
    emit( ROOT, "mov `d0h, `s0", i, r1 );
}%

BINOP<p,i>(REM, j, k) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d1, `s1", r1, k );
    emit( ROOT, "mov `d0, `s0", r0, j );
    emit( ROOT, "bl ___modsi3");
    emit( ROOT, "mov `d2, `s2", i, r0 );
}%

BINOP<l>(REM, j, k) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "mov `d2, `s1l", r2, k );
    emit( ROOT, "mov `d3, `s1h", r3, k );
    emit( ROOT, "mov `d0, `s0l", r0, j );
    emit( ROOT, "mov `d1, `s0h", r1, j );
    emit( ROOT, "bl ___moddi3");
    emit( ROOT, "mov `d4l, `s2", i, r0 );
    emit( ROOT, "mov `d4h, `s3", i, r1 );
}%

MEM<p,i,f>(e) = i %{
    Temp i = makeTemp();		
    emit(new InstrMEM(instrFactory, ROOT,
	             "ldr `d0, [`s0]",
		     new Temp[]{ i }, new Temp[]{ e }));
}%
MEM<l,d>(e) = i %{
    Temp i = makeTwoWordTemp();		
    emit(new InstrMEM(instrFactory, ROOT,
	             "ldr `d0l, [`s0]",
		     new Temp[]{ i }, new Temp[]{ e }));
    emit(new InstrMEM(instrFactory, ROOT,
	             "ldr `d0h, [`s0, #4]",
		     new Temp[]{ i }, new Temp[]{ e }));
}%


NAME(id) = i %{
    // produces a pointer
    Temp i = makeTemp();		
    emit(new Instr( instrFactory, ROOT,
		    "mov `d0, #" + id, 
		    new Temp[]{ i }, null ));
}%

MEM<f,i,p>(NAME(id)) = i %{
    Temp i = makeTemp();		
    emit(new Instr( instrFactory, ROOT,
		    "ldr `d0, " + id, 
		    new Temp[]{ i }, null ));
}%
MEM<d,l>(NAME(id)) = i %{
    Temp i = makeTwoWordTemp();		
    emit(new Instr( instrFactory, ROOT,
		    "ldr `d0l, " + id, 
		    new Temp[]{ i }, null ));
    emit(new Instr( instrFactory, ROOT,
		    "ldr `d0h, " + id + "+4", 
		    new Temp[]{ i }, null ));
}%


TEMP<p,i,f>(id) = i %{
    Temp i = makeTemp();
    if (((TEMP)ROOT) != param0) {
	emit( ROOT, "mov `d0, `s0", i, ((TEMP)ROOT).temp);
    } else {
	emit( ROOT, "bl _lookup\n"+
		    "mov `d0, `s0", i, r2 );
    }

}%
TEMP<l,d>(id) = i %{
    // Will need to modify these to do something like mapping from
    // TEMP's Temp to the necessary TwoWordTemp
    TwoWordTemp i = makeTwoWordTemp();		
    // Temp i = ((TEMP)ROOT).temp;
    

}%

UNOP<l>(_2B, arg) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Spec file doesn't handle long-to-byte conversion directly");
}%
UNOP<f>(_2B, arg) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Spec file doesn't handle float-to-byte conversion directly");
}%
UNOP<d>(_2B, arg) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Spec file doesn't handle double-to-byte conversion directly");
}%
UNOP<l>(_2C, arg) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Spec file doesn't handle long-to-char conversion directly");
}%
UNOP<f>(_2C, arg) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Spec file doesn't handle float-to-char conversion directly");
}%
UNOP<d>(_2C, arg) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Spec file doesn't handle double-to-char conversion directly");
}%
UNOP<l>(_2S, arg) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Spec file doesn't handle long-to-short conversion directly");
}%
UNOP<f>(_2S, arg) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Spec file doesn't handle float-to-short conversion directly");
}%
UNOP<d>(_2S, arg) = i %{
    Temp i = makeTemp();		
    Util.assert(false, "Spec file doesn't handle double-to-short conversion directly");
}%


UNOP<p,i>(_2B, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "and `d0, `s0, #0x000000ff", i, arg );
    // there may be cleverer ways to do this so that we don't have to
    // do three orrs
    emit( ROOT, "tst `s0, #0x00000080\n"+
		"orrne `d0, `s0, #0xff000000\n"+
		"orrne `d0, `s0, #0x00ff0000\n"+
		"orrne `d0, `s0, #0x0000ff00", i, i );
}%
UNOP<p,i>(_2C, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "and `d0, `s0, #0x000000ff", i, arg );
    emit( ROOT, "and `d0, `s0, #0x0000ff00", i, arg );
}%
UNOP<p,i>(_2S, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "and `d0, `s0, #0x000000ff", i, arg );
    emit( ROOT, "and `d0, `s0, #0x0000ff00", i, arg );
    emit( ROOT, "tst `s0, #0x00008000\n"+
		"orrne `d0, `s0, #0xff000000\n"+
		"orrne `d0, `s0, #0x00ff0000", i, i );
}%


UNOP<l>(_2D, arg) = i %{
    TwoWordTemp i = makeTwoWordTemp();
    emit( ROOT, "mov `d0, `s0l", r0, arg );
    emit( ROOT, "mov `d0, `s0h", r1, arg );
    emit( ROOT, "bl ___floatdidf" );
    emit( ROOT, "mov `d0l, `s0", i, r0 );
    emit( ROOT, "mov `d0h, `s0", i, r1 );
}%
UNOP<p,i>(_2D, arg) = i %{
    TwoWordTemp i = makeTwoWordTemp();		
    emit( ROOT, "mov `d0, `s0", r0, arg );
    emit( ROOT, "bl ___floatsidf" );
    emit( ROOT, "mov `d0l, `s0", i, r0 );
    emit( ROOT, "mov `d0h, `s0", i, r1 );
}%
UNOP<f>(_2D, arg) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "mov `d0, `s0", r0, arg );
    emit( ROOT, "bl ___extendsfdf2" );
    emit( ROOT, "mov `d0l, `s0", i, r0 );
    emit( ROOT, "mov `d0h, `s0", i, r1 );

}%
UNOP<d>(_2D, arg) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "mov `d0l, `s0l", i, arg );
    emit( ROOT, "mov `d0h, `s0h", i, arg );
}%

UNOP<l>(_2F, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0l", r0, arg );
    emit( ROOT, "mov `d0, `s0h", r1, arg );
    emit( ROOT, "bl ___floatdisf" );
    emit( ROOT, "mov `d0, `s0", i, r0 );
}%
UNOP<p,i>(_2F, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0", r0, arg );
    emit( ROOT, "bl ___floatsisf" );   
    emit( ROOT, "mov `d0, `s0", i, r0 );
}%
UNOP<f>(_2F, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0", i, arg );
}%
UNOP<d>(_2F, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0l", r0, arg );
    emit( ROOT, "mov `d0, `s0h", r1, arg );
    emit( ROOT, "bl ___truncdfsf2" );
    emit( ROOT, "mov `d0, `s0", i, r0 );
}%

UNOP<l>(_2I, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0l", i, arg );
}%
UNOP<p,i>(_2I, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0", i, arg );
}%
UNOP<f>(_2I, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0", r0, arg );
    emit( ROOT, "bl ___fixsfsi" );
    emit( ROOT, "mov `d0, `s0", i, r0 );
}%
UNOP<d>(_2I, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mov `d0, `s0l", r0, arg );
    emit( ROOT, "mov `d0, `s0h", r1, arg );
    emit( ROOT, "bl ___fixdfsi" );
    emit( ROOT, "mov `d0, `s0", i, r0 );
}%

UNOP<l>(_2L, arg) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "mov `d0l, `s0l", i, arg );
    emit( ROOT, "mov `d0h, `s0h", i, arg );
}%
UNOP<p,i>(_2L, arg) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "mov `d0l, `s0", i, arg );
    emit( ROOT, "mov `d0h, `s0l asr #31", i, i );
}%
UNOP<f>(_2L, arg) = i %{
    Temp i = makeTwoWordTemp();	
    emit( ROOT, "mov `d0, `s0", r0, arg );
    emit( ROOT, "bl ___fixsfdi" );
    emit( ROOT, "mov `d0l, `s0", i, r0 );
    emit( ROOT, "mov `d0h, `s0", i, r1 );
}%
UNOP<d>(_2L, arg) = i %{
    Temp i = makeTwoWordTemp();		
    emit( ROOT, "mov `d0, `s0l", r0, arg );
    emit( ROOT, "mov `d0, `s0h", r1, arg );
    emit( ROOT, "bl ___fixdfdi" );
    emit( ROOT, "mov `d0l, `s0", i, r0 );
    emit( ROOT, "mov `d0h, `s0", i, r1 );	 
}%


UNOP<p,i>(NEG, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "rsb `d0, `s0, #0", i, arg );
}% 

UNOP<p,i>(NOT, arg) = i %{
    Temp i = makeTemp();		
    emit( ROOT, "mvn `d0, `s0", i, arg );
}% 

/* STATEMENTS */
METHOD(params) %{

    param0 = params[0];

}%

CJUMP(test, iftrue, iffalse) %{
    Instr i = new Instr(instrFactory, ROOT, 
			"cmp `s0, #0 \n" +
			"beq " + iffalse + "\n" +
			"b " + iftrue,
			null, new Temp[]{ test });
    emit(i);
    blMap.put(i, iffalse);
    blMap.put(i, iftrue);
}%

EXP(e) %{
			/* this is a statement that's just an
			   expression; just throw away 
			   calculated value */
}%

JUMP(e) %{
    Instr i = new Instr(instrFactory, ROOT,"b `s0", null, new Temp[]{e});
    emit(i);
    LabelList targets = ((JUMP)ROOT).targets;
    while(targets!=null) {
	blMap.put(i, targets.head);
	targets = targets.tail;
    }
}%

LABEL(id) %{
    LABEL l = (LABEL) ROOT;
    Instr i = new InstrLABEL(instrFactory, l, l.label + ":", l.label);
    emit(i);
    liMap.put(l.label, i);
}%

MOVE<p,i,f>(dst, src) %{
    emitMOVE( ROOT, "mov `d0, `s0", dst, src );
}%

MOVE<d,l>(dst, src) %{
    Util.assert( dst instanceof TwoWordTemp, "why is dst: "+dst + 
		 " a normal Temp? " + harpoon.IR.Tree.Print.print(ROOT));

    Util.assert(src instanceof TwoWordTemp, "why is src: "+src + " a normal Temp?");
        // not certain an emitMOVE is legal with the l/h modifiers
    emitMOVE( ROOT, "mov `d0l, `s0l\n"+
		    "mov `d0h, `s0h", dst, src );
}%

MOVE<i>(dst, CONST<i>(s)) %{
    // TODO: this needs to be fixed, because StrongARM can't load more
    // than a byte of information at a time...
    emit(new Instr(instrFactory, ROOT,
		   "mov `d0, #"+((CONST)((MOVE)ROOT).src).value.intValue(),
		   new Temp[] { dst }, null));
}%

MOVE<p>(dst, CONST<p>(s)) %{ 
    // we should only see CONST of type pointer when the value is NULL
    emit(new Instr(instrFactory, ROOT,
		   "mov `d0, #0",
		   new Temp[] { dst }, null));
}%

/* // FSK: I don't want to code these now (will probably need some for
   // MOVE<d,f,l>(MEM(d), s) as well...)

MOVE<f>(dst, CONST(s)) %{

}%

MOVE<d,l>(dst, CONST(s)) %{

}%
*/

MOVE<i>(MEM(d), src) %{
    emit(new InstrMEM(instrFactory, ROOT,
		      "str `s0, [`s1]",
		      null, new Temp[]{ src, d }));   
}%

RETURN(val) %{
    // FSK: leaving OUT exception handling by passing excep-val in r1
    emit(new InstrMEM(instrFactory, ROOT, 
			   "mov `d0, `s0\n"+
			   "ldmea `s1, { `d1, `d2, `d3 }",
			   new Temp[]{ r0,
				       SAFrame.FP, SAFrame.SP, 
				       SAFrame.PC },
			   new Temp[]{ val,  SAFrame.FP }));
			
}%


THROW(val, handler) %{
    emitMOVE( ROOT, "mov `d0, `s0", r0, val );
    emit( ROOT, "bl _lookup ; only r0, lr (& ip?) "+
		"need to be preserved during lookup" ); 
    emit( ROOT, "jmp stdexit");
    
   
}%


CALL(retval, NAME(retex), func, arglist) %{
    ExpList list = arglist;
    
    // I assume that the elements of 'arglist' are all Temps after
    // canonicalization.

    int stackOffset = 0;

    for (int index=0; list != null; index++) { 
	TEMP tempExp = (TEMP) list.head;
	if (tempExp.isDoubleWord()) {
	   // arg takes up two words
	   switch(index) {
	   case 0: case 1: case 2: // put in registers 
        // not certain an emitMOVE is legal with the l/h modifiers
	      emitMOVE( ROOT, "mov `d0, `s0l",
		    frame.getAllRegisters()[index] ,
		    tempExp.temp );
	      index++;			     
        // not certain an emitMOVE is legal with the l/h modifiers
	      emitMOVE( ROOT, "mov `d0, `s0h",
		    frame.getAllRegisters()[index],
		    tempExp.temp );
	      break;			     
	   case 3: // spread between regs and stack
        // not certain an emitMOVE is legal with the l/h modifiers
	     emitMOVE( ROOT, "mov `d0, `s0l",
			      frame.getAllRegisters()[index],
			      tempExp.temp );
	     index++;
	     stackOffset += 4;
	     emit(new InstrMEM( instrFactory, ROOT,
		      "str `s0h, [`s1, #-4]!",
		      new Temp[]{ SAFrame.SP }, // SP *implicitly* modified
		      new Temp[]{ tempExp.temp, SAFrame.SP })); 
	     break;
	   default: // start putting args in memory
	     emit(new InstrMEM( instrFactory, ROOT,
				"str `s0l, [`s1, #-4]!", 
				null, 
			     new Temp[]{ SAFrame.SP, tempExp.temp }));
	     index++;
	     stackOffset += 4;
	     emit(new InstrMEM( instrFactory, ROOT,
		      "str `s0h, [`s1, #-4]!",
		      new Temp[]{ SAFrame.SP }, // SP *implicitly* modified
		      new Temp[]{ tempExp.temp, SAFrame.SP })); 
	     stackOffset += 4;
	     break;
	   }
	} else {
	  // arg is one word
	  if (index < 4) {
	     emitMOVE( ROOT, "mov `d0, `s0", 
		   frame.getAllRegisters()[index], tempExp.temp);
	  } else {
	     emit(new InstrMEM(
		      instrFactory, ROOT,
		      "str `s0, [`s1, #-4]!",
		      new Temp[]{ SAFrame.SP }, // SP *implicitly* modified
		      new Temp[]{ tempExp.temp, SAFrame.SP }));
	     stackOffset += 4;
	  }
	}	     
	list = list.tail;    	
    }

    // this '1f' and '1:' business is taking advantage of a GNU
    // Assembly feature to avoid polluting the global name space with
    // local labels
    emit( ROOT, "bl " + func );
    emitDIRECTIVE( ROOT, ".section fixup");
    emitDIRECTIVE( ROOT, "\t.word 1f, "+retex+"; (retaddr, handler)");
    emitDIRECTIVE( ROOT, ".section code");
    emitLABEL( ROOT, "1:", new Label("1")); 
    

    // this will break if stackOffset > 255 (ie >63 args)
    Util.assert( stackOffset < 256, 
		 "Update the spec file to handle large SP offsets");
    emit( ROOT, "add `d0, `s0, #" + stackOffset, SAFrame.SP , SAFrame.SP );
    if (((INVOCATION) ROOT).retval.isDoubleWord()) {
        // not certain an emitMOVE is legal with the l/h modifiers
        emitMOVE( ROOT, "mov `d0l, `s0", retval, r0 );
        emitMOVE( ROOT, "mov `d0h, `s0", retval, r1 );
    } else {
        emitMOVE( ROOT, "mov `d0, `s0", retval, r0 );
    }  
}%

NATIVECALL(retval, func, arglist) %{
    ExpList list = arglist;
    
    // I assume that the elements of 'arglist' are all Temps after
    // canonicalization.

    int stackOffset = 0;

    for (int index=0; list != null; index++) { 
	TEMP tempExp = (TEMP) list.head;
	if (tempExp.isDoubleWord()) {
	   // arg takes up two words
	   switch(index) {
	   case 0: case 1: case 2: // put in registers 
        // not certain an emitMOVE is legal with the l/h modifiers
	      emitMOVE( ROOT, "mov `d0, `s0l",
		    frame.getAllRegisters()[index] ,
		    tempExp.temp );
	      index++;			     
	      emitMOVE( ROOT, "mov `d0, `s0h",
		    frame.getAllRegisters()[index],
		    tempExp.temp );
	      break;			     
	   case 3: // spread between regs and stack
        // not certain an emitMOVE is legal with the l/h modifiers
	     emitMOVE( ROOT, "mov `d0, `s0l",
		       frame.getAllRegisters()[index],
		       tempExp.temp );
	     index++;
	     stackOffset += 4;
	     emit(new InstrMEM( instrFactory, ROOT,
		      "str `s0h, [`s1, #-4]!",
		      new Temp[]{ SAFrame.SP }, // SP *implicitly* modified
		      new Temp[]{ tempExp.temp, SAFrame.SP })); 
	     break;
	   default: // start putting args in memory
	     emit(new InstrMEM( instrFactory, ROOT,
				"str `s0l, [`s1, #-4]!", 
				null, 
			     new Temp[]{ SAFrame.SP, tempExp.temp }));
	     index++;
	     stackOffset += 4;
	     emit(new InstrMEM( instrFactory, ROOT,
		      "str `s0h, [`s1, #-4]!",
		      new Temp[]{ SAFrame.SP }, // SP *implicitly* modified
		      new Temp[]{ tempExp.temp, SAFrame.SP })); 
	     stackOffset += 4;
	     break;
	   }
	} else {
	  // arg is one word
	  if (index < 4) {
	     emit( ROOT, "mov `d0, `s0", 
		   frame.getAllRegisters()[index], tempExp.temp);
	  } else {
	     emit(new InstrMEM(
		      instrFactory, ROOT,
		      "str `s0, [`s1, #-4]!",
		      new Temp[]{ SAFrame.SP }, // SP *implicitly* modified
		      new Temp[]{ tempExp.temp, SAFrame.SP }));
	     stackOffset += 4;
	  }
	}
	list = list.tail;    	
    }


    // this '1f' and '1:' business is taking advantage of a GNU
    // Assembly feature to avoid polluting the global name space with
    // local labels
    emit( ROOT, "bl " + func );

    // this will break if stackOffset > 255 (ie >63 args)
    emit( ROOT, "add `d0, `s0, #" + stackOffset );
    if (((INVOCATION) ROOT).retval.isDoubleWord()) {
        // not certain an emitMOVE is legal with the l/h modifiers
        emitMOVE( ROOT, "mov `d0l, `s0", retval, r0 );
        emitMOVE( ROOT, "mov `d0h, `s0", retval, r1 );
    } else {
        emitMOVE( ROOT, "mov `d0, `s0", retval, r0 );
    }  
}%

