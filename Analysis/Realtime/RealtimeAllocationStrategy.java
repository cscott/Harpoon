// RealtimeAllocationStrategy.java, created by wbeebee
// Copyright (C) 2001 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Realtime;

import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Runtime1.MallocAllocationStrategy;

import harpoon.ClassFile.HCodeElement;

import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Temp.Temp;

/**
 * <code>RealtimeAllocationStrategy</code> 
 *
 * @author Wes Beebee <<a href="mailto:wbeebee@mit.edu">wbeebee@mit.edu</a>>
 */

public class RealtimeAllocationStrategy extends MallocAllocationStrategy {
    /** Creates a <code>RealtimeAllocationStrategy</code>.
     */
    public RealtimeAllocationStrategy(Frame f) { super (f, "RTJ_malloc"); }

    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	String funcname = "RTJ_malloc";
	ExpList addlArgs = null;
	return buildAllocCall(tf, source, dg, ap, funcname, length, addlArgs);
    }
}
