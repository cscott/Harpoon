// HEADER.java, created Fri Aug  7 15:19:12 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>HEADER</code> nodes are used to anchor the top end of the
 * quad graph.  They do not represent bytecode.<p>
 *
 * The <code>params</code> field of the <code>HEADER</code> tracks
 * the temporary variable names used for method parameters.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HEADER.java,v 1.1.2.3 1998-12-11 22:21:05 cananian Exp $
 * @see FOOTER
 */
public class HEADER extends Quad {
    /** the footer corresponding to this header. */
    protected FOOTER footer;
    /** the temporary variables used for method formals. */
    protected Temp[] params;

    /** Creates a <code>HEADER</code>. 
     * @param footer the footer corresponding to this header.
     * @param params the <code>Temp</code>s in which the formal parameters
     *               of the method will be passed.
     */
    public HEADER(HCodeElement source, FOOTER footer, Temp[] params) {
        super(source, 0 /* no predecessors */, 1);
	this.footer = footer;
	this.params = params;
	// VERIFY legality of HEADER.
	Util.assert(footer!=null && params!=null);
    }
    /** Returns the <code>FOOTER</code> corresponding to this 
     *  <code>HEADER</code>. */
    public FOOTER footer() { return footer; }
    /** Returns the <code>params</code> array which associates
     *  <code>Temp</code>s with formal parameters of a method. */
    public Temp[] params()
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, params); }
    /** Returns a specified member of the <code>params</code> array. */
    public Temp params(int i) { return params[i]; }
    /** Returns the length of the <code>params</code> array. */
    public int  paramsLength() { return params.length; }

    /** Returns the temps defined by this Quad. */
    public Temp[] def() {
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, params);
    }

    public int kind() { return QuadKind.HEADER; }
    
    public Quad rename(TempMap tm) {
	return new HEADER(this, footer, map(tm, params));
    }
    /** Rename all used variables in this Quad according to a mapping. */
    void renameUses(TempMap tm) { }
    /** Rename all defined variables in this Quad according to a mapping. */
    void renameDefs(TempMap tm) {
	for (int i=0; i<params.length; i++)
	    params[i] = tm.tempMap(params[i]);
    }

    /** Properly clone <code>params[]</code> array. */
    public Object clone() {
	HEADER q = (HEADER) super.clone();
	q.params = (Temp[]) params.clone();
	return q;
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer("HEADER(");
	for (int i=0; i<params.length; i++) {
	    sb.append(params[i].toString());
	    if (i<params.length-1)
		sb.append(", ");
	}
	sb.append(")");
	sb.append(": footer is #"+footer.getID());
	return sb.toString();
    }
}
