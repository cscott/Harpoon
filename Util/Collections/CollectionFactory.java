// CollectionFactory.java, created Tue Oct 19 22:21:39 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.Collections;

import harpoon.Util.Default;

import java.util.Collection;

/** <code>CollectionFactory</code> is a <code>Collection</code>
    generator.  Subclasses should implement constructions of specific
    types of <code>Collection</code>s.  
    <p>
    Note that since some types of <code>Collection</code>s have
    implicit constraints (such as <code>Set</code>s, which cannot
    contain more than one of the same element), code which uses the
    classes produced by <code>CollectionFactory</code>s must take care
    not to assume more than what is guaranteed by the
    <code>Collection</code> interface.
    <p>
    Note also that the current limitations on parametric types in
    Java mean that we can't easily type this class as
    <code>CollectionFactory&lt;C extends Collection&lt;V&gt;,V&gt;</code>,
    as <code>CollectionFactory&lt;Set&lt;V&gt;,V&gt;</code> is not
    a subtype of <code>CollectionFactory&lt;Collection&lt;V&gt;,V&gt;</code>,
    even though <code>Set</code> is a subtype of <code>Collection</code>.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CollectionFactory.java,v 1.2.2.2 2002-03-04 20:22:18 cananian Exp $
 */
public abstract class CollectionFactory<V> {
    
    /** Creates a <code>CollectionFactory</code>. */
    public CollectionFactory() {
	
    }
    
    /** Generates a new, mutable, empty <code>Collection</code>. */
    public Collection<V> makeCollection() {
	// XXX BUG IN JAVAC: should be Collections.EMPTY_SET
	return makeCollection(Default.EMPTY_SET());
    }

    /** Generates a new, mutable, empty <code>Collection</code>, using
	<code>initialCapacity</code> as a hint to use for the capacity
	for the produced <code>Collection</code>. */
    public Collection<V> makeCollection(int initialCapacity) {
	return makeCollection();
    }

    /** Generates a new, mutable <code>Collection</code>, using the
	elements of <code>c</code> as a template for its initial
	contents.  Note that the <code>Collection</code> returned is
	not a <i>view</i> of <code>c</code>, but rather a snapshot;
	changes to <code>c</code> are not reflected in the returned
	<code>Collection</code>. 
    */  
    public abstract Collection<V> makeCollection(Collection<V> c);

    
    
}
