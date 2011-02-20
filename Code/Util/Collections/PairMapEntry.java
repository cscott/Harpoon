// PairMapEntry.java, created Wed Aug  4 12:16:20 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

/**
 * <code>PairMapEntry</code> is the easiest implementation of a
 * <code>Map.Entry</code> ever: a pair!  Basically saves coders the
 * drugery of writing an inner class at the expense of an import
 * statement.
 *
 * Note that <code>PairMapEntry</code>s <b>are</b> mutable:
 * <code>setValue(Object)</code> is defined in this class.
 *
 * Using <code>null</code> as a key or value will not cause this class 
 * or <code>AbstractMapEntry</code> to fail, but be warned that
 * several <code>Map</code> implementations do not like 
 * <code>null</code>s in their internal structures.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: PairMapEntry.java,v 1.3 2002-04-10 03:07:13 cananian Exp $
 */
public class PairMapEntry<K,V> extends AbstractMapEntry<K,V> {
    private K key;
    private V value;

    /** Creates a <code>PairMapEntry</code>. */
    public PairMapEntry(K key, V value) {
        this.key = key;
	this.value = value;
    }

    public K getKey() {
	return key;
    }
    
    public V getValue() {
	return value;
    }

    /** For use in subclass implementations *only*. */
    protected K setKey(K newKey) {
	K old = key;
	key = newKey;
	return old;
    }

    public V setValue(V newValue) {
	V old = value;
	value = newValue;
	return old;
    }
}
