// ReHandler.java, created Tue Aug 3 23:30:32 1999 by bdemsky
// Copyright (C) 1998 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.IR.Quads.HANDLER.ProtectedSet;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
/**
 * <code>ReHandler</code> make exception handling implicit and adds
 * the <code>HANDLER</code> quads from the graph.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: ReHandler.java,v 1.1.2.3 1999-08-04 05:52:29 cananian Exp $
 */
final class ReHandler {
    // entry point.
    public static final Quad rehandler(final QuadFactory qf, final Code code) {
	final QuadMap qm = new QuadMap();
	final HEADER old_header = (HEADER)code.getRootElement();
	final METHOD old_method = (METHOD) old_header.next(1);
	final CloningTempMap ctm = new CloningTempMap(code.qf.tempFactory(),
						      qf.tempFactory());
	final ArrayList al = new ArrayList();
	final StaticState ss = new StaticState(qf, qm, ctm, al);
	visitAll(new Visitor(ss), old_header);
	// now qm contains mappings from old to new, we just have to link them.
	for (Iterator e = code.getElementsI(); e.hasNext(); ) {
	    Quad old = (Quad) e.next();
	    // link next.
	    Edge[] el = old.nextEdge();
	    for (int i=0; i<el.length; i++)
		Quad.addEdge(qm.getFoot((Quad)el[i].from()),el[i].which_succ(),
			     qm.getHead((Quad)el[i].to()), el[i].which_pred());
	}
	// fixup try blocks.
	Temp[] qMp = ((METHOD)qm.getHead(old_method)).params();
	final METHOD qM = new METHOD(qf, old_method, qMp,
				     1 + ss.al.size());
	final HEADER qH = (HEADER)qm.getHead(old_header);
	Quad.addEdge(qH, 1, qM, 0);
	Edge e = old_method.nextEdge(0);
	Quad.addEdge(qM, 0, qm.getHead((Quad)e.to()), e.which_pred());
	Iterator iterate=ss.al.iterator();
	int i=1;
	while (iterate.hasNext())
	    Quad.addEdge(qM, i++, (Quad)iterate.next(),0);
	return qH;
    }

    /** Recursively visit all quads starting at <code>start</code>. */
    private static final void visitAll(Visitor v, Quad start) {
	start.visit(v);
	final StaticState ss = v.ss;
	Util.assert(ss.qm.contains(start));
	Quad[] ql = start.next();
	for (int i=0; i<ql.length; i++) {
	    if (ss.qm.contains(ql[i])) continue; // skip if already done.
	    Visitor vv = (i==ql.length-1)? v : // don't clone if never reused
		new Visitor(ss);
	    visitAll(vv, ql[i]);
	}
    }
	

    /** mapping from old quads to new quads. */
    private static class QuadMap {
	final private Map h = new HashMap();
	void put(Quad old, Quad new_header, Quad new_footer) {
	    h.put(old, new Quad[] { new_header, new_footer });
	}
	Quad getHead(Quad old) {
	    Quad[] ql=(Quad[])h.get(old); return (ql==null)?null:ql[0];
	}
	Quad getFoot(Quad old) {
	    Quad[] ql=(Quad[])h.get(old); return (ql==null)?null:ql[1];
	}
	boolean contains(Quad old) { return h.containsKey(old); }
    }

    /** Static state for visitor. */
    private static final class StaticState {
	final QuadFactory qf;
	final QuadMap qm;
	final CloningTempMap ctm;
	final List al;
	StaticState(QuadFactory qf, QuadMap qm,
		    CloningTempMap ctm, List al) {
	    this.qf = qf; this.qm = qm; this.ctm = ctm;
	    this.al = al;
	}
    }

    /** Guts of the algorithm: map from old to new quads, putting the
     *  result in the QuadMap. */
    private static final class Visitor extends QuadVisitor {
	final QuadFactory qf;
	// which Temps are non-null/arrays of known length/integer constants
	// various bits of static state.
	final StaticState ss;

	Visitor(StaticState ss)
	{ this.qf = ss.qf; this.ss = ss; }

	/** By default, just clone and set all destinations to top. */
	public void visit(Quad q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    ss.qm.put(q, nq, nq);
	}

	public void visit(CALL q) {
	    Quad nq, head;
	    // if retex==null, add the proper checks.
	    if (q.retex()==null) nq=head=(Quad)q.clone(qf, ss.ctm);
	    else {
		head = new CALL(qf, q, q.method, Quad.map(ss.ctm, q.params()),
				Quad.map(ss.ctm, q.retval()), 
				null, q.isVirtual());
		Quad q0 = new CONST(qf, q, Quad.map(ss.ctm, q.retex()),
				    null, HClass.Void);
		ReProtection protlist=new ReProtection();
		protlist.insert(head);
		Quad newhandler = new HANDLER(qf, q, 
					      Quad.map(ss.ctm, q.retex()),
					      null, protlist);
		ss.al.add(newhandler);
		Temp[] dst=new Temp[0];
    		Quad phi = new PHI(qf, q, dst, 2);
       		Quad.addEdges(new Quad[] { head, q0, phi});
		Quad.addEdge(newhandler, 0, phi, 1);
		nq = phi;
	    }
	    ss.qm.put(q, head, nq);
	}
    }
    static final private class ReProtection extends HashSet
        implements ProtectedSet {
        ReProtection() { super(); }
        public boolean isProtected(Quad q) { return contains(q); }
        public void remove(Quad q) { super.remove(q); }
        public void insert(Quad q) { super.add(q); }
        public java.util.Enumeration elements() {
            return new harpoon.Util.IteratorEnumerator( iterator() );
        }       
    }
}


