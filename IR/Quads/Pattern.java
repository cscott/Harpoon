// Pattern.java, created Mon Aug 30 11:17:15 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.Tuple;
import harpoon.Util.Collections.WorkSet;
import harpoon.Analysis.UseDef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Stack;
import java.util.Enumeration;
import java.util.Set;
/**
 * <code>Pattern</code> <blink>please document me if I'm public!</blink>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: Pattern.java,v 1.4 2002-04-10 03:05:15 cananian Exp $
 */
public class Pattern {
    public static HClass exceptionCheck(Quad q) {
	//System.out.println("==="+q);
	ExcVisitor ev=new ExcVisitor();
	while (ev.status()) {
	    q.accept(ev);
	    if (ev.success())
		return ev.hclass();
	    q=q.next(0);
	}
	//System.out.println("Failed on "+q);
	return null;
    }

    public static Object[] boundCheck(Quad q, Temp array, Temp index, QuadWithTry code, UseDef ud) {
	LowBoundVisitor lbv=new LowBoundVisitor(index,code, ud);
	Quad lq=q;
	while (lbv.status()) {
	    lq.accept(lbv);
	    if (lbv.success())
		break;
	    lq=lq.prev(0);
	}

	Quad hq=q;
	if (lbv.success())
	    hq=lq.prev(0);

	HighBoundVisitor hbv=new HighBoundVisitor(index, array,code,ud);
	while (hbv.status()) {
	    hq.accept(hbv);
	    if (hbv.success())
		break;
	    hq=hq.prev(0);
	}

	if (lbv.success()&&hbv.success()&&(lbv.exchandler()[0]==hbv.exchandler()[0]))
	    return new Object[] { hq, hbv.exchandler()};
	else if (lbv.success())
	    return new Object[] { lq, lbv.exchandler()};
	else if (hbv.success())
	    return new Object[] { hq, hbv.exchandler()};
	else
	    return null;
    }

    public static Object[] minusCheck(Quad q, Temp checked,QuadWithTry code, UseDef ud) {
	MinusVisitor mv=new MinusVisitor(checked,code,ud);
	while (mv.status()) {
	    q.accept(mv);
	    if (mv.success())
		return new Object[] {q, mv.exchandler()};
	    q=q.prev(0);
	}
	return null;
    }

    public static Object[] nullCheck(Quad q, Temp checked,QuadWithTry code, UseDef ud) {
	NullVisitor nv=new NullVisitor(checked,code,ud);
	while (nv.status()) {
	    q.accept(nv);
	    if (nv.success())
		return new Object[] {q, nv.exchandler()};
	    q=q.prev(0);
	}
	return null;
    }

    public static Object[] componentCheck(Quad q, Temp oref, Temp aref,QuadWithTry code, UseDef ud) {
	CompVisitor cv=new CompVisitor(oref, aref,code,ud);
	while (cv.status()) {
	    q.accept(cv);
	    if (cv.success())
		return new Object[] {q, cv.exchandler()};
	    q=q.prev(cv.hint);
	}
	return null;
    }
    
    public static Object[] zeroCheck(Quad q, Temp checked, boolean isint,QuadWithTry code, UseDef ud) {
	ZeroVisitor zv=new ZeroVisitor(checked, isint,code,ud);
	while (zv.status()) {
	    q.accept(zv);
	    if (zv.success())
		return new Object[] {q, zv.exchandler()};
	    q=q.prev(0);
	}
	return null;
    }

