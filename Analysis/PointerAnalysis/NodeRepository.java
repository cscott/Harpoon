// NodeRepository.java, created Wed Jan 12 14:14:27 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Hashtable;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.Linker;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.AGET;

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.GenType;

import harpoon.Util.Util;

/**
 * <code>NodeRepository</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: NodeRepository.java,v 1.10 2004-02-08 03:20:02 cananian Exp $
 */
public class NodeRepository implements java.io.Serializable {

    public final static PANode NULL_NODE    = getNewNode(PANode.NULL);

    // this node acts as a summary for all load nodes that escape into
    // an unanalyzed method or into a static node.
    public final static PANode LOST_SUMMARY = getNewNode(PANode.LOST);
    
    private Hashtable/*<String,PANode>*/    static_nodes;
    private Hashtable/*<HMethod,PANode[]>*/ param_nodes;
    private Hashtable/*<Quad,PANode>*/      code_nodes;
    private Hashtable/*<CFPair,PANode>*/    cf_nodes;
    private Hashtable/*<Quad,PANode>*/      except_nodes;
    private Hashtable/*<PANode,Quad>*/      node2code;

    /** Creates a <code>NodeRepository</code>. */
    public NodeRepository(Linker linker) {
	static_nodes = new Hashtable();
	param_nodes  = new Hashtable();
	code_nodes   = new Hashtable();
	cf_nodes     = new Hashtable();
	except_nodes = new Hashtable();
	node2code    = new Hashtable();
	jl_Throwable = linker.forName("java.lang.Throwable");
	jl_Object    = linker.forName("java.lang.Object");
    }

    // debug flag: if it's set on, it shows how each node is created
    private final static boolean SHOW_NODE_CREATION = true;


    /** Returns the node that models the constant object
        <code>value</code>.  Currently, all constant objects are
        modelled by a single node. */
    public final PANode getConstNode(Object value) {
	if(value == null)
	    return NULL_NODE;
	return CONST_NODE;
    }
    // should be private
    static final PANode CONST_NODE = getNewNode(PANode.CONST);

    /** Returns the static node associated with the class
     * <code>class_name</code>. The node is automatically created if it
     * doesn't exist yet. <code>class_name</code> MUST be the full
     * (and hence unique) name of the class */
    public final PANode getStaticNode(String class_name) {

	if(PointerAnalysis.COMPRESS_LOST_NODES)
	    return LOST_SUMMARY;

	PANode node = (PANode) static_nodes.get(class_name);
	if(node == null) {
	    node = getNewNode(PANode.STATIC, null); // TODO
	    static_nodes.put(class_name, node);

	    if(SHOW_NODE_CREATION)
		System.out.println("CREATED NODE " + node +
				   " for " + class_name);
	}
	return node;
    }

