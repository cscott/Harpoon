// InterProcPA.java, created Tue Jan 18 11:04:20 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.Modifier;

import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Temp.Temp;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.NoSuchClassException;

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.GenType;

import harpoon.Util.TypeInference.ExactTemp;
import harpoon.Util.TypeInference.TypeInference;
import harpoon.IR.Quads.QuadFactory;

import harpoon.Util.Util;

/**
 * <code>InterProcPA</code> is a &quot;functional&quot; class (i.e. it 
 * contains just some methods, no persistent data) that wraps
 * the inter-procedural part of the pointer analysis. Normally, this should
 * be a part of the <code>PointerAnalysis</code>, but that class is already
 * too big and some code segmentation is always good!<br>
 * In the implementation of this class, most of the methods are static and
 * have <code>PointerAnalysis pa</code> as their first argument. This stands
 * for the <code>this</code> <i>hidden</i> argument that would exist if all
 * those methods were in the <code>PointerAnalysis</code> class.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: InterProcPA.java,v 1.14 2005-08-17 23:34:00 salcianu Exp $
 */
public abstract class InterProcPA implements java.io.Serializable {

    /** Call sites with more than <code>MAX_CALLEES</code> callees are simply
     *  considered to be holes. */ 
    public static final int MAX_CALLEES = 20;

    /** Activates a lot of debug messages. */
    public static boolean DEBUG = false;

    /** Displays some warnings, <i>eg</i> for the call sites with 0 callees.
     *  This is not necessarily an error! For example, if an application
     *  never instantiates a <code>SecurityManager</code>,
     *	each call to a method of that class has ZERO callees! */
    public static final boolean WARNINGS = true;

    /** Trust that a call to java.lang.Object.equals doesn't do
        anything interesting. */
    public static boolean TRUST_EQUALS = false;

    /** Trust that a call to java.lang.Object.hashCode doesn't do
        anything interesting. */
    public static boolean TRUST_HASHCODE = false;

    /** Trust that a call to java.lang.Object.toString doesn't do
        anything interesting. */
    public static boolean TRUST_TOSTRING = false;

    private static final boolean CONSIDER_WES_GOOD = false;

    /** Analyzes the call site <code>q</code> inside 
     *	<code>current_method</code>. If analyzing the call is not possible
     *	(e.g. one of the callees is native, hence unanalyzable), the call
     *	site is skipped and all the parameters are marked as escaping
     *	through that call site.
     *
     *	@param pa  The <code>PointerAnalysis</code> object that
     *	calls this method. This parameter stands for the <code>this</code>
     *	pointer that would exist if this method were in the
     *	<code>PointerAnalysis</code> class.
     *	@param current_method  The method that contains in its
     *	code the call site <code>q</code>.
     *	@param q  The analyzed call site.
     *	@param pig_before  The parallel interaction graph at
     *	the program point just before the call site; this graph will be 
     *	mutated, it is the responsability of the caller to clone it if it
     *	is necessary somewhere else.
     *
     *	@return  Two graphs are returned: one for the normal return from
     *  the procedure, the other one for a return due to an exception.
     */
    public static ParIntGraphPair analyze_call(PointerAnalysis pa,
					       MetaMethod current_mmethod,
					       CALL q,
					       ParIntGraph pig_before) {
	long b_time = 
	    PointerAnalysis.FINE_TIMING ? System.currentTimeMillis() : 0;

	ParIntGraphPair result = 
	    analyze_call2(pa, current_mmethod, q, pig_before);
	if(PointerAnalysis.FINE_TIMING)
	    total_interproc_time += System.currentTimeMillis() - b_time;
	return result;
    }


