// MallocAllocationStrategy.java, created Wed Oct 13 13:01:18 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Runtime1;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.ExpList;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.ESEQ;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.NATIVECALL;
import harpoon.IR.Tree.TEMP;
import harpoon.IR.Tree.Type;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
/**
 * <code>MallocAllocationStrategy</code> calls a C function with the
 * same prototype as <code>malloc()</code> to do the allocation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MallocAllocationStrategy.java,v 1.1.2.2 2000-02-16 06:18:09 cananian Exp $
 */
public class MallocAllocationStrategy extends AllocationStrategy {
    final Label func;
    /** Creates a <code>MallocAllocationStrategy</code>.
     * @param funcname The name of the function to call to do the allocation.
     */
    public MallocAllocationStrategy(String funcname) {
        this.func = new Label(funcname);
    }
    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			Exp length) {
	Temp Tret = new Temp(tf.tempFactory(), "ma");
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
	      new ExpList(length, null)),
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
