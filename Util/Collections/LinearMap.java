// LinearMap.java, created Wed Aug  4 11:59:14 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.AbstractMap;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;

/**
 * <code>LinearMap</code> is a simplistic light-weight
 * <code>Map</code> designed for use when the number of entries is
 * small.  It is backed by a <code>LinearSet</code>.
 *
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LinearMap.java,v 1.2.2.1 2002-04-09 21:38:18 cananian Exp $
 */
public class LinearMap<K,V> extends AbstractMap<K,V> {
    private LinearSet<Map.Entry<K,V>> set;

    /** Creates a <code>LinearMap</code>. */
    public LinearMap() {
        set = new LinearSet<Map.Entry<K,V>>();
    }

    public LinearMap(Map map) {
	set = new LinearSet<Map.Entry<K,V>>();
	putAll(map);
    }

    /** Creates a <code>LinearMap</code> with specified capacity. */
    public LinearMap(int capacity) {
        set = new LinearSet<Map.Entry<K,V>>(capacity);
    }

    public Set<Map.Entry<K,V>> entrySet() {
	return set;
    }

    public V put(K key, V value) {
	Iterator<Map.Entry<K,V>> entries = set.iterator();
	V oldValue = null;
	while(entries.hasNext()) {
	    Map.Entry<K,V> entry = entries.next();
	    if (keysMatch(key, entry.getKey())) {
		oldValue = entry.getValue();
		entry.setValue(value);
		return oldValue;
	    }
	}
	set.add(new PairMapEntry(key, value));
	return oldValue;
    }

    private boolean keysMatch(Object k1, K k2) {
	return ((k1 == null && k2 == null) ||
		(k1 != null && k2 != null && 
		 k1.equals(k2)));
    }

    public V remove(Object key) {
	Iterator<Map.Entry<K,V>> entries = set.iterator();
	V oldValue = null;
	while(entries.hasNext()) {
	    Map.Entry<K,V> entry = entries.next();
	    if (keysMatch(key, entry.getKey())) {
		oldValue = entry.getValue();
		set.remove(entry);
		break;
	    }
	}
	return oldValue;
    }
} 
