// PointerAnalysis.java, created Sat Jan  8 23:22:24 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.Collections;

import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.AllCallers;
import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.BasicBlock;

import harpoon.Temp.Temp;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.METHOD;
import harpoon.IR.Quads.AGET;
import harpoon.IR.Quads.ALENGTH;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.ASET;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.MOVE;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.RETURN;
import harpoon.IR.Quads.THROW;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.FOOTER;


/**
 * <code>PointerAnalysis</code> is the biggest class of the Pointer Analysis
 * package. It is designed to act as a <i>query-object</i>: after being
 * initialized, it can be asked to provide the Parallel InteractionGraph
 * valid at the end of a specific method. All the computation is done at
 * the construction time; the queries are only retrieving the already
 * computed results from the caches.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PointerAnalysis.java,v 1.1.2.11 2000-02-07 02:11:46 salcianu Exp $
 */
public class PointerAnalysis {

    public static final boolean DEBUG = true;
    public static final boolean EXTENDED_DEBUG = false;

    public static final String ARRAY_CONTENT = "array_elements";

    // The HCodeFactory providing the actual code of the analyzed methods
    private CallGraph cg;
    final CallGraph getCallGraph() { return cg; }
    private AllCallers ac;
    private CachingSCCBBFactory scc_bb_factory;

    // Maintains the partial points-to and escape information for the
    // analyzed methods. This info is successively refined by the fixed
    // point algorithm until no further change is possible
    // mapping HMethod -> ParIntGraph.
    private Hashtable hash_proc_int = new Hashtable();

    // Maintains the external view of the parallel interaction graphs
    // attached with the analyzed methods. These "bricks" are used
    // for the processing of the CALL nodes.
    // Mapping HMethod -> ParIntGraph.
    private Hashtable hash_proc_ext = new Hashtable();

    /** Creates a <code>PointerAnalysis</code>. It is also triggering
     * the analysis of <code>hm</code> and of all methods that are called
     * directly or indirectly by <codehm</code>.<br>
     *<b>Parameters</b>
     *<ul>
     *<li>The <code>CallGraph</code> and the <code>AllCallers</code> that
     * models the relations between the different methods;
     *<li>A <code>HCodefactory</code> that is used to generate the actual
     * code of the methods and
     *<li>The <i>root</i>method <code>hm</code>.
     *</ul> */
    public PointerAnalysis(CallGraph _cg, AllCallers _ac,
			   HCodeFactory _hcf, HMethod hm){
	cg  = _cg;
	ac  = _ac;
	scc_bb_factory = new CachingSCCBBFactory(_hcf);
	analyze(hm);
    }

    /** Returns the full (internal) <code>ParIntGraph</code> attached to
     * the method <code>hm</code> i.e. the graph at the end of the method.
     * Returns <code>null</code> if no such graph is available. */
    public ParIntGraph getIntParIntGraph(HMethod hm){
	return (ParIntGraph)hash_proc_int.get(hm);
    }

    /** Returns the simplified (external) <code>ParIntGraph</code> attached to
     * the method <code>hm</code> i.e. the graph at the end of the method.
     * of which only the parts reachable from the exterior (via parameters,
     * returned objects or static classes) have been preserved. The escape
     * function do not consider the parameters of the function (anyway, this
     * graph is supposed to be inlined into the graph of the caller, so the 
     * parameters will disappear anyway).
     * Returns <code>null</code> if no such graph is available. */
    public ParIntGraph getExtParIntGraph(HMethod hm){
	return (ParIntGraph)hash_proc_ext.get(hm);
    }

    /** Returns the parameter nodes of the method <code>hm</code>. This is
     * useful for the understanding of the <code>ParIntGraph</code> attached
     * to <code>hm</code> */
    public PANode[] getParamNodes(HMethod hm){
	return nodes.getAllParams(hm);
    }


