// EnumerationIterator.java, created Tue Feb 23 02:03:39 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import net.cscott.jutil.UnmodifiableIterator;

import java.util.Enumeration;
import java.util.Iterator;
/**
 * An <code>EnumerationIterator</code> converts an <code>Enumeration</code>
 * into an <code>Iterator</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: EnumerationIterator.java,v 1.5 2004-02-08 01:56:15 cananian Exp $
 */
public class EnumerationIterator<E> extends UnmodifiableIterator<E> implements Iterator<E> {
    private final Enumeration<E> e;
    /** Creates a <code>EnumerationIterator</code>. */
    public EnumerationIterator(Enumeration<E> e) { this.e = e; }
    public boolean hasNext() { return e.hasMoreElements(); }
    public E next() { return e.nextElement(); }
}
