// Indexer.java, created Mon Nov  8 22:42:56 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

/**
 * <code>Indexer</code> is an interface for extracting unique indices 
 * for a set of objects.  It is commonly implemented by
 * <i>Factories</i> for some class of objects, which can then generate and
 * store unique integers in the objects that they are used to
 * construct.  This way auxilliary data structures can efficiently
 * index objects that belong to a common Factory without that
 * data-structure needing an explict dependency on that particular
 * Factory or Object type.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: Indexer.java,v 1.1.2.7 2000-02-10 22:19:21 cananian Exp $
 */
public abstract class Indexer {
    
    /** Returns the "small" integer uniquely associated with
	<code>o</code> in <code>this</code>.
	<BR> <B>requires:</B> <code>o</code> is a member of the set of
	     objects indexed by this
	<BR> <B>effects:</B> 
	     returns the integer uniquely associated with
	     <code>o</code> from a densely-packed, non-negative 
	     set of integers whose smallest element is close to zero. 
    */
    public abstract int getID(Object o); 

    /** Provides a reverse mapping for the index returned by
     *  <code>getID</code>.  The constraint is that
     *  <code>getByID(getID(o))</code> must equal <code>o</code>
     *  for all objects indexed by <code>this</code>.
     * @exception UnsupportedOperationException if this functionality
     *  is not supported by <code>this</code>.
     */
    public Object getByID(int id) {
	throw new UnsupportedOperationException("getByID() not supported");
    }
    /** Tells user whether this particular <code>Indexer</code> implements
     *  the <code>getByID()</code> method.
     */
    public boolean implementsReverseMapping() { return false; }
}
