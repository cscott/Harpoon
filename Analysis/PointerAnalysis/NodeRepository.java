// NodeRepository.java, created Wed Jan 12 14:14:27 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Hashtable;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;

import harpoon.Analysis.MetaMethods.MetaMethod;

/**
 * <code>NodeRepository</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: NodeRepository.java,v 1.1.2.17 2000-04-02 03:27:55 salcianu Exp $
 */
public class NodeRepository {
    
    private Hashtable static_nodes;
    private Hashtable param_nodes;
    private Hashtable code_nodes;
    private Hashtable except_nodes;
    private Hashtable node2code;

    /** Creates a <code>NodeRepository</code>. */
    public NodeRepository() {
	static_nodes = new Hashtable();
	param_nodes  = new Hashtable();
	code_nodes   = new Hashtable();
	except_nodes = new Hashtable();
	node2code    = new Hashtable();
    }

    /** Returns the static node associated with the class
     * <code>class_name</code>. The node is automatically created if it
     * doesn't exist yet. <code>class_name</code> MUST be the full
     * (and hence unique) name of the class */
    public final PANode getStaticNode(String class_name){
	PANode node = (PANode) static_nodes.get(class_name);
	if(node==null)
	    static_nodes.put(class_name,node = getNewNode(PANode.STATIC));
	return node;
    }

    /** Creates all the parameter nodes associated with <code>mmethod</code>.
     *  <code>param_number</code> must contain the number of formal
     *  parameters of the meta-method. */
    public final void addParamNodes(MetaMethod mmethod, int param_number){
	// do not create the parameter nodes twice for the same procedure
	if(param_nodes.containsKey(mmethod)) return;
	PANode nodes[] = new PANode[param_number];
	for(int i = 0; i < param_number; i++){
	    nodes[i] = getNewNode(PANode.PARAM);
	}
	param_nodes.put(mmethod,nodes);
    }

    /** Returns the parameter node associated with the <code>count</code>th
     * formal parameter of <code>mmethod</code>. The parameter nodes for
     * <code>mmethod</code> should be created in advance, using
     * <code>addParamNodes</code>. */
    public final PANode getParamNode(MetaMethod mmethod, int count){
	// The runtime system will take care of all the debug messages ...
	return getAllParams(mmethod)[count];
    }

    /** Returns all the parameter nodes associated with the
     * formal parameters of <code>mmethod</code>. The parameter nodes for
     * <code>mmethod</code> should be
     * created in advance, using <code>addParamNodes</code>. */
    public final PANode[] getAllParams(MetaMethod mmethod){
	if(PointerAnalysis.DEBUG){
	    if(!param_nodes.containsKey(mmethod)){
		System.out.println("getAllParams: inexistent nodes");
		System.exit(1);
	    }
	}
	return (PANode[]) param_nodes.get(mmethod);
    }

    // Returns the EXCEPT node associated with a CALL instruction
    private final PANode getExceptNode(HCodeElement hce){
	PANode node = (PANode) except_nodes.get(hce);
	if(node == null){
	    except_nodes.put(hce,node = getNewNode(PANode.EXCEPT));
	    node2code.put(node,hce);
	}
	return node;	
    }

    /** Returns a <i>code</i>: a node associated with the
     * instruction <code>hce</code>: a load node 
     * (associated with a <code>GET</code> quad), a return node (associated
     * with a <code>CALL</code>) or an inside node (thread or not) associated
     * with a <code>NEW</code>). The node is automatically created if it
     * doesn't exist yet. The type of the node should be passed in the
     * <code>type</code> argument. */
    public final PANode getCodeNode(HCodeElement hce,int type){
	// we can have a RETURN node and an EXCEPT node associated to the
	// same code instruction. This is the only case where we have
	// two nodes associated with an instruction. We fix this by handling
	// EXCEPT nodes separately.
	if(type == PANode.EXCEPT) return getExceptNode(hce);

	PANode node = (PANode) code_nodes.get(hce);
	if(node == null){
	    code_nodes.put(hce,node = getNewNode(type));
	    node2code.put(node,hce);

	    if(type == PANode.LOAD){
		System.out.println(" new LOAD node " + node + " for " + 
				   hce.getSourceFile() + ":" + 
				   hce.getLineNumber() + " " + hce); 
	    }

	}
	return node;
    }

    // all the nodes are produced by this method. So, if we want them
    // to have a common characteristic, here is THE place to add things.
    static final PANode getNewNode(int type){
	return new PANode(type);
    }

    public final HCodeElement node2Code(PANode n){
	if(n==null) return null;
	return (HCodeElement) node2code.get(n);
    }

