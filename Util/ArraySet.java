// ArraySet.java, created Wed Sep  8 14:51:21 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.Iterator;
/**
 * <code>ArraySet</code> creates an unmodifiable <code>Set</code> view of an
 * array.  The idea is similar to <code>java.util.Arrays.asList()</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArraySet.java,v 1.2.2.1 2002-03-04 19:10:56 cananian Exp $
 */
public class ArraySet<E> extends java.util.AbstractSet<E> {
    final E[] oa;
    
    /** Creates a <code>ArraySet</code> from an object array.
     *  All objects must be unique.
     */
    public ArraySet(E[] oa) {
	this.oa = oa;
    }
    public Iterator<E> iterator() { return new ArrayIterator<E>(oa); }
    public int size() { return oa.length; }
}
