// Virtualize.java, created Mon Sep  9 18:16:57 2002 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Quad;
import net.cscott.jutil.SnapshotIterator;

import java.util.Iterator;
import java.util.Set;
/**
 * <code>Virtualize</code> makes any non-virtual invocations of
 * uncallable methods non-virtual, so that they don't lead to
 * link errors later. It works on any sort of quad form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Virtualize.java,v 1.2 2004-02-08 01:53:14 cananian Exp $
 * @see harpoon.Analysis.Quads.Nonvirtualize
 * @see harpoon.Analysis.Quads.QuadClassHierarchy
 */
public class Virtualize
    extends harpoon.Analysis.Transformation.MethodMutator<Quad> {
    /** The set of callable methods we will use to guide our virtualization. */
    private final Set<HMethod> callable;

    /** Creates a <code>Virtualize</code> code factory using the given
     *  <code>ClassHierarchy</code>. */
    public Virtualize(HCodeFactory hcf, ClassHierarchy ch) {
	super(hcf);
	this.callable = ch.callableMethods();
    }

    protected HCode<Quad> mutateHCode(HCodeAndMaps<Quad> input) {
	HCode<Quad> hc = input.hcode();
	for (Iterator<Quad> it = new SnapshotIterator<Quad>(hc.getElementsI());
	     it.hasNext(); ) {
	    Quad aquad = it.next();
	    if (!(aquad instanceof CALL)) continue;
	    CALL q = (CALL) aquad;
	    if (q.isVirtual()) continue;
	    if (callable.contains(q.method())) continue;
	    assert !q.method().isStatic() : "non-callable static?  how?";
	    // okay, CALL to a non-callable method.  make it virtual!
	    CALL nq = new CALL(q.getFactory(), q, q.method(), q.params(),
			       q.retval(), q.retex(), true/*isVirtual*/,
			       q.isTailCall(), q.dst(), q.src());
	    Quad.replace(q, nq);
	    Quad.transferHandlers(q, nq);
	    // ta-da!
	}
	return hc;
    }
}
