// AbstractGraph.java, created Sun May  4 11:29:53 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.SetFactory;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * An <code>AbstractGraph</code> provides a basic implementation
 * of the <code>Graph</code> interface.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AbstractGraph.java,v 1.1 2003-05-09 20:35:20 cananian Exp $
 */
public abstract class AbstractGraph<N extends Node<N,E>,E extends Graph.Edge<N,E>> implements MutableGraph<N,E> {
    final SetFactory<E> edgeSetFactory = new AggregateSetFactory<E>();
    final Set<N> nodes = new LinkedHashSet<N>();
    final Set<N> nodesRO = Collections.unmodifiableSet(nodes);
    public Set<N> nodes() { return nodesRO; }

    public void summarize(Collection<N> nodesToRemove) {
	for (Iterator<N> it=nodesToRemove.iterator(); it.hasNext(); )
	    summarize(it.next());
    }
    public void summarize(N nodeToRemove) {
	if (!nodes.contains(nodeToRemove)) return; // done already!
	// remove self-loops.
	for (Iterator<E> it=nodeToRemove.predC().iterator(); it.hasNext(); ) {
	    E edge = it.next();
	    assert edge.to()==nodeToRemove;
	    if (edge.from()==nodeToRemove)
		removeEdge(edge);
	}
	// A->n->B becomes A->B
	for (Iterator<E> it=nodeToRemove.predC().iterator(); it.hasNext(); ) {
	    N pred = it.next().from();
	    assert pred!=nodeToRemove;
	    // extend this edge to all successors.
	    for (Iterator<E> it2=nodeToRemove.succC().iterator();
		 it2.hasNext(); ) {
		N succ = it2.next().to();
		assert succ!=nodeToRemove;
		addEdge(pred, succ);
	    }
	}
	// remove original edges
	for (Iterator<E> it=nodeToRemove.predC().iterator(); it.hasNext(); )
	    removeEdge(it.next());
	for (Iterator<E> it=nodeToRemove.succC().iterator(); it.hasNext(); )
	    removeEdge(it.next());
	// finally, remove the node.
	boolean changed = nodes.remove(nodeToRemove);
	assert changed;
	assert !nodes.contains(nodeToRemove);
	assert nodeToRemove.predC().size()==0;
	assert nodeToRemove.succC().size()==0;
    }
    public abstract E addEdge(N from, N to);

    public void addNode(N n) {
	assert !nodes.contains(n);
	nodes.add(n);
    }
    protected void addEdge(E e) {
	e.from().succ.add(e);
	e.to().pred.add(e);
    }
    public void removeEdge(E e) {
	e.from().succ.remove(e);
	e.to().pred.remove(e);
    }

    protected AbstractGraph() { }

    /** 
     * <code>AbstractGraph.Node</code> provides a basic implementation of
     * the <code>Graph.Node</code> interface.
     *
     * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
     * @version $Id: AbstractGraph.java,v 1.1 2003-05-09 20:35:20 cananian Exp $
     */
    public static class Node<N extends Node<N,E>,E extends Graph.Edge<N,E>> implements Graph.Node<N,E> {
	final Set<E> pred, succ, predRO, succRO;
	public Node(AbstractGraph<N,E> parent) {
	    this.pred = parent.edgeSetFactory.makeSet();
	    this.succ = parent.edgeSetFactory.makeSet();
	    this.predRO = Collections.unmodifiableSet(this.pred);
	    this.succRO = Collections.unmodifiableSet(this.succ);
	}
	public Set<E> predC() { return predRO; }
	public Set<E> succC() { return succRO; }
	public boolean isSucc(N n) {
	    for (Iterator<E> it=succ.iterator(); it.hasNext(); )
		if (it.next().to().equals(n)) return true;
	    return false;
	}
	public boolean isPred(N n) {
	    for (Iterator<E> it=pred.iterator(); it.hasNext(); )
		if (it.next().from().equals(n)) return true;
	    return false;
	}
    }
    /**
     * <code>AbstractGraph.Edge</code> provides a basic implementation of
     * the <code>Graph.Edge</code> interface.  This is a simple pair
     * of "from" and "to" nodes; you don't have to extend this
     * class when implementing <code>AbstractGraph</code> if you
     * prefer to use your own <code>Graph.Edge</code> implementation.
     *
     * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
     * @version $Id: AbstractGraph.java,v 1.1 2003-05-09 20:35:20 cananian Exp $
     */
    public static class Edge<N extends Node<N,E>,E extends Edge<N,E>> implements Graph.Edge<N,E> {
	final N from;
	final N to;
	public Edge(N from, N to) { this.from=from; this.to=to; }
	public N from() { return from; }
	public N to() { return to; }
    }
}
