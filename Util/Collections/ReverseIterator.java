// ReverseIterator.java, created Wed Dec 23 04:56:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.List;

/**
 * A <code>ReverseIterator</code> iterates through an <code>Iterator</code>
 * in reverse order.  It extends <code>SnapshotIterator</code>, so is
 * insensitive to changes in the underlying collection once construction
 * is complete.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ReverseIterator.java,v 1.2 2002-08-30 23:27:16 cananian Exp $
 */
public class ReverseIterator<E> extends SnapshotIterator<E> {
    /** Creates a <code>ReverseIterator</code> of <code>Iterator</code>
     *  <code>it</code>. */
    public ReverseIterator(Iterator<E> it) {
	super(it);
	i = l.size()-1;
    }
    public boolean hasNext() { return ( i >= 0 ); }
    public E  next() {
	try { return l.get(i--); }
	catch (IndexOutOfBoundsException e)
	{ throw new NoSuchElementException(); }
    }
}
