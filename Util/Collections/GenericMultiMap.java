// GenericMultiMap.java, created Tue Nov  9 00:17:02 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Default;
import harpoon.Util.Collections.PairMapEntry;
import harpoon.Util.Util;

import java.util.Map;
import java.util.Iterator;
import java.util.Set;
import java.util.Collection;
import java.util.Collections;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.HashSet;

/**
 * <code>GenericMultiMap</code> is a default implementation of a
 * <code>MultiMap</code>.  
 *
 * <P>   FSK: right now the implementation tries to preserve the
 *       property that if a key 'k' maps to an empty collection 'c' in
 *	 some MultiMap 'mm', then users of 'mm' will not be able to
 *	 see that 'k' is a member of the keySet for 'mm'.  However, it
 *	 does not preserve this property when mm.getValues(k) is used
 *	 as a means to operate on the state of 'mm', and it is not
 *	 clear to me whether one can even ensure that the property
 *	 can be maintained if arbitrary operations on mm.getValues(k)
 *	 are passed on to 'mm'.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: GenericMultiMap.java,v 1.1.2.13 2001-11-08 20:47:16 cananian Exp $ */
public class GenericMultiMap implements MultiMap {
    
    // internal Map[KeyType -> Collection[ ValueType ]]
    private Map internMap;

    // constructs Collections as needed 
    private CollectionFactory cf;
    
    // used by identity constructor
    private MapFactory mf;

    /** Creates a <code>MultiMap</code> using a <code>HashMap</code> for
	the map and <code>HashSet</code>s for the value collections.
	To gain more control over the specific sets/map used in
	internal representation of <code>this</code>, use the more
	specific {@link GenericMultiMap#GenericMultiMap(MapFactory,CollectionFactory) constructor }
	that takes <code>CollectionFactory</code>s.
    */
    public GenericMultiMap() {
	this(Factories.hashMapFactory, Factories.hashSetFactory);
    }

    /** Creates a <code>MultiMap</code> using a <code>HashMap</code> for
     *  the map and the specified <code>CollectionFactory</code> to
     *  create the value collections. */
    public GenericMultiMap(CollectionFactory cf) {
	this(Factories.hashMapFactory, cf);
    }
    /** Creates a <code>MultiMap</code> using the specified
     *  <code>MapFactory</code> to create the map and the specified
     *  <code>CollectionFactory</code> to create the value collections.
    */
    public GenericMultiMap(MapFactory mf, CollectionFactory cf) {
	this.internMap = mf.makeMap();
	this.cf = cf;
	this.mf = mf;
    }

    /** Creates a <code>GenericMultiMap</code> from another
	<code>GenericMultiMap</code>.
	
	NOTE: I would make this ctor public, but I need to eliminate
	any issues with the Collection-values being shared between
	'this' and 'mm'.

    */
    GenericMultiMap(GenericMultiMap mm) { 
	this.mf = mm.mf;
	this.cf = mm.cf;
	this.internMap = this.mf.makeMap(mm.internMap);
    }
	
    /** Makes a new <code>MultiMap</code> initialized with all of the
	<code>Map.Entry</code>s in <code>m</code>.
    */
    public GenericMultiMap(Map m) {
	this();
	Iterator entries = m.entrySet().iterator();
	while(entries.hasNext()) {
	    Map.Entry entry= (Map.Entry) entries.next();
	    this.put(entry.getKey(), entry.getValue());
	}
    }

    // note: we'd like to maintain a separate 'size' field, but
    // we can't intercept direct modification to the sets returned
    // by getValues(key), which we'd need to do.  So size() and
    // isEmpty() are slower than they might otherwise be.
    public int size() {
	int count=0;
	for (Iterator it=internMap.values().iterator(); it.hasNext(); )
	    count += ((Collection)it.next()).size();
	return count;
    }

    public boolean isEmpty() {
	// we could return 'size()==0' but that's slow.
	for (Iterator it=internMap.values().iterator(); it.hasNext(); )
	    if (((Collection)it.next()).size()>0)
		return false;
	return true;
    }
    
