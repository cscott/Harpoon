// PreallocAllocationStrategy.java, created Thu Nov 28 20:01:25 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import harpoon.Backend.Runtime1.MallocAllocationStrategy;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.Type;
import harpoon.IR.Quads.Quad;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Map;

/** <code>PreallocAllocationStrategy</code> is the allocation strategy
    for the Static Memory Preallocation Optimization (via Ovy's
    Incompatibility Analysis).  When asked to generate code that
    allocates memory for an allocation site, it behaves as follows:

    <ul>

    <li> If the <code>AllocationProperties</code> object for that
    allocation site indicates that the memory space has been
    preallocated, then generate code that simply reads the static
    variable that points to the pre-allocated chunk of memory.

    <li> Otherwise, use the standard allocation from
    <code>MallocAllocatonStrategy</code>.

    </ul>
 
    @author  Alexandru Salcianu <salcianu@MIT.EDU>
    @version $Id: PreallocAllocationStrategy.java,v 1.6 2003-03-03 23:28:58 salcianu Exp $ */
public class PreallocAllocationStrategy extends MallocAllocationStrategy {
    
    /** Creates a <code>PreallocAllocationStrategy</code>. */
    public PreallocAllocationStrategy(Frame f) { 
	super(f, "GC_malloc");
    }

    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {

	Label label = ap.getLabelOfPtrToMemoryChunk();

	if(label != null) {
	    Exp pointer_expr =
		new MEM(tf, source,
			Type.POINTER, new NAME(tf, source, label));
	    dg.putType(pointer_expr, HClass.Void);

	    return pointer_expr;
	}
	else
	    return super.memAlloc(tf, source, dg, ap, length);
    }
}
