// AbstrPAEdgeSet.java, created Fri Jun 30 15:00:48 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;

import harpoon.Temp.Temp;


/**
 * <code>AbstrPAEdgeSet</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: AbstrPAEdgeSet.java,v 1.2 2002-02-25 20:58:38 cananian Exp $
 */
public abstract class AbstrPAEdgeSet implements PAEdgeSet, Cloneable {
    
    public void addEdge(Temp v, PANode node) {
	throw new UnsupportedOperationException();
    }

    public void addEdges(Temp v, Collection nodes) {
	throw new UnsupportedOperationException();
    }


    public void removeEdge(Temp v, PANode node) {
	throw new UnsupportedOperationException();
    }

    public void removeEdges(Temp v) {
	throw new UnsupportedOperationException();
    }


    public Set pointedNodes(Temp v) {
	throw new UnsupportedOperationException();
    }


    public Set allVariables() {
	throw new UnsupportedOperationException();
    }



    public void addEdge(PANode node1, String f, PANode node2) {
	throw new UnsupportedOperationException();
    }

    public void addEdges(PANode node1, String f, Collection node2s) {
	throw new UnsupportedOperationException();
    }

    public void addEdges(Collection node1s, String f, PANode node2) {
	for(Iterator it = node1s.iterator(); it.hasNext(); )
	    addEdge((PANode) it.next(), f, node2);
    }

    public void addEdges(Collection node1s, String f, Collection node2s) {
	if(node2s.isEmpty()) return;
	for(Iterator it = node1s.iterator(); it.hasNext(); )
	    addEdges((PANode) it.next(), f, node2s);
    }


    public void removeEdge(PANode node1, String f, PANode node2) {
	throw new UnsupportedOperationException();
    }
    
    public void removeEdges(PANode node1, String f) {
	throw new UnsupportedOperationException();
    }

    public void removeEdges(PANode node1) {
	throw new UnsupportedOperationException();
    }


    public Set pointedNodes(PANode node, String f) {
	throw new UnsupportedOperationException();
    }

    public Set pointedNodes(Collection nodes, String f) {
	Set retval = new HashSet();
	for(Iterator it = nodes.iterator(); it.hasNext(); )
	    retval.addAll(pointedNodes((PANode) it.next(), f));
	return retval;
    }

    public Set pointedNodes(PANode node) {
	throw new UnsupportedOperationException();
    }



    public Set allFlagsForNode(PANode node) {
	throw new UnsupportedOperationException();
    }


    public Set allSourceNodes() {
	throw new UnsupportedOperationException();
    }


    // I think this should be removed in the future
    public Set getEdgesFrom(PANode node, String f) {
	Set set = new HashSet();
	for(Iterator it = pointedNodes(node, f).iterator(); it.hasNext(); )
	    set.add(new PAEdge(node, f, (PANode) it.next()));
	return set;
    }



    public void forAllPointedNodes(Temp v, PANodeVisitor visitor) {
	for(Iterator it = pointedNodes(v).iterator(); it.hasNext(); )
	    visitor.visit((PANode) it.next());
    }


    public void forAllPointedNodes(PANode node, String f,
				   PANodeVisitor visitor) {
	for(Iterator it = pointedNodes(node, f).iterator(); it.hasNext(); )
	    visitor.visit((PANode) it.next());
    }

    // for maximum performance, this is implemented in the concrecet class
    public void forAllPointedNodes(PANode node, PANodeVisitor visitor) {
	throw new UnsupportedOperationException();
    }

    public void forAllNodes(PANodeVisitor visitor) {
	for(Iterator it = allVariables().iterator(); it.hasNext(); )
	    forAllPointedNodes((Temp) it.next(), visitor);

	for(Iterator it = allSourceNodes().iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    visitor.visit(node);
	    forAllPointedNodes(node, visitor);
	}
    }

    

    public void forAllEdges(Temp v, PAEdgeVisitor visitor) {
	for(Iterator it = pointedNodes(v).iterator(); it.hasNext(); )
	    visitor.visit(v, (PANode) it.next());
    }
    
    public void forAllEdges(PANode node, PAEdgeVisitor visitor) {
	throw new UnsupportedOperationException();
    }

    public void forAllEdges(PAEdgeVisitor visitor) {
	for(Iterator it = allVariables().iterator(); it.hasNext(); )
	    forAllEdges((Temp) it.next(), visitor);
	for(Iterator it = allSourceNodes().iterator(); it.hasNext(); )
	    forAllEdges((PANode) it.next(), visitor);
    }


