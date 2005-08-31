// GraphOptimizations.java, created Thu Jul 14 09:56:42 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Set;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

import jpaul.Graphs.DiGraph;
import jpaul.DataStructs.CompoundIterable;
import jpaul.DataStructs.DSUtil;
import jpaul.DataStructs.Pair;

import jpaul.Misc.Function;
import jpaul.Misc.Predicate;
import jpaul.Misc.SetMembership;

import net.cscott.jutil.DisjointSet;

import harpoon.ClassFile.HField;


/**
 * <code>GraphOptimizations</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: GraphOptimizations.java,v 1.3 2005-08-31 02:37:54 salcianu Exp $
 */
public class GraphOptimizations {

    private static final boolean VERBOSE = false;

    // UNIFY LOADS: TO BE CALLED AT THE END OF THE ANALYSIS OF EACH SCC
    static InterProcAnalysisResult unifyLoads(InterProcAnalysisResult ar) {
	boolean changed = false;
	
	DisjointSet<PANode> uf = new DisjointSet<PANode>();
	Function<PANode,PANode> nodeConv = null;

	PAEdgeSet O = ar.eomO();
	while(true) {
	    boolean newInfo = unifyLoads(O, uf);
	    if(!newInfo) break;
	    changed = true;
	    nodeConv = getNodeConverter(uf);
	    O = compressEdges(O, nodeConv);
	    
	    if(VERBOSE) System.out.println("Unified edges:\n" + O);
	}

	if(!changed)
	    return ar;

	return 
	    new IPResTupleImpl
	    (compressEdges(ar.eomI(), nodeConv),
	     O, // already compressed
	     compressNodes(ar.eomDirGblEsc(), nodeConv),
	     compressNodes(ar.eomAllGblEsc(), nodeConv),
	     compressNodes(ar.ret(), nodeConv),
	     compressNodes(ar.ex(),  nodeConv),
	     compressAbstractFields(ar.eomWrites(), nodeConv));
    }


    private static boolean unifyLoads(PAEdgeSet O, DisjointSet<PANode> uf) {
	boolean changed = false;
	for(PANode node : O.sources()) {
	    for(HField hf : O.fields(node)) {
		Collection<PANode> loadNodes = O.pointedNodes(node, hf);
		if(loadNodes.size() > 1) {
		    changed = true;
		    PANode firstLoadNode = null;
		    for(PANode loadNode : loadNodes) {
			if(firstLoadNode == null) {
			    firstLoadNode = loadNode;
			}
			else {
			    uf.union(firstLoadNode, loadNode);
			}
		    }
		}
	    }
	}
	return changed;
    }


    static PAEdgeSet compressEdges(PAEdgeSet edges, Function<PANode,PANode> nodeConv) {
	PAEdgeSet newEdges = DSFactories.edgeSetFactory.create();
	for(PANode source : edges.sources()) {
	    PANode newSource = nodeConv.f(source);
	    for(HField hf : edges.fields(source)) {
		for(PANode target : edges.pointedNodes(source, hf)) {
		    PANode newTarget = nodeConv.f(target);
		    newEdges.addEdge(newSource, hf, newTarget, true);
		}
	    }
	}
	return newEdges;
    }


    private static Function<PANode,PANode> getNodeConverter(final DisjointSet<PANode> uf) {
	// For each equivalence class (modeled by a (node representative),
	// compute the load node with the smallest hashcode.

	// TODO: maybe use a map factory here
	final Map<PANode,PANode> repr2min = new HashMap<PANode,PANode>();
	for(Map.Entry<PANode,PANode> entry : uf.asMap().entrySet()) {
	    PANode node = entry.getKey();
	    PANode repr = entry.getValue();
	    PANode min = repr2min.get(repr);
	    if((min == null) || (min.getId() > node.getId())) {
		repr2min.put(repr, node);
	    }
	}

	return 
	    new Function<PANode,PANode>() {
		public PANode f(PANode node) {
		    PANode repr = uf.find(node);
		    PANode min  = repr2min.get(repr);
		    if(min == null) return repr;
		    return min;
		}
	    };
    }

    static Set<PANode> compressNodes(Set<PANode> set, Function<PANode,PANode> nodeConv) {
	//return IntraProc.<PANode>mapSet(set, uf, DSFactories.nodeSetFactory);
	Set<PANode> newSet = DSFactories.nodeSetFactory.create();
	for(PANode node : set) {
	    newSet.add(nodeConv.f(node));
	}
	return newSet;
    }