    public static void patternMatch(QuadWithTry code, Map typemap) {
	Iterator iterate=code.getElementsI();
	PatternVisitor pv=new PatternVisitor(code);
	while(iterate.hasNext()) {
	    ((Quad)iterate.next()).accept(pv);
	}
	Map map=pv.map();
	iterate=map.keySet().iterator();
        ArrayList handlers=new ArrayList();
	WorkSet done=new WorkSet();
	while(iterate.hasNext()) {
	    Quad q=(Quad)iterate.next();
	    if (!done.contains(q)) {
		HInfo hi=(HInfo)map.get(q);
		//no pattern matching allowed on cutout regions
		for (Quad ptr=q; ptr!=hi.to().prev(0); ptr=ptr.prev(0))
		    done.add(ptr);
		Quad ql=q;
		//Don't cutout any TYPECAST quads
		while (ql.prev(0).kind()==QuadKind.TYPECAST)
		    ql=ql.prev(0);
		Quad.addEdge(hi.to().prev(0), hi.to().prevEdge(0).which_succ(), ql, 0);
		if (pv.fixupCast().containsKey(q)) {
		    Quad tcast=(Quad)pv.fixupCast().get(q);
		    //gotta handle case where tcast wasn't cut out...
		    if (tcast!=ql.prev(0)) {
			Quad.addEdge(ql.prev(0),ql.prevEdge(0).which_succ(),tcast,0);
			Quad.addEdge(tcast,0,ql,0);
		    }
		}

		   
		while (hi.needHandler()) {
		    Object[] handler=hi.pophandler();
		    Temp Tex=new Temp(q.getFactory().tempFactory());
		    HANDLER h=new HANDLER(q.getFactory(), q, Tex, (HClass) handler[2] , new HANDLER.HashProtectSet(Collections.singleton(q)));
		    typemap.put(new Tuple(new Object[]{h, Tex}), handler[2]);
		    handlers.add(h);
		    Quad.addEdge(h,0,(Quad)handler[0],((Integer)handler[1]).intValue());
		}
	    }
	}
	//need to add handlers in now
	METHOD m=(METHOD)((Quad)code.getRootElement()).next(1);
	for(int mc=1;mc<m.arity();mc++)
	    handlers.add(m.next(mc));

	Set reachable=handlerRemover(handlers,m);

	//build new METHOD quad
	METHOD newm=new METHOD(m.getFactory(), m, m.params(),1+handlers.size());
	//add in this node to reachable set
	reachable.add(newm);

	for (int i=0;i<m.paramsLength();i++) {
	    typemap.put(new Tuple(new Object[]{newm,m.params(i)}),
			typemap.get(new Tuple(new Object[]{m,m.params(i)})));
	}

	Quad.addEdge((Quad)code.getRootElement(),1,newm, 0);
	for (int i=0;i<handlers.size();i++) {
	    Quad.addEdge(newm, i+1, (Quad)handlers.get(i), 0);
	}

	Quad.addEdge(newm, 0, m.next(0),m.nextEdge(0).which_pred());
	Set rset=pv.removalSet();
	iterate=rset.iterator();

	//Get rid of phi's from ASET's...
	while (iterate.hasNext()) {
	    Object[] obj=(Object[])iterate.next();
	    PHI phi=(PHI) obj[0];
	    int edge=((Integer) obj[1]).intValue();
	    Quad.addEdge(phi.prev(edge), phi.prevEdge(edge).which_succ(),
			 phi.next(0), phi.nextEdge(0).which_pred());
	    //update reachable set
	    reachable.remove(phi);
	}

	// Modify this new CFG by emptying PHI nodes
	// Cleaning up from removal of handlers...
	// Need to make NoSSA for QuadWithTry
	// Also empties out phi edges that can't be reached.
	ReHandler.PHVisitor v = new ReHandler.PHVisitor(code.qf, reachable, typemap);
	WorkSet oldset=new WorkSet(reachable);
	for (Iterator it = oldset.iterator(); it.hasNext();) {
	    Quad q=(Quad)it.next();
	    q.accept(v);
	}
    }


    private static Set handlerRemover(ArrayList handlers, METHOD m) {
	//remove useless HANDLERS

	WorkSet reachable=new WorkSet();
	WorkSet todo=new WorkSet();
	todo.push(m.next(0));
	boolean  change=true;
	WorkSet handlerset=new WorkSet();
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
	    Iterator iterateh=handlers.iterator();
	    while (iterateh.hasNext()) {
		HANDLER h=(HANDLER) iterateh.next();
		if (!reachable.contains(h)) {
		    Enumeration enum=h.protectedQuads();
		    while (enum.hasMoreElements()) {
			Object ne=enum.nextElement();
			if (reachable.contains(ne)) {
			    todo.push(h);
			    handlerset.push(h);
			    change=true;
			    break;
			}
		    }
		}
	    }
	}

