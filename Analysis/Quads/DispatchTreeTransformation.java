// DispatchTreeTransformation.java, created Fri Oct 13 19:33:06 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Maps.ExactTypeMap;
import harpoon.Analysis.Quads.SCC.SCCAnalysis;
import harpoon.Analysis.Quads.SCC.SCCOptimize;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * <code>DispatchTreeTransformation</code> replaces dynamic dispatch
 * call sites with TYPESWITCHes leading to static dispatch calls.
 * Given proper optimization of the TYPESWITCH test, this should
 * speed up dispatch.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DispatchTreeTransformation.java,v 1.1.2.3 2000-11-16 00:11:59 cananian Exp $
 */
public class DispatchTreeTransformation
    extends harpoon.Analysis.Transformation.MethodMutator {
    private final static int CUTOFF = 10;

    final ClassHierarchy ch;
    
    /** Creates a <code>DispatchTreeTransformation</code>. */
    public DispatchTreeTransformation(HCodeFactory parent, ClassHierarchy ch) {
	// convert the code factory to relaxed-quad-ssi form.
        super(harpoon.IR.Quads.QuadRSSx.codeFactory(parent));
	this.ch = ch;
    }
    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode hc = input.hcode();
	// do a type analysis of the method.
	ExactTypeMap etm = new SCCAnalysis(hc);
	new SCCOptimize((SCCAnalysis)etm).optimize(hc);
	// now look for CALLs & devirtualize some of them.
	for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    if (q instanceof CALL && examineCALL((CALL)q, etm))
		devirtualizeCALL((CALL)q, etm);
	}
	// We *could* convert from RSSx to NoSSA or some such, but let's
	// just stay in RSSx form.
	Util.assert(hc.getName().equals(harpoon.IR.Quads.QuadRSSx.codename));
	// done!
	return hc;
    }
    private boolean examineCALL(CALL call, ExactTypeMap etm) {
	// skip if !isVirtual
	if (!call.isVirtual()) return false;
	// skip tail calls, too, just cuz they're funky.
	if (call.isTailCall()) return false;
	// final methods and methods in final classes are good candidates.
	if (Modifier.isFinal(call.method().getModifiers()) ||
	    Modifier.isFinal(call.method().getDeclaringClass().getModifiers()))
	    return true;
	// look at the receiver.
	Temp recv = call.params(0);
	// exact types always have just one receiver.
	if (etm.isExactType(null, recv)) return true;
	HClass type = etm.typeMap(null, recv);
	// now we have to count up possible receivers.
	int n = 1 + numChildren(type);
	// okay, figure out if it's worth it.
	return n < CUTOFF;
    }
    // separate method as it's recursive.
    private int numChildren(HClass hc) {
	int n=0;
	for (Iterator it=ch.children(hc).iterator(); it.hasNext(); )
	    n += 1 + numChildren((HClass)it.next());
	return n;
    }

    /** The meat of the transformation. */
    void devirtualizeCALL(CALL call, ExactTypeMap etm) {
	QuadFactory qf = call.getFactory();
	Temp recvr = call.params(0);

	// find the set of relevant calls.
	List methods = new ArrayList();
	if (!etm.isExactType(null, recvr))
	    collectMethods(etm.typeMap(null, recvr), call.method(),
			   methods);
	methods.add(call.method());
	// okay, so methods are ordered most-specific to least-specific.

	// make devirtualized calls.
	List spcalls = new ArrayList(methods.size());
	for (Iterator it=methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    // note that the quad will no longer be in SSI form when
	    // we're done because all CALLs have the same sigma functions.
	    CALL ncall = new CALL(qf, call, hm, call.params(),
				  call.retval(), call.retex(),
				  false /* isVirtual */,
				  call.isTailCall(),
				  call.dst(), call.src());
	    spcalls.add(ncall);
	}
	Util.assert(spcalls.size()>0);
	// make PHI node for regular and exceptional returns.
	PHI rephi = new PHI(qf, call, new Temp[0], spcalls.size());
	PHI exphi = new PHI(qf, call, new Temp[0], spcalls.size());
	// and TYPESWITCH node
	HClass[] keys = new HClass[methods.size()];
	for (int i=0; i<keys.length; i++)
	    keys[i] = ((HMethod)methods.get(i)).getDeclaringClass();
	TYPESWITCH ts = new TYPESWITCH(qf, call, recvr, keys,
				       new Temp[0], false/* no default*/);
	// link everything up.
	for (int i=0; i<spcalls.size(); i++) {
	    CALL ncall = (CALL) spcalls.get(i);
	    Quad.addEdge(ncall, 0, rephi, i);
	    Quad.addEdge(ncall, 1, exphi, i);
	    Quad.addEdge(ts, i, ncall, 0);
	}
	Edge toE = call.prevEdge(0);
	Edge reE = call.nextEdge(0);
	Edge exE = call.nextEdge(1);
	if (spcalls.size()==1) { // don't link TYPESWITCH and PHIs
	    CALL thecall = (CALL)spcalls.get(0);
	    Quad.addEdge((Quad)toE.from(), toE.which_succ(), thecall, 0);
	    Quad.addEdge(thecall, 0, (Quad)reE.to(), reE.which_pred());
	    Quad.addEdge(thecall, 1, (Quad)exE.to(), exE.which_pred());
	} else { // link up TYPESWITCH and PHIs
	    Quad.addEdge((Quad)toE.from(), toE.which_succ(), ts, 0);
	    Quad.addEdge(rephi, 0, (Quad)reE.to(), reE.which_pred());
	    Quad.addEdge(exphi, 0, (Quad)exE.to(), exE.which_pred());
	}
    }
    void collectMethods(HClass hc, HMethod hm, List l) {
	for (Iterator it=ch.children(hc).iterator(); it.hasNext(); ) {
	    HClass hcc = (HClass) it.next();
	    collectMethods(hcc, hm, l);// first added will be deepest.
	    try {
		l.add(hcc.getDeclaredMethod(hm.getName(), hm.getDescriptor()));
	    } catch (NoSuchMethodError ex) { /* ignore */ }
	}
    }
}
