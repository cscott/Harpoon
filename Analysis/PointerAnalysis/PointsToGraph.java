// PointsToGraph.java, created Sat Jan  8 23:33:34 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.LinkedList;

import harpoon.Temp.Temp;
import harpoon.Util.Util;

import harpoon.Util.Collections.LinearSet;
import harpoon.Util.DataStructs.Relation;

/**
 * <code>PointsToGraph</code> models the memory, as specified by the 
 abstraction of the object creation sites. Each &quot;node&quot; in our
 abstraction models one or moree objects and the graph of concrete objects
 is modelled as a graph of nodes. In addition, we preserve some escape 
 information.
 Look into one of Martin and John Whaley papers for the complete definition.
 *
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PointsToGraph.java,v 1.2 2002-02-25 20:58:41 cananian Exp $
 */
public class PointsToGraph implements Cloneable, java.io.Serializable{

    public static boolean DEBUG = false;
    
    /** The set of the outside edges. */
    public PAEdgeSet O;
    /** The set of the inside edges. */
    public PAEdgeSet I;

    /** The escape function. */
    public PAEscapeFunc e;
    
    /** The set of normally returned objects */
    public Set r;

    /** The set of objects which are returned as exception */
    public Set excp;
    
    /** Creates a <code>PointsToGraph</code>. */
    public PointsToGraph() {
	this(new LightPAEdgeSet(), new LightPAEdgeSet(),
	     new PAEscapeFunc(), new LinearSet(), new LinearSet());
    }

    // set of nodes reachable from nodes from r (r included)
    private Set reachable_from_r = null;
    // set of nodes reachable from nodes from excp (excp included)
    public Set reachable_from_excp = null;

    /** Flushes the internal caches in this <code>PointsToGraph</code>. */
    public void flushCaches(){
	reachable_from_r    = null;
	reachable_from_excp = null;
    }

    /** Computes the set of nodes reachable from nodes in <code>roots</code>
	through paths that use inside and outside edges. The argument must
	be a set of <code>PANode</code>s. The <code>roots</code> set is
	included in the returned set (i.e. 0-length paths are considered). */
    public final Set reachableNodes(final Set roots){
	// Invariant 1: set contains all the reached nodes
	final Set set = new HashSet();
	// Invariant 2: W contains all the reached nodes whose out-edges
	// have not been explored yet.
	// Obs: W is used as a stack (addLast + removeLast) which corresponds
	// to an "in depth" graph exploration.
	final LinkedList W = new LinkedList();
	// visitor for exploring reached nodes
	final PANodeVisitor nvisitor =
	    new PANodeVisitor(){
		    public void visit(PANode n){
			if(set.add(n)) // newly reached node
			    W.addLast(n); // add it to the worklist
			// note that a node can be added to W at most once!
		    }
		};

	set.addAll(roots);
	W.addAll(roots);

	while(!W.isEmpty()){
	    PANode node = (PANode) W.removeLast();
	    O.forAllPointedNodes(node, nvisitor);
	    I.forAllPointedNodes(node, nvisitor);
	}

	return set;
    }


    /** Returns the set of nodes reachable from the returned nodes
	(including the returned nodes). */
    public Set getReachableFromR(){
	if(reachable_from_r == null)
	    reachable_from_r = reachableNodes(r);
	
	return reachable_from_r;
    }


    /** Returns the set of nodes reachable from the exceptionally returned
	nodes (including the exceptionally returned nodes). */
    public Set getReachableFromExcp(){
	if(reachable_from_excp == null)
	    reachable_from_excp = reachableNodes(excp);
	
	return reachable_from_excp;
    }


    /** Checks whether node <code>node</code> will escape because
	it is returned or because it is reachable from a returned node.
	Both kinds of returns - <code>return</code> and <code>throw</code> -
	are considered. */
    public boolean willEscape(PANode node){
	if(reachable_from_r == null)
	    reachable_from_r = reachableNodes(r);
	if(reachable_from_excp== null)
	    reachable_from_excp = reachableNodes(excp);

	return 
	    reachable_from_r.contains(node) ||
	    reachable_from_excp.contains(node);
    }

    /** Tests whether node <code>node</code> is an escaped node.
	An <i>escaped</i> node is a node which has escaped through some node
	or method hole or is reachable (possibly through a 0-length path)
	from a node which is returned as a normal result or as an exception,
	from the method.  */
    public boolean escaped(PANode node){
	return 
	    e.hasEscaped(node) || 
	    willEscape(node);
    }

