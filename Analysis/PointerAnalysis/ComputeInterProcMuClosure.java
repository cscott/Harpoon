// ComputeMuClosure.java, created Tue Oct  8 11:11:44 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.IR.Quads.CALL;
import harpoon.Temp.Temp;
import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;
import harpoon.Util.Util;

import harpoon.Analysis.MetaMethods.GenType;

import java.util.Set;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;

/**
   <code>ComputeMuClosure</code> is a [functional-programming style]
   closure for the <code>computeInterProcMu method</code>.  See the
   comments around that method for more info.
 
   @author  Alexandru Salcianu <salcianu@MIT.EDU>
   @version $Id: ComputeInterProcMuClosure.java,v 1.5 2003-10-28 16:05:54 salcianu Exp $ */
public class ComputeInterProcMuClosure {

    static boolean DEBUG = false;

    /** Compute the node mappings for the inter-procedural analysis,
	according to the method outlined in Alex Salcianu's SM thesis
	(Figure 2-8, page 39).

       @param call analyzed call site
       @param pig_caller graph for the point right before the call
       @param pig_callee summary graph for the end of the callee method
       @param callee_params PARAM nodes for the callee

       @return node mapping relation */
    public static Relation computeInterProcMu
	(CALL call, ParIntGraph pig_caller, ParIntGraph pig_callee,
	 PANode[] callee_params, PointerAnalysis pa) {

	if(DEBUG) {
	    System.out.println
		("computeInterProcMu:\n" + 
		 "call = " + Util.code2str(call) + "\n" +
		 /*
		 "pig_caller = " + pig_caller + "\n" + 
		 "pig_callee = " + pig_callee + "\n" +
		 */
		 "callee_params = {");
	    for(int i = 0; i < callee_params.length; i++)
		System.out.println(" " + callee_params[i]);
	    System.out.println("}\n");
	}

	ComputeInterProcMuClosure cmc = new ComputeInterProcMuClosure
	    (call, pig_caller, pig_callee, callee_params, pa);
	cmc.compute_mu();

	return cmc.mu;
    }


    // no outside entity can create this object
    private ComputeInterProcMuClosure() {
	// this will never be executed; it's here only because
	// otherwise the compiler complains that the final vars are
	// not initialized.
	this(null, null, null, null, null);
    }

    private ComputeInterProcMuClosure
	(CALL call, ParIntGraph pig_caller, ParIntGraph pig_callee,
	 PANode[] callee_params, PointerAnalysis pa) {
	this.call          = call;
	this.pig_caller    = pig_caller;
	this.pig_callee    = pig_callee;
	this.callee_params = callee_params;
	this.pa            = pa;
    }

    // closure fields
    private final CALL call;
    private final ParIntGraph pig_caller;
    private final ParIntGraph pig_callee;
    private final PANode[] callee_params;
    private final Relation mu = new RelationImpl();
    private final PointerAnalysis pa;

    // top-level driver for the node mapping generation
    private void compute_mu() {
	// 2.5 and 2.6
	initialize_mu();
	
	if(DEBUG) 
	    System.out.println("Initial mu: " + mu);
	
	// 2.7 and 2.8
	extend_mu();

	if(DEBUG)
	    System.out.println("Middle mu: " + mu);
	
	// extend mu to obtain mu'
	compute_final_mu();

	/*
	if(DEBUG)
	    System.out.println("Final mu: " + mu);
	*/
    }
    

    // Compute a node mapping initialized by 2.5 and 2.6
    private void initialize_mu() {
	// 2.5: map parameters nodes to the actual arguments
	map_parameters();

	if(!PointerAnalysis.TOPLAS_PAPER) {
	    // 2.6: reflexive mapping of static nodes from callee
	    pig_callee.forAllNodes(new PANodeVisitor() {
		public void visit(PANode node) {
		    if((node.type == PANode.STATIC) ||
		       // LOST nodes may represent STATIC nodes
		       (PointerAnalysis.COMPRESS_LOST_NODES &&
			(node.type == PANode.LOST)))
			mu.add(node, node);
		}
	    });
	}
    }

    // Update the mapping mu to contain the mappings for the parameter nodes
    private void map_parameters() {
	Temp[] args = call.params();
	int object_params_count = 0;
	// map the object formal parameter nodes to the actual arguments
	for(int i = 0; i < args.length; i++)
	    if(!call.paramType(i).isPrimitive()) {
		// null only for some native methods
		if(callee_params != null)
		    addMappingAll(mu,
				  callee_params[object_params_count],
				  pig_caller.G.I.pointedNodes(args[i]));
		object_params_count++;
	    }
	
	assert (callee_params == null) ||
	    (object_params_count == callee_params.length) :
	    "\tDifferent numbers of object formals (" + 
	    callee_params.length + ") and object arguments (" +
	    object_params_count + ") for \n\t" + Util.code2str(call);
    }


