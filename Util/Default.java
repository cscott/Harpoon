// Default.java, created Thu Apr  8 02:22:56 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * <code>Default</code> contains one-off or 'standard, no-frills'
 * implementations of simple <code>Iterator</code>s,
 * <code>Enumeration</code>s, and <code>Comparator</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Default.java,v 1.1.2.4 1999-08-05 02:12:56 cananian Exp $
 */
public abstract class Default  {
    /** A <code>Comparator</code> for objects that implement 
     *   <code>Comparable</code>. */
    public static final Comparator comparator = new Comparator() {
	public int compare(Object o1, Object o2) {
	    return ((Comparable)o1).compareTo(o2);
	}
    };
    /** An <code>Enumerator</code> over the empty set.
     * @deprecated Use nullIterator. */
    public static final Enumeration nullEnumerator = new Enumeration() {
	public boolean hasMoreElements() { return false; }
	public Object nextElement() { throw new NoSuchElementException(); }
    };
    /** An <code>Iterator</code> over the empty set. */
    public static final Iterator nullIterator = new UnmodifiableIterator() {
	public boolean hasNext() { return false; }
	public Object next() { throw new NoSuchElementException(); }
    };
    /** An <code>Iterator</code> over a singleton set. */
    public static final Iterator singletonIterator(Object o) {
	return Collections.singleton(o).iterator();
    } 
    /** An unmodifiable version of the given iterator. */
    public static final Iterator unmodifiableIterator(final Iterator i) {
	return new UnmodifiableIterator() {
	    public boolean hasNext() { return i.hasNext(); }
	    public Object next() { return i.next(); }
	};
    }
}