    /** Tests whether node <code>node</code> is captured. 
     * This method is simply the negation of <code>escaped</code>. */
    public boolean captured(PANode node){
	return !escaped(node);
    }


    /** <code>join</code> is called in the control-flow join points. */
    public void join(PointsToGraph G2){
	O.union(G2.O);
	I.union(G2.I);
	e.union(G2.e);
	r.addAll(G2.r);
	excp.addAll(G2.excp);
	flushCaches();
    }

    /** Remove all the <code>PANode</code>s that appear in <code>set</code>
	from <code>this</code> points-to graph. */
    public void remove(Set set){
	//tbu
	O.remove(set);
	I.remove(set);
	e.remove(set);
	r.removeAll(set);
	excp.removeAll(set);
	flushCaches();
    }


    /** Inserts the image of <code>G2</code> points-to graph through
	the <code>mu</code> node mapping into <code>this</code>
	object. This method is designed to be called - indirectly
	through <code>ParIntGraph.insertAllButArEo</code> - at the end
	of the caller/callee or starter/startee interaction. <br>

	<code>principal</code> controls whether the return and
	exception set are inserted. */
    public void insert(PointsToGraph G2, Relation mu,
		       boolean principal, Set noholes){
	insert_edges(G2.O , G2.I , mu );
	e.insert(G2.e , mu , noholes );
	if(principal){
	    insert_set(G2.r    , mu , r );
	    insert_set(G2.excp , mu , excp );
	}
    }


    public void insert(PointsToGraph G2,
		       Relation mu,
		       Set noholes,
		       ODInformation odi_org,
		       Set holes_b4_callee,
		       ODInformation odi_new){

	insert_edges(G2.O, G2.I, 
		     mu, 
		     odi_org,
		     holes_b4_callee,
		     odi_new);

	e.insert(G2.e, mu, noholes);
    }

    public void insert(PAEdgeSet O_org,
		       PAEdgeSet I_org,
		       Relation mu,
		       ODInformation odi_tmp,
		       ODParIntGraph pig){

	insert_edges(O_org,
		     I_org,
		     mu, 
		     odi_tmp,
		     pig);
    }

    // Insert the outside edges O2 and the inside edges I2 into this graph,
    // transforming them through the mu mapping.
    // FV changed from private to public 
    public void insert_edges(PAEdgeSet O2, PAEdgeSet I2, final Relation mu){

	// visitor for the outside edges
	PAEdgeVisitor visitor_O = new PAEdgeVisitor(){
		public void visit(Temp var, PANode node){
		    Util.assert(false," var2node edge in O: " + 
				       var + "->" + node);
		}
		public void visit(PANode node1,String f, PANode node2){
		    if(!mu.contains(node2,node2)) return;
		    Set mu_node1 = mu.getValues(node1);
		    O.addEdges(mu_node1, f, node2);
		}
	    };

	O2.forAllEdges(visitor_O);

	// visitor for the inside edges
	PAEdgeVisitor visitor_I = new PAEdgeVisitor(){
		public void visit(Temp var, PANode node){
		    Set mu_node = mu.getValues(node);
		    I.addEdges(var,mu_node);
		}
		public void visit(PANode node1,String f, PANode node2){
		    Set mu_node1 = mu.getValues(node1);
		    Set mu_node2 = mu.getValues(node2);
		    I.addEdges(mu_node1,f,mu_node2);
		}
	    };

	I2.forAllEdges(visitor_I);
    }


