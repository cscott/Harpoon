// ParIntGraph.java, created Sun Jan  9 15:40:59 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.LinkedList;


import harpoon.Temp.Temp;
import harpoon.IR.Quads.CALL;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Util.Util;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.RelationEntryVisitor;


/**
 * <code>ParIntGraph</code> models a Parallel Interaction Graph data
 structure. Most of its fields retain the original name from the paper
 of Martin and John Whaley.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: ParIntGraph.java,v 1.5 2002-04-10 03:00:42 cananian Exp $
 */
public class ParIntGraph implements java.io.Serializable {

    public static boolean DEBUG = false;
    public static boolean DEBUG2 = false;

    /** Debug for the aggressive shrinking. */
    public static boolean DEBUG_AS = false;

    /** Display the gains due to AGGRESSIVE_SHRINKING. */
    public static boolean MEASURE_AS = false;

    /** Activates the aggressive shrinking. Buggy for the moment ... */
    public static boolean AGGRESSIVE_SHRINKING = true;

    /** Default (empty) graph. It doesn't contain any information.  */
    public static final ParIntGraph EMPTY_GRAPH = new ParIntGraph();

    /** Points-to escape graph that summarizes the points-to and escape
	information for the current thread. */
    public PointsToGraph G;
    
    /** The paralel thread map; it attaches to each thread node nT, an 
	integer from the set {0,1,2} where 2 signifies the possibility
	that multiple instances of nT execute in parallel with the current
	thread. */
    public PAThreadMap tau;
    
    /** Maintains the actions executed by the analysed code and the parallel
	action relation. <code>alpha</code> and <code>pi</code> from the 
	original paper have been merged into this single field for efficiency
	reasons. */
    public ActionRepository ar;

    /** Maintains the (conservative) ordering relations between the inside
	and the outside edges. <code>before(ei,eo)</code> is true if the
	inside edge ei might be created before the ouside edge eo was read. */
    public EdgeOrdering eo;
    
    /** Creates a <code>ParIntGraph</code>. */
    public ParIntGraph() {
	G   = new PointsToGraph();
	tau = new PAThreadMap();
	ar  = PointerAnalysis.RECORD_ACTIONS ? new ActionRepository() : null;
	eo  = PointerAnalysis.IGNORE_EO ? null : new EdgeOrdering();
    }
    

    /** <code>join</code> combines two <code>ParIntGraph</code>s in \
	a control-flow join point. */
    public void join(ParIntGraph pig2){
	if(pig2 == null) return;

	G.join(pig2.G);
	tau.join(pig2.tau);
	
	if(PointerAnalysis.RECORD_ACTIONS)
	    ar.join(pig2.ar);

	if(!PointerAnalysis.IGNORE_EO)
	    eo.join(pig2.eo);
    }


    /** Inserts the image of <code>pig2</code> parallel interaction graph
	through the <code>mu</code> node mapping into <code>this</code>
	object.
	This method is designed to be called at the end of the caller/callee
	or starter/startee interaction. It is *not* manipulating the action
	repository nor the edge ordering; those manipulations are too complex
	and variate to be done here.<br>
	<code>principal</code> controls whether the return and exception set
	are inserted. */
    void insertAllButArEo(ParIntGraph pig2, Relation mu,
			boolean principal, Set noholes){
	G.insert(pig2.G, mu, principal, noholes);
	tau.insert(pig2.tau, mu);
    }

    /** Convenient function equivalent to 
	<code>insertAllButArEo(pig2,mu,principal,Collections.EMPTY_SET). */
    void insertAllButArEo(ParIntGraph pig2, Relation mu, boolean principal){
	insertAllButArEo(pig2, mu, principal, Collections.EMPTY_SET);
    }


    ParIntGraph getBarVersion() {
	ParIntGraph bar_pig = new ParIntGraph();
	bar_pig.G = this.G.specialize(get_g2b_map(allNodes()));
	return bar_pig;
    }

