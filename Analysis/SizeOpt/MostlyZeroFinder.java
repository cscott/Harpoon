// MostlyZeroFinder.java, created Thu Oct 25 14:37:24 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.Analysis.Counters.CounterFactory;
import harpoon.Analysis.Transactions.BitFieldNumbering;
import harpoon.Analysis.Transactions.BitFieldNumbering.BitFieldTuple;
import harpoon.Analysis.Transformation.*;
import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Temp.*;
import harpoon.Util.*;

import java.util.*;
/**
 * <code>MostlyZeroFinder</code>
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: MostlyZeroFinder.java,v 1.1.2.1 2001-10-29 17:00:12 cananian Exp $
 */
public class MostlyZeroFinder extends MethodMutator {
    final BitFieldNumbering bfn;
    final boolean pointersAreLong;
    final String suffix = "$mzf";
    
    /** Creates a <code>MostlyZeroFinder</code>. */
    public MostlyZeroFinder(HCodeFactory parent, Frame frame) {
        super(QuadNoSSA.codeFactory(parent));
	this.bfn = new BitFieldNumbering(frame.getLinker(), suffix);
	this.pointersAreLong = frame.pointersAreLong();
    }
    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	// prevent instrumentation of the instrumentation code.
	if ("harpoon.Runtime.Counters".equals
	    (hc.getMethod().getDeclaringClass().getName()))
	    return hc;
	// use visitor.
	Visitor v = new Visitor(hc);
	// copy quads into array before visiting so as not to confuse iterator
	Quad[] quads = (Quad[]) hc.getElements();
	for (int i=0; i<quads.length; i++)
	    quads[i].accept(v);
	// done!
	return hc;
    }
    private class Visitor extends QuadVisitor {
	final QuadFactory qf;
	Visitor(HCode hc) { qf=((Quad)hc.getRootElement()).getFactory(); }
	public void visit(Quad q) { /* do nothing */ }
	// xxx: should we treat fields of subclasses differently?
	// perhaps a superclass never uses field x, but subclasses do?
	public void visit(NEW q) {
	    // add 1 to 'alloc field'
	    addFieldAllocCounters(q.nextEdge(0), q.hclass());
	}
	Edge addFieldAllocCounters(Edge e, HClass hc) {
	    if (hc==null) return e; // recursion termination condition.
	    Util.assert(!hc.isInterface() && !hc.isArray());
	    // one counter per declared non-static field.
	    for (Iterator it=new ArrayIterator(hc.getDeclaredFields());
		 it.hasNext(); ) {
		HField hf = (HField) it.next();
		Util.assert(hc==hf.getDeclaringClass());
		if (hf.isStatic()) continue;
		if (hf.getName().endsWith(suffix)) continue;//don't count bits
		e = CounterFactory.spliceIncrement
		    (qf, e, "mzf.alloc."+hc.getName()+"."+hf.getName());
		e = CounterFactory.spliceIncrement
		    (qf, e, "mzf.savedbytes."+hc.getName()+"."+hf.getName(),
		     sizeOf(hf.getType()));
	    }
	    // recurse.
	    return addFieldAllocCounters(e, hc.getSuperclass());
	}
	public void visit(SET q) {
	    // skip static fields.
	    if (q.field().isStatic()) return;

	    // get bit.
	    // if zero and x != 0
	    //  inc 'nonzero' counter
	    //  set bit.
	    // do set
	    Edge e = q.prevEdge(0);
	    BitFieldTuple bft = bfn.bfLoc(q.field());
	    Temp bitT = new Temp(qf.tempFactory(), "mzf_bit");
	    Temp maskT = new Temp(qf.tempFactory(), "mzf_mask");
	    Temp cmpT = new Temp(qf.tempFactory(), "mzf_cmp");
	    e = addAt(e, new GET(qf, q, bitT, bft.field, q.objectref()));
	    e = addAt(e, new CONST(qf, q, maskT,
				   new Integer(1<<bft.bit), HClass.Int));
	    e = addAt(e, new OPER(qf, q, Qop.IAND, cmpT,
				  new Temp[] {bitT, maskT}));
	    e = addAt(e, new OPER(qf, q, Qop.ICMPEQ, cmpT,
				  new Temp[] {cmpT, maskT}));
	    Quad qH = new CJMP(qf, q, cmpT, new Temp[0]);
	    e = addAt(e, 0, qH, 1);
	    Quad qF = new PHI(qf, q, new Temp[0], 3);
	    e = addAt(e, qF);
	    // okay, handle case that bit is 0.
	    Temp zeroT = new Temp(qf.tempFactory(), "mzf_zero");
	    Quad q0 = zeroConst(qf, q, zeroT, q.field().getType());
	    Quad q1 = new OPER(qf, q, cmpEqOp(q.field().getType()), cmpT,
			       new Temp[] {zeroT, q.src()});
	    Quad q2 = new CJMP(qf, q, cmpT, new Temp[0]);
	    // bail if new value (q.src()) is zero.
	    Quad.addEdge(qH, 0, q0, 0);
	    Quad.addEdge(q0, 0, q1, 0);
	    Quad.addEdge(q1, 0, q2, 0);
	    Quad.addEdge(q2, 1, qF, 1);
	    // set bit and increment counter. XXX: NOT THREAD SAFE!!!
	    Quad q3 = new OPER(qf, q, Qop.IOR, bitT, new Temp[] {bitT, maskT});
	    Quad q4 = new SET(qf, q, bft.field, q.objectref(), bitT);
	    Quad.addEdge(q2, 0, q3, 0);
	    Quad.addEdge(q3, 0, q4, 0);
	    Quad.addEdge(q4, 0, qF, 2);
	    HField hf = q.field();
	    e = CounterFactory.spliceIncrement
		(qf, q4.nextEdge(0),
		 "mzf.nonzero."+hf.getDeclaringClass().getName()+
		 "."+hf.getName());
	    e = CounterFactory.spliceIncrement
		(qf, e, "mzf.savedbytes."+hf.getDeclaringClass().getName()+
		 "."+hf.getName(), -sizeOf(hf.getType()));
	}
    }
    private static CONST zeroConst(QuadFactory qf, HCodeElement src,
				   Temp dst, HClass type) {
	if (!type.isPrimitive())
	    return new CONST(qf, src, dst, null, HClass.Void);
	if (type==HClass.Boolean || type==HClass.Byte ||
	    type==HClass.Short || type==HClass.Int || type==HClass.Char)
	    return new CONST(qf, src, dst, new Integer(0), HClass.Int);
	if (type==HClass.Long)
	    return new CONST(qf, src, dst, new Long(0), HClass.Long);
	if (type==HClass.Float)
	    return new CONST(qf, src, dst, new Float(0), HClass.Float);
	if (type==HClass.Double)
	    return new CONST(qf, src, dst, new Double(0), HClass.Double);
	Util.assert(false, "forgot a primitive type?");
	return null;
    }
    private static int cmpEqOp(HClass type) {
	if (!type.isPrimitive()) return Qop.ACMPEQ;
	if (type==HClass.Boolean || type==HClass.Byte || type==HClass.Char ||
	    type==HClass.Short || type==HClass.Int) return Qop.ICMPEQ;
	if (type==HClass.Long) return Qop.LCMPEQ;
	if (type==HClass.Float) return Qop.FCMPEQ;
	if (type==HClass.Double) return Qop.DCMPEQ;
	Util.assert(false, "forgot a primitive type?");
	return -1;
    }
    private int sizeOf(HClass type) {
	if (!type.isPrimitive()) return pointersAreLong ? 8 : 4;
	if (type==HClass.Boolean || type==HClass.Byte) return 1;
	if (type==HClass.Char || type==HClass.Short)   return 2;
	if (type==HClass.Int || type==HClass.Float)    return 4;
	if (type==HClass.Long || type==HClass.Double)  return 8;
	Util.assert(false, "forgot a primitive type?");
	return 0;
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
}
