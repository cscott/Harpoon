// ReHandler.java, created Tue Aug 3 23:30:32 1999 by bdemsky
// Copyright (C) 1998 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.Linker;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
import harpoon.IR.Quads.HANDLER.ProtectedSet;
import harpoon.Analysis.Quads.SSIToSSAMap;
import harpoon.Analysis.UseDef;
import harpoon.Analysis.Quads.TypeInfo;
import harpoon.Util.Tuple;
import harpoon.Util.Collections.WorkSet;
import harpoon.Analysis.Maps.TypeMap;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Enumeration;
import java.util.Stack;
/**
 * <code>ReHandler</code> make exception handling implicit and adds
 * the <code>HANDLER</code> quads from the graph.
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: ReHandler.java,v 1.4 2002-04-10 03:05:15 cananian Exp $
 */
final class ReHandler {
    /* <code>rehandler</code> takes in a <code>QuadFactory</code> and a 
     * <code>QuadSSI</code> and returns the first <code>Quad</code> of
     * a <code>QuadWithTry</code> IR. */

    public static QuadMapPair rehandler(final QuadFactory qf, final QuadSSI code) {
	//clone the original
	QuadSSI ncode=(QuadSSI)code.clone(code.getMethod()).hcode();
	//make it SSA & return TypeMap
	TypeMap ti=buildSSAMap(ncode);	

	//add in TYPECAST as necessary to make the bytecode verifier happy
	//does dataflow analysis, etc...
	analyzeTypes(ncode, ti);

	//Do pattern matching to make exceptions implicit...

	WorkSet callset=new WorkSet();
	WorkSet throwset=new WorkSet();
	WorkSet phiold=new WorkSet();
	WorkSet instanceset=new WorkSet();
	//Do actual pattern matching here
	HashMapList handlermap=analyze(ncode,callset,throwset, instanceset, phiold);

	final QuadMap qm = new QuadMap();
	final HEADER old_header = (HEADER)ncode.getRootElement();
	final METHOD old_method = (METHOD) old_header.next(1);
	final CloningTempMap ctm = new CloningTempMap(ncode.qf.tempFactory(),
						      qf.tempFactory());
	final ArrayList al = new ArrayList();
	final StaticState ss = new StaticState(qf, qm, ctm, al);
	WorkSet phiset=new WorkSet();
	WorkSet cjmpset=new WorkSet();

	//build a typemap
	HashMap typemap=new HashMap();
	//this visitor just clones and classifies the quads
	//also builds the first typemap
	visitAll(new Visitor(ss, handlermap, phiset, instanceset, cjmpset, ncode,typemap, ti), old_header);

	// now qm contains mappings from old to new, we just have to link them.
	for (Iterator e = ncode.getElementsI(); e.hasNext(); ) {
	    Quad old = (Quad) e.next();
	    // link next.
	    Edge[] el = old.nextEdge();
	    
	    for (int i=0; i<(callset.contains(old)?1:el.length); i++) {
		    Quad.addEdge(qm.getFoot((Quad)el[i].from()),el[i].which_succ(),
				 qm.getHead((Quad)el[i].to()), el[i].which_pred());
	    }
	}

	//Need to iterate through the call statements
	Iterator iteratecall=callset.iterator();
	    
	while (iteratecall.hasNext()) {
	    CALL call=(CALL)iteratecall.next();
	    boolean linkold=true;
	    HandInfo next;
	    //see if the call is covered by a handler
	    if(handlermap.containsKey(call)) {
		//iterate through the handlers covering the call
		Iterator iterate=handlermap.get(call).iterator();
		boolean any=false;
		while (iterate.hasNext()) {
		    HandInfo hi=(HandInfo)iterate.next();
		    //see if the any case is handled
		    //if so, we can't simply omit more specific handlers
		    //that just rethrow the exception
		    if (hi.anyhandler())
			if (!any) 
			    any=removable(throwset, phiold, hi, call);
		}
		//iterate through the handlers again
		iterate=handlermap.get(call).iterator();
		while(iterate.hasNext()) {
		    ProtectedSet protlist=new HANDLER.HashProtectSet();
		    protlist.insert(qm.getHead(call));
		    List handlers=handlermap.get(call);
		    HandInfo nexth=(HandInfo)iterate.next();
		    //cover any handler if it is needed
		    if (nexth.anyhandler()&&!any)
			makeanyhandler(qf, ss, qm, call, nexth, protlist, phiset,typemap, ti, callset);
		    //cover other handlers
   		    if (nexth.specificex()) 
			makespechandler(qf, ss, qm, call, throwset, nexth, protlist, phiset, phiold, any,typemap, ti);
		}
	    }
	}

	//reachable is needed to find out what phi functions have incoming
	//edges that can't ever be used....
	//Only puts in handlers edges that are reachable
	//ie...have reachable elements in their protectQuad set...
	//this is all handlers for now...

	Edge e = old_method.nextEdge(0);
	WorkSet reachable=new WorkSet();
	WorkSet todo=new WorkSet();
	todo.push(qm.getHead((Quad)e.to()));
	boolean  change=true;
        ArrayList handlerset=new ArrayList();
	while (change) {
	    while(!todo.isEmpty()) {
		Quad quad=(Quad)todo.pop();
		if (!reachable.contains(quad)) {
		    reachable.push(quad);
		    for (int i=0;i<quad.next().length;i++) {
			todo.push(quad.next(i));
		    }
		}
	    }
	    change=false;
	    Iterator iterate=ss.al.iterator();
	    while (iterate.hasNext()) {
		HANDLER h=(HANDLER) iterate.next();
		if (!reachable.contains(h)) {
		    Enumeration enum=h.protectedQuads();
		    while (enum.hasMoreElements()) {
			if (reachable.contains(enum.nextElement())) {
			    todo.push(h);
			    handlerset.add(h);
			    change=true;
			    break;
			}
		    }
		}
	    }
	}



	METHOD oldM=(METHOD)qm.getHead(old_method);
	Temp[] qMp = oldM.params();
	final METHOD qM = new METHOD(qf, old_method, qMp,
				     1 + handlerset.size());

	for(int i=0;i<qMp.length; i++) {
	    typemap.put(new Tuple(new Object[]{qM, qMp[i]}),
			typemap.get(new Tuple(new Object[]{oldM,qMp[i]})));
	}

	final HEADER qH = (HEADER)qm.getHead(old_header);
	reachable.add(qM);
	Quad.addEdge(qH, 1, qM, 0);
	Quad.addEdge(qM, 0, qm.getHead((Quad)e.to()), e.which_pred());
	Iterator iterate=handlerset.iterator();
	for (int i=1; iterate.hasNext();i++) {
	    Quad ij=(Quad)iterate.next();
	    Quad.addEdge(qM, i,ij,0);
	    //	    Quad.addEdge(qM, i, (Quad)iterate.next(),0);
	}


	// Modify this new CFG by emptying PHI nodes
	// Need to make NoSSA for QuadWithTry
	// Also empties out phi edges that can't be reached.
	PHVisitor v = new PHVisitor(qf, reachable, typemap);
	for (Iterator it = phiset.iterator(); it.hasNext();) {
	    Quad q=(Quad)it.next();
	    if (reachable.contains(q))
		q.accept(v);
	}
	return new QuadMapPair(qH, typemap);
    }

