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
 * @version $Id: PAEdgeSet.java,v 1.1.2.14 2000-04-02 19:47:59 salcianu Exp $
 */
public class PAEdgeSet {

    // There are two kinds of edges: variable --> node and 
    // node --f--> node where f is a field. This is reflected in
    // the two different structures we use to store the edges.

    // mapping variable -> set of pointed node_edges
    private Relation var_edges;

    // mapping node -> a <code>Relation</code> flag -> 
    // set of pointed node_edges
    private Hashtable node_edges;

    /** Creates a <code>PAEdgesSet</code>. */
    public PAEdgeSet() {
	var_edges  = new Relation();
	node_edges = new Hashtable();
    }

    // OPERATIONS ON THE SET OF EDGES


    // A. Operations on edges starting from variables.

    /** Returns the set of all the node_edges pointed to by the variable
     * <code>v</code>, i.e. all the node_edges <code>n</code> such that 
     * <code>&lt;v,n&gt;</code> exists. */
    public Set pointedNodes(Temp v){
	return var_edges.getValuesSet(v);
    }

    /** Adds the edges <code>&lt;v,n&gt;</code> where <code>n</code> ranges
     * over the set <code>pointed_nodes</code>. This function is more
     * efficient than just iterating over the set and calling 
     * <code>addEdge</code> for each element. */
    public void addEdges(Temp v, Set pointed_nodes){
	var_edges.addAll(v,pointed_nodes);
    }
    
    /** Adds the edge <code>&lt;v,n&gt;</code>. */
    public void addEdge(Temp v,PANode n){
	var_edges.add(v,n);
    }

    /** Removes all the edges from the variable <code>v</code>. */
    public void removeEdges(Temp v){
	var_edges.removeAll(v);
    }


    // B. Operations on edges starting from nodes.

