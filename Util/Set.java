// Set.java, created Tue Sep 15 19:28:05 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>Set</code> is an abstract set representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Set.java,v 1.3.2.4 1999-02-03 23:13:09 pnkfelix Exp $
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

    /** Ensure that an object is a member of the <code>Set</code>. 
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> Adds <code>o</code> to <code>this</code>,
	                     if it is present.  Else does nothing. 
     */
    public abstract void union(Object o);

    /** Worklist interface: an alias for <code>union</code>. */
    public abstract void push(Object o);
    
    /** Determines if an object is a member of the <code>Set</code>. */
    public abstract boolean contains(Object o);

    /** Determines if there are any elements in the <code>Set</code>. */
    public abstract boolean isEmpty();
    
    /** Returns the number of elements in the <code>Set</code>. */
    public abstract int size();

    /** Worklist interface: removes an arbitrary element from the
     *  <code>Set</code> and returns the removed element. */
    public abstract Object pull();
    
    /** Copies the elements of the <code>Set</code> into an array. */
    public abstract void copyInto(Object[] oa);

    /** Returns an <code>Enumeration</code> of the elements of the
     *  <code>Set</code>. */
    public abstract Enumeration elements();

    /** Returns a rather long string representation of the <code>Set</code>.
     *  @return a string representation of this <code>Set</code>. */
    public abstract String toString();
}
