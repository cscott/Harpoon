// CallGraph.java, created Thu Aug 24 17:06:06 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HMethod;

import harpoon.Util.Graphs.SCCTopSortedGraph;
import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.Navigator;
import harpoon.Util.Graphs.ReverseNavigator;

import harpoon.Util.Graphs.DiGraph;

import java.util.Set;

/**
 * <code>CallGraph</code> is a general IR-independant interface that
 * for a call graph.  IR-specific subclasses (see
 * <code>harpoon.Analysis.Quads.CallGraph</code>) can provide
 * call-site information.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CallGraph.java,v 1.3 2003-05-06 15:00:38 salcianu Exp $
 */
public abstract class CallGraph extends DiGraph {
    /** Returns an array containing all possible methods called by
	method <code>m</code>. If <code>hm</code> doesn't call any 
	method, return an array of length <code>0</code>. */
    public abstract HMethod[] calls(final HMethod hm);

    /** Returns the set of all the methods that can be called in the 
	execution of the program. */
    public abstract Set callableMethods();

    /** @return set of all <code>run()</code> methods that may
	be the bodies of a thread started by the program (optional operation).
	@throws UnsupportedOperationException if <code>getRunMethods</code>
	is not implemented by <code>this</code>. */
    public Set getRunMethods() {
	throw new UnsupportedOperationException();
    }


    public Set/*<HMethod>*/ getDiGraphRoots() {
    	// we simply return all the nodes (i.e., methods)
    	return callableMethods();
    }

    /** Returns a bi-directional top-down graph navigator through
        <code>this</code> meta-callgraph. */
    public Navigator/*<HMethod>*/ getDiGraphNavigator() {
	final AllCallers ac = new AllCallers(this);
	
	return new Navigator() {
	    public Object[] next(Object node) {
		return calls((HMethod) node);
	    }  
	    public Object[] prev(Object node) {
		return ac.directCallers((HMethod) node);
	    }
	};
    }
}
