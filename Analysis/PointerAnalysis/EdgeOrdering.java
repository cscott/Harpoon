// EdgeOrdering.java, created Wed Feb 16 14:50:33 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import harpoon.Temp.Temp;


/**
 * <code>EdgeOrdering</code> models the oredring relation between the 
 inside and the outside edges belonging to the same analysis scope.<br>
 Although the actual implementation is fully functional, it is not 
 a very performant one. My main concern was to make the algorithm work
 correctly; speed is only a second issue.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: EdgeOrdering.java,v 1.1.2.2 2000-02-21 04:47:59 salcianu Exp $
 */
public class EdgeOrdering{

    // the relation behind the implementation
    private Relation after;

    /** Creates a <code>EdgeOrdering</code> object. */
    public EdgeOrdering(){
	after = new Relation();
    }

    /** Records the ordering relation <i>the inside edge <code>ei</code> was
	created before the outside edges <code>eo</code> was read. */
    public boolean add(PAEdge eo, PAEdge ei){
	if(!eo.f.equals(ei.f)) return false;
	return after.add(eo,ei);
    }

    /** Checks whether the inside edge <code>ei</code> could have been
	created before the outside edge <code>eo</code> is read. */
    public boolean wasBefore(PAEdge ei, PAEdge eo){
	return after.contains(eo,ei);
    }

    // TODO: modify the EdgeOrdering such that only edges with the same
    // field are involved in a relation DONE
    // TODO: put the appropriate comments

    /** Records the fact that all the outside edges from the set 
	outside_edges are created after all the inside edges from I. */
    public boolean add(final Set outside_edges, PAEdgeSet I){
	class Dummy{
	    boolean modified = false;
	};
	final Dummy mod = new Dummy();

	I.forAllEdges(new PAEdgeVisitor(){
		public void visit(Temp var, PANode node){}
		public void visit(PANode node1, String f,PANode node2){
		    Iterator it = outside_edges.iterator();
		    while(it.hasNext()){
			PAEdge eo = (PAEdge) it.next();
			if(!f.equals(eo.f)) continue;
			PAEdge ei = new PAEdge(node1,f,node2);
			if(add(eo,ei)) mod.modified = true;
		    }
		}
	    });
	return mod.modified;
    }


    /** Records the fact that the new outside edges node1,f,node2 (forall
	node1 in nodes1) are created after all the inside edges from I. */
    public boolean add(Set nodes1, String f, PANode node2, PAEdgeSet I){
	Set outside_edges = new HashSet();
	Iterator it_nodes1 = nodes1.iterator();
	while(it_nodes1.hasNext()){
	    PANode node1 = (PANode) it_nodes1.next();
	    outside_edges.add(new PAEdge(node1,f,node2));
	}
	return add(outside_edges,I);
    }


    /** <code>join</code> is called in the control-flow join points. */
    public void join(EdgeOrdering eo2){
	after.union(eo2.after);
    }


    /** Visits all the entry of <code>this</code> edge ordering relation.
	In order not to add one more interface, a simple 
	<code>RelationEntryVisitor</code> interface is used for the type
	of of the argument <code>visitor</code>. */
    public void forAllEntries(RelationEntryVisitor visitor){
	after.forAllEntries(visitor);
    }

    /** Removes all the information related to edges containing nodes from
	<code>nodes</code>. */
    public void removeNodes(Set nodes){
	// TODO: some decent implementation
    }

    /** Removes all the information related to edges from <code>edges</code>.*/
    public void removeEdges(Set edges){
	// TODO: some decent implementation
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
	final Relation essence = new Relation();
	after.forAllEntries(new RelationEntryVisitor(){
		public void visit(Object o1, Object o2){
		    PAEdge eo = (PAEdge) o1;
		    PAEdge ei = (PAEdge) o2;
		    if(good_edge(eo) && good_edge(ei))
			essence.add(eo,ei);
		}
		private boolean good_edge(PAEdge e){
		    return 
			remaining_nodes.contains(e.n1) &&
			remaining_nodes.contains(e.n2);
		}
	    });
	return new EdgeOrdering(essence);
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


