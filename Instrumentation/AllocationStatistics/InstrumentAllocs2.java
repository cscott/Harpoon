// InstrumentAllocs2.java, created Fri Feb  7 21:40:28 2003 by salcianu
// Copyright (C) 2000  Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Instrumentation.AllocationStatistics;

import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.Temp.TempFactory;
import harpoon.Temp.Temp;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import net.cscott.jutil.WorkSet;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.DefaultAllocationInformationMap;
import harpoon.Analysis.ChainedAllocationProperties;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/** <code>InstrumentAllocs2</code> can be used together with
    <code>InstrumentedAllocationStrategy</code> to instrument the
    allocation sites from a program to record how many times each of
    them is executed, and the total amount of memory allocated at each
    site.  

    <p>
    Unlike <code>InstrumentAllocs</code> (which takes place at Quad
    level), <code>InstrumentAllocs2</code> operates at the Tree form
    level.  It changes the allocation properties of each allocation
    site such that the code generated for it in Tree form will contain
    a call to an instrumentation method, passing it both an unique
    identifier and the length of the memory allocated there.

    @author Alexandru Salcianu <salcianu@MIT.EDU>
    @version $Id: InstrumentAllocs2.java,v 1.5 2004-02-08 01:58:06 cananian Exp $ */
public class InstrumentAllocs2 extends MethodMutator
    implements java.io.Serializable {
    
    /** Creates a <code>InstrumentAllocs2</code> object.

	@param parent code factory providing the original,
	uninstrumented code

	@param main main method of the program

	@param linker linker for the analyzed program

	@param an provides a globally unique integer ID for each
	allocation site from the program */
    public InstrumentAllocs2(HCodeFactory parent, HMethod main,
			     Linker linker, AllocationNumbering an) {
	super(parent);

	assert
	    parent.getCodeName().equals(QuadNoSSA.codename) :
	    "InstrumentAllocs works only with QuadNoSSA";

	this.main   = main;
	this.an     = an;

	hm_instr_exit = InstrumentAllocs.getMethod
	    (linker, "harpoon.Runtime.CounterSupport",
	     "exit", new HClass[0]);
	hm_orig_exit = InstrumentAllocs.getMethod
	    (linker, "java.lang.System", "exit", "(I)V");
    }

    private HMethod main;
    private AllocationNumbering an;

    private HMethod hm_instr_exit;
    private HMethod hm_orig_exit;

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
       
	// we avoid instrumenting the instrumentation itself !
	if (hc.getMethod().getDeclaringClass().getName().
	    equals("harpoon.Runtime.CounterSupport"))
	    return hc;

	InstrumentAllocs.instrumentProgramTermination
	    (hc, hm_orig_exit, hm_instr_exit);

	try {
	    updateAllocationProperties((Code) hc, input.ancestorElementMap());
	} catch(UnknownAllocationSiteError e) {
	    // ignore: code called only by the instrumentation
	}
 
	if (hc.getMethod().equals(main))
	    InstrumentAllocs.treatMainMethod(hc, hm_instr_exit);

	return hc;
    }


    private void updateAllocationProperties(Code hc, Map ancestor) {
	AllocationInformationMap aim = 
	    (AllocationInformationMap) hc.getAllocationInformation();
	// Make sure there is an AllocationInfomationMap for "code"
	if(aim == null) {
	    aim = new DefaultAllocationInformationMap();
	    hc.setAllocationInformation(aim);
	}

	for(Iterator it = hc.selectAllocations().iterator(); it.hasNext(); ) {
	    Quad alloc = (Quad)it.next();
	    final int allocID = an.allocID((Quad) ancestor.get(alloc));
	    AllocationProperties formerAP = aim.query(alloc);
	    aim.associate(alloc,
			  new ChainedAllocationProperties(formerAP) {
		private final int index = allocID;
		public int getUniqueID() { return index; }
	    });
	}
    }
}
