// MRAFactory.java, created Sat Oct 13 19:47:53 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ARRAYINIT;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CJMP;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.FOOTER;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.MONITORENTER;
import harpoon.IR.Quads.MONITOREXIT;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.OPER;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.Qop;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.QuadFactory;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.SIGMA;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Temp.Temp;
import harpoon.Util.ParseUtil;
import harpoon.Util.Tuple;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <code>MRAFactory</code> generates <code>MRA</code>s.
 * 
 * @author  Karen Zee <kkz@tmi.lcs.mit.edu>
 * @version $Id: MRAFactory.java,v 1.4 2002-02-28 00:55:30 kkz Exp $
 */
public class MRAFactory {
    
    private final ClassHierarchy ch;
    private final CallGraph cg;
    private final HCodeFactory hcf;
    private final Map method2types;
    private Set safeMethods;
    private final CFGrapher cfger;
    private final UseDefer ud;
    private final Map cache;

    /** Creates an <code>MRAFactory</code>.  Requires that 
     *  the <code>HCodeFactory</code> produce code in 
     *  <code>Quad</code> form.  Note that some analysis
     *  is done in the constructor.  If the code is
     *  modified, a new <code>MRAFactory</code> is needed.
     *  For efficiency reasons, <code>hcf</code> should be
     *  a <code>CachingCodeFactory</code>.
     */
    public MRAFactory(ClassHierarchy ch, HCodeFactory hcf, Linker l, 
		      String rName, int optLevel) {
	this.ch = ch;
	this.hcf = hcf;
	this.cg = new CallGraphImpl(ch, hcf);
	this.cfger = CFGrapher.DEFAULT;
	this.ud = UseDefer.DEFAULT;
	this.cache = new HashMap();
	this.method2types = 
	    (optLevel == 2 || optLevel == 3)? dynamicDispatchM2TMap(l, rName): 
	    (optLevel == 4 || optLevel == 5 || optLevel == 7) ? 
	    createMethod2TypesMap(l, rName): new HashMap();
	if (optLevel == 2 || optLevel == 4 || optLevel == 6 || optLevel == 7)
	    findSafeMethods();
	else
	    safeMethods = Collections.EMPTY_SET;
    }

    /** Returns an <code>MRA</code>. */
    public MRA mra(Code c) {
	// check cache first
	HMethod hm = c.getMethod();
	MRA mra = (MRA) cache.get(c);
	if (mra == null) {
	    mra = new MRAImpl(c);
	    cache.put(c, mra);
	}
	return mra;
    }

    /**
     * Removes representation of <code>Code</code> 
     * <code>c</code> from this factory.
     */
    public void clear(Code c) {
	Object o = cache.remove(c);
	Util.ASSERT((o != null), "Failed to remove "+c.getMethod());
    }

    /** Checks whether a method is "safe" (i.e. whether all
     *  calls to a method occurs when the receiver object is
     *  the most recently allocated object.)
     */
    public boolean isSafeMethod(HMethod hm) {
	return safeMethods.contains(hm);
    }

    /** Checks whether the types that the <code>HMethod</code> 
     *  may allocate are known.
     */
    public boolean allocatedTypesKnown(HMethod hm) {
	return method2types.containsKey(hm);
    }

    /** Returns an unmodifiable <code>Set</code> of 
     *  <code>HClass</code>es. The presence of an
     *  <code>HClass</code> in the <code>Set</code> indicates 
     *  that the given <code>HMethod</code> may allocate,
     *  either directly or through calls, objects of that type.
     */ 
    public Set getAllocatedTypes(HMethod hm) {
	return (Set) method2types.get(hm);
    }

