// Graph.java, created Sat May  3 19:05:43 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Companions;

import java.util.Collection;
import java.util.List;
import java.util.Iterator;
import java.util.Set;

/**
 * This is a generic <code>Graph</code> implementation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Graph.java,v 1.2 2003-05-08 01:10:57 cananian Exp $
 */
public interface Graph<T, N extends Node<T,N,E>, E extends Edge<T,N,E>> {
    /** Return the set of nodes comprising this Graph. */
    Set<N> nodes();
    /** Remove the given node, preserving edges: if A-&gt;N-&gt;B, then after
     *  removing N there is an edge A-&gt;B. */
    void summarize(N nodeToRemove);
    /** Remove all specified nodes, preserving path information. */
    void summarize(Collection<N> nodesToRemove);

    /** Find a path from start to end. */
    List<E> findPath(N start, N end);

    /** Add an edge from <code>from</code> to <code>to</code> and return
     *  the new edge. */
    E addEdge(N from, N to);
    /** Remove the given edge from the graph. */
    void removeEdge(E edge);

    /** Add a new node to the graph with the specified value. */
    N newNode(T key);
    /** Find the node <code>n</code> where <code>n.value().equals(key)</code>.
     */
    N findNode(T key);

    /**
     * This class represents nodes in a <code>Graph</code>.
     *
     * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
     * @version $Id: Graph.java,v 1.2 2003-05-08 01:10:57 cananian Exp $
     */
    public static interface Node<T,N extends Node<T,N,E>,E extends Edge<T,N,E>>
    {
	/** Return the value object associated with this node. */
	T value();

	/** Return a collection of edges leading to this node. */
	Collection<E> pred();
	/** Return a collection of edges leaving this node. */
	Collection<E> succ();
	
	/** Return true iff the given node is a successor of this node. */
	boolean isSucc(N n);
	/** Return true iff the given node is a predecessor of this node. */
	boolean isPred(N n);
    }

    /**
     * This class represents edges between <code>Node</code>s in a
     * <code>Graph</code>.
     *
     * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
     * @version $Id: Graph.java,v 1.2 2003-05-08 01:10:57 cananian Exp $
     */
    public static interface Edge<T,N extends Node<T,N,E>,E extends Edge<T,N,E>>
    {
	/** Returns the node this edge comes from. */
	N from();
	/** Returns the node this edges goes to. */
	N to();
    }	   
}
