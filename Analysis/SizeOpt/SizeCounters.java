// SizeCounters.java, created Tue Jul 10 11:27:01 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.SizeOpt;

import harpoon.Analysis.Counters.CounterFactory;
import harpoon.Analysis.Transformation.MethodMutator;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Runtime;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HField;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Temp.Temp;
import harpoon.Util.ArrayIterator;

import java.util.Iterator;

/**
 * The <code>SizeCounters</code> code factory adds counters for
 * various allocation properties, to aid in determining the
 * effectiveness of the various size optimizations in this
 * package.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SizeCounters.java,v 1.1.2.7 2001-10-31 23:34:50 cananian Exp $
 */
public class SizeCounters extends MethodMutator {
    final Frame frame;
    final Runtime.TreeBuilder tb;
    final BitWidthAnalysis bwa;
    
    /** Creates a <code>SizeCounters</code>. */
    public SizeCounters(HCodeFactory parent, Frame frame) {
	this(parent, frame, null);
    }
    SizeCounters(HCodeFactory parent, Frame frame, BitWidthAnalysis bwa) {
	super(QuadNoSSA.codeFactory(parent));
	this.frame = frame;
	this.tb = frame.getRuntime().getTreeBuilder();
	this.bwa = bwa;
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	// prevent instrumentation of the instrumentation code. (sigh)
	if ("harpoon.Runtime.Counters".equals
	    (hc.getMethod().getDeclaringClass().getName()))
	    return hc; // skip this class.
	// total allocation counter.
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
	public void visit(ANEW q) {
	    // do not insert code if none of the relevant counters are enabled.
	    if (!enabled(q.hclass(), q.dimsLength())) return;
	    // okay, we have to calculate array lengths & whatnot.
	    Edge e = q.prevEdge(0);
	    HClass type = q.hclass();
	    Temp Tc = new Temp(qf.tempFactory(), "cntA");
	    Temp Tl = new Temp(qf.tempFactory(), "cntB");
	    e = addAt(e, new CONST(qf, null, Tc, new Long(1), HClass.Long));
	    for (int i=0; i<q.dimsLength(); i++) {
		type = type.getComponentType();
		// Tc contains how *many* arrays of this type
		// Tl will contain total *length* of these arrays.
		e = addAt(e, new OPER(qf, null, Qop.I2L, Tl,
				      new Temp[] { q.dims(i) } ));
		e = addAt(e, new OPER(qf, null, Qop.LMUL, Tl,
				      new Temp[] { Tc, Tl } ));
		// add counters
		e = CounterFactory.spliceIncrement
		    (qf, e, arrayprefix(type)+".count", Tc, true/*long*/);
		e = CounterFactory.spliceIncrement
		    (qf, e, arrayprefix(type)+".length", Tl, true/*long*/);
		// swap Tl and Tc
		{ Temp t=Tl; Tl=Tc; Tc=t; }
		// new type.
	    }
	    // done.
	}
	private String arrayprefix(HClass component_type) {
	    String typename = component_type.isPrimitive() ?
		component_type.getName() : "object";
	    return "sizecnt.array."+typename;
	}
	private boolean enabled(HClass arraytype, int dims) {
	    HClass type = arraytype.getComponentType();
	    for (int i=0; i<dims; i++, type = type.getComponentType())
		if (CounterFactory.isEnabled(arrayprefix(type)+".count") ||
		    CounterFactory.isEnabled(arrayprefix(type)+".length"))
		    return true;
	    return false;
	}

	public void visit(NEW q) {
	    Edge e = q.prevEdge(0);
	    e = CounterFactory.spliceIncrement(qf, e, "sizecnt.object.count");
	    int size = tb.headerSize(q.hclass())+tb.objectSize(q.hclass());
	    e = CounterFactory.spliceIncrement(qf, e,
					       "sizecnt.object.size."+size);
	    e = CounterFactory.spliceIncrement
		(qf, e, "sizecnt.count_by_type."+q.hclass().getName());
	    e = CounterFactory.spliceIncrement
		(qf, e, "sizecnt.bytes_by_type."+q.hclass().getName(), size);
	    // special bitwidth-aware counters.
	    if (bwa!=null) {
		int bits=0, rounded_bits=0;
		for (HClass hc=q.hclass(); hc!=null; hc=hc.getSuperclass()) {
		    for (Iterator it=new ArrayIterator(hc.getDeclaredFields());
			 it.hasNext(); ) {
			HField hf = (HField) it.next();
			if (hf.isStatic()) continue;
			int mybits;
			if (hf.getType()==HClass.Float)
			    mybits = 32;
			else if (hf.getType()==HClass.Double)
			    mybits = 64;
			else if (!hf.getType().isPrimitive())
			    mybits = frame.pointersAreLong() ? 64 : 32;
			else
			    mybits = Math.max(bwa.plusWidthMap(hf),
					      bwa.minusWidthMap(hf));
			bits += mybits;
			rounded_bits += ((mybits+7)/8)*8;
		    }
		}
		e = CounterFactory.spliceIncrement
		    (qf, e, "sizecnt.bits_by_type."+q.hclass().getName(),bits);
		e = CounterFactory.spliceIncrement
		    (qf, e, "sizecnt.rounded_bits_by_type."+q.hclass().getName(),rounded_bits);
	    }
	}
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