    public void insert_edges(PAEdgeSet O2, PAEdgeSet I2, 
			     final Relation mu,
			     final ODInformation odi_org,
			     final Set holes_b4_callee,
			     final ODInformation odi_new)
    {
	// visitor for the outside edges
	PAEdgeVisitor visitor_O = new PAEdgeVisitor(){
		public void visit(Temp var, PANode node){
		    Util.assert(false," var2node edge in O: " + 
				var + "->" + node);
		}
		public void visit(PANode node1,String f, PANode node2){
		    if(!mu.contains(node2,node2)) return;
		    Set mu_node1 = mu.getValues(node1);
		    O.addEdges(mu_node1, f, node2);
		    odi_new.addOutsideEdges(node1, f, node2,
					    odi_org,
					    mu_node1, holes_b4_callee);
		}
	    };
	
	O2.forAllEdges(visitor_O);

	// visitor for the inside edges
	PAEdgeVisitor visitor_I = new PAEdgeVisitor(){
		public void visit(Temp var, PANode node){
		    Set mu_node = mu.getValues(node);
		    I.addEdges(var,mu_node);
		}
		public void visit(PANode node1,String f, PANode node2){
		    Set mu_node1 = mu.getValues(node1);
		    Set mu_node2 = mu.getValues(node2);
		    I.addEdges(mu_node1,f,mu_node2);
		    odi_new.addInsideEdges(node1, f, node2,
					   odi_org,
					   mu_node1, mu_node2,
					   holes_b4_callee);
		}
	    };
	
	I2.forAllEdges(visitor_I);
    }

    public void insert_edges(final PAEdgeSet O_org,
			     final PAEdgeSet I_org,
			     final Relation mu,
			     final ODInformation odi_tmp,
			     final ODParIntGraph pig)
    {
	final ODInformation odi_org = pig.odi;

	// visitor for the outside edges
	PAEdgeVisitor visitor_O = new PAEdgeVisitor(){
	    public void visit(Temp var, PANode node){
		Util.assert(false," var2node edge in O: " + 
			    var + "->" + node);
	    }
	    public void visit(PANode node1,String f, PANode node2){
		Set mu_node1 = mu.getValues(node1);
		if ((mu_node1==null) || (mu_node1.isEmpty())) return; 
		
		O.addEdges(mu_node1, f, node2);
		odi_tmp.addOutsideEdges(node1, f, node2,
					odi_org,
					mu_node1);
	    }
	};
	
	O_org.forAllEdges(visitor_O);

	// visitor for the inside edges
	PAEdgeVisitor visitor_I = new PAEdgeVisitor(){
	    public void visit(Temp var, PANode node){
		Set mu_node = mu.getValues(node);
		I.addEdges(var,mu_node);
	    }
	    public void visit(PANode node1,String f, PANode node2){
		int n_projected = 2;

		Set mu_node1 = mu.getValues(node1);
		if ((mu_node1==null) || (mu_node1.isEmpty())) {
		    mu_node1 = new HashSet();
		    n_projected--;
		}
		mu_node1.add(node1);

		Set mu_node2 = mu.getValues(node2);
		if ((mu_node2==null) || (mu_node2.isEmpty())) {
		    mu_node2 = new HashSet();
		    n_projected--;
		}
		mu_node2.add(node2);

		// node of the two nodes are projected, there is
		// nothing to do here.
		if (n_projected==0) return;

		I.addEdges(mu_node1,f,mu_node2);
		odi_tmp.addInsideEdges(node1, f, node2,
				       odi_org,
				       mu_node1, mu_node2);
	    }
	};
	
	I_org.forAllEdges(visitor_I);
    }
    /* Specializes <code>this</code> <code>PointsToGraph</code> according to
       <code>map</code>, a mapping from <code>PANode<code> to
       <code>PANode</code>. Each node which is not explicitly mapped is
       considered to be mapped to itself. */
    public PointsToGraph specialize(final Map map){
	return
	    new PointsToGraph(O.specialize(map), I.specialize(map),
			      e.specialize(map),
			      PANode.specialize_set(r,map),
			      PANode.specialize_set(excp,map));
    }

    // Insert the image of the source set through the mu mapping into
    // dest. Forall node in source, addAll mu(node) to dest. source
    // and dest should both be sets of PANodes.
    private void insert_set(Set source, Relation mu, Set dest){
	Iterator it = source.iterator();
	while(it.hasNext()){
	    PANode node = (PANode) it.next();
	    Set node_image = mu.getValues(node);
	    dest.addAll(node_image);
	}
    }

