// PAEdge.java, created Wed Feb 16 15:02:44 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * <code>PAEdge</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PAEdge.java,v 1.1.2.6 2000-03-03 06:23:15 salcianu Exp $
 */
public class PAEdge {

    PANode n1;
    String f;
    PANode n2;
    
    /** Creates a <code>PAEdge</code>. */
    public PAEdge(PANode n1, String f, PANode n2) {
        this.n1 = n1;
	this.f  = f;
	this.n2 = n2;
    }


    public Set project(Relation mu){
	Set edges = new HashSet();
	Iterator it_n1 = mu.getValues(n1);
	while(it_n1.hasNext()){
	    PANode new_n1 = (PANode) it_n1.next();
	    Iterator it_n2 = mu.getValues(n2);
	    while(it_n2.hasNext()){
		PANode new_n2 = (PANode) it_n2.next();
		edges.add(new PAEdge(new_n1, f, new_n2));
	    }
	}
	return edges;
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





