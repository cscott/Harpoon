// SparseGraph.java, created Wed Jan 13 16:32:04 1999 by pnkfelix
// Copyright (C) 1998 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import harpoon.Util.UniqueStack;
import harpoon.Util.EnumerationIterator;
import harpoon.Util.Default;
import harpoon.Util.UnmodifiableIterator;
import java.util.Enumeration;
import java.util.Collection;
import java.util.Collections;
import java.util.AbstractCollection;
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;

/**
 * <code>SparseGraph</code> is an implementation of a
 * <code>ColorableGraph</code> object.  Nodes are represented by a
 * list <code>SparseNode</code>s, and edges are represented by the
 * references <code>SparseNode</code>s store internally.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: SparseGraph.java,v 1.1.2.11 2000-07-21 23:58:12 pnkfelix Exp $ 
 */

public class SparseGraph extends ColorableGraphImpl { // implements ColorableGraph {

    // List of hidden nodes, so that we may properly check when nodes
    // are unhid.
    UniqueStack hiddenNodes;
    
    /** Creates a <code>SparseGraph</code>. */
    public SparseGraph() {
        super();
	hiddenNodes = new UniqueStack();
    }
            
    /** Ensures that only <code>SparseNode</code>s are added to <code>this</code>.
	<BR> <B>requires:</B> <code>node</code> is an instance of a
	                      <code>SparseNode</code>
	<BR> <B>effects:</B> Does nothing.
    */
    protected void checkNode( Node node ) {
	super.checkNode( node );
	if (! (node instanceof SparseNode) ) {
	    throw new WrongNodeTypeException
		("Node " + node + " is not correct type for SparseGraph.");
	}
    }
    
    private void isNodePresent( Node node ) 
	throws NodeNotPresentInGraphException {
	if ( ! getNodeVector().contains( node ) ) {
	    throw new NodeNotPresentInGraphException
		("Node " + node + " not present in graph.");
	}
    }

    /** Generates an edge from <code>from</code> to <code>to</code>.
	<BR> <B>requires:</B> <code>from</code> and <code>to</code>
	                      are present in <code>this</code> and are
			      valid targets for a new edge
			      (<code>SparseGraph</code> does not allow
			      multiple edges or circular edges).
	<BR> <B>modifies:</B> <code>from</code>, <code>to</code>
	<BR> <B>effects:</B> Adds an edge from <code>from</code> to
	                     <code>to</code>. 
    */
    public void makeEdge( Node from, Node to ) {
	if (from == to) {
	    throw new IllegalEdgeException
		("SparseGraph does not allow circular edges");
	}
	
	isNodePresent( from );
	isNodePresent( to );
	// if nodes are in this object, they must be SparseNodes
	// (guaranteed by representation given above)  
	SparseNode sTo = (SparseNode) to;
	SparseNode sFrom = (SparseNode) from;

	if (!this.isModifiable()) {
	    throw new IllegalEdgeException
		(this + " is not allowed to be modified.");
	}
	
	try {
	    sTo.addEdgeFrom( sFrom );
	} catch (ObjectNotModifiableException e) {
	    throw new IllegalEdgeException
		(sFrom + " is not allowed to be modified.");
	}
	try {
	    sFrom.addEdgeTo( sTo );
	} catch (ObjectNotModifiableException e) {
	    throw new IllegalEdgeException 
		(sTo + " is not allowed to be modified.");
	}
    }
    
    /** Returns the degree of <code>node</code>.
	<BR> <B>requires:</B> <code>node</code> is present in
	                      <code>this</code>.
	<BR> <B>effects:</B> Returns the number of other
	                     <code>ColorableNode</code>s that
			     <code>node</code> is connected to.  
    */
    public int getDegree( Node node ) {
	isNodePresent( node );
	// if its in this object, it must be a SparseNode
	// (guaranteed by representation given above)  
	SparseNode snode = (SparseNode) node;
	return snode.getDegree();
    }

