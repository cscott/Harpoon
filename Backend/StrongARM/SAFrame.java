// SAFrame.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.CloningTempMap;
import harpoon.Backend.Allocation.AllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationInfo;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.IR.Assem.InstrList;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.TreeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;

/**
 * <code>SAFrame</code> contains the machine-dependant
 * information necessary to compile for the StrongARM processor.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: SAFrame.java,v 1.1.2.4 1999-02-26 22:48:02 andyb Exp $
 */
public class SAFrame extends Frame implements DefaultAllocationInfo {
    private static Temp[] reg = new Temp[16];
    private static Temp[] regLiveOnExit = new Temp[5];
    private static Temp[] regGeneral = new Temp[11];
    /** TempFactory used to create register temps */
    private static TempFactory regtf;
    /** TempFactory used to create global temps needed for memory
     *  allocation and garbage collection */
    private TempFactory tf;
    private AllocationStrategy mas;

    {
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
            regGeneral[i] = reg[i];
        }
        regLiveOnExit[0] = reg[0];  // return value
        regLiveOnExit[1] = reg[11]; // fp
        regLiveOnExit[2] = reg[13]; // sp
        regLiveOnExit[3] = reg[15]; // pc
        regLiveOnExit[4] = reg[1]; // return exceptional value
    }

    public SAFrame() { 
        mas = new DefaultAllocationStrategy(this);
    }

    public Frame newFrame(String scope) {
        SAFrame fr = new SAFrame();
        fr.tf = Temp.tempFactory(scope);
        return fr;
    }

    public boolean pointersAreLong() { return false; }

    public Temp FP() { return reg[11]; }

    public Temp[] getAllRegisters() { return reg; }

    public Temp[] getGeneralRegisters() { return regGeneral; }

    public Stm callGC(TreeFactory tf, HCodeElement src) { return null; }

    public Exp getMemLimit(TreeFactory tf, HCodeElement src) { return null; }

    public MEM getNextPtr(TreeFactory tf, HCodeElement src) { return null; }

    public Stm exitOutOfMemory(TreeFactory tf, HCodeElement src) { return null; }

    public Exp memAlloc(Exp size) { return mas.memAlloc(size); }

    public OffsetMap getOffsetMap() { return null; }

    public TempFactory tempFactory() { return tf; }

    public TempFactory regTempFactory() { return regtf; }

    public Stm procPrologue(TreeFactory tf, HCodeElement src, 
                            Temp[] paramdsts, CloningTempMap ctm) { 
        return null; 
    }

    public InstrList procLiveOnExit(InstrList body) { return body; }

    public InstrList procAssemDirectives(InstrList body) { return body; }
}
