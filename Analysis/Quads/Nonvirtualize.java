// Nonvirtualize.java, created Fri Oct 20 23:35:56 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Backend.Maps.FinalMap;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Quad;

import java.util.Set;
/**
 * <code>Nonvirtualize</code> uses a <code>FinalMap</code> to
 * change virtual method invocations to non-virtual invocations,
 * where this is safe.  Virtual method invocation is slower
 * than non-virtual invocation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Nonvirtualize.java,v 1.1.2.2 2000-11-12 03:07:07 cananian Exp $
 * @see harpoon.Backend.Maps.DefaultFinalMap
 * @see harpoon.Backend.Maps.CHFinalMap
 */
public class Nonvirtualize
    extends harpoon.Analysis.Transformation.MethodMutator {
    /** The <code>FinalMap</code> we will consult for our devirtualization. */
    private final FinalMap fm;
    /** An optional set of callable methods to limit our devirtualization. */
    private final Set callable;
    
    /** Creates a <code>Nonvirtualize</code> using the given
     *  <code>FinalMap</code>.
     * @see harpoon.Backend.Maps.DefaultFinalMap
     * @see harpoon.Backend.Maps.CHFinalMap
     */
    public Nonvirtualize(HCodeFactory hcf, FinalMap fm) {
	this(hcf, fm, null);
    }
    /** Creates a <code>Nonvirtualize</code> using the given
     *  <code>FinalMap</code>; the given <code>ClassHierarchy</code>
     *  is used to avoid devirtualizing calls to uncallable methods
     *  (which artificially inflates the class hierarchy size).
     * @see harpoon.Backend.Maps.DefaultFinalMap
     * @see harpoon.Backend.Maps.CHFinalMap
     */
    public Nonvirtualize(HCodeFactory hcf, FinalMap fm, ClassHierarchy ch) {
	super(hcf);
	this.fm = fm;
	this.callable = (ch==null) ? null : ch.callableMethods();
    }

    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	Quad[] quads = (Quad[]) hc.getElements();
	for (int i=0; i<quads.length; i++) {
	    if (!(quads[i] instanceof CALL)) continue;
	    CALL q = (CALL) quads[i];
	    if (!q.isVirtual()) continue;
	    if (!fm.isFinal(q.method())) continue;
	    // don't devirtualize uncallable methods, as the static reference
	    // will make it seem like they are callable.
	    if (callable!=null && !callable.contains(q.method())) continue;
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
