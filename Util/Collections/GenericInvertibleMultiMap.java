// GenericInvertibleMultiMap.java, created Sun Jun 17 16:19:35 2001 by cananian
// Copyright (C) 2001 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>GenericInvertibleMultiMap</code> is a default implementation of
 * <code>InvertibleMultiMap</code>.  It returns modifiable inverted
 * views of the mappings it maintains.  Note that a
 * <code>GenericInvertibleMultiMap</code> can directly replace a
 * <code>GenericInvertibleMap</code>, because <code>MultiMap</code>
 * correctly extends <code>Map</code>.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: GenericInvertibleMultiMap.java,v 1.1.2.2 2001-06-17 22:36:32 cananian Exp $
 */
public class GenericInvertibleMultiMap implements InvertibleMultiMap {
    private final MultiMap map, imap;
    private final InvertibleMultiMap inverse;
    private GenericInvertibleMultiMap(MultiMap map, MultiMap imap,
				      InvertibleMultiMap inverse) {
	this.map = map; this.imap = imap; this.inverse=inverse;
    }
    private GenericInvertibleMultiMap(MultiMap map, MultiMap imap) {
	this.map = map; this.imap = imap;
	this.inverse = new GenericInvertibleMultiMap(imap, map, this);
    }
    
    public GenericInvertibleMultiMap(MultiMap.Factory mmf) {
	this(mmf.makeMultiMap(), mmf.makeMultiMap());
    }
    public GenericInvertibleMultiMap(MapFactory mf, CollectionFactory cf) {
	this(new GenericMultiMap(mf,cf), new GenericMultiMap(mf,cf));
    }
    public GenericInvertibleMultiMap(CollectionFactory cf) {
	this(Factories.hashMapFactory, cf);
    }
    public GenericInvertibleMultiMap() {
	this(Factories.hashSetFactory);
    }
    /** Returns an unmodifiable inverted view of <code>this</code>.
     */
    public InvertibleMultiMap invert() { return inverse; }

    public boolean add(Object key, Object value) {
	imap.add(value, key);
	return map.add(key, value);
    }
    public boolean addAll(Object key, Collection values) {
	boolean changed = false;
	for (Iterator it=values.iterator(); it.hasNext(); )
	    if (this.add(key, it.next()))
		changed = true;
	return changed;
    }
    public void clear() { map.clear(); imap.clear(); }
    public boolean contains(Object a, Object b) {
	return map.contains(a, b);
    }
    public boolean containsKey(Object key) {
	return map.containsKey(key);
    }
    public boolean containsValue(Object value) {
	return imap.containsKey(value);
    }
    public Set entrySet() {
	// what should the 'value' field of an entry set contain?
	// a single value or a collection?
	throw new Error("unimplemented");
    }
    public boolean equals(Object o) {
	return map.equals(o);
    }
    public Object get(Object key) {
	return map.get(key);
    }
    public Collection getValues(final Object key) {
	return new AbstractCollection() {
	    public Iterator iterator() {
		final Iterator it=map.getValues(key).iterator();
		return new Iterator() {
		    Object last;
		    public boolean hasNext() { return it.hasNext(); }
		    public Object next() { last=it.next(); return last; }
		    public void remove() {
			imap.remove(last, key);
			it.remove();
		    }
		};
	    }
	    public boolean add(Object o) {
		return GenericInvertibleMultiMap.this.add(key, o);
	    }
	    public void clear() { map.remove(key); }
	    public boolean contains(Object o) {
		return GenericInvertibleMultiMap.this.contains(key, o);
	    }
	    public boolean remove(Object o) {
		return GenericInvertibleMultiMap.this.remove(key, o);
	    }
	    public int size() { return map.getValues(key).size(); }
	};
    }
    public int hashCode() {
	return map.hashCode();
    }
    public boolean isEmpty() {
	return map.isEmpty();
    }
    public Set keySet() {
	return new AbstractSet() {
	    public Iterator iterator() {
		final Iterator it = map.keySet().iterator();
		return new Iterator() {
		    Object last;
		    public boolean hasNext() { return it.hasNext(); }
		    public Object next() { last=it.next(); return last; }
		    public void remove() {
			// mirror op in imap.
			for (Iterator it2=map.getValues(last).iterator();
			     it2.hasNext(); )
			    imap.remove(it2.next(), last);
			// do it here.
			it.remove();
		    }
		};
	    }
	    public void clear() { GenericInvertibleMultiMap.this.clear(); }
	    public boolean contains(Object o) { return containsKey(o); }
	    public int size() { return GenericInvertibleMultiMap.this.size(); }
	    public boolean remove(Object o) {
		boolean changed = containsKey(o);
		GenericInvertibleMultiMap.this.remove(o);
		return changed;
	    }
	};
    }
    public Object put(Object key, Object value) {
	Object old = this.remove(key);
	this.add(key, value);
	return old;
    }
    public void putAll(Map t) {
	for (Iterator it=t.keySet().iterator(); it.hasNext(); ) {
	    Object key = it.next();
	    this.put(key, t.get(key));
	}
    }
    public Object remove(Object key) {
	Object old = null;
	for (Iterator it=this.getValues(key).iterator(); it.hasNext(); )
	    this.remove(key, old = it.next());
	return old;
    }
    public boolean remove(Object key, Object value) {
	imap.remove(value, key);
	return map.remove(key, value);
    }
    public boolean removeAll(Object key, Collection values) {
	boolean changed = false;
	for (Iterator it=values.iterator(); it.hasNext(); )
	    if (this.remove(key, it.next()))
		changed = true;
	return changed;
    }
    public boolean retainAll(Object key, Collection values) {
	boolean changed = false;
	for (Iterator it=this.getValues(key).iterator(); it.hasNext(); )
	    if (!values.contains(it.next())) {
		it.remove();
		changed = true;
	    }
	return changed;
    }
    public int size() { return map.size(); }
    public String toString() { return map.toString(); }
    // this is a little unexpected: only one copy of each value.
    public Collection values() { return inverse.keySet(); }
}
