// CodeGen.spec, created Tue Jul 6 12:12:41 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.Uop;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.Typed;
import harpoon.Backend.Generic.DefaultFrame;
import harpoon.Backend.Generic.Code;
import harpoon.Util.Util;
import harpoon.Temp.Temp;

/**
 * <code>CodeGen</code> is a code-generator for the ARM architecture.
 * 
 * @see Jaggar, <U>ARM Architecture Reference Manual</U>
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CodeGen.spec,v 1.1.2.4 1999-07-22 18:18:45 pnkfelix Exp $
 */
%%


    // FIELDS
    // last = null OR last instr passed to emit(Instr)
    private Instr last; 
    
    // InstrFactory to generate Instrs from
    private InstrFactory inf;
    
    // Frame for instructions to access to get platform specific variables (Register Temps, etc) 
    private SAFrame frame;

    private void emit(Instr i) {
	if (last == null) {
	    last = i;
	} else {
	    last.insertInstrAfter(last, i);
	    last = i;
	}
    }

    

    public CodeGen(SACode code) {
	last = null;
	this.frame = (SAFrame) code.getFrame();
	inf = code.getInstrFactory();
    }
    
%%
    /* this comment will be eaten by the .spec processor (unlike comments above) */
	
/* EXPRESSIONS */ 
BINOP<p,i>(ADD, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "add `d0, `s0, `s1",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<l>(ADD, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "adds `d0l, `s0l, `s1l\n"+
		   "adc  `d0h, `s0h, `s1h",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<f>(ADD, j, k) = i %{
    /* call auxillary fp routines */
    emit(new Instr(inf, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___addsf\n"+
		   "mov `d2, `s2",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, i })); 	       		       
			       
}%

BINOP<d>(ADD, j, k) = i %{
    /* call auxillary fp routines */
    emit(new Instr(inf, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___adddf3\n"+
		   "mov `d4l, `s2\n"+
		   "mov `d4h, `s3",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1],
			       frame.getAllRegisters()[2],
			       frame.getAllRegisters()[3], i },
		   new Temp[]{ j, k, 
			       frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1] }));
			       
}%

