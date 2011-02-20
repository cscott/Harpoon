// EdgeOrdering.java, created Wed Feb 16 14:50:33 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Iterator;
import java.util.Enumeration;

import harpoon.Temp.Temp;

import harpoon.Util.PredicateWrapper;
import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.RelationEntryVisitor;


/**
 * <code>EdgeOrdering</code> models the ordering relation between the 
 inside and the outside edges belonging to the same analysis scope.<br>

 This relation records facts like this: <i>the outside edge <code>eo</code>
 could have been read after the inside edge <code>ei</code> was created.</i>
 This information is used in the inter-thread analysis, when outside edges
 are matched not only against inside edges from the opposite scope but even
 against inside edges from their own scope. Of course, only edges with the
 same field can match, so we are interested in the ordering relation only
 between such edges.<br>

 Although the actual implementation is fully functional, it is not 
 a very performant one. My main concern was to make the algorithm work
 correctly; speed was only a second issue.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: EdgeOrdering.java,v 1.2 2002-02-25 20:58:39 cananian Exp $
 */
public class EdgeOrdering implements java.io.Serializable {

    // the relation behind the implementation: if <eo,ei> appears in this
    // relation, the outside edge eo could have been read after the inside
    // edge ei was created.
    // So, outside edges are the keys and inside edges are the values
    // of this relation.
    private Relation after;

    /** Creates a <code>EdgeOrdering</code> object. */
    public EdgeOrdering(){
	after = new LightRelation();
    }

    /** Adds a piece of ordering information. More specifically, it records
	the following information: <i>the outside edge <code>eo</code>
	could have been read after the inside edge <code>ei</code> was
	created</i>. */
    public boolean add(PAEdge eo, PAEdge ei){
	if(!eo.f.equals(ei.f)) return false;
	return after.add(eo,ei);
    }

    /** Returns an iterator over the set of the inside edges that could
	be already created when the inside edge <code>eo</code> is read.
	This method will be used in the intre-thread analysis when trying to
	match an outside edge against those inside edges from the same scope
	that could be created when the load is done. */
    public Iterator getBeforeEdges(PAEdge eo){
	return after.getValues(eo).iterator();
    }

    /** Checks whether the inside edge <code>ei</code> could have been
	created before the outside edge <code>eo</code> is read. */
    public boolean wasBefore(PAEdge ei, PAEdge eo){
	return after.contains(eo,ei);
    }

    /** Records the fact that all the outside edges from the set 
	<code>outside_edges</code> are created after all the inside
	edges from I. We are interested only in the relation between
	edges with the same field <code>f</code>.<br>
	<b>Parameters:</b> <code>outside_edge</code> must be a set
	of <code>PAEdge</code>s, all with the same field <code>f</code>.
	<b>Result:</b> it returns <code>true</code> if the edge ordering
	relation was changed by this addition. */
    private boolean add(final Set outside_edges, PAEdgeSet I, String f){
	boolean modified = false;

	for(Iterator it1 = I.allSourceNodes().iterator(); it1.hasNext(); ) {
	    PANode node1 = (PANode) it1.next();
	    for(Iterator it2 = I.pointedNodes(node1, f).iterator();
		it2.hasNext(); ) {
		PANode node2 = (PANode) it2.next();
		PAEdge ei = new PAEdge(node1, f, node2);
		Iterator it_eo = outside_edges.iterator();
		while(it_eo.hasNext()){
		    PAEdge eo = (PAEdge) it_eo.next();
		    if(add(eo,ei)) modified = true;
		}
	    }
	}

	return modified;
    }


    /** Records the fact that the new outside edges
	<code>&lt;node1,f,node2&gt;</code>
	(forall <code>node1</code> in <code>nodes1</code>) are created after
	all the inside edges from <code>I</code>.
	Returns <code>true</code> if he edge ordering relation was changed by
	this addition. */
    public boolean add(Set nodes1, String f, PANode node2, PAEdgeSet I){
	Set outside_edges = new HashSet();
	Iterator it_nodes1 = nodes1.iterator();
	while(it_nodes1.hasNext()){
	    PANode node1 = (PANode) it_nodes1.next();
	    outside_edges.add(new PAEdge(node1,f,node2));
	}
	return add(outside_edges,I,f);
    }


    /** <code>join</code> is called in the control-flow join points. */
    public void join(EdgeOrdering eo2){
	after.union(eo2.after);
    }


