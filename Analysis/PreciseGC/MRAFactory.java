// MRAFactory.java, created Sat Oct 13 19:47:53 2001 by kkz
// Copyright (C) 2000 Karen Zee <kkz@tmi.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PreciseGC;

import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.ClassHierarchy;
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
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.WorkSet;

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
 * @version $Id: MRAFactory.java,v 1.1.2.2 2001-10-19 20:45:13 kkz Exp $
 */
public class MRAFactory {
    
    private final ClassHierarchy ch;
    private final HCodeFactory ccf;
    private final Map safeMethods;
    private Set safeInitializers;
    private final CFGrapher cfger;
    private final UseDefer ud;
    private final Map cache;

    /** Creates a <code>MRAFactory</code>.  Requires that 
     *  the <code>HCodeFactory</code> produce code in 
     *  <code>Quad</code> form.  Note that some analysis
     *  is done in the constructor.  If the code is
     *  modified, a new <code>MRAFactory</code> is needed.
     */
    public MRAFactory(ClassHierarchy ch, HCodeFactory hcf, Linker l, 
		      String rName) {
	this.ch = ch;
	this.ccf = new CachingCodeFactory(hcf);
	this.cfger = CFGrapher.DEFAULT;
	this.ud = UseDefer.DEFAULT;
	this.cache = new HashMap();
	this.safeMethods = safeMethods(l, rName);
	// initialize to empty so findSafeInitializers
	// can run using the MRA analysis; update later
	this.safeInitializers = Collections.EMPTY_SET;
	findSafeInitializers();
	//System.out.println("ClassHierarchy size = "+
	//	   ch.callableMethods().size());
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

    /** Checks whether an initializer is "safe" (i.e. whether
     *  all calls to the initializer occurs when the object
     *  being initialized is the most recently allocated object.
     */
    public boolean isSafe(HMethod hm) {
	return safeInitializers.contains(hm);
    }

    private void findSafeInitializers() {
	Set allInitializers = new HashSet();
	// iterate through all callable methods,
	// identifying those that are initializers
	for (Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    if (hm.getName().equals("<init>")) {
		allInitializers.add(hm);
	    }
		/*
		((Code)ccf.convert(hm)).print
		    (new java.io.PrintWriter(System.out), null);
	    } else if (hm.getName().equals("getDefault")) {
		((Code)ccf.convert(hm)).print
		    (new java.io.PrintWriter(System.out), null);
	    }
		*/
	    /*
	    else if (hm.getName().equals("makeEdge") ||
		     hm.getName().equals("symmetric") ||
		     hm.getName().equals("buildDelaunay"))
		((Code)ccf.convert(hm)).print
		    (new java.io.PrintWriter(System.out), null);
	    */
	}
	// freeze results
	allInitializers = Collections.unmodifiableSet(allInitializers);
	//System.out.println("TOTAL INITIALIZERS = "+allInitializers.size());
	while (true) {
	    Set initializers = new HashSet(allInitializers);
	    // go through all Quads and throw out any initializers 
	    // that get called when the object being initialized is 
	    // not the mra object. iterate until fixed-point.
	    for (Iterator it = ch.callableMethods().iterator(); 
		 it.hasNext(); ) {
		HMethod hm = (HMethod) it.next();
		Code c = (Code) ccf.convert(hm);
		if (c != null) {
		    MRA mra = mra(c);
		    for (Iterator stms = c.getElementsI(); stms.hasNext(); ) {
			Quad q = (Quad) stms.next();
			if (q.kind() == QuadKind.CALL) {
			    CALL call = (CALL) q;
			    // if this initializer is still being considered...
			    if (initializers.contains(call.method())) {
				Set[] mra_before = mra.mra_before(call);
				if (!mra_before[0].contains(call.params(0)) ||
				    !mra_before[1].isEmpty())
				    // remove if receiver is not mra
				    // or if we have exceptions
				    initializers.remove(call.method());
				/*
				System.out.println("REMOVING "+call.method());
				System.out.println("REASON "+call);
				System.out.println(mra.mra_before(call));
				c.print(new java.io.PrintWriter(System.out), null);
				*/
			    }
			}
		    }
		}
	    }
	    // newly-minted safe iterators need to be re-analyzed
	    for (Iterator it = initializers.iterator(); it.hasNext(); ) {
		HMethod hm = (HMethod) it.next();
		if (!safeInitializers.contains(hm))
		    cache.remove((Code)ccf.convert(hm));
	    }
	    // save size of set
	    int old_size = safeInitializers.size();
	    safeInitializers = Collections.unmodifiableSet(initializers);
	    //System.out.println("SAFE INITIALIZERS = "+safeInitializers.size());
	    // reached fix point
	    if (old_size == safeInitializers.size())
		break;
	}
	//System.out.println("TOTAL INITIALIZERS = "+allInitializers.size());
	//System.out.println("SAFE INITIALIZERS = "+safeInitializers.size());
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
    private Map safeMethods(Linker linker, String resourceName) {
	// create a preliminary map
	Map prelim = new HashMap();
	// go through all callable methods, first pass
	for (Iterator it = ch.callableMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    Code c = (Code) ccf.convert(hm);
	    // ignore abstract or native methods
	    if (c != null) {
		Set classes_allocated = new HashSet();
		prelim.put(hm, classes_allocated);
		for (Iterator stms = c.getElementsI(); stms.hasNext(); ) {
		    Quad q = (Quad) stms.next();
		    // toss out any methods that we can't 
		    // be sure of b/c of dynamic dispatch
		    if (q.kind() == QuadKind.CALL && ((CALL)q).isVirtual()) {
			prelim.remove(hm);
			break;
		    } else if (q.kind() == QuadKind.ANEW) {
			classes_allocated.add(((ANEW)q).hclass());
		    } else if (q.kind() == QuadKind.NEW) {
			classes_allocated.add(((NEW)q).hclass());
		    }
		}
	    }
	}
	// freeze results
	prelim = Collections.unmodifiableMap(prelim);
	// the remaining methods may allocate indirectly
	Map cleared = new HashMap();
	// get any native methods
	for (Iterator it = parseResource(linker, resourceName).iterator(); 
	     it.hasNext(); )
	    cleared.put((HMethod)it.next(), Collections.EMPTY_SET);
	// start with the preliminary results
	Map clearable = new HashMap(prelim);
	while (true) {
	    boolean changes = false;
	    // use the prelim key set since the map is frozen
	    for (Iterator it = prelim.keySet().iterator(); it.hasNext(); ) {
		HMethod hm = (HMethod) it.next();
		Code c = (Code) ccf.convert(hm);
		boolean approved = true;
		// toss out any methods that allocates indirectly
		// only non-virtual calls remain at this point
		for (Iterator stms = c.getElementsI(); stms.hasNext(); ) {
		    Quad q = (Quad) stms.next();
		    if (q.kind() == QuadKind.CALL) {
			CALL call = (CALL) q;
			HMethod callee = call.method();
			// cleared methods are okay
			if (!cleared.containsKey(callee)) {
			    if (!clearable.containsKey(callee)) {
				// bad method... next!
				clearable.remove(hm);
				approved = false;
				/*
				if (hm.getName().equals("symmetric") ||
				    hm.getName().equals("<init>")) {
				    System.out.println("REMOVING "+hm);
				    System.out.println("REASON "+callee);
				    if (ccf.convert(callee) != null)
					((Code)ccf.convert(callee)).print
					    (new java.io.PrintWriter
					     (System.out), null);
				    else
					System.out.println("NO CODE");
				}
				*/
				break;
			    }
			    // even if the method not definitely
			    // bad, don't know yet, so not approved
			    approved = false;
			} else {
			    // cleared method, add to allocated classes
			    Set classes = (Set)cleared.get(((CALL)q).method());
			    // if the set is changing, then can't stop
			    if (((Set)clearable.get(hm)).addAll(classes))
				changes = true;
			}
		    }
		}
		// if we got this far, then we are
		// calling only cleared methods or
		// not calling at all
		if (approved) {
		    // transfer from clearable to cleared
		    cleared.put(hm, (Set)clearable.remove(hm));
		}
	    }
	    if (clearable.size() == prelim.size() && !changes) {
		break; // reached fix point
	    } else {
		// freeze results
		prelim = Collections.unmodifiableMap(clearable);
		clearable = new HashMap(clearable);
	    }
	}
	cleared.putAll(clearable);
	/*
	for (Iterator it = cleared.keySet().iterator(); it.hasNext(); ) {
	    HMethod hm = (HMethod) it.next();
	    System.out.println(hm);
	    System.out.println((Set)cleared.get(hm));
	}
	*/
	return Collections.unmodifiableMap(cleared);
    }
    
    /** Implementation of <code>MRA</code> analysis. */
    private class MRAImpl extends MRA {

	private boolean DEBUG = false;
	private final Code code;
	private final BasicBlock.Factory bbf;
	private final Map bb2pre;
	private Map bb2post;

	/** Creates a new <code>MRA</code>. Performs analysis. 
	 *  If the code is modified, then a new MRA needs to be
	 *  created.
	 */
	private MRAImpl(Code c) {
	    this.code = c;
	    //this.DEBUG = c.getMethod().getName().equals("buildDelaunay");
	    this.bbf = new BasicBlock.Factory(code, cfger);
	    this.bb2pre = new HashMap();
	    this.bb2post = new HashMap();
	    analyze();
	    // after analysis, we can get rid of the post map
	    this.bb2post = null;
	}

	/** Returns the Set of <code>Temp</code>s that
	 *  contain the address of the most recently
	 *  allocated object at the given program point,
	 *  before the given <code>Quad</code> is
	 *  executed.  This function is undefined for
	 *  <code>PHI<code>s.
	 */
	public Set[] mra_before(Quad q) {
	    BasicBlock bb = bbf.getBlock(q);
	    Util.assert(bb != null);
	    Set[] pre = (Set[]) bb2pre.get(bb);
	    Util.assert(pre != null);
	    Set[] post = new HashSet[] { new HashSet(pre[0]), 
					 new HashSet(pre[1]) };
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
		    transfer(curr, post);
		else if (DEBUG)
		    System.out.println(q);
	    }
	    return new Set[] { Collections.unmodifiableSet(post[0]),
			       Collections.unmodifiableSet(post[1]) };
	}


	private void analyze() {
	    // create universe
	    Set universe = new HashSet();
	    for(Iterator it = code.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		universe.addAll(ud.useC(q));
		universe.addAll(ud.defC(q));
	    }
	    universe = Collections.unmodifiableSet(universe);
	    // initialize the maps
	    for(Iterator it = bbf.blocksIterator(); it.hasNext(); )
		bb2post.put((BasicBlock)it.next(), 
			    new Set[] { universe, Collections.EMPTY_SET });
	    // initialize worklist
	    Worklist toprocess = new WorkSet();
	    toprocess.push((BasicBlock)bbf.getRoot());
	    // keep going until we reached a fixed point
	    while(!toprocess.isEmpty()) {
		BasicBlock bb = (BasicBlock)toprocess.pull();
		// construct the pre-Set
		Set[] pre = preSets(bb);
		// add the unmodifiable original to the map
		bb2pre.put(bb, pre);
		// make a copy
		Set mra = new HashSet(pre[0]);
		Set except = new HashSet(pre[1]);
		for(Iterator it = bb.statements().iterator() ; 
		    it.hasNext(); ) {
		    Quad q = (Quad) it.next();
		    // PHIs and LABELs (which are also PHIs) are handled 
		    // specially. so are CJMPs, SWITCHs and TYPESWITCHs, 
		    // which are actually SIGMAs
		    // CALLs are SIGMAs, but the "call" part is handled
		    // separately from the "sigma" part
		    if (q.kind() != QuadKind.PHI && 
			q.kind() != QuadKind.LABEL &&
			q.kind() != QuadKind.CJMP &&	
			q.kind() != QuadKind.SWITCH &&	
			q.kind() != QuadKind.TYPESWITCH)
			transfer(q, new Set[] { mra, except} );
		    else if (DEBUG)
			System.out.println(q);
		}
		// if the post-Sets have changed, then update
		// the map and add successors to worklist
		if (mra.size() != ((Set[])bb2post.get(bb))[0].size() ||
		    except.size() != ((Set[])bb2post.get(bb))[1].size()) {
		    bb2post.put(bb, new Set[] 
				{ Collections.unmodifiableSet(mra),
				  Collections.unmodifiableSet(except) });
		    for(Iterator it = bb.nextSet().iterator(); it.hasNext(); )
			toprocess.push((BasicBlock)it.next());
		}
	    }
	}

	// handles the complicated pre-Sets that
	// occur when we have SIGMAs and PHIs
	// returns an array of unmodifiable sets
	private Set[] preSets(BasicBlock bb) {
	    // start with the empty set
	    Set mra = new HashSet();
	    Set except = new HashSet();
	    Quad q = (Quad) bb.statements().get(0);
	    // PHIs are special
	    if (q.kind() == QuadKind.LABEL ||
		q.kind() == QuadKind.PHI) {
		PHI phi = (PHI) q;
		Quad[] prev = phi.prev();
		// need to cycle through predecessors
		for (int i = 0; i < phi.arity(); i++) {
		    Set[] post = postSets(bbf.getBlock(prev[i]), bb);
		    Set pmra = new HashSet(post[0]);
		    for (int j = 0; j < phi.numPhis(); j++) {
			Temp src = phi.src(j, i);
			// substitute srcs for dsts
			if (pmra.remove(src))
			    pmra.add(phi.dst(j));
		    }
		    // perform intersection
		    if (i == 0)
			mra.addAll(pmra);
		    else
			mra.retainAll(pmra);
		    // exceptions are just unioned
		    except.addAll(post[1]);
		}
	    } else {
		// special case for joins that
		// are not PHIs or LABELs (FOOTERs)
		// perform straight intersection
		// for mra, union for exceptions
		Iterator it = bb.prevSet().iterator();
		if (it.hasNext()) {
		    Set[] post = postSets((BasicBlock)it.next(), bb);
		    mra.addAll(post[0]);
		    except.addAll(post[1]);
		}
		while (it.hasNext()) {
		    Set[] post = postSets((BasicBlock)it.next(), bb);
		    mra.retainAll(post[0]);
		    except.addAll(post[1]);
		}
	    }
	    return new Set[] { Collections.unmodifiableSet(mra),
			       Collections.unmodifiableSet(except) };
	}

	// Handles the complicated post-Sets that
	// occur when we have SIGMAs and PHIs.
	// The successor is needed when bb has
	// multiple sucessors, because its post-Sets
	// are different for each successor
	// returns an array of unmodifiable sets
	private Set[] postSets(BasicBlock bb, BasicBlock succ) {
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
			Set[] post = (Set[]) bb2post.get(bb);
			Set mra = new HashSet(post[0]);
			for (int i = 0; i < sigma.numSigmas(); i++) {
			    // add only dst corresponding to
			    // arity and only if src is present
			    if (mra.remove(sigma.src(i)))
				mra.add(sigma.dst(i, arity));
			}
			// make unmodifiable
			return new Set[] { Collections.unmodifiableSet(mra),
					   post[1] };
		    }
		}
		// should never get here
		throw new Error("Cannot find arity of "+succ+" w.r.t. "+bb);
	    }
	    // otherwise...
	    return (Set[]) bb2post.get(bb);
	}

	// transfer function takes a Quad, the Set of
	// Temps containing the most-recently allocated
	// objects prior to the Quad, and modifies the
	// Set accordingly
	private void transfer(Quad q, Set[] pre) {
	    if (DEBUG) {
		System.out.println(q);
		System.out.print("  "+pre[0]);
		System.out.print(" except for "+pre[1]);
	    }
	    Set mra = pre[0];
	    Set except = pre[1];
	    int kind = q.kind();
	    if (kind == QuadKind.ANEW || kind == QuadKind.NEW) {
		// an ANEW or NEW creates a newly-allocated
		// object that supercedes all others
		mra.clear();
		mra.addAll(ud.defC(q));
		// no exceptions; we know we
		// just allocated an object
		except.clear();
	    } else if (kind == QuadKind.CALL) {
		// after a call, we can no longer be
		// certain of the most recently allocated
		// object, unless that method has been
		// pre-approved, in which case only the
		// return value and exception are toast
		// the "sigma" part of the call is
		// handled as part of the control flow
		CALL call = (CALL) q;
		if (safeMethods.containsKey(call.method())) {
		    Set classes = (Set) safeMethods.get(call.method());
		    Util.assert(classes != null);
		    // need to add exceptions
		    except.addAll(classes);
		    // note our defs
		    Temp retval = call.retval();
		    if (retval != null)
			mra.remove(retval);
		    Temp retex = call.retex();
		    if (retex != null)
			mra.remove(retex);
		} else {
		    // not safe, just clear
		    mra.clear();
		}
	    } else if (kind == QuadKind.MOVE) {
		// if the src is an mra, then the dst becomes
		// one, else the dst cannot be one
		MOVE move = (MOVE)q;
		if (mra.contains(move.src()))
		    mra.add(move.dst());
		else
		    mra.remove(move.dst());
	    } else if (kind == QuadKind.METHOD) {
		// parameters should not have any effect
		Util.assert(!mra.removeAll(ud.defC(q)));
		if (safeInitializers.contains(code.getMethod()))
		    mra.add(((METHOD)q).params(0));
	    } else if (kind == QuadKind.AGET || 
		       kind == QuadKind.ALENGTH ||
		       kind == QuadKind.COMPONENTOF ||
		       kind == QuadKind.CONST ||
		       kind == QuadKind.GET ||
		       kind == QuadKind.HANDLER ||
		       kind == QuadKind.INSTANCEOF ||
		       kind == QuadKind.OPER) {
		// when a Temp is re-defined, we can no 
		// longer be certain it points to the most 
		// recently-allocated object
		Util.assert(ud.defC(q).size() == 1);
		mra.removeAll(ud.defC(q));
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
		Util.assert(ud.defC(q).size() == 0);
	    } else {
		// LABELs, PHIs, SIGMAs, SWITCHs, TYPESWITCHs and XIs 
		// are not handled, but there should not be any
		throw new Error("Unknown QuadKind: "+q.kind()+" for "+q);
	    }
	    if (DEBUG) {
		System.out.print(" -> ");
		System.out.println(pre[0]+" except for "+pre[1]);
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
