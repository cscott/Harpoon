// SyncRemover.java, created Wed Jul  9 11:57:51 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DynamicSyncRemoval;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadValueVisitor;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
import net.cscott.jutil.WorkSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <code>SyncRemover</code> calls a "magic" native method to determine
 * if synchronization should be done on this object, and skips the
 * <code>MONITORENTER</code>/<code>MONITOREXIT</code> sequence if the
 * answer is no.  We need to duplication and split the code between
 * <code>MONITORENTER</code> and <code>MONITOREXIT</code> for this to
 * play nicely with the Transactions transformation.
 *
 * For best results, run <code>TreePostPass</code> after this
 * transformation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SyncRemover.java,v 1.5 2004-02-08 01:51:52 cananian Exp $
 */
public class SyncRemover
    extends harpoon.Analysis.Transformation.MethodMutator<Quad> {
    private final HMethod checkMethod;

    /** Creates a <code>SyncRemover</code>. */
    public SyncRemover(HCodeFactory parent, Linker l) {
	// force a caching quad-no-ssa code factory.
	this(new CachingCodeFactory(QuadNoSSA.codeFactory(parent)), l);
    }
    /** The private constructor knows that the code factory is cached and
     *  produces quad-no-ssa form. */
    private SyncRemover(CachingCodeFactory parent, Linker l) {
	super(parent);
	// get the reference to the discriminator method.
	checkMethod = l.forName("harpoon.Runtime.DynamicSyncImpl")
	    .getMethod("isNoSync",new HClass[]{l.forName("java.lang.Object")});
    }
    /** Return an <code>HCodeFactory</code> that will clean up the
     *  tree form of the transformed code by performing some optimizations
     *  which can't be represented in quad form. */
    public static HCodeFactory treeCodeFactory(Frame f, HCodeFactory hcf) {
	return new TreeCallOpt(f).codeFactory(hcf);
    }
    protected HCode<Quad> mutateHCode(HCodeAndMaps<Quad> input) {
	Code hc = (Code) input.hcode();
	METHOD qM = hc.getRootElement().method();
	AllocationInformationMap aim = (AllocationInformationMap)//heh heh
	    hc.getAllocationInformation();
	// recursively traverse graph, rewriting as we go.
	munge(new Munger(qM.getFactory(), aim), qM);
	return hc;
    }
    private void munge(Munger munger, Quad start) {
	WorkSet<Quad> toDo = new WorkSet<Quad>();
	Set<Quad> done = new HashSet<Quad>();
	toDo.add(start);
	while (!toDo.isEmpty()) {
	    Quad q = toDo.removeFirst();
	    if (done.contains(q)) continue;
	    for (Iterator<Edge> it=q.accept(munger).iterator(); it.hasNext();){
		Edge e = it.next();
		toDo.add(e.to());
	    }
	    done.add(q);
	}
    }

    private class Munger extends QuadValueVisitor<Collection<Edge>> {
	final Collection<Edge> NO_EDGES = Arrays.asList(new Edge[0]);
	final AllocationInformationMap aim;
	final QuadFactory qf;
	final TempFactory tf;
	Munger(QuadFactory qf, AllocationInformationMap aim) {
	    this.qf = qf;
	    this.tf = qf.tempFactory();
	    this.aim = aim;
	}

	public Collection<Edge> visit(Quad q) {
	    return q.succC();
	}
	public Collection<Edge> visit(MONITOREXIT q) {
	    // don't visit beyond a monitorexit.
	    return NO_EDGES;
	}
	public Collection<Edge> visit(MONITORENTER q) {
	    // rewrite this monitor enter, recurse into contents, and
	    // then continue after block.

	    Collection<Edge> result = new ArrayList<Edge>();
	    // rewrite contents.
	    munge(this, q.next(0));
	    // now copy contents
	    CopyInfo ci = copyGraph(q, aim);
	    // now rewrite as follows:
	    
	    // MONITORENTER(lock)
	    //   statements
	    // MONITOREXIT(lock)
	    //    --- becomes ---
	    // if (isSync(lock)) {
	    //    MONITORENTER(lock)
	    //    statements
	    //    MONITOREXIT(lock)
	    // } else {
	    //    statements
	    // }
	    Temp retval = new Temp(tf, "isNoSync");
	    Temp retex = new Temp(tf, "ignore");
	    Edge in = q.prevEdge(0);
	    CALL q0 = new CALL(qf, q, checkMethod, new Temp[] { q.lock() },
			       retval, retex, false, false, new Temp[0]);
	    CJMP q1 = new CJMP(qf, q, retval, new Temp[0]);
	    PHI  q2 = new PHI(qf, q, new Temp[0], 2);
	    in = addAt(in, q0);
	    in = addAt(in, q1); // monitorenter on 'false' edge
	    in = addAt(in, q2);
	    Quad.addEdge(q0, 1, q2, 1);
	    // add edge past copied monitorenter on 'true' edge.
	    Edge e = ci.start.copy.nextEdge(0);
	    Quad.addEdge(q1, 1, e.to(), e.which_pred());
	    
	    // now for every MONITOREXIT in copyMap, add a phi like so:
	    //
	    //    orig1               orig1           ...
	    //      \                  \              /
	    //      MONITOREXIT ->     MONITOREXIT  copy1
	    //        \                          \  /
	    //        orig2                        PHI
	    //                                     |
	    //                                   orig2
	    for (Iterator<CopyPair<MONITOREXIT>> it = ci.endList.iterator();
		 it.hasNext(); ) {
		CopyPair<MONITOREXIT> exit = it.next();
		PHI qP = new PHI(qf, exit.orig, new Temp[0], 2);
		addAt(exit.orig.nextEdge(0), qP);
		// now add edge from before copied monitorexit
		Edge ee = exit.copy.prevEdge(0);
		Quad.addEdge(ee.from(), ee.which_succ(), qP, 1);
		// add to our successors list
		result.addAll(qP.succC());
	    }
	    // done!
	    return result;
	}
	/** helper routine to add a quad on an edge. */
	private Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
	/** helper routine to add a quad on an edge. */
	private Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	    Quad frm = e.from(); int frm_succ = e.which_succ();
	    Quad to  = e.to();   int to_pred = e.which_pred();
	    Quad.addEdge(frm, frm_succ, q, which_pred);
	    Quad.addEdge(q, which_succ, to, to_pred);
	    return to.prevEdge(to_pred);
	}
    }
    private static class CopyPair<T> {
	public final T orig, copy;
	CopyPair(T orig, T copy) { this.orig = orig; this.copy = copy; }
    }
    private static class CopyInfo {
	// left of pair is orig; right of pair is copy
	final CopyPair<MONITORENTER> start;
	final List<CopyPair<MONITOREXIT>> endList =
	    new ArrayList<CopyPair<MONITOREXIT>>();
	CopyInfo(MONITORENTER origStart, MONITORENTER copyStart) {
	    this.start = new CopyPair<MONITORENTER>(origStart, copyStart);
	}
    }
    private static CopyInfo copyGraph(MONITORENTER start,
				      AllocationInformationMap aim) {
	WorkSet<Quad> frontier = new WorkSet<Quad>();
	List<Edge> toLink = new ArrayList<Edge>();
	Map<Quad,Quad> copyMap = new LinkedHashMap<Quad,Quad>();

	// initialize: deal with start node.
	CopyInfo result = new CopyInfo
	    (start, (MONITORENTER)
	     start.rename(identityTempMap, identityTempMap));
	copyMap.put(result.start.orig, result.start.copy);
	frontier.addAll(Arrays.asList(result.start.orig.next()));
	toLink.addAll(result.start.orig.succC());

	while(!frontier.isEmpty()) {
	    Quad q = frontier.removeFirst();
	    if (copyMap.containsKey(q)) continue;
	    // handle subgraphs first.
	    if (q instanceof MONITORENTER) {
		// this is a subgraph: recurse.
		CopyInfo ci = copyGraph((MONITORENTER)q, aim);
		// now merge the info, including the mapping for q
		copyMap.put(ci.start.orig, ci.start.copy);
		// continue from the subgraph's exit points.
		for (Iterator<CopyPair<MONITOREXIT>> it=ci.endList.iterator();
		     it.hasNext(); ) {
		    CopyPair<MONITOREXIT> exit = it.next();
		    frontier.addAll(Arrays.asList(exit.orig.next()));
		    toLink.addAll(exit.orig.succC());
		    copyMap.put(exit.orig, exit.copy);
		}
		continue;
	    }
	    // otherwise, clone q:
	    Quad qq = q.rename(identityTempMap, identityTempMap);
	    // add to the mapping.
	    copyMap.put(q, qq);
	    // clone allocation properties
	    if (q instanceof ANEW || q instanceof NEW)
		if (aim!=null) aim.transfer(qq, q, identityTempMap, aim);
	    // find connected quads.
	    if (q instanceof MONITOREXIT) {
		// don't go past MONITOREXIT, but add it to the endList
		result.endList.add(new CopyPair<MONITOREXIT>
				   ((MONITOREXIT)q, (MONITOREXIT)qq));
	    } else {
		// okay, it's normal, keep going through.
		frontier.addAll(Arrays.asList(q.next()));
		// note that we need to link this up eventually
		toLink.addAll(q.succC());
	    }
	}
	// now link all the outgoing edges.
	for (Iterator<Edge> it = toLink.iterator(); it.hasNext(); ) {
	    Edge e = it.next();
	    Quad.addEdge(copyMap.get(e.from()), e.which_succ(),
			 copyMap.get(e.to()), e.which_pred());
	}
	// done!
	return result;
    }
    private static final TempMap identityTempMap = new TempMap() {
	    public Temp tempMap(Temp t) { return t; }
	};
}