    /** Creates a <code>Map</code> of <code>HMethod</code>s
     *  to the <code>Set</code> of <code>Classes</code>
     *  whose objects may be allocated by the method either
     *  directly or indirectly through calling other methods.
     *  Handles dynamically-dispatched calls by conservative
     *  approximation.
     */
    private Map dynamicDispatchM2TMap(Linker linker, String resourceName) {
	// start with an empty map
	Map safe = new HashMap();
	// add methods unless we have no information about them
	for (Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    Code c = (Code) hcf.convert(hm);
	    if (c != null) {
		Set cls = new HashSet();
		safe.put(hm, cls);
		for (Iterator stms = c.getElementsI(); stms.hasNext(); ) {
		    Quad q = (Quad) stms.next();
		    int kind = q.kind();
		    if (kind == QuadKind.ANEW) {
			cls.add(((ANEW)q).hclass());
		    } else if (kind == QuadKind.NEW) {
			cls.add(((NEW)q).hclass());
		    }
		}
	    }
	}
	// add known native methods
	for (Iterator it = parseResource(linker, resourceName).iterator();
	     it.hasNext(); ) {
	    safe.put((HMethod)it.next(), Collections.EMPTY_SET);
	}
	// save results
	safe = Collections.unmodifiableMap(safe);
	// start with the map so far
	Map safer = new HashMap(safe);
	// iterate until fixed point to remove any that
	// are unsafe by calling unsafe methods
	while(true) {
	    boolean changed = false;
	    boolean done = false;
	    for (Iterator it = safe.keySet().iterator(); it.hasNext(); ) {
		HMethod hm = (HMethod) it.next();
		// get all the call sites in hm
		CALL[] calls = cg.getCallSites(hm);
		for (int i = 0; i < calls.length; i++) {
		    // there are multiple possible callees here
		    HMethod[] callees = cg.calls(hm, calls[i]);
		    for(int j = 0; j < callees.length && !done; j++) {
			HMethod callee = callees[j];
			if (!safe.containsKey(callee)) {
			    // if the method being called isn't in the 
			    // safe set, then the caller is also unsafe
			    safer.remove(hm);
			    changed = true;
			    done = true; // done with this method
			    break;
			} else {
			    // if the method being called is safe, then 
			    // add its classes to the set of known classes 
			    // being allocated
			    if (((Set)safer.get(hm)).addAll
				((Set)safe.get(callee)))
				changed = true;
			}
		    }
		}
	    }
	    if (changed) {
		// should be monotonic
		Util.ASSERT(safer.size() <= safe.size());
		safe = Collections.unmodifiableMap(safer);
		safer = new HashMap(safe);
	    } else {
		// reached fix point
		Util.ASSERT(safer.size() == safe.size());
		break;
	    }
	}
	// get a safe view of the keys
	Set keys = Collections.unmodifiableSet(new HashSet(safer.keySet()));
	// save results
	for (Iterator it = keys.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    safer.put(hm, Collections.unmodifiableSet((Set)safer.get(hm)));
	}
	return Collections.unmodifiableMap(safer);
    }
    
