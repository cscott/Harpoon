// RefList.java, created by wbeebee
// Copyright (C) 2002 Wes Beebee <wbeebee@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package javax.realtime;

import java.util.Iterator;
import java.util.NoSuchElementException;

/** The goal here is to make a thread list that's accessable from a NoHeapRealtimeThread that keeps
 *  all the relevant information about the threads accessable to the scheduler.
 *  Therefore, all memory allocations/deallocations must be made explicit.
 *  RefCountAreas are explicitly unsafe currently - an interesting research project to make them safe.
 */
class RefList {
    final RefCountArea ref = RefCountArea.refInstance();

    class Elt {
	Object obj;
	Elt next;
	boolean beingRemoved = false;

	/** <code>Obj</code> is assumed to have refCount == 1.
	 *  It's not kosher to share between lists.
	 *  Once an object is removed from the list, it is DEAD!
	 */

        Elt(Object obj, Elt next) {
	    if (next != null) RefList.this.ref.INCREF(next); 
	    this.obj = obj;
	    this.next = next;
	}
    }
    
    Elt elt = null;

    public RefList() {
    }

    public void add(final Object o) {
	ref.enter(new Runnable() { // These Runnables cause a memory leak...
	    public void run() {
		elt = new Elt(o, elt);
	    }
	});
    }

    public void add(final long l) {
	ref.enter(new Runnable() {
	    public void run() {
		elt = new Elt(new Long(l), elt);
	    }
	});
    }

    public void remove(final Object o) {
	Elt prev = elt;
	for (Elt e = elt; e != null; prev = e, e = e.next) {
	    if (o.equals(e.obj)) {
		if (prev == elt) {
		    elt = elt.next;
//		    ref.DECREF(prev);
		} else {
		    prev.next = elt.next;
//		    ref.DECREF(elt);
//		    ref.DECREF(elt);
		}
		break;
	    }
	}
    }
    
    public void remove(final long lo) {
	Elt prev = elt;
	for (Elt e = elt; e != null; prev = e, e = e.next) {
	    if ((e.obj instanceof Long)&&(((Long)e.obj).longValue()==lo)) {
		if (e == elt) {
		    elt = elt.next;
//		    ref.DECREF(prev);
		} else {
		    prev.next = e.next;
//		    ref.DECREF(elt);
//		    ref.DECREF(elt);
		}
		break;
	    }
	}
    }

    public boolean contains(final Object o) {
	for (Elt e = elt; e != null; e = e.next)
	    if (o.equals(e.obj)) return true;
	return false;
    }

    public boolean contains(final long lo) {
	for (Elt e = elt; e != null; e = e.next)
	    if ((e.obj instanceof Long)&&(((Long)e.obj).longValue()==lo)) return true;
	return false;
    }

    public boolean isEmpty() {
	return elt == null;
    }

    public long length() {
	int i = 0;
	for (Elt e = elt; e != null; e = e.next) i++;
	return i;
    }

    public Iterator iterator() {
	class RefListIterator implements Iterator {
	    private Elt prev;
	    private Elt curr;
	    
	    public RefListIterator() {
		if ((prev = curr = elt) != null) {
		    ref.INCREF(curr);
		}
	    }

	    /** Warning: not thread safe under mutation! */
	    public boolean hasNext() {  
		return curr != null;
	    }
	    
	    public Object next() throws NoSuchElementException {
		RefCountArea ref = RefList.this.ref;
		if (curr == null) {
		    throw new NoSuchElementException();
		}
		if (curr.beingRemoved) {
		    Elt newElt = curr.next;
		    if (newElt != null) { 
			ref.INCREF(newElt);
		    }
//		    ref.DECREF(curr);
		    curr = newElt;
		    return next();
		}
		Elt newElt = curr.next;
		if (newElt != null) {
		    ref.INCREF(newElt);
		}
		Object obj = curr.obj;
//		ref.DECREF(prev = curr);
		curr = newElt;
		return obj;
	    }

	    public void remove() {
		throw new UnsupportedOperationException("Not implemented!");
	    }
	}
	return new RefListIterator();
    }

    public Iterator roundIterator() {
	class RefListIterator implements Iterator {
	    private Elt prev;
	    private Elt curr;
	    
	    public RefListIterator() {
		if ((prev = curr = RefList.this.elt) != null) {
		    ref.INCREF(curr);
		}
	    }

	    /** Warning: not thread safe under mutation! */
	    public boolean hasNext() {  
		return !RefList.this.isEmpty();
	    }
	    
	    public Object next() throws NoSuchElementException {
		RefCountArea ref = RefList.this.ref;
		if (!hasNext()) {
		    throw new NoSuchElementException();
		}
		if (curr == null) {
		    if ((prev = curr = RefList.this.elt) != null) {
			ref.INCREF(curr);
		    }
		    return next();
		}
		if (curr.beingRemoved) {
		    Elt newElt = curr.next;
		    if (newElt != null) { 
			ref.INCREF(newElt);
		    }
//		    ref.DECREF(curr);
		    curr = newElt;
		    return next();
		}
		Elt newElt = curr.next;
		if (newElt != null) {
		    ref.INCREF(newElt);
		}
		Object obj = curr.obj;
//		ref.DECREF(prev = curr);
		curr = newElt;
		return obj;
	    }

	    public void remove() {
		throw new UnsupportedOperationException("Not implemented!");
	    }
	}
	return new RefListIterator();
    }

    public String toString() {
	String s = "[";
	try {
	    Iterator it = iterator();
	    while (true) s += it.next();
	} catch (NoSuchElementException e) {
	    return s+"]";
	}
    }

    public void printNoAlloc() {
	NoHeapRealtimeThread.print("[");
	for (Elt newElt = elt; newElt != null; newElt = newElt.next) {
	    NoHeapRealtimeThread.print(newElt.obj.toString());
	}
	NoHeapRealtimeThread.print("]");     
    }
}