    public boolean containsKey(Object key) {
	Collection s = (Collection) internMap.get(key);
	return (s != null && s.size() != 0);
    }
    
    public boolean containsValue(Object value) {
	Iterator entries = internMap.entrySet().iterator();
	boolean foundVal = false;
	while(entries.hasNext()) {
	    Collection s = (Collection) ((Map.Entry)entries.next()).getValue();
	    if (s.contains(value)) {
		foundVal = true;
		break;
	    }
	}
	return foundVal;
    }


    /** Returns some arbitrary value from the set of values to which
	this map maps the specified key.  Returns <code>null</code> if
	the map contains no mapping for the key; it's also possible
	that the map explicitly maps the key to <code>null</code>.
	The <code>containsKey</code> operation may be used to
	distinquish these two cases.
	
	Note that if only the <code>put</code> method is used to
	modify <code>this</code>, then <code>get</code> will operate
	just as it would in any other <code>Map</code>.
    */
    public Object get(Object key) {
	Collection s = (Collection) internMap.get(key);
	if (s == null || s.size() == 0) {
	    return null;
	} else {
	    return s.iterator().next();
	}
    }

    /** Associates the specified value with the specified key in this
	map.  If the map previously contained any mappings for this
	key, all of the old values are replaced.  Returns some value
	that was previous associated with the specified key, or
	<code>null</code> if no values were associated previously. 
    */
    public Object put(Object key,
		      Object value) {
	Collection c = getValues(key);
	Object prev = c.size()==0 ? null : c.iterator().next();
	c.clear();
	c.add(value);
	return prev;
    }

    /** Removes all mappings for this key from this map if present. 
	Returns some previous value associated with specified key, or
	<code>null</code> if there was no mapping for key.  
     */
    public Object remove(Object key) {
	Collection c = (Collection) internMap.get(key);
	internMap.remove(key);
	return (c==null || c.size()==0) ? null : c.iterator().next();
    }

    /** Removes a mapping from key to value from this map if present.
	Note that if multiple mappings from key to value are permitted
	by this map, then only one is guaranteed to be removed.
	Returns true if <code>this</code> was modified as a result of
	this operation, else returns false.
    */
    public boolean remove(Object key, Object value) {
	Collection c = (Collection) internMap.get(key);
	boolean result = (c!=null) ? c.remove(value) : false;
	if (c!=null && c.size()==0) internMap.remove(key);
	return result;
    }

    /** Copies the mappings from the specified map to this
	map.  These mappings will replace any mappings that this map
	had for any of the keys currently in the specified map.  Note
	that <code>putAll(mm)</code> where <code>mm</code> is a
	<code>MultiMap</code> will NOT add all of the mappings in
	<code>mm</code>; it will only add all of the Keys in
	<code>mm</code>, mapping each Key to one of the Values it
	mapped to in <code>mm</code>.  To add all of the mappings from
	another <code>MultiMap</code>, use
	<code>addAll(MultiMap)</code>.  */
    public void putAll(Map t) {
	Iterator entries = t.entrySet().iterator();
	while(entries.hasNext()) {
	    Map.Entry e = (Map.Entry) entries.next();
	    this.put( e.getKey(), e.getValue() );
	}
    }
    
    public void clear() {
	internMap.clear();
    }

    public boolean equals(Object o) {
	try {
	    Set entrySet = ((Map) o).entrySet();
	    return this.entrySet().equals(entrySet);
	} catch (ClassCastException e) {
	    return false;
	}
    }

    public int hashCode() {
	Iterator entries = entrySet().iterator();
	int sum = 0;
	while(entries.hasNext()) {
	    sum += entries.next().hashCode();
	}
	return sum;
    }

    /** Ensures that <code>this</code> contains an association from
	<code>key</code> to <code>value</code>.

	(<code>MultiMap</code> specific operation).

	@return <code>true</code> if this mapping changed as a result of
	        the call
    */
    public boolean add(Object key, Object value) {
	boolean changed = getValues(key).add(value);
	return changed;
    }
    
