// DiGraph.java, created Tue May  6 10:53:15 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Graphs;

import java.util.Set;

/**
 * DiGraph.java
 *
 *
 * Created: Sun May  4 20:56:57 2003
 *
 * @author Alexandru Salcianu <salcianu@mit.edu>
 * @version
 */
public abstract class DiGraph/*<Node extends Object>*/ {
    
    /** Returns a set that includes (but is not necessarily limited
        to) the roots of <code>this</code> directed graph.  By
        &quot;roots of a digraph&quot; we mean any set of nodes such
        that one can explore the entire graph by (transitively)
        navigating on their outgoing edges (using the
        <code>next</code> method of the navigator).  It is OK to
        return ALL the nodes from the digraph. */
    public abstract Set/*<Node>*/ getDiGraphRoots();

    /** Returns a navigator that, together with the set of roots,
        defines <code>this</code> digraph. */
    public abstract Navigator/*<Node>*/ getDiGraphNavigator();


    /** Returns the component graph for <code>this</code> graph.  The
        &quot;component graph&quot; of a graph <code>G</code> is the
        directed, acyclic graph consisting of the strongly connected
        components of <code>G</code>.  */
    public DiGraph/*<SCComponent<Node>>*/ getComponentGraph() {
	final Set/*<SCComponent<Node>>*/ sccs = SCComponent.buildSCC(this);
	return new DiGraph/*<SCComponent<Node>>*/() {
	    public Set/*<SCComponent<Node>*/ getDiGraphRoots() { 
		return sccs;
	    }
	    public Navigator/*<SCComponent<Node>>*/ getDiGraphNavigator() {
		return SCComponent.SCC_NAVIGATOR;
	    }
	};
    }

    /** Constructs a top-down topologically sorted view of the
        component graph for <code>this</code> digraph.  It starts with
        a strongly connected component with no incoming edges and ends
        with the strongly connected component(s) with no outgoing
        edges.  */
    public SCCTopSortedGraph getTopDownComponentView() {
	return
	    SCCTopSortedGraph.topSort
	    (SCComponent.buildSCC(this));
	// TODO: remove code redundancy (the call to buildSCC in both
	// this method and the previous one).  We should have a method
	// that topologicaly sorts an acyclic graph (or throws an
	// errors if it finds a cycle); then, we get the component
	// graph and pass it to that method.
    }
} // DiGraph
