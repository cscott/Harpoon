package harpoon.Util.Collections;

import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.AbstractMap;

public abstract class UnmodifiableMultiMap 
    extends AbstractMap implements MultiMap {

    public static MultiMap proxy(final MultiMap mmap) {
	return new UnmodifiableMultiMap() {
		public Object get(Object key) { 
		    return mmap.get(key); 
		}
		public Collection getValues(Object key) { 
		    return mmap.getValues(key);
		}
		public boolean contains(Object a, Object b) { 
		    return mmap.contains(a, b);
		}
		public Set entrySet() { return mmap.entrySet(); }
	    };
    }
    
    public Object put(Object key, Object value) { die(); return null; }
    public Object remove(Object key) { die(); return null; }
    public boolean remove(Object key, Object value) { return die(); }
    public void putAll(Map t) { die(); }
    public void clear() { die(); }
    public boolean add(Object key, Object value) { return die(); }
    public boolean addAll(Object key, Collection values) { return die(); }
    public boolean retainAll(Object key, Collection values) { return die(); }
    public boolean removeAll(Object key, Collection values) { return die(); }
    private boolean die() {
	if (true) throw new UnsupportedOperationException();
	return false;
    }
}
