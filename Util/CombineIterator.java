// CombineIterator.java, created Wed Oct 14 08:50:22 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
/**
 * A <code>CombineIterator</code> combines several different
 * <code>Iterator</code>s into one.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CombineIterator.java,v 1.2.2.1 2002-03-04 19:10:56 cananian Exp $
 */

public class CombineIterator<E> implements Iterator<E> {
    final Iterator<E>[] ita;
    int i=0;
    /** Creates a <code>CombineIterator</code> from an array of Iterators. */
    public CombineIterator(Iterator<E>[] ita) {
        this.ita = ita;
    }
    /** Creates a <code>CombineIterator</code> from a pair of
	Iterators. 
    */
    public CombineIterator(Iterator<E> i1, Iterator<E> i2) {
	this(new Iterator<E>[]{ i1, i2 });
    }

    /** Creates a <code>CombineIterator</code> from an
     *  Iterator over Iterators. */
    public CombineIterator(Iterator<Iterator<E>> it) {
	List<Iterator<E>> l = new ArrayList<Iterator<E>>();
	while (it.hasNext()) { l.add(it.next()); }
	this.ita = l.toArray(new Iterator<E>[l.size()]);
    }
    public E next() {
	while (i < ita.length && !ita[i].hasNext() )
	    i++;
	if (i < ita.length && ita[i].hasNext())
	    return ita[i].next();
	else
	    throw new NoSuchElementException();
    }
    public boolean hasNext() {
	for (int j=i; j<ita.length; j++)
	    if (ita[j].hasNext())
		return true;
	return false;
    }
    public void remove() {
	ita[i].remove();
    }
}
