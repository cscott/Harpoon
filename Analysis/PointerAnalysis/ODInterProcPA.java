// ODInterProcPA.java, created Tue Jan 18 11:04:20 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import java.lang.reflect.Modifier;

import harpoon.IR.Quads.CALL;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Temp.Temp;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.Loader;

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;

import harpoon.Util.TypeInference.ExactTemp;
import harpoon.Util.TypeInference.TypeInference;
import harpoon.IR.Quads.QuadFactory;


import harpoon.Util.Collections.LinearSet;
import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.LightMap;
import harpoon.Util.DataStructs.RelationEntryVisitor;

import harpoon.Util.Util;

/**
 * <code>ODInterProcPA</code> is a &quot;functional&quot; class (i.e. it 
 * contains just some methods, no persistent data) that wraps
 * the inter-procedural part of the pointer analysis. Normally, this should
 * be a part of the <code>ODPointerAnalysis</code>, but that class is already
 * too big and some code segmentation is always good!<br>
 * In the implementation of this class, most of the methods are static and
 * have <code>ODPointerAnalysis pa</code> as their first argument. This stands
 * for the <code>this</code> <i>hidden</i> argument that would exist if all
 * those methods were in the <code>ODPointerAnalysis</code> class.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: ODInterProcPA.java,v 1.4 2002-04-10 03:00:42 cananian Exp $
 */
abstract class ODInterProcPA {

    /** Call sites with more than <code>MAX_CALLEES</code> callees are simply
     *  considered to be holes. */ 
    public static final int MAX_CALLEES = 5;

    /** Activates a lot of debug messages. */
    public static final boolean DEBUG = false;

    /** Displays some warnings, <i>eg</i> for the call sites with 0 callees.
     *  This is not necessarily an error! For example, if an application
     *  never instantiates a <code>SecurityManager</code>,
     *	each call to a method of that class has ZERO callees! */
    public static final boolean WARNINGS = true;


    /** Specify the behaviour of the mapUP method for on demand
     * analysis in very tricky cases.
     */
    public static boolean ret_strong_update = false;
    public static boolean exc_strong_update = false;


    /** Analyzes the call site <code>q</code> inside 
     *	<code>current_method</code>. If analyzing the call is not possible
     *	(e.g. one of the callees is native, hence unanalyzable), the call
     *	site is skipped and all the parameters are marked as escaping
     *	through that call site.
     *
     *	@param pa  The <code>ODPointerAnalysis</code> object that
     *	calls this method. This parameter stands for the <code>this</code>
     *	pointer that would exist if this method were in the
     *	<code>ODPointerAnalysis</code> class.
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
    public static ODParIntGraphPair analyze_call(ODPointerAnalysis pa,
					       MetaMethod current_mmethod,
					       CALL q,
					       ODParIntGraph pig_before) {
	return analyze_call(pa, current_mmethod, q, pig_before, null, true, true);
    }

    public static ODParIntGraphPair analyze_call(ODPointerAnalysis pa,
					       MetaMethod current_mmethod,
					       CALL q,
					       ODParIntGraph pig_before,
					       MethodHole hole) {

	return analyze_call(pa, current_mmethod, q, pig_before, null, true, true);
    }

    public static ODParIntGraphPair analyze_call(ODPointerAnalysis pa,
					       MetaMethod current_mmethod,
					       CALL q,
					       ODParIntGraph pig_before,
					       MethodHole hole,
					       boolean verbose) {

	return analyze_call(pa, current_mmethod, q, pig_before, null, verbose, true);
    }

    public static ODParIntGraphPair analyze_call(ODPointerAnalysis pa,
					       MetaMethod current_mmethod,
					       CALL q,
					       ODParIntGraph pig_before,
					       MethodHole hole,
					       boolean verbose,
					       boolean updateformalnodes) {
	if(DEBUG)
	    System.out.println("Inter-procedural analysis " + q);

	ODParIntGraphPair pp_after = null;
	System.out.println("analyze_call");

	// treat some very special native methods
	//tbu
	if((pp_after = treatVerySpecialNatives(pa, q, pig_before)) != null)
	    return pp_after;

	MetaMethod[] mms = null;
	if (hole==null){
//  	    System.out.println("Hole is null for " + current_mmethod + " and call " +
// 			       Debug.code2str(q));
	    mms = pa.getMetaCallGraph().getCallees(current_mmethod, q);
//  	    System.out.println("Result is " + mms);
//  	    System.out.println("Size is " + mms.length);
// 	    MetaMethod[] allthecallees =
// 		pa.getMetaCallGraph().getCallees(current_mmethod);
// 	    System.out.println("Printing the " + allthecallees.length 
// 			       + " callees...");
// 	    for(int i=0; i<allthecallees.length; i++){
// 		System.out.println("Callee " + i + ": " + allthecallees[i]);
// 	    }
		
	}
	else
	    {
//  		System.out.println("Hole is not null");
		mms = hole.callees();
	    }
	int nb_callees   = mms.length;

	// I count on the fact that if a call site has 0 callees, it 
	// means that it doesn't occur in practice (because some classes
	// are not instantiated), not that the call graph is buggy!
	// So, the CALL is simply ignored.
	if(nb_callees == 0){
	    if(WARNINGS){
		System.out.println("Warning: CALL site with no callee! ");
		System.out.println("Warning:  " + Debug.code2str(q));
	    }
 	    return new ODParIntGraphPair(pig_before, pig_before);
	}

	// Due to the imprecisions in the call graph, most of them due to
	// dynamic dispatches, several call sites have a huge number of callees
	// These CALLs are not analyzed (i.e. they are treated as method holes)
	if(nb_callees > MAX_CALLEES){
	    if(DEBUG)
		System.out.println("TOO MANY CALLEES (" + nb_callees + ") "+q);
	    // Classical skip_call as, even in ODA, we are not going
	    // to try later on to analyze this method hole.
	    return skip_call(pa, q, pig_before, q.method());
	}

 	// For each analyzable callee mm, we store it in mms and its associated
	// parallel interaction graph in pigs. By "analyzable", we mean
	// (meta)-methods that are analyzable by the the Pointer Analysis PLUS
	// the so-called unharmful native methods - native method that we can't
	// analyze as we don't have their code but we know what their pig will
	// look like (for most of them it's empty - they don't create new
	// pointer links).
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
	boolean allnatives=true;
	ODParIntGraph pigs[] = new ODParIntGraph[nb_callees];
	for(int i = 0; i < nb_callees; i++){
	    HMethod hm = mms[i].getHMethod();

	    if(Modifier.isNative(hm.getModifiers())) {
		//if(DEBUG)
		    System.out.println("NATIVE: " + hm);
		if(isTotallyHarmful(hm)) {
		    if(DEBUG)
			System.out.print("NEED TO SKIP: " + Debug.code2str(q));
		    // Classical skip_call as, even in ODA, we are not
		    // going to try later on to analyze this method
		    // hole.
		    return skip_call(pa, q, pig_before, hm);
		}
		else {
		    pigs[nb_callees_with_pig] = null;
		    mms[nb_callees_with_pig]  = mms[i];
		    nb_callees_with_pig++;
		    continue;
		}
	    }
	    
	    if(!(ODPointerAnalysis.analyzable(hm))){
		if(DEBUG)
		    System.out.println("NEED TO SKIP: " + Debug.code2str(q));
		// Classical skip_call as, even in ODA, we are not
		// going to try later on to analyze this method hole.
		return skip_call(pa, q, pig_before, hm);
	    }

	    // If we are here, mms[i] does not correspond to a native
	    // method.
	    allnatives=false;
	    
	    //FV Take the pig computed at the previous level
// 	    The skip happens later on
// 	    if ((pa.BOUNDED_ANALYSIS_DEPTH==true)&&
// 		(pa.FIRST_ANALYSIS==true))
// 		return skip_call(pa, current_mmethod, q, pig_before, 
// 				 q.method(), mms);


	    // There will be a problem here...
	    // Should be refined later on (tbu)
	    if (!((pa.ON_DEMAND_ANALYSIS)&&(hole==null))){
		pa.current_analysis_depth--;
		ODParIntGraph pig = null;
		if (pa.ON_DEMAND_ANALYSIS){
		    pig = pa.getIntParIntGraph(mms[i],true);
		}
		else {
		    pig = pa.getExtParIntGraph(mms[i], false);
		}
 		pa.current_analysis_depth++;
		if(pig != null){
		    pigs[nb_callees_with_pig] = pig;
		    mms[nb_callees_with_pig]  = mms[i];
 		    nb_callees_with_pig++;
		}
	    }
	}


	//FV if we are analyzing at depth 0 in a bounded analyzis, we
	//just have to skip the call
	// Should be refined later on (tbu)
	if ((!allnatives)&&
	    (hole==null)&&
	    (ODPointerAnalysis.ON_DEMAND_ANALYSIS))
	    return skip_call(pa, current_mmethod, q, pig_before, 
			     q.method(), mms);
	// If none of the callees has been analyzed yet, do not do anything
	// (this can happen only in the strongly connected components of 
	// mutually recursive methods).
	if(nb_callees_with_pig == 0){
	    return new ODParIntGraphPair(pig_before, pig_before);}

	// Specialize the graphs of the callees for the context sensitive PA
	if(ODPointerAnalysis.CALL_CONTEXT_SENSITIVE)
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
	// minimum number of clone() (cloning a ODParIntGraph is very expensive).

	// 1. Special case: only one callee; no ODParIntGraph is cloned.
	if(nb_callees_with_pig == 1) {
	    pp_after =  mapUp(pa, mms[0], current_mmethod, q, 
			      pig_before, pigs[0], hole, verbose,
			      updateformalnodes);
	    // tbu
// 	    if (hole!=null)
// 		clean_call(pp_after,hole);

// 	    if (pp_after.pig[0].isCoherent())
// 		System.out.println("PIG coherent (end of analyze_call)");
// 	    else{
// 		System.err.println("PIG incoherent (end of analyze_call)");
// 		System.out.println("PIG incoherent (end of analyze_call)");
// 		System.out.println(pp_after.pig[0]);
// 	    }
// 	    if (pp_after.pig[1].isCoherent())
// 		System.out.println("PIG coherent (end of analyze_call)");
// 	    else{
// 		System.err.println("PIG incoherent (end of analyze_call)");
// 		System.out.println("PIG incoherent (end of analyze_call)");
// 		System.out.println(pp_after.pig[1]);
// 	    }


	    return pp_after;
	}

	// 2. More than one callee: 
	// 2.1. Compute the first term of the join operation.
	ODParIntGraph pig_clone = (ODParIntGraph) pig_before.clone();
	boolean jointype = true;
	if(hole!=null) jointype = false;

	pp_after = 
	    mapUp(pa, mms[0], current_mmethod, q, 
		  pig_before, pigs[0], hole, verbose,
		  updateformalnodes);
	

	// 2.2. Join to it all the others, except for the last one.
	for(int i = 1; i < nb_callees_with_pig - 1; i++) {
	    pp_after.join(mapUp(pa, mms[i], current_mmethod, q, 
				(ODParIntGraph)pig_clone.clone(),
				pigs[i], hole, verbose, updateformalnodes),
			  jointype);
	}

	// 2.3. Finally, join the graph for the last callee.
	MetaMethod last_mm = mms[nb_callees_with_pig - 1];
	pp_after.join
	    (mapUp(pa, last_mm, current_mmethod, q, 
		   pig_clone, pigs[nb_callees_with_pig-1], hole, verbose, 
		   updateformalnodes),
	     jointype);

	return pp_after; // the long awaited moment!
    }
    

    /** Updates the ODParIntGraph when the CALL is skipped. Two graphs are
	returned: one for the normal return from the procedure, the other
	one for a return due to an exception. */
    // Called even in ODA, on methods which won't be analyzed
    // afterwards.
    private static ODParIntGraphPair skip_call(ODPointerAnalysis pa,
					     CALL q, ODParIntGraph pig_caller,
					     HMethod hm) {

// 	if (ODPointerAnalysis.ON_DEMAND_ANALYSIS)
// 	    System.out.println("Classical skip_call");

	if(DEBUG)
	    System.out.println("SKIP: " + q);
	System.out.println("SKIP: " + q);
	System.out.println("Classical skip_call " + hm);

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
	//  3. No-update of the method holes and method holes history
	//  even if in ODA: this information is only needed is the
	//  holes will be analyzed later-on, which is not going to be
	//  the case!

	// clone the graph: we will have two distinct versions of it:
	// one on the 0-edge, the other on the 1-edge, corresponding
	// to the normal execution (return) respectively execution with
	// exceptions (throw).
	ODParIntGraph pig_caller1 = (ODParIntGraph) (pig_caller.clone());

	// The names of the variables closely match those used in the
	// formal description of the algorithm (section 9.2)

	// Set the edges for the result node in graph 0.
	// avoid generating useless nodes
	Temp l_R = q.retval();
	if(l_R != null){
	    pig_caller.G.I.removeEdges(l_R);
	    if(!hm.getReturnType().isPrimitive()){
		PANode n_R = node_rep.getCodeNode(q, PANode.RETURN);
		pig_caller.G.I.addEdge(l_R, n_R);
		// l_R is a variable: nothing more for ODA
		pig_caller.G.e.addMethodHole(n_R, hm);
	    }
	}

	// Set the edges for the exception node in graph 1.
	Temp l_E = q.retex();
	if(l_E != null){
	    pig_caller1.G.I.removeEdges(l_E);
	    PANode n_E = node_rep.getCodeNode(q, PANode.EXCEPT);
	    pig_caller1.G.I.addEdge(l_E, n_E);
		// l_E is a variable: nothing more for ODA
	    pig_caller1.G.e.addMethodHole(n_E, hm);
	}

	return new ODParIntGraphPair(pig_caller, pig_caller1);
    }
    
