// BDWAllocationStrategy.java, created Mon Apr 3 20:00:00 2000 by cananian
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
/**
 * <code>BDWAllocationStrategy</code> calls the appropriate C functions
 * in the Boehm-Demers-Weiser conservative garbage collector library
 * to do the allocation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: BDWAllocationStrategy.java,v 1.2 2002-02-25 21:02:19 cananian Exp $
 */
public class BDWAllocationStrategy extends MallocAllocationStrategy {

    /** Creates a <code>BDWAllocationStrategy</code>.
     */
    public BDWAllocationStrategy(Frame f) { super(f, "GC_malloc"); }

    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	return buildAllocCall(tf, source, dg, ap,
			      ap.hasInteriorPointers() ?
			      "GC_malloc" : "GC_malloc_atomic", length, null);
    }
}
