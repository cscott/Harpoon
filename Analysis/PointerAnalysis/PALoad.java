// PALoad.java, created Mon Feb  7 15:24:14 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.Map;


/**
 * <code>PALoad</code> models a LD action.
 * A <code>PALoad</code> object represents the loading of the 
 * outside reference <code>n1--f-->n2</code>.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PALoad.java,v 1.1.2.7 2001-06-17 22:30:43 cananian Exp $
 */
class PALoad implements java.io.Serializable {
    
    /** The start node of the outside edges. */
    PANode n1;
    /** The field that is read. */
    String f;
    /** The end node of the outside edge. */
    PANode n2;
    /** The thread that did it. */
    PANode nt;

    /** Creates a <code>PALoad</code>. */
    public PALoad(PANode n1, String f, PANode n2, PANode nt){
	this.n1 = n1;
	this.f  = f;
	this.n2 = n2;
	this.nt = nt;
    }    

    /* Specializes <code>this</code> <code>PALoad</code> according
       to <code>map</code>, a mapping from <code>PANode<code> to
       <code>PANode</code>. Each node which is not explicitly mapped is
       considered to be mapped to itself. */
    public final PALoad specialize(Map map){
	PANode n1p = PANode.translate(n1, map);
	PANode n2p = PANode.translate(n2, map);
	PANode ntp = PANode.translate(nt, map);
	if((n1p == n1) && (n2p == n2) && (ntp == nt)) return this;
	return new PALoad(n1p, f, n2p, ntp);
    }

    /** Checks the equality of two <code>PALoad</code> objects. */
    public boolean equals(Object o){
	PALoad load2 = (PALoad) o;
	return
	    (n1 == load2.n1)   && (n2 == load2.n2) &&
	    f.equals(load2.f)  && (nt == load2.nt);
    }

    public int hashCode(){
	return n2.hashCode();
    }

    /** Pretty-printer for debug purposes. */
    public String toString(){
	return "< ld , " + n1 + " , " + f + " , " + n2 + 
	    ((nt!=ActionRepository.THIS_THREAD)? (" , " + nt) : "") +
	    " >";
    }

    /** Checks whether <code>this</code> load contains only remaining nodes.
	This method is designed to be used by <code>keepTheEssential</code>.
    	Warning: <code>isGood</code> is NOT the negation of
	<code>isBad</code>. */
    boolean isGood(Set remaining_nodes){
	return
	    remaining_nodes.contains(n1) && remaining_nodes.contains(n2) &&
	    remaining_nodes.contains(nt);
    }

    /** Checks whether <code>this</code> load contains at least one 
	<i>bad node</i>. This method is designed to help us identifying
	the loads that are eliminated when some nodes are removed. */
    boolean isBad(Set bad_nodes){
	return
	    bad_nodes.contains(n1) || bad_nodes.contains(n2) ||
	    bad_nodes.contains(nt);
    }

}

