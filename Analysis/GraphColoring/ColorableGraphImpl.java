// ColorableGraph.java, created Wed Jan 13 14:13:21 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * <code>ColorableGraphImpl</code> defines a set of methods that graphs to
 * be colored should implement.  They are meant to be called by the
 * graph colorers defined in this package.
 *
 * @deprecated replaced by <code>ColorableGraph</code> interface.
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ColorableGraphImpl.java,v 1.1.2.4 2001-06-17 22:29:38 cananian Exp $ */

abstract class ColorableGraphImpl extends GraphImpl {

    private boolean mutable;

    /** Creates a <code>ColorableGraph</code>. */
    public ColorableGraphImpl() {
        super();
	mutable = true;
    }

    /** Returns the graph to a modifiable state.
	<BR> <B>modifies:</B> all colored and hidden nodes in
	                      <code>this</code>.
	<BR> <B>effects:</B> each colored node is given an 'uncolored'
	                     state and each hidden node is unhid.
    */
    public void resetGraph() {
	unhideAllNodes();
	resetColors();
    }

    /** Reverts the graph to an uncolored state.
	<BR> <B>modifies:</B> all colored nodes in <code>this</code>
	<BR> <B>effects:</B> Each colored node is given an 'uncolored'
	                     state. 
    */
    void resetColors() {
	Enumeration nodes = super.getNodes();
	while(nodes.hasMoreElements()) {
	    ColorableNode node = (ColorableNode) nodes.nextElement();
	    try {
		node.setColor(null);
	    } catch (NodeAlreadyColoredException e) {
		// this should not be thrown, because we are not
		// coloring the node.
		throw new RuntimeException
		    (e.getMessage());
	    }
	}
	mutable = true;
    }

    /** Sets the color of <code>node</code>.
	<BR> <B>requires:</B> <code>node</code> is present in graph
	                      and has not been colored.
	<BR> <B>effects:</B> Sets the color of <code>node</code> to
	                     <code>color</code>.  If
			     <code>color</code> is <code>null</code>
			     then <code>node</code> is given an
			     'uncolored' state.  Marks
			     <code>this</code> as unmodifiable.
    */
    void setColor(ColorableNode node, Color color) {
	// modifies: this.mutable
	mutable = false;
	node.setColor( color );
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
    abstract void hideNode( ColorableNode node );
    
    /** Replaces a hidden <code>node</code> in graph.
	<BR> <B>NOTE:</B> This method may be replaced by a "unhide
	                  last node" or equivalent method, like
			  popping a stack, because the current
			  implementation does not support unhiding
			  nodes out-of-reverse-order.  (I will look
			  into the difficulty of implementing this
			  correctly). 
	<BR> <B>requires:</B> <code>node</code> was previously hidden
	                      in <code>this</code> and has not been
			      replaced since its last removal.
	<BR> <B>modifies:</B> <code>this</code>, <code>node</code>
	<BR> <B>effects:</B> Moves <code>node</code> back into
			     the graph.  It also updates all edges and
			     nodes of <code>this</code> to reflect
			     that <code>node</code> has been replaced.  
    */
    abstract void unhideNode( ColorableNode node );

    /** Replaces all hidden nodes in graph.
	<BR> <B>modifies:</B> <code>this</code>, all
	                      <code>Node</code>s in <code>this</code> 
	<BR> <B>effects:</B> If a node was previously removed from
	                     <code>this</code>, and has not been
			     replaced since its last removal, then
			     moves node (and all other hidden ones)
			     back into the graph.  It also updates all
			     edges and nodes of <code>this</code> to
			     reflect that the nodes have been
			     replaced.   
    */
    abstract void unhideAllNodes();
    
    /** Ensures that only <code>ColorableNode</code>s are added to <code>this</code>.
	<BR> <B>requires:</B> <code>node</code> is an instance of a
	                      <code>ColorableNode</code>. 
	<BR> <B>effects:</B> Does nothing.
    */
    protected void checkNode( Node node ) {
	if (! (node instanceof ColorableNode) ) {
	    throw new WrongNodeTypeException(node + " is not a ColorableNode.");
	}
    }

    /** Wrapper class for Enumeration that filters out hidden nodes. */
    protected static class HiddenFilteringEnum implements Enumeration {
	Enumeration nodes;
	ColorableNode next = null;

	HiddenFilteringEnum(Enumeration e) {
	    nodes = e;
	}

	public boolean hasMoreElements() {
	    while(next == null && nodes.hasMoreElements()) {
		ColorableNode n = (ColorableNode)
		    nodes.nextElement();
		if (! n.isHidden()) {
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


    /** Constructs an enumeration for all the nodes.
	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     the <code>ColorableNode</code>s that have
			     been successfully added to
			     <code>this</code> that are not currently
			     hidden. 
	<BR> <B>requires:</B> <code>this</code> is not modified while
	                      the <code>Enumeration</code> returned is
			      in use.
    */
    public Enumeration getNodes() {
	HiddenFilteringEnum hf = new HiddenFilteringEnum(super.getNodes());
	return hf;
    }
    
    /** Modifiability check.
	Subclasses should override this method to incorporate
	consistency checks, and should ensure that they call
	<code>super.isModifiable</code> in their check.
	<BR> <B>effects:</B> if <code>this</code> is allowed to be
	                     modified, returns true.  Else returns
			     false.   
    */
    public boolean isModifiable() {
	return mutable && super.isModifiable();
    }


}