    // Get the mapping "genuine node -> bar node" for node \in nodes
    private Map get_g2b_map(Set nodes) {
        Map g2b = new HashMap();
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    assert node.isGenuine() : node + " is not genuine!";
	    PANode bar_node = node.getBarVersion();
	    g2b.put(node, bar_node);
	}

	System.out.println("g2b = " + g2b);

	return g2b;
    }

    /** Check the equality of two <code>ParIntGraph</code>s. */
    public boolean equals(Object obj){
	if(obj == null) return false;

	ParIntGraph pig2 = (ParIntGraph) obj;
	if(!G.equals(pig2.G)){
	    if(DEBUG2)
		System.out.println("The graphs are different");
	    return false;
	}
	if(!tau.equals(pig2.tau)){
	    if(DEBUG2)
		System.out.println("The tau's are different");
	    return false;
	}

	if(PointerAnalysis.RECORD_ACTIONS)
	    if(!ar.equals(pig2.ar)){
		if(DEBUG2){
		    System.out.println("The ar's are different");
		    ar.show_evolution(pig2.ar);
		}
		return false;
	    }

	if(!PointerAnalysis.IGNORE_EO)
	    if(!eo.equals(pig2.eo)) {
		if(DEBUG2)
		    System.out.println("The eo's are different");
		return false;
	    }

	return true;
    }

    /** Private constructor for <code>clone</code> and 
	<code>keepTheEssential</code>. */
    private ParIntGraph(PointsToGraph G, PAThreadMap tau, ActionRepository ar,
			EdgeOrdering eo) {
	this.G   = G;
	this.tau = tau;
	this.ar  = ar;
	this.eo  = eo;
    }

    /** <code>clone</code> produces a copy of the <code>this</code>
	Parallel Interaction Graph. */
    public Object clone() {

	ActionRepository _ar =
	    PointerAnalysis.RECORD_ACTIONS ?
	    (ActionRepository) ar.clone() : null;

	EdgeOrdering _eo = 
	    PointerAnalysis.IGNORE_EO ? null : (EdgeOrdering) eo.clone();

	return new ParIntGraph((PointsToGraph)G.clone(),
			       (PAThreadMap)tau.clone(),
			       _ar, _eo );
    }

    
    /** Produces a <code>ParIntGraph</code> containing only the
	nodes that could be reached from the outside.
	(i.e. via parameters,
	class nodes, normally or exceptionally returned nodes or the
	started thread nodes) */
    public ParIntGraph keepTheEssential(PANode[] params, boolean is_main){
	ParIntGraph pig2 = retain_essential(params, is_main);

	if(AGGRESSIVE_SHRINKING){
	    pig2.shrinking();
	    pig2 = retain_essential(params, is_main);
	}

	return pig2;
    }

    private final ParIntGraph retain_essential(PANode[] params,
					       boolean is_main){
	HashSet remaining_nodes = new HashSet();
	remaining_nodes.addAll(tau.activeThreadSet());

	PointsToGraph _G = 
	    G.keepTheEssential(params, remaining_nodes, is_main);

	PAThreadMap _tau = (PAThreadMap) tau.clone();

	ActionRepository _ar =
	    PointerAnalysis.RECORD_ACTIONS ?
	    ar.keepTheEssential(remaining_nodes) : null;

	EdgeOrdering _eo = 
	    PointerAnalysis.IGNORE_EO ? null : 
	    eo.keepTheEssential(remaining_nodes);

	return new ParIntGraph(_G, _tau, _ar, _eo);
    }


    private ParIntGraph external_view(PANode[] params) {
	return copy_from_roots(Collections.EMPTY_SET,
			       get_external_view_roots(params));
    }


    // Constructs a new ParIntGraph that contains all the elements of this
    // one that are reachable from the Temps vars and the PANodes roots.
    private ParIntGraph copy_from_roots(Set vars, Set roots) {
	Set remaining_nodes = new HashSet();

	PointsToGraph _G =
	    G.copy_from_roots(vars, roots, remaining_nodes);

	PAThreadMap _tau = (PAThreadMap) tau.clone();

	ActionRepository _ar =
	    PointerAnalysis.RECORD_ACTIONS ? 
	    ar.keepTheEssential(remaining_nodes) : null;

	EdgeOrdering _eo = 
	    PointerAnalysis.IGNORE_EO ? null : 
	    eo.keepTheEssential(remaining_nodes);

	return new ParIntGraph(_G, _tau, _ar, _eo);
    }


    // Returns the set of the nodes that can be directly accessed from
    // the outside. This are basically the points through which an external
    // entity can "look" into the parallel interaction graph.
    private Set get_external_view_roots(PANode[] params) {
	Set roots = new HashSet();
	// 1. param nodes +
	for(int i = 0; i < params.length; i++)
	    roots.add(params[i]);
	// 2. returned nodes +
	roots.addAll(G.r);
	// 3. excp. returned nodes +
	roots.addAll(G.excp);
	// 4. active thread nodes +
	roots.addAll(tau.activeThreadSet());
	// 5. static nodes
	for(Iterator it = allNodes().iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(node.type == PANode.STATIC)
		roots.add(node);
	}
	return roots;
    }


    ParIntGraph intra_proc_trimming(PANode[] params) {
	Set roots = get_external_view_roots(params);

	// TODO: in the future, only the live vars should be put here
	Set remaining_vars = new HashSet(G.I.allVariables());
	ParIntGraph newpig = copy_from_roots(remaining_vars,roots);

	System.out.println("ipt: " + this.allNodes().size() + " -> " +
			   newpig.allNodes().size() + " delta=" +
			   (this.allNodes().size() -
			    newpig.allNodes().size()));
	/* + 
			   " r = " + G.r + " " + newpig.G.r +  
			   " excp = " + G.excp + " " + newpig.G.excp);
	*/

	return newpig;
    }
    

    private void add_pointed_by_vars(Set roots, PAEdgeSet E) {
	for(Iterator it = E.allVariables().iterator(); it.hasNext(); ) {
	    Temp v = (Temp) it.next();
	    roots.addAll(E.pointedNodes(v));
	}
    }

    /** Aggressive shrinking: get rid of some unnecessary information.
	TODO: write some better comments. */
    private void shrinking() {
	final Set nodes = new HashSet(allNodes());
	final Set useful_nodes = new HashSet();
	final LinkedList Q = new LinkedList();
	
	init_queue(Q, useful_nodes, nodes);
	final Relation pred_rel = G.O.getPrecedenceRelation();

	while(!Q.isEmpty()) {
	    PANode node = (PANode) Q.removeFirst();
	    
	    for(Iterator it = pred_rel.getValues(node).iterator();
		it.hasNext(); ) {
		PANode pred = (PANode) it.next();
		if(useful_nodes.add(pred))
		    Q.addLast(pred);
	    }
	}

	if(MEASURE_AS)
	    System.out.println("as: " + nodes.size() + " -> " +
			       useful_nodes.size() + " delta=" +
			       (nodes.size() - useful_nodes.size()));
	
	// unuseful_nodes = nodes - useful_nodes
	nodes.removeAll(useful_nodes);

	remove(nodes);
    }
    

    private void init_queue(final LinkedList Q, final Set useful_nodes,
			    final Set nodes) {
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(relevant_node(node))
		if(useful_nodes.add(node))
		    Q.addLast(node);
	}
	G.I.forAllEdges(new PAEdgeVisitor(){
		public void visit(Temp var, PANode node) {}
		public void visit(PANode node1, String f, PANode node2) {
		    if(useful_nodes.add(node1))
			Q.addLast(node1);
		    if(useful_nodes.add(node2))
			Q.addLast(node2);
		}
	    });
    }


    private boolean relevant_node(PANode node) {
	switch(node.type()){
	case PANode.INSIDE:
	    return true;
	case PANode.PARAM:
	    return true;
	case PANode.STATIC:
	    return true;
	case PANode.LOAD:
	    if(!G.e.escapesOnlyInCaller(node) ||
	       (PointerAnalysis.RECORD_ACTIONS && ar.isSyncOn(node)) ||
	       G.r.contains(node) || G.excp.contains(node))
		return true;
	default:
	    if(G.willEscape(node))
		return true;
	}

	return false;
    }
    

    // Visits all the nodes from set_nodes.
    private void forSet(Set set_nodes, PANodeVisitor visitor){
	Iterator it_nodes = set_nodes.iterator();
	while(it_nodes.hasNext())
	    visitor.visit((PANode) it_nodes.next());
    }

    /** Visits all the nodes that appear in <code>this</code> graph. */
    public void forAllNodes(final PANodeVisitor visitor) {
	G.O.forAllNodes(visitor);
	G.I.forAllNodes(visitor);
	forSet(G.r,visitor);
	forSet(G.excp,visitor);
	forSet(G.e.escapedNodes(),visitor);
	forSet(tau.activeThreadSet(),visitor);

	if(PointerAnalysis.RECORD_ACTIONS) {
	    ar.forAllActions(new ActionVisitor(){
		    public void visit_ld(PALoad load){
			visitor.visit(load.n1);
			visitor.visit(load.n2);
			visitor.visit(load.nt);
		    }
		    public void visit_sync(PASync sync){
			visitor.visit(sync.n);
			visitor.visit(sync.nt);
		    }
		});
	    
	    ar.forAllParActions(new ParActionVisitor() {
		    public void visit_par_ld(PALoad load, PANode nt2) {
			visitor.visit(nt2);
		    }
		    public void visit_par_sync(PASync sync, PANode nt2) {
			visitor.visit(nt2);
		    }
		});
	}
	// the edges appearing in the edge ordering relation are also
	// present into G.O and G.I and so, they have been already visited
    }

    /** Returns the set of all the nodes that appear in <code>this</code>
	parallel interaction graph.<br> */
    public Set allNodes(){
	final Set nodes = new HashSet();
	forAllNodes(new PANodeVisitor(){
		public void visit(PANode node){
		    nodes.add(node);
		}
	    });
	nodes.remove(ActionRepository.THIS_THREAD);
	return nodes;
    }

    /* Specialize <code>this</code> <code>ParIntGraph</code> for the call
       site <code>q</code>. */
    final ParIntGraph csSpecialize(final CALL call){
	/* contains mappings old node -> specialized node; each unmapped
	   node is supposed to be mapped to itself. */
	final Map map = new HashMap();
	for(Iterator itn = allNodes().iterator(); itn.hasNext(); ){
	    PANode node = (PANode) itn.next();
	    if(node.type == PANode.INSIDE)
		map.put(node, node.csSpecialize(call));
	} 

	ActionRepository _ar = PointerAnalysis.RECORD_ACTIONS ?
	    ar.csSpecialize(map, call) : null;

	EdgeOrdering _eo = PointerAnalysis.IGNORE_EO ?
	    null : eo.specialize(map);

	ParIntGraph new_graph = 
	    new ParIntGraph(G.specialize(map), tau.specialize(map), _ar, _eo);

	return new_graph;
    }

    /* Specializes <code>this</code> <code>ActionRepository</code> for the
       thread whose run method is <code>run</code>. */
    final ParIntGraph tSpecialize(final MetaMethod run){
	// contains mappings old node -> speciaized node; each unmapped
	// node is supposed to be mapped to itself.
	final Map map = new HashMap();
	for(Iterator itn = allNodes().iterator(); itn.hasNext(); ){
	    PANode node  = (PANode) itn.next();
	    if((node.type != PANode.PARAM) && (node.type != PANode.STATIC)){
		PANode node2 = node.tSpecialize(run);
		map.put(node, node2);
	    }
	}
	
        ActionRepository _ar = PointerAnalysis.RECORD_ACTIONS ?
	    ar.tSpecialize(map, run) : null;

	EdgeOrdering _eo = PointerAnalysis.IGNORE_EO ?
	    null : eo.specialize(map);

	return
	    new ParIntGraph(G.specialize(map), tau.specialize(map), _ar, _eo);
    }

    // weak thread specialization
    final ParIntGraph wtSpecialize(final MetaMethod run){
	// contains mappings old node -> speciaized node; each unmapped
	// node is supposed to be mapped to itself.
	final Map map = new HashMap();
	for(Iterator itn = allNodes().iterator(); itn.hasNext(); ){
	    PANode node  = (PANode) itn.next();
	    if((node.type != PANode.PARAM) && (node.type != PANode.STATIC)){
		PANode node2 = node.wtSpecialize();
		map.put(node, node2);
	    }
	}

	ActionRepository _ar = PointerAnalysis.RECORD_ACTIONS ?
	    ar.tSpecialize(map, run) : null;

	EdgeOrdering _eo = PointerAnalysis.IGNORE_EO ?
	    null : eo.specialize(map);
	
	return
	    new ParIntGraph(G.specialize(map), tau.specialize(map), _ar, _eo);
    }


    /** Removes the nodes from <code>nodes</code> from <code>this</code>
	graph. */
    public void remove(Set nodes){
	G.remove(nodes);
	tau.remove(nodes);
	
	if(PointerAnalysis.RECORD_ACTIONS)
	    ar.removeNodes(nodes);

	if(!PointerAnalysis.IGNORE_EO)
	    eo.removeNodes(nodes);
    }

    /** Simplify <code>this</code> parallel interaction graph by removing the
	loads that don't escape anywhere (and hence, don't represent any
	object). In addition, the <code>&lt;&lt;n1,f&gt;,n2&gt;</code>
	ouside (load) edges where <code>n1</code> is an unescaped node are
	removed too. */
    public void removeEmptyLoads(){
	final Set empty_loads = new HashSet();
	final Set fake_outside_edges = new HashSet();
	
	G.O.forAllEdges(new PAEdgeVisitor(){
		public void visit(Temp var, PANode node){}
		public void visit(PANode node1, String f, PANode node2){
		    if(!G.e.hasEscaped(node1))
			fake_outside_edges.add(new PAEdge(node1,f,node2));
		    if(!G.e.hasEscaped(node2))
			empty_loads.add(node2);
		}
	    });

	if(DEBUG){
	    System.out.println("Empty loads:" + empty_loads);
	    System.out.println("Fake outside edges: " + fake_outside_edges);
	}

	remove(empty_loads);

	Iterator it_edges = fake_outside_edges.iterator();
	while(it_edges.hasNext()){
	    PAEdge edge = (PAEdge) it_edges.next();
	    G.O.removeEdge(edge.n1,edge.f,edge.n2);
	}

	if(PointerAnalysis.RECORD_ACTIONS)
	    ar.removeEdges(fake_outside_edges);

	if(!PointerAnalysis.IGNORE_EO)
	    eo.removeEdges(fake_outside_edges);
    }

    public static boolean SHOW_ACTIONS = false;

    /** Pretty-print function for debug purposes. 
	Two equal <code>ParIntGraph</code>s are guaranteed to have the same
	string representation. */
    public String toString(){
	return
	    "\nParIntGraph{\n" + G + " " + tau +
	    (PointerAnalysis.RECORD_ACTIONS && SHOW_ACTIONS ? 
	     ar.toString() : "") + 
	    (PointerAnalysis.IGNORE_EO ? "" : eo.toString())  + 
	    "}";
    }

    /** Checks whether two <code>ParIntGraph</code>s are equal or not. In
	addition to the <code>equals</code> method, this handles the 
	comparisom of <code>null</code> objects. */
    public static boolean identical(ParIntGraph pig1, ParIntGraph pig2){
	if((pig1 == null) || (pig2 == null))
	    return (pig1 == pig2);
	return pig1.equals(pig2);
    }
}