    static Set<Pair<PANode,HField>> compressAbstractFields(Set<Pair<PANode,HField>> writes, Function<PANode,PANode> nodeConv) {
	if(!Flags.RECORD_WRITES) return writes;

	Set<Pair<PANode,HField>> newWrites = DSFactories.abstractFieldSetFactory.create();
	for(Pair<PANode,HField> abstractField : writes) {
	    newWrites.add(new Pair<PANode,HField>(nodeConv.f(abstractField.left),
						  abstractField.right));
	}
	return newWrites;
    }


    /* // it refuses to compile for some unknown reasons
    private static <T> Set<T> mapSet(Set<T> set, DisjointSet<T> uf, SetFactory<T> setFactory) {
	Set<T> newSet = setFactory.create();
	for(T elem : set) {
	    newSet.add(uf.find(elem));
	}
	return newSet;
    }
    */


    // TRIM UNREACHABLE: TO BE CALLED AT THE END OF THE ANALYSIS OF EACH METHOD
    static InterProcAnalysisResult trimUnreachable(InterProcAnalysisResult ar,
						   Collection<PANode> paramNodes) {
	// Find all nodes reachable from parameters or from returned /
	// thrown nodes.  Keep only these nodes (the others should not
	// be visible to the caller).
	Set<PANode> reachable = getReachable(ar, paramNodes);
	Predicate<PANode> reachPred = new SetMembership<PANode>(reachable);

	return 
	    new IPResTupleImpl(filterEdges(ar.eomI(), reachPred), // inside  edges
			       filterEdges(ar.eomO(), reachPred), // outside edges
			       filterSet(ar.eomDirGblEsc(), reachPred),
			       ar.eomAllGblEsc(),  // this component should not be trimmed
			       ar.ret(),   // all of these nodes are reachable from callee
			       ar.ex(),    // all of these nodes are reachable from callee
			       filterAbstractFields(ar.eomWrites(), reachPred));
    }


    private static Set<PANode> getReachable(InterProcAnalysisResult ar, Collection<PANode> paramNodes) {
	Collection<PANode> roots = DSUtil.unionColl(ar.ret(), ar.ex(), paramNodes);
	return DiGraph.union(ar.eomI(), ar.eomO()).transitiveSucc(roots);
	// ^ beautiful and clear! compare with commented code below :)

	/*
	Set<PANode> roots = new HashSet<PANode>();
	roots.addAll(ar.ret());
	roots.addAll(ar.ex());
	roots.addAll(nodeRep.getParamNodes(hm));

	LinkedList<PANode> workList = new LinkedList<PANode>();
	workList.addAll(reachable);

	while(!workList.isEmpty()) {
	    PANode node = workList.removeFirst();
	    for(int i = 0; i < 2; i++) {
		PAEdgeSet edges = (i == 0) ? ar.eomI() : ar.eomO();
		for(HField hf : edges.fields(node)) {
		    for(PANode dest : edges.pointedNodes(node, hf)) {
			if(reachable.add(dest)) {
			    workList.addLast(dest);
			}
		    }
		}
	    }
	}

	return reachable;
	*/
    }


    private static PAEdgeSet filterEdges(PAEdgeSet edges, Predicate<PANode> pred) {
	PAEdgeSet edges2 = DSFactories.edgeSetFactory.create();
	for(PANode node : edges.sources()) {
	    if(!pred.check(node)) continue;
	    for(HField hf : edges.fields(node)) {
		Collection<PANode> dest2 = 
		    DSUtil.<PANode>filterColl(edges.pointedNodes(node, hf),
					      pred,
					      new LinkedList<PANode>());
		edges2.addEdges(node, hf, dest2, true);
	    }
	}
	return edges2;
    }


    private static Set<PANode> filterSet(Set<PANode> nodes, Predicate<PANode> pred) {
	return 
	    (Set<PANode>)
	    DSUtil.<PANode>filterColl(nodes,
				      pred,
				      DSFactories.nodeSetFactory.create());
    }

    
    private static Set<Pair<PANode,HField>> filterAbstractFields(Set<Pair<PANode,HField>> abstrFields, final Predicate<PANode> pred) {
	if(!Flags.RECORD_WRITES) return abstrFields;

	return
	    (Set<Pair<PANode,HField>>)
	    DSUtil.filterColl
	    (abstrFields,
	     new Predicate<Pair<PANode,HField>>() {
		public boolean check(Pair<PANode,HField> abstrField) {
		    PANode node = abstrField.left;
		    return (node == null) || (pred.check(node));
		}
	     },
	     DSFactories.abstractFieldSetFactory.create());
    }



