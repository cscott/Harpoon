// PTEdgesSet.java, created Sat Jan  8 23:37:30 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Collections;
import java.util.Set;
import java.util.Hashtable;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.Enumeration;

import java.util.Arrays;

import harpoon.Temp.Temp;

/**
 * <code>PAEdgeSet</code> models the concept of a set of edges.
 It tries to optimize the frequent operations on such a set. It is designed
 to handle the different types of edges (<i>i.e.</i> <code>var --> node</code>
 and <code>node1 --f-> node2</code>) in a convenient and type safe manner.
 It is also intended to be more performant in terms of space and time
 than the straightforward solution of a <code>HashSet</code> of edges.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PAEdgeSet.java,v 1.1.2.10 2000-02-15 04:37:39 salcianu Exp $
 */
public class PAEdgeSet {

    // There are two kinds of edges: variable --> node and 
    // node --f--> node where f is a field. This is reflected in
    // the two different structures we use to store the edges.

    // mapping variable -> set of pointed nodes
    private Relation vars;

    // mapping node -> a <code>Relation</code> flag -> set of pointed nodes
    private Hashtable nodes;


    /** Creates a <code>PAEdgesSet</code>. */
    public PAEdgeSet() {
	vars  = new Relation();
	nodes = new Hashtable();
    }

    // OPERATIONS ON THE SET OF EDGES


    // A. Operations on edges starting from variables.

    /** Returns the set of all the nodes pointed to by the variable
     * <code>v</code>, i.e. all the nodes <code>n</code> such that 
     * <code>&lt;v,n&gt;</code> exists. */
    public Set pointedNodes(Temp v){
	return vars.getValuesSet(v);
    }

    /** Adds the edges <code>&lt;v,n&gt;</code> where <code>n</code> ranges
     * over the set <code>pointed_nodes</code>. This function is more
     * efficient than just iterating over the set and calling 
     * <code>addEdge</code> for each element. */
    public void addEdges(Temp v, Set pointed_nodes){
	vars.addAll(v,pointed_nodes);
    }
    
    /** Adds the edge <code>&lt;v,n&gt;</code>. */
    public void addEdge(Temp v,PANode n){
	vars.add(v,n);
    }

    /** Removes all the edges from the variable <code>v</code>. */
    public void removeEdges(Temp v){
	vars.removeAll(v);
    }


    // B. Operations on edges starting from nodes.

    /** Returns all the nodes pointed to by the node <code>n</code> through
     *  the field <code>f</code>; <i>i.e.</i> all the nodes <code>n1</code>
     *  such that &lt;&lt;n,f&gt;,n1&gt; exists. */
    public Set pointedNodes(PANode n, String f){
	Relation rel = (Relation) nodes.get(n);
	if(rel==null) return Collections.EMPTY_SET;
	return rel.getValuesSet(f);
    }

    /** Returns all the nodes which are pointed to by nodes from the set
     *  <code>sources</code>. */
    public Set pointedNodes(Set sources, String f){
	Iterator it = sources.iterator();
	HashSet pointed_nodes = new HashSet(); 
	while(it.hasNext())
	    pointed_nodes.addAll(pointedNodes((PANode)it.next(),f));
	return pointed_nodes;
    }

    // DIFFERENT addEdge(s) functions
    /** Adds all the edges &lt;&lt;n,f&gt;,n1&gt; where <code>n1</code>
     *  ranges over the set <code>dests</code>. */
    public void addEdges(PANode n, String f, Set dests){
	if(dests.isEmpty()) return;
	Relation rel = (Relation)nodes.get(n);
	if(rel==null) 
	    nodes.put(n,rel=new Relation());
	rel.addAll(f,dests);
    }

    /** Adds all the edges <code>&lt;&lt;n1,f&gt;,n2&gt;</code> for every node
     *  <code>n1</code> from the set <code>sources</code> and every node
     *  <code>n2</code> from the set <code>dests</code>. */
    public void addEdges(Set sources, String f, Set dests){
	Iterator it = sources.iterator();
	while(it.hasNext())
	    addEdges((PANode)it.next(),f,dests);
    }

    /** Adds all the edges <code>&lt;&lt;n1,f&gt;,n&gt;</code> for every node
     *  <code>n1</code> from the set <code>sources</code>. */
    public void addEdges(Set sources, String f, PANode dest){
	Iterator it = sources.iterator();
	while(it.hasNext())
	    addEdge((PANode)it.next(),f,dest);
    }

