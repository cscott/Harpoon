// AbstractGraph.java, created Sun May  4 11:29:53 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Companions;

import harpoon.Util.Collections.AggregateSetFactory;
import harpoon.Util.Collections.SetFactory;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * An <code>AbstractGraph</code> provides a basic implementation
 * of the <code>Graph</code> interface.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AbstractGraph.java,v 1.3 2003-05-08 01:10:57 cananian Exp $
 */
public abstract class AbstractGraph<T,N extends Node<T,N,E>,E extends Graph.Edge<T,N,E>> implements Graph<T,N,E> {
    final SetFactory<E> edgeSetFactory = new AggregateSetFactory<E>();
    final Map<T,N> nodeMap = new LinkedHashMap<T,N>();
    final Set<N> nodes = new AbstractSet<N>() { // unmodifiable.
	final Collection<N> c =
	  Collections.unmodifiableCollection(nodeMap.values());
	public int size() { return c.size(); }
	public Iterator<N> iterator() { return c.iterator(); }
	public boolean contains(Object o) {
	    if (!(o instanceof Node)) return false;
	    Node n = (Node) o;
	    return (AbstractGraph.this==n.parent);
	}
    };
    public Set<N> nodes() { return nodes; }
    public N findNode(T key) { return nodeMap.get(key); }
    public void summarize(Collection<N> nodesToRemove) {
	for (Iterator<N> it=nodesToRemove.iterator(); it.hasNext(); )
	    summarize(it.next());
    }
    public void summarize(N nodeToRemove) {
	if (!nodes.contains(nodeToRemove)) return; // done already!
	// remove self-loops.
	for (Iterator<E> it=nodeToRemove.pred().iterator(); it.hasNext(); ) {
	    E edge = it.next();
	    assert edge.to()==nodeToRemove;
	    if (edge.from()==nodeToRemove)
		removeEdge(edge);
	}
	// A->n->B becomes A->B
	for (Iterator<E> it=nodeToRemove.pred().iterator(); it.hasNext(); ) {
	    N pred = it.next().from();
	    assert pred!=nodeToRemove;
	    // extend this edge to all successors.
	    for (Iterator<E> it2=nodeToRemove.succ().iterator();
		 it2.hasNext(); ) {
		N succ = it2.next().to();
		assert succ!=nodeToRemove;
		addEdge(pred, succ);
	    }
	}
	// remove original edges
	for (Iterator<E> it=nodeToRemove.pred().iterator(); it.hasNext(); )
	    removeEdge(it.next());
	for (Iterator<E> it=nodeToRemove.succ().iterator(); it.hasNext(); )
	    removeEdge(it.next());
	// finally, remove the node.
	N n = nodeMap.remove(nodeToRemove.value());
	assert n == nodeToRemove;
	nodeToRemove.parent=null;
	assert !nodes.contains(nodeToRemove);
	assert nodeToRemove.pred().size()==0;
	assert nodeToRemove.succ().size()==0;
    }
    public abstract N newNode(T key);
    public abstract E addEdge(N from, N to);

    protected void addNode(N n) {
	assert this==n.parent;
	assert !nodeMap.containsKey(n.value());
	nodeMap.put(n.value(), n);
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
     * @version $Id: AbstractGraph.java,v 1.3 2003-05-08 01:10:57 cananian Exp $
     */
    public static class Node<T,N extends Node<T,N,E>,E extends Graph.Edge<T,N,E>> implements Graph.Node<T,N,E> {
	AbstractGraph<T,N,E> parent; // set to null when unlinked.
	final T value;
	final Set<E> pred, succ, predRO, succRO;
	public Node(AbstractGraph<T,N,E> parent, T value) {
	    this.parent = parent;
	    this.value = value;
	    this.pred = parent.edgeSetFactory.makeSet();
	    this.succ = parent.edgeSetFactory.makeSet();
	    this.predRO = Collections.unmodifiableSet(this.pred);
	    this.succRO = Collections.unmodifiableSet(this.succ);
	}
	public T value() { return value; }
	public Set<E> pred() { return predRO; }
	public Set<E> succ() { return succRO; }
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
     * @version $Id: AbstractGraph.java,v 1.3 2003-05-08 01:10:57 cananian Exp $
     */
    public static class Edge<T,N extends Node<T,N,E>,E extends Edge<T,N,E>> implements Graph.Edge<T,N,E> {
	final N from;
	final N to;
	public Edge(N from, N to) { this.from=from; this.to=to; }
	public N from() { return from; }
	public N to() { return to; }
    }
}
