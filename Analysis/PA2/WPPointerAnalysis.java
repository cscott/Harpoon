// WPPointerAnalysis.java, created Wed Jul  6 06:54:55 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Collections;
import java.util.Collection;
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
import jpaul.Graphs.GraphUtil;

import jpaul.DataStructs.WorkSet;
import jpaul.DataStructs.WorkStack;
import jpaul.DataStructs.WorkList;
import jpaul.DataStructs.Pair;
import jpaul.DataStructs.DSUtil;

import jpaul.Misc.Predicate;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HCodeElement;

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
 * @version $Id: WPPointerAnalysis.java,v 1.6 2005-09-19 00:43:30 salcianu Exp $ */
public class WPPointerAnalysis extends PointerAnalysis {

    private static final boolean VERBOSE = PAUtil.VERBOSE;

    public WPPointerAnalysis(CachingCodeFactory hcf, CallGraph cg, Linker linker, ClassHierarchy classHierarchy,
			     Collection<HMethod> roots, int fpMaxIter) {
	this.hcf     = hcf;
	this.nodeRep = new NodeRepository(linker);
	this.cg      = cg;
	this.linker  = linker;

	TypeFilter.initClassHierarchy(classHierarchy);

	findNoFPCgSCC(roots, fpMaxIter);
    }

    private final CachingCodeFactory hcf;
    private final NodeRepository nodeRep;
    private final CallGraph cg;
    private final Linker linker;

    public CallGraph getCallGraph() { return cg; }
    public CachingCodeFactory getCodeFactory() { return hcf; }
    public NodeRepository getNodeRep() { return nodeRep; }


    // set of call graph SCCs that are so big that we do not run to
    // compute a precise fix-point; instead, we use a special kind of
    // widening: we consider unanalyzable all calls to methods from
    // the same SCC.
    private final Set<SCComponent<HMethod>> noFPCgSCCs = new HashSet<SCComponent<HMethod>>();
    // map method -> call graph SCC it belongs to
    private final Map<HMethod,SCComponent<HMethod>> hm2CgSCC = new HashMap<HMethod, SCComponent<HMethod>>();
    // The above two structures will be used when computing the
    // dependency graph for the yet-unanalyzed callees of a given
    // method.

    
    private void findNoFPCgSCC(Collection<HMethod> roots, int fpMaxIter) {
	Timer timer = new Timer();
	System.out.println("findNoFPCgSCC(" + roots + ")");
	// Find out inter-proc dependencies
	DiGraph<HMethod> methodDepDiGraph =
	    DiGraph.<HMethod>diGraph(roots, cgNav);
	TopSortedCompDiGraph<HMethod> tsCompDiGraph = 
	    new TopSortedCompDiGraph<HMethod>(methodDepDiGraph);

	// identify SCCs for which no fixed-point will be performed
	for(SCComponent<HMethod> scc : tsCompDiGraph.incrOrder()) {
	    if(maxIntraSccIter(scc, fpMaxIter) == 0) {
		noFPCgSCCs.add(scc);
		// for such SCCs, calls to same-scc methods won't be analyzed
		addUnanalyzedIntraSCCCalls(scc);
	    }
	}

	hm2CgSCC.putAll(tsCompDiGraph.getVertex2SccMap());
	System.out.println("findNoFPCgSCC TOTAL TIME = " + timer);
    }

