// PANode.java, created Sun Jan  9 16:24:57 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Collections;

import harpoon.Util.Util;
import harpoon.IR.Quads.CALL;

import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.GenType;
import harpoon.Util.DataStructs.LightMap;

/**
 * <code>PANode</code> class models a node for the Pointer Analysis
 * algorithm.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PANode.java,v 1.1.2.29 2001-10-18 22:36:10 salcianu Exp $
 */
final public class PANode implements java.io.Serializable {
    // activates some safety tests
    private static final boolean CAUTION = true;

    /** Possible type: Inside node */
    public static final int INSIDE          = 1;
    /** Possible type: Load node */
    public static final int LOAD            = 4;
    /** Possible type: Parameter node */
    public static final int PARAM           = 8;
    /** Possible type: Return node. Nodes of this type are used to represent
	the objects normally (i.e. not through a <code>throw</code>)
	returned by an unanalyzed method. */
    public static final int RETURN          = 16;
    /** Possible type: Exception node. Nodes of this type are used to
	represent the objects returned as exceptions from an unalyzed
	method. */
    public static final int EXCEPT          = 32;
    /** Possible type: Static node. 
	The class nodes from the original algorithm have been renamed
	STATIC now (just for confusion :-)) */
    public static final int STATIC          = 64;

    // /** The null pointers are modeled as pointing to the special node
    // * NULL_Node of the special type NULL */
    // public static final int NULL            = 128;
    ///** A symbolic node for the null pointers */ 
    //public static final PANode NULL_Node = new PANode(NULL);

    /** The type of the node */
    public int type;

    /** <code>count</code> is used to generate unique IDs
	for debug purposes. */
    static int count = 0;
    /** Holds the unique ID */
    /////private 
    int number;

    // the depth of the call chain associated with this node
    private int call_chain_depth = 0;
    // call context sensitivity: the mapping CALL -> specialized node
    private LightMap cs_specs = null;
    // the parent of this PANode on the CALL specialization branch
    private PANode cs_parent = null;
    // the CALL site that specialized cs_parent into this node
    private CALL call_site = null;

    // when trying to specialize this node for more than
    // PointerAnalysis.MAX_SPEC_DEPTH-1 times, this node is returned
    // (it is created the first time); basically, since we cannot make
    // the difference between a node of depth MAX_SPEC_DEPTH and a node
    // with some bigger call depth, all we need to know is that we have a node
    // for which we canot infer where it was created.
    private PANode bottom = null;

    // full thread sensitivity: the mapping MetaMethod -> specialized node
    private LightMap ts_specs = null;
    // the weak thread specialization of this node (if any)
    private PANode wtspec = null;
    // the parent of this PANode on the thread specialization branch
    private PANode ts_parent = null;
    // the run MetaMethod site that specialized cs_parent into this node
    private MetaMethod run_mmethod = null;

    // the type of the objects represented by this node: it's either an
    // exact type (ie <hclass, GenType.MONO>) or a type cone
    // (ie <hclass, GenType.POLY>).
    private GenType[] node_class = null;

    /** Creates a <code>PANode</code> of type <code>type</code>.
	<code>type</code> must be one of <code>PANode.INSIDE</code>,
	<code>PANode.LOAD</code> etc. */  
    public PANode(final int type, final GenType[] node_class) {
	this(type, node_class, true);
    }

    /** Constructor using the default <code>null</code> value for
	<code>node_class</code>. */
    public PANode(final int type) {
	this(type, null);
    }

    // Make sure nobody calls the void constructor.
    public PANode() {
	Util.assert(false, "You should never call this void constructor!");
    }


    /** Returns the bar version of <code>this</code> node.
	Each node has two versions:
	<ul>
	<li>a <i>genuine</i> one - the node itself;
	<li>a <i>bar</i> one (the name comes from the way we
	depict it graphically).
	</ul>

	To avoid fake mappings, in the caller/callee and the
	starter/startee interaction, we work with the "bar" version of
	the callee/startee parallel interaction graph. After we determine
	the mapping, we go back to the "genuine" nodes.<br>

	This versions are different for (and only for) <code>LOAD</code>
	nodes. This makes sense because any non-trivial mappings are for
	<code>LOAD</code> nodes.<br>
    */
    public PANode getBarVersion() {
	return bar_version;
    } 
    private PANode bar_version = null;

    public PANode getGenuine() { 
	return isGenuine() ? this : genuine_version;
    }
    /** <code>null</code> for the genuine nodes. */
    private PANode genuine_version = null;

    /** Checks whether <code>this</code> node is a genuine one. */
    public boolean isGenuine() {
	return genuine_version == null;
    }

