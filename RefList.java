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
	    RefCountArea ref = RefList.this.ref;
	    if (obj.memoryArea != ref) {
		throw new RuntimeException("Invalid memoryArea for RefList object");
	    }
	    if (next != null) ref.INCREF(next); 
	}
    }
    
    Elt elt = null;
    
    public RefList() {
	if (Math.sqrt(4)==0) {
	    new Elt(null, null);
	}
    }

    public void add(Object o) {
	try {
	    elt = (Elt)ref.newInstance(Elt.class, 
				       new Class[] {Object.class, Elt.class},
				       new Object[] {o, elt});
	} catch (IllegalAccessException e) {
	    throw new RuntimeException(e+" This can't happen!");
	} catch (InstantiationException e) {
	    throw new RuntimeException(e+" This can't happen!");
	}
    }

    public void add(final long l) {
	RefList.this.ref.enter(new Runnable() {
	    public void run() {
		add(new Long(l));
	    }
	});
    }

    public void remove(final Object o) {
	(new VTMemory()).enter(new Runnable() {
	    public void run() {
		Iterator it = iterator();
		try {
		    while (true) {
			Object obj = it.next();
			if (o.equals(obj)) {
			    it.remove();
			    break;
			}
		    }
		} catch (NoSuchElementException e) {
		    return;
		}
	    }
	});
    }
    
    public void remove(final long lo) {
	(new VTMemory()).enter(new Runnable() {
	    public void run() {
		Long l = new Long(lo);
		remove(l);
	    }
	});
    }

    public boolean isEmpty() {
	return elt == null;
    }

    public Iterator iterator() {
	class RefListIterator implements Iterator {
	    private Elt prev;
	    private Elt curr;
	    
	    public RefListIterator() {
		if ((prev = curr = elt) != null) {
		    RefList.this.ref.INCREF(curr);
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
		    ref.DECREF(curr);
		    curr = newElt;
		    return next();
		}
		Elt newElt = curr.next;
		if (newElt != null) {
		    ref.INCREF(newElt);
		}
		Object obj = curr.obj;
		ref.DECREF(prev = curr);
		curr = newElt;
		return obj;
	    }

	    public void remove() {
		if (curr == null) {
		    throw new NoSuchElementException();
		}
		curr.beingRemoved = true;
		if (prev == curr) {
		    if (elt != prev) {
			throw new RuntimeException("Inconsistent state!");
		    }
		    curr = elt = elt.next;
		    ref.DECREF(prev);
		    prev = curr;
		} else {
		    prev.next = curr.next;
		    ref.DECREF(curr);
		    ref.DECREF(curr);
		    curr = prev.next;
		}		
	    }
	}
	return new RefListIterator();
    }
}

