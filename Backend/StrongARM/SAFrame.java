// SAFrame.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Backend.Allocation.AllocationInfo;
import harpoon.Backend.Allocation.AllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationStrategy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.GenericCodeGen;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Backend.Maps.OffsetMap32;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrDIRECTIVE;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.CALL;
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Type;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Label;
import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;

import java.util.List;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;

/**
 * <code>SAFrame</code> contains the machine-dependant
 * information necessary to compile for the StrongARM processor.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @author  Felix Klock <pnkfelix@mit.edu>
 * @version $Id: SAFrame.java,v 1.1.2.18 1999-07-29 00:38:35 pnkfelix Exp $
 */
public class SAFrame extends Frame implements AllocationInfo {
    static Temp[] reg = new Temp[16];
    private static Temp[] regLiveOnExit = new Temp[5];
    private static Temp[] regGeneral = new Temp[11];
    private static TempFactory regtf;
    private TempFactory tf;
    private AllocationStrategy mas;
    private static OffsetMap offmap;

    static final Temp TP;  // Top of memory pointer
    static final Temp HP;  // Heap pointer
    static final Temp FP;  // Frame pointer
    static final Temp IP;  // Scratch register 
    static final Temp SP;  // Stack pointer
    static final Temp LR;  // Link register
    static final Temp PC;  // Program counter

    static {
        regtf = new TempFactory() {
            private int i = 0;
            private final String scope = "strongarm-registers";

            /* StrongARM has 16 general purpose registers.
             * Special notes on ones we set aside:
             *  r11 = fp
             *  r12 = ip
             *  r13 = sp
             *  r14 = lr
             *  r15 = pc (yes that's right. you can access the 
             *              program counter like any other register)
             */
            private final String[] names = {"r0", "r1", "r2", "r3", "r4", "r5",
                                            "r6", "r7", "r8", "r9", "r10", 
                                            "fp", "ip", "sp", "lr", "pc"};

            public String getScope() { return scope; }
            protected synchronized String getUniqueID(String suggestion) {
                Util.assert(i < names.length, "Don't use the "+
			    "TempFactory of Register bound Temps");
		i++;
                return names[i-1];
            }
        };
        for (int i = 0; i < 16; i++) {
            reg[i] = new Temp(regtf);
            if (i < 11) regGeneral[i] = reg[i];
        }

	TP = reg[9];
	HP = reg[10];
	FP = reg[11];
	IP = reg[12];
	SP = reg[13];
	LR = reg[14];
	PC = reg[15];

        regLiveOnExit[0] = reg[0];  // return value
        regLiveOnExit[1] = reg[1]; // return exceptional value
        regLiveOnExit[2] = FP;
        regLiveOnExit[3] = SP;
        regLiveOnExit[4] = PC;
        offmap = new OffsetMap32(null);
    }

    public SAFrame() { 
        mas = new DefaultAllocationStrategy(this);
    }

    /* "method" constructor, use for per-method initializations */
    public Frame newFrame(String scope) {
        SAFrame fr = new SAFrame();
        fr.tf = Temp.tempFactory(scope);
        return fr;
    }

    public boolean pointersAreLong() { return false; }

    public Temp FP() { return reg[11]; }

    public Temp[] getAllRegisters() { return reg; }

    public Temp[] getGeneralRegisters() { return regGeneral; }

    /* Generic version of the next six methods copied from 
     * DefaultFrame for now */

    public Label exitOutOfMemory() { return new Label("_EXIT_OOM"); }
    public Label GC()              { return new Label("_RUNTIME_GC"); }
    public Temp  getMemLimit()     { return TP; } 
    public Temp  getNextPtr()      { return HP; }



    public Exp memAlloc(Exp size) { return mas.memAlloc(size); }

    public OffsetMap getOffsetMap() { return offmap; }

    public TempFactory tempFactory() { return tf; }

    public TempFactory regTempFactory() { return regtf; }

    public Stm procPrologue(TreeFactory tf, HCodeElement src, 
                            Temp[] paramdsts, int[] paramtypes) { 
        Stm prologue = null, move = null;
        int i = 0;
        for (i = 0; i < paramdsts.length && i < 4; i++) {
            move = new MOVE(tf, src,
                            new TEMP(tf, src, paramtypes[i], paramdsts[i]),
                            new TEMP(tf, src, paramtypes[i], reg[i]));
            if (prologue == null) {
                prologue = move;
            } else {
                prologue = new SEQ(tf, src, move, prologue);
            }
        }
        return prologue;
    }

    public Instr procLiveOnExit(Instr body) { 
        return body; 
    }

    public Instr procAssemDirectives(Instr body) { 
        Util.assert(body != null);

        HCodeElement src = body;
        InstrFactory inf = ((Instr)src).getFactory();
	Instr dir1, dir2, dir3, dir4, dir5, dir6, dir7;

        dir1 = new InstrDIRECTIVE(inf, src, ".text");
        dir2 = new InstrDIRECTIVE(inf, src, ".align 0");
        dir3 = new InstrDIRECTIVE(inf, src, ".global " + 
                        offmap.label(inf.getMethod()));
        /* this should be a label */
        dir4 = new InstrLABEL(inf, src, 
                        offmap.label(inf.getMethod()) + ":",
                        offmap.label(inf.getMethod()));
        dir5 = new Instr(inf, src, "mov ip, sp", null, null);
        dir6 = new Instr(inf, src, "stmfd sp!, {fp, ip, lr, pc}",
                              null, null);
        dir7 = new Instr(inf, src, "sub fp, ip, #4", null, null);

	Instr.insertInstrBefore(body, dir1);
	Instr.insertInstrAfter(dir1, dir2);
	Instr.insertInstrAfter(dir2, dir3);
	Instr.insertInstrAfter(dir3, dir4);
	Instr.insertInstrAfter(dir4, dir5);
	Instr.insertInstrAfter(dir5, dir6);
	Instr.insertInstrAfter(dir6, dir7);

	return dir1;
    }

    /** Stub added by FSK */
    public List makeLoad(Temp r, int offset, Instr template) {
	InstrMEM load = 
	    new InstrMEM(template.getFactory(), template,
			 "ldr `d0, [`s0, #" +(-4*offset) + "] \t; " + template,
			 new Temp[]{ r },
			 new Temp[]{ SP  });
	return Arrays.asList(new Object[] { load });
				     
    }

    /** Stub added by FSK */
    public List makeStore(Temp r, int offset, Instr template) {
	InstrMEM store = 
	    new InstrMEM(template.getFactory(), template,
			 "str `s0, [`s1, #" +(-4*offset) + "] \t; " + template,
			 new Temp[]{ },
			 new Temp[]{ r , SP });
	return Arrays.asList(new Object[] { store });
    }

    /** Stub added by FSK */
    public Iterator suggestRegAssignment(Temp t, Map regfile) {
	Util.assert(false, "SAFrame.suggestRegAssigment() Not implemented yet");
	return null;
    }
    /** Not implemented. */    
    public String getRegisterName(Temp val, String suffix, Map valToRegMap) {
	Util.assert(false, "DefaultFrame.getRegisterName() Not implemented");
	return null;

    }

    /** Not implemented. */
    public GenericCodeGen codegen() { 
	Util.assert(false, "DefaultFrame.codegen() Not implemented");
	return null;
    }
}
