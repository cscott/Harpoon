// InterProcPA.java, created Tue Jan 18 11:04:20 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;

import harpoon.IR.Quads.CALL;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Temp.Temp;

import harpoon.ClassFile.HMethod;

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;

/**
 * <code>InterProcPA</code> is a &quot;functional&quot; class (i.e. it 
 * contains just some methods, no persistent data) that wraps
 * the inter-procedural part of the pointer analysis. Normally, this should
 * be a part of the <code>PointerAnalysis</code>, but that class is already
 * too big and some code segmentation is always good!
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: InterProcPA.java,v 1.1.2.19 2000-03-30 03:05:14 salcianu Exp $
 */
abstract class InterProcPA {

    public static final boolean DEBUG = false;

    /** Displays some warnings for the call sites with 0 callees etc. 
	This is not necessarily an error! For example, if an application
	is never instatiating a SecurityManager, each call to a method
	from this class has 0 callees! */
    public static boolean WARNINGS = true;

    /** Analyzes the call site <code>q</code> inside 
	<code>current_method</code>. If analyzing the call is not possible
	(e.g. one of the callees is native, hence unanalyzable), the call
	site is skipped and all the parameters are marked as escaping down
	through that call site.<br>
	Parameters:<br>
	<ul>
	<li><code>pig_before</code> is the parallel interaction graph at
	the program point just before the call site; this graph will be 
	mutated, it is the responsability of the caller to clone it if it
	is necessary somewhere else.
	<li><code>pa</code> is the <code>PointerAnalysis</code> object that
	calls this method. <code>pa</code> is used to extract the external
	parallel interaction graphs of the callees.
	</ul>
	Return result:<b>
	Two graphs are returned: one for the normal return from the procedure,
	the other one for a return due to an exception. */
    public static ParIntGraphPair analyze_call(MetaMethod current_mmethod,
					       CALL q,
					       ParIntGraph pig_before,
					       PointerAnalysis pa){
	MetaCallGraph mcg = pa.getMetaCallGraph();
	NodeRepository node_rep = pa.getNodeRepository(); 
	MetaMethod[] mms = mcg.getCallees(current_mmethod,q);
	int nb_callees = mms.length;

	if(nb_callees < 1){
	    if(WARNINGS){
		System.out.print("Error: CALL site with no callee! ");
		System.out.print(current_mmethod);
		System.out.println(" " + q);
	    }
	    return skip_call(q,pig_before,node_rep);
	}

	if(DEBUG){
	    System.out.println("Inter-procedural analysis");
	    System.out.println(" " + q + "\n");
	}

	ParIntGraph pigs[] = new ParIntGraph[nb_callees];
	for(int i = 0; i < nb_callees; i++){
	    HMethod hm = mms[i].getHMethod();
	    // TODO: the second part of the test is for debug only
	    if(!PointerAnalysis.analyzable(hm) ||
	       hm.getName().equals("unanalyzed"))
		return skip_call(q,pig_before,node_rep);
	    else
		pigs[i] = pa.getExtParIntGraph(mms[i], false);
	}

	// specialize the graphs of the callees for the context sensitive PA
	if(PointerAnalysis.CALL_CONTEXT_SENSITIVE)
	    for(int i = 0; i < pigs.length; i++){
		if(DEBUG)
		    System.out.println("Pig_callee before specialization:" +
				       pigs[i]);
		pigs[i] = pa.getSpecializedExtParIntGraph(mms[i],q);
		if(DEBUG)
		    System.out.println("Pig_callee after  specialization:" +
				       pigs[i]);
	    }

	int counter = 0;
	for(int i = 0; i < nb_callees; i++)
	    if(pigs[i] != null) counter++;

	// If none of the caller has been analyzed yet, do not do anything
	// (this can happen only in the strongly connected components of 
	// mutually recursive methods).
	if(counter == 0)
	    return new ParIntGraphPair(pig_before, pig_before);

	if(counter != nb_callees){
	    // some of the graphs are yet unknown (this situation appears
	    // in strongly connected components); do not consider them.
	    ParIntGraph pigs2[] = new ParIntGraph[counter];
	    MetaMethod  mms2[]  = new MetaMethod[counter];
	    int k = 0;
	    for(int i = 0; i < pigs.length; i++)
		if(pigs[i] != null){
		    pigs2[k] = pigs[i];
		    mms2[k]  = mms[i]; 
		    k++;
		}

	    pigs = pigs2;
	    mms  = mms2;
	    nb_callees = counter;
	}

	// special case: only one callee; no ParIntGraph is cloned
	if(nb_callees == 1){
	    /// System.out.println(hms[0]);
	    return mapUp(q,pig_before,pigs[0],pa.getParamNodes(mms[0]));
	}

	// more than one callee: the graph after the CALL is a join of all
	// the graphs obtained by combining, for each callee hms[i], the graph
	// before the CALL with the graph at the end of hms[i]. The
	// implementation is complicated by the need of doing only the
	// clone() (cloning a ParIntGraph is very expensive)

	// compute the first term of the join operation
	ParIntGraphPair pp_after = mapUp(q, (ParIntGraph)pig_before.clone(),
					 pigs[0], pa.getParamNodes(mms[0]));

	// join to it all the others, except the last one
	for(int i = 1; i < nb_callees - 1; i++)
	    pp_after.join(mapUp(q, (ParIntGraph)pig_before.clone(),
				pigs[i], pa.getParamNodes(mms[i])));

	// finally, join with the graph modeling the interaction with
	// the last callee
	pp_after.join(mapUp(q, pig_before, pigs[nb_callees-1],
			    pa.getParamNodes(mms[nb_callees-1])));

	return pp_after;
    }


