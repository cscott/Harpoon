// LLDisjointSetStructure.java, created Mon Jan 10 13:48:49 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.Util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * <code>LLDisjointSetStructure</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LLDisjointSetStructure.java,v 1.1.2.6 2001-06-17 22:36:32 cananian Exp $
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

	// True if 'this' is a representative for a set.  else False.
	boolean isRep; 

	/** Constructs a new Linked-List based set element, 
	    setting its fields to the defaults.
	    Note that <code>Elem</code>s are automatically
	    representatives upon construction (for the singleton set 
	    { o } ), so 'last', 'size', and 'isRep' will be set
	    accordingly.
	*/
	LLElem(Object o) {
	    super(o);
	    size = 1;
	    rep = this;
	    last = this;
	    next = null;
	    isRep = true;
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
	Util.assert(x.isRep, ex+" must be a set representative");
	Util.assert(y.isRep, ey+" must be a set representative");

	// set Elems to non-representatives
	x.isRep = false; 
	y.isRep = false;

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

    public Elem findSet(final Elem elem) { 
	LLElem rep = ((LLElem)elem).rep;
	Util.assert(rep.isRep, 
		    "Elem "+elem+" shouldn't think "+rep+
		    "is its set representaive");
	return rep;
    }

    public Set setView(final Elem o) { 
	LLElem rep = (LLElem) o;
	Util.assert(rep.isRep, o+
		    " must be a set-rerepresentative to "+
		    "call setView on it");
		    
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
