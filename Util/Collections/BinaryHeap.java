// BinaryHeap.java, created Sat Feb 12 10:04:53 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Default;
import harpoon.Util.Collections.PairMapEntry;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.Util;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
/**
 * <code>BinaryHeap</code> is an implementation of a binary heap.
 * The implementation in CLR is followed, except the comparisons
 * are reversed to keep the <b>minimum</b> element on the top of
 * the heap.  In addition, the function names <code>downheap()</code>
 * (for what CLR calls 'heapify') and <code>upheap()</code> (which
 * is part of the INSERT operation) have been adopted from
 * Sedgewick's book.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: BinaryHeap.java,v 1.4 2002-04-10 03:07:10 cananian Exp $
 * @see Heap
 */
public final class BinaryHeap<K,V> extends AbstractHeap<K,V> {
    private final boolean debug=false;

    final ArrayList<Entry<K,V>> A;
    final Comparator<Map.Entry<K,V>> c;
    
    /** Creates a new, empty <code>BinaryHeap</code>, which will
     *  use the keys' natural order. */
    public BinaryHeap() { this(Collections.EMPTY_SET, null); }
    /** Creates a new, empty <code>BinaryHeap</code> with the
     *  specified comparator. */
    public BinaryHeap(Comparator<K> c) { this(Collections.EMPTY_SET, c); }
    /** Builds a binary heap from the given heap, using
     *  the same key comparator as the given heap.  O(n) time. */
    public <V2 extends V> BinaryHeap(Heap<K,V2> h) {
	this(h.entries(), h.comparator());
    }
    /** Builds a binary heap from a collection of <code>Map.Entry</code>s
     *  and a key comparator.
     *  O(n) time. */
    public <K2 extends K, V2 extends V> BinaryHeap(Collection<Map.Entry<K2,V2>> collection, Comparator<K> comparator) {
	super(comparator);
	// initialize comparator
	c = entryComparator(); // cache in field.
	// initialize A
	A = new ArrayList<Entry<K,V>>(1+collection.size());
	A.add(null); /* element zero is reserved */
	// use BUILD-HEAP
	union(collection);
	if (debug) checkHeap();
    }
    public Map.Entry<K,V> insert(K key, V value) {
	int index=A.size();
	Entry<K,V> e=new Entry<K,V>(key, value, index);
	A.add(e);
	upheap(index);
	if (debug) checkHeap();
	return e;
    }
    public Map.Entry<K,V> minimum() {
	if (size() < 1) throw new java.util.NoSuchElementException();
	return A.get(1);
    }
    public Map.Entry<K,V> extractMinimum() {
	if (size() < 1) throw new java.util.NoSuchElementException();
	Entry min = A.get(1);
	set(1, A.get(size()));
	A.remove(size());
	downheap(1);
	if (debug) checkHeap();
	return min;
    }
    public <K2 extends K, V2 extends V> void union(Heap<K2,V2> h) {
	union(h.entries());
    }
    /** Union a collection of <code>Map.Entry</code>s, using BUILD-HEAP. */
    private <K2 extends K, V2 extends V> void union(Collection<Map.Entry<K2,V2>> coll) {
	// this is the BUILD-HEAP function. pg 145 in CLR.
	for (Iterator<Map.Entry<K2,V2>> it=coll.iterator(); it.hasNext(); ) {
	    Map.Entry<K2,V2> e = it.next();
	    A.add(new Entry(e.getKey(), e.getValue(), A.size()));
	}
	// okay, now heapify
	for (int i=size()/2; i>0; i--)
	    downheap(i);
	// done!
	if (debug) checkHeap();
    }
    public void decreaseKey(Map.Entry<K,V> me, K newkey) {
	updateKey(me, newkey);
    }
    public void updateKey(Map.Entry<K,V> me, K newkey) {
	Entry<K,V> e = (Entry<K,V>) me;
	if (keyComparator().compare(newkey, setKey(e, newkey)) < 0)
	    upheap(e.index);
	else
	    downheap(e.index);
	if (debug) checkHeap();
    }
    public void delete(Map.Entry<K,V> me) {
	int index = ((Entry<K,V>)me).index;
	// replace this entry with one at end of heap & shrink heap.
	Entry<K,V> newE = A.get(size());
	set(index, newE);
	A.remove(size());
	// now fixup the heap by calling either downheap or upheap.
	if (c.compare(newE, me) < 0)
	    upheap(index); // we've decreased the key
	else
	    downheap(index); // we've increased the key
	// done.
	if (debug) checkHeap();
    }
    public void clear() { A.clear(); A.add(null); }
    public int size() { return A.size()-1; /* 0'th element unused. */ }
    public Collection<Map.Entry<K,V>> entries() {
	return new AbstractCollection<Map.Entry<K,V>>() {
	    public int size() { return BinaryHeap.this.size(); }
	    public Iterator<Map.Entry<K,V>> iterator() {
		final Iterator<Entry<K,V>> it = A.iterator();
		it.next(); // filter out element 0 of the array list.
		// weakness of the type system: we need to wrap this
		// iterator to make it return Map.Entry instead of Entry.
		return new UnmodifiableIterator<Map.Entry<K,V>>() {
		    public boolean hasNext() { return it.hasNext(); }
		    public Map.Entry<K,V> next() { return it.next(); }
		};
	    }
	};
    }


