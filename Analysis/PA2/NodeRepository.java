// NodeRepository.java, created Sun Jun 26 16:53:30 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.LinkedList;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;

import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;
import harpoon.Analysis.MetaMethods.GenType;


/**
 * <code>NodeRepository</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: NodeRepository.java,v 1.2 2005-09-01 22:45:21 salcianu Exp $
 */
public class NodeRepository {

    private static final boolean VERBOSE = true;

    public NodeRepository(Linker linker) {
	this.linker = linker;
    }
    private final Linker linker;

    public PANode getGlobalNode() {
	if(global == null) {
	    global = new GBLNode(linker);
	    if(Flags.USE_FRESHEN_TRICK) {
		global.link(new GBLNode(linker));
	    }
	}
	return global;
    }
    private PANode global;
    

    private static class GBLNode extends PANode {
	public GBLNode(Linker linker) {
	    super(PANode.Kind.GBL,
		  new GenType(linker.forName("java.lang.Object"),
			      GenType.POLY));
	}
	public String toString() {
	    return 
		(isFresh() ? "f" : "") +
		"GBL";
	}
    };


    PANode getInsideNode(Quad q, HClass hclass) {
	// Treat all exception as globally escaping (after all,
	// exceptions are quite rare, and they cannot practically be
	// stack-allocated)
	if(PAUtil.isException(hclass))
	    return getGlobalNode();

	PANode node = new2node.get(q);
	if(node == null) {
	    node = new INode(q, hclass);
	    if(Flags.USE_FRESHEN_TRICK) {
		node.link(new INode(q, hclass));
	    }
	    new2node.put(q, node);
	}
	return node;
    }

    public PANode getInsideNode(Quad q) {
	return new2node.get(q);
    }

    private final Map<Quad,PANode> new2node = new HashMap<Quad,PANode>();

    public static class INode extends PANode {
	public INode(Quad q, HClass hclass) {
	    this(q, new GenType(hclass, GenType.MONO));
	}
	public INode(Quad q, GenType gt) {
	    super(PANode.Kind.INSIDE, gt);
	    id = icount++;
	    this.q = q;
	}
	private final Quad q;
	public final Quad getQuad() { return q; }
	private static int icount = 0;
	private final int id;
	public String toString() {
	    return 
		(isFresh() ? "f" : "") +
		"I" + id + 
		(VERBOSE ? ("(" + type + ")") : "");
	}
    }

    public PANode getSpecialInside(Quad q, GenType gt) {
	Map<GenType,PANode> gt2node = q2gt2node.get(q);
	if(gt2node == null) {
	    gt2node = new HashMap<GenType,PANode>();
	    q2gt2node.put(q, gt2node);
	}
	PANode node = gt2node.get(gt);
	if(node == null) {
	    node = new INode(q, gt);
	    if(Flags.USE_FRESHEN_TRICK) {
		node.link(new INode(q, gt));
	    }
	    gt2node.put(gt, node);
	}
	return node;
    }
    private final Map<Quad,Map<GenType,PANode>> q2gt2node = 
	new HashMap<Quad,Map<GenType,PANode>>();


    PANode getImmNode(CALL cs, HClass hClass) {
	ImmNode node = hClass2immNode.get(hClass);
	if(node == null) {
	    node = new ImmNode(hClass);
	    if(Flags.USE_FRESHEN_TRICK) {
		node.link(new ImmNode(hClass));
	    }
	    hClass2immNode.put(hClass, node);
	}
	return node;
    }
    private Map<HClass,ImmNode> hClass2immNode = new HashMap<HClass,ImmNode>();

    // Nodes for modeling immutable datastructures returned by some
    // special, safe methods: the only current such method is
    // toString: it returns an IMM node (the String) pointing to an
    // underlying IMM node (the array of chars).  No edges should be
    // permitted from an IMM node.
    private static class ImmNode extends PANode {
	public ImmNode(HClass hclass) {
	    super(PANode.Kind.IMM, new GenType(hclass, GenType.MONO));
	    id = immCount++;
	}
	private static int immCount = 0;
	private final int id;
	public String toString() { 
	    return 
		(isFresh() ? "f" : "") +
		"IMM" + id + 
		(VERBOSE ? ("(" + type + ")") : "");
	}
    }


    PANode getLoadNode(Quad q, HClass hclass) {
	PANode node = load2node.get(q);
	if(node == null) {
	    node = new LNode(hclass);
	    if(Flags.USE_FRESHEN_TRICK) {
		node.link(new LNode(hclass));
	    }
	    load2node.put(q, node);
	}
	return node;
    }

    private final Map<Quad,PANode> load2node = new HashMap<Quad,PANode>();

    private static class LNode extends PANode {
	public LNode(HClass hclass) {
	    super(PANode.Kind.LOAD, new GenType(hclass, GenType.POLY));
	    id = lcount++;
	}
	private static int lcount = 0;
	private final int id;
	public String toString() { 
	    return 
		(isFresh() ? "f" : "") +
		"L" + id + 
		(VERBOSE ? ("(" + type + ")") : "");
	}
    }


    public LNode getSpecialLoad(Quad q, HField hf) {
	Map<HField,LNode> hf2node = q2hf2node.get(q);
	if(hf2node == null) {
	    hf2node = new HashMap<HField,LNode>();
	    q2hf2node.put(q, hf2node);
	}
	LNode node = hf2node.get(hf);
	if(node == null) {
	    node = new LNode(hf.getType());
	    if(Flags.USE_FRESHEN_TRICK) {
		node.link(new LNode(hf.getType()));
	    }
	    hf2node.put(hf, node);
	}
	return node;
    }
    private final Map<Quad,Map<HField,LNode>> q2hf2node = 
	new HashMap<Quad,Map<HField,LNode>>();


    List<PANode> createParamNodes(HMethod hm, Collection<HClass> types) {
	List<PANode> oldParams = hm2params.get(hm);
	if(oldParams != null)
	    return oldParams;

	List<PANode> pnodes = new LinkedList<PANode>();
	List<PANode> nonNullParams = new LinkedList<PANode>();
	for(HClass hclass : types) {
	    if(!hclass.isPrimitive()) {
		PANode node = new PNode(hclass);
		if(Flags.USE_FRESHEN_TRICK) {
		    node.link(new PNode(hclass));
		}
		pnodes.add(node);
		nonNullParams.add(node);		
	    }
	    else {
		pnodes.add(null);
	    }
	}
	hm2params.put(hm, nonNullParams);
	return pnodes;
    }

    public List<PANode> getParamNodes(HMethod hm) {
	List<PANode> params = hm2params.get(hm);
	assert params != null : "no params found for " + hm;
	return params;
    }

    private final Map<HMethod,List<PANode>> hm2params = new HashMap<HMethod,List<PANode>>();

    private static class PNode extends PANode {
	public PNode(HClass hclass) {
	    super(PANode.Kind.PARAM, new GenType(hclass, GenType.POLY));
	    id = pcount++;
	}
	private static int pcount = 0;
	private final int id;
	public String toString() {
	    return 
		(isFresh() ? "f" : "") +
		"P" + id + 
		(VERBOSE ? ("(" + type + ")") : "");
	}
    }

}
