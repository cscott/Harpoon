// InterThreadPA.java, created Mon Jan 31 20:52:46 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Collections;
import java.util.Date;

import harpoon.Util.Util;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.Temp.Temp;

import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.GenType;
import harpoon.Analysis.MetaMethods.MetaCallGraph;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.RelationEntryVisitor;


/**
 * <code>InterThreadPA</code> groups together the functions related to the
 * inter-thread analysis. Normally, this should be a part of the 
 * <code>PointerAnalysis</code> class, but that class is
 * already too big and some code segmentation is always good!<br>
 *
 * In the implementation of this class, some of the methods are static and
 * have <code>PointerAnalysis pa</code> as their first parameter.
 * <code>pa</code> stands for the <code>this</code> <i>hidden</i> parameter
 * that would exist if all those methods were in the
 * <code>PointerAnalysis</code> class.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: InterThreadPA.java,v 1.2 2002-02-25 20:58:39 cananian Exp $
 */
public abstract class InterThreadPA implements java.io.Serializable {

    /** Activates a lot of debug messages. */
    public static final boolean DEBUG  = true;
    /** Activates even more debug messages! */
    public static final boolean DEBUG2 = false;
    /** Displays some time statistics. */
    public static boolean TIMING = true;

    /* Set of all the processed threads (thread nodes). PANodes keep
       accumulating here since the beginning of the inter-thread analysis,
       without any clear(), even if some thread nodes are reanalyzed when the
       graph has changed. */
    private static final Set processed_threads = new HashSet();

    /** Do the inter-thread analysis for a method.
     *
     *	@param pa  The <code>PointerAnalysis</code> object that called 
     *  this method and that generated <code>noit_pig</code>. This parameter
     *  stands for the <i>hidden</i> <code>this</code> parameter that would
     *  exist if the methods of this class were part of the
     *  <code>PointerAnalysis</code> class.
     *	@param noit_pig  The Parallel Interaction Graph at the end of
     *	the method, as produced by the intra and inter-procedural analysis.
     *
     *  @return the Parallel Interaction Graph modeling the interaction 
     *  between the method (that is just a part of the thread that launched
     *  it) and the threads it transitively starts. 
     */
    public static ParIntGraph resolve_threads(PointerAnalysis pa,
					      ParIntGraph noit_pig) {
	// This is the set of all the analyzed threads. When the graph has
	// changed, this set is cleared, so that some of the threads are
	// reanalyzed. 
	final Set analyzed_threads = new HashSet();

	if(DEBUG)
	    System.out.println("Inter-thread analysis started ...");

	long begin_time = 0;
	if(TIMING) begin_time = System.currentTimeMillis();

	processed_threads.clear();

	ParIntGraph pig = (ParIntGraph) noit_pig.clone();

	if(DEBUG2)
	    System.out.println("Initial thread map:" + pig.tau);

	PANode nt;

	// process first the threads nt with tau(nt) = 1
	/*
	while((nt = pick_an_unanalyzed_thread(pig,analyzed_threads)) != null) {
 	    analyzed_threads.add(nt);
	    if(pig.tau.getValue(nt) != 1)
		continue;

	    MetaMethod[] ops = get_run_mmethods(pa, nt);
	    pig = interaction_nt(pa, pig, nt, ops);

	    if(pig.tau.getValue(nt) == 1) {
		pig.G.e.removeNodeHoleFromAll(nt);
		pig.removeEmptyLoads();
		pig.tau.setToZero(nt);
		Util.assert(pig.tau.getValue(nt) == 0, "Error");
	    }
	}
	analyzed_threads.clear();

	nt = pick_an_unanalyzed_thread(pig, analyzed_threads);
	if(nt == null) return pig;

	int old_tau_nt = pig.tau.getValue(nt);
	if(old_tau_nt == 1) {
	    MetaMethod[] ops = get_run_mmethods(pa, nt);
	    pig = interaction_nt(pa, pig, nt, ops);
	    int new_tau_nt = pig.tau.getValue(nt);
	    System.out.println("nt = "  + nt);
	    System.out.println("old_tau_nt = " + old_tau_nt);
	    System.out.println("new_tau_nt = " + new_tau_nt);
	    pig.G.e.removeNodeHoleFromAll(nt);
	    pig.tau.setToZero(nt);
	    Util.assert(pig.tau.getValue(nt) == 0, "Error");
	    pig.removeEmptyLoads();
	    System.out.println("RESULTING PIG (after cleaning)" + pig);
	}
	*/

	while((nt = pick_an_unanalyzed_thread(pig,analyzed_threads)) != null) {

	    if(DEBUG)
		System.out.println(nt + " was chosen");

	    MetaMethod[] ops = get_run_mmethods(pa, nt);
	    analyzed_threads.add(nt);
	    if((ops == null) || (ops.length==0) ||
	       !analyzable_run_mmethods(pa, ops)) continue;

	    ParIntGraph old_pig = pig;
	    pig = interaction_nt(pa, pig, nt, ops);

	    // ParIntGraph.DEBUG2 = true;
	    if(!pig.equals(old_pig)){
		if(DEBUG)
		    System.out.println("The graph has changed");
		analyzed_threads.clear();
	    }
	    // ParIntGraph.DEBUG2 = false;

	    analyzed_threads.add(nt);
	    processed_threads.add(nt);
	}

	// the threads that have been analyzed are no longer holes
	for(Iterator it = processed_threads.iterator(); it.hasNext();) {
	    nt = (PANode) it.next();
	    if(DEBUG)
		System.out.println("Removed thread hole: " + nt);
	    pig.G.e.removeNodeHoleFromAll(nt);
	}
	// clean up some of the useless LOAD nodes
	pig.removeEmptyLoads();

	if(DEBUG2)
	    System.out.println("RESULTING PIG (WITHOUT EMPTY LOADS): " + pig);

	if(TIMING){
	    long total_time = System.currentTimeMillis() - begin_time;
	    System.out.println("Inter-thread analysis done in " + 
			       total_time + "ms");
	}

	return pig;
    }


