// Frame.java, created Tue Feb 16 22:29:44 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.MIPS;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.CallGraph;
import harpoon.Backend.Generic.GCInfo;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.Util.Util;
import java.util.Map;
import java.util.Set;

/**
 * <code>Frame</code> contains the machine-dependant
 * information necessary to compile for the MIPS processor.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @author  Felix Klock <pnkfelix@mit.edu>
 * @version $Id: Frame.java,v 1.1.2.11 2001-06-08 00:47:09 witchel Exp $
 */
public class Frame extends harpoon.Backend.Generic.Frame {
   private final harpoon.Backend.Generic.Runtime   runtime;
   private final RegFileInfo regFileInfo; 
    private final InstrBuilder instrBuilder;
    private final CodeGen codegen;
    private final TempBuilder tempBuilder;
    private final Linker linker;
    private GCInfo gcInfo; // should really be final
   private String type = "";
   private Map noTagCheck;
   private Set usedDANum;

    // HACK: this should really be a command-line parameter.
    private final static String alloc_strategy =
	System.getProperty("harpoon.alloc.strategy", "malloc");
    private final static boolean is_elf =
	System.getProperty("harpoon.target.elf", "yes")
	.equalsIgnoreCase("yes");


   public Frame(HMethod main, ClassHierarchy ch, CallGraph cg) {
	super();
	linker = main.getDeclaringClass().getLinker();
	regFileInfo = new RegFileInfo();
    noTagCheck = null;
	
	harpoon.Backend.Runtime1.AllocationStrategy as = // pick strategy
	    alloc_strategy.equalsIgnoreCase("nifty") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.NiftyAllocationStrategy(this) :
	    alloc_strategy.equalsIgnoreCase("bdw") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.BDWAllocationStrategy(this) :
	    // default, "malloc" strategy.
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.MallocAllocationStrategy(this,
								  "malloc");
	runtime = new harpoon.Backend.Runtime1.Runtime(this, as, main, ch, cg,
						       !is_elf);
	// FSK: CodeGen ctor needs regFileInfo set in 'this' Frame
	// [and it also needs nameMap out of Runtime --CSA], so
	// be careful about ordering of constructions.
	codegen = new CodeGen(this, is_elf);

	instrBuilder = new InstrBuilder(regFileInfo, this);
	tempBuilder = new TempBuilder();
    }

    public Frame(HMethod main, ClassHierarchy ch, CallGraph cg, GCInfo gcInfo)
    {
	this(main, ch, cg);
	this.gcInfo = gcInfo;
    }
   public Frame(HMethod main, ClassHierarchy ch, CallGraph cg, String type) {
	super();
	this.type = type;

	linker = main.getDeclaringClass().getLinker();
	regFileInfo = new RegFileInfo();
    noTagCheck = null;
	
	harpoon.Backend.Runtime1.AllocationStrategy as = // pick strategy
	    alloc_strategy.equalsIgnoreCase("nifty") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.NiftyAllocationStrategy(this) :
	    alloc_strategy.equalsIgnoreCase("bdw") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.BDWAllocationStrategy(this) :
	    // default, "malloc" strategy.
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.MallocAllocationStrategy(this,
								  "malloc");
	runtime = new harpoon.Backend.Runtime1.Runtime(this, as, main, ch, cg,
						       !is_elf);
	// FSK: CodeGen ctor needs regFileInfo set in 'this' Frame
	// [and it also needs nameMap out of Runtime --CSA], so
	// be careful about ordering of constructions.
	codegen = new CodeGen(this, is_elf);

	instrBuilder = new InstrBuilder(regFileInfo, this);
	tempBuilder = new TempBuilder();
   }

    public String getType() { return type; }

    public Linker getLinker() { return linker; }
   
    public boolean pointersAreLong() { return false; }

   public void setNoTagCheckMap(Map hm) {
      noTagCheck = hm;
   }
   public void setUsedDANum(Set hs) {
      usedDANum = hs;
   }
   public Set getUsedDANum() {
      return usedDANum;
   }
   public boolean memAccessNoTagCheck(HCodeElement hce) {
      // XXX This interface is superceeded by daNum, but I don't want
      // to rip it out yet 
      return false;
      //if(noTagCheck == null) return false;
      // return noTagCheck.get(hce) != null;
   }
   public Object daNum(HCodeElement hce) {
      if(noTagCheck == null) return null;
      return noTagCheck.get(hce);
   }

    /** Returns a <code>MIPS.CodeGen</code>. 
	Since no state is maintained in the returned
	<code>MIPS.CodeGen</code>, the same one is returned on
	every call to this method.
     */
    public harpoon.Backend.Generic.CodeGen getCodeGen() { 
	return codegen;
    }

    public harpoon.Backend.Generic.Runtime getRuntime() {
	return runtime;
    }

    public harpoon.Backend.Generic.RegFileInfo getRegFileInfo() { 
	return regFileInfo; 
    }

    public harpoon.Backend.Generic.LocationFactory getLocationFactory() {
	// regfileinfo holds the location factory implementation for this frame
	return regFileInfo;
    }

    public harpoon.Backend.Generic.InstrBuilder getInstrBuilder() { 
	return instrBuilder; 
    }
    public harpoon.Backend.Generic.TempBuilder getTempBuilder() {
	return tempBuilder;
    }
    public harpoon.Backend.Generic.GCInfo getGCInfo() {
	return gcInfo;
    }
    public HCodeFactory getCodeFactory(HCodeFactory hcf) {
	return Code.codeFactory(hcf, this);
    }
}
