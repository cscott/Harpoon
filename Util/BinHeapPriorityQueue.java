// BinHeapPriorityQueue.java, created Tue Jun  1 15:08:45 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.ArrayList;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * <code>BinHeapPriorityQueue</code> is an implementation of the
 * <code>PriorityQueue</code> interface. It supports O(1) time
 * <code>peekMax</code> and O(lg n) time <code>insert</code> and 
 * <code>removeMax</code> operations, assuming that
 * <code>ArrayList</code> is implemented in a reasonable manner.  The
 * <code>remove</code> operation is probably slow however.
 *
 * Look into implementing a FibinocciHeap-based representation if
 * speed becomes an issue. 
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: BinHeapPriorityQueue.java,v 1.3.2.1 2002-02-27 08:37:43 cananian Exp $
 */
public class BinHeapPriorityQueue extends AbstractCollection implements MaxPriorityQueue {

    // heap: List of Objects, maintaining the invariants: 
    // 1. for each element 'o' at index 'i', the parent of 'o' is
    //    located at index ( (i+1)/2 - 1)
    // 2. for each element 'o', the parent of 'o' has a priority
    //    greater than or equal to the priority of 'o'.
    private List heap;

    // Priorities of Objects in 'heap' are stored in Integer objects
    // here. 
    private List priorities;

    
    /** Creates a <code>BinHeapPriorityQueue</code>. */
    public BinHeapPriorityQueue() {
        heap = new ArrayList();
	priorities = new ArrayList();
    }
    
    private int _parent(int i) { return (i+1)/2 - 1; }
    private int _left(int i) { return 2*(i+1) - 1; }
    private int _right(int i) { return 2*(i+1) ; }

    private void _add(Object item, Integer priority) {
	heap.add(item);
	priorities.add(priority);
    }

    private void _swap(int index1, int index2) {
	Object one = heap.get(index1);
	Object two = heap.get(index2);
	Integer p1 = (Integer) priorities.get(index1);
	Integer p2 = (Integer) priorities.get(index2);
	heap.set(index1, two);
	priorities.set(index1, p2);
	heap.set(index2, one);
	priorities.set(index2, p1);
    }

    private void _set(int index, Object item, Integer priority) {
	heap.set(index, item);
	priorities.set(index, priority);
    }

    private void _heapify(int i) {
	assert i < heap.size() : "heapify param "+i+" out of bounds "+heap.size();
	int l = _left(i);
	int r = _right(i);
	int largest;
	if (l < priorities.size() && 
	    ((Integer)priorities.get(l)).intValue() >
	    ((Integer)priorities.get(i)).intValue()) {
	    largest = l;
	} else {
	    largest = i;
	}
	if (r < priorities.size() && 
	    ((Integer)priorities.get(r)).intValue() >
	    ((Integer)priorities.get(largest)).intValue()) {
	    largest = r;
	}
	if (largest != i) {
	    _swap(i, largest);
	    _heapify(largest);
	}
    }
    private void _remove(int index) {
	// replace element to remove with smallest element.
	int sizem1 = size()-1;
	heap.set(index, heap.get(sizem1));
	priorities.set(index, priorities.get(sizem1));
	heap.remove(sizem1);
	priorities.remove(sizem1);
	// now heapify to restore heap condition.
	if (index<sizem1) _heapify(index); //FSK:not sure if this is correct
    }

    public void insert(Object item, int priority) {
	heap.add(null); priorities.add(null);
	int i = heap.size()-1;
	while(i > 0 && ((Integer)priorities.get(_parent(i))).intValue() < priority) {
	    _set(i, heap.get(_parent(i)), (Integer)priorities.get(_parent(i)));
	    i = _parent(i);
	}
	_set(i, item, new Integer(priority));
    }

    public Object peekMax() {
	return heap.get(0);
    }

    public Object deleteMax() {
	assert heap.size() > 0 : "Heap Underflow";
	Object rtrn = heap.get(0);

	Object mov = heap.remove(heap.size() - 1);
	Integer pri = (Integer) priorities.remove(priorities.size() - 1);
	
	if (heap.size() == 0) {
	    // we just deleted last element
	} else {
	    assert heap.size() == priorities.size() : "Why are the two Lists' sizes in the BinHeap unequal?";
	    _set(0, mov, pri);
	    _heapify(0);
	}
	return rtrn;
    }

    public void clear() {
	heap.clear();
	priorities.clear();
    }
    
    /** This is slow. */
    public boolean contains(Object o) {
	return heap.contains(o);
    }

    public boolean equals(Object o) {
	BinHeapPriorityQueue bhpq;
	if (this==o) return true;  // common case
	if (null==o) return false; // uncommon case
	try {
	    bhpq = (BinHeapPriorityQueue) o;
	} catch (ClassCastException e) {
	    return false;
	}
	return 
	    bhpq.heap.equals(this.heap) &&
	    bhpq.priorities.equals(this.priorities);
    }

    public int hashCode() {
	return heap.hashCode() ^ priorities.hashCode();
    }

    public boolean isEmpty() {
	return heap.isEmpty();
    }

    /** Returns the elements of <code>this</code> in no specific
	order. 
	<BR> <B>effects:</B> Creates a new <code>Iterator</code> which
	     returns the elements of <code>this</code>
	<BR> <B>requires:</B> <code>this</code> is not modified while
	     the returned <code>Iterator</code> is in use.
    */
    public Iterator iterator() {
	// return a wrapping iterator, to ensure that the state of
	// 'priorities' is kept consistent
	return Default.unmodifiableIterator(heap.iterator());
    }

    /** This is slow. */
    public boolean remove(Object o) {
	int index = heap.indexOf(o); // this is the slow bit.
	if (index<0) return false;
	_remove(index);
	return true;
    }

    /* Use default implementation from AbstractCollection for
     * containsAll(), removeAll() and retainAll() */

    public int size() {
	return heap.size();
    }
}
