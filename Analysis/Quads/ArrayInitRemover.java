// ArrayInitRemover.java, created Mon Jan 22 14:33:36 2001 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.HandlerSet;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

/**
 * <code>ArrayInitRemover</code> converts <code>ARRAYINIT</code> quads
 * into chains of <code>ASET</code> quads.  Works only on hi-quad form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayInitRemover.java,v 1.1.2.1 2001-01-22 20:34:39 cananian Exp $
 */
public final class ArrayInitRemover
    extends harpoon.Analysis.Transformation.MethodMutator {
    
    /** Creates a <code>ArrayInitRemover</code>. */
    public ArrayInitRemover(HCodeFactory parent) { super(parent); }

    protected HCode mutateHCode(HCodeAndMaps input) {
	Code hc = (Code) input.hcode();
	// we put all elements in array to avoid screwing up the
	// iterator as we mutate the quad graph in-place.
	Quad[] allquads = (Quad[]) hc.getElements();
	for (int i=0; i<allquads.length; i++)
	    if (allquads[i] instanceof ARRAYINIT)
		replace((ARRAYINIT) allquads[i]);
	// yay, done!
	return hc;
    }

    private static void replace(ARRAYINIT ai) {
	HandlerSet h = ai.handlers(); // new quads will have same handlers.
	Edge e = ai.nextEdge(0); // all insertion will split this edge.
	QuadFactory qf = ai.getFactory();
	TempFactory tf = qf.tempFactory();
	Object[] value = ai.value();
	for (int i=0; i<value.length; i++) {
	    // temps
	    Temp idxT= new Temp(tf, "idx");
	    Temp valT= new Temp(tf, "val");
	    // quads
	    Quad q0  = new CONST(qf, ai, idxT, new Integer(i+ai.offset()),
				 HClass.Int);
	    Quad q1  = new CONST(qf, ai, valT, widen(value[i], ai.type()),
				 widen(ai.type()));
	    Quad q2  = new ASET(qf, ai, ai.objectref(), idxT, valT, ai.type());
	    // edges
	    Quad.addEdges(new Quad[] { q0, q1, q2 });
	    Quad.addEdge((Quad)e.from(), e.which_succ(), q0, 0);
	    e = Quad.addEdge(q2, 0, (Quad)e.to(), e.which_pred());
	    // handlers
	    q0.addHandlers(h); q1.addHandlers(h); q2.addHandlers(h);
	}
	// remove arrayinit
	ai.remove();
	// done!
    }
    private static HClass widen(HClass hc) {
	return (hc==HClass.Boolean || hc==HClass.Byte ||
		hc==HClass.Short || hc==HClass.Char) ? HClass.Int : hc;
    }
    private static Object widen(Object o, HClass hc) {
	return (hc==HClass.Byte || hc==HClass.Short) ?
	    new Integer(((Number)o).intValue()) :
	    (hc==HClass.Boolean) ?
	    new Integer(((Boolean)o).booleanValue() ? 1 : 0) :
	    (hc==HClass.Char) ?
	    new Integer((int) ((Character)o).charValue()) :
	    o;
    }
}
