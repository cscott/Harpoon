// Set.java, created Tue Sep 15 19:28:05 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Enumeration;
/**
 * <code>Set</code> is an abstract set representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Set.java,v 1.3.2.6 1999-06-17 21:00:53 cananian Exp $
 * @deprecated Use <code>java.util.Set</code> instead.
 */

public abstract class Set implements Worklist {
    
    /** Creates an empty <code>Set</code>. */
    public Set() {    }

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

    /** Determines if there are any elements in the <code>Set</code>. */
    public boolean isEmpty() {
	if (size() == 0) {
	    return true;
	} else {
	    return false;
	}
    }

    /** Worklist interface: removes an arbitrary element from the
     *  <code>Set</code> and returns the removed element. */
    public Object pull() {
	Object o = this.getArbitrary();
	this.remove(o);
	return o;
    }
    
    /** Copies the elements of the <code>Set</code> into an array. 
	<BR> <B>requires:</B> <code>oa</code> is large enough to hold
	                      all the elements of <code>this</code>.
     */
    public void copyInto(Object[] oa) {
	int i=0;
	for(Enumeration e = elements(); e.hasMoreElements(); )
	    oa[i++] = e.nextElement();
    }
    
    /** Worklist interface: an alias for <code>union</code>. */
    public void push(Object o) {
	this.union(o);
    }

}


