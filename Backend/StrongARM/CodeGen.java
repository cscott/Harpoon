// CodeGen.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Assem.InstrDIRECTIVE;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Tree.Bop;
import harpoon.IR.Tree.Uop;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.OPER;
import harpoon.IR.Tree.CJUMP;
import harpoon.IR.Tree.EXP;
import harpoon.IR.Tree.JUMP;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.BINOP;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.UNOP;
import harpoon.IR.Tree.RETURN;
import harpoon.IR.Tree.THROW;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.Tree;
import harpoon.IR.Tree.TreeCode;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Label;
import harpoon.Temp.LabelList;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;
import java.util.*;

/**
 * <code>CodeGen</code> is a utility class which implements instruction
 * selection of <code>Instr</code>s from an input <code>Tree</code>.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: CodeGen.java,v 1.1.2.16 1999-06-15 04:35:21 pnkfelix Exp $
 */
final class CodeGen {

    /** Generates StrongARM assembly from the internal Tree representation.
     *  <BR> XXX - NOT YET FULLY IMPLEMENTED.
        <BR> <B>requires:</B> TODO: Fill in.
	<BR> <B>effects:</B> TODO: Fill in.
     *
     *  @param  tree    The Tree codeview to generate code from.
     *  @param  code    The StrongARM codeview to generate code for.
     *  @return         A <code>List</code> of <code>Instr</code>s
                        representing the newly generated StrongARM
			instructions.   
     */
    public static final Instr codegen(TreeCode tree, SACode code) {
                                      
        InstrFactory inf = code.getInstrFactory();
        SAFrame f = (SAFrame) code.getFrame();
        Vector instrList = new Vector();
        Vector dataList = new Vector();
	Hashtable blMap = new Hashtable();
	Hashtable liMap = new Hashtable();
        Visitor v = new Visitor(inf, instrList, dataList, f, blMap, liMap);

        ((Tree)tree.getRootElement()).visit(v);

        Instr instrs = null, curr = null;

        Enumeration enum = instrList.elements();
	if (enum.hasMoreElements()) {
	    curr = instrs = (Instr)enum.nextElement();
        }
	while (enum.hasMoreElements()) {
	    Instr tinstr = (Instr)enum.nextElement();
	    Instr.insertInstrAfter(curr, tinstr);
	    curr = tinstr;
        }

	enum = dataList.elements();
	if (enum.hasMoreElements()) {
	    Label l = (Label)enum.nextElement();
	    Instr i = new InstrLABEL(inf, null, l+":", l);	
	    if (curr != null) {
		Instr.insertInstrAfter(curr, i);
		curr = i;
            } else {
		curr = instrs = i;
            }
        }
	while (enum.hasMoreElements()) {
            Label l = (Label)enum.nextElement();
	    Instr i = new InstrDIRECTIVE(inf, null, ".word " + l);
	    Instr.insertInstrAfter(curr, i);
	    curr = i;
        } 

	// add branch edges
	enum = blMap.keys();
	while (enum.hasMoreElements()) {
            Instr i = (Instr)enum.nextElement();
	    Label l = (Label)blMap.get(i);
	    Instr labeli = (Instr)liMap.get(l); 
            Instr.addEdge(i, labeli);
        }

        return instrs;
    }

    static final class Visitor extends TreeVisitor {
        final SAFrame f;
	final TempFactory tf;
        final InstrFactory inf; 
       	ExpValue visitRet;
        Vector instrList;
        Vector dataList;
        Label dataLabel;

	// maps instr's with branchs to the label that they branch to
	// strongarm instruction set only has condition branching - no switch
        Hashtable blMap;

	// maps labels to the instrLABEL's for those labels
	Hashtable liMap;

        Visitor(InstrFactory inf, Vector instrList, Vector dataList,
                SAFrame f, Hashtable blMap, Hashtable liMap) {
            this.f = f;
            this.inf = inf; 
            this.instrList = instrList;
            this.dataList = dataList;
	    this.blMap = blMap;
	    this.liMap = liMap;
            tf = inf.tempFactory();
        }

        void emit(Instr instr) {
            instrList.addElement(instr);
        }

        void emitLabelRef(Label label, Tree src) {
            if (dataList.size() == 0) {
                dataLabel = new Label();
                dataList.addElement(dataLabel);
            }
            dataList.addElement(label);
        }

