// PANode.java, created Sun Jan  9 16:24:57 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.Util.Util;
import harpoon.IR.Quads.CALL;


/**
 * <code>PANode</code> class models a node for the Pointer Analysis
 * algorithm.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PANode.java,v 1.1.2.7 2000-03-03 06:23:15 salcianu Exp $
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
        type   = _type;
	number = count++;
	/// System.out.println("New node: " + this);
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

}