    // Worklist for the inter-procedural analysis: only <code>HMethod</code>s
    // will be put here.
    private PAWorkStack W_inter_proc = new PAWorkStack();

    // Worklist for the intra-procedural analysis; at any moment, it
    // contains only basic blocks from the same method.
    private PAWorkList  W_intra_proc = new PAWorkList();

    /** Repository for node management. */
    // TODO: Must be private, it is public just for debug purposes
    public NodeRepository nodes = new NodeRepository(); 
    final NodeRepository getNodeRepository() { return nodes; }

    // Top-level procedure for the analysis. Receives the main method as
    // parameter. For the moment, it is not doing the inter-thread analysis
    private void analyze(HMethod hm){

	SCComponent.Navigator navigator =
	    new SCComponent.Navigator(){
		    public Object[] next(Object node){
			return cg.calls((HMethod)node);
		    }
		    public Object[] prev(Object node){
			return ac.directCallers((HMethod)node);
		    }
		};


	long begin_time = new Date().getTime();
	if(DEBUG)
	  System.out.println("Creating the strongly connected components " +
			     "of methods ...");

	// the topologically sorted graph of strongly connected components
	// composed of mutually recursive methods (the edges model the
	// caller-callee interaction).
	SCCTopSortedGraph method_sccs = 
	    SCCTopSortedGraph.topSort(SCComponent.buildSCC(hm,navigator));


	if(DEBUG){
	    long total_time = new Date().getTime() - begin_time;
	    int counter = 0;
	    int methods = 0;
	    SCComponent scc = method_sccs.getFirst();
	    while(scc != null){
		if(EXTENDED_DEBUG) System.out.print(scc.toString(cg));
		counter++;
		methods += scc.nodeSet().size();
		scc = scc.nextTopSort();
	    }
	    System.out.println(counter + " component(s); " +
			       methods + " method(s); " +
			       total_time + "ms processing time");
	}

	SCComponent scc = method_sccs.getLast();
	while(scc != null){
	    analyze_inter_proc_scc(scc);
	    scc = scc.prevTopSort();
	}
    }

    // inter-procedural analysis of a group of mutually recursive methods
    private void analyze_inter_proc_scc(SCComponent scc){

	// Initially, the worklist (actually a workstack) contains only one
	// of the methods from the actual group of mutually recursive
	// methods. The others will be added later (because they are reachable
	// in the AllCaller graph from this initial node). 
	HMethod method = (HMethod)scc.nodes().next();
	// if SCC composed of a native or abstract method, return immediately!
	if(!analyzable(method)){
	    System.out.println(scc.toString(cg) + " is unanalyzable");
	    return;
	}
	W_inter_proc.add(method);

	Set methods = scc.nodeSet();
	boolean must_check = scc.isLoop();

	System.out.print(scc.toString(cg));
	System.out.println("must_check = " + must_check);

	long begin_time = new Date().getTime();

	while(!W_inter_proc.isEmpty()){
	    // grab a method from the worklist
	    HMethod hm_work = (HMethod)W_inter_proc.remove();

	    ParIntGraph old_info = (ParIntGraph) hash_proc_int.get(hm_work);
	    analyze_intra_proc(hm_work);
	    ParIntGraph new_info = (ParIntGraph) hash_proc_int.get(hm_work);

	    // new info?
	    // TODO: this test is overkill! think about it!
	    if(must_check && !new_info.equals(old_info)){

		//System.out.println("----- The information has changed:");
		//System.out.println("Old graph: " + old_info);
		//System.out.println("New graph: " + new_info);

		//yes! The callers of hm_work should be added to
		// the inter-procedural worklist
		Iterator it = ac.directCallerSet(hm_work).iterator();
		while(it.hasNext()){
		   HMethod hm_caller = (HMethod) it.next();
		   if(methods.contains(hm_caller))
		       W_inter_proc.add(hm_caller);
		}
		//System.out.println("-----");
	    }
	}

	scc_bb_factory.clear();

	long total_time = new Date().getTime() - begin_time;
	System.out.println("SCC" + scc.getId() + " analyzed in " + 
			   total_time + " ms");

	// System.out.println("SCC"+scc.getId() + " analysis finished.");
    }

