// WPPointerAnalysis.java, created Wed Jul  6 06:54:55 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

import java.io.PrintWriter;

import jpaul.Graphs.DiGraph;
import jpaul.Graphs.Navigator;
import jpaul.Graphs.ForwardNavigator;
import jpaul.Graphs.SCComponent;
import jpaul.Graphs.TopSortedCompDiGraph;

import jpaul.DataStructs.WorkSet;
import jpaul.DataStructs.WorkStack;
import jpaul.DataStructs.WorkList;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HMethod;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.ClassHierarchy;

import harpoon.IR.Quads.CALL;

import harpoon.Util.Timer;
import harpoon.Util.Util;

/**
 * <code>WPPointerAnalysis</code> is a whole-program pointer analysis.
 * "Whole-program" = every time the analysis is queried to analyze a
 * specific method <code>m</code>, it will analyze all the transitive
 * callees of <code>m</code> (the analysis results for the callees are
 * used by the intre-procedural analysis).  Therefore, the analysis of
 * the main method of the application implies the analysis of all the
 * methods from the program, which leads to a precise result, but may
 * take a very long time.
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: WPPointerAnalysis.java,v 1.1 2005-08-10 02:58:19 salcianu Exp $ */
public class WPPointerAnalysis extends PointerAnalysis {

    private static final boolean VERBOSE = PAUtil.VERBOSE;

    public WPPointerAnalysis(CachingCodeFactory hcf, CallGraph cg, Linker linker, ClassHierarchy classHierarchy) {
	this.hcf     = hcf;
	this.nodeRep = new NodeRepository(linker);
	this.cg      = cg;
	this.linker  = linker;

	TypeFilter.initClassHierarchy(classHierarchy);
    }

    private final CachingCodeFactory hcf;
    private final NodeRepository nodeRep;
    private final CallGraph cg;
    private final Linker linker;

    public CallGraph getCallGraph() { return cg; }
    public CachingCodeFactory getCodeFactory() { return hcf; }
    public NodeRepository getNodeRep() { return nodeRep; }

    // used by CallConstraint
    InterProcAnalysisResult getCurrentInterProcResult(HMethod hm) {
	return hm2result.get(hm);
    }

    public InterProcAnalysisResult getInterProcResult(HMethod hm, AnalysisPolicy ap) {
	InterProcAnalysisResult res = null;
	// If we use the "freshen_trick", we must un-freshen a graph before returning it.
	// To speed up poitential clients, we cache the "unfreshed" inter-proc results.
	if(Flags.USE_FRESHEN_TRICK) {
	    res = hm2extResult.get(hm);
	    if(res != null) return res;
	}
	res = hm2result.get(hm);
	if(res == null) {
	    if(ap == null) return null;
	    // computeResult will also store res in hm2result (as part of the fixed-point computation)
	    res = computeResult(hm, ap);
	}
	if(Flags.USE_FRESHEN_TRICK) {
	    res = GraphOptimizations.unFreshen(res);
	    hm2extResult.put(hm, res);
	}
	return res;
    }

    private Map<HMethod,InterProcAnalysisResult> hm2extResult = 
	new HashMap<HMethod,InterProcAnalysisResult>();


    public FullAnalysisResult getFullResult(HMethod hm, AnalysisPolicy ap) {
	// this triggers the analysis of all callees, solving the fixed-points
	InterProcAnalysisResult res = getInterProcResult(hm, ap);
	// now, we only need to analyze hm once - no inter-proc fixed-point required
	MethodData md = new MethodData(new IntraProc(hm, ap.flowSensitivity, this), ap);
	return md.intraProc.fullSolve();
    }


    private Map<HMethod,InterProcAnalysisResult> hm2result =
	new HashMap<HMethod,InterProcAnalysisResult>();

    private InterProcAnalysisResult computeResult(HMethod hm, AnalysisPolicy ap) {
	Timer timer = new Timer();

	// 1. find all unanalyzed callees, compute dependencies between them
	// + find the dependency strongly connected components (SCCs).
	DiGraph<HMethod> methodDiGraph = 
	    DiGraph.<HMethod>diGraph(Collections.<HMethod>singleton(hm),
				     methodFwdNavigator);
	calleeNavigator = methodDiGraph.getNavigator();
	TopSortedCompDiGraph<HMethod> tsCompDiGraph = 
	    new TopSortedCompDiGraph<HMethod>(methodDiGraph);
	List<SCComponent<HMethod>> methodSCCs = tsCompDiGraph.incrOrder();

	if(Flags.SHOW_METHOD_SCC) {
	    printMethodSCCs(pwStdOut, methodSCCs, hm,
			    tsCompDiGraph.getVertex2SccMap());
	}

	// 2. analyze each scc in topological order
	for(SCComponent<HMethod> scc : methodSCCs) {
	    analyzeSCC(scc, ap);
	}

	System.out.println("ANALYSIS TIME " + timer);

	// enable some GC
	this.calleeNavigator = null;

	// 3. extract result and return
	InterProcAnalysisResult res = hm2result.get(hm);
	assert res != null;
	return res;
    }