	Iterator iterate=handlers.iterator();
	while(iterate.hasNext()) {
	    if(!reachable.contains(iterate.next()))
		iterate.remove();
	}
	return reachable;
    }

static class PatternVisitor extends QuadVisitor { // this is an inner class
    private Map map;
    QuadWithTry code;
    UseDef ud;
    WorkSet phiremovalset;
    Linker linker;
    private Map typecastmap;

    public PatternVisitor(QuadWithTry code) {
	map=new HashMap();
	this.code=code;
	this.ud=new UseDef();
	this.phiremovalset=new WorkSet();
	this.linker=code.qf.getLinker();
	typecastmap=new HashMap();
    }
    
    public Map fixupCast() {
	return typecastmap;
    }

    public Set removalSet() {
	return phiremovalset;
    }

    public Map map() {
	return map;
    }

    private void addmap(Quad q, Quad qd) {
	if (!map.containsKey(q))
	    map.put(q, new HInfo(qd));
	else {
	    HInfo hi=(HInfo)map.get(q);
	    hi.to(qd);
	}
    }

    private void addmap(Quad q, Quad qd, Quad handler, Integer handleredge, HClass hclass) {
	if (!map.containsKey(q))
	    map.put(q, new HInfo(qd, handler, handleredge, hclass));
	else {
	    HInfo hi=(HInfo)map.get(q);
	    hi.to(qd);
	    hi.pushhandler(handler, handleredge.intValue(), hclass);
	}
    }

    public void visit(Quad q) {
    }

