// FilterEnumerator.java, created Tue Dec 22 15:56:39 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A <code>FilterEnumerator</code> filters and maps a source
 * <code>Enumeration</code> to generate a new one.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: FilterEnumerator.java,v 1.2 2002-02-25 21:08:45 cananian Exp $
 * @deprecated Use harpoon.Util.FilterIterator instead.
 */
public class FilterEnumerator implements Enumeration {
    /*final*/ Enumeration e;
    /*final*/ Filter f;
    /** Creates a <code>FilterEnumerator</code>. */
    public FilterEnumerator(Enumeration e, Filter f) {
        this.e = e; this.f = f; advance();
    }

    private Object next = null;
    private boolean done = false;

    private void advance() {
	while (e.hasMoreElements()) {
	    next = e.nextElement();
	    if (f.isElement(next))
		return; // found next element.
	}
	done = true; // found end of enumeration.
    }

    public Object nextElement() {
	if (done) throw new NoSuchElementException();
	Object o = next; advance(); return f.map(o);
    }
    public boolean hasMoreElements() {
	return !done;
    }

    public static class Filter { // default is an identity mapping.
	/** Return <code>true</code> if the specified element should be
	 *  included in the filtered enumeration. */
	public boolean isElement(Object o) { return true; }
	/** Perform a mapping on elements from the source enumeration. */
	public Object  map(Object o) { return o; }
    }
}
