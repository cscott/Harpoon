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
 * @version $Id: ParIntGraph.java,v 1.1.2.5 2000-01-18 04:49:40 salcianu Exp $
 */
public class ParIntGraph {

    /** Default (empty) graph. It doesn't contain any information.  */
    public static final ParIntGraph EMPTY_GRAPH = new ParIntGraph();

    // Points-to escape graph that summarizes the points-to and escape
    // information for the current thread
    public PointsToGraph G;
    
    // The parralel thread map; it attaches to each thread node nT, an 
    // integer from the set {0,1,2} where 2 signifies the possibility
    // that multiple instances of nT execute in parallel with the current
    // thread
    public PAThreadMap tau;
    
    // The set of actions executed by the analyzed computation
    // PAActionSet alpha;
    
    // The parallel action relation that records ordering information
    // about the actions of the current thread and threads that execute
    // in parallel with it
    // PAParallelAction pi;
    
    /** Creates a <code>ParIntGraph</code>. */
    public ParIntGraph() {
	G = new PointsToGraph();
	tau = new PAThreadMap();
    }
    
    /** <code>join</code> combines two <code>ParIntGraph</code>s in
     *  a control-flow join poin */
    public void join(ParIntGraph pig2){
	G.join(pig2.G);
	tau.join(pig2.tau);
	// alpha.join(pig2.alpha);
	// pi.join(pig2.alpha);
    }

    /** Check the equality of two <code>ParIntGraph</code>s */
    public boolean equals(Object o){
	if(o==null) return false;
	ParIntGraph pig2 = (ParIntGraph)o;
	// return G.equals(pig2.G) && tau.equals(pig2.tau);
	if(!G.equals(pig2.G)){
	    //System.out.println("The graphs are different");
	    return false;
	}
	if(!tau.equals(pig2.tau)){
	    //System.out.println("The tau's are different");
	    return false;
	    
	}
	return true;
    }

    /** Private constructor for <code>clone</code> and 
     * <code>keepTheEssential</code>. */
    private ParIntGraph(PointsToGraph _G,PAThreadMap _tau 
			/*,PAActionSet _alpha, PAParallelAction _pi*/){
	G     = _G;
	tau   = _tau;
	// alpha = _alpha;
	// pi    = _pi;
    }

    /** <code>clone</code> produces a copy of the <code>this</code>
     * Parallel Interaction Graph. */
    public Object clone(){
	return new ParIntGraph((PointsToGraph)G.clone(),
			       (PAThreadMap)tau.clone()
			       /* ,alpha.clone(),pi.clone() */);
    }

    
    /** Produces a <code>ParIntGraph</code> containing only the 
     *  nodes that could be reached from <code>root_set</code>. */
    public ParIntGraph keepTheEssential(PANode[] params,boolean is_main){
	HashSet remaining_nodes = new HashSet();
	PointsToGraph _G = 
	    G.keepTheEssential(params, remaining_nodes, is_main);
	PAThreadMap _tau = tau.keepTheEssential(remaining_nodes); 
	return new ParIntGraph(_G,_tau);
    }


    /** pretty-print function for debug purposes */
    public String toString(){
	return "\nParIntGraph{\n" + G + " " + tau + /* alpha + pi + */ "}"; 
    }

}