    // TODO: the return result is used in a single place in the program
    // but the cost of its computation is quite big. We could make a
    // lightweight version of this function with return type void.
    /** Propagates the escape information along the edges.
	Returns the set of the newly escaped nodes. */
    public Set propagate(Collection escaped){
	final PAWorkList W_prop = new PAWorkList();
	final Set newly_escaped = new HashSet();

	W_prop.addAll(escaped);
	while(!W_prop.isEmpty()){
	    final PANode current_node = (PANode) W_prop.remove();

	    PANodeVisitor p_visitor = new PANodeVisitor(){
		    public final void visit(PANode node){
			boolean was_escaped = e.hasEscaped(node);
			boolean changed = false;
			if(e.addNodeHoles(node,e.nodeHolesSet(current_node))){
			    changed = true;
// 			    System.out.println(node + " because of node " +
// 					       current_node + 
// 					       e.nodeHolesSet(current_node));
			}
			///// if(e.getEscapedIntoMH().contains(current_node) &&
			/////   e.addMethodHole(node, null))
			/////    changed = true;
			if(e.addMethodHoles(node,
					    e.methodHolesSet(current_node))){
			    changed = true;
// 			    System.out.println(node + " because of node " +
// 					       current_node + 
// 					       e.methodHolesSet(current_node));
			}
			/////
			if(changed){
			    W_prop.add(node);
			    if(!was_escaped) newly_escaped.add(node);
			}
		    }
		};

	    I.forAllPointedNodes(current_node, p_visitor);
	    O.forAllPointedNodes(current_node, p_visitor);
	}
	return newly_escaped;
    }

    /** Convenient function equivalent that recomputes all the escape
	information.
	Equivalent to <code>propagate(e.escapedNodes())</code>. */
    public Set propagate(){
	return propagate(e.escapedNodes());
    }

    /** Checks the equality of two <code>PointsToGraph</code>s. */
    public boolean equals(Object o){
	if(o==null) return false;
	PointsToGraph G2 = (PointsToGraph)o;
	if(!O.equals(G2.O)){
	    if(ParIntGraph.DEBUG2 || DEBUG){
		System.out.println("different O's");
		AbstrPAEdgeSet.show_evolution(G2.O, O);
	    }
	    return false;
	}
	if(!I.equals(G2.I)){
	    if(ParIntGraph.DEBUG2 || DEBUG){
		System.out.println("different I's");
		AbstrPAEdgeSet.show_evolution(G2.I, I);
	    }
	    return false;
	}
	if(!r.equals(G2.r)){
	    if(ParIntGraph.DEBUG2 || DEBUG)
		System.out.println("different r's");
	    return false;
	}
	if(!excp.equals(G2.excp)){
	    if(ParIntGraph.DEBUG2 || DEBUG)
		System.out.println("different excp's");
	    return false;
	}
	if(!e.equals(G2.e)){
	    if(ParIntGraph.DEBUG2 || DEBUG) {
		System.out.println("different e's");
		System.out.println("this.e : " + e);
		System.out.println("G2.e   : " + G2.e);
	    }
	    return false;
	}
	return true;
    }

    /** Private constructor for the <code>clone</code> method. */
    private PointsToGraph(PAEdgeSet _O, PAEdgeSet _I,
			  PAEscapeFunc _e, Set _r, Set _excp){
	O = _O;
	I = _I;
	e = _e;
	r = _r;
	excp = _excp;
    }


    /** Deep copy of a <code>PointsToGraph</code>. */ 
    public Object clone() {
	try {
	    PointsToGraph newptg = (PointsToGraph) super.clone();
	    newptg.O    = (PAEdgeSet) O.clone();
	    newptg.I    = (PAEdgeSet) I.clone();
	    newptg.e    = (PAEscapeFunc) e.clone();
	    newptg.r    = (Set) ((LinearSet) r).clone();
	    newptg.excp = (Set) ((LinearSet) excp).clone();
	    return newptg;
	} catch(CloneNotSupportedException e) {
	    throw new InternalError();
	}
    }


    /** Finds the static nodes that appear as source nodes in the set of
	edges <code>E</code> and add them to <code>set</code> */
    private void grab_static_roots(PAEdgeSet E, Set set){
	for(Iterator it = E.allSourceNodes().iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(node.type == PANode.STATIC)
		set.add(node);
	}
    }

