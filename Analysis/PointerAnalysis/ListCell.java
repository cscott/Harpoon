// ListCell.java, created Wed Mar 29 13:32:14 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

/**
 * <code>ListCell</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: ListCell.java,v 1.1.2.3 2001-06-17 22:30:41 cananian Exp $
 */
public class ListCell implements java.io.Serializable {

    public Object   info;
    public ListCell next;
    
    /** Creates a <code>ListCell</code>. */
    public ListCell(Object info, ListCell next) {
        this.info = info;
	this.next = next;
    }

    /** Checks teh identity of two lists. */
    public static boolean identical(ListCell l1, ListCell l2){
	while((l1 != null) && (l2 != null)){
	    // check the equality of the info in the current cells
	    if(!l1.info.equals(l2.info))
		return false;
	    // advance in the list
	    l1 = l1.next;
	    l2 = l2.next;
	}
	// if they are both null here, they are equal, otherwise it means that
	// only one is null, so they have different sizes
	return (l1 == l2);
    }
}
