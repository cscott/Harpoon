// WriteBarrierQuadPass.java, created Tue Aug 21 19:42:49 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Runtime1.Data;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.Code;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Util;

/**
 * <code>WriteBarrierQuadPass</code> takes code in Quad form and
 * inserts a fake call to a write barrier that is later replaced with
 * a real implementation in <code>WriteBarrierTreePass</code>.
 *
 * When used without <code>WriteBarrierTreePass</code> and in 
 * combination with the mark-and-sweep collector, reports statistics 
 * about the number of times the write-barrier is called.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: WriteBarrierQuadPass.java,v 1.1.2.1 2001-08-30 23:08:15 kkz Exp $
 */
public class WriteBarrierQuadPass extends 
    harpoon.Analysis.Transformation.MethodMutator {
    
    final private HMethod arraySC;
    final private HMethod fieldSC;
    final private HClass JLT;
    final private HClass JLRF;
    private WriteBarrierStats wbs;

    /** Creates a <code>WriteBarrierQuadPass</code>. */
    public WriteBarrierQuadPass(HCodeFactory parent, Linker linker) {
	super(parent);
	this.JLT = linker.forName("java.lang.Throwable");
	HClass WB = linker.forName("harpoon.Runtime.PreciseGC.WriteBarrier");
	HClass JLO = linker.forName("java.lang.Object");
	this.JLRF = linker.forName("java.lang.reflect.Field");
	this.arraySC = WB.getMethod("asc", new HClass[] 
				    { JLO, HClass.Int, JLO, HClass.Int });
	this.fieldSC = WB.getMethod("fsc", new HClass[] 
				    { JLO, JLRF, JLO, HClass.Int });
    }
    
    protected HCode mutateHCode(HCodeAndMaps input) {
	Code hc = (Code)input.hcode();
	// we should not have to update derivation information
	Util.assert(hc.getDerivation() == null);
	//hc.print(new java.io.PrintWriter(System.out), null);
	HEADER header = (HEADER) hc.getRootElement();
	FOOTER footer = (FOOTER) header.footer();
	QuadVisitor qv = new WriteBarrierVisitor(JLT, JLRF, arraySC, 
						 fieldSC, footer);
	// we put all elements in array to avoid screwing up the
	// iterator as we mutate the quad graph in-place.
	Quad[] allquads = (Quad[]) hc.getElements();
	for (int i=0; i<allquads.length; i++)
	    allquads[i].accept(qv);
	// yay, done!
	//hc.print(new java.io.PrintWriter(System.out), null);
	return hc;
    }

    /** Return an <code>HCodeFactory</code> that will clean up the
     *  tree form of the transformed code by performing some optimizations
     *  which can't be represented in quad form. */
    public HCodeFactory treeCodeFactory(Frame f, HCodeFactory hcf, 
					ClassHierarchy ch) {
	return WriteBarrierTreePass.codeFactory
	    (hcf, f, ch, arraySC, fieldSC);
    }

    /** Return an <code>HCodeFactory</code> that will clean up the
     *  tree form of the transformed code by performing some optimizations
     *  which can't be represented in quad form; namely, removes
     *  write barriers for MOVEs that are assigned from constants. 
     *  This pass is needs to be run before the pass returned by
     *  <code>treeCodeFactory</code> to have any effect. */
    public HCodeFactory ceCodeFactory(Frame f, HCodeFactory hcf) {
	return WriteBarrierConstElim.codeFactory
	    (f, hcf, arraySC, fieldSC);
    }

    /** Code factory for post pass. Emits data needed for gathering
     *  write barrier statistics. This pass needs to be run before
     *  the pass returned by <code>treeCodeFactory</code> to have
     *  any effect.
     */
    public HCodeFactory statsCodeFactory(Frame f, HCodeFactory hcf,
					 ClassHierarchy ch) {
	this.wbs = new WriteBarrierStats(f, hcf, ch, arraySC, fieldSC);
	return wbs.codeFactory();
    }

    /** <code>Data</code> for gathering statistics on write barriers.
     *  Needs the results of the pass returned by
     *  <code>statsCodeFactory</code>.
     */
    public Data getData(HClass hc, Frame f) {
	// must have run statsCodeFactory first
	Util.assert(wbs != null);
	return new WriteBarrierData(hc, f, wbs.getCount());
    }

    private static class WriteBarrierVisitor extends QuadVisitor {
	final HClass JLT;
	final HClass JLRF;
	final HMethod arraySC;
	final HMethod fieldSC;
	FOOTER footer;
	WriteBarrierVisitor(HClass JLT, HClass JLRF, HMethod arraySC, 
			    HMethod fieldSC, FOOTER footer) {
	    this.JLT = JLT;
	    this.JLRF = JLRF;
	    this.arraySC = arraySC;
	    this.fieldSC = fieldSC;
	    this.footer = footer;
	}
	public void visit(Quad q) { /* do nothing */ }
	public void visit(ASET q) {
	    if (!q.type().isPrimitive()) {
		QuadFactory qf = (QuadFactory)q.getFactory();
		TempFactory tf = qf.tempFactory();
		// create Temps
		Temp retex = new Temp(tf, "wbex");
		Temp id = new Temp(tf, "wbid");
		// create needed Quads
		CONST idc = new CONST(qf, q, id, new Integer(0), HClass.Int);
		CALL call = new CALL(qf, q, arraySC,
				     new Temp[] { q.objectref(), q.index(), 
						  q.src(), id }, 
				     null, retex, false, false, new Temp[0]);
		THROW thr = new THROW(qf, q, retex);
		// add CONST and CALL before ASET
		splice(idc, q.prevEdge(0));
		splice(call, q.prevEdge(0));
		// add THROW after CALL
		Quad.addEdge(call, 1, thr, 0);
		footer = footer.attach(thr, 0);
	    }
	}
	public void visit(SET q) {
	    // we are interested only in non-static fields containing pointers
	    if (!q.isStatic() && !q.field().getType().isPrimitive()) {
		QuadFactory qf = (QuadFactory)q.getFactory();
		TempFactory tf = qf.tempFactory();
		// create needed Temps
		Temp field = new Temp(tf, "wbfield");
		Temp retex = new Temp(tf, "wbex");
		Temp id = new Temp(tf, "wbid");
		// create needed Quads
		CONST idc = new CONST(qf, q, id, new Integer(0), HClass.Int);
		CONST fieldc = new CONST(qf, q, field, q.field(), JLRF); 
		CALL call = new CALL(qf, q, fieldSC,
				     new Temp[] { q.objectref(), field, 
						  q.src(), id }, 
				     null, retex, false, false, new Temp[0]);
		THROW thr = new THROW(qf, q, retex);
		// add CONSTs and CALL before SET
		splice(idc, q.prevEdge(0));
		splice(fieldc, q.prevEdge(0));
		splice(call, q.prevEdge(0));
		// add THROW after CALL
		Quad.addEdge(call, 1, thr, 0);
		footer = footer.attach(thr, 0);
	    }
	}
	/** inserts the given Quad on the given Edge */
	private void splice(Quad q, Edge e) {
	    Quad.addEdge((Quad)e.from(), e.which_succ(), q, 0);
	    Quad.addEdge(q, 0, (Quad)e.to(), e.which_pred());
	}
    }
}
