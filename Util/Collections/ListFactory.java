// ListFactory.java, created Tue Oct 19 22:39:10 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * <code>ListFactory</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ListFactory.java,v 1.1.2.1 1999-10-20 06:00:26 pnkfelix Exp $
 */
public abstract class ListFactory extends CollectionFactory {
    
    /** Creates a <code>ListFactory</code>. */
    public ListFactory() {
        
    }
    
    public Collection makeCollection() {
	return makeList();
    }

    public Collection makeCollection(Collection c) {
	return makeList(c);
    }

    /** Generates a new, mutable, empty <code>List</code>. */
    public List makeList() {
	return makeList(Collections.EMPTY_LIST);
    }

    /** Generates a new mutable <code>List</code>, using the elements
	of <code>c</code> as a template for its initial contents. 
    */
    public abstract List makeList(Collection c); 
}
