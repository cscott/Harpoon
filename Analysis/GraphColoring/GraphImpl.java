// Graph.java, created Wed Jan 13 15:51:26 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import harpoon.Util.UniqueVector;

import java.util.Enumeration;

/**
 * <code>GraphImpl</code> is an abstract class containing the framework
 * for implementing a graph object.
 * 
 * @deprecated replaced by <code>Graph</code> interface.
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: GraphImpl.java,v 1.1.2.5 2001-06-17 22:29:38 cananian Exp $
 */

abstract class GraphImpl extends AbstractGraph {
    
    protected UniqueVector nodes;

    /** Creates a <code>Graph</code>. */
    public GraphImpl() {
        nodes = new UniqueVector();
    }
    
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
    public void addNode( Node node ) 
	throws WrongNodeTypeException, ObjectNotModifiableException { 
	// modifies: this.nodes
	if (this.isModifiable()) {
	    checkNode( node );
	    nodes.addElement( node );
	} else {
	    throw new ObjectNotModifiableException
		(node + " can not be added to " + 
		 this + "; it is not modifiable.");
	}
    }
    
    /** Node type-checking method for subclasses to implement.
	<BR> <B>effects:</B> If <code>node</code> is of the wrong type
	                     for the graph implementation being used,
			     throws
			     <code>WrongNodeTypeException</code>. 
			     Else does nothing. 
    */
    protected abstract void checkNode( Node node );

    /** Generates an edge from <code>from</code> to <code>to</code>.
	Subclasses should implement this method to match their
	internal representation of a graph.
	<BR> <B>requires:</B> <code>from</code> and <code>to</code>
	                      are present in <code>this</code> and are
			      valid targets for a new edge.
	<BR> <B>modifies:</B> <code>this</code>, <code>from</code>,
	                      <code>to</code>.
    */
    protected abstract void makeEdge( Node from, Node to );    

    /** Adds an edge from <code>from</code> to <code>to</code>.
	<BR> <B>requires:</B> <code>from</code> and <code>to</code>
	                      are present in <code>this</code> and are
			      valid targets for a new edge.
	<BR> <B>modifies:</B> <code>this</code>.
     */
    public void addEdge( Node from, Node to ) {
	if (this.isModifiable()) {
	    makeEdge( from, to );
	} else {
	    throw new ObjectNotModifiableException
		("Edge can not be added to " + 
		 this + "; it is not modifiable.");
	}

    }
    
    /** Returns the degree of <code>node</code>.
	<BR> <B>requires:</B>: <code>node</code> is present in
	                       <code>this</code>
	<BR> <B>effects:</B> Returns the number of other
	                     <code>ColorableNode</code>s that
			     <code>node</code> is connected to. 
    */
    public abstract int getDegree( Node node ) 
	throws NodeNotPresentInGraphException; 
	
    /** Constructs an enumeration for all the nodes.
	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     the <code>Node</code>s that have been
			     successfully added to <code>this</code>.  
        <BR> <B>requires:</B> <code>this</code> is not modified while
	                      the <code>Enumeration</code> returned is
			      still in use.
    */
    public Enumeration getNodes() {
	return nodes.elements();
    }

    /** Constructs an enumeration for the children of a specific node.
	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     <code>Node</code>s that have an edge from
			     <code>node</code> to them. 
        <BR> <B>requires:</B> <code>this</code> is not modified while
	                      the <code>Enumeration</code> returned is
			      still in use.
     */
    public abstract Enumeration getChildrenOf( Node node ) 
	throws NodeNotPresentInGraphException;

    /** Constructs an enumeration for the parents of a specific node.
	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     <code>Node</code>s that have an edge from
			     them to <code>node</code>. 
        <BR> <B>requires:</B> <code>this</code> is not modified while
	                      the <code>Enumeration</code> returned is
			      still in use.
     */
    public abstract Enumeration getParentsOf( Node node ) 
	throws NodeNotPresentInGraphException;

    /** Constructs an enumeration for the parents of a specific node.
	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     <code>Node</code>s that have an edge
			     between <code>node</code> and them. 
        <BR> <B>requires:</B> <code>this</code> is not modified while
	                      the <code>Enumeration</code> returned is
			      still in use.
     */
    public abstract Enumeration getNeighborsOf( Node node ) 
	throws NodeNotPresentInGraphException;

    /** Nodes accessor method for subclass use.
	<BR> <B>effects:</B> Returns the <code>UniqueVector</code> of the
	                     <code>Node</code>s that have been
			     successfully added to <code>this</code>.
    */
    protected UniqueVector getNodeVector() {
	return nodes;
    }

    /** Modifiability check.
	Subclasses should override this method to incorporate
	consistency checks
	<BR> <B>effects:</B> If <code>this</code> is allowed to be modified,
	                     returns true.  Else returns false. 
    */
    public boolean isModifiable() {
	return true;
    }

}



