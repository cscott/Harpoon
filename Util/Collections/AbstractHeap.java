// AbstractHeap.java, created Sat Feb 12 09:41:17 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
/**
 * <code>AbstractHeap</code> provides a skeletal implementation of
 * the <code>Heap</code> interface, to minimize the effort required
 * to implement this interface.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AbstractHeap.java,v 1.1.2.2 2000-02-12 18:11:13 cananian Exp $
 */
public abstract class AbstractHeap implements Heap {
    /** A comparator for <code>Map.Entry</code>s, based on the
     *  key comparator given to the constructor. */
    private final EntryComparator entryComparator;
    /** Sole constructor, for invocation by subclass constructors. */
    protected AbstractHeap(Comparator c) {
	this.entryComparator = new EntryComparator(c);
    }

    // abstract methods:
    public abstract Map.Entry insert(Object key, Object value);
    public abstract Map.Entry minimum();
    public abstract void decreaseKey(Map.Entry me, Object newkey);
    public abstract void delete(Map.Entry me);
    public abstract int size();
    public abstract Collection entries();
    public abstract void clear();

    // methods which we helpfully provide for you:
    public Map.Entry extractMinimum() {
	Map.Entry e = minimum();
	delete(e);
	return e;
    }
    public void union(Heap h) {
	for (Iterator it=h.entries().iterator(); it.hasNext(); ) {
	    Map.Entry e = (Map.Entry) it.next();
	    insert(e.getKey(), e.getValue());
	}
	h.clear();
    }
    public boolean isEmpty() { return size()==0; }
    public int hashCode() { return 1+entries().hashCode(); }
    public boolean equals(Object o) {
	if (o instanceof Heap) return entries().equals(((Heap)o).entries());
	return false;
    }
    public String toString() { return entries().toString(); }
    /** Returns the comparator used to compare keys in this <code>Heap</code>,
     *  or <code>null</code> if the keys' natural ordering is used. */
    public Comparator comparator() { return entryComparator.cc; }
    /** Returns a comparator which can be used to compare
     *  <code>Map.Entry</code>s. Will never return <code>null</code>. */
    protected Comparator entryComparator() { return entryComparator; }

    /** Compares <code>Map.Entry</code>s by key. */
    private static class EntryComparator implements Comparator {
	final Comparator cc;
	EntryComparator(Comparator cc) { this.cc = cc; }
	public int compare(Object o1, Object o2) {
	    Map.Entry e1 = (Map.Entry) o1, e2 = (Map.Entry) o2;
	    Object k1 = e1.getKey(), k2 = e2.getKey();
	    return (cc==null) ?
		((Comparable)k1).compareTo(k2) :
		cc.compare(k1, k2);
	}
	public boolean equals(Object obj) {
	    if (obj instanceof EntryComparator) {
		EntryComparator ec = (EntryComparator) obj;
		return (cc==null) ? (ec.cc==null) : cc.equals(ec.cc);
	    }
	    return false;
	}
    }
}
