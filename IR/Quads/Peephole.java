// Peephole.java, created Sun Dec 27 19:37:24 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Tuple;
import harpoon.Util.Collections.UniqueStack;
import harpoon.Util.Util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * <code>Peephole</code> performs peephole optimizations (mostly
 * <code>MOVE</code> collation) on <code>QuadWithTry</code> and
 * <code>QuadNoSSA</code> forms.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Peephole.java,v 1.3.2.1 2002-02-27 08:36:33 cananian Exp $
 */

final class Peephole  {
    final static void normalize(Quad head) {
	SwapVisitor sv = new SwapVisitor(null, head);
	sv.optimize(head); // usually things are screwy after trans.
    }

    final static void normalize(Quad head, Map typemap) {
	SwapVisitor sv = new SwapVisitor(typemap, head);
	sv.optimize(head); // usually things are screwy after trans.
    }

    final static void optimize(Quad head) {
	optimize(head, null);
    }

    final static void optimize(Quad head, Map typemap) {
	PeepholeVisitor pv = new PeepholeVisitor(typemap, head);
	while (pv.optimize(head))
	    /*repeat*/;
    }

    private static class CheckStack extends UniqueStack {
	public void push(Object o) {
	    assert o!=null;
	    Quad[] ql = ((Quad)o).next();
	    for (int i=0; i<ql.length; i++)
		assert ql[i]!=null;
	    super.push(o);
	}
    }
    private abstract static class SuperVisitor extends QuadVisitor {
	final METHOD method;
	SuperVisitor(Quad head) { this.method = (METHOD) head.next(1); }

	final UniqueStack todo = new CheckStack();
	final Set visited = new HashSet();
	boolean changed = false;

	public boolean optimize(Quad head) {
	    changed = false;
	    todo.clear();
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
	protected boolean sameHandlers(Quad q1, Quad q2) {
	    for (int i=1; i<method.nextLength(); i++) {
		HANDLER handler = (HANDLER) method.next(i);
		if (handler.isProtected(q1) != handler.isProtected(q2))
		    return false;
	    }
	    return true;
	}
    }
    private final static class SwapVisitor extends SuperVisitor {
	private Map typemap;

	public SwapVisitor(Map typemap, Quad head) {
	    super(head);
	    this.typemap=typemap;
	}

	public void visit(Quad q) {
	    Quad[] ql=q.next();
	    // very specific case for swaparoo:
	    //  tA = oper ..  -\  tB = oper ..
	    //  tB = MOVE tA  -/  tA = MOVE tB
	    // ONLY VALID IF BOTH ARE IN SAME HANDLER CONTEXT!
	    if (ql.length==1 && ql[0] instanceof MOVE &&
		!(q instanceof METHOD) && q.def().length==1 &&
		q.def()[0]==((MOVE)ql[0]).src() &&
		sameHandlers(q, ql[0])) {
		MOVE Qm = (MOVE)ql[0];
		TempMap tm0 = new OneToOneMap(Qm.src(), Qm.dst());
		TempMap tm1 = new OneToOneMap(Qm.dst(), Qm.src());
		Quad q1 = q.rename(q.qf, tm0, null);
		Quad q2 = Qm.rename(Qm.qf, tm1, tm0);
		if (typemap!=null) {
		    typemap.put(new Tuple(new Object[]{q1,Qm.dst() }),
				typemap.get(new Tuple(new Object[]{Qm, Qm.dst()})));
		    Temp uses[]=q.use();
		    for (int i=0;i<uses.length;i++)
			typemap.put(new Tuple(new Object[]{q1, uses[i]}),
				    typemap.get(new Tuple(new Object[] {q, uses[i]})));
		    typemap.put(new Tuple(new Object[]{q2, Qm.src()}),
				typemap.get(new Tuple(new Object[] {Qm, Qm.src()})));
		    typemap.put(new Tuple(new Object[]{q2, Qm.dst()}),
				typemap.get(new Tuple(new Object[] {Qm, Qm.dst()})));

		}
		replace(q,  q1);
	        replace(Qm, q2);
		visited.add(q1); visited.add(q2);
		todo.push(q2.next(0));
		changed=true;
	    } else {
		// garden variety instruction.
		for (int i=0; i<ql.length; i++)
		    todo.push(ql[i]);
		visited.add(q);
	    }
	}
    }
    private final static class PeepholeVisitor extends SuperVisitor {
	private Map typemap;

	PeepholeVisitor(Map typemap, Quad head) { 
	    super(head);
	    this.typemap=typemap;
	}

	public void visit(Quad q) {
	    Quad[] ql=q.next();
	    for (int i=0; i<ql.length; i++)
		todo.push(ql[i]);
	    visited.add(q);
	}