    /* See if the run method(s) that could be the body of a thread are 
       analyzable with regard to <code>pa</code> (i.e. <code>pa</code>
       can satisfy queries about these methods.) */
    private static boolean analyzable_run_mmethods(PointerAnalysis pa,
						   MetaMethod[] ops) {
	for(int i = 0 ; i < ops.length ; i++)
	    if((ops[i] == null) || (pa.getExtParIntGraph(ops[i]) == null))
		return false;
	return true;
    }


    /* Finds an active thread node (i.e. tau(nt) != 0) whose interactions
       with the Starter haven't been analyzed yet. */
    private static PANode pick_an_unanalyzed_thread(ParIntGraph pig,
						    Set analyzed_threads){
	Enumeration enum = pig.tau.activeThreads();
	while(enum.hasMoreElements()){
	    PANode nt = (PANode) enum.nextElement();
	    if((nt.type == PANode.INSIDE) && !analyzed_threads.contains(nt))
		return nt;
	}
	return null;
    }


    /* Returns a vector containing all the run() methods that could be
       the body of the threads abstracted by nt. */
    private static MetaMethod[] get_run_mmethods(PointerAnalysis pa,
						 PANode nt) {
	// TODO: think about the LOAD && PARAM thread nodes (not only INSIDE) 
	Quad quad = (Quad) pa.getNodeRepository().node2Code(nt.getRoot());
	Util.assert((quad instanceof NEW), nt + " has a strange instr." + 
		    " nt type: " + nt.type + " PANode.INSIDE: " +
		    PANode.INSIDE); 

	NEW q = (NEW) quad; 
	Util.assert( q != null, "Creation of " + nt + " not found!");

	HClass hclass = q.hclass();
	HMethod[] hms = hclass.getMethods();
	HMethod hm = null;
	for(int i = 0 ; i < hms.length ; i++)
	    if(hms[i].getName().equals("run") &&
	       (hms[i].getParameterTypes().length == 0)){
		hm = hms[i];
		break;
	    }

	if(hm == null) return null;

	MetaMethod mm_run = 
	    new MetaMethod(hm,new GenType[]{new GenType(hclass,GenType.MONO)});

	// some hack to cope with the fake meta call graph when every
	// method is polymorphic in its arguments.
	// TODO: try to find something more intelligent!
	MetaCallGraph mcg = pa.getMetaCallGraph();
	if(!mcg.getAllMetaMethods().contains(mm_run)) 
	    mm_run = new MetaMethod(hm,
			   new GenType[]{new GenType(hclass, GenType.POLY)});

	return new MetaMethod[]{mm_run};
    }


