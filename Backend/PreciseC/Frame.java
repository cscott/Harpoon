// Frame.java, created Wed Jun 28 22:25:27 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.PreciseC;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.CallGraph;
import harpoon.Analysis.Realtime.Realtime;
import harpoon.Analysis.Realtime.RealtimeRuntime;
import harpoon.Backend.Generic.GCInfo;
import harpoon.Backend.Generic.LocationFactory;
import harpoon.Backend.Analysis.BasicGCInfo;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HData;
import harpoon.ClassFile.HDataElement;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Tree.ALIGN;
import harpoon.IR.Tree.DATUM;
import harpoon.IR.Tree.Data;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.LABEL;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEGMENT;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Default;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>Frame</code> contains the machine/runtime information necessary
 * to compile for the preciseC backend.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Frame.java,v 1.1.2.13 2001-07-10 22:49:37 cananian Exp $
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
    public Frame(HMethod main) {
	super();
	linker = main.getDeclaringClass().getLinker();
	System.out.println("AllocationStrategy: "+
			   ((Realtime.REAL_SCOPES&&Realtime.REALTIME_JAVA)? 
			    "RTJ":alloc_strategy));
	harpoon.Backend.Runtime1.AllocationStrategy as = // pick strategy
	    (Realtime.REAL_SCOPES&&Realtime.REALTIME_JAVA) ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Analysis.Realtime.RealtimeAllocationStrategy(this) :
	    alloc_strategy.equalsIgnoreCase("nifty") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new PGCNiftyAllocationStrategy(this) :
	    alloc_strategy.equalsIgnoreCase("niftystats") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new PGCNiftyAllocationStrategyWithStats(this) :
	    alloc_strategy.equalsIgnoreCase("bdw") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.BDWAllocationStrategy(this) :
	    alloc_strategy.equalsIgnoreCase("sp") ?
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.SPAllocationStrategy(this) :	    
	    alloc_strategy.equalsIgnoreCase("precise") ?
	    new harpoon.Backend.Runtime1.MallocAllocationStrategy
	    (this, "precise_malloc") :
	    // default, "malloc" strategy.
	    (harpoon.Backend.Runtime1.AllocationStrategy)
	    new harpoon.Backend.Runtime1.MallocAllocationStrategy(this,
								  "malloc");
	runtime = Realtime.REALTIME_JAVA ?
	    new RealtimeRuntime(this, as, main, !is_elf) :
	    (System.getProperty("harpoon.runtime", "1").equals("2") ?
	     new harpoon.Backend.Runtime2.Runtime(this, as, main, !is_elf) :
	     new harpoon.Backend.Runtime1.Runtime(this, as, main, !is_elf));
    }
    public Linker getLinker() { return linker; }
    public boolean pointersAreLong() { return pointersAreLong; }
    public harpoon.Backend.Generic.CodeGen getCodeGen() { return null; }
    public harpoon.Backend.Generic.Runtime getRuntime() { return runtime; }
    public harpoon.Backend.Generic.RegFileInfo getRegFileInfo() {
	return regfileinfo;
    }
    public harpoon.Backend.Generic.LocationFactory getLocationFactory() {
	return locationfactory;
    }
    public harpoon.Backend.Generic.InstrBuilder getInstrBuilder(){return null;}
    public harpoon.Backend.Generic.TempBuilder getTempBuilder(){ return null; }
    public harpoon.Backend.Generic.GCInfo getGCInfo() { return null; }
    public HCodeFactory getCodeFactory(HCodeFactory hcf) { return null; }

    private harpoon.Backend.Generic.RegFileInfo regfileinfo =
	new harpoon.Backend.Generic.RegFileInfo() {
	    public Set calleeSave() { return null; }
	    public Set callerSave() { return null; }
	    public Set liveOnExit() { return null; }
	    public Temp[] getAllRegisters() { return null; }
	    public Temp[] getGeneralRegisters() { return null; }
	    public boolean isRegister(Temp t) { return false; }
	    public Iterator suggestRegAssignment(Temp t, Map regfile) {
		return null;
	    }
	};

    // simple location factory that allocates global variables for each loc.
    final List globals = new ArrayList();
    private LocationFactory locationfactory = new LocationFactory() {
	public LocationFactory.Location allocateLocation(final int type) {
	    final Label l = new Label();
	    globals.add(Default.pair(l, new Integer(type)));
	    return new Location() {
		public Exp makeAccessor(TreeFactory tf, HCodeElement source) {
		    return new MEM(tf, source, type, new NAME(tf, source, l));
		}
	    };
	}
	public HData makeLocationData(final harpoon.Backend.Generic.Frame f) {
	    Util.assert(f==Frame.this);
	    return new harpoon.IR.Tree.Data("location-data", f) {
		public HClass getHClass() { return null; }
		final HDataElement root;
		{   // initialize root:
		    List stmlist = new ArrayList();
		    stmlist.add(new SEGMENT(tf, null, SEGMENT.ZERO_DATA));
				
		    for (Iterator it=globals.iterator(); it.hasNext(); ) {
			List pair = (List) it.next();
			Label l = (Label) pair.get(0);
			int  ty = ((Integer)pair.get(1)).intValue();

			stmlist.add(new ALIGN(tf,null,8));
			stmlist.add(new LABEL(tf,null,l,true));
			stmlist.add(new DATUM(tf,null,ty));
		    }
		    this.root = (HDataElement) Stm.toStm(stmlist);
		}
		public HDataElement getRootElement() { return root; }
	    };
	}
    };
}
