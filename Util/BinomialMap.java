// BinomialMap.java, created Sat Jun 19 17:58:19 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * A <code>BinomialMap</code> is based on a binomial heap, which allows
 * O(lg n) time bounds for insert, minimum, extract-min, union,
 * decrease-key, and delete operations.  Implementation is based on
 * the description in <i>Introduction to Algorithms</i> by Cormen,
 * Leiserson, and Rivest, on page 400 and following.
 * <p>Note that this is not really a map, in that duplicate keys are
 * permitted.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: BinomialMap.java,v 1.1.2.1 1999-06-20 05:30:21 cananian Exp $
 */
public class BinomialMap extends AbstractMap implements Cloneable {
    private static final boolean debug=false;

    Node head=null;
    /*final*/ Comparator c;
    
    /** Constructs a new, empty <code>BinomialMap</code>, sorted according
     *  to the keys' natural order. All keys inserted into the new map
     *  must implement the <code>Comparable</code> interface. O(1) time. */
    public BinomialMap() { this.c = Default.comparator; }
    /** Constructs a new, empty <code>BinomialMap</code>, sorted according
     *  to the given comparator. O(1) time. */
    public BinomialMap(Comparator c) { this.c = c; }
    /** Constructs a new map containing the same mappings as the given map,
     *  sorted according to the keys' natural order.  All keys inserted
     *  into the new map must implement the <code>Comparable</code> 
     *  interface. This constructor runs in O(n lg n) time. */
    public BinomialMap(Map m) {
	this.c = Default.comparator;
	for (Iterator it=m.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry me = (Map.Entry) it.next();
	    put(me.getKey(), me.getValue());
	}
	Util.assert(isHeapOrdered(head, c));
    }
    /** Constructs a new map with the same mappings as the specified
     *  <code>BinomialMap</code>. O(n) time. */
    public BinomialMap(BinomialMap m) {
	this.c = m.c;
	this.head = _clone(null, m.head);
	Util.assert(isHeapOrdered(head, c));
    }

    /** Returns a mapping entry with minimal key.  Takes O(lg n) time. */
    public Map.Entry minimum() {
	Util.assert(isHeapOrdered(head, c));
	Node y=null;
	// minimum has to be one of the roots.
	for (Node x=this.head; x!=null; x=x.sibling)
	    if (y==null || c.compare(x.key, y.key) < 0)
		y=x;
	return y;
    }
    /** Links the B_{k-1} tree rooted at node y to the B_{k-1} tree rooted
     *  at node z; that is, it makes z the parent of y.  Node z thus becomes
     *  the root of a B_k tree. O(1) time. */
    private void _link(Node y, Node z) {
	y.parent = z;
	y.sibling= z.child;
	z.child = y;
	z.degree++;
    }
    /** Merges the root lists of H1 and H2 into a single linked list sorted
     *  by degree into monotonically increasing order.  O(m) time, where
     *  m is the total number of roots in H1 and H2. [O(lg n) time] */
    private Node _merge(Node h1, Node h2) {
	if (h1==null) return h2;
	if (h2==null) return h1;
	if (h1.degree<h2.degree) {
	    h1.sibling = _merge(h1.sibling, h2);
	    return h1;
	} else {
	    h2.sibling = _merge(h1, h2.sibling);
	    return h2;
	}
    }
    /** Adds all of the mappings from the specified map into this map.
     *  <b>Duplicates are not removed</b>.  If the specified map is
     *  an instance of <code>BinomialMap</code>, this operation takes
     *  O(n+lg n) time; otherwise it takes O(n lg n) time. Does not
     *  modified the specified map <code>m</code>. */
    public void putAll(Map m) {
	if (m instanceof BinomialMap)
	    putAll((BinomialMap) ((BinomialMap) m).clone());
	else super.putAll(m);
    }
    /** Copies all of the mappings from the specified map to this
     *  map. Note that duplicates <b>are</b> permitted. This operation
     *  takes O(lg n), where n is the number of entries in the resulting
     *  map. The comparator for m <b>must be identical</b> to the comparator
     *  for <code>this</code>. After calling putAll(), the specified map
     *  will be empty. */
    public void putAllAndClear(BinomialMap m) {
	Util.assert(m.c.equals(this.c));
	putAll(m.head);
	m.head = null; // empty out source map.
    }
    /** Union operation.  The specified node is the head of a binomial map. */
    void putAll(Node n) { // the binomial-heap-union operation.
	Util.assert(isHeapOrdered(head, c) && isHeapOrdered(n, c));
	this.head = _merge(this.head, n);
	if (this.head==null) return; // hmm.  both source maps were empty.
	
	Node prevx = null;
	Node x = this.head;
	for (Node nextx = x.sibling; nextx!=null; nextx=x.sibling) {
	    if ((x.degree != nextx.degree) ||
		((nextx.sibling!=null) && (nextx.sibling.degree==x.degree))) {
		prevx=x;
		x=nextx;
	    } else if (c.compare(x.key, nextx.key) <= 0) {
		x.sibling = nextx.sibling;
		_link(nextx, x);
	    } else {
		if (prevx==null)
		    this.head = nextx;
		else
		    prevx.sibling = nextx;
		_link(x, nextx);
		x = nextx;
	    }
	}
	Util.assert(isHeapOrdered(this.head, c));
    }