    /** Visits all the entry of <code>this</code> edge ordering relation.
	In order not to add one more interface, a simple 
	<code>RelationEntryVisitor</code> interface is used for the type
	of the argument <code>visitor</code>.
	For each outside edge <code>eo</code> and for each inside edge
	<code>ei</code> such that the information <i>eo can be read
	after ei was created</i> is recorded into <code>this</code> object,
	<code>visitor.visit(eo,ei)</code> is called. */
    public void forAllEntries(RelationEntryVisitor visitor){
	after.forAllEntries(visitor);
    }

    /** Removes all the information related to edges containing nodes from
	<code>nodes</code>. */
    public void removeNodes(final Set nodes){
	after.removeObjects(new PredicateWrapper(){
		public boolean check(Object obj){
		    return ((PAEdge) obj).isBad(nodes);
		}
	    });
    }

    /** Removes all the information related to edges from <code>edges</code>.
     <code>edges</code> must be a set of <code>PAEdge</code>s. */
    public void removeEdges(final Set edges){
	after.removeObjects(new PredicateWrapper(){
		public boolean check(Object obj){
		    return edges.contains((PAEdge) obj);
		}
	    });
    }

    // Private constructor for clone and keepTheEssential.
    private EdgeOrdering(Relation after){
	this.after = after;
    }


    /** Clone this object. */
    public Object clone(){
	return new EdgeOrdering((Relation)after.clone());
    }


    /** Returns a new relation containing information only about the ordering
	of edges between nodes from <code>remaining_nodes</code>. */
    public EdgeOrdering keepTheEssential(final Set remaining_nodes){
	final Relation essence = new LightRelation();
	after.forAllEntries(new RelationEntryVisitor(){
		public void visit(Object o1, Object o2){
		    PAEdge eo = (PAEdge) o1;
		    PAEdge ei = (PAEdge) o2;
		    if(ei.isGood(remaining_nodes) && 
		       eo.isGood(remaining_nodes))
			essence.add(eo,ei);
		}
	    });
	return new EdgeOrdering(essence);
    }


    public static void insertProjection(final EdgeOrdering eo_source,
					final EdgeOrdering eo_dest,
					final Relation mu){

	eo_source.forAllEntries(new RelationEntryVisitor(){
		public void visit(Object key, Object value){
		    PAEdge eo = (PAEdge) key;
		    PAEdge ei = (PAEdge) value;

		    Set eo_set = eo.project(mu);		    
		    Set ei_set = ei.project(mu);

		    if(ei_set.isEmpty() || eo_set.isEmpty()) return;

		    Iterator it_eo = eo_set.iterator();
		    while(it_eo.hasNext()){
			PAEdge new_eo = (PAEdge) it_eo.next();
			Iterator it_ei = ei_set.iterator();
			while(it_ei.hasNext()){
			    PAEdge new_ei = (PAEdge) it_ei.next();
			    eo_dest.add(new_eo,new_ei);
			}
		    }
		}
	    });
    }

    /* Specializes <code>this</code> edge ordering by mapping 
       the nodes appearing in the edges according to <code>map</code>.
       The nodes which are not explicitly mapped, are considered 
       to be mapped to themselves (identity mapping). */
    public EdgeOrdering specialize(final Map map){
	final EdgeOrdering eord2 = new EdgeOrdering();

	forAllEntries(new RelationEntryVisitor(){
		public void visit(Object key, Object value){
		    PAEdge eo = (PAEdge) key;
		    PAEdge ei = (PAEdge) value;
		    PAEdge eo2 = eo.specialize(map);
		    PAEdge ei2 = ei.specialize(map);
		    eord2.add(eo2,ei2);
		}
	    });

	return eord2;
    }

    /** Checks the equality of two <code>NodeOrdering</code> objects. */
    public boolean equals(Object o2){
	EdgeOrdering no2 = (EdgeOrdering) o2;
	if(this==no2) return true;
	return this.after.equals(no2.after);
    }

    /** String representation for debug purposes. */
    public String toString(){
	final StringBuffer buffer = new StringBuffer();
	buffer.append(" Edge ordering:\n");
	after.forAllEntries(new RelationEntryVisitor(){
		public void visit(Object o1, Object o2){
		    PAEdge eo = (PAEdge) o1;
		    PAEdge ei = (PAEdge) o2;
		    buffer.append("  ");
		    buffer.append(eo.toString());
		    buffer.append(" after ");
		    buffer.append(ei.toString());
		    buffer.append("\n");
		}
	    });
	return buffer.toString();
    }

}