    // Initially, all the nodes from the callee are put into the
    // caller's graph except for the PARAM nodes. Later, after
    // recomputing the escape info, the empty load nodes will be
    // removed.  Thesis equivalent: part 2 of the mapping function
    // (Figure 2-8), mu is extended to produce mu'
    private void compute_final_mu() {
	pig_callee.forAllNodes(new PANodeVisitor() {
	    public void visit(PANode node) {
		if(node.type != PANode.PARAM)
		    mu.add(node, node);
	    }
	});
    }


    
    // iteratively apply 2.7 and 2.8 to compute l.f.p. of 2.5-2.8
    private void extend_mu() {
	// worklist containing nodes with new mappings
	W = new PAWorkList();
	// relation containing the new mappings for nodes from the worklist
	new_mu = (Relation) mu.clone();
	rev_mu = mu.revert(new RelationImpl());

	W.addAll(mu.keys());
	while(!W.isEmpty()) {
	    PANode node = (PANode) W.remove();
	    // apply all newly enabled instances of 2.7
	    match_callee_caller(node);
	    // apply all newly enabled instances of 2.8
	    match_callee_callee(node);
	}

	W = null;
	new_mu = null;
    }
    // Yeah, I know these should be packaged with extend_mu (and
    // posibly other methods) into a separate closure!  I'm lazy ...
    private PAWorkList W;
    private Relation new_mu;
    private Relation rev_mu;


    // add the (potentially new) mapping source -> target; return
    // true if this is indeed a new mapping
    private boolean add_mapping_aux(PANode source, PANode target) {
	if(addMapping(mu, source, target)) {
	    NEWINFO = true;
	    if(DEBUG)
		System.out.println("new mapping: " + source + " -> " + target);
	    new_mu.add(source, target);
	    rev_mu.add(target, source);
	    return true;
	}
	return false;
    }

    // add one mapping; if this adds new info, source is put into W
    private void add_mapping(PANode source, PANode target) {
	if(add_mapping_aux(source, target))
	    W.add(source);
    }

    // add several mappings; if any new info, source is put into W
    private void add_mappings(PANode source, Set targets) {
	if(targets.isEmpty()) return;
	boolean changed = false;
	for(Iterator it = targets.iterator(); it.hasNext(); ) {
	    PANode target = (PANode) it.next();
	    if(add_mapping_aux(source, target))
		changed = true;
	}
	if(changed)
	    W.add(source);
    }


    // try to apply constraint 2.7 for node1
    private void match_callee_caller(PANode node1) {
	for(Iterator itf = pig_callee.G.O.allFlagsForNode(node1).iterator();
	    itf.hasNext(); )
	    match_callee_caller(node1, (String) itf.next());
    }

    // apply 2.7 for the field f
    private void match_callee_caller(PANode node1, String f) {
	Set node2s = pig_callee.G.O.pointedNodes(node1, f);
	if(node2s.isEmpty()) return;
	Set node4s = pig_caller.G.I.pointedNodes(new_mu.getValues(node1), f);
	if(node4s.isEmpty()) return;

	NEWINFO = false;

	for(Iterator it2 = node2s.iterator(); it2.hasNext(); ) {
	    PANode node2 = (PANode) it2.next();
	    add_mappings(node2, node4s);
	}

	if(NEWINFO && DEBUG)
	    System.out.println("2.7 for node1=" + node1 + ", f=" + f + "\n");
    }

    // try to apply constraint 2.8 for node1/node3 = node
    private void match_callee_callee(PANode node) {
	Set nodeps = new HashSet();
	// We explore the set of possible elements in the set intersection
	// from the formal rule 2.8; it is safe to look only at the
	// the new mappings (plus node which is new in 1st iteration)
	for(Iterator it = new_mu.getValues(node).iterator(); it.hasNext(); ) {
	    PANode common = (PANode) it.next();
	    callee_callee_grab_nodep(node, common, nodeps);
	}
	callee_callee_grab_nodep(node, node, nodeps);

	for(Iterator itp = nodeps.iterator(); itp.hasNext(); ) {
	    PANode node_prime = (PANode) itp.next();
	    match_callee_callee(node, node_prime);
	    match_callee_callee(node_prime, node);
	}
    }

    // try to apply 2.8 where the common element of the intersection is
    private void callee_callee_grab_nodep
	(PANode node, PANode common, Set nodeps) {
	// this corresponds to the "\ {n_null}" from the formal rule
	if(common.type == PANode.NULL)
	    return;
	nodeps.addAll(rev_mu.getValues(common));
    }


