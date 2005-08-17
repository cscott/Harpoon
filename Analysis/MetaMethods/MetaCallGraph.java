// MetaCallGraph.java, created Mon Mar 13 15:53:31 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MetaMethods;

import java.util.Set;
import java.util.List;
import java.util.Arrays;

import java.io.PrintStream;

import harpoon.IR.Quads.CALL;

import jpaul.DataStructs.Relation;

import jpaul.Graphs.Navigator;
import jpaul.Graphs.ForwardNavigator;
import jpaul.Graphs.DiGraph;

/**
 * <code>MetaCallGraph</code> is for meta methods what <code>callGraph</code>
 is for &quot;normal&quot; methods. It provides information on what meta
 methods are called by a given meta method [at a specific call site].
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: MetaCallGraph.java,v 1.9 2005-08-17 23:40:52 salcianu Exp $
 */

public abstract class MetaCallGraph extends DiGraph<MetaMethod>
    implements java.io.Serializable {
    
    /** Returns the meta methods that can be called by <code>mm</code>. */
    public abstract MetaMethod[] getCallees(MetaMethod mm);
    
    /** Returns the meta methods that can be called by <code>mm</code>
	at the call site <code>q</code>. */
    public abstract MetaMethod[] getCallees(MetaMethod mm, CALL cs);

    /** Returns the set of all the call sites in the code of the meta-method
	<code>mm</code>. */
    public abstract Set getCallSites(MetaMethod mm);
    
    /** Returns the set of all the meta methods that might be called during the
	execution of the program. */
    public abstract Set getAllMetaMethods();

    /** Returns the set of all the meta methods that might be called, directly
	or indirectly, by the meta method <code>mm</code>. It's just the
	transitive closure of the <code>getCallees</code> method. */
    public abstract Set getTransCallees(MetaMethod mm);

    /** Computes the <i>split</i> relation. This is a <code>Relation</code>
	that associates to each <code>HMethod</code> the set of
	<code>MetaMethod</code>s specialized from it. */
    public abstract Relation getSplitRelation();

    /** Returns the set of the meta-methods that could be called as the 
	body of some thread. */
    public abstract Set getRunMetaMethods();

    /** Nice pretty-printer for debug purposes. */
    public abstract void print(PrintStream ps, boolean detailed_view,
			       MetaMethod root);

    public Set/*<MetaMethod>*/ getRoots() {
	// we conservatively return all meta-methods
	return getAllMetaMethods();
    }
    
    /** Returns a bi-directional top-down graph navigator through
	<code>this</code> meta-callgraph.  Complexity: BIG; at least
	linear in the number of nodes and edges in the call graph.
	Therefore, we cache its result internally. */
    public Navigator<MetaMethod> getNavigator() {
	if(navigator == null) {
	    final MetaAllCallers mac = new MetaAllCallers(this);   
	    navigator = new Navigator<MetaMethod>() {
		public List<MetaMethod> next(MetaMethod mm) {
		    return Arrays.<MetaMethod>asList(getCallees(mm));
		}  
		public List<MetaMethod> prev(MetaMethod mm) {
		    return Arrays.<MetaMethod>asList(mac.getCallers(mm));
		}
	    };
	}
	return navigator;
    }
    protected Navigator<MetaMethod> navigator = null;


    /** Returns a forward-only navigator through <code>this</code>
        meta-callgraph.  Complexity: O(1).*/
    public ForwardNavigator<MetaMethod> getForwardNavigator() {
	return new ForwardNavigator<MetaMethod>() {
	    public List<MetaMethod> next(MetaMethod mm) {
		return Arrays.<MetaMethod>asList(getCallees(mm));
	    }  
	};
    }
}
