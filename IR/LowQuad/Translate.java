// Translate.java, created Fri Jan 22 17:07:31 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.Analysis.AllocationInformationMap;
import harpoon.Analysis.Maps.AllocationInformation;
import harpoon.Analysis.Maps.Derivation.DList;
import harpoon.Analysis.Maps.TypeMap;
import harpoon.Backend.Maps.FinalMap;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator; 
import java.util.Map;

/**
 * <code>Translate</code> is a utility class which implements the 
 * <code>QuadSSI</code>/<code>QuadNoSSA</code> to 
 * <code>LowQuadSSI</code>/<code>LowQuadNoSSA</code> translation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Translate.java,v 1.2 2002-02-25 21:04:40 cananian Exp $
 */
final class Translate { // not public
    public static final Quad translate(final LowQuadFactory qf,
				       final harpoon.IR.Quads.Code code,
				       TypeMap tym, FinalMap fm,
				       Map derivationTable,
				       Map typeTable,
				       AllocationInformationMap allocInfo) {
	final Quad old_header = (Quad) code.getRootElement();
	final CloningTempMap ctm =
	    new CloningTempMap(old_header.getFactory().tempFactory(),
			       qf.tempFactory());
	final LowQuadMap lqm = new LowQuadMap();
	final Visitor v = new Visitor(qf, lqm, ctm, code, tym, fm,
				      derivationTable, typeTable, allocInfo);

	// visit all.
	for (Iterator i = code.getElementsI(); i.hasNext(); ) 
	    ((Quad)i.next()).accept(v);
	// now qm contains mappings from old to new, we just have to link them
	for (Iterator i = code.getElementsI(); i.hasNext(); ) {
	    Quad old = (Quad)i.next();
	    // link next
	    Edge[] el = old.nextEdge();
	    for (int n=0; n<el.length; n++)
		Quad.addEdge(lqm.getFoot((Quad)el[n].from()),
			     el[n].which_succ(),
			     lqm.getHead((Quad)el[n].to()),
			     el[n].which_pred());
	}
	// return new header.
	return lqm.getHead(old_header);
    }

    private static class LowQuadMap {
	final private Map h  = new HashMap();
        void put(Quad old, Quad new_header, Quad new_footer) {
	    h.put(old, new Quad[] { new_header, new_footer });
	}
	Quad getHead(Quad old) {
	    Quad[] ql = (Quad[])h.get(old); return (ql==null)?null:ql[0];
	}
	Quad getFoot(Quad old) {
	    Quad[] ql = (Quad[])h.get(old); return (ql==null)?null:ql[1];
	}
        
	boolean contains(Quad old) { return h.containsKey(old); }
    }

    private static final class Visitor extends QuadVisitor {
	final LowQuadFactory qf;
	final LowQuadMap lqm;
	final CloningTempMap ctm;
	final harpoon.IR.Quads.Code code;
	final TypeMap tym;
	final FinalMap fm;
	final Map dT, tT;
	final AllocationInformation oldai;
	final AllocationInformationMap aim;

	Visitor(LowQuadFactory qf, LowQuadMap lqm, CloningTempMap ctm,
		harpoon.IR.Quads.Code code, TypeMap tym, FinalMap fm,
		Map dT, Map tT, AllocationInformationMap aim) {
	    this.qf = qf; this.lqm = lqm; this.ctm = ctm;
	    this.code = code; this.tym = tym; this.fm = fm; this.dT = dT;
	    this.tT = tT;
	    this.oldai = code.getAllocationInformation(); this.aim = aim;
	}

        private void updateTypeInfo(Quad q) {
	    Temp[] defs = q.def();
	    for (int i=0; i<defs.length; i++)
		tT.put(map(defs[i]), tym.typeMap(q, defs[i]));
	}

	/** By default, just clone and set all destinations to top. */
	public void visit(Quad q) {
	    Quad nq = (Quad) q.clone(qf, ctm);
	    lqm.put(q, nq, nq);
	    updateTypeInfo(q);
	    if (oldai!=null && (q instanceof ANEW || q instanceof NEW))
		aim.transfer(nq, q, ctm, oldai);
	}

