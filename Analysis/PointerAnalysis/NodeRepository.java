// NodeRepository.java, created Wed Jan 12 14:14:27 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Hashtable;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeElement;

/**
 * <code>NodeRepository</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: NodeRepository.java,v 1.1.2.2 2000-01-15 03:38:16 salcianu Exp $
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

    /** Creates a static node; <code>name</code> MUST be its
     *  full (and hence unique) name. */
    public final void addStaticNode(String name){
	if(PointerAnalysis.DEBUG){
	    if(code_nodes.get(name)!=null){
		System.out.println("duplicate code node addition");
		System.exit(1);
	    }
	}
	static_nodes.put(name,new PANode(PANode.STATIC));
    }

    /** Creates all the parameter nodes associated with <code>method</code>.
     *  <code>param_number</code> must contain the number of formal
     *  parameters of the method */
    public final void addParamNodes(HMethod method, int param_number){
	if(PointerAnalysis.DEBUG){
	    if(code_nodes.get(method)!=null){
		System.out.println("duplicate code node addition");
		System.exit(1);
	    }
	}
	PANode nodes[] = new PANode[param_number];
	for(int i=0;i<param_number;i++){
	    nodes[i] = new PANode(PANode.PARAM);
	}
	param_nodes.put(method,nodes);
    }
    
    /** Creates a node associated with an HCodeElement: a load node 
     *  (associated with a GET instruction), a return node (associated
     *  with a CALL) and an inside node (thread or not) associated with
     *  a NEW) */
    public final void addCodeNode(HCodeElement elem,PANode node){
	if(PointerAnalysis.DEBUG){
	    if(code_nodes.get(elem)!=null){
		System.out.println("duplicate code node addition");
		System.exit(1);
	    }
	}
	code_nodes.put(elem,node);
    }

    public final PANode getStaticNode(String name){
	if(PointerAnalysis.DEBUG){
	    if(!static_nodes.containsKey(name)){
		System.out.println("getStaticNode: inexistent node");
		System.exit(1);
	    }
	}
	return (PANode)static_nodes.get(name);
    }

    public final PANode getParamNode(HMethod method, int count){
	// The runtime system will take care of all the debug messages ...
	return getAllParams(method)[count];
    }

    public final PANode[] getAllParams(HMethod method){
	if(PointerAnalysis.DEBUG){
	    if(!param_nodes.containsKey(method)){
		System.out.println("getAllParams: inexistent nodes");
		System.exit(1);
	    }
	}
	return (PANode[])param_nodes.get(method);
    }

    public final PANode getCodeNode(HCodeElement hce,int type){
	PANode node = (PANode) code_nodes.get(hce);
	
	if(node == null){
	    node = new PANode(type);
	    code_nodes.put(hce,node);
	}
	return node;
    }

}

