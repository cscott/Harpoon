// ColorableGraph.java, created Wed Jan 13 14:13:21 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import java.util.*;

/**
 * <code>ColorableGraph</code> defines a set of methods that graphs to
 * be colored should implement.  They are meant to be called by the
 * graph colorers defined in this package.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: ColorableGraph.java,v 1.1.2.1 1999-01-14 20:12:10 pnkfelix Exp $ */

public abstract class ColorableGraph extends Graph {
    
    /** Creates a <code>ColorableGraph</code>. */
    public ColorableGraph() {
        super();
    }

    /** Reverts the graph to an uncolored state. 
	<BR> modifies: all colored nodes in <code>this</code>
	<BR> effects: Each colored node is given an 'uncolored'
	              state. 
    */
    void resetColors() {
	Enumeration nodes = super.getNodes();
	while(nodes.hasMoreElements()) {
	    ColorableNode node = (ColorableNode) nodes.nextElement();
	    node.setColor(null);
	}
    }

    /** Temporarily removes <code>node</code> from graph.
	effects: If <code>node</code> is present in <code>this</code>,
	         then removes <code>node</code> from
		 <code>this</code>.  It also updates all edges
		 and nodes of <code>this</code> to reflect that
		 <code>node</code> has been removed.
		 Else throws NodeNotPresentInGraphException
    */
    abstract void hideNode( ColorableNode node ) 
	throws NodeNotPresentInGraphException;
    
    /** Replaces a hidden <code>node</code> in graph.
	effects: If <code>node</code> was previously removed from 
	         <code>this</code>, and has not been replaced since
		 its last removal, then moves <code>node</code>
		 back into the graph.  It also
		 updates all edges and nodes of <code>this</code> to
		 reflect that <code>node</code> has been replaced. 
		 Else throws NodeNotRemovedException.
    */
    abstract void unhideNode( ColorableNode node ) 
	throws NodeNotRemovedException;

    /** Replaces all hidden nodes in graph.
	effects: If a node was previously removed from 
	         <code>this</code>, and has not been replaced since
		 its last removal, then moves node (and all other
		 hidden ones) back into the graph.  It also updates
		 all edges and nodes of <code>this</code> to reflect
		 that the nodes have been replaced.  
		 Else throws NodeNotRemovedException.
    */
    abstract void unhideAllNodes();
    
    /** Ensures that only <code>ColorableNode</code>s are added to <code>this</code>.
	<BR> effects: If <code>node</code> is not an instance of a
	              ColorableNode, throws WrongNodeTypeException. 
		      Else does nothing.
    */
    protected void checkNode( Node node ) 
	throws WrongNodeTypeException {
	if (! (node instanceof ColorableNode) ) {
	    throw new WrongNodeTypeException(node + " is not a ColorableNode.");
	}
    }

    /** Constructs an enumeration for all the nodes.
	<BR> effects: Returns an <code>Enumeration</code> of the nodes
	              that have been successfully added to
		      <code>this</code> that are not currently hidden.
    */
    public Enumeration getNodes() {
	class HiddenFilterer implements Enumeration {
	    Enumeration nodes = ColorableGraph.this.getNodeVector().elements();
	    ColorableNode next = null;
	    public boolean hasMoreElements() {
		while(next == null && nodes.hasMoreElements()) {
		    ColorableNode n = (ColorableNode) nodes.nextElement();
		    if (! n.isHidden() ) {
			next = n;
		    }
		}
		return (next != null);
	    }
	    public Object nextElement() {
		Object rtrn;
		if (hasMoreElements()) {
		    rtrn = next;
		    next = null;
		} else {
		    throw new NoSuchElementException();
		}
		return rtrn;
 	    }
	}

	return new HiddenFilterer();
    }
    
    
}

