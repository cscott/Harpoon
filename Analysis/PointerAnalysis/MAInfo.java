// MAInfo.java, created Mon Apr  3 18:17:57 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.

package harpoon.Analysis.PointerAnalysis;

import java.io.PrintWriter;
import java.io.Serializable;

import java.util.Arrays;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Collection;
import java.util.List;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Vector;
import java.util.Collections;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.CachingCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
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
import harpoon.IR.Quads.Code;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;

import harpoon.Util.LightBasicBlocks.CachingSCCLBBFactory;
import harpoon.Util.LightBasicBlocks.LightBasicBlock;

import harpoon.IR.Quads.QuadVisitor;
import harpoon.Util.Graphs.Navigator;
import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.SCCTopSortedGraph;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;


/**
 * <code>MAInfo</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: MAInfo.java,v 1.10 2003-04-30 21:26:16 salcianu Exp $
 */
public class MAInfo implements AllocationInformation, Serializable {

    /** Options for the <code>MAInfo</code> processing. */
    public static class MAInfoOptions implements Cloneable, Serializable {

	/** Controls the generation of stack allocation hints.
	    Default <code>false</code>. */
	public boolean DO_STACK_ALLOCATION  = false;

	/** Forbids stack allocation in a loop. Should be one of the
	    <code>STACK_ALLOCATE_*</code> constants. Default is
	    <code>STACK_ALLOCATE_NOT_IN_LOOPS</code>. */
	public int STACK_ALLOCATION_POLICY = STACK_ALLOCATE_NOT_IN_LOOPS;
	
	/** Don't stack allocate anything. */
	public static final int STACK_ALLOCATE_NOTHING      = 0;
	/** Don't stack allocate in loops. */
	public static final int STACK_ALLOCATE_NOT_IN_LOOPS = 1;
	/** Stack allocate everything that's captured. */
	public static final int STACK_ALLOCATE_ALWAYS       = 2;

	/** Return true if policy is STACK_ALLOCATE_NOT_IN_LOOPS. */
	public boolean stack_allocate_not_in_loops() {
	    return STACK_ALLOCATION_POLICY == STACK_ALLOCATE_NOT_IN_LOOPS;
	}
	
	/** Controls the generation of thread local heap allocation hints.
	    Default <code>false</code>. */
	public boolean DO_THREAD_ALLOCATION = false;

	/** Enables the application of some method inlining to
	    increase the effectiveness of the stack allocation. Only
	    inlinings that increase the effectiveness of the stack
	    allocation are done. Default <code>false</code>. */
	public boolean DO_INLINING_FOR_SA = false;

	/** Enables the application of some method inlining to
	    increase the effectiveness of the thread local heap
	    allocation. Default <code>false</code>. */
	public boolean DO_INLINING_FOR_TA = false;

	/** Checks whether any inlining is requested. */
	public boolean do_inlining() {
	    return DO_INLINING_FOR_SA || DO_INLINING_FOR_TA;
	}

	/** Enables the use of preallocation: if an object will be
	    accessed only by a thread (<i>ie</i> it is created just to
	    pass some parameters to a thread), it can be preallocated into
	    the heap of that thread.  For the moment, it is potentially
	    dangerous so it is deactivated by default.
	    Default <code>false</code>. */
	public boolean DO_PREALLOCATION    = false;

	/** Controls the detection of the objects for whom there are
            no concurrent synchronizations. This objects are marked with a
	    special flag to reduce the cost of synchronization operations.
	    Default <code>false</code>. */
	public boolean GEN_SYNC_FLAG        = false;

	/** Use the interthread analysis inside <code>MAInfo</code>.
	    If this analysis is too buggy or time/memory expensive,
	    you can disable it through this flag. <b>NOTE:</b> this
	    will also disable some of the optimizations (<i>eg</i> the
	    preallocation).
	    Default <code>false</code>. */
	public boolean USE_INTER_THREAD     = false;

	/** Use the old, 1-level inlining. Default is <code>false</code>
	    (ie the new, multilevel strategy is used). Might be useful
	    for people not willing to go into the bugs of the new strategy. */
	public boolean USE_OLD_INLINING     = false;

	/** The current implementation is able to inline call strings of
	    length bigger than one.
	    Default value is 2. */
	public int MAX_INLINING_LEVEL   = 2;

	/** The maximal size to which we can inflate the size of a method
	    through inlining.
	    Default is <code>1000</code> quads. */
	public int MAX_METHOD_SIZE      = 1000;

	/** The maximal size of a method that can be inlined.
	    Default is <code>100</code> quads. */
	public int MAX_INLINABLE_METHOD_SIZE = 100;

	/** Pretty printer. */
	public void print(String prefix) {
	    print_opt(prefix, "DO_STACK_ALLOCATION", DO_STACK_ALLOCATION);
	    if(DO_STACK_ALLOCATION) {
		System.out.print(prefix + "\tSTACK_ALLOCATION_POLICY = ");
		switch(STACK_ALLOCATION_POLICY) {
		case STACK_ALLOCATE_ALWAYS:
		    System.out.println("STACK_ALLOCATE_ALWAYS");
		    break;
		case STACK_ALLOCATE_NOT_IN_LOOPS:
		    System.out.println("STACK_ALLOCATE_NOT_IN_LOOPS");
		    break;
		default:
		    System.out.println("unknown -> fatal!");
		    System.exit(1);
		}
	    }
	    print_opt(prefix, "DO_THREAD_ALLOCATION", DO_THREAD_ALLOCATION);
	    print_opt(prefix, "DO_PREALLOCATION", DO_PREALLOCATION);
	    print_opt(prefix, "GEN_SYNC_FLAG", GEN_SYNC_FLAG);
	    print_opt(prefix, "USE_INTER_THREAD", USE_INTER_THREAD);
	    if(DO_INLINING_FOR_SA || DO_INLINING_FOR_TA) {
		print_opt(prefix, "DO_INLINING_FOR_SA", DO_INLINING_FOR_SA);
		print_opt(prefix, "DO_INLINING_FOR_TA", DO_INLINING_FOR_TA);
		if(USE_OLD_INLINING)
		    System.out.println
			(prefix + "\tUSE_OLD_INLINING (1-level)");
		else
		    System.out.println(prefix + "\tMAX_INLINING_LEVEL = " +
				       MAX_INLINING_LEVEL);
		System.out.println(prefix + "\tMAX_METHOD_SIZE = " +
				   MAX_METHOD_SIZE);
	    }
	}
	// Auxiliary method for the pretty printer.
	private void print_opt(String prefix, String flag_name, boolean flag) {
	    System.out.println(prefix + flag_name + " " +
			       (flag ? "on" : "off"));
	}