    // Mapping BB -> ParIntGraph.
    Hashtable bb2pig = new Hashtable();

    HMethod current_intra_method = null;
    ParIntGraph initial_pig = null;

    // Performs the intra-procedural pointer analysis.
    private void analyze_intra_proc(HMethod hm){
	//System.out.println("Method: " + hm);

	current_intra_method = hm;

	// cut the method into SCCs of basic blocks
	SCComponent scc = scc_bb_factory.computeSCCBB(hm).getFirst();

	// construct the ParIntGraph at the beginning of the method 
	BasicBlock first_bb = (BasicBlock)scc.nodes().next();
	HEADER first_hce = (HEADER) first_bb.getFirst();
	METHOD m  = (METHOD) first_hce.next(1); 
	initial_pig = get_method_initial_pig(hm,m);

	// analyze the SCCs in decreasing topological order
	while(scc != null){
	    analyze_intra_proc_scc(scc);
	    scc = scc.nextTopSort();
	}

	bb2pig.clear();
    }

    // Intra-procedural analysis of a strongly connected component of
    // basic blocks.
    private void analyze_intra_proc_scc(SCComponent scc){
	// add ALL the BB from this SCC to the worklist.
	W_intra_proc.addAll(scc.nodeSet());
	boolean must_check = scc.isLoop();

	while(!W_intra_proc.isEmpty()){
	    // grab a Basic Block from the worklist
	    BasicBlock bb_work = (BasicBlock)W_intra_proc.remove();

	    ParIntGraph old_info = (ParIntGraph) bb2pig.get(bb_work);
	    ParIntGraph new_info = analyze_basic_block(bb_work);

	    if(must_check && !new_info.equals(old_info)){
		// yes! The succesors of the analyzed basic block
		// are potentially "interesting", so they should be added
		// to the intra-procedural worklist
		
		/// System.out.print("Neighbors of " + bb_work + ": ");

		Enumeration enum = bb_work.next();
		while(enum.hasMoreElements()){
		    BasicBlock bb_next = (BasicBlock)enum.nextElement();
		    /// System.out.print(bb_next + " ");
		    // remain in the current strongly connected component
		    if(scc.contains(bb_next))
			W_intra_proc.add(bb_next);
		}
		
		/// System.out.println();
	    }
	}
    }


    /** The Parallel Interference Graph which is updated by the
     *  <code>analyze_basic_block</code>. This should normally be a
     *  local variable of that function but it must be also accessible
     *  to the <code>PAVisitor</code> class */
    private ParIntGraph bbpig = null;

    /** QuadVisitor for the <code>analyze_basic_block</code> */
    private class PAVisitor extends QuadVisitor{

	public void visit(Quad q){
	    // do nothing
	}
		

	/** Copy statements **/
	public void visit(MOVE q){
	    bbpig.G.I.removeEdges(q.dst());
	    Set set = bbpig.G.I.pointedNodes(q.src());
	    bbpig.G.I.addEdges(q.dst(),set);
	}
	
	
	// LOAD STATEMENTS
	/** Load statement; normal case */
	public void visit(GET q){
	    Temp l2 = q.objectref();
	    HField hf = q.field();
	    // do not analyze loads from non-pointer fields
	    if(hf.getType().isPrimitive()) return;
	    if(l2 == null){
		// special treatement of the static fields
		l2 = ArtificialTempFactory.getTempFor(hf);
		// this part should really be put in some preliminary step
		PANode static_node =
		    nodes.getStaticNode(hf.getDeclaringClass().getName());
		bbpig.G.I.addEdge(l2,static_node);
		bbpig.G.e.addNodeHole(static_node,static_node);
	    }
	    process_load(q,q.dst(),l2,hf.getName());
	}
	
