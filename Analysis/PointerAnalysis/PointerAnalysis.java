// PointerAnalysis.java, created Sat Jan  8 23:22:24 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;

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
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.FOOTER;


/**
 * <code>PointerAnalysis</code> is the biggest class of the Pointer Analysis
 * package. It is designed to act as a <i>query-object</i>: after being
 * initialized, it can be asked to provide the Parallel InteractionGraph
 * valid at the end of a specific method. All the computation is done at
 * the construction time; the queries are only retrieving the already
 * computed results from the caches.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PointerAnalysis.java,v 1.1.2.4 2000-01-17 23:49:03 cananian Exp $
 */
public class PointerAnalysis {

    public static final boolean DEBUG = true;

    public static final String ARRAY_CONTENT = "elements";

    // The HCodeFactory providing the actual code of the analyzed methods
    private CallGraph cg;
    private AllCallers ac;
    private CachingBBFactory bb_factory;

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
	bb_factory = new CachingBBFactory(_hcf);
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
     * returned objects or static classes) have been preserved.
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
    private NodeRepository nodes = new NodeRepository(); 

    // Top-level procedure for the analysis. Receives the main method as
    // parameter. For the moment, it is not doing the inter-thread analysis
    private void analyze(HMethod hm){
	initialize_W_inter_proc(hm);

	while(!W_inter_proc.isEmpty()){
	    // grab a method from the worklist
	    HMethod hm_work = (HMethod)W_inter_proc.remove();

	    ParIntGraph old_info = (ParIntGraph) hash_proc_int.get(hm_work);
	    analyze_intra(hm_work);
	    ParIntGraph new_info = (ParIntGraph) hash_proc_int.get(hm_work);

	    // new info?
	    // TODO: this test is overkill! think about it!
	    if(new_info == null){
		System.out.println("NULL new_info");
		System.exit(1);
	    }

	    if(!new_info.equals(old_info)){
		// yes! The callers of hm_work should be added to
		// the inter-procedural worklist
		Iterator it = ac.getDirectCallers(hm_work);
		while(it.hasNext())
		    W_inter_proc.add(it.next());
	    }
	}
    }

    Hashtable hash_bb = new Hashtable();

    HMethod current_intra_method = null;

    // Performs the intra-procedural pointer analysis.
    private void analyze_intra(HMethod hm){

	current_intra_method = hm;

	W_intra_proc.add(bb_factory.computeBasicBlocks(hm));
	while(!W_intra_proc.isEmpty()){
	    // grab some "interesting" Basic Block from the worklist
	    BasicBlock bb_work = (BasicBlock)W_intra_proc.remove();

	    ParIntGraph old_info = (ParIntGraph) hash_bb.get(bb_work);
	    ParIntGraph new_info = analyze_basic_block(bb_work);
	    
	    // new info?
	    // TODO: this test is overkill! think about it!
	    // IDEA: the equality should be checked only for basic blocks
	    // with backedges
	    if(!new_info.equals(old_info)){
		// yes! The succesors of the analyzed basic block
		// are potentially "interesting", so they should be added
		// to the intra-procedural worklist
		Enumeration enum = bb_work.next();
		while(enum.hasMoreElements()){
		    // System.out.println("Put a successor");
		    W_intra_proc.add(enum.nextElement());
		}
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
	    if(l2 == null)
		l2 = ArtificialTempFactory.getTempFor(hf);
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
	
	
	// STORE STATEMENTS
	/** Store statements; normal case */
	public void visit(SET q){
	    Temp   l1 = q.objectref();
	    HField hf = q.field();
	    // static field -> get the corresponding artificial node
	    if(l1 == null)
		l1 = ArtificialTempFactory.getTempFor(hf);
	    
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

	System.out.println("Analyze_basic_block " + bb);

	PAVisitor visitor = new PAVisitor();	
	Iterator instrs = bb.iterator();
	// bbpig is the graph at the *bb point; it will be 
	// updated till it become the graph at the bb* point
	bbpig = get_initial_bb_pig(bb);

	if(bbpig == null)
	    System.out.println("bbpig is already null");
	
	// go through all the instructions of this basic block
	while(instrs.hasNext()){
	    
	    Quad q = (Quad) instrs.next();

	    System.out.println("Analyzing " + q);
	    
	    // update the Parallel Interaction Graph according
	    // to the current instruction
	    q.accept(visitor);
	}

	hash_bb.put(bb,bbpig);
	return bbpig;
    }
    
    /** Returns the Parallel Interaction Graph at the point bb*
     *  The returned <code>ParIntGraph</code> must not be modified 
     *  by the caller. This function is used by 
     *  <code>get_initial_bb_pig</code>. */
    private ParIntGraph get_after_bb_pig(BasicBlock bb){
	ParIntGraph pig = (ParIntGraph) hash_bb.get(bb);
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

	    System.out.println("Gone this way!");

	    // This case is treated specially, it's about the
	    // graph at the beginning of the current method.
	    ParIntGraph pig = method_initial_pig(current_intra_method);

	    System.out.println("The mapping at the beginning of " + //DEBUG
			       current_intra_method + ":");         //DEBUG
	    System.out.println(pig);                                //DEBUG

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
    private ParIntGraph method_initial_pig(HMethod hm){
	BasicBlock bb = bb_factory.computeBasicBlocks(hm); 
	HEADER hce = (HEADER) bb.getFirst();
	METHOD m  = (METHOD) hce.next(1); 
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
    static public final boolean analyzable(HMethod hm){
	return !(java.lang.reflect.Modifier.isNative(hm.getModifiers()));
    }

    // put into the initial inter-procedural worklist all the
    // methods that could be called (directly and indirectly)
    // by the main method (which is transmited as a parameter in hm)
    //
    // In order to implement the efficiency of the fixed-point algorithm,
    // the methods are put into the inter-procedural worklist in reverse
    // dfs order (i.e. the "leaf" procedures are at the beginning, main is
    // at the end). For the same reason, this worklist is implemented as
    // a stack - this will force the fixed point algorithm to work with
    // strongly connected components of the call graph.
    private void initialize_W_inter_proc(HMethod hm){
	// temporary worklist for the dfs exploration
	PAWorkStack W_temp = new PAWorkStack();

	if(!analyzable(hm)) return;

	W_temp.add(hm);
	W_inter_proc.add(hm);

	while(!W_temp.isEmpty()){
	    HMethod hm_work = (HMethod)W_temp.remove();
	    // get all the methods that could be called by hm_work ...
	    HMethod[] callees = cg.calls(hm_work);

	    for(int i=0;i<callees.length;i++){
		HMethod hm_callee = callees[i];
		if(analyzable(hm_callee) && !W_inter_proc.contains(hm_callee)){
		    // ... and put them in the worklists if they haven't been
		    // analyzed yet.
		    W_temp.add(hm_callee);
		    W_inter_proc.add(hm_callee);
		}
	    }
	}
    }
}



