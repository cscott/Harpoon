// DataFlowSolver.java, created Thu May  8 20:07:24 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Companions;

import harpoon.ClassFile.HCode;
import harpoon.IR.Properties.CFGEdge;
import harpoon.IR.Properties.CFGraphable;
import harpoon.Util.Collections.Graph;
import harpoon.Util.Collections.WorkSet;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
/**
 * The <code>DataFlowSolver</code> class provides a parameterized framework
 * for building simple data flow analyses for various IRs.  The
 * <code>DataFlowSolver.Forward</code> and <code>DataFlowSolver.Backward</code>
 * subclasses contain code for forward and backward analyses, respectively.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DataFlowSolver.java,v 1.3 2003-05-12 19:14:52 cananian Exp $
 */
public abstract class DataFlowSolver<N extends Graph.Node<N,E>,
				     E extends Graph.Edge<N,E>,
				     FACT> {
    /** Compute the join operation on two dataflow <code>FACT</code>s. */
    protected abstract FACT join(FACT e1, FACT e2);
    /** Return the initial value of the dataflow <code>FACT</code> for the
     *  given <code>Node</code> (often, an <code>HCodeElement</code>). */
    protected abstract FACT init(N hce);
    /** Compute all dataflow facts on the given <code>Graph</code>.
     *  Note that <code>HCode</code> subclasses with elements implementing
     *  <code>CFGraphable</code> are typically also instances of
     *  <code>Graph</code>. */
    public abstract Map<N,FACT> compute(Graph<N,E> hc);

    /**
     * <code>DataFlowSolver.Forward</code> is a dataflow solver for
     * forward dataflow analyses.  You need only define a <code>join</code>
     * operation on your <code>FACT</code> type, an initial value for
     * each node (usually, an <code>HCodeElement</code> implementation which
     * also implements <code>Graph.Node</code>, such as <code>Quad</code>)
     * via the return value of the <code>init()</code> method), and a means
     * for computing the <code>OUT</code> dataflow fact given the
     * <code>IN</code> fact for a node (<code>HCodeElement</code>).
     * Calling <code>compute()</code> will then return a mapping from
     * every <code>Graph.Node</code> to the final <code>IN</code>
     * dataflow fact for that element. */
    public static abstract class Forward
	<N extends Graph.Node<N,E>, E extends Graph.Edge<N,E>, FACT>
	extends DataFlowSolver<N,E,FACT> {
	/** Compute the <code>OUT</code> fact given the <code>IN</code>
	 *  fact for a <code>Node</code> (<code>HCodeElement</code>). */
	protected abstract FACT out(N hce, FACT in);
	
	/** Look up the FACT for the Node, adding the initial value to the
	 *  map if no value was present. */
	private final FACT lookup(N hce, Map<N,FACT> map) {
	    if (!map.containsKey(hce))
		map.put(hce, init(hce));
	    return map.get(hce);
	}
	public final Map<N,FACT> compute(Graph<N,E> g) {
	    final Map<N,FACT> OUTresult = new HashMap<N,FACT>();
	    final WorkSet<N> worklist = new WorkSet<N>(g.nodes());
	    while (!worklist.isEmpty()) {
		N hce = worklist.removeFirst();
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
	    final Map<N,FACT> INresult = new HashMap<N,FACT>();
	    for (Iterator<N> it=g.nodes().iterator(); it.hasNext(); ) {
		N hce = it.next();
		INresult.put(hce, computeIN(hce, OUTresult));
	    }
	    // now really done.
	    return INresult;
	}
	private final FACT computeIN(N hce, Map<N,FACT> outMap) {
	    Iterator<E> it=hce.predC().iterator();
	    if (!it.hasNext()) return init(hce);
	    FACT f = lookup(it.next().from(), outMap);
	    while (it.hasNext())
		f = join(f, lookup(it.next().from(), outMap));
	    return f;
	}
    }
}
