// SCComponent.java, created Mon Jan 24 19:26:30 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Vector;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Hashtable;

/**
 * <code>SCComponent</code> models a <i>Strongly connected component</i> \
 of a graph.
 * The only way to split a graph into <code>SCComponent</code>s is though
 * <code>buildSSC</code>.
 * This method is quite flexible: all it needs is a root node and a 
 * <i>Navigator</i>: an object implementing the 
 * <code>SCCoomponent.Navigator</code> interface that provides the 
 * edges coming into/going out of a given <code>Object</code>. So, it can
 * build strongly connected components even for graphs that are not built
 * up from <code>CFGraphable</code> nodes, a good example being the set of
 * methods where the edges represent the caller-callee relation (in this
 * case, the strongly connected components group together sets of mutually
 * recursive methods).
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: SCComponent.java,v 1.1.2.1 2000-01-26 06:35:47 salcianu Exp $
 */
public class SCComponent {

    /** The <code>Navigator</code> interface allows the algorithm to detect
     * (and use) the arcs from and to a certain node. This allows the
     * construction of Strongly Connected Components even for very general
     * graphs where the arcs model only a subtle semantic relation
     * (e.g. caller-callee) which is not directly stored in the structure
     * of the nodes. */
    public static interface Navigator{
	/** Returns an iterator over the predecessors of <code>node</code>. */
	public Iterator next(Object node);
	/** Returns an iterator over the successors of <code>node</code>. */
	public Iterator prev(Object node);
    }

    // The internal version of a SCC: basically the same as the external
    // one, but using Sets instead of arrays; the idea is to use the most
    // convenient format during the generation and next to convert everything
    // to the small one to save some memory ...
    private static class SCComponentInt{
	// the nodes of this SCC
	public Set nodes;
	// the successors 
	public Set next;
	// the "economic format" component
	public SCComponent comp;
	
	public SCComponentInt(){
	    nodes = new HashSet();
	    next  = new HashSet();
	    comp  = new SCComponent();
	}
    }
    
    // the set of reached nodes (to avoid reanalyzing them "ad infinitum")
    private static HashSet   reached_nodes;
    // Mapping node (Object) -> Strongly Connected Component (SCComponentInt)
    private static Hashtable node2scc;
    // The vector of the reached nodes, in the order DFS finished them
    private static Vector nodes_vector;
    private static SCComponentInt current_scc_int;
    // the navigator used in the DFS algorithm
    private static Navigator nav;


    /** Constructs the strongly connected components of the graph containing \
	all the nodes reachable from <code>root</code> through the edges \
	indicated by <code>navigator</code>. */
    public static SCComponent buildSCC(Object root, final Navigator navigator){
	Vector scc_vector = new Vector();

	// STEP 1: compute the finished time of each node in a DFS exploration.
	// At the end of this step, nodes_vector will contain all the reached
	// nodes, in the order of their "finished" time. 
	nav = navigator;
	reached_nodes = new HashSet();
	nodes_vector  = new Vector();
	DFS_first(root);

	// STEP 2. build the SCCs by doing a DFS in the reverse graph.
	node2scc = new Hashtable();
	// "in reverse" navigator
	nav = new Navigator(){
		public Iterator next(Object node){
		    return navigator.prev(node);
		}
		public Iterator prev(Object node){
		    return navigator.next(node);
		}
	    };
	// Explore the nodes in the decreasing order of their finishing time.
	// This phase will create the SCCs (big format) and initialize the
	// node2scc mapping (but it won't set the inter-SCC edges). Also,
	// the SCCs are put in scc_vector.
	reached_nodes.clear();
	int nb_nodes = nodes_vector.capacity();
	for(int i = nb_nodes - 1; i >= 0; i--){
	    Object node = nodes_vector.elementAt(i);
	    if(node2scc.get(node) == null){
		current_scc_int = new SCComponentInt();
		scc_vector.add(current_scc_int);
		DFS_second(node);
	    }
	}

	// Put the edges between the SCCs.
	int nb_scc = scc_vector.capacity();
	for(int i = 0; i < nb_scc; i++){
	    SCComponentInt comp = (SCComponentInt) scc_vector.elementAt(i);
	    Iterator it = comp.nodes.iterator();
	    while(it.hasNext()){
		Object node = it.next();
		Iterator it_edges = navigator.next(node);
		while(it_edges.hasNext()){
		    Object node2 = it_edges.next();
		    SCComponent comp2 = 
			((SCComponentInt)node2scc.get(node2)).comp;
		    comp.next.add(comp2);
		}
	    }
	}
	
	// Convert the big format SCCs into the compressed format SCCs.
	for(int i = 0; i < nb_scc ; i++){
	    SCComponentInt compInt = (SCComponentInt) scc_vector.elementAt(i);
	    SCComponent comp = compInt.comp;
	    comp.nodes = compInt.nodes.toArray();
	    comp.next = (SCComponent[]) compInt.next.toArray();
	}

	// Save the root SSC somewhere before activating the GCC.
	SCComponent root_scc = ((SCComponentInt)node2scc.get(root)).comp;
	
	nav             = null; // enable the GC
	nodes_vector    = null;
	reached_nodes   = null;
	node2scc        = null;
	current_scc_int = null;

	return root_scc;
    }

    public static void DFS_first(Object node){
	// do not analyze nodes already reached
	if(reached_nodes.contains(node)) return;

	reached_nodes.add(node);
	Iterator it = nav.next(node);
	while(it.hasNext())
	    DFS_first(it.next());
	nodes_vector.add(node);
    }

    public static void DFS_second(Object node){
	if(reached_nodes.contains(node)) return;

	reached_nodes.add(node);
	node2scc.put(node,current_scc_int);
	current_scc_int.nodes.add(node);
	Iterator it = nav.next(node);
	while(it.hasNext())
	    DFS_second(it.next());
    }
    

    // The nodes of this SCC (Strongly Connected Component).
    private Object[] nodes;
    // The successors.
    private SCComponent[] next;
    // The only way to produce SCCs is through SCComponent.buildSSC !
    private SCComponent(){}
    
    /** Returns the number of successors. */
    public final int nextLength(){
	return next.length;
    }

    /** Returns the <code>i</code>th successor. */
    public final SCComponent next(int i){
	return next[i];
    }

    /** Returns the number of nodes inside <code>this</code> strongly \
	connected component. */
    public final int nodesLength(){
	return nodes.length;
    }

    /** Returns the <code>i</code>th node of <code>this</code> strongly \
	connected component */
    public final Object nodes(int i){
	return nodes[i];
    }
    
}
