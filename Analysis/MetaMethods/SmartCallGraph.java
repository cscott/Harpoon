// SmartCallGraph.java, created Tue Mar 21 15:32:43 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MetaMethods;

import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.IR.Quads.CALL;

import harpoon.Util.Util;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;
import harpoon.Util.DataStructs.RelationEntryVisitor;

/** <code>SmartCallGraph</code> is an improved call graph produced by
    compressing a meta call graph, ie, all metamethods are shrinked
    down to their originating method).  Constructing the call graph at
    the level of meta methods and shrinking it later to method level
    is more precise than constructing the call graph for methods
    directly using some RTA-like algorithm.

    <p>
    For a simple program that does just a
    <code>System.out.println()</code>, the size of the largest group
    of mutually recursive methods (which corresponds to a strongly
    connected component in the call graph) decreased from 53 to 8.

    @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
    @version $Id: SmartCallGraph.java,v 1.3 2002-04-12 21:48:05 salcianu Exp $ */
public class SmartCallGraph implements CallGraph {
    
    /** Creates a <code>SmartCallGraph</code>.
	@param mcg Meta call graph
    */
    public SmartCallGraph(MetaCallGraph mcg) {
	construct(mcg);
    }


    /** Convenient constructor for use in cases when a meta call graph
	does not already exist.

	@param hcf Caching code factory used to produce the code of the methods
	@param ch  Class hierarchy 
	@param mroots Set of method roots (entry points into the program: usually the main method, static initializers and methods called by the JVM before main. 
	
	The parameters of this constructor are used to construct a
	meta call graph that is used to create <code>this</code> smart
	call graph. */
    public SmartCallGraph(CachingCodeFactory hcf,
			  ClassHierarchy ch, Set mroots) {
	assert
	    hcf.getCodeName().equals(harpoon.IR.Quads.QuadNoSSA.codename) :
	    "SmartCallGraph works only with QuadNoSSA";
	// can't call this(...) because this is not the first statement ...
	construct(new MetaCallGraphImpl(hcf, ch, mroots));
    }

    // empty array to return in case of no callee
    private final HMethod[] empty_array = new HMethod[0];

    // cache to keep the association caller -> calees
    // Generic programming type: Map<HMethod,HMethod[]>
    final private Map hm2callees = new HashMap();
    /** Returns an array containing all possible methods called by
	method <code>m</code>. If <code>hm</code> doesn't call any 
	method, return an array of length <code>0</code>. */
    public final HMethod[] calls(final HMethod hm) {
	HMethod[] retval = (HMethod[]) hm2callees.get(hm);
	if(retval == null)
	    return empty_array;
	return retval;
    }

    // cache to keep the association caller -> call_site -> callees
    // Generic programming type: Map<HMethod, Map<CALL, HMethod[]>>
    final private Map hm2cs2callees = new HashMap();
    /** Returns an array containing  all possible methods called by 
	method <code>m</code> at the call site <code>cs</code>.
	If there is no known callee for the call site <code>cs>/code>, or if 
	<code>cs</code> doesn't belong to the code of <code>hm</code>,
	return an array pof length <code>0</code>. */
    public final HMethod[] calls(final HMethod hm, final CALL cs) {
	Map cs2callees = (Map) hm2cs2callees.get(hm);
	if(cs2callees == null)
	    return empty_array;
	HMethod[] retval = (HMethod[]) cs2callees.get(cs);
	if(retval == null)
	    return empty_array;
	return retval;
    }

    /** Returns a list of all the <code>CALL</code>s quads in the code 
	of <code>hm</code>. */
    public CALL[] getCallSites(final HMethod hm){
	Map map = (Map) hm2cs2callees.get(hm);
	if(map == null)
	    return new CALL[0];
	Set css = map.keySet();
	return (CALL[]) css.toArray(new CALL[css.size()]);
    }

    /** Returns the set of all the methods that can be called in the 
	execution of the program. */
    public Set callableMethods(){
	return hm2callees.keySet();
    }

    // Does the main computation: fill the hm2callees and hm2cs2callees
    // structures using the info from mcg.
    private final void construct(final MetaCallGraph mcg){
	final Relation split = mcg.getSplitRelation();

	for(Iterator ithm = split.keys().iterator(); ithm.hasNext(); ){
	    HMethod hm = (HMethod) ithm.next();
	    // vc stores all the callees of hm
	    Set sc = new HashSet();
	    // map stores the association cs -> callees (cs is from
	    // the code of hm)
	    Map map = new HashMap();

	    Iterator itmm = split.getValues(hm).iterator();
	    while(itmm.hasNext()) {
		MetaMethod mm = (MetaMethod) itmm.next();

		Iterator itcs = mcg.getCallSites(mm).iterator();
		while(itcs.hasNext()){
		    CALL cs = (CALL) itcs.next();
		    // vc_cs stores all the callers at site cs
		    Set sc_cs = new HashSet();
		    // get the meta-method whih are called at cs
		    MetaMethod[] callees = mcg.getCallees(mm,cs);
		    for(int i = 0; i < callees.length; i++){
			HMethod hm_callee = callees[i].getHMethod();
			sc.add(hm_callee);
			sc_cs.add(hm_callee);
		    }
		    map.put(cs, 
		       (HMethod[]) sc_cs.toArray(new HMethod[sc_cs.size()]));
		}
	    }

	    hm2callees.put(hm, (HMethod[])sc.toArray(new HMethod[sc.size()])); 
	    hm2cs2callees.put(hm, map);
	}
    }

}
