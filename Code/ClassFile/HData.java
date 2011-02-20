// HData.java, created Wed Jul 28 13:55:51 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

import java.util.Iterator;
import java.util.List;

/**
 * <code>HData</code> is an interface that all views of a particular
 * class's static data should extend. 
 * An <code>HData</code> encapsulates a data view of class data in
 * roughly the same way that an <code>HCode</code> encapsulates a
 * code view of method instructions.
 * A particular implementation of <code>HData</code> correlates with
 * "memory layout" for a particular runtime implementation.
 * <p>
 * It is possible (and rather recommended) for a class to have *several*
 * <code>HData</code>s associated with it, each of which encapsulates
 * a certain set of class data.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: HData.java,v 1.2 2002-02-25 21:03:03 cananian Exp $
 */
public abstract class HData { 
    
    /** Clone this <code>HData</code>, possibly moving it to a different
     *  <code>HClass</code>.
     * @exception CloneNotSupportedException if it is not possible to
     *            clone this <code>HData</code>. */
    public HData clone(HClass cls) throws CloneNotSupportedException {
	throw new CloneNotSupportedException();
    }

    /** Return the <code>HClass</code> to which this <code>HData</code>
     *  belongs. Returns <code>null</code> if this <code>HData</code>
     *  has global, rather than class-local, data structures. */
    public abstract HClass getHClass();

    /**
     * Return the 'root' element of this data view.
     * @return root of the data view, or <code>null</code> if this notion
     *         is not applicable.
     */
    public abstract HDataElement getRootElement();

    /**
     * Return an Iterator over the component objects making up this
     * data view.  If there is a 'root' to the data view, it should
     * be the first element enumerated.<p>
     * <b>AT LEAST ONE OF <code>getElementsI()</code> AND
     * <code>getElementsL()</code> MUST BE IMPLEMENTED.</b>
     */
    public Iterator getElementsI() { return getElementsL().iterator(); }
    
    /**
     * Return an ordered <code>Collection</code> (a <code>List</code>) of
     * the component objects making up this data view.  If there is a
     * 'root' to the data view, it should be the first element in the
     * List.<p>
     * <b>AT LEAST ONE OF <code>getElementsI()</code> AND
     * <code>getElementsL()</code> MUST BE IMPLEMENTED.</b>
     */
    public List getElementsL() {
	List l = new java.util.ArrayList();
	for (Iterator it=getElementsI(); it.hasNext(); )
	    l.add(it.next());
	return java.util.Collections.unmodifiableList(l);
    }

    /**
     * Print a human-readable representation of this dataview using
     * a null callback. */
    public final void print(java.io.PrintWriter pw) {
	print(pw, new HCode.PrintCallback());
    }
    /**
     * Print a human-readable representation of this dataview using
     * the specified callback. */
    public abstract void print(java.io.PrintWriter pw,
			       HCode.PrintCallback callback); 
}
