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
import harpoon.Temp.Temp;
import harpoon.Util.BinomialMap;
import harpoon.Util.Util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>ToTreeHelpers</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ToTreeHelpers.java,v 1.1.2.1 2000-02-12 13:40:42 cananian Exp $
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
    // XXX THIS DOESN'T WORK YET, BECAUSE BINOMIALMAP IS BROKEN.
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
	    System.err.println("[[START]]");
	    // get the 'single-source', which is the unique FOOTER node.
	    Util.assert(hc.getLeafElements().length==1);
	    HCodeElement s = hc.getLeafElements()[0];
	    // we're going to need a mapping from HCodeElements to Map.Entry's
	    Map m = new HashMap();
	    // make the priority queue, and initialize it.
	    BinomialMap Q = new BinomialMap();
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
		System.err.println("["+d(u)+"] DONE WITH "+u);
		for (Iterator it=cfg.predC(u).iterator(); it.hasNext(); ) {
		    HCodeElement v = ((HCodeEdge) it.next()).from();
		    // RELAX step.
		    if (d(v) > (d(u)+1))
			set_d(v, d(u)+1, Q, m);
		}
	    }
	    // Look, Ma!  d[v] is loaded & ready to go!
	    System.err.println("[[STOP]]");
	}
	/** Path weights mapping. */
	final Map d = new HashMap();
	// convenience functions for working with the path weights.
	private int d(HCodeElement v) {
	    if (!d.containsKey(v)) return Integer.MAX_VALUE;
	    return ((Integer)d.get(v)).intValue();
	}
	private void set_d(HCodeElement v, int k, BinomialMap Q, Map m) {
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
	    if (maxedge!=0) System.err.println("NON-ZERO DEFAULT");
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
}