    static TypeMap buildSSAMap(QuadSSI ncode) {
	final HashMap newType=new HashMap();
	UseDef ud=new UseDef();
	//Make TypeInfo really dumb
	TypeInfo titemp=new TypeInfo(ncode,ud,true);
	Iterator iterateQuad=ncode.getElementsI();
	SSIToSSAMap ssitossamap=new SSIToSSAMap(ncode);
	while (iterateQuad.hasNext()) {
	    Quad q=(Quad)iterateQuad.next();
	    Temp[] defs=q.def();
	    for(int i=0;i<defs.length;i++) {
		Temp t=defs[i];
		Temp t2=ssitossamap.tempMap(t);
		if (!newType.containsKey(t2))
		    newType.put(t2, titemp.typeMap(q,t));
		else {
		    HClass currType=(HClass)newType.get(t2);
		    HClass thisType=titemp.typeMap(q,t);
		    if (thisType==null)
			System.out.println("XXXXX: ERROR typing "+q+" : "+t);
		    if (currType==null)
			System.out.println("XXXXX: ERROR curr typing "+t2);
		    //Void policy
		    if (thisType!=HClass.Void)
			newType.put(t2, merge(ncode.qf.getLinker(),
					      currType, thisType));
		}
	    }
	}
	(new ReHandlerToSSA(ssitossamap)).optimize(ncode);
	return new TypeMap() {
	    public HClass typeMap(HCodeElement hc, Temp t) {
		return (HClass)newType.get(t);
	    }
	};
    }
    
    static HClass merge(Linker linker, HClass tcurr, HClass t2) {
	while((tcurr!=null)&&(!tcurr.isAssignableFrom(t2))) {
	    tcurr=tcurr.getSuperclass();
	}
	if (tcurr==null)
	    tcurr=linker.forName("java.lang.Object");
	return tcurr;
    }

    static class QuadMapPair {
	public final Quad quad;
	public final Map  map;
	QuadMapPair(Quad q, Map m) { this.quad=q; this.map = m; }
    }

    private static boolean removable(Set throwset, Set phiset, HandInfo hi, CALL call) {
	boolean any=false;
	if (throwset.contains(hi.handler())) {
	    Temp t=((THROW)hi.handler()).throwable();
	    if (hi.map().containsKey(t))
		t=(Temp)hi.map().get(t);
	    if (t==call.retex())
		any=true;
	}
	Quad handler=hi.handler();
	Temp ctemp=call.retex();
	int edge=hi.handleredge();
	boolean first=true;
	
	while (phiset.contains(handler)) {
	    PHI phi=(PHI)handler;
	    int phinum=-1;
	    for (int i=0;i<phi.numPhis();i++) {
		Temp t=phi.src(i, edge);
		if (first&&hi.map().containsKey(t))
		    t=(Temp)hi.map().get(t);
		if (ctemp==t) {
		    phinum=i;
		    break;
		}
	    }

	    edge=phi.nextEdge(0).which_pred();

	    if (phinum!=-1) {
		ctemp=phi.dst(phinum);
		first=false;
	    }
	    
	    if (throwset.contains(handler.next(0))) {
		Temp t=((THROW)handler.next(0)).throwable();
		if (first&&hi.map().containsKey(t))
		    t=(Temp)hi.map().get(t);
		if (t==ctemp)
		    any=true;
	    }
	    handler=handler.next(0);
	}
	    
	return any;
    }


    //makes an exit for the anyhandler
    private static void makeanyhandler(final QuadFactory qf, final StaticState ss, final QuadMap qm, CALL call, HandInfo nexth, ProtectedSet protlist, Set phiset, Map ntypemap, TypeMap otypemap, Set callset) {
	Quad newhandler = new HANDLER(qf, qm.getHead(call),
				      Quad.map(ss.ctm, call.retex()),
				      null, protlist);
	ntypemap.put(new Tuple(new Object[]{newhandler, Quad.map(ss.ctm, call.retex()) }),
		     otypemap.typeMap(call, call.retex()));
	ss.al.add(newhandler);
	Map phimap=nexth.map();
	Temp[] dst=new Temp[phimap.size()];
	Temp[][] src=new Temp[phimap.size()][2];
	Iterator ksit=phimap.keySet().iterator();
	int count=0;
	while (ksit.hasNext()) {
	    Temp t=(Temp)ksit.next();
	    dst[count]=Quad.map(ss.ctm, t);
	    src[count][0]=Quad.map(ss.ctm,(Temp)phimap.get(t));
	    src[count][1]=Quad.map(ss.ctm, t);
	    count++;
	}
	Quad phi = new PHI(qf, qm.getHead(call), dst, src, 2);
	ksit=phimap.keySet().iterator();
	while (ksit.hasNext()) {
	    Temp t=(Temp)ksit.next();
	    Temp t2=(Temp)phimap.get(t);
	    HClass type=otypemap.typeMap(null, t),
		type2=otypemap.typeMap(null, t2);
	    ntypemap.put(new Tuple(new Object[] {phi, Quad.map(ss.ctm,t)}), type);
	    ntypemap.put(new Tuple(new Object[] {phi, Quad.map(ss.ctm,t2)}), type2);
	}
	phiset.add(phi);
	Quad.addEdge(newhandler,0, phi, 0);
	if (callset.contains((nexth.handler()).prev(nexth.handleredge()))) {
	    //This is the case that the previous node we is
	    //a call statement...we don't want to link to this edge, because it
	    //is no longer here.
	}
	else
	    Quad.addEdge(qm.getHead(nexth.handler()).prev(nexth.handleredge()),
			 qm.getHead(nexth.handler()).prevEdge(nexth.handleredge()).which_succ(),phi,1);
	Quad.addEdge(phi, 0, qm.getHead(nexth.handler()), nexth.handleredge());
    }