    /* Computes the interactions with all the threads launched by the node
       nt. If tau(nt)==1 (at most one such thread could exist), this method
       computes the interaction of the Starter with only one instance of an
       nt-thread. If tau(nt)==2 (there could be more than one thread, anything
       between 0 and infinity) a fixed-point algorithm is necessary. */
    private static ParIntGraph interaction_nt(PointerAnalysis pa,
					      ParIntGraph pig, PANode nt,
					      MetaMethod[] ops) {

	ParIntGraph new_pig = null; /* it will point to the resulting pig */
	boolean only_once = (pig.tau.getValue(nt)==1);

	if(DEBUG)
	    System.out.println("interaction_nt: " + nt + 
			       (only_once?" only once":" many times"));

	// save the old outside edge set
	PAEdgeSet old_O = pig.G.O;
	// consider only the outside edges read in // with an "nt" thread.
	pig.G.O = construct_new_O(pig, nt);

	if(only_once){ // a single interaction is enough
	    // make sure this is the only one the thread interaction is
	    // resolved for the thread "nt" (because it is launched only once).
	    pig.tau.dec(nt);
	    new_pig = interact_once(pa, pig, nt, ops);
	}
	else{ // a fixed-point algorithm is necessary in this case
	    new_pig = pig; // before the 1st iteration
	    while(true) {
		ParIntGraph previous_pig = new_pig;
		new_pig = interact_once(pa, previous_pig, nt, ops);
		if(new_pig.equals(previous_pig)) break;
	    }
	}

	// add the old, unconsidered outside edges to the new graph  ...
	new_pig.G.O.union(old_O);
	// [propagate the escape info on the newly added edges]
	new_pig.G.propagate();
	// and restore the graph received as argument
	pig.G.O = old_O;

	if(DEBUG2)
	    System.out.println("RESULTING PIG (unclean yet)" + new_pig);

	return new_pig;
    }


    /* Constructs a new set of outside edges for pig, containing only those
       outside edges that are read in parallel with an nt thread.
       Returns the new set of edges. */
    private static PAEdgeSet construct_new_O(ParIntGraph pig, PANode nt) {

	if(PointerAnalysis.RECORD_ACTIONS) {
	    PAEdgeSet new_O = new LightPAEdgeSet();
	    
	    Iterator it_loads = pig.ar.parallelLoads(nt);
	    while(it_loads.hasNext()){
		PALoad load = (PALoad) it_loads.next();
		new_O.addEdge(load.n1, load.f, load.n2);
	    }

	    return new_O;
	}
	else
	    return (PAEdgeSet) pig.G.O.clone();
    }

    /* Computes the interaction with a SINGLE instance of a thread launched
       by the nt node. This involves separately computing the interactions
       with all the possible run() methods (the body of the thread) and
       joining the results. */
    private static ParIntGraph interact_once(PointerAnalysis pa,
					     ParIntGraph pig, PANode nt,
					     MetaMethod[] ops) {
	int nb_ops = ops.length;
	Util.assert(nb_ops > 0, "No run method for the thread" + nt);

	// compute the first term of the join operation:
	// the interaction with the first run() method
	ParIntGraph pig_after = interact_once_op(pa, pig, nt, ops[0]);
	
	// join to it all the other terms (interactions with all the
	// other run() methods).
	for(int i = 1 ; i < nb_ops ; i++)
	    pig_after.join(interact_once_op(pa, pig, nt, ops[i])); 

	return pig_after;
    }


