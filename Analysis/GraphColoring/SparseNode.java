// SparseNode.java, created Wed Jan 13 16:17:46 1999 by pnkfelix
package harpoon.Analysis.GraphColoring;

import harpoon.ClassFile.*;
import harpoon.Util.UniqueVector;
import java.util.Enumeration;

/**
 * <code>SparseNode</code> is an implementation of a ColorableNode for
 * use with the SparseGraph object.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: SparseNode.java,v 1.1.2.3 1999-01-14 23:53:36 pnkfelix Exp $ 
 */

public class SparseNode extends ColorableNode {
    
    private UniqueVector toNodes;
    private UniqueVector fromNodes;

    // unifiedNodes tracks the union of the TO and FROM set, to
    // simplify the getDegree() method
    private UniqueVector unifiedNodes;

    /** Creates a <code>SparseNode</code>. */
    public SparseNode() {
	super();
        toNodes = new UniqueVector();
	fromNodes = new UniqueVector();
	unifiedNodes = new UniqueVector();
    }

    /** Adds an edge from <code>this</code> to <code>to</code>    
     	<BR> modifies: <code>this.toNodes</code>,                      
	               <code>this.unifiedNodes</code>
        <BR> effects: If <code>this</code> is not allowed to be
	              modified, throws ObjectNotModifiableException. 
		      Else adds <code>to</code> to the
		      <code>toNodes</code> list and the
		      <code>unifiedNodes</code> list.  
     */
    void addEdgeTo( SparseNode to ) 
	throws IllegalEdgeException, ObjectNotModifiableException { 
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
        <BR> modifies: <code>this.fromNodes</code>,
                       <code>this.unifiedNodes</code>.
        <BR> effects: If <code>this</code> is not allowed to be
	              modified, throws ObjectNotModifiableException. 
		      Else adds <code>from</code> to the
		      <code>fromNodes</code> list and the
		      <code>unifiedNodes</code> list.
     */ 
    void addEdgeFrom( SparseNode from ) 
	throws IllegalEdgeException, ObjectNotModifiableException {
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
	<BR> modifies: <code>this.fromNodes</code>,
	               <code>this.unifiedNodes</code>.
	<BR> effects: If <code>this</code> is not allowed to be
	              modified, throws ObjectNotModifiableException. 
		      Else If <code>from</code> has an edge from it to
	              <code>this</code>, and <code>this</code> is
		      modifiable, removes <code>from</code>
		      from the <code>fromNodes</code> list, and if
		      there is no edge from <code>this</code> to
		      <code>from</code>, removes <code>from</code>
		      from the <code>unifiedNodes</code> list.
		      Else throws EdgeNotPresentException.
    */
    void removeEdgeFrom( SparseNode from ) 
	throws EdgeNotPresentException, ObjectNotModifiableException {
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
	<BR> modifies: <code>this.toNodes</code>,
	               <code>this.unifiedNodes</code>.
	<BR> effects: If <code>this</code> is not allowed to be
	              modified, throws ObjectNotModifiableException. 
	              Else If <code>this</code> has an edge from it to 
	              <code>to</code>, removes <code>to</code>
		      from the <code>toNodes</code> list, and if
		      there is no edge from <code>to</code> to
		      <code>this</code>, removes <code>to</code>
		      from the <code>unifiedNodes</code> list. 
		      Else throws EdgeNotPresentException.    */
    void removeEdgeTo( SparseNode to ) 
	throws EdgeNotPresentException, ObjectNotModifiableException {
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
    
    /** returns the degree of <code>this</code>.
	Note that if <code>this</code> is hidden, then the degree
	returned is not guaranteed to be correct.
      	<BR> effects: If <code>node</code> is present in
      	              <code>this</code>, then returns the number of
      		      other <code>ColorableNode</code>s that
      		      <code>node</code> is connected to. 
      		      Else throws NodeNotPresentInGraphException.
     */
    int getDegree() {
	return unifiedNodes.size();
    }    
    
    /** Returns a to-node enumerator.
 	<BR> effects: Returns an <code>Enumeration</code> of nodes that
	<code>this</code> has edges to.
    */
    Enumeration getToNodes() {
	return toNodes.elements();
    }

    /** Returns a from-node enumerator.
 	<BR> effects: Returns an <code>Enumeration</code> of nodes that
	<code>this</code> has edges from.
    */
    Enumeration getFromNodes() {
	return fromNodes.elements();
    }

    /** Returns a neighboring-node enumerator.
 	<BR> effects: Returns an <code>Enumeration</code> of nodes that
	<code>this</code> has edges to or from.
    */
    Enumeration getNeighboringNodes() {
	return unifiedNodes.elements();
    }

}