    /** Pretty-printer for debug purposes. */
    public final String toString(){
	StringBuffer buffer = new StringBuffer();

	buffer.append("PARAMETER NODES:\n");
	Object[] mmethods = Debug.sortedSet(param_nodes.keySet());
	for(int i = 0 ; i < mmethods.length ; i++){
	    MetaMethod mmethod = (MetaMethod) mmethods[i];
	    buffer.append(mmethod);
	    // buffer.append(method.getDeclaringClass().getName());
	    // buffer.append(".");
	    // buffer.append(method.getName());
	    buffer.append(":\t");
	    PANode[] nodes = getAllParams(mmethod);
	    for(int j = 0 ; j < nodes.length ; j++){
		buffer.append(nodes[j]);
		buffer.append(" ");
	    }
	    buffer.append("\n");
	}

	buffer.append("STATIC NODES:\n");
	Object[] statics = Debug.sortedSet(static_nodes.keySet());
	for(int i = 0; i < statics.length ; i++){
	    String class_name = (String) statics[i];
	    buffer.append(class_name);
	    buffer.append(":\t");
	    buffer.append(getStaticNode(class_name));
	    buffer.append("\n");
	}

	buffer.append("CODE NODES:\n");
	Object[] codes = Debug.sortedSet(code_nodes.keySet());
	for(int i = 0 ; i < codes.length ; i++){
	    HCodeElement hce = (HCodeElement) codes[i];
	    Quad q = (Quad) hce;
	    buffer.append((PANode)code_nodes.get(hce));
	    buffer.append("\t");
	    buffer.append(hce.getSourceFile());
	    buffer.append(":");
	    buffer.append(hce.getLineNumber());
	    buffer.append(" ");
	    buffer.append(hce);
	    buffer.append("\n");
	}

	codes = Debug.sortedSet(except_nodes.keySet());
	for(int i = 0 ; i < codes.length ; i++){
	    HCodeElement hce = (HCodeElement) codes[i];
	    Quad q = (Quad) hce;
	    buffer.append((PANode)except_nodes.get(hce));
	    buffer.append("\t");
	    buffer.append(hce.getSourceFile());
	    buffer.append(":");
	    buffer.append(hce.getLineNumber());
	    buffer.append(" ");
	    buffer.append(hce);
	    buffer.append("\n");
	}

	return buffer.toString();
    }

    /** Prints some statistics. */
    public void print_stats(){
	int nb_total   = PANode.count - 1;
	// the last -1 is for the InterThreadPA.THIS_THREAD conventional node
	int nb_excepts = except_nodes.size();
	int nb_statics = static_nodes.size();

	int nb_insides = 0;
	int nb_loads   = 0;
	int nb_returns = 0;
	
     	Iterator it = code_nodes.keySet().iterator();
	while(it.hasNext()){
	    PANode node = (PANode) code_nodes.get(it.next());
	    switch(node.type){
	    case PANode.INSIDE:
		nb_insides++;
		break;
	    case PANode.LOAD:
		nb_loads++;
		break;
	    case PANode.RETURN:
		nb_returns++;
		break;
	    }
	}

	int nb_params = 0;
	it = param_nodes.keySet().iterator();
	while(it.hasNext())
	    nb_params += ((PANode[])(param_nodes.get(it.next()))).length;

	int nb_roots = 
	    nb_insides + nb_returns + nb_excepts +
	    nb_statics + nb_loads + nb_params;

	System.out.println("-NODE STATS-------------------------------");
	System.out.println("INSIDE node(s) : " + nb_insides + " roots, " +
			   (nb_total-nb_roots) + " specializations");
	System.out.println("RETURN node(s) : " + nb_returns);
	System.out.println("EXCEPT node(s) : " + nb_excepts);
	System.out.println("STATIC node(s) : " + nb_statics);
	System.out.println("LOAD   node(s) : " + nb_loads);
	System.out.println("PARAM  node(s) : " + nb_params);
	System.out.println("-------------------------");
	System.out.println("TOTAL          : " + nb_total);
    }

    /** Nice looking info for debug purposes. */
    public void show_specializations(){
	System.out.println("-SPECIALIZATIONS--------------------------");

     	Iterator it = code_nodes.keySet().iterator();
	while(it.hasNext()){
	    PANode node = (PANode) code_nodes.get(it.next());
	    if(node.type == PANode.INSIDE){
		System.out.print(node);
		HCodeElement hce = node2Code(node);
		System.out.println(" created at " + hce.getSourceFile() + 
				 ":" + hce.getLineNumber() + " " + hce);
		show_node_specs(node, 1);
	    }
	}
    }

    // shows the specializations (call site and thread specs) for node.
    private void show_node_specs(PANode node, int ident){

	if(PointerAnalysis.CALL_CONTEXT_SENSITIVE)
	    for(Iterator it = node.getAllCSSpecs().iterator(); it.hasNext(); ){
		Map.Entry entry = (Map.Entry) it.next();
		CALL       q = (CALL) entry.getKey();
		PANode snode = (PANode) entry.getValue();
		
		for(int i = 0; i < ident ; i++)
		    System.out.print(" ");
		System.out.print(snode);
		
		System.out.print(" CallS from " + node +
				 " at " + q.getSourceFile() +
				 ":" + q.getLineNumber());	    
		//if(PointerAnalysis.DETAILS2){
		    HMethod method = q.method();
		    System.out.print(" (CALL " + 
				     method.getDeclaringClass().getName()+"."+
				     method.getName() + ") ");
		    //}
		System.out.println();
		show_node_specs(snode, ident+1);
	    }

	if(PointerAnalysis.THREAD_SENSITIVE)
	    for(Iterator it = node.getAllTSpecs().iterator(); it.hasNext(); ){
		Map.Entry entry = (Map.Entry) it.next();
		MetaMethod  run = (MetaMethod) entry.getKey();
		PANode    snode = (PANode) entry.getValue();
		
		for(int i = 0; i < ident ; i++)
		    System.out.print(" ");
		System.out.print(snode);
		System.out.println(" FTS from " + node +
				   " for " + run);
		show_node_specs(snode, ident+1);	    
	    }

	if(PointerAnalysis.WEAKLY_THREAD_SENSITIVE){
	    PANode snode = node.getWTSpec();
	    if(snode != null){
		for(int i = 0; i < ident ; i++)
		    System.out.print(" ");
		System.out.print(snode);
		System.out.println(" WTS from " + node);
		show_node_specs(snode, ident+1);	    
	    }
	}
    }

}








