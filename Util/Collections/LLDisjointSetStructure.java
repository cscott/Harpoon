// LLDisjointSetStructure.java, created Mon Jan 10 13:48:49 2000 by pnkfelix
// Copyright (C) 1999 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.UnmodifiableIterator;

import java.util.*;

/**
 * <code>LLDisjointSetStructure</code>
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: LLDisjointSetStructure.java,v 1.1.2.2 2000-01-13 19:01:19 pnkfelix Exp $
 */
public class LLDisjointSetStructure extends DisjointSetStructure {

    class LLElem extends DisjointSetStructure.Elem {
	LLElem next;
	LLElem rep;

	// these two fields are only strictly defined when 'this' 
	// is the representative for a set.  'last' is the last elem
	// of the list, 'size' is the number of elements in the list.
	LLElem last; 
	int size;

	/** Constructs a new Linked-List based set element, 
	    setting its fields to the defaults.
	*/
	LLElem(Object o) {
	    super(o);
	    size = 1;
	    rep = this;
	    last = this;
	    next = null;
	}
    }

    public Elem makeSet(Object o) { 
	return new LLElem(o);
    }

    public Elem union(final Elem ex, final Elem ey) { 
	final LLElem x, y, start;
	LLElem append;
	x = (LLElem) ex;
	y = (LLElem) ey;
	if (x.size >= y.size) {
	    start = x;
	    append = y;
	} else {
	    start = y;
	    append = x;
	}

	start.size += append.size;
	start.last.next = append;
	start.last = append.last;
	while(append != null) {
	    append.rep = start;
	    append = append.next;
	}
	
	return start; 
    }

    public Elem findSet(Elem elem) { 
	return ((LLElem)elem).rep;
    }

    public Set setView(final Elem o) { 
	return new AbstractSet() {
	    public Iterator iterator() {
		return new UnmodifiableIterator() {
		    LLElem curr = (LLElem)o;
		    public boolean hasNext() {
			return (curr!=null);
		    }
		    public Object next() {
			if (curr==null) {
			    throw new
				java.util.
				NoSuchElementException();
			} else {
			    Object obj = curr.member;
			    curr = curr.next;
			    return obj;
			}
		    }
		};
	    }
	    public int size() {
		return ((LLElem)o).size;
	    }
	};
    }

}
