// ReverseIterator.java, created Wed Dec 23 04:56:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.List;

/**
 * A <code>ReverseIterator</code> enumerates an <code>Enumeration</code>
 * in reverse order.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ReverseIterator.java,v 1.1.2.1 1999-03-02 10:48:13 cananian Exp $
 */
public class ReverseIterator implements Iterator {
    final List l = new ArrayList();
    int i;

    /** Creates a <code>ReverseIterator</code> of <code>Iterator</code>
     *  <code>it</code>. */
    public ReverseIterator(Iterator it) {
	ArrayList l = new ArrayList();
	while (it.hasNext()) l.add(it.next());
	l.trimToSize();
	i = l.size()-1;
    }
    public boolean hasNext() { return ( i >= 0 ); }
    public Object  next() {
	try { return l.get(i--); }
	catch (IndexOutOfBoundsException e)
	{ throw new NoSuchElementException(); }
    }
    public void remove() {
	throw new UnsupportedOperationException();
    }
}
