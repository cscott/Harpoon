// AllocSyncOptCompStage.java, created Fri Apr 18 22:13:12 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.ClassFile.HMethod;

import harpoon.Analysis.PointerAnalysis.MAInfo;
import harpoon.Analysis.PointerAnalysis.MAInfo.MAInfoOptions;

import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaMethod;

import harpoon.Main.CompilerState;
import harpoon.Main.CompilerStage;
import harpoon.Main.CompilerStageEZ;
import harpoon.Main.CompStagePipeline;
import harpoon.Util.Options.Option;

import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;


/**
 * <code>AllocSyncOptCompStage</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: AllocSyncOptCompStage.java,v 1.2 2003-06-04 18:44:31 salcianu Exp $
 */
public class AllocSyncOptCompStage extends CompilerStageEZ {

    /** Returns a compiler stage consisting of the sequential
        composition of a pointer analysis stage and a stack/heap
        allocation and sync removal optimization stage. */
    public static CompilerStage getFullStage() {
	final CompilerStage optStage = new AllocSyncOptCompStage();
	return 
	    new CompStagePipeline(new PointerAnalysisCompStage(true),
				  optStage) {
	    public boolean enabled() { return optStage.enabled(); }
	};
    }


    public AllocSyncOptCompStage() { super("alloc-sync-opt"); }

    private MAInfoOptions maio = new MAInfoOptions();
    private boolean SHOW_ALLOC_PROPERTIES = false;
    
    public List/*<Option>*/ getOptions() {
	List/*<Option>*/ opts = new LinkedList/*<Option>*/();
	
	opts.add(new Option("stack-alloc", "<policy>", "<inlining-depth>",
			    "Stack allocation policy: 1 (not in loops) | 2 (whenever it's possible); the optional argument <inlining-depth> specifies the maximul level of inlining for improving the stack allocation opportunities (no inlining by default)") {
	    public void action() {
		int policy = Integer.parseInt(getArg(0));
		switch(policy) {
		case MAInfoOptions.STACK_ALLOCATE_NOT_IN_LOOPS:
		case MAInfoOptions.STACK_ALLOCATE_ALWAYS:
		    maio.DO_STACK_ALLOCATION = true;
		    maio.STACK_ALLOCATION_POLICY = policy;
		    // see if any inlining depth is specified
		    if(getOptionalArg(0) != null) {
			int depth = Integer.parseInt(getOptionalArg(0));
			assert depth >= 0 : "Invalid inlining depth " + depth;
			maio.DO_INLINING_FOR_SA = true;
			maio.MAX_INLINING_LEVEL = depth;
		    }
		    break;
		default:
		    System.err.println("Unknown stack allocation policy " +
				       policy);
		    System.exit(1);
		}
	    }
	});
	
	opts.add(new Option("thread-alloc", "", "<inlining-depth>",
			    "Thread allocation; the optional argument <inlining-depth> specifies the maximul level of inlining for improving the thread allocation opportunities (no inlining by default)") {
	    public void action() { 
		maio.DO_THREAD_ALLOCATION = true;
		if(getOptionalArg(0) != null) {
		    int depth = Integer.parseInt(getOptionalArg(0));
		    assert depth >= 0 : "Invalid inlining depth " + depth;
		    maio.DO_INLINING_FOR_TA = true;
		    // TODO: currently, we have the same
		    // MAX_INLINING_LEVEL for both SA and TA; they
		    // should be either condensed in a single option;
		    // or separated.
		    maio.MAX_INLINING_LEVEL = depth;
		}
	    }
	});

	opts.add(new Option("thread-arg-prealloc",
			    "Preallocate thread parameters in thread local heap; DISCOURAGED!  Makes sense only in the context of thread allocation.") {
	    public void action() { maio.DO_PREALLOCATION = true; }
	});
	
	opts.add(new Option("sync-removal", "", "wit",
			    "Synchronization removal optimization; if optional argument \"wit\" is present, use the inter-thread pointer analysis (unrecommended).") {
	    public void action() {
		maio.GEN_SYNC_FLAG = true;
		if(getOptionalArg(0) != null) {
		    assert getOptionalArg(0).equals("wit") :
			"Unknown optional arg for sync-removal" +
			getOptionalArg(0);
		    maio.USE_INTER_THREAD = true;
		}
		// make sure we use Runtime2 - Runtime1 disconsiders
		// the noSync flags
		System.setProperty("harpoon.runtime", "2");
	    }
	});
	
	opts.add(new Option("show-ap", "Show alocation properties") {
	    public void action() { SHOW_ALLOC_PROPERTIES = true; }
	});

	opts.add(new Option("pa-optimize-all", "Optimize all code, including the static initializers (by default, optimize only code reachable from the main method)") {
	    public void action() { OPTIMIZE_ALL = true; }
	});
	
	return opts;
    }
    
