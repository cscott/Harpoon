// MAInfo.java, created Mon Apr  3 18:17:57 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.

// NOTE: I eliminated lots of debug messages by commenting them with
//   "//B/" - I don't trust the ability of "javac" to eliminate the
//    unuseful "if(DEBUG) ..." stuff.
//       If you need this messages back replace "//B/" with "" (nothing).

package harpoon.Analysis.PointerAnalysis;


import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.ClassFile.Loader;
import harpoon.Analysis.Maps.AllocationInformation;

import harpoon.Analysis.Quads.Unreachable;

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.MetaMethods.MetaAllCallers;

import harpoon.Analysis.DefaultAllocationInformation;

import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.NOP;
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.QuadFactory;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import harpoon.Util.Util;
import harpoon.Util.WorkSet;


import harpoon.IR.Quads.QuadVisitor;
import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.SCCTopSortedGraph;


import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;


/**
 * <code>MAInfo</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: MAInfo.java,v 1.1.2.43 2000-12-07 00:43:56 salcianu Exp $
 */
public class MAInfo implements AllocationInformation, java.io.Serializable {

    private static boolean DEBUG = false;

    /** Enabless the application of some method inlining to increase the
	effectiveness of the stack allocation. Only inlinings that
	increase the effectiveness of the stack allocation are done.
	For the time being, only 1-level inlining is done. */
    public static boolean DO_METHOD_INLINING = false;

    /** Only methods that have less than <code>MAX_INLINING_SIZE</code>
	instructions can be inlined. Just a simple way of preventing
	the code bloat. */
    public static int MAX_INLINING_SIZE = 50; 

    /** Enables the use of preallocation: if an object will be accessed only
	by a thread (<i>ie</i> it is created just to pass some parameters
	to a thread), it can be preallocated into the heap of that thread.
	For the moment, it is potentially dangerous so it is deactivated by
	default. */
    public static boolean DO_PREALLOCATION = false;

    /** Forces the allocation of ALL the threads on the stack. Of course,
	dummy and unsafe. */
    public static boolean NO_TG = false;

    private static Set good_holes = null;
    static {
	good_holes = new HashSet();
	Linker linker = Loader.systemLinker;

	// "java.lang.Thread.currentThread()" is not harmful with regard to
	// the thread specific heaps stuff; add it to the set "good_holes"
	HClass hclass = linker.forName("java.lang.Thread");
	HMethod[] hms = hclass.getDeclaredMethods();
	for(int i = 0; i < hms.length; i++)
	    if("currentThread".equals(hms[i].getName()))
		good_holes.add(hms[i]);

	if(DEBUG)
	    System.out.println("GOOD HOLES: " + good_holes);
    }

    PointerAnalysis pa;
    HCodeFactory    hcf;
    // the meta-method we are interested in (only those that could be
    // started by the main or by one of the threads (transitively) started
    // by the main thread.
    Set             mms;
    NodeRepository  node_rep;
    MetaCallGraph   mcg;
    MetaAllCallers  mac;

    // use the inter-thread analysis
    private boolean USE_INTER_THREAD     = false;
    private boolean DO_STACK_ALLOCATION  = false;
    private boolean DO_THREAD_ALLOCATION = false;
    private boolean GEN_SYNC_FLAG        = false;


    /** Creates a <code>MAInfo</code>. */
    public MAInfo(PointerAnalysis pa, HCodeFactory hcf, Set mms,
		  boolean USE_INTER_THREAD,
		  boolean DO_STACK_ALLOCATION,
		  boolean DO_THREAD_ALLOCATION,
		  boolean GEN_SYNC_FLAG) {
        this.pa  = pa;
	this.mcg = pa.getMetaCallGraph();
	this.mac = pa.getMetaAllCallers();
	this.hcf = hcf;
	this.mms = mms;
	this.node_rep = pa.getNodeRepository();
	this.USE_INTER_THREAD = USE_INTER_THREAD;
        //this.DO_PREALLOCATION = USE_INTER_THREAD;
	this.DO_PREALLOCATION     = false;
	this.DO_STACK_ALLOCATION  = DO_STACK_ALLOCATION;
	this.DO_THREAD_ALLOCATION = DO_THREAD_ALLOCATION;
	this.GEN_SYNC_FLAG        = GEN_SYNC_FLAG;
	analyze();
	// the nullify part was moved to prepareForSerialization
    }

    /** Nullifies some stuff to make the serialization possible. 
	This method <b>MUST</b> be called before serializing <code>this</code>
	object. */
    public void prepareForSerialization(){
	this.pa  = null;
	this.hcf = null;
	this.mms = null;
	this.mcg = null;
	this.mac = null;
	this.node_rep = null;
    }

    // Map<NEW, AllocationProperties>
    private final Map aps = new HashMap();
    
    /** Returns the allocation policy for <code>allocationSite</code>. */
    public AllocationInformation.AllocationProperties query
	(HCodeElement allocationSite){
	
	AllocationInformation.AllocationProperties ap = 
	    (AllocationInformation.AllocationProperties)
	    aps.get(allocationSite);

	if(ap != null)
	    return ap;

	// conservative allocation property: on the global heap
	// (by default).
	return new MyAP(getAllocatedType(allocationSite));
    }

    // returns the AllocationProperties object for the object creation site q
    // if no such object exists, create a default one. 
    private MyAP getAPObj(Quad q){
	MyAP retval = (MyAP) aps.get(q);
	if(retval == null)
	    aps.put(q, retval = new MyAP(getAllocatedType(q)));
	return retval;
    }

    // Set the allocation policy for q to be ap. Discard whatever allocation
    // policy info was assigned to q before.
    private void setAPObj(Quad q, MyAP ap) {
	aps.put(q, ap);
    }

