// ReverseViewList.java, created Thu Mar  4 11:38:44 2004 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.DataStructs;

import java.util.List;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.ListIterator;

import java.io.Serializable;

import net.cscott.jutil.ReverseListIterator;

/**
 * <code>ReverseViewList</code> is an immutable, reverse view of a
 * <code>List</code>.  The main thing you can do with it is iterate,
 * eg, <code>for(Element e : new ReverseListView(list))</code>.  In
 * addition, you can call <code>get</code> and <code>size</code>.
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: ReverseListView.java,v 1.1 2004-03-04 22:32:26 salcianu Exp $ */
public class ReverseListView<E>
    extends AbstractList<E> implements Serializable {
    /** Creates a reverse view of <code>origList</code>. */
    public ReverseListView(List<E> origList) { this.origList = origList; }
    private final List<E> origList;

    // as described in the JavaDoc for AbstractList, to implement an
    // unmodifiable list, we only need to override get and size.
    /** Returns the <code>i</code>th element from the end of the
        original list. */
    public E get(int index) {
	return origList.get(size() - index);
    }
    /** Returns the size of the original list. */
    public int size() { return origList.size(); }
    
    public Iterator<E> iterator() { return listIterator(); }
    public ListIterator<E> listIterator() {
	return new ReverseListIterator<E>(origList);
    }
}