        String getLabelRef(Label label) {
            boolean done = false;
            int i = 0;
            Label l;
            Enumeration enum = dataList.elements();
            if (!enum.hasMoreElements()) return "";
            enum.nextElement(); /* throw away initial label */
            while (enum.hasMoreElements() && !done) {
                l = (Label)enum.nextElement();
                done = (l.equals(label));
                i++;
            }
            if (!done) return "";
            return new String(dataLabel+"+"+(4*(i-1)));
        }
            
        public void visit(Tree e) {
            // Error! Error!
	    Util.assert(false, "Should not be visiting Tree in CodeGen");
	}

        public void visit(Exp e) {
	    Util.assert(false, "Should not be visiting Exp in CodeGen");
        }

        public void visit(Stm e) {
	    Util.assert(false, "Should not be visiting Stm in CodeGen");
        }

        public void visit(OPER e) {
	    Util.assert(false, "Should not be visiting OPER in CodeGen");
        }

        /* Statements */
        public void visit(CJUMP s) {
            s.test.visit(this);
            Util.assert(visitRet!=null, "visitRet was null after visiting " + s.test);
	    ExpValue test = visitRet;
            emit(new Instr(inf, s, "cmp `s0, #0  \t\t; " + s, 
                           null,
			   new Temp[] { test.temp() }));

	    Instr i = new Instr(inf, s, "beq " + s.iffalse, null, null);
	    emit(i);
	    blMap.put(i, s.iffalse);

	    i = new Instr(inf, s, "b " + s.iftrue, null, null);
            emit(i);
            blMap.put(i, s.iftrue);

            visitRet = null;
        }

        public void visit(EXP s) {
            s.exp.visit(this);
            visitRet = null;
        }

        public void visit(JUMP s) {
            /* TODO: this may not produce working code.  Check it. */
	    s.exp.visit(this);
	    Util.assert(visitRet != null, "visitRet was null after visiting " + s.exp);
	    Instr i = new Instr(inf, s, "b `s0  \t\t\t; " + s, null, new Temp[]{visitRet.temp()} );
	    emit(i);
	    LabelList targets = s.targets;
	    while (targets != null) {
		blMap.put(i, targets.head);
		targets = targets.tail;
	    }
	    
	    // Util.assert(false, "visit(JUMP) not implemented");
	    visitRet = null;
        }

        public void visit(LABEL s) {
            Instr i = new InstrLABEL(inf, s, s.label + ":", s.label);
            emit(i);
            liMap.put(s.label, i);

            visitRet = null;
        }

        public void visit(MOVE s) {
	    String movStr;
            if (s.dst instanceof MEM) {
		// prepare a store; note that strong arm makes the
		// store-target the second argument
		((MEM)s.dst).exp.visit(this);
		ExpValue dest = visitRet;

		s.src.visit(this);
 		ExpValue source = visitRet; 
		movStr = "str `s0, `s1";
		emit(new Instr(inf, s, "str `s0, [`s1]",
			       null, new Temp[]{ source.temp(),
						     dest.temp() }));
	    } else {
		s.dst.visit(this);
		Util.assert(visitRet != null, 
			    "visitRet should not be null after visiting " + 
			    s.dst);
		movStr = "mov `d0, `s0";

		ExpValue dest = visitRet;
		if (s.src instanceof CONST) {
		    emitMoveConst(s, dest.temp(), ((CONST)s.src).value);
		} else {
		    s.src.visit(this);
		    Util.assert(visitRet != null, 
				"visitRet should not be null after visiting " + 
				s.src);
		    ExpValue source = visitRet;
		    if (dest.isDouble()) {
			emit(new Instr(inf, s, movStr + "  \t; " + s,
				       new Temp[] { dest.low() },
				       new Temp[] { source.low() }));
			emit(new Instr(inf, s, movStr,
				       new Temp[] { dest.high() },
				       new Temp[] { source.high() }));
		    } else {
			emit(new Instr(inf, s, movStr + "  \t\t; " + s,
				       new Temp[] { dest.temp() },
				       new Temp[] { source.temp() }));
		    }
		}
		visitRet = null;
	    }
	}
	