    // map to store the inline hints:
    //  CALL to be inlined -> array of (A)NEWs that can be stack allocated
    private Map ih = null;

    // analyze all the methods
    public void analyze(){
	if(DO_METHOD_INLINING)
	    ih = new HashMap();

	for(Iterator it = mms.iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
	    if(pa.analyzable(mm.getHMethod()))
		analyze_mm(mm);
	}

	if(DO_METHOD_INLINING) {
	    do_the_inlining(hcf, ih);
	    ih = null; // allow some GC
	}
    }


    // get the type of the object allocated by the object creation site hce;
    // hce should be NEW or ANEW.
    public HClass getAllocatedType(final HCodeElement hce){
	if(hce instanceof NEW)
	    return ((NEW) hce).hclass();
	if(hce instanceof ANEW)
	    return ((ANEW) hce).hclass();
	Util.assert(false,"Not a NEW or ANEW: " + hce);
	return null; // should never happen
    }



    //////////////////////////////////////////////////////////////////
    ///////// analyze_mm START ///////////////////////////////////////

    /* Analyze a single method: take the object creation sites from it
       and generate an allocation policy for each one. */
    private final void analyze_mm(MetaMethod mm){
	if(DEBUG)
	    System.out.println("\n\nMAInfo: Analyzed Meta-Method: " + mm);

	HCode hcode = hcf.convert(mm.getHMethod());
	((harpoon.IR.Quads.Code) hcode).setAllocationInformation(this);

	Set nodes = new HashSet();
	for(Iterator it = hcode.getElementsI(); it.hasNext(); ) {
	    Quad quad = (Quad) it.next();
	    if((quad instanceof NEW) ||
	       (quad instanceof ANEW))
		nodes.add(node_rep.getCodeNode(quad, PANode.INSIDE));
	}
	System.out.println("inside nodes " + mm.getHMethod() + " " + nodes);

	// Obtain a clone of the internal pig (no interthread analysis yet)
	// at the end of hm and "cosmetize" it a bit.
	ParIntGraph pig = (ParIntGraph) pa.getIntParIntGraph(mm).clone();
	if(pig == null) return; // strange things happen these days ...
	pig.G.flushCaches();
	pig.G.e.removeMethodHoles(good_holes);

	if(DEBUG)
	    System.out.println("Parallel Interaction Graph:" + pig);

	generate_aps(mm, pig, nodes);

	if(DO_PREALLOCATION)
	    try_prealloc(mm, hcode, pig);

	handle_tg_stuff(pig);

	if(DO_METHOD_INLINING)
	    generate_inlining_hints(mm, pig);
    }

    // Auxiliary method for analyze_mm.
    // INPUT: a metamethod mm and the pig at its end.
    // ACTION: goes over all the level 0 inside nodes from pig
    //       and try to stack allocate or thread allocate them.
    private void generate_aps(MetaMethod mm, ParIntGraph pig, Set nodes) {
	// we study only level 0 nodes (ie allocated in THIS method).
	// Set nodes = getLevel0InsideNodes(pig);

	if(DO_STACK_ALLOCATION) {
	    if(DEBUG) System.out.println("Stack allocation");
	    generate_aps_sa(mm, pig, nodes);
	}

	if(DO_THREAD_ALLOCATION) {
	    if(DEBUG) System.out.println("Thread allocation");
	    generate_aps_ta(mm, pig, nodes);
	}

	if(GEN_SYNC_FLAG) {
	    if(DEBUG) System.out.println("Generating sync flag");
	    generate_aps_ns(mm, pig, nodes);
	}
    }

