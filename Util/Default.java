// Default.java, created Thu Apr  8 02:22:56 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import harpoon.Util.Collections.AbstractMapEntry;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.MultiMapSet;

import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
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
import java.util.SortedSet;

/**
 * <code>Default</code> contains one-off or 'standard, no-frills'
 * implementations of simple <code>Iterator</code>s,
 * <code>Collection</code>s, and <code>Comparator</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Default.java,v 1.2.2.4 2002-04-07 21:12:51 cananian Exp $
 */
public abstract class Default  {
    /** A <code>Comparator</code> for objects that implement 
     *   <code>Comparable</code>. */
    public static final Comparator<Comparable> comparator = new SerializableComparator<Comparable>() {
	public int compare(Comparable o1, Comparable o2) {
	    if (o1==null && o2==null) return 0;
	    // 'null' is less than everything.
	    if (o1==null) return -1;
	    if (o2==null) return 1;
	    return (o1==null) ? -o2.compareTo(o1):
	                         o1.compareTo(o2);
	}
	// this should always be a singleton.
	private Object readResolve() { return Default.comparator; }
    };
    /** An <code>Iterator</code> over the empty set. */
    public static final Iterator nullIterator = nullIterator();
    public static final <E> Iterator<E> nullIterator() {
	return new UnmodifiableIterator<E>() {
	    public boolean hasNext() { return false; }
	    public E next() { throw new NoSuchElementException(); }
	};
    }
    /** An <code>Iterator</code> over a singleton set. */
    public static final <E> Iterator<E> singletonIterator(E o) {
	return Collections.singletonList(o).iterator();
    } 
    /** An unmodifiable version of the given iterator. */
    public static final <E> Iterator<E> unmodifiableIterator(final Iterator<E> i) {
	return new UnmodifiableIterator<E>() {
	    public boolean hasNext() { return i.hasNext(); }
	    public E next() { return i.next(); }
	};
    }
    /** An empty set; the parameterized version.
     *  Made necessary by limitations in GJ's type system. */
    public static final <E> SortedSet<E> EMPTY_SET() {
	return new SerializableSortedSet<E>() {
	    public int size() { return 0; }
	    public boolean isEmpty() { return true; }
	    public boolean contains(Object e) { return false; }
	    public Iterator<E> iterator() { return nullIterator(); }
	    public Object[] toArray() { return new Object[0]; }
	    public <T> T[] toArray(T[] a) {
		if (a.length>0) a[0]=null;
		return a;
	    }
	    public boolean add(E e) {
		throw new UnsupportedOperationException();
	    }
	    public boolean remove(Object e) { return false; }
	    public <T> boolean containsAll(Collection<T> c) {
		return c.isEmpty();
	    }
	    public <T extends E> boolean addAll(Collection<T> c) {
		if (c.isEmpty()) return false;
		throw new UnsupportedOperationException();
	    }
	    public <T> boolean removeAll(Collection<T> c) { return false; }
	    public <T> boolean retainAll(Collection<T> c) { return false; }
	    public void clear() { }
	    public boolean equals(Object o) {
		// note we implement Set, not Collection, interface
		if (!(o instanceof Set)) return false;
		return ((Set)o).size()==0;
	    }
	    public int hashCode() { return 0; }
	    // sorted set interface:
	    public Comparator<E> comparator() { return null; }
	    public SortedSet<E> subSet(E fromEl, E toEl) { return this; }
	    public SortedSet<E> headSet(E toEl) { return this; }
	    public SortedSet<E> tailSet(E fromEl) { return this; }
	    public E first() { throw new NoSuchElementException(); }
	    public E last() { throw new NoSuchElementException(); }
	};
    }
    /** An empty list.  The parameterized version.
     *  Made necessary by limitations in GJ's type system. */
    public static final <E> List<E> EMPTY_LIST() {
	return new SerializableAbstractList<E>() {
	    public int size() { return 0; }
	    public E get(int index) {
		throw new IndexOutOfBoundsException();
	    }
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
    /** An empty multi-map. */
    public static final MultiMap EMPTY_MULTIMAP = new SerializableMultiMap() {
	private MultiMap _this_ = this;
	public void clear() { }
	public boolean containsKey(Object key) { return false; }
	public boolean containsValue(Object value) { return false; }
	public MultiMapSet entrySet() {
	    return new AbstractMultiMapSet() {
		    public Iterator iterator() { return nullIterator; }
		    public int size() { return 0; }
		    public MultiMap asMap() { return _this_; }
		    public MultiMap asMultiMap() { return _this_; }
	    };
	}
	abstract class AbstractMultiMapSet
	    extends AbstractSet implements MultiMapSet { }
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
	private Object readResolve() { return Default.EMPTY_MULTIMAP; }
	// MultiMap interface.
	public boolean remove(Object key, Object value) {
	    return false;
	}
	public boolean add(Object key, Object value) {
	    throw new UnsupportedOperationException();
	}
	public boolean addAll(Object key, Collection values) {
	    if (values.size()==0) return false;
	    throw new UnsupportedOperationException();
	}
	public boolean addAll(MultiMap mm) {
	    if (mm.size()==0) return false;
	    throw new UnsupportedOperationException();
	}
	public boolean retainAll(Object key, Collection values) {
	    return false;
	}
	public boolean removeAll(Object key, Collection values) {
	    return false;
	}
	public Collection getValues(Object key) {
	    return Collections.EMPTY_SET;
	}
	public boolean contains(Object key, Object value) {
	    return false;
	}
    };
    /**
     * Improved <code>unmodifiableCollection()</code> class that
     * helps w/ covariant subtyping. */
    public static <A,B extends A> Collection<A> unmodifiableCollection(final Collection<B> cc,
								       Collection<A> _ignore_ // XXX BUG IN JAVAC: this parameter should not be necessary.
								       ) {
	return new AbstractCollection<A>() {
	    public <T> boolean containsAll(Collection<T> c) {
		return cc.containsAll(c);
	    }
	    public <T> boolean removeAll(Collection<T> c) {
		return cc.removeAll(c);
	    }
	    public <T> boolean retainAll(Collection<T> c) {
		return cc.retainAll(c);
	    }
	    public boolean contains(Object o) { return cc.contains(o); }
	    public boolean isEmpty() { return cc.isEmpty(); }
	    public Iterator<A> iterator() {
		final Iterator<B> it = cc.iterator();
		return new UnmodifiableIterator<A>() {
		    public boolean hasNext() { return it.hasNext(); }
		    public A next() { return it.next(); }
		};
	    }
	    public int size() { return cc.size(); }
	};
    }
    /** A pair constructor method.  Pairs implement <code>hashCode()</code>
     *  and <code>equals()</code> "properly" so they can be used as keys
     *  in hashtables and etc.  They are implemented as mutable lists of
     *  fixed size 2. */
    public static <A,B> PairList<A,B> pair(final A left, final B right) {
	// this can't be an anonymous class because we want to make it
	// serializable.
	return new PairList<A,B>(left, right);
    }
    private static class PairList<A,B> extends AbstractList
	implements java.io.Serializable {
	private A left;
	private B right;
	PairList(A left, B right) {
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
	    case 0: prev=this.left; this.left=(A)element; return prev;
	    case 1: prev=this.right; this.right=(B)element; return prev;
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
     *  these consistent. The <code>Map.Entry</code> returned is immutable.
     */
    public static <K,V> Map.Entry<K,V> entry(final K key, final V value) {
	return new AbstractMapEntry<K,V>() {
		public K getKey() { return key; }
		public V getValue() { return value; }
	    };
    }

    /** A serializable comparator. */
    private static interface SerializableComparator<A>
	extends Comparator<A>, java.io.Serializable { /* only declare */ }
    /** A serializable abstract list. */
    private static abstract class SerializableAbstractList<E>
	extends AbstractList<E>
	implements java.io.Serializable { /* only declare */ }
    /** A serializable set. */
    private static interface SerializableSortedSet<E>
	extends SortedSet<E>, java.io.Serializable { /* only declare */ }
    /** A serializable map. */
    private static interface SerializableSortedMap<K,V>
	extends SortedMap<K,V>, java.io.Serializable { /* only declare */ }
    /** A serializable multi-map. */
    private static interface SerializableMultiMap<K,V>
	extends MultiMap<K,V>, java.io.Serializable { /* only declare */ }
}
