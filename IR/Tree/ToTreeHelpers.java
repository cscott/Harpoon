// ToTreeHelpers.java, created Fri Feb 11 15:11:20 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.Analysis.ReachingDefs;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.Quad;
import harpoon.IR.LowQuad.PCALL;
import harpoon.IR.LowQuad.PSET;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.BinaryHeap;
import harpoon.Util.Collections.BinomialHeap;
import harpoon.Util.Collections.FibonacciHeap;
import harpoon.Util.Collections.Heap;
import harpoon.Util.Environment;
import harpoon.Util.HashEnvironment;
import harpoon.Util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>ToTreeHelpers</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ToTreeHelpers.java,v 1.1.2.3 2000-02-13 02:34:58 cananian Exp $
 */
abstract class ToTreeHelpers {
    //------------ EdgeOracle IMPLEMENTATIONS ------------------

    /** The <code>DefaultEdgeOracle</code> always follows the zeroth
     *  outgoing edge.  Very simple. */
    static class DefaultEdgeOracle implements ToTree.EdgeOracle {
	DefaultEdgeOracle() { }
	public int defaultEdge(HCodeElement hce) { return 0; }
    }

    /** The <code>MinMaxEdgeOracle</code> lays code out according to the
     *  computed shortest paths to the footer.  That is, it computes
     *  an approximation to the single-source longest paths problem
     *  and tries to lay out code such that the longest paths in the
     *  method have no branches.  Since LONGEST-PATH is NP-complete,
     *  this class actual sets the default successor edge to be the
     *  one which has the longest shortest path to the footer.
     *  <p>
     *  See Karger, Motwani, and Ramkumar, "On Approximating the Longest
     *  Path in a Graph" in <i>Algorithmica</i> 1997 18:82-98; and
     *  Young, Johnson, Karger, and Smith, "Near-optimal intraprocedural
     *  Branch Alignment" in PLDI'97 for some tangentially-related work.
     *
     * @author C. Scott Ananian <cananian@alumni.princeton.edu>
     */
    static class MinMaxEdgeOracle implements ToTree.EdgeOracle {
	final CFGrapher cfg;
	/** Convenience constructor. */
	MinMaxEdgeOracle(HCode hc) { this(hc, CFGrapher.DEFAULT); }
	/** Compute single-source shortest paths on edge-reversed graph,
	 *  using the technique of CLR, 25.2 (Dijkstra's algorithm).
	 *  Uses a binomial heap, for an O(E lg V) runtime.  If we're
	 *  feeling bored, we could use a fibonacci heap instead for
	 *  an O(V lg V + E) runtime.  I don't think the difference is
	 *  enough to care about. */
	MinMaxEdgeOracle(HCode hc, CFGrapher cfg) {
	    this.cfg = cfg;
	    // get the 'single-source', which is the unique FOOTER node.
	    Util.assert(hc.getLeafElements().length==1);
	    HCodeElement s = hc.getLeafElements()[0];
	    // we're going to need a mapping from HCodeElements to Map.Entry's
	    Map m = new HashMap();
	    // make the priority queue, and initialize it.
	    Heap Q = new BinaryHeap(); // new FibonacciHeap();
	    for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
		HCodeElement hce = (HCodeElement) it.next();
		// d[v] = infinity.
		Map.Entry entry= Q.insert(new Integer(Integer.MAX_VALUE), hce);
		// don't forget entry-to-hce correlation.
		m.put(hce, entry);
	    }
	    // INITIALIZE-SINGLE-SOURCE by lowering source to 0.
	    set_d(s, 0, Q, m);
	    // now do Dijkstra's algorithm.
	    while (!Q.isEmpty()) {
		HCodeElement u = (HCodeElement) Q.extractMinimum().getValue();
		for (Iterator it=cfg.predC(u).iterator(); it.hasNext(); ) {
		    HCodeElement v = ((HCodeEdge) it.next()).from();
		    // RELAX step.
		    if (d(v) > (d(u)+1))
			set_d(v, d(u)+1, Q, m);
		}
	    }
	    // Look, Ma!  d[v] is loaded & ready to go!
	}
	/** Path weights mapping. */
	final Map d = new HashMap();
	// convenience functions for working with the path weights.
	private int d(HCodeElement v) {
	    if (!d.containsKey(v)) return Integer.MAX_VALUE;
	    return ((Integer)d.get(v)).intValue();
	}
	private void set_d(HCodeElement v, int k, Heap Q, Map m) {
	    Util.assert(d(v) > k);
	    Util.assert(((Map.Entry)m.get(v)).getKey()
			.equals(new Integer(d(v))));
	    d.put(v, new Integer(k));
	    Q.decreaseKey((Map.Entry)m.get(v), new Integer(k));
	    Util.assert(((Map.Entry)m.get(v)).getKey()
			.equals(new Integer(d(v))));
	}
	/** defaultEdge corresponds to the edge with largest shortest path */
	public int defaultEdge(HCodeElement hce) {
	    HCodeEdge[] succ = cfg.succ(hce);
	    // hacks for SWITCH & other common cases.
	    if (succ.length!=2) return 0;
	    int maxedge=0, maxscore=d(succ[0].to());
	    for (int i=1; i<succ.length; i++) {
		int score = d(succ[i].to());
		if (score > maxscore) {
		    maxedge=i; maxscore=score;
		}
	    }
	    return maxedge;
	}
    }

    //------------ ReachingDefs IMPLEMENTATIONS ------------------
    static class SSIReachingDefs extends ReachingDefs {
	final Map m = new HashMap();
	SSIReachingDefs(HCode hc) { this(hc, UseDefer.DEFAULT); }
	SSIReachingDefs(HCode hc, UseDefer ud) {
	    super(hc);
	    for (Iterator it = hc.getElementsI(); it.hasNext(); ) {
		HCodeElement hce = (HCodeElement) it.next();
		for (Iterator it2 = ud.defC(hce).iterator(); it2.hasNext(); ) {
		    Temp t = (Temp) it2.next();
		    Util.assert(!m.containsKey(t), "not in SSI form!");
		    m.put(t, hce);
		}
	    }
	}
	public Set reachingDefs(HCodeElement hce, Temp t) {
	    return Collections.singleton(m.get(t));
	}
    }

    //------------ FoldNanny IMPLEMENTATIONS ------------------
    static class DefaultFoldNanny implements ToTree.FoldNanny {
	DefaultFoldNanny() { }
	public boolean canFold(HCodeElement hce, Temp t) { return false; }
    }
    static class SSISimpleFoldNanny implements ToTree.FoldNanny {
	final Set safe = new HashSet();
	SSISimpleFoldNanny(HCode hc) {
	    // first, find all the single-use variables
	    HashSet singleUse = new HashSet(), multiUse = new HashSet();
	    for (Iterator it = hc.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		for (Iterator it2 = q.useC().iterator(); it2.hasNext(); ) {
		    Temp t = (Temp) it2.next();
		    if (multiUse.contains(t)) continue;
		    if (singleUse.contains(t)) {
			multiUse.add(t); singleUse.remove(t);
		    } else singleUse.add(t);
		}
	    }
	    multiUse = null;
	    // now filter the safe set.
	    // we don't want anything folded which is live over a memory
	    // store or a synchronization.  we're going to be a bit
	    // conservative here.
	    dfs((Quad)hc.getRootElement(), new HashSet(),
		new HashEnvironment(), singleUse);
	    // done!
	    //System.err.print("[FOLDING: "+safe+"]");
	}
	void dfs(Quad q, Set seen, Environment reachingDefs, Set singleUse) {
	    Util.assert(!seen.contains(q));
	    seen.add(q);
	    { // if def reached this use, add to safe set.
		Temp[] use = q.use();
		for (int i=0; i<use.length; i++)
		    if (reachingDefs.containsKey(use[i]))
			safe.add(use[i]); // reaching def means its safe.
	    }
	    { // stores and syncs clear the defs
		if (isBarrier(q))
		    reachingDefs.clear();
	    }
	    { // add environment entries for all appropriate defs
		Temp[] def = q.def();
		for (int i=0; i<def.length; i++)
		    if (singleUse.contains(def[i]))
			if (!(q instanceof PCALL)) // CALLS CAN NOT BE FOLDED!
			    reachingDefs.put(def[i], def[i]);
	    }
	    { // recurse.
		Quad[] next = q.next();
		Environment.Mark m = reachingDefs.getMark();
		for (int i=0; i<next.length; i++) {
		    if (!seen.contains(next[i])) {
			dfs(next[i], seen, reachingDefs, singleUse);
			reachingDefs.undoToMark(m);
		    }
		}
	    }
	}
	public static boolean isBarrier(Quad q) {
	    // ASET,CALL,SET are Quad-only.  We only deal with LowQuads here
	    if (q instanceof MONITORENTER || q instanceof MONITOREXIT ||
		q instanceof PCALL || q instanceof PSET) return false;
	    return true;
	}
	public boolean canFold(HCodeElement hce, Temp t) {
	    return safe.contains(t);
	}
    }
}