    /* Computes the interaction between the Starter and a SINGLE thread having
       the node nt as a receiver and op as the run() body function. */
    private static ParIntGraph interact_once_op(PointerAnalysis pa,
						ParIntGraph pig_starter,
						PANode nt, MetaMethod op) {
	ParIntGraph pig[] = new ParIntGraph[2];
	pig[0] = pig_starter;

	if(DEBUG)
	    System.out.println("interact_once_op; op = " + op.getHMethod());

	// some thread specialization if necessary
	if(PointerAnalysis.THREAD_SENSITIVE ||
	   PointerAnalysis.WEAKLY_THREAD_SENSITIVE)
	    pig[1] = pa.getSpecializedExtParIntGraph(op);
	else
	    pig[1] = pa.getExtParIntGraph(op);

	 if(DEBUG2){
	    System.out.println("interact_once_op:");
	    System.out.println("  nt node: " + nt);
	    System.out.println("  run method: " + op);
	    System.out.println("PIG STARTER: " + pig[0]);
	    System.out.println("PIG STARTEE: " + pig[1]);
 	}
	
	PANode[] params = pa.getParamNodes(op);

	Relation mu[] = compute_mappings(pig, nt, params);
	
	Set actives = active_threads_outside_startee(pa, pig[0]);

	if(DEBUG2)
	    System.out.println("interact_once_op: actives: " + actives);

	ParIntGraph new_pig = build_new_pig(pig,mu,params[0],nt,actives);

	if(DEBUG2){
	    System.out.println("NEW GRAPH:");
	    System.out.println(new_pig);
	}

	return new_pig;
    }


    // activates the use of the new mapping constraints
    private static boolean NEW_MAPPING_CONSTRAINTS = true;
    public static boolean VERY_NEW_MAPPINGS = true;
    static {
	if(NEW_MAPPING_CONSTRAINTS) {
	    System.out.println("InterThreadPA: NEW_MAPPING_CONSTRAINTS");
	    if(VERY_NEW_MAPPINGS)
		System.out.println("InterThreadPA: VERY_NEW_MAPPING");
	}
    }
    // generates lots of debug messages about the construction of mu
    private static boolean DEBUG_MU = false;


    private static Relation[] compute_mappings
	(ParIntGraph pig[], PANode nt, PANode[] params) {

	if(NEW_MAPPING_CONSTRAINTS)
	    return compute_mu(pig, nt, params[0]);

	Relation mu[] = compute_initial_mappings(pig, nt, params);
	
	if(DEBUG_MU) {
	    System.out.println("INITIAL MAPPINGS:");
	    System.out.println("starter -> startee:" + mu[0]);
	    System.out.println("startee -> starter:" + mu[1]);
	}
	
	concretize_loads(pig, mu);
	
	if(DEBUG_MU) {
	    System.out.println("AFTER CONCRETIZE LOADS:");
	    System.out.println("starter -> startee:" + mu[0]);
	    System.out.println("startee -> starter:" + mu[1]);
	}

	compute_final_mappings(pig,mu,nt);

	if(DEBUG_MU) {
	    System.out.println("FINAL MAPPINGS:");
	    System.out.println("starter -> startee:" + mu[0]);
	    System.out.println("startee -> starter:" + mu[1]);
	}

	return mu;
    }


    /* Sets the initial mappings: class nodes, parameter->thread node.
    	Parameters:
    	 pig[0] - the parallel interaction graph of the Starter;
    	 pig[1] - the parallel interaction graph of the Startee.
    
    	Returns:
    	 mu[0] - the mapping of the nodes from the Starter <br>;
    	 mu[1] - the mapping of the nodes from the Startee <br>. */
    private static Relation[] compute_initial_mappings(ParIntGraph[] pig,
						       PANode nt,
						       PANode[] params){
	// Paranoic debug! Trust no one!
	Util.assert(params.length == 1, "Thread function with too many args");

	Relation mu0 = new LightRelation();
	map_static_nodes(pig[0],mu0);

	Relation mu1 = new LightRelation();
	mu1.add(params[0],nt);
	map_static_nodes(pig[1],mu1);

	return (new Relation[]{mu0,mu1});
    }


