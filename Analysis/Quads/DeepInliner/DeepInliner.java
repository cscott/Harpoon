// DeepInliner.java, created Tue Jul 26 06:53:46 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads.DeepInliner;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Iterator;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.IR.Quads.QuadRSSx;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.Code;

import harpoon.Analysis.Quads.CallGraph;

import jpaul.DataStructs.Relation;
import jpaul.DataStructs.MapSetRelation;

import jpaul.Graphs.DiGraph;
import jpaul.Graphs.ArcBasedDiGraph;
import jpaul.Graphs.TopSortedCompDiGraph;
import jpaul.Graphs.SCComponent;

import harpoon.Util.Util;

/**
   <code>DeepInliner</code> contains the code responsible with
   inlining a set of <code>InlineChain</code>s.  Each
   <code>InlineChain</code> corresponds to a chain of several calls
   (simple inlining, when we inline call paths of length one, is a
   trivial special case).  Clients of this class should invoke the
   static method <code>DeepInliner.inline</code>

   <p>The inlining is performed eagerly, by mutating the code of the
   affected methods, as cached by the <code>CachingCodeFactory</code>
   that is passed as the first argument of <code>inline</code>.  The
   alternative would be to construct an <code>HCodeFactory</code> that
   generates the code of the affected methods on demand; however, the
   complications of inlining multiple <code>InlineChain</code>s forces
   us to perform the inlining eagerly.

   <p>Among these complications, we mention the desire to inline the
   "best version" of a callee, i.e., the code of the callee after the
   callee-relevant inlining has already been performed.  Hence, the
   order in which we process the inline chains is not irrelevant.
   Also, the implementation is careful to act efficiently if several
   calling chains contain the same call.
   
   @see InlineChain

 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: DeepInliner.java,v 1.1 2005-08-09 22:40:35 salcianu Exp $ */
public class DeepInliner {

    /** Activates some debugging messages for all the classes from
        this package. */
    static final boolean DEBUG = false;

    // The DeepInliner object is basically a closure for the real
    // computation performed by the static method DeepInliner.inline.
    // Only DeepInliner.inline creates a DeepInliner object.
    private DeepInliner(CachingCodeFactory ccf, Collection<InlineChain> ics, CallGraph cg) {
	this.ccf = ccf;
	this.cg  = cg;

	if(DEBUG) {
	    System.out.println("\n\n\nInline Chains: " + ics);
	    checkMatch(ics);
	    System.out.println("\n\n");
	}

	if(ics.isEmpty()) return;

	pq = new PriorityQueue<InlineChain>(ics.size(), getICcomp(ics));
	pq.addAll(ics);

	while(!pq.isEmpty()) {
	    InlineChain ic = pq.remove();
	    // An inline chain may become empty due to repeated
	    // re-adjustements inside InlineChain.applyInline (see the
	    // code in the InlineChain.java method).
	    if(ic.isEmpty()) continue;

	    if(DEBUG) {
		System.out.println("\nINLINE CHAIN" + ic);
	    }
	    Map<Quad,Quad> old2new = null;
	    for(Iterator<CALL> it = ic.calls().iterator() ; it.hasNext(); ) {
		CALL cs = it.next();
		HMethod callee = cg.calls(Util.quad2method(cs), cs)[0];
		
		if(old2new != null) {
		    // as we already inlined the method that contains cs in its body, 
		    // we need to adjust cs through the old2new map.
		    assert old2new.containsKey(cs);
		    cs = (CALL) old2new.get(cs);
		}

		if(DEBUG) {
		    System.out.println("\nInline cs = " + Util.code2str(cs));
		}
		// 1. do the real inlining.
		old2new = OneLevelInliner.inline(cs, callee, ccf);
		Code calleeCode = (Code) ccf.convert(callee);
		// 2. invoke user action.
		ic.action(cs, calleeCode, old2new);
		// 3. "adjust" the inline chains that contain cs
		for(InlineChain ic2 : pq) {
		    ic2.applyInline(cs, calleeCode, old2new);
		}
		// 4. make sure to call the final user action, if necessary.
		if(!it.hasNext()) {
		    ic.finalAction();
		}
	    }
	}
    }

    private final CachingCodeFactory ccf;
    private final CallGraph cg;
    private PriorityQueue<InlineChain> pq;


    // does some sanity checks on the inlining chains
    private void checkMatch(Collection<InlineChain> ics) {
	for(InlineChain ic : ics) {	    
	    HMethod previous = null;
	    for(CALL cs : ic.calls()) {
		if(previous != null) {
		    assert 
			previous.equals(Util.quad2method(cs)) :
			Util.code2str(cs) + "\n\texpected to be in " + previous + 
			"\nbut it's in\n\t" + Util.quad2method(cs) + "\nin" + ic;
		}
		
		HMethod[] callees = cg.calls(Util.quad2method(cs), cs);
		assert 
		    callees.length == 1 :
		    "Cannot inline\n\t" + Util.code2str(cs) + "\nit has " + 
		    callees.length + " callees; in" + ic;
		previous = callees[0];
	    }
	}
    }


    private Comparator<InlineChain> getICcomp(Collection<InlineChain> ics) {
	Relation<HMethod,HMethod> deps = getDeps(ics);
	TopSortedCompDiGraph<HMethod> depGraph = 
	    new TopSortedCompDiGraph(new ArcBasedDiGraph<HMethod>(deps));
	final Map<HMethod,Integer> hm2rank = new HashMap<HMethod,Integer>();
	int k = 0;
	for(SCComponent<HMethod> scc : depGraph.decrOrder()) {
	    if(DEBUG && (scc.size() > 1)) {
		System.out.println("Warning: circular inlining!\n\tThe system will not crush, but the inlining may not be optimal.");
	    }
	    Integer rank = new Integer(k++);
	    for(HMethod hm : scc.nodes()) {
		hm2rank.put(hm, rank);
	    }
	}
	return new Comparator<InlineChain>() {
	    public int compare(InlineChain ic1, InlineChain ic2) {
		Integer rank1 = hm2rank.get(ic1.getTargetMethod());
		Integer rank2 = hm2rank.get(ic2.getTargetMethod());
		return -rank1.compareTo(rank2);
	    }
	    public boolean equals(Object o) { return o == this; }
	};
    }


    private Relation<HMethod,HMethod> getDeps(Collection<InlineChain> ics) {
	Relation<HMethod,HMethod> deps = new MapSetRelation<HMethod,HMethod>();
	for(InlineChain ic : ics) {
	    for(CALL cs : ic.calls()) {
		HMethod caller = Util.quad2method(cs);
		HMethod[] callees = cg.calls(caller, cs);
		assert callees.length == 1 : "cannot inline virtual calls with 2+ callees";
		deps.add(caller, callees[0]);
	    }
	}
	return deps;
    }


    /** 
	Inline a set of <code>InlineChain</code>s.  

	@param ccf Caching code factory.  Must produce either RSSx or
	QuadNoSSA (the code is simply too hard to generate for other
	intermediate representations, like SSA and SSI).

	@param ics Collection of desired <code>InlineChain</code>.

	@param cg Call-graph.  DOES NOT need to be a whole-program
	call graph: it just needs to give us the callees for all the
	call sites involved in the inline chains from
	<code>cs</code>. 

	@see InlineChain */
    public static void inline(CachingCodeFactory ccf,
			      Collection<InlineChain> ics,
			      CallGraph cg) {	
	assert 
	    ccf.getCodeName().equals(QuadRSSx.codename) ||
	    ccf.getCodeName().equals(QuadNoSSA.codename);
	DeepInliner di = new DeepInliner(ccf, ics, cg);
    }
}
