// ReverseEnumerator.java, created Wed Dec 23 04:56:20 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * A <code>ReverseEnumerator</code> enumerates an <code>Enumeration</code>
 * in reverse order.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ReverseEnumerator.java,v 1.2 2002-02-25 21:08:49 cananian Exp $
 * @deprecated Use harpoon.Util.ReverseIterator instead.
 */
public class ReverseEnumerator implements Enumeration {
    final Vector v = new Vector();
    int i;

    /** Creates a <code>ReverseEnumerator</code> of <code>Enumeration</code>
     *  <code>e</code>. */
    public ReverseEnumerator(Enumeration e) {
        while (e.hasMoreElements())
	    v.addElement(e.nextElement());
	v.trimToSize();
	i = v.size()-1;
    }
    public boolean hasMoreElements() { return ( i >= 0 ); }
    public Object  nextElement() {
	try { return v.elementAt(i--); }
	catch (ArrayIndexOutOfBoundsException e)
	{ throw new NoSuchElementException(); }
    }
}
