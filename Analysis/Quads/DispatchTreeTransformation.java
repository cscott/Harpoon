// DispatchTreeTransformation.java, created Fri Oct 13 19:33:06 2000 by cananian
// Copyright (C) 2000 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Maps.ConstMapProxy;
import harpoon.Analysis.Maps.ExactTypeMap;
import harpoon.Analysis.Maps.ExactTypeMapProxy;
import harpoon.Analysis.Maps.ExecMapProxy;
import harpoon.Analysis.Quads.SCC.SCCAnalysis;
import harpoon.Analysis.Quads.SCC.SCCOptimize;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadRSSx;
import harpoon.IR.Quads.QuadSSI;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>DispatchTreeTransformation</code> replaces dynamic dispatch
 * call sites with TYPESWITCHes leading to static dispatch calls.
 * Given proper optimization of the TYPESWITCH test, this should
 * speed up dispatch.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: DispatchTreeTransformation.java,v 1.3 2002-02-26 22:41:41 cananian Exp $
 */
public class DispatchTreeTransformation
    extends harpoon.Analysis.Transformation.MethodMutator {
    private final static int CUTOFF = 10;

    final ClassHierarchy ch;
    
    /** Creates a <code>DispatchTreeTransformation</code>. */
    public DispatchTreeTransformation(HCodeFactory parent, ClassHierarchy ch) {
	// we take in SSI, and output RSSx.
        super(harpoon.IR.Quads.QuadSSI.codeFactory(parent));
	this.ch = ch;
    }
    protected HCode mutateHCode(HCodeAndMaps input) {
	HCode ahc = input.ancestorHCode();
	HCode hc = input.hcode();
	Util.ASSERT(ahc.getName().equals(harpoon.IR.Quads.QuadSSI.codename));
	Util.ASSERT(hc.getName().equals(harpoon.IR.Quads.QuadRSSx.codename));
	// do a type analysis of the method.
	SCCAnalysis scc = new SCCAnalysis(ahc);
	ExactTypeMap etm = new ExactTypeMapProxy(input, scc);
	new SCCOptimize(etm, 
			new ConstMapProxy(input, scc),
			new ExecMapProxy(input, scc)).optimize(hc);
	// now look for CALLs & devirtualize some of them.
	for (Iterator it=hc.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    if (q instanceof CALL && examineCALL((CALL)q, etm))
		devirtualizeCALL((CALL)q, etm);
	}
	// done!
	return hc;
    }
    protected HCodeAndMaps cloneHCode(HCode hc, HMethod newmethod) {
	// make SSI into RSSx.
	Util.ASSERT(hc.getName().equals(QuadSSI.codename));
	return MyRSSx.cloneToRSSx((harpoon.IR.Quads.Code)hc, newmethod);
    }
    private static class MyRSSx extends QuadRSSx {
	private MyRSSx(HMethod m) { super(m, null); }
	public static HCodeAndMaps cloneToRSSx(harpoon.IR.Quads.Code c,
					       HMethod m) {
	    MyRSSx r = new MyRSSx(m);
	    return r.cloneHelper(c, r);
	}
    }
    protected String mutateCodeName(String codeName) {
	Util.ASSERT(codeName.equals(QuadSSI.codename));
	return MyRSSx.codename;
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
	// now we have to count up possible receivers (excl. abstract classes)
	int n = numChildren(type);
	if (!Modifier.isAbstract(type.getModifiers())) n++;
	// okay, figure out if it's worth it.
	return n < CUTOFF;
    }
    // separate method as it's recursive.
    private int numChildren(HClass hc) {
	// note that we don't count abstract classes in the total.
	int n=0;
	for (Iterator it=ch.children(hc).iterator(); it.hasNext(); ) {
	    HClass hcc = (HClass) it.next();
	    if (!Modifier.isAbstract(hcc.getModifiers())) n += 1;
	    n += numChildren(hcc);
	}
	return n;
    }

    /** The meat of the transformation. */
    void devirtualizeCALL(CALL call, ExactTypeMap etm) {
	QuadFactory qf = call.getFactory();
	Temp recvr = call.params(0);

	// find the set of relevant calls.
	List methods = new ArrayList();
	{ // find the actual method which will be called.
	    HClass hc = etm.typeMap(null, recvr);
	    HMethod declarM = call.method();
	    HMethod actualM = hc.getMethod(declarM.getName(),
					   declarM.getDescriptor());
	    methods.add(actualM);
	    if (!etm.isExactType(null, recvr))
		methods.addAll(ch.overrides(hc, actualM, true));
	}
	// remove uncallable methods.
	methods.retainAll(ch.callableMethods());
	// remove interface and abstract methods.
	for (Iterator it=methods.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if (Modifier.isAbstract(hm.getModifiers()) ||
		hm.getDeclaringClass().isInterface())
		it.remove();
	}
	// could be that this method is completely uncallable.  SKIP IT.
	if (methods.size()==0) return;
	// now sort methods from most-specific to least-specific
	Collections.sort(methods, new Comparator() {
	    // ascending order.  smallest is most-specific
	    public int compare(Object o1, Object o2) { // neg if o1 more sp.
		HMethod hm1 = (HMethod)o1, hm2 = (HMethod)o2;
		HClass hc1=hm1.getDeclaringClass(),hc2=hm2.getDeclaringClass();
		return hc1.isInstanceOf(hc2)? -1: hc2.isInstanceOf(hc1)? 1: 0;
	    }
	});

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
	Util.ASSERT(spcalls.size()>0);
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
}