    /** Returns all the nodes pointed to by the node <code>n</code> through
     *  the field <code>f</code>; <i>i.e.</i> all the nodes <code>n1</code>
     *  such that &lt;&lt;n,f&gt;,n1&gt; exists. */
    public Set pointedNodes(PANode n, String f){
	Relation rel = (Relation) node_edges.get(n);
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
	Relation rel = (Relation)node_edges.get(n);
	if(rel==null) 
	    node_edges.put(n,rel=new Relation());
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
    public boolean addEdge(PANode n, String f, PANode n1){
	Relation rel = (Relation)node_edges.get(n);
	if(rel==null) 
	    node_edges.put(n,rel=new Relation());
	return rel.add(f,n1);
    }

    /** Removes the edge <code>&lt;&lt;n,f&gt;,n1&gt;</code>. */
    public void removeEdge(PANode n, String f, PANode n1){
	Relation rel = (Relation) node_edges.get(n);
	if(rel == null) return;
	rel.remove(f,n1);
	if(rel.isEmpty()) node_edges.remove(n);
    }

    /** Deletes all the edges of the type 
	<code>&lt;&lt;n,f&gt;,n1&gt;</code>. */
    public void removeEdges(PANode n, String f){
	Relation rel = (Relation)node_edges.get(n);
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
	return var_edges.keys();
    }

    /** Returns an <code>Enumeration</code> of the set of all the \
	source nodes that are mentioned in <code>this</code> set of edges.
	That is, the set of all <code>n</code> such that there is at least one 
	<code>&lt;&lt;n,f&gt;,n1&gt;</code> edge. */
    public Enumeration allSourceNodes(){
	return node_edges.keys();
    }

    // Enumeration over an empty set; ugly, let me know if you know
    // something better */ // TODO
    static final private Enumeration EMPTY_ENUM = (new Hashtable()).keys();

    /** Returns an <code>Enumeration</code> of all the flags from a
     * specific node n, i.e. <code>f</code> such that we have at least one 
     * &lt;&lt;n,f&gt;,n1&gt; edge */
    public Enumeration allFlagsForNode(PANode n){
	Relation rel = (Relation)node_edges.get(n);
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


    /** Returns the set of field <code>f</code> edges that originate in
	node <code>node</code>; <code>Set</code> version. */
    public final Set getEdgesFromSet(PANode node, String f){
	Set set = new HashSet();
	for(Iterator it = pointedNodes(node,f).iterator(); it.hasNext();){
	    PANode node2 = (PANode) it.next();
	    set.add(new PAEdge(node, f, node2));
	}
	return set;
    }

    /** Returns the set of field <code>f</code> edges that originate in
	node <code>node</code>; <code>Iterator</code> version. */
    public final Iterator getEdgesFrom(PANode node, String f){
	return getEdgesFromSet(node, f).iterator();
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
	Enumeration enum_var_edges = allVariables();
	while(enum_var_edges.hasMoreElements())
	    forAllPointedNodes((Temp) enum_var_edges.nextElement(),visitor);

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
    public void forAllEdges(PANode node, PAEdgeVisitor visitor){
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
	Enumeration enum_var_edges = allVariables();
	while(enum_var_edges.hasMoreElements())
	    forAllEdges((Temp)enum_var_edges.nextElement(),visitor);

	Enumeration enum_node_edges = allSourceNodes();
	while(enum_node_edges.hasMoreElements())
	    forAllEdges((PANode)enum_node_edges.nextElement(),visitor);
    }


    /* Specializes <code>this</code> set of edges according to
       <code>map</code>, a mapping from <code>PANode<code> to
       <code>PANode</code>. Each node which is not explicitly mapped is
       considered to be mapped to itself. */
    public PAEdgeSet specialize(final Map map){
	final PAEdgeSet es2 = new PAEdgeSet();

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
	Enumeration enum_node_edges = node_edges.keys();
	while(enum_node_edges.hasMoreElements()){
	    PANode node  = (PANode) enum_node_edges.nextElement();
	    Relation rel = (Relation) node_edges.get(node);
	    
	    Enumeration enum_flags = rel.keys();
	    while(enum_flags.hasMoreElements()){
		String f = (String) enum_flags.nextElement();
		rel.remove(f,set);
	    }

	    if(rel.isEmpty())
		node_edges.remove(node);
	}
    }


    /** <code>union</code> computes the union of two sets of edges
     * (in a control-flow join point, for example). */
    public void union(PAEdgeSet edges2){
	// union of the edges from the variables
	var_edges.union(edges2.var_edges);

	// union of the edges from the nodes
	Enumeration en = edges2.allSourceNodes();
	while(en.hasMoreElements()){
	    PANode n = (PANode)en.nextElement();
	    Relation rel = (Relation)node_edges.get(n);
	    if(rel==null){
		rel = new Relation();
		node_edges.put(n,rel);
	    }
	    rel.union((Relation)edges2.node_edges.get(n));
	}
    }


    /** Checks the equality of two <code>PAEdgeSet</code>s. */
    public boolean equals(Object o){
	if(o==null) return false;
	PAEdgeSet es2 = (PAEdgeSet) o;
	if(!var_edges.equals(es2.var_edges)){
	    //System.out.println("different var_edges");
	    return false;
	}
	if(!node_edges.keySet().equals(es2.node_edges.keySet())){
	    //System.out.println("different keySet's");
	    return false;
	}
	Enumeration enum = node_edges.keys();
	while(enum.hasMoreElements()){
	    PANode node = (PANode)enum.nextElement();
	    Relation rel1 = (Relation)node_edges.get(node);
	    Relation rel2 = (Relation)es2.node_edges.get(node);
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
	if(node_edges.containsKey(node)){
	    Relation rel = (Relation) node_edges.get(node);
	    es2.node_edges.put(node,rel);
	}
    }

    /** Private constructor, used only by <code>clone()</code>. */
    private PAEdgeSet(Relation _var_edges, Hashtable _node_edges) {
	var_edges  = _var_edges;
	node_edges = _node_edges;
    }

    /** <code>clone</code> clones <code>this</code> collection of
     * edges. */	
    public Object clone(){
	Relation new_var_edges = (Relation) var_edges.clone();

	Hashtable map_node_edges = new Hashtable();
	Iterator it_node_edges = node_edges.entrySet().iterator();

	while(it_node_edges.hasNext()){
	    Map.Entry entry = (Map.Entry) it_node_edges.next();
	    Relation rel = (Relation)((Relation)entry.getValue()).clone();
	    map_node_edges.put(entry.getKey(),rel);
	}

	return new PAEdgeSet(new_var_edges, map_node_edges);
    }

    /** Pretty-print function for debug purposes.
	<code>edges1.equals(edges2) <==> edges1.toString().equals(edges2.toString()).</code> */
    public String toString(){
	StringBuffer buffer = new StringBuffer();

	buffer.append("{\n");
	buffer.append(var_edges);
	buffer.append("\n");

	Object[] keys = Debug.sortedSet(node_edges.keySet());	
	for(int i = 0 ; i < keys.length ; i++){
	    Object key = keys[i];
	    Relation rel = (Relation) node_edges.get(key);
	    buffer.append(" ");
	    buffer.append(key);
	    buffer.append(" -> ");
	    buffer.append(rel);
	}
	
	buffer.append("}\n");
	
	return buffer.toString();
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

    /** Computes the difference bvetween two sets of edges. Returns the 
	set of edges which are present in es1 but not in es2. The returned
	set contains elements of type <code>PASimpleEdge</code> or
	<code>PAComplexEdge</code>. For debug purposes. */
    public static Set difference(final PAEdgeSet es1,final PAEdgeSet es2){
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
