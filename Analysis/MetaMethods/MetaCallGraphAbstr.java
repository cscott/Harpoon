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

import java.io.PrintWriter;

import harpoon.IR.Quads.CALL;
import harpoon.ClassFile.HCodeElement;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;
import harpoon.Util.DataStructs.RelationEntryVisitor;

/**
 * <code>MetaCallGraphAbstr</code> Abstract implementation of the
 <code>MetaCallGraph</code> interface.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: MetaCallGraphAbstr.java,v 1.3 2002-05-02 22:11:39 salcianu Exp $
 */
public abstract class MetaCallGraphAbstr implements MetaCallGraph {
    // Map<MetaMethod,MetaMethod[]>
    protected final Map callees1_cmpct = new HashMap();
    // Map<MetaMethod,Map<CALL,MetaMethod[]>>
    protected final Map callees2_cmpct = new HashMap();

    private final MetaMethod[] empty_array = new MetaMethod[0];

    /** Returns the meta methods that can be called by <code>mm</code>. */
    public MetaMethod[] getCallees(MetaMethod mm){
	MetaMethod[] retval = (MetaMethod[]) callees1_cmpct.get(mm);
	if(retval == null)
	    retval = empty_array;
	return retval;
    }

    /** Returns the meta methods that can be called by <code>mm</code>
	at the call site <code>q</code>. */
    public MetaMethod[] getCallees(MetaMethod mm, CALL cs){
	Map map = (Map) callees2_cmpct.get(mm);
	if(map == null)
	    return new MetaMethod[0];
	MetaMethod[] retval = (MetaMethod[]) map.get(cs);
	if(retval == null)
	    retval = empty_array;
	return retval;
    }
 
    /** Returns the set of all the meta methods that might be called, directly
	or indirectly, by the meta method <code>mm</code>. */
    public Set getTransCallees(MetaMethod mm){
	Set set = new HashSet();
	LinkedList W = new LinkedList();
	W.add(mm);
	while(!W.isEmpty()){
	    MetaMethod mm_work = (MetaMethod) W.removeFirst();
	    MetaMethod[] callees = getCallees(mm_work);
	    for(int i = 0; i < callees.length; i++)
		if(set.add(callees[i]))
		    W.addLast(callees[i]);
	}
	return set;
    }
   

    /** Returns the set of all the call sites in the code of the meta-method
	<code>mm</code>. */
    public Set getCallSites(MetaMethod mm){
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
	split = new RelationImpl();
	for(Iterator it = getAllMetaMethods().iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
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
    public void print(PrintWriter pw, boolean detailed_view, MetaMethod root){

	Set mms = new HashSet();
	Set roots = new HashSet(getRunMetaMethods());
	roots.add(root);

	for(Iterator it = roots.iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
	    mms.add(mm);
	    mms.addAll(getTransCallees(mm));
	}

	pw.println("MetaCallGraph rooted in " + root);
	pw.println("Roots: ");
	for(Iterator it = roots.iterator(); it.hasNext(); )
	    System.out.println("  " + (MetaMethod) it.next());
	pw.println("===========================================");
	
	for(Iterator itmm = mms.iterator(); itmm.hasNext();){
	    MetaMethod mm = (MetaMethod) itmm.next();
	    pw.println();
	    pw.print(mm);
	    if(detailed_view){
		pw.println();
		for(Iterator itcs=getCallSites(mm).iterator();itcs.hasNext();){
		    CALL cs = (CALL) itcs.next();
		    HCodeElement hce = (HCodeElement) cs;
		    MetaMethod[] callees = getCallees(mm,cs);
		    pw.println(" " + hce.getSourceFile() + ":" + 
			       hce.getLineNumber() + " " + cs + " (" +
			       callees.length + " callee(s)):");
		    for(int i = 0; i < callees.length; i++)
			pw.println("  " + callees[i]);
		}
	    }
	    else{
		MetaMethod[] callees = getCallees(mm);
		pw.println(" (" + callees.length + " callee(s)) :");
		for(int i = 0; i < callees.length; i++)
		    pw.println("  " + callees[i]);
	    }
	}
    }
}
