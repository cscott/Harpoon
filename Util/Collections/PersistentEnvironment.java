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
 * @version $Id: PersistentEnvironment.java,v 1.1.2.1 2001-11-08 00:09:37 cananian Exp $
 */
public class PersistentEnvironment extends AbstractMap
    implements Environment {
    PersistentMap m = new PersistentMap();
    
    /** Creates a <code>PersistentEnvironment</code> with no mappings. */
    public PersistentEnvironment() { }
    /** Creates a <code>PersistentEnvironment</code> with the same
     *  mappings as the given <code>Map</code>. */
    public PersistentEnvironment(Map m) { putAll(m); }

    // ------------- MAP INTERFACE ---------------
    /** Remove all mappings from this map. */
    public void clear() { this.m = new PersistentMap(); }
    /** Returns <code>true</code> is this map contains no key-value mappings.*/
    public boolean isEmpty() { return m.isEmpty(); }
    /** Returns the numer of key-value mappings in this map. */
    public int size() { return m.size(); }
    /** Returns the value to which this map maps the specified key. */
    public Object get(Object key) { return m.get(key); }
    /** Associates the specified value with the specified key in this map. */
    public Object put(Object key, Object value) {
	Object prev = m.get(key);
	this.m = m.put(key, value);
	return prev;
    }
    /** Returns <code>true</code> if this map contains a mapping for the
     *  specified key. */
    public boolean containsKey(Object key) { return m.containsKey(key); }
    /** Removes the mapping for this key from this map if present. */
    public Object remove(Object key) {
	Object prev = m.get(key);
	this.m = m.remove(key);
	return prev;
    }

    // ------------- ENVIRONMENT INTERFACE ---------------
    /** A mark into an <code>PersistentEnvironment</code>. */
    private static class Mark implements Environment.Mark {
	final PersistentMap m;
	Mark(PersistentMap m) { this.m = m; }
    }
    /** Get a mark that will allow you to restore the current state of
     *  this environment. */
    public Environment.Mark getMark() { return new Mark(m); }
    /** Undo all changes since the supplied mark, restoring the map to
     *  its state at the time the mark was taken. */
    public void undoToMark(Environment.Mark m) { this.m = ((Mark)m).m; }

    // ------------- THE DREADED ENTRYSET ---------------
    /** Returns a set view of the mappings contained in this map.
     *  The returned set is immutable. */
    public Set entrySet() {
	return Collections.unmodifiableSet(m.asMap().entrySet());
    }
}
