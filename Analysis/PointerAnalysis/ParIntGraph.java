// ParIntGraph.java, created Sun Jan  9 15:40:59 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Set;
import java.util.HashSet;


/**
 * <code>ParIntGraph</code> Parallel Interaction Graph
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: ParIntGraph.java,v 1.1.2.8 2000-02-11 06:12:07 salcianu Exp $
 */
public class ParIntGraph {

    /** Default (empty) graph. It doesn't contain any information.  */
    public static final ParIntGraph EMPTY_GRAPH = new ParIntGraph();

    /** Points-to escape graph that summarizes the points-to and escape
	information for the current thread. */
    public PointsToGraph G;
    
    /** The parralel thread map; it attaches to each thread node nT, an 
	integer from the set {0,1,2} where 2 signifies the possibility
	that multiple instances of nT execute in parallel with the current
	thread. */
    public PAThreadMap tau;
    
    /** Maintains the actions executed by the analysed code and the parallel\
	action relation. <code>alpha</code> and <code>pi</code> from the 
	original paper have been merged into this single field for efficiency
	reasons. */
    public ActionRepository ar;
    
    
    /** Creates a <code>ParIntGraph</code>. */
    public ParIntGraph() {
	G   = new PointsToGraph();
	tau = new PAThreadMap();
	ar  = new ActionRepository();
    }
    
    /** <code>join</code> combines two <code>ParIntGraph</code>s in \
	a control-flow join point. */
    public void join(ParIntGraph pig2){
	G.join(pig2.G);
	tau.join(pig2.tau);
	ar.join(pig2.ar);
    }

    /** Check the equality of two <code>ParIntGraph</code>s. */
    public boolean equals(Object o){
	if(o==null) return false;
	ParIntGraph pig2 = (ParIntGraph)o;
	// return G.equals(pig2.G) && tau.equals(pig2.tau);
	if(!G.equals(pig2.G)){
	    if(PointerAnalysis.DEBUG2)
		System.out.println("The graphs are different");
	    return false;
	}
	if(!tau.equals(pig2.tau)){
	    if(PointerAnalysis.DEBUG2)
		System.out.println("The tau's are different");
	    return false;
	}
	if(!ar.equals(pig2.ar)){
	    if(PointerAnalysis.DEBUG2)
		System.out.println("The ar's are different");
	    return false;
	}
	return true;
    }

    /** Private constructor for <code>clone</code> and 
	<code>keepTheEssential</code>. */
    private ParIntGraph(PointsToGraph G,PAThreadMap tau,ActionRepository ar){
	this.G   = G;
	this.tau = tau;
	this.ar  = ar;
    }

    /** <code>clone</code> produces a copy of the <code>this</code>
     * Parallel Interaction Graph. */
    public Object clone(){
	return new ParIntGraph((PointsToGraph)G.clone(),
			       (PAThreadMap)tau.clone(),
			       (ActionRepository)ar.clone());
    }

    
    /** Produces a <code>ParIntGraph</code> containing only the \
	nodes that could be reached from the outside.
	(i.e. via parameters,
	class nodes, normally or exceptionally returned nodes or the
	started thread nodes) */
    public ParIntGraph keepTheEssential(PANode[] params, boolean is_main){
	HashSet remaining_nodes = new HashSet();
	remaining_nodes.addAll(tau.activeThreadSet());
	PointsToGraph _G = 
	    G.keepTheEssential(params, remaining_nodes, is_main);
	PAThreadMap _tau = (PAThreadMap) tau.clone(); 
	//TODO: find something more intelligent!
	ActionRepository _ar = (ActionRepository) ar.clone(); 
	return new ParIntGraph(_G,_tau,_ar);
    }


    /** Pretty-print function for debug purposes. 
	Two equal <code>ParIntGraph</code>s are guaranteed to have the same
	string representation. */
    public String toString(){
	return "\nParIntGraph{\n" + G + " " + tau + ar + "}"; 
    }

}










