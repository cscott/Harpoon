// NewMover.java, created Wed Nov  7 22:20:28 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.SIGMA;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.GenericInvertibleMultiMap;
import harpoon.Util.Collections.InvertibleMultiMap;
import harpoon.Util.Util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * The <code>NewMover</code> class moves <code>NEW</code> operations
 * as close as possible to the <code>CALL</code> to their initializers.
 * In java bytecode, these two operations can be arbitrarily far apart
 * in expressions such as:
 * <ul><li><code>new Foo(new Bar(blah(), 2+5, ...), ...)</code></li></ul>
 * <p>
 * This movement is illegal if a <code>OutOfMemoryException</code> may
 * ever be thrown, as moving the <code>NEW</code> changes where the
 * exception would be thrown.  However, FLEX operates under the assumption
 * that such exceptions are never thrown.
 * <p>
 * <code>NewMover</code> works best on <code>QuadWithTry</code> form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NewMover.java,v 1.3 2002-02-26 22:41:42 cananian Exp $
 */
public class NewMover extends MethodMutator {
    /** If true, then the NewMover will attempt to move NEWs across SIGMAs.
     *  This yields better results when applied to QuadNoSSA form, but
     *  increases code size w/ no appreciable benefit on QuadWithTry form. */
    private static final boolean movePastSigmas=false;
    /** Creates a <code>NewMover</code> that uses the given
     *  <code>HCodeFactory</code> <code>hcf</code>. */
    public NewMover(HCodeFactory hcf) {
	super(new CachingCodeFactory(/*QuadWithTry.codeFactory*/(hcf)));
    }
    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	HEADER header = (HEADER) hc.getRootElement();
	METHOD method = (METHOD) header.next(1);
	// traverse all edges from method (handler blocks and regular code)
	MoveVisitor mv = new MoveVisitor();
	for (int i=0; i<method.nextLength(); i++) {
	    mv.state = new State();
	    traverseBlock(mv, method.nextEdge(i));
	}
	// done!
	return hc;
    }
    // recursive traversal of all paths.
    void traverseBlock(MoveVisitor mv, Edge in) {
	mv.from = in;
	mv.done = false;
	while (!mv.done)
	    ((Quad)mv.from.to()).accept(mv);
	// okay, recursively invoke on the one that stopped us.
	Quad stopper = (Quad) mv.from.to();
	if (stopper instanceof FOOTER) return; // end-of-the-line!
	if (mv.seen.add(stopper)) {
	    // haven't started with this one before, so do it!
	    State s = mv.state;
	    for (int i=0; i<stopper.nextLength(); i++) {
		mv.state = (i<stopper.nextLength()-1) ? new State(s) : s;
		traverseBlock(mv, stopper.nextEdge(i));
	    }
	} else Util.ASSERT(stopper instanceof PHI);
    }
    static class State {
	/** Set of NEWs we are moving, maintained as a map from dst->NEW. */
	final Map moving;
	/** MultiMap of aliases for a given Temp (defined by a NEW) */
	final InvertibleMultiMap aliases;
	State() {
	    this.moving=new HashMap();
	    this.aliases=new GenericInvertibleMultiMap();
	}
	State(State s) {
	    this.moving=new HashMap();
	    this.aliases=new GenericInvertibleMultiMap(s.aliases);
	    // copy/clone mappings from s.moving.
	    for (Iterator it=s.moving.values().iterator(); it.hasNext(); ) {
		NEW oldQ = (NEW) it.next();
		NEW newQ = (NEW) oldQ.clone();
		newQ.addHandlers(oldQ.handlers());
		this.moving.put(newQ.dst(), newQ);
	    }
	}
	public Object clone() { return new State(this); }
	public String toString() { return moving+" / "+aliases; }
    }
    class MoveVisitor extends QuadVisitor {
	Edge from; boolean done;
	State state = new State();
	Set seen = new HashSet();
	public void visit(Quad q) {
	    // if this quad uses any NEWs which are in 'moving',
	    // (or any temps which are in aliases.values())
	    // drop them before here. (MOVEs to aliases follow NEWs)
	    Util.ASSERT(q.prevLength()==1);
	    Edge e = q.prevEdge(0);
	    for (Iterator it=q.useC().iterator(); it.hasNext(); ) {
		Temp t = (Temp) it.next();
		if (!state.aliases.values().contains(t)) continue; //boring.
		// unmap alias to canonical temp (defined by NEW)
		Temp src = (Temp) state.aliases.invert().get(t);
		Util.ASSERT(state.aliases.invert().getValues(t).size()==1);
		// dump it!
		e = dumpOne(e, src);
		state.moving.remove(src);
		state.aliases.remove(src);
	    }
	    // done.
	    Util.ASSERT(q.nextLength()==1);
	    from = q.nextEdge(0);
	}
	public void visit(NEW q) {
	    // pick this guy up
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    from = Quad.addEdge((Quad)in.from(), in.which_succ(),
				(Quad)out.to(), out.which_pred());
	    // note that q is still in handler sets, which is what we want.
	    state.moving.put(q.dst(), q);
	    state.aliases.add(q.dst(), q.dst()); // always alias to itself.
	}
	public void visit(MOVE q) {
	    // if this MOVE uses the result of a NEW which we're moving,
	    // pick it up, too.
	    if (state.aliases.values().contains(q.src())) {
		// get 'real' source (filter through inverted aliasMap)
		Temp src = (Temp) state.aliases.invert().get(q.src());
		// add a new alias.
		state.aliases.add(src, q.dst());
		// remove this guy.
		from = q.remove();
	    } else visit((Quad) q); // fallback to boring.
	}
	Edge dumpOne(Edge e, Temp t) {
	    Util.ASSERT(state.moving.containsKey(t));
	    // first dump the NEW
	    NEW qN = (NEW) state.moving.get(t);
	    e = addAt(e, qN);
	    // then dump all associated MOVEs.
	    Temp src = qN.dst(); Util.ASSERT(t.equals(src));
	    for (Iterator it=state.aliases.getValues(src).iterator();
		 it.hasNext(); ) {
		Temp dst = (Temp) it.next();
		if (src.equals(dst)) continue;
		// no handler for the MOVE, but we don't care.
		e = addAt(e, new MOVE(qN.getFactory(), qN, dst, src));
	    }
	    // done!
	    return e;
	}
	void visitPhiSigma(Quad q) {
	    // stop here, dump all moving MOVEs and NEWs on 'from'
	    Edge e = from;
	    for (Iterator it=state.moving.keySet().iterator(); it.hasNext(); )
		e = dumpOne(e, (Temp)it.next());
	    state.moving.clear();
	    state.aliases.clear();
	}
	public void visit(PHI q) {
	    visitPhiSigma(q);
	    if (seen.add(q))
		// not seen before.  keep going with next edge.
		from = q.nextEdge(0);
	    else
		// seen before.  we're done with this.
		done = true;
	}
	public void visit(SIGMA q) {
	    // keep state, but bail out of the iteration to recurse.
	    if (!movePastSigmas)
		visitPhiSigma(q); // don't push past sigmas.
	    else { // check for vars used by sigma.
		Edge e = from;
		for (Iterator it=q.useC().iterator(); it.hasNext(); ) {
		    Temp t = (Temp) it.next();
		    if (!state.aliases.values().contains(t)) continue;//boring.
		    // unmap alias to canonical temp (defined by NEW)
		    Temp src = (Temp) state.aliases.invert().get(t);
		    Util.ASSERT(state.aliases.invert().getValues(t).size()==1);
		    // dump it!
		    e = dumpOne(e, src);
		    state.moving.remove(src);
		    state.aliases.remove(src);
		}
	    }
	    from = q.prevEdge(0);
	    done = true;
	}
	public void visit(CALL q) {
	    // calls aren't really sigmas in quad-with-try form.
	    if (q.retex()==null)
		visit((Quad)q); // an ordinary joe.
	    else
		visit((SIGMA)q); // a sigma!
	}
	public void visit(FOOTER q) {
	    // if we haven't used the MOVE/NEW by now, discard it.
	    for (Iterator it=state.moving.values().iterator(); it.hasNext(); ){
		NEW qN = (NEW) it.next();
		// remove the NEW from any handler sets, too.
		qN.removeHandlers(qN.handlers());
	    }
	    state.moving.clear();
	    state.aliases.clear();
	    done=true;
	}
    }
    // private helper functions.
    private static Edge addAt(Edge e, Quad q) { return addAt(e, 0, q, 0); }
    private static Edge addAt(Edge e, int which_pred, Quad q, int which_succ) {
	Quad frm = (Quad) e.from(); int frm_succ = e.which_succ();
	Quad to  = (Quad) e.to();   int to_pred = e.which_pred();
	Quad.addEdge(frm, frm_succ, q, which_pred);
	Quad.addEdge(q, which_succ, to, to_pred);
	return to.prevEdge(to_pred);
    }
    private static Edge addReversedAt(Edge e, Quad q) {
	return addReversedAt(e, 0, q, 0);
    }
    private static Edge addReversedAt(Edge e,
				      int which_pred, Quad q, int which_succ) {
	Quad frm = (Quad) e.from(); int frm_succ = e.which_succ();
	Quad to  = (Quad) e.to();   int to_pred = e.which_pred();
	Quad.addEdge(frm, frm_succ, q, which_pred);
	Quad.addEdge(q, which_succ, to, to_pred);
	return frm.nextEdge(frm_succ);
    }
}
