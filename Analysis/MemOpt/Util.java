// Util.java, created Fri May 10 20:55:27 2002 by salcianu
// Copyright (C) 2000 Alexandru Salcianu <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MemOpt;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

import harpoon.IR.Quads.Code;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.CachingCodeFactory;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;

import harpoon.Analysis.PointerAnalysis.PAWorkList;

import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.SCCTopSortedGraph;


/**
 * <code>Util</code>
 * 
 * @author  Alexandru Salcianu <salcianu@MIT.EDU>
 * @version $Id: Util.java,v 1.2 2002-05-11 03:25:33 salcianu Exp $
 */
public abstract class Util {

    private static boolean DEBUG = true;
    
    public static Set getDNEWs(CachingCodeFactory hcf, ClassHierarchy ch,
			       HMethod entry, CallGraph cg) {
	Set dnews = new HashSet();
	Set methods = reachable_from_rec(hcf, ch, entry, cg);

	if(DEBUG)
	    harpoon.Util.Util.print_collection
		(methods, "Methods called from recursive methods");

	for(Iterator it = methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    Code hcode = (Code) hcf.convert(hm);
	    dnews.addAll(hcode.selectAllocations());
	}
	return dnews;
    }


    private static Set reachable_from_rec(final CachingCodeFactory hcf,
					  final ClassHierarchy ch,
					  final HMethod entry,
					  final CallGraph cg) {
	// 1. construct the SCCs of the subgraph rooted in entry
	final AllCallers ac = new AllCallers(ch, hcf, cg);

	SCComponent.Navigator nav = new SCComponent.Navigator() {
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
	SCCTopSortedGraph method_sccs = 
	    SCCTopSortedGraph.topSort(SCComponent.buildSCC(entry, nav));

	Set reached_from_rec = new HashSet();

	// scan all strongly connected components of methods
	for(SCComponent scc = method_sccs.getLast(); scc != null;
	    scc = scc.prevTopSort()) {
	    if(scc.isLoop() || (scc.nodeSet().size() > 1)) {
		// if the method corresponds to a set of mutually
		// recursive methods, add all the methods transitively called
		// from it to reached_from_rec
		HMethod hm = (HMethod) scc.nodes()[0];
		if(!reached_from_rec.contains(hm))
		    grab_callees(hm, reached_from_rec);
	    }
	}

	return reached_from_rec;
    }


    private static void grab_callees(final HMethod hm,
				     final Set reached_from_rec) {
	PAWorkList W = new PAWorkList();
	W.add(hm);
	reached_from_rec.add(hm);

	while(!W.isEmpty()) {
	    HMethod callee = (HMethod) W.remove();
	    if(reached_from_rec.add(callee))
		W.add(callee);
	}
    }

}