    /* Maps the static nodes that appear in pig to themselves. Only those
       static nodes that appear as sources of arcs need to be mapped; if
       necessary, the others wil be mapped by the rest of the algorithm.
       (the matching goes always "forward" on the edges, never "backward", so
       it's necessary to trigger it just in the sources of the edges. */
    private static void map_static_nodes(ParIntGraph pig, Relation mu) {
	process_STATICs(pig.G.O.allSourceNodes(), mu);
	process_STATICs(pig.G.I.allSourceNodes(), mu);
    }
    // aux method for map_static_nodes
    private static void process_STATICs(final Set set, final Relation mu) {
	for(Iterator it = set.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(node.type == PANode.STATIC)
		mu.add(node,node);
	}
    }



    /* Computes the mappings by matching outside edges from one graph
       against inside edges from the other one. */
    private static void concretize_loads(ParIntGraph[] pig, Relation[] mu) {

	PAWorkList W[] = { new PAWorkList(), new PAWorkList() };

	Relation new_info[] = { (Relation)(mu[0].clone()),
				(Relation)(mu[1].clone()) };
	
	W[0].addAll(mu[0].keys());
	W[1].addAll(mu[1].keys());

	while(true){
	    int i,ib;
	    if(!W[0].isEmpty()) { i=0; ib=1; }
	    else 
		if(!W[1].isEmpty()) { i=1; ib=0; }
		else{
		    Matching.rule0(mu, W, new_info);
		    if(W[0].isEmpty() && W[1].isEmpty()) break;
		    else continue;
		}

	    PANode node = (PANode) W[i].remove();

	    // new mappings for node
	    Set new_mappings = new HashSet(new_info[i].getValues(node));
	    new_info[i].removeKey(node);

	    // Matching.rule0(node,new_mappings,pig,W,mu,new_info,i,ib);

	    Matching.rule2(node,new_mappings,pig,W,mu,new_info,i,ib);
	    Matching.rule22(node,new_mappings,pig,W,mu,new_info,i,ib);
	    Matching.rule3(node,new_mappings,pig,W,mu,new_info,i,ib);
	    Matching.rule32(node,new_mappings,pig,W,mu,new_info,i,ib);
	}
    }


    /* Computes the final mappings. Every node from the Starter and the Startee
       will be put in the new graph (node-mu->node) except for the parameter
       node of the Startee run() method; this one will be mapped to nt. */
    private static void compute_final_mappings(final ParIntGraph[] pig,
					       final Relation[] mu,
					       final PANode nt){
	PANodeVisitor visitor_starter = new PANodeVisitor(){
		public void visit(PANode node){
		    mu[0].add(node, node);
		}
	    };

	pig[0].forAllNodes(visitor_starter);

	PANodeVisitor visitor_startee = new PANodeVisitor(){
		public void visit(PANode node){
		    int type = node.type();
		    if ((type == PANode.PARAM) ||
                        (node == ActionRepository.THIS_THREAD))
			mu[1].add(node, nt);
		    else
			mu[1].add(node, node);
		}
	    };

	pig[1].forAllNodes(visitor_startee);
    }



    /////////////////////////////////////////////////////////////////////
    ////////////////// New mapping constraints START ////////////////////

    private static boolean USE_BAR = false;
    static {
	if(USE_BAR)
	    System.out.println("USE_BAR");
    }

