// ListWrapper.java, created Fri Jul 14 11:14:15 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.List;

/**
 * <code>ListWrapper</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ListWrapper.java,v 1.2.2.1 2002-03-10 08:05:02 cananian Exp $
 */
public class ListWrapper<E> extends CollectionWrapper<E>
    implements List<E> {
    final List<E> b;
    
    /** Creates a <code>ListWrapper</code>. */
    public ListWrapper(List<E> l) {
        super(l);
	this.b = l;
    }
    
    public List<E> subList(int i, int j) {
	return b.subList(i, j);
    }

    public E get(int i) {
	return b.get(i);
    }

    public E set(int i, E o) {
	return b.set(i, o);
    }

    public E remove(int i) {
	return b.remove(i);
    }

    public java.util.ListIterator<E> listIterator(int i) {
	return b.listIterator(i);
    }
    
    public java.util.ListIterator<E> listIterator() {
	return b.listIterator();
    }

    public int lastIndexOf(Object o) {
	return b.lastIndexOf(o);
    }

    public int indexOf(Object o) {
	return b.indexOf(o);
    }

    public boolean addAll(int i, java.util.Collection<E> c) {
	return b.addAll(i, c);
    }

    public void add(int i, E o) {
	b.add(i, o);
    }

}
