// AllocationHoisting.java, created Fri Oct 19 11:32:41 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsImpl;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.SerializableCodeFactory;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.SET;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Tuple;
import harpoon.Util.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>AllocationHoisting</code>
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: AllocationHoisting.java,v 1.3 2002-02-26 22:41:31 cananian Exp $
 */
public class AllocationHoisting extends 
    harpoon.Analysis.Transformation.MethodSplitter {
    
    private final HCodeFactory parent;
    private final ClassHierarchy ch;
    private final Linker linker;
    private final Map iMap;
    private final Map iMap2;
    private final MRAFactory mraf;
    
    /** Token for the hoisted version of an initializer. */
    public static final Token HOISTED = new Token("allochoist") {
	public Object readResolve() { return HOISTED; }
    };
    /** Creates an <code>AllocationHoisting</code>. For efficiency,
     *  <code>parent</code> should be a <code>CachingCodeFactory</code>.
     *  @param parent The input code factory.
     *  @param ch A class hierarchy for the application.
     */
    public AllocationHoisting(HCodeFactory parent, ClassHierarchy ch, 
			      Linker l, String rName, int optLevel) {
        super(parent, ch, true/*doesn't matter*/);
	this.parent = parent;
	this.ch = ch;
	this.linker = l;
	this.mraf = new MRAFactory(ch, parent, linker, rName, optLevel);
	this.iMap = new HashMap();
	this.iMap2 = new HashMap();
	System.out.print("Setting up iMap...");
	setupiMap(rName);
	System.out.println(" done!");
    }

    /** Adds parameter to descriptors of mutated methods. */
    protected String mutateDescriptor(HMethod hm, Token which) {
	String desc = hm.getDescriptor();
	if (which == HOISTED) {
	    Tuple tup = (Tuple) iMap.get(hm);
	    Quad q = (Quad) tup.proj(0);
	    HClass hc = (q.kind() == QuadKind.NEW) ? 
		((NEW)q).hclass() : ((ANEW)q).hclass();
	    int mid = desc.lastIndexOf(')');
	    String cls = hc.getDescriptor();
	    String result = desc.substring(0, mid) + hc.getDescriptor() + 
		desc.substring(mid, desc.length());
	    //System.out.println(desc+" -> "+result);
	    return result;
	}
	return desc;
    }

    /** Hoists allocation out of the split method. */
    protected HCode mutateHCode(HCodeAndMaps input, Token which) {
	Code c = (Code) input.hcode();
	if (which == HOISTED) {
	    hoistAlloc(c);
	} else {
	    Util.ASSERT(which == ORIGINAL);
	    Tuple tup = (Tuple) iMap.get(c.getMethod());
	    if (tup != null) {
		// remap 
		iMap.put(c.getMethod(), remap(tup, input.elementMap()));
	    }
	    modifyCalls(c);
	}
	// System.out.println(c.getMethod()+" is optimizable.\n");
	// c.print(new java.io.PrintWriter(System.out), null);
	return input.hcode();
    }
    
    /** Remaps the <code>Quad</code>s contained in the <code>Tuple</code>
     *  using the iven <code>Map</code>.
     */
    private Tuple remap(Tuple tup, Map m) {
	Quad q0 = (Quad) m.get((Quad)tup.proj(0));
	Util.ASSERT(q0 != null);
	if (q0.kind() == QuadKind.NEW) {
	    return new Tuple(new Object[] { q0 });
	} else {
	    Util.ASSERT(q0.kind() == QuadKind.ANEW);
	    CONST[] orig = (CONST[]) tup.proj(1);
	    CONST[] cloned = new CONST[orig.length];
	    for(int i = 0; i < cloned.length; i++) {
		cloned[i] = (CONST) m.get(orig[i]);
		Util.ASSERT(cloned[i] != null);
	    }
	    return new Tuple(new Object[] { q0, cloned });
	}
    }

    /** Clones <code>HCode</code> and creates an <code>iMap2</code>
     *  entry for the cloned code.
     */
    protected HCodeAndMaps cloneHCode(HCode hc, HMethod newmethod)
	throws CloneNotSupportedException {
	HCodeAndMaps hcam = hc.clone(newmethod);
	Tuple tup = (Tuple) iMap.get(hc.getMethod());
	if (tup != null) {
	    iMap2.put(newmethod, remap(tup, hcam.elementMap()));
	}
	return hcam;
    }

    /** Removes the hoisted allocation from the initializer. */
    private Code hoistAlloc(Code c) {
	Tuple tup = (Tuple) iMap2.get(c.getMethod());
	Util.ASSERT(tup != null);
	Quad q0 = (Quad) tup.proj(0);
	Temp dst;
	if (q0.kind() == QuadKind.NEW) {
	    dst = ((NEW)q0).dst();
	} else {
	    dst = ((ANEW)q0).dst();
	}
	// create a new Temp for the new parameter
	Temp newT = new Temp(q0.getFactory().tempFactory());
	METHOD oldM = ((HEADER)c.getRootElement()).method();
	Temp[] newTs = new Temp[oldM.paramsLength()+1];
	System.arraycopy(oldM.params(), 0, newTs, 0, oldM.paramsLength());
	newTs[newTs.length-1] = newT;
	// create new METHOD with an additional parameter
	METHOD newM = new METHOD(oldM.getFactory(), oldM, newTs, oldM.arity());
	Quad.replace(oldM, newM);
	// replace allocation with assignment
	MOVE m = new MOVE(q0.getFactory(), q0, dst, newT);
	Quad.replace(q0, m);
	//c.print(new java.io.PrintWriter(System.out), null);
	return c;
    }

    /** Add hoisted allocation to code that calls the transformed
     *  initializers, and fix up calls.
     */
    private Code modifyCalls(Code c) {
	// go through all the Quads
	for(Iterator it = c.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    if (q.kind() == QuadKind.CALL) {
		CALL call = (CALL)q;
		Tuple tup = (Tuple) iMap.get(call.method());
		// replace call and add alloc if needed
		if (tup != null) {
		    ReachingDefs rd = new ReachingDefsImpl(c);
		    Object[] defs = rd.reachingDefs
			(call, call.params(0)).toArray();
		    if (defs.length == 1 &&
			((Quad)defs[0]).kind() == QuadKind.NEW) {
			//System.out.println("Transforming "+call);
			// create destination Temp for hoisted allocation
			Temp dst = new Temp(call.getFactory().tempFactory());
			Quad template = (Quad) tup.proj(0);
			// hoist allocation
			if (template.kind() == QuadKind.NEW) {
			    addNEW((Quad)defs[0], dst, 
				   ((NEW)template).hclass()); 
			} else {
			    Util.ASSERT(template.kind() == QuadKind.ANEW);
			    addANEW((Quad)defs[0], dst, 
				    ((ANEW)template).hclass(),
				    (CONST[])tup.proj(1));
			}
			replaceCall(call, select(call.method(), HOISTED), dst);
		    }
		}
	    }
	}
	return c;
    }
	
    /** Add NEW before given <code>Quad</code>, assigning to <code>t</code>. */
    private static void addNEW(Quad q, Temp t, HClass hc) {
	Quad q0 = new NEW(q.getFactory(), q, t, hc);
	Util.ASSERT(q.prevLength() == 1);
	Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), q0, 0);
	Quad.addEdge(q0, 0, q, 0);
    }

    /** Add ANEW and associated CONSTs (for the array dimensions)
     *  before the given <code>Quad</code>, assigning to <code>t</code>.
     */
    private static void addANEW(Quad q, Temp t, HClass hc, CONST[] dims ) {
	QuadFactory qf = q.getFactory();
	TempFactory tf = qf.tempFactory();
	Temp[] dimTs = new Temp[dims.length];
	Quad[] dimQs = new Quad[dims.length];
	// fill arrays
	for(int i = 0; i < dims.length; i++) {
	    CONST orig = dims[i];
	    dimTs[i] = new Temp(tf);
	    dimQs[i] = new CONST(qf, q, dimTs[i], orig.value(), orig.type());
	}
	Quad q0 = new ANEW(q.getFactory(), q, t, hc, dimTs);
	Util.ASSERT(q.prevLength() == 1);
	Quad.addEdge(q.prev(0), q.prevEdge(0).which_succ(), dimQs[0], 0);
	// connect up the CONSTs
	for(int i = 0; i < dimQs.length-1; i++)
	    Quad.addEdge(dimQs[i], 0, dimQs[i+1], 0);
	Quad.addEdge(dimQs[dimQs.length-1], 0, q0, 0);
	Quad.addEdge(q0, 0, q, 0);
    }

    /** Replaces original call with call to transformed method.
     *  @param orig is the original <code>CALL</code>.
     *  @param nhm is the <code>HMethod</code> to call.
     *  @param t is the <code>Temp</code> containing the last parameter.
     */
    private static void replaceCall(CALL orig, HMethod nhm, Temp t) {
	QuadFactory qf = orig.getFactory();
	// make new parameter array and copy over
	Temp[] nparams = new Temp[orig.paramsLength()+1];
	System.arraycopy(orig.params(), 0, nparams, 0, orig.paramsLength());
	// the last parameter is the Temp containing the hoisted allocation
	nparams[nparams.length-1] = t;
	Quad ncall = new CALL(qf, orig, nhm, nparams, orig.retval(), 
			      orig.retex(), orig.isVirtual(), 
			      orig.isTailCall(), orig.dst(), orig.src());
	Quad.replace(orig, ncall);
	Quad.transferHandlers(orig, ncall);
    }

    /** Initializes the <code>iMap</code> to a <code>Map</code>
     *  of initializers (<code>HMethod</code>s) that can benefit
     *  from this transform, to a <code>Tuple</code> containing
     *  the <code>HClass</code> of the allocation to be hoisted,
     *  and, if the allocation is of an array, the constant array
     *  dimensions.
     *
     *  @param rName is the name of the path to the properties
     *               file that lists the native methods that do
     *               not allocate Java-visible objects
     */
    private void setupiMap(String rName) {
	for(Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    // only consifer methods where the receiver
	    // object is always the most recently allocated 
	    // object entering the method
	    if (!mraf.isSafeMethod(hm)) continue;
	    Code c = (Code) parent.convert(hm);
	    // start with the beginning of the executable code
	    Quad q = ((HEADER)c.getRootElement()).method().next(0);
	    // only continue as long as the Quads are hoistable
	    for( ; (q.nextLength() == 1 && q.prevLength() == 1) ||
		     q.kind() == QuadKind.CALL; q = q.next(0)) {
		Temp src, dst;
		if (q.kind() == QuadKind.ASET && 
		    !((ASET)q).type().isPrimitive()) {
		    dst = ((ASET)q).objectref();
		    src = ((ASET)q).src();
		} else if (q.kind() == QuadKind.SET &&
			   !((SET)q).isStatic() &&
			   !((SET)q).field().getType().isPrimitive()) {
		    dst = ((SET)q).objectref();
		    src = ((SET)q).src();
		} else {
		    // most Quads can be ignored
		    continue;
		}
		// determine whether the given ASET/SET can be optimized
		Tuple t = mraf.mra(c).mra_before(q);
		MRA.MRAToken tok = (MRA.MRAToken) ((Map)t.proj(0)).get(src);
		if (tok == MRA.MRAToken.SUCC /*is successor*/ &&
		    ((Set)t.proj(1)).isEmpty() /*no exceptions*/ &&
		    ((Set)t.proj(3)).contains(dst) /*writes to receiver*/) {
		    // single allocation site
		    Util.ASSERT((Quad)t.proj(2) != null);  
		    Quad alloc = (Quad) t.proj(2);
		    if (alloc.kind() == QuadKind.NEW) {
			iMap.put(hm, new Tuple(new Object[] { alloc }));
			// done with this initializer
			break;
		    } else {
			Util.ASSERT(alloc.kind() == QuadKind.ANEW);
			boolean valid = true;
			Temp[] dims = ((ANEW)alloc).dims();
			CONST[] consts = new CONST[dims.length];
			ReachingDefs rd = new ReachingDefsImpl(c);
			for(int i = 0; i < dims.length; i++) {
			    Object[] defs = (Object[]) rd.reachingDefs
				(alloc, dims[i]).toArray();
			    if (defs.length == 1 && 
				((Quad)defs[0]).kind() == QuadKind.CONST) {
				consts[i] = (CONST) defs[0];
			    } else {
				valid = false;
				break;
			    }
			}
			if (valid) {
			    iMap.put(hm, new Tuple(new Object[] 
						   { alloc, consts }));
			    // done w/ this method
			    break;
			}
		    }
		}
	    }
	}
    }

    /** Checks whether the given <code>Code</code> can benefit
     *  from the transformation.
     */
    private boolean optimizable(Code c) {
	// only worry about safe initializers
	if (!mraf.isSafeMethod(c.getMethod())) return false;
	// check for SETs/ASETs that will benefit
	MRA mra = mraf.mra(c);
	for (Iterator it = c.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    int kind = q.kind();
	    Temp src, dst;
	    // check for non-primitive ASETs and SETs 
	    // of non-primitive, non-static fields
	    if (kind == QuadKind.ASET && !((ASET)q).type().isPrimitive()) {
		dst = ((ASET)q).objectref();
		src = ((ASET)q).src();
	    } else if (kind == QuadKind.SET && !((SET)q).isStatic() &&
		       !((SET)q).field().getType().isPrimitive()) {
		dst = ((SET)q).objectref();
		src = ((SET)q).src();
	    } else {
		continue;
	    }
	    Tuple t = mra.mra_before(q);
	    MRA.MRAToken token = (MRA.MRAToken) ((Map)t.proj(0)).get(src);
	    if (token == MRA.MRAToken.SUCC && 
		((Set)t.proj(1)).isEmpty()) {
		Quad alloc = (Quad) t.proj(2);
		if (alloc == null) return false;
		// otherwise, see what we have
		if (alloc.kind() == QuadKind.NEW) {
		    //System.out.println(q);
		    return true;
		} else if (alloc.kind() == QuadKind.ANEW) {
		    Temp[] dims = ((ANEW)alloc).dims();
		    // only want to handle if all dimensions are constants
		    for (int i = 0; i < dims.length; i++) {
			Iterator ddefs = (new ReachingDefsImpl(c)).
			    reachingDefs(alloc, dims[i]).iterator();
			Util.ASSERT(ddefs.hasNext());
			Quad dd = (Quad) ddefs.next();
			if (ddefs.hasNext()) return false;
			if (dd.kind() != QuadKind.CONST)
			    return false;
		    }
		    //System.out.println(q);
		    return true;
		}
	    }
	}
	return false;
    }
    /** Check the validity of a given <code>MethodSplitter.Token</code>.
     */
    protected boolean isValidToken(Token which) {
	return which==HOISTED || super.isValidToken(which);
    }
}
