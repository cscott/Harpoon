// CodeGen.spec, created Mon Jun 28 23:00:48 1999 by cananian -*- Java -*-
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Sparc;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
/**
 * <code>CodeGen</code> is a code-generator for the Sparc architecture.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CodeGen.spec,v 1.1.2.1 1999-06-29 03:56:46 cananian Exp $
 */
%%
    // INNER CLASSES
    /** Sub-class to represent delay-slots.
     * <code>optimize()</code> uses this class information to determine that
     * it should rearrange code to try to eliminate these instructions.
     * @author C. Scott Ananian
     * @see Sparc.CodeGen#optimize
     */
    public class DELAYSLOT extends Instr {
	// a nop to fill the delay slot
	public DELAYSLOT(InstrFactory if, HCodeElement source) {
	    super(if, source, "\t nop \t ! delay slot \n");
	}
    }
    /** Sub-class to indicate dependencies on the condition code register
     *  for later optimization.  This prevents putting the <code>cmp</code>
     *  instruction in the delay-slot of its branch-conditional.
     */
    public class CC extends Instr {
	//a new instr type to indicate dependency on the condition code reg
	public CC(InstrFactory if, HCodeElement source, String assem,
	          Temp[] dst, Temp src[] src) {
	    super(if, source, assem, dst, src);
	}
	/** FIXME: How does Instr represent LabelLists? */
    }
}

/** FIXME: need definition of is13bit() here */
%%
/* this comment will be eaten by the spec lexer (unlike the ones above) */

// patterns with MOVE at their root.
//  32- and 64-bit integer STOREs:
MOVE(MEM<i,l,p>(CONST(c)), e) %pred %( is13bit(c) )% %{
 String op=((Typed) ROOT.dst).isDoubleWord()?"std":"st";
 emit(new InstrMEM(if, ROOT, "\t "+op+" `s0, ["+c+"]\n", null,
		   new Temp[] { e }));
}%
MOVE(MEM<i,l,p>(BINOP(ADD, CONST(c), e1)), e2) %pred %( is13bit(c) )% %{
 String op=((Typed) ROOT.dst).isDoubleWord()?"std":"st";
 emit(new InstrMEM(if, ROOT, "\t "+op+" `s1, [`s0+"+c+"]\n", null,
		   new Temp[] { e1, e2 }));
}%
MOVE(MEM<i,l,p>(BINOP(ADD, e1, CONST(c))), e2) %pred %( is13bit(c) )% %{
 String op=((Typed) ROOT.dst).isDoubleWord()?"std":"st";
 emit(new InstrMEM(if, ROOT, "\t "+op+" `s1, [`s0+"+c+"]\n", null,
		   new Temp[] { e1, e2 }));
}%
MOVE(MEM<i,l,p>(BINOP(ADD, e1, e2)), e3) %{
 String op=((Typed) ROOT.dst).isDoubleWord()?"std":"st";
 emit(new InstrMEM(if, ROOT, "\t "+op+" `s2, [`s0+`s1]\n", null,
                   new Temp[] { e1, e2, e3 }));
}% 
MOVE(MEM<i,l,p>(e1), e2) %{
 String op=((Typed) ROOT.dst).isDoubleWord()?"std":"st";
 emit(new InstrMEM(if, ROOT, "\t "+op+" `s1, [`s0]\n", null,
		   new Temp[] { e1, e2 }));
}%
// FIXME: floating-point stores.
// other MOVEs
MOVE(e1, CONST<i,p>(c)) %pred %( is13bit(c) )% %{
 emit(new Instr(if, ROOT, "\t mov "+c+", `d0\n",
		new Temp[] { e1 }, null));
}%
MOVE(e1, e2) %{ /* catch-all */
 emit(new Instr(if, ROOT, "\t mov `s0, `d0\n",
		new Temp[] { e1 }, new Temp[] { e2 }));
}%

// patterns with EXP at their root.
EXP(e1) %{
  /* throw away temp e1 (not used) */
}%

// patterns with JUMP at their root.
JUMP(NAME(l)) %{
 emit(new Instr(if, ROOT, "\t b "+l+"\n", null, null)); /* target list?! */
 emit(new DELAYSLOT(if, ROOT));
}%
JUMP(BINOP(ADD, e1, e2)) %{
 emit(new Instr(if, ROOT, "\t jmpl `s0+`s1, %g0\n", null,
		new Temp[] { e1, e2 })); /* target list?! */
 emit(new DELAYSLOT(if, ROOT));
}%
JUMP(e1) %{
 emit(new Instr(if, ROOT, "\t jmpl `s0, %g0\n", null,
		new Temp[] { e1 })); /* target list?! */
 emit(new DELAYSLOT(if, ROOT));
}%
