// TreeSet.java, created Fri Feb  5 17:30:03 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Enumeration;

/**
 * <code>TreeSet</code> is a simple Binary Tree structured
 * implementation of a Set.  Because it does not enforce the binary
 * property (as would a RedBlack tree or an AVL tree), it is not
 * guaranteed to have log(N) search time.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: TreeSet.java,v 1.1.2.2 1999-06-17 21:00:53 cananian Exp $
 * @deprecated Use <code>java.util.TreeSet</code> instead.
 */
public abstract class TreeSet extends Set {
    
    /** Internal data structure for Binary Tree.  hash LessOrEq on
	left, Greater on right. 
    */ 
    private class TreeNode {
	
	TreeNode left;
	TreeNode right;
	Object cell;

	TreeNode(Object o) {
	    left = null;
	    right = null;
	    cell = o;
	}

	/** if this != n then compare and put on appropriate side. */
	void add(TreeNode n) {
	    if (this.cell.equals( n.cell )) {
		// do nothing
		return;
	    } else if (n.cell.hashCode() <= this.cell.hashCode()) {
		// put on left
		if (left == null) {
		    left = n;
		} else {
		    left.add(n);
		}
	    } else {
		// put on right
		if (right == null) {
		    right = n;
		} else {
		    right.add(n);
		}
	    }
	}
    }
    
    /** Creates a <code>TreeSet</code>. */
    public TreeSet() {
        
    }


    /** Clears <code>this</code>.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> Removes all elements of
	                     <code>this</code>
    */
    public abstract void clear();
    
    /** Remove a member from the <code>Set</code>. 
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> Removes <code>o</code> from
	                     <code>this</code>, if it is present.
			     Else does nothing. 
     */
    public abstract void remove(Object o);

    /** Ensure that an object is a member of <code>this</code>. 
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> Adds <code>o</code> to <code>this</code>,
	                     if it is present.  Else does nothing. 
     */
    public abstract void union(Object o);

    /** Returns a arbitrary element from <code>this</code>.
	<BR> <B>effects:</B> Returns an element of <code>this</code>.
    */
    public abstract Object getArbitrary();
    
    /** Determines if an object is a member of the <code>Set</code>. 
     	<BR> <B>effects:</B> If <code>item</code> is an element of 
	                     <code>this</code>, returns true.
			     Else returns false.
     */
    public abstract boolean contains(Object o);

    /** Returns the number of elements in the <code>Set</code>. */
    public abstract int size();

    /** Returns an <code>Enumeration</code> of the elements of the
     *  <code>Set</code>. */
    public abstract Enumeration elements();

    /** Returns a rather long string representation of the <code>Set</code>.
     *  @return a string representation of this <code>Set</code>. */
    public abstract String toString();
    
    
}
