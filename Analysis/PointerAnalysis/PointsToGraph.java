// PointsToGraph.java, created Sat Jan  8 23:33:34 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.HashSet;
import java.util.Set;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * <code>PointsToGraph</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PointsToGraph.java,v 1.1.2.6 2000-01-23 00:42:11 salcianu Exp $
 */
public class PointsToGraph {
    
    /** The set of the outside edges. */
    public PAEdgeSet O;
    /** The set of the inside edges. */
    public PAEdgeSet I;

    /** The escape function. */
    public PAEscapeFunc e;
    
    /** The set of normally returned objects */
    public HashSet r;

    /** The set of objects which are returned as exception */
    public HashSet excp;
    
    /** Creates a <code>PointsToGraph</code>. */
    public PointsToGraph() {
	this(new PAEdgeSet(),new PAEdgeSet(),
	     new PAEscapeFunc(),new HashSet(),new HashSet());
    }

    /** Tests whether the node <code>n</code> is an escaped node; 
     * i.e. it has escaped through some node ot method hole or will be 
     * returned from the procedure. */
    public boolean escaped(PANode n){
	return e.hasEscaped(n) || r.contains(n);
    }

    /** <code>join</code> is called in the control-flow join points. */
    public void join(PointsToGraph G2){
	O.union(G2.O);
	I.union(G2.I);
	e.union(G2.e);
	r.addAll(G2.r);
	excp.addAll(G2.excp);
    }

    /** Private data for <code>propagate</code> algorithm: the worklist
     *  and the currently processed node. */ 
    private PAWorkList W_prop = new PAWorkList();
    private PANode current_node = null;

    class PropagateVisitor implements PANodeVisitor{
	public final void visit(PANode node){
	    // take care "|" and NOT "||"
	    boolean changed = 
		e.addNodeHoles(node,e.nodeHolesSet(current_node)) |
		e.addMethodHoles(node,e.methodHolesSet(current_node));
	    if(changed) W_prop.add(node);
	}
    }

    /** an instance of PropagateVisitor */
    private PropagateVisitor p_visitor = new PropagateVisitor();

    /** Propagates the escape information along the edges. */
    public void propagate(Set set){
	W_prop.addAll(set);
	while(!W_prop.isEmpty()){
	    current_node = (PANode) W_prop.remove();
	    I.forAllPointedNodes(current_node,p_visitor);
	    O.forAllPointedNodes(current_node,p_visitor);
	}
    }

    /** Checks the equality of two <code>PointsToGraph</code>s. */
    public boolean equals(Object o){
	if(o==null) return false;
	PointsToGraph G2 = (PointsToGraph)o;
	//return 
	//  O.equals(G2.O) && I.equals(G2.I) &&
	//  e.equals(G2.e) && r.equals(G2.r);
	if(!O.equals(G2.O)){
	    //System.out.println("different O's");
	    return false;
	}
	if(!I.equals(G2.I)){
	    //System.out.println("different I's");
	    return false;
	}
	if(!e.equals(G2.e)){
	    //System.out.println("different e's");
	    return false;
	}
	if(!r.equals(G2.r)){
	    //System.out.println("different r's");
	    return false;
	}
	if(!excp.equals(G2.excp)){
	    //System.out.println("different r's");
	    return false;
	}
	return true;
    }

    /** Private constructor for the <code>clone</code> method. */
    private PointsToGraph(PAEdgeSet _O, PAEdgeSet _I,
			  PAEscapeFunc _e, HashSet _r, HashSet _excp){
	O = _O;
	I = _I;
	e = _e;
	r = _r;
	excp = _excp;
    }


    /** Deep copy of a <code>PointsToGraph</code>. */ 
    public Object clone(){
	return new PointsToGraph((PAEdgeSet)(O.clone()),
				 (PAEdgeSet)(I.clone()),
				 (PAEscapeFunc)(e.clone()),
				 (HashSet)(r.clone()),
				 (HashSet)(excp.clone()));
    }


    /** Finds the static nodes that appear as source nodes in the set of
     * edges <code>E</code> and put them in <code>static_nodes</code> */
    private void grab_static_roots(PAEdgeSet E, Set static_nodes){
	Enumeration enum = E.allNodes();
	while(enum.hasMoreElements()){
	    PANode node = (PANode)enum.nextElement();
	    if(node.type == PANode.STATIC)
		static_nodes.add(node);
	}
    }

    /** Produces a <code>PointsToGraph</code> containing just the nodes
     * that are reachable from <i>root node</i>: the nodes that could be 
     * reached from outside
     * code (e.g. the caller): the parameter nodes (received in
     * the parameter <code>params</code>) and the returned nodes (found 
     * in <code>this.r</code>).
     * The static nodes are implicitly considered roots
     * for all the methods except &quot;main&quot; 
     * (<code>is_main=true</code>).<br>
     * In addition to returning the new, reduced graph, this method builds
     * the set of nodes that are in the new graph. This set is put in 
     * <code>remaining_nodes</code> */
    public PointsToGraph keepTheEssential(PANode[] params,
					  final Set remaining_nodes,
					  boolean is_main){
	PAEdgeSet _O = new PAEdgeSet();
	PAEdgeSet _I = new PAEdgeSet();
	PAEscapeFunc _e = new PAEscapeFunc();
	// the same sets of return nodes and exceptions
	HashSet _r = (HashSet) r.clone();
	HashSet _excp = (HashSet) excp.clone();

	// worklist of "to be explored" nodes
	final PAWorkList worklist = new PAWorkList();
	// set of the static nodes
	Set static_nodes = new HashSet();
	
	// Put the parameter nodes in the root set
	for(int i=0;i<params.length;i++){
	    worklist.add(params[i]);
	    remaining_nodes.add(params[i]);
	}
	
	// In the case of the main method, the static nodes are no longer
	// considered to be roots; for all the other methods they must be,
	// because they can be accessed by code outside the analyzed
	// procedure (e.g. the caller)
	if(!is_main){
	    grab_static_roots(O, static_nodes);
	    grab_static_roots(I, static_nodes);
	    worklist.addAll(static_nodes);
	    remaining_nodes.addAll(static_nodes);
	}

	// Add the normal return nodes to the set of roots
	worklist.addAll(r);
	remaining_nodes.addAll(r);
	// Add the exception nodes too
	worklist.addAll(excp);
	remaining_nodes.addAll(excp);

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

	// build the reduced graph
	PointsToGraph ptg = new PointsToGraph(_O,_I,_e,_r,_excp);

	// Build the new escape info: initial data + propagation
	Iterator it_static = remaining_nodes.iterator();
	while(it_static.hasNext()){
	    PANode node = (PANode) it_static.next();
	    // Each static node escapes through itself
	    if(node.type == PANode.STATIC)
		_e.addNodeHole(node,node);
	    // the method holes are preserved
	    _e.addMethodHoles(node,e.methodHolesSet(node));
	}
	ptg.propagate(static_nodes);

	return ptg;
    }
    

    public String toString(){
	return 
	    " Outside edges:" + O + "\n" +
	    " Inside edges:" + I + "\n" +
	    " " + e + "\n" + 
	    " Return set:" + r + "\n" +
	    " Exceptions:" + excp + "\n";
    }
    
}

