// SPAllocationStrategy.java, created Fri Jun  2 13:51:38 2000 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@tesuji.lcs.mit.edu>
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
import harpoon.IR.Tree.NATIVECALL;
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
 * @author  Karen K. Zee <kkz@tesuji.lcs.mit.edu>
 * @version $Id: SPAllocationStrategy.java,v 1.1.2.2 2000-06-08 20:07:24 kkz Exp $
 */
public class SPAllocationStrategy extends AllocationStrategy {
    final Frame frame;
    /** Creates a <code>SPAllocationStrategy</code>. */
    public SPAllocationStrategy(Frame f) {
        this.frame = f;
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
	return buildAllocCall(tf, source, dg, ap, func, length, null); 
    }
    protected Exp buildAllocCall(TreeFactory tf, HCodeElement source,
				 DerivationGenerator dg,
				 AllocationProperties ap,
				 String funcname, Exp length, ExpList addlArgs)
    {
	Runtime rt = frame.getRuntime();
	Label func = new Label(frame.getRuntime().nameMap
			       .c_function_name(funcname));
	Temp Tret = new Temp(tf.tempFactory(), "ma");
	NAME clazPtr = new NAME(tf, source, rt.nameMap.label
				(ap.actualClass(), "classinfo"));
	return new ESEQ
	    (tf, source, 
	     new NATIVECALL
	     (tf, source,
	      (TEMP)
	      DECLARE(dg, HClass.Void/*not an obj yet, just memory*/, Tret,
		      new TEMP(tf, source, Type.POINTER, Tret)),
	      (NAME)
	      DECLARE(dg, HClass.Void/*some random c function*/,
		      new NAME(tf, source, func)),
	      new ExpList(length, new ExpList(clazPtr, addlArgs))),
	     DECLARE(dg, HClass.Void/*not an obj yet, just memory*/, Tret,
		     new TEMP(tf, source, Type.POINTER, Tret)));
    }
    private static Exp DECLARE(DerivationGenerator dg, HClass hc, Exp exp) {
	if (dg!=null) dg.putType(exp, hc);
	return exp;
    }
    private static Exp DECLARE(DerivationGenerator dg, HClass hc, Temp t,
			       Exp exp) {
	if (dg!=null) dg.putTypeAndTemp(exp, hc, t);
	return exp;
    }
}
