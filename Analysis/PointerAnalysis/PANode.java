// PANode.java, created Sun Jan  9 16:24:57 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
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
import harpoon.Tools.DataStructs.LightMap;

/**
 * <code>PANode</code> class models a node for the Pointer Analysis
 * algorithm.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PANode.java,v 1.1.2.10 2000-03-19 23:50:04 salcianu Exp $
 */
final public class PANode {
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
    public final int type;

    /** <code>count</code> is used to generate unique IDs
	for debug purposes. */
    static int count = 0;
    /** Holds the unique ID */
    private final int number;

    // the depth of the call chain associated with this node, if it's
    // a specialized one.
    private int call_chain_depth = 0;

    // call context sensitivity: the mapping CALL -> specialized node
    private final LightMap cs_specs;
    // full thread sensitivity: the mapping MetaMethod -> speciliazed node
    private final LightMap ts_specs;
    // thread sensitivity: specialized or not?
    private boolean  thread_spec = false;
    // the weak thread specialization of this node (if any)
    private PANode wtspec = null;

    /** Creates a <code>PANode</code> of type <code>type</code.
	<code>type</code> must be one of <code>PANode.INSIDE</code>,
	<code>PANode.LOAD</code> etc. */  
    public PANode(final int type) {
        this.type = type;
	number = count++;

	if(PointerAnalysis.CALL_CONTEXT_SENSITIVE)
	    cs_specs = new LightMap();
	else cs_specs = null;

	if(PointerAnalysis.THREAD_SENSITIVE)
	    ts_specs = new LightMap();
	else ts_specs = null;
    }


    /** Returns the type of the node. */
    public final int type(){
	return type;
    }

    /** Checks if <code>this</code> node is an inside one. */
    public final boolean inside(){
	return type==INSIDE;
    }

    ////////////////////// CALL_CONTEXT_SENSITIVE /////////////////////////

    /** Returns the specialized node of <code>this</code> node for
	<code>call_site</code>. This method is guarranteed to return the
	same node if it's called on the same node, with the same argument.
	This method makes sense iff
	<code>PointerAnalysis.CALL_CONTEXT_SENSITIVE</code> is on. */
    public final PANode cs_specialize(final CALL call_site){
	if(CAUTION)
	    Util.assert(PointerAnalysis.CALL_CONTEXT_SENSITIVE,
			"Turn on CALL_CONTEXT_SENSITIVE!");
	    
	if(call_chain_depth >= PointerAnalysis.MAX_SPEC_DEPTH) return this;

	PANode spec = (PANode) cs_specs.get(call_site);
	if(spec == null){
	    spec = new PANode(this.type);
	    spec.call_chain_depth = this.call_chain_depth + 1;
	    cs_specs.put(call_site, spec);
	}

	return spec;
    }

    /** Returns all the call site specializations of <code>this</code> node.
	It returns a set of <code>Map.Entry</code>s, each element consisting
	of a mapping from a <code>CALL</code> quad (the call site) to the
	specialized node.
	This method makes sense iff
	<code>PointerAnalysis.CALL_CONTEXT_SENSITIVE</code> is on. */
    public final Set getAllCSSpecs(){
	return cs_specs.entrySet();
    }

    //////////////////////// THREAD_SENSITIVE /////////////////////////////

    /** Returns the specialized node of <code>this</code> node for the
	thread body (ie the <code>run()</code> method) <code>run</code>.
	This method is guarranteed to return the same node if it's called
	on the same node, with the same argument.
	This method makes sense iff
	<code>PointerAnalysis.THREAD_SENSITIVE</code> is on. */
    public final PANode ts_specialize(final MetaMethod run){
	if(CAUTION){
	    Util.assert(!thread_spec, "Repeated thread specialization!");
	    Util.assert(PointerAnalysis.THREAD_SENSITIVE,
			"Turn on THREAD_SENSITIVE!");
	}
	PANode spec = (PANode) ts_specs.get(run);
	if(spec == null){
	    spec = new PANode(this.type);
	    spec.thread_spec = true;
	    ts_specs.put(run,spec);
	}

	return spec;
    }

    /** Returns all the thread specializations of <code>this</code> node.
	It returns a set of <code>Map.Entry</code>s, each element consisting
	of a mapping from a <code>MetaMethod</code> (the body of a thread)
	to the specialized node.
	This method makes sense iff
	<code>PointerAnalysis.THREAD_SENSITIVE</code> is on. */
    public final Set getAllTSpecs(){
	return ts_specs.entrySet();
    }

    
    ////////////////////// WEAKLY_THREAD_SENSITIVE /////////////////////////

    /** Returns the thread specialization of <code>this</code> node.
	This method makes sense iff
	<code>PointerAnalysis.WEAKLY_THREAD_SENSITIVE</code> is on. */
    public final PANode ts_specialize(){
	if(CAUTION){
	    Util.assert(!thread_spec, "Repeated thread specialization!");
	    Util.assert(PointerAnalysis.THREAD_SENSITIVE,
			"Turn on WEAKLY_THREAD_SENSITIVE!");
	}
	if(wtspec == null){
	    wtspec = new PANode(this.type);
	    wtspec.thread_spec = true;
	}
	return wtspec;
    }

    /** Checks whether <code>this</code> node was produced as a weak
	thread specialization of some other node. This method makes sense iff
	<code>PointerAnalysis.WEAKLY_THREAD_SENSITIVE</code> is on. */
    public final boolean isWTSpecialized(){
	return wtspec != null;
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
	return (call_chain_depth != 0) || thread_spec;
    }

    /** Checks whether <code>this</code> node is an unspecialized one
	(a root node in the chain of specialization). Relevant only if the
	<code>PointerAnalysis.CALL_CONTEXT_SENSITIVE</code> flag is on. */
    public final boolean isPrimitive(){
	return !isSpecialized();
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
	return str + number;
    }

    /** Translates node <code>n</code> according to the map <code>map</code>.
       An unmapped node is implicitly mapped to itself. */
    static final PANode translate(final PANode n, final Map map){
	PANode n2 = (PANode) map.get(n);
	if(n2 == null) return n;
	return n2;
    }

    /** Specializes a set of PANodes according to the node mapping map. */
    static Set specialize_set(final Set set, final Map map){
	final Set set2 = new HashSet();
	for(Iterator it = set.iterator(); it.hasNext(); )
	    set2.add(PANode.translate((PANode) it.next(), map));
	return set2;
    }

}

