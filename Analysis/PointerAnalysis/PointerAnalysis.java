// PointerAnalysis.java, created Sat Jan  8 23:22:24 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Hashtable;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.Set;
import java.util.HashSet;

import harpoon.ClassFile.HCodeFactory;
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
import harpoon.IR.Quads.PHI;
import harpoon.IR.Quads.FOOTER;


/**
 * <code>PointerAnalysis</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PointerAnalysis.java,v 1.1.2.1 2000-01-14 20:50:59 salcianu Exp $
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
    // mapping HMethod -> ParIntGraph
    private Hashtable hash = new Hashtable();

    /** Creates a <code>PointerAnalysis</code>. */
    public PointerAnalysis(CallGraph _cg, AllCallers _ac,
			   HCodeFactory _hcf, HMethod hm){
	cg  = _cg;
	ac  = _ac;
	bb_factory = new CachingBBFactory(_hcf);
	analyze(hm);
    }

    // Returns the Parallel Interaction Graph attached to the method hm
    // (i.e. the graph at the end of the method)
    public ParIntGraph getParIntGraph(HMethod hm){
	return (ParIntGraph)hash.get(hm);
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

	    ParIntGraph old_info = (ParIntGraph) hash.get(hm_work);
	    analyze_intra(hm_work);
	    ParIntGraph new_info = (ParIntGraph) hash.get(hm_work);

	    // new info?
	    // TODO: this test is overkill! think about it!

	    System.out.println("PASS 1");

	    if(new_info == null){
		System.out.println("NULL new_info");
		System.exit(1);
	    }

	    if(!new_info.equals(old_info)){

		System.out.println("PASS 2");

		// yes! The callers of hm_work should be added to
		// the inter-procedural worklist
		Iterator it = ac.getDirectCallers(hm_work);
		while(it.hasNext())
		    W_inter_proc.add(it.next());
	    }

	    System.out.println("PASS 3");
	
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
		    System.out.println("Put a successor");
		    W_intra_proc.add(enum.nextElement());
		}
	    }
	}
    }


    /** The Parallel Interference Graph which is updated by the
     *  <code>analyze_nasic_block</code>. This should normally be a
     *  local variable of that function but it must be also accessible
     *  to the <code>PAVisitor</code> class */
    private ParIntGraph bbpig = null;

    /** QuadVisitor for the <code>analyze_basic_block</code> */
    class PAVisitor extends QuadVisitor{

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
	

	public void visit(PHI q){
		//TODO
	}
	
	/** End of the currently analyzed method; [trim the graph
	 *  of unnecessary edges], store it in the hash etc. */
	// TODO: trimming
	public void visit(FOOTER q){
	    hash.put(current_intra_method,bbpig);
	}
	
    };
    
    
    // Analyzes a basic block - a Parallel Interaction Graph is computed at
    // the beginning of the basic block, it is next updated by all the 
    // instructions appearing in the basic block (in the order they appear
    // in the original program).
    private ParIntGraph analyze_basic_block(BasicBlock bb){

	System.out.println("Analyze_basic_block " + bb);

	PAVisitor visitor = new PAVisitor();	
	Iterator instrs = bb.iterator();
	// bbpig is the graph at the *bb point; it will be 
	// updated till it become the graph at the bb* point
	bbpig = get_pig_for_bb(bb);

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
    
    // Recomputes the Parallel Interaction Thread associated with
    // the beginning of the <code>BasicBlock</code> <code>bb</code>.
    // This method is recomputing the stuff (instead of just grabbing it
    // from the cache) because the information attached with some of
    // the predecessors has changed (that's why bb is reanalyzed)
    private ParIntGraph get_pig_for_bb(BasicBlock bb){
	if(bb.prevLength() == 0){

	    System.out.println("Gone this way!");

	    // This case is treated specially, it's about the
	    // graph at the beginning of the current method.
	    return method_initial_pig(current_intra_method);
	}
	else{
	    Enumeration enum = bb.prev();

	    // do the union of the <code>ParIntGraph</code>s attached to
	    // all the predecessors of this basic block
	    ParIntGraph pig = (ParIntGraph)
		((ParIntGraph)hash_bb.get(enum.nextElement())).clone();
	    
	    while(enum.hasMoreElements())
		pig.join((ParIntGraph)hash_bb.get(enum.nextElement()));
	    return pig;
	}
    }


    private ParIntGraph method_initial_pig(HMethod hm){
	BasicBlock bb = bb_factory.computeBasicBlocks(hm); 
	HEADER hce = (HEADER) bb.getFirst();
	METHOD m  = (METHOD) hce.next(1); 
	Temp[] params = m.params();

	ParIntGraph pig = new ParIntGraph();
	
	nodes.addParamNodes(hm,params.length);

	// add all the edges of type <p,np> (i.e. parameter to 
	// parameter node). the edges for the static fields will
	// be added later.
	for(int i=0;i<params.length;i++)
	    pig.G.I.addEdge(params[i],nodes.getParamNode(hm,i));

	System.out.println("put pig for bb = " + bb);

	if(pig==null){
	    System.out.println("pig == null!!");
	    System.exit(1);
	}

	return pig;
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

	W_temp.add(hm);
	W_inter_proc.add(hm);

	while(!W_temp.isEmpty()){
	    HMethod hm_work = (HMethod)W_temp.remove();
	    // get all the methods that could be called by hm_work ...
	    HMethod[] callees = cg.calls(hm_work);

	    for(int i=0;i<callees.length;i++){
		HMethod hm_callee = callees[i];
		if(!W_inter_proc.contains(hm_callee)){
		    // ... and put them in the worklists if they haven't been
		    // analyzed yet.
		    W_temp.add(hm_callee);
		    W_inter_proc.add(hm_callee);
		}
	    }
	}
    }
}



