// HashEnvironment.java, created Sat Aug 28 23:02:21 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.FilterIterator;
import harpoon.Util.Util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * A <code>HashEnvironment</code> is an <code>Environment</code> using
 * a <code>HashMap</code> as the backing store.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HashEnvironment.java,v 1.4 2002-04-10 03:07:11 cananian Exp $
 */
public class HashEnvironment<K,V> extends AbstractMap<K,V>
    implements Environment<K,V> {
    final Map<K,LList<V>> back = new HashMap<K,LList<V>>();
    final List<K> scope = new ArrayList<K>();
    int size = 0;
    
    /** Creates a <code>HashEnvironment</code>. */
    public HashEnvironment() { }
    /** Creates a <code>HashEnvironment</code> with all the mappings in
     *  the given map. */
    public <K2 extends K, V2 extends V> HashEnvironment(Map<K2,V2> m) {
	this();
	putAll(m);
    }

    // -- MAP INTERFACE
    /** Returns <code>true</code> if this map contains a mapping for the
     *  specified key. */
    public boolean containsKey(Object key) {
	LList<V> l = back.get(key);
	return (l==null) ? false : l.hasValue();
    }
    /** Returns the value to which this map maps the specified key. */
    public V get(Object key) {
	LList<V> l = back.get(key);
	return (l==null) ? null : l.getValue();
    }
    /** Associates the specified value with the specified key in this map. */
    public V put(K key, V value) {
	LList<V> l = back.get(key);
	back.put(key, new Valued<V>(value, l));
	scope.add(key);
	if (l==null || !l.hasValue()) { size++; return null; }
	return l.getValue();
    }
    /** Removes the mapping for this key from this map if present. */
    public V remove(Object key) {
	LList<V> l = back.get(key);
	if (l==null || !l.hasValue()) return null;
	K k = (K) key; // safe if back.get(key) returned non-null.
	back.put(k, new NoValue<V>(l));
	scope.add(k);
	size--;
	return l.getValue();
    }
    /** Removes the top mapping for this key. */
    void pop(K key) {
	LList<V> l = back.get(key);
	assert l!=null;
	assert l instanceof Valued<V> || l.next instanceof Valued<V>;
	if (l.next==null) back.remove(key);
	else back.put(key, l.next);
	if (l.hasValue()) size--;
	if (l.next!=null && l.next.hasValue()) size++;
    }
    /** Returns the number of key-value mappings in this map. */
    public int size() { return size; }
    /** Clears all mappings. */
    public void clear() {
	for (Iterator<K> it=keySet().iterator(); it.hasNext(); )
	    remove(it.next());
    }
    
    // --- ENVIRONMENT INTERFACE
    public Environment.Mark getMark() {
	return new Mark(scope.size());
    }
    public void undoToMark(Environment.Mark m) {
	for (int i = ((Mark)m).i; scope.size() > i; ) {
	    pop(scope.get(scope.size()-1));
	    scope.remove(scope.size()-1);
	}
	assert scope.size()==((Mark)m).i : "undoToMark not repeatable!";
    }

    // --- EVIL EVIL SET VIEW
    /** The <code>Set</code> returned by this method is really a
     *  <code>MapSet</code>. */
    public MapSet<K,V> entrySet() {
	return new AbstractMapSet<K,V>() {
	    public int size() { return HashEnvironment.this.size; }
	    public Iterator<Map.Entry<K,V>> iterator() {
		return new FilterIterator<Map.Entry<K,LList<V>>,Map.Entry<K,V>>
		    (HashEnvironment.this.back.entrySet().iterator(),
		     new FilterIterator.Filter<Map.Entry<K,LList<V>>,Map.Entry<K,V>>() {
		    public boolean isElement(Map.Entry<K,LList<V>> e) {
			return e.getValue().hasValue();
		    }
		    public Map.Entry<K,V> map(final Map.Entry<K,LList<V>> e) {
			return new AbstractMapEntry<K,V>() {
			    public K getKey() { return e.getKey(); }
			    public V getValue() { return e.getValue().getValue(); }
			};
		    }
		});
	    }
	    public HashEnvironment<K,V> asMap(){ return HashEnvironment.this; }
	};
    }
    // needed to make the anonymous class declaration above work.
    static abstract class AbstractMapSet<K,V>
	extends AbstractSet<Map.Entry<K,V>> implements MapSet<K,V> { }
	
    private static class Mark implements Environment.Mark {
	final int i;
	Mark(int i) { this.i = i; }
    }
    private static abstract class LList<T> {
	final LList<T> next;
	LList(LList<T> next) { this.next=next; }
	abstract boolean hasValue();
	abstract T getValue();
    }
    private static class NoValue<T> extends LList<T> {
	NoValue(LList<T> next) { super(next); }
	boolean hasValue() { return false; }
	T getValue() { return null; }
    }
    private static class Valued<T> extends LList<T> {
	final T value;
	Valued(T value, LList<T> next) { super(next); this.value=value; }
	boolean hasValue() { return true; }
	T getValue() { return value; }
    }

    /** Self-test function. */
    public static void main(String argv[]) {
	Environment<String,String> e = new HashEnvironment<String,String>();
	assert e.size()==0 && !e.containsKey("a") && !e.containsKey("b");
	e.put("a","a"); e.put("a","b");
	assert e.size()==1 && e.containsKey("a") && e.containsValue("b");
	assert !e.containsValue("a") && !e.containsValue("c");
	Environment.Mark m = e.getMark();
	e.remove("a"); e.remove("a");
	assert e.size()==0 && !e.containsKey("a");
	assert !e.containsKey("b") && !e.containsValue("b");
	assert !e.containsValue("a");
	e.put("b","b"); e.put("b","c");
	assert e.size()==1 && e.containsKey("b");
	assert !e.containsKey("a") && e.containsValue("c");
	assert !e.containsValue("b");
	System.out.println(e);
	e.undoToMark(m);
	assert e.size()==1 && e.containsKey("a") && !e.containsKey("b");
	System.out.println(e);
	e.put("c", "d"); e.put("c", "d");  
	assert e.size()==2 && e.containsKey("c") && e.containsValue("d");
	m = e.getMark();
	e.clear();
	assert e.size()==0 && !e.containsKey("a") && !e.containsKey("b");
	e.undoToMark(m);
	assert e.size()==2 && e.containsKey("c") && e.containsValue("d");
	assert e.containsKey("a") && !e.containsKey("b");
	System.out.println(e);
    }
}
