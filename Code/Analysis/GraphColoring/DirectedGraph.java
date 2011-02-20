// DirectedGraph.java, created Thu Jul 20 15:34:24 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.Collection;

/**
 * <code>DirectedGraph</code> is an extension of the
 * <code>Graph</code> interface that tracks the direction of the edges
 * that have been added to the graph.   This results in a stregthening
 * of the specification for several of <code>Graph</code>'s methods
 * and the addition of two new methods, <code>childrenOf(node)</code>
 * and <code>parentsOf(node)</code>.
 *
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: DirectedGraph.java,v 1.2 2002-02-25 20:57:17 cananian Exp $
 */
public interface DirectedGraph extends Graph {
    
    /** Ensures that this graph contains an edge from
	<code>f</code> to <code>t</code>. 
	<BR> <B>modifies:</B> <code>this</code>.
	<BR> <B>effects:</B> If this method returns normally, an edge
	     from <code>f</code> to <code>t</code> will be in 
	     <code>this</code>.  Returns <tt>true</tt> if
	     <code>this</code> changed as a result of the call.
	@throws IllegalArgumentException <code>f</code> or
	     <code>t</code> is not present in the node set for
	     <code>this</code>.
	@throws UnsupportedOperationException addEdge is not supported
	     by this graph.
    */
    boolean addEdge( Object f, Object t );
    
    /** Returns a collection view for the children of a specific node. 
	<BR> <B>effects:</B> Returns an <code>Collection</code> view 
             of nodes that have an edge from <code>node</code> to
	     them.  
	<BR> <B>mandates:</B> <code>node</code> is not removed from
	                      <code>this</code> while the returned
			      <code>Collection</code> is in use.
        @throws IllegalArgumentException If <code>node</code> is not
	     present in the node set for <code>this</code>.
    */
    Collection childrenOf( Object node );


    /** Returns a collection view for the parents of a specific node. 
	<BR> <B>effects:</B> Returns a <code>Collection</code> view
             of nodes that have an edge from them to
	     <code>node</code>. 
        <BR> <B>mandates:</B> <code>node</code> is not removed from 
             <code>this</code> while the returned
	     <code>Collection</code> is in use. 
        @throws IllegalArgumentException If <code>node</code> is not
	     present in the node set for <code>this</code>.
    */
    Collection parentsOf( Object node );

    /** Returns a collection view of the edges contained in this graph.
	<BR> <B>effects:</B> Returns a <code>Collection</code> of
	     two-element <code>List</code>s (known as <i>pairs</i>)
	     where each pair corresponds to an edge { n1, n2 } in
	     this.  If the graph is modified while an iteration over
	     the collection is in progress, the results of the
	     iteration are undefined.  Order may or may not be
	     significant in the pairs returned. 
    */
    Collection edges();

    /** Returns a collection view of the edges joining
	<code>node</code> to nodes in the graph.
	<BR> <B>effects:</B> Returns a <code>Collection</code> of
	     two-element <code>List</code>s (known as
	     <i>pairs</i>) where each pair corresponds to an edge
	     [ node, n ] or [ n, node ] in this.  If the graph is
	     modified while an iteration over the collection is in
	     progress, the results of the iteration are undefined.
        @throws IllegalArgumentException If <code>node</code> is not
	     present in the node set for <code>this</code>.
    */
    Collection edgesFor( Object node );

}
