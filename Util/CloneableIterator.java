// CloneableIterator.java, created Tue Apr 20 15:34:34 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Iterator;
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
  
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: CloneableIterator.java,v 1.1.2.1 1999-04-20 19:47:12 pnkfelix Exp $ */
public class CloneableIterator implements Iterator, Cloneable {
    class LispCell { 
	final Object car; 
	LispCell cdr;
	LispCell(Object o, LispCell l)  {
	    car = o; 
	    cdr = l; 
	}
    }
    
    // Invariant: if 'currentCell' == null then there are no more
    //            elements to be iterated through.
    LispCell currentCell;
    final Iterator iter;	
    
    /** Creates a <code>CloneableIterator</code> using
	<code>iter</code> as its source. */ 
    public CloneableIterator(Iterator iter) {
	this.iter = iter;
	if (iter.hasNext()) currentCell = new LispCell(iter.next(), null);
	else currentCell = null;
    }

    public boolean hasNext() { 
	return (currentCell != null); 
    }
    
    public Object next() {
	if (currentCell != null) {
	    LispCell rtn = currentCell;
	    if (currentCell.cdr == null &&
		iter.hasNext()) { // maintain invariant
		currentCell.cdr = new LispCell(iter.next(), null);
	    } 
	    currentCell = currentCell.cdr;
	    return rtn.car;
	} else {
	    throw new NoSuchElementException();
	}
    }
    
    public void remove() { throw new UnsupportedOperationException(); }
    
    public Object clone() { 
	try { 
	    return super.clone(); 
	} catch (CloneNotSupportedException e) {
	    Util.assert(false, "Object should always be cloneable");
	    return null;
	}
    }
}

