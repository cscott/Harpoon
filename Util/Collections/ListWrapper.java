// ListWrapper.java, created Fri Jul 14 11:14:15 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.List;

/**
 * <code>ListWrapper</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ListWrapper.java,v 1.2 2002-02-25 21:09:05 cananian Exp $
 */
public class ListWrapper extends CollectionWrapper
    implements List {
    
    /** Creates a <code>ListWrapper</code>. */
    public ListWrapper(List l) {
        super(l);
    }
    
    public List subList(int i, int j) {
	return ((List)b).subList(i, j);
    }

    public Object get(int i) {
	return ((List)b).get(i);
    }

    public Object set(int i, Object o) {
	return ((List)b).set(i, o);
    }

    public Object remove(int i) {
	return ((List)b).remove(i);
    }

    public java.util.ListIterator listIterator(int i) {
	return ((List)b).listIterator(i);
    }
    
    public java.util.ListIterator listIterator() {
	return ((List)b).listIterator();
    }

    public int lastIndexOf(Object o) {
	return ((List)b).lastIndexOf(o);
    }

    public int indexOf(Object o) {
	return ((List)b).indexOf(o);
    }

    public boolean addAll(int i, java.util.Collection c) {
	return ((List)b).addAll(i, c);
    }

    public void add(int i, Object o) {
	((List)b).add(i, o);
    }

}