    /** Constructs an enumeration for the children of a specific node.
	<BR> <B>requires:</B> <code>node</code> is present in
	                      <code>this</code>.
	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     the <code>Node</code>s that have an edge
			     from <code>node</code> to them that are
			     not hidden. 
        <BR> <B>requires:</B> <code>this</code> is not modified while
	                      the <code>Enumeration</code> returned is
			      still in use.
     */
    public Enumeration getChildrenOf( Node node ) {
	isNodePresent( node );
	// if its in this object, it must be a SparseNode
	// (guaranteed by representation given above)  
	SparseNode snode = (SparseNode) node;
	
	ColorableGraphImpl.HiddenFilteringEnum filter = 
	    new ColorableGraphImpl.HiddenFilteringEnum(snode.getToNodes());
	return filter;
    }

    /** Constructs an enumeration for the parents of a specific node.
	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     the nodes that have an edge from them to
			     <code>node</code>. 
        <BR> <B>requires:</B> <code>this</code> is not modified while
	                      the <code>Enumeration</code> returned is
			      still in use.
     */
    public Enumeration getParentsOf( Node node ) 
	throws NodeNotPresentInGraphException {
	isNodePresent( node );
	// if its in this object, it must be a SparseNode
	// (guaranteed by representation given above)  
	SparseNode snode = (SparseNode) node;

	ColorableGraphImpl.HiddenFilteringEnum filter = 
	    new ColorableGraphImpl.HiddenFilteringEnum( snode.getFromNodes() );
	return filter;
    }

    /** Constructs an enumeration for the parents of a specific node.
	<BR> <B>requires:</B> <code>node</code> is present in
	                      <code>this</code>.
	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     the nodes that have an edge between
			     <code>node</code> and them. 
        <BR> <B>requires:</B> <code>this</code> is not modified while
	                      the <code>Enumeration</code> returned is
			      still in use.
     */
    public Enumeration getNeighborsOf( Node node ) 
	throws NodeNotPresentInGraphException {
	isNodePresent( node );
	// if its in this object, it must be a SparseNode
	// (guaranteed by representation given above)  
	SparseNode snode = (SparseNode) node;

	ColorableGraphImpl.HiddenFilteringEnum filter = 
	    new ColorableGraphImpl.HiddenFilteringEnum( snode.getNeighboringNodes());
	return filter;
    }

    
    /** Temporarily removes <code>node</code> from graph.
	<BR> <B>requires:</B> <code>node</code> is present in
	                      <code>this</code>.
	<BR> <B>modifies:</B> <code>this</code>, <code>node</code>
	<BR> <B>effects:</B> Removes <code>node</code> from
	                     <code>this</code>, placing it in storage
			     for later replacement in the graph.  Also
			     updates all edges and nodes of
			     <code>this</code> to reflect that 
			     <code>node</code> has been hidden.  
    */
    void hideNode( ColorableNode node ) {
	// modifies: this.hiddenNodes
	
	isNodePresent( node );
	// if its in this object, it must be a SparseNode
	// (guaranteed by representation given above)  
	SparseNode sNode = (SparseNode) node;

	hiddenNodes.push( sNode );
	sNode.setHidden( true );
	
	Enumeration fromNodes = sNode.getFromNodes(); 
	while (fromNodes.hasMoreElements()) {
	    SparseNode from = (SparseNode) fromNodes.nextElement();
	    try {
		from.removeEdgeTo( sNode );
	    } catch (EdgeNotPresentException e) {
		// should never be thrown for this rep...
		e.printStackTrace();
		throw new RuntimeException(e.getMessage());
	    } catch (ObjectNotModifiableException e) {
		e.printStackTrace();
		throw new RuntimeException
		    (sNode + " is not allowed to be modified");
	    }
	}
	Enumeration toNodes = sNode.getToNodes(); 
	while (toNodes.hasMoreElements()) {
	    SparseNode to = (SparseNode) toNodes.nextElement();
	    try {
		to.removeEdgeFrom( sNode );
	    } catch (EdgeNotPresentException e) {
		// should never be thrown for this rep...
		e.printStackTrace();
		throw new RuntimeException(e.getMessage());
	    } catch (ObjectNotModifiableException e) {
		e.printStackTrace();
		throw new RuntimeException(e.getMessage());
	    }
	}
    }
    
