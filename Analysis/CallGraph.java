// CallGraph.java, created Thu Aug 24 17:06:06 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import java.util.Set;
import java.util.List;
import java.util.Arrays;

import harpoon.ClassFile.HMethod;

import jpaul.Graphs.BiDiNavigator;
import jpaul.Graphs.ForwardNavigator;
import jpaul.Graphs.DiGraph;


/**
 * <code>CallGraph</code> is a general IR-independant interface that
 * for a call graph.  IR-specific subclasses (see
 * <code>harpoon.Analysis.Quads.CallGraph</code>) can provide
 * call-site information.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CallGraph.java,v 1.9 2005-12-10 17:26:38 salcianu Exp $
 */
public abstract class CallGraph extends DiGraph<HMethod> {
    /** Returns an array containing all possible methods called by
	method <code>m</code>. If <code>hm</code> doesn't call any 
	method, return an array of length <code>0</code>. */
    public abstract HMethod[] calls(final HMethod hm);

    /** Returns the set of all the methods that can be called in the 
	execution of the program. */
    public abstract Set<HMethod> callableMethods();

    /** @return set of all <code>run()</code> methods that may
	be the bodies of a thread started by the program (optional operation).
	@throws UnsupportedOperationException if <code>getRunMethods</code>
	is not implemented by <code>this</code>. */
    public Set getRunMethods() {
	throw new UnsupportedOperationException();
    }


    public Set<HMethod> getRoots() {
    	// we simply return all the nodes (i.e., methods)
    	return callableMethods();
    }


    /** Returns a bi-directional top-down graph navigator through
        <code>this</code> callgraph.  Result is internally cached. */
    public BiDiNavigator<HMethod> getBiDiNavigator() {
	if(navigator == null) {
	    final AllCallers ac = new AllCallers(this);
	    navigator = new BiDiNavigator<HMethod>() {
		public List<HMethod> next(HMethod node) {
		    return Arrays.<HMethod>asList(calls(node));
		}  
		public List<HMethod> prev(HMethod node) {
		    return Arrays.<HMethod>asList(ac.directCallers(node));
		}
	    };
	}
	return navigator;
    }
    /** cached bi-directional navigator */
    protected BiDiNavigator<HMethod> navigator = null;


    /** Returns a forward-only top-down graph navigator through
        <code>this</code> callgraph. */
    public ForwardNavigator<HMethod> getForwardNavigator() {
	return new ForwardNavigator<HMethod>() {
	    public List<HMethod> next(HMethod node) {
		return Arrays.<HMethod>asList(calls(node));
	    }
	};
    }
}
