// HeapStatsAllocationStrategy.java, created by cananian
// Copyright (C) 2002 C. Scott Ananian <cananian@alumni.princeton.edu>
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
import harpoon.IR.Tree.CONST;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
/**
 * <code>HeapStatsAllocationStrategy</code> calls the appropriate C functions
 * in the --with-live-heap-statistics runtime code to collect statistics
 * for objects and arrays allocated.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HeapStatsAllocationStrategy.java,v 1.2 2002-04-10 03:03:20 cananian Exp $
 */
public class HeapStatsAllocationStrategy extends MallocAllocationStrategy {

    /** Creates a <code>HeapStatsAllocationStrategy</code>.
     */
    public HeapStatsAllocationStrategy(Frame f) { super(f, "heapstats_alloc"); }

    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	return buildAllocCall(tf, source, dg, ap, "heapstats_alloc2", length,
			      new ExpList
			      (new CONST(tf, source,
					 ap.actualClass().isArray() ? 1 : 0),
			       null));
    }
}
