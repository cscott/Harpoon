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


/**
 * <code>PANode</code> class models a node for the Pointer Analysis
 * algorithm.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PANode.java,v 1.1.2.9 2000-03-18 23:39:28 salcianu Exp $
 */
public class PANode {
    
    /** The possible node types */ 
    /** Inside node */
    public static final int INSIDE          = 1;
    // /** Non-thread inside node */
    // public static final int INSIDE_THREAD   = 2;
    public static final int LOAD            = 4;
    public static final int PARAM           = 8;
    public static final int RETURN          = 16;
    public static final int EXCEPT          = 32;
    /** The class nodes from the original algorithm have been renamed
     *  STATIC now (just for confusion :-)) */
    public static final int STATIC          = 64;

    /** The null pointers are modeled as pointing to the special node
     * NULL_Node of the special type NULL */
    public static final int NULL       = 128;
    ///** A symbolic node for the null pointers */ 
    //public static final PANode NULL_Node = new PANode(NULL);

    /** The type of the node */
    public int type;

    /** <code>count</code> is used to generate unique IDs 
     *  for debug purposes. */
    static int count = 0;
    /** Holds the unique ID */
    public int number;

    /** Creates a <code>PANode</code>. */
    public PANode(int _type) {
        type   = _type;
	number = count++;
	// System.out.println("New node: " + this);
    }

    /** Returns the type of the node. */
    public final int type(){
	return type;
    }

    /** Checks if the node is an inside one */
    public final boolean inside(){
	return type==INSIDE;
    }

    /** Does nothing. This method is implemented only by the context sensitive
	version, the PANodeCS subclass */
    public PANode specialize(CALL q){
	Util.assert(false,"Don't call specialize on a PANode object!");
	return null;
    }

    /** Returns the set of nodes that are just a specialization of this one. */
    public Set getAllSpecializations(){
	return Collections.EMPTY_SET;
    }

    /** Checks whether this node is a specialization of some other node. */
    public boolean isSpecialized(){
	return false;
    }

    /** Returns the parent node (for a specialized node).
     The default implementation always returns <code>null</code>. */
    public PANode getParent(){ return null; }

    /** Returns the call site where this node was created through
	specialization. The default implementation always return
	<code>null</code> (of course, this method is overriden in
	<code>PANodeCS</code>. */
    public CALL getCallSite(){
	return null;
    }

    /** Pretty-print function for debug purposes */
    public String toString(){
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


    /* Translate node <code>n</code> according to the map <code>map</code>.
       An unmapped node is implicitly mapped to itself. */
    static final PANode translate(PANode n, Map map){
	PANode n2 = (PANode) map.get(n);
	if(n2 == null) return n;
	return n2;
    }

    // specialize a set of PANodes according to the node mapping map. 
    static Set specialize_set(final Set set, final Map map){
	final Set set2 = new HashSet();
	for(Iterator it = set.iterator(); it.hasNext(); )
	    set2.add(PANode.translate((PANode) it.next(), map));
	return set2;
    }

}

