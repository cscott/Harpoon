// Peephole.java, created Sun Dec 27 19:37:24 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Set;
import harpoon.Util.HashSet;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.util.Stack;
import java.util.Enumeration;
/**
 * <code>Peephole</code> performs peephole optimizations (mostly
 * <code>MOVE</code> collation) on <code>QuadWithTry</code> and
 * <code>QuadNoSSA</code> forms.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Peephole.java,v 1.1.2.9.2.3 1999-09-19 15:32:58 cananian Exp $
 */

final class Peephole  {
    final static void normalize(Quad head) {
	SwapVisitor sv = new SwapVisitor();
	sv.optimize(head); // usually things are screwy after trans.
    }
    final static void optimize(Quad head, boolean allowFarMoves) {
	WorkSet protectedquads=new WorkSet();
	if (!allowFarMoves) {
	    METHOD m=(METHOD)head.next(1);
	    for (int i=1;i<m.next().length;i++) {
		Enumeration enum=((HANDLER) m.next(i)).protectedQuads();
		while (enum.hasMoreElements())
		    protectedquads.add(enum.nextElement());
	    }
	}
	PeepholeVisitor pv = new PeepholeVisitor(allowFarMoves, protectedquads);
	while (pv.optimize(head))
	    /*repeat*/;
    }
    private static class CheckStack extends Stack {
	public Object push(Object o) {
	    Util.assert(o!=null);
	    Quad[] ql = ((Quad)o).next();
	    for (int i=0; i<ql.length; i++)
		Util.assert(ql[i]!=null);
	    return super.push(o);
	}
    }
    private abstract static class SuperVisitor extends QuadVisitor {
	final Stack todo = new CheckStack();
	final Set visited = new HashSet();
	boolean changed = false;