    /** Controls whether we optimize all the code or not.  By default,
	do not optimize the static initializers - some of them are
	really big (ex: jaca.util.Locale) and there is little interest
	in optimizing them anyway: they are executed only once.*/
    private boolean OPTIMIZE_ALL = false;
    
    public boolean enabled() {
	return
	    maio.DO_STACK_ALLOCATION ||
	    maio.DO_THREAD_ALLOCATION ||
	    maio.GEN_SYNC_FLAG;
    }
    
    protected void real_action() {
	PointerAnalysis pa = 
	    (PointerAnalysis) attribs.get("PointerAnalysis");
	
	MetaCallGraph mcg = pa.getMetaCallGraph();

	Set/*<MetaMethod>*/ mms = 
	    OPTIMIZE_ALL ? mcg.getAllMetaMethods() : 
	    mcg.transitiveSucc(new MetaMethod(mainM, true));

	time_analysis(pa, mms);
	
	System.out.println("MAInfo options: ");
	maio.print("\t");
	
	long g_tstart = time();
	MAInfo mainfo = new MAInfo(pa, hcf, linker, mms, maio);
	System.out.println("GENERATION OF MA INFO TIME  : " +
			   (time() - g_tstart) + "ms");
	System.out.println("===================================\n");
	
	if(SHOW_ALLOC_PROPERTIES) // show allocation policies
	    mainfo.print();
    }

    // time the pointer analysis over all (meta)methods from mms. 
    private void time_analysis(PointerAnalysis pa, Set/*<MetaMethod>*/ mms) {
	// The following loop times the analysis of the relevant part
	// of the program (i.e., methods from mms).  Doing it here,
	// before any optimization, allows us to time it accurately.
	long g_tstart = time();
	if(OPTIMIZE_ALL) {
	    for(Iterator it = mms.iterator(); it.hasNext(); ) {
		MetaMethod mm = (MetaMethod) it.next();
		if(!analyzable(mm)) continue;
		pa.getIntParIntGraph(mm);
	    }
	}
	else pa.getIntParIntGraph(new MetaMethod(mainM, true));
	System.out.println("Intrathread Analysis time: " +
			   (time() - g_tstart) + "ms");
	System.out.println("===================================\n");
	
	if (maio.USE_INTER_THREAD) {
	    g_tstart = System.currentTimeMillis();
	    for(Iterator it = mms.iterator(); it.hasNext(); ) {
		MetaMethod mm = (MetaMethod) it.next();
		if(!analyzable(mm)) continue;
		    pa.getIntThreadInteraction(mm);
	    }
	    System.out.println("Interthread Analysis time: " +
			       (time() - g_tstart) + "ms");
	    System.out.println("===================================\n");
	}
    }


    // TODO: change this to invoke hcf.convert and check result against null
    private static boolean analyzable(MetaMethod mm) {
	HMethod hm = mm.getHMethod();
	if(java.lang.reflect.Modifier.isNative(hm.getModifiers()))
	    return false;
	if(java.lang.reflect.Modifier.isAbstract(hm.getModifiers()))
	    return false;
	return true;
    }

    private static long time() {
	return System.currentTimeMillis();
    }    
}