	// take apart array references.
	public final void visit(harpoon.IR.Quads.AGET q) {
	    Quad q0 = new PARRAY(qf, q, extra(q.objectref()),
				 map(q.objectref()));
	    Quad q1 = new PAOFFSET(qf,q,extra(q.index()),type(q,q.objectref()),
				   map(q.index()));
	    Quad q2 = new POPER(qf, q, LQop.PADD, extra(q.objectref()),
				new Temp[] { q0.def()[0], q1.def()[0] });
	    Quad q3 = new PGET(qf, q, map(q.dst()), q2.def()[0],
			       type(q,q.objectref()).getComponentType());
	    Quad.addEdges(new Quad[] { q0, q1, q2, q3 });
	    lqm.put(q, q0, q3);
	    // update derivation table.
	    DList dl = new DList(map(q.objectref()), true, null);
	    dT.put(q0.def()[0], dl);
	    dT.put(q2.def()[0], dl);
	    // update type info
	    updateTypeInfo(q);
	    tT.put(q1.def()[0], HClass.Int);// maybe should be HClass.Void?
	}

	public final void visit(harpoon.IR.Quads.ASET q) {
	    Quad q0 = new PARRAY(qf, q, extra(q.objectref()),
				 map(q.objectref()));
	    Quad q1 = new PAOFFSET(qf,q,extra(q.index()),type(q,q.objectref()),
				   map(q.index()));
	    Quad q2 = new POPER(qf, q, LQop.PADD, extra(q.objectref()),
				new Temp[] { q0.def()[0], q1.def()[0] });
	    Quad q3 = new PSET(qf, q, q2.def()[0], map(q.src()),
			       type(q,q.objectref()).getComponentType());
	    Quad.addEdges(new Quad[] { q0, q1, q2, q3 });
	    lqm.put(q, q0, q3);
	    // update derivation table.
	    DList dl = new DList(map(q.objectref()), true, null);
	    dT.put(q0.def()[0], dl);
	    dT.put(q2.def()[0], dl);
	    // Update type information
	    updateTypeInfo(q);
	    tT.put(q1.def()[0], HClass.Int);// maybe should be HClass.Void?
	}

	public final void visit(harpoon.IR.Quads.CALL q) {
	    boolean isVirtual;
	    Quad q0, qN;
	    if (!q.isVirtual() || fm.isFinal(q.method())) {
		// non-virtual or final.  Method address is constant.
		isVirtual = false;
		q0 = qN = new PMCONST(qf, q, extra(), q.method());
		// Map q0.def()[0] to a generic pointer type.
		tT.put(q0.def()[0], HClass.Void); // opaque pointer
	    } else { // virtual; perform table lookup.
		isVirtual = true;
		q0 = new PMETHOD(qf, q, extra(q.params(0)), map(q.params(0)));
		Quad q1 = new PMOFFSET(qf, q, extra(q.params(0)), q.method());
		Quad q2 = new POPER(qf, q, LQop.PADD, extra(q.params(0)),
				    new Temp[] { q0.def()[0], q1.def()[0] });
		qN = q2;
		Quad.addEdges(new Quad[] { q0, q1, q2 });
		// update derivation table.
		// pointers into claz structure are opaque.
		tT.put(q0.def()[0], HClass.Void);
		tT.put(q1.def()[0], HClass.Int);
		tT.put(q2.def()[0], HClass.Void);
	    }
	    updateTypeInfo(q);
	    // for some reason SIGMA doesn't have a whole-array accessor.
	    Temp[]   nsrc = new Temp[q.numSigmas()];
	    Temp[][] ndst = new Temp[q.numSigmas()][];
	    for (int i=0; i<nsrc.length; i++) { // copy copy copy
		nsrc[i] = q.src(i); ndst[i] = q.dst(i);
	    }
	    // CALL->PCALL.
	    Quad q3 = new PCALL(qf, q, qN.def()[0], map(q.params()),
				map(q.retval()), map(q.retex()),
				map(ndst), map(nsrc),
				isVirtual, q.isTailCall());
	    Quad.addEdge(qN, 0, q3, 0);
	    lqm.put(q, q0, q3);
	}