    /** Adds to the current mappings: associations for
	<code>key</code> to each value in <code>values</code>.  

	(<code>MultiMap</code> specific operation). 

	@return <code>true</code> if this mapping changed as a result
	        of the call
    */
    public boolean addAll(Object key, Collection values) {
	boolean changed = false;
	for (Iterator it=values.iterator(); it.hasNext(); )
	    if (this.add(key, it.next()))
		changed = true;
	return changed;
    }
    /** Add all mappings in the given multimap to this multimap. */
    public boolean addAll(MultiMap mm) {
	boolean changed = false;
	for (Iterator it=mm.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry me = (Map.Entry) it.next();
	    if (add(me.getKey(), me.getValue()))
		changed = true;
	}
	return changed;
    }
	
    /** Removes from the current mappings: associations for
	<code>key</code> to any value not in <code>values</code>. 

	(<code>MultiMap</code> specific operation). 

	@return <code>true</code> if this mapping changed as a result
	        of the call
    */
    public boolean retainAll(Object key, Collection values) {
	boolean changed = false;
	for (Iterator it=getValues(key).iterator(); it.hasNext(); )
	    if (!values.contains(it.next())) {
		it.remove();
		changed = true;
	    }
	if (getValues(key).isEmpty()) internMap.remove(key);
	return changed;
    }

    /** Removes from the current mappings: associations for
	<code>key</code> to any value in <code>values</code>.

	(<code>MultiMap</code> specific operation). 

	@return <code>true</code> if this mapping changed as a result
	        of the call
    */
    public boolean removeAll(Object key, Collection values) {
	boolean changed = false;
	for (Iterator it=values.iterator(); it.hasNext(); )
	    if (this.remove(key, it.next()))
		changed = true;
	return changed;
    }

    /** Returns the collection of Values associated with
	<code>key</code>.  Modifications to the returned
	<code>Collection</code> affect <code>this</code> as well.  If 
	there are no Values currently associated with
	<code>key</code>, constructs a new, mutable, empty
	<code>Collection</code> and returns it.
	(<code>MultiMap</code> specific operation). 
    */
    public Collection getValues(final Object key) {
	Collection c = (Collection) internMap.get(key);
	if (c == null) {
	    c = cf.makeCollection();
	    internMap.put(key, c);
	}
	return c;
    }