    public void visit(AGET q) {
	Quad qd=q;
	Object[] n2=Pattern.boundCheck(qd.prev(0), q.objectref(), q.index(),code , ud);
	if (n2!=null) {
	    qd=(Quad)n2[0];
	    HClass hclass2=Pattern.exceptionCheck((Quad)((Object[])n2[1])[0]);
	    if (hclass2==linker.forName("java.lang.ArrayIndexOutOfBoundsException")) {
		addmap(q,qd);
	    } else {
		addmap(q, qd,(Quad)((Object[])n2[1])[0],
		       (Integer)((Object[])n2[1])[1],
		       linker.forName("java.lang.ArrayIndexOutOfBoundsException"));
	    }
	    if (qd.prev(0).kind()==QuadKind.TYPECAST)
		typecastmap.put(q, qd.prev(0));
	}



	Object[] nq=Pattern.nullCheck(qd.prev(0),q.objectref(), code, ud);
	if (nq!=null) {
	    qd=(Quad)nq[0];
	    HClass hclass=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hclass==linker.forName("java.lang.NullPointerException")) {
		addmap(q,qd);
	    } else {
		addmap(q,qd, (Quad)((Object[])nq[1])[0],
		       (Integer)((Object[])nq[1])[1],
		       linker.forName("java.lang.NullPointerException"));
	    }
	}
    }

    public void visit(ALENGTH q) {
	Object[] nq=Pattern.nullCheck(q.prev(0), q.objectref(), code, ud);
	if (nq!=null) {
	    HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hc==linker.forName("java.lang.NullPointerException"))
		addmap(q, (Quad)nq[0]);
	    else
		addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], linker.forName("java.lang.NullPointerException"));
	}
    }

    public void visit(INSTANCEOF q) {
	Object[] nq=Pattern.nullCheck(q.prev(0), q.src(), code, ud);
	if (nq!=null) {
	    if (((Quad)((Object[]) nq[1])[0]).next(0)==q.next(0)) {
		//got 
		if (((Quad)((Object[]) nq[1])[0]).kind()==QuadKind.CONST)
		    {
			CONST cons=(CONST)((Object[]) nq[1])[0];
			if ((cons.dst()==q.dst())&&
			    (cons.type()==HClass.Int)&&
			    (((Integer)cons.value()).intValue()==0)&&
			    (((PHI)q.next(0)).arity()==2)) {
			addmap(q, (Quad)nq[0]);
			phiremovalset.add(new Object[] {q.next(0),new Integer(q.nextEdge(0).which_pred())});
			}
		    }
	    }
	}
    }

    public void visit(ANEW q) {
	Quad qd=q;
	boolean flag=true;
	HClass hclass=null;
	Quad handler=null;
	Integer handleredge=null;
	for (int i=q.dimsLength()-1; i>=0; i--) {
	    Object[] nq=Pattern.minusCheck(qd.prev(0), q.dims(i),code, ud);
	    if (nq!=null) {
		//nq[1] is the exception thrown quad...
		if (i==(q.dimsLength()-1)) {
		    hclass=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		    if (hclass!=linker.forName("java.lang.NegativeArraySizeException")) {
			handler=(Quad)((Object[])nq[1])[0];
			handleredge=(Integer)((Object[])nq[1])[1];
			hclass=null;
		    }
		}
		else
		    if (hclass!=null)
			flag=flag&&(hclass==Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]));
		    else
			flag=flag&&(handler==(Quad)((Object[])nq[1])[0]);
		if (!flag)
		    //bail
		    break;
		qd=(Quad)nq[0];
	    }
	}
	if (hclass==linker.forName("java.lang.NegativeArraySizeException")) {
	    addmap(q, qd);
	} else {
	    if (handler!=null)
		addmap(q, qd, handler, handleredge, linker.forName("java.lang.NegativeArraySizeException"));
	}
    }

    public void visit(ASET q) {
	Quad qd=q;
	Object[] n1=Pattern.componentCheck(qd.prev(0), q.src(), q.objectref(),code, ud);
	if (n1!=null) {
	    qd=(Quad)n1[0];
	    HClass hclass=Pattern.exceptionCheck((Quad)((Object[])n1[1])[0]);
	    if (hclass==linker.forName("java.lang.ArrayStoreException")) {
		addmap(q,qd);
	    } else {
		addmap(q,qd, (Quad)((Object[])n1[1])[0],
		       (Integer)((Object[])n1[1])[1],
		       linker.forName("java.lang.ArrayStoreException"));
	    }
	}
	//componentof null check...
	Object[] n11=Pattern.nullCheck(qd.prev(0),q.src(),code,ud);
	if (n11!=null) {
	    if (((Quad)((Object[])n11[1])[0])==q.prev(0))
		qd=(Quad)n11[0];
	}

	Object[] n2=Pattern.boundCheck(qd.prev(0), q.objectref(), q.index(),code,ud);
	if (n2!=null) {
	    qd=(Quad)n2[0];
	    HClass hclass2=Pattern.exceptionCheck((Quad)((Object[])n2[1])[0]);
	    if (hclass2==linker.forName("java.lang.ArrayIndexOutOfBoundsException")) {
		addmap(q,qd);
	    } else {
		addmap(q, qd,(Quad)((Object[])n2[1])[0],
		       (Integer)((Object[])n2[1])[1],
		       linker.forName("java.lang.ArrayIndexOutOfBoundsException"));
	    }
	    if (qd.prev(0).kind()==QuadKind.TYPECAST)
		typecastmap.put(q, qd.prev(0));
	}



	Object[] nq=Pattern.nullCheck(qd.prev(0),q.objectref(),code,ud);
	if (nq!=null) {
	    qd=(Quad)nq[0];
	    HClass hclass=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hclass==linker.forName("java.lang.NullPointerException")) {
		addmap(q,qd);
	    } else {
		addmap(q,qd, (Quad)((Object[])nq[1])[0],
		       (Integer)((Object[])nq[1])[1],
		       linker.forName("java.lang.NullPointerException"));
	    }
	}
    }

    public void visit(CALL q) {
	if (!q.isStatic()) {
	    Object[] nq=Pattern.nullCheck(q.prev(0), q.params(0),code,ud);
	    if (nq!=null) {
		HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		if (hc==linker.forName("java.lang.NullPointerException"))
		    addmap(q, (Quad)nq[0]);
		else
		    addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], linker.forName("java.lang.NullPointerException"));
	    }	
	}
    }

    public void visit(GET q) {
	if (!q.isStatic()) {
	    Object[] nq=Pattern.nullCheck(q.prev(0), q.objectref(),code,ud);
	    if (nq!=null) {
		HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		if (hc==linker.forName("java.lang.NullPointerException"))
		    addmap(q, (Quad)nq[0]);
		else
		    addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], linker.forName("java.lang.NullPointerException"));
	    }	
	}
    }

    public void visit(MONITORENTER q) {
	Object[] nq=Pattern.nullCheck(q.prev(0), q.lock(),code,ud);
	if (nq!=null) {
	    HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hc==linker.forName("java.lang.NullPointerException"))
		addmap(q, (Quad)nq[0]);
	    else
		addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], linker.forName("java.lang.NullPointerException"));
	}
    }
    
    public void visit(MONITOREXIT q) {
	Object[] nq=Pattern.nullCheck(q.prev(0), q.lock(),code,ud);
	if (nq!=null) {
	    HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hc==linker.forName("java.lang.NullPointerException"))
		addmap(q, (Quad)nq[0]);
	    else
		addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], linker.forName("java.lang.NullPointerException"));
	}
    }

    public void visit(OPER q) {
	switch (q.opcode()) {
	case Qop.IDIV:
	case Qop.IREM:
	    Object[] nq=Pattern.zeroCheck(q.prev(0), q.operands(1),true,code,ud);
	    if (nq!=null) {
		HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		if (hc==linker.forName("java.lang.ArithmeticException"))
		    addmap(q, (Quad)nq[0]);
		else
		    addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], linker.forName("java.lang.ArithmeticException"));
	    }
	    break;
	case Qop.LDIV:
	case Qop.LREM:
	    nq=Pattern.zeroCheck(q.prev(0), q.operands(1),false,code,ud);
	    if (nq!=null) {
		HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		if (hc==linker.forName("java.lang.ArithmeticException"))
		    addmap(q, (Quad)nq[0]);
		else
		    addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], linker.forName("java.lang.ArithmeticException"));
	    }
	    break;
	default:
	}
    }

    public void visit(SET q) {
	if (!q.isStatic()) {
	    Object[] nq=Pattern.nullCheck(q.prev(0), q.objectref(),code,ud);
	    if (nq!=null) {
		HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		if (hc==linker.forName("java.lang.NullPointerException"))
		    addmap(q, (Quad)nq[0]);
		else
		    addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], linker.forName("java.lang.NullPointerException"));
	    }	
	}
    }

    public void visit(THROW q) {
	Object[] nq=Pattern.nullCheck(q.prev(0), q.throwable(),code,ud);
	if (nq!=null) {
	    HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hc==linker.forName("java.lang.NullPointerException"))
		addmap(q, (Quad)nq[0]);
	    else
		addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], linker.forName("java.lang.NullPointerException"));
	}
    }
}

