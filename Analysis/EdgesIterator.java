// EdgesIterator.java, created Mon Apr  5 16:15:47 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGraphable;
import harpoon.IR.Properties.CFGrapher;
import harpoon.Util.Collections.UnmodifiableIterator;
import harpoon.Util.Collections.WorkSet;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * <code>EdgesIterator</code> is a generic iterator for a set of
 * <code>CFGraphable</code> objects. 
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: EdgesIterator.java,v 1.3 2002-08-30 22:37:12 cananian Exp $
 */
public class EdgesIterator extends UnmodifiableIterator implements Iterator {
    private CFGrapher grapher;
    
    private WorkSet worklist;
    private Set done;

    /** Convenience constructor. */
    public EdgesIterator(CFGraphable e) {
	this(e, CFGrapher.DEFAULT);
    }
    /** Creates a <code>EdgesIterator</code> for all the edges
	reachable by recursively traversing the successors of
	<code>e</code>.  Predecessors are not included in the set.  
    */
    public EdgesIterator(HCodeElement e, CFGrapher grapher) {
	this.grapher = grapher;
        worklist = new WorkSet();
	done = new HashSet();
	worklist.add(e);
	done.add(e);
    }
    
    /** Checks if the set is empty.
	<BR> <B>effects:</B> returns true if more <code>CFGraphable</code>
	remain in the set.  Else returns false.
    */
    public boolean hasNext() { return worklist.size()!=0; } 
    
    /** Returns an <code>CFGraphable</code> if one remains.
	<BR> <B>requires:</B> <code>this.hasNext()</code> == true.
	<BR> <B>effects:</B> returns an <code>CFGraphable</code> from the
	set contained in <code>this</code> and removes it from the
	set.
    */ 
    public Object next() {
	HCodeElement e = (HCodeElement) worklist.pop();
	HCodeEdge[] edges = grapher.succ(e);
	for (int i=0, n=edges.length; i<n; ++i) {
	    HCodeElement ne = edges[i].to();
	    if (!done.contains(ne)) {
		done.add(ne);
		worklist.add(ne);
	    }
	}
	return e;
    }
}
