// PANode.java, created Sun Jan  9 16:24:57 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

/**
 * <code>PANode</code> class models a node for the Pointer Analysis
 * algorithm.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PANode.java,v 1.1.2.3 2000-01-18 04:49:40 salcianu Exp $
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
    /** The class nodes from the original algorithm have been renamed
     *  STATIC now (just for confusion :-)) */
    public static final int STATIC          = 32;

    /** The null pointers are modeled as pointing to the special node
     * NULL_Node of the special type NULL */
    public static final int NULL       = 64;
    /** A symbolic node for the null pointers */ 
    public static final PANode NULL_Node = new PANode(NULL);

    /** The type of the node */
    public int type;

    /** <code>count</code> is used to generate unique IDs 
     *  for debug purposes. */
    private static int count = 0;
    /** Holds the unique ID */
    public int number;

    /** Creates a <code>PANode</code>. */
    public PANode(int _type) {
	System.out.println("New node of type: " + _type);
        type   = _type;
	number = count++;
    }

    /** Returns the type of the node. */
    public final int type(){
	return type;
    }

    /** Checks if the node is an inside one */
    public final boolean inside(){
	return type==INSIDE;
    }

    /** Pretty-print function for debug purposes */
    public String toString(){
	String str = null;
	switch(type){
	case INSIDE: str="I";break;
	case LOAD:   str="L";break;
	case PARAM:  str="P";break;
	case RETURN: str="R";break;
	case STATIC: str="S";break;
	}
	return str + number;
    }

}