    protected PAEdgeSet getEmptyPAEdgeSet() {
	throw new UnsupportedOperationException();
    }

    // very easy implementation (can be hand-crafted into the concrete class)
    public PAEdgeSet specialize(final Map map) {
	final PAEdgeSet es2 = getEmptyPAEdgeSet();

	forAllEdges(new PAEdgeVisitor(){
		public void visit(Temp var, PANode node){
		    es2.addEdge(var, PANode.translate(node,map));
		}
		public void visit(PANode node1, String f, PANode node2){
		    es2.addEdge(PANode.translate(node1,map),
				f,
				PANode.translate(node2,map));
		}
	    });

	return es2;
    }


    public void remove(Set set) {
	throw new UnsupportedOperationException();
    }


    public void union(PAEdgeSet edges2) {
	throw new UnsupportedOperationException();
    }


    public Relation getPrecedenceRelation() {
	final Relation rel = new RelationImpl();
	forAllEdges(new PAEdgeVisitor() {
		public void visit(Temp var, PANode node) {}
		public void visit(PANode node1, String f, PANode node2) {
		    rel.add(node2, node1);
		}
	    });
	return rel;
    }


    public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append(" {\n");

	Object[] vars = Debug.sortedCollection(allVariables());
	for(int i = 0; i < vars.length; i++) {
	    Temp v = (Temp) vars[i];
	    buffer.append("  " + v + " -> ");
	    buffer.append(Debug.stringImg(pointedNodes(v)));
	    buffer.append("\n");
	}


	Object[] nodes = Debug.sortedCollection(allSourceNodes());
	for(int i = 0 ; i < nodes.length ; i++) {
	    PANode node = (PANode) nodes[i];

	    Object[] flags = Debug.sortedCollection(allFlagsForNode(node));
	    if(flags.length == 0) continue;

	    buffer.append("  " + node + " -> {\n");

	    for(int j = 0; j < flags.length; j++) {
		String f = (String) flags[j];
		buffer.append("    " + f + " -> ");
		buffer.append(Debug.stringImg(pointedNodes(node, f)));
		buffer.append("\n");
	    }
	    buffer.append("  }\n");
	}
	
	buffer.append(" }\n");
	
	return buffer.toString();
    }

    public Object clone() {
	try {
	    return super.clone();
	} catch(CloneNotSupportedException e) {
	    throw new InternalError();
	}
    }


    static class PASimpleEdge{
	Temp   t;
	PANode node;

	PASimpleEdge(Temp t, PANode node){
	    this.t    = t;
	    this.node = node;
	}

	public String toString(){
	    return "<" + t + "," + node + ">";
	}
    }

    static class PAComplexEdge{
	PANode node1;
	String f;
	PANode node2;

	PAComplexEdge(PANode node1, String f, PANode node2){
	    this.node1 = node1;
	    this.f     = f;
	    this.node2 = node2;
	}

	public String toString(){
	    return "<" + node1 + "," + f + "," + node2 + ">";
	}
    }

    /** Computes the difference between two sets of edges. Returns the 
	set of edges that are present in es1 but not in es2. The returned
	set contains elements of type <code>PASimpleEdge</code> or
	<code>PAComplexEdge</code>. For debug purposes. */
    public static Set difference(final PAEdgeSet es1,
				 final PAEdgeSet es2){
	final Set retval = new HashSet();
	es1.forAllEdges(new PAEdgeVisitor(){
		public void visit(Temp l, PANode node){
		    if(!es2.pointedNodes(l).contains(node))
			retval.add(new PASimpleEdge(l, node));
		}
		public void visit(PANode node1, String f, PANode node2){
		    if(!es2.pointedNodes(node1, f).contains(node2))
			retval.add(new PAComplexEdge(node1, f, node2));
		}
	    });
	return retval;
    }


    /** Display the edges which were removed and the edges which were
	added while passing from <code>es_old</code> to 
	<code>es_new</code>. */
    public static void show_evolution(final PAEdgeSet es_old,
				      final PAEdgeSet es_new){
	Set old_edges = difference(es_old, es_new);
	if(!old_edges.isEmpty()){
	    System.out.println("Some edges were removed:");
	    for(Iterator it = old_edges.iterator(); it.hasNext();)
		System.out.println("  " + it.next());
	}
	Set new_edges = difference(es_new, es_old);
	if(!new_edges.isEmpty()){
	    System.out.println("New edges were added:");
	    for(Iterator it = new_edges.iterator(); it.hasNext();)
		System.out.println("  " + it.next());
	}
    }
    
}