    /** Creates a <code>Map</code> of <code>HMethod</code>s
     *  to the <code>Set</code> of <code>Classes</code>
     *  whose objects may be allocated by the method either
     *  directly or indirectly through calling other methods.
     *  <code>HMethods</code> that do not allocate any
     *  objects map to the empty set, while 
     *  <code>HMethod</code>s that are entirely unsafe due
     *  to the presence of virtually-dispatched calls will
     *  be absent from the map (i.e. map to null).
     */
    private Map createMethod2TypesMap(Linker linker, String resourceName) {
	// start with an empty map
	Map safe = new HashMap();
	// add methods unless we know they are unsafe because of 
	// dynamic dispatch or if we have no information because 
	// it is native or abstract
	for (Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    Code c = (Code) hcf.convert(hm);
	    if (c != null) {
		/*
		if (hm.getName().equals("createTree")) {
		    c.print(new java.io.PrintWriter(System.out), null);
		    System.exit(1);
		}
		*/
		Set cls = new HashSet();
		safe.put(hm, cls);
		for (Iterator stms = c.getElementsI(); stms.hasNext(); ) {
		    Quad q = (Quad) stms.next();
		    int kind = q.kind();
		    if (kind == QuadKind.CALL && ((CALL)q).isVirtual()) {
			// remove if unsafe due to dynamic dispatch
			safe.remove(hm);
			break;
		    } else if (kind == QuadKind.ANEW) {
			cls.add(((ANEW)q).hclass());
		    } else if (kind == QuadKind.NEW) {
			cls.add(((NEW)q).hclass());
		    }
		}
	    }
	}
	// add known native methods
	for (Iterator it = parseResource(linker, resourceName).iterator();
	     it.hasNext(); ) {
	    safe.put((HMethod)it.next(), Collections.EMPTY_SET);
	}
	// save results
	safe = Collections.unmodifiableMap(safe);
	// start with the map so far
	Map safer = new HashMap(safe);
	// iterate until fixed point to remove any that
	// are unsafe by calling unsafe methods
	while(true) {
	    boolean changed = false;
	    for (Iterator it = safe.keySet().iterator(); it.hasNext(); ) {
		HMethod hm = (HMethod) it.next();
		CALL[] calls = cg.getCallSites(hm);
		for (int i = 0; i < calls.length; i++) {
		    HMethod callee = calls[i].method();
		    if (!safe.containsKey(callee)) {
			// if the method being called isn't in the safe set, 
			// then the caller is also unsafe
			safer.remove(hm);
			changed = true;
			break;
		    } else {
			// if the method being called is safe, then add its
			// classes to the set of known classes being allocated
			if (((Set)safer.get(hm)).addAll((Set)safe.get(callee)))
			    changed = true;
		    }
		}
	    }
	    if (changed) {
		// should be monotonic
		Util.ASSERT(safer.size() < safe.size());
		safe = Collections.unmodifiableMap(safer);
		safer = new HashMap(safe);
	    } else {
		// reached fix point
		Util.ASSERT(safer.size() == safe.size());
		break;
	    }
	}
	// get a safe view of the keys
	Set keys = Collections.unmodifiableSet(new HashSet(safer.keySet()));
	// save results
	for (Iterator it = keys.iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    safer.put(hm, Collections.unmodifiableSet((Set)safer.get(hm)));
	}
	return Collections.unmodifiableMap(safer);
    }
    
    /** Calculates the <code>Set</code> of safe methods;
     *  a safe method is one where the receiver object 
     *  is the most recently allocated object for all 
     *  calls of that method in the program.
     */
    private void findSafeMethods() {
	// start with the set of all analyzable methods
	Set methods = new HashSet();
	for (Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if (!hm.isStatic() && !hm.isInterfaceMethod()) {
		Code c = (Code) hcf.convert(hm);
		if (c != null)
		    methods.add(hm);
	    }
	}
	// assume that they are all safe
	safeMethods = Collections.unmodifiableSet(methods);
	// Start with the set of initializers and remove 
	// any that get called when the receiver is not 
	// mra. Iterate until a fixed-point is reached.
	while (true) {
	    Set safe = new HashSet(safeMethods);
	    for (Iterator it = ch.callableMethods().iterator(); 
		 it.hasNext(); ) {
		HMethod hm = (HMethod) it.next();
		Code c = (Code) hcf.convert(hm);
		if (c != null) {
		    MRA mra = mra(c);
		    CALL[] calls = cg.getCallSites(hm);
		    for(int i = 0; i < calls.length; i++) {
			CALL call = calls[i];
			// consider only initializers that
			// have not been eliminated
			if (safe.contains(call.method())) {
			    Tuple mra_before = mra.mra_before(call);
			    Map m = (Map) mra_before.proj(0);
			    Set s = (Set) mra_before.proj(1);
			    if (!m.containsKey(call.params(0)) || 
				!s.isEmpty()) {
				// if the receiver is not the MRA
				// object, or if there are exceptions,
				// then the called method is not safe
				if (call.isVirtual()) {
				    // if the call is virtual, none
				    // of the other methods callable
				    // at this site are safe, either
				    HMethod[] cms = cg.calls(hm, call);
				    for(int j = 0; j < cms.length; j++)
					safe.remove(cms[j]);
				} else {
				    // if the call is not virtual, the
				    // exact called method is removed
				    safe.remove(call.method());
				}
			    }
			}
		    }
		}
	    }
	    // save results
	    safe = Collections.unmodifiableSet(safe);
	    // newly-removed safe initializers must be re-analyzed
	    for (Iterator it = safeMethods.iterator(); it.hasNext(); ) {
		HMethod hm = (HMethod) it.next();
		if (!safe.contains(hm)) {
		    Code c = (Code) hcf.convert(hm);
		    Util.ASSERT(c != null);
		    clear(c);
		}
	    }
	    if (safeMethods.size() == safe.size()) {
		// reached fix-point
		safeMethods = safe;
		break;
	    } else {
		// continue, size of set must be decreasing
		Util.ASSERT(safeMethods.size() > safe.size());
		safeMethods = safe;
	    }
	}
    }
    
