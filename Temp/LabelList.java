// LabelList.java, created Tue Jul 28  1:09:44 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Temp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
   A <code>LabelList</code> is a simple singly-linked list of
   <code>Label</code>s.

   @deprecated Scott says so.  Use a real <code>java.util.List</code> instead.
   @author C. Scott Ananian <cananian@alumni.princeton.edu>
   @version $Id: LabelList.java,v 1.4 2002-02-25 21:07:05 cananian Exp $
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
	return an empty list). 
    */
    public static List toList(final LabelList l) {
	if (l==null) return Collections.EMPTY_LIST; // optimization.
	List al = new ArrayList();
	for (LabelList ll=l; ll!=null; ll=ll.tail)
	    al.add(ll.head);
	return Collections.unmodifiableList(al);
    }
}

