// Pattern.java, created Mon Aug 30 11:17:15 1999 by bdemsky
// Copyright (C) 1999 Brian Demsky <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;
import harpoon.ClassFile.HClass;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Stack;
/**
 * <code>Pattern</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: Pattern.java,v 1.1.2.3 1999-09-08 05:44:10 bdemsky Exp $
 */
public class Pattern {
    public static HClass exceptionCheck(Quad q) {
	System.out.println("==="+q);
	ExcVisitor ev=new ExcVisitor();
	while (ev.status()) {
	    q.visit(ev);
	    if (ev.success())
		return ev.hclass();
	    q=q.next(0);
	}
	System.out.println("Failed on "+q);
	return null;
    }

    public static Object[] boundCheck(Quad q, Temp array, Temp index) {
	LowBoundVisitor lbv=new LowBoundVisitor(index);
	Quad lq=q;
	while (lbv.status()) {
	    lq.visit(lbv);
	    if (lbv.success())
		break;
	    lq=lq.prev(0);
	}

	Quad hq=q;
	if (lbv.success())
	    hq=lq.prev(0);

	HighBoundVisitor hbv=new HighBoundVisitor(index, array);
	while (hbv.status()) {
	    hq.visit(hbv);
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

    public static Object[] minusCheck(Quad q, Temp checked) {
	MinusVisitor mv=new MinusVisitor(checked);
	while (mv.status()) {
	    q.visit(mv);
	    if (mv.success())
		return new Object[] {q, mv.exchandler()};
	    q=q.prev(0);
	}
	return null;
    }

    public static Object[] nullCheck(Quad q, Temp checked) {
	NullVisitor nv=new NullVisitor(checked);
	while (nv.status()) {
	    q.visit(nv);
	    if (nv.success())
		return new Object[] {q, nv.exchandler()};
	    q=q.prev(0);
	}
	return null;
    }

    public static Object[] componentCheck(Quad q, Temp oref, Temp aref) {
	CompVisitor cv=new CompVisitor(oref, aref);
	while (cv.status()) {
	    q.visit(cv);
	    if (cv.success())
		return new Object[] {q, cv.exchandler()};
	    q=q.prev(0);
	}
	return null;
    }
    
    public static Object[] zeroCheck(Quad q, Temp checked, boolean isint) {
	ZeroVisitor zv=new ZeroVisitor(checked, isint);
	while (zv.status()) {
	    q.visit(zv);
	    if (zv.success())
		return new Object[] {q, zv.exchandler()};
	    q=q.prev(0);
	}
	return null;
    }

    public static void patternMatch(QuadWithTry code) {
	Iterator iterate=code.getElementsI();
	PatternVisitor pv=new PatternVisitor();
	while(iterate.hasNext()) {
	    ((Quad)iterate.next()).visit(pv);
	}
	Map map=pv.map();
	iterate=map.keySet().iterator();
        ArrayList handlers=new ArrayList();
	while(iterate.hasNext()) {
	    Quad q=(Quad)iterate.next();
	    HInfo hi=(HInfo)map.get(q);
	    Quad.addEdge(hi.to().prev(0), hi.to().prevEdge(0).which_succ(), q, 0);
	    while (hi.needHandler()) {
		Object[] handler=hi.pophandler();
		HANDLER h=new HANDLER(q.getFactory(), q, new Temp(q.getFactory().tempFactory()), (HClass) handler[2] , new ReProtection(q));
		handlers.add(h);
		Quad.addEdge(h,0,(Quad)handler[0],((Integer)handler[1]).intValue());
	    }
	}
	//need to add handlers in now
	METHOD m=(METHOD)((Quad)code.getRootElement()).next(1);
	METHOD newm=new METHOD(m.getFactory(), m, m.params(),m.arity()+handlers.size());
	Quad.addEdge((Quad)code.getRootElement(),1,newm, 0);
	for (int i=0;i<handlers.size();i++) {
	    Quad.addEdge(newm, i+1, (Quad)handlers.get(i), 0);
	}
	for (int i=1;i<m.arity();i++) {
	    Quad.addEdge(newm, i+handlers.size(), (Quad) m.next(i),0);
	}
	Quad.addEdge(newm, 0, m.next(0),m.nextEdge(0).which_pred());
    }
}



class PatternVisitor extends QuadVisitor {
    private Map map;
    public PatternVisitor() {
	map=new HashMap();
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
	Object[] n2=Pattern.boundCheck(qd.prev(0), q.objectref(), q.index());
	if (n2!=null) {
	    qd=(Quad)n2[0];
	    HClass hclass2=Pattern.exceptionCheck((Quad)((Object[])n2[1])[0]);
	    if (hclass2==HClass.forName("java.lang.ArrayIndexOutOfBoundsException")) {
		addmap(q,qd);
	    } else {
		addmap(q, qd,(Quad)((Object[])n2[1])[0],
		       (Integer)((Object[])n2[1])[1],
		       HClass.forName("java.lang.ArrayIndexOutOfBoundsException"));
	    }
	}

	Object[] nq=Pattern.nullCheck(qd.prev(0),q.objectref());
	if (nq!=null) {
	    qd=(Quad)nq[0];
	    HClass hclass=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hclass==HClass.forName("java.lang.NullPointerException")) {
		addmap(q,qd);
	    } else {
		addmap(q,qd, (Quad)((Object[])nq[1])[0],
		       (Integer)((Object[])nq[1])[1],
		       HClass.forName("java.lang.NullPointerException"));
	    }
	}
    }

    public void visit(ALENGTH q) {
	Object[] nq=Pattern.nullCheck(q.prev(0), q.objectref());
	if (nq!=null) {
	    HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hc==HClass.forName("java.lang.NullPointerException"))
		addmap(q, (Quad)nq[0]);
	    else
		addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], HClass.forName("java.lang.NullPointerException"));
	}
    }

    public void visit(ANEW q) {
	Quad qd=q;
	boolean flag=true;
	HClass hclass=null;
	Quad handler=null;
	Integer handleredge=null;
	for (int i=q.dimsLength()-1; i>=0; i--) {
	    Object[] nq=Pattern.minusCheck(qd.prev(0), q.dims(i));
	    if (nq!=null) {
		//nq[1] is the exception thrown quad...
		if (i==0) {
		    hclass=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		    if (hclass!=HClass.forName("java.lang.NegativeArraySizeException")) {
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
	if (hclass==HClass.forName("java.lang.NegativeArraySizeException")) {
	    addmap(q, qd);
	} else {
	    if (handler!=null)
		addmap(q, qd, handler, handleredge, HClass.forName("java.lang.NegativeArraySizeException"));
	}
    }

    public void visit(ASET q) {
	Quad qd=q;
	Object[] n2=Pattern.boundCheck(qd.prev(0), q.objectref(), q.index());
	if (n2!=null) {
	    qd=(Quad)n2[0];
	    HClass hclass2=Pattern.exceptionCheck((Quad)((Object[])n2[1])[0]);
	    if (hclass2==HClass.forName("java.lang.ArrayIndexOutOfBoundsException")) {
		addmap(q,qd);
	    } else {
		addmap(q, qd,(Quad)((Object[])n2[1])[0],
		       (Integer)((Object[])n2[1])[1],
		       HClass.forName("java.lang.ArrayIndexOutOfBoundsException"));
	    }
	}

	Object[] nq=Pattern.nullCheck(qd.prev(0),q.objectref());
	if (nq!=null) {
	    qd=(Quad)nq[0];
	    HClass hclass=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hclass==HClass.forName("java.lang.NullPointerException")) {
		addmap(q,qd);
	    } else {
		addmap(q,qd, (Quad)((Object[])nq[1])[0],
		       (Integer)((Object[])nq[1])[1],
		       HClass.forName("java.lang.NullPointerException"));
	    }
	}

	Object[] n1=Pattern.componentCheck(qd.prev(0), q.src(), q.objectref());
	if (n1!=null) {
	    qd=(Quad)n1[0];
	    HClass hclass=Pattern.exceptionCheck((Quad)((Object[])n1[1])[0]);
	    if (hclass==HClass.forName("java.lang.ArrayStoreException")) {
		addmap(q,qd);
	    } else {
		addmap(q,qd, (Quad)((Object[])n1[1])[0],
		       (Integer)((Object[])n1[1])[1],
		       HClass.forName("java.lang.ArrayStoreException"));
	    }
	}
    }

    public void visit(CALL q) {
	if (!q.isStatic()) {
	    Object[] nq=Pattern.nullCheck(q.prev(0), q.params(0));
	    if (nq!=null) {
		HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		if (hc==HClass.forName("java.lang.NullPointerException"))
		    addmap(q, (Quad)nq[0]);
		else
		    addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], HClass.forName("java.lang.NullPointerException"));
	    }	
	}
    }

    public void visit(GET q) {
	if (!q.isStatic()) {
	    Object[] nq=Pattern.nullCheck(q.prev(0), q.objectref());
	    if (nq!=null) {
		HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		if (hc==HClass.forName("java.lang.NullPointerException"))
		    addmap(q, (Quad)nq[0]);
		else
		    addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], HClass.forName("java.lang.NullPointerException"));
	    }	
	}
    }

    public void visit(MONITORENTER q) {
	Object[] nq=Pattern.nullCheck(q.prev(0), q.lock());
	if (nq!=null) {
	    HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hc==HClass.forName("java.lang.NullPointerException"))
		addmap(q, (Quad)nq[0]);
	    else
		addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], HClass.forName("java.lang.NullPointerException"));
	}
    }
    
    public void visit(MONITOREXIT q) {
	Object[] nq=Pattern.nullCheck(q.prev(0), q.lock());
	if (nq!=null) {
	    HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hc==HClass.forName("java.lang.NullPointerException"))
		addmap(q, (Quad)nq[0]);
	    else
		addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], HClass.forName("java.lang.NullPointerException"));
	}
    }

    public void visit(OPER q) {
	switch (q.opcode()) {
	case Qop.IDIV:
	case Qop.IREM:
	    Object[] nq=Pattern.zeroCheck(q.prev(0), q.operands(1),true);
	    if (nq!=null) {
		HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		if (hc==HClass.forName("java.lang.ArithmeticException"))
		    addmap(q, (Quad)nq[0]);
		else
		    addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], HClass.forName("java.lang.ArithmeticException"));
	    }
	    break;
	case Qop.LDIV:
	case Qop.LREM:
	    nq=Pattern.zeroCheck(q.prev(0), q.operands(1),false);
	    if (nq!=null) {
		HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		if (hc==HClass.forName("java.lang.ArithmeticException"))
		    addmap(q, (Quad)nq[0]);
		else
		    addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], HClass.forName("java.lang.ArithmeticException"));
	    }
	    break;
	default:
	}
    }

    public void visit(SET q) {
	if (!q.isStatic()) {
	    Object[] nq=Pattern.nullCheck(q.prev(0), q.objectref());
	    if (nq!=null) {
		HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
		if (hc==HClass.forName("java.lang.NullPointerException"))
		    addmap(q, (Quad)nq[0]);
		else
		    addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], HClass.forName("java.lang.NullPointerException"));
	    }	
	}
    }

    public void visit(THROW q) {
	Object[] nq=Pattern.nullCheck(q.prev(0), q.throwable());
	if (nq!=null) {
	    HClass hc=Pattern.exceptionCheck((Quad)((Object[])nq[1])[0]);
	    if (hc==HClass.forName("java.lang.NullPointerException"))
		addmap(q, (Quad)nq[0]);
	    else
		addmap(q, (Quad) nq[0], (Quad)((Object[])nq[1])[0], (Integer)((Object[])nq[1])[1], HClass.forName("java.lang.NullPointerException"));
	}
    }
}

