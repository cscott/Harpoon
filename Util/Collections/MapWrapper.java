// MapWrapper.java, created Wed Jun 21  3:22:34 2000 by pnkfelix
// Copyright (C) 2001 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Map;

/** <code>MapWrapper</code> is a class that acts as a proxy for
    another backing map, to allow for easy extension of
    <code>Map</code> functionality while not restricting developers to
    one particular <code>Map</code> implementation. 

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: MapWrapper.java,v 1.2.2.1 2002-02-27 22:24:13 cananian Exp $
*/
public class MapWrapper<K,V> implements Map<K,V> {
    private Map<K,V> map;
    public MapWrapper(Map<K,V> map) { this.map = map; }
    public int size() { return map.size(); }
    public boolean isEmpty() { return map.isEmpty(); }
    public boolean containsKey(Object o) { return map.containsKey(o); }
    public boolean containsValue(Object o) { return map.containsValue(o); }
    public V get(Object o) { return map.get(o); }
    public V put(K k, V v) { return map.put(k, v); }
    public V remove(Object o) { return map.remove(o); }
    public void putAll(Map<K,V> m) { map.putAll(m); }
    public void clear() { map.clear(); }
    public java.util.Set<K> keySet() { return map.keySet(); }
    public java.util.Set<Map.Entry<K,V>> entrySet() { return map.entrySet(); }
    public java.util.Collection<V> values() { return map.values(); }
    public String toString() { return map.toString(); }
    public boolean equals(Object o) { return map.equals(o); }
    public int hashCode() { return map.hashCode(); }
}
