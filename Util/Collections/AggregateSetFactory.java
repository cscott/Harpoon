// AggregateSetFactory.java, created Fri Nov 10 16:58:26 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Default;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
/**
 * <code>AggregateSetFactory</code> uses a single <code>HashSet</code>
 * as backing store for the many smaller <code>Set</code>s created
 * by this <code>SetFactory</code>.  This means that we use much
 * less space and rehash less frequently than if we were using
 * the standard <code>Factories.hashSetFactory</code>.
 * Be aware that the <code>remove()</code> method of the subsets is slow,
 * but the iterator is as fast as <code>ArrayList</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AggregateSetFactory.java,v 1.3 2002-04-10 03:07:10 cananian Exp $
 */
public class AggregateSetFactory<V> extends SetFactory<V>
    implements java.io.Serializable {
    private static final class ID { }
    private final Set<Map.Entry<ID,V>> s = new HashSet<Map.Entry<ID,V>>();

    /** Creates an <code>AggregateSetFactory</code>. */
    public AggregateSetFactory() { /* nothing to do here */ }

    /** Generates a new mutable <code>Set</code> which is a 
     *  subset of the backing set of this
     *  <code>AggregateSetFactory</code>.
     *  <b>WARNING:</b>
     *  The <code>remove()</code> method of the returned <code>Set</code>
     *  is very slow.
     */
    public <T extends V> Set<V> makeSet(final Collection<T> c) {
	return new AbstractSet<V>() {
	    final ID IDENTITY;
	    /* backing store for efficient iteration */
	    final List<V> elements;

	    /* constructor */ { 
	    IDENTITY = new ID();
	    elements = new ArrayList<V>(c.size());
	    addAll(c); 
	    }
	    public boolean add(V o) {
		boolean r = s.add(Default.entry(IDENTITY, o));
		if (r) elements.add(o);
		return r;
	    }
	    public boolean contains(Object o) {
		return s.contains(Default.entry(IDENTITY, o));
	    }
	    public boolean isEmpty() {
		return elements.isEmpty();
	    }
	    public Iterator<V> iterator() {
		return new Iterator<V>() {
		    private int i=0;
		    public boolean hasNext() { return i < elements.size(); }
		    public V next() {
			if (!hasNext()) throw new NoSuchElementException();
			removeValid = true;
			return elements.get(i++);
		    }
		    public void remove() {
			if (!removeValid) throw new IllegalStateException();
			Object o = elements.remove(--i);
			s.remove(Default.entry(IDENTITY, o));
			removeValid = false;
		    }
		    private boolean removeValid = false;
		};
	    }
	    // XXX: remove is slow!
	    // making it fast would require replacing the ArrayList with
	    // our own custom linked-list impl, a la Util.WorkSet does.
	    //public boolean remove(Object o);
	    public int size() { return elements.size(); }
	    // garbage-collect entries in backing store, too!
	    protected void finalize() {
		clear();
	    }
	};
    }
}
