// GenericInvertibleMap.java, created Wed Jun 21  3:22:34 2000 by pnkfelix
// Copyright (C) 2001 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Map;
import java.util.Iterator;

/** <code>GenericInvertibleMap</code> is a default implementation of
    <code>InvertibleMap</code>.  It returns unmodifiable inverted
    views of the mappings it maintains.

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: GenericInvertibleMap.java,v 1.1.2.5 2001-06-17 22:36:32 cananian Exp $
*/
public class GenericInvertibleMap extends MapWrapper implements InvertibleMap {
    // inverted map
    private MultiMap imap;

    /** Constructs an invertible map backed by a HashMap.
     */
    public GenericInvertibleMap() {
	this(Factories.hashMapFactory, new MultiMap.Factory());
    }

    /** Constructs an invertible map backed by a map constructed by
	<code>mf</code> and an inverted map constructed by
	<code>mmf</code>. 
     */
    public GenericInvertibleMap(MapFactory mf, MultiMap.Factory mmf) {
	super(mf.makeMap());
	imap = mmf.makeMultiMap();
    }

    /** Returns an unmodifiable inverted view of <code>this</code>.
     */
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