    private final PrintWriter pwStdOut = new PrintWriter(System.out, true);

    private Navigator<HMethod> calleeNavigator;

    private final ForwardNavigator<HMethod> methodFwdNavigator = new ForwardNavigator<HMethod>() {
	public List<HMethod> next(HMethod hm) {
	    HMethod[] callees = cg.calls(hm);
	    List<HMethod> next = new LinkedList<HMethod>();
	    for(HMethod callee : callees) {
		if(PAUtil.isAbstract(callee)) {
		    if(VERBOSE) System.out.println("Warning: abstract! " + callee);
		    continue;
		}

		// consider only unanalyzed method
		if((hm2result.get(callee) == null) &&
		   !SpecialInterProc.canModel(callee)) {
		    next.add(callee);
		}
	    }
	    return next;
	}
    };

    private static class MethodData {
	public MethodData(IntraProc intraProc, AnalysisPolicy ap) {
	    this.intraProc = intraProc;
	    this.ap = ap;
	}
	public IntraProc intraProc;
	public AnalysisPolicy ap;
	public int iterCount;
    }

    
    private Map<HMethod, MethodData> hm2md = new HashMap<HMethod,MethodData>();
    private WorkSet<HMethod> workSet = new WorkStack<HMethod>();


    // _analyzeSCC does the real job.  This method is just a wrapper
    // to add timing and debug printing.  I have to agree that an
    // aspect would be cleaner here.
    private void analyzeSCC(SCComponent<HMethod> scc, AnalysisPolicy ap) {
	printMethodSCC(pwStdOut, scc, null);
	Timer timer = new Timer();
	
	_analyzeSCC(scc, ap);
	
	System.out.println("Total analysis time for SCC" + scc.getId() + " : " + timer);
	System.out.println();
    }


    private int maxIntraSccIter(SCComponent<HMethod> scc, int fpMaxIter) {
	int s = scc.size();
	int k = (s <= 5) ? 8 : (s <= 10) ? 4 : (s <= 15) ? 2 : (s <= 30) ? 1 : 0;
	return k * fpMaxIter * scc.size();
    }

    private void _analyzeSCC(SCComponent<HMethod> scc, AnalysisPolicy ap) {
	// refuze to analyze native methods
	if(PAUtil.isNative(scc.nodes().iterator().next()))
	    return;

	this.scc = scc;
	skipSameSccCalls = false;

	for(HMethod hm : scc.nodes()) {
	    hm2md.put(hm, (new MethodData(new IntraProc(hm, ap.flowSensitivity, this), ap)));
	    hm2scc.put(hm, scc);
	}

	if(!scc.exits().isEmpty()) {
	    workSet.addAll(scc.exits());
	}
	else {
	    workSet.addAll(scc.nodes());
	}

	boolean mustCheck = scc.isLoop();
	int nbIter = 0;

	while(!workSet.isEmpty()) {
	    HMethod hm = workSet.extract();
	    MethodData md = hm2md.get(hm);

	    if(mustCheck) {
		if(!skipSameSccCalls) {
		    if(nbIter > maxIntraSccIter(scc, ap.fpMaxIter)) {
			System.out.println("Too many intra-scc iterations -> skip all calls to same SCC methods");
			skipSameSccCalls = true;
			sccWithSkippedIntraCalls.add(scc);
			// cancel out the previous results
			for(HMethod hm2 : scc.nodes()) {
			    hm2result.remove(hm2);
			}
			// make sure all methods will be re-analyzed (once)
			workSet.addAll(scc.nodes());
			continue;
		    }
		    else {
			System.out.println("Iter #" + nbIter + " out of " + maxIntraSccIter(scc, ap.fpMaxIter));
			nbIter++;
		    }
		}
	    }

	    //// flow sensitivity -> insens.  However, worse performances: flow insensitivity
	    //// increases a lot the sets of mutually dependent constraints ...
 	    // degradePrecision(hm, md);
	    md.iterCount++;  // for stats only

	    InterProcAnalysisResult result = analyzeMethod(hm, md);
	    if(mustCheck) {
		if(newResult(hm, result) && !skipSameSccCalls) {
		    for(HMethod caller : calleeNavigator.prev(hm)) {
			if(scc.contains(caller)) {
			    workSet.add(caller);
			}
		    }
		}
	    }
	    else {
		hm2result.put(hm, result);
	    }
	}

	if(mustCheck) {
	    System.out.println("Fixed-point terminated:");
	    for(HMethod hm : scc.nodes()) {
		MethodData md = hm2md.get(hm);
		System.out.println("  #" + md.iterCount + " " + hm);
	    }
	}

	hm2md.clear(); // enable GC

	System.out.println("Post-SCC triming");
	for(HMethod hm : scc.nodes()) {
	    //if(VERBOSE) System.out.println("Trim results for \"" + hm + "\"");
	    InterProcAnalysisResult ipar = hm2result.get(hm);
	    ipar = GraphOptimizations.trimUnaffected(ipar);
	    ipar = GraphOptimizations.unifyLoads(ipar);
	    hm2result.put(hm, ipar);
	}
	
	this.scc = null;
    }

