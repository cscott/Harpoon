// MutableGraph.java, created Fri May  9 12:03:45 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Collection;

/**
 * A <code>MutableGraph</code> is a a <code>Graph</code> which can
 * be modified.  The methods allow the removal and addition of edges and
 * nodes to the graph.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MutableGraph.java,v 1.1 2003-05-09 20:35:20 cananian Exp $
 */
public interface MutableGraph<N extends Graph.Node<N,E>,
                              E extends Graph.Edge<N,E>> extends Graph<N,E> {
    /** Add a new node to the graph. */
    void addNode(N node);

    /** Add an edge from <code>from</code> to <code>to</code> and return
     *  the new edge. */
    E addEdge(N from, N to);
    /** Remove the given edge from the graph. */
    void removeEdge(E edge);

    /** Remove the given node, preserving edges: if A-&gt;N-&gt;B, then after
     *  removing N there is an edge A-&gt;B. */
    void summarize(N nodeToRemove);
    /** Remove all specified nodes, preserving path information. */
    void summarize(Collection<N> nodesToRemove);
}