    private static ParIntGraphPair analyze_call2(PointerAnalysis pa,
						 MetaMethod current_mmethod,
						 CALL q,
						 ParIntGraph pig_before) {
	
	HMethod src = q.getFactory().getMethod();

	if(DEBUG || DEBUG_MU)
	    System.out.println("\nInter-procedural analysis " +
			       Util.code2str(q));

	ParIntGraphPair pp_after = null;

	// treat some very special native methods
	if((pp_after = treatVerySpecialNatives(pa, q, pig_before)) != null)
	    return pp_after;

	MetaMethod[] mms = pa.getMetaCallGraph().getCallees(current_mmethod,q);
	int nb_callees   = mms.length;

	// I count on the fact that if a call site has 0 callees, it 
	// means that it doesn't occur in practice (because some classes
	// are not instantiated), not that the call graph is buggy!
	// So, the CALL is simply ignored.
	if(nb_callees == 0) {
	    if(WARNINGS) {
		System.out.println("Warning: CALL site with no callee! ");
		System.out.println("Warning:  " + Util.code2str(q));
	    }
 	    return new ParIntGraphPair(pig_before, pig_before);
	}

	// Due to the imprecisions in the call graph, most of them due to
	// dynamic dispatches, several call sites have a huge number of callees
	// These CALLs are not analyzed (i.e. they are treated as method holes)
	if(nb_callees > MAX_CALLEES) {
	    //if(DEBUG)
		System.out.println("TOO MANY CALLEES (" + nb_callees + ") "+q);
	    if((TRUST_EQUALS    &&  q.method().equals(jlO_equals)) ||
	       (TRUST_HASHCODE  &&  q.method().equals(jlO_hashCode)) ||
	       (TRUST_TOSTRING  &&  q.method().equals(jlO_toString))) {
		//if(DEBUG)
		    System.out.println("BUT equals/hashCode/toString ");
		return treatNatives(pa, new MetaMethod(q.method(), true),
				    q, pig_before);
	    }
	    return skip_call(pa, q, pig_before, q.method());
	}

 	// For each analyzable callee mm, we store it in mms and its associated
	// parallel interaction graph in pigs. By "analyzable", we mean
	// (meta)-methods that are analyzable by the the Pointer Analysis PLUS
	// the so-called unharmful native methods - native method that we can't
	// analyze as we don't have their code but we know what their pig will
	// look like (for most of them it's empty - they don't create new
	// pointer links and don't do any relevant action (e.g., syncs)).
	//
	// For the unharmful native methods the associated pig will be null
	// (they will be specially treated inside mapUp).
	//
	// pig could be also null if the method has not be analyzed yet; this
	// happens only in strongly connected components of mutually recursive
	// methods. In this case, we simply don't consider that callee; the
	// called method will be analyzed later, this will force its callers
	// to be reanalyzed, so in the end it will be considered (due to the
	// fixed-point algorithm).
	int nb_callees_with_pig = 0;
	ParIntGraph pigs[] = new ParIntGraph[nb_callees];
	for(int i = 0; i < nb_callees; i++){
	    HMethod hm = mms[i].getHMethod();

	    if(Modifier.isNative(hm.getModifiers())) {
		if(DEBUG)
		    System.out.println("\nNATIVE: " + hm);
		if(isTotallyHarmful(hm)) {
		    //if(DEBUG)
		    System.out.print("\nNEED TO SKIP: " + Util.code2str(q));
		    return skip_call(pa, q, pig_before, hm);
		}
		else {
		    ParIntGraph pig = model_native(hm);
		    pigs[nb_callees_with_pig] = pig;
		    mms[nb_callees_with_pig]  = mms[i];
		    nb_callees_with_pig++;
		    continue;
		}
	    }

	    // RTJ stuff START - Wes's stuff is supposed to be safe
	    if(CONSIDER_WES_GOOD) {
		if(wes_methods.contains(hm)) {
		    pigs[nb_callees_with_pig] = null;
		    mms[nb_callees_with_pig]  = mms[i];
		    nb_callees_with_pig++;
		    continue;
		}
	    }
	    // RTJ stuff END

	    if(!(PointerAnalysis.analyzable(hm))) {
		//if(DEBUG)
		    System.out.println("NEED TO SKIP2: " + Util.code2str(q));
		return skip_call(pa, q, pig_before, hm);
	    }
	    
	    ParIntGraph pig = pa.getExtParIntGraph(mms[i], false);
	    if(pig != null) {
		pigs[nb_callees_with_pig] = pig;
		mms[nb_callees_with_pig]  = mms[i];
		nb_callees_with_pig++;
	    }
	}

	// If none of the callers has been analyzed yet, do not do anything
	// (this can happen only in the strongly connected components of 
	// mutually recursive methods).
	if(nb_callees_with_pig == 0) {
	    ParIntGraph pig0 = (ParIntGraph) pig_before.clone();
	    Temp l_R = q.retval();
	    if(l_R != null) pig0.G.I.removeEdges(l_R);
	    ParIntGraph pig1 = (ParIntGraph) pig_before.clone();
	    Temp l_E = q.retex();
	    if(l_E != null) pig1.G.I.removeEdges(l_E);
	    return new ParIntGraphPair(pig0, pig1);
	}

	// Specialize the graphs of the callees for the context sensitive PA
	if(PointerAnalysis.CALL_CONTEXT_SENSITIVE)
	    for(int i = 0; i < nb_callees_with_pig; i++)
		if(pigs[i] != null){
		    if(DEBUG)
			System.out.println("BEFORE SPEC: " + pigs[i]);
		    pigs[i] = pa.getSpecializedExtParIntGraph(mms[i], q);
		    if(DEBUG)
			System.out.println("AFTER  SPEC: " + pigs[i]);
		}

	// The graph after the CALL is a join of all the graphs obtained
	// by combining, for each callee mms[i], the graph before the CALL
	// with the graph at the end of mms[i].
	// The implementation is complicated by the need of doing only the
	// minimum number of clone() (cloning a ParIntGraph is very expensive).

	// 1. Special case: only one callee; no ParIntGraph is cloned.
	if(nb_callees_with_pig == 1)
	    return mapUp(pa, mms[0], q, pig_before, pigs[0]);

	// 2. More than one callee: 
	// 2.1. Compute the first term of the join operation.
	pp_after = 
	    mapUp(pa, mms[0], q, (ParIntGraph) pig_before.clone(), pigs[0]);
	
	// 2.2. Join to it all the others, except for the last one.
	for(int i = 1; i < nb_callees_with_pig - 1; i++)
	    pp_after.join(mapUp(pa, mms[i], q, (ParIntGraph)pig_before.clone(),
				pigs[i]));

	// 2.3. Finally, join the graph for the last callee.
	MetaMethod last_mm = mms[nb_callees_with_pig - 1];
	pp_after.join
	    (mapUp(pa, last_mm, q, pig_before, pigs[nb_callees_with_pig-1]));

	return pp_after; // the long awaited moment!
    }


    /** Updates the ParIntGraph when the CALL is skipped. Two graphs are
	returned: one for the normal return from the procedure, the other
	one for a return due to an exception. */
    private static ParIntGraphPair skip_call(PointerAnalysis pa,
					     CALL q, ParIntGraph pig_caller,
					     HMethod hm) {

	if(DEBUG)
	    System.out.println("SKIP: " + q);

	NodeRepository node_rep = pa.getNodeRepository();

	// Construct the set S_M of the objects escaped through this unanalyzed
	// method invocation site.
	Set S_M = new HashSet();
	Temp[] params = q.params();
	for(int i = 0; i < params.length; i++)
	    S_M.addAll(pig_caller.G.I.pointedNodes(params[i]));

	// Update the escape information
	//  1. all the parameters are directly escaping into the method hole
	for(Iterator it = S_M.iterator(); it.hasNext(); )
	    pig_caller.G.e.addMethodHole((PANode)it.next(), hm);
	//  2. propagate the new escape information
	pig_caller.G.propagate(S_M);

	// clone the graph: we will have two distinct versions of it:
	// one on the 0-edge, the other on the 1-edge, corresponding
	// to the normal execution (return) respectively execution with
	// exceptions (throw).
	ParIntGraph pig_caller1 = (ParIntGraph) (pig_caller.clone());

	// The names of the variables closely match those used in the
	// formal description of the algorithm (section 9.2)

	// Set the edges for the result node in graph 0.
	// avoid generating useless nodes
	Temp l_R = q.retval();
	if(l_R != null){
	    pig_caller.G.I.removeEdges(l_R);
	    if(!hm.getReturnType().isPrimitive()) {
		PANode n_R = node_rep.getCodeNode(q, PANode.RETURN);
		pig_caller.G.I.addEdge(l_R, n_R);
		pig_caller.G.e.addMethodHole(n_R, hm);
	    }
	}

	// Set the edges for the exception node in graph 1.
	Temp l_E = q.retex();
	if(l_E != null){
	    pig_caller1.G.I.removeEdges(l_E);
	    PANode n_E = node_rep.getCodeNode(q, PANode.EXCEPT);
	    pig_caller1.G.I.addEdge(l_E, n_E);
	    pig_caller1.G.e.addMethodHole(n_E, hm);
	}

	return new ParIntGraphPair(pig_caller, pig_caller1);
    }