    private SCComponent<HMethod> scc;
    private boolean skipSameSccCalls = false;

    boolean shouldSkipDueToFPLimit(CALL cs, HMethod callee) {
	if(!skipSameSccCalls) return false;
	// if skipSameSccCalls, treat as unanalizable (skip) all calls
	// that may invoke methods from same SCC
	return anyCalleeInScc(cs, scc);
    }

    private boolean anyCalleeInScc(CALL cs, SCComponent<HMethod> scc) {
	for(HMethod callee : getCallGraph().calls(Util.quad2method(cs), cs)) {
	    if(scc.nodes().contains(callee)) return true;
	}
	return false;
    }
    
    private InterProcAnalysisResult analyzeMethod(HMethod hm, MethodData md) {
	System.out.println("Analyze (" + md.iterCount + ") \"" + hm + "\"" + "; " + hm.getDescriptor());
	Timer timerSolve = new Timer();
	FullAnalysisResult far = md.intraProc.fullSolve();
	timerSolve.stop();

	Timer timerSimplify = new Timer();
	InterProcAnalysisResult res = far;

	if(Flags.SHOW_TRIM_STATS)
	    System.out.println("\t\t" + PAUtil.graphSizeStats(far) + " --trimUnreachable-> ");

	res = GraphOptimizations.trimUnreachable(far, nodeRep.getParamNodes(hm));

	if(Flags.SHOW_TRIM_STATS)
	    System.out.println("\t\t" + PAUtil.graphSizeStats(res) + " --trimUnaffected--> ");

	res = GraphOptimizations.trimUnaffected(res);

	if(Flags.SHOW_TRIM_STATS)
	    System.out.println("\t\t" + PAUtil.graphSizeStats(res) + " --unifyLoads------> ");

	res = GraphOptimizations.unifyLoads(res);

	if(Flags.SHOW_TRIM_STATS)
	    System.out.println("\t\t" + PAUtil.graphSizeStats(res) + " --unifyGblEsc-----> ");

	res = GraphOptimizations.unifyGblEsc(res, nodeRep);

	if(Flags.SHOW_TRIM_STATS)
	    System.out.println("\t\t" + PAUtil.graphSizeStats(res));

	if(Flags.USE_FRESHEN_TRICK) {
	    res = GraphOptimizations.freshen(res);
	}

	timerSimplify.stop();

	System.out.println("\tMethod analysis time: " + timerSolve + " + " + timerSimplify);
	return res;
    }


    private AnalysisPolicy lessPrecise(AnalysisPolicy ap) {
	if(ap.flowSensitivity) {
	    return new AnalysisPolicy(false, ap.staticCallDepth, ap.fpMaxIter);
	}
	return ap;
    }


