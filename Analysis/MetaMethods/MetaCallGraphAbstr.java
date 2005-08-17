// MetaCallGraphAbstr.java, created Mon Mar 13 16:03:18 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MetaMethods;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;

import java.io.PrintStream;

import harpoon.IR.Quads.CALL;
import harpoon.ClassFile.HCodeElement;

import jpaul.DataStructs.Relation;
import jpaul.DataStructs.MapSetRelation;

/**
 * <code>MetaCallGraphAbstr</code> Map based abstract
 * <code>MetaCallGraph</code>.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: MetaCallGraphAbstr.java,v 1.8 2005-08-17 23:40:52 salcianu Exp $ */
public abstract class MetaCallGraphAbstr extends MetaCallGraph {
    // Map<MetaMethod,MetaMethod[]>
    protected final Map callees1_cmpct = new HashMap();
    // Map<MetaMethod,Map<CALL,MetaMethod[]>>
    protected final Map callees2_cmpct = new HashMap();

    private final MetaMethod[] empty_array = new MetaMethod[0];

    /** Returns the meta methods that can be called by <code>mm</code>. */
    public MetaMethod[] getCallees(MetaMethod mm) {
	MetaMethod[] retval = (MetaMethod[]) callees1_cmpct.get(mm);
	if(retval == null)
	    retval = empty_array;
	return retval;
    }

    /** Returns the meta methods that can be called by <code>mm</code>
	at the call site <code>q</code>. */
    public MetaMethod[] getCallees(MetaMethod mm, CALL cs) {
	Map map = (Map) callees2_cmpct.get(mm);
	if(map == null)
	    return new MetaMethod[0];
	MetaMethod[] retval = (MetaMethod[]) map.get(cs);
	if(retval == null)
	    retval = empty_array;
	return retval;
    }
 
    /** Returns the set of all the meta methods that might be called,
	directly or indirectly, by meta method <code>mm</code>. */
    public Set getTransCallees(MetaMethod mm) {
	return transitiveSucc(mm);
    }
   

    /** Returns the set of all the call sites in the code of the meta-method
	<code>mm</code>. */
    public Set/*<CALL>*/ getCallSites(MetaMethod mm){
	Map map = (Map) callees2_cmpct.get(mm);
	if(map == null)
	    return Collections.EMPTY_SET;
	return map.keySet();
    }

    // set of all encountered meta methods
    protected final Set all_meta_methods = new HashSet();

    /** Returns the set of all the meta methods that might be called during the
	execution of the program. */
    public Set getAllMetaMethods(){
	return all_meta_methods;
    }

    /** Computes the <i>split</i> relation. This is a <code>Relation</code>
	that associates to each <code>HMethod</code> the set of
	<code>MetaMethod</code>s specialized from it. */
    public Relation getSplitRelation(){
	if(split != null) return split;
	split = new MapSetRelation();
	for(Object mmO : getAllMetaMethods()){
	    MetaMethod mm = (MetaMethod) mmO;
	    split.add(mm.getHMethod(), mm);
	}
	return split;
    }
    // keeps the split relation
    private Relation split = null;


    protected Set run_mms = new HashSet();
    public Set getRunMetaMethods(){
	return run_mms;
    }


    /** Nice pretty-printer for debug purposes. */
    public void print(PrintStream ps, boolean detailed_view, MetaMethod root) {
	for(Object mmO : getAllMetaMethods()) {
	    MetaMethod mm = (MetaMethod) mmO;
	    ps.println();
	    ps.print(mm);
	    if(detailed_view) {
		ps.println();
		for(Object csO : getCallSites(mm)){
		    CALL cs = (CALL) csO;
		    HCodeElement hce = (HCodeElement) cs;
		    MetaMethod[] callees = getCallees(mm,cs);
		    ps.println(" " + hce.getSourceFile() + ":" + 
			       hce.getLineNumber() + " " + cs + " (" +
			       callees.length + " callee(s)):");
		    for(int i = 0; i < callees.length; i++)
			ps.println("  " + callees[i]);
		}
	    }
	    else {
		MetaMethod[] callees = getCallees(mm);
		ps.println(" (" + callees.length + " callee(s)) :");
		for(int i = 0; i < callees.length; i++)
		    ps.println("  " + callees[i]);
	    }
	}
    }
}