    /** Implementation of <code>MRA</code> analysis. */
    private class MRAImpl extends MRA {

	private boolean DEBUG = false;
	private final Code code;
	private final boolean isSafeMethod;
	private final BasicBlock.Factory bbf;
	private final Map bb2pre;
	private Map bb2post;

	/** Creates a new <code>MRA</code>. Performs analysis. 
	 *  If the code is modified, then a new MRA needs to be
	 *  created.
	 */
	private MRAImpl(Code c) {
	    this.code = c;
	    this.isSafeMethod = isSafeMethod(code.getMethod());
	    this.bbf = new BasicBlock.Factory(code, cfger);
	    this.bb2pre = new HashMap();
	    this.bb2post = new HashMap();
	    analyze();
	    // after analysis, we can get rid of the post map
	    this.bb2post = null;
	}

	private Map results = new HashMap();

	/** Returns a <code>Tuple</code> that characterizes
	 *  the state before the given <code>Quad</code>.
	 */
	public Tuple mra_before(Quad q) {
	    // first, check our cache
	    Tuple t = (Tuple) results.get(q);
	    if (t != null) return t;
	    // if not in cache, then calculate
	    BasicBlock bb = bbf.getBlock(q);
	    Util.ASSERT(bb != null);
	    Tuple pre = (Tuple) bb2pre.get(bb);
	    Util.ASSERT(pre != null);
	    Tuple post = new Tuple(new Object[] 
				   { new HashMap((Map)pre.proj(0)),
				     new HashSet((Set)pre.proj(1)),
				     (Quad)pre.proj(2),
				     new HashSet((Set)pre.proj(3)) });
	    for(Iterator it = bb.statements().iterator(); it.hasNext(); ) {
		Quad curr = (Quad) it.next();
		if (q.equals(curr))
		    break;
		// PHIs and LABELs (which are also PHIs) are 
		// handled specially. so are CJMPs, SWITCHs and 
		// TYPESWITCHs, which are actually SIGMAs.
		// CALLs are SIGMAs, but the "call" part is 
		// handled separately from the "sigma" part
		if (curr.kind() != QuadKind.PHI && 
		    curr.kind() != QuadKind.LABEL &&
		    curr.kind() != QuadKind.CJMP &&	
		    curr.kind() != QuadKind.SWITCH &&	
		    curr.kind() != QuadKind.TYPESWITCH)
		    post = transfer(curr, post);
		else if (DEBUG)
		    System.out.println(q);
	    }
	    t = new Tuple
		(new Object[] 
		 { Collections.unmodifiableMap((Map)post.proj(0)),
		   Collections.unmodifiableSet((Set)post.proj(1)),
		   (Quad)post.proj(2),
		   Collections.unmodifiableSet((Set)post.proj(3)) });
	    results.put(q, t);
	    return t;
	}