    //makes a specific handler    
    private static void makespechandler(final QuadFactory qf, final StaticState ss, final QuadMap qm, CALL call, Set throwset, HandInfo nexth, ProtectedSet protlist, Set phiset, Set phiold, boolean any, Map ntypemap, TypeMap otypemap) {
	boolean needhand=true;
     
	if (any)
	    needhand=!removable(throwset, phiold, nexth, call);
	
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
					  nexth.hclass(), protlist);
	    ntypemap.put(new Tuple(new Object[] {newhandler, Quad.map(ss.ctm, call.retex()) }),
			 otypemap.typeMap(call, call.retex()));
	    ss.al.add(newhandler);
	    Map phimap=nexth.map();
	    Temp[] dst=new Temp[phimap.size()];
	    Temp[][] src=new Temp[phimap.size()][2];
	    Iterator ksit=phimap.keySet().iterator();
	    int count=0;
	    while (ksit.hasNext()) {
		Temp t=(Temp)ksit.next();
		dst[count]=Quad.map(ss.ctm, t);
		src[count][0]=Quad.map(ss.ctm, (Temp)phimap.get(t));
		src[count][1]=Quad.map(ss.ctm, t);
		count++;
	    }
	    Quad phi = new PHI(qf, qm.getHead(call), dst, src, 2);
	    ksit=phimap.keySet().iterator();
	    while (ksit.hasNext()) {
		Temp t=(Temp)ksit.next();
		Temp t2=(Temp)phimap.get(t);
		HClass type=otypemap.typeMap(null, t),
		    type2=otypemap.typeMap(null, t2);
		ntypemap.put(new Tuple(new Object[] {phi, Quad.map(ss.ctm,t)}), type);
		ntypemap.put(new Tuple(new Object[] {phi, Quad.map(ss.ctm,t2)}), type2);
	    }
	    phiset.add(phi);
	    Quad.addEdge(newhandler,0, phi, 0);
	    Quad.addEdge(qm.getHead(nexth.handler()).prev(nexth.handleredge()),
			 qm.getHead(nexth.handler()).prevEdge(nexth.handleredge()).which_succ(),phi,1);
	    Quad.addEdge(phi, 0, qm.getHead(nexth.handler()), nexth.handleredge());
	}
    }


    /** <code>analyzeTypes</code> implements analysis to determine
     *  what <code>TYPECAST</code> are implicit in the Quads.
     *  Then each outgoing edge of every quad is checked to see
     *  if a new implicit <code>TYPECAST</code> appears across it.  If so
     *  an explicit <code>TYPECAST</code> is added.*/

    static void analyzeTypes(final QuadSSI code, TypeMap ti) {
	HCodeElement start=code.getRootElement();
	WorkSet todo=new WorkSet();
	todo.add(start);
	//set up visitor for analysis
	TypeVisitor visitor=new TypeVisitor(ti, todo,code);
	//do the analysis
	visitanalyze(todo, visitor);

	Quad ql[]=(Quad[]) code.getElements();
	Map typecast=visitor.typecast();
	//loop through quads and their next quads
	//looking to see if we need a TYPECAST
	for (int i=0; i<ql.length; i++)
	    for (int j=0;j<ql[i].nextLength(); j++) {
		//Need to check to see if ql[i].next(j)
		//has more types than ql[i]
		//if so, add in the necessary typecasts...
		Set oldcasts=(Set)typecast.get(ql[i]);
		Set newcasts=(Set)typecast.get(ql[i].next(j));
		Iterator iterate=newcasts.iterator();
		while (iterate.hasNext()) {
		    Tuple cast=(Tuple)iterate.next();
		    if (!oldcasts.contains(cast)) {
			//gotta see if we have cast or not
			Temp t=(Temp)cast.asList().get(0);
			Iterator iterate2=oldcasts.iterator();
			boolean found=false;
			while (iterate2.hasNext()) {
			    Tuple tple=(Tuple)iterate2.next();
			    if ((tple.asList().get(0))==t)
				if (((HClass)cast.asList().get(1)).isArray()) {
				    if (((HClass)cast.asList().get(1)).compareTo((HClass)tple.asList().get(1))==0) {
					found=true;
					break;
				    }
				} else
				    if (((HClass)cast.asList().get(1)).isAssignableFrom((HClass)tple.asList().get(1))) {
					found=true;
					break;
				    }
			}
			if (!found) {
			    //Gotta add TYPECAST 'cast' quad
			    //No typecasting to primitives...
			    if (!((HClass) cast.asList().get(1)).isPrimitive()) {
				TYPECAST tc=new TYPECAST(ql[i].getFactory(),
				      ql[i], (Temp) cast.asList().get(0),
				      (HClass) cast.asList().get(1));
				Quad.addEdge(tc,0, ql[i].next(j),ql[i].nextEdge(j).which_pred());
				Quad.addEdge(ql[i], j, tc, 0);
			    }
			}
		    }
		}
	    }
    }

    //Worklist based approac
    //Keep visiting quads until the list is empty...
    static void visitanalyze(WorkSet todo, TypeVisitor visitor) {
	while(!todo.isEmpty()) {
		Quad next=(Quad)todo.pop();
		//System.out.println(next.toString());
		//System.out.println(next+"IN="+visitor.typecast().get(next));
		next.accept(visitor);
		//System.out.println(next+"OUT="+visitor.typecast().get(next));
		//if (!(next instanceof HEADER))
		//    System.out.println(next+".PREV OUT="+visitor.typecast().get(next.prev(0)));
		//System.out.println();
	}
    }
    
    //This method does the pattern matching on calls
    //to determine HANDLERS
    private static HashMapList analyze(final Code code, Set callset, Set throwset, Set instanceset, Set phiset) {
	CALLVisitor cv=new CALLVisitor(callset, throwset, instanceset, phiset);
	for (Iterator e =  code.getElementsI(); e.hasNext(); )
	    ((Quad)e.next()).accept(cv);
	HashMapList callhand=new HashMapList();
	AnalysingVisitor avisitor=new AnalysingVisitor(callhand,code);
    	analyzevisit(avisitor, callset);
	return callhand;
    }

    /** Recursively visit all quads starting at <code>start</code>. */
    private static final void visitAll(Visitor v, Quad start) {
	start.accept(v);
	final StaticState ss = v.ss;
	assert ss.qm.contains(start);
	Quad[] ql = start.next();
	for (int i=0; i<ql.length; i++) {
	    if (ss.qm.contains(ql[i])) continue; // skip if already done.
	    visitAll(v, ql[i]);
	}
    }

    //This method visits all the calls to do pattern matching
    private static final void analyzevisit(AnalysingVisitor v, Set callset) {
	Iterator iterate=callset.iterator();
	while (iterate.hasNext()) {
	    WorkSet qm=new WorkSet();
	    Quad ptr=(Quad) iterate.next();
	    v.reset();
	    ptr.accept(v);
	    int edge=1;
	    while (v.more()&&(!qm.contains(ptr))) {
		//System.out.println("**"+v.more());
		//System.out.println("Visiting:" +ptr.toString());
		qm.add(ptr);
		ptr = ptr.next(edge);
		edge=0;
		ptr.accept(v);
		//System.out.println(v.more());
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

    //This visitor creates sets of the THROW, INSTANCEOF, and CALL quads
    private static final class CALLVisitor extends QuadVisitor {
	Set callset;
	Set throwset;
	Set instanceset;
	Set phiset;

	CALLVisitor(Set callset, Set throwset, Set instanceset, Set phiset) {
	    this.callset=callset;
	    this.throwset=throwset;
	    this.instanceset=instanceset;
	    this.phiset=phiset;
	}
	public void visit(Quad q) {}

	public void visit(PHI q) {
	    phiset.add(q);
	}

	public void visit(THROW q) {
	    throwset.add(q);
	}
	
	public void visit(INSTANCEOF q) {
	    instanceset.add(q);
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
	final Set instanceset;
	final Set cjmpset;
	final HCode hc;
	UseDef ud;
	Map ntypemap;
	TypeMap otypemap;

	Visitor(StaticState ss, HashMapList handlermap, Set phiset, Set instanceset, Set cjmpset, HCode hc, Map ntypemap, TypeMap otypemap) { 
	    this.qf = ss.qf;
	    this.ss = ss;
	    this.handlermap=handlermap;
	    this.phiset=phiset;
	    this.instanceset=instanceset;
	    this.cjmpset=cjmpset;
	    this.ud=new UseDef();
	    this.hc=hc;
	    this.ntypemap=ntypemap;
	    this.otypemap=otypemap;
	}

	private void updatemap(Quad old, Quad nq) {
	    Temp[] uses=old.use(), defs=old.def();
	    for (int i=0;i<uses.length;i++) {
		Temp ntemp=Quad.map(ss.ctm, uses[i]);
		ntypemap.put(new Tuple(new Object[]{nq, ntemp}),otypemap.typeMap(old, uses[i]));
	    }
	    for (int i=0;i<defs.length;i++) {
		Temp ntemp=Quad.map(ss.ctm, defs[i]);
		ntypemap.put(new Tuple(new Object[]{nq, ntemp}),otypemap.typeMap(old, defs[i]));
	    }
	}

	/** By default, just clone and set all destinations to top. */
	public void visit(Quad q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    updatemap(q,nq);
	    ss.qm.put(q, nq, nq);
	}

	public void visit(CJMP q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    updatemap(q,nq);
	    ss.qm.put(q, nq, nq);
	    HCodeElement[] hce=ud.defMap(hc, q.test());
	    assert hce.length==1;
	    if (instanceset.contains(hce[0]))
		cjmpset.add(q);
	}

	public void visit(PHI q) {
	    Quad nq = (Quad) q.clone(qf, ss.ctm);
	    updatemap(q,nq);
	    ss.qm.put(q, nq, nq);
	    phiset.add(nq);
	}

	public void visit(CALL q) {
	    Quad nq, head;
	    // if retex!=null, add the proper checks.
	    if (q.retex()==null) nq=head=(Quad)q.clone(qf, ss.ctm);
	    else {
		//same old type of call intended...
		head = new CALL(qf, q, q.method, Quad.map(ss.ctm, q.params()),
				Quad.map(ss.ctm, q.retval()), 
				null, q.isVirtual(), q.isTailCall(),
				new Temp[0]);
       		Quad q0 = new CONST(qf, q, Quad.map(ss.ctm, q.retex()),
				    null, HClass.Void);
		updatemap(q,head);
		updatemap(q,q0);
		Quad.addEdge(head, 0, q0, 0);
		nq = q0;
	    }
	    ss.qm.put(q, head, nq);
	}
    }

    /** <code>AnalysingVisitor</code> implements most of pattern matching
     *  for CALL statements.*/

    private static final class AnalysingVisitor extends QuadVisitor {
	HashMapList handlermap;
	CALL callquad;
	HashMap callmap;
	Quad anyhandler;
	int anyedge;
	Code code;
	UseDef ud;
	boolean reset;
	HashMap phimap;
	HashMap oldphimap;
	Quad last;
	int laste;
	boolean flag;

	AnalysingVisitor(HashMapList handlermap, Code code) {
	    this.handlermap=handlermap;
	    this.callquad=null;
	    this.anyhandler=null;
	    this.anyedge=0;
	    this.code=code;
	    this.ud=new UseDef();
	    this.reset=true;
	    this.phimap=new HashMap();
	    this.last=null;
	    this.laste=0;
	    this.oldphimap=null;
	}

	private Temp remap(Temp orig) {
	    if (phimap.containsKey(orig))
		return (Temp) phimap.get(orig);
	    else
		return orig;
	}

	public void reset() {
	    phimap=new HashMap();
	    anyhandler=null;
	    anyedge=0;
	    last=null;
	    laste=0;
	    reset=true;
	    oldphimap=null;
	}

	private void standard(Quad q) {
	    last=q;
	    laste=0;
	    flag=true;
	}

        boolean more() {
	    return flag;
	}

	private void weird(Quad q) {
	    if (anyhandler!=null) {
		handlermap.add(callquad, new HandInfo(true, anyhandler, anyedge, new HashMap(oldphimap)));
		anyhandler=null;
		anyedge=0;
	    }
	    flag=false;
	    last=q;
	    laste=0;
	}

	public void visit(Quad q) {
	    //Might have done something useful
	    weird(q);
	}

	public void visit(CALL q) {
	    //Reset last call pointer
	    last=q;
	    laste=1;
	    if (reset) {
		reset=false;
		if (q.retex()!=null) {
		    anyhandler=q.next(1);
		    anyedge=q.nextEdge(1).which_pred();
		    callquad=q;
		    oldphimap=new HashMap(phimap);
		    callmap=new HashMap();
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
	    assert last!=null;
	    //Don't want to follow edge 0 if last was a call
	    int ent=last.nextEdge(laste).which_pred();
	    for (int i=0;i<q.numPhis();i++) {
		phimap.put(q.dst(i),remap(q.src(i,ent)));
		phimap.remove(q.src(i,ent));
	    }
	    standard(q);
	}

	public void visit(INSTANCEOF q) {
	    Temp dest=q.dst();
	    if ((ud.useMap(code,dest).length==1)&&(callquad!=null)) {
		//make sure it is only used once
		if (remap(q.src())==callquad.retex()) {
		    //System.out.println("***instanceof");
		    callmap.put(q.dst(),q.hclass());
		    standard(q);
		}
		else weird(q);
	    } else weird(q);
	}
	
	public void visit(CJMP q) {
	    if (callquad!=null)
		if (callmap.containsKey(q.test())) {
		    //we have an exception
		    //next[1] is the case of this exception
		    handlermap.add(callquad, 
				   new HandInfo((HClass)callmap.get(q.test()), q.next(1), q.nextEdge(1).which_pred(),new HashMap(phimap)));
		    oldphimap=new HashMap(phimap);
		    anyhandler=q.next(0);
		    anyedge=q.nextEdge(0).which_pred();
		    standard(q);
		} else weird(q);
	}
    }


/**
 * Performs the second phase of the transformation to NoSSA form:
 * the removal of the PHI nodes.  This is done by actually modifying
 * the CFG directly, so it is advisable to use this visitor only
 * on a clone of the actual CFG you wish to translate.  
 */
static class PHVisitor extends QuadVisitor // this is an inner class
{
    private QuadFactory     m_qf;
    private Set             reachable;
    private Map             typemap;

    public PHVisitor(QuadFactory qf, Set reachable, Map typemap)
    {     
	m_qf          = qf;
	this.reachable=reachable;
	this.typemap=typemap;
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
	int count=0;
	boolean[] info=new boolean[q.arity()];
	for (int i=0;i<q.arity();i++) {
	    if (q.prevEdge(i)!=null)
		if (reachable.contains(q.prev(i))) {
		    count++;
		    info[i]=true;
		} else
		    info[i]=false;
	    else
		info[i]=false;
	}
	LABEL label = new LABEL(m_qf, q, q.label(), new Temp[0], count);
	reachable.add(label);
	int numPhis = q.numPhis(), arity = q.arity();

	for (int i=0; i<numPhis; i++)
	    for (int j=0; j<arity; j++)
		if (info[j])
		pushBack(q, i, j);
      
	//removePHIs(q, new LABEL(m_qf, q, q.label(), new Temp[] {}, q.arity()));
	removeTuples(q);  // Updates derivation table

	Quad []prev=q.prev();
	Quad []next=q.next(); assert next.length==1;
	int recount=0;
	if (count!=1) {
	    for(int i=0;i<prev.length;i++) {
		if (info[i])
		    Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),label,recount++);
	    }	    
	    Quad.addEdge(label,0,next[0],q.nextEdge(0).which_pred());
	} else {
	    int i=0;
	    while (!info[i])
		i++;
	    Quad.addEdge(prev[i], q.prevEdge(i).which_succ(), next[0], q.nextEdge(0).which_pred());
	}
    }
      
    public void visit(PHI q)
    {
	int count=0;
	boolean[] info=new boolean[q.arity()];
	for (int i=0;i<q.arity();i++) {
	    if (q.prevEdge(i)!=null)
		if (reachable.contains(q.prev(i))) {
		    count++;
		    info[i]=true;
		} else
		    info[i]=false;
	    else
		info[i]=false;
	}
	PHI phi = new PHI(q.getFactory(), q, new Temp[0], count);
	reachable.add(phi);
	int numPhis = q.numPhis(), arity = q.arity();
	for (int i=0; i<numPhis; i++)
	    for (int j=0; j<arity; j++)
		if (info[j])
		    pushBack(q, i, j);

	//removePHIs(q, new PHI(m_qf, q, new Temp[] {}, q.arity()));
	removeTuples(q);  // Updates derivation table

	Quad []prev=q.prev();
	Quad []next=q.next(); assert next.length==1;
	int recount=0;
	if (count!=1) {
	    for(int i=0;i<prev.length;i++) {
		if (info[i])
		    Quad.addEdge(prev[i],q.prevEdge(i).which_succ(),phi,recount++);
	    }
	    Quad.addEdge(phi,0,next[0],q.nextEdge(0).which_pred());
	} else {
	    int i=0;
	    while (!info[i])
		i++;
	    Quad.addEdge(prev[i], q.prevEdge(i).which_succ(), next[0], q.nextEdge(0).which_pred());
	}
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
	    typemap.put(new Tuple(new Object[] {m,q.dst(dstIndex)}),
			typemap.get(new Tuple(new Object[]{q,q.dst(dstIndex)})));
	    typemap.put(new Tuple(new Object[] {m,q.src(dstIndex,srcIndex)}),
			typemap.get(new Tuple(new Object[] {q,q.src(dstIndex,srcIndex)})));
	    reachable.add(m);
	    Quad.addEdge(q.prev(srcIndex), from.which_succ(), m, 0);
	    Quad.addEdge(m, 0, q, from.which_pred());
	}
    }
}

/** <code>TypeVisitor</code> determines what implicit <code>TYPECAST</code>
 *  exist. */

static class TypeVisitor extends QuadVisitor { // this is an inner class
    TypeMap ti;
    HashMap typecast;
    Set visited;
    Set todo;
    HCode hc;
    HClass otype;
    TypeVisitor(TypeMap ti, Set todo, HCode hc) {
	this.hc=hc;
	this.ti=ti;
	this.todo=todo;
	this.typecast=new HashMap();
	this.visited=new WorkSet();
	this.otype=((Code)hc).qf.getLinker().forDescriptor("[Ljava/lang/Object;");
    }

    public Map typecast() {
	return typecast;
    }
    
    public void visit(HEADER q) {
	if (!visited.contains(q)) {
	    WorkSet ourcasts=new WorkSet();
	    typecast.put(q, ourcasts);
	    visited.add(q);
	    for (int i=0;i<q.nextLength();i++)
		todo.add(q.next(i));	
	}
    }

    //This method handles generic quads...
    //Equation for it is out=in.
    public void visit(Quad q) {
	boolean changed=false;
	if (visited.contains(q)) {
	    Quad pred=q.prev(0);
	    Set casts=(Set)typecast.get(pred);
	    Set ourcasts=(Set)typecast.get(q);
	    Iterator iterate=casts.iterator();
	    while (iterate.hasNext()) {
		Tuple cast=(Tuple)iterate.next();
		if (!ourcasts.contains(cast)) {
		    changed=true;
		    ourcasts.add(cast);
		}
	    }
	    if (changed) {
		//push our descendants
		for (int i=0;i<q.nextLength();i++) {
		    todo.add(q.next(i));
		}
	    }
	}
	else {
	    //never seen yet...
	    Set parentcast=(Set)typecast.get(q.prev(0));
	    WorkSet ourcasts=new WorkSet(parentcast);
	    typecast.put(q, ourcasts);
	    visited.add(q);
	    for (int i=0;i<q.nextLength();i++)
		todo.add(q.next(i));
	}
    }
    
    //This handles MOVE quads...
    //Equation for it is out=in union gen
    //where gen=any typecast for dst that were typecast for src in in.
    public void visit(MOVE q) {
	boolean changed=false;
	if (visited.contains(q)) {
	    Quad pred=q.prev(0);
	    Set casts=(Set)typecast.get(pred);
	    Set ourcasts=(Set)typecast.get(q);
	    Iterator iterate=casts.iterator();
	    while (iterate.hasNext()) {
		Tuple cast=(Tuple)iterate.next();
		if (!ourcasts.contains(cast)) {
		    changed=true;
		    ourcasts.add(cast);
		    if (((Temp)cast.asList().get(0))==q.src())
			ourcasts.add(new Tuple(new Object[] {q.dst(), (HClass) cast.asList().get(1)}));
		}
	    }
	    if (changed) {
		//push our descendants
		for (int i=0;i<q.nextLength();i++) {
		    todo.add(q.next(i));
		}
	    }
	}
	else {
	    //never seen yet...
	    Set parentcast=(Set)typecast.get(q.prev(0));
	    WorkSet ourcasts=new WorkSet();
	    typecast.put(q, ourcasts);
	    Iterator iterate=parentcast.iterator();
	    while (iterate.hasNext()) {
		Tuple cast=(Tuple)iterate.next();
		ourcasts.add(cast);
		if (((Temp)cast.asList().get(0))==q.src())
		    ourcasts.add(new Tuple(new Object[] {q.dst(), (HClass) cast.asList().get(1)}));
	    }
	    visited.add(q);
	    for (int i=0;i<q.nextLength();i++)
		todo.add(q.next(i));
	}
    }

    //Method for SET
    //out=in union gen
    //where gen=any cast required by SET [not already in in]
    public void visit(SET q) {
	//q.objectref() is the object to use
	//q.src() is the temp to put in the q.field() of this object
	//need to make sure that:
	//1) q.field().getDeclaringClass() is assignable from q.objectref()
	//2) q.field().getType() is assignable from q.src()
	
	boolean changed=false;
	if (visited.contains(q)) {
	    Quad pred=q.prev(0);
	    Set casts=(Set)typecast.get(pred);
	    Set ourcasts=(Set)typecast.get(q);
	    Iterator iterate=casts.iterator();
	    while (iterate.hasNext()) {
		Tuple cast=(Tuple)iterate.next();
		if (!ourcasts.contains(cast)) {
		    changed=true;
		    ourcasts.add(cast);
		}
	    }
	    if (changed) {
		//push our descendants
		for (int i=0;i<q.nextLength();i++) {
		    todo.add(q.next(i));
		}
	    }
	}
	else {
	    //never seen yet...
	    Set parentcast=(Set)typecast.get(q.prev(0));
	    WorkSet ourcasts=new WorkSet(parentcast);
	    if (!q.isStatic())
		if (!q.field().getDeclaringClass().isAssignableFrom(ti.typeMap(null,q.objectref()))) {
		    //Need typecast??
		    Iterator iterate=ourcasts.iterator();
		    boolean foundcast=false;
		    while (iterate.hasNext()) {
			Tuple cast=(Tuple)iterate.next();
			List list=cast.asList();
			if (list.get(0)==q.objectref()) {
			    HClass hc=(HClass)list.get(1);
			    if (q.field().getDeclaringClass().isAssignableFrom(hc)) {
				foundcast=true;
				break;
			    }
			}
		    }
		    if (!foundcast) {
			//Add typecast
			ourcasts.add(new Tuple(new Object[]{q.objectref(),q.field().getDeclaringClass() }));
		    }
		}
	    if (ti.typeMap(null, q.src())!=HClass.Void) {
		if (ti.typeMap(null,q.src())==null) {
		    System.out.println("XXXXXXXXXXXXX");
		    System.out.println(q);
		    System.out.println(q.field().getType());
		    System.out.println(q.src());
		    System.out.println(hc.getMethod());
		    java.io.PrintWriter out = new java.io.PrintWriter(System.out, true);
		    hc.print(out);
		}
		if (!q.field().getType().isAssignableFrom(ti.typeMap(null, q.src()))) {
		    //Need typecast??
		    Iterator iterate=ourcasts.iterator();
		    boolean foundcast=false;
		    while (iterate.hasNext()) {
			Tuple cast=(Tuple)iterate.next();
			List list=cast.asList();
			if (list.get(0)==q.src()) {
			    HClass hc=(HClass)list.get(1);
			    if (q.field().getType().isAssignableFrom(hc)) {
				foundcast=true;
				break;
			    }
			}
		    }
		    if (!foundcast) {
			//Add typecast
			ourcasts.add(new Tuple(new Object[]{q.src(),q.field().getType() }));
		    }
		}
	    }
	    typecast.put(q, ourcasts);
	    visited.add(q);
	    for (int i=0;i<q.nextLength();i++)
		todo.add(q.next(i));
	}
    }

    //Method for ASET
    //out=in union gen
    //where gen=any cast required by ASET [not already in in]
    public void visit(ASET q) {
	//q.objectref() is the array to use
	//need to make sure that:
	//1) Object[] is assignable from q.objectref()
	
	boolean changed=false;
	if (visited.contains(q)) {
	    Quad pred=q.prev(0);
	    Set casts=(Set)typecast.get(pred);
	    Set ourcasts=(Set)typecast.get(q);
	    Iterator iterate=casts.iterator();
	    while (iterate.hasNext()) {
		Tuple cast=(Tuple)iterate.next();
		if (!ourcasts.contains(cast)) {
		    changed=true;
		    ourcasts.add(cast);
		}
	    }
	    if (changed) {
		//push our descendants
		for (int i=0;i<q.nextLength();i++) {
		    todo.add(q.next(i));
		}
	    }
	}
	else {
	    //never seen yet...
	    Set parentcast=(Set)typecast.get(q.prev(0));
	    WorkSet ourcasts=new WorkSet(parentcast);
	    if ((!(ti.typeMap(null,q.objectref())).isArray())||
		(!(ti.typeMap(null,q.objectref())).getComponentType().isAssignableFrom(ti.typeMap(null,q.src())))) {
		//Need typecast??
		Iterator iterate=ourcasts.iterator();
		boolean foundcast=false;
		while (iterate.hasNext()&&(foundcast==false)) {
		    Tuple cast=(Tuple)iterate.next();
		    List list=cast.asList();
		    if (list.get(0)==q.objectref()) {
			HClass hc=(HClass)list.get(1);
			if (hc.isArray()&&
			    hc.getComponentType().isAssignableFrom(ti.typeMap(null,q.src()))) {
			    foundcast=true;
			    break;
			} else {
			    Iterator iterate2=ourcasts.iterator();
			    while (iterate2.hasNext()) {
				Tuple cast2=(Tuple)iterate.next();
				List list2=cast2.asList();
				if (list2.get(0)==q.src()) {
				    HClass hc2=(HClass)list.get(1);
				    if ((ti.typeMap(null,q.objectref())).isArray()&&
					(ti.typeMap(null,q.objectref())).getComponentType().isAssignableFrom(hc2)) {
					foundcast=true;
					break;
				    }
				    if (hc.isArray()&&
					hc.getComponentType().isAssignableFrom(hc2)) {
					foundcast=true;
					break;
				    }
				}
			    }
			}
		    }
		}
		if (!foundcast) {
		    //Add typecast
		    if (!(ti.typeMap(null,q.objectref())).isArray())
			ourcasts.add(new Tuple(new Object[]{q.objectref(), otype}));
		    else
			ourcasts.add(new Tuple(new Object[]{q.src(), (ti.typeMap(null,q.objectref())).getComponentType()}));
		}
	    }
	    typecast.put(q, ourcasts);
	    visited.add(q);
	    for (int i=0;i<q.nextLength();i++)
		todo.add(q.next(i));
	}
    }

    //Method for ALENGTH
    //out=in union gen
    //where gen=any cast required by ALENGTH [not already in in]
    public void visit(ALENGTH q) {
	//q.objectref() is the array to use
	//need to make sure that:
	//1) Object[] is assignable from q.objectref()
	
	boolean changed=false;
	if (visited.contains(q)) {
	    Quad pred=q.prev(0);
	    Set casts=(Set)typecast.get(pred);
	    Set ourcasts=(Set)typecast.get(q);
	    Iterator iterate=casts.iterator();
	    while (iterate.hasNext()) {
		Tuple cast=(Tuple)iterate.next();
		if (!ourcasts.contains(cast)) {
		    changed=true;
		    ourcasts.add(cast);
		}
	    }
	    if (changed) {
		//push our descendants
		for (int i=0;i<q.nextLength();i++) {
		    todo.add(q.next(i));
		}
	    }
	}
	else {
	    //never seen yet...
	    Set parentcast=(Set)typecast.get(q.prev(0));
	    WorkSet ourcasts=new WorkSet(parentcast);
	    if (!(ti.typeMap(null,q.objectref)).isArray()) {
		//Need typecast??
		Iterator iterate=ourcasts.iterator();
		boolean foundcast=false;
		while (iterate.hasNext()) {
		    Tuple cast=(Tuple)iterate.next();
		    List list=cast.asList();
		    if (list.get(0)==q.objectref()) {
			HClass hc=(HClass)list.get(1);
			if (otype.isAssignableFrom(hc)) {
			    foundcast=true;
			    break;
			}
		    }
		}
		if (!foundcast) {
		    //Add typecast
		    ourcasts.add(new Tuple(new Object[]{q.objectref(), otype}));
		}
	    }
	    typecast.put(q, ourcasts);
	    visited.add(q);
	    for (int i=0;i<q.nextLength();i++)
		todo.add(q.next(i));
	}
    }

    //Method for RETURN
    //out=in union gen
    //where gen=any cast required by AGET [not already in in]
    public void visit(RETURN q) {
	//q.objectref() is the array to use
	//need to make sure that:
	//1) Object[] is assignable from q.objectref()
	
	boolean changed=false;
	if (visited.contains(q)) {
	    Quad pred=q.prev(0);
	    Set casts=(Set)typecast.get(pred);
	    Set ourcasts=(Set)typecast.get(q);
	    Iterator iterate=casts.iterator();
	    while (iterate.hasNext()) {
		Tuple cast=(Tuple)iterate.next();
		if (!ourcasts.contains(cast)) {
		    changed=true;
		    ourcasts.add(cast);
		}
	    }
	    if (changed) {
		//push our descendants
		for (int i=0;i<q.nextLength();i++) {
		    todo.add(q.next(i));
		}
	    }
	}
	else {
	    if ((q.retval()!=null)&&(ti.typeMap(q,q.retval())!=HClass.Void)) {
		HClass rettype=hc.getMethod().getReturnType();
		Set parentcast=(Set)typecast.get(q.prev(0));
		WorkSet ourcasts=new WorkSet(parentcast);
		if (!rettype.isAssignableFrom(ti.typeMap(q,q.retval()))) {
		    //Need typecast??
		    Iterator iterate=ourcasts.iterator();
		    boolean foundcast=false;
		    while (iterate.hasNext()) {
			Tuple cast=(Tuple)iterate.next();
			List list=cast.asList();
			if (list.get(0)==q.retval()) {
			    HClass hc=(HClass)list.get(1);
			    if (rettype.isAssignableFrom(hc)) {
				foundcast=true;
				break;
			    }
			}
		    }
		    if (!foundcast) {
			//Add typecast
			ourcasts.add(new Tuple(new Object[]{q.retval(),rettype }));
		    }
		}
		typecast.put(q, ourcasts);
		visited.add(q);
		for (int i=0;i<q.nextLength();i++)
		    todo.add(q.next(i));
   	    } else {
		//never seen yet...
		Set parentcast=(Set)typecast.get(q.prev(0));
		WorkSet ourcasts=new WorkSet(parentcast);
		typecast.put(q, ourcasts);
		visited.add(q);
		for (int i=0;i<q.nextLength();i++)
		    todo.add(q.next(i));
	    }
	}
    }

    //Method for AGET
    //out=in union gen
    //where gen=any cast required by AGET [not already in in]
    public void visit(AGET q) {
	//q.objectref() is the array to use
	//need to make sure that:
	//1) Object[] is assignable from q.objectref()
	
	boolean changed=false;
	if (visited.contains(q)) {
	    Quad pred=q.prev(0);
	    Set casts=(Set)typecast.get(pred);
	    Set ourcasts=(Set)typecast.get(q);
	    Iterator iterate=casts.iterator();
	    while (iterate.hasNext()) {
		Tuple cast=(Tuple)iterate.next();
		if (!ourcasts.contains(cast)) {
		    changed=true;
		    ourcasts.add(cast);
		}
	    }
	    if (changed) {
		//push our descendants
		for (int i=0;i<q.nextLength();i++) {
		    todo.add(q.next(i));
		}
	    }
	}
	else {
	    //never seen yet...
	    Set parentcast=(Set)typecast.get(q.prev(0));
	    WorkSet ourcasts=new WorkSet(parentcast);
	    if (!(ti.typeMap(null,q.objectref())).isArray()) {
		//Need typecast??
		Iterator iterate=ourcasts.iterator();
		boolean foundcast=false;
		while (iterate.hasNext()) {
		    Tuple cast=(Tuple)iterate.next();
		    List list=cast.asList();
		    if (list.get(0)==q.objectref()) {
			HClass hc=(HClass)list.get(1);
			if (otype.isAssignableFrom(hc)) {
			    foundcast=true;
			    break;
			}
		    }
		}
		if (!foundcast) {
		    //Add typecast
		    ourcasts.add(new Tuple(new Object[]{q.objectref(), otype}));
		}
	    }
	    typecast.put(q, ourcasts);
	    visited.add(q);
	    for (int i=0;i<q.nextLength();i++)
		todo.add(q.next(i));
	}
    }

    //method for GET...
    //equations same as SET...
    public void visit(GET q) {
	//q.objectref() is the object to use
	//q.dst() is the temp to get from the q.field() of this object
	//need to make sure that:
	//1) q.field().getDeclaringClass() is assignable from q.objectref()
	boolean changed=false;
	if (visited.contains(q)) {
	    Quad pred=q.prev(0);
	    Set casts=(Set)typecast.get(pred);
	    Set ourcasts=(Set)typecast.get(q);
	    Iterator iterate=casts.iterator();
	    while (iterate.hasNext()) {
		Tuple cast=(Tuple)iterate.next();
		if (!ourcasts.contains(cast)) {
		    changed=true;
		    ourcasts.add(cast);
		}
	    }
	    if (changed) {
		//push our descendants
		for (int i=0;i<q.nextLength();i++) {
		    todo.add(q.next(i));
		}
	    }
	}
	else {
	    //never seen yet...
	    Set parentcast=(Set)typecast.get(q.prev(0));
	    WorkSet ourcasts=new WorkSet(parentcast);
	    if (!q.isStatic())
		if (!q.field().getDeclaringClass().isAssignableFrom(ti.typeMap(q,q.objectref()))) {
		    //Need typecast??
		    Iterator iterate=ourcasts.iterator();
		    boolean foundcast=false;
		    while (iterate.hasNext()) {
			Tuple cast=(Tuple)iterate.next();
			List list=cast.asList();
			if (list.get(0)==q.objectref()) {
			    HClass hc=(HClass)list.get(1);
			    if (q.field().getDeclaringClass().isAssignableFrom(hc)) {
				foundcast=true;
				break;
			    }
			}
		    }
		    if (!foundcast) {
			//Add typecast
			ourcasts.add(new Tuple(new Object[]{q.objectref(),q.field().getDeclaringClass() }));
		    }
		}
	    typecast.put(q, ourcasts);
	    visited.add(q);
	    for (int i=0;i<q.nextLength();i++)
		todo.add(q.next(i));
	}
    }
    

    //method for call
    //same basic idea
    public void visit(CALL q) {
	boolean changed=false;
	if (visited.contains(q)) {
	    Quad pred=q.prev(0);
	    Set casts=(Set)typecast.get(pred);
	    Set ourcasts=(Set)typecast.get(q);
	    Iterator iterate=casts.iterator();
	    while (iterate.hasNext()) {
		Tuple cast=(Tuple)iterate.next();
		if (!ourcasts.contains(cast)) {
		    changed=true;
		    ourcasts.add(cast);
		}
	    }
	    if (changed) {
		//push our descendants
		for (int i=0;i<q.nextLength();i++) {
		    todo.add(q.next(i));
		}
	    }
	}
	else {
	    //never seen yet...
	    Set parentcast=(Set)typecast.get(q.prev(0));
	    WorkSet ourcasts=new WorkSet(parentcast);
	    //XXXXXXXX DEBUG
	    //System.out.println(q);
	    for (int i=0; i<q.paramsLength();i++) {
		Temp param=q.params(i);
		HClass type=typeOf(q, param);
		HClass neededclass=q.paramType(i);
		if (type!=HClass.Void)
		    if (!neededclass.isAssignableFrom(type)) {
			//Need typecast??
			Iterator iterate=ourcasts.iterator();
			boolean foundcast=false;
			while (iterate.hasNext()) {
			    Tuple cast=(Tuple)iterate.next();
			    List list=cast.asList();
			    if (list.get(0)==q.params(i)) {
				HClass hc=(HClass)list.get(1);
				if (neededclass.isAssignableFrom(hc)) {
				    foundcast=true;
				    break;
				}
			    }
			}
			if (!foundcast) {
			    //Add typecast
			    ourcasts.add(new Tuple(new Object[]{q.params(i),neededclass }));
			}
		    }
	    }
	    typecast.put(q, ourcasts);
	    visited.add(q);
	    for (int i=0;i<q.nextLength();i++)
		todo.add(q.next(i));
	}
    }
    
    //PHI method...
    //merges TYPECAST...
    //Does it by taking intersection of all incoming in's....
    public void visit(PHI q) {
	WorkSet casts=new WorkSet();
	int firstv;
	Quad first=q.prev(0);
	if (visited.contains(first)) {
	    Set firstcast=(Set)typecast.get(first);
	    Iterator iterate=firstcast.iterator();
	    while (iterate.hasNext()) {
		List cast=((Tuple)iterate.next()).asList();
		Temp t=(Temp)cast.get(0);
		HClass hclass=(HClass)cast.get(1);
		checkphi(q, null, t, casts, hclass);
	    }
	    for (int i=0;i<q.numPhis(); i++) {
		iterate=firstcast.iterator();
		while (iterate.hasNext()) {
		    List cast=((Tuple)iterate.next()).asList();
		    if (q.dst(i)==(Temp)cast.get(0))
			checkphi(q, q.src(i), q.dst(i), casts, (HClass) cast.get(1));
		}
	    }
	}
	if (visited.contains(q)) {
	    boolean changed=false;
	    Set ourcasts=(Set)typecast.get(q);
	    Iterator iterate=casts.iterator();
	    while (iterate.hasNext()) {
		Tuple cast=(Tuple)iterate.next();
		if (!ourcasts.contains(cast)) {
		    changed=true;
		    ourcasts.add(cast);
		}
	    }
	    if (changed) {
		//push our descendants
		for (int i=0;i<q.nextLength();i++) {
		    todo.add(q.next(i));
		}
	    }
	}
	else {
	    //never seen yet...
	    typecast.put(q, casts);
	    visited.add(q);
	    for (int i=0;i<q.nextLength();i++)
		todo.add(q.next(i));
	}
    }

    private void checkphi(PHI q, Temp[] map, Temp tf,Set casts, HClass hclass) {
	Linker linker = q.getFactory().getLinker();
	boolean good=true;
	for (int i=1;i<q.arity();i++) {
	    if (typecast.containsKey(q.prev(i))) {
		Set prevcasts=(Set)typecast.get(q.prev(i));
		Iterator previter=prevcasts.iterator();
		HClass best=null;
		while (previter.hasNext()) {
		    List ctuple=((Tuple)previter.next()).asList();
		    Temp t=null;
		    if (map!=null)
			t=map[i];
		    else
			t=tf;
		    if (((Temp)ctuple.get(0))==t) {
			HClass tclass=(HClass)ctuple.get(1);
			HClass tc=hclass;
			while((tc!=null)&&(!tc.isAssignableFrom(tclass))) {
			    //System.out.println("=======");
			    //System.out.println(tclass);
			    //System.out.println(q);
			    //System.out.println(hclass);
			    //System.out.println(tc);
			    //System.out.println(tc.getSuperclass());
			    tc=tc.getSuperclass();
			}
			//Its safe to assume object for interfaces...
			//because the interface has to be for some sort of
			//real object
			if (tc==null)
			    tc=linker.forName("java.lang.Object");
			if (best==null)
			    best=tc;
			else if (best.isAssignableFrom(tc))
			    best=tc;
		    }
		}
		if (best==null) {
		    good=false;
		    break;
		} else
		    hclass=best;
	    } else
		good=false;
	}
	if (good) {
	    //Add in typecast (t, hclass)
	    Tuple tple=new Tuple(new Object[] {tf, hclass});
	    if (!casts.contains(tple))
		casts.add(tple);
	}
    }

    private HClass typeOf(Quad q, Temp t) {
	return ti.typeMap(q, t);
    }

}

public static void clean(QuadWithTry code) {
    HEADER h=(HEADER) code.getRootElement();
    METHOD m=(METHOD) h.next(1);
    WorkSet handlerset=new WorkSet();
    for (int i=1;i<m.nextLength();i++)
	handlerset.add(m.next(i));

    WorkSet todo=new WorkSet(code.getElementsL());
    CleanVisitor cv=new CleanVisitor(handlerset, todo);

    while(!todo.isEmpty()) {
	Quad q=(Quad)todo.pop();
	q.accept(cv);
    }
    Set useful=cv.useful();
    Iterator iterate=code.getElementsI();
    while (iterate.hasNext()) {
	Quad q=(Quad)iterate.next();
	if (!useful.contains(q)) {
	    //System.out.println("Cleaning " + q);
	    Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), q.next(0), q.nextEdge(0).which_pred());
	}
    }
}


static class CleanVisitor extends QuadVisitor {
    HashMap in;
    WorkSet useful;
    Set todo;
    WorkSet usefulS;
    Set handlerset;
    WorkSet protectedquads;

    CleanVisitor(Set handlerset, Set todo) {
	this.useful=new WorkSet();
	this.in=new HashMap();
	this.handlerset=handlerset;
	this.protectedquads=new WorkSet();
	this.todo=todo;
	for (Iterator i=handlerset.iterator();i.hasNext();) {
	    HANDLER h=(HANDLER)i.next();
	    for (Enumeration enum=h.protectedQuads();
		 enum.hasMoreElements();)
		protectedquads.add(enum.nextElement());	    
	}
    }

    public Set useful() {
	return useful;
    }

    public void visit(Quad q) {
	useful(q,false);
    }
    public void visit(MOVE q) {
	maybeuseful(q);
    }
    public void visit(CONST q) {
	maybeuseful(q);
    }
    public void visit(HANDLER q) {
	useful(q, true);
    }

    void useful(Quad q, boolean ishandler) {
	WorkSet livein=new WorkSet();
	useful.add(q);
	for (int i=0;i<q.nextLength();i++) {
	    if (in.containsKey(q.next(i))) {
		WorkSet ini=(WorkSet) in.get(q.next(i));
		for(Iterator iterate=ini.iterator();iterate.hasNext();)
		    livein.add(iterate.next());
	    }
	}
	if (protectedquads.contains(q)) {
	    for (Iterator hiterate=handlerset.iterator(); hiterate.hasNext();) {
		HANDLER h=(HANDLER)hiterate.next();
		if (h.isProtected(q))
		    if (in.containsKey(h)) {
			WorkSet ini=(WorkSet) in.get(h);
			for(Iterator iterate=ini.iterator();iterate.hasNext();)
			    livein.add(iterate.next());
		    }
	    }
	}
	//out-defs
	Temp defs[]=q.def();
	for (int i=0;i<defs.length;i++)
	    livein.remove(defs[i]);
	//+uses
	Temp uses[]=q.use();
	for (int i=0;i<uses.length;i++)
	    livein.add(uses[i]);
	int oldsize=0;
	if (in.containsKey(q))
	    oldsize=((WorkSet)in.get(q)).size();
	if (oldsize!=livein.size()) {
	    //Things have changed
	    //Add our set in
	    in.put(q, livein);
	    //Put our predecessors in the todo list
	    for(int i=0;i<q.prevLength();i++)
		todo.add(q.prev(i));
	    if (ishandler) {
		//notify the handled quads
		for (Enumeration enum=((HANDLER) q).protectedQuads();
		     enum.hasMoreElements();)
		    todo.add(enum.nextElement());
	    }
	}
    }

    void maybeuseful(Quad q) {
	if (useful.contains(q)) 
	    useful(q,false);
	else {
	    WorkSet livein=new WorkSet();
	    for (int i=0;i<q.nextLength();i++) {
		if (in.containsKey(q.next(i))) {
		    WorkSet ini=(WorkSet) in.get(q.next(i));
		    for(Iterator iterate=ini.iterator();iterate.hasNext();)
			livein.add(iterate.next());
		}
	    }
	    if (protectedquads.contains(q)) {
		for (Iterator hiterate=handlerset.iterator(); hiterate.hasNext();) {
		    HANDLER h=(HANDLER)hiterate.next();
		    if (h.isProtected(q))
			if (in.containsKey(h)) {
			    WorkSet ini=(WorkSet) in.get(h);
			    for(Iterator iterate=ini.iterator();iterate.hasNext();)
				livein.add(iterate.next());
			}
		}
	    }
	    //out-defs
	    Temp defs[]=q.def();
	    boolean hasuse=false;
	    
	    for (int i=0;i<defs.length;i++)
		if (livein.contains(defs[i])) {
		    hasuse=true;
		    break;
		}
	    if (hasuse) {
		useful.add(q);
		useful(q,false);
	    } else {
		int oldsize=0;
		if (in.containsKey(q))
		    oldsize=((WorkSet)in.get(q)).size();
		if (oldsize!=livein.size()) {
		    //Things have changed
		    //Add our set in
		    in.put(q, livein);
		    //Put our predecessors in the todo list
		    for(int i=0;i<q.prevLength();i++)
			todo.add(q.prev(i));
		}
	    }
	}
    }
}

} // close the ReHandler class (yes, the indentation's screwed up,
  // but I don't feel like re-indenting all this code) [CSA]
