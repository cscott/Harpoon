// Default.java, created Thu Apr  8 02:22:56 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import harpoon.Util.Collections.AbstractMapEntry;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;

/**
 * <code>Default</code> contains one-off or 'standard, no-frills'
 * implementations of simple <code>Iterator</code>s,
 * <code>Enumeration</code>s, and <code>Comparator</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Default.java,v 1.2 2002-02-25 21:08:45 cananian Exp $
 */
public abstract class Default  {
    /** A <code>Comparator</code> for objects that implement 
     *   <code>Comparable</code>. */
    public static final Comparator comparator = new SerializableComparator() {
	public int compare(Object o1, Object o2) {
	    if (o1==null && o2==null) return 0;
	    // 'null' is less than everything.
	    if (o1==null) return -1;
	    if (o2==null) return 1;
	    // hack: in JDK1.1 String is not Comparable
	    if (o1 instanceof String && o2 instanceof String)
	       return ((String)o1).compareTo((String)o2);
	    // hack: in JDK1.1 Integer is not Comparable
	    if (o1 instanceof Integer && o2 instanceof Integer)
	       return ((Integer)o1).intValue() - ((Integer)o2).intValue();
	    return (o1==null) ? -((Comparable)o2).compareTo(o1):
	                         ((Comparable)o1).compareTo(o2);
	}
	// this should always be a singleton.
	private Object readResolve() { return Default.comparator; }
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
    /** An empty map. Missing from <code>java.util.Collections</code>.*/
    public static final SortedMap EMPTY_MAP = new SerializableSortedMap() {
	public void clear() { }
	public boolean containsKey(Object key) { return false; }
	public boolean containsValue(Object value) { return false; }
	public Set entrySet() { return Collections.EMPTY_SET; }
	public boolean equals(Object o) {
	    if (!(o instanceof Map)) return false;
	    return ((Map)o).size()==0;
	}
	public Object get(Object key) { return null; }
	public int hashCode() { return 0; }
	public boolean isEmpty() { return true; }
	public Set keySet() { return Collections.EMPTY_SET; }
	public Object put(Object key, Object value) {
	    throw new UnsupportedOperationException();
	}
	public void putAll(Map t) {
	    if (t.size()==0) return;
	    throw new UnsupportedOperationException();
	}
	public Object remove(Object key) { return null; }
	public int size() { return 0; }
	public Collection values() { return Collections.EMPTY_SET; }
	public String toString() { return "{}"; }
	// this should always be a singleton.
	private Object readResolve() { return Default.EMPTY_MAP; }
	// SortedMap interface.
	public Comparator comparator() { return null; }
	public Object firstKey() { throw new NoSuchElementException(); }
	public Object lastKey() { throw new NoSuchElementException(); }
	public SortedMap headMap(Object toKey) { return Default.EMPTY_MAP; }
	public SortedMap tailMap(Object fromKey) { return Default.EMPTY_MAP; }
	public SortedMap subMap(Object fromKey, Object toKey) {
	    return Default.EMPTY_MAP;
	}
    };
    /** A pair constructor method.  Pairs implement <code>hashCode()</code>
     *  and <code>equals()</code> "properly" so they can be used as keys
     *  in hashtables and etc.  They are implemented as mutable lists of
     *  fixed size 2. */
    public static List pair(final Object left, final Object right) {
	// this can't be an anonymous class because we want to make it
	// serializable.
	return new PairList(left, right);
    }
    private static class PairList extends AbstractList
	implements java.io.Serializable {
	private Object left, right;
	PairList(Object left, Object right) {
	    this.left = left; this.right = right;
	}
	public int size() { return 2; }
	public Object get(int index) {
	    switch(index) {
	    case 0: return this.left;
	    case 1: return this.right;
	    default: throw new IndexOutOfBoundsException();
	    }
	}
	public Object set(int index, Object element) {
	    Object prev;
	    switch(index) {
	    case 0: prev=this.left; this.left=element; return prev;
	    case 1: prev=this.right; this.right=element; return prev;
	    default: throw new IndexOutOfBoundsException();
	    }
	}
    }
    /** A pair constructor method more appropriate for <code>Set</code>
     *  views of <code>Map</code>s and <code>MultiMap</code>s.
     *  The returned object is an instance of <code>Map.Entry</code>;
     *  the only (real) difference from the pairs returned by
     *  <code>Default.pair()</code> is the definition of
     *  <code>hashCode()</code>, which corresponds to <code>Map.Entry</code>
     *  (being <code>key.hashCode() ^ value.hashCode()</code> ) rather
     *  than <code>List</code> (which would be 
     *  <code>31*(31+key.hashCode())+value.hashCode()</code> ). This is
     *  an annoying distinction; I wish the JDK API authors had made
     *  these consistent. The <code>Map.Entry</code> returned is immuatable.
     */
    public static Map.Entry entry(final Object key, final Object value) {
	return new AbstractMapEntry() {
		public Object getKey() { return key; }
		public Object getValue() { return value; }
	    };
    }

    /** A serializable comparator. */
    private static interface SerializableComparator
	extends Comparator, java.io.Serializable { /* only declare */ }
    /** A serializable map. */
    private static interface SerializableSortedMap
	extends SortedMap, java.io.Serializable { /* only declare */ }
}
