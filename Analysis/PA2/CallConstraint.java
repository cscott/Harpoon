// CallConstraint.java, created Tue Jun 28 15:23:09 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Collections;
import java.util.Collection;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.Queue;

import harpoon.IR.Quads.CALL;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;

import jpaul.Constraints.Var;
import jpaul.Constraints.SolAccessor;
import jpaul.Constraints.Constraint;

import jpaul.DataStructs.Relation;
import jpaul.DataStructs.DSUtil;
import jpaul.DataStructs.WorkSet;
import jpaul.DataStructs.VerboseWorkSet;
import jpaul.DataStructs.WorkList;
import jpaul.DataStructs.DisjointSet;

import jpaul.Graphs.DiGraph;


/**
 * <code>CallConstraint</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: CallConstraint.java,v 1.2 2005-08-16 22:41:57 salcianu Exp $
 */
class CallConstraint extends Constraint {

    public CallConstraint(CALL cs,
			  HMethod callee,
			  LVar v_res, LVar v_ex,
			  List<LVar> v_args,
			  IVar v_preI,
			  FVar v_preF,
			  IVar v_postI,
			  FVar v_postF,
			  OVar v_O,
			  EVar v_E,
			  PointerAnalysis pa) {

	this(cs,
	     callee,
	     (NodeSetVar) v_res, (NodeSetVar) v_ex,
	     convertTypes(v_args),
	     (EdgeSetVar) v_preI,
	     (NodeSetVar) v_preF,
	     (EdgeSetVar) v_postI,
	     (NodeSetVar) v_postF,
	     (EdgeSetVar) v_O,
	     (NodeSetVar) v_E,
	     pa);
    }

    private static final List<NodeSetVar> convertTypes(List<LVar> v_args) {
	LinkedList<NodeSetVar> res = new LinkedList<NodeSetVar>();
	for(LVar v : v_args) {
	    res.addLast((NodeSetVar) v);
	}
	return res;
    }

    public CallConstraint(CALL cs,
			  HMethod callee,
			  NodeSetVar v_res, NodeSetVar v_ex,
			  List<NodeSetVar> v_args,
			  EdgeSetVar v_preI,
			  NodeSetVar v_preF,
			  EdgeSetVar v_postI,
			  NodeSetVar v_postF,
			  EdgeSetVar v_O,
			  NodeSetVar v_E,
			  PointerAnalysis pa) {
	
	this.cs = cs;
	this.callee = callee;
	this.v_res = v_res;
	this.v_ex  = v_ex;
	this.v_args = v_args;
	this.v_preI  = v_preI;
	this.v_preF  = v_preF;
	this.v_postI = v_postI;
	this.v_postF = v_postF;
	this.v_O = v_O;
	this.v_E = v_E;
	this.pa = pa;

	in = new ArrayList<Var>();
	in.addAll(v_args);
	in.add(v_preI);
	in.add(v_preF);

	out = new ArrayList<Var>();
	if(v_res != null)
	    out.add(v_res);
	if(v_ex != null)
	    out.add(v_ex);

	out.add(v_postI);
	out.add(v_postF);
	out.add(v_O);
	out.add(v_E);
    }


    private final CALL cs;
    private final HMethod callee;
    private final NodeSetVar v_res;
    private final NodeSetVar v_ex;
    private final List<NodeSetVar> v_args;
    private final EdgeSetVar v_preI;
    private final NodeSetVar v_preF;
    private final EdgeSetVar v_postI;
    private final NodeSetVar v_postF;
    private final EdgeSetVar v_O;
    private final NodeSetVar v_E;

    private final PointerAnalysis pa;

    private final Collection<Var> in;
    private final Collection<Var> out;
    