    /** Adds the edge <code>&lt;&lt;n,f&gt;,n1&gt;</code>. If you want to
     * add many edges, consider using <code>addEdges</code> (repeatedly
     * calling <code>addEdge</code> over all the edges of  a set is
     * <b>very</b> expensive. */
    public void addEdge(PANode n, String f, PANode n1){
	Relation rel = (Relation)nodes.get(n);
	if(rel==null) 
	    nodes.put(n,rel=new Relation());
	rel.add(f,n1);
    }

    /** Deletes all the edges of the type 
	<code>&lt;&lt;n,f&gt;,n1&gt;</code>. */
    public void removeEdges(PANode n, String f){
	Relation rel = (Relation)nodes.get(n);
	if(rel==null) return;
	rel.removeAll(f);
    }


    /** Deletes all the edges leaving from <code>n</code>. */
    public void removeEdges(PANode n){
	Enumeration enum = allFlagsForNode(n);
	while(enum.hasMoreElements())
	    removeEdges( n , (String)enum.nextElement() );
    }


    /** Returns an <code>Enumeration</code> of the set of all the
     * variables that are mentioned in <code>this</code> set of edges
     * (<i>ie</i> <code>v</code> such that there is at least one 
     * <code>&lt;v,n&gt;</code> edge). */
    public Enumeration allVariables(){
	return vars.keys();
    }

    /** Returns an <code>Enumeration</code> of the set of all the \
	source nodes that are mentioned in <code>this</code> set of edges.
	That is, the set of all <code>n</code> such that there is at least one 
	<code>&lt;&lt;n,f&gt;,n1&gt;</code> edge. */
    public Enumeration allSourceNodes(){
	return nodes.keys();
    }

    // Enumeration over an empty set; ugly, let me know if you know
    // something better */ // TODO
    static final private Enumeration EMPTY_ENUM = (new Hashtable()).keys();

    /** Returns an <code>Enumeration</code> of all the flags from a
     * specific node n, i.e. <code>f</code> such that we have at least one 
     * &lt;&lt;n,f&gt;,n1&gt; edge */
    public Enumeration allFlagsForNode(PANode n){
	Relation rel = (Relation)nodes.get(n);
	if(rel==null)
	    return EMPTY_ENUM;
	return rel.keys();
    }

    /** Visits all the nodes pointed by <code>n</code> and executes
     *  <code>visitor.visit(...)</code> on each of them. */
    public void forAllPointedNodes(PANode n, PANodeVisitor visitor){
	// Go through all the fields attached to n;
	Enumeration enum = allFlagsForNode(n);
	while(enum.hasMoreElements()){
	    String field = (String)enum.nextElement();
	    // for each such field, visit all the pointed nodes.
	    Iterator it = pointedNodes(n,field).iterator();
	    while(it.hasNext()){
		PANode node = (PANode) it.next();
		visitor.visit(node);
	    }
	}
    }
    
    /** Visits all the nodes pointed by the variable <code>v</code>. */
    public final void forAllPointedNodes(Temp var, PANodeVisitor visitor){
	Iterator it = pointedNodes(var).iterator();
	while(it.hasNext()){
	    PANode n = (PANode) it.next();
	    visitor.visit(n);
	}
    }

    /** Visits each node that appears in <code>this</code> collection of edges.
	Each node is visited at least once; if the visit function is not 
	idempotent it is its reponsibility to check and return immediately
	from nodes that have already been visited. */
    public void forAllNodes(PANodeVisitor visitor){
	Enumeration enum_vars = allVariables();
	while(enum_vars.hasMoreElements())
	    forAllPointedNodes((Temp) enum_vars.nextElement(),visitor);

	Enumeration enum_nodes = allSourceNodes();
	while(enum_nodes.hasMoreElements())
	    forAllPointedNodes((PANode)enum_nodes.nextElement(),visitor);
    }

    /** Visits all the edges starting from the variable <code>v</code>. */
    private void forAllEdges(Temp var, PAEdgeVisitor visitor){
	Iterator it = pointedNodes(var).iterator();
	while(it.hasNext()){
	    PANode node = (PANode) it.next();
	    visitor.visit(var,node);
	}
    }

    /** Visits all the edges starting from the node <code>node</code>. */
    private void forAllEdges(PANode node, PAEdgeVisitor visitor){
	// Go through all the fields attached to n;
	Enumeration enum = allFlagsForNode(node);
	while(enum.hasMoreElements()){
	    String field = (String)enum.nextElement();
	    // for each such field, visit all the pointed nodes.
	    Iterator it = pointedNodes(node,field).iterator();
	    while(it.hasNext()){
		PANode node2 = (PANode) it.next();
		visitor.visit(node,field,node2);
	    }
	}
    }

