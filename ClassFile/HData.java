// HData.java, created Wed Jul 28 13:55:51 1999 by duncan
// Copyright (C) 1998 Duncan Bryce     <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.util.Iterator;
import java.util.List;


/**
 * <code>HData</code> is an interface that all views of a particular
 * class's static data should extend. 
 * An <code>HData</code> corresponds roughly to a "memory layout".
 *
 * @author  Duncan Bryce     <duncan@lcs.mit.edu>
 * @version $Id: HData.java,v 1.1.2.4 1999-08-04 06:30:56 cananian Exp $
 */
public interface HData { 
    
    /** Clone this <code>HData</code>, possibly moving it to a different
     *  <code>HClass</code> */
    public HData    clone(HClass cls);


    /** Return the <code>HClass</code> to which this <code>HData</code>
     *  belongs. */
    public HClass   getHClass();

    /**
     * Return an Iterator over the component objects making up this
     * data view.  If there is a 'root' to the data view, it should
     * be the first element enumerated.   */
    public Iterator getElementsI();
    
    /**
     * Return an ordered <code>Collection</code> (a <code>List</code>) of
     * the component objects making up this data view.  If there is a
     * 'root' to the data view, it should be the first element in the
     * List. */
    public List     getElementsL();

    /**
     * Print a human-readable representation of this dataview */
    public void     print(java.io.PrintWriter pw); 
}