    // Checks whether the inter-proc result res for hm brings any new
    // information that would determine the re-examination of hm's
    // callers.  If new result, join it to the one that is already
    // stored in hm2result.
    private boolean newResult(HMethod hm, InterProcAnalysisResult res) {

	InterProcAnalysisResult oldRes = hm2result.get(hm);
	if(oldRes == null) {
	    if(res == null) return false;
	    hm2result.put(hm, res);
	    return true;
	}

	// IT IS ESSENTIAL TO USE "|" instead of "||": "|" forces the
	// evaluation of each subexpression, even if the result of teh
	// big logical expression is already known.

	boolean newResult = false;

	System.out.print("\t");

	if(oldRes.eomI().join(res.eomI())) {
	    System.out.print("inside edges : ");
	    newResult = true;
	}

	int oldSize = oldRes.eomO().size().right.intValue();
	PAEdgeSet oldOutside = (PAEdgeSet) oldRes.eomO().clone();

	if(oldRes.eomO().join(res.eomO())) {
	    System.out.print("outside edges : ");
	    newResult = true;

	    /*
	    int newSize = oldRes.eomO().size().right.intValue();
	    System.out.println("newSize = " + newSize + "; oldSize = " + oldSize);
	    System.out.println("oldOutside: \n" + oldOutside + "\nsize=" + oldOutside.size());
	    System.out.println("newOutside: \n" + oldRes.eomO() + "\nsize=" + oldRes.eomO().size());
	    
	    if(newSize <= oldSize) {
		assert false : 
		    "set of outside edges should go only up!\n" + 
		    "newSize(" + newSize + ") < oldSize(" + oldSize + ")" +
		    "\noldOutside:\n " + oldOutside +
		    "\nnewOutside:\n " + oldRes.eomO() + "\n";
		System.exit(1);
	    }

	    System.out.flush();
	    */
	}
	    
	if(oldRes.eomDirGblEsc().addAll(res.eomDirGblEsc())) {
	    System.out.print("DirGblEsc : ");
	    newResult = true;
	}

	if(oldRes.ret().addAll(res.ret())) {
	    System.out.print("res : ");
	    newResult = true;
	}

	if(oldRes.ex().addAll(res.ex())) {
	    System.out.print("ex : ");
	    newResult = true;
	}
	
	if(newResult) {
	    oldRes.invalidateCaches();
	}

	if(newResult) {
	    System.out.println("CHANGED");
	}
	else {
	    System.out.println("UNCHANGED!");
	}

	System.out.println("\t\t" + PAUtil.graphSizeStats(oldRes, true));

	return newResult;
    }


    private void printMethodSCCs(PrintWriter pw, List<SCComponent<HMethod>> listSCCs, HMethod hmRoot,
				 Map<HMethod,SCComponent<HMethod>> hm2scc) {
	pw.println("SCC of methods for the analysis of " + hmRoot);
	for(SCComponent<HMethod> scc : listSCCs) {
	    printMethodSCC(pw, scc, hm2scc);
	}
	pw.flush();
    }

    private void printMethodSCC(PrintWriter pw,
				SCComponent<HMethod> scc,
				Map<HMethod,SCComponent<HMethod>> hm2scc) {
	pw.print("SCC" + scc.getId() + " (" + scc.size() + " method(s)");
	if(scc.isLoop()) pw.print(" - loop");
	pw.println(") {");
	for(HMethod hm : scc.nodes()) {
	    pw.println("  " + hm + "{" + hm.getDescriptor() + "}");
	    if(hm2scc == null) continue;
	    for(HMethod callee : cg.calls(hm)) {
		pw.print("    " + callee);
		SCComponent<HMethod> sccCallee = hm2scc.get(callee);
		if(sccCallee != null) {
		    if(sccCallee == scc)
			pw.print(" [SAME SCC]");
		    else
			pw.print(" [SCC" + sccCallee.getId() + "]");
		}
		pw.println();
	    }
	}
	pw.println("}");
	pw.flush();
    }

    public boolean hasAnalyzedCALL(HMethod caller, CALL cs, HMethod callee) {
	// calls to exception initializers were considered unanalyzable (for efficiency)
	if(PAUtil.exceptionInitializerCall(cs)) return false;
	if(SpecialInterProc.canModel(callee)) return false;

	SCComponent<HMethod> scc1 = hm2scc.get(caller);
	SCComponent<HMethod> scc2 = hm2scc.get(callee);
	if((scc1 == null) || (scc2 == null))
	    return false;
	return
	    !(sccWithSkippedIntraCalls.contains(scc1) &&
	      anyCalleeInScc(cs, scc1));
    }
    private Set<SCComponent<HMethod>> sccWithSkippedIntraCalls = new HashSet<SCComponent<HMethod>>();
    private Map<HMethod,SCComponent<HMethod>> hm2scc = new HashMap<HMethod,SCComponent<HMethod>>();


    protected void finalize() {
	TypeFilter.releaseClassHierarchy();
    }

    /*
    private void degradePrecision(HMethod hm, MethodData md) {
	if(md.iterCount == ap.fpMaxIter) {
	    // try to reduce the precision, in the hope of making the analysis finish on this SCC
	    AnalysisPolicy ap2 = lessPrecise(md.ap);
	    if(!ap2.equals(md.ap)) {
		System.out.println("Downgraded the policy for " + hm + "\n\t" + 
				   md.ap + " -> " + ap2);
		md.ap = ap2;
		md.intraProc = new IntraProc(hm, ap2, this);
		md.iterCount = 0; // reset the counter
	    }
	}
    }
    */

}
