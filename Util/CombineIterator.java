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
 * @version $Id: CombineIterator.java,v 1.2 2002-02-25 21:08:45 cananian Exp $
 */

public class CombineIterator implements Iterator {
    final Iterator[] ita;
    int i=0;
    /** Creates a <code>CombineIterator</code> from an array of Iterators. */
    public CombineIterator(Iterator[] ita) {
        this.ita = ita;
    }
    /** Creates a <code>CombineIterator</code> from a pair of
	Iterators. 
    */
    public CombineIterator(Iterator i1, Iterator i2) {
	this(new Iterator[]{ i1, i2 });
    }

    /** Creates a <code>CombineIterator</code> from an
     *  Iterator over Iterators. */
    public CombineIterator(Iterator it) {
	List l = new ArrayList();
	while (it.hasNext()) { l.add(it.next()); }
	this.ita = (Iterator[]) l.toArray(new Iterator[l.size()]);
    }
    public Object next() {
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