    /** Creates all the parameter nodes associated with <code>mmethod</code>.
     *  <code>param_number</code> must contain the number of formal
     *  parameters of the meta-method. */
    public final void addParamNodes(MetaMethod mmethod, int param_number) {
	// do not create the parameter nodes twice for the same procedure
	if(param_nodes.containsKey(mmethod)) return;
	// grab the types for the method's object parameters
	GenType[] gts = new GenType[param_number];
	int count = 0;
	for(int i = 0; i < mmethod.nbParams(); i++) {
	    GenType gt = mmethod.getType(i);
	    if(!gt.getHClass().isPrimitive()) {
		assert count < param_number : 
		    "Strange number of params for " + mmethod;
		gts[count] = gt;
		count++;
	    }
	}
	// do the essential thing: create the nodes
	PANode nodes[] = new PANode[param_number];
	for(int i = 0; i < param_number; i++)
	    nodes[i] = getNewNode(PANode.PARAM, new GenType[]{gts[i]});
	param_nodes.put(mmethod,nodes);

	if(SHOW_NODE_CREATION && (nodes.length > 0)) {
	    System.out.print("\nCREATED NODES ");
	    for(int i = 0; i < nodes.length; i++)
		System.out.print(" " + nodes[i]);
	    System.out.println("; parameters for " + mmethod.getHMethod());
	}
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
	    if(!param_nodes.containsKey(mmethod) &&
	       !Modifier.isNative(mmethod.getHMethod().getModifiers())) {
		System.out.println("getAllParams: inexistent nodes");
		System.exit(1);
	   }
	}
	return (PANode[]) param_nodes.get(mmethod);
    }

    private final GenType[] get_exceptions(HMethod callee) {
	HClass[] excs = callee.getExceptionTypes();
	GenType[] retval = new GenType[excs.length];
	for(int i = 0; i < excs.length; i++)
	    retval[i] = new GenType(excs[i], GenType.POLY);
	return retval;
    }

    // Returns the EXCEPT node associated with a CALL instruction
    private final PANode getExceptNode(HCodeElement hce){
	if(COALESCE_RESULTS_FROM_NATIVES) {
	    if(coalesced_native_excp == null)
		coalesced_native_excp = getNewNode(PANode.EXCEPT);
	    return coalesced_native_excp;
	}

	PANode node = (PANode) except_nodes.get(hce);
	if(node == null) {
	    HMethod callee = ((CALL) hce).method();
	    node = getNewNode(PANode.EXCEPT, get_exceptions(callee));
	    except_nodes.put(hce, node);
	    node2code.put(node, hce);
	}
	return node;	
    }
    private PANode coalesced_native_excp = null;
    private PANode coalesced_native_ret  = null;
    private final boolean COALESCE_RESULTS_FROM_NATIVES = true;
    private PANode coalesced_excp = null;
    private final HClass jl_Throwable;
    private final HClass jl_Object;
    private final boolean COALESCE_EXCEPTIONS = true;

    // Auxiliary class: the key type for the "cf_nodes" hashtable.
    private static class CFPair implements java.io.Serializable {
	HCodeElement hce;
	String f;
	CFPair(HCodeElement hce, String f){
	    this.hce = hce;
	    this.f   = f;
	}

	int hash = 0; // small caching hack
	public int hashCode(){
	    if(hash == 0)
		hash = hce.hashCode() + f.hashCode();
	    return hash;
	}

	public boolean equals(Object obj){
	    CFPair hf2 = (CFPair) obj;
	    return (hf2.hce.equals(hce) && hf2.f.equals(f));
	}

	public String toString(){
	    return hce.toString() + f.toString();
	}
    }

    /** Special function for load instructions that read multiple fields
	in the same time (the motivating example is that of "clone()")
	For such an instruction, a load node is generated for each field
	that is read (<code>f</code> in the parameter list). */
    public final PANode getLoadNodeSpecial(HCodeElement hce, String f){
	CFPair key = new CFPair(hce, f); 
	PANode retval = (PANode) cf_nodes.get(key);
	if(retval == null) {
	    retval = new PANode(PANode.LOAD);
	    cf_nodes.put(key, retval);
	}
	return retval;
    }


    public final PANode getCodeNode(HCodeElement hce, int type) {
	return getCodeNode(hce, type, null, true);
    }

    public final PANode getCodeNode(HCodeElement hce, int type, boolean make) {
	return getCodeNode(hce, type, null, make);
    }

    /** Returns a <i>code</i> node: a node associated with the
     * instruction <code>hce</code>: a load node 
     * (associated with a <code>GET</code> quad), a return node (associated
     * with a <code>CALL</code>) or an inside node (thread or not) associated
     * with a <code>NEW</code>). The node is automatically created if it
     * doesn't exist yet. The type of the node should be passed in the
     * <code>type</code> argument. */
    public final PANode getCodeNode(HCodeElement hce, int type, GenType[] gts){
	return getCodeNode(hce, type, gts, true);
    }

    /** Returns a <i>code</i> node: a node associated with an instruction.
	The boolean parameter <code>make</code> controls the generation of
	such a node in case it doesn't exist yet. */
    public final PANode getCodeNode(HCodeElement hce, int type, GenType[] gts,
				    boolean make) {

	if(((type == PANode.RETURN) || (type == PANode.EXCEPT)) &&
	   PointerAnalysis.COMPRESS_LOST_NODES)
	    return LOST_SUMMARY;

	// we can have a RETURN node and an EXCEPT node associated to the
	// same code instruction. This is the only case where we have
	// two nodes associated with an instruction. We fix this by handling
	// EXCEPT nodes separately.
	if(type == PANode.EXCEPT) return getExceptNode(hce);

	if(type == PANode.RETURN && COALESCE_RESULTS_FROM_NATIVES) {
	    if(coalesced_native_ret == null)
		coalesced_native_ret = 
		    getNewNode
		    (PANode.RETURN,
		     new GenType[] { new GenType(jl_Object, GenType.POLY) });
	    return coalesced_native_ret;
	}

	if((type == PANode.INSIDE) && COALESCE_EXCEPTIONS &&
	   ((hce instanceof NEW) || (hce instanceof ANEW)) &&
	   getAllocatedType(hce).isInstanceOf(jl_Throwable)) {
	    if(coalesced_excp == null)
		coalesced_excp = 
		    getNewNode
		    (PANode.INSIDE, 
		     new GenType[] { new GenType(jl_Throwable, GenType.POLY)});
	    return coalesced_excp;
	}

	PANode node = (PANode) code_nodes.get(hce);
	if((node == null) && make) {
	    if(gts == null)
		gts = new GenType[] { get_code_node_type(hce) };
	    node = getNewNode(type, gts);
	    code_nodes.put(hce, node);
	    node2code.put(node, hce);

	    if(SHOW_NODE_CREATION)
		System.out.println("CREATED NODE " + node + " for " +
				   Util.code2str(hce));
	}
	return node;
    }

    private final GenType get_code_node_type(HCodeElement hce) {
	Quad quad = (Quad) hce;

	class GenTypeWrapper{
	    GenType gt = null;
	}
	final GenTypeWrapper retval = new GenTypeWrapper();

	quad.accept(new QuadVisitor() {
		public void visit(NEW q) { // INSIDE nodes
		    retval.gt = new GenType(q.hclass(), GenType.MONO);
		}
		
		public void visit(ANEW q) { // array INSIDE nodes
		    retval.gt = new GenType(q.hclass(), GenType.MONO);
		}

		public void visit(CALL q) { // RETURN nodes
		    retval.gt =
			new GenType(q.method().getReturnType(), GenType.POLY);
		}

		public void visit(GET q) { // LOAD nodes
		    retval.gt = new GenType(q.field().getType(), GenType.POLY);
		}

		public void visit(AGET q) { // LOAD nodes from array ops
		    retval.gt = new GenType(q.type(), GenType.POLY);
		}

		public void visit(Quad q) {
		    // this should not happen
		    assert false : "Unknown quad " + q;
		}
	    });

	return retval.gt;
    }


    // all the nodes are produced by this method. So, if we want them
    // to have a common characteristic, here is THE place to add things.
    static final PANode getNewNode(int type, GenType[] node_class) {
	PANode node = new PANode(type, node_class);

	if(number2node == null)
	    number2node = new Hashtable();
	number2node.put(new Integer(node.number), node);

	return node;
    } 

    private static Hashtable/*<Integer,PANode>*/ number2node;
    static PANode nodeForNumber(int number) {
	if(number2node == null)
	    return null;
	return (PANode) number2node.get(new Integer(number));
    }


    // default parameter for the node_class array.
    static final PANode getNewNode(int type) {
	return getNewNode(type, null);
    }
	
    /** Returns the instruction that created <code>node</code>. */
    public final HCodeElement node2Code(PANode node){
	if(node == null) return null;
	return (HCodeElement) node2code.get(node);
    }


    /** Gets the type of an inside node. */
    public final HClass getInsideNodeType(PANode node) {
	assert node.type == PANode.INSIDE : "Not an inside node!";
	PANode root = node.getRoot();
	System.out.println("getInsideNodeType: " + root);
	HCodeElement hce = node2Code(root);
	System.out.println("getInsideNodeType: " + Util.code2str(hce));
	return getAllocatedType(hce);
    }


    // get the type of the object allocated by the object creation site hce;
    // hce should be NEW or ANEW.
    public static HClass getAllocatedType(final HCodeElement hce){
	if(hce instanceof NEW)
	    return ((NEW) hce).hclass();
	if(hce instanceof ANEW)
	    return ((ANEW) hce).hclass();
	assert false : ("Not a NEW or ANEW: " + hce);
	return null; // should never happen
    }


    /** Modify the node2code mapping such that now node is associated
	with hce. */
    public final void updateNode2Code(PANode node, HCodeElement hce){
	// put the new one
	node2code.put(node, hce);
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

	Object cfs[] = Debug.sortedSet(cf_nodes.keySet());
	for(int i = 0; i < cfs.length; i++){
	    CFPair cfp = (CFPair) cfs[i];
	    HCodeElement hce = cfp.hce;
	    buffer.append((PANode) cf_nodes.get(cfp) + "\t" + 
			  hce.getSourceFile() + ":" + hce.getLineNumber() +
			  " " + cfp.hce + "\n");
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
	int nb_consts  = 1; // no other way of computing it
	int nb_nulls   = 1; // no other way of computing it
	
     	for(Iterator it = code_nodes.keySet().iterator(); it.hasNext(); ) {
	    PANode node = (PANode) code_nodes.get(it.next());
	    switch(node.type) {
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
	for(Iterator it = param_nodes.keySet().iterator(); it.hasNext(); )
	    nb_params += ((PANode[])(param_nodes.get(it.next()))).length;

	int nb_roots = 
	    nb_insides + nb_returns + nb_excepts +
	    nb_statics + nb_loads + nb_params + nb_consts + nb_nulls;

	System.out.println("-NODE STATS-------------------------------");
	System.out.println("INSIDE node(s) : " + nb_insides + " roots, " +
			   (nb_total - nb_roots) + " specializations");
	System.out.println("RETURN node(s) : " + nb_returns);
	System.out.println("EXCEPT node(s) : " + nb_excepts);
	System.out.println("STATIC node(s) : " + nb_statics);
	System.out.println("LOAD   node(s) : " + nb_loads);
	System.out.println("PARAM  node(s) : " + nb_params);
	System.out.println("CONST  node(s) : " + nb_consts);
	System.out.println("NULL   node(s) : " + nb_nulls);
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

	if(PointerAnalysis.CALL_CONTEXT_SENSITIVE){
	    for(Object entryO : node.getAllCSSpecs()){
		Map.Entry entry = (Map.Entry) entryO;
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
	    PANode bottom = node.getBottom();
	    if(bottom != null){
		for(int i = 0; i < ident ; i++)
		    System.out.print(" ");
		System.out.print(bottom);
		System.out.println("B");
	    }
	}

	if(PointerAnalysis.THREAD_SENSITIVE)
	    for(Object entryO : node.getAllTSpecs()){
		Map.Entry entry = (Map.Entry) entryO;
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
