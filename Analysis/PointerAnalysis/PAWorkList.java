// PAWorkList.java, created Mon Jan 10 20:32:58 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.LinkedList;
import java.util.HashSet;

/**
 * <code>PAWorkList</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAWorkList.java,v 1.1.2.1 2000-01-14 20:50:59 salcianu Exp $
 */
public class PAWorkList extends PAWorkSet{

    LinkedList list;
    HashSet    set;
    
    /** Creates a <code>PAWorkList</code>. */
    public PAWorkList() {
        list = new LinkedList();
	set  = new HashSet();
    }

    // add an object to the workset
    public final void add(Object o){
	if(!set.contains(o)){
	    set.add(o);
	    list.addLast(o);
	}
    }
    // take an object from the workset
    public final Object remove(){
	if(list.isEmpty()){
	    System.err.println("Trying to get an element from an empty" +
			       "PAWorkStack");
	    System.exit(1);
	}
	// Add is at the end, remove at the beginning -> list
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