	public Object clone() {
	    try{
		return super.clone();
	    }
	    catch (CloneNotSupportedException e) { 
		throw new Error("Should never happen! " + e);
	    }
	}
    };


    
    private static boolean DEBUG = false;
    // Controls the "inlining chains" - related debug messages
    private final boolean DEBUG_IC = false;


    /** Forces the allocation of ALL the threads on the stack. Of course,
	dummy and unsafe. */
    public static boolean NO_TG = false;

    PointerAnalysis pa;
    HCodeFactory    hcf;
    Linker linker;
    // the meta-method we are interested in (only those that could be
    // started by the main or by one of the threads (transitively) started
    // by the main thread.
    Set             mms;
    NodeRepository  node_rep;
    MetaCallGraph   mcg;
    MetaAllCallers  mac;

    private MAInfoOptions opt = null;

    // the factory that generates the SCC LBB representation (ie
    // strongly connected components of the light basic blocks of
    // quads) for the code of a method.
    private CachingSCCLBBFactory caching_scc_lbb_factory = null;

    /** Creates a <code>MAInfo</code>. */
    public MAInfo(PointerAnalysis pa, HCodeFactory hcf,
		  Linker linker, Set mms,
		  final MAInfoOptions opt) {
	assert hcf instanceof CachingCodeFactory : "hcf should be a CachingCodeFactory!";
        this.pa  = pa;
	this.mcg = pa.getMetaCallGraph();
	this.mac = pa.getMetaAllCallers();
	this.caching_scc_lbb_factory = pa.getCachingSCCLBBFactory();
	this.hcf = hcf;
	this.linker = linker;
	this.mms = mms;
	this.node_rep = pa.getNodeRepository();

	this.opt = (MAInfoOptions) opt.clone();

	java_lang_Thread    = linker.forName("java.lang.Thread");
	java_lang_Throwable = linker.forName("java.lang.Throwable");
	init_good_holes();

	analyze();
	// the nullify part was moved to prepareForSerialization
    }

    /** Nullifies some stuff to make the serialization possible. 
	This method <b>MUST</b> be called before serializing <code>this</code>
	object. */
    public void prepareForSerialization() {
	this.pa  = null;
	this.hcf = null;
	this.mms = null;
	this.mcg = null;
	this.mac = null;
	this.node_rep = null;
    }



    private void init_good_holes() {
	good_holes = new HashSet();

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

    private Set good_holes = null;
    private HClass java_lang_Thread    = null;
    private HClass java_lang_Throwable = null;


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

	// conservative allocation property: in the global heap
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

    private static long time() {
	return System.currentTimeMillis();
    }

    // analyze all the methods
    public void analyze() {
	if(opt.do_inlining() ||
	   (opt.DO_STACK_ALLOCATION && opt.stack_allocate_not_in_loops()))
	    build_quad2scc();

	if(opt.do_inlining()) {
	    if(opt.USE_OLD_INLINING)
		ih = new HashMap();
	    else {
		chains = new LinkedList();
		hm2rang = get_hm2rang();
	    }
	}

	for(Iterator it = mms.iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
	    if(pa.analyzable(mm.getHMethod()))
		analyze_mm(mm);
	}
	
	if(opt.do_inlining()) {
	    if(opt.USE_OLD_INLINING) {
		do_the_inlining(hcf, ih);
		ih = null;
	    }
	    else {
		process_inlining_chains();
		chains = null;
		hm2rang = null;
	    }
	}
	quad2scc = null;
    }

    private Map/*<HMethod,Integer>*/ hm2rang;

    // compute a function rang : HMethods -> natural numbers such that
    // if m1 calls m2, rang(m1) >= rang(m2). Basically, the leafs of the call
    // graph will have the smallest rang; all the methods from the same
    // strongly connected component of the call graph have the same rang.
    private Map/*<HMethod,Integer>*/ get_hm2rang() {
	if(DEBUG_IC)
	    System.out.println("set_hm2rang");

	SCCTopSortedGraph mmethod_sccs = mcg.getTopDownSortedView();
	
	if(DEBUG_IC)
	    System.out.println("\n\nMethod rang:");

	Map/*<HMethod,Integer>*/hm2rang = new HashMap/*<HMethod,Integer>*/();
	int counter = 0;
	for(SCComponent scc = mmethod_sccs.getLast(); scc != null;
	    scc = scc.prevTopSort()) {
	    Object[] mms = scc.nodes();
	    for(int i = 0; i < mms.length; i++) {
		HMethod hm = ((MetaMethod) mms[i]).getHMethod();
		if(DEBUG_IC)
		    System.out.println(hm + " -> " + counter);
		hm2rang.put(hm, new Integer(counter));
	    }
	    counter++;
	}
	if(DEBUG_IC)
	    System.out.println("======================================");

	return hm2rang;
    }



    // get the type of the object allocated by the object creation site hce;
    // hce should be NEW or ANEW.
    public static HClass getAllocatedType(final HCodeElement hce){
	if(hce instanceof NEW)
	    return ((NEW) hce).hclass();
	if(hce instanceof ANEW)
	    return ((ANEW) hce).hclass();
	assert false : ("Not a NEW or ANEW: " + hce);
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
	((Code) hcode).setAllocationInformation(this);

	Set nodes = new HashSet();
	for(Iterator it = hcode.getElementsI(); it.hasNext(); ) {
	    Quad quad = (Quad) it.next();
	    if((quad instanceof NEW) ||
	       (quad instanceof ANEW))
		nodes.add(node_rep.getCodeNode(quad, PANode.INSIDE));
	}

	if(DEBUG)
	    System.out.println("inside nodes " + mm.getHMethod() + 
			       " " + nodes);

	// Obtain a clone of the internal pig (no interthread analysis yet)
	// at the end of hm and "cosmetize" it a bit.
	ParIntGraph pig = (ParIntGraph) pa.getIntParIntGraph(mm).clone();
	if(pig == null) return; // strange things happen these days ...
	pig.G.flushCaches();
	pig.G.e.removeMethodHoles(good_holes);

	if(DEBUG)
	    System.out.println("Parallel Interaction Graph:" + pig);

	generate_aps(mm, pig, nodes);

	if(opt.DO_PREALLOCATION)
	    try_prealloc(mm, hcode, pig);

	if(opt.DO_THREAD_ALLOCATION)
	    set_make_heap(pig.tau.activeThreadSet());

	// handle_tg_stuff(pig);

	if(opt.do_inlining()) {
	    if(opt.USE_OLD_INLINING)
		generate_inlining_hints(mm, pig);
	    else
		generate_inlining_chains(mm);
	}
    }

    // Auxiliary method for analyze_mm.
    // INPUT: a metamethod mm and the pig at its end.
    // ACTION: goes over all the level 0 inside nodes from pig
    //       and try to stack allocate or thread allocate them.
    private void generate_aps(MetaMethod mm, ParIntGraph pig, Set nodes) {
	// we study only level 0 nodes (ie allocated in THIS method).
	// Set nodes = getLevel0InsideNodes(pig);

	if(opt.DO_STACK_ALLOCATION) {
	    if(DEBUG) System.out.println("Stack allocation");
	    generate_aps_sa(mm, pig, nodes);
	}

	if(opt.DO_THREAD_ALLOCATION) {
	    if(DEBUG) System.out.println("Thread allocation");
	    generate_aps_ta(mm, pig, nodes);
	}

	if(opt.GEN_SYNC_FLAG) {
	    if(DEBUG) System.out.println("Generating sync flag");
	    generate_aps_ns(mm, pig, nodes);
	}
    }

    // aux method for generate_aps: generate stack allocation hints
    private void generate_aps_sa(MetaMethod mm, ParIntGraph pig, Set nodes) {
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    Quad q  = (Quad) node_rep.node2Code(node);
	    assert q != null : "No quad for " + node;
	    MyAP ap = getAPObj(q);
	    
	    if(pig.G.captured(node) && stack_alloc_extra_cond(node, q)) {
		ap.sa = true;
		if(DEBUG)
		    System.out.println("STACK: " + node + 
				       " was stack allocated " +
				       Util.getLine(q));
	    }
	}
    }

