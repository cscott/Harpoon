// PAWorkset.java, created Mon Jan 10 20:11:23 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Iterator;
import java.util.Collection;

/**
 * The <code>PAWorkset</code> class tries to formalize the concept
 * of a workset used in many dataflow analysis. There is some workset
 * interface in the harpoon.Util package but I didn't like it and I 
 * decided to implement my own one. 
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAWorkSet.java,v 1.1.2.4 2001-02-27 22:11:12 salcianu Exp $
 */
public abstract class PAWorkSet implements java.io.Serializable {
    // add an object to the workset
    public abstract boolean add(Object o);
    // take an object from the workset
    public abstract Object remove();
    // test the presence of an object into the workset
    public abstract boolean contains(Object o);
    // check whether the workset is empty or not
    public abstract boolean isEmpty();

    /** Adds all the elements of a <code>Collection</code> */ 
    public void addAll(Collection c){
	for(Iterator it=c.iterator();it.hasNext();)
	    add(it.next());
    }
}
