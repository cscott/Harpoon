// Tuple.java, created Sat Oct 10 01:37:39 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
/**
 * A <code>Tuple</code> is an ordered list of objects that works
 * properly in Hashtables & etc.  Tuples may have <code>null</code> elements.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Tuple.java,v 1.2.2.5 1999-06-18 01:48:10 cananian Exp $
 */
public class Tuple  {
    Object elements[];
    /** Creates a <code>Tuple</code>. */
    public Tuple(Object[] elements) {
        this.elements = elements;
    }
    /** Projects an element of the <code>Tuple</code>. */
    public Object proj(int i) { return elements[i]; }
    /** Returns an enumeration of the elements of the <code>Tuple</code>. */
    public Enumeration elements() { return new ArrayEnumerator(elements); }
    /** Returns an unmodifiable list view of the <code>tuple</code>. */
    public List asList() {
	return Collections.unmodifiableList(Arrays.asList(elements));
    }

    /** Synthesizes a hashcode from the hashcodes of the elements. */
    public int hashCode() {
	int hc = elements.length;
	for (int i=0; i<elements.length; i++)
	    if (elements[i]!=null)
		hc ^= elements[i].hashCode();
	return hc;
    }
    /** Does an element-by-element comparison of two <code>Tuple</code>s. */
    public boolean equals(Object obj) {
	Tuple t;
	if (this==obj) return true;
	if (null==obj) return false;
	try { t = (Tuple) obj; } catch (ClassCastException e) { return false; }
	if (this.elements.length != t.elements.length) return false;
	for (int i=0; i<elements.length; i++)
	    if (this.elements[i]==null) {
		if (t.elements[i]!=null) return false;
	    } else {
		if (!this.elements[i].equals(t.elements[i])) return false;
	    }
	return true;
    }
}
