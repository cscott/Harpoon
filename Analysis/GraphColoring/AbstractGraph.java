// AbstractGraph.java, created Tue Jul 25 15:52:49 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.GraphColoring;

import harpoon.Util.FilterIterator;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.Default;

import java.util.Iterator;
import java.util.Collection;
import java.util.AbstractCollection;
import java.util.Set;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.List;

/**
 * <code>AbstractGraph</code> is a skeletal implementation of the
 * <code>Graph</code> interface, to minimize the effort required to
 * implement this interface.
 * 
 * To implement an unmodifiable graph, the programmer needs only to
 * extend this class and provide implementations for the
 * <code>nodeSet</code> and <code>neighborsOf</code> methods. 
 *
 * To implement a modifiable graph, the programmer must additionally
 * override this class's <code>addNode</code> and <code>addEdge</code>
 * methods  (which otherwise throws UnsupportedOperationException).
 *
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: AbstractGraph.java,v 1.2 2002-02-25 20:57:12 cananian Exp $
 */
public abstract class AbstractGraph implements Graph {
    
    /** Creates a <code>AbstractGraph</code>. */
    public AbstractGraph() { }
    public abstract Set nodeSet();
    public abstract Collection neighborsOf(Object n);


    public boolean addNode(Object n) { 
	throw new UnsupportedOperationException(); 
    }
    public boolean addEdge(Object m, Object n) { 
	throw new UnsupportedOperationException(); 
    }
    public int getDegree(Object n) { 
	return neighborsOf(n).size(); 
    }
    public Collection edgesFor(final Object n) { 
	final FilterIterator.Filter filter = new FilterIterator.Filter(){
	    public Object map(Object o) { return Default.pair(n, o); }
	};
	return new AbstractCollection() {
	    public int size() { return neighborsOf(n).size(); }
	    public Iterator iterator() {
		return new FilterIterator(neighborsOf(n).iterator(), 
					  filter);
	    }
	};
    }

    public Collection edges() {
	return new AbstractCollection() {
	    public int size() {
		int d = 0;
		for(Iterator nodes=nodeSet().iterator();nodes.hasNext();){
		    Object n = nodes.next();
		    d += neighborsOf(n).size();
		}
		return d/2;
	    }
	    public Iterator iterator() {
		final Iterator nodes = nodeSet().iterator();

		// visited holds all previous values for curr in
		// returned Iterator
		final Set visited = new HashSet();
		
		final Object first;
		if (nodes.hasNext()) {
		    first = nodes.next();
		} else {
		    first = null;
		}
		final Iterator firstNeighbors;
		if (first != null) {
		    firstNeighbors = neighborsOf(first).iterator();
		} else {
		    firstNeighbors = Default.nullIterator;
		}

		return new UnmodifiableIterator(){
		    // (nbor != null)  ==> next edge is <curr, nbor>
		    Object curr = first, nbor = null;
		    
		    // (curr == null)  ==> !niter.hasNext()
		    // niter.hasNext() ==> niter.next() is next
		    //                     potential nbor
		    Iterator niter = firstNeighbors;

		    // (nbor == null)  ==> push up to next nbor val
		    public boolean hasNext() {
			if(nbor!=null) {
			    return true;
			} else {
			    // loop until 
			    // <curr, nbor> is a valid new edge 
			    // or (!niter.hasNext() /\ !nodes.hasNext())
			    while(true) {
				while(niter.hasNext()) {
				    nbor = niter.next();
				    if(!visited.contains(nbor)) 
					return true;
				}
				
				// finished with neighbors for curr 
				nbor = null;
				
				if(nodes.hasNext()) {
				    visited.add(curr);
				    curr = nodes.next();
				    niter = neighborsOf(curr).iterator();
				} else {
				    return false;
				}
			    }
			}
		    }
		    public Object next() {
			if(hasNext()){
			    List edge = Default.pair(curr, nbor);
			    nbor = null;
			    return edge;
			} else {
			    throw new java.util.NoSuchElementException();
			}
		    }
		};
	    }
	};
    }
}
