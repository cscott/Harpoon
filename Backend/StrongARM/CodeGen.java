// CodeGen.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Assem.Instr;
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
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;
import java.util.Enumeration;
import java.util.Vector;

/**
 * <code>CodeGen</code> is a utility class which implements instruction
 * selection of <code>Instr</code>s from an input <code>Tree</code>.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: CodeGen.java,v 1.1.2.7 1999-05-17 20:02:07 andyb Exp $
 */
final class CodeGen {

    /** Generates StrongARM assembly from the internal Tree representation.
     *  <BR> XXX - NOT YET FULLY IMPLEMENTED.
     *
     *  @param  tree    The Tree codeview to generate code from.
     *  @param  code    The StrongARM codeview to generate code for.
     *  @return         The newly generated StrongARM instructions.
     */
    public static final Instr[] codegen(TreeCode tree, SACode code) {
                                      
        InstrFactory inf = code.getInstrFactory();
        Frame f = code.getFrame();
        Vector instrList = new Vector();
        Vector dataList = new Vector();
        Visitor v = new Visitor(inf, instrList, dataList, f);

        ((Tree)tree.getRootElement()).visit(v);

        Instr[] instrs = new Instr[instrList.size()+dataList.size()];
        Enumeration enum = instrList.elements();
        for (int i = 0; enum.hasMoreElements(); i++) {
            instrs[i] = (Instr)enum.nextElement();
        }
        enum = dataList.elements();
        for (int i = instrList.size(); enum.hasMoreElements(); i++) {
            Label l = (Label)enum.nextElement();
            if (i == instrList.size()) {
                instrs[i] = new InstrLABEL(inf, null, l+":", l);
            } else {
                instrs[i] = new InstrDIRECTIVE(inf, null, ".word "+l);
            }
        }
        return instrs;
    }

    static final class Visitor extends TreeVisitor {
        final Frame f;
		final TempFactory tf;
        final InstrFactory inf; 
       	ExpValue visitRet;
        Vector instrList;
        Vector dataList;
        Label dataLabel;

        Visitor(InstrFactory inf, Vector instrList, Vector dataList,Frame f) {
            this.f = f;
            this.inf = inf; 
            this.instrList = instrList;
            this.dataList = dataList;
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
	}

        public void visit(Exp e) {
            // Error! Error!
        }

        public void visit(Stm e) {
            // Error! Error!
        }

        public void visit(OPER e) {
            // Error! Error!
        }

        /* Statements */
        public void visit(CJUMP s) {
            s.test.visit(this);
            ExpValue test = visitRet;
            emit(new Instr(inf, s, "cmp `s0, #0", 
                           new Temp[] { test.temp() },
                           null));
            emit(new Instr(inf, s, "beq " + s.iffalse, null, null,
                           new Label[] { s.iffalse }));
            emit(new Instr(inf, s, "b " + s.iftrue, null, null,
                           new Label[] { s.iftrue }));
            visitRet = null;
        }

        public void visit(EXP s) {
            s.exp.visit(this);
            visitRet = null;
        }

        public void visit(JUMP s) {
            /* XXX */
            visitRet = null;
        }

        public void visit(LABEL s) {
            emit(new InstrLABEL(inf, s, s.label + ":", s.label));
            visitRet = null;
        }

        public void visit(MOVE s) {
            s.dst.visit(this);
            ExpValue dest = visitRet;
            if (s.src instanceof CONST) {
                emitMoveConst(s, dest.temp(), ((CONST)s.src).value);
            } else {
            s.src.visit(this);
            ExpValue source = visitRet;
            if (dest.isDouble()) {
                emit(new Instr(inf, s, "mov `d0, `s0",
                               new Temp[] { source.low() },
                               new Temp[] { dest.low() }));
                emit(new Instr(inf, s, "mov `d0, `s0",
                               new Temp[] { source.high() },
                               new Temp[] { dest.high() }));
            } else {
                emit(new Instr(inf, s, "mov `d0, `s0",
                               new Temp[] { source.temp() },
                               new Temp[] { dest.temp() }));
            }
            }
            visitRet = null;
        }
       
        public void visit(RETURN s) {
                s.retval.visit(this);
                ExpValue retval = visitRet;
                emit(new Instr(inf, s, "mov r1, #0", null, null));
                emit(new Instr(inf, s, "mov r0, `s0",
                              new Temp[] { retval.temp() }, null));
                emit(new Instr(inf, s, "ldmea fp, {fp, sp, pc}", null, null));
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
            emit(new Instr(inf, s, "mov r1, `s0",
                           new Temp[] { retval.temp() }, null));
            emit(new Instr(inf, s, "mov r0, #0", null, null));
            emit(new Instr(inf, s, "ldmea fp, {fp, sp, pc}", null, null));
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
                emit(new Instr(inf, e, "add `d0, `s0, `s1",
                               new Temp[] { left.temp(), right.temp() },
                               new Temp[] { retval.temp() }));
                visitRet = retval;
                break;
            case Bop.CMPEQ:
                e.left.visit(this);
                left = visitRet;
                e.right.visit(this);
                right = visitRet;
                retval = new ExpValue(new Temp(tf));
                emit(new Instr(inf, e, "mov `d0, #0",
                               null,
                               new Temp[] { retval.temp() }));
                emit(new Instr(inf, e, "cmp `s0, `s1",
                               new Temp[] { left.temp(), right.temp() },
                               null));
                emit(new Instr(inf, e, "moveq `d0, #1",
                               null,
                               new Temp[] { retval.temp() }));
                visitRet = retval;
                break;
            }
        }

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
                               new Temp[] { arg.temp() },
                               new Temp[] { (f.getAllRegisters())[i] }));
                args = args.tail;
                i++;
            }
            emit(new Instr(inf, e, "mov `d0, `s0",
                                    new Temp[] { (f.getAllRegisters())[15] },
                                    new Temp[] { (f.getAllRegisters())[14] }));
            /* need someway to get label for branch target info */
            emit(new Instr(inf, e, "mov `d0, `s0",
                                    new Temp[] { func.temp() },
                                    new Temp[] { (f.getAllRegisters())[15] }));
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
            /* XXX */
        }

        public void visit(NAME e) {
            ExpValue retval = new ExpValue(new Temp(tf));
            emitLabelRef(((NAME)e).label, e);
            emit(new Instr(inf, e, "ldr `d0, "+ getLabelRef(((NAME)e).label),
                           null,
                           new Temp[] { retval.temp() }));
            visitRet = retval;
        }

        public void visit(NATIVECALL e) {

        }

        public void visit(TEMP e) {
			if (e.isDoubleWord()) {
                /* to be fixed */
				visitRet = new ExpValue(new Temp(tf), new Temp(tf));
			} else {
				visitRet = new ExpValue(e.temp);
			}
        }

        public void visit(UNOP e) {
            /* XXX */
        }

        private void emitMoveConst(HCodeElement s, Temp t, Number n) {
            emit(new Instr(inf, s, "mov `d0, #"+n.intValue(), null,
                           new Temp[] { t }));
        }
    }
}
