// LinearMap.java, created Wed Aug  4 11:59:14 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.AbstractMap;
import java.util.Set;
import java.util.Iterator;
/**
 * <code>LinearMap</code> is a simplistic light-weight
 * <code>Map</code> designed for use when the number of entries is
 * small.  It is backed by a <code>LinearSet</code>.
 *
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LinearMap.java,v 1.1.2.2 1999-08-12 20:42:38 pnkfelix Exp $
 */
public class LinearMap extends AbstractMap {
    private LinearSet set;

    /** Creates a <code>LinearMap</code>. */
    public LinearMap() {
        set = new LinearSet();
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
	    if ((key == null && entry.getKey() == null) ||
		(key != null && entry.getKey() != null && 
		 key.equals(entry.getKey()))) {
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
} 
