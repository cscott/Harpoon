// METHOD.java, created Thu Aug  5  3:54:47 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Tree;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Arrays;
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
 * @version $Id: METHOD.java,v 1.2 2002-02-25 21:05:31 cananian Exp $
 */
public class METHOD extends Stm {
    private final int paramsLength;
    private final int retType;
    private final Label method;
    /** Creates a <code>Tree.METHOD</code> object. 
     * @param method   label to mark the top of the method
     * @param retType  integer enumeration (see <code>Typed</code>) of the
     *                 return type of this method, or -1 if the method
     *                 returns no value.
     * @param params   temporaries which map directly to the 
     *                 parameters of this <code>Tree.METHOD</code>.
     *                 The first element should be a pointer to the 
     *                 exception-handling code for this method.  For 
     *                 non-static methods, the second parameter should be the 
     *                 <code>this</code> pointer of the caller.  Subsequent 
     *                 elements of the array should be the formal parameters
     *                 of the method, in the order which they are declared. 
     */
    public METHOD(TreeFactory tf, HCodeElement source,
		  Label method, int retType, TEMP[] params) { 
        super(tf, source, params.length);
	Util.assert(method!=null);
	Util.assert(retType==-1 || Type.isValid(retType));
	Util.assert(params!=null); Util.assert(params.length>0);
	for (int i=0; i<params.length; i++) Util.assert(params[i].tf == tf);
	this.method = method;
	this.retType = retType;
	this.paramsLength = params.length;
	this.setParams(params);
    }
    /** Return the label which should mark the top of the method. */
    public Label getMethod() { return method; }
    /** Return an integer enumeration (see <code>Typed</code>) of the
     *  return type of the method.  Returns -1 if the method returns no
     *  value. */
    public int getReturnType() { return retType; }

    /** Return the temporary variables used for method formals. */
    public TEMP[] getParams() {
	TEMP[] result = new TEMP[paramsLength];
	int i=0;
	for (Tree t=getFirstChild(); t!=null; t=t.getSibling())
	    result[i++] = (TEMP) t;
	return result;
    }
    /** Set the temporary variables used for method formals. */
    public void setParams(TEMP[] params) { 
	Util.assert(paramsLength == params.length,
		    "Can't change number of parameters to METHOD");
	for (int i=0; i<paramsLength; i++)
	    setChild(i, params[i]);
    }

    // convenience/efficiency methods.
    public int getParamsLength() { return paramsLength; }
    public TEMP getParams(int i) { return (TEMP) getChild(i); }

    public int kind() { return TreeKind.METHOD; }

    public ExpList kids() { return null; /* definitions not considered kids */}
    public Stm build(TreeFactory tf, ExpList kids) { 
	Util.assert(kids==null);
	Util.assert(tf==this.tf, "cloning Params not yet implemented");
	return new METHOD(tf, this, method, retType, getParams());
    }

    public Tree rename(TreeFactory tf, TempMap tm, CloneCallback cb) {
	TEMP[] params  = getParams();
	TEMP[] newTmps = new TEMP[params.length];
	for (int i=0; i<params.length; i++) 
	    newTmps[i] = (TEMP)params[i].rename(tf, tm, cb);
	return cb.callback(this,
			   new METHOD(tf, this, method, retType, newTmps),
			   tm);
    }

    /** Accept a visitor. */
    public void accept(TreeVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this <code>Tree</code>. */
    public String toString() {
	StringBuffer sb = new StringBuffer("METHOD(");
	TEMP[] params = getParams();
	for (int i=0; i<params.length-1; i++) {
	    sb.append(params[i].toString());
	    sb.append(", ");
	}
	sb.append(params[params.length-1].toString());
	sb.append(")");
	return sb.toString();
    }
}
