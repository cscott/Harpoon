// Util.java, created Fri May 10 20:55:27 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.QuadValueVisitor;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;

import harpoon.Analysis.PointerAnalysis.PAWorkList;

import harpoon.Util.Graphs.DiGraph;
import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.TopSortedCompDiGraph;
import harpoon.Util.Graphs.Navigator;


/**
 * <code>Util</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: Util.java,v 1.13 2004-03-06 21:52:18 salcianu Exp $
 */
public abstract class Util {

    private static boolean DEBUG = true;

    private static QuadValueVisitor<Boolean> new_visitor = 
	new QuadValueVisitor<Boolean>() {
	    public Boolean visit(NEW q) {
		return Boolean.TRUE;
	    }
	    public Boolean visit(Quad q) {
		return Boolean.FALSE;
	    }
	};
    
    public static Set getDNEWs(CachingCodeFactory hcf, 
			       HMethod entry, CallGraph cg) {
	Set dnews = new HashSet();
	Set methods = reachable_from_rec(entry, cg);

	if(DEBUG)
	    harpoon.Util.Util.print_collection
		(methods, "Methods called from recursive methods");

	for(Object hmO : methods) {
	    HMethod hm = (HMethod) hmO;
	    Code hcode = (Code) hcf.convert(hm);
            if (hcode != null) 
                dnews.addAll(hcode.selectAllocations());
	}
	return dnews;
    }


    private static Set reachable_from_rec(final HMethod entry,
					  final CallGraph cg) {
	// 1. construct the SCCs of the subgraph rooted in entry
	final AllCallers ac = new AllCallers(cg);

	Navigator nav = new Navigator() {
	    public Object[] next(Object node) {
		return cg.calls((HMethod) node);
	    }
	    public Object[] prev(Object node) {
		return ac.directCallers((HMethod) node);
	    }
	};
	
	// the topologically sorted graph of strongly connected components
	// composed of mutually recursive methods (the edges model the
	// caller-callee interaction).

	TopSortedCompDiGraph<HMethod> ts_hms = 
	    new TopSortedCompDiGraph
	    (DiGraph.diGraph(Collections.singleton(entry), nav));
	
	if(DEBUG) {
	    System.out.println("SETS OF MUTUALLY RECURSIVE METHODS");
	    int count = 0;
	    for(SCComponent/*<HMethod>*/ scc : ts_hms.decrOrder()) {
		count++;
		harpoon.Util.Util.print_collection
		    (scc.nodes(), "SCC " + count);
	    }
	    System.out.println();
	}

	Set reached_from_rec = new HashSet();

	for(SCComponent/*<HMethod>*/ scc : ts_hms.decrOrder()) {
	    if(scc.isLoop() || (scc.nodes().size() > 1)) {
		// if the SCC corresponds to a set of mutually
		// recursive methods, add all the methods transitively
		// called from it to reached_from_rec

		// exploring the tree rooted in one of the methods
		// from scc is enough as it includes all the other
		// methods from scc
		HMethod hm = (HMethod) scc.nodes().iterator().next();
		if(!reached_from_rec.contains(hm))
		    grab_callees(hm, reached_from_rec, cg);
	    }
	}

	return reached_from_rec;
    }


    // put into reached_from_rec all methods that may be transitively
    // called from hm
    private static void grab_callees(final HMethod hm,
				     final Set reached_from_rec,
				     final CallGraph cg) {
	PAWorkList W = new PAWorkList();
	W.add(hm);
	reached_from_rec.add(hm);

	while(!W.isEmpty()) {
	    HMethod caller = (HMethod) W.remove();
	    HMethod callees[] = cg.calls(caller);
	    for(int i = 0; i < callees.length; i++)
		// if newly seen methods, put it into the worklist
		// to process later
		if(reached_from_rec.add(callees[i]))
		    W.add(callees[i]);
	}
    }

}
