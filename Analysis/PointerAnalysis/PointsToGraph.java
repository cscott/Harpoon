// PointsToGraph.java, created Sat Jan  8 23:33:34 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.HashSet;
import java.util.Set;

/**
 * <code>PointsToGraph</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PointsToGraph.java,v 1.1.2.1 2000-01-14 20:51:00 salcianu Exp $
 */
public class PointsToGraph {
    
    // The set of the outside edges
    public PAEdgeSet O;
    // The set of the inside edges
    public PAEdgeSet I;

    // The escape function;
    public PAEscapeFunc e;
    
    // The return set
    public HashSet r;
    
    /** Creates a <code>PointsToGraph</code>. */
    public PointsToGraph() {
	this(new PAEdgeSet(),new PAEdgeSet(),
	     new PAEscapeFunc(),new HashSet());
    }

    /** Tests whether the node n is an escaped node (i.e. it has escaped
     *  through some node ot method hole or will be returned from the 
     *  procedure */
    public boolean escaped(PANode n){
	return e.hasEscaped(n) || r.contains(n);
    }

    /** <code>join</code> is called in the control-flow join points */
    public void join(PointsToGraph G2){
	O.union(G2.O);
	I.union(G2.I);
	e.union(G2.e);
	r.addAll(G2.r);
    }

    /** Private data for <code>propagate</code> algorithm: the worklist
     *  and the currently processed node */ 
    private PAWorkList W_prop = new PAWorkList();
    private PANode current_node = null;

    class PropagateVisitor implements PANodeVisitor{
	public final void visit(PANode node){
	    // take care "|" and NOT "||"
	    boolean changed = 
		e.addNodeHoles(current_node,e.nodeHolesSet(node)) |
		e.addMethodHoles(current_node,e.methodHolesSet(node));
	    if(changed) W_prop.add(node);
	}
    }

    /** an instance of PropagateVisitor */
    private PropagateVisitor p_visitor = new PropagateVisitor();

    /** Propagates the escape information along the edges */
    public void propagate(Set set){
	W_prop.addAll(set);
	while(!W_prop.isEmpty()){
	    current_node = (PANode) W_prop.remove();
	    I.forAllPointedNodes(current_node,p_visitor);
	    O.forAllPointedNodes(current_node,p_visitor);
	}
    }


    /** private constructor for the <code>clone</code> method. */
    private PointsToGraph(PAEdgeSet _O, PAEdgeSet _I,
			  PAEscapeFunc _e, HashSet _r){
	O = _O;
	I = _I;
	e = _e;
	r = _r;
    }

    // Deep copy of the PointsTo Graph 
    public Object clone(){
	return new PointsToGraph((PAEdgeSet)(O.clone()),
				 (PAEdgeSet)(I.clone()),
				 (PAEscapeFunc)(e.clone()),
				 (HashSet)(r.clone()));
    }

    public String toString(){
	return 
	    "Ouside edges:\n" + O +
	    "Inside edges:\n" + I +
	    e + 
	    "Return set:\n" + r;
    }
    
}

