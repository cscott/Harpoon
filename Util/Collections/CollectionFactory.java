// CollectionFactory.java, created Tue Oct 19 22:21:39 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import java.util.Collection;

/** <code>CollectionFactory</code> is a <code>Collection</code>
    generator.  Subclasses should implement constructions of specific
    types of <code>Collection</code>s.  

    Note that since some types of <code>Collection</code>s have
    implicit constraints (such as <code>Set</code>s, which cannot
    contain more than one of the same element), code which uses the
    classes produced by <code>CollectionFactory</code>s must take care
    not to assume more than what is guaranteed by the
    <code>Collection</code> interface.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CollectionFactory.java,v 1.1.2.1 1999-10-20 06:00:26 pnkfelix Exp $
 */
public abstract class CollectionFactory {
    
    /** Creates a <code>CollectionFactory</code>. */
    public CollectionFactory() {
	
    }
    
    /** Generates a new, mutable, empty <code>Collection</code>. */
    public Collection makeCollection() {
	return makeCollection(java.util.Collections.EMPTY_SET);
    }

    /** Generates a new, mutable <code>Collection</code>, using the
	elements of <code>c</code> as a template for its initial
	contents.  
    */  
    public abstract Collection makeCollection(Collection c);

    
    
}