    /** Associates the specified value with the specified key in the map.
     *  If the map previously contained a mapping for this key, the old
     *  value is <b>not replaced</b>; both mappings will be present after
     *  the <code>put()</code>.  O(lg n) time.
     */
    public Object put(Object key, Object value) { // binomial-heap-insert
	Util.assert(isHeapOrdered(head, c));
	putAll(new Node(key, value));
	Util.assert(isHeapOrdered(head, c));
	return null;
    }
    /** Remove an return the map entry with minimal key. O(lg n) time. */
    public Map.Entry extractMinimum() {
	Util.assert(isHeapOrdered(head, c));
	Node x=(Node)minimum(); // find min node...
	// ..and remove it.
	_removeRoot(x);
	Util.assert(isHeapOrdered(head, c));
	// return minimum entry
	return x;
    }
    /** Remove a tree root from the binomial heap. O(lg n) time. */
    private void _removeRoot(Node x) {
	Util.assert(x.parent==null); // x is a root node.
	// remove from linked list of binomial tree roots.
	if (this.head==x) this.head=x.sibling;
	else for (Node y=this.head; y!=null; y=y.sibling)
	    if (y.sibling==x) { y.sibling=x.sibling; break; }
	Util.assert(isHeapOrdered(head, c));
	// reverse linked list of children of x.
	Node hprime=_reverse(null, x.child);
	Util.assert(isHeapOrdered(hprime, c));
	// union this and hprime.
	putAll(hprime);
	Util.assert(isHeapOrdered(head, c));
    }
    /** Reverse a linked list of siblings. */
    private Node _reverse(Node prev, Node n) {
	if (n==null) return prev;
	Node r = _reverse(n, n.sibling);
	n.parent = null;
	n.sibling = prev;
	return r;
    }