    // Compute the mappings using the new constraints
    private static Relation[] compute_mu
	(ParIntGraph pig[], PANode nt, PANode param) {

	ParIntGraph initial_pig1 = pig[1];
	if(USE_BAR) {
	    pig[1] = pig[1].getBarVersion();
	}

	//System.out.println("Beginning of compute_mu:");
	//System.out.println("initial_pig1.G.O = " + initial_pig1.G.O);
	//System.out.println("pig[1].G.O = " + pig[1].G.O);


	// initialize the mappings mu
	Relation mu[] = get_initial_mu(pig, nt, param);
	if(DEBUG_MU) {
	    System.out.println("INITIAL MAPPINGS:");
	    System.out.println("starter -> startee:" + mu[0]);
	    System.out.println("startee -> starter:" + mu[1]);
	}

	//System.out.println("BEFORE extend_mu pig[0] = " + pig[0]);
	//System.out.println("BEFORE extend_mu pig[1] = " + pig[1]);

	// extend the mappings mu according to the inference rules
	extend_mu(mu, pig);
	if(DEBUG_MU) {
	    System.out.println("MAPPINGS AFTER extend_mu:");
	    System.out.println("starter -> startee:" + mu[0]);
	    System.out.println("startee -> starter:" + mu[1]);
	}

	if(VERY_NEW_MAPPINGS) {
	    compute_final_mappings(pig, mu, nt);
	    if(DEBUG_MU) {
		System.out.println("MAPPINGS AFTER extend_mu:");
		System.out.println("starter -> startee:" + mu[0]);
		System.out.println("startee -> starter:" + mu[1]);
	    }
	}

	if(USE_BAR)
	    mu = get_adjusted_mu(mu);

	if(DEBUG_MU) {
	    System.out.println("FINAL MAPPINGS:");
	    System.out.println("starter -> startee:" + mu[0]);
	    System.out.println("startee -> starter:" + mu[1]);
	}

	if(USE_BAR) {
	    // restore the initial graph
	    pig[1] = initial_pig1;
	}

	return mu;
    }

    // Get the pair of initial mappings: each node in entity i
    // (starter/startee) is mapped to itself in mu[i], except for the
    // only param node of the 
    private static Relation[] get_initial_mu
	(ParIntGraph pig[], PANode nt, PANode param) {

	Relation mu[] = 
	    new Relation[] { new RelationImpl(), new RelationImpl() };

	// map the "this" param node of the startee to the thread node nt
	mu[1].add(param, nt);
	// map the dummy current thread from the startee to nt
	mu[1].add(ActionRepository.THIS_THREAD, nt);

	if(VERY_NEW_MAPPINGS) {
	    // map each static node to itself
	    for(int i = 0; i < 2; i++) {
		for(Iterator it = pig[i].allNodes().iterator();
		    it.hasNext(); ) {
		    PANode node = (PANode) it.next();
		    if(node.type == PANode.STATIC)
			mu[i].add(node, node);
		}
	    }
	    return mu;
	}
	
	// map every node to itself, except the "this" param node of the
	// startee that will disappear after the thread interaction
	//  a. first map node to node, \forall node
	for(int i = 0; i < 2; i++) {
	    for(Iterator it = pig[i].allNodes().iterator(); it.hasNext(); ) {
		PANode node = (PANode) it.next();
		mu[i].add(node, node);
	    }
	}
	//  b. now, delete the mapping params[0], params[0]
	mu[1].remove(param, param);

	mu[1].remove(ActionRepository.THIS_THREAD,
		     ActionRepository.THIS_THREAD);

	return mu;
    }


    // extend the mappings using the inference rules coded into Matching
    private static void extend_mu(Relation mu[], ParIntGraph pig[]) {
	PAWorkList W[] = { new PAWorkList(), new PAWorkList() };

	Relation new_info[] = { (Relation)(mu[0].clone()),
				(Relation)(mu[1].clone()) };
	
	W[0].addAll(mu[0].keys());
	W[1].addAll(mu[1].keys());

	while(true){
	    int i,ib;

	    if(!W[0].isEmpty()) { i=0; ib=1; }
	    else 
		if(!W[1].isEmpty()) { i=1; ib=0; }
		else {
		    Matching.rule0(mu, W, new_info);

		    for(int k = 0; k < 2; k++)
			Matching.aliasingSameScopeRule
			    (mu[k], pig[k], W[k], new_info[k]);

		    if(W[0].isEmpty() && W[1].isEmpty()) break;
		    else continue;
		}

	    PANode node = (PANode) W[i].remove();

	    // new mappings for node
	    Set new_mappings = new HashSet(new_info[i].getValues(node));
	    new_info[i].removeKey(node);

	    Matching.rule2(node,new_mappings,pig,W,mu,new_info,i,ib);
	    Matching.rule22(node,new_mappings,pig,W,mu,new_info,i,ib);
	    Matching.rule3(node,new_mappings,pig,W,mu,new_info,i,ib);
	    Matching.rule32(node,new_mappings,pig,W,mu,new_info,i,ib);
	}

    }


