// PANodeCS.java, created Thu Mar  2 18:17:24 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.HashSet;

import harpoon.ClassFile.HMethod;

import harpoon.IR.Quads.CALL;


/**
 * <code>PANodeCS</code> 
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PANodeCS.java,v 1.1.2.2 2000-03-05 03:12:38 salcianu Exp $
 */
public class PANodeCS extends PANode {
    // a node is a specialization of its parent for a specific call_site
    // primitive nodes have parent == null
    private PANodeCS parent    = null;
    private CALL     call_site = null;

    // the depth of the call path associated with this specialization
    private int depth = 0;

    /** Creates a <code>PANodeCS</code>. */
    public PANodeCS(int type, PANodeCS parent, CALL call_site) {
        super(type);
	this.parent    = parent;
	this.call_site = call_site;
	if(parent != null) depth = parent.depth + 1;
	else depth = 0;
    }

    /** Constructor for the root nodes (ie that are not the specialization
	of some other node). */
    public PANodeCS(int type){
	super(type);
    }

    private class ListItem{
	CALL call_site;
	PANodeCS node;
	ListItem next;
	ListItem(CALL _call_site, PANodeCS _node, ListItem _next){
	    call_site = _call_site;
	    node   = _node;
	    next   = _next;
	} 
    };

    // we keep the specializations of a node in list to avoid regenerating
    // new nodes for already seen specializations
    private ListItem specializations = null;

    /** Returns the specialized node of <code>this</code> node for
	<code>call_site</code>. This method is guarranteed to return the
	same node if it's called with the same argument. */
    public PANode specialize(CALL call_site){
	if(depth >= PointerAnalysis.MAX_SPEC_DEPTH) return this;

	ListItem l = specializations;
	boolean found = false;
	while(l!=null){
	    if(l.call_site == call_site){
		found = true;
		break;
	    }
	    l = l.next;
	}

	if(!found){
	    PANodeCS node   = new PANodeCS(type, this, call_site);
	    l  = new ListItem(call_site,node,specializations);
	    specializations = l;
	}

	// System.out.println("specialize(" + this + "," + call_site + 
	//		   ") = " + l.node);

	return l.node;
    }

    /** Returns the set of nodes that are just a specialization of this one. */
    public Set getAllSpecializations(){
	Set set = new HashSet();
	ListItem l = specializations;
	while(l != null){
	    set.add(l.node);
	    l = l.next;
	}
	return set;
    }

    /** Checks whether this node is a specialization of some other node. */
    public boolean isSpecialized(){
	return parent!=null;
    }

    /** Returns the call site where this node was created through
	specialization. Returns <code>null</code> if <code>this</code> node
	is not a speialization of some other node. */
    public CALL getCallSite(){
	return call_site;
    }

    /** Debug purposes description. */
    public String description(){
	if(isPrimitive()) return this.toString();
	StringBuffer buffer = new StringBuffer();
	PANodeCS node = this;
	while(!node.isPrimitive()){
	    buffer.append(node.call_site);
	    buffer.append("\n");
	    node = node.parent;
	}
	return buffer.toString();
    }

    /** Returns the parent node. <code>this</code> is a direct specialization
	of the parent node. */
    public PANode getParent(){ return parent; }

    /** Checks whether <code>this</code> node is an unspecialized one
	(a root node in the chain of specialization). */
    public boolean isPrimitive(){ return parent == null; }

    /** Searches in the specialization chain to find the initial node. */
    public PANodeCS initial() {
	PANodeCS r = this;
	while(!r.isPrimitive())
	    r = r.parent;
	return r;
    }
}
