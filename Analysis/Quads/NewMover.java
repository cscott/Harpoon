// NewMover.java, created Wed Nov  7 22:20:28 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.Transformation.*;
import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Temp.*;
import harpoon.Util.*;

import java.util.*;
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
 * @version $Id: NewMover.java,v 1.1.2.1 2001-11-08 18:08:52 cananian Exp $
 */
public class NewMover extends MethodMutator {
    /** Creates a <code>NewMover</code> that uses the given
     *  <code>HCodeFactory</code> <code>hcf</code>. */
    public NewMover(HCodeFactory hcf) {
	super(new CachingCodeFactory(/*QuadWithTry.codeFactory*/(hcf)));
    }
    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	HEADER header = (HEADER) hc.getRootElement();
	traverseBlock(new MoveVisitor(), header.nextEdge(1));
	return hc;
    }
    // recursive traversal of all paths.
    void traverseBlock(MoveVisitor mv, Edge in) {
	mv.from = in;
	mv.done = false;
	Util.assert(mv.movingNews.isEmpty());
	Util.assert(mv.movingMoves.isEmpty());
	while (!mv.done)
	    ((Quad)mv.from.to()).accept(mv);
	// okay, recursively invoke on the one that stopped us.
	Quad stopper = (Quad) mv.from.to();
	if (stopper instanceof FOOTER) return; // end-of-the-line!
	if (mv.seen.add(stopper))
	    // haven't started with this one before, so do i!
	    for (int i=0; i<stopper.nextLength(); i++)
		traverseBlock(mv, stopper.nextEdge(i));
    }
    class MoveVisitor extends QuadVisitor {
	Edge from; boolean done;
	Map movingNews = new HashMap();
	Map movingMoves = new HashMap();
	Set seen = new HashSet();
	public void visit(Quad q) {
	    // if this quad uses any NEWs which are in 'movingNews',
	    // or any MOVEs which are in 'movingMoves', then
	    // drop them before here. (moves follow news)
	    Util.assert(q.prevLength()==1);
	    Edge e = q.prevEdge(0);
	    // find MOVEs first.
	    for (Iterator it=q.useC().iterator(); it.hasNext(); ) {
		Temp t = (Temp) it.next();
		if (movingMoves.containsKey(t)) {
		    MOVE qM = (MOVE) movingMoves.remove(t);
		    e = addReversedAt(e, qM);
		}
	    }
	    // now add NEWs before the MOVEs.
	    for (Iterator it=q.useC().iterator(); it.hasNext(); ) {
		Temp t = (Temp) it.next();
		if (movingNews.containsKey(t)) {
		    NEW qN = (NEW) movingNews.remove(t);
		    e = addAt(e, qN);
		}
	    }
	    // done.
	    Util.assert(q.nextLength()==1);
	    from = q.nextEdge(0);
	}
	public void visit(NEW q) {
	    // pick this guy up
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    from = Quad.addEdge((Quad)in.from(), in.which_succ(),
				(Quad)out.to(), out.which_pred());
	    // note that q is still in handler sets, which is what we want.
	    movingNews.put(q.dst(), q);
	}
	public void visit(MOVE q) {
	    // if this MOVE uses the result of a NEW which we're moving,
	    // pick it up, too.
	    if (movingNews.containsKey(q.src()) ||
		false/*movingMoves.containsKey(q.src())*/) {
		// pick me up!
		Edge in = q.prevEdge(0), out = q.nextEdge(0);
		from = Quad.addEdge((Quad)in.from(), in.which_succ(),
				    (Quad)out.to(), out.which_pred());
		movingMoves.put(q.dst(), q);
	    } else visit((Quad) q); // fallback to boring.
	}
	void visitPhiSigma(Quad q) {
	    // stop here, dump all moving MOVEs and NEWs on 'from'
	    Edge e = from;
	    for (int i=0; i<2; i++) {
		Map m = (i==0) ? movingNews : movingMoves; // news first.
		for (Iterator it=m.values().iterator(); it.hasNext(); )
		    e = addAt(e, (Quad) it.next());
		m.clear();
	    }
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
	    visitPhiSigma(q);
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
	    movingNews.clear();
	    movingMoves.clear();
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
