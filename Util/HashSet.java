// Set.java, created Tue Sep 15 19:28:05 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>HashSet</code> is a set representation with constant-time
 * membership test, union element, and remove element operations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HashSet.java,v 1.1.2.1 1999-02-03 23:13:08 pnkfelix Exp $
 */

public class HashSet extends Set {
    protected Hashtable h;
    /** Creates an empty <code>Set</code>. */
    public HashSet() {
        h = new Hashtable();
    }
    /** Clear this set. */
    public void clear() {
	h.clear();
    }
    /** Remove a member from the <code>Set</code>. */
    public void remove(Object o) {
	h.remove(o);
    }
    /** Ensure that an object is a member of the <code>Set</code>. */
    public void union(Object o) {
	h.put(o, o);
    }
    /** Worklist interface: an alias for <code>union</code>. */
    public void push(Object o) {
	h.put(o, o);
    }
    /** Determines if an object is a member of the <code>Set</code>. */
    public boolean contains(Object o) {
	return h.containsKey(o);
    }
    /** Determines if there are any elements in the <code>Set</code>. */
    public boolean isEmpty() {
	return h.isEmpty();
    }
    /** Returns the number of elements in the <code>Set</code>. */
    public int size() { 
	return h.size(); 
    }
    /** Worklist interface: removes an arbitrary element from the
     *  <code>Set</code> and returns the removed element. */
    public Object pull() {
	Object o = h.keys().nextElement();
	h.remove(o);
	return o;
    }
    /** Copies the elements of the <code>Set</code> into an array. */
    public void copyInto(Object[] oa) {
	int i=0;
	for(Enumeration e = h.keys(); e.hasMoreElements(); )
	    oa[i++] = e.nextElement();
    }
    /** Returns an <code>Enumeration</code> of the elements of the
     *  <code>Set</code>. */
    public Enumeration elements() {
	return h.keys();
    }
    /** Returns a rather long string representation of the <code>Set</code>.
     *  @return a string representation of this <code>Set</code>. */
    public String toString() {
	StringBuffer sb = new StringBuffer("{");
	for (Enumeration e = elements(); e.hasMoreElements(); ) {
	    sb.append(e.nextElement().toString());
	    if (e.hasMoreElements())
		sb.append(", ");
	}
	sb.append("}");
	return sb.toString();
    }
}
