// WorkSet.java, created Tue Feb 23 01:18:37 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.HashMap;
import java.util.Iterator;
/**
 * A <code>WorkSet</code> is a <code>Set</code> offering constant-time
 * access to the last element inserted, and an iterator whose speed
 * is not dependent on the total capacity of the underlying hashtable.
 * <p>Conforms to the JDK 1.2 Collections API.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: WorkSet.java,v 1.1.2.1 1999-02-23 07:12:08 cananian Exp $
 */
public class WorkSet extends java.util.AbstractSet {
    private /*final*/ HashMap hm;
    private EntryList el = new EntryList(null, null, null);// header
    
    /** Creates a new, empty <code>WorkSet</code> with a default capacity
     *  and load factor. */
    public WorkSet() {
	hm = new HashMap();
    }
    /** Constructs a new, empty <code>WorkSet</code> with the specified
     *  initial capacity and default load factor. */
    public WorkSet(int initialCapacity) {
	hm = new HashMap(initialCapacity);
    }
    /** Constructs a new, empty <code>WorkSet</code> with the specified
     *  initial capacity and the specified load factor. */
    public WorkSet(int initialCapacity, float loadFactor) {
	hm = new HashMap(initialCapacity, loadFactor);
    }
    /** Constructs a new <code>WorkSet</code> with the contents of the
     *  specified <code>Collection</code>. */
    public WorkSet(java.util.Collection c) {
	hm = new HashMap();
	for (Iterator i = c.iterator(); i.hasNext(); )
	    add(i.next());
    }

    /** Returns the last element added to the WorkSet, in constant-time. */
    public Object get() {
	return el.next.o;
    }

    public boolean add(Object o) {
	if (hm.containsKey(o)) return false;
	EntryList nel = new EntryList(el, o, el.next);
	el.next = nel.next.prev = nel;
	hm.put(o, nel);
	return true;
    }
    public void clear() {
	hm.clear(); el.next = null;
    }
    public boolean contains(Object o) {
	return hm.containsKey(o);
    }
    public boolean isEmpty() {
	return (el.next == null);
    }
    /** Efficient set iterator. */
    public Iterator iterator() {
	return new Iterator() {
	    EntryList elp = el;
	    public boolean hasNext() { return elp.next!=null; }
	    public Object next() {
		try {
		    Object o=elp.next.o; elp=elp.next; return o;
		} catch (NullPointerException e) {
		    throw new java.util.NoSuchElementException();
		}
	    }
	    public void remove() {
		if (elp==el) throw new IllegalStateException();
		hm.remove(elp.o);
		elp.prev.next = elp.next;
		elp.next.prev = elp.prev;
		elp = elp.prev;
	    }
	};
    }
    public boolean remove(Object o) {
	if (!hm.containsKey(o)) return false;
	EntryList elp = (EntryList) hm.get(o);
	hm.remove(o); // remove from hashmap
	elp.prev.next = elp.next; // remove from linked list.
	elp.next.prev = elp.prev;
	return true;
    }
    public int size() { return hm.size(); }

    // INNER CLASS. -------------------------------------------
    private final class EntryList {
	final Object o;
	EntryList prev, next;
	EntryList(EntryList prev, Object o, EntryList next) {
	    this.prev = prev; this.o = o; this.next = next;
	}
    }
}
