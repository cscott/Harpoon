// EscapeFunc.java, created Sun Jan  9 20:53:09 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Hashtable;
import java.util.HashSet;
import java.util.Set;
import java.util.Collections;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Map;

import harpoon.ClassFile.HCodeElement;

/**
 * <code>EscapeFunc</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAEscapeFunc.java,v 1.1.2.4 2000-01-17 23:49:03 cananian Exp $
 */
public class PAEscapeFunc {

    // rel_n attaches to each node the set of all the nodes
    // that it can escape through.
    Relation rel_n;

    // rel_m attaches to each node the set of all the method
    // invocation sites that it can escape through.
    Relation rel_m;

    /** Creates a <code>EscapeFunc</code>. */
    public PAEscapeFunc() {
        rel_n = new Relation();
        rel_m = new Relation();
    }
    
    /** Records the fact that <code>n</code> can escape through
     *  the node <code>n_hole</code>. Returns <code>true</code>
     *  if new information has been gained */
    public final boolean addNodeHole(PANode n, PANode n_hole){
	return rel_n.add(n,n_hole);
    }

    /** Records the fact that <code>n</code> can escape through
     *  the node <code>n_holes</code>. Returns <code>true</code>
     *  if new information has been gained */
    public final boolean addNodeHoles(PANode n, Set n_holes){
	return rel_n.addAll(n,n_holes);
    }

    /** The dual of <code>addNodeHole</code> */
    public final void removeNodeHole(PANode n, PANode n_hole){
	rel_n.remove(n,n_hole);
    }

    /** Records the fact that <code>n</code> can escape through the
     *  method invocation site <code>m_hole</code>. Returns <code>true</code>
     *  if new information has been gained */
    public final boolean addMethodHole(PANode n, HCodeElement m_hole){
	return rel_m.add(n,m_hole);
    }

    /** Records the fact that <code>n</code> can escape through the
     *  method invocation sites <code>m_holes</code>. Returns <code>true</code>
     *  if new information has been gained */
    public final boolean addMethodHoles(PANode n, Set m_holes){
	return rel_m.addAll(n,m_holes);
    }

    /** The dual of <code>addMethodHole</code> */
    public void removeMethodHole(PANode n, HCodeElement m_hole){
	rel_m.remove(n,m_hole);
    }

    // Check if n has escaped in some "hole" - node accessed
    // by unalyzed code or unanalyzed method invocation site
    public boolean hasEscaped(PANode n){
	Set set_n = rel_n.getValuesSet(n);
	Set set_m = rel_m.getValuesSet(n);

	return !(
		 ((set_n == null) || (set_n.isEmpty())) &&
		 ((set_m == null) || (set_m.isEmpty()))
		);
    }

    // Returns the set of all the node "holes" n can escape through
    public Set nodeHolesSet(PANode n){
	return rel_n.getValuesSet(n);
    }

    // Returns an iterator over the set of the node "holes"
    // n escaped through
    public Iterator nodeHoles(PANode n){
	return rel_n.getValues(n);
    }

    // Returns the set of all the method "holes" n can escape through
    public Set methodHolesSet(PANode n){
	return rel_m.getValuesSet(n);
    }

    // Returns an iterator over the set of the unanalyzed method
    // invocation sites (the method "holes") n escaped through
    public Iterator methodHoles(PANode n){
	return rel_m.getValues(n);
    }


    public void union(PAEscapeFunc e2){
	rel_n.union(e2.rel_n);
	rel_m.union(e2.rel_m);
    }

    /** Checks the equality of two <code>PAEscapeFunc</code> */
    public boolean equals(Object o){
	if(o==null) return false;
	PAEscapeFunc e2 = (PAEscapeFunc)o;
	return rel_n.equals(e2.rel_n) && rel_m.equals(e2.rel_m);
    }

    public Set escapedNodes(){
	HashSet set = new HashSet();
	set.addAll(rel_n.keySet());
	set.addAll(rel_m.keySet());
	return set;
    }

    /** Private constructor used only by <code>clone</code> */
    private PAEscapeFunc(Relation _rel_n,Relation _rel_m){
	rel_n = _rel_n;
	rel_m = _rel_m;
    }

    /** <code>clone</clone> does a deep copy of <code>this</code> object. */
    public Object clone(){
	return
	    new PAEscapeFunc((Relation)(rel_n.clone()),
			     (Relation)(rel_m.clone()));
    }

    // Pretty-print debug function
    public String toString(){
	StringBuffer buffer = new StringBuffer("Escape function: ");

        HashSet set = new HashSet(rel_n.keySet());
        set.addAll(rel_m.keySet());
        Iterator it = set.iterator();

        while(it.hasNext()){
            PANode n = (PANode)it.next();
            buffer.append("\n  " + n + ":  ");
	    
	    Iterator it_n = nodeHoles(n);
	    if(it_n != null){
		while(it_n.hasNext())
		    buffer.append((PANode)it_n.next() + " ");
	    }
	    
	    Iterator it_m = methodHoles(n);
	    if(it_m != null){
		while(it_m.hasNext()){
		    HCodeElement hce = (HCodeElement) it_m.next();
		    buffer.append("M" + hce.getSourceFile() + ":" + 
				  hce.getLineNumber() + " ");
		}
	    }
	}
	
	return buffer.toString();
    }

}