	/** Performs analysis. This may take a while.
	 */
	private void analyze() {
	    // create universe
	    Set temps = new HashSet();
	    for(Iterator it = code.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		temps.addAll(ud.useC(q));
		temps.addAll(ud.defC(q));
	    }
	    // save resultss
	    temps = Collections.unmodifiableSet(temps);
	    // maps Temps to an array of 2 booleans
	    // the first boolean is true iff
	    // the method is an initializer and
	    // the given Temp points to the
	    // receiver object
	    // the second boolean is true iff
	    // the given Temp succeeded the receiver
	    // as the most-recently-allocated object
	    Map universe = new HashMap();
	    for(Iterator it = temps.iterator(); it.hasNext(); ) {
		universe.put((Temp)it.next(), MRA.MRAToken.TOP);
	    }
	    universe = Collections.unmodifiableMap(universe);
	    // initialize the maps
	    for(Iterator it = bbf.blocksIterator(); it.hasNext(); )
		bb2post.put((BasicBlock)it.next(), 
			    new Tuple(new Object[] { universe, 
						     Collections.EMPTY_SET,
						     null,
			                             temps }));
	    // initialize worklist
	    Worklist toprocess = new WorkSet();
	    toprocess.push((BasicBlock)bbf.getRoot());
	    // keep going until we reached a fixed point
	    while(!toprocess.isEmpty()) {
		BasicBlock bb = (BasicBlock)toprocess.pull();
		// construct the pre-Set
		Tuple pre = getPre(bb);
		// add the unmodifiable original to the map
		bb2pre.put(bb, pre);
		// make a copy to modify
		Tuple tup = new Tuple
		    (new Object[] { new HashMap((Map)pre.proj(0)),
				    new HashSet((Set)pre.proj(1)),
				    (Quad)pre.proj(2),
		                    new HashSet((Set)pre.proj(3)) });
		for(Iterator it = bb.statements().iterator(); it.hasNext(); ) {
		    Quad q = (Quad) it.next();
		    // PHIs (which include LABELs) are handled implicitly 
		    // in the call to getPre(). SIGMAs (which include  
		    // CALLs, CJMPs, SWITCHes and TYPESWITCHes) are also 
		    // handled implicitly in the call to getPre(), which 
		    // calls getPost(). CALLs are handled twice because
		    // they have non-SIGMA defs.
		    int kind = q.kind();
		    if (kind != QuadKind.PHI && kind != QuadKind.LABEL &&
			kind != QuadKind.CJMP && kind != QuadKind.SWITCH &&
			kind != QuadKind.TYPESWITCH)
			tup = transfer(q, tup);
		    else if (DEBUG)
			System.out.println(q);
		}
		Tuple post = (Tuple) bb2post.get(bb);
		// check for convergence:
		Set except = (Set) tup.proj(1);
		if (((Set)tup.proj(1)).size() == ((Set)post.proj(1)).size()) {
		    // exception sets converged, check next:
		    if (((Map)tup.proj(0)).equals((Map)post.proj(0))) {
			// mra maps converged, check next: 
			Quad q1 = (Quad)tup.proj(2);
			Quad q2 = (Quad)post.proj(2);
			if ((q1 == null && q2 == null) ||
			    (q1 != null && q1.equals(q2))) {
			    // all converged, go to next in worklist
			    continue;
			}
		    }
		} else {
		    Util.ASSERT(((Set)tup.proj(1)).size() > 
				 ((Set)post.proj(1)).size());
		}
		// not converged, update map with new values,
		// and add successors to worklist
		bb2post.put(bb, new Tuple
			    (new Object[]
			     { Collections.unmodifiableMap((Map)tup.proj(0)),
			       Collections.unmodifiableSet((Set)tup.proj(1)),
			       (Quad)tup.proj(2),
			       Collections.unmodifiableSet((Set)tup.proj(3)) }));
		for(Iterator it = bb.nextSet().iterator(); it.hasNext(); )
		    toprocess.push((BasicBlock)it.next());
	    }
	}

