// PreallocAllocationStrategy.java, created Thu Nov 28 20:01:25 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import harpoon.Backend.Runtime1.MallocAllocationStrategy;
import harpoon.Analysis.Maps.AllocationInformation.AllocationProperties;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Tree.DerivationGenerator;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.TreeFactory;
import harpoon.IR.Tree.NAME;
import harpoon.IR.Tree.MEM;
import harpoon.IR.Tree.Type;

import harpoon.IR.Quads.Quad;

import harpoon.Analysis.PointerAnalysis.Debug;

/** <code>PreallocAllocationStrategy</code> is the allocation strategy
    for the Static Memory Preallocation Optimization (via Ovy's
    Incompatibility Analysis).  When asked to generate code that
    allocates memory for an allocation site, it behaves as follows:

    <ul>

    <li> If the <code>AllocationProperties</code> object for that
    allocation site indicates that the memory space has been
    preallocated, then generate code that simply reads the static
    field that points to the pre-allocated chunk of memory.

    <li> Otherwise, use the standard allocation from
    <code>MallocAllocatonStrategy</code>.

    </ul>
 
    @author  Alexandru Salcianu <salcianu@MIT.EDU>
    @version $Id: PreallocAllocationStrategy.java,v 1.3 2003-01-07 15:05:02 salcianu Exp $ */
public class PreallocAllocationStrategy extends MallocAllocationStrategy {
    
    /** Creates a <code>PreallocAllocationStrategy</code>. */
    public PreallocAllocationStrategy(Frame f) { 
	super(f, "GC_malloc");
    }

    public Exp memAlloc(TreeFactory tf, HCodeElement source,
			DerivationGenerator dg,
			AllocationProperties ap,
			Exp length) {
	HField hfield = ap.getMemoryChunkField();
	// TODO: cut out the second part of the test!
	// STATUS: we cannot do this yet, due to an incompleteness in
	// the IncompatibilityAnalysis
	if((hfield != null) && extraCond((Quad) source, ap.actualClass())) {
	    /*
	    System.out.println
		("I was called for " + 
		 harpoon.Analysis.PointerAnalysis.Debug.code2str(source));
	    */
	    return
		DECLARE
		(dg, HClass.Void,
		 new MEM
		 (tf, source, Type.POINTER,
		  new NAME
		  (tf, source,
		   frame.getRuntime().getNameMap().label(hfield))));
	}
	else return super.memAlloc(tf, source, dg, ap, length);
    }

    public static boolean extraCond(Quad q, HClass hclass) {
	String className = hclass.getName();
	// hack to go around some missing things in Ovy's
	// IncompatibilityAnalysis: IA analyzes only the program that
	// is rooted in the main method (no initialization code
	// considered; that code happen to allocate a PrintStream, and
	// some connected objects with it ...)
	// TODO: properly implement Ovy's stuff
	if(className.equals("java.io.BufferedWriter") ||
	   className.equals("java.io.OutputStreamWriter")) {
	    HClass hdeclc = q.getFactory().getMethod().getDeclaringClass();
	    boolean result = ! hdeclc.getName().equals("java.io.PrintStream");
	    if(!result)
		System.out.println
		    ("\nPreallocAS: false for\t" + Debug.code2str(q) +
		     "\tin\t" + q.getFactory().getMethod());
	    return result;
	}
	else
	    return true;
    }
}
