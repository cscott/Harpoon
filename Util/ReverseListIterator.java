// ReverseListIterator.java, created Fri Mar 15 19:39:14 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.List;
import java.util.ListIterator;
/**
 * <code>ReverseListIterator</code> takes a <code>List</code> and gives
 * you an <code>Iterator</code> that traverses the list in reverse
 * order.  Similar to using <code>Util.ReverseIterator(l.iterator())</code>
 * but more efficient.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ReverseListIterator.java,v 1.1.2.1 2002-03-16 01:27:44 cananian Exp $
 */
public class ReverseListIterator<E> implements ListIterator<E> {
    private final ListIterator<E> it;
    /** Creates a <code>ReverseListIterator</code>. */
    public ReverseListIterator(List<E> l) {
	this.it = l.listIterator(l.size());
    }
    public void add(E o) { it.add(o); }
    public boolean hasNext() { return it.hasPrevious(); }
    public boolean hasPrevious() { return it.hasNext(); }
    public E next() { return it.previous(); }
    public int nextIndex() { return it.previousIndex(); }
    public E previous() { return it.next(); }
    public int previousIndex() { return it.nextIndex(); }
    public void remove() { it.remove(); }
    public void set(E o) { it.set(o); }
    
}
