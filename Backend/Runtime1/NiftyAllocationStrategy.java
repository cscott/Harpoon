// NiftyAllocationStrategy.java, created Mon Apr 3 20:09:00 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
/**
 * <code>NiftyAllocationStrategy</code> implements stack- and
 * thread-clustered-allocation strategies.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NiftyAllocationStrategy.java,v 1.3 2002-02-26 22:44:31 cananian Exp $
 */
public class NiftyAllocationStrategy extends MallocAllocationStrategy {

    /** Creates a <code>NiftyAllocationStrategy</code>.
     */
    public NiftyAllocationStrategy(Frame f) { super(f, "GC_malloc"); }

    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	String funcname = "NGBL_malloc";
	ExpList addlArgs = null;
	if (!ap.hasInteriorPointers()) funcname = "NGBL_malloc_atomic";
	if (ap.canBeStackAllocated()) funcname = "NSTK_malloc";
	if (ap.canBeThreadAllocated())funcname = "NTHR_malloc";
	if (ap.makeHeap())
	    funcname = ap.canBeThreadAllocated() ?
		"NTHR_malloc_with_heap" : "NGBL_malloc_with_heap";
	if (ap.allocationHeap()!=null) {
	    // from the AllocationProperties interface specification...
	    Util.ASSERT(ap.canBeThreadAllocated() && !ap.makeHeap());
	    funcname = "NTHR_malloc_other";
	    // this is somewhat bogus, since the heap-association object is
	    // most certainly an *object*, *not* Void -- but it can't hurt
	    // since the association object will be kept live by other means
	    // anyway.
	    Temp aht = ap.allocationHeap();
	    TEMP ahT = new TEMP(tf, source, Type.POINTER, aht);
	    if (dg!=null) dg.putTypeAndTemp(ahT, HClass.Void, aht);
	    addlArgs = new ExpList(ahT, addlArgs);
	}
	return buildAllocCall(tf, source, dg, ap, funcname, length, addlArgs);
    }
}