    public Collection<Var> in()  { return in; }
    public Collection<Var> out() { return out; }

   
    public Constraint rewrite(DisjointSet uf) {
	List<NodeSetVar> new_v_args = new LinkedList<NodeSetVar>();
	for(NodeSetVar lvar : v_args) {
	    new_v_args.add((NodeSetVar) uf.find(lvar));
	}

	return 
	    new CallConstraint(cs,
			       callee,
			       (NodeSetVar) uf.find(v_res),
			       (NodeSetVar) uf.find(v_ex),
			       new_v_args,
			       (EdgeSetVar) uf.find(v_preI),
			       (NodeSetVar) uf.find(v_preF),
			       (EdgeSetVar) uf.find(v_postI),
			       (NodeSetVar) uf.find(v_postF),
			       (EdgeSetVar) uf.find(v_O),
			       (NodeSetVar) uf.find(v_E),
			       pa);
    }
    
    public String toString() {
	return 
	    "\n<" + v_res + "," + v_ex + "> := call(" + v_args + 
	    ")\n\t" + callee + "\n\tinsd: " + v_preI + " -> " + v_postI + 
	    ";  outsd: " + v_O + ";  escpd: " + v_preF + " -> " + v_postF;
    }

    public int cost() { return Constraint.HIGH_COST; }


    private void treatAsUnanalyzable(SolAccessor sa) {
	// propagate edges and escape info before -> after
	flow(sa, v_preI, v_postI);
	flow(sa, v_preF, v_postF);

	// (globally) escape all arguments
	for(NodeSetVar v_arg : v_args) {
	    flow(sa, v_arg, v_E);
	    flow(sa, v_arg, v_postF);
	}

	// make retval and retex point to the GBL node
	if(v_res != null) {
	    sa.join(v_res, Collections.<PANode>singleton(pa.getNodeRep().getGlobalNode()));
	}
	if(v_ex != null) {
	    sa.join(v_ex,  Collections.<PANode>singleton(pa.getNodeRep().getGlobalNode()));
	}
    }

    private void flow(SolAccessor sa, Var v1, Var v2) {
	Object o1 = sa.get(v1);
	if(o1 != null)
	    sa.join(v2, o1);
    }
    
    public void action(SolAccessor sa) {

	Flags.VERBOSE_CALL = false;
	if(Flags.SHOW_INTRA_PROC_RESULTS &&
	   callee.getDeclaringClass().getName().equals("java.lang.String") &&
	   callee.getName().equals("<init>") &&
	   (cs.getLineNumber() == 1391)) {
	    
	    System.out.println("\n\nUUUU\n\n");
	    Flags.VERBOSE_CALL = true;

	}

	if(pa.shouldSkipDueToFPLimit(cs, callee)) {
	    treatAsUnanalyzable(sa);
	}
	
	if(!cs.isStatic()) {
	    Set<PANode> receivers = (Set<PANode>) sa.get(v_args.get(0));
	    // this method was not invoked during program execution
	    // (imprecission in the call graph)
	    if((receivers == null) || receivers.isEmpty()) return;
	}

	calleeRes = pa.getCurrentInterProcResult(callee);
	if(calleeRes == null) {
	    return;
	}

	this.sa = sa;
	this.mu = DSFactories.mappingFactory.create();
	this.deltaMu = DSFactories.mappingFactory.create();

	this.deltaI = DSFactories.edgeSetFactory.create();
	this.deltaO = DSFactories.edgeSetFactory.create();
	this.deltaG = DSFactories.nodeSetFactory.create();

	this.calleeI    = calleeRes.eomI();
	this.revCalleeI = calleeRes.revEomI();
	this.calleeO    = calleeRes.eomO();

	this.preI = PAUtil.fixNull((PAEdgeSet) sa.get(v_preI));
	this.escapedNodes = DSFactories.nodeSetFactory.create
	    (PAUtil.fixNull((Set<PANode>) sa.get(v_preF)));

	this.alreadyMappedToEsc = DSFactories.nodeSetFactory.create();

	if(Flags.VERBOSE_CALL) {
	    System.out.println("\n\ncs = " + cs);
	    System.out.println("callee = \"" + callee + "\"");
	    System.out.println("calleeRes:" + calleeRes);
	    System.out.println("revEomI(calleeRes):" + calleeRes.revEomI());
	    System.out.println("\npreI:" + preI);
	}

	initMu();
	while(!workQueue.isEmpty()) {
	    PANode node = workQueue.extract();
	    Collection<PANode> newMappings = deltaMu.getValues(node);
	    deltaMu.removeKey(node);
	    extendMu(node, newMappings);
	}

	if(!mu.isEmpty()) {
	    if(Flags.VERBOSE_CALL) System.out.println("mu = " + mu);
	}

	Set<PANode> deltaDirGblEsc = projectSet(calleeRes.eomDirGblEsc());
	Set<PANode> retNodes = projectSet(calleeRes.ret());
	Set<PANode> exNodes  = projectSet(calleeRes.ex());

	sa.join(v_postI, adjust(deltaI));
	sa.join(v_O,     adjust(deltaO));
	sa.join(v_postF, adjust(escapedNodes));
	sa.join(v_E,     adjust(deltaDirGblEsc));
	if(v_res != null) {
	    sa.join(v_res,   adjust(retNodes));
	}
	if(v_ex != null) {
	    sa.join(v_ex,    adjust(exNodes));
	}

	// enable GC
	this.sa = null;
	this.mu = null;
	this.revMu = null;
	this.calleeRes = null;
	this.deltaI = null;
	this.deltaO = null;
	this.deltaG = null;
	this.calleeI = null;
	this.calleeO = null;
	this.revCalleeI = null;
	this.preI = null;
	this.escapedNodes = null;
	this.alreadyMappedToEsc = null;
    }

