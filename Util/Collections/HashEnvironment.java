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
 * @version $Id: HashEnvironment.java,v 1.3.2.1 2002-02-27 08:37:54 cananian Exp $
 */
public class HashEnvironment extends AbstractMap
    implements Environment {
    final Map back = new HashMap();
    final List scope = new ArrayList();
    int size = 0;
    
    /** Creates a <code>HashEnvironment</code>. */
    public HashEnvironment() { }
    /** Creates a <code>HashEnvironment</code> with all the mappings in
     *  the given map. */
    public HashEnvironment(Map m) { putAll(m); }

    // -- MAP INTERFACE
    /** Returns <code>true</code> if this map contains a mapping for the
     *  specified key. */
    public boolean containsKey(Object key) {
	LList l = (LList) back.get(key);
	return !(l==null || l instanceof NoValue);
    }
    /** Returns the value to which this map maps the specified key. */
    public Object get(Object key) {
	LList l = (LList) back.get(key);
	return (l==null)?null:l.getValue();
    }
    /** Associates the specified value with the specified key in this map. */
    public Object put(Object key, Object value) {
	LList l = (LList) back.get(key);
	back.put(key, new Valued(value, l));
	scope.add(key);
	if (l==null || l instanceof NoValue) { size++; return null; }
	return l.getValue();
    }
    /** Removes the mapping for this key from this map if present. */
    public Object remove(Object key) {
	LList l = (LList) back.get(key);
	if (l==null || l instanceof NoValue) return null;
	back.put(key, new NoValue(l));
	scope.add(key);
	size--;
	return l.getValue();
    }
    /** Removes the top mapping for this key. */
    void pop(Object key) {
	LList l = (LList) back.get(key);
	assert l!=null;
	assert l instanceof Valued || l.next instanceof Valued;
	if (l.next==null) back.remove(key);
	else back.put(key, l.next);
	if (l instanceof Valued) size--;
	if (l.next instanceof Valued) size++;
    }
    /** Returns the number of key-value mappings in this map. */
    public int size() { return size; }
    /** Clears all mappings. */
    public void clear() {
	for (Iterator it=keySet().iterator(); it.hasNext(); )
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
    public Set entrySet() {
	return new AbstractMapSet() {
	    public int size() { return HashEnvironment.this.size; }
	    public Iterator iterator() {
		return new FilterIterator(HashEnvironment.this.back.entrySet().iterator(), new FilterIterator.Filter() {
		    public boolean isElement(Object o) {
			Map.Entry e = (Map.Entry) o;
			return !(e.getValue() instanceof NoValue);
		    }
		    public Object map(Object o) {
			final Map.Entry e = (Map.Entry) o;
			return new AbstractMapEntry() {
			    public Object getKey() { return e.getKey(); }
			    public Object getValue() { return ((LList) e.getValue()).getValue(); }
			};
		    }
		});
	    }
	    public Map asMap() { return HashEnvironment.this; }
	};
    }
    // needed to make the anonymous class declaration above work.
    static abstract class AbstractMapSet extends AbstractSet
	implements MapSet { }
	
    private static class Mark implements Environment.Mark {
	final int i;
	Mark(int i) { this.i = i; }
    }
    private static abstract class LList {
	final LList next;
	LList(LList next) { this.next=next; }
	abstract Object getValue();
    }
    private static class NoValue extends LList {
	NoValue(LList next) { super(next); }
	Object getValue() { return null; }
    }
    private static class Valued extends LList {
	final Object value;
	Valued(Object value, LList next) { super(next); this.value=value; }
	Object getValue() { return value; }
    }

    /** Self-test function. */
    public static void main(String argv[]) {
	Environment e = new HashEnvironment();
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
