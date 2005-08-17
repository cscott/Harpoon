// PAEdge.java, created Wed Feb 16 15:02:44 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Iterator;

/**
 * <code>PAEdge</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAEdge.java,v 1.4 2005-08-17 23:34:01 salcianu Exp $
 */
public class PAEdge implements java.io.Serializable {

    PANode n1;
    String f;
    PANode n2;
    
    /** Creates a <code>PAEdge</code>. */
    public PAEdge(PANode n1, String f, PANode n2) {
        this.n1 = n1;
	this.f  = f;
	this.n2 = n2;
    }

    /* Project this edge through the relation <code>mu</code>. */
    public Set project(Relation mu){
	Set edges = new HashSet();
	for(Object new_n1O : mu.getValues(n1)) {
	    PANode new_n1 = (PANode) new_n1O;
	    for(Object new_n2O : mu.getValues(n2)) {
		PANode new_n2 = (PANode) new_n2O;
		edges.add(new PAEdge(new_n1, f, new_n2));
	    }
	}
	return edges;
    }

    /* Specialize <code>this</code> edge by using the mapping
       <code>map</code>. <code>map</code> should be a mappig from
       <code>PANode</code> to <code>PANode</code>.
       Returns an edge obtained from <code>this</code>
       one by translating the two ends of it according to the mapping
       <code>map</code>. Each node that don't appear in <code>map</code>
       is implicitly assumed to be mapped to itself.
       This method is smart enough not to reallocated a new object if 
       none of its ends is mapped to something else; in this case, it returns
       <code>this</code> object. */
    public PAEdge specialize(Map map){
	PANode n1p = (PANode) map.get(n1);
	PANode n2p = (PANode) map.get(n2);
	if(n1p == null){
	    // hack to avoid regenerating the same edge
	    if(n2p == null) return this;
	    // an unmaped nodes is implicitly maped to itself
	    n1p = n1;
	}
	// an unmaped nodes is implicitly maped to itself
	if(n2p == null) n2p = n2;
	return new PAEdge(n1p,f,n2p);
    }

    /** Checks the equality of two edges. */ 
    public boolean equals(Object o){
	PAEdge edge2 = (PAEdge) o;
	return (n1==edge2.n1) && (n2==edge2.n2) && (f.equals(edge2.f));
    }

    /** Computes the hash code of <code>this</code> object. */
    public int hashCode(){
	return n1.hashCode();
    }

    /** String representation for debug purposes. */
    public String toString(){
	return "<" + n1.toString() + "," + f + "," + n2.toString() + ">";
    }

    /** Checks whether <code>this</code> edge refers only to remaining nodes.
	This is supposed to be used by <code>keepTheEssential</code> methods.
	Warning: <code>isGood</code> is NOT the negation of
	<code>isBad</code>. */
    boolean isGood(Set remaining_nodes){
	return remaining_nodes.contains(n1) && remaining_nodes.contains(n2);
    }

    /** Checks whether <code>this</code> edge refers to at least one
	node from the set <code>bad_nodes</code>. This is supposed to
	help us to determine which edges must go when we remove some nodes. */
    boolean isBad(Set bad_nodes){
	return bad_nodes.contains(n1) || bad_nodes.contains(n2);
    }
}