	/** Returns a <code>Tuple</code> containing an unmodifiable 
	 *  <code>Map</code>, an unmodifiable <code>Set</code>, and a
	 *  <code>Quad</code> characterizing the state of the 
	 *  analysis prior to the given <code>Quad</code>.
	 *  
	 *  Handles complications that arise due to
	 *  <code>SIGMA</code>s and <code>PHI</code>s.
	 */
	private Tuple getPre(BasicBlock bb) {
	    // start from empty maps and sets
	    Map mra = new HashMap();
	    Set except = new HashSet();
	    Set allocs = new HashSet();
	    Set receiver = new HashSet();
	    // look at the first Quad of the BasicBlock
	    Quad q = (Quad) bb.statements().get(0);
	    // PHIs are special
	    if (q.kind() == QuadKind.LABEL ||
		q.kind() == QuadKind.PHI) {
		PHI phi = (PHI) q;
		Quad[] prev = phi.prev();
		// need to cycle through predecessors
		for (int i = 0; i < phi.arity(); i++) {
		    Tuple post = getPost(bbf.getBlock(prev[i]), bb);
		    // create a new map since we need to modify it
		    Map pmra = new HashMap((Map) post.proj(0));
		    // create a new set since we need to modify it
		    Set preceiver = new HashSet((Set) post.proj(3));
		    for (int j = 0; j < phi.numPhis(); j++) {
			Temp src = phi.src(j, i);
			// substitute srcs for dsts
			if (pmra.containsKey(src)) {
			    MRA.MRAToken tok = (MRA.MRAToken) pmra.remove(src);
			    Util.ASSERT(tok != null);
			    pmra.put(phi.dst(j), tok);
			}
			if (preceiver.remove(src)) {
			    preceiver.add(phi.dst(j));
			}
		    }
		    // perform intersection
		    if (i == 0) {
			mra.putAll(pmra);
			receiver.addAll(preceiver);
		    } else {
			intersect(mra, pmra);
			receiver.retainAll(preceiver);
		    }
		    // exceptions are just unioned
		    except.addAll((Set) post.proj(1));
		    // collect all allocations sites
		    allocs.add((Quad) post.proj(2));
		}
	    } else {
		// we may have joins that are not
		// PHIs or LABELs (FOOTERs), so
		// perform intersection for mra,
		// union for exceptions
		Iterator it = bb.prevSet().iterator();
		if (it.hasNext()) {
		    Tuple post = getPost((BasicBlock)it.next(), bb);
		    mra.putAll((Map) post.proj(0));
		    except.addAll((Set) post.proj(1));
		    allocs.add((Quad) post.proj(2));
		    receiver.addAll((Set) post.proj(3));
		}
		while (it.hasNext()) {
		    Tuple post = getPost((BasicBlock)it.next(), bb);
		    Map pmra = (Map) post.proj(0);
		    intersect(mra, pmra);
		    except.addAll((Set) post.proj(1));
		    allocs.add((Quad) post.proj(2));
		    receiver.retainAll((Set) post.proj(3));
		}
	    }
	    Quad alloc = null;
	    if (allocs.size() == 1) {
		alloc = (Quad) allocs.iterator().next();
	    }
	    return new Tuple(new Object[]
			     { Collections.unmodifiableMap(mra),
			       Collections.unmodifiableSet(except), 
			       alloc,
			       Collections.unmodifiableSet(receiver) });
	}

	// Handles the complicated post-Sets that
	// occur when we have SIGMAs and PHIs.
	// The successor is needed when bb has
	// multiple sucessors, because its post-Sets
	// are different for each successor
	// returns an array of unmodifiable sets
	private Tuple getPost(BasicBlock bb, BasicBlock succ) {
	    List stms = bb.statements();
	    Quad q = (Quad) stms.get(stms.size()-1);
	    // SIGMAs are special, but abstract
	    if (q.kind() == QuadKind.CALL ||
		q.kind() == QuadKind.CJMP || 
		q.kind() == QuadKind.SWITCH || 
		q.kind() == QuadKind.TYPESWITCH) {
		// find our arity
		SIGMA sigma = (SIGMA) q;
		int arity = 0;
		Quad match = (Quad) succ.statements().get(0);
		Quad[] next = sigma.next();
		for ( ; arity < sigma.arity(); arity++) {
		    if (match.equals(next[arity])) {
			// found arity
			Tuple t = (Tuple) bb2post.get(bb);
			Map mra = new HashMap((Map)t.proj(0));
			Set receiver = new HashSet((Set)t.proj(3));
			for (int i = 0; i < sigma.numSigmas(); i++) {
			    Temp src = sigma.src(i);
			    // exchange dst for src in mra
			    if (mra.containsKey(src)) {
				MRA.MRAToken tok = (MRA.MRAToken) mra.remove(src);
				Util.ASSERT(tok != null);
				mra.put(sigma.dst(i, arity), tok);
			    }
			    if (receiver.remove(src)) {
				receiver.add(sigma.dst(i, arity));
			    }
			}
			// make unmodifiable
			return new Tuple(new Object[]
					 { Collections.unmodifiableMap(mra),
					   (Set)t.proj(1), 
					   (Quad)t.proj(2),
					   Collections.unmodifiableSet(receiver) });
		    }
		}
		// should never get here
		throw new Error("Cannot find arity of "+succ+" w.r.t. "+bb);
	    }
	    // otherwise...
	    return (Tuple) bb2post.get(bb);
	}

