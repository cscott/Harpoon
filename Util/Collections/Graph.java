// Graph.java, created Sat May  3 19:05:43 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * This is a generic <code>Graph</code> implementation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Graph.java,v 1.2 2003-05-09 21:04:42 cananian Exp $
 */
public interface Graph<N extends Node<N,E>, E extends Edge<N,E>> {
    /** Return the set of nodes comprising this Graph. */
    Set<N> nodes();

    /** Find a path from start to end. */
    //XXX this method should be in a "GraphUtilities" class.
    //List<E> findPath(N start, N end);

    /**
     * This class represents nodes in a <code>Graph</code>.
     *
     * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
     * @version $Id: Graph.java,v 1.2 2003-05-09 21:04:42 cananian Exp $
     */
    public static interface Node<N extends Node<N,E>,E extends Edge<N,E>>
    {
	/** Return a collection of edges leading to this node. */
	Collection<E> predC();
	/** Return a collection of edges leaving this node. */
	Collection<E> succC();
	
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
     * @version $Id: Graph.java,v 1.2 2003-05-09 21:04:42 cananian Exp $
     */
    public static interface Edge<N extends Node<N,E>,E extends Edge<N,E>>
    {
	/** Returns the node this edge comes from. */
	N from();
	/** Returns the node this edges goes to. */
	N to();
    }	   
}
