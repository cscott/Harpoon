// ReHandler.java, created Tue Aug 3 23:30:32 1999 by bdemsky
// Copyright (C) 1998 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
import harpoon.IR.Quads.HANDLER.ProtectedSet;
import harpoon.Analysis.SSITOSSAMap;
import harpoon.Analysis.ToSSA;
import harpoon.Analysis.UseDef;
import harpoon.Util.WorkSet;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>ReHandler</code> make exception handling implicit and adds
 * the <code>HANDLER</code> quads from the graph.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: ReHandler.java,v 1.1.2.4 1999-08-10 21:58:21 bdemsky Exp $
 */
final class ReHandler {
    // entry point.
    public static final Quad rehandler(final QuadFactory qf, final Code code) {
	Code ncode=(Code)code.clone(code.getMethod());
	(new ToSSA(new SSITOSSAMap(ncode))).optimize(ncode);
	
	HashMapList handlermap=analyze(ncode);

	final QuadMap qm = new QuadMap();
	final HEADER old_header = (HEADER)ncode.getRootElement();
	final METHOD old_method = (METHOD) old_header.next(1);
	final CloningTempMap ctm = new CloningTempMap(ncode.qf.tempFactory(),
						      qf.tempFactory());
	final ArrayList al = new ArrayList();
	final StaticState ss = new StaticState(qf, qm, ctm, al);
	WorkSet callset=new WorkSet();
	visitAll(new Visitor(ss, callset,handlermap), old_header);
	// now qm contains mappings from old to new, we just have to link them.

	for (Iterator e = ncode.getElementsI(); e.hasNext(); ) {
	    Quad old = (Quad) e.next();
	    // link next.
	    Edge[] el = old.nextEdge();
	    for (int i=0; i<el.length; i++) {
		    Quad.addEdge(qm.getFoot((Quad)el[i].from()),el[i].which_succ(),
				 qm.getHead((Quad)el[i].to()), el[i].which_pred());
	    }
	}

	//--------------------
	Iterator iteratecall=callset.iterator();
	    
	while (iteratecall.hasNext()) {
	    CALL call=(CALL)iteratecall.next();
	    boolean linkold=true;
	    HandInfo next;
	    if(handlermap.containsKey(call)) {
		Iterator iterate=handlermap.get(call).iterator();
		while(iterate.hasNext()) {
		    ReProtection protlist=new ReProtection();
		    protlist.insert(qm.getHead(call));
		    List handlers=handlermap.get(call);
		    HandInfo nexth=(HandInfo)iterate.next();
		    if (nexth.defaultexit()) {
			Temp[] dst=new Temp[0];
			Quad phi = new PHI(qf, qm.getHead(call), dst, 2);
			Quad.addEdge(qm.getFoot(call),0, phi, 0);
			Quad.addEdge(qm.getHead(nexth.handler()).prev(nexth.handleredge()),
				     qm.getHead(nexth.handler()).prevEdge(nexth.handleredge()).which_pred(),phi,1);
			Quad.addEdge(phi, 0, qm.getHead(nexth.handler()), nexth.handleredge());
		    }
		    if (nexth.anyhandler()) {
			Quad newhandler = new HANDLER(qf, qm.getHead(call),
						      Quad.map(ss.ctm, call.retex()),
						      null, protlist);
			ss.al.add(newhandler);
			Temp[] dst=new Temp[0];
			Quad phi = new PHI(qf, qm.getHead(call), dst, 2);
			Quad.addEdge(newhandler,0, phi, 0);
			Quad.addEdge(qm.getHead(nexth.handler()).prev(nexth.handleredge()),
				     qm.getHead(nexth.handler()).prevEdge(nexth.handleredge()).which_pred(),phi,1);
			Quad.addEdge(phi, 0, qm.getHead(nexth.handler()), nexth.handleredge());
		    }
		    if (nexth.specificex()) {
			Quad newhandler = new HANDLER(qf, qm.getHead(call), 
						      Quad.map(ss.ctm, call.retex()),
						      null, protlist);
			ss.al.add(newhandler);
			Temp[] dst=new Temp[0];
			Quad phi = new PHI(qf, qm.getHead(call), dst, 2);
			Quad.addEdge(newhandler,0, phi, 0);
			Quad.addEdge(qm.getHead(nexth.handler()).prev(nexth.handleredge()),
				     qm.getHead(nexth.handler()).prevEdge(nexth.handleredge()).which_pred(),phi,1);
			Quad.addEdge(phi, 0, qm.getHead(nexth.handler()), nexth.handleredge());
		    }
		}  
	    }
	}

	//--------------------
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
    
    private static HashMapList analyze(final Code code) {
	HashMapList callhand=new HashMapList();
	AnalysingVisitor avisitor=new AnalysingVisitor(callhand,code);
    	analyzevisit(avisitor, (Quad) code.getRootElement());
	return callhand;
    }

    /** Recursively visit all quads starting at <code>start</code>. */
    private static final void visitAll(Visitor v, Quad start) {
	start.visit(v);
	final StaticState ss = v.ss;
	Util.assert(ss.qm.contains(start));
	Quad[] ql = start.next();
	for (int i=0; i<ql.length; i++) {
	    if (ss.qm.contains(ql[i])) continue; // skip if already done.
	    visitAll(v, ql[i]);
	}
    }

    private static final void analyzevisit(AnalysingVisitor v, Quad start) {
	start.visit(v);
	Set qm=v.qm;
	Util.assert(qm.contains(start));
	Quad[] ql = start.next();
	AnalysingVisitor vv=new AnalysingVisitor(v);
	for (int i=0; i<ql.length; i++) {
	    if (qm.contains(ql[i])) continue; // skip if already done.
	    analyzevisit(vv, ql[i]);
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
	final Set callset;
	final HashMapList handlermap;

	Visitor(StaticState ss, Set callset, HashMapList handlermap) { 
	    this.qf = ss.qf; 
	    this.ss = ss; 
	    this.callset = callset;
	    this.handlermap=handlermap;
	}

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
		callset.add(q);
		head = new CALL(qf, q, q.method, Quad.map(ss.ctm, q.params()),
				Quad.map(ss.ctm, q.retval()), 
				null, q.isVirtual());
       		Quad q0 = new CONST(qf, q, Quad.map(ss.ctm, q.retex()),
				    null, HClass.Void);
		Quad.addEdge(head, 0, q0, 0);
		nq = q0;
	    }
	    ss.qm.put(q, head, nq);
	}
    }

