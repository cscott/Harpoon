// Nonvirtualize.java, created Fri Oct 20 23:35:56 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Backend.Maps.FinalMap;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Quad;
/**
 * <code>Nonvirtualize</code> uses a <code>FinalMap</code> to
 * change virtual method invocations to non-virtual invocations,
 * where this is safe.  Virtual method invocation is slower
 * than non-virtual invocation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Nonvirtualize.java,v 1.1.2.1 2000-10-21 20:20:10 cananian Exp $
 * @see harpoon.Backend.Maps.DefaultFinalMap
 * @see harpoon.Backend.Maps.CHFinalMap
 */
public class Nonvirtualize
    extends harpoon.Analysis.Transformation.MethodMutator {
    /** The <code>FinalMap</code> we will consult for our devirtualization. */
    private final FinalMap fm;
    
    /** Creates a <code>Nonvirtualize</code> using the given
     *  <code>FinalMap</code>.
     * @see harpoon.Backend.Maps.DefaultFinalMap
     * @see harpoon.Backend.Maps.CHFinalMap
     */
    public Nonvirtualize(HCodeFactory hcf, FinalMap fm) {
	super(hcf);
	this.fm = fm;
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	Quad[] quads = (Quad[]) hc.getElements();
	for (int i=0; i<quads.length; i++) {
	    if (!(quads[i] instanceof CALL)) continue;
	    CALL q = (CALL) quads[i];
	    if (!q.isVirtual()) continue;
	    if (!fm.isFinal(q.method())) continue;
	    // okay, this is a CALL to a final method.  make it non-virtual!
	    CALL nq = new CALL(q.getFactory(), q, q.method(), q.params(),
			       q.retval(), q.retex(), false/*isVirtual*/,
			       q.isTailCall(), q.dst(), q.src());
	    Quad.replace(q, nq);
	    Quad.transferHandlers(q, nq);
	    // ta-da!
	}
	return hc;
    }
}
