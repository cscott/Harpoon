// Tuple.java, created Sat Oct 10 01:37:39 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import harpoon.ClassFile.*;

import java.util.Enumeration;

/**
 * A <code>Tuple</code> is an ordered list of objects that works
 * properly in Hashtables & etc.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Tuple.java,v 1.2.2.1 1998-12-09 21:44:30 cananian Exp $
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

    /** Synthesizes a hashcode from the hashcodes of the elements. */
    public int hashCode() {
	int hc = elements.length;
	for (int i=0; i<elements.length; i++)
	    hc ^= elements[i].hashCode();
	return hc;
    }
    /** Does an element-by-element comparison of two <code>Tuple</code>s. */
    public boolean equals(Object obj) {
	if (!(obj instanceof Tuple)) return false;
	Tuple t = (Tuple) obj;
	if (this.elements.length != t.elements.length) return false;
	for (int i=0; i<elements.length; i++)
	    if (!this.elements[i].equals(t.elements[i]))
		return false;
	return true;
    }
}
