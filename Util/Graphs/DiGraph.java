// DiGraph.java, created Tue May  6 10:53:15 2003 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Graphs;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;

/**
 * <code>DiGraph</code> models a directed graph.  A directed graph is
 * defined by a set of <i>root</i> vertices and a <i>navigator</i>.  The
 * navigator may be interpreted as an iterator over the graph: given a
 * vertex <code>n</code>, it gives <code>n</code>'s direct successors
 * (i.e., vertices pointed to by arcs that start in <code>n</code>), and
 * (optionally), the <code>n></code>'s direct predecessors.
 * The graph contains all the (transitive and reflexive) successors
 * of the root vertices.
 *
 * <p>There are two kinds of navigators:
 * <code>ForwardNavigator</code>s (give only successors) and
 * bi-directional <code>Navigator</code> (both successors and
 * predecessors).  For a given graph you should define at least one of
 * them, i.e., you should override at least one of the methods
 * <code>getDiGraphNavigator</code> and
 * <code>getDiGraphForwardNavigator</code>.  The standard
 * implementation of these methods in this class is able to construct
 * the other navigator: a bi-directional navigator is trivially a
 * forward one too.  Also, if we know the root vertices and the successor
 * relation, we can produce the predecessor relation (although it's
 * quite costly).  However, for efficiency, it's good to define both
 * (of course, in a consistent manner).
 *
 * @see ForwardNavigator
 * @see Navigator
 *
 * Created: Sun May  4 20:56:57 2003
 *
 * @author Alexandru Salcianu <salcianu@mit.edu>
 * @version $Id: DiGraph.java,v 1.3 2003-05-09 15:54:56 salcianu Exp $ 
 */
public abstract class DiGraph/*<Vertex extends Object>*/ {
    
    /** Returns a set that includes (but is not necessarily limited
        to) the roots of <code>this</code> directed graph.  By
        &quot;roots of a digraph&quot; we mean any set of vertices such
        that one can explore the entire graph by (transitively)
        navigating on their outgoing arcs (using the
        <code>next</code> method of the navigator).  It is OK to
        return ALL the vertices from the digraph. */
    public abstract Set/*<Vertex>*/ getDiGraphRoots();

    /** Returns a navigator that, together with the set of roots,
        defines <code>this</code> digraph.  By default, obtains the
        forward navigator (by calling
        <code>getDiGraphForwardNavigator</code>), explores the entire
        graph and constructs the predecessor relation.

	<p><strong>Note:</strong> You MUST overwrite at least one of
	<code>getDiGraphNavigator</code> and
	<code>getDiGraphForwardNavigator</code>. */
    public Navigator/*<Vertex>*/ getDiGraphNavigator() {
	final Relation/*<Vertex,Vertex>*/ prevRel = 
	    new RelationImpl/*<Vertex,Vertex>*/();
	final ForwardNavigator/*<Vertex>*/ fnav = getDiGraphForwardNavigator();
	
	for(Iterator/*<Vertex>*/ it = allVertices().iterator();it.hasNext();) {

	    Object vertex = it.next();
	    Object next[] = fnav.next(vertex);
	    for(int i = 0; i < next.length; i++)
		prevRel.add(next, vertex);
	}

	return new Navigator() {
	    public Object[] next(Object vertex) { return fnav.next(vertex); }
	    public Object[] prev(Object vertex) {
		Set prev = prevRel.getValues(vertex);
		return prev.toArray(new Object[prev.size()]);
	    }
	};
    }
    
    /** Returns a navigator that, together with the set of roots,
        defines <code>this</code> digraph. 
	
	<p><strong>Note:</strong> You MUST overwrite at least one of
	<code>getDiGraphNavigator</code> and
	<code>getDiGraphForwardNavigator</code>. */
    public ForwardNavigator/*<Vertex>*/ getDiGraphForwardNavigator() {
	return getDiGraphNavigator();
    }


    /** @return Set of all transitive and reflexive successors of
        <code>vertex</code>. */
    public Set/*<Vertex>*/ transitiveSucc(Object vertex) {
	return transitiveSucc(Collections.singleton(vertex).iterator());
    }

