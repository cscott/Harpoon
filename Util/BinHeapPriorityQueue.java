// BinHeapPriorityQueue.java, created Tue Jun  1 15:08:45 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Vector;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

/**
 * <code>BinHeapPriorityQueue</code> is an implementation of the
 * <code>PriorityQueue</code> interface. It supports O(1) time
 * <code>peekMax</code> and O(lg n) time <code>insert</code> and 
 * <code>removeMax</code> operations, assuming that
 * <code>Vector</code> is implemented in a reasonable manner.  The
 * <code>remove</code> operation is probably slow however.
 *
 * Look into implementing a FibinocciHeap-based representation if
 * speed becomes an issue. 
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: BinHeapPriorityQueue.java,v 1.1.2.4 1999-06-16 02:35:02 cananian Exp $
 */
public class BinHeapPriorityQueue extends AbstractCollection implements MaxPriorityQueue {

    // heap: Vector of Objects, maintaining the invariants: 
    // 1. for each element 'o' at index 'i', the parent of 'o' is
    //    located at index ( (i+1)/2 - 1)
    // 2. for each element 'o', the parent of 'o' has a priority
    //    greater than or equal to the priority of 'o'.
    private Vector heap;

    // Priorities of Objects in 'heap' are stored in Integer objects
    // here. 
    private Vector priorities;

    
    /** Creates a <code>BinHeapPriorityQueue</code>. */
    public BinHeapPriorityQueue() {
        heap = new Vector();
	priorities = new Vector();
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

    private void _set(int index, Object item, Object priority) {
	heap.set(index, item);
	priorities.set(index, priority);
    }

    private void _heapify(int i) {
	Util.assert(i < heap.size(),
		    "heapify param out of bounds");
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

    public void insert(Object item, int priority) {
	heap.add(null); priorities.add(null);
	int i = heap.size()-1;
	while(i > 0 && ((Integer)priorities.get(_parent(i))).intValue() < priority) {
	    _set(i, heap.get(_parent(i)), priorities.get(_parent(i)));
	    i = _parent(i);
	}
	_set(i, item, new Integer(priority));
    }

    public Object peekMax() {
	return heap.get(0);
    }

    public Object deleteMax() {
	Util.assert(heap.size() > 0, "Heap Underflow");
	Object rtrn = heap.get(0);

	Object mov = heap.remove(heap.size() - 1);
	Integer pri = (Integer) priorities.remove(priorities.size() - 1);
	
	if (heap.size() == 0) {
	    // we just deleted last element
	} else {
	    Util.assert(heap.size() == priorities.size(),
			"Why are the two Vectors' sizes in the BinHeap unequal?");
	    _set(0, mov, pri);
	    _heapify(0);
	}
	return rtrn;
    }

    public void clear() {
	heap = new Vector();
    }
    
    public boolean contains(Object o) {
	return heap.contains(o);
    }

    public boolean containsAll(Collection c) {
	return heap.containsAll(c);
    }

    public boolean equals(Object o) {
	if (o instanceof BinHeapPriorityQueue) {
	    BinHeapPriorityQueue bhpq = (BinHeapPriorityQueue) o;
	    return 
		bhpq.heap.equals(this.heap) &&
		bhpq.priorities.equals(this.priorities);
	} else {
	    return false;
	}
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
	return new UnmodifiableIterator() {
	    final Iterator inner = heap.iterator();
	    public boolean hasNext() { return inner.hasNext();} 
	    public Object next() {return inner.next();}
	};
    }

    public boolean remove(Object o) {
	int index = heap.indexOf(o);
	if (index >= 0) {
	    heap.remove(index);
	    priorities.remove(index);
	    return true;
	} else {
	    return false;
	}
    }

    public boolean removeAll(Collection c) {
	Iterator iter = c.iterator();
	boolean altered = false;
	while(iter.hasNext()) {
	    if(remove(iter.next()))
		altered = true;
	}
	return altered;
    }

    /** Throws UnsupportedOperationException. */
    public boolean retainAll(Collection c) {
	// don't know a good way to implement this yet.
	throw new UnsupportedOperationException();
    }

    public int size() {
	return heap.size();
    }
}
