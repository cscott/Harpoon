// METHOD.java, created Thu Aug  5  3:54:47 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.CloningTempMap;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * <code>Tree.METHOD</code> objects encode method-specific information:
 * the mapping of method formals to temporary variables, and
 * links to the exception handlers for the method. 
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: METHOD.java,v 1.1.2.10 2000-01-17 23:41:30 cananian Exp $
 */
public class METHOD extends Stm {
    /** The temporary variables used for method formals. */
    public TEMP[] params;
    
    /** Creates a <code>Tree.METHOD</code> object. 
     * @param params   temporaries which map directly to the 
     *                 parameters of this <code>Tree.METHOD</code>.
     *                 The first element should be a pointer to the 
     *                 exception-handling code for this method.  For 
     *                 non-static methods, the second parameter should be the 
     *                 <code>this</code> pointer of the caller.  Subsequent 
     *                 elements of the array should be the formal parameters
     *                 of the method, in the order which they are declared. 
     */
    public METHOD(TreeFactory tf, HCodeElement source, TEMP[] params) { 
        super(tf, source);
	Util.assert(params!=null); Util.assert(params.length>0);
	for (int i=0; i<params.length; i++) Util.assert(params[i].tf == tf);
	this.setParams(params);
    }

    public Tree getFirstChild() { 
	// Ok, b/c params.length MUST be greater than 0. 
	return this.params[0];
    }

    public TEMP[] getParams() { return this.params; }
    
    public void setParams(TEMP[] params) { 
	this.params = params; 

	Tree sibling = null; 
	for (int i=params.length-1; i>=0; i--) { 
	    TEMP param = params[i]; 
	    param.parent = this; 
	    param.sibling = sibling; 
	    sibling = param; 
	}
    }

    public int kind() { return TreeKind.METHOD; }

    public Stm build(ExpList kids) { return build(tf, kids); } 

    public Stm build(TreeFactory tf, ExpList kids) { 
	List sParams = new ArrayList();
	for (ExpList e=kids; e!=null; e=e.tail) {
	    Util.assert(e.head.tf == tf);
	    sParams.add(e.head);
	}
	TEMP[] tParams = (TEMP[])sParams.toArray(new TEMP[0]);
	return new METHOD(tf, this, tParams);
    }

    public Tree rename(TreeFactory tf, CloningTempMap ctm) { 
	TEMP[] newTmps = new TEMP[params.length];
	for (int i=0; i<params.length; i++) 
	    newTmps[i] = (TEMP)params[i].rename(tf, ctm);
	return new METHOD(tf,this,newTmps);
    }

    /** Accept a visitor. */
    public void accept(TreeVisitor v) { v.visit(this); }

    protected Set defSet() { 
	Set def = new HashSet();
	for (int i=0; i<params.length; i++)
	    def.add(params[i].temp);
	return def;
    }

    protected Set useSet() { return Collections.EMPTY_SET; }

    /** Returns human-readable representation of this <code>Tree</code>. */
    public String toString() {
	StringBuffer sb = new StringBuffer("METHOD(");
	for (int i=0; i<params.length-1; i++) {
	    sb.append(params[i].toString());
	    sb.append(", ");
	}
	sb.append(params[params.length-1].toString());
	sb.append(")");
	return sb.toString();
    }
}