	/** Transfer function for the analysis. Returns
	 *  a <code>Tuple</code> characterizing the
	 *  state after the <code>Quad</code> <code>q</code>.
	 */
    	private Tuple transfer(Quad q, Tuple pre) {
	    if (DEBUG) {
		System.out.println(q);
		System.out.print("  "+pre.proj(0));
		System.out.print(" except for "+pre.proj(1));
		System.out.print(" allocated at "+pre.proj(2));
	    }
	    int kind = q.kind();
	    Map mra = (Map) pre.proj(0);
	    Set except = (Set) pre.proj(1);
	    Set receiver = (Set) pre.proj(3);
	    if (kind == QuadKind.ANEW || 
		kind == QuadKind.NEW) {
		boolean succeeding = false;
		if (isSafeMethod) {
		    // check if the Temp we def is succeeding the
		    // receiver as the most recently allocated object
		    succeeding = true;
		    for(Iterator it = mra.values().iterator(); 
			it.hasNext(); ) {
			if (((MRA.MRAToken)it.next()) != MRA.MRAToken.RCVR)
			    succeeding = false;
		    }
		}
		// the Temp we def is now the mra
		mra.clear();
		mra.put(q.def()[0], (succeeding ? 
				     MRA.MRAToken.SUCC : MRA.MRAToken.BOTTOM));
		// there should only be one
		Util.ASSERT(ud.defC(q).size() == 1);
		// clear exceptions after an allocation
		except.clear();
		// remove dst from receiver
		receiver.remove(q.def()[0]);
		// make new Tuple, because we are changing the allocation site
		pre = new Tuple(new Object[] { mra, except, q, receiver });
	    } else if (kind == QuadKind.CALL) {
		// after a call, we can no longer be
		// certain of the most recently allocated
		// object, unless that method has been
		// pre-approved, in which case only the
		// return value and exception are toast
		// the "sigma" part of the call is
		// handled as part of the control flow
		CALL call = (CALL) q;
		if (allocatedTypesKnown(call.method())) {
		    Set exceptions = (Set) getAllocatedTypes(call.method());
		    Util.ASSERT(exceptions != null);
		    // need to add exceptions
		    except.addAll(exceptions);
		    // note our defs
		    Temp retval = call.retval();
		    if (retval != null) {
			mra.remove(retval);
			receiver.remove(retval);
		    }
		    Temp retex = call.retex();
		    if (retex != null) {
			mra.remove(retex);
			receiver.remove(retval);
		    }
		} else {
		    // not safe, just clear
		    mra.clear();
		}
	    } else if (kind == QuadKind.MOVE) {
		// if the src is an mra, then the dst should
		// be added to the map as a clone of the src
		MOVE move = (MOVE)q;
		if (mra.containsKey(move.src())) {
		    MRA.MRAToken token = (MRA.MRAToken) mra.get(move.src());
		    Util.ASSERT(token != null);
		    mra.put(move.dst(), token);
		} else {
		    mra.remove(move.dst());
		}
		// keep receiver set updated
		if (receiver.contains(move.src())) {
		    receiver.add(move.dst());
		} else {
		    receiver.remove(move.dst());
		}
	    } else if (kind == QuadKind.METHOD) {
		Util.ASSERT(mra.isEmpty());
		if (isSafeMethod) {
		    Temp rcvr = ((METHOD) q).params(0);
		    mra.put(rcvr, MRA.MRAToken.RCVR);
		    receiver.add(rcvr);
		}
	    } else if (kind == QuadKind.AGET || 
		       kind == QuadKind.ALENGTH ||
		       kind == QuadKind.COMPONENTOF ||
		       kind == QuadKind.CONST ||
		       kind == QuadKind.GET ||
		       kind == QuadKind.HANDLER ||
		       kind == QuadKind.INSTANCEOF ||
		       kind == QuadKind.OPER) {
		// defs of a Temp revoke its mra status
		mra.remove(q.def()[0]);
		receiver.remove(q.def()[0]);
		// should only be one for these Quads
		Util.ASSERT(ud.defC(q).size() == 1);
	    } else if (kind == QuadKind.ARRAYINIT ||
		       kind == QuadKind.ASET ||
		       kind == QuadKind.DEBUG ||
		       kind == QuadKind.FOOTER ||
		       kind == QuadKind.HEADER ||
		       kind == QuadKind.MONITORENTER ||
		       kind == QuadKind.MONITOREXIT ||
		       kind == QuadKind.NOP ||
		       kind == QuadKind.RETURN ||
		       kind == QuadKind.SET ||
		       kind == QuadKind.THROW ||
		       kind == QuadKind.TYPECAST) {
		// Quads with no defs have no effect
		Util.ASSERT(ud.defC(q).size() == 0);
	    } else {
		// LABELs, PHIs, SIGMAs, SWITCHs, TYPESWITCHs and XIs 
		// are not handled, but there should not be any
		throw new Error("Unknown QuadKind: "+q.kind()+" for "+q);
	    }
	    if (DEBUG) {
		System.out.print(" -> ");
		System.out.println(pre.proj(0)+" except for "+pre.proj(1));
	    }
	    return pre;
	}
    }