    // aux method for generate_aps: generate stack allocation hints
    private void generate_aps_sa(MetaMethod mm, ParIntGraph pig, Set nodes) {
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    Quad q  = (Quad) node_rep.node2Code(node);
	    Util.assert(q != null, "No quad for " + node);
	    MyAP ap = getAPObj(q);
	    
	    if(pig.G.captured(node) && stack_alloc_extra_cond(node, q)) {
		ap.sa = true;
		if(DEBUG)
		    System.out.println("STACK: " + node + 
				       " was stack allocated " +
				       Debug.getLine(q));
	    }
	}
    }
    // aux method for generate_aps: generate thread allocation hints
    private void generate_aps_ta(MetaMethod mm, ParIntGraph pig, Set nodes) {
	HMethod hm = mm.getHMethod();	
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    Quad q  = (Quad) node_rep.node2Code(node);
	    Util.assert(q != null, "No quad for " + node);
	    MyAP ap = getAPObj(q);
	    
	    if(!ap.sa) {
		// the node is not stack allocated; maybe we can
		// thread allocate it
		if(remainInThread(node, hm, "")) {
		    ap.ta = true; // thread allocation
		    ap.ah = null; // on the current heap
		    if(DEBUG)
			System.out.println("THREAD: " + node +
					   " was thread allocated " +
					   Debug.getLine(q));
		}
	    }
	}
    }
    // aux method for generate_aps: generate "no syncs" hints
    private void generate_aps_ns(MetaMethod mm, ParIntGraph pig, Set nodes) {
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    Quad q  = (Quad) node_rep.node2Code(node);
	    MyAP ap = getAPObj((Quad) node_rep.node2Code(node));
	    if(ap.sa || ap.ta) 
		ap.ns = true; // trivial setting of ns
	    else { // the hard work ...
		ap.ns = noConflictingSyncs(node, mm);
		if(ap.ns)
		    System.out.println("BRAVO: " + node + " " +
				       Debug.code2str(q));
	    }
	}
    }

    // Returns the INSIDE nodes of level 0 from pig (that is supposed to
    // be attached to the end of some procedure): the set of all the
    // inside nodes that appear in pig and are not the specialization of any
    // other node (ie they are allocated in exactly that method and in the
    // current thread).
    private Set getLevel0InsideNodes(ParIntGraph pig) {
	final Set retval = new HashSet();
	pig.forAllNodes(new PANodeVisitor() {
		public void visit(PANode node) {
		    if((node.type == PANode.INSIDE) &&
		       !(node.isTSpec()) &&
		       (node.getCallChainDepth() == 0))
			retval.add(node);
		}
	    });
	retval.remove(ActionRepository.THIS_THREAD);
	return retval;
    }

    // Auxiliary method for analyze_mm.
    // Do some dummy things. Hopefully, this will be improved in the future.
    private void handle_tg_stuff(ParIntGraph pig) {
	/// DUMMY CODE: we don't have NSTK_malloc_with_heap yet
	if(!NO_TG)
	    set_make_heap(pig.tau.activeThreadSet());
	
	/// DUMMY CODE: Stack allocate ALL the threads
	if(NO_TG) {
	    Set set = getLevel0InsideNodes(pig);
	    for(Iterator it = set.iterator(); it.hasNext(); ) {
		PANode node = (PANode) it.next();
		Quad q = (Quad) node_rep.node2Code(node);
		if(q == null) {
		    System.out.println("BELL: " + node + " " + q);
		    continue;
		}
		if(thread_on_stack(node, q)) {
		    MyAP ap = getAPObj(q);
		    ap.sa = true;
		    ap.ta = false;
		}
	    }
	}
    }

    // Auxiliary method for analyze_mm.
    // Set the allocation policy info such that each of the threads allocated
    // and started into the currently analyzed method has a thread specific
    // heap associated with it.
    private void set_make_heap(Set threads) {
	for(Iterator it = threads.iterator(); it.hasNext(); ) {
	    PANode nt = (PANode) it.next();
	    if((nt.type != PANode.INSIDE) || 
	       (nt.getCallChainDepth() != 0) ||
	       (nt.isTSpec())) continue;

	    NEW qnt = (NEW) node_rep.node2Code(nt);
	    MyAP ap = getAPObj(qnt);
	    ap.mh = true;
	}
    }

    ///////// analyze_mm END /////////////////////////////////////////
    //////////////////////////////////////////////////////////////////



    /** Checks whether <code>node</code> escapes only in the caller:
	it is reached through a parameter or it is returned from the
	method but not lost due to some other reasons. */
    private boolean lostOnlyInCaller(PANode node, ParIntGraph pig){
	return
	    ! ( lostInAStatic(node, pig) ||
		lostInAMethodHole(node, pig) ||
		lostInAThread(node, pig) );
    }

    /** Checks whether <code>node</code> escapes through a static field. */
    private boolean lostInAStatic(PANode node, ParIntGraph pig) {
	for(Iterator it = pig.G.e.nodeHolesSet(node).iterator();it.hasNext();){
	    PANode nhole = (PANode)it.next();
	    if(nhole.type == PANode.STATIC)
		return true;
	}
	return false;
    }


    /** Checks whether <code>node</code> escapes into a method hole. */
    private boolean lostInAMethodHole(PANode node, ParIntGraph pig) {
	return pig.G.e.hasEscapedIntoAMethod(node);
    }


    /** Checks whether <code>node</code> escapes in the caller: it is reachable
	from a parameter or from one of the returned nodes (method result and
	exceptions). */
    private boolean lostInCaller(PANode node, ParIntGraph pig) {
	// 1. maybe node escapes through a parameter
	for(Iterator it = pig.G.e.nodeHolesSet(node).iterator();it.hasNext();){
	    PANode nhole = (PANode)it.next();
	    if(nhole.type == PANode.PARAM)
		return true;
	}
	// 2. maybe node escapes through a returned node
	return
	    pig.G.getReachableFromR().contains(node) ||
	    pig.G.getReachableFromExcp().contains(node);
    }


    /** Checks whether <code>node</code> escapes in a thread. */
    private boolean lostInAThread(PANode node, ParIntGraph pig) {
	// threads is the set of thread nodes from pig
	Set threads = pig.tau.activeThreadSet();
	// go over all the node holes node escapes into and
	// check if any of them corresponds to a thread.
	for(Iterator it = pig.G.e.nodeHolesSet(node).iterator();it.hasNext();){
	    PANode nhole = (PANode) it.next();
	    if(threads.contains(nhole))
		return true;
	}
	return false;
    }


    ////////////////////////////////////////////////////////////////////
    /////////////// remainInThread START ///////////////////////////////
    
    private static int MAX_LEVEL_BOTTOM_MODE = 10;
    private boolean remainInThreadBottom(PANode node, MetaMethod mm,
					 int level, String ident){
	if(node == null)
	    return true; // it was false before

	if(DEBUG)
	    System.out.println(ident + "remainInThreadBottom called for " + 
			       node + " mm = " + mm);

	ParIntGraph pig = pa.getIntParIntGraph(mm);
	
	if(pig.G.captured(node)){
	    if(DEBUG)
		System.out.println(ident + node+ " is captured -> true");
	    return true;
	}
	
	if(!lostOnlyInCaller(node, pig)){
	    if(DEBUG)
		System.out.println(ident + node +
				   " escapes somewhere else -> false");
	    return false;
	}

	if(level == MAX_LEVEL_BOTTOM_MODE){
	    if(DEBUG)
		System.out.println(ident + node + 
				   "max level reached -> false");
	    return false;
	}

	MetaMethod[] callers = mac.getCallers(mm);

	// This is a very, very delicate case: if there is no caller, it means
	// the currently analyzed method is either "main" or the run method of
	// some thread; the node might accessible from outside the current
	// thread => we conservatively return "false".
	if(callers.length == 0){
	    if(DEBUG)
		System.out.println(ident + node + "pours out of main/run");
	    return false;
	}

	for(int i = 0; i < callers.length; i++){
	    if(!remainInThreadBottom(node.getBottom(), callers[i], level+1,
				     ident + " ")){
		if(DEBUG)
		    System.out.println(ident + node + " -> false");
		return false;
	    }
	}
	
	if(DEBUG)
	    System.out.println(ident + node +
			       " remains in the current thread");
	return true;
    }


    // Checks whether node defined into hm, remains into the current
    // thread even if it escapes from the method which defines it.
    private boolean remainInThread(PANode node, HMethod hm, String ident){
	if(DEBUG)
	    System.out.println(ident + "remainInThread called for " +
			       node + "  hm = " + hm);

	if(node.getCallChainDepth() == PointerAnalysis.MAX_SPEC_DEPTH){
	    System.out.println(ident + node + " is too old -> might escape");
	    return false;
	}

	MetaMethod mm = new MetaMethod(hm, true);
	ParIntGraph pig = pa.getIntParIntGraph(mm);

	Util.assert(pig != null, "pig is null for hm = " + hm + " " + mm);
	
	if(pig.G.captured(node)){
	    if(DEBUG)
		System.out.println(ident + node+ " is captured -> true");
	    return true;
	}
	
	if(!lostOnlyInCaller(node, pig)) {
	    if(DEBUG)
		System.out.println(ident + node +
					 " escapes somewhere else -> false");
	    return false;
	}

	if(node.getCallChainDepth() == PointerAnalysis.MAX_SPEC_DEPTH - 1){
	    if(DEBUG)
		System.out.println(ident + node + 
				   " is almost too old and uncaptured -> " + 
				   "bottom mode");
	    boolean retval = remainInThreadBottom(node, mm, 0, ident);
	    if(DEBUG)
		System.out.println(ident + node + " " + retval);
	    return retval;
	}
	
	for(Iterator it = node.getAllCSSpecs().iterator(); it.hasNext(); ){
	    Map.Entry entry = (Map.Entry) it.next();
	    CALL   call = (CALL) entry.getKey();
	    PANode spec = (PANode) entry.getValue();
	    
	    QuadFactory qf = call.getFactory();
	    HMethod hm_caller = qf.getMethod();

	    if(!remainInThread(spec, hm_caller, ident + " ")){
		if(DEBUG)
		    System.out.println(ident + node +
				       " might escape -> false");
		return false;
	    }
	}

	if(DEBUG)
	    System.out.println(ident + node + 
			       " remains in thread -> true");

	return true;
    }

    /////////////// remainInThread END /////////////////////////////////
    ////////////////////////////////////////////////////////////////////

    
    ///////////////////////////////////////////////////////////////////////
    /////////////// noConflictingSyncs  START /////////////////////////////
    
    // Checks whether the synchronizations done on node are useless or not.
    private boolean noConflictingSyncs(PANode node, MetaMethod mm) {
	return noConflictingSyncs(node, mm, 0, "");
    }
    
    // Set up a limit for the length of the reverse call paths we explore
    private static final int MAX_LEVEL_NO_CONCURRENT_SYNCS = 
	PointerAnalysis.MAX_SPEC_DEPTH + 10;
	
    // Does the real job of noConflictingSyncs(node, mm); the algorithm should
    // be quite easy from the comments I've put into the code.
    private boolean noConflictingSyncs(PANode node, MetaMethod mm,
				       int level, String ident) {
	if(DEBUG)
	    System.out.print("noConflictingSyncs \n" +
			     "\tnode  = " + node +
			     "\tcreated at " + 
			     Debug.code2str
			     (node_rep.node2Code
			      (node != null ? node.getRoot() : null)) + "\n" +
			     "\tmm    = " + mm + "\n" +
			     "\tlevel = " + level + "\n");

	// Catch some strange case when a node is reported as escaping in
	// the graph of the callee but it's absent from the caller's graph
	// (which means it's not really escaping)
	if(node == null) return true;

	if(level > MAInfo.MAX_LEVEL_NO_CONCURRENT_SYNCS)
	    return false;

	ParIntGraph pig = pa.getIntParIntGraph(mm);

	// Case 1: node is captured in mm -> true
	if(pig.G.captured(node))
	    return true;

	// Case 2: node escapes into the entire program -> false
	if(lostInAStatic(node, pig) || lostInAMethodHole(node, pig))
	    return false;

	// Case 3: node escapes into the caller(s) -> recursively call
	// the function on each of the callers and compute a big AND.
	if(lostInCaller(node, pig)) {
	    MetaMethod[] callers = mac.getCallers(mm);
	    // This is a very, very delicate case: if there is no caller,
	    // it means the currently analyzed method is either "main" or
	    // the run method of some thread; the node might be accessible
	    // from outside the current thread => we conservatively return
	    // "false".
	    if(callers.length == 0)
		return false;

	    // Case 3.1: we are still far from the bottom ie we have precise
	    // information about the specializations of node and their
	    // corresponding CALL sites.
	    if(node.getCallChainDepth() < PointerAnalysis.MAX_SPEC_DEPTH - 1) {
		for(Iterator it = node.getAllCSSpecs().iterator();
		    it.hasNext(); ) {
		    Map.Entry entry = (Map.Entry) it.next();
		    CALL   call = (CALL) entry.getKey();
		    PANode spec = (PANode) entry.getValue();
		    
		    MetaMethod mm_caller = 
			new MetaMethod(call.getFactory().getMethod(), true);
		    
		    if(!noConflictingSyncs(spec, mm_caller,
					   level + 1, ident + " "))
			return false;
		}
		return true;
	    }

	    // Case 3.2: the bottom was/is about to be reached; we have to
	    // go and inspect all the callers of mm.
	    for(int i = 0; i < callers.length; i++) {
		if(!noConflictingSyncs(node.getBottom(), callers[i], level+1,
				       ident + " "))
		    return false;
	    }
	    return true;
	}

	// Case 4: node doesn't escape into a static, a method hole or the
	// caller, so it must escape only into one/more thread(s).
	if(!USE_INTER_THREAD)
	    return false;
	ParIntGraph pig_t = pa.threadInteraction(mm);
	// if node is not captured in pig_t, we give up (it means that the
	// thread that accesses might put it in some static, method hole etc.
	if(!pig_t.G.captured(node))
	    return false;
	// otherwise, we see if there are any conflicting syncs on node
	return noConcurrentSyncs(node, pig_t);
    }

    // auxiliary method for noConflictingSyncs
    // Checks whether the syncs on node that appear in pig are
    // nonconflicting (ie cannot occur at the same time) or not.
    private boolean noConcurrentSyncs(PANode node, ParIntGraph pig) {
	System.out.print("noConcurrentSyncs \n" +
			 "\tnode  = " + node +
			 "\tcreated at " + 
			 Debug.code2str
			 (node_rep.node2Code
			  (node != null ? node.getRoot() : null)) + "\n");
	return pig.ar.independent(node);
    }

    /////////////// noConflictingSyncs  END / /////////////////////////////
    ///////////////////////////////////////////////////////////////////////

    
    /** Checks some additional conditions for the stack allocation of the
	objects created at Quad q (something else than just captured(node)

	For the time being, we do just a minor hack to save our experimental
	results: the Thread objects that are captured (e.g. a Thread object
	that is created but never started and remains captured into its
	creating method) should NOT be stack allocated. The constructor
	of Thread does a very nasty thing: it puts a reference to the
	newly created Thread object into the ThreadGroup of the current
	Thread. Normally, this means that any thread escapes into the
	currentThread native method but we did some special tricks for this
	case to ignore it (so that we can allocate a Thread object in its
	own thread specific heap).

	Normally, some other conditions should be tested too: e.g. do not
	stack allocate objects that are too big etc. */
    private boolean stack_alloc_extra_cond(PANode node, Quad q) {
	// a hack for the Thread objects ...
	HClass hclass = getAllocatedType(q);
	if(java_lang_Thread.isSuperclassOf(hclass)){
	    //if(DEBUG)
		System.out.println(node + " allocated in " + q + 
				   " could be a thread -> NOT stack alloc");
	    return false;
	}
	// ... and nothing else
	return true;
    }
    private static HClass java_lang_Thread = null;
    static {
	Linker linker = Loader.systemLinker;
	java_lang_Thread = linker.forName("java.lang.Thread");
    }


    private boolean thread_on_stack(PANode node, Quad q) {
	HClass hclass = getAllocatedType(q);
	if(java_lang_Thread.isSuperclassOf(hclass)) {
	    // if(DEBUG)
		System.out.println(Debug.code2str(q) + " Thread on Stack");
	    return true;
	}
	return false;
    }



    /** Pretty printer for debug. */
    public void print() {
	System.out.println("ALLOCATION POLLICIES:");
	for(Iterator it = aps.keySet().iterator(); it.hasNext(); ){
	    Quad newq = (Quad) it.next();
	    MyAP ap   = (MyAP) aps.get(newq);
	    HMethod hm = newq.getFactory().getMethod();
	    HClass hclass = hm.getDeclaringClass();
	    PANode node = node_rep.getCodeNode(newq, PANode.INSIDE, false);
	    
	    System.out.println(hclass.getPackage() + "." + 
			       newq.getSourceFile() + ":" +
			       newq.getLineNumber() + " " +
			       newq + "(" + node + ") (" + 
			       hm + ") \t -> " + ap); 
	}
	System.out.println("====================");
    }

    
    /////////////////////////////////////////////////////////////////////
    //////////// try_prealloc START /////////////////////////////////////
    // PERSONAL ADVICE: This is buggy, incomplete and dummy code.
    //   Try to stay away from it. 

    // hope that this evil string really doesn't exist anywhere else
    private static String my_scope = "pa!";
    private static final TempFactory 
	temp_factory = Temp.tempFactory(my_scope);

    // try to apply some aggressive preallocation into the thread specific
    // heap.
    private void try_prealloc(MetaMethod mm, HCode hcode, ParIntGraph pig) {
	// not very clear if we really need to clone it but it's safer
	PAThreadMap tau = (PAThreadMap) pig.tau.clone();

	if(tau.activeThreadSet().size() != 1) return;

	Set active_threads = tau.activeThreadSet();
	PANode nt = (PANode) (active_threads.iterator().next());

	// protect against some patological cases
	if((nt.type != PANode.INSIDE) || 
	   (nt.getCallChainDepth() != 0) ||
	   (nt.isTSpec()) ||
	   (tau.getValue(nt) != 1))
	    return;

	// pray that no thread is allocated through an ANEW!
	// (it seems quite a reasonable assumption)
	NEW qnt = (NEW) node_rep.node2Code(nt);

	// compute the nodes pointed to by the thread node at the moment of 
	// the "start()" call. Since we analyze only "good" programs (we
	// have to produce a paper, don't we?) we know that the start() is
	// the last operation in the method so we just take the info from
	// the graph at the end of the method.
	Set pointed = pig.G.I.pointedNodes(nt);

	////////
	//if(DEBUG)
	//System.out.println("Pointed = " + pointed);

	// retain in "pointed" only the nodes allocated in this method, 
	// and which escaped only through the thread nt.
	for(Iterator it = pointed.iterator(); it.hasNext(); ){
	    PANode node = (PANode) it.next();
	    if( (node.type != PANode.INSIDE) ||
		(node.getCallChainDepth() != 0) ||
		!escapes_only_in_thread(node, nt, pig) ){
		/// System.out.println(node + " escapes somewhere else too");
		it.remove();
	    }
 	}

	////////
	///if(DEBUG)
	//System.out.println("Good Pointed = " + pointed);

	// grab into "news" the set of the NEW/ANEW quads allocating objects
	// that should be put into the heap of "nt".
	Set news = new HashSet();
	for(Iterator it = pointed.iterator(); it.hasNext(); ){
	    PANode node = (PANode) it.next();
	    news.add(node_rep.node2Code(node));
	}
	
	if(news.isEmpty()){
	    // specially treat this simple case:
	    // just allocate the thread node nt on its own heap
	    MyAP ap = getAPObj(qnt);
	    ap.ta  = true; // allocate on thread specific heap
	    ap.ns  = true; // SYNC
	    ap.mh  = true;  // makeHeap
	    // ap.ah = qnt.dst(); // use own heap
	    return;
	}
	
	// this NEW no longer exists
	aps.remove(qnt);

	Temp l2 = new Temp(temp_factory);
	QuadFactory qf = qnt.getFactory();
	MOVE moveq = new MOVE(qf, null, qnt.dst(), l2);
	NEW  newq  = new  NEW(qf, null, l2, qnt.hclass());

	// insert the MOVE instead of the original allocation
	Quad.replace(qnt, moveq);
	// insert the new NEW quad right after the METHOD quad
	// (at the very top of the method)
	insert_newq((METHOD) (((Quad)hcode.getRootElement()).next(1)), newq);

	// since the object creation site for the thread node has been changed,
	// we need to update the node2code relation.
	node_rep.updateNode2Code(nt, newq);

	// the thread object should be allocated in its own
	// thread specific heap.
	MyAP newq_ap = getAPObj(newq);

	newq_ap.ta = true;       // thread allocation
	newq_ap.ns = true; // SYNC
	newq_ap.mh = true;       // makeHeap for the thread object
	// newq_ap.ah = newq.dst(); // use own heap
	HClass hclass = getAllocatedType(newq);
	newq_ap.hip = 
	    DefaultAllocationInformation.hasInteriorPointers(hclass);
	
	// the objects pointed by the thread node and which don't escape
	// anywhere else are allocated on the heap of the thread node
	for(Iterator it = news.iterator(); it.hasNext(); ){
	    Quad cnewq = (Quad) it.next();
	    MyAP cnewq_ap = getAPObj(cnewq);
	    cnewq_ap.ta = true;
	    cnewq_ap.ns = true; // SYNC
	    cnewq_ap.ah = l2;
	}

	/* //B/ */
	/*
	if(DEBUG){
	    System.out.println("After the preallocation transformation:");
	    hcode.print(new java.io.PrintWriter(System.out, true));
	    System.out.println("Thread specific NEW:");
	    for(Iterator it = news.iterator(); it.hasNext(); ){
		Quad new_site = (Quad) it.next();
		System.out.println(new_site.getSourceFile() + ":" + 
				   new_site.getLineNumber() + " " + 
				   new_site);
	    }
	}
	*/
    }

    // checks whether "node" escapes only through the thread node "nt".
    private boolean escapes_only_in_thread(PANode node, PANode nt,
					   ParIntGraph pig){
	if(pig.G.e.hasEscapedIntoAMethod(node)){
	    if(DEBUG)
		System.out.println(node + " escapes into a method");
	    return false;
	}
	if(pig.G.getReachableFromR().contains(node)) {
	    if(DEBUG)
		System.out.println(node + " is reachable from R");
	    return false;
	}
	if(pig.G.getReachableFromExcp().contains(node)) {
	    if(DEBUG)
		System.out.println(node + " is reachable from Excp");
	    return false;
	}
	return true;
    }

    private void insert_newq(METHOD method, NEW newq){
	Util.assert(method.nextLength() == 1,
		    "A METHOD quad should have exactly one successor!");
	Edge nextedge = method.nextEdge(0);
	Quad nextquad = method.next(0);
	Quad.addEdge(method, nextedge.which_succ(), newq, 0);
	Quad.addEdge(newq, 0, nextquad, nextedge.which_pred());
    }

    //////////// try_prealloc END ///////////////////////////////////////
    /////////////////////////////////////////////////////////////////////



    /////////////////////////////////////////////////////////////////////
    //////////// INLINING STUFF START ///////////////////////////////////

    private void generate_inlining_hints(MetaMethod mm, ParIntGraph pig){
	HMethod hm  = mm.getHMethod();
	HCode hcode = hcf.convert(hm);
	if(hcode.getElementsL().size() > MAX_INLINING_SIZE) return;

	// obtain in A the set of nodes that might be captured after inlining 
	Set level0 = getLevel0InsideNodes(pig);
	Set A = new HashSet();
	for(Iterator it = level0.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(!pig.G.captured(node) && lostOnlyInCaller(node, pig)) {
		// we are not interested in stack allocating the exceptions
		// since they  don't appear in normal case and so, they
		// are not critical for the memory management
		HClass hclass = getAllocatedType(node_rep.node2Code(node));
		if(!java_lang_Throwable.isSuperclassOf(hclass))
		    A.add(node);
	    }
	}

	if(A.isEmpty()) return;

	// very dummy 1-level inlining
	MetaMethod[] callers = mac.getCallers(mm);
	for(int i = 0; i < callers.length; i++) {
	    MetaMethod mcaller = callers[i];
	    HMethod hcaller = mcaller.getHMethod();
	    for(Iterator it = mcg.getCallSites(mcaller).iterator();
		it.hasNext(); ) {
		CALL cs = (CALL) it.next();
		MetaMethod[] callees = mcg.getCallees(mcaller, cs);
		if((callees.length == 1) && (callees[0] == mm) && good_cs(cs))
		    try_inlining(mcaller, cs, A);
	    }
	}
    }
    private static HClass java_lang_Throwable = 
	Loader.systemLinker.forName("java.lang.Throwable");

    /* Normally, we should refuse to inline calls that are inside loops
       because that + stack allocation might lead to stack overflow errors.
       However, at this moment we don't test this condition. */
    private boolean good_cs(CALL cs){
	return true;
    }

    private void try_inlining(MetaMethod mcaller, CALL cs, Set A) {
	ParIntGraph caller_pig = pa.getIntParIntGraph(mcaller);

	Set B = new HashSet();
	for(Iterator it = A.iterator(); it.hasNext(); ){
	    PANode node = (PANode) it.next();
	    PANode spec = node.csSpecialize(cs);
	    if(spec == null) continue;
	    if(caller_pig.G.captured(spec))
		B.add(spec);
	}

	// no stack allocation benefits from this inlining
	if(B.isEmpty()) return;

	Set news = new HashSet();
	for(Iterator it = B.iterator(); it.hasNext(); ) {
	    PANode node = ((PANode) it.next()).getRoot();
	    Quad q = (Quad) node_rep.node2Code(node);
	    Util.assert((q != null) && 
			((q instanceof NEW) || (q instanceof ANEW)),
			" Bad quad attached to " + node + " " + q);
	    news.add(q);
	}

	Quad[] news_array = (Quad[]) news.toArray(new Quad[news.size()]);
	ih.put(cs, news_array);

	if(DEBUG) {
	    System.out.println("\nINLINING HINT: " + Debug.code2str(cs));
	    System.out.println("NEW STACK ALLOCATION SITES:");
	    for(int i = 0; i < news_array.length; i++)
		System.out.println(" " + Debug.code2str(news_array[i]));
	}
    }


    private void do_the_inlining(HCodeFactory hcf, Map ih){
	SCComponent scc = reverse_top_sort_of_cs(ih);
	Set toPrune=new WorkSet();
	while(scc != null) {
	    if(DEBUG) {
		System.out.println("Processed SCC:{");
		for(Iterator it= scc.nodes(); it.hasNext(); )
		    System.out.println(" " + Debug.code2str((CALL) it.next()));
		System.out.println("}");
	    }
	    for(Iterator it = scc.nodes(); it.hasNext(); ) {
		CALL cs=(CALL) it.next();
		inline_call_site(cs, hcf, ih);
		toPrune.add(cs.getFactory().getParent());
	    }
	    scc = scc.prevTopSort();
	}
	for(Iterator pit=toPrune.iterator();pit.hasNext();)
	    Unreachable.prune((HCode)pit.next());
    }


    private SCComponent reverse_top_sort_of_cs(Map ih) {
	final Relation m2csINm = new LightRelation();
	final Relation m2csTOm = new LightRelation();
	for(Iterator it = ih.keySet().iterator(); it.hasNext(); ) {
	    CALL cs = (CALL) it.next();
	    m2csINm.add(quad2method(cs), cs);
	    m2csTOm.add(cs.method(), cs);
	}

	final SCComponent.Navigator nav = new SCComponent.Navigator() {
		public Object[] next(final Object node) {
		    Set set = m2csINm.getValues(node);
		    return set.toArray(new Object[set.size()]);
		}
		public Object[] prev(final Object node) {
		    Set set = m2csTOm.getValues(node);
		    return set.toArray(new Object[set.size()]);
		}
	    };

	Set cs_set = new HashSet(ih.keySet());
	
	SCCTopSortedGraph sccts =
	    SCCTopSortedGraph.topSort(SCComponent.buildSCC(cs_set, nav));

	return sccts.getLast();
    }


    // given a quad q, returns the method q is part of 
    private final HMethod quad2method(Quad q) {
	return q.getFactory().getMethod();
    }


    private void inline_call_site(CALL cs, HCodeFactory hcf, Map ih) {
	System.out.println("INLINING " + Debug.code2str(cs));
	
	HMethod caller = quad2method(cs);
	
	System.out.println("caller = " + caller);

	Map old2new = new HashMap();

	HEADER header_new = null;
	try{
	    header_new = get_cloned_code(cs, caller, old2new, hcf);
	} catch(CloneNotSupportedException excp) { return; }

	METHOD qm = (METHOD) (header_new.next(1));

	// add the code for the parameter passing
	add_entry_sequence(cs, qm);

	modify_return_and_throw(cs, header_new);

	translate_ap(old2new);
	
	extra_stack_allocation(cs, ih, old2new);
    }


    private void extra_stack_allocation(CALL cs, Map ih, Map old2new) {
	Quad[] news = (Quad[]) ih.get(cs);
	for(int i = 0; i < news.length; i++) {
	    Quad q  = (Quad) old2new.get(news[i]);
	    Util.assert(q != null, "no new Quad for " + news[i]);
	    MyAP ap = new MyAP(getAllocatedType(q));
	    ap.sa = true;
	    ap.ns = true; // SYNC
	    setAPObj(q, ap);
	}
    }
    

    private void add_entry_sequence(CALL cs, METHOD qm) {
	// TODO: put something better than null in the 2nd argument
	Quad replace_cs = new NOP(cs.getFactory(), null);

	move_pred_edges(cs, replace_cs);

	Util.assert(cs.paramsLength() == qm.paramsLength(),
		    " different nb. of parameters between CALL and METHOD");
	
	Quad previous = replace_cs;

	int nb_params = cs.paramsLength();
	for(int i = 0; i < nb_params; i++) {
	    Temp formal = qm.params(i);
	    Temp actual = cs.params(i);
	    // emulate the Java parameter passing semantics
	    MOVE move = new MOVE(cs.getFactory(), null, formal, actual);
	    Quad.addEdge(previous, 0, move, 0);
	    previous = move;
	}

	// the edge pointing to the first instruction of the method body
	Edge edge = qm.nextEdge(0);
	Quad.addEdge(previous, 0, (Quad) edge.toCFG(), edge.which_pred());
    }

    
    private void modify_return_and_throw(final CALL cs, final HEADER header) {
	class QVisitor extends  QuadVisitor {
	    Set returnset;
	    Set throwset;
	    QVisitor() {
		returnset=new WorkSet();
		throwset=new WorkSet();
	    }

	    public void finish() {
		PHI returnphi=new PHI(cs.getFactory(),null, new Temp[0],
				      returnset.size());
		int edge=0;
		for(Iterator returnit=returnset.iterator();returnit.hasNext();)
		    Quad.addEdge((Quad)returnit.next(), 0, returnphi, edge++);
		
		Quad.addEdge(returnphi, 0, cs.next(0),
			     cs.nextEdge(0).which_pred());

		PHI throwphi=new PHI(cs.getFactory(),null, new Temp[0],
				     throwset.size());
		edge=0;
		for(Iterator throwit=throwset.iterator();throwit.hasNext();)
		    Quad.addEdge((Quad)throwit.next(), 0, throwphi, edge++);
		
		Quad.addEdge(throwphi, 0, cs.next(1),
			     cs.nextEdge(1).which_pred());
	    }

	    public void visit(Quad q) {
	    } 

	    public void visit(RETURN q) {
		Temp retVal = cs.retval(); 
		
		Quad replace;
		if(retVal != null)
		replace = new MOVE
		(cs.getFactory(), null, retVal, q.retval());
		else
		replace = new NOP(cs.getFactory(), null);
		
		// make the predecessors of q point to replace
		move_pred_edges(q, replace);
		
		// the only succesor of replace should now be the
		// 0-successor of the CALL instruction (normal return)
		returnset.add(replace);
	    }

	    public void visit(THROW q) {
		Temp retEx = cs.retex(); 
		
		Quad replace;
		if(retEx != null)
		replace = new MOVE
		(cs.getFactory(), null, retEx, q.throwable());
		else
		replace = new NOP(cs.getFactory(), null);
		
		// make the predecessors of q point to replace
		move_pred_edges(q, replace);
		
		// the only succesor of replace should now be the
		// 1-successor of the CALL instruction (exception return)
		throwset.add(replace);
	    }
	};
	QVisitor inlining_qv=new QVisitor();
	apply_qv_to_tree(header, inlining_qv);
	inlining_qv.finish();
    }


    private static void apply_qv_to_tree(Quad q, QuadVisitor qv) {
	recursive_apply_qv(q, qv, new HashSet());
    }

    private static void recursive_apply_qv(Quad q, QuadVisitor qv, Set seen) {
	if(!seen.add(q)) return; // q has already been seen
	
	q.accept(qv);
	
	int nb_next = q.nextLength();
	for(int i = 0; i < nb_next; i++)
	    recursive_apply_qv(q.next(i), qv, seen);
    }


    // For any predecessor pred of oldq, replace the arc pred->oldq
    // with old->newq. 
    private static void move_pred_edges(Quad oldq, Quad newq) {
	Edge[] edges = oldq.prevEdge();
	for(int i = 0; i < edges.length; i++) {
	    Edge e = edges[i];
	    Quad.addEdge((Quad) e.fromCFG(), e.which_succ(),
			 newq, e.which_pred());
	}
    }


    private void translate_ap(Map old2new) {
	for(Iterator it = old2new.entrySet().iterator(); it.hasNext(); ) {
	    Map.Entry entry = (Map.Entry) it.next();
	    Quad old_q = (Quad) entry.getKey();
	    Quad new_q = (Quad) entry.getValue();
	    setAPObj(new_q, (MyAP) (getAPObj(old_q).clone()));
	}
    }


    private HEADER get_cloned_code(CALL cs, HMethod caller,
				 Map old2new, HCodeFactory hcf)
	throws CloneNotSupportedException {

	// get the only method that is called by cs.
	MetaMethod[] mms = mcg.getCallees(new MetaMethod(caller, true), cs);
	Util.assert(mms.length == 1,
		    "Trying to inline a call site with " + mms.length +
		    " callees");
	HMethod hm = mms[0].getHMethod();

	HCode hcode_orig = hcf.convert(hm);

	System.out.println("caller = " + caller);

	HEADER header_orig = 
	    (HEADER) hcode_orig.getRootElement();
	HEADER header_new  = 
	    (HEADER) Quad.clone(cs.getFactory(), header_orig);

	fill_the_map(header_orig, header_new, old2new, new HashSet());

	return header_new;
    }


    // recursively explore two HCode's (the second is the clone of the first
    // one) and set up a mapping "old NEW/ANEW -> new NEW/ANEW
    private static void fill_the_map(Quad q1, Quad q2, Map map, Set seen) {
	// avoid entering infinite loops: return when we meet a previously
	// seen instruction 
	if(!seen.add(q1)) return;

	if((q1 instanceof NEW) || (q2 instanceof ANEW))
	    map.put(q1, q2);

	Quad[] next1 = q1.next();
	Quad[] next2 = q2.next();

	Util.assert(next1.length == next2.length,
		    " Possible error in HCode.clone()");

	for(int i = 0; i < next1.length; i++)
	    fill_the_map(next1[i], next2[i], map, seen);
    }

    //////////// INLINING STUFF END /////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
}

