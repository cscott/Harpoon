// PGCNiftyAllocationStrategy.java, created Tue Jul 11 23:43:35 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.PreciseC;

import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HClass;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.ESEQ;
import harpoon.Temp.Label;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;

import harpoon.IR.Tree.TreeFactory;
/**
 * <code>PGCNiftyAllocationStrategy</code> uses <code>alloca()</code>
 * instead of <code>NSTK_alloc</code> to do stack allocation.  Other
 * than this, it is identical to <code>NiftyAllocationStrategy</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PGCNiftyAllocationStrategy.java,v 1.1.2.3 2000-11-11 18:16:38 bdemsky Exp $
 */
public class PGCNiftyAllocationStrategy
    extends harpoon.Backend.Runtime1.NiftyAllocationStrategy {
    boolean dostats;
    /** Creates a <code>PGCNiftyAllocationStrategy</code>. */
    public PGCNiftyAllocationStrategy(Frame f,boolean dostats) { 
	super(f); 
	this.dostats=dostats;
    }
    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	if (ap.canBeStackAllocated()) {
	    if (dostats)
		return new ESEQ(tf, source, new NATIVECALL
				(tf, source,
				 null,
				 (NAME)
				 DECLARE(dg, HClass.Void/*some random c function*/,
					 new NAME(tf, source, new Label(frame.getRuntime().nameMap.c_function_name("NSTK2_malloc")))),
				 new ExpList(length, null)), buildAllocCall(tf, source, dg, ap, "alloca", length, null));
	    else
		return buildAllocCall(tf, source, dg, ap, "alloca", length, null);
	} else
	    return super.memAlloc(tf, source, dg, ap, length);
    }
}