    /** Updates the ParIntGraph when the CALL is skipped. Two graphs are
	returned: one for the normal return from the procedure, the other
	one for a return due to an exception. */
    private static ParIntGraphPair skip_call(CALL q, ParIntGraph pig_caller,
					     NodeRepository node_rep){
	// Construct the set S_M of the objects escaped through this unanalyzed
	// method invocation site.
	Set S_M = new HashSet();
	Temp[] params = q.params();
	for(int i = 0; i < params.length; i++)
	    S_M.addAll(pig_caller.G.I.pointedNodes(params[i]));
	// Update the escape information
	for(Iterator it = S_M.iterator(); it.hasNext(); )
	    pig_caller.G.e.addMethodHole((PANode)it.next(),q);
	// propagate the new escape information
	pig_caller.G.propagate(S_M);

	// clone the graph: we will have two distinct versions of it:
	// one on the 0-edge, the other on the 1-edge, corresponding
	// to the normal execution (return) respectively execution with
	// exceptions (throw).
	ParIntGraph pig_caller1 = (ParIntGraph) (pig_caller.clone());

	// The names of the variables closely match those used in the
	// formal description of the algorithm (section 9.2)

	// Set the edges for the result node in graph 0.
	Temp l_R = q.retval();
	if(l_R != null){
	    pig_caller.G.I.removeEdges(l_R);
	    PANode n_R = node_rep.getCodeNode(q, PANode.RETURN);
	    pig_caller.G.I.addEdge(l_R, n_R);
	}

	// Set the edges for the exception node in graph 1.
	Temp l_E = q.retex();
	if(l_E != null){
	    pig_caller1.G.I.removeEdges(l_E);
	    PANode n_E = node_rep.getCodeNode(q, PANode.EXCEPT);
	    pig_caller1.G.I.addEdge(l_E, n_E);
	}

	return new ParIntGraphPair(pig_caller, pig_caller1);
    }