    // aux method for generate_aps: generate thread allocation hints
    private void generate_aps_ta(MetaMethod mm, ParIntGraph pig, Set nodes) {
	HMethod hm = mm.getHMethod();	
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    Quad q  = (Quad) node_rep.node2Code(node);
	    assert q != null : "No quad for " + node;
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
					   Util.getLine(q));
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
		if(ap.ns) {
		    System.out.println("BRAVO: " + node + " " +
				       Util.code2str(q));
		}
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
	//if(!NO_TG)
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
	    System.out.println("set_make_heap: " + Util.code2str(qnt));
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

	assert pig != null : "pig is null for hm = " + hm + " " + mm;
	
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
	    
	    HMethod hm_caller = quad2method(call);

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
			     Util.code2str
			     (node_rep.node2Code
			      (node != null ? node.getRoot() : null)) + "\n" +
			     "\tmm    = " + mm + "\n" +
			     "\tlevel = " + level + "\n");

	// Catch some strange case when a node is reported as escaping in
	// the graph of the callee but it's absent from the caller's graph
	// (which means it's not really escaping)

	// TODO: DEBUG THIS!
	if(node == null) return true;
	// assert node != null;

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

	    // Case 3.1: we are still far from the bottom ie we
	    // have precise information about the specializations
	    // of node and their corresponding CALL sites.
	    if(PointerAnalysis.CALL_CONTEXT_SENSITIVE &&
	       (node.getCallChainDepth() < 
		PointerAnalysis.MAX_SPEC_DEPTH - 1)) {

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
		PANode node2 = 
		    PointerAnalysis.CALL_CONTEXT_SENSITIVE ?
		    node.getBottom() : node;
		    
		if(!noConflictingSyncs(node2, callers[i], level + 1,
				       ident + " "))
		    return false;
	    }
	    return true;
	}

	// Case 4: node doesn't escape into a static, a method hole or the
	// caller, so it must escape only into one/more thread(s).

	// some conservative guards
	if(!(opt.USE_INTER_THREAD && PointerAnalysis.RECORD_ACTIONS))
	    return false;

	System.out.println("CHKPT 1: " + node + " mm = " + mm);

	// ParIntGraph pig_t = pa.threadInteraction(mm);
	ParIntGraph pig_t = pa.getIntThreadInteraction(mm);
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
			 Util.code2str
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
	// if the appropriate options is turned on in opt,
	// refuse to do stack allocation in a loop
	if(opt.stack_allocate_not_in_loops() && in_a_loop(q)) {
	    System.out.println("stack_alloc_extra_cond: " + Util.code2str(q) +
			       " is in a LOOP -> don't sa");
	    return false;
	}

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


    /** Checks whether Quad q is in a loop; this is equivalent to
	testing if it's in a strongly connected component of Quads. */
    private boolean in_a_loop(Quad q) {
	SCComponent scc = (SCComponent) quad2scc.get(q);
	return scc.isLoop();
    }

    // map quad -> strongly connected component
    private Map quad2scc = null;

    private void build_quad2scc() {
	long tstart = time();
	quad2scc = new HashMap();
	System.out.print("quad2scc construction ... ");
	for(Iterator it = mcg.getAllMetaMethods().iterator(); it.hasNext(); ) {
	    HMethod hm = ((MetaMethod) it.next()).getHMethod();
	    HCode hcode = hcf.convert(hm);
	    if(hcode != null)
		extend_quad2scc(hm);
	}
	System.out.println((time() - tstart) + " ms");
    }

    private void extend_quad2scc(HMethod hm) {
	SCCTopSortedGraph graph = 
	    caching_scc_lbb_factory.computeSCCLBB(hm);
	for(SCComponent scc = graph.getFirst(); scc != null;
	    scc = scc.nextTopSort()) {
	    Object nodes[] = scc.nodes();
	    for(int i = 0; i < nodes.length; i++) {
		LightBasicBlock lbb = (LightBasicBlock) nodes[i];
		HCodeElement[] hce = lbb.getElements();
		for(int j = 0; j < hce.length; j++)
		    quad2scc.put(hce[j], scc);
	    }
	}
    }


    private boolean thread_on_stack(PANode node, Quad q) {
	HClass hclass = getAllocatedType(q);
	if(java_lang_Thread.isSuperclassOf(hclass)) {
	    // if(DEBUG)
		System.out.println(Util.code2str(q) + " Thread on Stack");
	    return true;
	}
	return false;
    }



    /** Pretty printer for debug. */
    public void print() {
	System.out.println("\n(INTERESTING) ALLOCATION PROPERTIES:");
	for(Iterator it = aps.keySet().iterator(); it.hasNext(); ){
	    Quad newq = (Quad) it.next();
	    MyAP ap   = (MyAP) aps.get(newq);
	    HMethod hm = newq.getFactory().getMethod();
	    HClass hclass = hm.getDeclaringClass();
	    PANode node = node_rep.getCodeNode(newq, PANode.INSIDE, false);

	    // don't print uninteresting allocation properties
	    if(!(ap.canBeStackAllocated() || ap.canBeThreadAllocated() ||
		 ap.noSync())) continue;

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
	System.out.println("try_prealloc(" + mm.getHMethod() + ")");

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
	for(Iterator it = pointed.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if( (node.type != PANode.INSIDE) ||
		(node.getCallChainDepth() != 0) ||
		!escapes_only_in_thread(node, nt, pig) ) {
		/// System.out.println(node + " escapes somewhere else too");
		it.remove();
	    }
 	}

	Set to_prealloc = new HashSet(pointed);
	to_prealloc.add(nt);
	
	////////
	///if(DEBUG)
	//System.out.println("Good Pointed = " + pointed);

	// grab into "news" the set of the NEW/ANEW quads allocating objects
	// that should be put into the heap of "nt".
	Set news = new HashSet();
	for(Iterator it = pointed.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    news.add(node_rep.node2Code(node));
	}
	
	if(news.isEmpty()) {
	    System.out.println("preallocation: " + qnt);

	    // specially treat this simple case:
	    // just allocate the thread node nt on its own heap
	    MyAP ap = getAPObj(qnt);
	    ap.ta  = true; // allocate on thread specific heap
	    ap.ns  = true; // SYNC
	    ap.mh  = true; // makeHeap
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

	newq_ap.ta = true;  // thread allocation
	newq_ap.ns = true;  // SYNC
	newq_ap.mh = true;  // makeHeap for the thread object
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

	//	if(DEBUG){
	    System.out.println("After the preallocation transformation:");
	    hcode.print(new java.io.PrintWriter(System.out, true));
	    System.out.println("Thread specific NEW:");
	    for(Iterator it = news.iterator(); it.hasNext(); ){
		Quad new_site = (Quad) it.next();
		System.out.println(new_site.getSourceFile() + ":" + 
				   new_site.getLineNumber() + " " + 
				   new_site);
	    }
	    //}
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
	assert method.nextLength() == 1 : "A METHOD quad should have exactly one successor!";
	Edge nextedge = method.nextEdge(0);
	Quad nextquad = method.next(0);
	Quad.addEdge(method, nextedge.which_succ(), newq, 0);
	Quad.addEdge(newq, 0, nextquad, nextedge.which_pred());
    }

    //////////// try_prealloc END ///////////////////////////////////////
    /////////////////////////////////////////////////////////////////////



    /////////////////////////////////////////////////////////////////////
    //////////// INLINING STUFF START ///////////////////////////////////

    //////////// A. SIMPLE (1 LEVEL) INLINING START /////////////////////
    
    /** Only methods that have less than <code>MAX_INLINING_SIZE</code>
	instructions can be inlined. Just a simple way of preventing
	the code bloat. */
    private int MAX_INLINING_SIZE = 50; 

    private void generate_inlining_hints(MetaMethod mm, ParIntGraph pig) {
	HMethod hm  = mm.getHMethod();
	HCode hcode = hcf.convert(hm);
	if(hcode.getElementsL().size() > MAX_INLINING_SIZE) return;

	// obtain in A the set of nodes that might be captured after inlining 
	Set A = getInterestingLevel0InsideNodes(mm.getHMethod(), pig);
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

    private void try_inlining(MetaMethod mcaller, CALL cs, Set A) {
	// refuse to stack allocate stuff in a loop
	// TODO: this stuff can still be allocated in the thread local heap
	if(opt.stack_allocate_not_in_loops() && in_a_loop(cs)) {
	    System.out.println("try_inlining: " + Util.code2str(cs) +
			       " is in a loop -> don't sa");
	    return;
	}

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
	    assert (q != null) && 
			((q instanceof NEW) || (q instanceof ANEW)) : " Bad quad attached to " + node + " " + q;
	    if(!(opt.stack_allocate_not_in_loops() && in_a_loop(q)))
		news.add(q);
	    else
		System.out.println("try_inlining: " + Util.code2str(q) + 
				   " is in a loop -> don't sa");
	}

	// no stack allocation benefits from this inlining
	if(news.isEmpty()) return;

	Quad[] news_array = (Quad[]) news.toArray(new Quad[news.size()]);
	ih.put(cs, news_array);

	if(DEBUG) {
	    System.out.println("\nINLINING HINT: " + Util.code2str(cs));
	    System.out.println("NEW STACK ALLOCATION SITES:");
	    for(int i = 0; i < news_array.length; i++)
		System.out.println(" " + Util.code2str(news_array[i]));
	}
    }


    private void do_the_inlining(HCodeFactory hcf, Map ih) {
	SCComponent scc = reverse_top_sort_of_cs(ih);
	Set toPrune = new WorkSet();
	while(scc != null) {
	    if(DEBUG) {
		System.out.println("Processed SCC:{");
		Object[] calls = scc.nodes();
		for(int i = 0; i < calls.length; i++)
		    System.out.println(" " + Util.code2str((CALL) calls[i]));
		System.out.println("}");
	    }
	    Object[] calls = scc.nodes();
	    for(int i = 0; i < calls.length; i++) {
		CALL cs = (CALL) calls[i];
		HMethod hcaller = extract_caller(cs);
		HMethod hcallee = extract_callee(cs);
		Map old2new = inline_call_site(cs, hcaller, hcallee, hcf);
		if(old2new != null) {
		    Quad[] news = (Quad[]) ih.get(cs);
		    extra_stack_allocation(news, old2new);
		    toPrune.add(cs.getFactory().getParent());
		}
	    }
	    scc = scc.prevTopSort();
	}
	for(Iterator pit = toPrune.iterator(); pit.hasNext(); )
	    Unreachable.prune((Code) pit.next());
    }


    // Topologically sorts the to-be-inlined call sites such that if
    // call cs1 calls method m and inside m, cs2 calls some method m2,
    // c1 --> cs2. What's returned is a list of strongly connected
    // components starting with the last one (in topological order).
    // To process the call sites, thee caller of this method should
    // follow the prevTopSort field / method.
    private SCComponent reverse_top_sort_of_cs(Map ih) {
	// The following two relations are useful in computing the
	// successors and the predecesssors or a given call site.
	// 1. keeps the assoc. method -> to-be-inlined calls inside method
	final Relation m2csINm = new LightRelation();
	// 2. keeps the assoc. method -> to-be-inlined calls to method
	final Relation m2csTOm = new LightRelation();
	// Initialize the aforementioned two relations
	for(Iterator it = ih.keySet().iterator(); it.hasNext(); ) {
	    CALL cs = (CALL) it.next();
	    m2csINm.add(extract_caller(cs), cs);
	    m2csTOm.add(extract_callee(cs), cs);
	}

	final Navigator nav = new Navigator() {
		public Object[] next(final Object node) {
		    CALL cs = (CALL) node;
		    HMethod hm = extract_callee(cs);
		    Set set = m2csINm.getValues(hm);
		    return set.toArray(new Object[set.size()]);
		}
		public Object[] prev(final Object node) {
		    CALL cs = (CALL) node;
		    HMethod hm = extract_caller(cs);
		    Set set = m2csTOm.getValues(hm);
		    return set.toArray(new Object[set.size()]);
		}
	    };

	Set cs_set = ih.keySet();
	CALL[] cs_array = (CALL[]) cs_set.toArray(new CALL[cs_set.size()]);
	
	SCCTopSortedGraph sccts =
	    SCCTopSortedGraph.topSort(SCComponent.buildSCC(cs_array, nav));

	return sccts.getLast();
    }


    //////////// B. AUXILIARY METHODS FOR INLINING START //////////////////

    private Set getInterestingLevel0InsideNodes(ParIntGraph pig) {
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
	return A;
    }

    private Set getInterestingLevel0InsideNodes(HMethod hm, ParIntGraph pig) {
	Set A = new HashSet();
	HCode hcode = hcf.convert(hm);
	for(Iterator it = hcode.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    if((q instanceof NEW) || (q instanceof ANEW)) {
		PANode node = node_rep.getCodeNode(q, PANode.INSIDE);
		if(!pig.G.captured(node) && lostOnlyInCaller(node, pig)) {
		    // we are not interested in stack allocating the exceptions
		    // since they  don't appear in normal case and so, they
		    // are not critical for the memory management
		    HClass hclass = getAllocatedType(q);
		    if(!java_lang_Throwable.isSuperclassOf(hclass))
			A.add(node);
		}
	    }
	}
	return A;
    }

    /* Normally, we should refuse to inline calls that are inside loops
       because that + stack allocation might lead to stack overflow errors.
       However, at this moment we don't test this condition. */
    private boolean good_cs(CALL cs){
	return true;
    }


    // given a quad q, returns the method q is part of 
    private final HMethod quad2method(Quad q) {
	return q.getFactory().getMethod();
    }


    /** Replace call site cs from hmethod with a clone of hcallee's
	code.
	
	@return map from the quads of the original hcallee to the
 	quads of its clone (these are the quads that are inserted in
 	hcaller's code). */
    private Map inline_call_site(CALL cs, HMethod hcaller, HMethod hcallee,
				 HCodeFactory hcf) {
	System.out.println("INLINING " + call2str(cs));

	Map old2new = new HashMap();

	HEADER header_new = null;
	try {
	    header_new = get_cloned_code(cs, hcaller, hcallee, old2new, hcf);
	} catch(CloneNotSupportedException e) {
	    throw new Error("Should never happen! " + e);
	}

	METHOD qm = (METHOD) (header_new.next(1));

	// add the code for the parameter passing
	add_entry_sequence(cs, qm);

	modify_return_and_throw(cs, header_new);

	translate_ap(old2new);
	
	return old2new;
    }


    private void extra_stack_allocation(Quad[] news, Map old2new) {
	for(int i = 0; i < news.length; i++) {
	    Quad q  = (Quad) old2new.get(news[i]);

	    assert q != null : "Warning: no new Quad for " + 
			Util.code2str(news[i]) + " in [ " +
			quad2method(news[i]) + " ]";

	    System.out.println("STKALLOC " + Util.code2str(q));

	    MyAP ap = getAPObj(q);
	    // new MyAP(getAllocatedType(q));
	    ap.sa = true;
	    ap.ns = true; // SYNC
	    setAPObj(q, ap);
	}
    }


    private void extra_thread_allocation(Quad[] news, Map old2new) {
	for(int i = 0; i < news.length; i++) {
	    Quad q  = (Quad) old2new.get(news[i]);

	    assert q != null : "Warning: no new Quad for " + 
			Util.code2str(news[i]) + " in [ " +
			quad2method(news[i]) + " ]";

	    System.out.println("THRALLOC " + Util.code2str(q));

	    MyAP ap = getAPObj(q);
	    // new MyAP(getAllocatedType(q));
	    if(!ap.sa) ap.ta = true;
	    ap.ns = true; // SYNC
	    setAPObj(q, ap);
	}
    }


    // Returns an HCodeElement that can be used as the provider of
    // source file and line number for the creation of new Quads.
    private HCodeElement get_inl_hce(final CALL cs) {
	HCodeElement result = new HCodeElement() {
		    public int getID() {
			assert false : "Unimplemented";
			// this should never happen
			return 0;
		    }
		    public String getSourceFile() {
			return
			    "INL_" + cs.getSourceFile() + "_" +
			    cs.getLineNumber();
		    }
		    public int getLineNumber() {
			return 1;
		    }
		};
	return result;
    }
    

    private void add_entry_sequence(CALL cs, METHOD qm) {
	HCodeElement inl_hce = get_inl_hce(cs);

	Quad replace_cs = new NOP(cs.getFactory(), inl_hce);

	move_pred_edges(cs, replace_cs);

	assert cs.paramsLength() == qm.paramsLength() : 
	    " different nb. of parameters between CALL and METHOD";
	
	Quad previous = replace_cs;

	int nb_params = cs.paramsLength();
	for(int i = 0; i < nb_params; i++) {
	    Temp formal = qm.params(i);
	    Temp actual = cs.params(i);
	    // emulate the Java parameter passing semantics
	    MOVE move = new MOVE(cs.getFactory(), inl_hce, formal, actual);
	    Quad.addEdge(previous, 0, move, 0);
	    previous = move;
	}

	// the edge pointing to the first instruction of the method body
	Edge edge = qm.nextEdge(0);
	Quad.addEdge(previous, 0, (Quad) edge.toCFG(), edge.which_pred());
    }

    
    private void modify_return_and_throw(final CALL cs, final HEADER header) {
	final HCodeElement inl_hce = get_inl_hce(cs);

	class QVisitor extends  QuadVisitor {
	    Set returnset;
	    Set throwset;
	    QVisitor() {
		returnset = new WorkSet();
		throwset  = new WorkSet();
	    }

	    public void finish() {
		PHI returnphi = new PHI(cs.getFactory(), inl_hce, new Temp[0],
					returnset.size());
		int edge = 0;
		for(Iterator returnit=returnset.iterator();returnit.hasNext();)
		    Quad.addEdge((Quad)returnit.next(), 0, returnphi, edge++);
		
		Quad.addEdge(returnphi, 0, cs.next(0),
			     cs.nextEdge(0).which_pred());

		PHI throwphi = new PHI(cs.getFactory(), inl_hce, new Temp[0],
				       throwset.size());
		edge = 0;
		for(Iterator throwit=throwset.iterator();throwit.hasNext();)
		    Quad.addEdge((Quad)throwit.next(), 0, throwphi, edge++);
		
		Quad.addEdge(throwphi, 0, cs.next(1),
			     cs.nextEdge(1).which_pred());
	    }

	    public void visit(Quad q) {}

	    public void visit(RETURN q) {
		Temp retVal = cs.retval(); 
		
		Quad replace = (retVal != null) ?
		    ((Quad) new MOVE
			(cs.getFactory(), inl_hce, retVal, q.retval())) :
		    ((Quad) new NOP(cs.getFactory(),  inl_hce));
		
		// make the predecessors of q point to replace
		move_pred_edges(q, replace);
		
		// the only succesor of replace should now be the
		// 0-successor of the CALL instruction (normal return)
		returnset.add(replace);
	    }

	    public void visit(THROW q) {
		Temp retEx = cs.retex(); 
		
		Quad replace = (retEx != null) ?
		    ((Quad) new MOVE
			(cs.getFactory(), inl_hce, retEx, q.throwable())) :
		    ((Quad) new NOP(cs.getFactory(),  inl_hce));
		
		// make the predecessors of q point to replace
		move_pred_edges(q, replace);
		
		// the only succesor of replace should now be the
		// 1-successor of the CALL instruction (exception return)
		throwset.add(replace);
	    }
	};
	QVisitor inlining_qv = new QVisitor();
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
	    if(!((old_q instanceof NEW) || (old_q instanceof ANEW)))
		continue;
	    setAPObj(new_q, (MyAP) (getAPObj(old_q).clone()));
	}
    }

    private HEADER get_cloned_code(CALL cs, HMethod caller, HMethod callee,
				 Map old2new, HCodeFactory hcf)
	throws CloneNotSupportedException {

	HCode hcode_orig = hcf.convert(callee);

	HEADER header_orig = 
	    (HEADER) hcode_orig.getRootElement();
	HEADER header_new  = 
	    (HEADER) Quad.clone(cs.getFactory(), header_orig);

	fill_the_map(header_orig, header_new, old2new, new HashSet());

	return header_new;
    }


    // recursively explore two HCode's (the second is the clone of the first
    // one) and set up a mapping "old NEW/ANEW/CALL -> new NEW/ANEW/CALL
    private static void fill_the_map(Quad q1, Quad q2, Map map, Set seen) {
	// avoid entering infinite loops: return when we meet a previously
	// seen instruction 
	if(!seen.add(q1)) return;

	if( (q1 instanceof NEW)  || 
	    (q1 instanceof ANEW) ||
	    (q1 instanceof CALL) )
	    map.put(q1, q2);

	Quad[] next1 = q1.next();
	Quad[] next2 = q2.next();

	assert next1.length == next2.length : " Possible error in HCode.clone()";

	for(int i = 0; i < next1.length; i++)
	    fill_the_map(next1[i], next2[i], map, seen);
    }

    private boolean hcodeOf(final HCode hcode,
			    final String cls_name, final String method_name) { 
	HMethod hm = hcode.getMethod();
	return isThisMethod(hm, cls_name, method_name);
    }

    private boolean isThisMethod(final HMethod hm, final String cls_name,
				 final String method_name) {
	HClass hc  = hm.getDeclaringClass();

	return
	    hm.getName().equals(method_name) &&
	    hc.getName().equals(cls_name);
    }



    private String call2str(CALL cs) {
	return
	    Util.code2str(cs) + "  [ " + quad2method(cs) + " ] ";
    }


    private void print_modified_hcode(HMethod hm,
				      final Collection  new_quads) {
	print_modified_hcode(hcf.convert(hm), new_quads);
    }

    private void print_modified_hcode(HCode hcode,
				      final Collection new_quads) {
	class MyCallBack extends HCode.PrintCallback {
	    public void printBefore(PrintWriter pw, HCodeElement hce) {
		if(new_quads.contains(hce))
		    pw.print(" *** ");
		else
		    pw.print("     ");
	    }
	    public void printAfter(PrintWriter pw, HCodeElement hce) {}
	};
	
	hcode.print(new PrintWriter(System.out, true), new MyCallBack());
	System.out.println("========================================\n"); 
    }

    // Given a call instruction cs, return the method whose body contains cs.
    private HMethod extract_caller(CALL cs) {
	return quad2method(cs);
    }

    // Given a call instruction cs, return the method that is called by cs.
    private HMethod extract_callee(CALL cs) {
	MetaMethod mm_caller = new MetaMethod(extract_caller(cs), true);
	MetaMethod[] mm_callees = mcg.getCallees(mm_caller, cs);
	if(mm_callees.length == 0) return null;
	assert mm_callees.length == 1 : "More than one callee for " + cs;
	return mm_callees[0].getHMethod();
    }


    //////////// C. ADVANCED INLINING STUFF START ///////////////////////
    
    // Given a (meta) method mm, generates inlining chains that will
    // make some object creation sites stack allocatable.
    // An inlining chain will be a list of calls like:
    //   call m1    (occuring in the body of m2)
    //   call m2    (occuring in the body of m3)
    //   ...
    //   call mk-1  (occuring in the body of mk)
    // where m1 == mm. Also, forall i, mi and mi+1 should appear in
    // different strongly connected components in the call graph (to avoid
    // circular inlining in nests of mutually recursive methods).
    private void generate_inlining_chains(MetaMethod mm) {
	ParIntGraph pig = pa.getExtParIntGraph(mm);

	Set nodes = getInterestingLevel0InsideNodes(mm.getHMethod(), pig);
	Set sa_nodes = new HashSet();
	Set ta_nodes = new HashSet();
	split_nodes(nodes, sa_nodes, ta_nodes);
	if(nodes.isEmpty()) return;

	current_chain_cs = new LinkedList();
	current_chain_callees = new LinkedList();

	discover_inlining_chains(mm, sa_nodes, ta_nodes, 0);
	
	current_chain_cs = null;
	current_chain_callees = null;
    }
    private LinkedList current_chain_cs;
    private LinkedList current_chain_callees;
    
    private void split_nodes(Set nodes, Set sa_nodes, Set ta_nodes) {
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    Quad q = (Quad) node_rep.node2Code(node);
	    if(opt.stack_allocate_not_in_loops() && in_a_loop(q)) {
		System.out.println("split_nodes: " + Util.code2str(q) + 
				   " in a loop -> no sa, try to ta");
		ta_nodes.add(node);
	    }
	    else
		sa_nodes.add(node);
	}
    }

    private int get_rang(HMethod hm) {
	return ((Integer) hm2rang.get(hm)).intValue();
    }

    private boolean good_cs2(CALL cs) {
	int rang_caller = get_rang(extract_caller(cs));
	int rang_callee = get_rang(extract_callee(cs));
	assert rang_caller >= rang_callee : "Bad method rangs " + cs;
	return rang_caller > rang_callee;
    }

    // Parameters:
    //  nodes - the set of PANodes that escape from some method called by mm,
    //    only into the caller (ie mm).
    //  level - nodes originate in a callee at distance level in the call
    //    graph; to be able to stack allocate them, an inlining chain of length
    //    at least level is necessary.
    private void discover_inlining_chains(MetaMethod mm,
					  Set sa_nodes, Set ta_nodes,
					  int level) {
	// iterate through all the call sites where mm is called
	MetaMethod[] callers = mac.getCallers(mm);
	for(int i = 0; i < callers.length; i++) {
	    MetaMethod mcaller = callers[i];

	    Set call_sites = mcg.getCallSites(mcaller);
	    for(Iterator it = call_sites.iterator(); it.hasNext(); ) {
		CALL cs = (CALL) it.next();

		MetaMethod[] callees = mcg.getCallees(mcaller, cs);
		
		// we can only inline call sites that are only to mm
		if(! ( (callees.length == 1) &&
		       (callees[0] == mm) && good_cs(cs)
		       && good_cs2(cs) ) ) continue;
		
		// refuse to stack allocate stuff in a loop
		if(opt.stack_allocate_not_in_loops() && in_a_loop(cs)) {
		    System.out.println("discover_inlining_chains: " +
				       Util.code2str(cs) +
				       " is in a loop -> don't sa");
		    ta_nodes = new HashSet(ta_nodes);
		    for(Iterator itn = sa_nodes.iterator(); itn.hasNext(); ) {
			PANode node = (PANode) itn.next();
			MyAP ap = getAP_special(node);
			if(!ap.ta)
			    ta_nodes.add(node);
		    }
		    sa_nodes = Collections.EMPTY_SET;
		}

		current_chain_cs.addLast(cs);
		current_chain_callees.addLast(mm.getHMethod());
		
		// compute specializations of nodes for cs
		Set sa_specs = specializeNodes(sa_nodes, cs);
		Set ta_specs = specializeNodes(ta_nodes, cs);
		
		ParIntGraph pig_caller = pa.getIntParIntGraph(mcaller);
		
		// B = specs that are captured in mcaller
		Set sa_B = captured_subset(sa_specs, pig_caller);
		Set ta_B = captured_subset(ta_specs, pig_caller);
		
		// here we have some good inlining chain; mark it
		if((opt.DO_STACK_ALLOCATION &&
		    opt.DO_INLINING_FOR_SA && !sa_B.isEmpty()) ||
		   (opt.DO_THREAD_ALLOCATION &&
		    opt.DO_INLINING_FOR_TA && !ta_B.isEmpty())) {
		    // avoid inlining the class initializers: past
		    // experience showed this might lead to circular
		    // dependencies in the static initializer code
		    if(!mcaller.getHMethod().getName().equals("<clinit>")){
			InliningChain new_ic =
			    new InliningChain(current_chain_cs,
					      current_chain_callees,
					      get_news(sa_B),
					      get_news(ta_B));
			if(DEBUG_IC)
			    System.out.println("Discovered chain: "
					       + new_ic);
			if(new_ic.isAcceptable())
			    chains.add(new_ic);
		    }
		}
		
		// the length of current_chain_cs is level + 1
		if(level + 1 < opt.MAX_INLINING_LEVEL) {
		    // C = specs that escape only in caller 
		    Set sa_C = only_in_caller_subset(sa_specs, pig_caller);
		    Set ta_C = only_in_caller_subset(ta_specs, pig_caller);
		    
		    if((opt.DO_STACK_ALLOCATION &&
			opt.DO_INLINING_FOR_SA && !sa_C.isEmpty()) ||
		       (opt.DO_THREAD_ALLOCATION &&
			opt.DO_INLINING_FOR_TA && !ta_C.isEmpty()))
			discover_inlining_chains
			    (mcaller, sa_C, ta_C, level + 1);
		}
		
		current_chain_cs.removeLast();
		current_chain_callees.removeLast();
	    }
	}
    }

    
    // get the allocation policy object associated with the object creation
    // site for node. node might be a specialization so we have to go first
    // to its root node and take the AP of this one.
    private MyAP getAP_special(PANode node) {
	PANode root = node.getRoot();
	Quad q = (Quad) node_rep.node2Code(root);
	return getAPObj(q);
    }


    private LinkedList chains = null;


    // Computes the set of NEW / ANEW quads that created the nodes
    private Set get_news(final Set nodes) {
	Set result = new HashSet();
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = ((PANode) it.next()).getRoot();
	    Quad quad = (Quad) node_rep.node2Code(node);
	    assert quad != null : "No quad for " + node;
	    result.add(quad);
	}
	return result;
    }


    // specialize a set of nodes for a given call site
    private Set specializeNodes(final Set nodes, final CALL cs) {
	Set result = new HashSet();
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    PANode spec = node.csSpecialize(cs);
	    if(spec == null) continue;
	    result.add(spec);
	}
	return result;	    
    }

    
    // Returns a subset of nodes containing the nodes captured in pig
    private Set captured_subset(final Set nodes,
				final ParIntGraph pig) {
	Set result = new HashSet();
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(pig.G.captured(node))
		result.add(node);
	}
	return result;
    }

    // Returns a subset of nodes containing the nodes that escape
    // only (exactly) in the caller
    private Set only_in_caller_subset(final Set nodes,
				      final ParIntGraph pig) {
	Set result = new HashSet();
	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(!pig.G.captured(node) && lostOnlyInCaller(node, pig))
		result.add(node);
	}
	return result;
    }


    private void extra_stack_allocation(Set news) {
	for(Iterator it = news.iterator(); it.hasNext(); ) {
	    Quad q  = (Quad) it.next();

	    System.out.println("STKALLOC " + Util.code2str(q));

	    MyAP ap = getAPObj(q);
	    // new MyAP(getAllocatedType(q));
	    ap.sa = true;
	    ap.ns = true; // SYNC
	    setAPObj(q, ap);
	}
    }


    private void extra_thread_allocation(Set news) {
	for(Iterator it = news.iterator(); it.hasNext(); ) {
	    Quad q  = (Quad) it.next();

	    System.out.println("THRALLOC " + Util.code2str(q));

	    MyAP ap = getAPObj(q);
	    // new MyAP(getAllocatedType(q));
	    ap.ta = true;
	    ap.ns = true; // SYNC
	    setAPObj(q, ap);
	}
    }


    private class InliningChain {
	private LinkedList calls;
	private LinkedList callees;
	private Set sa_news;
	private Set ta_news;

	InliningChain(final LinkedList calls,
		      final LinkedList callees,
		      final Set sa_news,
		      final Set ta_news) {
	    this.calls = (LinkedList) calls.clone();
	    this.callees = (LinkedList) callees.clone();
	    this.sa_news = new HashSet(sa_news);
	    this.ta_news = new HashSet(ta_news);
	}

	Set get_sa_news() { return sa_news; }
	Set get_ta_news() { return ta_news; }

	public String toString() {
	    StringBuffer buff = new StringBuffer();
	    buff.append("INLINING CHAIN (" + calls.size() + "): {\n CALLS:\n");
	    for(Iterator it = calls.iterator(); it.hasNext(); ) {
		CALL cs = (CALL) it.next();
		buff.append("  ")
		    .append(Util.code2str(cs))
		    .append(" [ ")
		    .append(extract_caller(cs))
		    .append(" ]  { ")
		    .append(extract_callee(cs))
		    .append(" }\n");
	    }

	    present_news(buff, " STACK  ALLOCATABLE STUFF:", get_sa_news());
	    present_news(buff, " THREAD ALLOCATABLE STUFF:", get_ta_news());
	    buff.append("}\n");
	    return buff.toString();
	}

	// aux method for pretty printing of sa/ta news
	private void present_news(StringBuffer buff, String message,
				  Set news) {
	    if(news.isEmpty()) return;
	    buff.append(message).append("\n");
	    for(Iterator it = news.iterator(); it.hasNext(); ) {
		buff.append("  ")
		    .append(Util.code2str((Quad) it.next()))
		    .append("\n");
	    }
	}


	// test whether the size of the inlined method will be smaller
	// than the maximum acceptable one.
	public boolean isAcceptable() {
	    if(isDone()) return true;
	    int size = get_method_size(extract_caller(getLastCall()));
	    for(Iterator it = calls.iterator(); it.hasNext(); ) {
		CALL cs = (CALL) it.next();
		HMethod hm = extract_callee(cs);
		if(get_method_size(hm) > opt.MAX_INLINABLE_METHOD_SIZE)
		    return false;
		//if(hm != null)
		size += get_method_size(hm);
	    }
	    return size < opt.MAX_METHOD_SIZE;
	}

	private int get_method_size(HMethod hm) {
	    HCode hcode = hcf.convert(hm);
	    return hcode.getElementsL().size();
	}

	// The call cs has just been inlined. As a consequence, any inlining
	// chain containing cs should be updated in the following way: cs
	// must be removed from there and the previous call in the chain
	// updated according to the old2new map.
	void update_ic(CALL cs, Map old2new) {
	    if(isDone()) return;
	    if(!calls.contains(cs)) return;


	    if(DEBUG_IC)
		System.out.println("update_ic(" + cs + ") for " + this);

	    CALL first_cs = (CALL) calls.getFirst();
	    if(first_cs == cs) {
		if(DEBUG_IC)
		    System.out.println("The news are modified");
		sa_news = project_set(sa_news, old2new);
		ta_news = project_set(ta_news, old2new);
	    }

	    ListIterator ithm = callees.listIterator(0);
	    for(ListIterator it = calls.listIterator(0); it.hasNext(); ) {
		CALL current = (CALL) it.next();
		HMethod current_callee = (HMethod) ithm.next();

		if(cs == current) {
		    it.remove();   // this inlining has already been done
		    ithm.remove(); // it is hence unnecessary

		    if(it.hasPrevious()) {
			CALL previous = (CALL) it.previous();
			CALL new_prev = (CALL) old2new.get(previous);
			if(DEBUG_IC) {
			    System.out.println("previous = " + previous);
			    System.out.println("new_prev = " + new_prev);
			}
			it.remove();      // replace the previous call with
			it.add(new_prev); // its updated version
		    }

		    if(DEBUG_IC)
			System.out.println("After " + this);

		    if(isDone()) {
			extra_stack_allocation(sa_news);
			extra_thread_allocation(ta_news);
		    }
		}
	    }
	}

	private Set project_set(Set set, Map old2new) {
	    Set result = new HashSet();
	    for(Iterator it = set.iterator(); it.hasNext(); ) {
		Quad old_quad = (Quad) it.next();
		Quad new_quad = (Quad) old2new.get(old_quad);
		assert new_quad != null : "Warning: no new Quad for " + 
			    Util.code2str(old_quad) + " in [ " +
			    quad2method(old_quad) + " ]";
		result.add(new_quad);
	    }
	    return result;
	}


	CALL getLastCall() {
	    assert !isDone() : "You shouldn't call this!";
	    return (CALL) calls.getLast();
	}


	HMethod getLastCallee() {
	    assert !isDone() : "You shouldn't call this!";
	    return (HMethod) callees.getLast();
	}


	boolean isDone() {
	    return calls.isEmpty();
	}


	// Returns the rang of the last method (the one where everything in
	// this inlining chain is going to be inlined).
	int get_rang() {
	    return MAInfo.this.get_rang(extract_caller(getLastCall()));
	}
    };


    private void process_chain(InliningChain ic) {
	if(ic.isDone()) return;
	//	if(DEBUG)
	    System.out.println("\n\nPROCESSING " + ic);

	while(!ic.isDone()) {
	    CALL cs = ic.getLastCall();
	    HMethod hcaller = extract_caller(cs);
	    System.out.println("hcaller = " + hcaller);

	    HCode hcode = hcf.convert(hcaller);

	    HMethod hcallee = ic.getLastCallee();
	    Map old2new = inline_call_site(cs, hcaller, hcallee, hcf);

	    // update all the Inlining Chains
	    for(Iterator it = chains.iterator(); it.hasNext(); )
		((InliningChain) it.next()).update_ic(cs, old2new);
	}
    }


    private void sort_chains() {
	Object[] ics = chains.toArray(new Object[chains.size()]);
	Arrays.sort
	    (ics, 
	     new Comparator() {
		public int compare(Object obj1, Object obj2) {
		    int rang1 = ((InliningChain) obj1).get_rang();
		    int rang2 = ((InliningChain) obj2).get_rang();
		    if(rang1 < rang2) return -1;
		    if(rang1 == rang2) return 0;
		    return 1;
		}
		public boolean equals(Object obj) {
		    return obj == this;
		}
	    });
	chains = new LinkedList();
	for(int i = 0; i < ics.length; i++)
	    chains.addLast(ics[i]);
    }


    private void process_inlining_chains() {
      if(DEBUG_IC) {
	    Util.print_collection(chains, "\n\nINLINING CHAINS");
	    System.out.println("=======================");
      }

      // db debug
      System.out.println("Chains that influence Main.run"); 
      for(Iterator it = chains.iterator(); it.hasNext(); ) {
	  InliningChain ic = (InliningChain) it.next();
	  HMethod hm = ic.getLastCallee();
	  if(hm.getName().equals("run") &&
	     hm.getClass().getName().equals("Main"))
	      System.out.println(ic);
      }
      System.out.println("============================");
      
      sort_chains();
      
      Set toPrune = new HashSet();
      for(Iterator it = chains.iterator(); it.hasNext(); ) {
	  CALL cs = ((InliningChain) it.next()).getLastCall();
	  toPrune.add(cs.getFactory().getParent());
      }
      
      for(Iterator it = chains.iterator(); it.hasNext(); )
	  process_chain((InliningChain) it.next());
      
      // remove the newly introduced unreachable code
      for(Iterator pit = toPrune.iterator(); pit.hasNext(); ) {
	  Code hcode = (Code) pit.next();
	  if(DEBUG_IC)
	      System.out.print("Pruning " + hcode.getMethod());
	  Unreachable.prune(hcode);
      }
    }
    
    //////////// INLINING STUFF END /////////////////////////////////////
    /////////////////////////////////////////////////////////////////////
}