    private static final class AnalysingVisitor extends QuadVisitor {
	HashMapList handlermap;
	Set nullset;
	CALL callquad;
	CALL newcallquad;
	Set qm;
	HashMap callmap;
	HashMap newcallmap;
	Quad anyhandler;
	int anyedge;
	Code code;
	UseDef ud;

	AnalysingVisitor(HashMapList handlermap, Code code) {
	    this.handlermap=handlermap;
	    this.nullset=new WorkSet();
	    this.callquad=null;
	    this.newcallquad=null;
	    this.qm=new WorkSet();
	    this.anyhandler=null;
	    this.anyedge=0;
	    this.code=code;
	    this.ud=new UseDef();
	}

	AnalysingVisitor(AnalysingVisitor v) {
	    //ssa--this is safe
	    this.nullset=v.nullset;
	    this.callquad=v.newcallquad;
	    this.qm=v.qm;
	    this.callmap=v.newcallmap;
	    this.anyhandler=v.anyhandler;
	    this.anyedge=v.anyedge;
	    this.code=v.code;
	    this.ud=v.ud;
	    this.handlermap=v.handlermap;
	    v.anyhandler=null;
	    v.anyedge=0;
	    //only 1st edge should get this!
	}

	private void standard(Quad q) {
	    qm.add(q);
	    newcallquad=callquad;
	    newcallmap=callmap;
	}

	private void weird(Quad q) {
	    qm.add(q);
	    if (anyhandler!=null) {
	        handlermap.add(callquad, new HandInfo(true, anyhandler, anyedge));
		anyhandler=null;
		anyedge=0;
	    }
	    newcallquad=null;
	    newcallmap=null;
	}

	public void visit(Quad q) {
	    //Might have done something useful
	    weird(q);
	}

	public void visit(CALL q) {
	    //Reset last call pointer
	    if (q.retex()!=null) {
		newcallquad=q;
		newcallmap=new HashMap();
		anyhandler=null;
		anyedge=0;
	    } else {
		newcallquad=null;
		newcallmap=null;
		anyhandler=null;
		anyedge=0;
	    }
	    qm.add(q);
	}

	public void visit(OPER q) {
	    if ((q.opcode()==Qop.ACMPEQ)&&(callquad!=null)) {
		Temp dest=q.dst();
		if (ud.useMap(code,dest).length==1) {
		    //make sure it is only used once
		    int nulls=0, exceptions=0;
		    for (int i=0;i<q.operands().length;i++) {
			if (nullset.contains(q.operands(i)))
			    nulls++;
			if (q.operands(i)==callquad.retex())
			    exceptions++;
		    }
		    //want exactly 1 null and 1 exception
		    if ((nulls==1)&&(exceptions==1)) {
			callmap.put(q.dst(),null);
			standard(q);
		    }
		    else weird(q);
		} else weird(q);
	    } else weird(q);
	    qm.add(q);
	}

	public void visit(CONST q) {
	    if (q.value()==null) {
		if (ud.useMap(code,q.dst()).length==1) {
		    nullset.add(q.dst());
		    standard(q);
		} else weird(q);
	    } else weird(q);
	    qm.add(q);
	}

	public void visit(INSTANCEOF q) {
	    Temp dest=q.dst();
	    if ((ud.useMap(code,dest).length==1)&&(callquad!=null)) {
		//make sure it is only used once
		if (q.src()==callquad.retex()) {
		    callmap.put(q.dst(),q.hclass());
		    standard(q);
		}
		else weird(q);
	    } else weird(q);
	    qm.add(q);
	}
	
	public void visit(CJMP q) {
	    if (callquad!=null)
		if (callmap.containsKey(q.test())) {
		    if (callmap.get(q.test())==null) {
			//we have a acmpeq
			//next[1] is the no exception case
			//fix********
			handlermap.add(callquad,new HandInfo(false, q.next(1), q.nextEdge(1).which_pred()));
			anyhandler=q.next(0);
			anyedge=q.nextEdge(0).which_pred();
		    } else {
			//we have an exception
			//next[1] is the case of this exception
			handlermap.add(callquad, 
				       new HandInfo((HClass)callmap.get(q.test()), q.next(1), q.nextEdge(1).which_pred()));
			anyhandler=q.next(0);
			anyedge=q.nextEdge(0).which_pred();
		    }
		} else weird(q);
	    qm.add(q);
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
