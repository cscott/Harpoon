// PAWorkList.java, created Mon Jan 10 20:32:58 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.LinkedList;
import java.util.HashSet;

/**
 * <code>PAWorkList</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAWorkList.java,v 1.2 2002-02-25 20:58:40 cananian Exp $
 */
public class PAWorkList extends PAWorkSet implements java.io.Serializable {

    LinkedList list;
    HashSet    set;
    
    /** Creates a <code>PAWorkList</code>. */
    public PAWorkList() {
        list = new LinkedList();
	set  = new HashSet();
    }

    // add an object to the workset
    public final boolean add(Object o){
	if(set.add(o)){
	    list.addLast(o);
	    return true;
	}
	return false;
    }
    // take an object from the workset
    public final Object remove(){
	if(list.isEmpty()){
	    System.err.println("Trying to get an element from an empty" +
			       "PAWorkStack");
	    System.exit(1);
	}
	// Add is at the end, remove at the beginning -> list (FIFO)
	Object o = list.removeFirst();
	set.remove(o);
	return o;
    }

    // test the presence of an object into the workset
    public final boolean contains(Object o){
	return set.contains(o);
    }

    // check whether the workset is empty or not
    public final boolean isEmpty(){
	return list.isEmpty();
    }
}
