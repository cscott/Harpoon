// LinearSet.java, created Wed Aug  4 12:03:54 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * <code>LinearSet</code> is a simplistic light-weight
 * <code>Set</code> designed for use when the number of entries is
 * small.  It is backed by an <code>ArrayList</code>.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LinearSet.java,v 1.1.2.1 1999-08-04 17:57:15 pnkfelix Exp $
 */
public class LinearSet extends AbstractSet {
    private ArrayList list;

    /** Creates a <code>LinearSet</code>. */
    public LinearSet() {
        list = new ArrayList();
    }
    
    public Iterator iterator() {
	return list.iterator();
    }

    public int size() {
	return list.size();
    }

    public boolean add(Object o) {
	if (!list.contains(o)) {
	    list.add(o);
	    return true;
	} else {
	    return false;
	}
    }
}
