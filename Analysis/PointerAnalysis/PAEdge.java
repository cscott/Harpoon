// PAEdge.java, created Wed Feb 16 15:02:44 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;

/**
 * <code>PAEdge</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PAEdge.java,v 1.1.2.3 2000-02-25 00:49:35 salcianu Exp $
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
	Warning: <code>goodEdge</code> is NOT the negation of
	<code>badEdge</code>. */
    boolean goodEdge(Set remaining_nodes){
	return remaining_nodes.contains(n1) && remaining_nodes.contains(n2);
    }

    /** Checks whether <code>this</code> edge refers to at least one
	node from the set <code>bad_nodes</code>. This is supposed to
	help us to determine which edges must go when we remove some nodes. */
    boolean badEdge(Set bad_nodes){
	return bad_nodes.contains(n1) || bad_nodes.contains(n2);
    }
}
