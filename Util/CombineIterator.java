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
 * @version $Id: CombineIterator.java,v 1.1.2.1 1999-03-02 06:42:45 cananian Exp $
 */

public class CombineIterator implements Iterator {
    final Iterator[] ita;
    int i=0;
    /** Creates a <code>CombineIterator</code>. */
    public CombineIterator(Iterator[] ita) {
        this.ita = ita;
    }
    private void adv() {
	while (i < ita.length && !ita[i].hasNext() )
	    i++;
    }
    public Object next() {
	if (hasNext())
	    return ita[i].next();
	else
	    throw new NoSuchElementException();
    }
    public boolean hasNext() {
	adv();
	return (i<ita.length);
    }
    public void remove() {
	ita[i].remove();
    }
}
