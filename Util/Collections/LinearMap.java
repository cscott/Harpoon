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
 * @version $Id: LinearMap.java,v 1.1.2.1 2001-11-08 00:09:37 cananian Exp $
 */
public class LinearMap extends AbstractMap {
    private LinearSet set;

    /** Creates a <code>LinearMap</code>. */
    public LinearMap() {
        set = new LinearSet();
    }

    public LinearMap(Map map) {
	set = new LinearSet();
	putAll(map);
    }

    /** Creates a <code>LinearMap</code> with specified capacity. */
    public LinearMap(int capacity) {
        set = new LinearSet(capacity);
    }

    public Set entrySet() {
	return set;
    }

    public Object put(Object key, Object value) {
	Iterator entries = set.iterator();
	Object oldValue = null;
	while(entries.hasNext()) {
	    PairMapEntry entry = (PairMapEntry) entries.next();
	    if (keysMatch(key, entry.getKey())) {
		oldValue = entry.getValue();
		entry.setValue(value);
		break;
	    }
	}
	if (oldValue == null) {
	    set.add(new PairMapEntry(key, value));
	}
	return oldValue;
    }

    private boolean keysMatch(Object k1, Object k2) {
	return ((k1 == null && k2 == null) ||
		(k1 != null && k2 != null && 
		 k1.equals(k2)));
    }

    public Object remove(Object key) {
	Iterator entries = set.iterator();
	Object oldValue = null;
	while(entries.hasNext()) {
	    PairMapEntry entry = (PairMapEntry) entries.next();
	    if (keysMatch(key, entry.getKey())) {
		oldValue = entry.getValue();
		set.remove(entry);
		break;
	    }
	}
	return oldValue;
    }
} 
