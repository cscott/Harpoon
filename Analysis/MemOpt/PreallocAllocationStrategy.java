// PreallocAllocationStrategy.java, created Thu Nov 28 20:01:25 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import harpoon.Backend.Runtime1.MallocAllocationStrategy;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.Type;


/** <code>PreallocAllocationStrategy</code> is the allocation strategy
    for the Static Memory Preallocation Optimization (via Ovy's
    Incompatibility Analysis).  When asked to generate code that
    allocates memory for an allocation site, it behaves as follows:

    <ul>

    <li> If the <code>AllocationProperties</code> object for that
    allocation site indicates that the memory space has been
    preallocated, then generate code that simply reads the static
    field that points to the pre-allocated chunk of memory.

    <li> Otherwise, use the standard allocation from
    <code>MallocAllocatonStrategy</code>.

    </ul>
 
    @author  Alexandru Salcianu <salcianu@MIT.EDU>
    @version $Id: PreallocAllocationStrategy.java,v 1.1 2002-11-29 20:43:43 salcianu Exp $ */
public class PreallocAllocationStrategy extends MallocAllocationStrategy {
    
    /** Creates a <code>PreallocAllocationStrategy</code>. */
    public PreallocAllocationStrategy(Frame f) { 
	super(f, "GC_malloc");
    }

    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	HField hfield = ap.getMemoryChunkField();
	if(hfield != null) {

	    System.out.println
		("I was called for " + 
		 harpoon.Analysis.PointerAnalysis.Debug.code2str(source));

	    return
		DECLARE
		(dg, HClass.Void,
		 new MEM
		 (tf, source, Type.POINTER,
		  new NAME
		  (tf, source,
		   frame.getRuntime().getNameMap().label(hfield))));
	}
	else return super.memAlloc(tf, source, dg, ap, length);
    }
}