    /** Visits all the edges from <code>this</code> set of edges. */ 
    public void forAllEdges(PAEdgeVisitor visitor){
	Enumeration enum_vars = allVariables();
	while(enum_vars.hasMoreElements())
	    forAllEdges((Temp)enum_vars.nextElement(),visitor);

	Enumeration enum_nodes = allSourceNodes();
	while(enum_nodes.hasMoreElements())
	    forAllEdges((PANode)enum_nodes.nextElement(),visitor);
    }


    /** Remove all the <code>PANode</code>s that appear in <code>set</code>
	from <code>this</code> edge set together with the related edges. */
    public void remove(Set set){
	// remove the edges starting from these nodes
	Iterator it_nodes = set.iterator();
	while(it_nodes.hasNext()){
	    PANode node = (PANode) it_nodes.next();
	    removeEdges(node);
	}
	// remove the edges ending into these nodes
	Enumeration enum_nodes = nodes.keys();
	while(enum_nodes.hasMoreElements()){
	    PANode node  = (PANode) enum_nodes.nextElement();
	    Relation rel = (Relation) nodes.get(node);
	    
	    Enumeration enum_flags = rel.keys();
	    while(enum_flags.hasMoreElements()){
		String f = (String) enum_flags.nextElement();
		rel.remove(f,set);
	    }

	    if(rel.isEmpty())
		nodes.remove(node);
	}
    }


    /** <code>union</code> computes the union of two sets of edges
     * (in a control-flow join point, for example). */
    public void union(PAEdgeSet edges2){
	// union of the edges from the variables
	vars.union(edges2.vars);

	// union of the edges from the nodes
	Enumeration en = edges2.allSourceNodes();
	while(en.hasMoreElements()){
	    PANode n = (PANode)en.nextElement();
	    Relation rel = (Relation)nodes.get(n);
	    if(rel==null){
		rel = new Relation();
		nodes.put(n,rel);
	    }
	    rel.union((Relation)edges2.nodes.get(n));
	}
    }


    /** Checks the equality of two <code>PAEdgeSet</code>s. */
    public boolean equals(Object o){
	if(o==null) return false;
	PAEdgeSet es2 = (PAEdgeSet) o;
	if(!vars.equals(es2.vars)){
	    //System.out.println("different vars");
	    return false;
	}
	if(!nodes.keySet().equals(es2.nodes.keySet())){
	    //System.out.println("different keySet's");
	    return false;
	}
	Enumeration enum = nodes.keys();
	while(enum.hasMoreElements()){
	    PANode node = (PANode)enum.nextElement();
	    Relation rel1 = (Relation)nodes.get(node);
	    Relation rel2 = (Relation)es2.nodes.get(node);
	    if(!rel1.equals(rel2)){
		//System.out.println("different relations");
		return false;
	    }
	}
	return true;
    }

    /** Copies all the edges associated with <code>node</code> in
     * <code>this</code> set of edges to <code>es2</code>. It is
     * implicitly assumed that there is no edge from <code>node</code>
     * in <code>es2</code> (otherwise, those edges are lost) */
    public void copyEdges(PANode node,PAEdgeSet es2){
	if(nodes.containsKey(node)){
	    Relation rel = (Relation) nodes.get(node);
	    es2.nodes.put(node,rel);
	}
    }

    /** Private constructor, used only by <code>clone()</code>. */
    private PAEdgeSet(Relation _vars, Hashtable _nodes) {
	vars  = _vars;
	nodes = _nodes;
    }

    /** <code>clone</code> clones <code>this</code> collection of
     * edges. */	
    public Object clone(){
	Relation new_vars = (Relation) vars.clone();

	Hashtable map_nodes = new Hashtable();
	Iterator it_nodes = nodes.entrySet().iterator();

	while(it_nodes.hasNext()){
	    Map.Entry entry = (Map.Entry) it_nodes.next();
	    Relation rel = (Relation)((Relation)entry.getValue()).clone();
	    map_nodes.put(entry.getKey(),rel);
	}

	return new PAEdgeSet(new_vars, map_nodes);
    }

    /** Pretty-print function for debug purposes.
	<code>edges1.equals(edges2) <==> edges1.toString().equals(edges2.toString()).</code> */
    public String toString(){
	StringBuffer buffer = new StringBuffer();

	buffer.append("{\n");
	buffer.append(vars);
	buffer.append("\n");

	Object[] keys = Debug.sortedSet(nodes.keySet());	
	for(int i = 0 ; i < keys.length ; i++){
	    Object key = keys[i];
	    Relation rel = (Relation) nodes.get(key);
	    buffer.append(" ");
	    buffer.append(key);
	    buffer.append(" -> ");
	    buffer.append(rel);
	}
	
	buffer.append("}\n");
	
	return buffer.toString();
    }
}








