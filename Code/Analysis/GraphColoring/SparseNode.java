// SparseNode.java, created Wed Jan 13 16:17:46 1999 by pnkfelix
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import harpoon.Util.Collections.UniqueVector;
import java.util.Enumeration;

/**
 * <code>SparseNode</code> is an extension of a ColorableNode for
 * use with the SparseGraph object.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SparseNode.java,v 1.2 2002-02-25 20:57:17 cananian Exp $ 
 */

public abstract class SparseNode extends ColorableNode {
    
    private UniqueVector toNodes;
    private UniqueVector fromNodes;

    // unifiedNodes tracks the union of the TO and FROM set, to
    // simplify the getDegree() method
    UniqueVector unifiedNodes;

    /** Creates a <code>SparseNode</code>. */
    public SparseNode() {
	super();
        toNodes = new UniqueVector();
	fromNodes = new UniqueVector();
	unifiedNodes = new UniqueVector();
    }

    /** Adds an edge from <code>this</code> to <code>to</code>
	<BR> <B>requires:</B> 
	     1. <code>this</code> is allowed to be modified. 
	     2. <code>this</code> does not have an edge to <code>to</code>
	<BR> <B>modifies:</B> this
        <BR> <B>effects:</B> Adds an edge from <code>this</code> to
	                     <code>to</code>  
     */
    void addEdgeTo( SparseNode to ) {
     	// modifies: this.toNodes, this.unifiedNodes
	if (toNodes.contains( to )) {
	    throw new IllegalEdgeException
		("SparseNode does not allow duplicate edges");
	}

	if (this.isModifiable()) {
	    toNodes.addElement( to );
	    unifiedNodes.addElement( to );
	} else {
	    throw new ObjectNotModifiableException
		(this + " is not allowed to be modified.");
	}
    }

    /** Adds an edge from <code>from</code> to <code>this</code>.
	<BR> <B>requires:</B> 
	     1. <code>this</code> is allowed to be modified
	     2. <code>this</code> does not have an edge from <code>from</code>
	<BR> <B>modifies:</B> <code>this</code>
        <BR> <B>effects:</B> Adds an edge from <code>from</code> to <code>this</code>
     */ 
    void addEdgeFrom( SparseNode from ) throws IllegalEdgeException {
        // modifies: this.fromNodes, this.unifiedNodes
	if (fromNodes.contains( from )) {
	    throw new IllegalEdgeException
		("SparseNode does not allow duplicate edges");
	}

	if (this.isModifiable()) {
	    fromNodes.addElement( from );
	    unifiedNodes.addElement( from );
	} else {
	    throw new ObjectNotModifiableException
		(this + " is not allowed to be modified.");
	}
    }

    /** Removes an edge from <code>from</code> to <code>this</code>.
	<BR> <B>requires:</B> <code>this</code> is allowed to be
	                      modified and an edge exists from
			      <code>from</code> to <code>this</code>
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> Removes the edge from <code>from</code>
	                     to <code>this</code> 
    */
    void removeEdgeFrom( SparseNode from ) 
	throws EdgeNotPresentException, ObjectNotModifiableException {
	// modifies: this.fromNodes, this.unifiedNodes
	if (this.isModifiable()) {
	    if (! fromNodes.removeElement( from )) {
		throw new EdgeNotPresentException
		    ("No Edge from " + from +" to " + this);
	    } else {
		if ( ! toNodes.contains( from ) ) {
		    unifiedNodes.removeElement( from );
		}
	    }
	} else {
	    throw new ObjectNotModifiableException
		(this + " is not allowed to be modified.");
	}
    }

    /** Removes an edge from <code>this</code> to <code>to</code>.
	<BR> <B>requires:</B> <code>this</code> is allowed to be
	                      modified, and an edge exists from
			      <code>this</code> to <code>to</code>
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> Removes the edge from <code>this</code>
	                     to <code>to</code>. 
    */
    void removeEdgeTo( SparseNode to ) 
	throws EdgeNotPresentException, ObjectNotModifiableException {
	// modifies: this.toNodes, this.unifiedNodes

	if (this.isModifiable()) {
	    if (! toNodes.removeElement( to )) {
		throw new EdgeNotPresentException
		    ("No Edge from " + to +" to " + this);
	    } else {
		if ( ! fromNodes.contains( to ) ) {
		    unifiedNodes.removeElement( to );
		}
	    }
	} else {
	    throw new ObjectNotModifiableException
		(this + " is not allowed to be modified.");
	}
    }
    
    /** Returns the degree of <code>this</code>.
      	<BR> <B>effects:</B> Returns the number of other
	                     <code>SparseNode</code>s that
			     <code>node</code> is connected to.  
    */
    int getDegree() {
	return unifiedNodes.size();
    }    
    
    /** Returns a to-node enumerator.
 	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     nodes that <code>this</code> has edges
			     to.
	<BR> <B>requires:</B> <code>this</code> and the
	                      <code>Node</code>s connected to it are
			      not modified while the
			      <code>Enumeration</code> returned is still in use.
    */
    Enumeration getToNodes() {
	return toNodes.elements();
    }

    /** Returns a from-node enumerator.
 	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     nodes that <code>this</code> has edges
			     from.
	<BR> <B>requires:</B> <code>this</code> and the
	                      <code>Node</code>s connected to it are
			      not modified while the
			      <code>Enumeration</code> returned is still in use.
    */
    Enumeration getFromNodes() {
	return fromNodes.elements();
    }

    /** Returns a neighboring-node enumerator.
 	<BR> <B>effects:</B> Returns an <code>Enumeration</code> of
	                     nodes that <code>this</code> has edges to
			     or from. 
	<BR> <B>requires:</B> <code>this</code> and the
	                      <code>Node</code>s connected to it are
			      not modified while the
			      <code>Enumeration</code> returned is
			      still in use. 
    */
    Enumeration getNeighboringNodes() {
	return unifiedNodes.elements();
    }

}



