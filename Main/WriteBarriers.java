// WriteBarriers.java, created Sat Apr 12 15:50:32 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Main;

import harpoon.ClassFile.CachingCodeFactory;

import harpoon.Analysis.Quads.QuadClassHierarchy;
import harpoon.Analysis.PreciseGC.WriteBarrierPrePass;
import harpoon.Analysis.PreciseGC.WriteBarrierStats;
import harpoon.Analysis.PreciseGC.WriteBarrierTreePass;

import harpoon.Util.Options.Option;

import java.util.List;
import java.util.LinkedList;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * <code>WriteBarriers</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: WriteBarriers.java,v 1.1 2003-04-17 00:19:29 salcianu Exp $
 */
public abstract class WriteBarriers {
    
    static boolean DYNAMICWBS = false;
    static boolean WRITEBARRIERS = false;
    static boolean WB_STATISTICS = false;
    static WriteBarrierStats writeBarrierStats = null;

    private static int WB_TRANSFORMS = 0; // no transformations by default
    static int wbOptLevel = 0; // no removals by default
    private static PrintStream wbps = null;

    
    private static List/*<Option>*/ getOptions() {
	List/*<Option>*/ opts = new LinkedList/*<Option>*/();
	
	opts.add(new Option("w", "", "<wbOptLevel>",
			    "Add write barriers (KKZ)") {
	    public void action() {
		WRITEBARRIERS = true;
		assert DYNAMICWBS == false :
		    "Only one of options -d and -w may be selected.";
		String arg = getOptionalArg(0);
		if (arg != null) {
		    try {
			wbOptLevel = Integer.parseInt(arg);
			assert wbOptLevel >= 0 && wbOptLevel <= 6;
		    } catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		    }
		}
	    }
	});
	
	opts.add(new Option("d", "", "<wbOptLevel>",
			    "Dynamic write barriers (KKZ)") {
	    public void action() {
		DYNAMICWBS = true;
		assert WRITEBARRIERS == false :
		    "Only one of options -d and -w may be selected.";
		String arg = getOptionalArg(0);
		if (arg != null) {
		    try {
			wbOptLevel = Integer.parseInt(arg);
			assert wbOptLevel == 0 || wbOptLevel == 1;
		    } catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		    }
		}
	    }
	});

	opts.add(new Option("x", "", "<outputFile>",
			    "Compile with write barrier statistics (KKZ)") {
	    public void action() {
		WB_STATISTICS = true;
		String arg = getOptionalArg(0);
		if (arg != null) {
		    try {
			wbps = 
			    new PrintStream(new FileOutputStream(arg, true));
			System.out.println
			    ("Writing write barrier list to file "+arg);
		    } catch (Exception e) {
			System.err.println(e);
			System.exit(1);
		    }
		}
	    }
	});

	opts.add(new Option("y", "", "<WBTransfLevel>",
			    "Compile with transformations (KKZ)") {
	    public void action() {
		String arg = getOptionalArg(0);
		if (arg != null) {
		    // select transformation level
		    WB_TRANSFORMS = Integer.parseInt(arg);
		    assert WB_TRANSFORMS >= 0 && WB_TRANSFORMS <= 3;
		} else {
		    // both transformations on
		    WB_TRANSFORMS = 3;
		}
	    }
	});

	return opts;
    }
    

    public static class WBQuadPass extends CompilerStageEZ {
	public WBQuadPass() { super("write-barrier[-removal].quad-pass"); }
	public boolean enabled() { return WRITEBARRIERS; }

	public List/*<Option>*/ getOptions() {
	    return WriteBarriers.getOptions();
	}

	public void real_action() {
	    System.out.println("Using write barriers for generational gc.");
	    if (wbOptLevel != 0)
		System.out.println
		    ("Removing write barriers at optimization level " +
		     wbOptLevel+".");
	    String rName = frame.getRuntime().resourcePath
		("writebarrier-safe.properties");
	    System.out.println(rName);
	    if (WB_TRANSFORMS == 2 || WB_TRANSFORMS == 3) {
		// transform recursive constructors
		hcf = new harpoon.Analysis.PreciseGC.RCTransformer
		    (hcf, classHierarchy, linker).codeFactory();
		// re-generate class hierarchy to handle modified methods
		classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    }
	    if (WB_TRANSFORMS == 1 || WB_TRANSFORMS == 3) {
		// perform allocation hoisting
		System.out.println("Performing allocation hoisting.");
		hcf = new CachingCodeFactory(hcf);
		hcf = new harpoon.Analysis.PreciseGC.AllocationHoisting
		    (hcf, classHierarchy, linker, rName, 
		     (wbOptLevel != 0) ? wbOptLevel : 2).codeFactory();
		// re-generate class hierarchy to handle modified methods
		classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	    }
	    hcf = new CachingCodeFactory(hcf);
	    hcf = (new harpoon.Analysis.PreciseGC.WriteBarrierQuadPass
		   (classHierarchy, hcf, linker, rName, wbOptLevel)).
		codeFactory();
	    // re-generate class hierarchy to handle added calls
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	}
    };



    public static class DynamicWBQuadPass extends CompilerStageEZ {
	public DynamicWBQuadPass() { super("dynamic-write-barrier.quad-pass");}
	public boolean enabled() { return DYNAMICWBS; }

	public void real_action() {
	    System.out.println
		("Using dynamic write barriers for generational gc.");
	    if (wbOptLevel == 1)
		System.out.println("Optimistically removing write barriers.");
	    harpoon.Analysis.PreciseGC.DynamicWBQuadPass dynamicWB = null;
	    if (wbOptLevel == 1 || WB_STATISTICS) {
		dynamicWB = new harpoon.Analysis.PreciseGC.DynamicWBQuadPass
		    (hcf, linker);
		hcf = dynamicWB.codeFactory();
	    }
	    hcf = (wbOptLevel == 0) ?
		(new harpoon.Analysis.PreciseGC.WriteBarrierInserter
		 (hcf, linker)).codeFactory() :
		(new harpoon.Analysis.PreciseGC.WriteBarrierInserter
		 (hcf, linker, dynamicWB)).codeFactory();
	    // re-generate class hierarchy to handle added calls
	    classHierarchy = new QuadClassHierarchy(linker, roots, hcf);
	}
    };


    public static class WBDynamicWBTreePass extends CompilerStageEZ {
	public WBDynamicWBTreePass() { super("wb-dynamicwb.tree-pass"); }
	public boolean enabled() { return WRITEBARRIERS || DYNAMICWBS; }

	public void real_action() {
	    // run constant propagation
	    hcf = new harpoon.Analysis.Tree.ConstantPropagation(hcf).
		codeFactory();
	    // remove write barriers for assignment to constants
	    hcf = harpoon.Analysis.PreciseGC.WriteBarrierConstElim.codeFactory
		(frame, hcf, linker);
	    if (WB_STATISTICS) {
		System.out.println("Compiling for write barrier statistics.");
		if (wbps != null)
		    wbps.println("\nWRITE BARRIER LIST FOR " + 
				 SAMain.className);
		writeBarrierStats = 
		    new harpoon.Analysis.PreciseGC.WriteBarrierStats
		    (frame, hcf, classHierarchy, wbps, linker);
		hcf = writeBarrierStats.codeFactory();
	    } else {
		hcf = harpoon.Analysis.PreciseGC.WriteBarrierTreePass.
		    codeFactory(hcf, frame, classHierarchy, linker);
	    }
	}
    };
    
}
