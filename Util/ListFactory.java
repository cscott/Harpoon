// ListFactory.java, created Wed Aug  4 12:58:08 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import java.util.List;
import java.util.Arrays;
import java.util.AbstractList;

/**
 * <code>ListFactory</code> is a set of static helper methods for
 * building <code>List</code> objects.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ListFactory.java,v 1.1.2.1 1999-08-04 17:57:15 pnkfelix Exp $
 */
public class ListFactory {

    /** Creates and returns a <code>List</code> made up from
	connecting <code>lists</code> together in order.
	<BR> <B>requires:</B> <code>lists</code> is a
	     <code>List</code> of <code>List</code>s.
    */
    public static List concatenate(final List lists) {
	return new AbstractList(){
	    public Object get(int index) {
		int origIndex = index;
		int totalSize = 0;
		if (index < 0) 
		    throw new IndexOutOfBoundsException(""+origIndex+" < 0"); 
		int lindex = 0;
		List l = (List) lists.get(lindex);
		totalSize += l.size();
		
		while(true) {
		    if (index < l.size()) {
			return l.get(index);
		    } else {
			index -= l.size();
			lindex++; 
			if(lindex < lists.size()) {
			    l = (List) lists.get(lindex);
			    totalSize += l.size();
			} else {
			    throw new IndexOutOfBoundsException
				(""+origIndex+" > "+totalSize); 
			}
		    }
		}
	    }
	    public int size() {
		int sz = 0; 
		for(int i=0; i<lists.size(); i++) {
		    sz += ((List)lists.get(i)).size();
		}
		return sz;
	    }
	};
	
    }

    /** Creates and returns a <code>List</code> made up from
	connecting <code>lists</code> together in order.
    */
    public static List concatenate(final List[] lists) {
	return concatenate(Arrays.asList(lists));
    }

    /** Creates and returns a <code>List</code> of one element. */
    public static List singleton(final Object o) {
	return new AbstractList() {
	    public Object get(int index) {
		if(index==0) return o;
		throw new IndexOutOfBoundsException
			(""+index+" is out of bounds for list of size 1"); 
	    }
	    public int size() { return 1; }
	};
    }
}
