// WriteBarrierPrePass.java, created Mon Aug 13 18:29:34 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Backend.Generic.Frame;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.LowQuad.DerivationMap;
import harpoon.IR.LowQuad.LowQuadFactory;
import harpoon.IR.LowQuad.LowQuadVisitor;
import harpoon.IR.LowQuad.PCALL;
import harpoon.IR.LowQuad.PMCONST;
import harpoon.IR.LowQuad.PSET;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.Code;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;


/**
 * <code>WriteBarrierPrePass</code> takes code in LowQuad form and
 * inserts a fake call to a write barrier that is later replaced with
 * a real implementation in <code>WriteBarrierPostPass</code>.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: WriteBarrierPrePass.java,v 1.1.2.2 2001-08-30 23:01:40 kkz Exp $
 */
public class WriteBarrierPrePass extends 
    harpoon.Analysis.Transformation.MethodMutator {
    
    final private HMethod dummyHM;
    final private HClass JLT;
    
    /** Creates a <code>WriteBarrierPrePass</code>. */
    public WriteBarrierPrePass(HCodeFactory parent, Linker linker) { 
	super(parent);
	this.JLT = linker.forName("java.lang.Throwable");
	HClass WB = linker.forName("harpoon.Runtime.PreciseGC.WriteBarrier");
	HClass JLO = linker.forName("java.lang.Object");
	this.dummyHM = WB.getMethod("storeCheck", new HClass[] { JLO });
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	Code hc = (harpoon.IR.Quads.Code)input.hcode();
	//hc.print(new java.io.PrintWriter(System.out), null);
	HEADER header = (HEADER) hc.getRootElement();
	FOOTER footer = (FOOTER) header.footer();
	LowQuadVisitor qv = new WriteBarrierVisitor
	    ((DerivationMap)hc.getDerivation(), JLT, dummyHM, footer);
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
    public HCodeFactory treeCodeFactory(Frame f, HCodeFactory hcf) {
	return new WriteBarrierPostPass(f, dummyHM).codeFactory(hcf);
    }


    private static class WriteBarrierVisitor extends LowQuadVisitor {
	final DerivationMap dm;
	final HClass JLT;
	final HMethod dummyHM;
	FOOTER footer;
	WriteBarrierVisitor(DerivationMap dm, HClass JLT, HMethod dummyHM, 
			    FOOTER footer) {
	    this.dm = dm;
	    this.JLT = JLT;
	    this.dummyHM = dummyHM;
	    this.footer = footer;
	}
	public void visit(Quad q) { /* do nothing */ }
	public void visit(PSET q) {
	    if (!q.type().isPrimitive()) {
		LowQuadFactory qf = (LowQuadFactory)q.getFactory();
		TempFactory tf = qf.tempFactory();
		// create needed Temps
		Temp func = new Temp(tf, "wb");
		Temp retex = new Temp(tf, "wbex");
		// create needed LowQuads
		PMCONST pmconst = new PMCONST(qf, q, func, dummyHM);
		PCALL pcall = new PCALL(qf, q, func,
					new Temp[] { q.ptr() }, null, retex, 
					new Temp[0],  false, false);
		THROW thr = new THROW(qf, q, retex);
		// update derivation information
		dm.putType(pmconst, func, HClass.Void);
		dm.putType(pcall, retex, JLT);
		// add PMCONST and PCALL before PSET
		splice(pmconst, q.prevEdge(0));
		splice(pcall, q.prevEdge(0));
		// add THROW after PCALL
		Quad.addEdge(pcall, 1, thr, 0);
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
