// SAFrame.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Backend.Allocation.AllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationInfo;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Backend.Maps.OffsetMap32;
import harpoon.IR.Assem.Instr;
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

/**
 * <code>SAFrame</code> contains the machine-dependant
 * information necessary to compile for the StrongARM processor.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: SAFrame.java,v 1.1.2.7 1999-05-17 20:02:08 andyb Exp $
 */
public class SAFrame extends Frame implements DefaultAllocationInfo {
    private static Temp[] reg = new Temp[16];
    private static Temp[] regLiveOnExit = new Temp[5];
    private static Temp[] regGeneral = new Temp[11];
    private static TempFactory regtf;
    private TempFactory tf;
    private AllocationStrategy mas;
    private static OffsetMap offmap;
    private static int nextPtr;

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
                Util.assert(i <= names.length);
                return names[i++];
            }
        };
        for (int i = 0; i < 16; i++) {
            reg[i] = new Temp(regtf);
            if (i < 11) regGeneral[i] = reg[i];
        }
        regLiveOnExit[0] = reg[0];  // return value
        regLiveOnExit[1] = reg[11]; // fp
        regLiveOnExit[2] = reg[13]; // sp
        regLiveOnExit[3] = reg[15]; // pc
        regLiveOnExit[4] = reg[1]; // return exceptional value
        offmap = new OffsetMap32(null);
        nextPtr = 0x0fff0000; // arbitrary value
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
    public Stm callGC(TreeFactory tf, HCodeElement src) { 
        return new CALL(tf, src,
                        new TEMP(tf, src,
                                 Type.POINTER, new Temp(tf.tempFactory())),
                        new TEMP(tf, src,
                                 Type.POINTER, new Temp(tf.tempFactory())),
                        new NAME(tf, src, new Label("_RUNTIME_GC")),
                        null); 
    }

    public Exp getMemLimit(TreeFactory tf, HCodeElement src) { 
        return new CONST(tf, src, 4000000);
    }

    public MEM getNextPtr(TreeFactory tf, HCodeElement src) { 
        return new MEM(tf, src, Type.INT,
                       new CONST(tf, src, nextPtr));
    }

    public Stm exitOutOfMemory(TreeFactory tf, HCodeElement src) { 
        return new CALL(tf, src,
                        new TEMP(tf, src,
                                 Type.POINTER, new Temp(tf.tempFactory())),
                        new TEMP(tf, src,
                                 Type.POINTER, new Temp(tf.tempFactory())),
                        new NAME(tf, src, new Label("_RUNTIME_OOM")),
                        null);
    }

    public Exp memAlloc(Exp size) { return mas.memAlloc(size); }

    public OffsetMap getOffsetMap() { return offmap; }

    public TempFactory tempFactory() { return tf; }

    public TempFactory regTempFactory() { return regtf; }

    public Stm procPrologue(TreeFactory tf, HCodeElement src, 
                            Temp[] paramdsts) { 
        Stm prologue = null, move = null;
        int i = 0;
        for (i = 0; i < paramdsts.length && i < 4; i++) {
            move = new MOVE(tf, src,
                            new TEMP(tf, src, Type.INT, paramdsts[i]),
                            new TEMP(tf, src, Type.INT, reg[i]));
            if (prologue == null) {
                prologue = move;
            } else {
                prologue = new SEQ(tf, src, move, prologue);
            }
        }
        return prologue;
    }

    public Instr[] procLiveOnExit(Instr[] body) { 
        return body; 
    }

    public Instr[] procAssemDirectives(Instr[] body) { 
        Util.assert((body != null) && (body.length > 0));
        Instr[] newbody = new Instr[body.length + 7];
        HCodeElement src = body[0];
        InstrFactory inf = ((Instr)src).getFactory();
        newbody[0] = new InstrDIRECTIVE(inf, src, ".text");
        newbody[1] = new InstrDIRECTIVE(inf, src, ".align 0");
        newbody[2] = new InstrDIRECTIVE(inf, src, ".global " + 
                        offmap.label(inf.getMethod()));
        /* this should be a label */
        newbody[3] = new InstrLABEL(inf, src, 
                        offmap.label(inf.getMethod()) + ":",
                        offmap.label(inf.getMethod()));
        newbody[4] = new Instr(inf, src, "mov ip, sp", null, null);
        newbody[5] = new Instr(inf, src, "stmfd sp!, {fp, ip, lr, pc}",
                              null, null);
        newbody[6] = new Instr(inf, src, "sub fp, ip, #4", null, null);
        System.arraycopy(body, 0, newbody, 7, body.length);
        return newbody; 
    }
}