    // Adjust the relations mu[0], mu[1]: bar(n) -> n, \forall n.
    private static Relation[] get_adjusted_mu(Relation[] mu) {
        return
	    new Relation[] { get_adjusted_mu(mu[0]),
			     get_adjusted_mu(mu[1]) };
    }


    // Go over the mapping mu and replace every node with its
    // genuine version.
    private static Relation get_adjusted_mu(Relation mu) {
	final Relation unbar_mu = new RelationImpl();
	mu.forAllEntries(new RelationEntryVisitor() {
		public void visit(Object key, Object value) {
		    PANode node1 = ((PANode) key).getGenuine();
		    PANode node2 = ((PANode) value).getGenuine();
		    unbar_mu.add(node1, node2);
		}
	    });
	return unbar_mu;
    }

    ////////////////// New mapping constraints END //////////////////////
    /////////////////////////////////////////////////////////////////////


    /* Builds the new graph using the graphs from the starter and the startee
       and the mu mappings. */
    private static ParIntGraph build_new_pig(ParIntGraph[] pig, Relation[] mu,
					     PANode nparam, PANode nt,
					     Set active_threads_in_starter) {

	ParIntGraph new_pig = new ParIntGraph();

	new_pig.insertAllButArEo(pig[0],mu[0], true);
	new_pig.insertAllButArEo(pig[1],mu[1], false,
				 Collections.singleton(nparam));

	// compute the escape function for the new graph
	new_pig.G.propagate();

	if(PointerAnalysis.RECORD_ACTIONS) {
	    // all the actions from starter run in parallel with the threads
	    // that are started by the startee (it is not necessary to do a
	    // transitive closure, the threads that are indirectly launched
	    // by the startee will be considered when their direct started
	    // is analyzed.
	    bring_starter_actions(pig[0], new_pig, mu[0],
				  pig[1].tau.activeThreadSet(), nt);
	    
	    // all the actions from startee run in parallel with all the other
	    // threads that could be (transitively) started by the starter.
	    // Transitive closure is necessary since we may never revisit this
	    // actions (we must put the parallel relation action || thread now)
	    bring_startee_actions(pig[1], new_pig, mu[1],
				  active_threads_in_starter, nt);
	}

	return new_pig;
    }


    /* Returns the set of all the threads that can run in parallel with the
       startee thread: the threads that have already been processed +
       the threads that are still unanalyzed and the transitive closure of
       them (the threads that are directly/indirectly started by them)
       This method is expected to be called after adjusting the tau function
       for the startee node; that's why it doesn'y need to take it as an
       argument. */
    private static Set active_threads_outside_startee(PointerAnalysis pa,
						      ParIntGraph pig_starter){
	Set active_threads = new HashSet(pig_starter.tau.activeThreadSet());
	thread_closure(pa, active_threads);
	active_threads.addAll(processed_threads);
	return active_threads;
    }


    /* Transitively extends the set "threads" according to the 
       relation "thread nt1 launched thread nt2" */
    private static void thread_closure(PointerAnalysis pa, Set threads){
	PAWorkList W = new PAWorkList();
	W.addAll(threads);

	while(!W.isEmpty()){
	    PANode nt1 = (PANode) W.remove();

	    if(nt1.type != PANode.INSIDE) continue;

	    MetaMethod[] ops = get_run_mmethods(pa, nt1);
	    if(!analyzable_run_mmethods(pa, ops)) continue;
	    for(int i = 0; i < ops.length; i++){
		PAThreadMap tau_nt1 = pa.getExtParIntGraph(ops[i]).tau;
		Enumeration enum = tau_nt1.activeThreads();
		while(enum.hasMoreElements()){
		    PANode nt2 = (PANode) enum.nextElement();
		    if(threads.add(nt2)) W.add(nt2);
		}
	    }
	}
    }