	/** Load statement; special case - arrays */
	public void visit(AGET q){
	    process_load(q,q.dst(),q.objectref(),ARRAY_CONTENT);
	}
	
	/** Does the real processing of a load statement */
	public void process_load(Quad q, Temp l1, Temp l2, String f){
	    Set set_aux = bbpig.G.I.pointedNodes(l2);
	    Set set_S = bbpig.G.I.pointedNodes(set_aux,f);
	    HashSet set_E = new HashSet();
	    
	    Iterator it = set_aux.iterator();
	    while(it.hasNext()){
		PANode node = (PANode)it.next();
		// hasEscaped instead of escaped (there is no problem
		// with the nodes that *will* escape - the future cannot
		// affect us).
		if(bbpig.G.e.hasEscaped(node))
		    set_E.add(node);
	    }
	    
	    bbpig.G.I.removeEdges(l1);
	    
	    if(set_E.isEmpty()){
		bbpig.G.I.addEdges(l1,set_S);
	    }
	    else{
		PANode load_node = nodes.getCodeNode(q,PANode.LOAD); 
		set_S.add(load_node);
		bbpig.G.I.addEdges(l1,set_S);
		bbpig.G.O.addEdges(set_E,f,load_node);
		bbpig.G.propagate(set_E);
		// TODO: update alpha
		// TODO: update pi
	    }
	}


	// OBJECT CREATION SITES
	/** Object creation sites; normal case */
	public void visit(NEW q){
	    process_new(q,q.dst());
	}
	
	/** Object creation sites; special case - arrays */
	public void visit(ANEW q){
	    process_new(q,q.dst());
	}
	
	private void process_new(Quad q,Temp tmp){
	    // Kill_I = edges(I,l)
	    PANode node = nodes.getCodeNode(q,PANode.INSIDE);
	    bbpig.G.I.removeEdges(tmp);
	    // Gen_I = {<l,n>}
	    bbpig.G.I.addEdge(tmp,node);
	}
	
	
	/** Return statement: r' = I(l) */
	public void visit(RETURN q){
	    Temp tmp = q.retval();
	    // return without value; nothing to be done
	    if(tmp == null) return;
	    Set set = bbpig.G.I.pointedNodes(tmp);
	    // TAKE CARE: r is assumed to be empty before!
	    bbpig.G.r.addAll(set);
	}

	/** Return statement: r' = I(l) */
	public void visit(THROW q){
	    Temp tmp = q.throwable();
	    Set set = bbpig.G.I.pointedNodes(tmp);
	    // TAKE CARE: r_excp is assumed to be empty before!
	    bbpig.G.excp.addAll(set);
	}
	
	
	// STORE STATEMENTS
	/** Store statements; normal case */
	public void visit(SET q){
	    Temp   l1 = q.objectref();
	    HField hf = q.field();
	    // do not analyze stores into non-pointer fields
	    if(hf.getType().isPrimitive()) return;
	    // static field -> get the corresponding artificial node
	    if(l1 == null){
		// special treatement of the static fields
		l1 = ArtificialTempFactory.getTempFor(hf);
		// this part should really be put in some preliminary step
		PANode static_node =
		    nodes.getStaticNode(hf.getDeclaringClass().getName());
		bbpig.G.I.addEdge(l1,static_node);
		bbpig.G.e.addNodeHole(static_node,static_node);
	    }
	    process_store(l1,hf.getName(),q.src());
	}
	
	/** Store statement; special case - array */
	public void visit(ASET q){
	    process_store(q.objectref(),ARRAY_CONTENT,q.src());		
	}
	
