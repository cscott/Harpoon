// CachingCallGraph.java, created Tue Apr  2 19:16:43 2002 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;

/** <code>CachingCallGraph</code> is a caching wrapper for a call
    graph.  In general, multiple stages in the compiler will use the
    call graph so it is good to cache it.  You should manually take
    care of constructing a new call graph when you modify the code in
    some way that might invalidate the old call graph.

    @author  Alexandru SALCIANU <salcianu@MIT.EDU>
    @version $Id: CachingCallGraph.java,v 1.5 2004-02-08 03:20:10 cananian Exp $ */
public class CachingCallGraph extends CallGraph {
    
    /** Creates a <code>CachingCallGraph</code> that caches the results
        of <code>cg</code>.

	@param cg underlying call graph (the one actually doing the job)
	@param m_cache specifies whether to cache the results of
	<code>calls(HMethod hm)</code>
	@param cs_cache specifies whether to cache the results of
	<code>calls(HMethod hm, CALL cs)</code>   */
    public CachingCallGraph(CallGraph cg, boolean m_cache, boolean cs_cache) {
	constr(cg, m_cache, cs_cache);
    }

    // does the real job for a constructor
    private void constr(CallGraph cg, boolean m_cache, boolean cs_cache) {
	this.cg = cg;
	this.m_cache  = m_cache;
	this.cs_cache = cs_cache;

	if(m_cache)  m2callees  = new HashMap();
	if(cs_cache) cs2callees = new HashMap();
    }

    /** Convenient to use constructor. If <code>cg</code> is a
	subclass of <code>CallGraphImpl</code>, it is equivalent to
	<code>CallGraphImpl(cg, false, true)</code>
	(<code>CallGraphImpl</code> already caches the callees of a method.
	Otherwise, it is equivalent to
	<code>CallGraphImpl(cg, true, true)</code> (full caching).

	@param cg underlying call graph */
    public CachingCallGraph(CallGraph cg) {
	// funny thing: the use of this(...) to call another constructor
	// is restricted to the first line, so we cannot use it here.
	if(cg instanceof CallGraphImpl)
	    constr(cg, false, true);
	else
	    constr(cg, true, true);
    }

    private CallGraph cg;     // underlying call graph
    private boolean m_cache;  // should we cache calls(hm) ?
    private Map m2callees;    // cache for calls(hm)
    private boolean cs_cache; // should we cache calls(hm, call) ?
    private Map cs2callees;   // cache for calls(hm, call)
    
    public HMethod[] calls(final HMethod hm) {
	if(!m_cache) return cg.calls(hm);

	HMethod[] result = (HMethod[]) m2callees.get(hm);
	if(result == null) {
	    result = cg.calls(hm);
	    m2callees.put(hm, result);
	}
	return result;
    }

    public HMethod[] calls(final HMethod hm, final CALL cs) {
	if(!cs_cache) return cg.calls(hm, cs);

	HMethod[] result = (HMethod[]) cs2callees.get(cs);
	if(result == null) {
	    result = cg.calls(hm, cs);
	    cs2callees.put(cs, result);
	}
	return result;
    }

    public CALL[] getCallSites(HMethod hm) {
	return cg.getCallSites(hm);
    }

    public Set callableMethods() {
	return cg.callableMethods();
    }

    public Set getRunMethods() {
	return cg.getRunMethods();
    }

    public void load_caches() {
	for(Object hmO : callableMethods()) {
	    HMethod hm = (HMethod) hmO;
	    
	    if(m_cache || (cg instanceof CallGraphImpl)) calls(hm);
	    if(cs_cache) {
		CALL[] calls = getCallSites(hm);
		for(int i = 0; i < calls.length; i++)
		    calls(hm, calls[i]);
	    }
	}
    }

}
