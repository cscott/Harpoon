// MAInfo.java, created Mon Apr  3 18:17:57 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
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

import harpoon.Analysis.MetaMethods.MetaMethod;

import harpoon.Analysis.DefaultAllocationInformation;

import harpoon.IR.Quads.Edge;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.QuadFactory;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import harpoon.Util.Util;

/**
 * <code>MAInfo</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: MAInfo.java,v 1.1.2.17 2000-05-18 03:48:15 salcianu Exp $
 */
public class MAInfo implements AllocationInformation, java.io.Serializable {

    private static boolean DEBUG = false;

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

    // use the inter-thread analysis
    private boolean USE_INTER_THREAD = false;
    
    /** Creates a <code>MAInfo</code>. */
    public MAInfo(PointerAnalysis pa, HCodeFactory hcf,
		  Set mms, boolean USE_INTER_THREAD){
        this.pa  = pa;
	this.hcf = hcf;
	this.mms = mms;
	this.node_rep = pa.getNodeRepository();
	this.USE_INTER_THREAD = USE_INTER_THREAD;

	analyze();

	// nullifying some stuff to ease the serialization
	this.pa  = null;
	this.hcf = null;
	this.mms = null;
	this.node_rep = null;
    }


    // Map<NEW, AllocationProperties>
    private final Map aps = new HashMap();
    
    // conservative allocation property: on the global heap
    // (by default).
    private final AllocationInformation.AllocationProperties 
	cons_ap = new MyAP();
    

    /** Returns the allocation policy for <code>allocationSite</code>. */
    public AllocationInformation.AllocationProperties query
	(HCodeElement allocationSite){
	
	AllocationInformation.AllocationProperties ap = 
	    (AllocationInformation.AllocationProperties)
	    aps.get(allocationSite);

	if(ap != null)
	    return ap;

	return cons_ap;
    }