    /** Replaces a hidden <code>node</code> in graph.
	<BR> <B>requires:</B> <code>node</code> was previously hidden
	                      in <code>this</code> and has not been
			      replaced since its last removal.
	<BR> <B>modifies:</B> <code>this</code>, <code>node</code>
	<BR> <B>effects:</B> If <code>node</code> was previously
	                     removed from <code>this</code>, and has
			     not been replaced since its last
			     removal), then moves <code>node</code>
			     from temporary storage back into the
			     graph.  It also updates all edges and
			     nodes of <code>this</code> to reflect
			     that <code>node</code> has been replaced.   
    */
    void unhideNode( ColorableNode node ) {
	// modifies: this.hiddenNodes
	if ( ! hiddenNodes.contains( node ) ) {
	    throw new NodeNotRemovedException
		("Node " + node + 
		 " was never hidden in graph " + this);
	}
	// if node WAS hidden at some point, it must be a SparseNode.
	SparseNode sNode = (SparseNode) node;

	hiddenNodes.removeElement( sNode );
	sNode.setHidden( false );

	Enumeration fromNodes = sNode.getFromNodes(); 
	
	try {
	    while (fromNodes.hasMoreElements()) {
		SparseNode from = (SparseNode) fromNodes.nextElement();
		from.addEdgeTo( sNode );
	    }
	    Enumeration toNodes = sNode.getToNodes(); 
	    while (toNodes.hasMoreElements()) {
		SparseNode to = (SparseNode) toNodes.nextElement();
		to.addEdgeFrom( sNode );
	    }
	} catch (IllegalEdgeException e) {
	    // should not happen...
	    e.printStackTrace();
	    throw new RuntimeException(e.getMessage());
	} catch (ObjectNotModifiableException e) {
	    e.printStackTrace();
	    throw new RuntimeException(sNode + " could not be unhid");
	}
    }

    /** Replaces all hidden nodes in graph.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> If a node was previously removed from
	                     <code>this</code>, and has not been
			     replaced since its last removal, then
			     moves node (and all other hidden ones)
			     back into the graph.  It also updates all
			     edges and nodes of <code>this</code> to
			     reflect that the nodes have been
			     replaced.    
    */
    void unhideAllNodes() {
	try {
	    // need to unhide nodes in reverse order.
	    while(! hiddenNodes.empty()) {
		unhideNode( (ColorableNode) hiddenNodes.peek() );
	    }
	} catch ( NodeNotRemovedException e ) {
	    // should never be thrown, as the internal representation
	    // requires that all nodes in hiddenNode vector are in
	    // fact hidden.
	    throw new RuntimeException(e.getMessage());
	}
    }

    /** Modifiability check.
	Subclasses should override this method to incorporate
	consistency checks, and should ensure that they call
	super.isModifiable in their check.
	<BR> <B>effects:</B> If <code>this</code> is allowed to be modified,
	                     returns true.  Else returns false.
    */
    public boolean isModifiable() {
	return hiddenNodes.size() == 0 && super.isModifiable();
    }
    

          /***** ColorableGraph Support methods *****/
    
    /** Wrapper method that calls <code>super.addNode(n)</code>.
	Needed to resolve ambiguity in method resolution at
	compile-time. 
    */
    public void addNode( Node n ) {
	super.addNode(n);
    }

    /** Ensures that this graph contains <code>node</code> (adapter
	method for <code>ColorableGraph</code> interface).
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
	@throws AlreadyHiddenException node is part of the set of
	        hidden nodes in <code>this</code>
	@throws IllegalArgumentException some aspect of
	        <code>node</code> prevents it from being added to the
		node-set for this graph.
    */
    public boolean addNode( Object node ) {
	throw new UnsupportedOperationException();
    }

    /** Returns all nodes in graph to their original state.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> the Node -> Color mapping 
	     is cleared and each hidden node is restored;
	     <code>this</code> is otherwise unchanged.
    */
    public void resetGraph() {
	super.resetGraph();
    }

    /** Reverts the graph to an uncolored state.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> clears the Node -> Color mapping.
    */
    public void resetColors() {
	super.resetColors();
    }

