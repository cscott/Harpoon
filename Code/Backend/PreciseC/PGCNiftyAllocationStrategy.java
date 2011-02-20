// PGCNiftyAllocationStrategy.java, created Tue Jul 11 23:43:35 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.PreciseC;

import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.TreeFactory;
/**
 * <code>PGCNiftyAllocationStrategy</code> uses <code>alloca()</code>
 * instead of <code>NSTK_alloc</code> to do stack allocation.  Other
 * than this, it is identical to <code>NiftyAllocationStrategy</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PGCNiftyAllocationStrategy.java,v 1.2 2002-02-25 21:02:09 cananian Exp $
 */
public class PGCNiftyAllocationStrategy
    extends harpoon.Backend.Runtime1.NiftyAllocationStrategy {
    
    /** Creates a <code>PGCNiftyAllocationStrategy</code>. */
    public PGCNiftyAllocationStrategy(Frame f) { super(f); }
    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	if (ap.canBeStackAllocated())
	    return buildAllocCall(tf, source, dg, ap, "alloca", length, null);
	else
	    return super.memAlloc(tf, source, dg, ap, length);
    }
}