        public void visit(RETURN s) {
                s.retval.visit(this);
                ExpValue retval = visitRet;

		// FSK: need to still do substitution (instead of
		// typing r0 and r1 into the String directly below) so
		// that the allocator can identify when operations are
		// occuring in the register file.
                emit(new Instr(inf, s, "mov `d0, #0  \t\t; " + s, new Temp[] {f.getAllRegisters()[1]}, null));
                emit(new Instr(inf, s, "mov `d0, `s0",
			       new Temp[] { f.getAllRegisters()[0] }, new Temp[] { retval.temp() }));
                emit(new InstrMEM(inf, s, "ldmea `s0, {`d0, `d1, `d2}",
				  new Temp[]{ SAFrame.FP, SAFrame.SP, SAFrame.PC }, 
				  new Temp[]{ SAFrame.FP } ));
                visitRet = null;
        }

        public void visit(SEQ s) {
            s.left.visit(this);
            s.right.visit(this);
            visitRet = null;
        }

        public void visit(THROW s) {
            s.retex.visit(this);
            ExpValue retval = visitRet;
            emit(new Instr(inf, s, "mov `d0, `s0  \t\t; " + s,
                           new Temp[] { f.getAllRegisters()[1] },
			   new Temp[] { retval.temp() }));
            emit(new Instr(inf, s, "mov `d0, #0", new Temp[]{ f.getAllRegisters()[0] }, null));
            emit(new InstrMEM(inf, s, "ldmea `s0, {`d0, `d1, `d2}", 
			      new Temp[]{ SAFrame.FP, SAFrame.SP, SAFrame.PC },
			      new Temp[]{ SAFrame.FP }));
            visitRet = null;
        }
       
