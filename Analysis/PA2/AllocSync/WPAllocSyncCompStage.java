// WPAllocSyncCompStage.java, created Tue Aug  2 11:38:25 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.AllocSync;

import java.util.List;
import java.util.LinkedList;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.Main.CompilerStage;
import harpoon.Main.CompilerStageEZ;
import harpoon.Main.CompStagePipeline;
import harpoon.Main.CompilerState;
import harpoon.Util.Options.Option;
import harpoon.Util.Util;
import harpoon.Util.Timer;

import harpoon.Analysis.Quads.CallGraph;

import harpoon.Analysis.Quads.DeepInliner.DeepInliner;
import harpoon.Analysis.Quads.DeepInliner.InlineChain;

import harpoon.Analysis.PA2.InterProcAnalysisResult;
import harpoon.Analysis.PA2.PointerAnalysis;
import harpoon.Analysis.PA2.AnalysisPolicy;
import harpoon.Analysis.PA2.Flags;
import harpoon.Analysis.PA2.WPPointerAnalysisCompStage;

import jpaul.Misc.Predicate;

/**
 * <code>WPAllocSyncCompStage</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: WPAllocSyncCompStage.java,v 1.2 2005-08-10 03:25:37 salcianu Exp $
 */
public class WPAllocSyncCompStage extends CompilerStageEZ {

    public static CompilerStage getFullStage() {
	final WPAllocSyncCompStage wpSaSr = new WPAllocSyncCompStage();
	final WPPointerAnalysisCompStage wpPa= 
	    new WPPointerAnalysisCompStage
	    (new Predicate() { 
		public boolean check(Object obj) {
		    return wpSaSr.enabled();
		}});
	return new CompStagePipeline(wpPa, wpSaSr) {
	    public boolean enabled() {
		return wpSaSr.enabled();
	    }
	};
    }

    public WPAllocSyncCompStage() { super("pa2:sa-sr"); }

    public boolean enabled() {
	return STACK_ALLOCATION || SYNC_REMOVAL;
    }

    private boolean STACK_ALLOCATION = false;
    private int     MAX_SA_INLINE_LEVEL = 0;
    private boolean SA_IN_LOOPS = false;

    private boolean SYNC_REMOVAL = false;

    public List<Option> getOptions() { 
	List<Option> opts = new LinkedList<Option>();

	opts.add(new Option("pa2:sa", "<maxInlineLevel>", "inLoops",
			    "Stack allocation using pointer analysis") {
	    public void action() {
		STACK_ALLOCATION = true;
		MAX_SA_INLINE_LEVEL = Integer.parseInt(getArg(0));
		if(getOptionalArg(0) != null) {
		    SA_IN_LOOPS = true;
		    assert 
			getOptionalArg(0).equals("inLoops") : 
			"unknown optional arg of --pa2:sa is not isLoop";
		}
		System.out.println("STACK ALLOC; inlining depth <= " + MAX_SA_INLINE_LEVEL + "; " + 
				   (SA_IN_LOOPS ? "" : "not ") + "in loops");
		//System.setProperty("harpoon.runtime", "2");
	    }
	});

	opts.add(new Option("pa2:sa-range", "<min> <max>", "") {
	    public void action() {
		ASFlags.SA_MIN_LINE = Integer.parseInt(getArg(0));
		ASFlags.SA_MAX_LINE = Integer.parseInt(getArg(1));
		System.out.println("STACK ALLOCATE ONLY ALLOCS WITH LINE #s BETWEEN [" + 
				   ASFlags.SA_MIN_LINE + ", " + ASFlags.SA_MAX_LINE + "]");
	    }
	});

	return opts;
    }


    protected void real_action() {
	CachingCodeFactory ccf = (CachingCodeFactory) hcf;
	PointerAnalysis pa = (PointerAnalysis) attribs.get("pa");
	CallGraph cg = pa.getCallGraph();
	AnalysisPolicy ap = new AnalysisPolicy(Flags.FLOW_SENSITIVITY, -1, Flags.MAX_INTRA_SCC_ITER);

	System.out.println("\nWHOLE PROGRAM STACK ALLOCATION");

	System.out.println("\n0. SSA IR GENERATION");
	Timer timer = new Timer();
	for(Object hm : cg.transitiveSucc(mainM)) {
	    if(pa.isAnalyzable((HMethod) hm)) {
		ccf.convert((HMethod) hm);
	    }
	}
	System.out.println("SSA IR GENERATION TOTAL TIME: " + timer);

	System.out.println("\n1. WHOLE PROGRAM POINTER ANALYSIS");
	timer = new Timer();
	pa.getInterProcResult(mainM, ap);
	for(Object hm : cg.transitiveSucc(mainM)) {
	    if(pa.isAnalyzable((HMethod) hm)) {
		InterProcAnalysisResult ipar = pa.getInterProcResult((HMethod) hm, ap);
		ipar.invalidateCaches();
	    }
	}
	System.out.println("WHOLE PROGRAM POINTER ANALYSIS TOTAL TIME: " + timer);


	// inlining depth 0 is not bad: it means doing just direct stack allocation
	System.out.println("\n2. GENERATE INLINING CHAINS OF DEPTH <= " + MAX_SA_INLINE_LEVEL);
	timer = new Timer();
	List<InlineChain> ics = new LinkedList<InlineChain>();	
	LoopDetector loopDet = new LoopDetector(ccf);
	for(Object hm : cg.transitiveSucc(mainM)) {
	    if(pa.isAnalyzable((HMethod) hm)) {
		ics.addAll((new AllocSyncOneMethod(pa, (HMethod) hm, ccf, loopDet,
						   MAX_SA_INLINE_LEVEL,
						   SA_IN_LOOPS)).getICS());
	    }
	}
	System.out.println("INLINING CHAIN GENERATION TOTAL TIME: " + timer);

	
	System.out.println("\n3. PERFORM THE INLINING:");
	timer = new Timer();
	DeepInliner.inline(ccf, ics, pa.getCallGraph());	
	System.out.println("INLINING TOTAL TIME: " + timer);
    }

}