class HInfo {
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

class ExcVisitor extends QuadVisitor {
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

    public void visit(PHI q) {
	//safe to ignore
    }

    public void visit(MOVE q) {
	System.out.println("Match failed because of: "+q.toString());
	status=-1;
    }
}

class HighBoundVisitor extends QuadVisitor {
    int status;
    Temp []compares;
    Temp test;
    Quad exchandler;
    int excedge;
    Temp tested;
    Temp array;

    HighBoundVisitor(Temp tested, Temp array) {
	this.status=0;
	this.tested=tested;
	this.array=array;
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
	//    Util.assert(compares.length==2);
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
	    Util.assert(compares.length==2);
	    if ((compares[0]==q.dst())&&(compares[1]==tested))
		status=3;
	    else
		status=-1;
	} else
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

class LowBoundVisitor extends MinusVisitor {
    LowBoundVisitor(Temp tested) {
	super(tested);
    }
}

class MinusVisitor extends QuadVisitor {
    int status;
    Temp []compares;
    Temp test;
    Quad exchandler;
    int excedge;
    Temp tested;

    MinusVisitor(Temp tested) {
	this.status=0;
	this.tested=tested;
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
	    Util.assert(compares.length==2);
	    if ((q.value()==null)&&
		(compares[0]==q.dst())&&(compares[1]==tested))
		status=3;
	    else
		status=-1;
	} else
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

class NullVisitor extends QuadVisitor {
    int status;
    Temp []compares;
    Temp test;
    Quad exchandler;
    int excedge;
    Temp tested;

