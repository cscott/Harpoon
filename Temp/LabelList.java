// LabelList.java, created Tue Jul 28  1:09:44 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Temp;

import java.util.List;
import java.util.AbstractList;

/**
   A <code>LabelList</code> is a simple singly-linked list of
   <code>Label</code>s.

   @deprecated Scott says so.  Use a real <code>java.util.List</code> instead.
   @author C. Scott Ananian <cananian@alumni.princeton.edu>
   @version $Id: LabelList.java,v 1.3.2.6 1999-08-27 23:27:02 pnkfelix Exp $
 */
public final class LabelList {
    /* The head of the list. */
    public final Label head;
    /* The tail of the list. */
    public final LabelList tail;
    /* Constructor. */
    public LabelList(Label head, LabelList tail){ 
	this.head=head; this.tail=tail; 
    }
    
    /** Converts a <code>LabelList</code> to a <code>java.util.List</code>. 
	Accepts <code>null</code> as an argument (which will
	effectively return an empty list). 
    */
    public static List toList(final LabelList l) {
	return new AbstractList() {
	    public Object get(int index) {
		LabelList ll = l;
		while(index != 0) {
		    try {
			ll = ll.tail;
		    } catch (NullPointerException e) {
			throw new IndexOutOfBoundsException();
		    }
		    index--;
		}
		return ll.head;
	    }
	    public int size() {
		int i = 0;
		LabelList ll = l;
		while(ll != null) { 
		    i++; ll = ll.tail; 
		}
		return i;
	    }
	};
    }
}

