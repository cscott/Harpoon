// PALoad.java, created Mon Feb  7 15:24:14 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Set;

/**
 * <code>PALoad</code> models a LD action.
 * A <code>PALoad</code> object represents the loading of the 
 * outside reference <code>n1--f-->n2</code>.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PALoad.java,v 1.1.2.2 2000-02-24 22:34:02 salcianu Exp $
 */
class PALoad {
    
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

    /** Checks whether <code>this</code> load contains at least one 
	<i>bad node</i>. This method is designed to help us identifying
	the loads that are eliminated when some nodes are removed. */
    public boolean isBad(Set bad_nodes){
	return
	    bad_nodes.contains(n1) || bad_nodes.contains(n2) ||
	    bad_nodes.contains(nt);
    }

}