    // analyze all the methods
    public final void analyze(){
	for(Iterator it = mms.iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
	    if(pa.analyzable(mm.getHMethod()))
		analyze_mm(mm);
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

    // analyze a single method: take the object creation sites from it
    // and generate an allocation policy for each one.
    public final void analyze_mm(MetaMethod mm){
	HMethod hm  = mm.getHMethod();

	//if(DEBUG)
	    System.out.println("MAInfo: Analyzed Meta-Method: " + mm);

	HCode hcode = hcf.convert(hm);

	ParIntGraph initial_pig = pa.getIntParIntGraph(mm);
	////	    USE_INTER_THREAD ? pa.threadInteraction(mm): 
	////                       pa.getIntParIntGraph(mm);
	
	ParIntGraph pig = (ParIntGraph) initial_pig.clone();

	pig.G.flushCaches();

	//System.out.println("BEFORE REMOVE METHOD HOLES: " + pig);

	//System.out.println("GOOD HOLES: " + good_holes); 
	pig.G.e.removeMethodHoles(good_holes);

	//System.out.println("AFTER  REMOVE METHOD HOLES: " + pig);

	if(pig == null) return;

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
	
	for(Iterator it = nodes.iterator(); it.hasNext(); ){
	    PANode node = (PANode) it.next();
	    if(node.type != PANode.INSIDE) continue;

	    // we are interested in objects allocated in the current thread
	    if(node.isTSpec()) continue;
	    
	    int depth = node.getCallChainDepth();

	    if(pig.G.captured(node)){
		if((depth == 0) /* && news.contains(node_rep.node2Code(node)) */ ) {
		    // captured nodes of depth 0 (ie allocated in this method,
		    // not in a callee) are allocated on the stack.
		    Quad q  = (Quad) node_rep.node2Code(node);
		    Util.assert(q != null, "No quad for " + node);
		    MyAP ap = getAPObj(q);
		    ap.sa = true;

		    System.out.println("STACK: r+ = " + pig.G.getReachableFromR());
		    System.out.println("STACK: " + node + " was stack allocated." +
				       q.getSourceFile() + ":" + q.getLineNumber()
				       + "\n");

		}
	    }
	    else{
		if((depth == 0) /* && news.contains(node_rep.node2Code(node)) */ ) {
		    if(remainInThread(node, hm)){
			Quad q = (Quad) node_rep.node2Code(node);
			Util.assert(q != null, "No quad for " + node);

			//if(escapes_only_in_methods(node, pig)){
			// objects that escape only in a method hole are
			// considered to remain in this thread and so, they
			// can be thread allocated
			
			MyAP ap = getAPObj(q);
			ap.ta = true; // thread allocation
			ap.ah = null; // on the current heap
		    }
		}
	    }
	}
	
	PAThreadMap tau = (PAThreadMap) (pa.getIntParIntGraph(mm).tau.clone());

	if(tau.activeThreadSet().size() == 1)
	    analyze_prealloc(mm, hcode, pig, tau);

	set_make_heap(tau.activeThreadSet());
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
	Set pointed = pig.G.I.getPointedNodes(nt);

	////////
	System.out.println("Pointed = " + pointed);

	// retain in "pointed" only the nodes allocated in this method, 
	// and which escaped only through the thread nt.
	for(Iterator it = pointed.iterator(); it.hasNext(); ){
	    PANode node = (PANode) it.next();
	    if( (node.type != PANode.INSIDE) ||
		(node.getCallChainDepth() != 0) ||
		!escapes_only_in_thread(node, nt, pig) ){
		System.out.println(node + " escapes somewhere else too");
		it.remove();
	    }
 	}

	////////
	System.out.println("Good Pointed = " + pointed);

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
	
	// TODO: move the NEW q at the beginning of the method,
	// and modify the object creation sites from the set "news" so
	// that they allocate on the heap specific thread.	

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
	// we need to update the ndoe2code relation.
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
    private MyAP getAPObj(Quad q){
	MyAP retval = (MyAP) aps.get(q);
	if(retval == null)
	    aps.put(q, retval = new MyAP());
	return retval;
    }

    // checks whether "node" escapes only through the thread node "nt".
    private boolean escapes_only_in_thread(PANode node, PANode nt,
					   ParIntGraph pig){

	if(pig.G.e.hasEscapedIntoAMethod(node)){
	    System.out.println(node + " escapes into a method");
	    return false;
	}
	if(pig.G.getReachableFromR().contains(node)) {
	    System.out.println(node + " is reachable from R");
	    return false;
	}
	if(pig.G.getReachableFromExcp().contains(node)) {
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
    
    
    // Checks whether node defined into hm, remain into the current
    // thread even if it escapes from the method which defines it.
    private boolean remainInThread(PANode node, HMethod hm){
	if(node.getCallChainDepth() == PointerAnalysis.MAX_SPEC_DEPTH)
	    return false;

	MetaMethod mm = new MetaMethod(hm, true);
	ParIntGraph pig = pa.getIntParIntGraph(mm);
	
	if(pig.G.captured(node))
	    return true;

	if(!lostOnlyInCaller(node, pig))
	    return false;

	for(Iterator it = node.getAllCSSpecs().iterator(); it.hasNext(); ){
	    Map.Entry entry = (Map.Entry) it.next();
	    CALL   call = (CALL) entry.getKey();
	    PANode spec = (PANode) entry.getValue();
	    
	    QuadFactory qf = call.getFactory();
	    HMethod hm_caller = qf.getMethod();

	    if(!remainInThread(spec, hm_caller))
		return false;
	}

	return true;	
    }


    /** Pretty printer for debug. */
    public void print(){
	System.out.println("ALLOCATION POLICIES:");
	for(Iterator it = aps.keySet().iterator(); it.hasNext(); ){
	    Quad newq = (Quad) it.next();
	    MyAP ap   = (MyAP) aps.get(newq);
	    HClass hclass = newq.getFactory().getMethod().getDeclaringClass();
	    

	    System.out.println(hclass.getPackage() + "." + 
			       newq.getSourceFile() + ":" +
			       newq.getLineNumber() + " " +
			       newq + " -> " + ap); 
	}
	System.out.println("====================");
    }

}

