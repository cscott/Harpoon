// METHOD.java, created Tue Dec 15 14:20:38 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>METHOD</code> nodes encode method-specific information:
 * the mapping of method formals to temporary variables, and
 * links to the exception handlers for the method.<p>
 * The 0-edge out of the <Code>METHOD</code> quad points to the
 * beginning of the executable code for the method.  Other
 * edges point to <code>HANDLER</code> quads defining execution
 * handlers.  The lowest-numbered <code>HANDLER</code> edge 
 * (ie, the 1-edge) is the innermost nested try-block.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: METHOD.java,v 1.1.2.1 1998-12-17 21:38:36 cananian Exp $
 * @see HEADER
 * @see HANDLER
 */
public class METHOD extends Quad {
    /** the temporary variables used for method formals. */
    protected Temp[] params;
    
    /** Creates a <code>METHOD</code> quad. 
     * @param params the <code>Temp</code>s in which the formal parameters
     *               of the method will be passed.
     * @param arity  the number of outgoing edges from this 
     *               <code>METHOD</code>.  Always at least one.
     *               The number of exception handlers for this method is
     *               <code>(arity-1)</code>.
     */
    public METHOD(QuadFactory qf, HCodeElement source,
		  Temp[] params, int arity) {
        super(qf, source, 1 /* predecessor is HEADER */, arity);
	Util.assert(arity>=1);
	Util.assert(params!=null);
	this.params = params;
    }
    /** Returns the arity of this <code>METHOD</code>. */
    public int arity() { return next.length; }
    /** Returns the <code>params</code> array which associates
     *  <code>Temp</code>s with formal parameters of a method. */
    public Temp[] params()
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, params); }
    /** Returns a specified member of the <code>params</code> array. */
    public Temp params(int i) { return params[i]; }
    /** Returns the length of the <code>params</code> array. */
    public int  paramsLength() { return params.length; }
    
    /** Returns the <code>Temp</code>s defined by this <code>Quad</code>. */
    public Temp[] def() {
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, params);
    }

    public int kind() { return QuadKind.METHOD; }

    public Quad rename(QuadFactory qqf, TempMap tm) {
	return new METHOD(qqf, this, map(tm, params), arity());
    }
    /** Rename all defined variables in this <code>Quad</code>. */
    void renameDefs(TempMap tm) {
	for (int i=0; i<params.length; i++)
	    params[i] = tm.tempMap(params[i]);
    }

    /** Properly clone <code>params[]</code> array. */
    public Object clone() {
	METHOD q = (METHOD) super.clone();
	q.params = (Temp[]) params.clone();
	return q;
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this <code>Quad</code>. */
    public String toString() {
	StringBuffer sb = new StringBuffer("METHOD(");
	for (int i=0; i<params.length; i++) {
	    sb.append(params[i].toString());
	    if (i<params.length-1)
		sb.append(", ");
	}
	sb.append(")");
	sb.append("["+arity()+"]");
	return sb.toString();
    }
}
