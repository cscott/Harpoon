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
 * @version $Id: SAFrame.java,v 1.1.2.3 1999-02-17 21:14:09 andyb Exp $
 */
public class SAFrame extends Frame implements DefaultAllocationInfo {
    private static Temp[] reg = new Temp[16];
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
            private final String[] names = {"r0", "r1", "r2", "r3", "r4", "r5",
                                            "r6", "r7", "r8", "r9", "r10", 
                                            "r11", "r12", "r13", "r14", "pc"};

            public String getScope() { return scope; }
            protected synchronized String getUniqueID(String suggestion) {
                Util.assert(i <= names.length);
                return names[i++];
            }
        };
        for (int i = 0; i < 16; i++) 
            reg[i] = new Temp(regtf);
    }

    public SAFrame() {
        mas = new DefaultAllocationStrategy(this);
        tf = Temp.tempFactory("global");
    }

    public boolean pointersAreLong() { return false; }

    /* soon to go away */
    public Temp RV() { return null; }

    /* soon to go away */
    public Temp RX() { return null; }

    public Temp FP() { return null; }

    public Temp[] registers() { return reg; }

    public Stm GC(TreeFactory tf, HCodeElement src) { return null; }

    public Exp mem_limit(TreeFactory tf, HCodeElement src) { return null; }

    public MEM next_ptr(TreeFactory tf, HCodeElement src) { return null; }

    public Stm out_of_memory(TreeFactory tf, HCodeElement src) { return null; }

    public Exp malloc(Exp size) { return mas.malloc(size); }

    public OffsetMap offsetMap() { return null; }

    public TempFactory tempFactory() { return tf; }
}
