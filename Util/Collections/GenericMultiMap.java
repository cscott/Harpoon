// GenericMultiMap.java, created Tue Nov  9 00:17:02 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Util;
import harpoon.Util.PairMapEntry;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.CombineIterator;
import harpoon.Util.FilterIterator;

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
 * <P> FSK: This data structure is a bit experimental; a few changes
 *     may be coming:<OL>
 *       <LI> The <code>Collection</code> views returned right now
 *	      don't offer very much in terms of modifying the
 *	      state of <code>this</code> internally.
 *	 <LI> Some of the views returned do not properly reflect
 *	      modification in <code>this</code>.  This is a gross
 *	      oversight of <code>Collection</code>'s interface
 *	      on my part and I need to fix it, which I will do when I
 *	      have free time.
 *	 </OL> 
 *
 * <P>   Also, right now the implementation tries to preserve the
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
 * @version $Id: GenericMultiMap.java,v 1.1.2.8 2000-11-10 19:55:11 cananian Exp $ */
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

    public int size() {
	return entrySet().size();
    }

    public boolean isEmpty() {
	boolean empty = true;
	Iterator entries = internMap.entrySet().iterator();
	while(entries.hasNext()) {
	    Collection s = (Collection)
		((Map.Entry)entries.next()).getValue();
	    if (s != null && s.size() != 0) {
		empty = false;
	    }
	}
	return empty;
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
	Object prev = get(key);
	internMap.put(key, cf.makeCollection(Collections.singleton(value)));
	return prev;
    }

    /** Removes all mappings for this key from this map if present. 
	Returns some previous value associated with specified key, or
	<code>null</code> if there was no mapping for key.  
     */
    public Object remove(Object key) {
	Object prev = get(key);
	internMap.remove(key);
	return prev;
    }

    /** Removes a mapping from key to value from this map if present.
	Note that if multiple mappings from key to value are permitted
	by this map, then only one is guaranteed to be removed.
	Returns true if <code>this</code> was modified as a result of
	this operation, else returns false.
    */
    public boolean remove(Object key, Object value) {
	Collection c = (Collection) internMap.get(key);
	if (c != null) 
	    return c.remove(value);
	else 
	    return false;
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

    /** Returns a set view of the keys in this map.

	NOTE: Does not properly implement Map.keySet(), since changes
	in Map structure are not reflected in previously returned
	keySets.  Fix this at some point for safety.
    */
    public Set keySet() {
	FilterIterator iter = 
	    new FilterIterator(internMap.keySet().iterator(),
			       new FilterIterator.Filter() {
				   public boolean isElement( Object k ) {
				       return !((Collection)internMap.get(k)).isEmpty();
				   }
			       });

	HashSet set = new HashSet();
	while(iter.hasNext()) {
	    set.add(iter.next());
	}
	return Collections.unmodifiableSet(set);
    }
    
    /** Returns a collection view of the values contained in this
	map.  

	NOTE: Does not properly implement Map.values(), since changes
	in Map structure are not reflected in previously returned
	Collections.  Fix this at some point for safety.
    */
    public Collection values() { 
	final Iterator collIter = internMap.values().iterator();
	final Iterator iterIter = new UnmodifiableIterator() {
	    public boolean hasNext() {
		return collIter.hasNext();
	    }
	    public Object next() {
		return ((Collection)collIter.next()).iterator();
	    }
	};
	return new AbstractCollection() {
	    public Iterator iterator() {
		return new CombineIterator(iterIter);
	    }
	    public int size() {
		return GenericMultiMap.this.size();
	    }
	};
    }

    /** Returns a set view of the mappings contained in this map.

	NOTE: Does not properly implement Map.entrySet(), since changes
	in Map structure are not reflected in previously returned
	entrySets.  Fix this at some point for safety.
    */
    public Set entrySet() {
	int size = 0;
	Iterator k2cEntries = internMap.entrySet().iterator();
	while(k2cEntries.hasNext()) {
	    Iterator cIter = 
		((Collection) ((Map.Entry)
			       k2cEntries.next()).getValue()).iterator(); 
	    while(cIter.hasNext()) { 
		size++; cIter.next(); 
	    }
	}
	final int sz = size;

	final Iterator entries = internMap.entrySet().iterator();
	final Iterator iterIter = new UnmodifiableIterator() {
	    public boolean hasNext() {
		return entries.hasNext();
	    }
	    public Object next() {
		Map.Entry entry = (Map.Entry) entries.next();
		final Object key = entry.getKey();
		final Iterator valueC = ((Collection) entry.getValue()).iterator();
		return new UnmodifiableIterator() {
		    public boolean hasNext() {
			return valueC.hasNext();
		    }
		    public Object next() {
			final Object val = valueC.next();
			return new PairMapEntry(key, val);
		    }
		};
	    }
	};
	return new AbstractSet() {
	    public Iterator iterator() {
		return new CombineIterator(iterIter);
	    }

	    public int size() {
		return sz;
	    }
	};
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
	return getValues(key).add(value);
    }
    
    /** Adds to the current mappings: associations for
	<code>key</code> to each value in <code>values</code>.  

	(<code>MultiMap</code> specific operation). 

	@return <code>true</code> if this mapping changed as a result
	        of the call
    */
    public boolean addAll(Object key, Collection values) {
	return getValues(key).addAll(values);
    }
	
    /** Removes from the current mappings: associations for
	<code>key</code> to any value not in <code>values</code>. 

	(<code>MultiMap</code> specific operation). 

	@return <code>true</code> if this mapping changed as a result
	        of the call
    */
    public boolean retainAll(Object key, Collection values) {
	boolean changed = false;
	changed = getValues(key).retainAll(values);
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
	changed = getValues(key).removeAll(values);
	if (getValues(key).isEmpty()) internMap.remove(key);
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
	    sb.append("< "+k+" -> "+values+" > ");
	}
	sb.append("]");
	return sb.toString();
    }
}