	/** Does the real processing of a store statement */
	public void process_store(Temp l1, String f, Temp l2){
	    Set set1 = bbpig.G.I.pointedNodes(l1);
	    Set set2 = bbpig.G.I.pointedNodes(l2);
		
	    bbpig.G.I.addEdges(set1,f,set2);
	    bbpig.G.propagate(set1);
	}
	

	public void visit(CALL q){

	    /// System.out.println("Before CALL");
	    /// System.out.println(bbpig);

	    if(thread_start_site(q)){
		System.out.println("THREAD START SITE: " + 
				   q.getSourceFile() + ":" +
				   q.getLineNumber());
		Temp l = q.params(0);
		Set set = bbpig.G.I.pointedNodes(l);
		bbpig.tau.incAll(set);

		Iterator it_nt = set.iterator();
		while(it_nt.hasNext()){
		    PANode nt = (PANode) it_nt.next();
		    bbpig.G.e.addNodeHole(nt,nt);
		    bbpig.G.propagate(Collections.singleton(nt));
		}

		return;
	    }

	    InterProcPA.analyze_call(current_intra_method,
				     q,     // the CALL site
				     bbpig, // the graph before the call
				     PointerAnalysis.this);

	    /// System.out.println("After CALL");
	    /// System.out.println(bbpig);

	}

	// We are sure that the call site q corresponds to a thread start
	// site if only java.lang.Thread.start can be called there. (If
	// some other methods can be called, it is possible that some of
	// them do not start a thread.)
	private boolean thread_start_site(CALL q){
	    HMethod hms[] = cg.calls(current_intra_method,q);
	    if(hms.length!=1) return false;

	    HMethod hm = hms[0];
	    String name = hm.getName();
	    if((name==null) || !name.equals("start")) return false;

	    if(hm.isStatic()) return false;
	    HClass hclass = hm.getDeclaringClass();
	    return hclass.getName().equals("java.lang.Thread");
	}

	/** End of the currently analyzed method; trim the graph
	 *  of unnecessary edges, store it in the hash tables etc. */
	public void visit(FOOTER q){

	    // The full graph is stored in the hash_proc_int hashtable;
	    hash_proc_int.put(current_intra_method,bbpig);

	    // To obtain the external view of the method, the graph must be
	    // shrinked to the really necessary parts: only the stuff
	    // that is accessible from the "root" nodes (i.e. the nodes
	    // that can be accessed by the rest of the program - e.g.
	    // the caller).
	    // The set of root nodes consists of the param and return nodes,
	    // and (only for the non-"main" methods) the static node.
	    PANode[] nodes = getParamNodes(current_intra_method);
	    boolean is_main = current_intra_method.getName().equals("main");
      	    ParIntGraph shrinked_graph = bbpig.keepTheEssential(nodes,is_main);

	    // The external view of the graph is stored in the
	    // hash_proc_ext hashtable;
	    hash_proc_ext.put(current_intra_method,shrinked_graph);
	}
	
    };
    
    
    /** Analyzes a basic block - a Parallel Interaction Graph is computed at
     *  the beginning of the basic block, it is next updated by all the 
     *  instructions appearing in the basic block (in the order they appear
     *  in the original program). */
    private ParIntGraph analyze_basic_block(BasicBlock bb){

	/// System.out.println("BEGIN: Analyze_basic_block " + bb);

	PAVisitor visitor = new PAVisitor();	
	Iterator instrs = bb.statements().iterator();
	// bbpig is the graph at the *bb point; it will be 
	// updated till it become the graph at the bb* point
	bbpig = get_initial_bb_pig(bb);

	// go through all the instructions of this basic block
	while(instrs.hasNext()){
	    
	    Quad q = (Quad) instrs.next();

	    /// System.out.println("Analyzing " + q);
	    
	    // update the Parallel Interaction Graph according
	    // to the current instruction
	    q.accept(visitor);
	}

	bb2pig.put(bb,bbpig);

	/// System.out.println("END: Analyze_basic_block " + bb);

	return bbpig;
    }
    