    private PAEdgeSet adjust(PAEdgeSet edges) {
	return 
	    Flags.USE_FRESHEN_TRICK ?
	    GraphOptimizations.compressEdges(edges, GraphOptimizations.unFreshConv) :
	    edges;
    }

    private Set<PANode> adjust(Set<PANode> nodes) {
	return 
	    Flags.USE_FRESHEN_TRICK ?
	    GraphOptimizations.compressNodes(nodes, GraphOptimizations.unFreshConv) :
	    nodes;
    }


    private PAEdgeSet deltaI;
    private PAEdgeSet deltaO;
    private Set<PANode> deltaG;

    private PAEdgeSet calleeI;
    private PAEdgeSet revCalleeI;
    private PAEdgeSet calleeO;

    private PAEdgeSet preI;

    private Set<PANode> escapedNodes;

    private Set<PANode> alreadyMappedToEsc;

    private InterProcAnalysisResult calleeRes;
    private SolAccessor sa;

    private Relation<PANode,PANode> mu;
    private Relation<PANode,PANode> revMu;
    private Relation<PANode,PANode> deltaMu;

    private WorkSet<PANode> workQueue = 
	Flags.VERBOSE_CALL ? 
	(WorkSet<PANode>) (new VerboseWorkSet<PANode>(new WorkList<PANode>(), "workQueue: ")) :
	(WorkSet<PANode>) (new WorkList<PANode>());

    /*
    private static class NewInfo {
	public final PANode node;
	public final Collection<PANode> newMappings;
	
	public NewInfo(PANode node, Collection<PANode> newMappings) {
	    this.node = node;
	    this.newMappings = newMappings;
	}
    }
    */

    
    private void initMu() {
	// map each formal param to the corresponding arguments
	List<PANode> formals = pa.getNodeRep().getParamNodes(callee);
	assert v_args.size() == formals.size();	    
	Iterator<NodeSetVar> it_arg = v_args.iterator();
	for(PANode pNode : pa.getNodeRep().getParamNodes(callee)) {
	    NodeSetVar v_arg = it_arg.next();
	    if(Flags.USE_FRESHEN_TRICK) {
		assert !pNode.isFresh();
		pNode = pNode.other();
	    }
	    mu.addAll(pNode, PAUtil.fixNull((Set<PANode>) sa.get(v_arg)));
	    // make sure param nodes are at the head of the worklist
	    workQueue.add(pNode);
	}

	// map some nodes to themselves
	for(PANode node : calleeRes.getAllNodes()) {
	    switch(node.kind) {
	    case INSIDE:
	    case GBL:
	    case NULL:
	    case CONST:
	    case IMM:
		mu.add(node, node);
		break;
	    case PARAM: // params are fully disambiguated
	    case LOAD:  // we'll decide later which LOAD nodes to preserve
		; // do nothing
	    }
	}

	revMu = mu.revert(DSFactories.mappingFactory.create());

	for(PANode node : mu.keys()) {
	    deltaMu.addAll(node, mu.getValues(node));
	    workQueue.add(node);
	}

	if(Flags.VERBOSE_CALL) {
	    System.out.println("Initial mu = " + mu);
	}
    }