	void fixmap(Quad old, Quad newq, TempMap tm) {
	    if (typemap!=null) {
		Temp[] defs=old.def();
		for (int i=0;i<defs.length;i++) {
		    typemap.put(new Tuple(new Object[]{newq, defs[i]}),
				typemap.get(new Tuple(new Object[]{old, defs[i]})));
		}
		Temp[] uses=old.use();
		for (int i=0;i<uses.length;i++) {
		    if (tm==null)
			typemap.put(new Tuple(new Object[]{newq, uses[i]}),
				    typemap.get(new Tuple(new Object[]{old, uses[i]})));
		    else
			typemap.put(new Tuple(new Object[]{newq, tm.tempMap(uses[i])}),
				    typemap.get(new Tuple(new Object[]{old, uses[i]})));
		}
	    }
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
		Quad newquad=Qnext.rename(Qnext.qf, null, tm);
		fixmap(Qnext, newquad, tm);
		replace(Qnext, newquad);
		// unlink MOVE
		todo.push(unlink(q));
		changed=true;
	    } else {
		// see if we can move the MOVE forward.
		//  (we can move it ahead until someone redefines our src/def)
		Quad Qp; boolean pastNonMove=false;
		boolean moveit=false, deleteit=false;
		for (Qp=Qnext; true; Qp=Qp.next(0)) {
		    if (Qp instanceof FOOTER) {
			// move isn't useful after return!
			deleteit=true;
			break;
		    }
		    // we stop as soon as we come to:
		    //  - a quad belonging to a different handler set
		    //  - a PHI or SIGMA (only optimize within basic blocks)
		    //  - a statement which destroys the source temp
		    if (!sameHandlers(q, Qp) ||
			Qp instanceof PHI || Qp instanceof SIGMA ||
			isMember(q.src(), Qp.def())) {
			if (pastNonMove) moveit=true;
			// enable more moves
			//if (Qp instanceof SIGMA) moveit=true;
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
		//      - belongs to same handlers as q
		// note that we'll move quads even if Qp is unsafe, if
		// moveit is true.  deleteit can't be true if Qp is a
		// SIGMA.
		if (moveit || deleteit) {
		    TempMap tm = new OneToOneMap(q.dst(), q.src());
		    Edge lstE;
		    for (lstE=q.nextEdge(0); lstE.to() != Qp; ) {
			Quad qq = (Quad)lstE.to();
			Quad newquad=qq.rename(qq.qf, null, tm);
			fixmap(qq,newquad,tm);
			replace(qq, newquad);
			lstE=((Quad)lstE.from()).next(0).nextEdge(0);
		    }
		    HandlerSet hs=q.handlers();
		    todo.push(unlink(q));
		    // usually we relink the MOVE quad before Qp, but
		    // we'll relink *after* a sigma if we can -- this
		    // helps keep the MOVEs propagating.  Relink *after*
		    // if sigma doesn't redefine the source of the MOVE
		    // and if sigma belongs to the same handler region.
		    if (Qp instanceof SIGMA && sameHandlers(q, Qp) &&
			!(Qp instanceof CALL &&
			  (q.src()==((CALL)Qp).retval() ||
			   q.src()==((CALL)Qp).retex()))) {
			SIGMA Qs = (SIGMA) Qp.rename(Qp.qf, null, tm);
			fixmap(Qp, Qs, tm);
			todo.removeElement(Qp); // remove old SIGMA from todo
			replace(Qp, Qs);
			// we can just delete the MOVE if the CALL
			// overwrites the destination.
			if (Qp instanceof CALL &&
			    (q.dst() == ((CALL)Qp).retval() ||
			     q.dst() == ((CALL)Qp).retex())) {
			    // the move is gone & we don't need it any mo'
			    changed=true;
			    return;
			}
			Edge[] el = Qs.nextEdge();
			for (int i=0; i<el.length; i++) {
			    MOVE Qm = (i==0) ? q : (MOVE)q.clone();
			    if (i!=0)
				fixmap(q, Qm, null);
			    Quad.addEdge((Quad)el[i].from(),el[i].which_succ(),
					 Qm, 0);
			    Quad.addEdge(Qm, 0,
					 (Quad)el[i].to(), el[i].which_pred());
			    // those nasty HANDLERs
			    Qm.addHandlers(hs);
			}
		    } else if (moveit) { // relink before Qend
			Quad.addEdge((Quad)lstE.from(),lstE.which_succ(), q,0);
			Quad.addEdge(q,0, (Quad)lstE.to(), lstE.which_pred());
			q.addHandlers(hs);
		    } else assert deleteit;
		    changed=true;
		} else {
		    // do nothing.
		    visited.add(q);
		    todo.push(Qnext);
		}
	    }
	}
	private static Quad unlink(Quad q) {
	    Edge in = q.prevEdge(0), out = q.nextEdge(0);
	    Quad.addEdge((Quad)in.from(), in.which_succ(),
			 (Quad)out.to(), out.which_pred());
	    q.removeHandlers(q.handlers());
	    return (Quad)out.to();
	}
	private static boolean isMember(Temp t, Temp[] Tset) {
	    for (int i=0; i<Tset.length; i++)
		if (t == Tset[i]) return true;
	    return false;
	}
    }
    // replace oldQ with newQ, updating handlers, too.
    private static void replace(Quad oldQ, Quad newQ) {
	Quad.replace(oldQ, newQ);
	Quad.transferHandlers(oldQ, newQ);
    }
    // simple temp mapping.
    private static class OneToOneMap implements TempMap {
	final Temp from;
	final Temp to;
	OneToOneMap(Temp from, Temp to) { this.from=from; this.to=to; }
	public Temp tempMap(Temp t) { return (t==from) ? to : t; }
    }
}
