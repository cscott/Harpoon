// Graph.java, created Wed Jan 13 15:51:26 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import harpoon.Util.UniqueVector;

import java.util.Set;
import java.util.Collection;

/**
 * Abstractly, a <code>Graph</code> is a pair (V, E), where V is a set
 * of nodes and E is collection of edges between those nodes.
 *
 * (E is a collection instead of a set to allow for <i>multigraphs</i>
 *  which allow multiple edges to exist between the same two nodes)
 *
 * The <code>Graph</code> interface provides a number of <i>collection
 * views</i> of the data maintained by a graph.  In addition to being
 * able to view the nodes and their neighbors, one can also view the
 * edges between the nodes themselves.  Note that since some graphs
 * may not concretely maintain edge objects in their internal
 * representation.  
 * Also note that not all graphs are directed, which means that the
 * following behavior is legal:
 * <pre>
 * Object n1, n2;
 * Graph g;
 * ...
 * // g.getChildrenOf(n2).contains(n1) is false here
 * g.add(n1, n2);
 * // g.getChildrenOf(n2).contains(n1) is true here
 * </pre>

 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: Graph.java,v 1.1.2.9 2000-07-20 18:45:40 pnkfelix Exp $
 */

public interface Graph  {
    
    /** Ensures that this graph contains <code>node</code>.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B>  If this method returns normally,
	     <code>node</code> will be present in the node-set for
	     <code>this</code>.  Returns <tt>true</tt> if this graph
	     changed as a result of the call, <tt>false</tt>
	     otherwise.
	@throws UnsupportedOperationException addNode is not supported
	        by this graph.
	@throws ClassCastException class of specified element prevents
	        it from being added to this graph.
	@throws IllegalArgumentException some aspect of
	        <code>node</code> prevents it from being added to the
		node-set for this graph.
    */
    boolean addNode( Object node );

    /** Ensures that this graph contains an edge from
	<code>from</code> to <code>to</code>. 
	<BR> <B>modifies:</B> <code>this</code>.
	<BR> <B>effects:</B> If this method returns normally, an edge
	     from <code>from</code> to <code>to</code> will be in
	     <code>this</code>.  Returns <tt>true</tt> if
	     <code>this</code> changed as a result of the call.
	@throws IllegalArgumentException <code>from</code> or
	     <code>to</code> is not present in the node set for
	     <code>this</code>.
	@throws UnsupportedOperationException addEdge is not supported
	     by this graph.
     */
    boolean addEdge( Object from, Object to );
    
    /** Returns the degree of <code>node</code>.
	<BR> <B>effects:</B> Returns the number of other
	     <code>Object</code>s that <code>node</code> is joined to.
        @throws IllegalArgumentException If <code>node</code> is not
	     present in the node set for <code>this</code>.
    */
    int getDegree( Object node );

    /** Returns a set view of the nodes in <code>this</code>.
	<BR> <B>effects:</B> Returns an <code>Set</code> of
	                     the <code>Object</code>s that have been
			     successfully added to <code>this</code>.  
    */
    Set nodeSet();

    /** Returns a collection view for the children of a specific node. 
	<BR> <B>effects:</B> Returns an <code>Collection</code> view 
	                     of nodes that have an edge from
			     <code>node</code> to them. 
	<BR> <B>mandates:</B> <code>node</code> is not removed from
	                      <code>this</code> while the returned
			      <code>Collection</code> is in use.
     */
    Collection childrenOf( Object node );


    /** Returns a collection view for the parents of a specific node. 
	<BR> <B>effects:</B> Returns a <code>Collection</code> view
	                     of nodes that have an edge from
			     them to <code>node</code>. 
        <BR> <B>mandates:</B> <code>node</code> is not removed from 
	                      <code>this</code> while the returned
			      <code>Collection</code> is in use.
     */
    Collection parentsOf( Object node );

    /** Returns a collection view for the neighbors of a specific node. 
	<BR> <B>effects:</B> Returns an <code>Collection</code> of
	                     <code>Object</code>s that have an edge
			     between <code>node</code> and them. 
        <BR> <B>mandates:</B> <code>this</code> is not modified while
	                      the <code>Enumeration</code> returned is
			      still in use.
     */
    Collection neighborsOf( Object node );

    /** Returns a collection view of the edges contained in this graph.
	<BR> <B>effects:</B> Returns a <code>Collection</code> of
	     two-element <code>List</code>s (known as <i>pairs</i>)
	     where each pair corresponds to an edge (n1, n2) in this.
	     If the graph is modified while an iteration over the
	     collection is in progress, the results of the iteration
	     are undefined. 
    */
    Collection edges();

    /** Returns a collection view of the edges joining
	<code>node</code> to nodes in the graph.
	<BR> <B>effects:</B> Returns a <code>Collection</code> of
	     two-element <code>List</code>s (known as <i>pairs</i>)
	     where each pair corresponds to an edge (node, n2) or (n1,
	     node) in this.  If the graph is modified while an
	     iteration over the collection is in progress, the results
	     of the iteration are undefined. 
    */
    Collection edgesFor( Object node );

}



