// PAWorkStack.java, created Mon Jan 10 20:16:39 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.LinkedList;
import java.util.HashSet;

/**
 * <code>PAWorkStack</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAWorkStack.java,v 1.1.2.6 2001-06-17 22:30:48 cananian Exp $
 */
public class PAWorkStack extends PAWorkSet implements java.io.Serializable {

    LinkedList list;
    HashSet    set;

    /** Creates a <code>PAWorkStack</code>. */
    public PAWorkStack() {
        list = new LinkedList();
	set  = new HashSet();
    }

    // add an object to the workset
    public final boolean add(Object o){
	if(!set.contains(o)){
	    set.add(o);
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
	// Both add and remove takes place at the end of the list -> queue
	Object o = list.removeLast();
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
