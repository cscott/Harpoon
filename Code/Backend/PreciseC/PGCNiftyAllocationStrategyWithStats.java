// PGCNiftyAllocationStrategyWithStats.java, created Sat Nov 11 22:43:51 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.PreciseC;

import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
/**
 * <code>PGCNiftyAllocationStrategyWithStats</code> adds a callback
 * to properly update statistics after stack allocation.  This is
 * pretty pointless, because you could just fake the stack allocation
 * to get the statistics, but... Martin apparently wants it this way.
 * <p>
 * This code originally written by Brian; Scott rearranged it.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: PGCNiftyAllocationStrategyWithStats.java,v 1.2 2002-02-25 21:02:09 cananian Exp $
 */
public class PGCNiftyAllocationStrategyWithStats
     extends PGCNiftyAllocationStrategy {
    
    /** Creates a <code>PGCNiftyAllocationStrategyWithStats</code>. */
    public PGCNiftyAllocationStrategyWithStats(Frame f) { super(f); }
    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	if (ap.canBeStackAllocated()) {
	    Label func = new Label(frame.getRuntime().getNameMap()
				   .c_function_name("NSTK_update_stats"));
	    Temp Tlen = new Temp(tf.tempFactory(), "len");
	    return new ESEQ
		(tf, source, 
		 new SEQ
		 (tf, source,
		  new MOVE
		  (tf, source,
		   new TEMP(tf, source, Type.INT, Tlen),
		   length),
		  new NATIVECALL
		  (tf, source,
		   null/*no retval*/,
		   (NAME)
		   DECLARE(dg, HClass.Void/*some random c function*/,
		   new NAME(tf, source, func)),
		   new ExpList(new TEMP(tf, source, Type.INT, Tlen), null))),
		 super.memAlloc(tf, source, dg, ap,
				new TEMP(tf, source, Type.INT, Tlen)));
	} else
	    return super.memAlloc(tf, source, dg, ap, length);
    }
}
