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
 * @version $Id: ArraySet.java,v 1.2 2002-02-25 21:08:44 cananian Exp $
 */
public class ArraySet extends java.util.AbstractSet {
    final Object[] oa;
    
    /** Creates a <code>ArraySet</code> from an object array.
     *  All objects must be unique.
     */
    public ArraySet(Object[] oa) {
	this.oa = oa;
    }
    public Iterator iterator() { return new ArrayIterator(oa); }
    public int size() { return oa.length; }
}
