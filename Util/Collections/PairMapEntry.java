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
 * @version $Id: PairMapEntry.java,v 1.1.2.1 2001-11-08 00:09:37 cananian Exp $
 */
public class PairMapEntry extends AbstractMapEntry {
    private Object key, value;

    /** Creates a <code>PairMapEntry</code>. */
    public PairMapEntry(Object key, Object value) {
        this.key = key;
	this.value = value;
    }

    public Object getKey() {
	return key;
    }
    
    public Object getValue() {
	return value;
    }

    /** For use in subclass implementations *only*. */
    protected Object setKey(Object newKey) {
	Object old = key;
	key = newKey;
	return old;
    }

    public Object setValue(Object newValue) {
	Object old = value;
	value = newValue;
	return old;
    }
}
