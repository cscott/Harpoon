// Frame.java, created by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.StrongARM;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Backend.Allocation.AllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationStrategy;
import harpoon.Backend.Allocation.DefaultAllocationInfo;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.TreeFactory;
import harpoon.ClassFile.HCodeElement;

/**
 * <code>StrongARM.Frame</code> contains the machine-dependant
 * information necessary to compile for the StrongARM processor.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Frame.java,v 1.1.2.1 1999-02-16 21:14:40 andyb Exp $
 */
public class Frame extends harpoon.Backend.Generic.Frame 
  implements DefaultAllocationInfo {
    private static Temp[] reg = new Temp[16];
    /** TempFactory used to create register temps */
    private TempFactory regtf;
    /** TempFactory used to create global temps needed for memory
     *  allocation and garbage collection */
    private TempFactory tf;
    private AllocationStrategy mas;

    {
        //regtf = new TempFactory() {
        //    /* create a temp factory here which will
        //     * generate things like r0, r1, fp, yadda yadda yadda */
        //};
    }

    Frame() {
        mas = new DefaultAllocationStrategy(this);
        tf = Temp.tempFactory("global");
    }

    public boolean pointersAreLong() { return false; }

    /* not yet implemented */
    public Temp RV() { return null; }

    public Temp FP() { return null; }

    public Temp[] registers() { return null; }

    public Stm GC(TreeFactory tf, HCodeElement src) { return null; }

    public Exp mem_limit(TreeFactory tf, HCodeElement src) { return null; }

    public MEM next_ptr(TreeFactory tf, HCodeElement src) { return null; }

    public Stm out_of_memory(TreeFactory tf, HCodeElement src) { return null; }

    public Exp malloc(Exp size) { return mas.malloc(size); }

    public OffsetMap offsetMap() { return null; }

    public TempFactory tempFactory() { return tf; }
}