static class HInfo { // this is an inner class
    Quad to;
    Stack handlers;
    //entries look like
    //new Object[] {handler, edge, hclass}

    HInfo(Quad to) {
	this.to=to;
	this.handlers=new Stack();
    }
    HInfo(Quad to, Quad handler, Integer edge, HClass hclass) {
	this.to=to;
	this.handlers=new Stack();
	handlers.push(new Object[] {handler, edge, hclass});
    }
    HInfo(Quad to, Quad handler, int edge, HClass hclass) {
	this.to=to;
	this.handlers=new Stack();
	handlers.push(new Object[] {handler, new Integer(edge), hclass});
    }

    boolean needHandler() {
	return (!handlers.empty());
    }

    void to(Quad to) {
	this.to=to;
    }

    Quad to() {
	return to;
    }

    Object[] pophandler() {
	return ((Object [])handlers.pop());
    }

    void pushhandler(Quad handler, int edge, HClass hclass) {
	handlers.push(new Object[] {handler, new Integer(edge), hclass});
    }
}

static class ExcVisitor extends QuadVisitor { // this is an inner class
    int status;
    HClass hclass;
    Temp exctemp;

    public ExcVisitor() {
	status=0;
    }

    public HClass hclass() {
	if (this.success())
	    return hclass;
	else
	    return null;
    }

