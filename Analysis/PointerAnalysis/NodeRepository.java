// NodeRepository.java, created Wed Jan 12 14:14:27 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Hashtable;
import java.util.Iterator;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.Quad;

/**
 * <code>NodeRepository</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: NodeRepository.java,v 1.1.2.10 2000-03-03 06:23:15 salcianu Exp $
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

    /** Creates all the parameter nodes associated with <code>method</code>.
     *  <code>param_number</code> must contain the number of formal
     *  parameters of the method. */
    public final void addParamNodes(HMethod method, int param_number){
	// do not create the parameter nodes twice for the same procedure
	if(param_nodes.containsKey(method)) return;
	PANode nodes[] = new PANode[param_number];
	for(int i=0;i<param_number;i++){
	    nodes[i] = getNewNode(PANode.PARAM);
	}
	param_nodes.put(method,nodes);
    }

    /** Returns the parameter node associated with the <code>count</code>th
     * formal parameter of <code>hm</code>. The parameter nodes should for
     * <code>hm</code> should be created in advance, using
     * <code>addParamNodes</code>. */
    public final PANode getParamNode(HMethod method, int count){
	// The runtime system will take care of all the debug messages ...
	return getAllParams(method)[count];
    }

    /** Returns all the parameter nodes associated with the
     * formal parameters of <code>hm</code>. The parameter nodes for
     * <code>hm</code> should be
     * created in advance, using <code>addParamNodes</code>. */
    public final PANode[] getAllParams(HMethod method){
	if(PointerAnalysis.DEBUG){
	    if(!param_nodes.containsKey(method)){
		System.out.println("getAllParams: inexistent nodes");
		System.exit(1);
	    }
	}
	return (PANode[])param_nodes.get(method);
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
	}
	return node;
    }

    public static final PANode getNewNode(int type){
	if(PointerAnalysis.CONTEXT_SENSITIVE)
	    return new PANodeCS(type);
	else return new PANode(type);
    }

    public final HCodeElement node2Code(PANode n){
	if(n==null) return null;
	return (HCodeElement) node2code.get(n);
    }

    /** Pretty-printer for debug purposes. */
    public final String toString(){
	StringBuffer buffer = new StringBuffer();

	buffer.append("PARAMETER NODES:\n");
	Object[] methods = Debug.sortedSet(param_nodes.keySet());
	for(int i = 0 ; i < methods.length ; i++){
	    HMethod method = (HMethod) methods[i];
	    buffer.append(method.getDeclaringClass().getName());
	    buffer.append(".");
	    buffer.append(method.getName());
	    buffer.append(":\t");
	    PANode[] nodes = getAllParams(method);
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

	System.out.println("-NODE STATS-------------------------------");
	System.out.println("INSIDE node(s) : " + nb_insides);
	System.out.println("RETURN node(s) : " + nb_returns);
	System.out.println("EXCEPT node(s) : " + nb_excepts);
	System.out.println("STATIC node(s) : " + nb_statics);
	System.out.println("LOAD   node(s) : " + nb_loads);
	System.out.println("PARAM  node(s) : " + nb_params);
	int nb_total =
	    nb_insides + nb_returns + nb_excepts +
	    nb_statics + nb_loads + nb_params;
	System.out.println("-------------------------");
	System.out.println("TOTAL          : " + nb_total);
    }

}