    /** Requires that m1 and m2 map <code>Temp</code>s to
     *  <code>MRA.MRAToken</code>s.
     *  Modifies m1 such that it is an intersection of m1
     *  and m2 according to the following rules:
     * 
     *  1. m1 will contain a mapping for a key k iff m1
     *     originally contained a mapping for k and if m2 
     *     contains a mapping for k.
     *  2. The value to which k maps in m1 will be the
     *     join of the value to which k originally mapped
     *     in m1 and the value to which k maps in m2.
     */
    private static void intersect(Map m1, Map m2) {
	// make a copy of the keys before modifying the map
	Set keySet = new HashSet(m1.keySet());
	for(Iterator it = keySet.iterator(); it.hasNext(); ) {
	    Temp t = (Temp) it.next();
	    if (m2.containsKey(t)) {
		// both m1 and m2 contain a mapping for t
		// compute join and put new mapping in m1
		MRA.MRAToken t1 = (MRA.MRAToken) m1.get(t);
		MRA.MRAToken t2 = (MRA.MRAToken) m2.get(t);
		Util.ASSERT(t1 != null && t2 != null);
		m1.put(t, t1.join(t2));
	    } else {
		// m2 does not contain a mapping for t
		// therefore, remove mapping from m1
		m1.remove(m2);
	    }
	}
    }

    private static Set parseResource(final Linker l, String resourceName) {
	final Set result = new HashSet();
	try {
	    ParseUtil.readResource(resourceName, new ParseUtil.StringParser() {
		public void parseString(String s)
		    throws ParseUtil.BadLineException {
		    result.add(ParseUtil.parseMethod(l, s));
		}
	    });
	} catch (java.io.IOException ex) {
	    System.err.println("ERROR READING SAFE SET, SKIPPING REST.");
	    System.err.println(ex.toString());
	}
	// done.
	return result;
    }
}