    private void extendMu(final PANode n, final Collection<PANode> newMappings) {
	if(Flags.VERBOSE_CALL) {
	    System.out.println("\nextendsMu(" + n + ", " + newMappings);
	}
	addNewEdges(n, newMappings);
	matchOutsideInside(n, newMappings);
    }

    private void addNewEdges(final PANode n, final Collection<PANode> newMappings) {

       	// filter only the newMappings that escape
	final Collection<PANode> escNewMappings = new LinkedList<PANode>();
	for(PANode mu_n : newMappings) {
	    if(PAUtil.escape(mu_n, escapedNodes)) {
		escNewMappings.add(mu_n);
	    }
	}
	final boolean newMappingsEscape = !escNewMappings.isEmpty();

	if(Flags.VERBOSE_CALL) {
	    System.out.println("escNewMappings = " + escNewMappings);
	}

	// store new nodes that escape: escape info must be propagated
	// only from these nodes (instead of over the entire graph).
	final Set<PANode> newDirEscape = DSFactories.nodeSetFactory.create();

	// ADD POTENTIALLY NEW INSIDE EDGES
	calleeI.forAllEdges
	    (n,
	     new PAEdgeSet.EdgeAction() {
		public void action(PANode n1, HField hf, PANode n2) {
		    assert n1 == n;
		    Set<PANode> mu_n2s = mu.getValues(n2);
		    if(Flags.VERBOSE_CALL) {
			showNewEdges(newMappings, hf, mu_n2s, deltaI, "DIR: ", "inside");
		    }
		    boolean newEdges = deltaI.addEdges(newMappings, hf, mu_n2s, true);
		    if(newEdges && newMappingsEscape) {
			for(PANode escNewMap : escNewMappings) {
			    Collection<PANode> P = deltaI.pointedNodes(escNewMap, hf);
			    for(PANode mu_n2 : mu_n2s) {
				if(P.contains(mu_n2)) {
				    newDirEscape.addAll(mu_n2s);
				}
			    }
			}
		    }
		}
	    });

	revCalleeI.forAllEdges
	    (n,
	     new PAEdgeSet.EdgeAction() {
		private boolean newMappingsAlreadyEscape = false;
		public void action(PANode n1, HField hf, PANode n2) {
		    assert n1 == n;
		    Set<PANode> mu_n2s = mu.getValues(n2);
		    if(Flags.VERBOSE_CALL) {
			showNewEdges(mu_n2s, hf, newMappings, deltaI, "REV: ", "inside");
		    }
		    // the reverse order is NOT a mistake
		    if(deltaI.addEdges(mu_n2s, hf, newMappings, true)) {
			if(!newMappingsAlreadyEscape &&
			   PAUtil.escapeAny(mu_n2s, escapedNodes)) {
				newDirEscape.addAll(newMappings);
				newMappingsAlreadyEscape = true;
			}
		    }
		}
	    });

	// PROPAGE NEW ESCAPE INFO
	Set<PANode> newEscapedNodes = 
	    PAUtil.findNewEsc(newDirEscape, 
			      DiGraph.union(preI, deltaI),
			      escapedNodes);

	if(Flags.VERBOSE_CALL && !newEscapedNodes.isEmpty()) {
	    System.out.println("newEscapedNodes = " + newEscapedNodes);
	}

	// IMPORT POTENTIALLY NEW OUTSIDE EDGES
	if(!escNewMappings.isEmpty()) {
	    // for any new mapping that escapes, translate corresponding
	    // outside edges from the callee to the caller.
	    for(HField hf : calleeO.fields(n)) {
		Collection<PANode> loadNodes = calleeO.pointedNodes(n, hf);
		boolean added = false;
		for(PANode mu_n : escNewMappings) {
		    if(Flags.VERBOSE_CALL) {
			showNewEdges(Collections.<PANode>singleton(mu_n), hf, loadNodes, deltaO, "newMap: ", "outside");
		    }
		    if(deltaO.addEdges(mu_n, hf, loadNodes)) {
			added = true;
		    }
		}
		if(added) {
		    addSelfMappings(loadNodes);
		}
	    }
	}

	// for any new escaped node from the result graph, examine
	// nodes mapped to it + import corresponding outside edges
	for(PANode mu_n : newEscapedNodes) {
	    for(PANode n2 : revMu.getValues(mu_n)) {
		if(alreadyMappedToEsc.add(n2)) {
		    if(Flags.VERBOSE_CALL) {
			System.out.println("new node mapped to escaped one(s) " + n2);
		    }
		    for(HField hf : calleeO.fields(n2)) {
			Collection<PANode> loadNodes = calleeO.pointedNodes(n2, hf);
			if(Flags.VERBOSE_CALL) {
			    showNewEdges(Collections.<PANode>singleton(mu_n), hf, loadNodes, deltaO, "newMap2Esc: ", "outside");
			}
			if(deltaO.addEdges(mu_n, hf, loadNodes)) {
			    addSelfMappings(loadNodes);
			}
		    }
		}
	    }
	}
    }


