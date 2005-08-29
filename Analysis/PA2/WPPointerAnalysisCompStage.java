// WPPointerAnalysisCompStage.java, created Thu Jun 23 11:44:16 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.*;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;


import harpoon.Main.CompilerStage;
import harpoon.Main.CompilerStageEZ;
import harpoon.Main.CompilerState;
import harpoon.Util.Options.Option;
import harpoon.Util.Util;

import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.*;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadRSSx;

import harpoon.Util.BasicBlocks.BBConverter;
import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.ClassHierarchy;

import harpoon.Analysis.Quads.CallGraph;

import jpaul.Misc.Predicate;

import harpoon.Analysis.Quads.DeepInliner.DeepInliner;
import harpoon.Analysis.Quads.DeepInliner.InlineChain;

import harpoon.Util.Options.Option;


/**
 * <code>WPPointerAnalysisCompStage</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: WPPointerAnalysisCompStage.java,v 1.3 2005-08-29 16:13:35 salcianu Exp $
 */
public class WPPointerAnalysisCompStage extends CompilerStageEZ {

    public WPPointerAnalysisCompStage() { 
	this(new Predicate() {
	    public boolean check(Object obj) { return false; }
	});
    }

    public WPPointerAnalysisCompStage(Predicate externalEnabler) { 
	super("pa2");
	this.externalEnabler = externalEnabler;
    }

    private final Predicate externalEnabler;

    public boolean enabled() { 
	return 
	    externalEnabler.check(null) ||
	    DO_ANALYSIS ||
	    INTERACTIVE_ANALYSIS;
    }

    private boolean DO_ANALYSIS = false;
    private Set<String> methodsToAnalyze = new HashSet<String>();

    private boolean INTERACTIVE_ANALYSIS = false;

    public List<Option> getOptions() { 
	List<Option> opts = new LinkedList<Option>();
	// add the internal options of the whole-program analysis
	opts.addAll(Flags.getOptions());
	opts.add(new Option("pa2:a", "<method>", 
			    "Run Pointer Analysis over one method and present detailed results") {
	    public void action() {
		DO_ANALYSIS = true;
		methodsToAnalyze.add(getArg(0));
	    }
	});
	opts.add(new Option("pa2:i",
			    "Run the Pointer Analysis over methods whose names are interactively read.") {
	    public void action() {
		INTERACTIVE_ANALYSIS = true;
	    }
	});
	return opts;
    }


    protected void real_action() {
	CachingCodeFactory ccf;
	if(!(hcf instanceof CachingCodeFactory) || !hcf.getCodeName().equals(QuadRSSx.codename)) {
	    ccf = new CachingCodeFactory(QuadRSSx.codeFactory(QuadSSA.codeFactory(hcf)));
	    hcf = ccf;	
	}
	else { 
	    ccf = (CachingCodeFactory) hcf;
	}
	
	CallGraph cg = new harpoon.Analysis.Quads.CallGraphImpl(classHierarchy, ccf);
	WPPointerAnalysis pa = 
	    new WPPointerAnalysis(ccf, cg, linker, classHierarchy,
				  Collections.<HMethod>singleton(mainM),
				  Flags.MAX_INTRA_SCC_ITER);
	
	attribs = attribs.put("pa", pa);

	if(DO_ANALYSIS) {
	    for(String method : methodsToAnalyze) {
		displayInfo(method, pa);
	    }
	    System.out.println("pa2: Stop compiler after analysis");
	    System.exit(1);
	}

	if(INTERACTIVE_ANALYSIS) {
	    BufferedReader d = new BufferedReader(new InputStreamReader(System.in));
	    while(true) {
		System.out.print("Method name:");
		String method = null;
		try {
		    method = d.readLine();
		} catch(IOException e) {
		    throw new Error(e);
		}
		if(method == null) { // EOF received
		    System.out.println();
		    break;
		}
		if(method.equals("quit")) {
		    System.out.println("pa2: Stopping compiler after interactive analysis");
		    System.exit(1);
		}
		
		displayInfo(method, pa);
	    }
	}

	/*
	pa.getInterProcResult(mainM, new AnalysisPolicy(true, -1, Flags.MAX_INTRA_SCC_ITER));

	LoopDetector loopDet = new LoopDetector(ccf);
	List<InlineChain> ics = new LinkedList<InlineChain>();
	for(Object hm : cg.transitiveSucc(mainM)) {
	    ics.addAll((new AllocSyncOneMethod(pa, (HMethod) hm, ccf, loopDet)).getICS());
	}

	DeepInliner.inline(ccf, ics, pa.getCallGraph());
	*/

	/*
	examineAllMethods();
	System.exit(1);


	*/
    }

    //private NodeRepository nodeRep;
    //nodeRep = new NodeRepository(linker);
    
    //private BBConverter bbConv;
    //bbConv = new BBConverter(QuadNoSSA.codeFactory(hcf));

    //ConstraintSystem.DEBUG = true;
    //cons_gen = new ConstraintGenerator(hcf, nodeRep);