    private static ODParIntGraphPair skip_call(ODPointerAnalysis pa,
					     MetaMethod caller_mmethod,
					     CALL q, ODParIntGraph pig_caller,
					     HMethod hm,
					     MetaMethod [] mms) {

	System.out.println("SKIP: " + q);
	System.out.println("Modified  skip_call " + hm);
// 	System.out.println("Pig (caller) before skip " + pig_caller);

	if(DEBUG)
	    System.out.println("SKIP: " + q);

	NodeRepository node_rep = pa.getNodeRepository();

	// Construct the set S_M of the objects escaped through this unanalyzed
	// method invocation site.
	Set S_M = new HashSet();
	Temp[] params = q.params();
	Set caller_params[] = new Set[params.length];
	for(int i = 0; i < params.length; i++){
	    caller_params[i] = pig_caller.G.I.pointedNodes(params[i]);
	    S_M.addAll(caller_params[i]);
	}

// 	System.out.println(" number of objects escaping through this "
// 			   +" unanalyzed method invocation site " +
// 			   S_M.size() + "/" + params.length);

	// Update the escape information
	//  1. all the parameters are directly escaping into the method hole
	for(Iterator it = S_M.iterator(); it.hasNext(); )
	    pig_caller.G.e.addMethodHole((PANode)it.next(), hm);

	//  2. propagate the new escape information
	pig_caller.G.propagate(S_M);




// 	// Just printing...
// 	System.out.println(" before the modifications ");
// 	System.out.println(" number of params : " + params.length);
// 	for(int i = 0; i < params.length; i++){
// 	    System.out.print(" " + i + "-th param");
// 	    Iterator parsetit=pig_caller.G.I.pointedNodes(params[i]).iterator();
// 	    while(parsetit.hasNext()){
// 		PANode n = (PANode) parsetit.next();
// 		System.out.print(" " + n);
// 	    }
// 	    System.out.println("");
// 	}
		

	// clone the graph: we will have two distinct versions of it:
	// one on the 0-edge, the other on the 1-edge, corresponding
	// to the normal execution (return) respectively execution with
	// exceptions (throw).
	ODParIntGraph pig_caller1 = (ODParIntGraph) (pig_caller.clone());

	// The names of the variables closely match those used in the
	// formal description of the algorithm (section 9.2)

	// Set the edges for the result node in graph 0.
	// avoid generating useless nodes
	Temp l_R = q.retval();
	PANode n_R = null;
	if(l_R != null){
	    pig_caller.G.I.removeEdges(l_R);
	    if(!hm.getReturnType().isPrimitive()){
		n_R = node_rep.getCodeNode(q, PANode.RETURN);
		pig_caller.G.I.addEdge(l_R, n_R);
		// l_R is a variable : nothing to add for ODA
		pig_caller.G.e.addMethodHole(n_R, hm);
	    }
	}

	// Set the edges for the exception node in graph 1.
	Temp l_E = q.retex();
	PANode n_E = null;
	if(l_E != null){
	    pig_caller1.G.I.removeEdges(l_E);
	    n_E = node_rep.getCodeNode(q, PANode.EXCEPT);
	    pig_caller1.G.I.addEdge(l_E, n_E);
	    // l_E is a variable : nothing to add for ODA
	    pig_caller1.G.e.addMethodHole(n_E, hm);
	}


	// 3. Update the MethodHole Set and method holes history of
	// the caller, if we are in ODA.
	if (pa.ON_DEMAND_ANALYSIS){
	    // Computation of the escaped called site number (in case
	    // of loop or recursion). This is a quick but safe hack.
// 	    int number=0;
// 	    for(Iterator n_it=pig_caller.method_holes.iterator();
// 		n_it.hasNext();){
// 		int n = ((MethodHole) n_it.next()).rank();
// 		if (n>number) number=n;
// 	    }
// 	    number++;
	    
	    if(pa.ODA_precise)
		pa.mh_number++;

	    //tbu depth...
	    MethodHole hole  = new MethodHole(q, S_M, mms, caller_params, 
					      n_R, n_E, pa.mh_number, 1);
	    pig_caller.odi.addHole(hole);
	    pig_caller1.odi.addHole(hole);
	}

	return new ODParIntGraphPair(pig_caller, pig_caller1);
    }
    