    /** Returns the Parallel Interaction Graph at the point bb*
     *  The returned <code>ParIntGraph</code> must not be modified 
     *  by the caller. This function is used by 
     *  <code>get_initial_bb_pig</code>. */
    private ParIntGraph get_after_bb_pig(BasicBlock bb){
	ParIntGraph pig = (ParIntGraph) bb2pig.get(bb);
	return (pig==null)?ParIntGraph.EMPTY_GRAPH:pig;
    }


    /** (Re)computes the Parallel Interaction Thread associated with
     *  the beginning of the <code>BasicBlock</code> <code>bb</code>.
     *  This method is recomputing the stuff (instead of just grabbing it
     *  from the cache) because the information attached with some of
     *  the predecessors has changed (that's why <code>bb</code> is 
     *  reanalyzed) */
    private ParIntGraph get_initial_bb_pig(BasicBlock bb){
	if(bb.prevLength() == 0){
	    // This case is treated specially, it's about the
	    // graph at the beginning of the current method.
	    ParIntGraph pig = initial_pig;
	    return pig;
	}
	else{
	    Enumeration enum = bb.prev();

	    // do the union of the <code>ParIntGraph</code>s attached to
	    // all the predecessors of this basic block
	    ParIntGraph pig = (ParIntGraph)
		(get_after_bb_pig((BasicBlock)enum.nextElement())).clone();
	    
	    while(enum.hasMoreElements())
		pig.join(get_after_bb_pig((BasicBlock)enum.nextElement()));

	    return pig;
	}
    }


    /** Computes the parallel interaction graph at the beginning of a 
     *  method; an almost empty graph except for the parameter nodes
     *  for the object formal parameter (i.e. primitive type parameters
     *  such as <code>int</code>, <code>float</code> do not have associated
     *  nodes */
    private ParIntGraph get_method_initial_pig(HMethod hm, METHOD m){
	Temp[] params = m.params();
	HClass[] types = hm.getParameterTypes();

	ParIntGraph pig = new ParIntGraph();

	// the following code is quite messy ... The problem is that I 
	// create param nodes only for the parameter with object types;
	// unfortunately, the types could be found only in HMethod (and
	// do not include the evetual this parameter for non-static nodes)
	// while the actual Temps associated with all the formal parameters
	// could be found only in METHOD. So, we have to coordinate
	// information from two different places and, even more, we have
	// to handle the missing this parameter (which is present in METHOD
	// but not in HMethod). 
	boolean isStatic = 
	    java.lang.reflect.Modifier.isStatic(hm.getModifiers());
	// if the method is non-static, the first parameter is not metioned
	// in HMethod - it's the implicit this parameter.
	int skew = isStatic?0:1;
	// number of object formal parameters = the number of param nodes
	int count = skew;
	for(int i=0;i<types.length;i++)
	    if(!types[i].isPrimitive()) count++;
	
	nodes.addParamNodes(hm,count);

	// add all the edges of type <p,np> (i.e. parameter to 
	// parameter node) - just for the non-primitive types (e.g. int params
	// do not clutter our analysis)
	// the edges for the static fields will
	// be added later.
	count = 0;
	for(int i=0;i<params.length;i++)
	    if((i<skew) || ((i>=skew) && !types[i-skew].isPrimitive())){
		PANode param_node = nodes.getParamNode(hm,count);
		pig.G.I.addEdge(params[i],param_node);
		// The param nodes are escaping through themselves */
		pig.G.e.addNodeHole(param_node,param_node);
		count++;
	    }

	return pig;
    }

    /** Check if <code>hm</code> can be analyzed by the pointer analysis. */
    public final boolean analyzable(HMethod hm){
	int modifier = hm.getModifiers();
	return ! (
	    (java.lang.reflect.Modifier.isNative(modifier)) ||
	    (java.lang.reflect.Modifier.isAbstract(modifier))
	    );
    }

}