    public boolean status() {
	return (status!=-1);
    }

    public boolean success() {
	return (status==3);
    }

    public void visit(Quad q) {
	status=-1;
    }

    public void visit(NEW q) {
	if (status==0) {
	    hclass=q.hclass();
	    exctemp=q.dst();
	    status=1;
	}
	else
	    status=-1;
    }

    public void visit(CALL q) {
	if (status==1) {
	    if ((q.method()==hclass.getConstructor(new HClass[0]))&&
		(q.params(0)==exctemp)&&
		(q.paramsLength()==1)) {
		status=2;
	    } else
		status=-1;
	}
	else
	    status=-1;
    }

    public void visit(THROW q) {
	if (status==2) {
	    if (q.throwable()==exctemp)
		status=3;
	    else
		status=-1;
	} else
	    status=-1;
    }

    public void visit(TYPECAST q) {
	//safe to ignore.
    }

    public void visit(PHI q) {
	//safe to ignore
    }

    public void visit(MOVE q) {
	//System.out.println("Match failed because of: "+q.toString());
	status=-1;
    }
}

static class HighBoundVisitor extends QuadVisitor { // this is an inner class
    int status;
    Temp []compares;
    Temp test;
    Quad exchandler;
    int excedge;
    Temp tested;
    Temp array;
    QuadWithTry code;
    UseDef ud;

    HighBoundVisitor(Temp tested, Temp array,QuadWithTry code, UseDef ud) {
	this.status=0;
	this.tested=tested;
	this.array=array;
	this.code=code;
	this.ud=ud;
    }

    Object[] exchandler() {
	if (this.success())
	    return new Object[] {exchandler, new Integer(excedge) };
	else
	    return null;
    }

    public boolean status() {
	return (status!=-1);
    }

    public boolean success() {
	return (status==3);
    }

    public void visit(Quad q) {
	status=-1;
    }

    public void visit(CONST q) {
	//have to make sure that
	//value of constant==alength(array)
	//Punt this!
	status=-1;
	//	if ((status==2)&&(q.type()==HClass.Int)) {
	//    assert compares.length==2;
	//    if ((q.value()==null)&&
	//	(compares[0]==q.dst())&&(compares[1]==tested))
	//	status=3;
	//    else
	//	status=-1;
	//} else
	//    status=-1;
    }

    public void visit(ALENGTH q) {
	if ((status==2)&&
	    (array==q.objectref())) {
	    assert compares.length==2;
	    if ((compares[0]==q.dst())&&(compares[1]==tested))
		status=3;
	    else
		status=-1;
	} else
	    status=-1;
	if (ud.useMap(code,q.dst()).length!=1)
	    status=-1;
    }

    public void visit(OPER q) {
	if ((status==1)&&
	    (q.opcode()==Qop.ICMPGT)
	    &&(test==q.dst())) {
	    compares=q.use();
	    status=2;
	}
	else
	    status=-1;
	if (ud.useMap(code,q.dst()).length!=1)
	    status=-1;
    }

    public void visit(TYPECAST q) {
	//safe to ignore
    }

    public void visit(CJMP q) {
	if (status==0) {
	    test=q.test();
	    //on this pattern
	    //the exception actually occurs on the 0 edge
	    exchandler=q.next(0);
	    excedge=q.nextEdge(0).which_pred();
	    status=1;
	}
	else 
	    status=-1;
    }
}