	public boolean optimize(Quad head) {
	    changed = false;
	    todo.setSize(0);
	    visited.clear();

	    todo.push(head);
	    while (!todo.empty()) {
		Quad q = (Quad) todo.pop();
		if (!visited.contains(q)) {
		    q.accept(this);
		}
	    }
	    return changed;
	}
    }
    private final static class SwapVisitor extends SuperVisitor {
	public void visit(Quad q) {
	    Quad[] ql=q.next();
	    // very specific case for swaparoo.
	    if (ql.length==1 && ql[0] instanceof MOVE &&
		q.def().length==1 &&
		q.def()[0]==((MOVE)ql[0]).src()) {
		MOVE Qm = (MOVE)ql[0];
		TempMap tm0 = new OneToOneMap(Qm.src(), Qm.dst());
		TempMap tm1 = new OneToOneMap(Qm.dst(), Qm.src());
		Quad q1 = q.rename(q.qf, tm0, null);
		Quad q2 = Qm.rename(Qm.qf, tm1, tm0);
		Quad.replace(q, q1);
		Quad.replace(Qm, q2);
		visited.union(q1); visited.union(q2);
		todo.push(q2.next(0));
		changed=true;
	    } else {
		// garden variety instruction.
		for (int i=0; i<ql.length; i++)
		    todo.push(ql[i]);
		visited.union(q);
	    }
	}
    }
    private final static class PeepholeVisitor extends SuperVisitor {
	final boolean allowFarMoves;
	final WorkSet pquads;
	PeepholeVisitor(boolean afm, WorkSet pquads) { 
	    this.allowFarMoves=afm; 
	    this.pquads=pquads;}
	public void visit(Quad q) {
	    Quad[] ql=q.next();
	    for (int i=0; i<ql.length; i++)
		todo.push(ql[i]);
	    visited.union(q);
	}
	public void visit(final MOVE q) {
	    final Quad Qnext = q.next(0);
	    if (q.dst() == q.src()) {
		// dst==src, delete.
		todo.push(unlink(q));
		changed=true;
	    } else if (isMember(q.dst(), Qnext.def())) {
		// rename Qnext & delete MOVE.
		TempMap tm = new OneToOneMap(q.dst(), q.src());
		Quad.replace(Qnext, Qnext.rename(Qnext.qf, null, tm));
		// unlink MOVE
		todo.push(unlink(q));
		changed=true;
	    } else {
		// see if we can move the MOVE forward.
		//  (we can move it ahead until someone redefines our src/def)
		Quad Qp; boolean pastNonMove=false;
		boolean moveit=false, deleteit=false;
		for (Qp=Qnext; true; Qp=Qp.next(0)) {
		    // unless allowFarMoves==true, we stop as soon as
		    // we come to a 'protected' quad.
		    if (!allowFarMoves)
			if (pquads.contains(Qp)) {
			    if (pastNonMove) moveit=true;
			    break;
			}
			    

		    if (Qp instanceof PHI || Qp instanceof SIGMA) {
			// sorry, only optimize within basic blocks.
			if (pastNonMove) moveit=true;
			break;
		    }

		    if (Qp instanceof FOOTER) {
			// move isn't useful after return!
			deleteit=true;
			break;
		    }
		    if (isMember(q.src(), Qp.def())) {
			// destroys source temp, so stop.
			if (pastNonMove) moveit=true;
			break;
		    }
		    if (isMember(q.dst(), Qp.def())) {
			// destroys def, so move isn't useful anymore.
			if (pastNonMove) moveit=true;
			// BE CAREFUL: Qp may *use* q.dst(), so 'deleteit' is
			// not correct.
			break;
		    }
		    if (!(Qp instanceof MOVE)) pastNonMove=true; 
		}
		// we're going to unlink and move this quad if:
		//   -- moveit is true, or
		//   -- deleteit is true, or
		//   -- Qp is a 'safe' sigma function where safe means:
		//      - not a CALL
		//      - not a protected quad.
		// note that we'll move quads even if Qp is unsafe, if
		// moveit is true.  deleteit can't be true if Qp is a
		// SIGMA.
		if (moveit || deleteit ||
		    (Qp instanceof SIGMA &&
		     !(Qp instanceof CALL || pquads.contains(Qp)))) {
		    TempMap tm = new OneToOneMap(q.dst(), q.src());
		    Edge lstE;
		    for (lstE=q.nextEdge(0); lstE.to() != Qp; ) {
			Quad qq = (Quad)lstE.to();
			Quad.replace(qq, qq.rename(qq.qf, null, tm));
			lstE=((Quad)lstE.from()).next(0).nextEdge(0);
		    }
		    todo.push(unlink(q));
		    // usually we relink the MOVE quad before Qp, but
		    // we'll relink *after* a sigma if we can -- this
		    // helps keep the MOVEs propagating.  Relink *after*
		    // if sigma doesn't redefine the source of the MOVE
		    // and if sigma isn't protected.
		    if (Qp instanceof SIGMA && !pquads.contains(Qp) &&
			!(Qp instanceof CALL &&
			  (q.src()==((CALL)Qp).retval() ||
			   q.src()==((CALL)Qp).retex()))) {
			SIGMA Qs = (SIGMA) Qp.rename(Qp.qf, null, tm);
			todo.removeElement(Qp); // remove old SIGMA from todo
			Quad.replace(Qp, Qs);
			// we can just delete the MOVE if the CALL
			// overwrites the destination.
			if (Qp instanceof CALL &&
			    (q.dst() == ((CALL)Qp).retval() ||
			     q.dst() == ((CALL)Qp).retex())) {
			    // the move is gone & we don't need it any mo'
			    changed=true;
			    return;
			}
			HandlerSet hs=q.handlers();
			Edge[] el = Qs.nextEdge();
			for (int i=0; i<el.length; i++) {
			    MOVE Qm = (i==0) ? q : (MOVE)q.clone();
			    Quad.addEdge((Quad)el[i].from(),el[i].which_succ(),
					 Qm, 0);
			    Quad.addEdge(Qm, 0,
					 (Quad)el[i].to(), el[i].which_pred());
			    // those nasty HANDLERs
			    if (i==0) continue; // skip next part.
			    for (HandlerSet hsp=hs; hsp!=null; hsp=hsp.next)
				hsp.h.protectedSet.insert(Qm);
			}
		    } else if (moveit) { // relink before Qend
			Quad.addEdge((Quad)lstE.from(),lstE.which_succ(), q,0);
			Quad.addEdge(q,0, (Quad)lstE.to(), lstE.which_pred());
		    } else Util.assert(deleteit);
		    changed=true;
		} else {
		    // do nothing.
		    visited.union(q);
		    todo.push(Qnext);
		}
	    }
	}
	private static Quad unlink(Quad q) {
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    Quad.addEdge((Quad)in.from(), in.which_succ(),
			 (Quad)out.to(), out.which_pred());
	    return (Quad)out.to();
	}
	private static boolean isMember(Temp t, Temp[] Tset) {
	    for (int i=0; i<Tset.length; i++)
		if (t == Tset[i]) return true;
	    return false;
	}
    }
    private static class OneToOneMap implements TempMap {
	final Temp from;
	final Temp to;
	OneToOneMap(Temp from, Temp to) { this.from=from; this.to=to; }
	public Temp tempMap(Temp t) { return (t==from) ? to : t; }
    }
}
