// SPAllocationStrategy.java, created Fri Jun  2 13:51:38 2000 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime;
import harpoon.Backend.Generic.Runtime.TreeBuilder;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.Loader;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.MOVE;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.SEQ;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
/**
 * <code>SPAllocationStrategy</code> implements a "semi-precise"
 * allocation strategy by providing the BDW collector with more 
 * precise information about pointer locations.
 * 
 * @author  Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: SPAllocationStrategy.java,v 1.2 2002-02-25 21:02:20 cananian Exp $
 */
public class SPAllocationStrategy extends MallocAllocationStrategy {
    /** Creates a <code>SPAllocationStrategy</code>. */
    public SPAllocationStrategy(Frame f) {
	super(f, "__dont_use__");
    }
    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	String func;
	if (ap.hasInteriorPointers()) {
	    if (ap.actualClass().isArray()) { // array with interior pointers
		Util.assert(!ap.actualClass().getComponentType().
			    isPrimitive());
		func = "SP_malloc_array";
	    } else { // non-array
		func = "SP_malloc";
	    }
	} else {
	    if (ap.actualClass().isArray()) // array with no interior pointers
		Util.assert(ap.actualClass().getComponentType().
			    isPrimitive());
	    func = "SP_malloc_atomic";
	}
	Runtime rt = frame.getRuntime();
	return buildAllocCall(tf, source, dg, ap, func, length,
			      new ExpList(new NAME(tf, source, rt.getNameMap().label
						   (ap.actualClass(),
						    "classinfo")), null));
    }
}