    /** @return Set of all transitive and reflexive successors of the
        vertices returned by <code>itVertices</code>. */
    public Set/*<Vertex>*/ transitiveSucc(Iterator/*<Vertex>*/ itVertices) {
	return reachableVertices(itVertices, getDiGraphForwardNavigator());
    }

    /** @return Set of all transitive and reflexive
        predecessors of <code>vertex</code> */
    public Set/*<Vertex>*/ transitivePred(Object vertex) {
	return transitivePred(Collections.singleton(vertex).iterator());
    }

    /** @return Set of all transitive and reflexive predecessors of
        the vertices returned by <code>itVertices</code>. */
    public Set/*<Vertex>*/ transitivePred(Iterator/*<Vertex>*/ itVertices) {
	return reachableVertices(itVertices, 
			      new ReverseNavigator(getDiGraphNavigator()));
    }


    /** @return Set of all transitive and reflexive predecessors of
        the vertices returned by <code>itVertices</code>, where the
        predecessors of a vertex are given by method <code>next</code>
        of <code>navigator</code>. */
    public static Set/*<Vertex>*/ reachableVertices
	(Iterator/*<Vertex>*/ itVertices,
	 ForwardNavigator/*<Vertex>*/ navigator) {

	Set/*<Vertex>*/ reachables = new HashSet/*<Vertex>*/();

	LinkedList/*<Vertex>*/ w = new LinkedList/*<Vertex>*/();
	while(itVertices.hasNext()) {
	    Object vertex = itVertices.next();
	    if(reachables.add(vertex))
		w.addLast(vertex);
	}

	while(!w.isEmpty()) {
	    Object vertex = w.removeFirst();
	    Object succs[] = navigator.next(vertex);
	    for(int i = 0; i < succs.length; i++) {
		Object succ = succs[i];
		if(reachables.add(succ))
		    w.addLast(succ);
	    }
	}

	return reachables;
    }


    /** Returns the component graph for <code>this</code> graph.  The
        &quot;component graph&quot; of a graph <code>G</code> is the
        directed, acyclic graph consisting of the strongly connected
        components of <code>G</code>.  */
    public DiGraph/*<SCComponent<Vertex>>*/ getComponentGraph() {
	final Set/*<SCComponent<Vertex>>*/ sccs = SCComponent.buildSCC(this);
	return new DiGraph/*<SCComponent<Vertex>>*/() {
	    public Set/*<SCComponent<Vertex>*/ getDiGraphRoots() { 
		return sccs;
	    }
	    public Navigator/*<SCComponent<Vertex>>*/ getDiGraphNavigator() {
		return SCComponent.SCC_NAVIGATOR;
	    }
	};
    }

    /** Constructs a top-down topologically sorted view of the
        component graph for <code>this</code> digraph.  It starts with
        a strongly connected component with no incoming arcs and ends
        with the strongly connected component(s) with no outgoing
        arcs.  */
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

    
    /** @return set of vertices from <code>this</code> directed graph. */
    public Set/*<Vertex>*/ allVertices() {
	return transitiveSucc(getDiGraphRoots().iterator());
    }

    
    /** @return reverse of <code>this</code> directed graph: a
        directed graph with the same set of vertices, that contains an
        arc from <code>v1</code> to <code>v2</code> iff
        <code>this</code> graph contains an arc from <code>v2</code>
        to <code>v1</code>. */
    public DiGraph/*<Vertex>*/ reverseGraph() {
	return new DiGraph/*<Vertex>*/() {
	    public Set/*<Vertex>*/ getDiGraphRoots() {
		return DiGraph.this.allVertices();
	    }
	    public Navigator/*<Vertex>*/ getDiGraphNavigator() {
		return 
		    new ReverseNavigator(DiGraph.this.getDiGraphNavigator());
	    }
	    // the default implementation of
	    // getDiGraphForwardNavigator() is precisely what we want.
	};
    }
}