    // TRIM UNAFFECTED: TO BE CALLED AT THE END OF THE ANALYSIS OF EACH METHOD
    static InterProcAnalysisResult trimUnaffected(InterProcAnalysisResult ar) {

	// An ESSENTIAL load node is a load node that is placed on
	// path leading to a load node that is returned from the
	// method, globally lost, or is involved (source or target) in
	// a newly created reference (inside edge).
	// If we are interested in MUTATION, then all mutated nodes are ESSENTIAL too.
	Iterable<PANode> affectedRoots = 
	    DSUtil.<PANode>unionIterable
	    (Arrays.<Iterable<PANode>>asList(ar.ret(),
					     ar.ex(),
					     ar.eomI().allNodes(),
					     ar.eomAllGblEsc()));
    
	if(Flags.RECORD_WRITES) {
	    // for mutation analysis, mutated nodes are essential too
	    affectedRoots = 
		DSUtil.<PANode>unionIterable
		(affectedRoots,
		 DSUtil.<Pair<PANode,HField>,PANode>mapIterable
		 (ar.eomWrites(), Pair.<PANode,HField>leftProj()));
	}

	// an over-approximation of the essential load nodes (e.g., it
	// may also contains inside nodes).
	final Set<PANode> essentialLoads = 
	    ar.eomO().transitivePred(DSUtil.<PANode>iterable2coll(affectedRoots));

	if(essentialLoads.containsAll(DSUtil.<PANode>iterable2coll(ar.eomO().allNodes()))) {
	    if(VERBOSE) System.out.println("No point to do any trimming!");
	    return ar;
	}

	Predicate<PANode> essPred = new Predicate<PANode>() {
	    public boolean check(PANode node) {
		switch(node.kind) {
		    case LOAD: return essentialLoads.contains(node);
		    default:   return true;
		}
	    }
	};

	if(VERBOSE) System.out.println("before trimUnaffected:\n" + ar);

	InterProcAnalysisResult ipResult =
	    new IPResTupleImpl(filterEdges(ar.eomI(), essPred), // inside  edges
			       filterEdges(ar.eomO(), essPred), // outside edges
			       filterSet(ar.eomDirGblEsc(), essPred),
			       ar.eomAllGblEsc(),
			       ar.ret(),
			       ar.ex(),
			       ar.eomWrites());

	if(VERBOSE) System.out.println("after trimUnaffected:\n" + ipResult);

	return ipResult;
    }


    static InterProcAnalysisResult unifyGblEsc(final InterProcAnalysisResult ar, NodeRepository nodeRep) {
	final PANode gblNode = nodeRep.getGlobalNode();

	Function<PANode,PANode> compactor = 
	    new Function<PANode,PANode>() {
		public PANode f(PANode node) {
		    switch(node.kind) {
			case PARAM:  return node;
			default:
			if(ar.eomAllGblEsc().contains(node)) {
			    return gblNode;
			}
			else {
			    return node;
			}
		    }
		}
	    };

	return 
	    new IPResTupleImpl
	    (compressEdges(ar.eomI(), compactor),
	     compressEdges(ar.eomO(), compactor),
	     compressNodes(ar.eomDirGblEsc(), compactor),
	     ar.eomAllGblEsc(),
	     compressNodes(ar.ret(), compactor),
	     compressNodes(ar.ex(),  compactor),
	     compressAbstractFields(ar.eomWrites(), compactor));
    }


    static InterProcAnalysisResult freshen(final InterProcAnalysisResult ar) {
	return _freshen(ar, freshConv);
    }

    static Function<PANode,PANode> freshConv = 
	new Function<PANode,PANode>() {
		public PANode f(PANode node) { 
		    assert !node.isFresh();
		    return node.other();
		}
	};

    public static InterProcAnalysisResult unFreshen(final InterProcAnalysisResult ar) {
	return _freshen(ar, unFreshConv);
    }

    static Function<PANode,PANode> unFreshConv = 
	new Function<PANode,PANode>() {
	    public PANode f(PANode node) {
		return node.isFresh() ? node.other() : node;
	    }
	};
    

    static InterProcAnalysisResult _freshen(final InterProcAnalysisResult ar, Function<PANode,PANode> map) {
	return
	    new IPResTupleImpl
	    (compressEdges(ar.eomI(), map),
	     compressEdges(ar.eomO(), map),
	     compressNodes(ar.eomDirGblEsc(), map),
	     compressNodes(ar.eomAllGblEsc(), map),
	     compressNodes(ar.ret(), map),
	     compressNodes(ar.ex(), map),
	     compressAbstractFields(ar.eomWrites(), map));
    }

}