static class LowBoundVisitor extends MinusVisitor { // this is an inner class
    LowBoundVisitor(Temp tested,QuadWithTry code, UseDef ud) {
	super(tested,code,ud);
    }
}

static class MinusVisitor extends QuadVisitor { // this is an inner class
    int status;
    Temp []compares;
    Temp test;
    Quad exchandler;
    int excedge;
    Temp tested;
    QuadWithTry code;
    UseDef ud;

    MinusVisitor(Temp tested,QuadWithTry code, UseDef ud) {
	this.status=0;
	this.tested=tested;
	this.code=code;
	this.ud=ud;
    }

    public boolean status() {
	return (status!=-1);
    }

    public boolean success() {
	return (status==3);
    }

    public void visit(Quad q) {
	status=-1;
    }

    Object[] exchandler() {
	if (this.success())
	    return new Object[] {exchandler, new Integer(excedge)};
	else
	    return null;
    }

    public void visit(CONST q) {
	if ((status==2)&&
	    (q.type()==HClass.Int)) {
	    assert compares.length==2;
	    if ((((Integer)q.value()).intValue()==0)&&
		(compares[0]==q.dst())&&(compares[1]==tested))
		status=3;
	    else
		status=-1;
	} else
	    status=-1;
	if (ud.useMap(code,q.dst()).length!=1)
	    status=-1;
    }

    public void visit(TYPECAST q) {
	//safe to ignore
    }

    public void visit(OPER q) {
	if ((status==1)&&
	    (q.opcode()==Qop.ICMPGT)
	    &&(test==q.dst())) {
	    compares=q.use();
	    status=2;
	}
	else
	    status=-1;
	if (ud.useMap(code,q.dst()).length!=1)
	    status=-1;
    }

    public void visit(CJMP q) {
	if (status==0) {
	    test=q.test();
	    exchandler=q.next(1);
	    excedge=q.nextEdge(1).which_pred();
	    status=1;
	}
	else 
	    status=-1;
    }
}

static class NullVisitor extends QuadVisitor { // this is an inner class
    int status;
    Temp []compares;
    Temp test;
    Quad exchandler;
    int excedge;
    Temp tested;
    QuadWithTry code;
    UseDef ud;

    NullVisitor(Temp tested,QuadWithTry code, UseDef ud) {
	this.status=0;
	this.tested=tested;
	this.code=code;
	this.ud=ud;
    }

    public boolean status() {
	return (status!=-1);
    }

    public boolean success() {
	return (status==3);
    }

    public void visit(Quad q) {
	status=-1;
    }

    Object[] exchandler() {
	if (this.success())
	    return new Object[] {exchandler, new Integer(excedge)};
	else
	    return null;
    }

    public void visit(CONST q) {
	if ((status==2)&&
	    (q.type()==HClass.Void)) {
	    assert compares.length==2;
	    if ((q.value()==null)&&
		(((compares[0]==q.dst())&&(compares[1]==tested))||
		 ((compares[1]==q.dst())&&(compares[0]==tested))))
		status=3;
	    else
		status=-1;
	} else
	    status=-1;
	if (ud.useMap(code,q.dst()).length!=1)
	    status=-1;
    }

    public void visit(OPER q) {
	if ((status==1)&&
	    (q.opcode()==Qop.ACMPEQ)
	    &&(test==q.dst())) {
	    compares=q.use();
	    status=2;
	}
	else
	    status=-1;
	if (ud.useMap(code,q.dst()).length!=1)
	    status=-1;
    }

    public void visit(TYPECAST q) {
	//safe to ignore
    }

    public void visit(CJMP q) {
	if (status==0) {
	    test=q.test();
	    exchandler=q.next(1);
	    excedge=q.nextEdge(1).which_pred();
	    status=1;
	}
	else 
	    status=-1;
    }
}

static class CompVisitor extends QuadVisitor { // this is an inner class
    int status;
    Temp test;
    Quad exchandler;
    int excedge;
    Temp oref;
    Temp aref;
    QuadWithTry code;
    UseDef ud;
    int hint;