    // Treat the calls to native methods.
    private static ODParIntGraphPair treatNatives(ODPointerAnalysis pa,
						MetaMethod mm,
						CALL q,
						ODParIntGraph pig_caller) {
	//TBU
	NodeRepository node_rep = pa.getNodeRepository();
	HMethod hm = mm.getHMethod();
	if(DEBUG) System.out.println("treatNatives: " + hm);

	//	System.out.println("ERROR treatNatives: " + hm + " NOT MODIFIED !" );

	ODParIntGraph pig_before = (ODParIntGraph) pig_caller.clone();

	if(!isUnharmful(hm))
	    markEscapedParameters(q, hm, pig_before);

	ODParIntGraph pig0 = (ODParIntGraph) pig_before.clone();
	ODParIntGraph pig1 = (ODParIntGraph) pig_before.clone();

	Temp l_R = q.retval();
	if((l_R != null) && !hm.getReturnType().isPrimitive()){
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

	return new ODParIntGraphPair(pig0, pig1);	
    }

    private static void markEscapedParameters(CALL q, HMethod hm,
					      ODParIntGraph pig) {
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
							ODParIntGraph pig) {
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
    private static ODParIntGraphPair mapUp(ODPointerAnalysis pa,
					 MetaMethod mm,
					 CALL q, 
					 ODParIntGraph pig_caller,
					 ODParIntGraph pig_callee) {
	System.err.println("ERROR: mapUp unmodified should not be called");

	// if native method, apply special treatment
	if(pig_callee == null)
	    return treatNatives(pa, mm, q, pig_caller);

	PANode[] callee_params = pa.getParamNodes(mm);

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
	pig_caller.insertAllButArEo(pig_callee, mu, false, params);

	// bring the actions of the callee into the caller's graph
	bring_actions(pig_caller.ar, pig_callee.ar,
		      pig_caller.tau.activeThreadSet(), mu);

	// bring the edge ordering relation into the caller's graph
	if(!ODPointerAnalysis.IGNORE_EO)
	    bring_eo(pig_caller.eo, old_caller_I, pig_callee.eo,
		     pig_callee.G.O, mu);

	// recompute the escape info
	pig_caller.G.propagate();

	if(DEBUG)
	    System.out.println("Unsimplified graph:\n" + pig_caller);

	// simplify the graph by removing the empty loads
// 	pig_caller.removeEmptyLoads();

	if(DEBUG)
	    System.out.println("Simplified graph:\n" + pig_caller);

	// make a copy of the parallel interaction graph
	ODParIntGraph pig_caller1 = (ODParIntGraph) (pig_caller.clone());

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

	return new ODParIntGraphPair(pig_caller, pig_caller1);
    }
    
    private static ODParIntGraphPair mapUp(ODPointerAnalysis pa,
					 MetaMethod callee_mm,
					 MetaMethod caller_mm,
					 CALL q, 
					 ODParIntGraph pig_caller,
					 ODParIntGraph pig_callee) {

	return mapUp(pa, callee_mm, caller_mm, q, 
		     pig_caller, pig_callee, 
		     null, true, true);
    }
    
    private static ODParIntGraphPair mapUp(ODPointerAnalysis pa,
					 MetaMethod callee_mm,
					 MetaMethod caller_mm,
					 CALL q, 
					 ODParIntGraph pig_caller,
					 ODParIntGraph pig_callee,
					 MethodHole m_hole) {
	return mapUp(pa, callee_mm, caller_mm, q, 
		     pig_caller, pig_callee, 
		     m_hole, true, true);
    }

    private static ODParIntGraphPair mapUp(ODPointerAnalysis pa,
					 MetaMethod callee_mm,
					 MetaMethod caller_mm,
					 CALL q, 
					 ODParIntGraph pig_caller,
					 ODParIntGraph pig_callee,
					 MethodHole virtual_m_hole,
					 boolean verbose,
					 boolean updateformalnodes) {
	long m_start = System.currentTimeMillis();
	System.out.println("*** in mapUp:");
	ODPointerAnalysis.number_of_mapups++;
	long u_end = 0, u_start = 0;

	HashSet toberemoved = new HashSet();

	// Real hole computation
	MethodHole m_hole = null;

	if (virtual_m_hole==null){
	    m_hole = virtual_m_hole;
	}
	else {
	    m_hole = virtual_m_hole.IsInAs(pig_caller.odi.skippedCS);

	    if (m_hole==null){
		System.err.println("Hole not found !!!");
		System.out.println("Hole not found !!!");
		System.out.println("Hole = " + virtual_m_hole);
		System.out.println("Set = " + pig_caller.odi.skippedCS);
	    }
	    else {
		pig_caller.odi.skippedCS.remove(m_hole);
		if (virtual_m_hole.IsInAs(pig_caller.odi.skippedCS)!=null){
		    System.err.println("Hole found TWICE !!!");
		    System.out.println("Hole found TWICE !!!");
		    System.out.println("Hole original = " + virtual_m_hole);
		    System.out.println("Hole found = " + m_hole);
// 		    System.out.println("Set whithout hole= " + pig_caller.method_holes);
		}	
	    }
	}


	if ((m_hole!=null)&&
	    (!ODPointerAnalysis.ON_DEMAND_ANALYSIS))
	    System.err.println("Error in mapUp:  arguments inconsistent with context.");


	// if native method, apply special treatment
	if(pig_callee == null){
	    if (m_hole==null)
		return treatNatives(pa, callee_mm, q, pig_caller);
	    else{
		System.err.println("Problem in mapUp, semi-inconsistency");
		System.out.println("Problem in mapUp, semi-inconsistency");
		System.out.println("Hole: " + m_hole);
		System.out.println(pig_caller);
		pig_caller.odi.remove(m_hole);
		return treatNatives(pa, callee_mm, q, pig_caller);
	    }
	}


	PANode [] callee_params = pa.getParamNodes(callee_mm);
	
// 	pig_caller.G.flushCaches();
// 	pig_caller.G.propagate();

    	if(DEBUG)
	    {
 		System.out.println("Pig_caller:" + pig_caller);
		System.out.println("Pig_callee:" + pig_callee);
	    }


	// get the initial mapping: formal param -> actual parameter,
	// and class node -> class node
	Relation first_mapping = null;
	if (m_hole==null)
	    first_mapping = get_initial_mapping(q,pig_caller,pig_callee,callee_params);
	else
	    first_mapping = get_initial_mapping(q,pig_callee,callee_params,m_hole);

	// Printing
        if(verbose) System.out.println("First mapping " + first_mapping);
	
	if (verbose) {
	    for(int i=0; i<callee_params.length;i++){ 
		System.out.print("Param " + callee_params[i] + "->");
		Iterator it_img = 
		    first_mapping.getValues(callee_params[i]).iterator();
		while(it_img.hasNext()) {
		    PANode node_img = (PANode) it_img.next();
		    System.out.print(" " + node_img);
		}
		System.out.println();
	    }
	}


	if(DEBUG) System.out.println("Initial mapping:" + first_mapping);

	// update the node mapping by matching outside edges from the caller
	// with inside and outside edges from the callee
	//tbu
	if(verbose) System.out.println("Before matching edges:" + first_mapping);
	if ((m_hole==null)||(!pa.ODA_precise))
	    match_edges(first_mapping, pig_caller, pig_callee);
	else
	    match_edges(first_mapping, pig_caller, pig_callee, m_hole);

	if(DEBUG) System.out.println("After matching edges:" + first_mapping);
	if (verbose) 
	    System.out.println("After matching edges:" + first_mapping);
	
	// all the nodes from the callee (except for PARAM) are
	// initially inserted into the caller's graph
	compute_the_final_mapping(first_mapping,pig_caller,pig_callee);

	if(verbose)
	    System.out.println("After final mapping:" + first_mapping);


	// Inserts the image of the callee's graph into the caller's graph.
	Set params = new HashSet();
	for(int i=0;i<callee_params.length;i++)
	    params.add(callee_params[i]);

	if (!ODPointerAnalysis.ON_DEMAND_ANALYSIS){
	    PAEdgeSet old_caller_I = (PAEdgeSet) pig_caller.G.I.clone();
	    pig_caller.insertAllButArEo(pig_callee, first_mapping, false, params);
	    
	    // bring the actions of the callee into the caller's graph
	    bring_actions(pig_caller.ar, pig_callee.ar,
			  pig_caller.tau.activeThreadSet(), first_mapping);

	    // bring the edge ordering relation into the caller's graph
	    if(!ODPointerAnalysis.IGNORE_EO)
		bring_eo(pig_caller.eo, old_caller_I, pig_callee.eo,
			 pig_callee.G.O, first_mapping);
	}
	else{
	    // tbu problem if m_hole==null
	    Set holes_b4_callee = pig_caller.odi.predecessors(m_hole);
	    
	    PAEdgeSet org_O = (PAEdgeSet) pig_caller.G.O.clone();
	    PAEdgeSet org_I = (PAEdgeSet) pig_caller.G.I.clone();

	    ODInformation temp_odi = new ODInformation(pig_caller.odi.precise);
	    ODInformation tmp_odi_2 = new ODInformation(pig_caller.odi.precise);

	    if(verbose) System.out.println("B4 insertAllButArEoTau");
	    pig_caller.insertAllButArEoTau(pig_callee, first_mapping, 
					   params, 
					   holes_b4_callee,
					   temp_odi);
	    if(verbose) System.out.println("After insertAllButArEoTau");

	    
	    pig_caller.G.flushCaches();
	    pig_caller.G.propagate();
// 	    System.out.println("After first mapping " + pig_caller);

	    // The following won't be used if m_hole!=null, but the
	    // definition is common to enable more code sharing.
	    Relation second_mapping = null;
	    LightRelation second_mapping_extended = 
		new LightRelation();

	    if (m_hole!=null){
		// We compute the interaction between the part of the
		// caller pig *after* the call site, and the callee.

		Relation nu = new LightRelation();
		if ((m_hole.ret()!=null)&&
		    (pig_callee.G.r!=null)&&(!(pig_callee.G.r.isEmpty())))
		    nu.addAll(m_hole.ret(),pig_callee.G.r);
		if ((m_hole.exc()!=null)&&
		    (pig_callee.G.excp!=null)&&(!(pig_callee.G.excp.isEmpty())))
		    nu.addAll(m_hole.exc(),pig_callee.G.excp);

		if(verbose) System.out.println("Mapping before match " + nu);

		match_edges(nu, first_mapping,
			    org_O, 
			    pig_callee, pig_caller, 
			    m_hole);

 		if (verbose) System.out.println("Mapping after match " + nu);
		
		Relation extended_first_mapping = 
		    (Relation) first_mapping.clone();
		
		second_mapping = 
		    projection(nu, extended_first_mapping);

 		if (verbose) 
		    System.out.println("Mapping after projection " + second_mapping);

		pig_caller.insertAllButArEoTau(org_O,
					       org_I,
					       second_mapping,
					       tmp_odi_2);
    

	    pig_caller.G.flushCaches();
	    pig_caller.G.propagate();
// 	    System.out.println("After second mapping " + pig_caller);
		

		if (updateformalnodes){
		    if (m_hole.ret()!=null)
			toberemoved.add(m_hole.ret());
		    if (m_hole.exc()!=null)
			toberemoved.add(m_hole.exc());
		}

		second_mapping_extended = 
		    create_extended_mapping(pig_caller,
					    second_mapping,
					    toberemoved);

	    }

	    // ************************
	    // Updating escape functions of actual return and exception nodes
	    // ************************
	    
	    if (m_hole.ret()!=null){
		Set ret_methodholes = pig_caller.G.e.methodHolesSet(m_hole.ret());
		if((!updateformalnodes)&&(ODInterProcPA.ret_strong_update))
		    ret_methodholes.remove(m_hole.callsite().method());
		Set ret_nodesholes  = pig_caller.G.e.nodeHolesSet(m_hole.ret());

		for(Iterator ret_it=pig_callee.G.r.iterator(); ret_it.hasNext(); ){
		    PANode ret_n = (PANode) ret_it.next();
		    pig_caller.G.e.addMethodHoles(ret_n, ret_methodholes);
		    pig_caller.G.e.addNodeHoles(ret_n, ret_nodesholes);
		}
	    }

	    if (m_hole.exc()!=null){
		Set exc_methodholes = pig_caller.G.e.methodHolesSet(m_hole.exc());
		if((!updateformalnodes)&&(ODInterProcPA.exc_strong_update))
		    exc_methodholes.remove(m_hole.callsite().method());
		Set exc_nodesholes  = pig_caller.G.e.nodeHolesSet(m_hole.exc());
		for(Iterator excp_it=pig_callee.G.excp.iterator(); excp_it.hasNext(); ){
		    PANode excp_n = (PANode) excp_it.next();
		    pig_caller.G.e.addMethodHoles(excp_n, exc_methodholes);
		    pig_caller.G.e.addNodeHoles  (excp_n, exc_nodesholes); 
		}
	    }

	    pig_caller.G.flushCaches();
	    pig_caller.G.propagate();
// 	    System.out.println("After escape function update " + pig_caller);


	    // **********************************
	    // Updating return and exception sets
	    // **********************************

	    if (m_hole!=null){
		Set new_ret  = new LinearSet();
		projection(pig_caller.G.r,    
			   second_mapping_extended, new_ret);
		Set new_excp = new LinearSet();
		projection(pig_caller.G.excp, 
			   second_mapping_extended, new_excp);

		pig_caller.G.r    = new_ret;
		pig_caller.G.excp = new_excp;
	    }
// 	    else{
// 		new_ret  = pig_caller.G.r;
// 		new_excp = pig_caller.G.excp;
// 	    }

	    
	    // **********************************
	    // Updating the classical part of the graph
	    // **********************************

	    // Cleaning the formal exception and return node of the
	    // method hole.
	    if (updateformalnodes)
		clean_call(pig_caller, m_hole);

	    // Recomputing the escape info
	    pig_caller.G.flushCaches();
	    pig_caller.G.propagate();
	    pig_caller.G.getReachableFromR();
	    pig_caller.G.getReachableFromExcp();
	    
	    if(DEBUG)
		System.out.println("Unsimplified graph:\n" + pig_caller);
	    
	    // simplify the graph by removing the empty loads
	    //pig_caller.removeEmptyLoads();
	    Set emptyloads   = new HashSet();
	    Set fakeOutEdges = new HashSet();
	    pig_caller.removeEmptyLoads(emptyloads, fakeOutEdges);

  	    if(DEBUG)
		System.out.println("first_mapping : " + first_mapping);	    
 	    if(DEBUG)
		System.out.println("Empty loads : " + emptyloads);

	    
	    toberemoved.addAll(emptyloads);

	    if(DEBUG)
		System.out.println("Simplified graph:\n" + pig_caller);

	    // Cleaning the first and second mapping from the empty
	    // load nodes. (The second mapping is notbused later on,
	    // we only update the seconde mapping extended which is
	    // used).
	    for(Iterator load_it = emptyloads.iterator(); load_it.hasNext(); ){
		PANode n = (PANode) load_it.next();
		first_mapping.remove(n,n);
		second_mapping_extended.remove(n,n);
	    }

	    u_start = System.currentTimeMillis();
	    ODInformation new_odi = new ODInformation(pig_caller.odi.precise);
	    Set par_locks = pig_caller.odi.update(new_odi, pig_caller, pig_callee, 
						  first_mapping, second_mapping_extended,
						  m_hole,
						  temp_odi, tmp_odi_2, toberemoved);
	    u_end = System.currentTimeMillis();
		
	    // ***************************
	    // Updating active threads set
	    // ***************************
		
	    PAThreadMap new_caller_threads = null;

	    if (m_hole!=null){
		new_caller_threads = (PAThreadMap) pig_caller.tau.clone();
		new_caller_threads.insert(new_caller_threads,
					  second_mapping_extended);
	    }
	    else {
		new_caller_threads = pig_caller.tau;
	    }

	    new_caller_threads.insert(pig_callee.tau, first_mapping);
	    
	    PAThreadMap new_callee_threads = new PAThreadMap();
	    new_callee_threads.insert(pig_callee.tau, first_mapping);


	    // ***************************
	    // Updating action repository
	    // ***************************
	    
	    // updating the actions of the caller (if necessary)
	    ActionRepository new_ar_caller = null;
	    if (m_hole!=null){
		new_ar_caller = new ActionRepository();
		update_actions(pig_caller.ar, new_ar_caller, 
			       second_mapping_extended);
	    }
	    else {
		new_ar_caller = pig_caller.ar;
	    }

	    // bring the actions of the callee into the caller's graph
	    // The use of pig_caller.tau.activeThreadSet() is
	    // conservative.
	    bring_actions(new_ar_caller, pig_callee.ar,
			  pig_caller.tau.activeThreadSet(), first_mapping);
	    
	    // add actions relative to loads of the caller which
	    // happened in parallel to the callee
	    if (m_hole!=null){
		actions_add_ld(pig_callee.odi,
			       pig_caller,
			       new_ar_caller,
			       second_mapping_extended,
			       m_hole,
			       new_callee_threads.activeThreadSet());
		
		actions_add_sync(new_ar_caller,
				 par_locks,
				 new_callee_threads.activeThreadSet());
	    }

	    
	    
	    //***********************
	    // Storing the new values
	    //***********************
	    
	    pig_caller.odi = new_odi;
	    pig_caller.tau    = new_caller_threads;
	    pig_caller.ar     = new_ar_caller;
	}

	// Cleaning the formal exception and return node.
	if (updateformalnodes)
	    clean_call(pig_caller, m_hole);
	
	// recompute the escape info
	pig_caller.G.flushCaches();
	pig_caller.G.propagate();
	pig_caller.G.getReachableFromR();
	pig_caller.G.getReachableFromExcp();
	

	if(DEBUG)
	    System.out.println("Unsimplified graph:\n" + pig_caller);
	
	// simplify the graph by removing the empty loads
 	//pig_caller.removeEmptyLoads();
// 	Set emptyloads   = new HashSet();
// 	Set fakeOutEdges = new HashSet();
// 	pig_caller.removeEmptyLoads(emptyloads, fakeOutEdges);

	if(DEBUG)
	    System.out.println("Simplified graph:\n" + pig_caller);

	// make a copy of the parallel interaction graph
	ODParIntGraph pig_caller1 = null;
	
	
	if (m_hole==null){
	    pig_caller1 = (ODParIntGraph) (pig_caller.clone());
	    // set the edges for the exception on the out-edge 1
	    set_edges_res_ex(q.retex() , first_mapping, pig_caller1, pig_callee.G.excp);
	    // set the edges for the result on the out-edge 0
	    set_edges_res_ex(q.retval(), first_mapping, pig_caller,  pig_callee.G.r);
	}
	else{
	    pig_caller1 = pig_caller;
	    // pig_callee.G.excp and pig_callee.G.r have already been
	    // updated and we are not interested in the value of variables...
	}
	

       	if(DEBUG){
	    System.out.println("Final graphs:{");
	    System.out.println(" The graph on edge 0:");
	    System.out.println(pig_caller);
	    System.out.println(" The graph on edge 1:");
	    System.out.println(pig_caller1);
	    System.out.println("}");
	}

	long m_end = System.currentTimeMillis();
// 	System.err.println("Timing mapUp: " + (m_end - m_start) +  "ms for "+
// 			   caller_mm);
//  	System.out.println("Final graph:");
//  	System.out.println(pig_caller);

	System.out.print("Timing mapUp: " + (m_end - m_start) +  "ms for "+
			   caller_mm);
	System.out.println("[update : " + (u_end - u_start) + "]");
	return new ODParIntGraphPair(pig_caller, pig_caller1);
    }
    

    
//     private static void map_callee_method_holes(ODPointerAnalysis pa,
// 						MetaMethod caller_mm, 
// 						MetaMethod callee_mm,
// 						Relation mu,
// 						CALL q){

// 	// Preparation of the caller enhanced escape function
// 	Set _holeset = (Set)pa.hash_method_holes.get(caller_mm);
// 	HashSet holeset =  null;
// 	if (_holeset==null)
// 	    holeset = new HashSet();
// 	else
// 	    holeset = (HashSet) _holeset;


// 	// Specialization of the callee enhanced escape function
// 	Set calleeset = (Set) pa.hash_method_holes.get(callee_mm);

// 	if (calleeset==null)
// 	    System.out.println("  no callee enhanced escape function");
// 	else{
// 	    HashSet calleeHset = (HashSet) calleeset;
// 	    HashSet mappedcalleeset  = new HashSet();

// 	    System.out.println("  Original callee enhanced escape function");
// 	    for(Iterator it = calleeHset.iterator(); it.hasNext(); ){
// 		MethodHole hole = (MethodHole) it.next();
// 		System.out.println("   " + hole);
// 	    }
	    
// 	    // Loop on the MethodHoles
// 	    for(Iterator it = calleeHset.iterator(); it.hasNext(); ){
// 		MethodHole hole = (MethodHole) it.next();

// 		Set [] old_caller_params = hole.ungroupedparameters();
// 		Set oldparams   = hole.parameters();
// 		CALL site       = hole.callsite();
// 		HMethod hm      = site.method();

// 		// Initialization of the new array of parameters
// 		Set [] new_caller_params = new Set[old_caller_params.length];
// 		for(int i = 0; i< old_caller_params.length; i++)
// 		    new_caller_params[i] = new HashSet();

// 		// Loop on the parameters
// 		for(int i = 0; i< old_caller_params.length; i++)
// 		    if ((old_caller_params[i]!=null)&&
// 			(!(old_caller_params[i].isEmpty())))
// 			// Loop on the possible values for the current
// 			// parameter
// 			for(Iterator parit = (old_caller_params[i]).iterator(); 
// 			    parit.hasNext(); ){
// 			    PANode param = (PANode) parit.next();

// 			    // If the node param is a key in mu, it is
// 			    // a formal parameter. We project it on
// 			    // the correspondant possible actual
// 			    // argument.
// 			    if (mu.containsKey(param)){
// 				Set projection = mu.getValues(param);
// 				(new_caller_params[i]).addAll(projection);
// 			    }
// 			    else
// 				// Otherwise, we check whether the
// 				// node param is a node which is
// 				// specialized, and replace it by its
// 				// specialization.
// 				if ((ODPointerAnalysis.CALL_CONTEXT_SENSITIVE)&&
// 				    (param.csSpecialize(q)!=null))
// 				    (new_caller_params[i]).add(param.csSpecialize(q));
// 				else
// 				    (new_caller_params[i]).add(param);
// 			}
// 		// Finally the mapped and specialized callee's
// 		// MethodHoles is added to the set.
// 		mappedcalleeset.add(new MethodHole(hole, new_caller_params));
// 		}
		

// 	    System.out.println("  Updated callee enhanced escape function");
// 	    for(Iterator it = mappedcalleeset.iterator(); it.hasNext(); ){
// 		MethodHole hole = (MethodHole) it.next();
// 		System.out.println("   " + hole);
// 	    }
	    
// 	    // The set of the mapped and specialized callee's
// 	    // MethodHoles is added to the caller's
// 	    holeset.addAll((Set)mappedcalleeset);
// 	    pa.hash_method_holes.put(caller_mm, holeset);
// 	}
//     }


    
    /** Sets the initial mapping: each formal parameter is mapped
     * to the nodes pointed to by the corresponding actual argument
     * (into the caller graph) and each static node is mapped to itself.
     * (only the static nodes that really point to something are important,
     * the leaf static nodes are not interesting in our analysis)
     */
    private static Relation get_initial_mapping(CALL q,
						ODParIntGraph pig_caller,
						ODParIntGraph pig_callee,
						PANode[] callee_params){
	Relation mu = new LightRelation();
	Temp[] args = q.params();
	int object_params_count = 0;

	// map the object formal parameter nodes to the actual arguments
	System.out.println("get_initial_mapping for " + args.length + " parameter(s)");
	for(int i = 0; i < args.length; i++){
	    if(!q.paramType(i).isPrimitive()){
// 		System.out.println(" for the " + i + "-th parameter (" + 
// 				   callee_params[object_params_count] + ")");
		
		mu.addAll(callee_params[object_params_count],
			  pig_caller.G.I.pointedNodes(args[i]));
		object_params_count++;

		// Just for debugging
		Set caller_params = pig_caller.G.I.pointedNodes(args[i]);
		if (caller_params.size()==0){
		    System.out.println("  param " + args[i] + " -> emptyset");
		}
		else{
		    Iterator parsetit=(caller_params).iterator();
		    System.out.print("  param " + args[i] + " ->");
		    while(parsetit.hasNext()){
			PANode n = (PANode) parsetit.next();
			System.out.print(" " + n);
		    }
		    System.out.println(" .");
		}

		
	    }
	    else{
		System.out.println(" for the " + i + "-th parameter.");
	    }
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

	// map the static nodes to themselves;
	// only the static nodes that appear as sources of the outside edges
	// must be initially mapped
	process_STATICs(pig_callee.G.O.allSourceNodes(), mu);

	return mu;
    }
    
    private static Relation get_initial_mapping(CALL q,
						ODParIntGraph pig_callee,
						PANode[] callee_params,
						MethodHole hole){
	Relation mu = new LightRelation();
	Temp[] args = q.params();
	Set [] caller_params = hole.ungroupedparameters();
	int object_params_count = 0;

	// map the object formal parameter nodes to the actual arguments
// 	System.out.println("get_initial_mapping for " + args.length + " parameter(s)");
	for(int i = 0; i < args.length; i++){
	    if(!q.paramType(i).isPrimitive()){
// 		System.out.println(" for the " + i + "-th parameter (" + 
// 				   callee_params[object_params_count] + ")");
		
		mu.addAll(callee_params[object_params_count],
			  caller_params[i]);
		object_params_count++;


		// Just for debugging
		Set caller_params_i = caller_params[i];
		if (caller_params_i.size()==0){
		    System.out.println("  param " + i + " -> emptyset");
		}
// 		else{
// 		    Iterator parsetit=(caller_params_i).iterator();
// 		    System.out.print("  param " + args[i] + " ->");
// 		    while(parsetit.hasNext()){
// 			PANode n = (PANode) parsetit.next();
// 			System.out.print(" " + n);
// 		    }
// 		    System.out.println(" .");
// 		}

		
	    }
// 	    else{
// 		System.out.println(" for the " + i + "-th parameter.");
// 	    }
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

	// map the static nodes to themselves;
	// only the static nodes that appear as sources of the outside edges
	// must be initially mapped
	process_STATICs(pig_callee.G.O.allSourceNodes(), mu);

	return mu;
    }

    
    private static Relation get_secondary_mapping(CALL q,
						  ODParIntGraph pig_callee,
						  PANode[] callee_params,
						  MethodHole hole,
						  Set nodes,
						  Relation mu,
						  boolean type){
	Relation nu = new LightRelation();
	Temp[] args = q.params();
	Set [] caller_params = hole.ungroupedparameters();

	// map the actual arguments to themselves
	for(int i = 0; i < args.length; i++)
	    if(!q.paramType(i).isPrimitive())
		for(Iterator node_it=caller_params[i].iterator(); node_it.hasNext();){
		    PANode node = (PANode) node_it.next();
		    nu.add(node,node);
		}

	PANode org = null;
	if (type==true) {//We are dealing with the return nodes
	    // We map the formal return node to the actuals ones
	    org   = hole.ret();
	}
	else{
	    // We map the formal exception node to the actuals ones
	    org = hole.exc();
	}

	// Set of the return/exception nodes (after the mu mapping)
	final Set mu_nodes = new HashSet();
	Iterator it = nodes.iterator();
	while(it.hasNext())
	    mu_nodes.addAll(mu.getValues((PANode) it.next()));
	//	pig_caller.G.I.addEdges(l,mu_nodes);
	
	nu.addAll(org,mu_nodes);

	System.out.print("   " + org + "->");
	if ((nodes!=null)&&(!nodes.isEmpty()))
	    for(Iterator n_it=nodes.iterator(); n_it.hasNext();){
		PANode n = (PANode) n_it.next();
		System.out.print(" " + n);
	    }
	System.out.print(" ->");
	if ((mu_nodes!=null)&&(!mu_nodes.isEmpty()))
	    for(Iterator m_it=mu_nodes.iterator(); m_it.hasNext();){
		PANode m_n = (PANode) m_it.next();
		System.out.print(" " + m_n);
	    }
	System.out.println(".");
	
	return nu;
    }
    
    private static Relation get_secondary_mapping(ODParIntGraph pig_callee,
						  PANode[] callee_params,
						  MethodHole hole){
	Relation nu = new LightRelation();
	CALL q = hole.callsite();
	int n_args = q.params().length;
	Set [] caller_params = hole.ungroupedparameters();

	int object_params_count = 0;

	// map the object actual argumenst nodes to the formal parameters
	for(int i = 0; i < n_args; i++){
	    if(!q.paramType(i).isPrimitive()){
		for(Iterator it = caller_params[i].iterator(); it.hasNext();)
		    nu.add(it.next(),callee_params[object_params_count]);
		
		object_params_count++;
	    }
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

	// Map the formal return node to the actual return nodes
// 	System.out.println("before adding return hole " + hole.ret());
// 	print_relation(nu);
	nu.addAll(hole.ret(),  pig_callee.G.r);
// 	System.out.println(" after adding return hole " + hole.ret());
// 	print_relation(nu);

	// Map the formal exception node to the actual exception nodes
	nu.addAll(hole.exc(), pig_callee.G.excp);



	// map the static nodes to themselves; only the static nodes
	// that appear as sources of the outside edges must be
	// initially mapped
	//
	// Is this necessary ?

	process_STATICs(pig_callee.G.O.allSourceNodes(), nu);
	
	return nu;
    }

    
    private static void map_mapping(Relation reference, Relation addenda){
	Set keys = addenda.keys();
	for(Iterator k_it = keys.iterator(); k_it.hasNext();){
	    PANode mapped = (PANode) k_it.next();
	    Set image = addenda.getValues(mapped);
	    reference.addAll(mapped, projection(image, reference, new LinearSet()));
	}
    }


    
//     private static Set [] get_caller_params(CALL q, ODParIntGraph pig_caller){
// 	Temp[] args = q.params();
// 	Set caller_params[args];
// 	int object_params_count = 0;

// 	// map the object formal parameter nodes to the actual arguments
// 	System.out.println("get_initial_mapping for " + args.length + " parameter(s)");
// 	for(int i = 0; i < args.length; i++){
// 	    if(!q.paramType(i).isPrimitive()){
// 		caller_params[i] = pig_caller.G.I.pointedNodes(args[i]);

// 		if (caller_params[i].size()==0){
// 		    System.out.println("  param " + args[i] + " -> emptyset");
// 		}
// 		else{
// 		    Iterator parsetit=(caller_params).iterator();
// 		    System.out.print("  param " + args[i] + " ->");
// 		    while(parsetit.hasNext()){
// 			PANode n = (PANode) parsetit.next();
// 			System.out.print(" " + n);
// 		    }
// 		    System.out.println(" .");
// 		}
// 		object_params_count++;
		
// 	    }
// 	    else{
// 		System.out.println(" for the " + i + "-th parameter.");
// 	    }
// 	}

// 	return caller_params;
//     }
    
    // aux method for get_initial_mapping
    private static void process_STATICs(final Set set, final Relation mu) {
	for(Iterator it = set.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(node.type == PANode.STATIC)
		mu.add(node, node);
	}
    }

    

    /** Matches outside edges from the graph of (used by ) the callee
	against inside and outside edges from the graph of (created
	by) the caller.  (repeated application of constraint 2). The
	goal of this method is to resolve the load nodes from the
	callee, i.e. to detect the nodes from the caller that each
	load node might represent. */
    private static void match_edges(Relation mu,
				    ODParIntGraph pig_caller,
				    ODParIntGraph pig_callee){

	PAWorkList W = new PAWorkList();
	// here is the new stuff; only nodes with new stuff are
	// put in the worklist W.
	Relation new_info = (Relation) mu.clone();

	W.addAll(mu.keys());
	while(!W.isEmpty()){
	    PANode node1 = (PANode) W.remove();

	    // nodes3 stands for all the new instances of n3
	    // from the inference rule
	    HashSet nodes3 = new HashSet(new_info.getValues(node1));
	    new_info.removeKey(node1);

	    Iterator itf = pig_callee.G.O.allFlagsForNode(node1).iterator();
	    while(itf.hasNext()) {
		String f = (String) itf.next();

		// nodes2 stands for all the nodes that could play
		// the role of n2 from the inference rule
		Set nodes2 = pig_callee.G.O.pointedNodes(node1,f);
		if(nodes2.isEmpty()) continue;

		// nodes4 stands for all the nodes that could play
		// the role of n4 from the inference rule
		Set nodes4 = pig_caller.G.I.pointedNodes(nodes3,f);
		nodes4.addAll(pig_caller.G.O.pointedNodes(nodes3,f));

		if(nodes4.isEmpty()) continue;

		// set up the relation from any node from nodes2
		// to any node from nodes4
		for(Iterator it2 = nodes2.iterator(); it2.hasNext(); ) {
		    PANode node2 = (PANode)it2.next();
		    boolean changed = false;
		    for(Iterator it4 = nodes4.iterator(); it4.hasNext(); ) {
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

//     private static void match_edges(Relation mu,
// 				    ODParIntGraph pig_caller,
// 				    ODParIntGraph pig_callee,
// 				    MethodHole hole){

// 	PAWorkList W = new PAWorkList();
// 	// here is the new stuff; only nodes with new stuff are
// 	// put in the worklist W.
// 	Relation new_info = (Relation) mu.clone();

// 	W.addAll(mu.keys());
// 	while(!W.isEmpty()){
// 	    PANode node1 = (PANode) W.remove();

// 	    // nodes3 stands for all the new instances of n3
// 	    // from the inference rule
// 	    HashSet nodes3 = new HashSet(new_info.getValues(node1));
// 	    new_info.removeKey(node1);

// 	    Iterator itf = pig_callee.G.O.allFlagsForNode(node1).iterator();
// 	    while(itf.hasNext()) {
// 		String f = (String) itf.next();

// 		// nodes2 stands for all the nodes that could play
// 		// the role of n2 from the inference rule
// 		Set nodes2 = pig_callee.G.O.pointedNodes(node1,f);
// 		if(nodes2.isEmpty()) continue;

// 		// nodes4 stands for all the nodes that could play
// 		// the role of n4 from the inference rule
// 		for(Iterator n3_it = nodes3.iterator(); n3_it.hasNext(); ) {
// 		    PANode node3 = (PANode) n3_it.next();
// 		    Set nodes4 = pig_caller.G.I.pointedNodes(node3,f);
// 		    if ((nodes4==null)||(nodes4.isEmpty())) continue;

// 		    Map i_from_node3 = (Map) pig_caller.in_edge_always.get(node3);
// 		    if (i_from_node3==null) {
// 			System.err.println("Error. Map should not be null...");
// 			System.out.println("Error. Map should not be null...");
// 			System.out.println("Map (pig_caller.in_edge_always) : ");
// 			ODPointerAnalysis.print_edge_mh_map(pig_caller.in_edge_always);
// 			System.out.println(" n1 " + node1 + " -> " + f +
// 					   " -> " + nodes2);
// 			System.out.println(" n1 " + node1 + " |-> nodes3 " 
// 					   + nodes3);
// 			System.out.println(" n3 " + node3 + " -> " + f +
// 					   " -> " + nodes4);

// 			continue;
// 		    }
		    
// 		    Relation node3_f = (Relation) i_from_node3.get(f);
// 		    if (node3_f==null){
// 			System.err.println("Error. Relation should not be null...");
// 			continue;
// 		    }

// 		    for(Iterator it4 = nodes4.iterator(); it4.hasNext(); ) {
// 			PANode node4 = (PANode)it4.next();
// 			Set node3_f_node4 = (Set) node3_f.getValues(node4);
			
// 			// The edge may preexist the call site to be
// 			// considered
// 			if (!node3_f_node4.contains(hole)){
// 			    // set up the relation from any node from
// 			    // nodes2 to any node from nodes4

// 			    for(Iterator it2 = nodes2.iterator(); it2.hasNext(); ) {
// 				PANode node2 = (PANode)it2.next();
// 				if(mu.add(node2,node4)){
// 				    new_info.add(node2,node4);
// 				    // nodes with new info are put in the worklist
// 				    W.add(node2);
// 				}
// 			    }
// 			}
// 		    }
// 		}


// 		// nodes6 stands for all the nodes that could play
// 		// the role of n6 from the inference rule
// 		for(Iterator n5_it = nodes3.iterator(); n5_it.hasNext(); ) {
// 		    PANode node5 = (PANode) n5_it.next();
// 		    Set nodes6 = pig_caller.G.O.pointedNodes(node5,f);
// 		    //		    if (nodes6==null) continue;
// 		    if ((nodes6==null)||(nodes6.isEmpty())) continue;

// 		    Map i_from_node5 = (Map) pig_caller.out_edge_always.get(node5);
// 		    if (i_from_node5==null) {
// 			System.err.println("Error. Map should not be null...");
// 			System.out.println("Error. Map should not be null...");
// 			System.out.println("Map : ");
// 			ODPointerAnalysis.print_edge_mh_map(pig_caller.out_edge_always);
// 			continue;
// 		    }
		    
// 		    Relation node5_f = (Relation) i_from_node5.get(f);
// 		    if (node5_f==null){
// 			System.err.println("Error. Relation should not be null...");
// 			continue;
// 		    }

// 		    for(Iterator it6 = nodes6.iterator(); it6.hasNext(); ) {
// 			PANode node6 = (PANode)it6.next();
// 			Set node5_f_node6 = (Set) node5_f.getValues(node6);
			
// 			// The edge may preexist the call site to be
// 			// considered
// 			if (!node5_f_node6.contains(hole)){
// 			    // set up the relation from any node from
// 			    // nodes2 to any node from nodes6

// 			    for(Iterator it2 = nodes2.iterator(); it2.hasNext(); ) {
// 				PANode node2 = (PANode)it2.next();
// 				if(mu.add(node2,node6)){
// 				    new_info.add(node2,node6);
// 				    // nodes with new info are put in the worklist
// 				    W.add(node2);
// 				}
// 			    }
// 			}
// 		    }
// 		}
// 	    }
// 	}
//     }

    private static void match_edges(Relation mu,
				    ODParIntGraph pig_caller,
				    ODParIntGraph pig_callee,
				    MethodHole hole)
    {
	PAWorkList W = new PAWorkList();
	// here is the new stuff; only nodes with new stuff are
	// put in the worklist W.
	Relation new_info = (Relation) mu.clone();

	W.addAll(mu.keys());
	while(!W.isEmpty()){
	    PANode node1 = (PANode) W.remove();

	    // nodes3 stands for all the new instances of n3
	    // from the inference rule
	    HashSet nodes3 = new HashSet(new_info.getValues(node1));
	    new_info.removeKey(node1);

	    Iterator itf = pig_callee.G.O.allFlagsForNode(node1).iterator();
	    while(itf.hasNext()) {
		String f = (String) itf.next();

		// nodes2 stands for all the nodes that could play
		// the role of n2 from the inference rule
		Set nodes2 = pig_callee.G.O.pointedNodes(node1,f);
		if(nodes2.isEmpty()) continue;

		// nodes4 stands for all the nodes that could play
		// the role of n4 from the inference rule
		for(Iterator n3_it = nodes3.iterator(); n3_it.hasNext(); ) {
		    PANode node3 = (PANode) n3_it.next();
		    Set nodes4 = pig_caller.G.I.pointedNodes(node3,f);
		    if ((nodes4==null)||(nodes4.isEmpty())) continue;

		    Map i_from_node3 = (Map) pig_caller.odi.inAlways.edges.get(node3);
		    if (i_from_node3==null) {
			System.err.println("Error. Map should not be null...");
			System.out.println("Error. Map should not be null...");
			System.out.println("Map (pig_caller.in_edge_always) : " +
					   pig_caller.odi.inAlways);
			System.out.println(" n1 " + node1 + " -> " + f +
					   " -> " + nodes2);
			System.out.println(" n1 " + node1 + " |-> nodes3 " 
					   + nodes3);
			System.out.println(" n3 " + node3 + " -> " + f +
					   " -> " + nodes4);
			
			continue;
		    }
		    
		    Relation node3_f = (Relation) i_from_node3.get(f);
		    if (node3_f==null){
			System.err.println("Error. Relation should not be null...");
			continue;
		    }
		    
		    for(Iterator it4 = nodes4.iterator(); it4.hasNext(); ) {
			PANode node4 = (PANode)it4.next();
			Set node3_f_node4 = (Set) node3_f.getValues(node4);
			
			// The edge may preexist the call site to be
			// considered
			if (!node3_f_node4.contains(hole)){
			    // set up the relation from any node from
			    // nodes2 to any node from nodes4

			    for(Iterator it2 = nodes2.iterator(); it2.hasNext(); ) {
				PANode node2 = (PANode)it2.next();
				if(mu.add(node2,node4)){
				    new_info.add(node2,node4);
				    // nodes with new info are put in the worklist
				    W.add(node2);
				}
			    }
			}
		    }
		}

		
		// nodes6 stands for all the nodes that could play
		// the role of n6 from the inference rule
		for(Iterator n5_it = nodes3.iterator(); n5_it.hasNext(); ) {
		    PANode node5 = (PANode) n5_it.next();
		    Set nodes6 = pig_caller.G.O.pointedNodes(node5,f);
		    //		    if (nodes6==null) continue;
		    if ((nodes6==null)||(nodes6.isEmpty())) continue;

		    Map i_from_node5 = (Map) pig_caller.odi.outAlways.edges.get(node5);
		    if (i_from_node5==null) {
			System.err.println("Error. Map should not be null...");
			System.out.println("Error. Map should not be null...");
			System.out.println("Map : "+
					   pig_caller.odi.outAlways);
			continue;
		    }
		    
		    Relation node5_f = (Relation) i_from_node5.get(f);
		    if (node5_f==null){
			System.err.println("Error. Relation should not be null...");
			continue;
		    }

		    for(Iterator it6 = nodes6.iterator(); it6.hasNext(); ) {
			PANode node6 = (PANode)it6.next();
			Set node5_f_node6 = (Set) node5_f.getValues(node6);
			
			// The edge may preexist the call site to be
			// considered
			if (!node5_f_node6.contains(hole)){
			    // set up the relation from any node from
			    // nodes2 to any node from nodes6

			    for(Iterator it2 = nodes2.iterator(); it2.hasNext(); ) {
				PANode node2 = (PANode)it2.next();
				if(mu.add(node2,node6)){
				    new_info.add(node2,node6);
				    // nodes with new info are put in the worklist
				    W.add(node2);
				}
			    }
			}
		    }
		}
	    }
	}
    }

    

    // This function is only used for the second phase of the matching
    // caller/callee in case of ODA, when the mapup is made after an
    // initial skip.

//     private static void match_edges(Relation mu,
// 				    Relation first_mapping,
// 				    PAEdgeSet O,
// 				    ODParIntGraph pig_callee,
// 				    Map edges_maybe,
// 				    MethodHole hole){

// 	PAWorkList W = new PAWorkList();
// 	// here is the new stuff; only nodes with new stuff are
// 	// put in the worklist W.
// 	Relation new_info    = (Relation) mu.clone();

// 	// We store all the nodes that we know must be mapped (formal
// 	// return and exception nodes)
// 	W.addAll(mu.keys());
// 	Set formal_nodes = mu.keys();

// 	// In order to know which nodes in the caller are mapped to by
// 	// nodes in the callee, we ``reverse'' the first mapping.
// 	Relation ancestor = new LightRelation();
// 	for(Iterator key_it=first_mapping.keys().iterator(); key_it.hasNext(); ){
// 	    PANode source = (PANode) key_it.next();
// 	    for(Iterator sinks_it=first_mapping.getValues(source).iterator();
// 		sinks_it.hasNext(); )
// 		ancestor.add(sinks_it.next(),source);
// 	}

// 	// We look for outside edges in the caller, which may happen
// 	// *after* the skipped call site, and whose tail is mapped
// 	// unto by one node in the callee.
// 	for(Iterator n_it=edges_maybe.keySet().iterator(); n_it.hasNext(); ){
// 	    boolean not_included = true; 

// 	    PANode tail = (PANode) n_it.next();
// 	    // If one of the callee nodes is mapped to this node we
// 	    // process it, else we are not going any further.
// // 	    if (ancestor.getValues(tail)==null) continue;
// 	    if (!(ancestor.containsKey(tail))) continue;

// 	    Map from_tail = (Map) edges_maybe.get(tail);
// 	    for(Iterator k_it = from_tail.keySet().iterator(); 
// 		k_it.hasNext() && not_included; )
// 		{
// 		String f = (String) k_it.next();
// 		Relation from_f = (Relation) from_tail.get(f);
// 		for(Iterator t_it = from_f.keys().iterator(); 
// 		    t_it.hasNext() && not_included; )
// 		    {
// 		    PANode head = (PANode) t_it.next();
// 		    if (from_f.getValues(head).contains(hole)){
// 			W.add(tail);
// 			not_included=false;
// 		    }
// 		}
// 	    }
// 	}

// 	while(!W.isEmpty()){
// 	    PANode node1 = (PANode) W.remove();

// 	    // nodes3 stands for all the new instances of n3
// 	    // from the inference rule
// 	    HashSet nodes3 = new HashSet(new_info.getValues(node1));
// 	    new_info.removeKey(node1);

// 	    Map from_node1 = (Map) edges_maybe.get(node1);
// 	    if (from_node1==null){
// // 		if (!(formal_nodes.contains(node1))){
// // 		    System.err.println("Strange... (really possible !)");
// // 		    System.out.println("Strange... (really possible !)");
// // 		    System.out.println("n1 " + node1);
// // 		    System.out.println("edges_maybe ");
// // 		    ODPointerAnalysis.print_edge_mh_map(edges_maybe);
// // 		}
// 		continue;
// 	    }

// // 	    Iterator itf = pig_caller.G.O.allFlagsForNode(node1).iterator();
// 	    Iterator itf = O.allFlagsForNode(node1).iterator();
// 	    while(itf.hasNext()) {
// 		String f = (String) itf.next();
// 		Relation node1_f = (Relation) from_node1.get(f); 
// 		if (node1_f==null){
// 		    // In fact, this happens if the outside edge was
// 		    // created during the first part of the
// 		    // mapping. In that case, this edge is not a
// 		    // candidate.
//  		    System.err.println("Bug... ");
// 		    System.out.println("Bug... ");
// 		    System.out.println("n1 " + node1 + " -> " + f);
// 		    continue;
// 		}
// 		// nodes2 stands for all the nodes that could play
// 		// the role of n2 from the inference rule
// // 		Set nodes2 = pig_caller.G.O.pointedNodes(node1,f);
// 		Set nodes2 = O.pointedNodes(node1,f);
// 		if(nodes2.isEmpty()) continue;
		

// 		// nodes4 stands for all the nodes that could play
// 		// the role of n4 from the inference rule
// 		Set nodes4 = pig_callee.G.I.pointedNodes(nodes3,f);
// 		nodes4.addAll(pig_callee.G.O.pointedNodes(nodes3,f));

// 		if(nodes4.isEmpty()) continue;

// 		// set up the relation from any node from nodes2
// 		// to any node from nodes4
// 		for(Iterator it2 = nodes2.iterator(); it2.hasNext(); ) {
// 		    PANode node2 = (PANode)it2.next();

// 		    // The edges must possibly have been created after
// 		    // the call site to be considered
// 		    if (! ((node1_f.getValues(node2)).contains(hole))) 
// 			continue;

// 		    boolean changed = false;

// 		    for(Iterator it4 = nodes4.iterator(); it4.hasNext(); ) {
// 			PANode node4 = (PANode)it4.next();
// 			if(mu.add(node2,node4)){
// 			    changed = true;
// 			    new_info.add(node2,node4);
// 			}
// 		    }
// 		    //tbu
// // 		    if(mu.add(node2,node2)){
// // 			changed = true;
// // 			new_info.add(node2,node2);
// // 		    }

// 		    // nodes with new info are put in the worklist
// 		    if(changed) W.add(node2);
// 		}
// 	    }
// 	}
//     }


    private static void match_edges(Relation mu,
				    Relation first_mapping,
				    PAEdgeSet O,
				    ODParIntGraph pig_callee,
				    ODParIntGraph pig_caller,
				    MethodHole hole){

	boolean precise = pig_caller.odi.precise;

	final PAWorkList W = new PAWorkList();
	// here is the new stuff; only nodes with new stuff are
	// put in the worklist W.
	Relation new_info    = (Relation) mu.clone();

	// We store all the nodes that we know must be mapped (formal
	// return and exception nodes)
	W.addAll(mu.keys());
	Set formal_nodes = mu.keys();

	// In order to know which nodes in the caller are mapped to by
	// nodes in the callee, we ``reverse'' the first mapping.
	final Relation ancestor = new LightRelation();
	for(Iterator key_it=first_mapping.keys().iterator(); key_it.hasNext(); ){
	    PANode source = (PANode) key_it.next();
	    for(Iterator sinks_it=first_mapping.getValues(source).iterator();
		sinks_it.hasNext(); )
		ancestor.add(sinks_it.next(),source);
	}

	// We look for outside edges in the caller, which may happen
	// *after* the skipped call site, and whose tail is mapped
	// unto by one node in the callee.
	if(precise){
	    for(Iterator n_it=pig_caller.odi.outMaybe.edges.keySet().iterator(); 
		n_it.hasNext(); ){
		boolean not_included = true; 

		PANode tail = (PANode) n_it.next();
		// If one of the callee nodes is mapped to this node
		// we process it, else we are not going any further.
		if (!(ancestor.containsKey(tail))) continue;
		
		Map from_tail = (Map) pig_caller.odi.outMaybe.edges.get(tail);
		for(Iterator k_it = from_tail.keySet().iterator(); 
		    k_it.hasNext() && not_included; )
		    {
			String f = (String) k_it.next();
			Relation from_f = (Relation) from_tail.get(f);
			for(Iterator t_it = from_f.keys().iterator(); 
			    t_it.hasNext() && not_included; )
			    {
				PANode head = (PANode) t_it.next();
				if (from_f.getValues(head).contains(hole)){
				    W.add(tail);
				    not_included=false;
				}
			    }
		    }
	    }
	}
	else{
	    PAEdgeVisitor visitor_O = new PAEdgeVisitor(){
		    public void visit(Temp var, PANode node){
			assert false : (" var2node edge in O: " + 
				    var + "->" + node);
		    }
		    public void visit(PANode node1,String f, PANode node2){
			if(ancestor.containsKey(node1))
			    W.add(node1);
		    }
		};
	    
	    pig_caller.G.O.forAllEdges(visitor_O);
	}

	
	Map from_node1 =null;
	Relation node1_f = null;

	while(!W.isEmpty()){
	    PANode node1 = (PANode) W.remove();

	    // nodes3 stands for all the new instances of n3
	    // from the inference rule
	    HashSet nodes3 = new HashSet(new_info.getValues(node1));
	    new_info.removeKey(node1);
	    
	    if(precise){
		from_node1 = (Map) pig_caller.odi.outMaybe.edges.get(node1);
		if (from_node1==null){
		    continue;
		}
	    }

	    Iterator itf = O.allFlagsForNode(node1).iterator();
	    while(itf.hasNext()) {
		String f = (String) itf.next();

		if(precise){
		    node1_f = (Relation) from_node1.get(f); 
		    if (node1_f==null){
			// In fact, this happens if the outside edge was
			// created during the first part of the
			// mapping. In that case, this edge is not a
			// candidate.
			System.err.println("Bug... ");
			System.out.println("Bug... ");
			System.out.println("n1 " + node1 + " -> " + f);
			continue;
		    }
		}
		// nodes2 stands for all the nodes that could play
		// the role of n2 from the inference rule
		Set nodes2 = O.pointedNodes(node1,f);
		if(nodes2.isEmpty()) continue;
		

		// nodes4 stands for all the nodes that could play
		// the role of n4 from the inference rule
		Set nodes4 = pig_callee.G.I.pointedNodes(nodes3,f);
		nodes4.addAll(pig_callee.G.O.pointedNodes(nodes3,f));

		if(nodes4.isEmpty()) continue;

		// set up the relation from any node from nodes2
		// to any node from nodes4
		for(Iterator it2 = nodes2.iterator(); it2.hasNext(); ) {
		    PANode node2 = (PANode)it2.next();

		    // The edges must possibly have been created after
		    // the call site to be considered
		    if ((precise)&& (! ((node1_f.getValues(node2)).contains(hole))))
			continue;

		    boolean changed = false;

		    for(Iterator it4 = nodes4.iterator(); it4.hasNext(); ) {
			PANode node4 = (PANode)it4.next();
			if(mu.add(node2,node4)){
			    changed = true;
			    new_info.add(node2,node4);
			}
		    }
		    //tbu
// 		    if(mu.add(node2,node2)){
// 			changed = true;
// 			new_info.add(node2,node2);
// 		    }

		    // nodes with new info are put in the worklist
		    if(changed) W.add(node2);
		}
	    }
	}
    }


    private static void match_edges(Relation mu,
				    PAEdgeSet candidate,
				    PAEdgeSet reference){

	PAWorkList W = new PAWorkList();
	// here is the new stuff; only nodes with new stuff are
	// put in the worklist W.
	Relation new_info = (Relation) mu.clone();
	
	W.addAll(mu.keys());
	while(!W.isEmpty()){
	    PANode node1 = (PANode) W.remove();
	    
	    // nodes3 stands for all the new instances of n3
	    // from the inference rule
	    HashSet nodes3 = new HashSet(new_info.getValues(node1));
	    new_info.removeKey(node1);

	    Iterator itf = candidate.allFlagsForNode(node1).iterator();
	    while(itf.hasNext()) {
		String f = (String) itf.next();

		// nodes2 stands for all the nodes that could play
		// the role of n2 from the inference rule
		Set nodes2 = candidate.pointedNodes(node1,f);
		if(nodes2.isEmpty()) continue;

		// nodes4 stands for all the nodes that could play
		// the role of n4 from the inference rule
		Set nodes4 = reference.pointedNodes(nodes3,f);
		if(nodes4.isEmpty()) continue;

		// set up the relation from any node from nodes2
		// to any node from nodes4
		for(Iterator it2 = nodes2.iterator(); it2.hasNext(); ) {
		    PANode node2 = (PANode)it2.next();
		    boolean changed = false;
		    for(Iterator it4 = nodes4.iterator(); it4.hasNext(); ) {
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
    // escape info, the empty load nodes will be removed (together with
    // the related information)
    private static void compute_the_final_mapping(final Relation mu,
						  final ODParIntGraph pig_caller,
						  final ODParIntGraph pig_callee){
	pig_callee.forAllNodes(new PANodeVisitor(){
		public void visit(PANode node){
		    if(node.type() != PANode.PARAM)
			mu.add(node,node);
		}
	    });
    }

    private static void compute_the_final_mapping(final Relation mu,
						  final ODParIntGraph pig_caller) {
	pig_caller.forAllNodes(new PANodeVisitor(){
	    public void visit(PANode node){
		mu.add(node,node);
	    }
	});
    }


    /** Sets the edges for the result or the exception returned by the callee.
     * Remember the syntax of a method invocation: 
     * <code>&lt;retval,retexc&gt; = CALL (...)</code>. */
    private static void set_edges_res_ex(Temp l, Relation mu,
					 ODParIntGraph pig_caller, Set nodes){
	if(l == null) return;
	/// System.out.println("Setting the edges for " + l);
	pig_caller.G.I.removeEdges(l);
	Set mu_nodes = new HashSet();
	Iterator it = nodes.iterator();
	while(it.hasNext())
	    mu_nodes.addAll(mu.getValues((PANode) it.next()));
	pig_caller.G.I.addEdges(l,mu_nodes);
    }

    private static void set_edges_res_ex_soft(Temp l, Relation mu,
					 ODParIntGraph pig_caller, Set nodes){
	
	// Update of the caller Return and Exception nodes
	Set mu_ret_nodes = projection(pig_caller.G.r, mu, new LinearSet());
// 	System.out.println("pig_caller.G.r");
// 	print_nodeset(pig_caller.G.r);
// 	System.out.println("mu_ret_nodes");
// 	print_nodeset(mu_ret_nodes);
	pig_caller.G.r = mu_ret_nodes;

	Set mu_exc_nodes = projection(pig_caller.G.excp, mu, new LinearSet());
// 	System.out.println("pig_caller.G.excp");
// 	print_nodeset(pig_caller.G.excp);
// 	System.out.println("mu_exc_nodes");
// 	print_nodeset(mu_exc_nodes);
	pig_caller.G.excp = mu_exc_nodes;

	if(l == null) return;
	/// System.out.println("Setting the edges for " + l);
	Set mu_nodes = new HashSet();
	Iterator it = nodes.iterator();
	while(it.hasNext())
	    mu_nodes.addAll(mu.getValues((PANode) it.next()));
	pig_caller.G.I.addEdges(l,mu_nodes);
    }

    private static void set_edges_res_ex(Temp l, Relation mu,
					 final ODParIntGraph pig_caller, final Set nodes,
					 final PANode org){
	System.out.println("In set_edges_res_ex (" + l + ")");
	if(l == null) return;
	/// System.out.println("Setting the edges for " + l);
	final Set mu_nodes = new HashSet();
	Iterator it = nodes.iterator();
	while(it.hasNext())
	    mu_nodes.addAll(mu.getValues((PANode) it.next()));
	pig_caller.G.I.addEdges(l,mu_nodes);

	System.out.print("   mu_nodes: " + org + " ->");
	it = mu_nodes.iterator();
	while(it.hasNext())
	    System.out.print(" " + (PANode) it.next());
	System.out.println(" ("+ mu_nodes.size()  +").");

	System.out.print("   nodes:    " + org + " ->");
	it = nodes.iterator();
	while(it.hasNext())
	    System.out.print(" " + (PANode) it.next());
	System.out.println(" ("+ nodes.size()  +").");

	/// Memory Leak : copy !
	if (org!=null){
	    PAEdgeSet copy = (PAEdgeSet) pig_caller.G.I.clone(); 
	    PAEdgeVisitor myvisitor = new PAEdgeVisitor(){
		public void visit(Temp tt, PANode node){
		    System.out.println(node); 
		    if(node.equals(org))
			pig_caller.G.I.addEdges(tt, nodes);
		    //			pig_caller.G.I.addEdges(tt, mu_nodes);
		}
		public void visit(PANode node1, String f, PANode node2){
		    System.out.println(node2); 
		    if(node2==org)
			pig_caller.G.I.addEdges(node1, f, nodes);
		    //			pig_caller.G.I.addEdges(node1, f, mu_nodes);
		}
	    };
	    copy.forAllEdges(myvisitor);
	}
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

	// Step 2. Translate the "parallel action" items of
	// information from the callee, by applying the mu function on
	// their components (except for the n2 component of a load
	// action which is left unchanged if it's still present in the
	// new graph).
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


    private static void update_actions(final ActionRepository org_ar,
				       final ActionRepository new_ar,
				       final Relation mu){
	// Add this "common-sense" rule to the mapping: the inter-procedural
	// analysis stays in the same thread.
	mu.add(ActionRepository.THIS_THREAD,ActionRepository.THIS_THREAD);

	ActionVisitor act_visitor = new ActionVisitor(){
	    public void visit_ld(PALoad load){
		new_ar.add_ld(mu.getValues(load.n1),
			      load.f,
			      mu.getValues(load.n2),
			      mu.getValues(load.nt),
			      Collections.EMPTY_SET);
	    }
	    public void visit_sync(PASync sync){
		new_ar.add_sync(sync.project(mu),
				Collections.EMPTY_SET);
	    }
	};

	org_ar.forAllActions(act_visitor);

	// Step 2. Translate the "parallel action" items of
	// information from the callee, by applying the mu function on
	// their components (except for the n2 component of a load
	// action which is left unchanged if it's still present in the
	// new graph).
	ParActionVisitor par_act_visitor = new ParActionVisitor(){
	    public void visit_par_ld(PALoad load, PANode nt2){
		new_ar.add_ld(mu.getValues(load.n1),
			      load.f,
			      mu.getValues(load.n2),
			      mu.getValues(load.nt),
			      mu.getValues(nt2));
		}
	    public void visit_par_sync(PASync sync, PANode nt2){
		new_ar.add_sync(sync.project(mu),
				mu.getValues(nt2));
	    }
	};

	org_ar.forAllParActions(par_act_visitor);
    }

    private static void actions_add_ld(ODInformation callee_odi,
				       final ODParIntGraph pig_caller,
				       final ActionRepository ar,
				       final Relation mapping,
				       MethodHole hole,
				       final Set callee_threads
				       )
    {
	if (callee_odi.precise){
	    for(Iterator in_n=callee_odi.outMaybe.edges.keySet().iterator(); in_n.hasNext(); ){
		PANode n1 = (PANode) in_n.next();
		Map n1_map = (Map) callee_odi.outMaybe.edges.get(n1);
		for(Iterator it_f=n1_map.keySet().iterator(); it_f.hasNext(); ){
		    String f = (String) it_f.next(); 
		    Relation n1_f = (Relation) n1_map.get(f);
		    for(Iterator out_n=n1_f.keys().iterator(); out_n.hasNext(); ){
			PANode n2 = (PANode) out_n.next();
			if (n1_f.getValues(n2).contains(hole))
			    ar.add_ld(mapping.getValues(n1), 
				      f, 
				      mapping.getValues(n2), 
				      ActionRepository.THIS_THREAD,
				      callee_threads);
		    }
		}
	    }
	}
	else{
	    PAEdgeVisitor visitor_O = new PAEdgeVisitor(){
		    public void visit(Temp var, PANode node){
			assert false : (" var2node edge in O: " + 
				    var + "->" + node);
		    }
		    public void visit(PANode node1,String f, PANode node2){
			ar.add_ld(mapping.getValues(node1), 
				  f, 
				  mapping.getValues(node2), 
				  ActionRepository.THIS_THREAD,
				  callee_threads);
		    }
		};
	    pig_caller.G.O.forAllEdges(visitor_O);
	}
    }

    private static void actions_add_sync(ActionRepository ar,
					 Set par_locks,
					 Set callee_threads
					 )
    {
	for(Iterator it=par_locks.iterator();it.hasNext(); )
	    ar.add_sync((PASync) it.next(), callee_threads);
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

		    Set ei_n1_set = mu.getValues(ei.n1);
		    if(ei_n1_set.isEmpty()) return;
		    Set ei_n2_set = mu.getValues(ei.n2);
		    if(ei_n2_set.isEmpty()) return;
		    Set eo_n1_set = mu.getValues(eo.n1);
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
		    eo_caller.add(mu.getValues(n1),f,n2,callerI);
		}
	    });
    }


    // Treats some native methods in a special way. Returns a pair of
    // ODParIntGraphs or null if it cannot handle this call.
    private static ODParIntGraphPair treatVerySpecialNatives
	(ODPointerAnalysis pa, CALL q, ODParIntGraph pig_before){

	HMethod hm = q.method();
	if(hm == null) return null;
	ODParIntGraphPair pair = null;

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
	for(Iterator it = types.iterator();it.hasNext(); ){
	    HClass hclass = (HClass) it.next();
	    if(hclass.isArray())
		System.out.println("CLONE: might be called for an array");
	    HField[] hfields = hclass.getFields();
	    for(int i = 0; i < hfields.length; i++)
		if(!hfields[i].getType().isPrimitive())
		    retval.add(hfields[i].getName());
	}
	return retval;
    }
 

    // Treat the sequence "ld t = n_src.f; store n_R.f = t;". For a clone()
    // call, this is done for all fields f of the cloned object.
    private static void aux_clone_treat_pseudo_ldst(CALL q, String f,
       PANode n_R, PANode n_src, ODParIntGraph pig, NodeRepository node_rep){

	// f_set contains all the nodes pointed to by <n_src, f>
	Set f_set = new HashSet(pig.G.I.pointedNodes(n_src, f));

	if(pig.G.escaped(n_src)){
	    PANode n_L = node_rep.getLoadNodeSpecial(q, f);
	    pig.G.O.addEdge(n_src, f, n_L);
	    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
		pig.odi.addOutsideEdges(n_src, f, n_L);
	    }

	    pig.ar.add_ld(n_src, f, n_L, ActionRepository.THIS_THREAD,
			  pig.tau.activeThreadSet());
	    // TODO: edge ordering relation (if we want to maintain it)
	    f_set.add(n_L);
	}

	for(Iterator it = f_set.iterator(); it.hasNext();){
	    PANode n = (PANode) it.next();
	    pig.G.I.addEdge(n_R, f, n);
	    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
		pig.odi.addInsideEdges(n_R, f, n);
	    }
	}
    }


    // Specially treats 
    //    "protected native java.lang.Object java.lang.Object.clone()"
    private static ODParIntGraphPair treat_clone(ODPointerAnalysis pa, CALL q,
					       ODParIntGraph pig_before){
	HMethod callee = q.method();
	if(!callee.getName().equals("clone") ||
	   !callee.getDeclaringClass().getName().equals("java.lang.Object"))
	    return null;

	//if(DEBUG)
	    System.out.println("NATIVE (special): " + callee);

	ODParIntGraph pig_after0 = pig_before;
	ODParIntGraph pig_after1 = (ODParIntGraph) pig_before.clone();

	// do the actions of the "clone()" method: create a new object (n_R),
	// copy the fields from the objects passed as params to clone to n_R
	NodeRepository node_rep = pa.getNodeRepository(); 
	PANode n_R = node_rep.getCodeNode(q, PANode.RETURN);

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
	    for(Iterator it_src = srcs.iterator(); it_src.hasNext();){
		PANode n_src = (PANode) it_src.next();
		aux_clone_treat_pseudo_ldst(q, f, n_R, n_src, pig_after0,
					    node_rep);
	    }
	}

	// set the link l_R to the RETURN node
	Temp l_R = q.retval();
	if(l_R != null)
	    pig_after0.G.I.addEdge(l_R, n_R);

	aux_native_treat_excp(q, pig_after1, node_rep);

	return new ODParIntGraphPair(pig_after0, pig_after1);
    }

    
    // Aux method for all the special treatments of native methods. If the
    // called native method can return an exception, and there is a Temp l_E
    // to receive (a pointer to) it, a PANode.EXCEPT node associated with this
    // CALL is created (if it doesn't exist yet) and the proper link is set
    // from l_E to n_E, in the parallel interaction graph "pig".
    private static void aux_native_treat_excp(CALL q, ODParIntGraph pig,
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
    private static ODParIntGraphPair treat_arraycopy(ODPointerAnalysis pa, CALL q,
						   ODParIntGraph pig_before){
	HMethod hm = q.method();
	if(!hm.getName().equals("arraycopy") ||
	   !hm.getDeclaringClass().getName().equals("java.lang.System"))
	    return null;

	//if(DEBUG)
	    System.out.println("NATIVE (special): " + Debug.code2str(q));

	// the conventional field name used for the array's entries
	final String f = ODPointerAnalysis.ARRAY_CONTENT;

	Temp l_src = q.params(0);
	Temp l_dst = q.params(2);

	Set dst_set = pig_before.G.I.pointedNodes(l_dst);

	Set src_set = pig_before.G.I.pointedNodes(l_src);
	Set set_S = 
	    pig_before.G.I.pointedNodes(src_set, f);

	Set set_E = new HashSet();
	for(Iterator it = src_set.iterator(); it.hasNext(); ){
	    PANode node = (PANode) it.next();
	    if(pig_before.G.e.hasEscaped(node))
		set_E.add(node);
	}

	NodeRepository node_rep = pa.getNodeRepository();

	if(set_E.isEmpty()){
	    pig_before.G.I.addEdges(dst_set, f, set_S);
	    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
		pig_before.odi.addInsideEdges(dst_set, f, set_S);
	    }
	}
	else{
	    PANode load_node = node_rep.getCodeNode(q, PANode.LOAD);

	    pig_before.G.O.addEdges(set_E, f, load_node);
	    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
		pig_before.odi.addOutsideEdges(set_E, f, load_node);
	    }

	    if(!ODPointerAnalysis.IGNORE_EO)
		pig_before.eo.add(set_E, f, load_node, pig_before.G.I);

	    set_S.add(load_node);
	    pig_before.G.I.addEdges(dst_set, f, set_S);
	    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
		pig_before.odi.addInsideEdges(dst_set, f, set_S);
	    }
	    pig_before.G.propagate(set_E);

	    // update the action repository
	    Set active_threads = pig_before.tau.activeThreadSet();
	    for(Iterator it_E = set_E.iterator(); it_E.hasNext(); )
		pig_before.ar.add_ld((PANode) it_E.next(), f, load_node,
				     ActionRepository.THIS_THREAD,
				     active_threads);
	}

	ODParIntGraph pig_after1 = (ODParIntGraph) pig_before.clone();
	// Set the edges for the exception node in graph 1.
	Temp l_E = q.retex();
	if(l_E != null){
	    pig_after1.G.I.removeEdges(l_E);
	    PANode n_E = node_rep.getCodeNode(q, PANode.EXCEPT);
	    pig_after1.G.I.addEdge(l_E, n_E);
	    pig_after1.G.e.addMethodHole(n_E, hm);
	}
	return new ODParIntGraphPair(pig_before, pig_after1);	
    }


    // Many native methods don't do any synchronizations on their object
    // parameters, don't store them in static fields and don't modify the
    // points-to graph accessible from these object parameters.
    private static boolean isUnharmful(HMethod hm) {
	return uhms.contains(hm); 
    }
    // Checks whether a method is totally harmful (i.e. all the parameters
    // must be marked as escaping into it). A CALL to such a method cannot
    // be treated otherwise than by skipping it.
    private static boolean isTotallyHarmful(HMethod hm) {
	return !isUnharmful(hm); // for the moment, conservative treatment
    }
    private static Set uhms = new HashSet();
    static{
	String[][] methods = {
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

	    {"java.lang.Object", "hashCode"}
	};

	for(int i = 0; i < methods.length; i++)
	    uhms.addAll(getMethods(methods[i][0], methods[i][1]));
    }

    // Returns all the methods having the name m_name
    // that are declared in class c_name.
    private static Collection getMethods(String c_name, String m_name) {
	List retval = new LinkedList();
	HClass hclass = Loader.systemLinker.forName(c_name);
	HMethod[] hms = hclass.getDeclaredMethods();
	for(int i = 0; i < hms.length; i++)
	    if(m_name.equals(hms[i].getName()))
		retval.add(hms[i]);
	return retval;
    }


    // After a method hole has been analyzed for all possible callees,
    // the formal return and exception nodes are removed for the pig.

    public static void clean_call(ODParIntGraphPair pp, MethodHole hole){
	clean_call(pp.pig[0],hole);
	clean_call(pp.pig[1],hole);
    }

    public static void clean_call(ODParIntGraph pig, MethodHole hole){
	HashSet nodes = new HashSet();
	if (hole.ret()!=null) nodes.add(hole.ret());
	if (hole.exc()!=null) nodes.add(hole.exc());
	if (!nodes.isEmpty())
	    pig.remove(nodes);
//  	System.out.println("   Pig after cleaning" + pig);
    }

    public static void print_relation(Relation mu){
	Set keys = mu.keys();
	for(Iterator k_it = keys.iterator(); k_it.hasNext();){
	    PANode source = (PANode) k_it.next();
	    System.out.print("    " + source + " ->");
	    Set sinks = mu.getValues(source);
	    for(Iterator s_it = sinks.iterator(); s_it.hasNext();){
		PANode sink = (PANode) s_it.next();
		System.out.print(" " + sink);
	    }
	    System.out.println(".");
	}
    }

    public static Set projection(Set nodes, Relation mu, Set mu_nodes){
	Iterator it = nodes.iterator();
	while(it.hasNext())
	    mu_nodes.addAll(mu.getValues(it.next()));

	return mu_nodes;
    }

    public static void print_nodeset(Set nodes){
	System.out.print("  ");
	Iterator it = nodes.iterator();
	while(it.hasNext())
	    System.out.print(" " + ((PANode) it.next()));
	System.out.println();
    }

    private static Relation projection(Relation original, Relation projecting){
	Relation mapping = new LightRelation();

	for(Iterator org_it = original.keys().iterator(); org_it.hasNext(); ){
	    PANode n_org = (PANode) org_it.next();
	    for(Iterator inter_it = original.getValues(n_org).iterator();
		inter_it.hasNext(); )
		mapping.addAll(n_org,projecting.getValues(inter_it.next()));
	}

	return mapping;
    }


    public static LightRelation create_extended_mapping(ODParIntGraph pig,
							final Relation mapping,
							Set toberemoved)
    {
	final LightRelation mapping_extended = (LightRelation) mapping.clone();

	pig.forAllNodes(new PANodeVisitor(){
		public void visit(PANode node){
		    mapping_extended.add(node,node);
		}
	    });

	for(Iterator n_it=toberemoved.iterator(); n_it.hasNext(); ) {
	    PANode n = (PANode) n_it.next();
	    mapping_extended.remove(n,n);
	}

	return mapping_extended;
    }

}// end of the class
