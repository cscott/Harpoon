// Frame.java, created Wed Jun 28 22:25:27 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.PreciseC;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.CallGraph;
import harpoon.Analysis.Realtime.Realtime;
import harpoon.Analysis.Realtime.RealtimeRuntime;
import harpoon.Analysis.MemOpt.PreallocOpt;
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

import harpoon.Backend.Runtime1.AllocationStrategy;
import harpoon.Backend.Runtime1.AllocationStrategyFactory;

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
 * @version $Id: Frame.java,v 1.8 2003-02-11 21:49:14 salcianu Exp $
 */
public class Frame extends harpoon.Backend.Generic.Frame {
    private final harpoon.Backend.Generic.Runtime   runtime;
    private final Linker linker;
    private final static boolean pointersAreLong =
	System.getProperty("harpoon.frame.pointers", "short")
	.equalsIgnoreCase("long");
    private final static boolean is_elf = true;

    public Frame(HMethod main) {
	this(main, null);
    }

    /** Creates a <code>Frame</code>.

	@param main main method of the program

	@param asFact Factory providing the appropriate allocation
	strategy.  If <code>null</code>, we use the default,
	&quot;malloc&quot; based allocation strategy. */
    public Frame(HMethod main, AllocationStrategyFactory asFact) {
	super();
	linker = main.getDeclaringClass().getLinker();

	// get an appropriate allocation strategy
	AllocationStrategy as = null;
	if(asFact != null)
	    as = asFact.getAllocationStrategy(this);
	else {
	    System.out.print("Allocation strategy: malloc (default)");
	    // default, "malloc" allocation strategy
	    as = new harpoon.Backend.Runtime1.MallocAllocationStrategy
		(this,
		 System.getProperty("harpoon.alloc.func", "malloc"));
	}

	harpoon.Backend.Generic.Runtime m_runtime =
	    Realtime.REALTIME_JAVA ?
	    new RealtimeRuntime(this, as, main, !is_elf) :
	    new harpoon.Backend.Runtime1.Runtime(this, as, main, !is_elf);
	if (!System.getProperty("harpoon.runtime","1").equals("1")) try {
	    Class c;
	    try {
		// try abbreviated name first
		c = Class.forName
		("harpoon.Backend.Runtime"+
		 System.getProperty("harpoon.runtime","1")+
		 ".Runtime");
	    } catch (ClassNotFoundException e) {
		// try full name
		c = Class.forName(System.getProperty("harpoon.runtime"));
	    }
	    java.lang.reflect.Constructor cc = c.getConstructor(new Class[] {
		Class.forName("harpoon.Backend.Generic.Frame"),
		Class.forName("harpoon.Backend.Runtime1.AllocationStrategy"),
		Class.forName("harpoon.ClassFile.HMethod"),
		Boolean.TYPE });
	    m_runtime = (harpoon.Backend.Generic.Runtime)
		cc.newInstance(new Object[] { this, as, main,
					      new Boolean(!is_elf) });
	} catch (Throwable t) {
	    throw new RuntimeException("Can't use specified runtime: "+t);
	}
	runtime = m_runtime;
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
	    assert f==Frame.this;
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
