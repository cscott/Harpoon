package harpoon.Util.Collections;

import java.util.Map;
import java.util.Iterator;

public class GenericInvertibleMap extends MapWrapper implements InvertibleMap {
    // inverted map
    private MultiMap imap;

    public GenericInvertibleMap() {
	this(Factories.hashMapFactory(), new MultiMap.Factory());
    }

    public GenericInvertibleMap(MapFactory mf, MultiMap.Factory mmf) {
	super(mf.makeMap());
	imap = mmf.makeMultiMap();
    }

    public MultiMap invert() {
	return UnmodifiableMultiMap.proxy(imap);
    }

    public Object put(Object key, Object value) {
	Object old = super.put(key, value);
	imap.remove(old, key);
	imap.add(value, key);
	return old;
    }

    public void putAll(Map m) {
	super.putAll(m);
	Iterator entries = m.entrySet().iterator();
	while(entries.hasNext()) {
	    Map.Entry e = (Map.Entry) entries.next();
	    imap.add(e.getValue(), e.getKey());
	}
    }
    
    public Object remove(Object key) {
	Object r = super.remove(key);
	imap.remove(r, key);
	return r;
    }
}