    private final ForwardNavigator<HMethod> cgNav = 
	GraphUtil.cachedFwdNavigator(new ForwardNavigator<HMethod>() {
	    public List<HMethod> next(HMethod hm) {
		Set<HMethod> next = new HashSet<HMethod>();
		
		for(CALL cs : cg.getCallSites(hm)) {
		    // to save time, we don't analyze exception initializers
		    // we just assume they don't do anything interesting
		    if(PAUtil.exceptionInitializerCall(cs)) continue;
		    
		    // ignore clearly unanalyzable programs
		    HMethod[] callees = cg.calls(hm, cs);
		    if(InterProcConsGen.clearlyUnanalyzableCALL(cs, callees)) continue;
		    
		    for(HMethod callee : callees) {
			if(PAUtil.isAbstract(callee)) {
			    continue;
			}
			// disconsider methods that can be modeled without actually analyzing them
			if(SpecialInterProc.canModel(callee)) {
			    continue;
			}
			// no dependency on already-analyzed callees
			if(hm2result.get(callee) != null) {
			    continue;
			}
			next.add(callee);
		    }
		}
		return new LinkedList<HMethod>(next);
	    }
	});
    


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

	// 1. find all unanalyzed callees, compute dependencies
	// between them + find the strongly connected components
	// (SCCs) of the dependency relation.
	Pair<Navigator<HMethod>,TopSortedCompDiGraph<HMethod>> pair = computeMethodSCCs(hm, ap);
	interProcDepNav = pair.left;
	TopSortedCompDiGraph<HMethod> tsCompDiGraph = pair.right;
	List<SCComponent<HMethod>> methodSCCs = tsCompDiGraph.incrOrder();

	if(Flags.SHOW_METHOD_SCC) {
	    printMethodSCCs(pwStdOut, methodSCCs, hm,
			    tsCompDiGraph.getVertex2SccMap(),
			    cg);
	}

	// 2. analyze each scc in topological order
	for(SCComponent<HMethod> scc : methodSCCs) {
	    analyzeSCC(scc, ap);
	}

	System.out.println("ANALYSIS TIME " + timer);

	// enable some GC
	this.interProcDepNav = null;

