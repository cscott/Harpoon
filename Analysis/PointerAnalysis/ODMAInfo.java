// ODMAInfo.java, created Mon Apr  3 18:17:57 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
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
import harpoon.Analysis.MetaMethods.GenType;

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
import harpoon.Util.DataStructs.LightMap;


/**
 * <code>ODMAInfo</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: ODMAInfo.java,v 1.1.2.3 2001-06-17 22:30:42 cananian Exp $
 */
public class ODMAInfo implements AllocationInformation, java.io.Serializable {

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

    public static Map Nodes2Status = new LightMap();
    public static int nStudiedNode = 0;

    private static Set good_holes = null;
    private void initialize_good_holes(){
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

    ODPointerAnalysis pa;
    HCodeFactory    hcf;
    // the meta-method we are interested in (only those that could be
    // started by the main or by one of the threads (transitively) started
    // by the main thread.
    Set             mms;
    NodeRepository  node_rep;
    MetaCallGraph   mcg;
    MetaAllCallers  mac;

    private static Linker linker = null;

    // use the inter-thread analysis
    private boolean USE_INTER_THREAD = false;
    private boolean DO_STACK_ALLOCATION  = false;
    private boolean DO_THREAD_ALLOCATION = false;
    private boolean GEN_SYNC_FLAG        = false;
    public static boolean SYNC_ELIM        = false;
    public static boolean MEM_OPTIMIZATION = true;
    
        /** Creates a <code>ODMAInfo</code>. */
    public ODMAInfo(ODPointerAnalysis pa, HCodeFactory hcf,
		    Set mms, boolean USE_INTER_THREAD,
		    boolean DO_STACK_ALLOCATION,
		    boolean DO_THREAD_ALLOCATION,
		    boolean GEN_SYNC_FLAG){
        this.pa  = pa;
	this.mcg = pa.getMetaCallGraph();
	this.mac = pa.getMetaAllCallers();
	this.hcf = hcf;
	this.mms = mms;
	this.node_rep = pa.getNodeRepository();
	this.USE_INTER_THREAD = USE_INTER_THREAD;
        this.DO_PREALLOCATION = USE_INTER_THREAD;
	this.DO_STACK_ALLOCATION  = DO_STACK_ALLOCATION;
	this.DO_THREAD_ALLOCATION = DO_THREAD_ALLOCATION;
	this.GEN_SYNC_FLAG        = GEN_SYNC_FLAG;

	if(mms.size()==0)
	    linker = null;
	else{
	    Iterator it = mms.iterator();
	    MetaMethod mm = (MetaMethod) it.next();
	    linker = mm.getHMethod().getDeclaringClass().getLinker();
	}

	initialize_good_holes();
	java_lang_Thread = linker.forName("java.lang.Thread");
	java_lang_Throwable = linker.forName("java.lang.Throwable");


	if(!ODPointerAnalysis.ON_DEMAND_ANALYSIS)
	    analyze();
	
	this.MAX_LEVEL_BOTTOM_MODE = ODPointerAnalysis.MAX_ANALYSIS_ABOVE_SPEC;

	if(DO_METHOD_INLINING)
	    this.ih = new HashMap();
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
    public final void analyze_mm(MetaMethod mm){
	HMethod hm  = mm.getHMethod();

	if(DEBUG)
	    System.out.println("\n\nODMAInfo: Analyzed Meta-Method: " + mm);

	HCode hcode = hcf.convert(hm);

	ODParIntGraph initial_pig = pa.getIntParIntGraph(mm,true);
	////	    USE_INTER_THREAD ? pa.threadInteraction(mm): 
	////                       pa.getIntParIntGraph(mm);
	
//tbu 	ODParIntGraph pig = (ODParIntGraph) initial_pig.clone();
	ODParIntGraph pig = initial_pig;
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
	HClass excp_class = 
	    linker.forName("java.lang.Exception");

	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(node.type != PANode.INSIDE) continue;
	    GenType[] types = node.getPossibleClasses();
	    boolean isExcp = false;
	    if(types==null){
// 		node2Code(node);
		isExcp = true;
	    }
	    else {
		for(int i=0; (i<types.length)&&(!isExcp); i++){
		    if (excp_class.isSuperclassOf(types[i].getHClass()))
			isExcp = true;
		}
	    }
	    if (isExcp)
		continue;

	    nStudiedNode++;
	    if (ODMAInfo.Nodes2Status.get(node)==null){
		ODNodeStatus newstatus = new ODNodeStatus();
		newstatus.node = node;
		ODMAInfo.Nodes2Status.put(node, newstatus);
	    }
	    else {
		System.err.println("Processing twice a node... ??? " + node);
	    }
	    ODNodeStatus status = (ODNodeStatus) Nodes2Status.get(node);


	    // we are interested in objects allocated in the current thread
	    if(node.isTSpec()) continue;
	    
	    int depth = node.getCallChainDepth();
	    System.out.println("CallChainDepth = " + depth); 
	    if (depth!=0) continue;

	    //FV This test has to be changed
	    boolean iscaptured = captured(pig, mm, node);
	    pig = pa.getIntParIntGraph(mm);

// 	    if(iscaptured)
// 		System.out.println("--captured " + node + " in " + mm + pig);
// 	    else
// 		System.out.println("--escaping " + node + " in " + mm + pig);

	    //	    if(pig.G.captured(node)){
	    if(iscaptured){
		if((depth == 0) 
		   /* && news.contains(node_rep.node2Code(node)) */ ) {
		    // captured nodes of depth 0 (ie allocated in this method,
		    // not in a callee) are allocated on the stack.
		    Quad q  = (Quad) node_rep.node2Code(node);
		    Util.assert(q != null, "No quad for " + node);

		    if(stack_alloc_extra_cond(node, q)) {
			MyAP ap = getAPObj(q);
			if (ODMAInfo.MEM_OPTIMIZATION)
			    ap.sa = true;
			else
			    ap.sa = false;
			if (ODMAInfo.SYNC_ELIM)
			    ap.ns = true;
			else
			    ap.ns = false;
// 			if(DEBUG)
			    System.out.println("STACK: " +
					       " was stack allocated " +
					       Debug.getLine(q) + " " + node);
			status.onStack = true;
		    }
		}
	    }
	    else {
		if((depth == 0)
		   /* && news.contains(node_rep.node2Code(node)) */ ) {
		    if(remainInThread(node, hm, "")) {
			System.out.println("node remainInThread");
			Quad q = (Quad) node_rep.node2Code(node);
			Util.assert(q != null, "No quad for " + node);

			MyAP ap = getAPObj(q);
			if (ODMAInfo.MEM_OPTIMIZATION)
 			    ap.ta = true; // thread allocation
// 			    ap.ta = false;
			else
			    ap.ta = false;
			ap.ah = null; // on the current heap
			if (ODMAInfo.SYNC_ELIM)
			    ap.ns = true;
			else
			    ap.ns = false;
			status.onLocalHeap = true;
			       

			//if(DEBUG)
                           {
			       System.out.print("THREAD:  was thread allocated " +
					       Debug.getLine(q) + " " + node);
			       if (pig.G.excp.contains(node))
				   System.out.println(" (returned as exception)");
			       else
				   System.out.println(" ()");
			       System.out.println("Analyzed Meta-Method: " + mm);
			   }
		    }
		    else{
			Quad q = (Quad) node_rep.node2Code(node);
			System.out.print("node escapes of Thread " + Debug.getLine(q) + ".");
			if (pig.G.excp.contains(node))
			    System.out.println(" (returned as exception)");
			else
			    System.out.println(" ()");
		    }
		}
		else {
		    System.out.println("Depth != 0 (" + depth + ")");
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
		    if (ODMAInfo.MEM_OPTIMIZATION){
			ap.sa = true;
			ap.ta = false;
		    }
		    else{
			ap.sa = false;
			ap.ta = false;
		    }
		    if (ODMAInfo.SYNC_ELIM)
			ap.ns = true;
		    else
			ap.ns = false;

		}
	    }
	}

	if(DO_METHOD_INLINING)
	    generate_inlining_hints(mm, pig);
    }

    public final void analyze_mm(MetaMethod mm, Set nodes){
	HMethod hm  = mm.getHMethod();

	if(DEBUG)
	    System.out.println("\n\nODMAInfo: Analyzed Meta-Method: " + mm);

	System.out.println("MEM_OPTIMIZATION = " + ODMAInfo.MEM_OPTIMIZATION);
	System.out.println("SYNC_ELIM = " + SYNC_ELIM);

	HCode hcode = hcf.convert(hm);
	if(hcode == null) return;

	((harpoon.IR.Quads.Code) hcode).setAllocationInformation(this);
	System.out.println("WOW: ai set for " + hm);
	AllocationInformation ai = ((harpoon.IR.Quads.Code) hcode).getAllocationInformation();
// 	System.out.println("0. " + (ai==null?
// 			   (hm + " baaad!") :
// 			   (hm + " good!")));

// 	System.out.println("\nB4 getIntParIntGraph");
	ODParIntGraph initial_pig = pa.getIntParIntGraph(mm,true);
// 	System.out.println("AFTER getIntParIntGraph\n");
	////	    USE_INTER_THREAD ? pa.threadInteraction(mm): 
	////                       pa.getIntParIntGraph(mm);
	
//tbu 	ODParIntGraph pig = (ODParIntGraph) initial_pig.clone();
	ODParIntGraph pig = initial_pig;
	if(pig == null) return;
	pig.G.flushCaches();
	pig.G.e.removeMethodHoles(good_holes);

	if(DEBUG)
	    System.out.println("Parallel Interaction Graph:" + pig);

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

	for(Iterator it = nodes.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(node.type != PANode.INSIDE) continue;

	    if (ODMAInfo.Nodes2Status.get(node)==null){
		ODMAInfo.Nodes2Status.put(node,new ODNodeStatus());
	    }
	    else {
		System.err.println("Processing twice a node... ??? " + node);
	    }
// 	    ODMAInfo.Nodes2Status.put(node,new Integer(3));
	    ODNodeStatus status = (ODNodeStatus) Nodes2Status.get(node);

	    // we are interested in objects allocated in the current thread
	    if(node.isTSpec()) continue;
	    
	    int depth = node.getCallChainDepth();
	    System.out.println("CallChainDepth = " + depth); 
	    if (depth!=0) continue;

	    //FV This test has to be changed
	    boolean iscaptured = captured(pig, mm, node);
	    pig = pa.getIntParIntGraph(mm);

// 	    if(iscaptured)
// 		System.out.println("--captured " + node + " in " + mm + pig);
// 	    else
// 		System.out.println("--escaping " + node + " in " + mm + pig);

	    //	    if(pig.G.captured(node)){
	    if(iscaptured){
		if((depth == 0) 
		   /* && news.contains(node_rep.node2Code(node)) */ ) {
		    // captured nodes of depth 0 (ie allocated in this method,
		    // not in a callee) are allocated on the stack.
		    Quad q  = (Quad) node_rep.node2Code(node);
		    Util.assert(q != null, "No quad for " + node);

		    if(stack_alloc_extra_cond(node, q)) {
			MyAP ap = getAPObj(q);
			if (ODMAInfo.MEM_OPTIMIZATION){
			    ap.sa = true;
			    ap.ta = false;
			}
			else{
			    ap.sa = false;
			    ap.ta = false;
			}
			if (ODMAInfo.SYNC_ELIM)
			    ap.ns = true;
			else
			    ap.ns = false;
// 			if(DEBUG)
			    System.out.println("STACK: " + 
					       " was stack allocated " +
					       Debug.getLine(q)  +  " " + node );
			status.onStack = true;
// 			ODMAInfo.Nodes2Status.put(node,new Integer(1));
		    }
		}
	    }
	    else {
		if((depth == 0)
		   /* && news.contains(node_rep.node2Code(node)) */ ) {
		    if(remainInThread(node, hm, "")) {
			System.out.println("node remainInThread");
			Quad q = (Quad) node_rep.node2Code(node);
			Util.assert(q != null, "No quad for " + node);

			MyAP ap = getAPObj(q);
			if (ODMAInfo.MEM_OPTIMIZATION){
			    ap.sa = false;
// 			    ap.ta = false;
 			    ap.ta = true;
			    ap.ah = null; // on the current heap
			}
			else{
			    ap.sa = false;
			    ap.ta = false;
			}
			if (ODMAInfo.SYNC_ELIM)
			    ap.ns = true;
			else
			    ap.ns = false;
			status.onLocalHeap = true;
// 			ODMAInfo.Nodes2Status.put(node,new Integer(2));
			//if(DEBUG)
                           {
			       System.out.print("THREAD: " + " was thread allocated " +
					       Debug.getLine(q) + " " + node );
			       if (pig.G.excp.contains(node))
				   System.out.println(" (returned as exception)");
			       else
				   System.out.println(" ()");
			       System.out.println("Analyzed Meta-Method: " + mm);
			   }
		    }
		    else{
			Quad q = (Quad) node_rep.node2Code(node);
			System.out.print("node escapes of Thread " + Debug.getLine(q) 
					 + " " + node);
			if (pig.G.excp.contains(node))
			    System.out.println(" (returned as exception)");
			else
			    System.out.println(" ()");
		    }
		}
		else {
		    System.out.println("Depth != 0 (" + depth + ")");
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
		    if (ODMAInfo.MEM_OPTIMIZATION){
			ap.sa = true;
			ap.ta = false;
		    }
		    else{
			ap.sa = false;
			ap.ta = false;
		    }
		    if (ODMAInfo.SYNC_ELIM)
			ap.ns = true;
		    else
			ap.ns = false;
		}
	    }
	}

	if(DO_METHOD_INLINING)
	    generate_inlining_hints(mm, pig, nodes);
    }

    /** Only try to stack allocate the node given as second argument.
     */
    public final void analyze_mm(MetaMethod mm, PANode node, boolean tryThread){
	HMethod hm  = mm.getHMethod();

	if(DEBUG)
	    System.out.println("\n\nODMAInfo: Analyzed Meta-Method: " + mm);

	System.out.println("MEM_OPTIMIZATION = " + ODMAInfo.MEM_OPTIMIZATION);
	System.out.println("SYNC_ELIM = " + SYNC_ELIM);

	HCode hcode = hcf.convert(hm);
	if(hcode == null) return;

	((harpoon.IR.Quads.Code) hcode).setAllocationInformation(this);
	System.out.println("WOW: ai set for " + hm);
	AllocationInformation ai = ((harpoon.IR.Quads.Code) hcode).getAllocationInformation();

	ODParIntGraph initial_pig = pa.getIntParIntGraph(mm,true);
	
//tbu 	ODParIntGraph pig = (ODParIntGraph) initial_pig.clone();
	ODParIntGraph pig = initial_pig;
	if(pig == null) return;
	pig.G.flushCaches();
	pig.G.e.removeMethodHoles(good_holes);

	if(DEBUG)
	    System.out.println("Parallel Interaction Graph:" + pig);

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

	Util.assert(node.type == PANode.INSIDE, 
		    "This node should be an inside node" + node);

	if (ODMAInfo.Nodes2Status.get(node)==null){
	    ODMAInfo.Nodes2Status.put(node,new ODNodeStatus());
	}
// 	else {
// 	    System.err.println("Processing twice a node... ??? " + node);
// 	}
	ODNodeStatus status = (ODNodeStatus) Nodes2Status.get(node);

	// we are interested in objects allocated in the current thread
	Util.assert(node.isTSpec()==false,
		    "This node should not be a specialization " + node);
	    
	int depth = node.getCallChainDepth();
	System.out.println("CallChainDepth = " + depth); 
	Util.assert(depth==0,
		    "This node should have be created by the current MetaMethod!");

	boolean iscaptured = captured(pig, mm, node);
	pig = pa.getIntParIntGraph(mm);

	if(iscaptured){
	    // captured nodes of depth 0 (ie allocated in this method,
	    // not in a callee) are allocated on the stack.
	    Quad q  = (Quad) node_rep.node2Code(node);
	    Util.assert(q != null, "No quad for " + node);
	    
	    if(stack_alloc_extra_cond(node, q)) {
		MyAP ap = getAPObj(q);
		if (ODMAInfo.MEM_OPTIMIZATION){
		    ap.sa = true;
		    ap.ta = false;
		}
		else{
		    ap.sa = false;
		    ap.ta = false;
		}
		if (ODMAInfo.SYNC_ELIM)
		    ap.ns = true;
		else
		    ap.ns = false;
		// 			if(DEBUG)
		System.out.println("STACK: " + 
				   " was stack allocated " +
				   Debug.getLine(q)  +  " " + node );
		status.onStack = true;
	    }
	}
	else if(tryThread){
	    if(remainInThread(node, hm, "")) {
		System.out.println("node remainInThread");
		Quad q = (Quad) node_rep.node2Code(node);
		Util.assert(q != null, "No quad for " + node);
		
		MyAP ap = getAPObj(q);
		if (ODMAInfo.MEM_OPTIMIZATION){
		    ap.sa = false;
		    ap.ta = true;
		    ap.ah = null; // on the current heap
		}
		else{
		    ap.sa = false;
		    ap.ta = false;
		}
		if (ODMAInfo.SYNC_ELIM)
		    ap.ns = true;
		else
		    ap.ns = false;
		status.onLocalHeap = true;

		if(DEBUG)
		    {
			System.out.print("THREAD: " + " was thread allocated " +
					 Debug.getLine(q) + " " + node );
			if (pig.G.excp.contains(node))
			    System.out.println(" (returned as exception)");
			else
			    System.out.println(" ()");
			System.out.println("Analyzed Meta-Method: " + mm);
		    }
	    }
	    else{
		Quad q = (Quad) node_rep.node2Code(node);
		System.out.print("node escapes of Thread " + Debug.getLine(q) 
				 + " " + node);
		if (pig.G.excp.contains(node))
		    System.out.println(" (returned as exception)");
		else
		    System.out.println(" ()");
	    }
	}
	
	PAThreadMap tau = (PAThreadMap) (pa.getIntParIntGraph(mm).tau.clone());

	if(DO_PREALLOCATION && (tau.activeThreadSet().size() == 1))
	    analyze_prealloc(mm, hcode, pig, tau);

	set_make_heap(tau.activeThreadSet());
    }

    // Returns the INSIDE nodes of level 0 from pig.
    private Set getLevel0InsideNodes(ODParIntGraph pig) {
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
    private boolean escapes_only_in_methods(PANode node, ODParIntGraph pig){
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
    private void analyze_prealloc(MetaMethod mm, HCode hcode, ODParIntGraph pig,
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
		//FV the last test should be refined...
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
	    if (ODMAInfo.MEM_OPTIMIZATION){
		ap.sa = false;
// 		ap.ta = false;
 		ap.ta = true;
		ap.mh = true;  // makeHeap
	    }
	    else{
		ap.sa = false;
		ap.ta = false;
	    }
	    if (ODMAInfo.SYNC_ELIM)
		ap.ns = true;
	    else
		ap.ns = false;

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
	if (ODMAInfo.MEM_OPTIMIZATION){
	    newq_ap.sa = false;
// 	    newq_ap.ta = false;
 	    newq_ap.ta = true;
	    newq_ap.mh = true;  // makeHeap
	}
	else{
	    newq_ap.sa = false;
	    newq_ap.ta = false;
	}

// 	newq_ap.ta = true;       // thread allocation
// 	newq_ap.mh = true;       // makeHeap for the thread object
	if (ODMAInfo.SYNC_ELIM)
	    newq_ap.ns = true;
	else
	    newq_ap.ns = false;
	// newq_ap.ah = newq.dst(); // use own heap
	HClass hclass = getAllocatedType(newq);
	newq_ap.hip = 
	    DefaultAllocationInformation.hasInteriorPointers(hclass);
	
	// the objects pointed by the thread node and which don't escape
	// anywhere else are allocated on the heap of the thread node
	for(Iterator it = news.iterator(); it.hasNext(); ){
	    Quad cnewq = (Quad) it.next();
	    MyAP cnewq_ap = getAPObj(cnewq);
	    if (ODMAInfo.MEM_OPTIMIZATION){
		cnewq_ap.sa = false;
// 		cnewq_ap.ta = false;
 		cnewq_ap.ta = true;
		cnewq_ap.ah = l2;
	    }
	    else{
		cnewq_ap.sa = false;
		cnewq_ap.ta = false;
	    }

// 	    cnewq_ap.ta = true;
// 	    cnewq_ap.ah = l2;
	    if (ODMAInfo.SYNC_ELIM)
		cnewq_ap.ns = true;
	    else
		cnewq_ap.ns = false;

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
    public MyAP getAPObj(Quad q){
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
					   ODParIntGraph pig){
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
    private boolean lostOnlyInCaller(PANode node, ODParIntGraph pig,
				     MetaMethod mm){
	// if node escapes into a method hole it's wrong ...
	System.out.println("lostOnlyInCaller");

	analyzeholes(pig, mm, node);
	pig = pa.getIntParIntGraph(mm);
	if(pig.G.e.hasEscapedIntoAMethod(node)){
	    System.out.println("lostOnlyInCaller: hasEscapedIntoAMethod");
	    return false;
	}

	for(Iterator it=pig.G.e.nodeHolesSet(node).iterator();it.hasNext();){
	    PANode nhole = (PANode)it.next();
	    // if the node escapes through some node that is not a parameter
	    // it's wrong ...
	    if(nhole.type != PANode.PARAM){
		System.out.println("lostOnlyInCaller: escapes through non param nodes "
				   + nhole);
		return false;
	    }
	}

	System.out.println(node + "is lostOnlyInCaller");
	return true;
    }
    
    private static int MAX_LEVEL_BOTTOM_MODE = 10;
    private boolean remainInThreadBottom(PANode node, MetaMethod mm,
					 int level, String ident){
	if(node == null){
	    System.out.println("Null node");
	    return false;
	}

	if(DEBUG)
	    System.out.println(ident + "remainInThreadBottom called for " + 
			       node + " mm = " + mm);

	ODParIntGraph pig = pa.getIntParIntGraph(mm,true);
	
	if (!(pig.allNodes().contains(node))){
	    System.out.println("--escaping (does not belong to the graph)");
	    return false;
	}

	if(captured(pig, mm, node, true, false)){
// 	if(pig.G.captured(node)){
	    if(DEBUG)
		System.out.println(ident + node+ " is captured -> true");
	    return true;
	}
	pig = pa.getIntParIntGraph(mm);
	
	if(!lostOnlyInCaller(node, pig, mm)){
	    if(DEBUG)
		System.out.println(ident + node +
				   " escapes somewhere else -> false");
	    return false;
	}
	pig = pa.getIntParIntGraph(mm);

	if(level == MAX_LEVEL_BOTTOM_MODE){
	    if(DEBUG)
		System.out.println(ident + node + 
				   "max level reached -> false");
	    return false;
	}

// 	//Conservative approximation
// 	if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
// 	    System.out.println("ODA: conservative approximation");
// 	    return false;
// 	}

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
	    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS)
		if (mm.equals(callers[i])){
		    System.out.println("Identical to callee: skipping");
		}
		else{
		    int old_depth = pa.current_analysis_depth;
		    pa.current_analysis_depth=0;
		    ODParIntGraph caller_pig = (ODParIntGraph)
			pa.getIntParIntGraph(callers[i],true).clone();
		    pa.current_analysis_depth=old_depth;
		    pa.hash_proc_int_d[pa.current_analysis_depth].put(callers[i],
								      caller_pig);
		    pa.hash_proc_ext_d[pa.current_analysis_depth].put(callers[i],
								      caller_pig.clone());
		    System.out.println("Analyzing " + callers[i]);
		    analyze_call(callers[i], mm.getHMethod());
		}
	

	    if (!mm.equals(callers[i]))
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

	if(node.getCallChainDepth() == ODPointerAnalysis.MAX_SPEC_DEPTH){
	    System.out.println(ident + node + " is too old -> might escape");
	    return false;
	}

 	MetaMethod mm = new MetaMethod(hm, true);
 	ODParIntGraph pig = pa.getIntParIntGraph(mm,true);

	System.out.println(ident + "remainInThread called for " +
			   node + "  hm = " + hm);
// 	System.out.println(pig);

	Util.assert(pig != null, "pig is null for hm = " + hm + " " + mm);
	
	if (!(pig.allNodes().contains(node))){
	    System.out.println("--escaping (does not belong to the graph)");
	    return false;
	}

// 	if(pig.G.captured(node)){
	if(captured(pig, mm, node, true, false)){
	    if(DEBUG)
		System.out.println(ident + node+ " is captured -> true");
	    return true;
	}
	pig = pa.getIntParIntGraph(mm);
	
	if(!lostOnlyInCaller(node, pig, mm)){
	    if(DEBUG)
		System.out.println(ident + node +
					 " escapes somewhere else -> false");
	    return false;
	}
	pig = pa.getIntParIntGraph(mm);

	if(node.getCallChainDepth() == ODPointerAnalysis.MAX_SPEC_DEPTH - 1){
	    if(DEBUG)
		System.out.println(ident + node + 
				   " is almost too old and uncaptured -> " + 
				   "bottom mode");
	    boolean retval = remainInThreadBottom(node, mm, 0, ident);
	    if(DEBUG)
		System.out.println(ident + node + " " + retval);
	    return retval;
	}
	
	if (PointerAnalysis.CALL_CONTEXT_SENSITIVE)
	    System.out.println("CONTEXT_SENSITIVE");
	else
	    System.out.println("CONTEXT_insensitive :-(");

	System.out.println("FV Node " + node);
	System.out.println("FV spec " + node.getAllCSSpecs());

	// In the case of On Demand Analysis, we must ensure that all
	// the specializations of the current node where created (one
	// depth further)
	if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
	    // Find all the metamethods which call the current
	    // metamethod
	    System.out.println("\nMetaMethod : " + mm);
	    System.out.println("HMethod : " + mm.getHMethod());
	    MetaMethod[] callers = mac.getCallers(mm);
	    // Analyze them for the current meta method
// 	    System.out.println("Callers:");
	    for(int i = 0; i < callers.length; i++){
		if (mm.equals(callers[i])){
		    System.out.println("Identical to callee: skipping");
		}
		else{
		    int old_depth = pa.current_analysis_depth;
		    pa.current_analysis_depth=0;
		    ODParIntGraph caller_pig = (ODParIntGraph)
			pa.getIntParIntGraph(callers[i],true).clone();
		    pa.current_analysis_depth=old_depth;
		    pa.hash_proc_int_d[pa.current_analysis_depth].put(callers[i],
								      caller_pig);
		    pa.hash_proc_ext_d[pa.current_analysis_depth].put(callers[i],
								      caller_pig.clone());
		    System.out.println("Analyzing " + callers[i]);
		    analyze_call(callers[i], mm.getHMethod());
		}
	    }
	}

	System.out.println("FV Node " + node);
	System.out.println("FV spec " + node.getAllCSSpecs());

	for(Iterator it = node.getAllCSSpecs().iterator(); it.hasNext(); ){
	    Map.Entry entry = (Map.Entry) it.next();
	    CALL   call = (CALL) entry.getKey();
	    PANode spec = (PANode) entry.getValue();
	    
	    QuadFactory qf = call.getFactory();
	    HMethod hm_caller = qf.getMethod();
	    MetaMethod mm_caller = new MetaMethod(hm_caller, true);
	    if (mm.equals(mm_caller)){
		System.out.println("Identical to callee: skipping");
	    }
	    else{
		

		if(!remainInThread(spec, hm_caller, ident + " ")){
		    if(DEBUG)
			System.out.println(ident + node +
					   " might escape -> false");
		    return false;
		}
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

// 	ODMAInfo.do_additional_testing(hcf);
    }
    
    public static void do_additional_testing(HCodeFactory hcf) {
	System.out.println("ADDITIONAL TESTING of java.util.AbstractList.equals");
	HMethod hm = get_hmethod_for_name("java.util.AbstractList","equals");
	HCode hcode = hcf.convert(hm);
	AllocationInformation ai = ((harpoon.IR.Quads.Code) hcode).getAllocationInformation();
	System.out.println("NEW sites inside it");
	for(Iterator it = hcode.getElementsI(); it.hasNext(); ) {
	    Quad quad = (Quad) it.next();
	    if(!(quad instanceof NEW)) continue;
	    NEW qn = (NEW) quad;
	    MyAP ap = (MyAP) ai.query(qn);
	    System.out.println(Debug.code2str(qn) + "->" + ap);			       
	}
	System.out.println("--------------------");
    }

    private static HMethod get_hmethod_for_name(String cls, String mthd) {
	HClass hclass = linker.forName(cls);
	HMethod[] ms = hclass.getMethods();
	for(int i = 0; i < ms.length; i++) {
	    HMethod hm = ms[i];
	    if(hm.getName().equals(mthd))
		return hm;
	}
	return null;
    }

    private void generate_inlining_hints(MetaMethod mm, ODParIntGraph pig){
	generate_inlining_hints(mm, pig, null);
    }

    private void generate_inlining_hints(MetaMethod mm, ODParIntGraph pig, Set candidates){
// 	if (DEBUG)
	    System.out.println("In generate_inlining_hints " + mm);

	HMethod hm  = mm.getHMethod();
	HCode hcode = hcf.convert(hm);
// 	if(hcode.getElementsL().size() > MAX_INLINING_SIZE) return;

	// obtain in A the set of nodes that might be captured after inlining 
	Set level0 = getLevel0InsideNodes(pig);
	if (candidates!=null) 
	    level0.retainAll(candidates);
// 	System.out.println("original candidates " + level0);
	Set A = new HashSet();
	for(Iterator it = level0.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
// 	    if(!pig.G.captured(node) && lostOnlyInCaller(node, pig, mm)) {
	    if(!captured(pig, mm, node)){
		pig = pa.getIntParIntGraph(mm);
		if(lostOnlyInCaller(node, pig, mm)) {
		    pig = pa.getIntParIntGraph(mm);
		    // we are not interested in stack allocating the exceptions
		    // since they  don't appear in normal case and so, they
		    // are not critical for the memory management
		    HClass hclass = getAllocatedType(node_rep.node2Code(node));
		    if(!java_lang_Throwable.isSuperclassOf(hclass))
			A.add(node);
		}
	    }
	}

	if(A.isEmpty()) {
// 	    System.out.println("No candidate for inlining");
	    return;
	}
	else{
// 	    System.out.println("Candidates for inlining " + A);
	}

// 	System.out.println("Looking for " + mm);
	// very dummy 1-level inlining
	int n_sites=0;
	MetaMethod[] callers = mac.getCallers(mm);
	for(int i = 0; i < callers.length; i++) {
	    MetaMethod mcaller = callers[i];
	    HMethod hcaller = mcaller.getHMethod();
//  	    System.out.println("Caller : " + mcaller);
	    for(Iterator it = mcg.getCallSites(mcaller).iterator();
		it.hasNext(); ) {
		CALL cs = (CALL) it.next();
		MetaMethod[] callees = mcg.getCallees(mcaller, cs);

		if (callees.length==0) continue;
		if (callees[0].equals(mm))
		    n_sites++;

		if((callees.length == 1) && (callees[0].equals(mm)) && good_cs(cs)){
		    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
			int old_depth = pa.current_analysis_depth;
			pa.current_analysis_depth=0;
			ODParIntGraph caller_pig = (ODParIntGraph)
			    pa.getIntParIntGraph(mcaller,true).clone();
			pa.current_analysis_depth=old_depth;
			pa.hash_proc_int_d[pa.current_analysis_depth].
			    put(mcaller,caller_pig);
			pa.hash_proc_ext_d[pa.current_analysis_depth].
			    put(mcaller,caller_pig.clone());
// 			System.out.println("In generate_inlining_hints: " + 
// 					   "Analyzing " + mcaller);
			analyze_call(mcaller, mm.getHMethod());
		    }
		    try_inlining(mcaller, cs, A);
		}
	    }
	}
	
	for(Iterator it = A.iterator(); it.hasNext(); ) {
	    PANode n = (PANode) it.next();
	    ODNodeStatus status = (ODNodeStatus) Nodes2Status.get(n);
	    if (status==null) {
		System.err.println("Problem somewhere with Nodes2Status");
		continue;
	    }
	    status.nCallers = n_sites;
	}



    }


    
    public void generate_inlining_hints(MetaMethod mm, 
					Set candidates, Set winners,
					MetaMethod mcaller, CALL cs){
// 	if (DEBUG)
	    System.out.println("In generate_inlining_hints " + mm);

	HMethod hm  = mm.getHMethod();
	HCode hcode = hcf.convert(hm);
	ODParIntGraph pig = (ODParIntGraph) pa.getIntParIntGraph(mm);

	// obtain in A the set of nodes that might be captured after inlining 
	Set A = new HashSet();
	for(Iterator it = candidates.iterator(); it.hasNext(); ) {
	    PANode node = (PANode) it.next();
	    if(!captured(pig, mm, node)){
		pig = pa.getIntParIntGraph(mm);
		if(lostOnlyInCaller(node, pig, mm)) {
		    pig = pa.getIntParIntGraph(mm);
		    // we are not interested in stack allocating the exceptions
		    // since they  don't appear in normal case and so, they
		    // are not critical for the memory management
		    HClass hclass = getAllocatedType(node_rep.node2Code(node));
		    if(!java_lang_Throwable.isSuperclassOf(hclass))
			A.add(node);
		}
	    }
	}

	if(A.isEmpty()) {
	    System.out.println("No candidate for inlining");
	    return;
	}
	else{
	    System.out.println("Candidates for inlining " + A);
	}

	// very dummy 1-level inlining
	HMethod hcaller = mcaller.getHMethod();
	MetaMethod[] callees = mcg.getCallees(mcaller, cs);
	System.out.println("callees : " + callees.length);
	if(callees.length==0){
	    System.err.println("Call site with no callees !!!");
	    System.out.println("Call site with no callees !!!");
	    return;
	}
	System.out.println("callees[0].equals(mm) " + callees[0].equals(mm)
			   + callees[0] + mm);
	System.out.println("good_cs(cs) " + good_cs(cs));
	if((callees.length == 1) && (callees[0].equals(mm)) && good_cs(cs)){
	    System.out.println(" no problem");
	    if (ODPointerAnalysis.ON_DEMAND_ANALYSIS){
		int old_depth = pa.current_analysis_depth;
		pa.current_analysis_depth=0;
		ODParIntGraph caller_pig = (ODParIntGraph)
		    pa.getIntParIntGraph(mcaller,true).clone();
		pa.current_analysis_depth=old_depth;
		pa.hash_proc_int_d[pa.current_analysis_depth].
		    put(mcaller,caller_pig);
		pa.hash_proc_ext_d[pa.current_analysis_depth].
		    put(mcaller,caller_pig.clone());
		analyze_call(mcaller, mm.getHMethod());
	    }
	    try_inlining(mcaller, cs, A, winners);
	}
    }


    private static HClass java_lang_Throwable = null;

    /* Normally, we should refuse to inline calls that are inside loops
       because that + stack allocation might lead to stack overflow errors.
       However, at this moment we don't test this condition. */
    private boolean good_cs(CALL cs){
	return true;
    }

    private void try_inlining(MetaMethod mcaller, CALL cs, Set A) {
	try_inlining(mcaller, cs, A, new HashSet());
    }

    private void try_inlining(MetaMethod mcaller, CALL cs, Set A, Set C) {
	ODParIntGraph caller_pig = pa.getIntParIntGraph(mcaller,true);

	System.out.println("Inside try_inlining");

	Set B = new HashSet();
	for(Iterator it = A.iterator(); it.hasNext(); ){
	    PANode node = (PANode) it.next();
	    PANode spec = node.csSpecialize(cs);
	    if(spec == null) continue;
	    if(captured(caller_pig, mcaller, spec, true, false)){
		B.add(spec);
		C.add(node);
		caller_pig = pa.getIntParIntGraph(mcaller);
	    }
	}

	// no stack allocation benefits from this inlining
	if(B.isEmpty())
	    return;

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

// 	if(DEBUG) 
	{
	    System.err.println("INLINING HINT: " + Debug.code2str(cs));
	    System.out.println("\nINLINING HINT: " + Debug.code2str(cs));
	    System.out.println("NEW STACK ALLOCATION SITES:");
	    for(int i = 0; i < news_array.length; i++)
		System.out.println(" " + Debug.code2str(news_array[i]));
	}
    }

    public void do_the_inlining(){
	do_the_inlining(hcf,ih);
    }

    private void do_the_inlining(HCodeFactory hcf, Map ih){
	SCComponent scc = reverse_top_sort_of_cs(ih);
	Set toPrune=new WorkSet();
	while(scc != null) {
	    if(DEBUG) {
		System.out.println("Processed SCC:{");
		Object[] nodes = scc.nodes();
		for(int i = 0; i < nodes.length; i++)
		    System.out.println(" " + Debug.code2str((CALL) nodes[i]));
		System.out.println("}");
	    }
	    Object[] nodes = scc.nodes();
	    for(int i = 0; i < nodes.length; i++) {
		CALL cs = (CALL) nodes[i];
		inline_call_site(cs, hcf, ih);
		toPrune.add(cs.getFactory().getParent());
	    }
	    scc = scc.prevTopSort();
	}
	for(Iterator pit=toPrune.iterator();pit.hasNext();)
	    Unreachable.prune((HEADER)((HCode)pit.next()).getRootElement());
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

	Set cs_set = ih.keySet();
	CALL[] cs_array = (CALL[]) cs_set.toArray(new CALL[cs_set.size()]);

	SCCTopSortedGraph sccts =
	    SCCTopSortedGraph.topSort(SCComponent.buildSCC(cs_array, nav));

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

	HCode hcode = hcf.convert(caller);
	((harpoon.IR.Quads.Code) hcode).setAllocationInformation(this);

// 	AllocationInformation ai = ((harpoon.IR.Quads.Code) hcode).getAllocationInformation();
// 	System.out.println("1. " + (ai==null?
// 			   (caller + " baaad!") :
// 			   (caller + " good!")));
	
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

// 	hcode = hcf.convert(caller);
// 	ai = ((harpoon.IR.Quads.Code) hcode).getAllocationInformation();
// 	System.out.println("2. " + (ai==null?
// 			   (caller + " baaad!") :
// 			   (caller + " good!")));

    }


    private void extra_stack_allocation(CALL cs, Map ih, Map old2new) {
	Quad[] news = (Quad[]) ih.get(cs);
	for(int i = 0; i < news.length; i++) {
	    Quad q  = (Quad) old2new.get(news[i]);
	    Util.assert(q != null, "no new Quad for " + news[i]);
	    MyAP ap = new MyAP(getAllocatedType(q));

	    System.out.println("New Stack Allocation " + Debug.code2str(q));

 	    if (ODMAInfo.MEM_OPTIMIZATION)
		ap.sa = true;
  	    else
  		ap.sa = false;
	    if (ODMAInfo.SYNC_ELIM)
		ap.ns = true;
	    else
		ap.ns = false;
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

	MetaMethod mcaller = new MetaMethod(caller, true);	
	MetaMethod[] callees = mcg.getCallees(mcaller, cs);
	Util.assert(callees.length == 1, "not exactly one callee in " + cs);
	HMethod hm = callees[0].getHMethod();

	HCode hcode_orig = hcf.convert(hm);

// 	System.out.println("caller = " + caller);
// 	System.out.println("hcode_orig " + hcode_orig);
// 	System.out.println("\n\nhm " + hm);

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


    public boolean captured(ODParIntGraph pig, 
			    MetaMethod current_mmethod, 
			    PANode node) 
    {
	System.out.println("Inside ODMAInfo.captured() for " + node);
	return captured(pig, current_mmethod, node, true, true);

    }

    public boolean analyzeholes(ODParIntGraph pig, 
				MetaMethod current_mmethod, 
				PANode node) 
    {
	System.out.println("Inside ODMAInfo.analyzeholes() for " + node);

	boolean result = captured(pig, current_mmethod, node, false, false);
//  	System.out.println("PIG after analysis");
//  	System.out.println(pig);
	return result;

    }

    public boolean captured(ODParIntGraph pig, 
			    MetaMethod current_mmethod, 
			    PANode node,
			    boolean methodcapture) 
    {
	return captured(pig, current_mmethod, node, methodcapture, false);
    }

    public boolean captured(ODParIntGraph pig, 
			    MetaMethod current_mmethod, 
			    PANode node,
			    boolean methodcapture,
			    boolean verbose) 
    {
	verbose = false;

	if(verbose)
	    {
		System.out.println("  pig B4 analysis");
		System.out.println(pig);
	    }	

	if (!(pig.allNodes().contains(node))){
	    System.err.println("--escaping (does not belong to the graph)");
	    System.out.println("--escaping (does not belong to the graph)");
	    return false;
	}
	
	if (pig.G.captured(node)){
	    System.out.println("--captured (no analysis) " + node);
// 	    System.out.println(pig);
	    return true;	
	}
	else if ((pa.BOUNDED_ANALYSIS_DEPTH==false)
		 ||(pa.MAX_ANALYSIS_DEPTH==0)){
	    System.out.println("--escaping (no recursive analysis) " + node);
	    return false;	
	}

	int depth = 1;
	do{

	    if (pig.G.captured(node)){
		System.out.println("--captured (no analysis) " + node);
// 		System.out.println(pig);
		prepare_exit(current_mmethod);
		return true;	
	    }
	    
	    // If the node escape through a return node, there is nothing
	    // to be done.
	    if ((methodcapture)&&(pig.G.willEscape(node))){
		System.out.println("--escaping (no analysis) " + 
				   node + " (returned)");
		prepare_exit(current_mmethod);
		return false;
	    }

	    // If the node escapes through a node (either a parameter node
	    // or a thread node), there is nothing to be done.
// 	    if ((methodcapture)&&(pig.G.e.hasEscapedIntoANode(node))){
	    if (pig.G.e.hasEscapedIntoANode(node)){
		if (!methodcapture){
		    for(Iterator n_it=pig.G.e.nodeHolesSet(node).iterator(); n_it.hasNext(); ){
			PANode n = (PANode) n_it.next();
			if (n.type != PANode.PARAM)
			    {
				System.out.println("--escaping (no analysis) " + 
						   node + " (through non param node " + n
						   + ") in");
// 				System.out.println(pig);
				prepare_exit(current_mmethod);
				return false;
			    }
		    }
		}
		else {
		    System.out.println("--escaping (no analysis) " + 
				       node + " (through a node)");
		    prepare_exit(current_mmethod);
		    return false;
		}
// 		Set nodeholes = (Set) pig.G.e.nodeHolesSet(node);
// 		System.out.print("  " + node + " :");
// 		for(Iterator nn_it=nodeholes.iterator(); nn_it.hasNext(); ) {
// 		    PANode nn_esc_ = (PANode)nn_it.next();
// 		    System.out.print(" " + nn_esc_);
// 		}
// 		System.out.println(" ");
		

	    }


	    // If the node does not escape through a method hole, there is
	    // nothing to be done.
	    if (!pig.G.e.hasEscapedIntoAMethod(node)){
		if ((methodcapture)&&(depth==1)){
		    System.err.println("--escaping (no analysis) " + 
				       node + " (not through method) ERROR ?");
		    System.out.println("--escaping (no analysis) " + 
				       node + " (not through method) ERROR ?");
		    if(verbose)
			System.out.println(pig);
		}
		else{
		    System.out.println("--escaping " + 
				       node + " (not through method)");
		    System.out.println(pig);
		}
		prepare_exit(current_mmethod);
		return false;
	    }

	    boolean holefound = true;
	    
	    do{
		// Set of unanalyzed methods for current_mmethod
		Set mm_holeset = (Set)pig.odi.skippedCS;
		
// 		System.out.println("\n\n pig.method_holes : " + mm_holeset);

		// If all analyzable methods were already analyzed
		if ((mm_holeset==null)||(mm_holeset.isEmpty())){
		    System.out.println(" All analyzable methods were "
				       + "already analyzed...");
		    System.out.println("--escaping " + node);
		    prepare_exit(current_mmethod);
		    return false;
		}

		// Set of unanalyzed methods for node
		Set methodholes = (Set) pig.G.e.methodHolesSet(node);
// 		System.out.println("\n\nunanalyzed methods for the node " + methodholes);

		
		
		// Select the callsites the studied node escapes through
		HashSet callsites = new HashSet();
		HashSet hmethods_set = new HashSet();
		holefound = false;
		MethodHole thehole = null;

		for(Iterator hole_it=mm_holeset.iterator(); hole_it.hasNext(); ) {
		    MethodHole hole = (MethodHole)hole_it.next();
		    // The node must be reachable from the parameters
		    if (hole.arguments()==null)
			System.err.println(hole);
		    else 
			if (pig.G.reachableNodes(hole.arguments()).contains(node))
			    {
				hmethods_set.add(hole.method());
				callsites.add(hole);
				// It must be of the current depth
				if (hole.depth()==depth){
				    holefound = true;
				    thehole  = hole;
				}
			    }
		}

		if (!holefound) {
		    System.out.println("No hole found " + depth);
		    continue;
		}

		// The node does not escape through an unanalyzed method...
		if (callsites==null){
		    if(verbose) System.out.println(" The node does not escape through an " +
				       "unanalyzed method... (callsites null)");
		    System.err.println(" The node does not escape through an " +
				       "unanalyzed method... (callsites null)");
		    System.out.println("--escaping " + node);
		    prepare_exit(current_mmethod);
		    return false;
		}

		if (callsites.isEmpty()){
		    System.out.println(" The node does not escape through an " +
				       "unanalyzed method... (callsites isEmpty)" 
				       + node);
		    System.err.println(" The node does not escape through an " +
				       "unanalyzed method... (callsites isEmpty)");
		    System.out.println("--escaping " + node);
		    prepare_exit(current_mmethod);
		    return false;
		}

		Set arguments = thehole.arguments();
		CALL q     = thehole.callsite();
		HMethod hm = q.method();
		System.out.println("****method hole to be filled:" + hm);
		
		
		
		// Set of unanalyzed method holes for current_mmethod
		Set mm_holes = (Set)pig.odi.skippedCS;
		
		// Check whether, in the remaining unanalyzed call
		// sites, some correspond to the same HMethod, or
		// originally correspond to the same call site.
		boolean hm_esc  = false;
		HashSet hmnodes = new HashSet();
		HashSet hmnodes_ret = new HashSet(); 
		HashSet hmnodes_exc = new HashSet(); 
		HashSet annoyingCS = new HashSet();
		Set mm_holes_whithout_hole = new HashSet(pig.odi.skippedCS);
		mm_holes_whithout_hole.remove(thehole);
		boolean unique = true;
		PANode ret = thehole.ret();
		PANode exc = thehole.exc();

		for(Iterator it = mm_holes_whithout_hole.iterator(); it.hasNext(); ){
		    MethodHole mh = (MethodHole) it.next();
		    CALL cs = mh.callsite();
		    if (hm.equals(cs.method())){
			annoyingCS.add(mh);
			hm_esc = true;
			hmnodes_ret.addAll(mh.parameters());
			hmnodes_exc.addAll(mh.parameters());
			hmnodes.addAll(mh.arguments());
			if (mh.ret()!=null){
			    if (mh.ret()!=ret){
				hmnodes_ret.add(mh.ret());
				hmnodes_exc.add(mh.ret());
			    }
			    else{
				hmnodes_exc.add(mh.ret());
			    }
			}
			if (mh.exc()!=null){
			    if (mh.exc()!=exc){
				hmnodes_ret.add(mh.exc());
				hmnodes_exc.add(mh.exc());
			    }
			    else{
				hmnodes_ret.add(mh.exc());
			    }
			}
		    }
		    if ((unique)&&((mh.ret()==ret)||(mh.exc()==exc))){
			unique = false;
		    }
		}

		if (hm_esc==false){
		    // No more unanalyzed call sites refer to the same
		    // HMethod than the actual call site. We safely
		    // remove this HMethod from the escaping ones.
		    //
		    // CAUTION: we make the assumption that if a call
		    // site on method hm is unanalyzed and appears in
		    // pig.method_holes, this is also the case for all
		    // the (unanalyzed) call sites on method hm.
  		    if(DEBUG) 
			System.out.println("  no other call site has same hmethod");

		    Set oldhole = new HashSet();
		    oldhole.add(hm);
		    pig.G.e.removeMethodHoles(oldhole);
		    // tbu to be optimized with singleton...
		}
		else{
		    if(DEBUG)
			System.out.println("  there is at least one "
					   + "other call site "
					   + "with same hmethod");
		    
		    // Nodes reachable from the call site we are going to analyze.
		    Set reachable = pig.G.reachableNodes(arguments);
		    if(DEBUG)
		        System.out.println("reachable from the call site " +
					   reachable);

		    
		    // Nodes reachable from perturbing call sites.
		    Set perturbed = pig.G.reachableNodes(hmnodes);
  		    if(DEBUG)
			{
			    System.out.println("reachable from perturbing call sites " +
					       perturbed);
			    System.out.println("Perturbing method holes ");
			}

		    // Nodes reachable from the call site we are going to
		    // analyze but not from perturbing call sites. These
		    // are the nodes we can update the escape function.
		    reachable.removeAll(perturbed);
   		    if(DEBUG)
			System.out.println("nodes than can be updated " +
					   reachable);
		    
		    // Update of escape functions
		    Iterator upd_it = reachable.iterator();
		    while(upd_it.hasNext()){
			PANode upd_n = (PANode) upd_it.next();
 			if(verbose) 
			    System.out.println("    " + upd_n + " updated !!!");
			pig.G.e.removeMethodHole(upd_n, hm);
		    }

		    // Checking whether the params are still escaping
		    for(Iterator par_it=arguments.iterator(); par_it.hasNext(); ) {
			PANode param = (PANode) par_it.next();
			if ((!pig.G.willEscape(param))&&(!pig.G.e.hasEscaped(param)))
			    pig.G.e.removeNodeHoleFromAll(param);
		    }
		    
		    // Checking whether the actual return and
		    // exception nodes will still be escaping into the
		    // HMethod of the current hole, due to some other
		    // holes in the caller, if there are some other
		    // holes corresponding to the same call site
		    if(!unique){
			if(pig.G.reachableNodes(hmnodes_ret).contains(thehole.ret()))
			    // Still going to escape
			    ODInterProcPA.ret_strong_update = false;
			else
			    ODInterProcPA.ret_strong_update = true;

			if(pig.G.reachableNodes(hmnodes_exc).contains(thehole.exc()))
			    // Still going to escape
			    ODInterProcPA.exc_strong_update = false;
			else
			    ODInterProcPA.exc_strong_update = true;
		    }
		}

		// The method-holes set is updated.
		pig.odi.skippedCS = mm_holes;
		
  		if(verbose) 
		    System.out.println("  pig before analysis " + pig);
		if(verbose) System.out.println("  Nodes ");
		Iterator parsetit=(pig.allNodes()).iterator();
		if(verbose) System.out.print("    ");
		while(parsetit.hasNext()){
		    PANode n = (PANode) parsetit.next();
		    if(verbose) System.out.print(" " + n + "(" + n.details() + ")");
		}
		if(verbose) System.out.println(" .");
		
		// Finally the method is analyzed
		if(verbose) System.out.println("   before call on hole! " + thehole);
		ODParIntGraphPair pp = 
		    ODInterProcPA.analyze_call(pa, current_mmethod,
				 	       thehole.callsite(), pig, 
					       thehole,
					       verbose,
					       unique);    

		pig = pp.pig[0];

		//tbu
// 		pa.hash_proc_int_d[pa.current_analysis_depth].put(current_mmethod,pig.clone());
		pa.hash_proc_int_d[pa.current_analysis_depth].put(current_mmethod,pig);
// 		ODParIntGraph new_pig = pp.pig[1];
// 		pa.hash_proc_ext_d[pa.current_analysis_depth].put(current_mmethod,new_pig);

// 		ODParIntGraph new_pig = (ODParIntGraph) pig.clone();
// 		if(current_mmethod.getHMethod().getReturnType().isPrimitive())
// 		    new_pig.G.r.clear();
// 		if(current_mmethod.getHMethod().getExceptionTypes().length == 0)
// 		    new_pig.G.excp.clear();
// 		System.out.println("  pig after analysis and cleaning" + pig);
// 		System.out.println("  Real value: (" + current_mmethod + ")");
// 		System.out.println(pa.getIntODParIntGraph(current_mmethod));
	    }
	    while(holefound);
	    
	    depth++;
	}
	while((depth<=pa.MAX_ANALYSIS_DEPTH)&&(!pig.G.captured(node)));

// 	if(depth>pa.MAX_ANALYSIS_DEPTH){
// 	    System.err.println("Maximal depth reached for node " + node);
// 	    System.out.println("Maximal depth reached for node " + node);
// 	    System.out.println("Holes " + pig.G.e.methodHolesSet(node));
// 	}

	prepare_exit(current_mmethod);

	if(verbose) System.out.println("  pig after loop " + pig);

   	System.out.println("  pig after WHOLE analysis " + current_mmethod);
   	System.out.println(pig);
	
	if (pig.G.captured(node))
	    System.out.println("--captured " + node);
	else
	    System.out.println("--escaping " + node);
	return pig.G.captured(node);
    }


    private void prepare_exit(MetaMethod mm){
	ODParIntGraph new_pig = (ODParIntGraph)
	    ((ODParIntGraph) pa.hash_proc_int_d[pa.current_analysis_depth].get(mm))
	    .clone();
	pa.hash_proc_ext_d[pa.current_analysis_depth].put(mm,new_pig);
    }

    public void analyze_call(MetaMethod mm, 
			     HMethod    hole) {

	boolean holefound = true;
	boolean unique    = true;
	ODParIntGraph pig = pa.getIntParIntGraph(mm,true);
	
	System.out.println("Analyze Call on " + hole +
			   "\n in " + mm + "\n with pig ");
//   	System.out.println(pig);

	do{
	    // Set of unanalyzed methods for current_mmethod
	    Set mm_holeset = (Set) pig.odi.skippedCS;
		
	    // If all analyzable methods were already analyzed
	    if ((mm_holeset==null)||(mm_holeset.isEmpty())){
		System.out.println(" All analyzable methods were "
				   + "already analyzed...");
		prepare_exit(mm);
		return;
	    }
		
	    // Select the callsites corresponding to the targeted hmethod
	    HashSet callsites = new HashSet();
	    holefound = false;
	    MethodHole thehole = null;

	    for(Iterator hole_it=mm_holeset.iterator(); hole_it.hasNext(); ) {
		MethodHole c_hole = (MethodHole)hole_it.next();
		// The HMethod of one of its specialization
		// (MetaMethods) must be the targeted one
		MetaMethod[] mms = c_hole.callees();
		for(int i=0; (i<mms.length); i++){
		    if (hole.equals(mms[i].getHMethod()))
			// It must be of depth 1
			if ((c_hole.depth()==1)&&(!holefound)){
			    holefound = true;
			    thehole  = c_hole;
			}
			else {
			    callsites.add(c_hole);
			}
		}
	    }

	    if (!holefound){
		if (!callsites.isEmpty()){
// 		    System.err.print("ERROR Depth of the found call sites : ");
		    System.out.print("ERROR Depth of the found call sites : ");
		    for (Iterator cs_it=callsites.iterator(); (cs_it.hasNext()); ){
			MethodHole _hole_ = (MethodHole) cs_it.next();
// 			System.err.print(_hole_.depth() + " ");
			System.out.print(_hole_.depth() + " ");
		    }
// 		    System.err.println();
		    System.out.println();
		}
		continue;
	    }

	    Set arguments = thehole.arguments();
	    CALL q     = thehole.callsite();
	    HMethod hm = q.method();
 	    System.out.println("****method hole to be filled:" + hm 
			       + " (" + thehole.rank()+ ")");

	    // Set of unanalyzed method holes for current_mmethod
	    Set mm_holes = (Set)pig.odi.skippedCS;
		
	    // We remove the call site that we are going to analyze
	    //mm_holes.remove(thehole);
	    
	    // Check whether, in the remaining unanalyzed call sites,
	    // one correspond to the same HMethod or one originally
	    // corresponds to the same call site.
	    boolean hm_esc  = false;
	    if (callsites.size()!=0) hm_esc = true;
	    unique = true;

	    if (hm_esc){
		PANode ret = thehole.ret();
		PANode exc = thehole.exc();
		for (Iterator cs_it=callsites.iterator(); 
		     (unique&&(cs_it.hasNext())); )
		    {
			MethodHole cur_mh = (MethodHole) cs_it.next();
			if ((cur_mh.ret()==ret)||(cur_mh.exc()==exc))
			    unique = false;
		    }
	    }

	    System.out.println("MethodHole : " + thehole);


	    if (hm_esc==false){
		// No more unanalyzed call sites refer to the same
		// HMethod than the actual call site. We safely
		// remove this HMethod from the escaping ones.
		//
		// CAUTION: we make the assumption that if a call
		// site on method hm is unanalyzed and appears in
		// pig.odi.skippedCS, this is also the case for all
		// the (unanalyzed) call sites on method hm.

		System.out.println("No other call sites refer to the same hmethod");
		System.out.println("Removing " + hm);
		
		Set oldhole = new HashSet();
		oldhole.add(hm);
		pig.G.e.removeMethodHoles(oldhole);
		// tbu to be optimized with singleton...
	    }
	    else{
// 		System.out.println("AT LEAST one other call sites refer to the same hmethod "
// 				   + callsites);

		// Nodes reachable from the call site we are going to analyze.
		Set reachable = pig.G.reachableNodes(arguments);
// 		System.out.println("Nodes escaping through the call site " +
// 				   " we are going to analyze : " +
// 				   reachable);
		
		// Nodes reachable from perturbing call sites.
		HashSet hmnodes = new HashSet();
		HashSet hmnodes_ret = new HashSet(); 
		HashSet hmnodes_exc = new HashSet(); 
		PANode ret = thehole.ret();
		PANode exc = thehole.exc();

		for(Iterator it = callsites.iterator(); it.hasNext(); ){
		    MethodHole mh = (MethodHole) it.next();
		    hmnodes_ret.addAll(mh.parameters());
		    hmnodes_exc.addAll(mh.parameters());
		    hmnodes.addAll(mh.arguments());
		    if (mh.ret()!=null){
			if (mh.ret()!=ret){
			    hmnodes_ret.add(mh.ret());
			    hmnodes_exc.add(mh.ret());
			}
			else{
			    hmnodes_exc.add(mh.ret());
			}
		    }
		    if (mh.exc()!=null){
			if (mh.exc()!=exc){
			    hmnodes_ret.add(mh.exc());
			    hmnodes_exc.add(mh.exc());
			}
			else{
			    hmnodes_ret.add(mh.exc());
			}
		    }
		}
		Set perturbed = pig.G.reachableNodes(hmnodes);
// 		System.out.println("Nodes escaping through other call sites " +
// 				   " with same hmethod : " +
// 				   perturbed);

		// Nodes reachable from the call site we are going to
		// analyze but not from perturbing call sites. These
		// are the nodes we can update the escape function.
		reachable.removeAll(perturbed);
// 		System.out.println("Nodes escaping from the hole that we can update: "
// 				   + reachable);


		    
		// For debugging purposes
// 		for(Iterator hole_it=mm_holes.iterator(); hole_it.hasNext(); ) {
// 		    MethodHole hole_m = (MethodHole)hole_it.next();
// 		    System.out.print(" ");
// 		    for(Iterator par_it=
// 			    (pig.G.reachableNodes(hole_m.parameters())).iterator(); 
// 			par_it.hasNext(); ) 
// 			{
// 			    PANode __par__it = (PANode) par_it.next();
// 			    System.out.print(" " + __par__it);
// 			}
// 		    System.out.println(" : " + (hole_m.callsite()).method());
// 		}
		    
		// Update of escape functions
		Iterator upd_it = reachable.iterator();
		while(upd_it.hasNext()){
		    PANode upd_n = (PANode) upd_it.next();
		    pig.G.e.removeMethodHole(upd_n, hm);
		}
		  
		// Checking whether the params are still escaping
		for(Iterator par_it=arguments.iterator(); par_it.hasNext(); ) {
		    PANode param = (PANode) par_it.next();
		    if ((!pig.G.willEscape(param))&&(!pig.G.e.hasEscaped(param)))
			pig.G.e.removeNodeHoleFromAll(param);
		}
		
		// Checking whether the actual return and exception
		// nodes will still be escaping into the HMethod of
		// the current hole, due to some other holes in the
		// caller, if there are some other holes corresponding
		// to the same call site
		if(!unique){
		    if(pig.G.reachableNodes(hmnodes_ret).contains(thehole.ret()))
			// Still going to escape
			ODInterProcPA.ret_strong_update = false;
		    else
			ODInterProcPA.ret_strong_update = true;
		    
		    if(pig.G.reachableNodes(hmnodes_exc).contains(thehole.exc()))
			// Still going to escape
			ODInterProcPA.exc_strong_update = false;
		    else
			ODInterProcPA.exc_strong_update = true;
		}
	    }
		
	    // The method-holes set is updated.
	    pig.odi.skippedCS = mm_holes;

	    ODParIntGraphPair pp = 
		ODInterProcPA.analyze_call(pa, mm,
					   thehole.callsite(), pig, thehole,
					   false,
					   unique);    
		
	    pig = pp.pig[0];
	    //tbu
	    pa.hash_proc_int_d[pa.current_analysis_depth].put(mm,pig);
// 	    ParIntGraph new_pig = (ODParIntGraph) pig.clone();
// 	    ODParIntGraph new_pig = pp.pig[1];
// 	    if(mm.getHMethod().getReturnType().isPrimitive())
// 		new_pig.G.r.clear();
// 	    if(mm.getHMethod().getExceptionTypes().length == 0)
// 		new_pig.G.excp.clear();
// 	    pa.hash_proc_ext_d[pa.current_analysis_depth].put(mm,new_pig);
//  	    System.out.println("  pig after analysis and cleaning" + pig);
    }
	while(holefound);
   	System.out.println("  pig after WHOLE analysis");
   	System.out.println(pig);

	prepare_exit(mm);
    }



}

	

