// DisjointSet.java, created Wed Feb 23 13:06:57 2000 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>DisjointSet</code> is an implementation of disjoint-set forests
 * using the path compression and union-by-rank heuristics to achieve
 * O(m * alpha(m, n)) runtime, where 'm' is the total number of
 * operations, 'n' is the total number of elements in the set, and
 * 'alpha' denotes the *extremely* slowly-growing inverse Ackermann
 * function.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DisjointSet.java,v 1.1.2.6 2000-11-25 16:27:42 cananian Exp $
 */
public class DisjointSet  {
    private final Map elmap = new HashMap();

    /** Creates a <code>DisjointSet</code>. */
    public DisjointSet() { }

    /** Unites the dynamic sets that contain <code>o1</code> and
     *  <code>o2</code>, say S1 and S2, into a new set that is the
     *  union of these two sets.  The two sets are assumed to be
     *  disjoint prior to the operation.  The representative of the
     *  resulting set is the representative of either S1 or S2; if
     *  both S1 and S2 were previously singletons, the representative
     *  of S1 union S2 is the representative of S2. */
    public void union(Object o1, Object o2) { // this is UNION
	Node x = (Node) elmap.get(o1);
	if (x==null) x=_make_set(o1);
	Node y = (Node) elmap.get(o2);
	if (y==null) y=_make_set(o2);
	Util.assert(!x.equals(y), "Sets assumed to be disjoint");
	_union(x, y);
    }
    /** Returns the representative of the (unique) set containing
     *  <code>o</code>. */
    public Object find(Object o) { // this is FIND-SET
	Node x = (Node) elmap.get(o);
	if (x==null) return o;
	return _find_set(x).element;
    }
    /** Determines if there is a set of more than one element containing
     *  <code>o</code>. */
    public boolean contains(Object o) {
	return elmap.containsKey(o);
    }
    // these are the routines according to CLR
    private Node _make_set(Object o) {
	Util.assert(!elmap.containsKey(o));
	Node x = new Node(o);
	elmap.put(o, x);
	return x;
    }
    private Node _find_set(Node x) {
	if (x.parent != x)
	    x.parent = _find_set(x.parent);
	return x.parent;
    }
    private void _union(Node x, Node y) {
	_link(_find_set(x), _find_set(y));
    }
    private void _link(Node x, Node y) {
	if (x.rank > y.rank)
	    y.parent = x;
	else {
	    x.parent = y;
	    if (x.rank == y.rank)
		y.rank++;
	}
    }
    /** Returns an unmodifiable <code>Map</code> view of the disjoint
     *  set, where every element is mapped to its canonical representative.
     */
    public Map asMap() {
	return new AbstractMap() {
	    public boolean containsKey(Object key) {
		return elmap.containsKey(key);
	    }
	    // XXX: returns identity mapping for objects not in set.
	    public Object get(Object key) { return find(key); }
	    public Set entrySet() {
		final Set objects = elmap.keySet();
		return new AbstractSet() {
		    public int size() { return objects.size(); }
		    public Iterator iterator() {
			final Iterator objit = objects.iterator();
			return new UnmodifiableIterator() {
			    public boolean hasNext(){ return objit.hasNext(); }
			    public Object next() {
				final Object key = objit.next();
				return new AbstractMapEntry() {
				    public Object getKey() { return key; }
				    public Object getValue() {
					// note deferred for efficiency.
					return find(key);
				    }
				};
			    }
			};
		    }
		};
	    }
	};
    }
    /** Returns a human-readable representation of the DisjointSet. */
    public String toString() {
	MultiMap mm = new GenericMultiMap();
	for (Iterator it=elmap.values().iterator(); it.hasNext(); ) {
	    Node n = (Node) it.next();
	    Node r = _find_set(n);
	    if (n!=r) mm.add(r.element, n.element);
	}
	return mm.toString();
    }
    // node representation.
    private static class Node {
	Node parent;
	final Object element;
	int rank;
	Node(Object element) {
	    this.parent = this; this.element = element; this.rank = 0;
	}
    }
    /** Self-test method. */
    public static void main(String[] args) {
	DisjointSet ds = new DisjointSet();
	String a="a", b="b", c="c", d="d", e="e", f="f", g="g", h="h";
	Util.assert(!ds.contains(a) && !ds.contains(b) && !ds.contains(c));
	Util.assert(ds.find(a)==a && ds.find(b)==b && ds.find(c)==c);
	Util.assert(!ds.contains(a) && !ds.contains(b) && !ds.contains(c));
	ds.union(e, c); ds.union(b, h); ds.union(h, c);
	Util.assert(ds.find(e)==ds.find(c) && ds.find(h)==ds.find(e));
	Util.assert(ds.find(b)==ds.find(c) && ds.find(b)!=ds.find(a));
	ds.union(d, f); ds.union(g, d);
	Util.assert(ds.find(d)==ds.find(f) && ds.find(f)==ds.find(g));
	Util.assert(ds.find(d)!=ds.find(c) && ds.find(d)!=ds.find(a));
	ds.union(c, f);
	Util.assert(ds.find(e)==ds.find(f));
	Util.assert(ds.find(a)==a);
	System.err.println("PASSED.");
    }
}
