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
import harpoon.Util.Tuple;
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
 * @version $Id: ReHandler.java,v 1.1.2.7 1999-08-11 22:25:24 bdemsky Exp $
 */
final class ReHandler {
    // entry point.
    public static final Quad rehandler(final QuadFactory qf, final Code code) {
	Code ncode=(Code)code.clone(code.getMethod());
	(new ToSSA(new SSITOSSAMap(ncode))).optimize(ncode);
	
	WorkSet callset=new WorkSet();
	WorkSet throwset=new WorkSet();
	HashMapList handlermap=analyze(ncode,callset,throwset);

	final QuadMap qm = new QuadMap();
	final HEADER old_header = (HEADER)ncode.getRootElement();
	final METHOD old_method = (METHOD) old_header.next(1);
	final CloningTempMap ctm = new CloningTempMap(ncode.qf.tempFactory(),
						      qf.tempFactory());
	final ArrayList al = new ArrayList();
	final StaticState ss = new StaticState(qf, qm, ctm, al);
	WorkSet phiset=new WorkSet();
	visitAll(new Visitor(ss, handlermap, phiset), old_header);
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
		boolean any=false;
		while (iterate.hasNext()) {
		    HandInfo hi=(HandInfo)iterate.next();
		    if (hi.anyhandler()) {
			if (throwset.contains(hi.handler())) {
			    Temp t=((THROW)hi.handler()).throwable();
			    if (hi.map().containsKey(t))
				t=(Temp)hi.map().get(t);
			    if (t==call.retex())
				any=true;
			}
		    }
		}
		iterate=handlermap.get(call).iterator();
		while(iterate.hasNext()) {
		    ReProtection protlist=new ReProtection();
		    protlist.insert(qm.getHead(call));
		    List handlers=handlermap.get(call);
		    HandInfo nexth=(HandInfo)iterate.next();
		    if (nexth.defaultexit()) {
			Map phimap=nexth.map();
			Temp[] dst=new Temp[phimap.size()];
			Temp[][] src=new Temp[phimap.size()][2];
			Iterator ksit=phimap.keySet().iterator();
			int count=0;
			while (ksit.hasNext()) {
			    Temp t=(Temp)ksit.next();
			    dst[count]=Quad.map(ss.ctm, t);
			    src[count][1]=Quad.map(ss.ctm, t);
			    src[count][0]=Quad.map(ss.ctm, (Temp)phimap.get(t));
			    count++;
			}
			Quad phi = new PHI(qf, qm.getHead(call), dst, src, 2);
			Quad.addEdge(qm.getFoot(call),0, phi, 0);
			Quad.addEdge(qm.getHead(nexth.handler()).prev(nexth.handleredge()),
				     qm.getHead(nexth.handler()).prevEdge(nexth.handleredge()).which_succ(),phi,1);
			Quad.addEdge(phi, 0, qm.getHead(nexth.handler()), nexth.handleredge());
		    }
		    if (nexth.anyhandler()) {
			System.out.println("any");
			if (!any) {
			    Quad newhandler = new HANDLER(qf, qm.getHead(call),
							  Quad.map(ss.ctm, call.retex()),
							  null, protlist);
			    ss.al.add(newhandler);
			    Map phimap=nexth.map();
			    Temp[] dst=new Temp[phimap.size()];
			    Temp[][] src=new Temp[phimap.size()][2];
			    Iterator ksit=phimap.keySet().iterator();
			    int count=0;
			    while (ksit.hasNext()) {
				Temp t=(Temp)ksit.next();
				dst[count]=Quad.map(ss.ctm, t);
				src[count][1]=Quad.map(ss.ctm, t);
				src[count][0]=Quad.map(ss.ctm, (Temp)phimap.get(t));
				count++;
			    }
			    Quad phi = new PHI(qf, qm.getHead(call), dst, src, 2);
			    Quad.addEdge(newhandler,0, phi, 0);
			    Quad.addEdge(qm.getHead(nexth.handler()).prev(nexth.handleredge()),
					 qm.getHead(nexth.handler()).prevEdge(nexth.handleredge()).which_succ(),phi,1);
			    Quad.addEdge(phi, 0, qm.getHead(nexth.handler()), nexth.handleredge());
			}
		    }
		    if (nexth.specificex()) {
			System.out.println("spec");

			boolean needhand=true;
			if (throwset.contains(nexth.handler())&&any) {
			    Temp t=((THROW)nexth.handler()).throwable();
			    if (nexth.map().containsKey(t))
				t=(Temp)nexth.map().get(t);
			    if (t==call.retex())
				needhand=false;
			}
			if (needhand) {
			    Quad newhandler = new HANDLER(qf, qm.getHead(call), 
							  Quad.map(ss.ctm, call.retex()),
							  null, protlist);
			    ss.al.add(newhandler);
			    Map phimap=nexth.map();
			    Temp[] dst=new Temp[phimap.size()];
			    Temp[][] src=new Temp[phimap.size()][2];
			    Iterator ksit=phimap.keySet().iterator();
			    int count=0;
			    while (ksit.hasNext()) {
				Temp t=(Temp)ksit.next();
				dst[count]=Quad.map(ss.ctm, t);
				src[count][1]=Quad.map(ss.ctm, t);
				src[count][0]=Quad.map(ss.ctm, (Temp)phimap.get(t));
				count++;
			    }
			    Quad phi = new PHI(qf, qm.getHead(call), dst, src, 2);
			    Quad.addEdge(newhandler,0, phi, 0);
			    Quad.addEdge(qm.getHead(nexth.handler()).prev(nexth.handleredge()),
					 qm.getHead(nexth.handler()).prevEdge(nexth.handleredge()).which_succ(),phi,1);
			    Quad.addEdge(phi, 0, qm.getHead(nexth.handler()), nexth.handleredge());
			}
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

	// Modify this new CFG by emptying PHI nodes
	PHVisitor v = new PHVisitor(qf);
	for (Iterator it = phiset.iterator(); it.hasNext();)
	    ((Quad)it.next()).visit(v);

	return qH;
    }
    
    private static HashMapList analyze(final Code code, Set callset, Set throwset) {

	CALLVisitor cv=new CALLVisitor(callset, throwset);
	for (Iterator e =  code.getElementsI(); e.hasNext(); )
	    ((Quad)e.next()).visit(cv);

	HashMapList callhand=new HashMapList();
	AnalysingVisitor avisitor=new AnalysingVisitor(callhand,code);
    	analyzevisit(avisitor, callset);
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

    private static final void analyzevisit(AnalysingVisitor v, Set callset) {
	Iterator iterate=callset.iterator();
	while (iterate.hasNext()) {
	    WorkSet qm=new WorkSet();
	    Quad ptr=(Quad) iterate.next();
	    v.reset();
	    ptr.visit(v);
	    while (v.more()&&(!qm.contains(ptr))) {
		System.out.println("**"+v.more());
		System.out.println("Visiting:" +ptr.toString());
		qm.add(ptr);
		ptr = ptr.next(0);
		ptr.visit(v);
		System.out.println(v.more());
	    }
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

    private static final class CALLVisitor extends QuadVisitor {
	Set callset;
	Set throwset;

	CALLVisitor(Set callset, Set throwset) {
	    this.callset=callset;
	    this.throwset=throwset;
	}
	public void visit(Quad q) {}

	public void visit(THROW q) {
	    throwset.add(q);
	}

	public void visit(CALL q) {
	    callset.add(q);
	}
    }


    /** Guts of the algorithm: map from old to new quads, putting the
     *  result in the QuadMap. */
    private static final class Visitor extends QuadVisitor {
	final QuadFactory qf;
	// which Temps are non-null/arrays of known length/integer constants
	// various bits of static state.
	final StaticState ss;
	final Set phiset;
	final HashMapList handlermap;

	Visitor(StaticState ss, HashMapList handlermap, Set phiset) { 
	    this.qf = ss.qf; 
	    this.ss = ss; 
	    this.handlermap=handlermap;
	    this.phiset=phiset;
	}

	/** By default, just clone and set all destinations to top. */
	public void visit(Quad q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    ss.qm.put(q, nq, nq);
	}

	public void visit(PHI q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    ss.qm.put(q, nq, nq);
	    phiset.add(nq);
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
	HashMap callmap;
	Quad anyhandler;
	int anyedge;
	Code code;
	UseDef ud;
	boolean reset;
	HashMap phimap;
	Quad last;
	boolean flag;

	AnalysingVisitor(HashMapList handlermap, Code code) {
	    this.handlermap=handlermap;
	    this.nullset=new WorkSet();
	    this.callquad=null;
	    this.anyhandler=null;
	    this.anyedge=0;
	    this.code=code;
	    this.ud=new UseDef();
	    this.reset=true;
	    this.phimap=new HashMap();
	    this.last=null;
	}

	private Temp remap(Temp orig) {
	    if (phimap.containsKey(orig))
		return (Temp) phimap.get(orig);
	    else
		return orig;
	}

	public void reset() {
	    phimap=new HashMap();
	    last=null;
	    reset=true;
	}

	private void standard(Quad q) {
	    last=q;
	    flag=true;
	}

        boolean more() {
	    return flag;
	}

	private void weird(Quad q) {
	    if (anyhandler!=null) {
	        handlermap.add(callquad, new HandInfo(true, anyhandler, anyedge, new HashMap(phimap)));
		anyhandler=null;
		anyedge=0;
	    }
	    flag=false;
	    last=q;
	}

	public void visit(Quad q) {
	    //Might have done something useful
	    weird(q);
	}

	public void visit(CALL q) {
	    //Reset last call pointer
	    last=q;
	    if (reset) {
		reset=false;
		if (q.retex()!=null) {
		    callquad=q;
		    callmap=new HashMap();
		    anyhandler=null;
		    anyedge=0;
		    flag=true;
		} else {
		    flag=false;
		    anyhandler=null;
		    anyedge=0;
		}
	    } else
		weird(q);
	}

	public void visit(PHI q) {
	    Util.assert(last!=null);
	    int ent=last.nextEdge(0).which_succ();
	    for (int i=0;i<q.numPhis();i++) {
		phimap.put(q.dst(i),remap(q.src(i,ent)));
		phimap.remove(q.src(i,ent));
	    }
	    standard(q);
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
			if (remap(q.operands(i))==callquad.retex())
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
	}

	public void visit(CONST q) {
	    if (q.value()==null) {
		System.out.println("C:1");
		if (ud.useMap(code,q.dst()).length==1) {
		    System.out.println("C:2");
		    nullset.add(q.dst());
		    standard(q);
		} else weird(q);
	    } else weird(q);
	}

	public void visit(INSTANCEOF q) {
	    Temp dest=q.dst();
	    if ((ud.useMap(code,dest).length==1)&&(callquad!=null)) {
		//make sure it is only used once
		if (remap(q.src())==callquad.retex()) {
		    System.out.println("***instanceof");
		    callmap.put(q.dst(),q.hclass());
		    standard(q);
		}
		else weird(q);
	    } else weird(q);
	}
	
	public void visit(CJMP q) {
	    if (callquad!=null)
		if (callmap.containsKey(q.test())) {
		    System.out.println("**CJMP");
		    if (callmap.get(q.test())==null) {
			//we have a acmpeq
			//next[1] is the no exception case
			//fix********
			System.out.println("1**");
			handlermap.add(callquad,new HandInfo(false, q.next(1), q.nextEdge(1).which_pred(), new HashMap(phimap)));
			anyhandler=q.next(0);
			anyedge=q.nextEdge(0).which_pred();
		    } else {
			//we have an exception
			//next[1] is the case of this exception
			System.out.println("2**");
			handlermap.add(callquad, 
				       new HandInfo((HClass)callmap.get(q.test()), q.next(1), q.nextEdge(1).which_pred(),new HashMap(phimap)));
			anyhandler=q.next(0);
			anyedge=q.nextEdge(0).which_pred();
		    }
		} else weird(q);
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

/**
 * Performs the second phase of the transformation to NoSSA form:
 * the removal of the PHI nodes.  This is done by actually modifying
 * the CFG directly, so it is advisable to use this visitor only
 * on a clone of the actual CFG you wish to translate.  
 */
class PHVisitor extends QuadVisitor
{
    private QuadFactory     m_qf;

    public PHVisitor(QuadFactory qf)
    {     
	m_qf          = qf;
    }

    public void visit(Quad q) { }

    public void visit(AGET q)    { visit((Quad)q); }
    public void visit(ASET q)    { visit((Quad)q); }
    public void visit(CALL q)    { visit((Quad)q); }
    public void visit(GET q)     { visit((Quad)q); }
    public void visit(HANDLER q) { visit((Quad)q); }
    public void visit(OPER q)    { visit((Quad)q); }
    public void visit(SET q)     { visit((Quad)q); }
  
    public void visit(LABEL q)
    {
	LABEL label = new LABEL(m_qf, q, q.label(), new Temp[0], q.arity());
	int numPhis = q.numPhis(), arity = q.arity();

	for (int i=0; i<numPhis; i++)
	    for (int j=0; j<arity; j++)
		pushBack(q, i, j);
      
	//removePHIs(q, new LABEL(m_qf, q, q.label(), new Temp[] {}, q.arity()));
	removeTuples(q);  // Updates derivation table

	Quad []prev=q.prev();
	Quad []next=q.next(); Util.assert(next.length==1);
	for(int i=0;i<prev.length;i++) {
	    Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),label,i);
	}
	Quad.addEdge(label,0,next[0],q.nextEdge(0).which_pred());
    }
      
    public void visit(PHI q)
    {
	PHI phi = new PHI(q.getFactory(), q, new Temp[0], q.arity());

	int numPhis = q.numPhis(), arity = q.arity();
	for (int i=0; i<numPhis; i++)
	    for (int j=0; j<arity; j++)
		pushBack(q, i, j);

	//removePHIs(q, new PHI(m_qf, q, new Temp[] {}, q.arity()));
	removeTuples(q);  // Updates derivation table

	Quad []prev=q.prev();
	Quad []next=q.next(); Util.assert(next.length==1);
	for(int i=0;i<prev.length;i++) {
	    Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),phi,i);
	}
	Quad.addEdge(phi,0,next[0],q.nextEdge(0).which_pred());
    }

    private void removePHIs(PHI q, PHI q0)
    {
	Edge[] el;
      
	el = q.prevEdge();
	for (int i=0; i<el.length; i++)
	    Quad.addEdge(q.prev(i), q.prevEdge(i).which_succ(),
			 q0, q.prevEdge(i).which_pred());
      
	el = q.nextEdge();
	for (int i=0; i<el.length; i++) {
	    Quad.addEdge(q0, q.nextEdge(i).which_pred(),
			 q.next(i), q.nextEdge(i).which_succ());
	}
      
    }

    private void removeTuples(Quad q)
    {
	Temp[] tDef = q.def(), tUse = q.use();       
	Tuple t;

	for (int i=0; i<tDef.length; i++) {
	    t = new Tuple(new Object[] { q, tDef[i] });
	}
	for (int i=0; i<tUse.length; i++) {
	    t = new Tuple(new Object[] { q, tUse[i] });
	}
    }
  

    private void pushBack(PHI q, int dstIndex, int srcIndex)
    {
	if (q.dst(dstIndex)!=q.src(dstIndex, srcIndex)) {
	    Edge from = q.prevEdge(srcIndex);
	    MOVE m    = new MOVE(m_qf, q, q.dst(dstIndex), 
				 q.src(dstIndex, srcIndex));
	    Quad.addEdge(q.prev(srcIndex), from.which_succ(), m, 0);
	    Quad.addEdge(m, 0, q, from.which_pred());
	}
    }

}