    /** The guts of the binary heap algorithms: DOWNHEAP and UPHEAP */
    private final void downheap(int i) { // aka HEAPIFY
	int l = LEFT(i), r = RIGHT(i);
	int smallest = i;
	if (l<=size() && c.compare(A.get(l), A.get(smallest)) < 0)
	    smallest = l;
	if (r<=size() && c.compare(A.get(r), A.get(smallest)) < 0)
	    smallest = r;
	if (smallest != i) {
	    exchange(i, smallest);
	    downheap(smallest);
	}
    }
    private final void upheap(int i) {
	int p = PARENT(i);
	if (p>0 && i<=size() && c.compare(A.get(p), A.get(i)) > 0) {
	    exchange(p, i);
	    upheap(p);
	}
    }
    // exchange helper
    private final void exchange(int i, int j) {
	if (i==j) return; // efficiency hack.
	Entry<K,V> ei = A.get(i), ej = A.get(j);
	A.set(i, ej); ej.index = i;
	A.set(j, ei); ei.index = j;
    }
    // set helper
    private final void set(int i, Entry<K,V> e) {
	A.set(i, e); e.index = i;
    }
    // macros.  i sure hope the compiler is smart enough to inline these.
    private static final int LEFT(int i) { return 2*i; }
    private static final int RIGHT(int i) { return 2*i+1; }
    private static final int PARENT(int i) { return i/2; }

    // verify the heap condition.
    private final void checkHeap() {
	for (int i=2; i<A.size(); i++)
	    assert c.compare(A.get(PARENT(i)), A.get(i)) <= 0;
    }

    /** Our <code>BinaryHeap</code> <code>Map.Entry</code>s look like this: */
    private static class Entry<K,V> extends PairMapEntry<K,V> {
	int index;
	Entry(K key, V value, int index) {
	    super(key, value);
	    this.index = index;
	}
	K _setKey(K newKey) { return setKey(newKey); }
    }
    // to implement updateKey, etc...
    protected final K setKey(Map.Entry<K,V> me, K newkey) {
	Entry<K,V> e = (Entry<K,V>) me;
	return e._setKey(newkey);
    }

    //--------------------------------------------------
    /** Self-test function. */
    public static void main(String[] args) {
	{
	Heap<Integer,Integer> h = new BinaryHeap<Integer,Integer>();
	assert h.size()==0 && h.isEmpty();
	// example from CLR, page 146/151
	h = new BinaryHeap<Integer,Integer>(new AbstractCollection<Map.Entry<Integer,Integer>>() {
	    int el[] = { -4, -1, -3, -2, -16, -9, -10, -14, -8, -7 };
	    public int size() { return el.length; }
	    public Iterator<Map.Entry<Integer,Integer>> iterator() {
		return new harpoon.Util.UnmodifiableIterator<Map.Entry<Integer,Integer>>() {
		    int i = 0;
		    public boolean hasNext() { return i<el.length; }
		    public Map.Entry<Integer,Integer> next() {
			Integer io = new Integer(el[i++]);
			return new PairMapEntry(io, io);
		    }
		};
	    }
	}, null/* default comparator */);
	assert h.size()==10 && !h.isEmpty();
	assert h.minimum().getKey().equals(new Integer(-16));
	System.out.println(h);
	h.insert(new Integer(-15), new Integer(-15));
	assert h.size()==11 && !h.isEmpty();
	assert h.minimum().getKey().equals(new Integer(-16));
	System.out.println(h);
	// now verify that we'll get all the keys out in properly sorted order
	assert h.extractMinimum().getKey().equals(new Integer(-16));
	assert h.extractMinimum().getKey().equals(new Integer(-15));
	assert h.extractMinimum().getKey().equals(new Integer(-14));
	assert h.extractMinimum().getKey().equals(new Integer(-10));
	assert h.extractMinimum().getKey().equals(new Integer(-9));
	assert h.extractMinimum().getKey().equals(new Integer(-8));
	assert h.extractMinimum().getKey().equals(new Integer(-7));
	assert h.extractMinimum().getKey().equals(new Integer(-4));
	assert h.extractMinimum().getKey().equals(new Integer(-3));
	assert h.extractMinimum().getKey().equals(new Integer(-2));
	assert h.extractMinimum().getKey().equals(new Integer(-1));
	assert h.isEmpty() && h.size()==0;
	// okay, test delete and decreaseKey now.
	h.clear(); assert h.isEmpty() && h.size()==0;
	}{
	Heap<String,String> h = new BinaryHeap<String,String>();
	assert h.size()==0 && h.isEmpty();
	Map.Entry<String,String> me[] = {
	    h.insert("C", "c1"), h.insert("S", "s1"), h.insert("A", "a"),
	    h.insert("S", "s2"), h.insert("C", "c2"), h.insert("O", "o"),
	    h.insert("T", "t1"), h.insert("T", "t2"), h.insert("Z", "z"),
	    h.insert("M", "m"),
	};
	assert h.extractMinimum().getValue().equals("a");
	System.out.println(h);
	h.decreaseKey(me[3], "B"); // s2
	assert h.extractMinimum().getValue().equals("s2");
	h.delete(me[4]); // c2
	assert h.extractMinimum().getValue().equals("c1");
	System.out.println(h);
	// finally, test updateKey
	h.updateKey(me[9], "P"); // m
	assert h.extractMinimum().getValue().equals("o");
	assert h.extractMinimum().getValue().equals("m");
	System.out.println(h);
	// DONE.
	System.out.println("PASSED.");
	}
    }
}