    // Treat the calls to native methods.
    private static ParIntGraphPair treatNatives(PointerAnalysis pa,
						MetaMethod mm,
						CALL q,
						ParIntGraph pig_caller) {
	NodeRepository node_rep = pa.getNodeRepository();
	HMethod hm = mm.getHMethod();
	if(DEBUG) System.out.println("treatNatives: " + hm);

	ParIntGraph pig_before = (ParIntGraph) pig_caller.clone();

	if(!isUnharmful(hm))
	    markEscapedParameters(q, hm, pig_before);

	ParIntGraph pig0 = (ParIntGraph) pig_before.clone();
	ParIntGraph pig1 = pig_before;

	Temp l_R = q.retval();
	if((l_R != null) && !hm.getReturnType().isPrimitive()) {
	    pig0.G.I.removeEdges(l_R);
	    PANode n_R = node_rep.getCodeNode(q, PANode.RETURN);
	    pig0.G.I.addEdge(l_R, n_R);
	    pig0.G.e.addMethodHole(n_R, hm);
	}

	Temp l_E = q.retex();
	if(l_E != null){
	    pig1.G.I.removeEdges(l_E);
	    PANode n_E = node_rep.getCodeNode(q, PANode.EXCEPT);
	    pig1.G.I.addEdge(l_E, n_E);
	    pig1.G.e.addMethodHole(n_E, hm);
	}

	return new ParIntGraphPair(pig0, pig1);	
    }

    private static void markEscapedParameters(CALL q, HMethod hm,
					      ParIntGraph pig) {
	if(markEscapedParametersSpecial(q, hm, pig))
	    return;
	List escaped = new LinkedList();
	Temp[] params = q.params();
	for(int i = 0; i < params.length; i++) {
	    HClass hc = q.paramType(i);
	    if(!hc.isPrimitive())
		escaped.addAll(pig.G.I.pointedNodes(params[i]));
	}

	// Update the escape information
	//  1. all the object parameters directly escape into the method hole
	for(Iterator it = escaped.iterator(); it.hasNext(); )
	    pig.G.e.addMethodHole((PANode)it.next(), hm);
	//  2. propagate the new escape information
	pig.G.propagate(escaped);
    }

    // For some native methods, maybe only some of the parameters escape
    // (ie, we know that the others are not "harmed").
    // For the moment, we don't treat any method in this special way.
    private static boolean markEscapedParametersSpecial(CALL q, HMethod hm,
							ParIntGraph pig) {
	return false;
    }

    /** Updates the graph of the caller at an analyzed call site. This consists
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
    private static ParIntGraphPair mapUp(PointerAnalysis pa,
					 MetaMethod mm,
					 CALL q, 
					 ParIntGraph pig_caller,
					 ParIntGraph pig_callee) {

	// if native method, apply special treatment
	if(pig_callee == null)
	    return treatNatives(pa, mm, q, pig_caller);

	PANode[] callee_params = pa.getParamNodes(mm);
	if(callee_params == null)
	    callee_params = get_params4native(mm, pa);

	long b_time = 
	    PointerAnalysis.FINE_TIMING ? System.currentTimeMillis() : 0;

	Relation mu = 
	    compute_mapping(q, pig_caller, pig_callee, callee_params, pa);

	if(PointerAnalysis.FINE_TIMING) {
	    long delta = System.currentTimeMillis() - b_time;
	    total_mapping_time += delta;
	    b_time = System.currentTimeMillis();
	}

	PAEdgeSet old_caller_I = 
	    PointerAnalysis.RECORD_ACTIONS ?
	    (PAEdgeSet) pig_caller.G.I.clone() : null;

	// Inserts the image of the callee's graph into the caller's graph.
	Set params = new HashSet();
	for(int i = 0; i < callee_params.length; i++)
	    if(!pig_callee.tau.isStarted(callee_params[i]))
		params.add(callee_params[i]);

	Set ppgRoots = new HashSet();
	pig_caller.insertAllButArEo
	    (pig_callee, mu, false, params, ppgRoots, false);

	// bring the actions of the callee into the caller's graph
	if(PointerAnalysis.RECORD_ACTIONS)
	    bring_actions(pig_caller.ar, pig_callee.ar,
			  pig_caller.tau.activeThreadSet(), mu);

	// bring the edge ordering relation into the caller's graph
	if(!PointerAnalysis.IGNORE_EO)
	    bring_eo(pig_caller.eo, old_caller_I, pig_callee.eo,
		     pig_callee.G.O, mu);

	if(PointerAnalysis.FINE_TIMING) { // TIMING
	    long delta = System.currentTimeMillis() - b_time;
	    total_merging_time += delta;
	    b_time = System.currentTimeMillis();
	}

	// recompute the escape info
	pig_caller.G.propagate(ppgRoots);
	
	if(PointerAnalysis.FINE_TIMING) {
	    long delta = System.currentTimeMillis() - b_time;
	    total_propagate_time += delta;
	    b_time = System.currentTimeMillis();
	}

	// simplify the graph by removing the empty loads
	pig_caller.removeEmptyLoads();

	if(PointerAnalysis.FINE_TIMING) {
	    long delta = System.currentTimeMillis() - b_time;
	    total_cleaning_time += delta;
	    b_time = System.currentTimeMillis();
	}

	// make a copy of the parallel interaction graph
	ParIntGraph pig_caller1 = (ParIntGraph) (pig_caller.clone());

	// set the edges for the exception on the out-edge 1
	set_edges_res_ex(q.retex() , mu, pig_caller1, pig_callee.G.excp);
	// set the edges for the result on the out-edge 0
	set_edges_res_ex(q.retval(), mu, pig_caller,  pig_callee.G.r);

	/*
	if(DEBUG) {
	    System.out.println("Final graphs:{");
	    System.out.println(" The graph on edge 0:");
	    System.out.println(pig_caller);
	    System.out.println(" The graph on edge 1:");
	    System.out.println(pig_caller1);
	    System.out.println("}");
	}
	*/