    /** Update the graph of the caller at an analyzed call site. This consists
     * of mapping the important parts of the callee's graph into the graph of
     * the caller. See the paper of Martin and Whaley for a description of the
     * algorithm.<br>
     * <b>Parameters:</b>
     * <ul>
     * <li>The <code>CALL</code> quad that represents the call site.<br>
     * <li>The Parallel Interaction Graph of the caller
     *(<code>pig_caller</code>) that models the memory state just before the
     *call site.
     * <li>The Parallel Interaction Graph of the callee
     *(<code>pig_callee</code>). For efficiency reasons it's advisable to pass
     *the reduced, external view of that graph.
     * <li>The nodes representing the <b>formal</b> parameters of the callee.
     *The actual parameters do not need to be sent: they will be retrieved
     *from the <code>CALL</code> quad (the same for return and exception
     *temporaries.
     *</ul>
     */
    private static ParIntGraphPair mapUp(CALL q, 
					 ParIntGraph pig_caller,
					 ParIntGraph pig_callee,
					 PANode[] callee_params){

	if(DEBUG){
	    System.out.println("Pig_caller:" + pig_caller);
	    System.out.println("Pig_callee:" + pig_callee);
	}

	// get the initial mapping: formal param -> actual parameter,
	// and class node -> class node
	Relation mu = 
	    get_initial_mapping(q,pig_caller,pig_callee,callee_params);

	if(DEBUG) System.out.println("Initial mapping:" + mu);

	// update the node mapping by matching outside edges from the caller
	// with inside edges from the callee
	match_edges(mu,pig_caller,pig_callee);
	
	if(DEBUG) System.out.println("After matching edges:" + mu);

	// all the nodes from the caller (except for PARAM) are
	// initially inserted into the caller's graph
	compute_the_final_mapping(mu,pig_caller,pig_callee);

	if(DEBUG) System.out.println("Final mapping:" + mu);


	PAEdgeSet old_caller_I = (PAEdgeSet) pig_caller.G.I.clone();

	// Inserts the image of the callee's graph into the caller's graph.
	Set params = new HashSet();
	for(int i=0;i<callee_params.length;i++)
	    params.add(callee_params[i]);
	pig_caller.insertAllButArEo(pig_callee,mu,false,params);

	// bring the actions of the callee into the caller's graph
	bring_actions(pig_caller.ar, pig_callee.ar,
		      pig_caller.tau.activeThreadSet(), mu);

	// bring the edge ordering relation into the caller's graph
	bring_eo(pig_caller.eo, old_caller_I, pig_callee.eo,
		 pig_callee.G.O, mu);

	// recompute the escape info
	pig_caller.G.propagate();

	if(DEBUG){
	    System.out.println("Unsimplified graph:");
	    System.out.println(pig_caller);
	}

	// simplify the graph by removing the empty loads
	pig_caller.removeEmptyLoads();

	if(DEBUG){
	    System.out.println("Simplified graph:");
	    System.out.println(pig_caller);
	}

	// make a copy of the parallel interaction graph
	ParIntGraph pig_caller1 = (ParIntGraph) (pig_caller.clone());
	// set the edges for the exception on the out-edge 1
	set_edges_res_ex(q.retex() , mu, pig_caller1, pig_callee.G.excp);
	// set the edges for the result on the out-edge 0
	set_edges_res_ex(q.retval(), mu, pig_caller,  pig_callee.G.r);

	if(DEBUG){
	    System.out.println("Final graphs:{");
	    System.out.println(" The graph on edge 0:");
	    System.out.println(pig_caller);
	    System.out.println(" The graph on edge 1:");
	    System.out.println(pig_caller1);
	    System.out.println("}");
	}

	return new ParIntGraphPair(pig_caller, pig_caller1);
    }
    
    /** Sets the initial mapping: each formal parameter is mapped
     * to the nodes pointed to by the corresponding actual argument
     * (into the caller graph) and each static node is mapped to itself.
     * (only the static nodes that really point to something are important,
     * the leaf static nodes are not interesting in our analysis)
     */
    private static Relation get_initial_mapping(CALL q,
						ParIntGraph pig_caller,
						ParIntGraph pig_callee,
						PANode[] callee_params){
	Relation mu = new Relation();
	Temp[] args = q.params();
	int object_params_count = 0;

	// map the object formal parameter nodes to the actual arguments
	for(int i = 0; i < args.length; i++)
	    if(!q.paramType(i).isPrimitive()){
		mu.addAll(callee_params[object_params_count],
			  pig_caller.G.I.pointedNodes(args[i]));
		object_params_count++;
	    }

	if(object_params_count != callee_params.length){
	    System.err.println("Fatal error in get_initial_mapping");
	    System.out.println("\tDifferent numbers of object formal " +
			       "parameters (" + callee_params.length + 
			       ") and object arguments (" +
			       object_params_count + ")");
	    System.out.println(q);
	    System.exit(1);
	}

	// map the static nodes to themselves
	Enumeration enum = pig_callee.G.O.allSourceNodes();
	while(enum.hasMoreElements()){
	    PANode node = (PANode) enum.nextElement();
	    if(node.type == PANode.STATIC)
		mu.add(node,node);
	}
	// only the static nodes that appear as sources of the outside edges
	// must be initially mapped

	return mu;
    }