    private void addSelfMappings(Collection<PANode> loadNodes) {
	for(PANode node : loadNodes) {    
	    if(mu.add(node, node)) {
		if(Flags.VERBOSE_CALL) {
		    System.out.println("Newly imported load node: " + node);
		}
		revMu.add(node, node);
		deltaMu.add(node, node);
		workQueue.add(node);
	    }
	}
    }


    private void matchOutsideInside(final PANode n, final Collection<PANode> newMappings) {
	for(HField hf : calleeO.fields(n)) {
	    for(PANode nl : calleeO.pointedNodes(n, hf)) {
		boolean changed = false;
		for(PANode mu_n : newMappings) {
		    Collection<PANode> T1 = preI.pointedNodes(mu_n, hf);
		    Collection<PANode> T2 = deltaI.pointedNodes(mu_n, hf);
				    
		    for(PANode mu_nl : DSUtil.unionIterable(T1, T2)) {
			if(mu.add(nl, mu_nl)) {
			    if(Flags.VERBOSE_CALL) {
				System.out.println("New mapping: " + nl + " -> " + mu_nl + "\n\t" +
						   "out-in match: n=" + n + "; hf=" + hf);
			    }
			    deltaMu.add(nl, mu_nl);
			    revMu.add(mu_nl, nl);
			    changed = true;
			}
		    }
		}
		if(changed) {
		    workQueue.add(nl);
		}
	    }
	}
    }

    private Set<PANode> projectSet(Set<PANode> set) {
	Set<PANode> res = DSFactories.nodeSetFactory.create();
	for(PANode node : set) {
	    res.addAll(mu.getValues(node));
	}
	return res;
    }


    private void showNewEdges(Collection<PANode> n1s, HField hf, Collection<PANode> n2s, PAEdgeSet edges, String pre, String name) {
	edges = DSFactories.edgeSetFactory.create(edges);
	for(PANode n1 : n1s) {
	    for(PANode n2 : n2s) {
		if(edges.addEdge(n1, hf, n2, true)) {
		    System.out.println(pre + "New " + name + " edge: <" + n1 + ", " + hf + ", " + n2 + ">");
		}
	    }
	}
    }
    
}
