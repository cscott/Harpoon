// Web.java, created Fri Nov  5 15:05:24 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

/**
 * <code>Web</code> is a helper class used in Register Allocation.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: Web.java,v 1.4 2002-04-10 02:59:47 cananian Exp $
 */
class Web extends harpoon.Analysis.GraphColoring.SparseNode {
    Temp var;

    /** Set of <code>Properties.UseDefable</code>s which use or define
	<code>this.var</code>. 
    */
    HashSet refs;

    // We may want to consider making some sort of WebFactory so that
    // we can store a shared Reference->Web map.  But for now we'll
    // force developers to construct such a map on their own.

    static int counter=1;
    int id;

    Web(Temp var) {
	assert var != null;
	this.var = var;
	refs = new HashSet();
	id = counter;
	counter++;
    }
    
    Web(Temp var, Set refSet) {
	this(var);
	refs.addAll(refSet);
    }
    
    public boolean equals(Object o) {
	try {
	    Web w = (Web) o;
	    return w.var.equals(this.var) &&
		w.refs.equals(this.refs);
	} catch (ClassCastException e) {
	    return false;
	}
    }
    
    public int hashCode() {
	// reusing Temp's hash; we shouldn't be using both Webs and
	// Temps as keys in the same table anyway.
	return var.hashCode();
    }
    
    public String toString() {
	String ids = "";
	Iterator iter = refs.iterator();
	while(iter.hasNext()) {
	    ids += ((HCodeElement)iter.next()).getID();
	    if (iter.hasNext()) ids+=", ";
	}
	return "Web[id: "+id+", Var: " + var + ", Refs: {"+ ids +"} ]";
    }
}