    CompVisitor(Temp objectref, Temp arrayref,QuadWithTry code, UseDef ud) {
	this.oref=objectref;
	this.aref=arrayref;
	this.status=0;
	this.code=code;
	this.ud=ud;
	this.hint=0;
    }
    public boolean status() {
	return (status!=-1);
    }

    public boolean success() {
	return (status==3);
    }

    Object[] exchandler() {
	if (this.success())
	    return new Object[] { exchandler, new Integer(excedge)};
	else
	    return null;
    }
    public void visit(Quad q) {
	status=-1;
    }

    public void visit(COMPONENTOF q) {
	if ((status==2)&&
	    (q.dst()==test)&&
	    (q.arrayref()==aref)&&
	    (q.objectref()==oref))
	    status=3;
	else
	    status=-1;
	if (ud.useMap(code,q.dst()).length!=1)
	    status=-1;
    }

    public void visit(PHI q) {
	if (status==0) {
	    status=1;
	    hint=1;
	}
	else 
	    status=-1;
    }

    public void visit(TYPECAST q) {
	//safe to ignore
    }

    public void visit(CJMP q) {
	if ((status==1)||(status==0)) {
	    test=q.test();
	    exchandler=q.next(0);
	    excedge=q.nextEdge(0).which_pred();
	    status=2;
	    hint=0;
	}
	else 
	    status=-1;
    }
}

static class ZeroVisitor extends QuadVisitor { // this is an inner class
    int status;
    Temp []compares;
    Temp test;
    Quad exchandler;
    int excedge;
    Temp tested;
    boolean isint;
    QuadWithTry code;
    UseDef ud;

    ZeroVisitor(Temp tested, boolean isint,QuadWithTry code, UseDef ud) {
	this.status=0;
	this.tested=tested;
	this.isint=isint;
	this.code=code;
	this.ud=ud;
    }

    public boolean status() {
	return (status!=-1);
    }

    public boolean success() {
	return (status==3);
    }

    Object[] exchandler() {
	if (this.success())
	    return new Object[] {exchandler, new Integer(excedge)};
	else
	    return null;
    }
    public void visit(Quad q) {
	status=-1;
    }

    public void visit(CONST q) {
	if ((status==2)&&
	    (((q.type()==HClass.Int)&&(isint))
	     ||((!isint)&&(q.type()==HClass.Long)))) {
	    assert compares.length==2;
	    if (isint)
		if ((((Integer)q.value()).intValue()==0)&&
		    (((compares[0]==q.dst())&&(compares[1]==tested))||
		     ((compares[1]==q.dst())&&(compares[0]==tested))))
		    status=3;
		else
		    status=-1;
	    else 
		if ((((Long)q.value()).longValue()==0)&&
		    (((compares[0]==q.dst())&&(compares[1]==tested))||
		     ((compares[1]==q.dst())&&(compares[0]==tested))))
		    status=3;
		else
		    status=-1;
	} else
	    status=-1;
	if (ud.useMap(code,q.dst()).length!=1)
	    status=-1;
    }

    public void visit(OPER q) {
	if ((status==1)&&
	    (((q.opcode()==Qop.ICMPEQ)&&isint)||((!isint)&&(q.opcode()==Qop.LCMPEQ)))
	    &&(test==q.dst())) {
	    compares=q.use();
	    status=2;
	}
	else
	    status=-1;
	if (ud.useMap(code,q.dst()).length!=1)
	    status=-1;
    }

    public void visit(TYPECAST q) {
	//safe to ignore
    }

    public void visit(CJMP q) {
	if (status==0) {
	    test=q.test();
	    exchandler=q.next(1);
	    excedge=q.nextEdge(1).which_pred();
	    status=1;
	}
	else 
	    status=-1;
    }
}

} // close the Pattern class (yes, the indentation's screwed up,
  // but I don't feel like re-indenting all this code) [CSA]

