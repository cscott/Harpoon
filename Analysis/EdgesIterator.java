// EdgesIterator.java, created Mon Apr  5 16:15:47 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.IR.Properties.Edges;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * <code>EdgesIterator</code> is a generic iterator for a set of
 * <code>Edges</code> objects. 
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: EdgesIterator.java,v 1.1.2.1 1999-04-05 21:06:48 pnkfelix Exp $
 */
public class EdgesIterator implements Iterator {
    
    private Set worklist;
    private Set done;

    /** Creates a <code>EdgesIterator</code> for all the edges
	reachable by recursively traversing the successors of
	<code>e</code>.  Predecessors are not included in the set.  
    */
    public EdgesIterator(Edges e) {
        worklist = new HashSet();
	done = new HashSet();
	worklist.add(e);
	done.add(e);
    }
    
    /** Checks if the set is empty.
	<BR> <B>effects:</B> returns true if more <code>Edges</code>
	remain in the set.  Else returns false.
    */
    public boolean hasNext() { return worklist.size()!=0; } 
    
    /** Returns an <code>Edges</code> if one remains.
	<BR> <B>requires:</B> <code>this.hasNext()</code> == true.
	<BR> <B>effects:</B> returns an <code>Edges</code> from the
	set contained in <code>this</code> and removes it from the
	set.
    */ 
    public Object next() {
	Edges e = (Edges) worklist.iterator().next(); worklist.remove(e);
	for (int i=0, n=e.succ().length; i<n; ++i) {
	    Edges ne = (Edges) e.succ()[i].to();
	    if (!done.contains(ne)) {
		done.add(ne);
		worklist.add(ne);
	    }
	}
	return e;
    }

    /** not implemented in this.
	<BR> <B>effects:</B> throws an
	OperationNotImplementedException. 
    */
    public void remove() { throw new UnsupportedOperationException(); }
}
