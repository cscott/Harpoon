// Graph.java, created Wed Jan 13 15:51:26 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.Util.UniqueVector;

import java.util.Enumeration;

/**
 * <code>Graph</code> is an abstract class containing the framework
 * for implementing a graph object.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: Graph.java,v 1.1.2.3 1999-01-19 16:07:59 pnkfelix Exp $
 */

public abstract class Graph  {
    
    private UniqueVector nodes;

    /** Creates a <code>Graph</code>. */
    public Graph() {
        nodes = new UniqueVector();
    }
    
    /** Adds <code>node</code> to <code>this</code>
	<BR> modifies: <code>this.nodes</code>
	<BR> effects: If <code>node</code> is of the wrong type for the
	              graph implementation being used, throws
		      WrongNodeTypeException.
		      Else If <code>node</code> isn't already present
		      in this, then adds <code>node</code> to
		      <code>this.nodes</code>. 
		      Else does nothing.
    */
    public void addNode( Node node ) 
	throws WrongNodeTypeException, ObjectNotModifiableException { 
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
	<BR> effects: If <code>node</code> is of the wrong type for
	              the graph implementation being used, throws 
		      WrongNodeTypeException.
		      Else does nothing.
    */
    protected abstract void checkNode( Node node ) 
	throws WrongNodeTypeException;

    /** Generates an edge from <code>from</code> to <code>to</code>.
	Subclasses should implement this method to match their
	internal representation of a graph. 
    */
    protected abstract void makeEdge( Node from, Node to ) 
	throws NodeNotPresentInGraphException, IllegalEdgeException;
    
    /** Adds an edges from <code>from</code> to <code>to</code>.
     */
    public void addEdge( Node from, Node to ) throws
	NodeNotPresentInGraphException, IllegalEdgeException,
	ObjectNotModifiableException {
	if (this.isModifiable()) {
	    makeEdge( from, to );
	} else {
	    throw new ObjectNotModifiableException
		("Edge can not be added to " + 
		 this + "; it is not modifiable.");
	}

    }
    
    /** Returns the degree of <code>node</code>.
	<BR> effects: If <code>node</code> is present in
	              <code>this</code>, then returns the number of
		      other <code>ColorableNode</code>s that
		      <code>node</code> is connected to.  
		      Else throws NodeNotPresentInGraphException.
    */
    public abstract int getDegree( Node node ) 
	throws NodeNotPresentInGraphException; 
	
    /** Constructs an enumeration for all the nodes.
	<BR> effects: Returns an <code>Enumeration</code> of the nodes
	              that have been successfully added to
		      <code>this</code>.  
    */
    public Enumeration getNodes() {
	return nodes.elements();
    }

    /** Constructs an enumeration for the children of a specific node.
	<BR> effects: Returns an <code>Enumeration</code> of the nodes
	              that have an edge from <code>node</code> to them.
     */
    public abstract Enumeration getChildrenOf( Node node ) 
	throws NodeNotPresentInGraphException;

    /** Constructs an enumeration for the parents of a specific node.
	<BR> effects: Returns an <code>Enumeration</code> of the nodes
	              that have an edge from them to <code>node</code>.
     */
    public abstract Enumeration getParentsOf( Node node ) 
	throws NodeNotPresentInGraphException;

    /** Constructs an enumeration for the parents of a specific node.
	<BR> effects: Returns an <code>Enumeration</code> of the nodes
	              that have an edge between <code>node</code> and
		      them. 
     */
    public abstract Enumeration getNeighborsOf( Node node ) 
	throws NodeNotPresentInGraphException;

    /** Nodes accessor method for subclass use.
	<BR> effects: Returns an <code>UniqueVector</code> of the
	              nodes that have been successfully added to
		      <code>this</code>.
    */
    protected UniqueVector getNodeVector() {
	return nodes;
    }

    /** Modifiability check.
	Subclasses should override this method to incorporate
	consistency checks
	effects: if <code>this</code> is allowed to be modified,
	returns true.  Else returns false. 
    */
    public boolean isModifiable() {
	return true;
    }

}