    /** Sets the color of <code>n</code>.
	<BR> <B>effects:</B> If <code>c</code> is null, then
	     removes <code>n</code> from the Node -> Color mapping.
	     Else puts (n, c) in the Node -> Color mapping.
        @throws IllegalArgumentException If <code>n</code> is not
	     present in the node set for <code>this</code> or
	     <code>n</code> has already been colored.
    */
    public void setColor(Object n, Color c) {
	throw new IllegalArgumentException();
    }


    /** Returns the color of <code>node</code>.
	<BR> <B>effects:</B> Returns the color associated with
	     <code>node</code> in the Node -> Color mapping, or null
	     if there is no entry in the mapping for
	     <code>node</code>.  
        @throws IllegalArgumentException If <code>node</code> is not
	     present in the node set for <code>this</code>.
    */
    public Color getColor(Object node) {
	throw new IllegalArgumentException();
    }

    /** Temporarily removes <code>node</code> from graph.
	<BR> <B>modifies:</B> <code>this</code>, <code>node</code>
	<BR> <B>effects:</B> Removes <code>node</code> and
	     <code>node</code>'s associated edges from
	     <code>this</code>, pushing it onto hidden-nodes stack
	     for later replacement in the graph.  Also updates all
	     edges and nodes of <code>this</code> to reflect that 
	     <code>node</code> has been hidden.
        @throws IllegalArgumentException If <code>node</code> is not
	     present in the node set for <code>this</code>.
    */
    public void hide( Object node ) {
	throw new IllegalArgumentException();
    }
    
    /** Replaces the last hidden node</code> into the graph.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> 
	     if hidden-nodes stack is empty,
	     then returns null
	     else let n = Pop(hidden-nodes stack).
	          puts `n' and its associated edges back in the
		  graph, updating all edges and nodes of
		  <code>this</code> to reflect that n has been 
		  replaced. 
		  Returns n. 
    */
    public Object replace() {
	return null;
    }

    /** Replaces all hidden nodes in graph.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> 
	     until hidden-nodes stack is empty,
	          let n = Pop(hidden-nodes stack).
	          puts `n' and its associated edges back in the
		  graph, updating all edges and nodes of
		  <code>this</code> to reflect that n has been 
		  replaced. 
    */
    public void replaceAll() {
	
    }

    /** Wrapper implementation of <code>super.addEdge(m,n)</code>.
	Needed to resolve ambiguity at compile-time.
    */
    public void addEdge( Node m, Node n ) {
	super.addEdge(m, n);
    }

    /** Ensures that this graph contains an edge between
	<code>m</code> and <code>n</code> (optional operation). 
	<BR> <B>modifies:</B> <code>this</code>.
	<BR> <B>effects:</B> If this method returns normally, an edge
	     between <code>m</code> and <code>n</code> will be in 
	     <code>this</code>.  Returns <tt>true</tt> if
	     <code>this</code> changed as a result of the call.
	@throws IllegalArgumentException <code>m</code> or
	     <code>n</code> is not present in the node set for
	     <code>this</code>.
	@throws UnsupportedOperationException addEdge is not supported
	     by this graph.
    */
    public boolean addEdge( Object m, Object n ) {
	throw new UnsupportedOperationException();
    }

    /** Returns the degree of <code>node</code>.
	<BR> <B>effects:</B> Returns the number of other
	     <code>Object</code>s that <code>node</code> is joined
	     with.
        @throws IllegalArgumentException If <code>node</code> is not
	     present in the node set for <code>this</code>.
    */
    public int getDegree( Object node ) {
	throw new IllegalArgumentException();
    }

    /** Returns a set view of the nodes in <code>this</code>.
	<BR> <B>effects:</B> Returns an <code>Set</code> of
	     the <code>Object</code>s that have been successfully
	     added to <code>this</code>.   
    */
    public Set nodeSet() {
	return Collections.unmodifiableSet(nodes);
    }

