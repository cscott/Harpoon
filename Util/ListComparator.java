// ListComparator.java, created Wed Feb 24 15:33:11 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
/**
 * A <code>ListComparator</code> compares two lists element-by-element.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ListComparator.java,v 1.2 2002-02-25 21:08:45 cananian Exp $
 */
public class ListComparator implements Comparator {
    final boolean cmpForwards;
    final Comparator elementComparator;
    
    /** Creates a <code>ListComparator</code> which compares
     *  elements first-to-last.  All elements of the list must
     *  implement <code>java.lang.Comparable</code>. */
    public ListComparator() {
        this.cmpForwards = true;
	this.elementComparator=null;
    }
    /** Creates a <code>ListComparator</code>.  If <code>cmpForwards</code>
     *  is <code>true</code>, compares elements first-to-last, otherwise
     *  compares them last-to-first.  If <code>elementComparator</code>
     *  is <code>null</code>, then all elements of the list must
     *  implement <code>java.lang.Comparable</code> and the
     *  <code>ListComparator</code> uses their natural ordering.  Otherwise,
     *  it uses the supplied <code>Comparator</code> to compare elements.
     */
    public ListComparator(boolean cmpForwards, Comparator elementComparator) {
	this.cmpForwards = cmpForwards;
	this.elementComparator=elementComparator;
    }

    public int compare(Object o1, Object o2) {
	// throws ClassCastException if objects are not the proper types.
	List l1 = (List) o1, l2 = (List) o2;
	ListIterator li1 = l1.listIterator(cmpForwards?0:l1.size());
	ListIterator li2 = l2.listIterator(cmpForwards?0:l2.size());
	if (cmpForwards) {
	    while (li1.hasNext() && li2.hasNext()) {
		int cmp = compareElement(li1.next(), li2.next());
		if (cmp!=0) return cmp;
	    }
	} else {
	    while (li1.hasPrevious() && li2.hasPrevious()) {
		int cmp = compareElement(li1.previous(), li2.previous());
		if (cmp!=0) return cmp;
	    }
	}
	// for lists of unequal length, the shorter is the smaller.
	return l1.size() - l2.size();
    }
    private int compareElement(Object o1, Object o2) {
	if (elementComparator!=null)
	    return elementComparator.compare(o1, o2);
	else // throws ClassCastException if objects are not comparable.
	    return ((Comparable)o1).compareTo(o2);
    }
}
