// LightPAEdgeSet.java, created Fri Jun 30 19:32:01 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.LightMap;
import harpoon.Util.PredicateWrapper;

import harpoon.Util.Util;

import harpoon.Temp.Temp;


/**
 * <code>LightPAEdgeSet</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: LightPAEdgeSet.java,v 1.3.2.1 2002-02-27 08:32:05 cananian Exp $
 */
public class LightPAEdgeSet extends AbstrPAEdgeSet
    implements java.io.Serializable {
    
    private Relation var_edges;
    private Map node_edges;
    

    /** Creates a <code>LightPAEdgeSet</code>. */
    public LightPAEdgeSet() {
        var_edges  = new LightRelation();
	node_edges = new LightMap();
    }


    public void addEdge(Temp v, PANode node) {
	var_edges.add(v, node);
    }


    public void addEdges(Temp v, Collection nodes) {
	var_edges.addAll(v, nodes);
    }


    public void removeEdge(Temp v, PANode node) {
	var_edges.remove(v, node);
    }


    public void removeEdges(Temp v) {
	var_edges.removeKey(v);
    }


    public Set pointedNodes(Temp v) {
	Set retval = var_edges.getValues(v);
	if(retval == null)
	    retval = Collections.EMPTY_SET;
	return retval;
    }


    public Set allVariables() {
	return var_edges.keys();
    }


    public void addEdge(PANode node1, String f, PANode node2) {
	Relation rel = (Relation) node_edges.get(node1);
	if(rel == null)
	    node_edges.put(node1, rel = new LightRelation());
	rel.add(f, node2);
    }


    public void addEdges(PANode node1, String f, Collection node2s) {
	if(node2s.isEmpty()) return;
	Relation rel = (Relation) node_edges.get(node1);
	if(rel == null)
	    node_edges.put(node1, rel = new LightRelation());
	rel.addAll(f, node2s);
    }


    public void removeEdge(PANode node1, String f, PANode node2) {
	Relation rel = (Relation) node_edges.get(node1);
	if(rel != null)
	    rel.remove(f, node2);
    }

    
    public void removeEdges(PANode node, String f) {
	Relation rel = (Relation) node_edges.get(node);
	if(rel != null)
	    rel.removeKey(f);
    }


    public void removeEdges(PANode node1) {
	node_edges.remove(node1);
    }


    public Set pointedNodes(PANode node, String f) {
	Relation rel = (Relation) node_edges.get(node);
	if(rel == null)
	    return Collections.EMPTY_SET;
	Set retval = rel.getValues(f);
	if(retval == null)
	    return Collections.EMPTY_SET;
	return retval;
    }


    public Set pointedNodes(PANode node) {
	Relation rel = (Relation) node_edges.get(node);
	if(rel == null)
	    return Collections.EMPTY_SET;
	return rel.values();
    }


    public Set allFlagsForNode(PANode node) {
	Relation rel = (Relation) node_edges.get(node);
	if(rel == null)
	    return Collections.EMPTY_SET;
	return rel.keys();
    }


    public Set allSourceNodes() {
	return node_edges.keySet();
    }
    

    public void forAllPointedNodes(PANode node, PANodeVisitor visitor) {
	Relation rel = (Relation) node_edges.get(node);
	if(rel == null) return;

	for(Iterator itf = rel.keys().iterator(); itf.hasNext(); ) {
	    Set nodes = rel.getValues((String) itf.next());
	    for(Iterator itn = nodes.iterator(); itn.hasNext(); )
		visitor.visit((PANode) itn.next());
	}
    }


    public void forAllEdges(PANode node, PAEdgeVisitor visitor) {
	Relation rel = (Relation) node_edges.get(node);
	if(rel == null) return;

	for(Iterator itf = rel.keys().iterator(); itf.hasNext(); ) {
	    String f = (String) itf.next();
	    Set nodes = rel.getValues(f);
	    for(Iterator itn = nodes.iterator(); itn.hasNext(); )
		visitor.visit(node, f, (PANode) itn.next());
	}
    }


    protected PAEdgeSet getEmptyPAEdgeSet() {
	return new LightPAEdgeSet();
    }


    public void copyEdges(PANode node, PAEdgeSet dest_es) {
	// for efficiency reasons, treat only the homogeneous case
	assert dest_es instanceof LightPAEdgeSet;
	LightPAEdgeSet es2 = (LightPAEdgeSet) dest_es;
	if(node_edges.containsKey(node)){
	    LightRelation rel = (LightRelation) node_edges.get(node);
	    es2.node_edges.put(node, rel);
	}
    }


    public void remove(final Set set) {
	// 1. remove the var edges terminated into a node from set
	PredicateWrapper predicate = new PredicateWrapper() {
		public boolean check(Object o) {
		    return set.contains(o);
		}
	    };
	var_edges.removeValues(predicate);

	// 2. remove the node edges involving at least one node from set
	// 2.1 remove the edges starting from a node from set
	for(Iterator it = set.iterator(); it.hasNext(); )
	    removeEdges((PANode) it.next());
	// 2.2 remove the edges ending in a node from set
	for(Iterator it = node_edges.keySet().iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    Relation rel = (Relation) node_edges.get(node);
	    rel.removeValues(predicate);
	    if(rel.isEmpty())
		node_edges.remove(node);
	}
    }


    public void union(PAEdgeSet edges2) {
	// for efficiency reasons, treat only the homogeneous case
	if(!(edges2 instanceof LightPAEdgeSet))
	    throw new UnsupportedOperationException();

	LightPAEdgeSet les2 = (LightPAEdgeSet) edges2;

	// union of the var edges
	var_edges.union(les2.var_edges);

	// union of the node edges
	for(Iterator it = les2.node_edges.keySet().iterator(); it.hasNext(); ){
	    PANode node = (PANode) it.next();
	    LightRelation rel2 = (LightRelation) les2.node_edges.get(node);
	    LightRelation rel1 = (LightRelation) node_edges.get(node);
	    if(rel1 == null)
		node_edges.put(node, (LightRelation) rel2.clone());
	    else
		rel1.union(rel2);
	}
    }


    public boolean equals(Object o) {
	if((o == null) || !(o instanceof PAEdgeSet))
	    return false;

	// for efficiency reasons, treat only the homogeneous case
	if(!(o instanceof LightPAEdgeSet))
	    throw new UnsupportedOperationException();

	LightPAEdgeSet les2 = (LightPAEdgeSet) o;
	
	return
	    var_edges.equals(les2.var_edges) && 
	    node_edges.equals(les2.node_edges);
    }


    public int hashCode() {
	return var_edges.hashCode() + node_edges.hashCode();
    }


    public Object clone() {
	LightPAEdgeSet newes = (LightPAEdgeSet) super.clone();
	
	newes.var_edges = 
	    (LightRelation) var_edges.clone();
	
	LightMap new_nes = (LightMap) ((LightMap) node_edges).clone();
	for(Iterator it = new_nes.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) it.next();
	    LightRelation rel = 
		(LightRelation) ((Relation) entry.getValue()).clone();
	    entry.setValue(rel);
	}
	newes.node_edges = new_nes;
	
	return newes;
    }

}
