// EpilogMutator.java, created Fri Feb 23 02:28:16 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Counters;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadNoSSA;
import harpoon.IR.Quads.QuadRSSx;
import harpoon.IR.Quads.QuadSSA;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * <code>EpilogMutator</code> adds the appropriate call to
 * <code>harpoon.Runtime.Counters</code><code>.report()</code> at the
 * end of the main method and just before any call to
 * <code>System.exit()</code>.  For package-internal use by
 * <code>CounterFactory</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: EpilogMutator.java,v 1.2 2002-02-25 20:56:26 cananian Exp $
 */
class EpilogMutator extends harpoon.Analysis.Transformation.MethodMutator {
    private final HMethod HMmain, HMreport, HMexit;
    
    /** Creates a <code>EpilogMutator</code>. */
    public EpilogMutator(HCodeFactory parent, Linker l, HMethod main) {
	super(parent);
	this.HMmain = main;
	this.HMexit = l.forName("java.lang.Runtime")
	    .getDeclaredMethod("exit","(I)V");
	this.HMreport = l.forName("harpoon.Runtime.Counters")
	    .getDeclaredMethod("report","()V");
	Util.assert(HMmain.isStatic());
	Util.assert(!HMexit.isStatic());
	Util.assert(HMreport.isStatic());
	Util.assert(parent.getCodeName().equals(QuadNoSSA.codename) ||
		    parent.getCodeName().equals(QuadRSSx.codename) ||
		    parent.getCodeName().equals(QuadSSA.codename) ||
		    parent.getCodeName().equals(QuadSSI.codename));
    }
    public HCode mutateHCode(HCodeAndMaps input) {
	final boolean isMain = input.hcode().getMethod().equals(HMmain);
	QuadVisitor qv = new QuadVisitor() {
	    public void visit(Quad q) { /* do nothing */ }
	    public void visit(RETURN q) {
		if (isMain) addReportBefore(q);
	    }
	    public void visit(THROW q) {
		if (isMain) addReportBefore(q);
	    }
	    public void visit(CALL q) {
		if (q.method().equals(HMexit)) addReportBefore(q);
	    }
	    private void addReportBefore(Quad q) {
		Util.assert(q.prevLength()==1);
		QuadFactory qf = q.getFactory();
		Temp t = new Temp(qf.tempFactory(), "ignore");
		Quad q0 = new CALL(qf, q, HMreport, new Temp[0],
				   null, t, false, false, new Temp[0]);
		Quad q1 = new PHI(qf, q, new Temp[0], 2);
		Quad.addEdge(q0, 0, q1, 0);
		Quad.addEdge(q0, 1, q1, 1);
		Edge e = q.prevEdge(0);
		Quad.addEdge((Quad)e.from(), e.which_succ(), q0, 0);
		Quad.addEdge(q1, 0, (Quad)e.to(), e.which_pred());
	    }
	};
	Quad[] qa = (Quad[]) input.hcode().getElements();
	for (int i=0; i<qa.length; i++)
	    qa[i].accept(qv);
	return input.hcode();
    }
}
