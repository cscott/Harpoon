// ArrayIterator.java, created Tue Jun 15 15:59:04 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.ListIterator;
import java.util.NoSuchElementException;
/**
 * An <code>ArrayIterator</code> iterates over the elements of an array.
 * <p>The <code>add()</code>, <code>set()</code> and <code>remove()</code>
 * methods are <b>not</b> implemented; this iterator is unmodifiable.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayIterator.java,v 1.6 2004-02-07 19:26:09 cananian Exp $
 */

public class ArrayIterator<E> implements ListIterator<E> {
    final E[] oa;
    int i = 0;

    /** Creates an <code>ArrayIterator</code> that iterates over the
     *  contents of <code>oa</code>. */
    public ArrayIterator(E[] oa) {
	assert oa!=null;
        this.oa = oa;
    }
    public boolean hasNext() { return ( i < oa.length ); }
    public E next() {
	if (i < oa.length)
	    return oa[i++];
	else
	    throw new NoSuchElementException();
    }
    public int nextIndex() { return i; }

    public boolean hasPrevious() { return ( i > 0 ); }
    public E previous() {
	if (i > 0)
	    return oa[--i];
	else
	    throw new NoSuchElementException();
    }
    public int previousIndex() { return i-1; }

    /* unmodifiable */
    public void add(E o) {
	throw new UnsupportedOperationException();
    }
    public void remove() {
	throw new UnsupportedOperationException();
    }
    public void set(E o) {
	throw new UnsupportedOperationException();
    }
}
