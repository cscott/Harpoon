// NodeRepository.java, created Wed Jan 12 14:14:27 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Hashtable;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeElement;

/**
 * <code>NodeRepository</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: NodeRepository.java,v 1.1.2.4 2000-01-18 04:49:40 salcianu Exp $
 */
public class NodeRepository {
    
    private Hashtable static_nodes;
    private Hashtable param_nodes;
    private Hashtable code_nodes;

    /** Creates a <code>NodeRepository</code>. */
    public NodeRepository() {
	static_nodes = new Hashtable();
	param_nodes  = new Hashtable();
	code_nodes   = new Hashtable();
    }

    /** Returns the static node associated with the class
     * <code>class_name</code>. The node is automatically created if it
     * doesn't exist yet. <code>class_name</code> MUST be the full
     * (and hence unique) name of the class */
    public final PANode getStaticNode(String class_name){
	PANode node = (PANode) code_nodes.get(class_name);
	if(node==null)
	    code_nodes.put(class_name,node = new PANode(PANode.STATIC));
	return node;
    }

    /** Creates all the parameter nodes associated with <code>method</code>.
     *  <code>param_number</code> must contain the number of formal
     *  parameters of the method. */
    public final void addParamNodes(HMethod method, int param_number){
	// do not create the parameter nodes twice for the same procedure
	if(param_nodes.get(method)!=null) return;
	PANode nodes[] = new PANode[param_number];
	for(int i=0;i<param_number;i++){
	    nodes[i] = new PANode(PANode.PARAM);
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

    /** Returns a <i>code</i>: a node associated with the
     * instruction <code>hce</code>: a load node 
     * (associated with a <code>GET</code> quad), a return node (associated
     * with a <code>CALL</code>) or an inside node (thread or not) associated
     * with a <code>NEW</code>). The node is automatically created if it
     * doesn't exist yet. The type of the node should be passed in the
     * <code>type</code> argument. */
    public final PANode getCodeNode(HCodeElement hce,int type){
	PANode node = (PANode) code_nodes.get(hce);
	if(node == null)
	    code_nodes.put(hce,node = new PANode(type));
	return node;
    }

}