        /* Expressions */
        public void visit(BINOP e) {
            ExpValue retval, left, right;
            switch (e.op) {
            case Bop.ADD:
                e.left.visit(this);
                left = visitRet;
                e.right.visit(this);
                right = visitRet;
                retval = new ExpValue(new Temp(tf));
                emit(new Instr(inf, e, "add `d0, `s0, `s1  \t\t; " + e,
                               new Temp[] { retval.temp() } ,
			       new Temp[] { left.temp(), right.temp() } ));
                visitRet = retval;
                break;
	    case Bop.AND: // FSK added, not andyb
		e.left.visit(this);
		left = visitRet;
		e.right.visit(this);
		right = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "and `d0, `s0, `s1  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { left.temp(), right.temp() }));
		visitRet = retval;
		break;
            case Bop.CMPEQ:
		// Three lines of Assem in ONE Instr!  Crazy, but
		// necessary for proper analysis
                e.left.visit(this);
                left = visitRet;
                e.right.visit(this);
                right = visitRet;
                retval = new ExpValue(new Temp(tf));
                emit(new Instr(inf, e, 
			       "mov `d0, #0  \t\t; " + e + "\n\t"+
			       "cmp `s0, `s1 \n\t"+
			       "moveq `d0, #1", 
			       new Temp[] { retval.temp() },
                               new Temp[] { left.temp(), right.temp() }));
                visitRet = retval;
                break;
	    case Bop.CMPGT: // FSK added, not andyb
                e.left.visit(this);
                left = visitRet;
                e.right.visit(this);
                right = visitRet;
                retval = new ExpValue(new Temp(tf));
                emit(new Instr(inf, e, 
			       "mov `d0, #0  \t\t; " + e + "\n\t"+
			       "cmp `s0, `s1 \n\t"+
			       "moveq `d0, #1", 
			       new Temp[] { retval.temp() },
                               new Temp[] { left.temp(), right.temp() }));
                visitRet = retval;
		break;
	    case Bop.CMPLE: // FSK added, not andyb
                e.left.visit(this);
                left = visitRet;
                e.right.visit(this);
                right = visitRet;
                retval = new ExpValue(new Temp(tf));
                emit(new Instr(inf, e, 
			       "mov `d0, #0  \t\t; " + e + "\n\t"+
			       "cmp `s0, `s1 \n\t"+
			       "movle `d0, #1", 
			       new Temp[] { retval.temp() },
                               new Temp[] { left.temp(), right.temp() }));
                visitRet = retval;
		break;
	    case Bop.CMPLT: // FSK added, not andyb
                e.left.visit(this);
                left = visitRet;
                e.right.visit(this);
                right = visitRet;
                retval = new ExpValue(new Temp(tf));
                emit(new Instr(inf, e, 
			       "mov `d0, #0  \t\t; " + e + "\n"+
			       "cmp `s0, `s1 \n"+
			       "movlt `d0, #1", 
			       new Temp[] { retval.temp() },
                               new Temp[] { left.temp(), right.temp() }));
                visitRet = retval;
		break;
	    case Bop.DIV: // FSK added, not andyb
		Util.assert(false, "Don't know how to handle Bop.DIV");
		break;
	    case Bop.MUL: // FSK added, not andyb
		e.left.visit(this);
                left = visitRet;
                e.right.visit(this);
                right = visitRet;
                retval = new ExpValue(new Temp(tf));
                emit(new Instr(inf, e, "mul `d0, `s0, `s1  \t\t; " + e,
                               new Temp[] { retval.temp() },
			       new Temp[] { left.temp(), right.temp() }));
                visitRet = retval;
		break;
	    case Bop.OR: // FSK added, not andyb
		e.left.visit(this);
		left = visitRet;
		e.right.visit(this);
		right = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "orr `d0, `s0, `s1  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { left.temp(), right.temp() }));
		visitRet = retval;
		break;
	    case Bop.REM: // FSK added, not andyb
		Util.assert(false, "Don't know how to handle Bop.REM");
		break;
	    case Bop.SHL: // FSK added, not andyb
		e.left.visit(this);
		left = visitRet;
		e.right.visit(this);
		right = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "mov `d0, `s0 LSL `s1  \t\t; " + e,
			       new Temp[] { retval.temp() } ,
			       new Temp[] { left.temp(), right.temp() }));
		visitRet = retval;
		break;
	    case Bop.SHR: // FSK added, not andyb
		e.left.visit(this);
		left = visitRet;
		e.right.visit(this);
		right = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "mov `d0, `s0 LSR `s1  \t\t; " + e,
			       new Temp[] { retval.temp() } ,
			       new Temp[] { left.temp(), right.temp() }));
		visitRet = retval;
		break;
	    case Bop.USHR: // FSK added, not andyb
		Util.assert(false, "Don't know how to handle Bop.USHR");
		break;
	    case Bop.XOR: // FSK added, not andyb
		e.left.visit(this);
		left = visitRet;
		e.right.visit(this);
		right = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "eor `d0, `s0, `s1  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { left.temp(), right.temp() }));
		visitRet = retval;
		break;
	    default:
		Util.assert(false, "Failed to match operation " + e);
            }
        }

	// The argument passing in this routine looks flawed.  Check it.
        public void visit(CALL e) {
            ExpValue retval, retex, arg, func;
            ExpList args = e.args;
            int i = 0;
            e.retval.visit(this);
            retval = visitRet;
            e.retex.visit(this);
            retex = visitRet;
            e.func.visit(this);
            func = visitRet; 
            while (args != null) {
                args.head.visit(this); 
                arg = visitRet;
                emit(new Instr(inf, e, "mov `d0, `s0",
                               new Temp[] { (f.getAllRegisters())[i] },
                               new Temp[] { arg.temp() }));
                args = args.tail;
                i++;
            }
            emit(new Instr(inf, e, "mov `d0, `s0  \t\t; " + e,
                                    new Temp[] { f.PC },
                                    new Temp[] { f.LR }));
            /* need someway to get label for branch target info */
            emit(new Instr(inf, e, "mov `d0, `s0",
                                    new Temp[] { f.PC },
                                    new Temp[] { func.temp() }));
            emit(new Instr(inf, e, "mov `d0, `s0",
                                   new Temp[] { (f.getAllRegisters())[0] },
                                   new Temp[] { retval.temp() }));
            emit(new Instr(inf, e, "mov `d0, `s0",
                                   new Temp[] { (f.getAllRegisters())[1] },
                                   new Temp[] { retex.temp() }));
            visitRet = retval;
        }

        public void visit(CONST e) {
            ExpValue retval = new ExpValue(new Temp(tf));
            emitMoveConst(e, retval.temp(), e.value);
            visitRet = retval;
        }


        public void visit(ESEQ e) {
            e.stm.visit(this);
            e.exp.visit(this);
            /* this returns whatever the last visitor set
             * visitRet to. */
        }
	
        public void visit(MEM e) { 
	    /* 
	       MEM is handled differently depending on which child of 
	       the Exp it is:
	       LeftHand Side --> Store
	       RightHand Side --> Load
	       The problem is that from the scope of this function, we
	       don't know whether we are on the LHS or RHS...

	       Solution:
	       INVARIANT --  MEM is only directly visited on the RHS
	                     of an instr.
	       This way visit(MEM) knows to output a Load
	    */
	    e.exp.visit(this);
	    ExpValue addr = visitRet;
	    visitRet = new ExpValue(new Temp(tf));
	    emit(new InstrMEM(inf, e, "ldr `d0, `s0  \t\t; " + e, 
			   new Temp[] { visitRet.temp() },
			   new Temp[] { addr.temp() }));
	}
	
	// I don't think the below code is handled correctly; should
	// this be an InstrMEM or not?  Its *definitely* a load, but
	// the register allocator as currently implemented thinks that
	// its a store because the dest isn't a register
        public void visit(NAME e) {
            ExpValue retval = new ExpValue(new Temp(tf));
            emitLabelRef(((NAME)e).label, e);
            emit(new Instr(inf, e, "ldr `d0, "+ getLabelRef(((NAME)e).label) + "  \t\t; " + e,
                           new Temp[] { retval.temp() },null));
            visitRet = retval;
        }

        public void visit(NATIVECALL e) {

        }

        public void visit(TEMP e) {
    	    if (e.isDoubleWord()) {
                /* XXX - to be fixed */
	        visitRet = new ExpValue(new Temp(tf), new Temp(tf));
	    } else {
		visitRet = new ExpValue(e.temp);
            }
        }

	/** FIX THIS.
	 */
        public void visit(UNOP e) {
	    ExpValue retval, exp;
	    switch(e.op) {
	    case Uop._2B:
		e.operand.visit(this);
		exp = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "_2b `d0, `s0  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { exp.temp() }));
		break;
		// Util.assert(false, "FSK: Don't know how to handle _2B for StrongARM " + e);
	    case Uop._2C:
		e.operand.visit(this);
		exp = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "_2c `d0, `s0  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { exp.temp() }));
		break;
		// Util.assert(false, "FSK: Don't know how to handle _2C for StrongARM " + e);
	    case Uop._2D:
		e.operand.visit(this);
		exp = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "_2d `d0, `s0  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { exp.temp() }));
		break;
		// Util.assert(false, "FSK: Don't know how to handle _2D for StrongARM " + e);
	    case Uop._2F:
		e.operand.visit(this);
		exp = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "_2f `d0, `s0  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { exp.temp() }));
		break;
		// Util.assert(false, "FSK: Don't know how to handle _2F for StrongARM " + e);
	    case Uop._2I:
		e.operand.visit(this);
		exp = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "_2i `d0, `s0  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { exp.temp() }));
		break;
		// Util.assert(false, "FSK: Don't know how to handle _2I for StrongARM " + e);
	    case Uop._2L:
		e.operand.visit(this);
		exp = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "_2l `d0, `s0  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { exp.temp() }));
		break;
		// Util.assert(false, "FSK: Don't know how to handle _2L for StrongARM " + e);
	    case Uop._2S:
		e.operand.visit(this);
		exp = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "_2s `d0, `s0  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { exp.temp() }));
		break;
		// Util.assert(false, "FSK: Don't know how to handle _2S for StrongARM " + e);
	    case Uop.NEG:
		e.operand.visit(this);
		exp = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "neg `d0, `s0  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { exp.temp() }));
		break;
		// Util.assert(false, "FSK: Don't know how to handle NEG for StrongARM " + e);
	    case Uop.NOT:
		e.operand.visit(this);
		exp = visitRet;
		retval = new ExpValue(new Temp(tf));
		emit(new Instr(inf, e, "not `d0, `s0  \t\t; " + e,
			       new Temp[] { retval.temp() },
			       new Temp[] { exp.temp() }));
		break;
		// Util.assert(false, "FSK: Don't know how to handle NOT for StrongARM " + e);
	    default:
		Util.assert(false, "Failed to match operation " + e);
	    }
        }

        private void emitMoveConst(HCodeElement s, Temp t, Number n) {
            emit(new Instr(inf, s, "mov `d0, #"+n.intValue()  + "  \t\t; " + s,
                           new Temp[] { t },null));
        }
    }
}
