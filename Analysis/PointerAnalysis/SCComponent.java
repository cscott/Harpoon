// SCComponent.java, created Mon Jan 24 19:26:30 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Vector;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.util.Hashtable;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.ClassFile.HMethod;

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
 * @version $Id: SCComponent.java,v 1.1.2.2 2000-01-27 06:19:09 salcianu Exp $
 */
public final class SCComponent {

    /** The <code>Navigator</code> interface allows the algorithm to detect
     * (and use) the arcs from and to a certain node. This allows the
     * construction of Strongly Connected Components even for very general
     * graphs where the arcs model only a subtle semantic relation
     * (e.g. caller-callee) which is not directly stored in the structure
     * of the nodes. */
    public static interface Navigator{
	/** Returns an iterator over the predecessors of <code>node</code>. */
	public Object[] next(Object node);
	/** Returns an iterator over the successors of <code>node</code>. */
	public Object[] prev(Object node);
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
	// is there any edge to itself?
	public boolean loop;

	public SCComponentInt(){
	    nodes = new HashSet();
	    next  = new HashSet();
	    comp  = new SCComponent();
	    loop  = false;
	}
    }
    
    // the set of nodes that are reachable from the root object
    private static Set reachable_nodes;
    // the set of reached nodes (to avoid reanalyzing them "ad infinitum")
    private static Set analyzed_nodes;  
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
	analyzed_nodes = new HashSet();
	nodes_vector  = new Vector();
	DFS_first(root);

	// STEP 2. build the SCCs by doing a DFS in the reverse graph.
	node2scc = new Hashtable();
	// "in reverse" navigator
	nav = new Navigator(){
		public Object[] next(Object node){
		    return navigator.prev(node);
		}
		public Object[] prev(Object node){
		    return navigator.next(node);
		}
	    };

	// Explore the nodes in the decreasing order of their finishing time.
	// This phase will create the SCCs (big format) and initialize the
	// node2scc mapping (but it won't set the inter-SCC edges). Also,
	// the SCCs are put in scc_vector.

	// only the nodes reachable from root count!
	reachable_nodes = analyzed_nodes; 
	analyzed_nodes  = new HashSet();
	int nb_nodes = nodes_vector.size();
	for(int i = nb_nodes - 1; i >= 0; i--){
	    Object node = nodes_vector.elementAt(i);
	    if(node2scc.get(node) == null){
		current_scc_int = new SCComponentInt();
		scc_vector.add(current_scc_int);
		DFS_second(node);
	    }
	}

	// Put the edges between the SCCs.
	int nb_scc = scc_vector.size();
	for(int i = 0; i < nb_scc; i++){
	    SCComponentInt comp = (SCComponentInt) scc_vector.elementAt(i);
	    Iterator it = comp.nodes.iterator();
	    while(it.hasNext()){
		Object node = it.next();
		Object[] edges = navigator.next(node);

		for(int j = 0; j < edges.length; j++){
		    Object node2 = edges[j];
		    SCComponentInt comp2 = 
			((SCComponentInt)node2scc.get(node2));

		    if(comp2 == comp) comp.loop = true; 
		    else comp.next.add(comp2.comp);

		}
	    }
	}
	
	// Convert the big format SCCs into the compressed format SCCs.
	for(int i = 0; i < nb_scc ; i++){
	    SCComponentInt compInt = (SCComponentInt) scc_vector.elementAt(i);
	    SCComponent comp = compInt.comp;
	    comp.loop  = compInt.loop;
	    comp.nodes = compInt.nodes;
	    comp.next = 
		(SCComponent[]) compInt.next.toArray(new SCComponent[0]);
	}

	// Save the root SSC somewhere before activating the GCC.
	SCComponent root_scc = ((SCComponentInt)node2scc.get(root)).comp;
	
	nav             = null; // enable the GC
	nodes_vector    = null;
	reachable_nodes = null;
	analyzed_nodes  = null;
	node2scc        = null;
	current_scc_int = null;

	return root_scc;
    }

    public static void DFS_first(Object node){
	// do not analyze nodes already reached
	if(analyzed_nodes.contains(node)) return;

	analyzed_nodes.add(node);

	Object[] next = nav.next(node);
	int nb_next = next.length;
	for(int i = 0 ; i < nb_next ; i++)
	    DFS_first(next[i]);

	nodes_vector.add(node);
    }

    public static void DFS_second(Object node){
	if(analyzed_nodes.contains(node) ||
	   !reachable_nodes.contains(node)) return;

	analyzed_nodes.add(node);
	node2scc.put(node,current_scc_int);
	current_scc_int.nodes.add(node);

	Object[] next = nav.next(node);
	int nb_next = next.length;
	for(int i = 0 ; i < nb_next ; i++)
	    DFS_second(next[i]);
    }
    

    private static int count = 0;
    private int id;

    /** Returns the numeric ID of <code>this</code> <code>SCComponent</code>.
	Just for debug purposes ... */
    public int getId(){ return id; }

    // The nodes of this SCC (Strongly Connected Component).
    Set nodes;
    // The successors.
    private SCComponent[] next;

    // is there any edge to itself?
    private boolean loop;
    public final boolean isLoop(){ return loop; }

    //The only way to produce SCCs is through SCComponent.buildSSC !
    SCComponent(){ id = count++;}
    
    /** Returns the number of successors. */
    public final int nextLength(){
	return next.length;
    }

    /** Returns the <code>i</code>th successor. */
    public final SCComponent next(int i){
	return next[i];
    }

    /** Returns an iterator over the nodes of <code>this</code> strongly \
	connected component. */
    public final Iterator nodes(){
	return nodes.iterator();
    }

    /** Returns the nodes of <code>this</code> strongly connected component. */
    public final Set nodeSet(){
	return nodes;
    }

    /** Checks whether <code>node</code> belongs to <code>this</code> \
	strongly connected component. */
    public final boolean contains(Object node){
	return nodes.contains(node);
    }

    // the next and prev links in the double linked list of SCCs in
    // decreasing topological order
    SCComponent nextTopSort = null;
    SCComponent prevTopSort = null;

    /** Returns the next <code>SCComponent</code> according to the decreasing
     * topological order */
    public final SCComponent nextTopSort(){
	return nextTopSort;
    }

    /** Returns the previous <code>SCComponent</code> according to the
     * decreasing topological order */
    public final SCComponent prevTopSort(){
	return prevTopSort;
    }

    /** Pretty print debug function. */
    public final String toString(CallGraph cg){
	StringBuffer buffer = new StringBuffer();

	boolean extended = nodes.size() > 1;

	buffer.append("SCC" + id + " {\n");
	Iterator it = nodes.iterator();
	while(it.hasNext()){
	    Object o = it.next();
	    buffer.append(o);
	    buffer.append("\n");
	    if(extended){
		Object[] next = cg.calls((HMethod)o);
		for(int i = 0; i<next.length; i++)
		    if(nodes.contains(next[i]))
		       buffer.append("  " + next[i] + "\n");
		buffer.append("\n");
	    }
	}
	buffer.append("}\n");
	int nb_next = nextLength();
	if(nb_next > 0){
	    buffer.append("Next:");
	    for(int i = 0; i < nb_next ; i++){
		buffer.append(" SCC" + next(i).id);
	    }
	    buffer.append("\n");
	}
	return buffer.toString();
    }

}