    /* Adds the actions from the starter to the new graph. */
    private static void bring_starter_actions(final ParIntGraph pig_starter,
					      final ParIntGraph new_pig,
					      final Relation mu_starter,
					      final Set startee_active_threads,
					      final PANode nt){

	mu_starter.add(ActionRepository.THIS_THREAD,
		       ActionRepository.THIS_THREAD);

	ActionVisitor act_visitor_starter = new ActionVisitor(){
		public void visit_ld(PALoad load){
		    Util.assert(mu_starter.contains(load.n2,load.n2),
				load.n2 + "->" + load.n2 +
				"  should be in mu_starter");

		    new_pig.ar.add_ld(mu_starter.getValues(load.n1),
				      load.f,
				      load.n2,
				      mu_starter.getValues(load.nt),
				      Collections.EMPTY_SET);
		}
		public void visit_sync(PASync sync){
		    new_pig.ar.add_sync(sync.project(mu_starter),
					Collections.EMPTY_SET);
		}
	    };

	pig_starter.ar.forAllActions(act_visitor_starter);

	ParActionVisitor par_act_visitor_starter = new ParActionVisitor() {

		public void visit_par_ld(PALoad load, PANode nt2){
		    Util.assert(mu_starter.contains(load.n2,load.n2),
				load.n2 + "->" + load.n2 +
				"  should be in mu_starter");

		    Set parallel_threads =
			new HashSet(mu_starter.getValues(nt2));

		    if(nt2 == nt)
			parallel_threads.addAll(startee_active_threads);

		    new_pig.ar.add_ld(mu_starter.getValues(load.n1),
				      load.f,
				      load.n2,
				      mu_starter.getValues(load.nt),
				      parallel_threads);
		}

		public void visit_par_sync(PASync sync, PANode nt2){
		    Set parallel_threads = 
			new HashSet(mu_starter.getValues(nt2));

		    if(nt2 == nt)
			parallel_threads.addAll(startee_active_threads);

		    new_pig.ar.add_sync(sync.project(mu_starter),
					parallel_threads);
		}
	    };

	pig_starter.ar.forAllParActions(par_act_visitor_starter);
    }


    /* Adds the actions from the startee to the new graph. */
    private static void bring_startee_actions(final ParIntGraph pig_startee,
					      final ParIntGraph new_pig,
					      final Relation mu_startee,
					      final Set starter_active_threads,
					      final PANode nt){

	mu_startee.add(ActionRepository.THIS_THREAD, nt);

	ActionVisitor act_visitor_startee = new ActionVisitor(){
		public void visit_ld(PALoad load){
		    if(!mu_startee.contains(load.n2,load.n2)) return;
		    new_pig.ar.add_ld(mu_startee.getValues(load.n1),
				      load.f,
				      load.n2,
				      mu_startee.getValues(load.nt),
				      starter_active_threads);
		}
		public void visit_sync(PASync sync){
		    new_pig.ar.add_sync(sync.project(mu_startee),
					starter_active_threads);
		}
	    };

	pig_startee.ar.forAllActions(act_visitor_startee);

	ParActionVisitor par_act_visitor_startee = new ParActionVisitor(){
		public void visit_par_ld(PALoad load, PANode nt2){
		    if(!mu_startee.contains(load.n2,load.n2)) return;
		    new_pig.ar.add_ld(mu_startee.getValues(load.n1),
				      load.f,
				      load.n2,
				      mu_startee.getValues(load.nt),
				      mu_startee.getValues(nt2));
		}
		public void visit_par_sync(PASync sync, PANode nt2){
		    new_pig.ar.add_sync(sync.project(mu_startee),
					mu_startee.getValues(nt2));
		}
	    };

	pig_startee.ar.forAllParActions(par_act_visitor_startee);

    }

}
