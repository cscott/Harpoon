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
 * @version $Id: MAInfo.java,v 1.1.2.37 2000-10-17 00:27:56 cananian Exp $
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
    private boolean USE_INTER_THREAD = false;
    
    /** Creates a <code>MAInfo</code>. */
    public MAInfo(PointerAnalysis pa, HCodeFactory hcf,
		  Set mms, boolean USE_INTER_THREAD){
        this.pa  = pa;
	this.mcg = pa.getMetaCallGraph();
	this.mac = pa.getMetaAllCallers();
	this.hcf = hcf;
	this.mms = mms;
	this.node_rep = pa.getNodeRepository();
	this.USE_INTER_THREAD = USE_INTER_THREAD;
        this.DO_PREALLOCATION = USE_INTER_THREAD;

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

    /* Analyze a single method: take the object creation sites from it
       and generate an allocation policy for each one. */
    private final void analyze_mm(MetaMethod mm){
	HMethod hm  = mm.getHMethod();

	if(DEBUG)
	    System.out.println("\n\nMAInfo: Analyzed Meta-Method: " + mm);

	HCode hcode = hcf.convert(hm);

	ParIntGraph initial_pig = pa.getIntParIntGraph(mm);
	////	    USE_INTER_THREAD ? pa.threadInteraction(mm): 
	////                       pa.getIntParIntGraph(mm);
	
	ParIntGraph pig = (ParIntGraph) initial_pig.clone();
	if(pig == null) return;
	pig.G.flushCaches();
	pig.G.e.removeMethodHoles(good_holes);

	if(DEBUG)
	    System.out.println("Parallel Interaction Graph:" + pig);

	((harpoon.IR.Quads.Code) hcode).setAllocationInformation(this);

	Set news = new HashSet();

	for(Iterator it = hcode.getElementsI(); it.hasNext(); ){
	    HCodeElement hce = (HCodeElement) it.next();
	    if((hce instanceof NEW) || (hce instanceof ANEW)){
		news.add(hce);
		MyAP ap = getAPObj((Quad) hce);
		HClass hclass = getAllocatedType(hce);
		ap.hip = 
		    DefaultAllocationInformation.hasInteriorPointers(hclass);
	    }
	}

	Set nodes = pig.allNodes();

	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(node.type != PANode.INSIDE) continue;

	    // we are interested in objects allocated in the current thread
	    if(node.isTSpec()) continue;
	    
	    int depth = node.getCallChainDepth();

	    if(pig.G.captured(node)){
		if((depth == 0) 
		   /* && news.contains(node_rep.node2Code(node)) */ ) {
		    // captured nodes of depth 0 (ie allocated in this method,
		    // not in a callee) are allocated on the stack.
		    Quad q  = (Quad) node_rep.node2Code(node);
		    Util.assert(q != null, "No quad for " + node);

		    if(stack_alloc_extra_cond(node, q)) {
			MyAP ap = getAPObj(q);
			ap.sa = true;
			if(DEBUG)
			    System.out.println("STACK: " + node + 
					       " was stack allocated " +
					       Debug.getLine(q));
		    }
		}
	    }
	    else {
		if((depth == 0)
		   /* && news.contains(node_rep.node2Code(node)) */ ) {
		    if(remainInThread(node, hm, "")) {
			Quad q = (Quad) node_rep.node2Code(node);
			Util.assert(q != null, "No quad for " + node);

			MyAP ap = getAPObj(q);
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
	
	PAThreadMap tau = (PAThreadMap) (pa.getIntParIntGraph(mm).tau.clone());

	if(DO_PREALLOCATION && (tau.activeThreadSet().size() == 1))
	    analyze_prealloc(mm, hcode, pig, tau);

	/// DUMMY CODE: we don't have NSTK_malloc_with_heap yet
	if(!NO_TG)
	    set_make_heap(tau.activeThreadSet());

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

	if(DO_METHOD_INLINING)
	    generate_inlining_hints(mm, pig);
    }

    // Returns the INSIDE nodes of level 0 from pig.
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

    /** Set the allocation policy info such that each of the threads allocated
	and started into the currently analyzed method has a thread specific
	heap associated with it. */
    private void set_make_heap(Set threads){
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


    // checks whether node escapes only in some method. We assume that
    // no method hole starts a new thread and so, we can allocate
    // the onject on the thread specific heap
    private boolean escapes_only_in_methods(PANode node, ParIntGraph pig){
	if(!pig.G.e.nodeHolesSet(node).isEmpty())
	    return false;
	if(pig.G.getReachableFromR().contains(node))
	    return false;
	if(pig.G.getReachableFromExcp().contains(node))
	    return false;
	
	return true;
    }

    // hope that this evil string really doesn't exist anywhere else
    private static String my_scope = "pa!";
    private static final TempFactory 
	temp_factory = Temp.tempFactory(my_scope);

    // try to apply some aggressive preallocation into the thread specific
    // heap.
    private void analyze_prealloc(MetaMethod mm, HCode hcode, ParIntGraph pig,
				  PAThreadMap tau){

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
	    ap.mh = true;  // makeHeap
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

    private void insert_newq(METHOD method, NEW newq){
	Util.assert(method.nextLength() == 1,
		    "A METHOD quad should have exactly one successor!");
	Edge nextedge = method.nextEdge(0);
	Quad nextquad = method.next(0);
	Quad.addEdge(method, nextedge.which_succ(), newq, 0);
	Quad.addEdge(newq, 0, nextquad, nextedge.which_pred());
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


    /** Checks whether <code>node</code> escapes only in the caller:
	it is reached through a parameter or it is returned from the
	method but not lost due to some other reasons. */
    private boolean lostOnlyInCaller(PANode node, ParIntGraph pig){
	// if node escapes into a method hole it's wrong ...
	if(pig.G.e.hasEscapedIntoAMethod(node))
	    return false;

	for(Iterator it=pig.G.e.nodeHolesSet(node).iterator();it.hasNext();){
	    PANode nhole = (PANode)it.next();
	    // if the node escapes through some node that is not a parameter
	    // it's wrong ...
	    if(nhole.type != PANode.PARAM)
		return false;
	}

	return true;
    }
    
    private static int MAX_LEVEL_BOTTOM_MODE = 10;
    private boolean remainInThreadBottom(PANode node, MetaMethod mm,
					 int level, String ident){
	if(node == null)
	    return false;

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


    // Checks whether node defined into hm, remain into the current
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
	
	if(!lostOnlyInCaller(node, pig)){
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
    public void print(){
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
		    Quad.addEdge((Quad)returnit.next(),0,returnphi,edge++);
		
		Quad.addEdge(returnphi,0,cs.next(0),cs.nextEdge(0).which_pred());

		PHI throwphi=new PHI(cs.getFactory(),null, new Temp[0],
				     throwset.size());
		edge=0;
		for(Iterator throwit=throwset.iterator();throwit.hasNext();)
		    Quad.addEdge((Quad)throwit.next(),0,throwphi,edge++);
		
		Quad.addEdge(throwphi,0,cs.next(1),cs.nextEdge(1).which_pred());
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
	HMethod hm = cs.method();
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

}

