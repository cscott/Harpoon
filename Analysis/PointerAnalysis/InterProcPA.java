// InterProcPA.java, created Tue Jan 18 11:04:20 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;


import harpoon.IR.Quads.CALL;
import harpoon.ClassFile.HMethod;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Temp.Temp;


/**
 * <code>InterProcPA</code> is a &quot;functional&quot; class (i.e. it 
 * contains just some methods, no persistent data) that wraps
 * the inter-procedural part of the pointer analysis. Normally, this should
 * be a part of the <code>PointerAnalysis</code>, but that class is already
 * too big and some code segmentation is always good!
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: InterProcPA.java,v 1.1.2.6 2000-02-09 05:23:42 salcianu Exp $
 */
abstract class InterProcPA {

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
	</ul> */
    public static ParIntGraph analyze_call(HMethod current_method, CALL q,
					   ParIntGraph pig_before,
					   PointerAnalysis pa){
	CallGraph cg = pa.getCallGraph();
	NodeRepository node_rep = pa.getNodeRepository(); 
	HMethod[] hms = cg.calls(current_method,q);
	int nb_callees = hms.length;


	// This test seems to be a bit paranoic but it helped me to find
	// an obscure bug in CallGraph. TRUST NO ONE!
	if(nb_callees < 1){
	    /// System.out.println("Callees: " + hms);
	    /// System.out.println("nb_callees = " + nb_callees);
	    /// System.out.println("Error: CALL site with no callee!");
	    /// System.out.println(current_method);
	    /// System.out.println(q);
	    //System.exit(1);
	    return skip_call(q,pig_before,node_rep);
	}

	ParIntGraph pigs[] = new ParIntGraph[nb_callees];
	for(int i=0;i<nb_callees;i++){
	    pigs[i] = pa.getExtParIntGraph(hms[i]);
	    if(pigs[i] == null){
		// one of the callee doesn't have a // interaction graph
		return skip_call(q,pig_before,node_rep);
	    }
	}

	// special case: only one callee; no ParIntGraph is cloned
	if(nb_callees == 1){
	    /// System.out.println(hms[0]);
	    return mapUp(q,pig_before,pigs[0],pa.getParamNodes(hms[0]));
	}

	// more than one callee: the graph after the CALL is a join of all
	// the graphs obtained by combining, for each callee hms[i], the graph
	// before the CALL with the graph at the end of hms[i]. The
	// implementation is complicated by the need of doing only the
	// clone() (cloning a ParIntGraph is very expensive)

	// compute the first term of the join operation
	ParIntGraph pig_after = 
	    mapUp(q,(ParIntGraph)pig_before.clone(),
		  pa.getExtParIntGraph(hms[0]),
		  pa.getParamNodes(hms[0]));

	// join to it all the others, except the last one
	for(int i=1; i< nb_callees - 1 ; i++)
	    pig_after.join(mapUp(q,(ParIntGraph)pig_before.clone(),
				 pigs[i],
				 pa.getParamNodes(hms[i])));

	// finally, join with the graph modeling the interaction with
	// the last callee
	pig_after.join(mapUp(q,pig_before,
			     pigs[nb_callees-1],
			     pa.getParamNodes(hms[nb_callees-1])));

	return pig_after;
    }


