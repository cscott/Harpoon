// AggregateMapFactory.java, created Fri Nov 10 16:58:26 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Default;
import harpoon.Util.PairMapEntry;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
 * @version $Id: AggregateMapFactory.java,v 1.1.2.1 2001-11-04 02:38:22 cananian Exp $
 */
public class AggregateMapFactory extends MapFactory
    implements java.io.Serializable {
    private final Map m = new HashMap();

    /** Creates an <code>AggregateMapFactory</code>. */
    public AggregateMapFactory() { /* nothing to do here */ }

    /** Generates a new mutable <code>Map</code> which is a 
     *  subset of the backing set of this
     *  <code>AggregateMapFactory</code>.
     */
    public Map makeMap(final Map mm) {
	return new AggregateMap(mm);
    }
    class AggregateMap extends AbstractMap {
	final Object IDENTITY = new Object();
	/* backing store for efficient iteration */
	DoublyLinkedList entries=null;
	int size=0;

	AggregateMap(Map mm) { putAll(mm); }

	private void unlink(DoublyLinkedList entry) {
	    if (entries==entry) { // first element.
		entries = entry.next; // reset first element.
	    } else {
		entry.prev.next = entry.next;
		if (entry.next!=null) // maybe the last element.
		    entry.next.prev = entry.prev;
	    }
	    size--;
	}
	private void link(DoublyLinkedList entry) {
	    entry.next = entries;
	    if (entries!=null) // maybe nothing on list.
		entries.prev = entry;
	    entries = entry;
	    size++;
	}

	public Object put(Object key, Object value) {
	    DoublyLinkedList entry = new DoublyLinkedList(key, value);
	    DoublyLinkedList old = (DoublyLinkedList)
		m.put(Default.pair(IDENTITY, key), entry);
	    if (old!=null) unlink(old);
	    link(entry);
	    return (old==null) ? null : old.getValue();
	}
	public boolean containsKey(Object key) {
	    return m.containsKey(Default.pair(IDENTITY, key));
	}
	public boolean containsValue(Object value) {
	    for (DoublyLinkedList dll=entries; dll!=null; dll=dll.next)
		if (value==null ?
		    (value==dll.getValue()) :
		    value.equals(dll.getValue()))
		    return true;
	    return false;
	}
	public Set entrySet() {
	    return new AbstractMapSet() {
		    public Iterator iterator() {
			return new Iterator() {
				DoublyLinkedList dll=entries, last=null;
				public boolean hasNext() { return dll!=null; }
				public Object next() {
				    last = dll;
				    dll=dll.next;
				    return last;
				}
				public void remove() {
				    if (last==null)
					throw new
					    UnsupportedOperationException();
				    m.remove(Default.pair(IDENTITY,
							  last.getKey()));
				    unlink(last);
				    last=null;
				}
			    };
		    }
		    public int size() { return size; }
		    public boolean contains(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			Map.Entry me = (Map.Entry) o;
			List pair = Default.pair(IDENTITY, me.getKey());
			if (!m.containsKey(pair)) return false;
			return me.equals(m.get(pair));
		    }
		    public boolean remove(Object o) {
			if (!(o instanceof Map.Entry)) return false;
			Map.Entry me = (Map.Entry) o;
			List pair = Default.pair(IDENTITY, me.getKey());
			if (!m.containsKey(pair)) return false;
			return me.equals(m.remove(pair));
		    }
		    public Map asMap() { return AggregateMap.this; }
		};
	}
	public Object get(Object key) {
	    DoublyLinkedList entry = (DoublyLinkedList)
		m.get(Default.pair(IDENTITY, key));
	    return (entry==null)?null:entry.getValue();
	}
	public Object remove(Object key) {
	    DoublyLinkedList entry = (DoublyLinkedList)
		m.remove(Default.pair(IDENTITY, key));
	    if (entry!=null) unlink(entry);
	    return (entry==null)?null:entry.getValue();
	}
	public int size() { return size; }
	// garbage-collect entries in backing store, too!
	protected void finalize() {
	    clear();
	}
    }

    static class DoublyLinkedList extends PairMapEntry {
	DoublyLinkedList next, prev;
	DoublyLinkedList(Object key, Object value) {
	    super(key, value);
	}
    }
    static abstract class AbstractMapSet extends AbstractSet implements MapSet
    {}
}