    // "apply" all possible instantiations of 2.8 for node1 and node3
    private void match_callee_callee(PANode node1, PANode node3) {
	if( ! ( (node1 != node3) || 
		((node1.type == PANode.LOAD) ||
		 (node1.type == PANode.LOST)) ) )
	    return;

	for(Iterator itf = pig_callee.G.O.allFlagsForNode(node1).iterator();
	    itf.hasNext(); ) {
	    String f = (String) itf.next();
	    Set node2s = pig_callee.G.O.pointedNodes(node1, f);
	    Set node4s = pig_callee.G.I.pointedNodes(node3, f);
	    if(node4s.isEmpty()) continue;

	    for(Iterator it2 = node2s.iterator(); it2.hasNext(); ) {
		PANode node2 = (PANode) it2.next();
		for(Iterator it4 = node4s.iterator(); it4.hasNext(); ) {

		    NEWINFO = false;

		    PANode node4 = (PANode) it4.next();
		    if(node4.type != PANode.PARAM)
			add_mapping(node2, node4);
		    
		    if(NEWINFO && DEBUG)
			System.out.println
			    ("2.8a for node1=" + node1 + ", node2=" + node2 + 
			     ", f=" + f +
			     ", node3=" + node3 + ", node4=" + node4 + "\n");
		    NEWINFO = false;

		    add_mappings(node2, mu.getValues(node4));

		    if(NEWINFO && DEBUG)
			System.out.println
			    ("2.8b for node1=" + node1 + ", node2=" + node2 + 
			     ", f=" + f +
			     ", node3=" + node3 + ", node4=" + node4 + "\n");
		}
	    }
	}
    }
    private boolean NEWINFO = false;



    private static boolean USE_TYPE_INFO = true;
    static {
	if(USE_TYPE_INFO)
	    System.out.println("USE TYPE INFO IN INTERPROC MAPPING");
    }
    
    private boolean addMappingAll(Relation mu, PANode node, Set images) {
	if(!USE_TYPE_INFO)
	    return mu.addAll(node, images);
	boolean changed = false;
	for(Iterator it = images.iterator(); it.hasNext(); ) {
	    if(addMapping(mu, node, (PANode) it.next()))
		changed = true;
	}
	return changed;
    }


    private boolean addMapping(Relation mu, PANode node, PANode image) {
	if(!USE_TYPE_INFO)
	    return mu.add(node, image);
	if(mu.contains(node, image)) return false;
	if(compatible(node, image)) 
	    return mu.add(node, image);
	else
	    return false;
    }


    private static class CompatibleQuery {
	PANode node1;
	PANode node2;
	int hash;
	CompatibleQuery() {} // just to make the compiler happy
	CompatibleQuery(PANode node1, PANode node2) {
	    init(node1, node2);
	}
	private void init(PANode node1, PANode node2) {
	    this.node1 = node1;
	    this.node2 = node2;
	    this.hash = node1.hashCode() ^ node2.hashCode();
	}
	public int hashCode() { return hash; }
	public boolean equals(Object o) {
	    if(this.hashCode() != o.hashCode())
		return false;
	    CompatibleQuery q = (CompatibleQuery) o;
	    return
		this.node1.equals(q.node1) && 
		this.node2.equals(q.node2);
	}
    }
    private boolean compatible(PANode node1, PANode node2) {
	compatibleQuery.init(node1, node2);
	Boolean answer = (Boolean) compatibleCache.get(compatibleQuery);
	if(answer == null) {
	    boolean banswer = _compatible(node1, node2);
	    answer = new Boolean(banswer);
	    compatibleCache.put(new CompatibleQuery(node1, node2), answer);
	    /* // debug code
	    if(!banswer)
		System.out.println("INCOMPAT: " + node1 + " , " + node2);
	    */
	}
	return answer.booleanValue();
    }
    private static Hashtable/*<CompatibleQuery,Boolean>*/ compatibleCache = 
	new Hashtable();
    private CompatibleQuery compatibleQuery = new CompatibleQuery();

    private boolean _compatible(PANode node1, PANode node2) {
	GenType[] gts1 = node1.getPossibleClasses();
	GenType[] gts2 = node2.getPossibleClasses();
	if((gts1 == null) || (gts2 == null))
	    return true;
	else
	    return !disjoint(pa.getAllConcreteClasses(gts1),
			     pa.getAllConcreteClasses(gts2));
    }

    private static boolean disjoint(Set set1, Set set2) {
	if(set1.size() > set2.size()) {
	    Set temp;
	    temp = set1;
	    set1 = set2;
	    set2 = temp;
	}
	for(Iterator it = set1.iterator(); it.hasNext(); )
	    if(set2.contains(it.next()))
		return false;
	return true;
    }
}