    /** Replace the key in the specified map entry with the specified
     *  <b>smaller</b> key.  O(lg n) time. */
    public void decreaseKey(Map.Entry me, Object newkey) {
	Util.assert(isHeapOrdered(head, c));
	Node x = (Node) me;
	if (c.compare(newkey, x.key) > 0)
	    throw new UnsupportedOperationException("New key is greater than "+
						    "current key.");
	x.key = newkey;
	_bubbleUp(x, false);
	// done.
	Util.assert(isHeapOrdered(head, c));
    }
    private Node _bubbleUp(final Node x, boolean delete) {
	Node y = x;
	Node z = y.parent;
	while ((z!=null) && (delete || (c.compare(y.key, z.key)<0))) {
	    // exchange fields of y and z.
	    Object yk = y.key, yv = y.value;
	    y.key = z.key; y.value = z.value;
	    z.key = yk;    z.value = yv;
	    y = z;
	    z = y.parent;
	}
	return y;
    }
    /** Remove the specied map entry from the mapping. O(lg n) time. */
    public void removeEntry(Map.Entry me) {
	Util.assert(isHeapOrdered(head, c));
	Node x = (Node) me;
	Node y = _bubbleUp(x, true);
	// y is now root node to be removed.
	_removeRoot(y);
	Util.assert(isHeapOrdered(head, c));
    }

    /** Return the next node after the specified node in the iteration order.
     *  O(1) time. */
    Node successor(Node n) {
	if (n==null) return null;
	if (n.child!=null) return n.child;
	if (n.parent==null) return n.sibling; // first root only.
	return n.parent.sibling;
    }
    /** Return an unmodifiable set of entries in this mapping.
     *  Note that the returned object may actually be a <b>Collection,
     *  not a Set</b> because the <code>BinomialMap</code> doesn't prohibit
     *  duplicates. */
    public Set entrySet() {
	Util.assert(isHeapOrdered(head, c));
	return new AbstractSet() {
	    public int size() { return BinomialMap.this.size(); }
	    public Iterator iterator() {
		return new UnmodifiableIterator() {
		    Node next = head;
		    public boolean hasNext() { return next!=null; }
		    public Object next() {
			Node n=next; next = successor(next); return n;
		    }
		};
	    }
	};
    }
    /** Returns the size of this map. O(lg n) time. */
    public int size() {
	Util.assert(isHeapOrdered(head, c));
	int s=0;
	for (Node nx=head; nx!=null; nx=nx.sibling)
	    s+=(1<<nx.degree);
	return s;
    }
    /** Removes all mappings from this map. O(1) time. */
    public void clear() {
	this.head=null;
    }

    /** Creates a new BinomialMap with all the key-value pairs this one
     *  has.  O(n) time. */
    public Object clone() {
	BinomialMap bm=new BinomialMap(this.c);
	bm.head=_clone(null, this.head);
	Util.assert(isHeapOrdered(head, c));
	Util.assert(isHeapOrdered(bm.head, bm.c));
	return bm;
    }
    /** Recursively clone a node. */
    private Node _clone(Node parent, Node n) {
	if (n==null) return null;
	Node nn = new Node(n.key, n.value);
	nn.degree = n.degree;
	nn.parent = parent;
	nn.child = _clone(nn, n.child);
	nn.sibling=_clone(parent, n.sibling);
	return nn;
    }