BINOP<p,i>(AND, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "and `d0, `s0, `s1",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<l>(AND, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "and `d0l, `s0l, `s1l\n"+
		   "and `d0h, `s0h, `s1h",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<p,i>(CMPEQ, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "cmp `s0, `s1\n"+
		   "moveq `d0, #1\n"+
		   "movne `d0, #0",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<l>(CMPEQ, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "cmp `s0l, `s1l\n"+
		   "cmpeq `s0h, `s1h\n"+
		   "moveq `d0, #1\n"+
		   "movne `d0, #0",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%
  
BINOP<f>(CMPEQ, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___eqsf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n+
		   "movne `d3, #0",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%

BINOP<d>(CMPEQ, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___eqdf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n+
		   "movne `d3, #0",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%

BINOP<p,i>(CMPGT, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "cmp `s0, `s1\n"+	
		   "movgt `d0, #1\n"+	
		   "movne `d0, #0",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<l>(CMPGT, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "cmp `s0h, `s1h\n"+
	           "cmpeq `s0l, `s1l\n"+
		   "movgt `d0, #1\n"+
		   "movle `d0, #0",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%
BINOP<f>(CMPGT, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___gtsf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n+
		   "movne `d3, #0",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%

BINOP<d>(CMPGT, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___gtdf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n+
		   "movne `d3, #0",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%


BINOP<p,i>(CMPGE, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "cmp `s0, `s1\n"+
		   "movge `d0, #1\n"+
		   "movlt `d0, #0",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<l>(CMPGE, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "cmp `s0h, `s1h\n"+
	           "cmpeq `s0l, `s1l\n"+
		   "movge `d0, #1\n"+
		   "movlt `d0, #0",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<f>(CMPGE, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___gesf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n+
		   "movne `d3, #0",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%

BINOP<d>(CMPGE, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___gedf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n+
		   "movne `d3, #0",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%


BINOP<p,i>(CMPLE, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "cmp `s0, `s1\n"+
		   "movle `d0, #1\n"+
		   "movgt `d0, #0",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<l>(CMPLE, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "cmp `s0h, `s1h\n"+
	           "cmpeq `s0l, `s1l\n"+
		   "movle `d0, #1\n"+
		   "movgt `d0, #0",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<f>(CMPLE, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___lesf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n+
		   "movne `d3, #0",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%

BINOP<d>(CMPLE, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___ledf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n+
		   "movne `d3, #0",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%

BINOP<p,i>(CMPLT, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "cmp `s0, `s1\n"+
		   "movlt `d0, #1\n"+
		   "movge `d0, #0",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<l>(CMPLT, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "cmp `s0h, `s1h\n"+
	           "cmpeq `s0l, `s1l\n"+
		   "movlt `d0, #1\n"+
		   "movge `d0, #0",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<f>(CMPLT, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___ltsf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n+
		   "movne `d3, #0",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%

BINOP<d>(CMPLT, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___ltdf2\n"+
		   "cmp `s2, #0\n"+
		   "moveq `d3, #1\n+
		   "movne `d3, #0",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%

BINOP(OR, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "orr `d0, `s0, `s1",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<p,i>(SHL, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "mov `d0, `s0 lsl `s1",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<p,i>(SHR, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "mov `d0, `s0 lsr `s1",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<p,i>(USHR, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "mov `d0, `s0 asr `s1",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP<p,i>(XOR, j, k) = i %{
    emit(new Instr(inf, ROOT,
	           "eor `d0, `s0, `s1",
		   new Temp[]{ i }, new Temp[]{ j, k }));
}%

CONST<i>(c) = i %{
    emit(new Instr(inf, ROOT,
	           "mov `d0, #"+((CONST)ROOT).value.intValue(),
		   new Temp[]{ i }, null));
}%

BINOP<p,i>(MUL, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mul `d0, `s0, `s1",
		   new Temp[] { i }, new Temp[] { j , k }));	   
}%

BINOP<l>(MUL, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d1, `s0l\n"+
		   "mov `d0, `s0h\n"+
		   "bl ___muldi3\n"+
		   "mov `d4l, `s2\n"+
		   "mov `d4h, `s3",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1],
			       frame.getAllRegisters()[2],
			       frame.getAllRegisters()[3], i },
		   new Temp[]{ j, k, 
			       frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1] }));
}%

BINOP<f>(MUL, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___mulsf\n"+
		   "mov `d2, `s2",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, i })); 	       		       
}%

BINOP<d>(MUL, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___muldf3\n"+
		   "mov `d4l, `s2\n"+
		   "mov `d4h, `s3",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1],
			       frame.getAllRegisters()[2],
			       frame.getAllRegisters()[3], i },
		   new Temp[]{ j, k, 
			       frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1] }));
}%

BINOP<p,i>(DIV, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___divsi3\n"+
		   "mov `d2, `s2",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%

BINOP<l>(DIV, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___divdi3\n"+
		   "mov `d4l, `s2\n"+
		   "mov `d4h, `s3",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1],
			       frame.getAllRegisters()[2],
			       frame.getAllRegisters()[3], i },
		   new Temp[]{ j, k, 
			       frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1] }));
}%

BINOP<f>(DIV, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___divsf3\n"+
		   "mov `d2, `s2",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
}%

BINOP<d>(DIV, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___divdf3\n"+
		   "mov `d4l, `s2\n"+
		   "mov `d4h, `s3",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1],
			       frame.getAllRegisters()[2],
			       frame.getAllRegisters()[3], i },
		   new Temp[]{ j, k, 
			       frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1] }));
}%

BINOP<p,i>(REM, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d1, `s1\n"+
		   "mov `d0, `s0\n"+
		   "bl ___modsi3\n"+
		   "mov `d2, `s2",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1], i },
		   new Temp[]{ j, k, frame.getAllRegisters()[0] }));
   
}%

BINOP<l>(REM, j, k) = i %{
    emit(new Instr(inf, ROOT,
		   "mov `d2, `s1l\n"+
		   "mov `d3, `s1h\n"+
		   "mov `d0, `s0l\n"+
		   "mov `d1, `s0h\n"+
		   "bl ___moddi3\n"+
		   "mov `d4l, `s2\n"+
		   "mov `d4h, `s3",
		   new Temp[]{ frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1],
			       frame.getAllRegisters()[2],
			       frame.getAllRegisters()[3], i },
		   new Temp[]{ j, k, 
			       frame.getAllRegisters()[0],
			       frame.getAllRegisters()[1] }));
}%

MEM<p,i,f>(e) = i %{
    emit(new InstrMEM(inf, ROOT,
	           "ldr `d0, `s0",
		   new Temp[]{ i }, new Temp[]{ e }));
}%

NAME(id) = i %{

}%

TEMP(id) = i %{

}%

UNOP(_2B, arg) = i %{

}%

UNOP(_2C, arg) = i %{

}%

UNOP(_2D, arg) = i %{

}%

UNOP(_2F, arg) = i %{

}%

UNOP(_2I, arg) = i %{

}%

UNOP(_2L, arg) = i %{

}%

UNOP(_2S, arg) = i %{

}%

UNOP<p,i>(NEG, arg) = i %{
    emit(new Instr(inf, ROOT,
	           "mov `d0, #0\n"+
		   "sub `d0, `d0, `s0",
		   new Temp[] { i }, new Temp[]{ arg }));
}% 

UNOP<p,i>(NOT, arg) = i %{
    emit(new Instr(inf, ROOT, 
	           "mvn `d0, `s0",
		   new Temp[]{ i }, new Temp[]{ arg }));
}% 

/* STATEMENTS */
CJUMP(test, iftrue, iffalse) %{
    Instr i = new Instr(inf, ROOT, 
			"cmp `s0, #0 \n" +
			"beq " + iffalse + "\n" +
			"b " + iftrue,
			null, new Temp[]{ test });
    emit(i);
    blMap.put(i, s.iffalse);
    blMap.put(i, s.iftrue);
}%

EXP(e) %{
			/* this is a statement that's just an
			   expression; just throw away 
			   calculated value */
}%

JUMP(e) %{
    Instr i = new Instr(inf, ROOT,"b `s0", null, new Temp[]{e});
    emit(i);
    LabelList targets = ((JUMP)ROOT).targets;
    while(targets!=null) {
	blMap.put(i, targets.head);
	targets = targets.tail;
    }
}%

LABEL(id) %{
    LABEL l = (LABEL) ROOT;
    Instr i = new InstrLABEL(inf, l, l.label + ":", l.label);
    emit(i);
    liMap.pit(l.label, i);
}%

MOVE<p,i,f>(dst, src) %{
    emit(new Instr(inf, ROOT, 
		   "mov `d0, `s0",
		   new Temp[] { dst },
		   new Temp[] { src }));
}%

MOVE<d,l>(dst, src) %{
    emit(new Instr(inf, ROOT, 
		   "mov `d0l, `s0l\n"+
		   "mov `d0h, `s0h",
		   new Temp[] { dst },
		   new Temp[] { src }));
		  
}%

MOVE<i>(dst, CONST(s)) %{
    emit(new Instr(inf, ROOT,
		   "mov `d0, #"+((CONST)((MOVE)ROOT).src).value.intValue(),
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
    emit(new InstrMEM(inf, ROOT,
		      "str `s0, [`s1]",
		      null, new Temp[]{ src, d }));   
}%

MOVE<i>(MEM(d), CONST(s)) %extra { t } %{
    emit(new InstrMEM(inf, ROOT,
		      "mov `d0, #"+
		      ((CONST)((MOVE)ROOT).src).value.intValue()+"\n"+
		      "str `s0, [`s1]",
		      new Temp[] { t }, new Temp[] { t, d }));
}%

RETURN(val) %{
    // FSK: leaving OUT exception handling by passing excep-val in r1

    emit(new InstrMEM(inf, s, 
			   "mov `d0, `s0\n"+
			   "ldmea `s1, { `d1, `d2, `d3 }",
			   new Temp[]{ frame.getAllRegisters()[0],
				       SAFrame.FP, SAFrame.SP, 
				       SAFrame.PC },
			   new Temp[]{ val,  SAFrame.FP }));
			
}%

/* // FSK: leaving OUT exception handling 
THROW(val) %{

}%
*/

CALL(retval, retex, func, arglist) %{

}%

NATIVECALL(retval, retex, func, arglist) %{

}%

