// Prune.java, created Fri Jun 30 19:15:56 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCode;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.Collections.SetFactory;
import harpoon.Util.Util;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>Prune</code> quickly prunes dead variable assignments from the
 * quad graph.  It doesn't do a full liveness analysis, so it doesn't
 * get *everything*, but it works in linear time (well, O(EV) time)
 * and gets most of the egregious dead vars.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Prune.java,v 1.1.2.2 2000-12-13 18:46:29 cananian Exp $
 */
class Prune {
    private final static boolean DEBUG=false;

    /** one one can create an instance of this class */
    private Prune() { }

    /** Remove quads which do nothing but define dead variables from
     *  the CFG. */
    static void prune(HCode hc) {
	// the State object does the analysis.
	State s = new State(hc);
	// now trim out useless quads.
	for (Iterator it=s.useless.iterator(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    if (DEBUG) System.err.println("PRUNING: #"+q.getID()+" "+q);
	    Util.assert(q.nextLength()==1 && q.prevLength()==1);
	    Edge fromE = q.prevEdge(0), toE = q.nextEdge(0);
	    Quad.addEdge((Quad)fromE.from(), fromE.which_succ(),
			 (Quad)toE.to(), toE.which_pred());
	}
	// done!
    }

    private static class State {
	private final SetFactory sf;
	private final Map seen = new HashMap();
	private final Set EMPTY_SET, FULL_SET;
	/** useless quads, as determined by our analysis. */
	final Set useless = new HashSet();
	State(HCode hc) {
	    // create Temp universe.
	    Set universe = new HashSet();
	    for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		universe.addAll(q.defC());
		universe.addAll(q.useC());
	    }
	    // create BitSetFactory
	    this.sf = new BitSetFactory(universe, Temp.INDEXER);
	    this.EMPTY_SET = sf.makeSet();
	    this.FULL_SET  = sf.makeSet(universe);
	    // okay, do the thing that we do.
	    knownDeadInto((Quad)hc.getRootElement());
	    // now we've got a useless set.
	}

	/** main body of the analysis is a post-order dfs search */
	private Set knownDeadInto(Quad q) {
	    if (seen.containsKey(q)) return sf.makeSet((Set) seen.get(q));
	    if (q.prevLength()>1) seen.put(q, EMPTY_SET);
	    Set ra[] = new Set[q.nextLength()];
	    for (int i=0; i<q.nextLength(); i++)
		ra[i]=knownDeadInto(q.next(i));
	    Set r = sf.makeSet(FULL_SET);
	    for (int i=0; i<q.nextLength(); i++)
		r.retainAll(ra[i]);
	    // now we have the knownDeadOutOf set.
	    if (isUseless(q, r)) useless.add(q);
	    // construct knownDeadInto set:
	    // all defs of this instr are known dead into this.
	    r.addAll(q.defC());
	    // special case calls.
	    if (q instanceof CALL && q.nextLength()>1) {
		// calls leave one of their defs untouched.
		CALL Q = (CALL) q;
		if (Q.retval()!=null && !ra[1].contains(Q.retval()))
		    r.remove(Q.retval());
		if (Q.retex()!=null && !ra[0].contains(Q.retex()))
		    r.remove(Q.retex());
	    }
	    // all uses of this instr are live into this.
	    // (note that the simpler r.removeAll(q.useC()) uses time
	    //  proportional to the size of *r*, which is not what we want)
	    for (Iterator it=q.useC().iterator(); it.hasNext(); )
		r.remove(it.next());
	    if (DEBUG)
		System.err.println("DEAD INTO #"+q.getID()+" is "+r+
				   " (using "+q.useC()+")");
	    // if this is a phi, record the set.
	    if (q.prevLength()>1) seen.put(q, sf.makeSet(r));
	    // okay, this is it!
	    return r;
	}

	private static boolean isUseless(Quad q, Set knownDeadOut) {
	    // quads defining live variables are not useless.
	    if (!knownDeadOut.containsAll(q.defC())) return false;
	    // also, quads with side effects are not useless.
	    if (q instanceof ARRAYINIT || q instanceof ASET ||
		q instanceof SIGMA || q instanceof PHI ||
		q instanceof DEBUG || q instanceof HANDLER ||
		q instanceof HEADER || q instanceof FOOTER ||
		q instanceof LABEL || q instanceof METHOD ||
		q instanceof MONITORENTER || q instanceof MONITOREXIT ||
		q instanceof TYPECAST || q instanceof RETURN ||
		q instanceof SET || q instanceof THROW)
		return false;
	    // otherwise, it's useless!
	    if (DEBUG)
		System.err.println("#"+q.getID()+" is useless because all of "+
				   q.defC()+" are in "+knownDeadOut);
	    return true;
	}
    }
}
