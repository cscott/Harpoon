// CombineIterator.java, created Wed Oct 14 08:50:22 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Iterator;
import java.util.NoSuchElementException;
/**
 * A <code>CombineIterator</code> combines several different
 * <code>Iterator</code>s into one.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CombineIterator.java,v 1.1.2.2 1999-06-15 20:25:56 sportbilly Exp $
 */

public class CombineIterator implements Iterator {
    final Iterator[] ita;
    int i=0;
    /** Creates a <code>CombineIterator</code>. */
    public CombineIterator(Iterator[] ita) {
        this.ita = ita;
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
