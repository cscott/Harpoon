// SCComponent.java, created Mon Jan 24 19:26:30 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Graphs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Collection;
import java.util.Comparator;

import java.io.Serializable;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;

import harpoon.Util.UComp;

import harpoon.Util.Util;

/**
 * <code>SCComponent</code> models a <i>Strongly connected component</i>
 * of a graph.
 * The only way to split a graph into <code>SCComponent</code>s is through
 * <code>buildSCC</code>.
 * That method is quite flexible: all it needs is a root node (or a set of
 * root nodes) and a <i>Navigator</i>: an object implementing the 
 * <code>Navigator</code> interface that provides the 
 * edges coming into/going out of a given <code>Object</code>. So, it can
 * build strongly connected components even for graphs that are not built
 * up from <code>CFGraphable</code> nodes, a good example being the set of
 * methods where the edges represent the caller-callee relation (in this
 * case, the strongly connected components group together sets of mutually
 * recursive methods).
 * 
 * @author  Alexandru SALCIANU <salcianu@lcs.mit.edu>
 * @version $Id: SCComponent.java,v 1.14 2004-03-05 22:18:36 salcianu Exp $
 */
public final class SCComponent/*<Vertex>*/
    implements Comparable<SCComponent/*<Vertex>*/>, Serializable {

    /** Default navigator through a component graph (a dag of strongly
        connected components).  */
    public static final Navigator<SCComponent> SCC_NAVIGATOR =
	new Navigator<SCComponent>() {
	    public SCComponent[] next(SCComponent node) {
		return node.next;
	    }
	    public SCComponent[] prev(SCComponent node) {
		return node.prev;
	    }
	};


    // THE FIRST PART CONTAINS JUST SOME STATIC METHODS & FIELDS

    /** Convenient version of
        <code>buildSCC(Object[],Navigator)</code>

	@param graph directed graph

	@return set of top-level strongly connected components (ie,
	SCCs that are not pointed to by anybody). */
    public static final /*<Vertex>*/ Set<SCComponent/*<Vertex>*/>
	buildSCC(final DiGraph/*<Vertex>*/ graph) {
	return buildSCC(graph.getRoots(), graph.getNavigator());
    }
    

    /** Convenient version of
        <code>buildSCC(Object[],Navigator)</code>

	@param roots collection of nodes such that if we start from
	these nodes and (transitively) navigate on the outgoing edges,
	we explore the entire graph.

	@param navigator navigator for exploring the graph

	@return set of strongly connected components of the graph
	defined by <code>roots</code> and <code>navigator</code> */
    public static final /*<Vertex>*/ Set<SCComponent/*<Vertex>*/>
	buildSCC(final Collection/*<Vertex>*/ roots,
		 final Navigator/*<Vertex>*/ navigator) {
	return 
	    (new BuildSCCClosure/*<Vertex>*/()).doIt(roots, navigator);
    }

    // OO programming is great, especially when one encodes functional
    // programming into it :)
    // BuildSCCClosure.doIt computes the strongly connected
    // components, using a DFS-based algorithm from CLR (page 488).
    private static class BuildSCCClosure/*<Vertex>*/ {
	// set of nodes reachable from root
	private Set/*<Vertex>*/ reachable_nodes;
	// set of reached nodes (to avoid reanalyzing them "ad infinitum")
	private Set/*<Vertex>*/ visited_nodes;
	// Mapping vertex -> SCComponent
	private HashMap/*<Vertex,SCComponent<Vertex>>*/ v2scc;
	// The vector of the reached nodes, in the order DFS finished them
	private Vector/*<Vertex>*/ nodes_vector;
	// vector to put the generated SCCs in.
	private Vector/*<SCComponent>*/ scc_vector;

	// currently generated strongly connected component
	private SCComponent current_scc = null;
	// the navigator used in the DFS algorithm
	private Navigator/*<Vertex>*/ nav = null;

	// does the real work behind SCComponent.buildSCC
	public final /*<Vertex>*/ Set<SCComponent/*<Vertex>*/> doIt
	    (final Collection/*<Vertex>*/ roots,
	     final Navigator/*<Vertex>*/ navigator) {
	    scc_vector     = new Vector();
	    visited_nodes  = new HashSet();
	    nodes_vector   = new Vector();
	    v2scc          = new HashMap();

	    // STEP 1: DFS exploration; add all reached nodes in
	    // "nodes_vector", in the order of their "finished" time.
	    direct_dfs(roots, navigator);

	    // STEP 2. build the SCCs by doing a DFS in the reverse graph.
	    reverse_dfs(navigator);

	    // produce the final formal SCCs
	    build_final_sccs(navigator);
	    
	    return get_root_sccs(roots);
	}


	private final void direct_dfs
	    (Collection/*<Vertex>*/ roots, Navigator/*<Vertex>*/ navigator) {
	    nav = navigator;
	    for(Object vertex : roots) 
		DFS_first(vertex);
	}

	// DFS for the first step: the "forward" navigation
	private final void DFS_first(Object node) {
	    // skip already visited nodes
	    if(visited_nodes.contains(node)) return;

	    visited_nodes.add(node);
	    
	    Object[] next = nav.next(node);
	    for(int i = 0 ; i < next.length ; i++)
		DFS_first(next[i]);
	    
	    nodes_vector.add(node);
	}


	private final void reverse_dfs(Navigator/*<Vertex>*/ navigator) {
	    // "in reverse" navigator
	    nav = new ReverseNavigator/*<Vertex>*/(navigator);

	    // Explore the vertices in the decreasing order of their
	    // "finished" time.  For each unvisited vertex, grab all
	    // vertices reachable on the reverse navigator.  No
	    // inter-SCC edges yet.  Put SCCs in scc_vector.
	    reachable_nodes = visited_nodes;
	    visited_nodes = new HashSet();
	    for(int i = nodes_vector.size() - 1; i >= 0; i--){
		Object node = nodes_vector.elementAt(i);
		// explore nodes that are still unanalyzed
		if(!visited_nodes.contains(node)) {
		    current_scc = new SCComponent();
		    scc_vector.add(current_scc);
		    DFS_second(node);
		}
	    }
	}

	// DFS for the second step: the "backward" navigation.
	private final void DFS_second(Object node) {
	    // only nodes reachable from root nodes count: we make
	    // sure that navigator.prev does not take us to strange
	    // places!
	    if(visited_nodes.contains(node) ||
	       !reachable_nodes.contains(node)) return;
	    
	    visited_nodes.add(node);

	    v2scc.put(node, current_scc);
	    current_scc.nodes.add(node);
	    
	    Object[] next = nav.next(node);
	    for(int i = 0 ; i < next.length ; i++)
		DFS_second(next[i]);
	}
    

	// Build the final SCC.  This requires converting some sets to
	// arrays (and sorting them in the deterministic case).
	private final void build_final_sccs(Navigator/*<Vertex>*/ navigator) {
	    nextRel        = new RelationImpl();
	    prevRel        = new RelationImpl();
	    scc2exits      = new RelationImpl();
	    scc2entries    = new RelationImpl();

	    // Put inter-SCCs edges.
	    collect_edges(navigator);
	    
	    for(Iterator it = scc_vector.iterator(); it.hasNext(); ) {
		SCComponent scc = (SCComponent) it.next();

		scc.nodes_array = 
		    scc.nodes.toArray(new Object[scc.nodes.size()]);
		// add the edges
		scc.next = toArraySCC(nextRel.getValues(scc));
		scc.prev = toArraySCC(prevRel.getValues(scc));
		// add the entries / exits
		scc.entries = toArrayVertex(scc2entries.getValues(scc));
		scc.exits   = toArrayVertex(scc2exits.getValues(scc));
	    }
	}

	// relation vertex -> successor vertices
	private Relation/*<SCComponent,SCComponent>*/ nextRel;
	// scc -> set of exit vertices
	private Relation/*<SCComponent,Vertex>*/ scc2exits;
	// relation vertex -> predecessor vertices
	private Relation/*<SCComponent,SCComponent>*/ prevRel;
	// scc -> set of entry vertices
	private Relation/*<SCComponent,Vertex>*/ scc2entries;


	// Collect inter-SCCs edges in nextRel/prevRel: there is an
	// edge from scc1 to scc2 iff there is at least one pair of
	// nodes n1 in scc1 and n2 in scc2 such that there exists an
	// edge from n1 to n2.
	private final void collect_edges
	    (final Navigator/*<Vertex>*/ navigator) {
	    for(Iterator itscc = scc_vector.iterator(); itscc.hasNext(); ) {
		SCComponent scc1 = (SCComponent) itscc.next();

		for(Iterator itv = scc1.nodes.iterator(); itv.hasNext(); ) {
		    Object v1 = itv.next();

		    Object[] edges = navigator.next(v1);
		    for(int j = 0; j < edges.length; j++) {
			Object v2 = edges[j];

			SCComponent scc2 = (SCComponent) v2scc.get(v2);
			if(scc1 == scc2) {
			    scc1.loop = true;
			}
			else {
			    nextRel.add(scc1, scc2);
			    prevRel.add(scc2, scc1);
			    scc2exits.add(scc1, v1);
			    scc2entries.add(scc2, v2);
			}
		    }
		}
	    }
	}

	private final SCComponent[] toArraySCC
	    (final Collection/*<SCComponent>*/ c) {
	    return (SCComponent[]) c.toArray(new SCComponent[c.size()]);
	}

	private final Object[]/*Vertex[]*/ toArrayVertex
	    (final Collection/*<Vertex>*/ c) {
	    return /*(Vertex[])*/ c.toArray(new Object[c.size()]);
	}

	// Compute set of root SCCs.
	private final Set<SCComponent/*<Vertex>*/> 
	    get_root_sccs(Collection/*<Vertex>*/ roots) {
	    Set<SCComponent/*<Vertex>*/> root_sccs = new HashSet();
	    for(Object root_vertex : roots) {
		SCComponent scc = (SCComponent) v2scc.get(root_vertex);
		if(scc.prevLength() == 0)
		    root_sccs.add(scc);
	    }
	    return root_sccs;
	}
    }


    // HERE STARTS THE REAL (i.e. NON STATIC) CLASS

    // The only way to produce SCCs is through SCComponent.buildSSC !
    private SCComponent() { id = count++; }
    private static int count = 0;

    /** Returns the numeric ID of <code>this</code> <code>SCComponent</code>.
	Just for debug purposes ... */
    public int getId(){ return id; }
    private int id;

    // The nodes of this SCC (Strongly Connected Component).
    Set nodes = new TreeSet(DEFAULT_COMPARATOR);
    Object[] nodes_array;
    // The successors.
    private SCComponent[] next;
    // The predecessors.
    private SCComponent[] prev;

    // entries/exists into this SCC
    private Object[] entries;
    private Object[] exits;

    /** Checks whether <code>this</code> strongly connected component
	corresponds to a loop, <i>ie</i> it has at least one arc to
	itself. */
    public final boolean isLoop() { return loop; }
    // is there any arc to itself?
    private boolean loop;


    public int compareTo(SCComponent/*<Vertex>*/ scc2) {
	int id2 = scc2.id;
	if(id  < id2) return -1;
	if(id == id2) return 0;
	return 1;
    }
    
    /** Returns the number of successors. */
    public final int nextLength() { return next.length; }

    /** Returns the <code>i</code>th successor. */
    public final SCComponent next(int i) { return next[i]; }

    /** Returns the number of predecessors. */
    public final int prevLength() { return prev.length; }

    /** Returns the <code>i</code>th predecessor. */
    public final SCComponent prev(int i) { return prev[i]; }

    /** Returns the nodes of <code>this</code> strongly connected component
	(set version). */
    public final Set nodeSet() { return nodes; }

    /** Returns the nodes of <code>this</code> strongly connected component;
	array version - good for iterating over the elements of the SCC. */
    public final Object[] nodes() { return nodes_array; }

    /** Returns the number of nodes in <code>this</code> strongly connected
	component. */
    public final int size() { return nodes_array.length; }

    /** Checks whether <code>node</code> belongs to <code>this</code> \
	strongly connected component. */
    public final boolean contains(Object node) { return nodes.contains(node); }

    /** Returns the entry nodes of <code>this</code> strongly
        connected component.  These are the nodes taht are reachable
        from outside the component. */
    public final Object[] entries() { return entries; }

    /** Returns the exit nodes of <code>this</code> strongly connected
        component.  These are the nodes that have arcs toward nodes
        outside the component. */
    public final Object[] exits()   { return exits; }


    /** Pretty print debug function. */
    public final String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("SCC" + id + " (size " + size() + ") {\n");
	for(int i = 0; i < nodes_array.length; i++) {
	    buffer.append(nodes_array[i]);
	    buffer.append("\n");
	}
	buffer.append("}\n");
	// string representation of the "prev" links.
	int nb_prev = prevLength();
	if(nb_prev > 0) {
	    buffer.append("Prev:");
	    for(int i = 0; i < nb_prev ; i++)
		buffer.append(" SCC" + prev(i).getId());
	    buffer.append("\n");
	}
	// string representation of the "next" links.
	int nb_next = nextLength();
	if(nb_next > 0) {
	    buffer.append("Next:");
	    for(int i = 0; i < nb_next ; i++)
		buffer.append(" SCC" + next(i).getId());
	    buffer.append("\n");
	}
	return buffer.toString();
    }

    // put some unit tests here
    public void test() {
	// TODO
    }

    // make sure we have a default comparator for the TreeSet
    private static Comparator DEFAULT_COMPARATOR = new DefaultComparator();
    private static class DefaultComparator implements Comparator {
	public int compare(Object o1, Object o2) {
	    if(o1.equals(o2)) return 0;
	    return (o1.hashCode() < o2.hashCode()) ? -1 : 1;
	}
    }
}
