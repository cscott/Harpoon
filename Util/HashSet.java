// HashSet.java, created Tue Sep 15 19:28:05 1998 by cananian
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
 * @version $Id: HashSet.java,v 1.1.2.2 1999-02-05 23:09:04 pnkfelix Exp $
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

    /** Determines if an object is a member of the <code>Set</code>. */
    public boolean contains(Object o) {
	return h.containsKey(o);
    }

    /** Returns the number of elements in the <code>Set</code>. */
    public int size() { 
	return h.size(); 
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

    /** Returns a arbitrary element from <code>this</code>.
	<BR> <B>effects:</B> Returns an element of <code>this</code>.
    */
    public Object getArbitrary() {
	return h.keys().nextElement();
    }

}


