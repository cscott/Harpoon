// TestSuperInt.java, created Sat Sep 22 17:26:44 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

import java.util.*;
/**
 * <code>TestSuperInt</code> tests method inheritance from super-interfaces.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TestSuperInt.java,v 1.1 2001-09-24 17:09:59 cananian Exp $
 */
public class TestSuperInt {
    public static void main(String[] args) {
	Map m = new HashMap();
	Map m2 = EMPTY_MAP;
	if (args.length>200) m2 = m;
	m2.equals(m);
	System.out.println("PASSED: Map.equals()");
	EMPTY_MAP.equals(m);
	System.out.println("PASSED: SortedMap.equals()");
    }
    /** An empty map. Missing from <code>java.util.Collections</code>.*/
    public static final SortedMap EMPTY_MAP = new SerializableSortedMap() {
	public void clear() { }
	public boolean containsKey(Object key) { return false; }
	public boolean containsValue(Object value) { return false; }
	public Set entrySet() { return Collections.EMPTY_SET; }
	public boolean equals(Object o) {
	    if (!(o instanceof Map)) return false;
	    return ((Map)o).size()==0;
	}
	public Object get(Object key) { return null; }
	public int hashCode() { return 0; }
	public boolean isEmpty() { return true; }
	public Set keySet() { return Collections.EMPTY_SET; }
	public Object put(Object key, Object value) {
	    throw new UnsupportedOperationException();
	}
	public void putAll(Map t) {
	    if (t.size()==0) return;
	    throw new UnsupportedOperationException();
	}
	public Object remove(Object key) { return null; }
	public int size() { return 0; }
	public Collection values() { return Collections.EMPTY_SET; }
	public String toString() { return "{}"; }
	// this should always be a singleton.
	private Object readResolve() { return TestSuperInt.EMPTY_MAP; }
	// SortedMap interface.
	public Comparator comparator() { return null; }
	public Object firstKey() { throw new NoSuchElementException(); }
	public Object lastKey() { throw new NoSuchElementException(); }
	public SortedMap headMap(Object toKey) {
	    return TestSuperInt.EMPTY_MAP;
	}
	public SortedMap tailMap(Object fromKey) {
	    return TestSuperInt.EMPTY_MAP;
	}
	public SortedMap subMap(Object fromKey, Object toKey) {
	    return TestSuperInt.EMPTY_MAP;
	}
    };
    private static interface SerializableSortedMap
	extends SortedMap, java.io.Serializable { /* only declare */ }
}
