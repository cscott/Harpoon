// Frame.java, created Wed Jun 28 22:25:27 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.PreciseC;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Backend.Generic.GCInfo;
import harpoon.Backend.Analysis.BasicGCInfo;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.Util.Util;

/**
 * <code>Frame</code> contains the machine/runtime information necessary
 * to compile for the preciseC backend.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Frame.java,v 1.1.2.1 2000-06-29 02:35:24 cananian Exp $
 */
public class Frame extends harpoon.Backend.Generic.Frame {
    private final harpoon.Backend.Generic.Runtime   runtime;
    private final Linker linker;
    // HACK: this should really be a command-line parameter.
    private final static String alloc_strategy =
	System.getProperty("harpoon.alloc.strategy", "malloc");
    private final static boolean pointersAreLong =
	System.getProperty("harpoon.frame.pointers", "short")
	.equalsIgnoreCase("long");
    private final static boolean is_elf = true;

    /** Creates a <code>Frame</code>. */
    public Frame(HMethod main, ClassHierarchy ch, CallGraph cg) { 
	super();
	linker = main.getDeclaringClass().getLinker();
	System.out.println("AllocationStrategy: "+alloc_strategy);
	harpoon.Backend.Runtime1.AllocationStrategy as = // pick strategy
	    alloc_strategy.equalsIgnoreCase("nifty") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.NiftyAllocationStrategy(this) :
	    alloc_strategy.equalsIgnoreCase("bdw") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.BDWAllocationStrategy(this) :
	    alloc_strategy.equalsIgnoreCase("sp") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.SPAllocationStrategy(this) :	    
	    alloc_strategy.equalsIgnoreCase("precise") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.MallocAllocationStrategy
	    (this, "precise_malloc") :
	    // default, "malloc" strategy.
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.MallocAllocationStrategy(this,
								  "malloc");
	runtime = new harpoon.Backend.Runtime1.Runtime(this, as, main, ch, cg,
						       !is_elf);

    }
    public Linker getLinker() { return linker; }
    public boolean pointersAreLong() { return pointersAreLong; }
    public harpoon.Backend.Generic.CodeGen getCodeGen() { return null; }
    public harpoon.Backend.Generic.Runtime getRuntime() { return runtime; }
    public harpoon.Backend.Generic.RegFileInfo getRegFileInfo(){ return null; }
    public harpoon.Backend.Generic.LocationFactory getLocationFactory() {
	return null;
    }
    public harpoon.Backend.Generic.InstrBuilder getInstrBuilder(){return null;}
    public harpoon.Backend.Generic.TempBuilder getTempBuilder(){ return null; }
    public harpoon.Backend.Generic.GCInfo getGCInfo() { return null; }
    public HCodeFactory getCodeFactory(HCodeFactory hcf) { return null; }
}