	return new ParIntGraphPair(pig_caller, pig_caller1);
    }

    static long total_mapping_time = 0;
    static long total_merging_time = 0;
    static long total_propagate_time = 0;
    static long total_cleaning_time = 0;
    static long total_interproc_time = 0;


    private static PANode[] get_params4native
	(MetaMethod mm, PointerAnalysis pa) {

	HMethod hm = mm.getHMethod();
	assert Modifier.isNative(hm.getModifiers());
	
	HClass[] types = hm.getParameterTypes();
	int count =
	    java.lang.reflect.Modifier.isStatic(hm.getModifiers()) ? 0 : 1;
	for(int i = 0; i < types.length; i++)
	    if(!types[i].isPrimitive()) count++;

	pa.getNodeRepository().addParamNodes(mm, count);
	return pa.getParamNodes(mm);
    }


    // Conversion array -> set
    private static final Set array2set(Object[] array) {
	Set result = new HashSet();
	for(int i = 0; i < array.length; i++)
	    result.add(array[i]);
	return result;
    }

    // activates the use of the new mapping constraints
    private static boolean NEW_MAPPING_CONSTRAINTS = false;
    public static boolean VERY_NEW_MAPPINGS = true;

    // activates the use of constraints from Alex Salcianu's thesis.
    private static boolean THESIS_MAPPING_CONSTRAINTS = true;
    static {
	if(THESIS_MAPPING_CONSTRAINTS)
	    System.out.println("InterProcPA: Alex Salcianu's SM thesis");
	System.out.println("MAX_CALLEES = " + MAX_CALLEES);
    }

    // generates lots of debug messages about the construction of mu
    private static boolean DEBUG_MU = false;

    // Computes the mapping mu of the callee nodes
    private static Relation compute_mapping
	(CALL q,
	 ParIntGraph pig_caller, ParIntGraph pig_callee,
	 PANode[] callee_params,
	 PointerAnalysis pa) {

	// use the new mapping constraints, if asked to do so
	if(THESIS_MAPPING_CONSTRAINTS)
	    return ComputeInterProcMuClosure.computeInterProcMu
		(q, pig_caller, pig_callee, callee_params, pa);

	// get the initial mapping: formal param -> actual parameter,
	// and class node -> class node
	Relation mu = 
	    get_initial_mapping(q, pig_caller, pig_callee, callee_params);

	if(DEBUG_MU) System.out.println("Initial mapping:" + mu);

	// update the node mapping by matching outside edges from the caller
	// with inside edges from the callee
	Set redundant_ln = new HashSet();
	match_edges(mu, pig_caller, pig_callee, redundant_ln);

	if(DEBUG_MU) System.out.println("After matching edges:" + mu);

	// all the nodes from the caller (except for PARAM) are
	// initially inserted into the caller's graph
	compute_the_final_mapping(mu, pig_callee, redundant_ln);

	if(DEBUG_MU) System.out.println("Final mapping:" + mu);
	
	return mu;
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
						PANode[] callee_params) {
	Relation mu = new RelationImpl();

	// first, map the parameter nodes to the actual nodes being passed
	// as arguments in the call q
	map_parameters(mu, q, pig_caller, callee_params);

	// next, map the static nodes to themselves;
	// only the static nodes that appear as sources of the outside edges
	// must be initially mapped
	process_STATICs(pig_callee.G.O.allSourceNodes(), mu);

	return mu;
    }


    // Update the mapping mu to contain the mappings for the parameter nodes
    private static void map_parameters
	(Relation mu, CALL call,
	 ParIntGraph pig_caller, PANode[] callee_params) {
	Temp[] args = call.params();
	int object_params_count = 0;
	// map the object formal parameter nodes to the actual arguments
	for(int i = 0; i < args.length; i++)
	    if(!call.paramType(i).isPrimitive()) {
		mu.addAll(callee_params[object_params_count],
			  pig_caller.G.I.pointedNodes(args[i]));
		object_params_count++;
	    }
	
	assert object_params_count == callee_params.length :
	    "\tDifferent numbers of object formals (" + 
	    callee_params.length + ") and object arguments (" +
	    object_params_count + ") for \n\t" + Util.code2str(call);
    }


    // aux method for get_initial_mapping
    private static void process_STATICs(final Set set, final Relation mu) {
	for(Object nodeO : set) {
	    PANode node = (PANode) nodeO;
	    if(node.type == PANode.STATIC)
		mu.add(node, node);
	}
    }

    /** Matches outside edges from the graph of (used by ) the callee 
	against inside edges from the graph of (created by) the caller.
	(repeated application of constraint 2). The goal of this method is
	to resolve the load nodes from the callee, i.e. to detect the
	nodes from the caller that each load node might represent. */
    private static void match_edges(Relation mu,
				    ParIntGraph pig_caller,
				    ParIntGraph pig_callee,
				    final Set redundant_ln) {

	PAWorkList W = new PAWorkList();
	// here is the new stuff; only nodes with new stuff are
	// put in the worklist W.
	Relation new_info = (Relation) mu.clone();

	W.addAll(mu.keys());
	while(!W.isEmpty()) {
	    PANode node1 = (PANode) W.remove();

	    // nodes3 stands for all the new instances of n3
	    // from the inference rule
	    HashSet nodes3 = new HashSet(new_info.getValues(node1));
	    new_info.removeKey(node1);

	    for (Object fO : pig_callee.G.O.allFlagsForNode(node1)) {
		String f = (String) fO;
		// 1. matching outside edges/callee against inside edges/caller
		// nodes2 stands for all the nodes that could play
		// the role of n2 from the inference rule
		Set nodes2 = pig_callee.G.O.pointedNodes(node1, f);
		if(nodes2.isEmpty()) continue;

		// nodes4 stands for all the nodes that could play
		// the role of n4 from the inference rule
		Set nodes4 = pig_caller.G.I.pointedNodes(nodes3, f);
		if(!nodes4.isEmpty()) {
		    // set up the relation from any node from nodes2
		    // to any node from nodes4
		    for(Object node2O : nodes2) {
			PANode node2 = (PANode) node2O;
			boolean changed = false;
			for(Object node4O : nodes4) {
			    PANode node4 = (PANode) node4O;
			    if(mu.add(node2,node4)){
				changed = true;
				new_info.add(node2, node4);
			    }
		    }
			// nodes with new info are put in the worklist
			if(changed) W.add(node2);
		    }
		}
		    
		// 2.matching outside edges/callee against outside edges/caller
		nodes4 = pig_caller.G.O.pointedNodes(nodes3, f);
		if(!nodes4.isEmpty()) {
		    // set up the relation from any node from nodes2
		    // to any node from nodes4
		    for(Object node2O : nodes2) {
			PANode node2 = (PANode) node2O;
			int type2 = node2.type();
			boolean changed = false;
			for(Object node4O : nodes4) {
			    PANode node4 = (PANode) node4O;
			    if(mu.add(node2, node4)) {
				changed = true;
				new_info.add(node2, node4);
				if((type2 == PANode.LOAD) &&
				   (node4.type() == PANode.LOAD))
				    redundant_ln.add(node2);
			    }
			}
			// nodes with new info are put in the worklist
			if(changed) W.add(node2);
		    }
		}
	    }
	}
    }

    // Initially, all the nodes from the callee are put into the caller's
    // graph except for the PARAM nodes. Later, after recomputing the
    // escape info, the empty load nodes will be removed (together with
    // the related information)
    private static void compute_the_final_mapping
	(final Relation mu,
	 final ParIntGraph pig_callee,
	 final Set redundant_ln) {
	pig_callee.forAllNodes(new PANodeVisitor(){
		public void visit(PANode node){
		    int type = node.type();
		    if(type == PANode.LOAD) {
			if(!redundant_ln.contains(node))
			    mu.add(node, node);
			return;
		    }
		    if(type != PANode.PARAM)
			mu.add(node, node);
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
	    mu_nodes.addAll(mu.getValues((PANode) it.next()));
	pig_caller.G.I.addEdges(l,mu_nodes);
    }


    private static void bring_actions(final ActionRepository ar_caller,
				      final ActionRepository ar_callee,
				      final Set active_threads_in_caller,
				      final Relation mu) {
	// Add this "common-sense" rule to the mapping: the inter-procedural
	// analysis stays in the same thread.
	mu.add(ActionRepository.THIS_THREAD, ActionRepository.THIS_THREAD);

	// Step 1. Put all the actions from the callee as being executed
	// in // with all the threads that are active in the caller.
	ActionVisitor act_visitor = new ActionVisitor(){
		public void visit_ld(PALoad load){
		    if(!mu.contains(load.n2,load.n2)) return;
		    ar_caller.add_ld(mu.getValues(load.n1),
				     load.f,
				     load.n2,
				     mu.getValues(load.nt),
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
		    ar_caller.add_ld(mu.getValues(load.n1),
				     load.f,
				     load.n2,
				     mu.getValues(load.nt),
				     mu.getValues(nt2));
		}
		public void visit_par_sync(PASync sync, PANode nt2){
		    ar_caller.add_sync(sync.project(mu),
				       mu.getValues(nt2));
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

	eo_callee.forAllEntries(new RelationEntryVisitor() {
		public void visit(Object key, Object value){
		    PAEdge eo = (PAEdge) key;
		    PAEdge ei = (PAEdge) value;
		    // make sure eo will appear into the new graph
		    if(!mu.contains(eo.n2,eo.n2)) return;

		    Set ei_n1_set = mu.getValues(ei.n1);
		    if(ei_n1_set.isEmpty()) return;
		    Set ei_n2_set = mu.getValues(ei.n2);
		    if(ei_n2_set.isEmpty()) return;
		    Set eo_n1_set = mu.getValues(eo.n1);
		    if(eo_n1_set.isEmpty()) return;

		    for (Object ei_n1O : ei_n1_set){
			PANode ei_n1 = (PANode) ei_n1O;
			for (Object ei_n2O : ei_n2_set){
			    PANode ei_n2 = (PANode) ei_n2O;
			    PAEdge new_ei = new PAEdge(ei_n1,ei.f,ei_n2);
			    for (Object eo_n1O : eo_n1_set){
				PANode eo_n1 = (PANode) eo_n1O;
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
		    eo_caller.add(mu.getValues(n1),f,n2,callerI);
		}
	    });
    }


    // Treats some native methods in a special way. Returns a pair of
    // ParIntGraphs or null if it cannot handle this call.
    private static ParIntGraphPair treatVerySpecialNatives
	(PointerAnalysis pa, CALL q, ParIntGraph pig_before){

	HMethod hm = q.method();
	if(hm == null) return null;
	ParIntGraphPair pair = null;

	if((pair = treat_arraycopy(pa, q, pig_before)) != null)
	    return pair;
	if((pair = treat_clone(pa, q, pig_before)) != null)
	    return pair;

	return null;
    }


    // Aux method for treat_clone. It returns the set of possible types
    // (Set<HClass>) of the argument of clone(). Ugly code: I think we should
    // have a Quad form with types.
    private static Set aux_clone_get_types(CALL q){
	// The quad factory that generated this CALL quad
	QuadFactory qf = q.getFactory();
	// the method into which it appears.
	HMethod     hm = qf.getMethod();
	// and the HCode containing the code of that method.
	HCode    hcode = (HCode) qf.getParent();
	// et = Temp q.params(0) used in Quad q
	ExactTemp   et = new ExactTemp(q, q.params(0));

	TypeInference ti = 
	    new TypeInference(hm, hcode, Collections.singleton(et));

	return ti.getType(et);
    }


    // Aux method for "treat_clone". Returns the set of all the fields
    // appearing in the types from the set "types".
    private static Set aux_clone_get_obj_fields(Set types){
	Set retval = new HashSet();
	for(Object hclassO : types){
	    HClass hclass = (HClass) hclassO;
	    if(hclass.isArray()) {
		System.out.println("CLONE: might be called for an array");
		retval.add(PointerAnalysis.ARRAY_CONTENT);
	    }
	    else {
		HField[] hfields = hclass.getFields();
		for(int i = 0; i < hfields.length; i++)
		    if(!hfields[i].getType().isPrimitive())
			retval.add(PointerAnalysis.getFieldName(hfields[i]));
	    }
	}
	return retval;
    }
 

    // Treat the sequence "ld t = n_src.f; store n_R.f = t;". For a clone()
    // call, this is done for all fields f of the cloned object.
    private static void aux_clone_treat_pseudo_ldst(CALL q, String f,
       PANode n_R, PANode n_src, ParIntGraph pig, NodeRepository node_rep){

	// f_set contains all the nodes pointed to by <n_src, f>
	Set f_set = new HashSet(pig.G.I.pointedNodes(n_src, f));

	if(pig.G.escaped(n_src)){
	    PANode n_L = node_rep.getLoadNodeSpecial(q, f);
	    pig.G.O.addEdge(n_src, f, n_L);
	    if(PointerAnalysis.RECORD_ACTIONS)
		pig.ar.add_ld(n_src, f, n_L, ActionRepository.THIS_THREAD,
			      pig.tau.activeThreadSet());
	    if(!PointerAnalysis.IGNORE_EO) {
		// TODO: edge ordering relation (if we want to maintain it)
		assert false : "edge ordering not implemented here!";
	    }
	    f_set.add(n_L);
	}

	for(Iterator it = f_set.iterator(); it.hasNext();)
	    pig.G.I.addEdge(n_R, f, (PANode) it.next());
    }


    // Specially treats 
    //    "protected native java.lang.Object java.lang.Object.clone()"
    private static ParIntGraphPair treat_clone(PointerAnalysis pa, CALL q,
					       ParIntGraph pig_before){
	HMethod callee = q.method();
	if(!callee.getName().equals("clone") ||
	   !callee.getDeclaringClass().getName().equals("java.lang.Object"))
	    return null;

	if(DEBUG)
	    System.out.println("NATIVE (special): " + callee);

	ParIntGraph pig_after0 = pig_before;
	ParIntGraph pig_after1 = (ParIntGraph) pig_before.clone();

	// do the actions of the "clone()" method: create a new object (n_R),
	// copy the fields from the objects passed as params to clone to n_R
	NodeRepository node_rep = pa.getNodeRepository();
	// [AS] clone is a special way of creating objects; therefore,
	// it returns an INSIDE node, not a RETURN node.  This also
	// protects us against losses of precision if we coalesce all
	// RETURN nodes into LOST.
	PANode n_R = node_rep.getCodeNode(q, PANode.INSIDE2);

	Set lo_types = aux_clone_get_types(q);
	if(DEBUG)
	    System.out.println("CLONE: POSSIBLE TYPES:" + lo_types);

	Set flags = aux_clone_get_obj_fields(lo_types);
	if(DEBUG)
	    System.out.println("CLONE: POSSIBLE FLAGS:" + flags);

	int nb_flags = flags.size();

	Temp lo  = q.params(0);
	Set srcs = pig_before.G.I.pointedNodes(lo);

	Iterator it = flags.iterator();
	for(int i = 0; i < nb_flags; i++){
	    String f = (String) it.next();
	    for(Object n_srcO : srcs){
		PANode n_src = (PANode) n_srcO;
		aux_clone_treat_pseudo_ldst(q, f, n_R, n_src, pig_after0,
					    node_rep);
	    }
	}

	// set the link l_R to the RETURN node
	Temp l_R = q.retval();
	if(l_R != null)
	    pig_after0.G.I.addEdge(l_R, n_R);

	aux_native_treat_excp(q, pig_after1, node_rep);

	return new ParIntGraphPair(pig_after0, pig_after1);
    }

    
    // Aux method for all the special treatments of native methods. If the
    // called native method can return an exception, and there is a Temp l_E
    // to receive (a pointer to) it, a PANode.EXCEPT node associated with this
    // CALL is created (if it doesn't exist yet) and the proper link is set
    // from l_E to n_E, in the parallel interaction graph "pig".
    private static void aux_native_treat_excp(CALL q, ParIntGraph pig,
					      NodeRepository node_rep){
	Temp l_E = q.retex();
	HMethod hm = q.method();

	if((l_E == null) || (hm.getExceptionTypes().length == 0)) return;

	pig.G.I.removeEdges(l_E);
	PANode n_E = node_rep.getCodeNode(q, PANode.EXCEPT);
	pig.G.I.addEdge(l_E, n_E);
	pig.G.e.addMethodHole(n_E, hm);
	// no pig.G.propagate is necessary since n_E cannot point to anything
	// that is not escaped into that native method.
    }

    // treat specially "public static native void java.lang.System.arraycopy
    // (Object src, int, Object dst, int, int);
    // We hope that we really know what arraycopy does ...
    private static ParIntGraphPair treat_arraycopy(PointerAnalysis pa, CALL q,
						   ParIntGraph pig_before) {
	HMethod hm = q.method();
	if(!hm.getName().equals("arraycopy") ||
	   !hm.getDeclaringClass().getName().equals("java.lang.System"))
	    return null;

	if(DEBUG)
	    System.out.println("NATIVE (special): " + Util.code2str(q));

	// the conventional field name used for the array's entries
	final String f = PointerAnalysis.ARRAY_CONTENT;

	Temp l_src = q.params(0);
	Temp l_dst = q.params(2);

	Set dst_set = 
	    PointerAnalysis.selectArraysOfObjs
	    (pig_before.G.I.pointedNodes(l_dst));
	Set src_set = 
	    PointerAnalysis.selectArraysOfObjs
	    (pig_before.G.I.pointedNodes(l_src));

	Set set_S = pig_before.G.I.pointedNodes(src_set, f);

	Set set_E = new HashSet();
	Set gtypes = new HashSet();
	for(Object nodeO : src_set){
	    PANode node = (PANode) nodeO;
	    if(pig_before.G.e.hasEscaped(node)) {
		set_E.add(node);
		Set compTypes = PointerAnalysis.getObjArrayComp(node);
		for(Iterator it2 = compTypes.iterator(); it2.hasNext(); )
		    gtypes.add(new GenType((HClass) it2.next(), GenType.POLY));
	    }
	}

	NodeRepository node_rep = pa.getNodeRepository();

	if(set_E.isEmpty())
	    pig_before.G.I.addEdges(dst_set, f, set_S);
	else {
	    GenType[] gts = 
		(GenType[]) gtypes.toArray(new GenType[gtypes.size()]);
	    PANode load_node = node_rep.getCodeNode(q, PANode.LOAD, gts);

	    pig_before.G.O.addEdges(set_E, f, load_node);

	    if(!PointerAnalysis.IGNORE_EO)
		pig_before.eo.add(set_E, f, load_node, pig_before.G.I);

	    set_S.add(load_node);
	    pig_before.G.I.addEdges(dst_set, f, set_S);

	    pig_before.G.propagate(set_E);

	    if(PointerAnalysis.RECORD_ACTIONS) {
		// update the action repository
		Set active_threads = pig_before.tau.activeThreadSet();
		for(Iterator it_E = set_E.iterator(); it_E.hasNext(); )
		    pig_before.ar.add_ld((PANode) it_E.next(), f, load_node,
					 ActionRepository.THIS_THREAD,
					 active_threads);
	    }
	}

	ParIntGraph pig_after1 = (ParIntGraph) pig_before.clone();
	// Set the edges for the exception node in graph 1.
	Temp l_E = q.retex();
	if(l_E != null){
	    pig_after1.G.I.removeEdges(l_E);
	    PANode n_E = node_rep.getCodeNode(q, PANode.EXCEPT);
	    pig_after1.G.I.addEdge(l_E, n_E);
	    pig_after1.G.e.addMethodHole(n_E, hm);
	}
	return new ParIntGraphPair(pig_before, pig_after1);	
    }


    private static Set wes_methods = new HashSet();
    private static void build_rtj_methods(Linker linker) {
	String[][] methods = {
	    {"javax.realtime.RealtimeThread", "getMemoryArea"},
	    {"javax.realtime.RealtimeThread", "currentRealtimeThread"},
	    {"javax.realtime.MemoryArea",     "checkAccess"},
	    {"javax.realtime.MemoryArea",     "bless"},
	    {"javax.realtime.Stats",          "addCheck"},
	    {"javax.realtime.Stats",          "addNewObject"},
	    {"javax.realtime.Stats",          "addNewArrayObject"},
	    {"javax.realtime.HeapMemory",     "instance"},
	    {"javax.realtime.ImmortalMemory", "instance"},
	    {"javax.realtime.MemoryArea",     "enterMemBlock"}
	};
	for(int i = 0; i < methods.length; i++)
	    wes_methods.addAll
		(getMethods(methods[i][0], methods[i][1], linker));
 
	if(DEBUG)
	    Util.print_collection(wes_methods, "Wes RTJ methods");
    }

    // Many native methods don't do any synchronizations on their object
    // parameters, don't store them in static fields and don't modify the
    // points-to graph accessible from these object parameters.
    private static boolean isUnharmful(HMethod hm) {
	return
	    uhms.contains(hm) ||
	    (CONSIDER_WES_GOOD && wes_methods.contains(hm)); 
    }
    // Checks whether a method is totally harmful (i.e. all the parameters
    // must be marked as escaping into it). A CALL to such a method cannot
    // be treated otherwise than by skipping it.
    private static boolean isTotallyHarmful(HMethod hm) {
	return !isUnharmful(hm); // for the moment, conservative treatment
    }
    public static Set getUnharmfulMethods() { return uhms; }
    private static Set uhms = new HashSet();
    private static void build_uhms(Linker linker) {
	String[][] methods = {
	    {"java.io.File", "length0"},

	    {"java.io.FileInputStream", "open"},
	    {"java.io.FileInputStream", "close"},
	    {"java.io.FileInputStream", "available"},
	    {"java.io.FileInputStream", "read"},
	    {"java.io.FileInputStream", "readBytes"},

	    {"java.io.FileOutputStream", "open"},
	    {"java.io.FileOutputStream", "openAppend"},
	    {"java.io.FileOutputStream", "close"},
	    {"java.io.FileOutputStream", "write"},
	    {"java.io.FileOutputStream", "writeBytes"},

	    {"java.lang.Throwable", "printStackTrace0"},
	    {"java.lang.Throwable", "fillInStackTrace"},
	    {"java.lang.Class", "getName"},
	    {"java.lang.Class", "getPrimitiveClass"},

	    {"java.net.SocketOutputStream", "socketWrite"},
	    {"java.net.SocketInputStream",  "socketRead"},

	    {"java.net.PlainSocketImpl", "socketClose"},
	    {"java.net.PlainSocketImpl", "socketAvailable"},

	    {"java.net.PlainSocketImpl", "socketAccept"},
	    {"java.net.PlainSocketImpl", "socketBind"},
	    {"java.net.PlainSocketImpl", "socketCreate"},
	    {"java.net.PlainSocketImpl", "socketListen"},

	    {"java.lang.Object", "hashCode"},
	    {"java.lang.Object", "equals"},
	    {"java.lang.Object", "toString"},

	    // this one does a sync on its argument; that's why it
	    // we also create a graph modelling its execution
	    {"java.lang.Object", "wait"},

	    {"java.lang.Thread", "isAlive"},
	    {"java.lang.Object", "getClass"},

	    {"java.lang.Class",  "isArray"},
	    {"java.lang.Class",  "getComponentType"},

	    {"java.lang.reflect.Method", "getName"},
	    {"java.lang.reflect.Method", "getDeclaringClass"},
	    {"java.lang.reflect.Method", "getModifiers"},
	    {"java.lang.reflect.Method", "getReturnType"},
	    {"java.lang.reflect.Method", "getParameterTypes"},
	    {"java.lang.reflect.Method", "getExceptionTypes"},

	    {"java.lang.reflect.Constructor", "getModifiers"},
	    {"java.lang.reflect.Constructor", "getParameterTypes"},
	    {"java.lang.reflect.Constructor", "getTypes"},

	    {"java.lang.reflect.Field", "getDeclaringClass"},
	    {"java.lang.reflect.Field", "getName"},
	    {"java.lang.reflect.Field", "getType"},

	    {"java.lang.reflect.Field", "getByte"},
	    {"java.lang.reflect.Field", "setByte"},
	    {"java.lang.reflect.Field", "getInt"},
	    {"java.lang.reflect.Field", "setInt"},
	    {"java.lang.reflect.Field", "getBoolean"},
	    {"java.lang.reflect.Field", "setBoolean"},
	    {"java.lang.reflect.Field", "getFloat"},
	    {"java.lang.reflect.Field", "setFloat"},
	    {"java.lang.reflect.Field", "getDouble"},
	    {"java.lang.reflect.Field", "setDouble"},
	    {"java.lang.reflect.Field", "getLong"},
	    {"java.lang.reflect.Field", "setLong"},
	    {"java.lang.reflect.Field", "getShort"},
	    {"java.lang.reflect.Field", "setShort"},
	    {"java.lang.reflect.Field", "getModifiers"},

	    {"java.lang.reflect.Array", "getLength"},
	    {"java.lang.reflect.Array", "getByte"},
	    {"java.lang.reflect.Array", "setByte"},
	    {"java.lang.reflect.Array", "getInt"},
	    {"java.lang.reflect.Array", "setInt"},
	    {"java.lang.reflect.Array", "getBoolean"},
	    {"java.lang.reflect.Array", "setBoolean"},
	    {"java.lang.reflect.Array", "getFloat"},
	    {"java.lang.reflect.Array", "setFloat"},
	    {"java.lang.reflect.Array", "getDouble"},
	    {"java.lang.reflect.Array", "setDouble"},
	    {"java.lang.reflect.Array", "getLong"},
	    {"java.lang.reflect.Array", "setLong"},
	    {"java.lang.reflect.Array", "getShort"},
	    {"java.lang.reflect.Array", "setShort"},

	    {"java.lang.reflect.Method", "getDeclaringClass"},
	    {"java.lang.reflect.Constructor", "getDeclaringClass"}
	};

	for(int i = 0; i < methods.length; i++)
	    uhms.addAll(getMethods(methods[i][0], methods[i][1], linker));

	if(DEBUG)
	    Util.print_collection(uhms, "Unharmful methods");
    }

    // Returns all the methods having the name m_name
    // that are declared in class c_name.
    private static Collection getMethods(String c_name, String m_name,
					 Linker linker) {
	List retval = new LinkedList();
	try {
	HClass hclass = linker.forName(c_name);
	HMethod[] hms = hclass.getDeclaredMethods();
	for(int i = 0; i < hms.length; i++)
	    if(m_name.equals(hms[i].getName()))
		retval.add(hms[i]);
	} catch (NoSuchClassException e) { /* ignore this class */ }
	return retval;
    }


    private static ParIntGraph model_native(HMethod hm) {
	ParIntGraph pig = (ParIntGraph) graphs_for_natives.get(hm); 
	return pig;
    }

    private static Map graphs_for_natives = new HashMap();
    private static void build_graphs_for_natives(PointerAnalysis pa) {
	add_graph4java_lang_Object_wait(pa);
    }

    private static void add_graph4java_lang_Object_wait
	(PointerAnalysis pa) {

	// java_lang_Object_wait
	HMethod hm = get_method(pa, "java.lang.Object", "wait", 0);
	MetaMethod mm = new MetaMethod(hm, true);

	NodeRepository nodes = pa.getNodeRepository();
	nodes.addParamNodes(mm, 1);
	PANode node = nodes.getParamNode(mm, 0);

	ParIntGraph pig = new ParIntGraph();
	if(PointerAnalysis.RECORD_ACTIONS)
	    pig.ar.add_sync
		(new PASync(node, ActionRepository.THIS_THREAD,null), null);

	graphs_for_natives.put(hm, pig);
    }


    private static HMethod get_method(PointerAnalysis pa,
				      String cls_name, String mthd_name,
				      int nb_args) {
	Set set = new HashSet();
	HClass hclass = pa.getLinker().forName(cls_name);
	assert hclass != null : cls_name + " was not found!";

	HMethod[] hms = hclass.getMethods();
	for(int i = 0; i < hms.length; i++)
	    if(hms[i].getName().equals(mthd_name) &&
	       (hms[i].getParameterTypes().length == nb_args))
		set.add(hms[i]);

	assert set.size() > 0 : mthd_name + "(" + nb_args +
		    ") not found in " + cls_name;
	assert set.size() == 1 : "Too many methods";

	return (HMethod) set.iterator().next();
    }

    // should be called before any other method
    static void static_init(PointerAnalysis pa) {
	build_uhms(pa.getLinker());

	if(CONSIDER_WES_GOOD)
	    build_rtj_methods(pa.getLinker());

	build_graphs_for_natives(pa);

	jlO_equals   = get_method(pa, "java.lang.Object", "equals", 1);
	jlO_hashCode = get_method(pa, "java.lang.Object", "hashCode", 0);
	jlO_toString = get_method(pa, "java.lang.Object", "toString", 0);
    }

    private static HMethod jlO_equals   = null;
    private static HMethod jlO_hashCode = null;
    private static HMethod jlO_toString = null;
}// end of the class
