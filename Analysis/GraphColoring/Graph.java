// Graph.java, created Wed Jan 13 15:51:26 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import harpoon.Util.UniqueVector;

import java.util.Set;
import java.util.Collection;

/**
 * <code>Graph</code> is an interface describing the operations
 * available for manipulating a graph object.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: Graph.java,v 1.1.2.8 2000-07-20 17:55:43 pnkfelix Exp $
 */

public interface Graph  {
    
    /** Adds <code>node</code> to <code>this</code>.
	<BR> <B>requires:</B> <OL>
	     <LI> <code>this</code> is modifiable, 
	     <LI> <code>node</code> is of the correct type 
	          for the graph type implemented by
		  <code>this</code>. 
	</OL>
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B>  Adds <code>node</code> to
	                      <code>this</code>, if <code>node</code>
			      is not already a member of
			      <code>this</code>.  
			      Else does nothing.  
    */
    void addNode( Object node );

    /** Adds an edge from <code>from</code> to <code>to</code>.
	<BR> <B>requires:</B> <code>from</code> and <code>to</code>
	                      are present in <code>this</code> and are
			      valid targets for a new edge.
	<BR> <B>modifies:</B> <code>this</code>.
     */
    void addEdge( Object from, Object to );
    
    /** Returns the degree of <code>node</code>.
	<BR> <B>requires:</B>: <code>node</code> is present in
	                       <code>this</code>
	<BR> <B>effects:</B> Returns the number of other
	                     <code>ColorableNode</code>s that
			     <code>node</code> is connected to. 
    */
    int getDegree( Object node );

	
    /** Constructs a <code>Set</code> view of the nodes in <code>this</code>.
	<BR> <B>effects:</B> Returns an <code>Set</code> of
	                     the <code>Object</code>s that have been
			     successfully added to <code>this</code>.  
    */
    Set getNodes();

    /** Constructs a <code>Collection</code> view for the children of
	a specific node.
	<BR> <B>effects:</B> Returns an <code>Collection</code> view 
	                     of nodes that have an edge from
			     <code>node</code> to them. 
	<BR> <B>mandates:</B> <code>node</code> is not removed from
	                      <code>this</code> while the returned
			      <code>Collection</code> is in use.
     */
    Collection getChildrenOf( Object node );


    /** Constructs a <code>Collection</code> view for the parents of a
	specific node. 
	<BR> <B>effects:</B> Returns a <code>Collection</code> view
	                     of nodes that have an edge from
			     them to <code>node</code>. 
        <BR> <B>mandates:</B> <code>node</code> is not removed from 
	                      <code>this</code> while the returned
			      <code>Collection</code> is in use.
     */
    Collection getParentsOf( Object node );

    /** Constructs a <code>Collection</code> view for the neighbors of
	a specific node. 
	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     <code>Node</code>s that have an edge
			     between <code>node</code> and them. 
        <BR> <B>requires:</B> <code>this</code> is not modified while
	                      the <code>Enumeration</code> returned is
			      still in use.
     */
    Collection getNeighborsOf( Object node );

}



