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
 * @version $Id: WorkSet.java,v 1.1.2.3 1999-02-25 02:09:20 cananian Exp $
 */
public class WorkSet extends java.util.AbstractSet {
    private /*final*/ HashMap hm;
    private EntryList el = EntryList.init(); // header and footer nodes.
    private final boolean debug=false; // turn on consistency checks.
    
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
    public Object peek() {
	if (isEmpty()) throw new java.util.NoSuchElementException();
	return el.next.o;
    }
    /** Return and remove the last element added to the WorkSet. */
    public Object pop() {
	if (isEmpty()) throw new java.util.NoSuchElementException();
	Object o = el.next.o;
	hm.remove(o);
	el.next.remove();
	return o;
    }

    public boolean add(Object o) {
	if (o==null) throw new NullPointerException();
	if (hm.containsKey(o)) return false;
	EntryList nel = new EntryList(o);
	el.add(nel);
	hm.put(o, nel);
	// verify list/set correspondence.
	if (debug) Util.assert(EntryList.equals(el, hm.keySet()));
	return true;
    }
    public void clear() {
	hm.clear(); el = EntryList.init();
    }
    public boolean contains(Object o) {
	return hm.containsKey(o);
    }
    public boolean isEmpty() {
	return (el.next.next == null);
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
		(elp = elp.prev).next.remove();
		// verify list/set correspondence.
		if (debug) Util.assert(EntryList.equals(el, hm.keySet()));
	    }
	};
    }
    public boolean remove(Object o) {
	if (!hm.containsKey(o)) return false;
	EntryList elp = (EntryList) hm.get(o);
	// remove from hashmap
	hm.remove(o);
	// remove from linked list.
	elp.remove();
	// verify list/set correspondence.
	if (debug) Util.assert(EntryList.equals(el, hm.keySet()));
	return true;
    }
    public int size() { return hm.size(); }

    // INNER CLASS. -------------------------------------------
    private static final class EntryList {
	final Object o;
	EntryList prev=null, next=null;
	EntryList(Object o) { this.o = o; }

	public String toString() {
	    StringBuffer sb = new StringBuffer("[");
	    for (EntryList elp = this; elp!=null; elp=elp.next) {
		sb.append(elp.o);
		if (elp.next!=null)
		    sb.append(", ");
	    }
	    return sb.toString();
	}
	static boolean equals(EntryList el, java.util.Collection c) {
	    int size=0;
	    for (EntryList elp=el.next; elp.next!=null; elp=elp.next, size++)
		if (!c.contains(elp.o)) return false;
	    if (size!=c.size()) return false;
	    return true;
	}
	// utility.
	/** Remove this entry from the list. */
	void remove() {
	    // always a predecessor and successor.
	    this.prev.next = this.next;
	    this.next.prev = this.prev;
	    this.next = this.prev = null; // safety.
	}
	/** Link in the supplied entry after this one. */
	void add(EntryList nel) {
	    nel.next = this.next;
	    nel.prev = this;
	    this.next = nel.next.prev = nel;
	}
	// return a list with only a header and footer node.
	static EntryList init() {
	    EntryList header = new EntryList(null);
	    EntryList footer = new EntryList(null);
	    header.next = footer;
	    footer.prev = header;
	    return header;
	}
    }
}