    /*

    private void examineAllMethods() {
	all_ivars = 0;
	all_evars = 0;
	all_fvars = 0;
	all_lvars = 0;
	all_ovars = 0;
	all_bbs = 0;
	for(HMethod hm : classHierarchy.callableMethods()) {
	    displayInfo(hm);
	}

	System.out.println("-------------------------------------");
	System.out.println("TOTAL:");
	System.out.println("  " + 
			   all_ivars + " IVar(s); " + 
			   all_evars + " EVar(s); " + 
			   all_fvars + " FVar(s); " + 
			   all_lvars + " LVar(s); " + 
			   all_ovars + " OVar(s) ");
	System.out.println("  " + all_bbs + " NoSSA basic blocks");
    }

    private int all_ivars;
    private int all_evars;
    private int all_fvars;
    private int all_lvars;
    private int all_ovars;
    private int all_bbs;
    */


    private void displayInfo(String method, WPPointerAnalysis pa) {
	for(HMethod hm : getHMethods(method)) {
	    displayInfo(hm, pa);
	}	
    }


    private Collection<HMethod> getHMethods(String method) {
	String desc = null;
	int paranPos = method.indexOf('(');
	if(paranPos != -1) {
	    // interpret the method name as a fully qualified descriptor (including the param types)
	    desc   = method.substring(paranPos);
	    method = method.substring(0, paranPos);
	}

	int lastDotPos = method.lastIndexOf('.');
	if(lastDotPos == -1) {
	    System.out.println("pa2: cannot get the class name for method \"" + method + "\"");
	    return Collections.<HMethod>emptySet();
	}
	String className  = method.substring(0, lastDotPos);
	String methodName = method.substring(lastDotPos+1);

	HClass hc = null;
	try {
	    hc = linker.forName(className);
	}
	catch(harpoon.ClassFile.NoSuchClassException e) {
	    System.out.println("pa2: No class " + className + "; ignore method \"" + method + "\"");
	    return Collections.<HMethod>emptySet();
	}

	if(desc != null) {
	    try {
		return Collections.<HMethod>singleton(hc.getMethod(methodName, desc));
	    } 
	    catch(NoSuchMethodError e) {
		System.out.println("pa2: No method named " + methodName + " with desc " + desc + " in " + className +
				   "; ignore method \"method\"");
		return Collections.<HMethod>emptySet();
	    }
	}
	else {
	    Collection<HMethod> hms = new LinkedList<HMethod>();
	    for(HMethod hm : hc.getMethods()) {
		if(hm.getName().equals(methodName)) {
		    hms.add(hm);
		}
	    }
	    return hms;
	}
    }


    private void displayInfo(HMethod hm, WPPointerAnalysis pa) {
	AnalysisPolicy ap = new AnalysisPolicy(true, -1, Flags.MAX_INTRA_SCC_ITER);
	// make sure the inter-proc fixed-points are solved
	pa.getInterProcResult(hm, ap);

	boolean oldShowIntraProcConstraints = Flags.SHOW_INTRA_PROC_CONSTRAINTS;
	Flags.SHOW_INTRA_PROC_CONSTRAINTS = true;
	// new IntraProc has the side effect of printing the constraints
	new IntraProc(hm, ap.flowSensitivity, pa);
	Flags.SHOW_INTRA_PROC_CONSTRAINTS = oldShowIntraProcConstraints;

	System.out.println("\n-----------\n");

	boolean oldShowIntraProcResults = Flags.SHOW_INTRA_PROC_RESULTS;
	Flags.SHOW_INTRA_PROC_RESULTS = true;
	pa.getFullResult(hm, ap);
	Flags.SHOW_INTRA_PROC_RESULTS = oldShowIntraProcResults;
    }


    /*
    private void displayInfo(HMethod hm) {
	HCode hcode = hcf.convert(hm);
	if(hcode == null) return;

	int bbs = bbConv.convert2bb(hm).blockSet().size();

	//ConstraintSystem sys = cons_gen.buildConstraints(hm, new AnalysisPolicy(true, -1, -1));
	
	IntraProc intraProc = new IntraProc(hm, new AnalysisPolicy(true, -1, -1), hcf, nodeRep);
	ConstraintSystem sys = intraProc.cs;

	int ivars = 0;
	int evars = 0;
	int fvars = 0;
	int lvars = 0;
	int ovars = 0;

	for(Object o : sys.debugUniqueVars()) {
	    Var v = (Var) o;
	    if(v instanceof IVar) ivars++;
	    if(v instanceof EVar) evars++;
	    if(v instanceof FVar) fvars++;
	    if(v instanceof LVar) lvars++;
	    if(v instanceof OVar) ovars++;
	}
	
	//	if(bbs > 75) {
	    System.out.println(hm);
	    System.out.println("  " + 
			       ivars + " IVar(s); " + 
			       evars + " EVar(s); " + 
			       fvars + " FVar(s); " + 
			       lvars + " LVar(s); " + 
			       ovars + " OVar(s)");
	    System.out.println("  " + bbs + " NoSSA basic blocks");
	    //	}

	//sys.debugPrintSolverStructs(System.out);

	//System.out.print("Solving system ...");
	intraProc.solve();
	System.out.println();
	
	all_ivars += ivars;
	all_evars += evars;
	all_fvars += fvars;
	all_lvars += lvars;
	all_ovars += ovars;
	all_bbs   += bbs;
    }
    */

}
