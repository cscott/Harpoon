// ArrayEnumerator.java, created Wed Sep 16 15:14:59 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
/**
 * An <code>ArrayEnumerator</code> enumerates the elements of an array.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayEnumerator.java,v 1.3 2002-02-25 21:08:44 cananian Exp $
 * @deprecated Use harpoon.Util.ArrayIterator instead.
 */

public class ArrayEnumerator implements Enumeration {
    final Object[] oa;
    int i = 0;

    /** Creates an <code>ArrayEnumerator</code>. */
    public ArrayEnumerator(Object[] oa) {
        this.oa = oa;
    }
    public boolean hasMoreElements() { return ( i < oa.length ); }
    public Object  nextElement() {
	if (i < oa.length)
	    return oa[i++];
	else
	    throw new NoSuchElementException();
    }
}