    /** Produces a <code>PointsToGraph</code> containing just the nodes
	that are reachable from <i>root node</i>: the nodes that could be 
	reached from outside
	code (e.g. the caller): the parameter nodes (received in
	the <code>params</code>) and the returned nodes (found 
	in <code>this.r</code>).
	The static nodes are implicitly considered roots
	for all the methods except &quot;main&quot; 
	(<code>is_main=true</code>).<br>
	In addition to returning the new, reduced graph, this method builds
	the set of nodes that are in the new graph. This set is put in 
	<code>remaining_nodes</code> */
    public PointsToGraph keepTheEssential(PANode[] params,
					  final Set remaining_nodes,
					  boolean is_main){
	PAEdgeSet _O = new LightPAEdgeSet();
	PAEdgeSet _I = new LightPAEdgeSet();
	// the same sets of return nodes and exceptions
	Set _r    = (Set) ((LinearSet) r).clone();
	Set _excp = (Set) ((LinearSet) excp).clone();

	// Put the parameter nodes in the root set
	for(int i = 0; i < params.length; i++)
	    remaining_nodes.add(params[i]);

	// In the case of the main method, the static nodes are no longer
	// considered to be roots; for all the other methods they must be,
	// because they can be accessed by code outside the analyzed
	// procedure (e.g. the caller)
	if(!is_main){
	    grab_static_roots(O, remaining_nodes);
	    grab_static_roots(I, remaining_nodes);
	}

	// Add the normal return & exception nodes to the set of roots
	remaining_nodes.addAll(r);
	remaining_nodes.addAll(excp);

	// worklist of "to be explored" nodes
	final PAWorkList worklist = new PAWorkList();
	// put all the roots in the worklist
	worklist.addAll(remaining_nodes);

	// copy the relevant edges and build the set of essential_nodes
	PANodeVisitor visitor = new PANodeVisitor(){
		public final void visit(PANode node){
		    if(remaining_nodes.add(node))
			worklist.add(node);
		}		
	    };
	while(!worklist.isEmpty()){
	    PANode node = (PANode)worklist.remove();
	    O.copyEdges(node,_O);
	    I.copyEdges(node,_I);
	    O.forAllPointedNodes(node,visitor);
	    I.forAllPointedNodes(node,visitor);
	}

	// System.out.println("\nREMAINING NODES: " + remaining_nodes);

	// retain only the escape information for the remaining nodes
	PAEscapeFunc _e = e.select(remaining_nodes);

	return new PointsToGraph(_O,_I,_e,_r,_excp);
    }
    

    // TODO: keep the essential should be modified to use this method.
    // there is a lot of code duplication here.
    PointsToGraph copy_from_roots(final Set vars, final Set roots,
				  final Set remaining_nodes) {
	remaining_nodes.addAll(roots);

	final PAEdgeSet _O = new LightPAEdgeSet();
	final PAEdgeSet _I = new LightPAEdgeSet();

	for(Iterator it = vars.iterator(); it.hasNext(); ) {
	    final Temp v = (Temp) it.next();
	    I.forAllPointedNodes
		(v,
		 new PANodeVisitor(){
			 public void visit(PANode node) {
			     _I.addEdge(v, node);
			     remaining_nodes.add(node);
			 }
		     });
	}

	// the same sets of return nodes and exceptions
	Set _r    = (Set) ((LinearSet) r).clone();
	Set _excp = (Set) ((LinearSet) excp).clone();

	// worklist of "to be explored" nodes
	final PAWorkList worklist = new PAWorkList();
	// put all the roots in the worklist
	worklist.addAll(remaining_nodes);

	// copy the relevant edges and build the set of essential_nodes
	PANodeVisitor visitor = new PANodeVisitor(){
		public final void visit(PANode node){
		    if(remaining_nodes.add(node))
			worklist.add(node);
		}		
	    };

	while(!worklist.isEmpty()){
	    PANode node = (PANode) worklist.remove();
	    O.copyEdges(node, _O);
	    I.copyEdges(node, _I);
	    O.forAllPointedNodes(node, visitor);
	    I.forAllPointedNodes(node, visitor);
	}

	// retain only the escape information for the remaining nodes
	PAEscapeFunc _e = e.select(remaining_nodes);

	return new PointsToGraph(_O,_I,_e,_r,_excp);	
    }

    /** Pretty-print function for debug purposes.
	Two equal <code>PointsToGraph</code>s are guaranteed to have the same
	string representation. */
    public String toString(){
	return 
	    " Outside edges:" + O +
	    " Inside edges:" + I +
	    e + 
	    " Return set:" + Debug.stringImg(r) + "\n" +
	    " Exceptions:" + Debug.stringImg(excp) + "\n";
    }
    
}
