// Tuple.java, created Sat Oct 10 01:37:39 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.cscott.jutil.Default;
/**
 * A <code>Tuple</code> is an ordered list of objects that works
 * properly in Hashtables & etc.  Tuples may have <code>null</code> elements.
 * <code>Tuple</code>s are <code>Comparable</code> iff the objects in
 * the elements array are <code>Comparable</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Tuple.java,v 1.4 2004-02-08 01:56:15 cananian Exp $
 */
public class Tuple implements Comparable, java.io.Serializable {
    final Comparator objcmp;
    final Object elements[];
    /** Creates a <code>Tuple</code>. */
    public Tuple(Object[] elements) {
        this.elements = elements;
	this.objcmp = Default.comparator;
    }
    /** Creates a <code>Comparable</code> <code>Tuple</code> which will use
     *  the specified <code>Comparator</code> to do object comparisons. */
    public Tuple(Object[] elements, Comparator objcmp) {
	this.elements = elements;
	this.objcmp = objcmp;
    }
    /** Projects an element of the <code>Tuple</code>. */
    public Object proj(int i) { return elements[i]; }
    /** Returns an unmodifiable list view of the <code>Tuple</code>. */
    public List asList() {
	return Collections.unmodifiableList(Arrays.asList(elements));
    }
    /** Returns a human-readable description of this <code>Tuple</code>. */
    public String toString() { return Util.print(asList()); }
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
    /** Does an element-by-element comparison of two <code>Tuple</code>s.
     *  Inconsistent with <code>equals()</code> only if the underlying
     *  comparator is.  Shorter tuples are compared as less than longer
     *  tuples. */
    public int compareTo(Object o) { // dict order: smaller first.
	Object[] el2 = ((Tuple)o).elements;
	if (elements.length!=el2.length) return elements.length-el2.length;
	for (int i=0; i<elements.length; i++) {
	    int c = objcmp.compare(elements[i], el2[i]);
	    if (c!=0) return c;
	}
	return 0;
    }
}
