// SparseGraph.java, created Wed Jan 13 16:32:04 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.Util.UniqueStack;
import java.util.Enumeration;

/**
 * <code>SparseGraph</code> is an implementation of a
 * <code>ColorableGraph</code> object.  Nodes are represented by a
 * list <code>SparseNode</code>s, and edges are represented by the
 * references <code>SparseNode</code>s store internally.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: SparseGraph.java,v 1.1.2.4 1999-01-19 16:08:00 pnkfelix Exp $ 
 */

public class SparseGraph extends ColorableGraph {

    // List of hidden nodes, so that we may properly check when nodes
    // are unhid.
    UniqueStack hiddenNodes;
    
    /** Creates a <code>SparseGraph</code>. */
    public SparseGraph() {
        super();
	hiddenNodes = new UniqueStack();
    }
            
    /** Ensures that only <code>SparseNode</code>s are added to <code>this</code>.
	<BR> effects: If <code>node</code> is of the wrong type for
	              the graph implementation being used, throws 
		      WrongNodeTypeException.
		      Else does nothing.
    */
    protected void checkNode( Node node ) throws WrongNodeTypeException {
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

    /** Adds an edge from <code>from</code> to <code>to</code>.
	<BR> modifies: <code>from</code>,
	               <code>to</code>
	<BR> effects: If <code>from</code> or <code>to</code> are not
	              already present in this, then throws a
		      NodeNotPresentInGraphException.
		      Else adds an edge from <code>from</code> to
		      <code>to</code>.
    */
    public void makeEdge( Node from, Node to ) 
	throws NodeNotPresentInGraphException, IllegalEdgeException {
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
	<BR> effects: If <code>node</code> is present in
	              <code>this</code>, then returns the number of
		      other <code>ColorableNode</code>s that
		      <code>node</code> is connected to.  
		      Else throws NodeNotPresentInGraphException.
    */
    public int getDegree( Node node ) throws NodeNotPresentInGraphException {
	isNodePresent( node );
	// if its in this object, it must be a SparseNode
	// (guaranteed by representation given above)  
	SparseNode snode = (SparseNode) node;
	return snode.getDegree();
    }

    /** Constructs an enumeration for the children of a specific node.
	<BR> effects: Returns an <code>Enumeration</code> of the nodes
	              that have an edge from <code>node</code> to them
		      that are not hidden.
     */
    public Enumeration getChildrenOf( Node node ) 
	throws NodeNotPresentInGraphException {
		isNodePresent( node );
	// if its in this object, it must be a SparseNode
	// (guaranteed by representation given above)  
	SparseNode snode = (SparseNode) node;
	
	ColorableGraph.HiddenFilteringEnum filter = 
	    new ColorableGraph.HiddenFilteringEnum();
	filter.setEnumeration( snode.getToNodes() );
	return filter;
    }

    /** Constructs an enumeration for the parents of a specific node.
	<BR> effects: Returns an <code>Enumeration</code> of the nodes
	              that have an edge from them to <code>node</code>.
     */
    public Enumeration getParentsOf( Node node ) 
	throws NodeNotPresentInGraphException {
	isNodePresent( node );
	// if its in this object, it must be a SparseNode
	// (guaranteed by representation given above)  
	SparseNode snode = (SparseNode) node;

	ColorableGraph.HiddenFilteringEnum filter = 
	    new ColorableGraph.HiddenFilteringEnum();
	filter.setEnumeration( snode.getFromNodes() );
	return filter;
    }

    /** Constructs an enumeration for the parents of a specific node.
	<BR> effects: Returns an <code>Enumeration</code> of the nodes
	              that have an edge between <code>node</code> and
		      them. 
     */
    public Enumeration getNeighborsOf( Node node ) 
	throws NodeNotPresentInGraphException {
	isNodePresent( node );
	// if its in this object, it must be a SparseNode
	// (guaranteed by representation given above)  
	SparseNode snode = (SparseNode) node;

	ColorableGraph.HiddenFilteringEnum filter = 
	    new ColorableGraph.HiddenFilteringEnum();
	filter.setEnumeration( snode.getNeighboringNodes() );
	return filter;
    }

    
    /** Temporarily removes <code>node</code> from graph.
	<BR> effects: If <code>node</code> is present in
	              <code>this</code>, then removes
		      <code>node</code> from <code>this</code>,
		      placing it in storage for later replacement in
		      the graph.  It also updates all edges and nodes
		      of <code>this</code> to reflect that
		      <code>node</code> has been removed. 
		      Else throws NodeNotPresentInGraphException
    */
    void hideNode( ColorableNode node ) 
	throws NodeNotPresentInGraphException {
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
	<BR> effects: If <code>node</code> was previously removed from
	              <code>this</code>, and has not been replaced
		      since its last removal), then moves
		      <code>node</code> from temporary storage back
		      into the graph.  It also updates all edges and
		      nodes of <code>this</code> to reflect that
		      <code>node</code> has been replaced.  
		      Else throws NodeNotRemovedException.
    */
    void unhideNode( ColorableNode node ) 
	throws NodeNotRemovedException {
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
	<BR> effects: If a node was previously removed from 
	              <code>this</code>, and has not been replaced
		      since its last removal, then moves node (and all
		      other hidden ones) back into the graph.  It also
		      updates all edges and nodes of <code>this</code>
		      to reflect that the nodes have been replaced.   
		      Else throws NodeNotRemovedException.
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
	effects: if <code>this</code> is allowed to be modified,
	returns true.  Else returns false. 
    */
    public boolean isModifiable() {
	return hiddenNodes.size() == 0 && super.isModifiable();
    }
    
}