	// 3. extract result and return
	InterProcAnalysisResult res = hm2result.get(hm);
	assert res != null;
	return res;
    }



    private Pair<Navigator<HMethod>,TopSortedCompDiGraph<HMethod>> computeMethodSCCs(final HMethod root, AnalysisPolicy ap) {
	// methodDepDiGraph = directed graph of dependecies between analysis of different methods
	SCComponent<HMethod> scc_root = hm2CgSCC.get(root);
	if(scc_root == null) {
	    findNoFPCgSCC(Collections.<HMethod>singleton(root), ap.fpMaxIter);
	}

	// Compute dependencies between methods; exclude skipped calls
	// and calls to already analyzed methods.  These dependencies
	// are affected by the previously analyzed methods - the
	// reason to have two steps is that we want the set of skipped
	// calls from too-large SCCs to not depend on the previous
	// analysis invocations.
	ForwardNavigator<HMethod> specFwdNavigator = new ForwardNavigator<HMethod>() {
	    public List<HMethod> next(HMethod hm) {
		final SCComponent<HMethod> hm_scc = hm2CgSCC.get(hm);
		assert hm_scc != null : " problem with " + hm + " root = " + root;
		final boolean shouldCheckNoFPCgSCC = (hm_scc != null) && noFPCgSCCs.contains(hm_scc);
		return 
		(List<HMethod>)
		DSUtil.<HMethod>filterColl
		(cgNav.next(hm),
		 new Predicate<HMethod>() {
		     public boolean check(HMethod callee) {
			 SCComponent<HMethod> callee_scc = hm2CgSCC.get(callee);
			 assert callee_scc != null;
			 // for no FP SCCs, no dependencies on same-scc methods
			 if(shouldCheckNoFPCgSCC && (callee_scc == hm_scc)) return false;
			 // no dependency on already-analyzed callees
			 if(hm2result.get(callee) != null) return false;
			 return true;
		     }
		 },
		 new LinkedList<HMethod>());
	    }
	};
	
	DiGraph<HMethod> methodDepDiGraph =
	    DiGraph.<HMethod>diGraph(Collections.<HMethod>singleton(root),
						 specFwdNavigator);
	TopSortedCompDiGraph<HMethod> tsCompDiGraph = 
	    new TopSortedCompDiGraph<HMethod>(methodDepDiGraph);
	
	return 
	    new Pair<Navigator<HMethod>,TopSortedCompDiGraph<HMethod>>
	    (methodDepDiGraph.getNavigator(),
	     tsCompDiGraph);
    }


    // Add to the set of unanalyzable CALLs all CALLs from scc-methods
    // that may invoke a method from scc.
    private void addUnanalyzedIntraSCCCalls(SCComponent<HMethod> scc) {
	System.out.println("SCC with unanalyzed intra-SCC CALLs");
	printMethodSCC(pwStdOut, scc, null, null);
	for(HMethod hm : scc.nodes()) {
	    for(CALL cs : cg.getCallSites(hm)) {
		if(anyCalleeInScc(cs, scc)) {
		    unanalyzedIntraSCCCalls.add(cs);
		}
	    }
	}
    }
    private boolean anyCalleeInScc(CALL cs, SCComponent<HMethod> scc) {
	for(HMethod callee : getCallGraph().calls(Util.quad2method(cs), cs)) {
	    if(scc.nodes().contains(callee)) return true;
	}
	return false;
    }
    
    // Set of CALLs that we treat as unanalyzable because they invoke
    // callees from the same, too big SCC
    private Set<CALL> unanalyzedIntraSCCCalls = new HashSet<CALL>();

    boolean shouldSkipDueToFPLimit(CALL cs, HMethod callee) {
	return unanalyzedIntraSCCCalls.contains(cs);
    }


    private final PrintWriter pwStdOut = new PrintWriter(System.out, true);

    private Navigator<HMethod> interProcDepNav;


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
	printMethodSCC(pwStdOut, scc, null, null);
	Timer timer = new Timer();
	
	_analyzeSCC(scc, ap);
	
	System.out.println("Total analysis time for SCC" + scc.getId() + " : " + timer);
	System.out.println();
    }


    private int maxIntraSccIter(SCComponent<HMethod> scc, int fpMaxIter) {
	int s = scc.size();
	int k = (s <= 5) ? 8 : (s <= 10) ? 4 : (s <= 15) ? 2 : (s <= 30) ? 1 : 0;
	// there seems to be good to iterate more over some special
	// nests of mutually-recursive methods.
	for(HMethod hm : scc.nodes()) {
	    String hmName = hm.getName();
	    String hmClassName = hm.getDeclaringClass().getName();
	    if((hmName.equals("write") || hmName.equals("read")) &&
	       hmClassName.startsWith("java.io.")) {
		k *= 5;
		break;
	    }
	}
	return k * fpMaxIter * scc.size();
    }

    private void _analyzeSCC(SCComponent<HMethod> scc, AnalysisPolicy ap) {
	// refuze to analyze native methods
	if(PAUtil.isNative(DSUtil.getFirst(scc.nodes())))
	    return;

	boolean skipSameSccCalls = false;

	for(HMethod hm : scc.nodes()) {
	    hm2md.put(hm, (new MethodData(new IntraProc(hm, ap.flowSensitivity, this), ap)));
	    assert hm2result.get(hm) == null;
	}

	if(!scc.exits().isEmpty()) {
	    workSet.addAll(scc.exits());
	}
	else {
	    workSet.addAll(scc.nodes());
	}

	boolean mustCheck = scc.isLoop();
	int nbIter = 0;
	int maxIter = maxIntraSccIter(scc, ap.fpMaxIter);

	while(!workSet.isEmpty()) {
	    HMethod hm = workSet.extract();
	    MethodData md = hm2md.get(hm);

	    if(mustCheck) {
		if(!skipSameSccCalls) {
		    if(nbIter >= maxIter) {
			System.out.println("Too many intra-scc iterations -> skip all calls to same SCC methods");
			skipSameSccCalls = true;
			addUnanalyzedIntraSCCCalls(scc);
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

	    //// flow sens. -> insens.  However, worse performances: flow insensitivity
	    //// increases a lot the sets of mutually dependent constraints ...
 	    // degradePrecision(hm, md);
	    md.iterCount++;  // for stats only

	    InterProcAnalysisResult result = analyzeMethod(hm, md);
	    if(mustCheck) {
		if(newResult(hm, result) && !skipSameSccCalls) {
		    System.out.println("CHANGED");
		    for(HMethod caller : interProcDepNav.prev(hm)) {
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
    }


    private InterProcAnalysisResult analyzeMethod(HMethod hm, MethodData md) {
	System.out.println("Analyze (" + md.iterCount + ") \"" + hm + "\"" + "; " + hm.getDescriptor());
	
	Timer timerSolve = new Timer();
	FullAnalysisResult far = md.intraProc.fullSolve();
	timerSolve.stop();

	Timer timerSimplify = new Timer();
	InterProcAnalysisResult res = far;
	displayStats(res, " --trimUnreachable-> ");

	res = GraphOptimizations.trimUnreachable(far, nodeRep.getParamNodes(hm));
	displayStats(res, " --trimUnaffected--> ");

	res = GraphOptimizations.trimUnaffected(res);
	displayStats(res, " --unifyLoads------> ");

	res = GraphOptimizations.unifyLoads(res);
	displayStats(res, " --unifyGblEsc-----> ");

	res = GraphOptimizations.unifyGblEsc(res, nodeRep);
	displayStats(res, "");

	if(Flags.USE_FRESHEN_TRICK) {
	    res = GraphOptimizations.freshen(res);
	}

	System.out.println("\tMethod analysis time: " + timerSolve + " + " + timerSimplify);
	return res;
    }

    private void displayStats(InterProcAnalysisResult res, String suffix) {
	if(Flags.SHOW_TRIM_STATS)
	    System.out.println("\t\t" + PAUtil.graphSizeStats(res) + suffix);
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

	if(oldRes.eomWrites().addAll(res.eomWrites())) {
	    System.out.print("eomWrites : ");
	    newResult = true;
	}
	
	if(newResult) {
	    oldRes.invalidateCaches();
	}

	System.out.println("\t\t" + PAUtil.graphSizeStats(oldRes, true));

	return newResult;
    }


    private static void printMethodSCCs(PrintWriter pw, List<SCComponent<HMethod>> listSCCs, HMethod hmRoot,
					Map<HMethod,SCComponent<HMethod>> hm2scc,
					CallGraph cg) {
	pw.println("SCC of methods for the analysis of " + hmRoot);
	for(SCComponent<HMethod> scc : listSCCs) {
	    printMethodSCC(pw, scc, hm2scc, cg);
	}
	pw.flush();
    }

    private static void printMethodSCC(PrintWriter pw,
				       SCComponent<HMethod> scc,
				       Map<HMethod,SCComponent<HMethod>> hm2scc,
				       CallGraph cg) {
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


    public boolean hasAnalyzedCALL(CALL cs, HMethod callee) {
	// 0. Take care against un-analyzed callers -> in that case,
	// the CALL was definitely not analyzed
	if(!hm2result.containsKey(Util.quad2method(cs))) return false;
	// 1. For very-large SCCs of mutually-recursive methods, we skip calls to same-scc methods.
	if(unanalyzedIntraSCCCalls.contains(cs)) return false;
	// 2. Calls to exception initializers were considered unanalyzable (for efficiency).
	if(PAUtil.exceptionInitializerCall(cs)) return false;
	// 3. Calls to certain callees are specially modeled.
	if(SpecialInterProc.canModel(callee)) return false;
	// Note: 2 & 3 were actually modeled by the analysis somehow.
	// Still, they were not properly analyzed, and this fact is
	// important for several optimizations.  Let them know this.

	return super.hasAnalyzedCALL(cs, callee);
    }


    protected void finalize() {
	TypeFilter.releaseClassHierarchy();
    }

}
