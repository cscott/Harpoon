// AggregateMapFactory.java, created Fri Nov 10 16:58:26 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Collections.PairMapEntry;
import harpoon.Util.Default;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * <code>AggregateMapFactory</code> uses a single <code>HashMap</code>
 * as backing store for the many smaller <code>Map</code>s created
 * by this <code>MapFactory</code>.  This means that we use much
 * less space and rehash less frequently than if we were using
 * the standard <code>Factories.hashMapFactory</code>.
 * The iterators of the submaps are fast, unlike those of
 * <code>HashMap</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AggregateMapFactory.java,v 1.3 2002-04-10 03:07:10 cananian Exp $
 */
public class AggregateMapFactory<K,V> extends MapFactory<K,V>
    implements java.io.Serializable {
    private static final class ID { }
    private final Map<Map.Entry<ID,K>,DoublyLinkedList> m =
	new HashMap<Map.Entry<ID,K>,DoublyLinkedList>();

    /** Creates an <code>AggregateMapFactory</code>. */
    public AggregateMapFactory() { /* nothing to do here */ }

    /** Generates a new mutable <code>Map</code> which is a 
     *  subset of the backing set of this
     *  <code>AggregateMapFactory</code>.
     */
    public <K2 extends K,V2 extends V> Map<K,V> makeMap(final Map<K2,V2> mm) {
	return new AggregateMap(mm);
    }
    class AggregateMap extends AbstractMap<K,V> {
	final ID IDENTITY = new ID();
	/* backing store for efficient iteration */
	DoublyLinkedList<K,V> entries=null;
	int size=0;

	<K2 extends K,V2 extends V> AggregateMap(Map<K2,V2> mm) { putAll(mm); }

	private void unlink(DoublyLinkedList<K,V> entry) {
	    if (entries==entry) { // first element.
		entries = entry.next; // reset first element.
	    } else {
		entry.prev.next = entry.next;
		if (entry.next!=null) // maybe the last element.
		    entry.next.prev = entry.prev;
	    }
	    size--;
	}
	private void link(DoublyLinkedList<K,V> entry) {
	    entry.next = entries;
	    if (entries!=null) // maybe nothing on list.
		entries.prev = entry;
	    entries = entry;
	    size++;
	}

	public V put(K key, V value) {
	    DoublyLinkedList<K,V> entry=new DoublyLinkedList<K,V>(key, value);
	    DoublyLinkedList<K,V> old=m.put(Default.entry(IDENTITY,key),entry);
	    if (old!=null) unlink(old);
	    link(entry);
	    return (old==null) ? null : old.getValue();
	}
	public boolean containsKey(Object key) {
	    return m.containsKey(Default.entry(IDENTITY, key));
	}
	public boolean containsValue(Object value) {
	    for (DoublyLinkedList<K,V> dll=entries; dll!=null; dll=dll.next)
		if (value==null ?
		    (value==dll.getValue()) :
		    value.equals(dll.getValue()))
		    return true;
	    return false;
	}
	public Set<Map.Entry<K,V>> entrySet() {
	    return new AbstractMapSet<K,V>() {
		    public Iterator<Map.Entry<K,V>> iterator() {
			return new Iterator<Map.Entry<K,V>>() {
				DoublyLinkedList<K,V> dll=entries, last=null;
				public boolean hasNext() { return dll!=null; }
				public Map.Entry<K,V> next() {
				    if (dll==null)
					throw new NoSuchElementException();
				    last = dll;
				    dll=dll.next;
				    return last;
				}
				public void remove() {
				    if (last==null)
					throw new
					    UnsupportedOperationException();
				    m.remove(Default.entry(IDENTITY,
							  last.getKey()));
				    unlink(last);
				    last=null;
				}
			    };
		    }
		    public int size() { return size; }
		    public boolean add(Map.Entry<K,V> me) {
			if (contains(me)) return false; // already here.
			if (AggregateMap.this.containsKey(me.getKey()))
			    // this is not a multimap!
			    throw new UnsupportedOperationException();
			AggregateMap.this.put(me.getKey(), me.getValue());
			return true;
		    }
		    public boolean contains(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			Map.Entry me = (Map.Entry) o;
			Map.Entry pair = Default.entry(IDENTITY, me.getKey());
			if (!m.containsKey(pair)) return false;
			return me.equals(m.get(pair));
		    }
		    public boolean remove(Object o) {
			if (!contains(o)) return false;
			Map.Entry me = (Map.Entry) o;
			AggregateMap.this.remove(me.getKey());
			return true;
		    }
		    public Map<K,V> asMap() { return AggregateMap.this; }
		};
	}
	public V get(Object key) {
	    DoublyLinkedList<K,V> entry = m.get(Default.entry(IDENTITY, key));
	    return (entry==null)?null:entry.getValue();
	}
	public V remove(Object key) {
	    DoublyLinkedList<K,V> entry = m.remove(Default.entry(IDENTITY, key));
	    if (entry!=null) unlink(entry);
	    return (entry==null)?null:entry.getValue();
	}
	public int size() { return size; }
	// garbage-collect entries in backing store, too!
	protected void finalize() {
	    clear();
	}
    }

    static class DoublyLinkedList<K,V> extends PairMapEntry<K,V> {
	DoublyLinkedList<K,V> next, prev;
	DoublyLinkedList(K key, V value) {
	    super(key, value);
	}
    }
    static abstract class AbstractMapSet<K,V>
	extends AbstractSet<Map.Entry<K,V>> implements MapSet<K,V> { }
}
