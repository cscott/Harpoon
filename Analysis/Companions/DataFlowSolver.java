// DataFlowSolver.java, created Thu May  8 20:07:24 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Companions;

import harpoon.ClassFile.HCode;
import harpoon.IR.Properties.CFGEdge;
import harpoon.IR.Properties.CFGraphable;
import harpoon.Util.Collections.WorkSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * The <code>DataFlowSolver</code> class provides a parameterized framework
 * for building simple data flow analyses for various IRs.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataFlowSolver.java,v 1.2 2003-05-09 00:44:05 cananian Exp $
 */
public abstract class DataFlowSolver
    <HCE extends CFGraphable<HCE,E>, E extends CFGEdge<HCE,E>, FACT> {
    /** Compute the join operation on two dataflow <code>FACT</code>s. */
    protected abstract FACT join(FACT e1, FACT e2);
    /** Return the initial value of the dataflow <code>FACT</code> for the
     *  given <code>HCodeElement</code> <code>hce</code>. */
    protected abstract FACT init(HCE hce);
    /** Compute all dataflow facts for the given <code>HCode</code>. */
    public abstract Map<HCE,FACT> compute(HCode<HCE> hc);

    /**
     * <code>DataFlowSolver.Forward</code> is a dataflow solver for
     * forward dataflow analyses.  You need only define a <code>join</code>
     * operation on your <code>FACT</code> type, an initial value for
     * each quad (via the return value of the <code>init()</code> method),
     * and a means for computing the <code>OUT</code> dataflow fact
     * given the <code>IN</code> fact for a <code>HCodeElement</code>.
     * Calling <code>compute()</code> will then return a mapping from
     * every <code>HCodeElement</code> to the final <code>IN</code>
     * dataflow fact for that element. */
    public static abstract class Forward
	<HCE extends CFGraphable<HCE,E>, E extends CFGEdge<HCE,E>, FACT>
	extends DataFlowSolver<HCE,E,FACT> {
	/** Compute the <code>OUT</code> fact given the <code>IN</code>
	 *  fact for an <code>HCodeElement</code> <code>hce</code>. */
	protected abstract FACT out(HCE hce, FACT in);
	
	private final FACT lookup(HCE hce, Map<HCE,FACT> map) {
	    if (!map.containsKey(hce))
		map.put(hce, init(hce));
	    return map.get(hce);
	}
	public final Map<HCE,FACT> compute(HCode<HCE> hc) {
	    final Map<HCE,FACT> OUTresult = new HashMap<HCE,FACT>();
	    final WorkSet<HCE> worklist = new WorkSet<HCE>(hc.getElementsL());
	    while (!worklist.isEmpty()) {
		HCE hce = worklist.removeFirst();
		// compute in using out result.
		FACT in = computeIN(hce, OUTresult);
		// now recompute new out.
		FACT out = out(hce, in);
		// if different from previous, add successors to worklist.
		if (!out.equals(lookup(hce, OUTresult))) {
		    OUTresult.put(hce, out);
		    for (Iterator<E> it=hce.succC().iterator(); it.hasNext();)
			worklist.add(it.next().to());
		}
	    }
	    // done!  but we want to return IN set, not OUT set.
	    final Map<HCE,FACT> INresult = new HashMap<HCE,FACT>();
	    for (Iterator<HCE> it=hc.getElementsI(); it.hasNext(); ) {
		HCE hce = it.next();
		INresult.put(hce, computeIN(hce, OUTresult));
	    }
	    // now really done.
	    return INresult;
	}
	private final FACT computeIN(HCE hce, Map<HCE,FACT> outMap) {
	    Iterator<E> it=hce.predC().iterator();
	    if (!it.hasNext()) return init(hce);
	    FACT f = lookup(it.next().from(), outMap);
	    while (it.hasNext())
		f = join(f, lookup(it.next().from(), outMap));
	    return f;
	}
    }
}