    /** Updates the ParIntGraph when the CALL is skipped */
    private static ParIntGraph skip_call(CALL q, ParIntGraph pig_caller,
					 NodeRepository node_rep){
	// The names of the variables closely match those used in the
	// formal description of the algorithm (section 9.2)
	Temp l_R = q.retval();
	PANode n_R = (l_R!=null)?node_rep.getCodeNode(q,PANode.RETURN):null;
	Temp l_E = q.retex();
	PANode n_E = (l_E!=null)?node_rep.getCodeNode(q,PANode.EXCEPT):null;

	// remove the old edges, add the new ones
	if(l_R != null) pig_caller.G.I.removeEdges(l_R);
	if(l_E != null) pig_caller.G.I.removeEdges(l_E);
	if(l_R != null) pig_caller.G.I.addEdge(l_R,n_R);
	if(l_E != null) pig_caller.G.I.addEdge(l_E,n_E);

	// Construct the set S_M of the objects escaped through this unanalyzed
	// method invocation site.
	Set S_M = new HashSet();
	Temp[] params = q.params();
	for(int i=0;i<params.length;i++)
	    S_M.addAll(pig_caller.G.I.pointedNodes(params[i]));
	Iterator it = S_M.iterator();
	// Update the escape information
	while(it.hasNext())
	    pig_caller.G.e.addMethodHole((PANode)it.next(),q);
	// propagate the new escape information
	pig_caller.G.propagate(S_M);
	return pig_caller;
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
    private static ParIntGraph mapUp(CALL q, 
				     ParIntGraph pig_caller,
				     ParIntGraph pig_callee,
				     PANode[] callee_params){
	// get the initial mapping: formal param -> actual parameter,
	// and class node -> class node
	Relation mu = 
	    get_initial_mapping(q,pig_caller,pig_callee,callee_params);

	// update the node map by matching outside edges from the caller
	// with inside edges from the callee
	match_edges(mu,pig_caller,pig_callee);
	
	// For each node n from the callee's graph, if n is going to 
	// stay, add n to mu(n).
	compute_the_final_mapping(mu,pig_caller,pig_callee);

	// get the set of the insertion points:
	Set roots = new HashSet(mu.keySet());
	roots.addAll(pig_callee.G.r);

	// translate edges from pig_callee to pig_caller
	bring_edges(mu,pig_caller,pig_callee,roots);

	// TODO: (Priority 0) map the thread map too!

	// translate the actions too
	pig_callee.ar.translateTheActions(pig_caller.ar, mu,
					 pig_caller.tau.activeThreadSet());

	// set the edges for the result and for the exception variables
	set_edges_res_ex(q.retval(),mu,pig_caller,pig_callee.G.r);
	set_edges_res_ex(q.retex() ,mu,pig_caller,pig_callee.G.excp);

	return pig_caller;
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
	enum = pig_callee.G.I.allSourceNodes();
	while(enum.hasMoreElements()){
	    PANode node = (PANode) enum.nextElement();
	    if(node.type == PANode.STATIC)
		mu.add(node,node);
	}

	return mu;
    }


    /** Matches outside edges from the graph of (used by ) the callee 
	against inside edges from the graph of (created by) the caller.
	(repeated application of constraint 2) */
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

    /** For each node n from the callee's graph, if n is going to 
     * stay in the new graph, add n to mu(n). */
    private static void compute_the_final_mapping(Relation mu,
						  ParIntGraph pig_caller,
						  ParIntGraph pig_callee){
	// the normally returned nodes MUST be into the new graph!
	Iterator it_ret = pig_callee.G.r.iterator();
	while(it_ret.hasNext()){
	    PANode node = (PANode) it_ret.next();
	    if(!mu.containsKey(node)){
		/// System.out.println("Added to mu: " + node + " -> " + node);
		mu.add(node,node);
	    }
	}

	// the exceptionally returned nodes MUST be into the new graph!
	Iterator it_excp = pig_callee.G.excp.iterator();
	while(it_excp.hasNext()){
	    PANode node = (PANode) it_excp.next();
	    if(!mu.containsKey(node)){
		/// System.out.println("Added to mu: " + node + " -> " + node);
		mu.add(node,node);
	    }
	}

	// the thread nodes MUST be into the new graph!
	Enumeration enum_threads = pig_callee.tau.activeThreads();
	while(enum_threads.hasMoreElements()){
	    PANode node = (PANode) enum_threads.nextElement();
	    if(!mu.containsKey(node)){
		/// System.out.println("Added to mu: " + node + " -> " + node);
		mu.add(node,node);
	    }
	}
	
	Relation new_info = (Relation)mu.clone();
	PAWorkList W = new PAWorkList();
	W.addAll(mu.keySet());
	while(!W.isEmpty()){
	    PANode node1 = (PANode) W.remove();

	    Set new_mappings = new HashSet(new_info.getValuesSet(node1));

	    new_info.removeAll(node1);

	    // taking care of constraint (5)
	    Enumeration enumf = pig_callee.G.I.allFlagsForNode(node1);
	    while(enumf.hasMoreElements()){
		String f = (String) enumf.nextElement();

		Iterator it2 = pig_callee.G.I.pointedNodes(node1,f).iterator();
		while(it2.hasNext()){
		    PANode node2 = (PANode) it2.next();
		    int type = node2.type();
		    // n2 should be an inside or a return/exception node
		    if((type!=PANode.INSIDE) && (type!=PANode.RETURN)
		       && (type!=PANode.EXCEPT))
			continue;
		    if(mu.add(node2,node2)){
			new_info.add(node2,node2);
			W.add(node2);
		    }
		}
	    }
	    
	    // taking care of constraint (6)
	    enumf = pig_callee.G.O.allFlagsForNode(node1);
	    while(enumf.hasMoreElements()){
		String f = (String) enumf.nextElement();
		// we construct in nodesn the set of all the nodes to which
		// node is mapped and which are escaping in the caller's graph
		Set nodesn = new HashSet();
		Iterator itn = new_mappings.iterator();
		while(itn.hasNext()){
		    PANode noden = (PANode)itn.next();
		    if(pig_caller.G.e.hasEscaped(noden))
			nodesn.add(noden);
		}
		if(nodesn.isEmpty()) continue;

		// navigate through the nodes which are pointed by n1 (the
		// possible n2's from the rule (6)
		Iterator it2 = pig_callee.G.O.pointedNodes(node1,f).iterator();
		while(it2.hasNext()){
		    PANode node2 = (PANode) it2.next();
		    if(node2.type != PANode.LOAD) continue;
		    if(mu.add(node2,node2)){
			new_info.add(node2,node2);
			W.add(node2);
		    }
		    pig_caller.G.O.addEdges(nodesn,f,node2);
		}
	    }
	    
	}
    }

    /** Use the node mapping mu to bring edges from the graph of the
     * callee into the graph of the caller */
    private static void bring_edges(Relation mu,
				    ParIntGraph pig_caller,
				    ParIntGraph pig_callee,
				    Set roots){

	PAWorkList W = new PAWorkList();
	W.addAll(roots);
	// the set of already analyzed nodes; they must not be put in the
	// worklist again
	Set analyzed = new HashSet(roots);
	
	while(!W.isEmpty()){
	    PANode node1 = (PANode)W.remove();
	    Enumeration enumf = pig_callee.G.I.allFlagsForNode(node1);
	    Set set_from = mu.getValuesSet(node1);
	    while(enumf.hasMoreElements()){
		String f = (String) enumf.nextElement();
		Iterator it2 = pig_callee.G.I.pointedNodes(node1,f).iterator();
		while(it2.hasNext()){
		    PANode node2 = (PANode)it2.next();
		    if(!analyzed.contains(node2))
			W.add(node2);
		    // apply constraint (7)
		    Set set_to = mu.getValuesSet(node2);
		    pig_caller.G.I.addEdges(set_from,f,set_to);
		}
	    }
	}
    } // end of bring_edges


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

}// end of the class








