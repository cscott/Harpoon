package harpoon.Util.Collections;

import java.util.Map;

public class MapWrapper implements Map {
    private Map map;
    public MapWrapper(Map map) { this.map = map; }
    public int size() { return map.size(); }
    public boolean isEmpty() { return map.isEmpty(); }
    public boolean containsKey(Object o) { return map.containsKey(o); }
    public boolean containsValue(Object o) { return map.containsValue(o); }
    public Object get(Object o) { return map.get(o); }
    public Object put(Object k, Object v) { return map.put(k, v); }
    public Object remove(Object o) { return map.remove(o); }
    public void putAll(Map m) { map.putAll(m); }
    public void clear() { map.clear(); }
    public java.util.Set keySet() { return map.keySet(); }
    public java.util.Set entrySet() { return map.entrySet(); }
    public java.util.Collection values() { return map.values(); }
}
