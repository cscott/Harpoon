// PersistentEnvironment.java, created Sat Aug 28 22:23:47 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
/**
 * <code>PersistentEnvironment</code> is an <code>Environment</code>
 * built on a <code>PersistentMap</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PersistentEnvironment.java,v 1.2.2.1 2002-04-09 21:38:18 cananian Exp $
 */
public class PersistentEnvironment<K,V> extends AbstractMap<K,V>
    implements Environment<K,V> {
    PersistentMap<K,V> m = new PersistentMap<K,V>();
    
    /** Creates a <code>PersistentEnvironment</code> with no mappings. */
    public PersistentEnvironment() { }
    /** Creates a <code>PersistentEnvironment</code> with the same
     *  mappings as the given <code>Map</code>. */
    public <K2 extends K, V2 extends V> PersistentEnvironment(Map<K2,V2> m) {
	putAll(m);
    }

    // ------------- MAP INTERFACE ---------------
    /** Remove all mappings from this map. */
    public void clear() { this.m = new PersistentMap<K,V>(); }
    /** Returns <code>true</code> is this map contains no key-value mappings.*/
    public boolean isEmpty() { return m.isEmpty(); }
    /** Returns the numer of key-value mappings in this map. */
    public int size() { return m.size(); }
    /** Returns the value to which this map maps the specified key. */
    public V get(Object key) { return m.get((K)key); }
    /** Associates the specified value with the specified key in this map. */
    public V put(K key, V value) {
	V prev = m.get(key);
	this.m = m.put(key, value);
	return prev;
    }
    /** Returns <code>true</code> if this map contains a mapping for the
     *  specified key. */
    public boolean containsKey(Object key) { return m.containsKey((K)key); }
    /** Removes the mapping for this key from this map if present. */
    public V remove(Object key) {
	K k = (K) key; // not safe, since we depend on a Comparator<K>
	V prev = m.get(k);
	this.m = m.remove(k);
	return prev;
    }

    // ------------- ENVIRONMENT INTERFACE ---------------
    /** A mark into an <code>PersistentEnvironment</code>. */
    private static class Mark<K,V> implements Environment.Mark {
	final PersistentMap<K,V> m;
	Mark(PersistentMap<K,V> m) { this.m = m; }
    }
    /** Get a mark that will allow you to restore the current state of
     *  this environment. */
    public Environment.Mark getMark() { return new Mark<K,V>(m); }
    /** Undo all changes since the supplied mark, restoring the map to
     *  its state at the time the mark was taken. */
    public void undoToMark(Environment.Mark m) { this.m = ((Mark)m).m; }

    // ------------- THE DREADED ENTRYSET ---------------
    /** Returns a set view of the mappings contained in this map.
     *  The returned set is immutable. */
    public Set<Map.Entry<K,V>> entrySet() {
	return Collections.unmodifiableSet(m.asMap().entrySet());
    }
}
