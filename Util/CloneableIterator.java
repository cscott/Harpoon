// CloneableIterator.java, created Tue Apr 20 15:34:34 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.LinkedList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
   <code>CloneableIterator</code> is a wrapper around
   <code>Iterator</code> that is safely <code>Cloneable</code>.
   Essentially <code>this</code> and all of the clones of
   <code>this</code> share the original <code>Iterator</code> and
   maintain a shared list of objects, each keeping a pointer into
   their current object in the shared list.  If <code>this</code> or
   one of its clones reaches the end of the shared list, it attempts
   to add more elements onto the end of the list by extracting them
   from the shared <code>Iterator</code>.
  
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CloneableIterator.java,v 1.2 2002-02-25 21:08:45 cananian Exp $ */
public class CloneableIterator implements ListIterator, Cloneable {
    int index;
    Object next;
    final Iterator iter;	
    final LinkedList list;
    
    /** Creates a <code>CloneableIterator</code> using
	<code>iter</code> as its source. */ 
    public CloneableIterator(Iterator iter) {
	if (iter instanceof CloneableIterator) {
	    CloneableIterator citer = (CloneableIterator) iter;
	    this.iter = citer.iter;
	    this.list = citer.list;
	    this.next = citer.next;
	    this.index = citer.index;
	} else {
	    this.iter = iter;
	    list = new LinkedList();
	    if (iter.hasNext()) {
		next = iter.next();
		list.add(next);
		index = 0;
	    } else {
		next = null;
		index = 0;
	    }
	}
    }

    public boolean hasNext() { 
	return (next != null);
    }
    
    public Object next() {
	if (next != null) {
	    index++;
	    Object rtn = next;
	    if (index < list.size()) {
		next = list.get(index);
	    } else if (iter.hasNext()) {
		next = iter.next();
		list.add(next);
	    } else {
		next = null;
	    }
	    return rtn;
	} else {
	    throw new NoSuchElementException();
	}
    }

    public int nextIndex() {
	return index;
    }
    
    public boolean hasPrevious() {
	return index > 0;
    }

    public Object previous() {
	if (index > 0) {
	    index--;
	    return list.get(index);
	} else {
	    throw new NoSuchElementException();
	}
    }
    
    public int previousIndex() {
	return index - 1;
    }
    
    public Object clone() { 
	try { 
	    return super.clone(); 
	} catch (CloneNotSupportedException e) {
	    Util.assert(false, "Object should always be cloneable");
	    return null;
	}
    }

    public void remove() { throw new UnsupportedOperationException(); }
    public void add(Object o) { throw new UnsupportedOperationException(); }
    public void set(Object o) { throw new UnsupportedOperationException(); }

}

