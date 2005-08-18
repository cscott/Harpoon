// MapRelPAEdgeSet.java, created Mon Jun 27 15:06:39 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Collections;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.AbstractList;
import java.util.Iterator;


import harpoon.ClassFile.HField;

import jpaul.DataStructs.Factory;
import jpaul.DataStructs.Relation;
import jpaul.DataStructs.CompoundIterable;
import jpaul.DataStructs.DSUtil;
import jpaul.Misc.Function;

import jpaul.Graphs.ForwardNavigator;
import jpaul.Graphs.DiGraph;


/**
 * <code>MapRelPAEdgeSet</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: MapRelPAEdgeSet.java,v 1.2 2005-08-18 21:35:36 salcianu Exp $
 */
public class MapRelPAEdgeSet extends PAEdgeSet {

    /** @param mapFact Factory that generates the underlying map from
	nodes to relations between fields and nodes.
	@param relFact Factory that generates the underlying relations.
    */
    public MapRelPAEdgeSet(Factory<Map<PANode,Relation<HField,PANode>>> mapFact,
			   Factory<Relation<HField,PANode>> relFact) {
	this.mapFact  = mapFact;
	this.relFact  = relFact;
	this.node2env = mapFact.create();
    }

    private final Factory<Map<PANode,Relation<HField,PANode>>> mapFact;
    private final Factory<Relation<HField,PANode>> relFact;


    private Map<PANode,Relation<HField,PANode>> node2env;

    private Relation<HField,PANode> getNodeEnv(PANode n) {
	Relation<HField,PANode> env = node2env.get(n);
	if(env == null) {
	    env = relFact.create();
	    node2env.put(n, env);
	}
	return env;
    }


    boolean addEdge(PANode n1, HField hf, PANode n2, boolean addToIMM) {
	// Unless otherwise specified, refuse to add edges starting
	// from an IMM or CONST node.
	if(!addToIMM) {
	    switch(n1.kind) {
	    case IMM:
	    case CONST:
	    case NULL:
		return false;
	    default: // continue;
	    }
	}
	if(!TypeFilter.mayHaveField(n1, hf)) return false;
	if(!TypeFilter.mayPointTo(hf, n2)) return false;
	Relation<HField,PANode> env = getNodeEnv(n1);
	return env.add(hf, n2);
    }


    protected boolean uncheckedAddEdge(PANode n1, HField hf, PANode n2) {
	Relation<HField,PANode> env = getNodeEnv(n1);
	return env.add(hf, n2);
    }


    boolean addEdges(PANode n1, HField hf, Collection<PANode> n2s, boolean addToIMM) {
	// Unless otherwise specified, refuse to add edges starting
	// from an IMM or CONST node.
	if(!addToIMM) {
	    switch(n1.kind) {
	    case IMM:
	    case CONST:
	    case NULL:
		return false;
	    default: // continue;
	    }
	}
	if(n2s.isEmpty()) return false;
	if(!TypeFilter.mayHaveField(n1, hf)) return false;	

	Relation<HField,PANode> env = getNodeEnv(n1);
	boolean changed = false;
	for(PANode n2 : n2s) {
	    if(TypeFilter.mayPointTo(hf, n2)) {
		if(env.add(hf, n2)) {
		    changed = true;
		}
	    }
	}
	return changed;
	//return env.addAll(hf, n2s);
    }

    
    public Collection<PANode> pointedNodes(PANode n) {
	Relation<HField,PANode> env = node2env.get(n);
	if(env == null) {
	    return Collections.<PANode>emptySet();
	}
	else {
	    return DSUtil.iterable2coll(env.values());
	}
    }


    public Collection<PANode> pointedNodes(PANode n, HField hf) {
	Relation<HField,PANode> env = node2env.get(n);
	if(env == null) {
	    return Collections.<PANode>emptySet();
	}
	else {
	    return env.getValues(hf);
	}
    }


    public Collection<PANode> sources() {
	return Collections.unmodifiableCollection(node2env.keySet());
    }


    public Iterable<PANode> allNodes() {
	return 
	    DSUtil.<PANode>unionIterable
	    (sources(),
	     // ... and edge targets
	     new CompoundIterable<PANode,PANode>
	     (sources(),
	      new Function<PANode,Iterable<PANode>>() {
		 public Iterable<PANode> f(PANode node) {
		     return node2env.get(node).values();
		 }
	     }));
    }

    
    public Collection<HField> fields(PANode n) {
	Relation<HField,PANode> env = node2env.get(n);
	if(env == null) {
	    return Collections.<HField>emptySet();
	}
	return Collections.unmodifiableCollection(env.keys());
    }


    public boolean join(PAEdgeSet es2) {
	boolean modified = false;
	if(es2 instanceof MapRelPAEdgeSet) {
	    return optimizedJoin((MapRelPAEdgeSet) es2);
	}
	// general, inneficient case
	for(PANode n1 : es2.sources()) {
	    for(HField hf : es2.fields(n1)) {
		if(this.addEdges(n1, hf, es2.pointedNodes(n1, hf), true)) {
		    modified = true;
		}
	    }
	}
	return modified;
    }


    private boolean optimizedJoin(MapRelPAEdgeSet es2) {
	boolean modified = false;
	for(Map.Entry<PANode,Relation<HField,PANode>> entry : es2.node2env.entrySet()) {
	    PANode node = entry.getKey();
	    Relation<HField,PANode> env2 = entry.getValue();
	    Relation<HField,PANode> env = node2env.get(node);
	    if(env == null) {
		node2env.put(node, env2);
		modified = true;
	    }
	    else {
		if(env.union(env2)) {
		    modified = true;
		}
	    }
	}
	return modified;
    }


    /** Creates a new, independent set of edges (independent = the
	operations on the new relation won't affect the old one). */
    public Object clone() {
	MapRelPAEdgeSet newES = (MapRelPAEdgeSet) super.clone();

	// first, do a shallow copy of the node2env ...
	newES.node2env = mapFact.create(this.node2env);

	// next, go over the map entries and do a deep copy of the values.
	for(Map.Entry<PANode, Relation<HField,PANode>> entry : newES.node2env.entrySet()) {
	    Relation<HField,PANode> env = entry.getValue();
	    entry.setValue(relFact.create(env));
	}

	return newES;
    }

    public ForwardNavigator<PANode> getForwardNavigator() {
	return new ForwardNavigator<PANode>() {
	    public List<PANode> next(PANode node) {
		Relation<HField,PANode> nodeEnv = node2env.get(node);
		final Iterable<PANode> succs = 
		    (nodeEnv != null) ? nodeEnv.values() : (Iterable<PANode>) Collections.<PANode>emptySet();
		return new AbstractList<PANode>() {
		    public Iterator<PANode> iterator() { return succs.iterator(); }
		    public int size() { return DSUtil.iterableSize(succs); }
		    public PANode get(int index) { throw new UnsupportedOperationException(); }
		};
	    }
	};
    }


    public void print(PrintWriter pw, String indentStr) {
	for(PANode node : sources()) {
	    pw.print("\n");
	    pw.print(indentStr);
	    pw.print(node);
	    pw.print(" --> ");
	    Relation<HField,PANode> rel = map.get(node);
	    pw.println(rel);
	}
	pw.flush();
    }

}
