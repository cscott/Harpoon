// RCTransformer.java, created Fri Feb  8 20:02:15 2002 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeAndMaps;
import harpoon.ClassFile.HMethod;
import harpoon.IR.LowQuad.PCALL;
import harpoon.IR.Properties.CFGEdge;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.SWITCH;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Util.Tuple;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <code>RCTransformer</code> transforms recursive constructors
 * that build their data structures in a top-down fashion into 
 * methods that build their data structures in a bottom-up fashion.
 * Also transforms the callers of these methods.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: RCTransformer.java,v 1.2 2002-03-07 00:44:32 cananian Exp $
 */
public class RCTransformer extends 
    harpoon.Analysis.Transformation.MethodSplitter {

    private final Set recursiveConstructors;

    /** Creates a <code>RCTransformer</code>. */
    public RCTransformer(HCodeFactory parent, ClassHierarchy ch) {
        super(parent, ch, true/* doesn't matter */);
	this.recursiveConstructors = new HashSet();
	scanConstructors(parent, ch);
	System.out.println(recursiveConstructors.size() + " constructors");
    }
    
    /** The <code>CREATOR</code> token represents the transformed
     *  version of the method. */
    public static final Token CREATOR = new Token("creator") {
	public Object readResolve() { return CREATOR; }
	public String toString() { return "TOKEN<CREATOR>"; }
    };

    /** Mutated constructors have signatures differing from the
     *  original.
     */
    protected String mutateDescriptor(HMethod hm, Token which) {
	if (which != CREATOR) {
	    return hm.getDescriptor();
	} else {
	    String desc = hm.getDescriptor();
	    return desc.substring(0,desc.length()-1) + 
		hm.getDeclaringClass().getDescriptor();
	}
    }

    /** Modifies two classes of methods:
     *  - mutates recursive constructors into "creators", methods
     *    that build objects in a bottom-up fashion
     *  - modifies callers of these constructors to call the new
     *    creator methods
     */
    protected HCode mutateHCode(HCodeAndMaps input, Token which) {
	Code c = (Code) input.hcode();
	if (recursiveConstructors.size() == 0) {
	    // no need to make changes if no
	    // constructors are modifiable
	    return c;
	} else if (which == CREATOR) {
	    makeCreator(c);
	}
	// modify calls for all methods including
	// newly-minted creator methods
	return modifyCalls(c);
    }

    /** Convert recursive constructor to a recursive
     *  creator method.
     */
    private Code makeCreator(Code c) {
	// find call to superclass constructor
	System.out.println("Making creator");
	c.print(new java.io.PrintWriter(System.out), null);
	Quad q = (Quad) c.getRootElement();
	CFGEdge[] succs = q.succ();
	// first Quad should be the HEADER
	Util.ASSERT(q.kind() == QuadKind.HEADER && succs.length == 2);
	q = (Quad) succs[1].to();
	succs = q.succ();
	// second Quad should be the METHOD
	Util.ASSERT(q.kind() == QuadKind.METHOD && succs.length == 1);
	METHOD omethod = (METHOD)q;
	// get a handle on the METHOD parameters
	Temp[] oparams = omethod.params();
	Temp retval = oparams[0];
	// replace METHOD
	METHOD nmethod = replaceMETHOD(omethod, oparams);
	q = (Quad) succs[0].to();
	succs = q.succ();
	// third Quad should be the CALL to the superclass constructor
	Util.ASSERT(q.kind() == QuadKind.CALL);
	CALL call = (CALL)q;
	Util.ASSERT(call.retval() == null && call.params(0).equals(retval));
	// create a NEW
	NEW nnew = new NEW(call.getFactory(), call, retval, 
			   c.getMethod().getDeclaringClass());
	// put NEW before CALL (and after METHOD)
	Util.ASSERT(call.prevLength() == 1);
	Quad.addEdge(call.prev(0), call.prevEdge(0).which_succ(), nnew, 0);
	Quad.addEdge(nnew, 0, call, 0);
	// fix-up RETURN(s)
	call.accept(new RETURNFixUp(retval));
	c.print(new java.io.PrintWriter(System.out), null);
	// start with the Quad on the 0-edge (normal return)
	// of the call to the superclass constructor, and
	// find the straight-line code that we want to move
	findChain fc = new findChain(retval, nnew, call);
	Quad end = fc.end;
	fc = null;
	System.out.println(end);
	findTemps ft = new findTemps(nmethod, nnew, call, end);
	checkDependencies cd = new checkDependencies(nmethod, nnew, call, end);
	// only perform transformation if possible
	if (ft.needs.size() == 0 && ft.safe && cd.safe) {
	    System.out.println("Safe...");
	    updateDependencies ud = new updateDependencies(nmethod, nnew, call,
							   end, c.getMethod());
	    c.print(new java.io.PrintWriter(System.out), null);
	    Map insertPts = ud.insertPts;
	    // null out for GC
	    ud = null;
	    for(Iterator it = insertPts.keySet().iterator(); it.hasNext(); )
		{
		    Quad key = (Quad) it.next();
		    Quad[] value = (Quad[]) insertPts.get(key);
		    System.out.println(key);
		    for(int i = 0; i < value.length; i++) {
			System.out.println(":: "+value[i]);
		    }
		}
	    System.out.println("Done...");
	    makeRecurse mr = new makeRecurse(insertPts, nnew);
	    c.print(new java.io.PrintWriter(System.out), null);
	    nmethod.next(0).accept(mr);
	    c.print(new java.io.PrintWriter(System.out), null);
	}
	return c;
    }



    /** Replaces the <code>METHOD</code> <code>Quad</code> of the
     *  creator method with a corrected one.
     */
    private METHOD replaceMETHOD(METHOD omethod, Temp[] oparams) {
	Temp[] nparams = new Temp[oparams.length-1];
	// create a new METHOD with the right parameters
	System.arraycopy(oparams, 1, nparams, 0, nparams.length);
	METHOD nmethod = new METHOD(omethod.getFactory(), omethod, 
				    nparams, omethod.arity());
	Quad.replace(omethod, nmethod);
	return nmethod;
    }


    /** Replaces a call to a recursive constructor with a
     *  call to the new creator method.
     */
    private Code modifyCalls(Code c) {
	/*
	if (recursiveConstructors.contains(c.getMethod()))
	    return c;
	*/
	for(Iterator it = c.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    // modify callers of recursive constructors
	    // so that they call "creators" instead
	    if (q.kind() == QuadKind.CALL) {
		CALL call = (CALL) q;
		if (recursiveConstructors.contains(call.method())) {
		    // find allocation site of object being initialized
		    CFGEdge[] preds = q.pred();
		    if (preds.length == 1) {
			Quad pred = (Quad) preds[0].from();
			if (pred.kind() == QuadKind.NEW && 
			    ((NEW)pred).dst().equals(call.params(0))) {
			    HMethod nhm = select(call.method(), CREATOR);
			    replaceCall(call, (NEW)pred, nhm);
			    //select(call.method(), CREATOR));
			}
		    }
		}
	    }
	}
	return c;
    }
    
    /** Helper method that actually updates the graph by 
     *  replacing a call to a recursive constructor with
     *  a call to the new creator method.
     */
    private static void replaceCall(CALL ocall, NEW alloc, HMethod nhm) {
	// hack to turn constructor into static method
	nhm.getMutator().addModifiers(Modifier.STATIC);
	Temp[] nparams = new Temp[ocall.paramsLength()-1];
	System.arraycopy(ocall.params(), 1, nparams, 0, nparams.length);
	Util.ASSERT(ocall.retval() == null);
	CALL ncall = new CALL(ocall.getFactory(), ocall, nhm, nparams,
			      ocall.params(0), ocall.retex(), 
			      ocall.isVirtual(), ocall.isTailCall(),
			      ocall.dst(), ocall.src());
	System.out.println("Replacing:");
	System.out.println(alloc);
	System.out.println(ocall);
	System.out.println("with");
	System.out.println(ncall);
	// update graph
	alloc.remove();
	Quad.replace(ocall,ncall);
	Quad.transferHandlers(ocall,ncall);
    }

    private void scanConstructors(HCodeFactory parent, ClassHierarchy ch) {
	// first, find constructors that call themselves
	for(Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    // skip methods that are not constructors
	    if (!hm.getName().equals("<init>")) continue;
	    Code c = (Code) parent.convert(hm);
	    // look for constructors that call themselves
	    for(Iterator quads = c.getElementsI(); quads.hasNext(); ) {
		Quad q = (Quad) quads.next();
		if (q.kind() == QuadKind.CALL &&
		    ((CALL)q).method().equals(hm)) {
		    recursiveConstructors.add(hm);
		    break;
		}
	    }
	}
    }

    /** Check the validity of a given <code>MethodSplitter.Token</code>.
     */
    protected boolean isValidToken(Token which) {
	return which == CREATOR || super.isValidToken(which);
    }

    /** private class for traversing the graph to fix-up the
     *  RETURN nodes.
     */
    private static class RETURNFixUp extends QuadVisitor
    {
	Temp retval;
	int nParam;
	Map done;

	RETURNFixUp(Temp retval) {
	    this.retval = retval;
	    this.nParam = 0;
	    this.done = new HashMap();
	}

	public void visit(CALL q) {
	    for(int i = 0; i < q.numSigmas(); i++) {
		if (q.src(i).equals(retval)) {
		    for(int j = i+1; j < q.numSigmas(); j++) {
			Util.ASSERT(!q.src(j).equals(retval));
		    }
		    // handle normal return
		    if (i < q.numSigmas() && 
			(retval == null || !retval.equals(q.retval())))
			retval = q.dst(i, 0);
		    else
			retval = null;
		    nParam = q.nextEdge(0).which_pred();
		    q.next(0).accept(this);
		    // handle exceptional return
		    if (i < q.numSigmas() &&
			(retval == null || !retval.equals(q.retex())))
			retval = q.dst(i, 1);
		    else
			retval = null;
		    nParam = q.nextEdge(1).which_pred();
		    q.next(1).accept(this);
		    break;
		}
	    }
	}

	public void visit(Quad q) {
	    if (q.nextLength() != 0) {
		Util.ASSERT(q.nextLength() == 1, q);
		if (q.defC().contains(retval))
		    retval = null;
		nParam = q.nextEdge(0).which_pred();
		q.next(0).accept(this);
	    }
	}

	public void visit(PHI q) {
	    for(int i = 0; i < q.numPhis(); i++) {
		if (q.src(i, nParam).equals(retval)) {
		    for(int j = i+1; j < q.numPhis(); j++) {
			Util.ASSERT(!q.src(j, nParam).equals(retval));
		    }
		    if (done.containsKey(q)) {
			// already handled once, check correctness
			Util.ASSERT(q.dst(i).equals(done.get(q)));
		    } else {
			retval = q.dst(i);
			done.put(q, retval);
			Util.ASSERT(q.nextLength() == 1);
			nParam = q.nextEdge(0).which_pred();
			q.next(0).accept(this);
		    }
		    return;
		}
	    }
	    // in some cases, we may have to add a phi function to the PHI
	    if (done.containsKey(q) && !retval.equals(done.get(q))) {
		Util.ASSERT(retval != null);
		Tuple tup = (Tuple) done.get(q);
		Temp[] retvals = (Temp[]) tup.proj(0);
		int count = ((Integer) tup.proj(1)).intValue();
		if (count == q.arity()-1) {
		    // fix-up PHI
		    Temp dst[] = new Temp[q.numPhis()+1];
		    Temp src[][] = new Temp[q.numPhis()+1][];
		    for(int i = 0; i < q.numPhis(); i++)
			dst[i] = q.dst(i);
		    // new destination Temp
		    dst[dst.length-1] = new Temp(q.getFactory().tempFactory());
		    for(int i = 0; i < q.numPhis(); i++)
			src[i] = q.src(i);
		    retvals[nParam] = retval;
		    src[src.length-1] = retvals;
		    PHI phi = new PHI(q.getFactory(), q, dst, src, q.arity());
		    Quad.replace(q, phi);
		    retval = dst[dst.length-1];
		    Util.ASSERT(phi.nextLength() == 1);
		    nParam = phi.nextEdge(0).which_pred();
		    phi.next(0).accept(this);
		} else {
		    retvals[nParam] = retval;
		    done.put(q, new Tuple(new Object[] 
					  { retvals, new Integer(count+1) }));
		}
	    } else {
		Temp[] retvals = new Temp[q.arity()];
		Util.ASSERT(retval != null);
		retvals[nParam] = retval;
		done.put(q, new Tuple(new Object[] {retvals, new Integer(1)}));
	    }
	}

	public void visit(RETURN q) {
	    Util.ASSERT(retval != null);
	    RETURN nreturn = new RETURN(q.getFactory(), q, retval);
	    Quad.replace(q, nreturn);
	    // RETURNs only have one successor, the FOOTER node,
	    // which we don't need to visit
	}

	public void visit(SIGMA q) {
	    for(int i = 0; i < q.numSigmas(); i++) {
		if (q.src(i).equals(retval)) {
		    for(int j = i+1; j < q.numSigmas(); j++) {
			Util.ASSERT(!q.src(j).equals(retval));
		    }
		    for(int k = 0; k < q.arity(); k++) {
			retval = q.dst(i, k);
			nParam = q.nextEdge(k).which_pred();
			q.next(k).accept(this);
		    }
		    return;
		}
	    }
	    Util.ASSERT(false);
	}
    }

    /** private class for traversing the graph to find the
     *  straight-line code that needs to be moved.
     */
    private static class findChain extends QuadVisitor
    {
	Temp thisObj;
	Quad end; // last Quad that needs *this* before control flow split

	findChain(Temp thisObj, NEW nnew, CALL call) {
	    this.thisObj = thisObj;
	    for(int i = 0; i < call.numSigmas(); i++) {
		if (call.src(i).equals(thisObj)) {
		    this.thisObj = call.dst(i, 0);
		    break;
		}
	    }
	    // start with the Quad on the 0-edge (normal return)
	    // of the call to the superclass constructor
	    call.next(0).accept(this);
	}

	// done when we hit a control flow split
	public void visit(SIGMA q) {
	    return;
	}

	public void visit(PHI q) {
	    Util.ASSERT(false, "Cannot be transformed.");
	}

	public void visit(Quad q) {
	    if (q.useC().contains(thisObj))
		end = q;
	    Util.ASSERT(q.nextLength() == 1, "Unexpected Quad.");
	    q.next(0).accept(this);
	}
    }

    /** private class for traversing the graph to find
     *  the <code>Temp</code>s used by a chain of
     *  straight-line code.
     */
    private static class findTemps extends QuadVisitor
    {
	Quad end; // last Quad
	CALL call;
	Set needs;
	Set gen;
	Set protect;
	Map tMap;
	boolean safe;

	findTemps(METHOD method, NEW nnew, CALL call, Quad end) {
	    this.end = end;
	    this.call = call;
	    this.gen = new HashSet();
	    this.needs = new HashSet();
	    this.protect = new HashSet();
	    this.tMap = new HashMap();
	    this.safe = true;
	    // handle NEW
	    gen.add(nnew.dst());
	    // handle CALL
	    for(int i = 0; i < call.paramsLength(); i++) {
		if (!gen.contains(call.params(0)))
		    needs.add(call.params(0));
	    }
	    gen.add(call.retval());
	    // build protected set of Temps
	    for(int i = 0; i < method.paramsLength(); i++) {
		Temp t = method.params(i);
		protect.add(t);
		for(int j = 0; j < call.numSigmas(); j++) {
		    if (call.src(j).equals(t)) {
			protect.remove(t);
			protect.add(call.dst(j,0));
			break;
		    }
		}
	    }
	    // build mapping of Temps
	    for(int i = 0; i < call.numSigmas(); i++) {
		tMap.put(call.dst(i,0), call.src(i));
	    }
	    // start with the Quad on the 0-edge (normal return)
	    // of the call to the superclass constructor
	    call.next(0).accept(this);
	}

	// done when we hit a control flow split
	public void visit(SIGMA q) {
	    Util.ASSERT(false, "Invalid sequence.");
	}

	public void visit(PHI q) {
	    Util.ASSERT(false, "Invalid sequence.");
	}

	public void visit(Quad q) {
 	    for(Iterator it = q.useC().iterator(); it.hasNext(); ) {
		Temp t = (Temp) it.next();
		Temp m = (Temp) tMap.get(t);
		t = (m == null) ? t : m;
		if (!gen.contains(t))
		    needs.add(t);
	    }
	    for(Iterator it = q.defC().iterator(); it.hasNext(); ) {
		Temp t = (Temp) it.next();
		if (protect.contains(t)) {
		    safe = false;
		    return;
		}
		Temp m = (Temp) tMap.get(t);
		t = (m == null) ? t : m;
		gen.add(t);
	    }
	    Util.ASSERT(q.nextLength() == 1, "Unexpected Quad.");
	    if (!q.equals(end))
		q.next(0).accept(this);
	}
    }

    /** private class for traversing the graph as setup for 
     *  conversion to recursive form.
     */
    private static class checkDependencies extends QuadVisitor
    {
	Temp thisObj;
	Set phis;
	Set gen;
	boolean safe;
	int nParam;

	checkDependencies(METHOD method, NEW nnew, CALL call, Quad start) {
	    this.gen = new HashSet();
	    this.safe = true;
	    this.phis = new HashSet();
	    this.thisObj = nnew.dst();
	    // map thisObj to appropriate post-call Temp
	    for(int i = 0; i < call.numSigmas(); i++) {
		if (call.src(i).equals(thisObj))
		    thisObj = call.dst(i, 0);
	    }
	    // assumes that the only available Temps are
	    // the method parameters
	    for(int i = 0; i < method.paramsLength(); i++) {
		Temp t = method.params(i);
		gen.add(t);
		for(int j = 0; j < call.numSigmas(); j++) {
		    if (call.src(j).equals(t)) {
			gen.remove(t);
			gen.add(call.dst(j, 0));
		    }
		}
	    }
	    // start with the next Quad
	    Util.ASSERT(start.nextLength() == 1);
	    nParam = start.nextEdge(0).which_pred();
	    start.next(0).accept(this);
	}

	public void visit(CALL q) {
	    for(int i = 0; i < q.paramsLength(); i++) {
		if (!gen.contains(q.params(i))) {
		    safe = false;
		    System.out.println(q + " needs " + q.params(i));
		    return;
		}
	    }
	    Temp savedThis = thisObj;
	    Set savedGen = gen;
	    for(int i = 0; i < q.arity(); i++) {
		// fixup thisObj
		for(int j = 0; j < q.numSigmas(); j++) {
		    if (q.src(j).equals(savedThis)) {
			thisObj = q.dst(j, i);
			break;
		    }
		}
		// fixup gen
		gen = new HashSet();
		if (i == 0) {
		    for(int j = 0; j < q.numSigmas(); j++) {
			if (q.src(j).equals(q.retval())) {
			    gen.add(q.dst(j, 0));
			    break;
			}
		    }
		} else {
		    Util.ASSERT(i == 1);
		    for(int j = 0; j < q.numSigmas(); j++) {
			if (q.src(j).equals(q.retex())) {
			    gen.add(q.dst(j, 1));
			    break;
			}
		    }
		}
		for(Iterator it = savedGen.iterator(); it.hasNext(); ) {
		    Temp t = (Temp) it.next();
		    gen.add(t);
		    for(int j = 0; j < q.numSigmas(); j++) {
			if (q.src(j).equals(t)) {
			    gen.remove(t);
			    gen.add(q.dst(j, i));
			}
		    }
		}
		// go down this path
		nParam = q.nextEdge(i).which_pred();
		q.next(i).accept(this);
	    }
	}

	public void visit(CJMP q) {
	    if (!gen.contains(q.test())) {
		safe = false;
		System.out.println(q + " needs " + q.test());
		return;
	    }
	    visit((SIGMA)q);
	}

	public void visit(PCALL q) {
	    Util.ASSERT(false, "Unimplemented.");
	}

	public void visit(PHI q) {
	    if (!phis.contains(q)) {
		phis.add(q);
		// fixup thisObj
		for(int i = 0; i < q.numPhis(); i++) {
		    if (q.src(i, nParam).equals(thisObj))
			thisObj = q.dst(i);
		}
		// fixup gen
		Set savedGen = gen;
		gen = new HashSet();
		for(Iterator it = savedGen.iterator(); it.hasNext(); ) {
		    Temp t = (Temp) it.next();
		    gen.add(t);
		    for(int i = 0; i < q.numPhis(); i++) {
			if (q.src(i, nParam).equals(t)) {
			    gen.remove(t);
			    gen.add(q.dst(i));
			}
		    }
		}
		Util.ASSERT(q.nextLength() == 1);
		nParam = q.nextEdge(0).which_pred();
		q.next(0).accept(this);
	    }
	}

	public void visit(Quad q) {
	    // check dependencies
	    for(Iterator it = q.useC().iterator(); it.hasNext(); ) {
		Temp t = (Temp) it.next();
		if (!gen.contains(t) && !t.equals(thisObj)) {
		    System.out.println(thisObj);
		    safe = false;
		    System.out.println(q + " needs " + t);
		    return;
		}
	    }
	    // add defs
	    for(Iterator it = q.defC().iterator(); it.hasNext(); ) {
		gen.add(it.next());
	    }
	    if (q.nextLength() == 0) return;
	    Util.ASSERT(q.nextLength() == 1);
	    nParam = q.nextEdge(0).which_pred();
	    q.next(0).accept(this);
	}

	public void visit(SIGMA q) {
	    Temp savedThis = thisObj;
	    Set savedGen = gen;
	    for(int i = 0; i < q.arity(); i++) {
		// fixup thisObj
		for(int j = 0; j < q.numSigmas(); j++) {
		    if (q.src(j).equals(savedThis)) {
			thisObj = q.dst(j, i);
			break;
		    }
		}
		// fixup gen
		gen = new HashSet();
		for(Iterator it = savedGen.iterator(); it.hasNext(); ) {
		    Temp t = (Temp) it.next();
		    gen.add(t);
		    for(int j = 0; j < q.numSigmas(); j++) {
			if (q.src(j).equals(t)) {
			    gen.remove(t);
			    gen.add(q.dst(j, i));
			}
		    }
		}
		// go down this path
		nParam = q.nextEdge(i).which_pred();
		q.next(i).accept(this);
	    }
	}

	public void visit(SWITCH q) {
	    Util.ASSERT(false, "Unimplemented.");
	}

	public void visit(TYPESWITCH q) {
	    Util.ASSERT(false, "Unimplemented.");
	}
    }

    /** private class for traversing the graph for 
     *  conversion to recursive form.
     */
    private static class updateDependencies extends QuadVisitor
    {
	PHI lastPhi;
	Map phiMap;
	int nParam;
	Quad insertPt;
	Map insertPts;
	Map tMap;
	HMethod hm;

	updateDependencies(METHOD method, NEW nnew, CALL call, Quad start,
			   HMethod hm) {
	    this.insertPts = new HashMap();
	    this.phiMap = new HashMap();
	    this.tMap = new HashMap();
	    this.hm = hm;
	    // create map needed for updating Quads
	    // using post-call Temps to correct pre-call Temps
	    for(int i = 0; i < method.paramsLength(); i++) {
		Temp t = method.params(i);
		for(int j = 0; j < call.numSigmas(); j++) {
		    tMap.put(call.dst(j, 0), call.src(j));
		}
	    }
	    // start with the next Quad
	    Util.ASSERT(start.nextLength() == 1);
	    nParam = start.nextEdge(0).which_pred();
	    //start.next(0).accept(this);
	    call.next(0).accept(this);
	}

	public void visit(CALL q) {
	    System.out.println(insertPt + " :: " + q);
	    if (q.method().equals(hm))
		insertPt = q;
	    Temp[] oparams = q.params();
	    Temp[] nparams = null;
	    // remap params if necessary
	    for(int i = 0; i < oparams.length; i++) {
		if (tMap.containsKey(oparams[i])) {
		    if (nparams == null) {
			nparams = new Temp[oparams.length];
			System.arraycopy(oparams, 0, nparams, 0, 
					 nparams.length);
		    }
		    nparams[i] = (Temp) tMap.get(oparams[i]);
		    Util.ASSERT(nparams[i] != null);
		}
	    }
	    Temp[] osrc = q.src();
	    Temp[] nsrc = null;
	    // remap sigma if needed
	    for(int i = 0; i < q.numSigmas(); i++) {
		if (tMap.containsKey(osrc[i])) {
		    if (nsrc == null) {
			nsrc = new Temp[osrc.length];
			System.arraycopy(osrc, 0, nsrc, 0, nsrc.length);
		    }
		    nsrc[i] = (Temp) tMap.get(osrc[i]);
		    Util.ASSERT(nsrc[i] != null);
		}
	    }
	    // replace call if necessary
	    if (nparams != null || nsrc != null) {
		nparams = (nparams == null) ? oparams : nparams;
		nsrc = (nsrc == null) ? osrc : nsrc;
		CALL ncall = new CALL(q.getFactory(), q, q.method(), 
					 nparams, q.retval(), q.retex(),
					 q.isVirtual(), q.isTailCall(),
					 q.dst(), nsrc);
		Quad.replace(q, ncall);
		q = ncall;   
	    }
	    visit((SIGMA)q);
	}

	public void visit(CJMP q) {
	    System.out.println(insertPt + " :: " + q);
	    Temp test = null;
	    // remap test if needed
	    if (tMap.containsKey(q.test())) {
		test = (Temp) tMap.get(q.test());
		Util.ASSERT(test != null);
	    }
	    Temp[] osrc = q.src();
	    Temp[] nsrc = null;
	    // remap sigma if needed
	    for(int i = 0; i < q.numSigmas(); i++) {
		if (tMap.containsKey(osrc[i])) {
		    if (nsrc == null) {
			nsrc = new Temp[osrc.length];
			System.arraycopy(osrc, 0, nsrc, 0, nsrc.length);
		    }
		    nsrc[i] = (Temp) tMap.get(osrc[i]);
		    Util.ASSERT(nsrc[i] != null);
		}
	    }
	    if (test != null || nsrc != null) {
		test = (test == null) ? q.test() : test;
		nsrc = (nsrc == null) ? q.src() : nsrc;
		CJMP ncjmp = new CJMP(q.getFactory(), q, test, q.dst(), nsrc);
		Quad.replace(q, ncjmp);
		q = ncjmp;
	    }
	    visit((SIGMA)q);
	}

	public void visit(FOOTER q) {
	    System.out.println(insertPt + " :: " + q);
	    if (lastPhi != null && insertPt != null)
		phiMap.put(lastPhi, insertPt);
	}

	public void visit(OPER q) {
	    System.out.println(insertPt + " :: " + q);
	    // update operands if necessary
	    Temp[] nt = null;
	    Temp[] ot = q.operands();
	    for(int i = 0; i < ot.length; i++) {
		if (tMap.containsKey(ot[i])) {
		    if (nt == null) {
			nt = new Temp[ot.length];
			System.arraycopy(ot, 0, nt, 0, nt.length);
		    }
		    nt[i] = (Temp) tMap.get(ot[i]);
		    Util.ASSERT(nt[i] != null);
		}
	    }
	    // replace OPER if necessary
	    if (nt != null) {
		OPER noper = new OPER(q.getFactory(), q, q.opcode(), q.dst(), 
				      nt);
		Quad.replace(q, noper);
		q = noper;
	    }
	    // update map if necessary
	    tMap.remove(q.dst());
	    Util.ASSERT(q.nextLength() == 1);
	    nParam = q.nextEdge(0).which_pred();
	    q.next(0).accept(this);
	}

	public void visit(PCALL q) {
	    Util.ASSERT(false, "Unimplemented.");
	}

	public void visit(PHI q) {
	    System.out.println(insertPt + " :: " + q);
	    // replace PHI if needed
	    Temp[][] nsrc = null;
	    for(int i = 0; i < q.numPhis(); i++) {
		if (tMap.containsKey(q.src(i, nParam))) {
		    if (nsrc == null) {
			nsrc = new Temp[q.numPhis()][];
			for(int j = 0; j < nsrc.length; j++) {
			    nsrc[j] = new Temp[q.arity()];
			    System.arraycopy(q.src(i), 0, nsrc[j], 0, 
					     nsrc[j].length);
			}
		    nsrc[i][nParam] = (Temp) tMap.get(q.src(i, nParam));
		    Util.ASSERT(nsrc[i][nParam] != null);
		    }
		}
	    }
	    if (nsrc != null) {
		Temp[] dst = new Temp[q.numPhis()];
		for(int i = 0; i < dst.length; i++)
		    dst[i] = q.dst(i);
		PHI phi = new PHI(q.getFactory(), q, dst, nsrc, q.arity());
		Quad.replace(q, phi);
		q = phi;
	    }
	    if (!phiMap.containsKey(q)) {
		phiMap.put(q, null); // placeholder
		lastPhi = q;
		// fixup tMap
		Map savedTMap = tMap;
		tMap = new HashMap();
		for(Iterator it = savedTMap.keySet().iterator(); 
		    it.hasNext(); ) {
		    Temp from = (Temp) it.next();
		    Temp to = (Temp) savedTMap.get(from);
		    tMap.put(from, to);
		    for(int i = 0; i < q.numPhis(); i++) {
			if (q.src(i, nParam).equals(to))
			    tMap.put(from, q.dst(i));
			if (q.src(i, nParam).equals(from))
			    tMap.put(from, null);
		    }
		}
		Util.ASSERT(q.nextLength() == 1);
		nParam = q.nextEdge(0).which_pred();
		q.next(0).accept(this);
	    } else {
		Quad lastIP = (Quad) phiMap.get(q);
		if (lastIP == null && insertPt != null) {
		    phiMap.put(q, insertPt);
		}
	    }
	}

	public void visit(Quad q) {
	    System.out.println(insertPt + " :: " + q);
	    // we don't handle uses here
	    Util.ASSERT(q.useC().isEmpty());
	    for(Iterator it = tMap.keySet().iterator(); it.hasNext(); ) {
		Temp from = (Temp) it.next();
		Temp to = (Temp) tMap.get(from);
		if (q.defC().contains(from))
		    tMap.remove(from);
		if (q.defC().contains(to))
		    tMap.put(from, null);
	    }
	    Util.ASSERT(q.nextLength() == 1);
	    nParam = q.nextEdge(0).which_pred();
	    q.next(0).accept(this);
	}

	public void visit(RETURN q) {
	    System.out.println(insertPt + " :: " + q);
	    if (tMap.containsKey(q.retval())) {
		Temp t = (Temp) tMap.get(q.retval());
		Util.ASSERT(t != null);
		RETURN nreturn = new RETURN(q.getFactory(), q, t);
		Quad.replace(q, nreturn);
		q = nreturn;
	    }
	    Util.ASSERT(q.nextLength() == 1);
	    nParam = q.nextEdge(0).which_pred();
	    q.next(0).accept(this);
	}

	public void visit(SET q) {
	    System.out.println(insertPt + " :: " + q);
	    // replace objectref if necessary
	    Temp objectref = null;
	    if (tMap.containsKey(q.objectref())) {
		objectref = (Temp) tMap.get(q.objectref());
		Util.ASSERT(objectref != null);;
	    }
	    // replace src if necessary
	    Temp src = null;
	    if (tMap.containsKey(q.src())) {
		src = (Temp) tMap.get(q.src());
		Util.ASSERT(src != null);
	    }
	    // replace SET if necessary
	    if (objectref != null || src != null) {
		objectref = (objectref == null) ? q.objectref() : objectref;
		src = (src == null) ? q.src() : src;
		SET set = new SET(q.getFactory(), q, q.field(), objectref, 
				  src);
		Quad.replace(q, set);
		q = set;
	    }
	    Util.ASSERT(q.nextLength() == 1);
	    nParam = q.nextEdge(0).which_pred();
	    q.next(0).accept(this);
	}

	public void visit(SIGMA q) {
	    System.out.println(insertPt + " :: " + q);
	    Quad savedIP = insertPt;
	    Util.ASSERT(savedIP == null || savedIP.equals(q));
	    Quad[] savedIPs = new Quad[q.arity()];
	    Map savedTMap = tMap;
	    PHI savedPhi = lastPhi; 
	    for(int i = 0; i < q.arity(); i++) {
		// fixup tMap
		tMap = new HashMap();
		for(Iterator it = savedTMap.keySet().iterator(); 
		    it.hasNext(); ) {
		    Temp from = (Temp) it.next();
		    Temp to = (Temp) savedTMap.get(from);
		    tMap.put(from, to);
		    if (q.kind() == QuadKind.CALL) {
			if (i == 0 && to.equals(((CALL)q).retval())) {
			    tMap.put(from, null);
			    continue;
			}
			if (i == 1 && to.equals(((CALL)q).retex())) {
			    tMap.put(from, null);
			    continue;
			}
		    }
		    for(int j = 0; j < q.numSigmas(); j++) {
			// if Temp needs remapping, do so
			if (q.src(j).equals(to))
			    tMap.put(from, q.dst(j, i));
			// if Temp is being overwritten, flag
			if (q.src(j).equals(from)) 
			    tMap.put(from, null);
		    }
		}
		insertPt = null;
		// fixup lastPhi
		lastPhi = savedPhi;
		// go down this path
		nParam = q.nextEdge(i).which_pred();
		q.next(i).accept(this);
		// check what we got from this path
		savedIPs[i] = insertPt;
	    }
	    boolean all_null = true;
	    for(int i = 0; i < q.arity(); i++) {
		if (savedIPs[i] != null) {
		    all_null = false;
		    break;
		}
	    }
	    if (!all_null || savedIP != null) {
		insertPts.put(q, savedIPs);
		insertPt = q;
	    }
	}

	public void visit(SWITCH q) {
	    Util.ASSERT(false, "Unimplemented.");
	}

	public void visit(THROW q) {
	    System.out.println(insertPt + " :: " + q);
	    if (tMap.containsKey(q.throwable())) {
		Temp t = (Temp) tMap.get(q.throwable());
		Util.ASSERT(t != null);
		THROW nthrow = new THROW(q.getFactory(), q, t);
		Quad.replace(q, nthrow);
		q = nthrow;
	    }
	    Util.ASSERT(q.nextLength() == 1);
	    nParam = q.nextEdge(0).which_pred();
	    q.next(0).accept(this);
	}

	public void visit(TYPESWITCH q) {
	    Util.ASSERT(false, "Unimplemented.");
	}
    }

    /** private class for traversing the graph to convert 
     *  to recursive form.
     */
    private static class makeRecurse extends QuadVisitor
    {
	Map quad2quadA;
	NEW begin;
	Temp thisObj;
	Set news = new HashSet();

	makeRecurse(Map quad2quadA, NEW begin) {
	    this.quad2quadA = quad2quadA;
	    // prepare exception edge
	    CALL ocall = (CALL) begin.next(0);
	    // map receiver
	    Map rcvrMap = (new mapTemp(begin.dst(), ocall.next(0),
				       quad2quadA, 
				       Collections.EMPTY_SET)).quad2temp;
	    PHI phi = null;
	    int ex_pred = 0;
	    if (ocall.next(1).kind() == QuadKind.PHI) {
		phi = (PHI) ocall.next(1);
		ex_pred = phi.arity();
	    }
	    // move things
	    for(Iterator it = quad2quadA.keySet().iterator(); it.hasNext(); ) {
		SIGMA sigma = (SIGMA) it.next();
		Quad[] value = (Quad[]) quad2quadA.get(sigma);
		for(int i = 0; i < sigma.arity(); i++) {
		    if (sigma.kind() == QuadKind.CALL && i == 1)
			// ignore exception edges
			continue;
		    if (value[i] != null)
			// look elsewhere
			continue;
		    Temp rcvr = (Temp) rcvrMap.get(sigma);
		    Util.ASSERT(rcvr != null, sigma);
		    TempFactory tf = begin.getFactory().tempFactory();
		    Temp rcvr0 = new Temp(tf);
		    NEW nnew = new NEW(begin.getFactory(), begin, rcvr0,
				       begin.hclass());
		    news.add(nnew);
		    Temp retval = (ocall.retval() == null) ? null : 
			new Temp(tf);
		    Temp retex = (ocall.retex() == null) ? null : 
			new Temp(tf);
		    // find out if any sigmas need to be propagated
		    Temp[] dstSaved = new Temp[sigma.numSigmas()];
		    Temp[] dst0 = new Temp[sigma.numSigmas()];
		    int numSigmas = 0;
		    for(int j = 0; j < sigma.numSigmas(); j++) {
			if (!sigma.dst(j, 0).equals(rcvr)) {
			    dstSaved[j] = sigma.dst(j, 0);
			    dst0[j] = new Temp(tf);
			    numSigmas++;
			} else {
			    dst0[j] = sigma.dst(j, 0);
			}
		    }
		    // fixup sigma
		    sigma.assign(dst0, 0);
		    // shrink src array to size
		    Temp[] src = new Temp[numSigmas+1];
		    src[0] = rcvr0;
		    for(int j = 0, k = 1; j < sigma.numSigmas(); j++) {
			if (dstSaved[j] != null) {
			    src[k] = sigma.dst(j, 0);
			    k++;
			}
		    }
		    Temp[][] dst = new Temp[numSigmas+1][];
		    dst[0] = new Temp[] { rcvr, new Temp(tf) };
		    for(int j = 0, k = 1; j < sigma.numSigmas(); j++) {
			if (dstSaved[j] != null) {
			    dst[k] = new Temp[] { dstSaved[j], new Temp(tf) };
			    k++;
			}
		    }
		    // make CALL
		    CALL ncall = new CALL(ocall.getFactory(), ocall, 
					  ocall.method(), new Temp[] { rcvr0 },
					  retval, retex, ocall.isVirtual(),
					  ocall.isTailCall(), dst, src);
		    System.out.println(ncall);
		    Quad.addEdge(ncall, 0, sigma.next(i), 
				 sigma.nextEdge(i).which_pred());
		    Quad.addEdge(nnew, 0, ncall, 0);
		    Quad.addEdge(sigma, i, nnew, 0);
		    if (phi == null) {
			Util.ASSERT(retex != null);
			Temp[][] srcs = new Temp[1][];
			srcs[0] = new Temp[] { retex };
			phi = new PHI(ocall.getFactory(), ocall, 
				      new Temp[] { ocall.retex() }, srcs, 1);
			Quad.addEdge(phi, 0, ocall.next(1), 
				     ocall.nextEdge(1).which_pred());
		    } else {
			phi = phi.grow(new Temp[] { retex }, ex_pred);
		    }
		    Quad.addEdge(ncall, 1, phi, ex_pred++);
		    // update value to point to new CALL
		    value[i] = ncall;
		}
	    }
	    // further updates on quad2quadA
	    for(Iterator it = news.iterator(); it.hasNext(); ) {
		quad2quadA.put(((Quad)it.next()).next(0), 
			       new Quad[] { null, null });	    
	    }
	    // hang on to METHOD
	    METHOD method = (METHOD) begin.prev(0);
	    // remove original NEW and CALL
	    if (ocall.next(1) != null & ocall.next(1).kind() == QuadKind.PHI)
		phi = phi.shrink(ocall.nextEdge(1).which_pred());
	    Quad.addEdge(begin.prev(0), begin.prevEdge(0).which_succ(),
			 ocall.next(0), ocall.nextEdge(0).which_pred());
	    // start updating other Quads
	    this.thisObj = begin.dst();
	    
	    //method.next(0).accept(this);
	}

	public void visit(SET q) {
	    //System.out.println("VISITING: " + q);
	    if (q.objectref() != null && q.objectref().equals(thisObj)) {
		// need to move this SET to the right place
		//System.out.println("Making srcMap");
		Map rcvrMap = (new mapTemp(thisObj, q, quad2quadA,
					   news)).quad2temp;
		Map srcMap = (new mapTemp(q.src(), q, quad2quadA,
					  Collections.EMPTY_SET)).quad2temp;
		// HACK! repeat to make sure the Quads don't change
		rcvrMap = (new mapTemp(thisObj, q, quad2quadA, 
				       news)).quad2temp;
		for(Iterator it = quad2quadA.keySet().iterator(); 
		    it.hasNext(); ) {
		    SIGMA sigma = (SIGMA) it.next();
		    Quad[] value = (Quad[]) quad2quadA.get(sigma);
		    for(int i = 0; i < sigma.arity(); i++) {
			if (sigma.kind() == QuadKind.CALL && i == 1)
			    // ignore exception edges
			    continue;
			if (value[i] != null && 
			    value[i].kind() != QuadKind.SET)
			    // look elsewhere
			    continue;
			Temp rcvr = (Temp) rcvrMap.get(sigma);
			Util.ASSERT(rcvr != null, sigma);
			Temp src = (Temp) srcMap.get(sigma);
			//System.out.println("Printing srcMap");
			SET set = new SET(q.getFactory(), q, q.field(), rcvr,
					  src);
			if (value[i] == null) {
			    // attaching right after CALL
			    Quad.addEdge(set, 0, sigma.next(0),
					 sigma.nextEdge(0).which_pred());
			    Quad.addEdge(sigma, 0, set, 0);
			} else {
			    // attaching after another SET
			    Quad.addEdge(set, 0, value[i].next(0), 
					 value[i].nextEdge(0).which_pred());
			    Quad.addEdge(value[i], 0, set, 0);
			}
			// modify value
			value[i] = set;
		    }
		}
		// remove original SET
		Quad next = q.next(0);
		q.remove();
		next.accept(this);
	    } else {
		// irrelevant SET, just continue
		Util.ASSERT(!q.useC().contains(thisObj));
		q.next(0).accept(this);
	    }
	}

	public void visit(NEW q) {
	    //	    System.out.println("VISITING: " + q);
	    // only continue if this isn't one of the NEWs we added
	    if (!news.contains(q))
		q.next(0).accept(this);
	}

	public void visit(Quad q) {
	    //	    System.out.println("VISITING: " + q);
	    // should not be referring to the receiver
	    Util.ASSERT(!q.useC().contains(thisObj));
	    Util.ASSERT(q.nextLength() == 1 && q.prevLength() == 1, q);
	    q.next(0).accept(this);
	}

	public void visit(SIGMA q) {
	    //	    System.out.println("VISITING: " + q);
	    // only mapped sigmas should get here
	    Util.ASSERT(quad2quadA.containsKey(q));
	    Quad[] quadA = (Quad[]) quad2quadA.get(q);
	    int nSigma = q.numSigmas();
	    // find sigma function for thisObj
	    for(int i = 0; i < q.numSigmas(); i++) {
		if (q.src(i).equals(thisObj)) {
		    nSigma = i;
		    break;
		}
	    }
	    // go down branches
	    Map savedQuad2QuadA = quad2quadA;
	    for(int i = 0; i < q.arity(); i++) {
		if (q.kind() == QuadKind.CALL && i == 1)
		    // skip exceptional branches
		    continue;
		if (quadA[i] == null)
		    // skip, no need to look
		    continue;
		quad2quadA = new HashMap();
		if (quadA[i] != null && quadA[i].kind() != QuadKind.SET) {
		    Worklist toDo = new WorkSet();
		    toDo.push(quadA[i]);
		    while(!toDo.isEmpty()) {
			SIGMA curr = (SIGMA) toDo.pull();
			Quad[] value = (Quad[]) savedQuad2QuadA.remove(curr);
			Util.ASSERT(value != null, curr);
			quad2quadA.put(curr, value);
			for(int j = 0; j < value.length; j++) {
			    if (value[j] != null && 
				value[j].kind() != QuadKind.SET) {
				toDo.push(value[j]);
			    }
			}
		    }
		}
		// setup thisObj
		thisObj = (nSigma < q.numSigmas()) ? q.dst(nSigma, i) : null;
		q.next(i).accept(this);
		// fixup savedQuad2QuadA with possibly changed mappings
		savedQuad2QuadA.putAll(quad2quadA);
	    }
	    // remove referencse to thisObj, if any
	    if (nSigma < q.numSigmas()) {
		// create new src array
		Temp[] src = new Temp[q.numSigmas()-1];
		for(int i = 0; i < q.numSigmas(); i++) {
		    if (i < nSigma)
			src[i] = q.src(i);
		    else if (i > nSigma)
			src[i-1] = q.src(i);
		}
		// create new dst array
		Temp[][] dst = new Temp[q.numSigmas()-1][];
		for(int i = 0; i < q.numSigmas(); i++) {
		    if (i < nSigma) {
			dst[i] = new Temp[q.arity()];
			System.arraycopy(q.dst(i), 0, dst[i], 0, q.arity());
		    } else if (i > nSigma) {
			dst[i-1] = new Temp[q.arity()];
			System.arraycopy(q.dst(i), 0, dst[i-1], 0, q.arity());
		    }
		}
		SIGMA nq = null;
		if (q.kind() == QuadKind.CALL) {
		    CALL ocall = (CALL)q;
		    CALL ncall = new CALL(ocall.getFactory(), ocall, 
					  ocall.method(), ocall.params(), 
					  ocall.retval(), ocall.retex(),
					  ocall.isVirtual(), 
					  ocall.isTailCall(), dst, src);
		    nq = ncall;
		} else {
		    Util.ASSERT(q.kind() == QuadKind.CJMP);
		    CJMP ocjmp = (CJMP)q;
		    CJMP ncjmp = new CJMP(ocjmp.getFactory(), ocjmp, 
					  ocjmp.test(), dst, src);
		    nq = ncjmp;
		}
		// link up new SIGMA
		Quad.replace(q, nq);
		// fix up quad2quadA
		if (quad2quadA.containsKey(q))
		    quad2quadA.put(nq, quad2quadA.remove(q));
		for(Iterator it = quad2quadA.keySet().iterator(); 
		    it.hasNext(); ) {
		    Quad key = (Quad) it.next();
		    Quad[] value = (Quad[]) quad2quadA.get(key);
		    Util.ASSERT(value != null, key);
		    for(int j = 0; j < value.length; j++) {
			if (q.equals(value[j]))
			    value[j] = nq;
		    }
		}		
	    }
	}
    }

    private static class mapTemp extends QuadVisitor
    {
	Map quad2temp;
	Temp target;
	Map quad2quadA;
	Set news;

	mapTemp(Temp target, Quad begin, Map quad2quadA, Set news) {
	    this.news = news;
	    this.target = target;
	    this.quad2quadA = quad2quadA;
	    this.quad2temp = new HashMap();
	    begin.accept(this);
	}

	public void visit(NEW q) {
	    // fixup for thisObj
	    if (news.contains(q))
		target = q.dst();
	    q.next(0).accept(this);
	}
	
	public void visit(SIGMA q) {
	    Temp savedTarget = target;
	    // any SIGMA we visit should be mapped
	    Quad[] quadA = (Quad[]) quad2quadA.get(q);
	    Util.ASSERT(quadA != null, q);
	    for(int i = 0; i < q.arity(); i++) {
		boolean found = false;
		target = savedTarget;
		for(int j = 0; j < q.numSigmas(); j++) {
		    //System.out.println(q.src(j) + " =? " + target);
		    if (q.src(j).equals(target)) {
			target = q.dst(j, i);
			// check for repeats
			for(int k = j+1; k < q.numSigmas(); k++)
			    Util.ASSERT(!q.src(k).equals(target));
			found = true;
			break;
		    }
		}
		if (q.kind() == QuadKind.CALL && i == 1) {
		    // exception edge, ignore
		    //target = savedTarget;
		    continue;
		}
		// may need to add sigma function
		if (!found) {
		    //System.out.println("CANNOT FIND " + target);
		    //System.out.println(q + "\n");
		    Temp[][] dsts = new Temp[q.numSigmas()+1][];
		    for(int j = 0; j < q.numSigmas(); j++) {
			Temp[] dst = new Temp[q.arity()];
			System.arraycopy(q.dst(j), 0, dst, 0, dst.length);
			dsts[j] = dst;
		    }
		    // add sigma function
		    dsts[dsts.length-1] = new Temp[q.arity()];
		    TempFactory tf = q.getFactory().tempFactory();
		    for(int j = 0; j < q.arity(); j++)
			dsts[dsts.length-1][j] = new Temp(tf);
		    Temp[] src = new Temp[q.numSigmas()+1];
		    System.arraycopy(q.src(), 0, src, 0, src.length-1);
		    src[src.length-1] = target;
		    SIGMA nq = null;
		    if (q.kind() == QuadKind.CALL) {
			CALL ocall = (CALL)q;
			CALL ncall = new CALL(ocall.getFactory(), ocall,
					      ocall.method(), ocall.params(),
					      ocall.retval(), ocall.retex(),
					      ocall.isVirtual(), 
					      ocall.isTailCall(), dsts, src);
			nq = ncall;
		    } else {
			Util.ASSERT(q.kind() == QuadKind.CJMP);
			CJMP ocjmp = (CJMP)q;
			CJMP ncjmp = new CJMP(ocjmp.getFactory(), ocjmp,
					      ocjmp.test(), dsts, src);
			nq = ncjmp;
		    }
		    // link up new Quad
		    Quad.replace(q, nq);
		    // fix up maps
		    quad2temp.remove(q);
		    if (quad2quadA.containsKey(q))
			quad2quadA.put(nq, quad2quadA.remove(q));
		    for(Iterator it = quad2quadA.keySet().iterator(); 
			it.hasNext(); ) {
			Quad key = (Quad) it.next();
			Quad[] value = (Quad[]) quad2quadA.get(key);
			for(int j = 0; j < value.length; j++) {
			    if (q.equals(value[j]))
				value[j] = nq;
			}
		    }		
		    q = nq;
		    target = q.dst(q.numSigmas()-1, i);
		    //System.out.println("MADE new " + q);
		    //System.out.println("Target is " + target);
		}
		//System.out.println("CHECKING "+quadA[i]);
		if (quadA[i] != null && quadA[i].kind() != QuadKind.SET) {
		    // continue down this branch
		    //System.out.println("Continuing");
		    q.next(i).accept(this);
		} else {
		    // end here
		    //System.out.println("adding entry");
		    quad2temp.put(q, target);
		}
	    }
	}

	public void visit(Quad q) {
	    Util.ASSERT(q.nextLength() == 1 && q.prevLength() == 1);
	    q.next(0).accept(this);
	}
    }
}