    /** The underlying node representation for the binomial heap */
    static class Node extends AbstractMapEntry {
	Node parent;
	public Object key;
	public Object value;
	int degree;
	Node child, sibling; // left child, right sibling.
	/*-----------------------------*/
	Node(Object key, Object value) {
	    this.key = key; this.value = value; this.degree=0;
	}
	public Object getKey() { return key; }
	public Object getValue() { return value; }
	public Object setValue(Object value) {
	    Object old = this.value;
	    this.value = value;
	    return old;
	}
	public String toString() {
	    return "<"+super.toString()+", "+
		"degree: "+degree+", "+
		"parent key: "
		+((parent!=null)?parent.key.toString():"(nil)")+", "+
		"child key: "
		+((child!=null)?child.key.toString():"(nil)")+", "+
		"sibling key: "
		+((sibling!=null)?sibling.key.toString():"(nil)")+">";
	}
    }
    /*-- debugging functions --*/
    private static boolean isTreeOrdered(Node n, Comparator c) {
	if (!debug) return true; // skip costly test if not debugging.
	if (n.parent==null) // special rules for root.
	    return 
		(n.sibling==null || n.sibling.parent==null) &&
		(n.child==null || 
		 (isTreeOrdered(n.child, c)) &&
		 (n.degree==n.child.degree+1) &&
		 (n==n.child.parent)) &&
		((n.child==null) == (n.degree==0));
	// rules for non-root nodes.
	if (! (c.compare(n.key, n.parent.key) >= 0)) return false;
	if (n.sibling!=null) {
	    if (! isTreeOrdered(n.sibling, c)) return false;
	    if (! (n.degree==n.sibling.degree+1)) return false;
	    if (! (n.parent==n.sibling.parent)) return false;
	}
	if (n.child!=null) {
	    if (! isTreeOrdered(n.child, c)) return false;
	    if (! (n.degree==n.child.degree+1)) return false;
	    if (! (n==n.child.parent)) return false;
	}
	return (n.degree==0)
	    ?((n.child==null) && (n.sibling==null))
	    :((n.child!=null) && (n.sibling!=null));
    }
    private static boolean isHeapOrdered(Node n, Comparator c) {
	if (!debug) return true; // skip costly test if not debugging.
	if (n==null) return true;
	return (n.parent==null) && // all top-level trees
	    isTreeOrdered(n, c) && // each tree in set is well-formed
	    ((n.sibling==null) ||  // either left-most, or
	     ((n.degree < n.sibling.degree) && // strictly increasing
	      isHeapOrdered(n.sibling, c)));   // and sibling well-formed.
    };
    /** Self-test function. */
    public static void main(String argv[]) {
	Comparator ic = new Comparator() { // integer comparator.
	    public int compare(Object o1, Object o2) {
		return ((Integer)o1).intValue()-((Integer)o2).intValue();
	    }
	};
	BinomialMap bm1 = new BinomialMap(ic);
	BinomialMap bm2 = new BinomialMap(ic);
	// construct heap on page 404 of CLR
	int ia[] = new int[] {29, 6, 14, 38, 17, 8, 11, 27, 1, 25, 12, 18, 10};
	for (int i=0; i<ia.length; i++)
	    bm1.put(new Integer(ia[i]), null);
	System.out.println(bm1.keySet());
	System.out.println(bm1.minimum());
	System.out.println("----");
	bm1.clear();

	// union example on pages 410-411 of CLR
	ia=new int[] { 15, 33, 28, 41, 7, 25, 12 };
	for (int i=0; i<ia.length; i++)
	    bm1.put(new Integer(ia[i]), null);
	ia=new int[] { 6, 44, 10, 17, 29, 31, 48, 50, 8, 22, 23, 24, 30, 32,
		       45, 55, 3, 37, 18 };
	for (int i=0; i<ia.length; i++)
	    bm2.put(new Integer(ia[i]), null);

	System.out.println(bm1.keySet());
	System.out.println(bm2.keySet());
	bm1.putAllAndClear(bm2);
	System.out.println(bm1.keySet());
	System.out.println(bm1.minimum());
	System.out.println("----");
	bm2 = new BinomialMap(bm1);

	// print sorted using extractMinimum()
	bm1.put(new Integer(3), null); // duplicate key.
	Util.assert(bm1.size()==27);
	while (bm1.size()>0)
	    System.out.print(bm1.extractMinimum().getKey().toString()+" ");
	System.out.println();
	Util.assert(bm1.size()==0);
	System.out.println("----");
	bm1 = (BinomialMap) bm2.clone();

	// print sorted using minimum() and removeEntry()
	Iterator it=bm1.entrySet().iterator();
	it.next(); it.next(); it.next();
	bm1.removeEntry((Map.Entry) it.next());
	Util.assert(bm1.size()==25);

	while (bm1.size()>0) {
	    Map.Entry me = bm1.minimum();
	    bm1.removeEntry(me);
	    System.out.print(me.getKey().toString()+" ");
	}
	System.out.println();
    }
}
