// AggregateSetFactory.java, created Fri Nov 10 16:58:26 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Default;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
/**
 * <code>AggregateSetFactory</code> uses a single <code>HashMap</code>
 * as backing store for the many smaller <code>Set</code>s created
 * by this <code>SetFactory</code>.  This means that we use much
 * less space and rehash less frequently than if we were using
 * the standard <code>Factories.hashSetFactory</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AggregateSetFactory.java,v 1.4 2003-05-06 15:30:54 cananian Exp $
 */
public class AggregateSetFactory<V> extends SetFactory<V>
    implements java.io.Serializable {
    private final Map<Map.Entry<EntryList<V>,V>,EntryList<V>> m =
	new HashMap<Map.Entry<EntryList<V>,V>,EntryList<V>>();

    /** Creates an <code>AggregateSetFactory</code>. */
    public AggregateSetFactory() { /* nothing to do here */ }

    /** Generates a new mutable <code>Set</code> which is a 
     *  subset of the backing set of this
     *  <code>AggregateSetFactory</code>.
     *  <b>WARNING:</b>
     *  The <code>remove()</code> method of the returned <code>Set</code>
     *  is very slow.
     */
    public <T extends V> Set<V> makeSet(Collection<T> c) {
	return new AggregateSet(c);
    }
    private class AggregateSet extends AbstractSet<V> {
	// list header is the IDENTITY object for the set.
	// this is also the backing store for efficient iteration.
	final EntryList<V> IDENTITY = new EntryList<V>(null);
	int size=0;

	<T extends V> AggregateSet(Collection<T> c) {
	    addAll(c);
	}
	public boolean add(V o) {
	    Map.Entry<EntryList<V>,V> key = Default.entry(IDENTITY, o);
	    if (m.containsKey(key)) return false;
	    EntryList<V> entry = new EntryList<V>(o);
	    IDENTITY.add(entry);
	    m.put(key, entry);
	    size++;
	    return true;
	}
	public boolean contains(Object o) {
	    return m.containsKey(Default.entry(IDENTITY, o));
	}
	public boolean isEmpty() {
	    assert (size==0)==(IDENTITY.next==null);
	    return size==0;
	}
	public Iterator<V> iterator() {
	    return new Iterator<V>() {
		EntryList<V> entry = IDENTITY;
		public boolean hasNext() { return entry.next!=null; }
		public V next() {
		    if (!hasNext()) throw new NoSuchElementException();
		    removeValid = true;
		    entry = entry.next;
		    return entry.value;
		}
		public void remove() {
		    if (!removeValid) throw new IllegalStateException();
		    EntryList<V> oentry = entry;
		    entry = entry.prev;
		    AggregateSet.this.remove(oentry);
		    removeValid = false;
		}
		private boolean removeValid = false;
	    };
	}
	private void remove(EntryList<V> entry) {
	    EntryList<V> e = m.remove(Default.entry(IDENTITY, entry.value));
	    assert e == entry;
	    entry.remove();
	    size--;
	}
	public boolean remove(Object o) {
	    EntryList<V> entry = m.get(Default.entry(IDENTITY, o));
	    if (entry==null) return false;
	    remove(entry);
	    return true;
	}
	public int size() { return size; }
	// garbage-collect entries in backing store, too!
	protected void finalize() {
	    clear();
	}
    }
    /** Linked list of set elements. */
    private static final class EntryList<E> implements java.io.Serializable {
	final E value;
	EntryList<E> prev=null, next=null;
	EntryList(E value) { this.value = value; }

	// utility.
	/** Remove this entry from the list. */
	void remove() {
	    // always a predecessor; maybe no successor.
	    this.prev.next = this.next;
	    if (this.next!=null) this.next.prev = this.prev;
	    this.next = this.prev = null; // safety.
	}
	/** Link in the supplied entry after this one. */
	void add(EntryList<E> nel) {
	    nel.next = this.next;
	    nel.prev = this;
	    this.next = nel.next.prev = nel;
	}
    }
}