    /** Matches outside edges from the graph of (used by ) the callee 
	against inside edges from the graph of (created by) the caller.
	(repeated application of constraint 2). The goal of this method is
	to resove the load nodes from the callee, i.e. to detect the
	nodes from the caller that each load node might represent. */
    private static void match_edges(Relation mu,
				    ParIntGraph pig_caller,
				    ParIntGraph pig_callee){

	PAWorkList W = new PAWorkList();
	// here is the new stuff; only nodes with new stuff are
	// put in the worklist W.
	Relation new_info = (Relation) mu.clone();

	W.addAll(mu.keySet());
	while(!W.isEmpty()){
	    PANode node1 = (PANode) W.remove();

	    // nodes3 stands for all the new instances of n3
	    // from the inference rule
	    HashSet nodes3 = new HashSet(new_info.getValuesSet(node1));
	    new_info.removeAll(node1);

	    Enumeration flags = pig_callee.G.O.allFlagsForNode(node1);
	    while(flags.hasMoreElements()){
		String f = (String) flags.nextElement();

		// nodes2 stands for all the nodes that could play
		// the role of n2 from the inference rule
		Set nodes2 = pig_callee.G.O.pointedNodes(node1,f);
		if(nodes2.isEmpty()) continue;

		// nodes4 stands for all the nodes that could play
		// the role of n4 from the inference rule
		Set nodes4 = pig_caller.G.I.pointedNodes(nodes3,f);
		if(nodes4.isEmpty()) continue;

		// set up the relation from any node from nodes2
		// to any node from nodes4
		Iterator it2 = nodes2.iterator();
		while(it2.hasNext()){
		    PANode node2 = (PANode)it2.next();
		    boolean changed = false;
		    Iterator it4 = nodes4.iterator();
		    while(it4.hasNext()){
			PANode node4 = (PANode)it4.next();
			if(mu.add(node2,node4)){
			    changed = true;
			    new_info.add(node2,node4);
			}
		    }
		    // nodes with new info are put in the worklist
		    if(changed) W.add(node2);
		}
	    }
	}
    }

    // Initially, all the nodes from the callee are put into the caller's
    // graph except for the PARAM nodes. Later, after recomputing the
    // escape info, the empy load nodes will be removed (together with
    // the related information)
    private static void compute_the_final_mapping(final Relation mu,
						final ParIntGraph pig_caller,
						final ParIntGraph pig_callee){
	pig_callee.forAllNodes(new PANodeVisitor(){
		public void visit(PANode node){
		    if(node.type() != PANode.PARAM)
			mu.add(node,node);
		}
	    });
    }

    /** Sets the edges for the result or the exception returned by the callee.
     * Remember the syntax of a method invocation: 
     * <code>&lt;retval,retexc&gt; = CALL (...)</code>. */
    private static void set_edges_res_ex(Temp l, Relation mu,
					  ParIntGraph pig_caller, Set nodes){
	if(l == null) return;
	/// System.out.println("Setting the edges for " + l);
	pig_caller.G.I.removeEdges(l);
	Set mu_nodes = new HashSet();
	Iterator it = nodes.iterator();
	while(it.hasNext())
	    mu_nodes.addAll(mu.getValuesSet((PANode)it.next()));
	pig_caller.G.I.addEdges(l,mu_nodes);
    }


