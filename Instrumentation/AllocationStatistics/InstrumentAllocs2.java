// InstrumentAllocs2.java, created Fri Feb  7 21:40:28 2003 by salcianu
// Copyright (C) 2000  Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Instrumentation.AllocationStatistics;

import harpoon.Analysis.Transformation.MethodMutator;
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
import harpoon.Util.Collections.WorkSet;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.DefaultAllocationInformationMap;
import harpoon.Analysis.ChainedAllocationProperties;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * <code>InstrumentAllocs2</code>
 * 
 * @author Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: InstrumentAllocs2.java,v 1.1 2003-02-08 23:22:39 salcianu Exp $
 */
public class InstrumentAllocs2 extends MethodMutator
    implements java.io.Serializable {
    
    /** Creates a <code>InstrumentAllocs2</code>. */
    public InstrumentAllocs2(HCodeFactory parent, HMethod main,
			     Linker linker, AllocationNumbering an) {
	super(parent);

	this.parent = parent;
	this.main   = main;
	this.an     = an;

	hm_instr_exit = InstrumentAllocs.getMethod
	    (linker, "harpoon.Runtime.CounterSupport",
	     "exit", new HClass[0]);
	hm_orig_exit = InstrumentAllocs.getMethod
	    (linker, "java.lang.System", "exit", "(I)V");
    }

    private HMethod main;
    private HCodeFactory parent;
    private AllocationNumbering an;

    private HMethod hm_instr_exit;
    private HMethod hm_orig_exit;

    public HCodeFactory parent() {
	return parent;
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
       
	// we avoid instrumenting the instrumentation itself !
	if (hc.getMethod().getDeclaringClass().getName().
	    equals("harpoon.Runtime.CounterSupport"))
	    return hc;

	InstrumentAllocs.instrumentProgramTermination
	    (hc, hm_orig_exit, hm_instr_exit);

	updateAllocationProperties((Code) hc, input.ancestorElementMap());
 
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
	    Quad alloc = (Quad) it.next();
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
