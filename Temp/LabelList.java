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
   @version $Id: LabelList.java,v 1.5 2004-02-08 01:59:46 cananian Exp $
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
    public static List<Label> toList(final LabelList l) {
	if (l==null) return (List<Label>) Collections.EMPTY_LIST; // optimization.
	ArrayList<Label> al = new ArrayList<Label>();
	for (LabelList ll=l; ll!=null; ll=ll.tail)
	    al.add(ll.head);
	al.trimToSize();
	return Collections.unmodifiableList(al);
    }
}