    // Does the real work!
    private PANode
	(final int type, final GenType[] node_class, boolean genuine) {
        this.type       = type;
	this.node_class = node_class;
	number = count++;

	if(PointerAnalysis.DEBUG)
	    if((number % 100) == 0)
		System.out.println("PANODE " + this);

	if(genuine) {
	    if(PointerAnalysis.CALL_CONTEXT_SENSITIVE)
		cs_specs = new LightMap();

	    if(PointerAnalysis.THREAD_SENSITIVE)
		ts_specs = new LightMap();

	    bar_version = 
		(type == LOAD) ? new PANode(type, node_class, this) : this;
	}
    }

    private PANode
	(final int type, final GenType[] node_class, PANode genuine) {
	this(type, node_class, false);
	this.genuine_version = genuine;
    }


    /** Returns the type of the node. */
    public final int type(){
	return type;
    }

    /** Checks if <code>this</code> node is an inside one. */
    public final boolean inside(){
	return type == INSIDE;
    }

    /** Returns an array of <code>GenType</code>s describing all
	the possible types (ie <code>HClass</code>es) this node can have.
	If we were unable to determine these types, <code>null</code> is
	returned instead; it stands for <i>any</i> type. */
    public final GenType[] getPossibleClasses() {
	if(getCallChainDepth() == 0)
	    return node_class;
	else
	    return getRoot().getPossibleClasses();
    }

    ////////////////////// CALL_CONTEXT_SENSITIVE /////////////////////////

    /** Returns the specialized node of <code>this</code> node for
	<code>call_site</code>. This method is guarranteed to return the
	same node if it's called on the same node, with the same argument.<br>
	This method makes sense iff
	<code>PointerAnalysis.CALL_CONTEXT_SENSITIVE</code> is on. */
    public final PANode csSpecialize(final CALL call_site){

	Util.assert(call_site != null, "call_site == null");

	if(CAUTION)
	    Util.assert(PointerAnalysis.CALL_CONTEXT_SENSITIVE,
			"Turn on CALL_CONTEXT_SENSITIVE!");

	// a node with a maximal call_chain_depth doesn't have precise
	// info about the call path which produced it; so it's not worth
	// specializing one for each call site: one is enough
	if(call_chain_depth == PointerAnalysis.MAX_SPEC_DEPTH - 1){
	    if(bottom == null){
		bottom = new PANode(this.type);
		bottom.call_chain_depth = this.call_chain_depth + 1;
		bottom.call_site = call_site;
		bottom.cs_parent = this;
		bottom.bottom = bottom;
	    }
	    return bottom;
	}
	    
	if(call_chain_depth >= PointerAnalysis.MAX_SPEC_DEPTH) return this;

	PANode spec = (PANode) cs_specs.get(call_site);
	if(spec == null){
	    spec = new PANode(this.type);
	    spec.call_chain_depth = this.call_chain_depth + 1;
	    spec.cs_parent = this;
	    spec.call_site = call_site;
	    cs_specs.put(call_site, spec);
	}

	return spec;
    }

    /** Returns all the call site specializations of <code>this</code> node.
	It returns a set of <code>Map.Entry</code>s, each element consisting
	of a mapping from a <code>CALL</code> quad (the call site) to the
	specialized node.<br>
	This method makes sense iff
	<code>PointerAnalysis.CALL_CONTEXT_SENSITIVE</code> is on. */
    public final Set getAllCSSpecs(){
	return cs_specs.entrySet();
    }

    /** Checks whether this node is a call site specialization of
	another node. */
    public final boolean isCSSpec(){
	return cs_parent != null;
    }

    /** Returns the parent of this node, on the call site specialization
	brach. */
    public final PANode getCSParent(){
	return cs_parent;
    }

    /** Returns the bottom field: the last specialization. */ 
    public final PANode getBottom(){
	return bottom;
    }

    /** Returns the specialization depth of <code>this</code> node. */
    public final int getCallChainDepth(){
	return call_chain_depth;
    }
    
    //////////////////////// THREAD_SENSITIVE /////////////////////////////

    /** Returns the specialized node of <code>this</code> node for the
	thread body (ie the <code>run()</code> method) <code>run</code>.
	This method is guarranteed to return the same node if it's called
	on the same node, with the same argument.<br>
	This method makes sense iff
	<code>PointerAnalysis.THREAD_SENSITIVE</code> is on. */
    public final PANode tSpecialize(final MetaMethod run){
	if(CAUTION){
	    Util.assert(!isTSpec(), "Repeated thread specialization!");
	    Util.assert(PointerAnalysis.THREAD_SENSITIVE,
			"Turn on THREAD_SENSITIVE!");
	}
	PANode spec = (PANode) ts_specs.get(run);
	if(spec == null){
	    spec = new PANode(this.type);
	    spec.ts_parent = this;
	    spec.run_mmethod = run;
	    ts_specs.put(run,spec);
	}

	return spec;
    }