    /** Returns true if <code>a</code> has a mapping to <code>b</code>
	in <code>this</code>.
	(<code>MultiMap</code> specific operation). 
    */
    public boolean contains(Object a, Object b) {
	Collection c = (Collection)internMap.get(a);
	if (c != null)
	    return c.contains(b);
	else
	    return false;
    }

    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("[");
	Iterator keys = keySet().iterator();
	while(keys.hasNext()) {
	    Object k = keys.next();
	    Collection values = getValues(k);
	    if (values.size()==0) continue;
	    sb.append("< "+k+" -> "+values+" > ");
	}
	sb.append("]");
	return sb.toString();
    }

    /** Returns a set view of the keys in this map. */
    public Set keySet() {
	return keySet;
    }
    private final Set keySet = new KeySet();
    
    /** Returns a collection view of the values contained in this
	map.  
    */
    public Collection values() { 
	return valuesCollection;
    }
    private final Collection valuesCollection = new ValuesCollection();

    /** Returns a set view of the mappings contained in this map.
	This view is fully modifiable; the elements are
	<code>Map.Entry</code>s.  The returned set is actually a
	<code>MultiMapSet</code>, from which you can get back the
	original <code>MultiMap</code>.
    */
    public Set entrySet() {
	return entrySet;
    }
    private final MultiMapSet entrySet = new EntrySet();
    
    // here are the class declarations that make the key set, entry set and
    // values collection work.
    class KeySet extends AbstractSet {
	public int size() { return internMap.keySet().size(); }
	public Iterator iterator() {
	    return new Iterator() {
		    Iterator it = internMap.keySet().iterator();
		    Object lastKey;
		    public boolean hasNext() { return it.hasNext(); }
		    public Object next() { return (lastKey=it.next()); }
		    public void remove() {
			Collection c = (Collection) internMap.get(lastKey);
			it.remove();
		    }
		};
	}
	public boolean remove(Object o) {
	    boolean changed = contains(o);
	    GenericMultiMap.this.remove(o);
	    return changed;
	}
	// for efficiency.
	public boolean contains(Object o) {
	    // note that this is slightly different from MM.containsKey(o)
	    return internMap.containsKey(o);
	}
	public void clear() {
	    GenericMultiMap.this.clear();
	}
    }
    class EntrySet extends CollectionView implements Set, MultiMapSet {
	EntrySet() { super(ENTRY); }
	// these are methods in MultiMapSet
	public Map asMap() { return asMultiMap(); }
	public MultiMap asMultiMap() { return GenericMultiMap.this; }
	// these methods aren't in the collections interface
	// (from classpath impl of AbstractSet)
	public boolean equals(Object o) {
	    if (o == this)
		return true;
	    else if (o instanceof Set && ((Set) o).size() ==
		     EntrySet.this.size())
		return EntrySet.this.containsAll((Collection) o);
	    else
		return false;
	}
	public int hashCode() {
	    Iterator itr = EntrySet.this.iterator();
	    int size = EntrySet.this.size();
	    int hash = 0;
	    for (int pos = 0; pos < size; pos++)
		{
		    Object obj = itr.next();
		    if (obj != null)
			hash += obj.hashCode();
		}
	    return hash;
	}
    }
    class ValuesCollection extends CollectionView {
	ValuesCollection() { super(VALUES); }
    }
    // common code
    class CollectionView extends AbstractCollection {
	static final int ENTRY = 0; // set, really.
	static final int VALUES = 1;
	final int type;
	protected CollectionView(int type) {
	    this.type = type;
	    if (type==ENTRY)
		Util.assert(this instanceof Set);
	}
	public int size() { return GenericMultiMap.this.size(); }
	public Iterator iterator() {
	    return new Iterator() {
		    Iterator mapit = internMap.entrySet().iterator();
		    Iterator setit = Default.nullIterator;
		    Iterator lastit;
		    Object key;
		    { advance(); }
		    public boolean hasNext() { return setit.hasNext(); }
		    public Object next() {
			Object o = setit.next();
			Object k = key;
			advance();
			if (type==ENTRY)
			    return new PairMapEntry(k, o) {
				    public Object setValue(Object newValue) {
					Object oldValue = getValue();
					GenericMultiMap.this.remove(getKey(),
								    oldValue);
					GenericMultiMap.this.add(getKey(),
								 newValue);
					super.setValue(newValue);
					return oldValue;
				    }
				};
			else return o;
		    }
		    void advance() {
			lastit = setit;
			while (!setit.hasNext() && mapit.hasNext()) {
			    Map.Entry me = (Map.Entry) mapit.next();
			    key = me.getKey();
			    Collection c = (Collection) me.getValue();
			    setit = c.iterator();
			}
		    }
		    public void remove() {
			lastit.remove();
		    }
		};
	}
	public boolean add(Object o) {
	    if (type==VALUES) throw new UnsupportedOperationException();
	    // o should be a Map.Entry.
	    if (!(o instanceof Map.Entry))
		throw new UnsupportedOperationException();
	    Map.Entry me = (Map.Entry) o;
	    return GenericMultiMap.this.add(me.getKey(), me.getValue());
	}
	public boolean remove(Object o) {
	    if (type==VALUES) throw new UnsupportedOperationException();
	    // o should be a Map.Entry.
	    if (!(o instanceof Map.Entry))
		throw new UnsupportedOperationException();
	    Map.Entry me = (Map.Entry) o;
	    return GenericMultiMap.this.remove(me.getKey(), me.getValue());
	}
	// other methods for efficiency.
	public boolean contains(Object o) {
	    if (type==VALUES)
		return GenericMultiMap.this.containsValue(o);
	    // o should be a Map.Entry.
	    if (!(o instanceof Map.Entry)) return false;
	    Map.Entry me = (Map.Entry) o;
	    return GenericMultiMap.this.contains(me.getKey(), me.getValue());
	}
	public void clear() {
	    GenericMultiMap.this.clear();
	}
    }
}
