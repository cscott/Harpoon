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
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.CALL;

import harpoon.Util.Util;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.RelationImpl;

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
    @version $Id: SmartCallGraph.java,v 1.7 2003-04-18 16:25:00 salcianu Exp $
*/
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

	@param linker Linker to get classes

	@param ch  Class hierarchy

	@param roots Set of program roots (the same notion of roots
	as that used by <code>QuadClassHierarchy</code>).
	
	The parameters of this constructor are used to construct a
	meta call graph that is used to create <code>this</code> smart
	call graph. */
    public SmartCallGraph(CachingCodeFactory hcf, Linker linker,
			  ClassHierarchy ch, Set roots) {
	assert
	    hcf.getCodeName().equals(harpoon.IR.Quads.QuadNoSSA.codename) ||
	    hcf.getCodeName().equals(harpoon.IR.Quads.QuadSSA.codename) ||
	    hcf.getCodeName().equals(harpoon.IR.Quads.QuadSSI.codename) :
	    "unsupported quad factory " + hcf;
	// can't call this(...) because this is not the first statement ...
	construct(new MetaCallGraphImpl(hcf, linker, ch,
					constructMRoots(roots, ch)));
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
    public Set callableMethods() {
	return hm2callees.keySet();
    }

    public Set getRunMethods() {
	return run_hms;
    }
    private Set run_hms;

    // Does the main computation: fill the hm2callees and hm2cs2callees
    // structures using the info from mcg.
    private final void construct(final MetaCallGraph mcg) {
	Relation split = mcg.getSplitRelation();

	for(Iterator it_hm = split.keys().iterator(); it_hm.hasNext(); ) {
	    HMethod hm = (HMethod) it_hm.next();
	    // ac stores all the callees of hm
	    Set ac = new HashSet();
	    // rel stores associations cs -> callees(cs)
	    // where cs is a call site from hm
	    Relation rel = new RelationImpl();

	    // iterate over all metamethods corresponding to hm
	    for(Iterator im = split.getValues(hm).iterator(); im.hasNext(); ) {
		MetaMethod mm = (MetaMethod) im.next();

		for(Iterator it_cs = mcg.getCallSites(mm).iterator();
		    it_cs.hasNext(); ) {
		    CALL cs = (CALL) it_cs.next();
		    // get meta-methods called at cs
		    MetaMethod[] callees = mcg.getCallees(mm, cs);
		    for(int i = 0; i < callees.length; i++) {
			HMethod hm_callee = callees[i].getHMethod();
			rel.add(cs, hm_callee);
			ac.add(hm_callee);
		    }
		}
	    }
	    
	    hm2callees.put(hm, ac.toArray(new HMethod[ac.size()])); 
	    
	    // map stores the association cs -> array of callees,
	    // where cs is a call site from the code of hm
	    Map map = new HashMap();
	    // iterate over the call sites from hm to build "map"
	    for(Iterator it_cs = rel.keys().iterator(); it_cs.hasNext(); ) {
		CALL cs = (CALL) it_cs.next();
		Set callees = rel.getValues(cs);
		map.put(cs, callees.toArray(new HMethod[callees.size()]));
	    }
	    hm2cs2callees.put(hm, map);
	}

	run_hms = new HashSet();
	for(Iterator it = mcg.getRunMetaMethods().iterator(); it.hasNext(); ) {
	    MetaMethod mm = (MetaMethod) it.next();
	    run_hms.add(mm.getHMethod());
	}
    }


    /** Constructs the set of method roots for the compiled program.
	<code>MetaCallGraphImpl<code> has a different definition of
	roots than <code>QuadClassHierarchy</code>
	(<code>SAMain</code> uses that definition).  Note: the
	constructor of <code>SmartCallGraph</code> already calls this;
	no need to call it externally.
	
	@param roots Set of roots from SAMain (this includes both
	methods and classes)
	
	@param ch Class hierarchy for the compiled program.
	
	@return Set of method roots for the compiled program.  This
	set includes: the main method + all methods that may be called
	by JVM (including all static initializers).

	TODO: unify the two concepts of roots. */
    public static Set/*<HMethod>*/ constructMRoots(Set roots,
						   ClassHierarchy ch) {
	Set/*<HMethod>*/ mroots = new HashSet/*<HMethod>*/();
	mroots.addAll(select_methods(roots));
	mroots.addAll(get_static_initializers(ch));
	return mroots;
    }

    // returns the set of methods from "set"
    private static Set select_methods(Set set) {
	Set retval = new HashSet();
	for(Iterator it = set.iterator(); it.hasNext(); ) {
	    Object elm = it.next();
	    if(elm instanceof HMethod)
		retval.add((HMethod) elm);
	}
	return retval;
    }
    

    // Returns the set of static initializers of all the instanciated classes.
    private static Set get_static_initializers(ClassHierarchy ch) {
	Set retval = new HashSet();
	for(Iterator it = ch.classes().iterator(); it.hasNext(); ) {
	    HClass hclass = (HClass) it.next();
	    HMethod hm = hclass.getClassInitializer();
	    if(hm != null)
		retval.add(hm);
	}
	return retval;
    }
}