    private static void bring_actions(final ActionRepository ar_caller,
				      final ActionRepository ar_callee,
				      final Set active_threads_in_caller,
				      final Relation mu){
	// Add this "common-sense" rule to the mapping: the inter-procedural
	// analysis stays in the same thread.
	mu.add(ActionRepository.THIS_THREAD,ActionRepository.THIS_THREAD);

	// Step 1. Put all the actions from the callee as being executed
	// in // with all the threads that are active in the caller.
	ActionVisitor act_visitor = new ActionVisitor(){
		public void visit_ld(PALoad load){
		    if(!mu.contains(load.n2,load.n2)) return;
		    ar_caller.add_ld(mu.getValuesSet(load.n1),
				     load.f,
				     load.n2,
				     mu.getValuesSet(load.nt),
				     active_threads_in_caller);
		}
		public void visit_sync(PASync sync){
		    ar_caller.add_sync(sync.project(mu),
				       active_threads_in_caller);
		}
	    };

	ar_callee.forAllActions(act_visitor);

	// Step 2. Translate the "parallel action" items of information from
	// the callee, by applying the mu function on their components
	// (except for the n2 componenent of a load action which is left
	// unchanged if it's still present in the new graph.
	ParActionVisitor par_act_visitor = new ParActionVisitor(){
		public void visit_par_ld(PALoad load, PANode nt2){
		    if(!mu.contains(load.n2,load.n2)) return;
		    ar_caller.add_ld(mu.getValuesSet(load.n1),
				     load.f,
				     load.n2,
				     mu.getValuesSet(load.nt),
				     mu.getValuesSet(nt2));
		}
		public void visit_par_sync(PASync sync, PANode nt2){
		    ar_caller.add_sync(sync.project(mu),
				       mu.getValuesSet(nt2));
		}
	    };

	ar_callee.forAllParActions(par_act_visitor);
    }

    // Inserts the edge ordering relation of the callee into the graph of
    // the caller. In addition, records the fact that all the outside edges
    // from the callee appear after all the inside edges of the caller.
    private static void bring_eo(final EdgeOrdering eo_caller,
				 final PAEdgeSet callerI,
				 final EdgeOrdering eo_callee,
				 final PAEdgeSet calleeO,
				 final Relation mu){

	eo_callee.forAllEntries(new RelationEntryVisitor(){
		public void visit(Object key, Object value){
		    PAEdge eo = (PAEdge) key;
		    PAEdge ei = (PAEdge) value;
		    // make sure eo will appear into the new graph
		    if(!mu.contains(eo.n2,eo.n2)) return;

		    Set ei_n1_set = mu.getValuesSet(ei.n1);
		    if(ei_n1_set.isEmpty()) return;
		    Set ei_n2_set = mu.getValuesSet(ei.n2);
		    if(ei_n2_set.isEmpty()) return;
		    Set eo_n1_set = mu.getValuesSet(eo.n1);
		    if(eo_n1_set.isEmpty()) return;

		    Iterator it_ei_n1 = ei_n1_set.iterator();
		    while(it_ei_n1.hasNext()){
			PANode ei_n1 = (PANode) it_ei_n1.next();
			Iterator it_ei_n2 = ei_n2_set.iterator();
			while(it_ei_n2.hasNext()){
			    PANode ei_n2 = (PANode) it_ei_n2.next();
			    PAEdge new_ei = new PAEdge(ei_n1,ei.f,ei_n2);
			    Iterator it_eo_n1 = eo_n1_set.iterator();
			    while(it_eo_n1.hasNext()){
				PANode eo_n1 = (PANode) it_eo_n1.next();
				eo_caller.add(new PAEdge(eo_n1,ei.f,eo.n2),
					      new_ei);
			    }
			}
		    }
		}
	    });


	// all the outside edges from the callee occur *after* all
	// the inside edges that exist in the caller right before the CALL
	calleeO.forAllEdges(new PAEdgeVisitor(){
		public void visit(Temp var, PANode node){}
		public void visit(PANode n1, String f, PANode n2){
		    if(!mu.contains(n2,n2)) return;
		    eo_caller.add(mu.getValuesSet(n1),f,n2,callerI);
		}
	    });
    }

}// end of the class