	public final void visit(harpoon.IR.Quads.GET q) {
	    Quad q0, qN;
	    if (q.isStatic()) {
		q0 = qN = new PFCONST(qf, q, extra(), q.field());
		// Map q0.def()[0] to a generic pointer type.
		tT.put(q0.def()[0], HClass.Void);
	    } else { // virtual
		q0 = new PFIELD(qf, q,
				extra(q.objectref()), map(q.objectref()));
		Quad q1 = new PFOFFSET(qf, q, extra(q.objectref()), q.field());
		Quad q2 = new POPER(qf, q, LQop.PADD, extra(q.objectref()),
				    new Temp[] { q0.def()[0], q1.def()[0] });
		qN = q2;
		Quad.addEdges(new Quad[] { q0, q1, q2 });
		// update derivation table.
		DList dl = new DList(map(q.objectref()), true, null);
		dT.put(q0.def()[0], dl);
		dT.put(q2.def()[0], dl);
		tT.put(q1.def()[0], HClass.Int);
	    }
	    updateTypeInfo(q);
	    Quad q3 = new PGET(qf, q, map(q.dst()), qN.def()[0],
			       q.field().getType());
	    Quad.addEdge(qN, 0, q3, 0);
	    lqm.put(q, q0, q3);
	}

	public final void visit(harpoon.IR.Quads.OPER q) {
	    // mutate into POPER
	    Quad nq = new POPER(qf, q, q.opcode(),
			       map(q.dst()), map(q.operands()));
	    lqm.put(q, nq, nq);
	    updateTypeInfo(q);
	}

	public final void visit(harpoon.IR.Quads.SET q) {
	    Quad q0, qN;
	    if (q.isStatic()) {
		q0 = qN = new PFCONST(qf, q, extra(), q.field());	
		// Map q0.def()[0] to a generic pointer type.
		tT.put(q0.def()[0], HClass.Void);
	    } else { // virtual
		q0 = new PFIELD(qf, q,
				extra(q.objectref()), map(q.objectref()));
		Quad q1 = new PFOFFSET(qf, q, extra(q.objectref()), q.field());
		Quad q2 = new POPER(qf, q, LQop.PADD, extra(q.objectref()),
				    new Temp[] { q0.def()[0], q1.def()[0] });
		qN = q2;
		Quad.addEdges(new Quad[] { q0, q1, q2 });
		// update derivation table.
		DList dl = new DList(map(q.objectref()), true, null);
		dT.put(q0.def()[0], dl);
		dT.put(q2.def()[0], dl);
		tT.put(q1.def()[0], HClass.Int);
	    }
	    updateTypeInfo(q);
	    Quad q3 = new PSET(qf, q, qN.def()[0], map(q.src()),
			       q.field().getType());
	    Quad.addEdge(qN, 0, q3, 0);
	    lqm.put(q, q0, q3);
	}
      
	//---------------------------------------------------------
 	// UTILITY FUNCTIONS:
	private Temp extra() { return new Temp(qf.tempFactory(), "lq_"); }
	private Temp extra(Temp t) { return t.clone(qf.tempFactory()); }
	private HClass type(HCodeElement hce, Temp t) { 
	    return tym.typeMap(hce, t); 
	}
	private Temp map(Temp t) {
	    return (t==null)?null:ctm.tempMap(t);
	}
	private Temp[] map(Temp[] ta) {
	    Temp[] r = new Temp[ta.length];
	    for (int i=0; i<r.length; i++)
		r[i] = map(ta[i]);
	    return r;
	}
	private Temp[][] map(Temp[][] taa) {
	    Temp[][] r = new Temp[taa.length][];
	    for (int i=0; i<r.length; i++)
		r[i] = map(taa[i]);
	    return r;
	}
    }
}