    /** Returns all the thread specializations of <code>this</code> node.
	It returns a set of <code>Map.Entry</code>s, each element consisting
	of a mapping from a <code>MetaMethod</code> (the body of a thread)
	to the specialized node.<br>
	This method makes sense iff
	<code>PointerAnalysis.THREAD_SENSITIVE</code> is on. */
    public final Set getAllTSpecs(){
	return ts_specs.entrySet();
    }

    
    ////////////////////// WEAKLY_THREAD_SENSITIVE /////////////////////////

    /** Returns the thread specialization of <code>this</code> node.<br>
	This method makes sense iff
	<code>PointerAnalysis.WEAKLY_THREAD_SENSITIVE</code> is on. */
    public final PANode wtSpecialize(){
	if(CAUTION){
	    Util.assert(!isTSpec(), "Repeated thread specialization!");
	    Util.assert(PointerAnalysis.WEAKLY_THREAD_SENSITIVE,
			"Turn on WEAKLY_THREAD_SENSITIVE!");
	}
	if(wtspec == null){
	    wtspec = new PANode(this.type);
	    wtspec.ts_parent = this;
	}
	return wtspec;
    }

    /** Returns the weak thread specialization of <code>this</code> node.
	(returns <code>null</code> if none).<br>
	This method makes sense iff
	<code>PointerAnalysis.WEAKLY_THREAD_SENSITIVE</code> is on. */
    public final PANode getWTSpec(){
	return wtspec;
    }

    /** Checks whether this node is a thread specialization of some other
	node. */
    public final boolean isTSpec(){
	return ts_parent != null;
    }

    /** Returns the parent of this node, on the thread specialization
	branch. */
    public final PANode getTSParent(){
	return ts_parent;
    }
    /////////////////////////////////////////////////////////////////////////


    /** Returns the set of nodes that were obtained by specializing
	<code>this</code> one function of some call sites. */
    public Set getAllCSSpecializations(){
	return new HashSet(cs_specs.values());
    }

    /** Checks whether <code>this</code> node is a specialization of some
	other node. Relevant only if the
	<code>PointerAnalysis.CALL_CONTEXT_SENSITIVE</code> flag is on. */
    public final boolean isSpecialized(){
	return isCSSpec() || isTSpec();
    }

    /** Checks whether <code>this</code> node is an unspecialized one
	(a root node in the chain of specialization). Relevant only if the
	<code>PointerAnalysis.CALL_CONTEXT_SENSITIVE</code> flag is on. */
    public final boolean isPrimitive(){
	return !isSpecialized();
    }

    /** Returns the root of <code>this</code> node. This is the root of the
	specialization chain which produced <code>this</code> node. */
    public final PANode getRoot(){
	PANode node = this;
	while(!node.isPrimitive()){
	    if(node.isCSSpec()) node = node.getCSParent();
	    else if(node.isTSpec()) node = node.getTSParent();
	    else Util.assert(false,"Specialized node but not CS or TS");
	}
	return node;
    }

    /** Tests whether <code>this</code> is the bottom of some specialization
	chain, i.e.
	<code>call_chain_depth == PointerAnalysis.MAX_SPEC_DEPTH</code>. */
    public boolean isBottom(){
	return call_chain_depth == PointerAnalysis.MAX_SPEC_DEPTH;
    }

    /** Pretty-print function for debug purposes */
    public final String toString(){
	String str = null;
	switch(type){
	case INSIDE: str="I";break;
	case LOAD:   str="L";break;
	case PARAM:  str="P";break;
	case RETURN: str="R";break;
	case EXCEPT: str="E";break;
	case STATIC: str="S";break;
	}
	str = str + number;
	if(isBottom()) str += "B";
	return str;
    }

    /** Returns a detailed description of <code>this</code> node for debug
	purposes. */
    public final String details(){
	StringBuffer buffer = new StringBuffer();
	buffer.append(this);
	// print the ancestor on the thread specialization branch
	if(isTSpec()){
	    buffer.append(" <-TS- "); 
	    buffer.append(getTSParent().details());
	}
	// nodes that are at the limit of the maximal call chain depth
	// are signaled by attaching a B to them: they don't carry any
	// exact information about the call path so they are "bottom"
	if(call_chain_depth == PointerAnalysis.MAX_SPEC_DEPTH)
	    return buffer.toString();
	// print the ancestors on the call specialization brach
	if(isCSSpec()){
	    buffer.append(" <-CS- ");
	    buffer.append(getCSParent().details());
	}
	return buffer.toString();
    }

    /** Translates node <code>n</code> according to the map <code>map</code>.
       An unmapped node is implicitly mapped to itself. */
    static final PANode translate(final PANode n, final Map map){
	PANode n2 = (PANode) map.get(n);
	if(n2 == null) return n;
	return n2;
    }

    /** Specializes a set of PANodes according to the node mapping map. */
    static Set specialize_set(final Set set, final Map map) {
	final Set set2 = new HashSet();
	for(Iterator it = set.iterator(); it.hasNext(); )
	    set2.add(PANode.translate((PANode) it.next(), map));
	return set2;
    }

}