    /** Returns a collection view for the neighbors of a specific node. 
	<BR> <B>effects:</B> Returns an <code>Collection</code> of
	     <code>Object</code>s that have an edge between
	     <code>node</code> and them.  
	<BR> <B>mandates:</B> <code>node</code> is not removed from 
	     <code>this</code> while the returned
	     <code>Collection</code> is in use. 
	@throws IllegalArgumentException <code>node</code> is not
	     present in the node set for <code>this</code>.
     */
    public Collection neighborsOf( Object node ) {
	final SparseNode sn;
	try {
	    sn = (SparseNode) node;
	} catch (ClassCastException e) {
	    throw new IllegalArgumentException();
	}
	return new AbstractCollection() {
	    public int size() {
		return sn.getDegree();
	    }
	    public Iterator iterator() {
		Enumeration e = sn.getNeighboringNodes();
		return new EnumerationIterator(e);
	    }
	};
    }

    /** Returns a collection view of the edges joining
	<code>node</code> to nodes in the graph (adapter method).
	<BR> <B>effects:</B> Returns a <code>Collection</code> of
	     two-element <code>Collection</code>s (known as 
	     <i>pairs</i>) where each pair corresponds to an edge
	     { node, n } in this.  If the graph is modified while an
	     iteration over the collection is in progress, the results
	     of the iteration are undefined.  Order may or may not be
	     significant in the pairs returned.
        @throws IllegalArgumentException If <code>node</code> is not
	     present in the node set for <code>this</code>.
    */
    public Collection edgesFor( final Object node ) {
	final Collection neighbors = neighborsOf(node);
	return new AbstractCollection() {
	    public int size() {
		return neighbors.size();
	    }
	    public Iterator iterator() {
		return new UnmodifiableIterator() {
		    Iterator ns = neighbors.iterator();
		    public boolean hasNext() {
			return ns.hasNext();
		    }
		    public Object next() {
			return Default.pair(node,ns.next());
		    }
		};
	    }
	};
    }


    /** Returns a collection view of the edges contained in this graph
	(adapter method).
	<BR> <B>effects:</B> Returns a <code>Collection</code> of
	     two-element <code>Collection</code>s (known as
	     <i>pairs</i>) where each pair corresponds to an edge 
	     { n1, n2 } in this.  If the graph is modified while an
	     iteration over the collection is in progress, the results
	     of the iteration are undefined.  Order may or
	     may not be significant in the pairs returned.
    */
    public Collection edges() {
	return new AbstractCollection() {
	    public int size() {
		int d = 0;
		for(Iterator nodesI=nodes.iterator();nodesI.hasNext();){
		    SparseNode n = (SparseNode) nodesI.next();
		    d += n.getDegree();
		}
		return d/2;
	    }
	    public Iterator iterator() {
		final Set visited = new HashSet();
		final Iterator nodesI = nodeSet().iterator();
		final SparseNode firstNode;
		if (nodesI.hasNext()) {
		    firstNode = (SparseNode) nodesI.next();
		    visited.add(firstNode);
		} else {
		    firstNode = null;
		}
		final Iterator firstNeighbors;
		if (firstNode != null) {
		    Enumeration e = firstNode.getNeighboringNodes();
		    firstNeighbors = new EnumerationIterator(e);
		} else {
		    firstNeighbors = Default.nullIterator;
		}
		return new UnmodifiableIterator(){
		    SparseNode currNode = firstNode;
		    
		    // niter iterates over currNode's neighbors
		    Iterator niter = firstNeighbors;

		    // (neighbor != null) ==> next is <currNode,neighbor>
		    SparseNode neighbor = null;

		    // (hasNext() returns true) <==> neighbor != null
		    public boolean hasNext() {
			if(neighbor!=null){
			    return true;
			}
			while(true) {
			    while(niter.hasNext()) {
				neighbor=(SparseNode)niter.next();
				if(!visited.contains(neighbor)) 
				    return true;
			    }
			    // got here, finished with neighbors for
			    // currNode 
			    neighbor = null;

			    if(nodesI.hasNext()) {
				currNode = (SparseNode)nodesI.next();
				visited.add(currNode);
				Enumeration e=currNode.getNeighboringNodes();
				niter= new EnumerationIterator(e);
			    } else {
				return false;
			    }
			}
		    }
		    public Object next() {
			if(hasNext()){
			    List edge = Default.pair(currNode,neighbor);
			    neighbor = null;
			    return edge;
			} else {
			    throw new java.util.NoSuchElementException();
			}
		    }
		};
	    }
	};
    }
}
