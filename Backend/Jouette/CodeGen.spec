// Jouette.spec, created Tue Jun 29 15:52:41 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Jouette;

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
import harpoon.Backend.Generic.Code;
import harpoon.Util.Util;
import harpoon.Temp.Temp;

// normally a Code and Frame for a specific architecture would be in
// its own class accesible to other components of the system, but this
// is just an example illustrating how to write a spec file, not a
// whole backend.  These are just stub-classes to let the "sensible"
// Instruction Patterns below be converted into compiliable Java
// source code.
class JouetteCode extends Code {
    JouetteCode(HMethod m, Instr instrs, JouetteFrame frame) {
	super(m, instrs, frame);
    }
    public HCode clone(HMethod hm) 
        throws CloneNotSupportedException {
	throw new CloneNotSupportedException
	    ("why are you trying to do something useful with JouetteCode");
    }
    public String getName() { return "jouette"; }
}
class JouetteFrame extends harpoon.Backend.Generic.Frame {
    public Temp regZero() { return getRegister(0); }
}
// end of stub classes.

/**
 * <code>Jouette.CodeGen</code> is a code-generator for the Jouette architecture.
 * 
 * @see Appel, <U>Modern Compiler Implementation in Java</U>, pg. 196, 213-214 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CodeGen.spec,v 1.2 2002-02-25 21:01:36 cananian Exp $
 */
%%


    // FIELDS
    // last = null OR last instr passed to emit(Instr)
    private Instr last; 
    
    // InstrFactory to generate Instrs from
    private InstrFactory inf;
    
    // Frame for instructions to access to get platform specific variables (Register Temps, etc) 
    private JouetteFrame frame;

    private void emit(Instr i) {
	if (last == null) {
	    last = i;
	} else {
	    last.insertInstrAfter(last, i);
	    last = i;
	}
    }

    

    public CodeGen(JouetteCode code) {
	last = null;
	this.frame = (JouetteFrame) code.getFrame();
	inf = code.getInstrFactory();
    }
    
%%
    /* this comment will be eaten by the .spec processor (unlike comments above) */
	
BINOP(ADD, j, k) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new Instr(inf, ROOT,
				 "ADD `d0 <- `s0 + `s1\n", 
				 new Temp[]{ i }, new Temp[]{ j, k }));
}%
	
BINOP(MUL, j, k) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new Instr(inf, ROOT,
				 "MUL `d0 <- `s0 * `s1\n",
				 new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP(SUB, j, k) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new Instr(inf, ROOT,
				 "SUB `d0 <- `s0 - `s1\n",
				 new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP(DIV, j, k) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new Instr(inf, ROOT,
				 "SUB `d0 <- `s0 - `s1\n",
				 new Temp[]{ i }, new Temp[]{ j, k }));
}%

BINOP(ADD, j, CONST(c)) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new Instr(inf, ROOT,
				 "ADDI `d0 <- `s0 + " + c + "\n",
				 new Temp[]{ i }, new Temp[]{ j }));
}%

    // need to manually represent symmetry by repeating patterns
BINOP(ADD, CONST(c), j) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new Instr(inf, ROOT,
				 "ADDI `d0 <- `s0 + " + c + "\n",
				 new Temp[]{ i }, new Temp[]{ j }));
}%

CONST(c) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new Instr(inf, ROOT,
				 "ADDI `d0 <- `s0 + " + c + "\n",
				 new Temp[]{ i }, new Temp[]{ frame.regZero() }));
}%

BINOP(SUB, j, CONST(c)) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new Instr(inf, ROOT,
				 "SUBI `d0 <- `s0 - " + c + "\n",
				 new Temp[]{ i }, new Temp[]{ j }));
}%

MEM(BINOP(ADD, j, CONST(c))) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new InstrMEM(inf, ROOT,
				 "LOAD `d0 <- M[`s0 + " + c + "]\n",
				 new Temp[] { i }, new Temp[]{ j }));
}%

MEM(BINOP(ADD, CONST(c), j)) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new InstrMEM(inf, ROOT,
				 "LOAD `d0 <- M[`s0 + " + c + "]\n",
				 new Temp[] { i }, new Temp[]{ j }));
}%

MEM(CONST(c)) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new InstrMEM(inf, ROOT,
				 "LOAD `d0 <- M[`s0 + " + c + "]\n",
				 new Temp[] { i }, new Temp[]{ frame.regZero() }));
}%

MEM(j) = i %{
			Temp i = new Temp(frame.tempFactory());
			emit(new InstrMEM(inf, ROOT,
				 "LOAD `d0 <- M[`s0 + 0]\n",
				 new Temp[] { i }, new Temp[]{ j }));
}%

MOVE(MEM(BINOP(ADD, j, CONST(c))), src) %{
		        Temp i = new Temp(frame.tempFactory());
			emit(new InstrMEM(inf, ROOT,
				 "STORE M[`s0 + " + c + "] <- `s1\n",
				 new Temp[] { }, new Temp[] { j, src }));
}%

MOVE(MEM(BINOP(ADD, CONST(c), j)), src) %{
			Temp i = new Temp(frame.tempFactory());
			emit(new InstrMEM(inf, ROOT,
				 "STORE M[`s0 + " + c + "] <- `s1\n",
				 new Temp[] { }, new Temp[] { j, src }));
}%

MOVE(MEM(CONST(c)), src) %{
			 Temp i = new Temp(frame.tempFactory());
			 emit(new InstrMEM(inf, ROOT,
				  "STORE M[`s0 + " + c + "] <- `s1\n",
				  new Temp[] { }, new Temp[] { frame.regZero(), src }));
}%

MOVE(MEM(target), src) %{
			 Temp i = new Temp(frame.tempFactory());
			 emit(new InstrMEM(inf, ROOT,
				  "STORE M[`s0 + 0] <- `s1\n",
				  new Temp[] { }, new Temp[] { target, src }));
}%

MOVE(MEM(target), MEM(src)) %{
			 Temp i = new Temp(frame.tempFactory());
			 emit(new Instr(inf, ROOT,
				  "MOVE M[`s0] <- M[`s1]\n",
				  new Temp[] {}, new Temp[] { target, src }));
}%

EXPR(e1) %{
			/* this is a statement that's just an
			   expression; just throw away 
			   calculated value */
}%
