// ArrayIterator.java, created Tue Jun 15 15:59:04 1999 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Iterator;
import java.util.NoSuchElementException;
/**
 * An <code>ArrayIterator</code> iterates over the elements of an array.
 * <p>The <code>remove()</code> method is <b>not</b> implemented.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayIterator.java,v 1.3.2.3 2002-03-14 01:54:43 cananian Exp $
 */

public class ArrayIterator<E> extends UnmodifiableIterator<E>
    implements Iterator<E> {
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
}
