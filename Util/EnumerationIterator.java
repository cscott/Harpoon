// EnumerationIterator.java, created Tue Feb 23 02:03:39 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Enumeration;
import java.util.Iterator;
/**
 * An <code>EnumerationIterator</code> converts an <code>Enumeration</code>
 * into an <code>Iterator</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: EnumerationIterator.java,v 1.1.2.1 1999-02-23 07:12:08 cananian Exp $
 */
public class EnumerationIterator implements Iterator {
    private final Enumeration e;
    /** Creates a <code>EnumerationIterator</code>. */
    public EnumerationIterator(Enumeration e) { this.e = e; }
    public boolean hasNext() { return e.hasMoreElements(); }
    public Object next() { return e.nextElement(); }
    public void remove() { throw new UnsupportedOperationException(); }
}
