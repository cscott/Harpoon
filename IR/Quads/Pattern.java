// Pattern.java, created Mon Aug 30 11:17:15 1999 by root
// Copyright (C) 1999 Brian <bdemsky@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;
import harpoon.ClassFile.HClass;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
/**
 * <code>Pattern</code>
 * 
 * @author  Brian Demsky <bdemsky@mit.edu>
 * @version $Id: Pattern.java,v 1.1.2.1 1999-08-30 22:09:49 bdemsky Exp $
 */
public class Pattern {
    public static HClass exceptionCheck(Quad q) {
	ExcVisitor ev=new ExcVisitor();
	while (ev.status()) {
	    q.visit(ev);
	    if (ev.success())
		return ev.hclass();
	}
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
	if (lbv.success()||hbv.success())
	    return new Object[] {lq, hq, lbv.exchandler(), hbv.exchandler()};
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
	while(iterate.hasNext())
	    ((Quad)iterate.next()).visit(pv);
	Map map=pv.map();
	iterate=map.keySet().iterator();
	WorkSet handlers=new WorkSet();
	while(iterate.hasNext()) {
	    Quad q=(Quad)iterate.next();
	    HInfo hi=(HInfo)map.get(q);
	    Quad.addEdge(hi.to().prev(0), hi.to().prevEdge(0).which_succ(), q, 0);
	    if (hi.needHandler()) {
		handlers.push(new HANDLER(q.getFactory(), q, new Temp(q.getFactory().tempFactory()), hi.hclass(), new ReProtection(q)));
	    }
	}
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

    public void visit(Quad q) {
    }

    public void visit(ANEW q) {
//  	Quad qd=q;
//  	boolean flag=true;
//  	HClass hclass=null;
//  	Quad handler=null;
//  	for (int i=0; i<q.dimsLength(); i++) {
//  	    Quad[] nq=Pattern.minusCheck(qd.prev(0), q.dims(i));
//  	    if (nq!=null) {
//  		qd=nq[0];
//  		//nq[1] is the exception thrown quad...
//  		if (i==0) {
//  		    hclass=Pattern.exceptionCheck(nq[1]);
//  		    if (hclass==null)
//  			handler=nq[1];
//  		}
//  		else
//  		    if (hclass!=null)
//  			flag=flag&&(hclass==Pattern.exceptionCheck(nq[1]));
//  		    else
//  			flag=flag&&(handler==nq[1]);
//  		if (!flag) {
//  		    //bail
//  		}
//  	    }
//  	}
    }

    public void visit(OPER q) {
	switch (q.opcode()) {
	case Qop.IDIV:
	case Qop.IREM:
	    Object[] nq=Pattern.zeroCheck(q.prev(0), q.operands(1),true);
	    HClass hc=Pattern.exceptionCheck(((Quad)nq[0]).prev(0));
	    if (hc==HClass.forName("java.lang.ArithmeticException"))
		map.put(q, new HInfo((Quad)nq[0]));
	    else
		map.put(q, new HInfo((Quad) nq[0], (Quad)nq[1], (Integer)nq[2], HClass.forName("java.lang.ArithmeticException")));
	default:
	}
    }
}

class HInfo {
    Quad to;
    Quad handler;
    int edge;
    HClass hclass;
    HInfo(Quad to) {
	this.to=to;
	this.handler=null;
	this.edge=-1;
    }
    HInfo(Quad to, Quad handler, Integer edge, HClass hclass) {
	this.to=to;
	this.handler=handler;
	this.edge=edge.intValue();
	this.hclass=hclass;
    }
    HInfo(Quad to, Quad handler, int edge, HClass hclass) {
	this.to=to;
	this.handler=handler;
	this.edge=edge;
	this.hclass=hclass;
    }
    boolean needHandler() {
	return (handler!=null);
    }
    Quad to() {
	return to;
    }

    HClass hclass() {
	return hclass;
    }
    Quad handler() {
	return handler;
    }
    int edge() {
	return edge;
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