    NullVisitor(Temp tested) {
	this.status=0;
	this.tested=tested;
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
	    Util.assert(compares.length==2);
	    if ((q.value()==null)&&
		(((compares[0]==q.dst())&&(compares[1]==tested))||
		 ((compares[1]==q.dst())&&(compares[0]==tested))))
		status=3;
	    else
		status=-1;
	} else
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

class CompVisitor extends QuadVisitor {
    int status;
    Temp test;
    Quad exchandler;
    int excedge;
    Temp oref;
    Temp aref;

    CompVisitor(Temp objectref, Temp arrayref) {
	this.oref=objectref;
	this.aref=arrayref;
	this.status=0;
    }
    public boolean status() {
	return (status!=-1);
    }

    public boolean success() {
	return (status==2);
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
	if ((status==1)&&
	    (q.dst()==test)&&
	    (q.arrayref()==aref)&&
	    (q.objectref()==oref))
	    status=2;
	else
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

class ZeroVisitor extends QuadVisitor {
    int status;
    Temp []compares;
    Temp test;
    Quad exchandler;
    int excedge;
    Temp tested;
    boolean isint;

    ZeroVisitor(Temp tested, boolean isint) {
	this.status=0;
	this.tested=tested;
	this.isint=isint;
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
	    Util.assert(compares.length==2);
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
