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
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadVisitor;
import java.util.List;

/**
 * The <code>SizeCounters</code> code factory adds counters for
 * various allocation properties, to aid in determining the
 * effectiveness of the various size optimizations in this
 * package.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SizeCounters.java,v 1.1.2.2 2001-07-11 06:26:23 cananian Exp $
 */
public class SizeCounters extends MethodMutator {
    final Runtime.TreeBuilder tb;
    
    /** Creates a <code>SizeCounters</code>. */
    public SizeCounters(HCodeFactory parent, Frame frame) {
	super(parent);
	this.tb = frame.getRuntime().getTreeBuilder();
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
	    Edge e = q.prevEdge(0);
	    e = CounterFactory.spliceIncrement(qf, e, "array_count");
	    // code to compute array size and add it to array_size here.
	}
	public void visit(NEW q) {
	    Edge e = q.prevEdge(0);
	    e = CounterFactory.spliceIncrement(qf, e, "object_count");
	    int size = tb.headerSize(q.hclass())+tb.objectSize(q.hclass());
	    e = CounterFactory.spliceIncrement(qf, e, "object_size", size);
	    e = CounterFactory.spliceIncrement(qf, e, "object_size_"+size);
	}
    }
}
