// CodeGen.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Tree.Exp;
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
import harpoon.IR.Tree.TreeCode;
import harpoon.IR.Tree.TreeVisitor;
import harpoon.Temp.Temp;

/**
 * <code>CodeGen</code> is a utility class which implements instruction
 * selection of <code>Instr</code>s from an input <code>Tree</code>.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: CodeGen.java,v 1.1.2.5 1999-02-17 21:32:40 andyb Exp $
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
        Visitor v = new Visitor(inf);

        return null;
    }

    public static final class Visitor extends TreeVisitor {
        final InstrFactory inf; 
        Temp visitRet;

        Visitor(InstrFactory inf) {
            this.inf = inf; 
        }

        void emit(Instr instr) {
            /* XXX */
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
            /* XXX */
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
            /* XXX */
            visitRet = null;
        }

        public void visit(MOVE s) {
            /* XXX */
            visitRet = null;
        }
        
        public void visit(SEQ s) {
            s.left.visit(this);
            s.right.visit(this);
            visitRet = null;
        }
       
        /* Expressions */
        public void visit(BINOP e) {
            /* XXX */
        }

        public void visit(CALL e) {
            /* XXX */
        }

        public void visit(CONST e) {
            /* XXX */
            Temp r = new Temp(inf.tempFactory());
            
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
            /* XXX */
        }

        public void visit(TEMP e) {
            visitRet = e.temp.clone(inf.tempFactory());
        }

        public void visit(UNOP e) {
            /* XXX */
        }
    }
}
