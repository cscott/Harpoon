// PHI.java, created Fri Aug  7 13:51:47 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>PHI</code> objects represent blocks of PHI functions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PHI.java,v 1.20 1998-11-10 01:09:00 cananian Exp $
 */

public class PHI extends Quad {
    public Temp dst[];
    public Temp src[][];
    /** Creates a <code>PHI</code> object. */
    public PHI(HCodeElement source,
	       Temp dst[], Temp src[][], int arity) {
        super(source, arity, 1);
	this.dst = dst;
	this.src = src;
    }
    /** Creates a <code>PHI</code> object with the specified arity. */
    public PHI(HCodeElement source,
	       Temp dst[], int arity) {
	this(source, dst, new Temp[dst.length][arity], arity);
	for (int i=0; i<dst.length; i++)
	    for (int j=0; j<arity; j++)
		this.src[i][j] = null;
    }

    /** Remove a predecessor from this phi. */
    public void remove(int which_pred) {
	prev = (Edge[]) Util.shrink(prev, which_pred);
	for (int i=0; i<dst.length; i++)
	    src[i] = (Temp[]) Util.shrink(src[i], which_pred);
	// fixup edges.
	for (int i=which_pred; i<prev.length; i++)
	    prev[i].to_index--;
	// double-check this.
	for (int i=0; i<prev.length; i++)
	    Util.assert(prev[i].to_index == i);
    }

    /** Grow the arity of a PHI by one. */
    public void grow(Temp args[]) {
	// increase number of prev links by one.
	Edge[] nprev = new Edge[prev.length+1];
	System.arraycopy(prev, 0, nprev, 0, prev.length);
	nprev[prev.length] = null;
	prev = nprev;
	// add contents of src to each phi function.
	for (int i=0; i<dst.length; i++) {
	    Temp[] nsrc = new Temp[src[i].length+1];
	    System.arraycopy(src[i], 0, nsrc, 0, src[i].length);
	    nsrc[src[i].length] = args[i];
	    src[i] = nsrc;
	}
    }
    /** Returns all the Temps used by this Quad. */
    public Temp[] use() {
	int n=0;
	for (int i=0; i<src.length; i++)
	    n+=src[i].length;
	Temp[] u = new Temp[n];
	n=0;
	for (int i=0; i<src.length; i++) {
	    System.arraycopy(src[i], 0, u, n, src[i].length);
	    n+=src[i].length;
	}
	return u;
    }
    /** Returns all the Temps defined by this Quad. */
    public Temp[] def() { return (Temp[]) dst.clone(); }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	for (int i=0; i<src.length; i++) {
	    for (int j=0; j<src[i].length; j++)
		src[i][j] = tm.tempMap(src[i][j]);
	}
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	for (int i=0; i<dst.length; i++) {
	    dst[i] = tm.tempMap(dst[i]);
	}
    }

    /** Properly clone <code>src[][]</code> and <code>dst[]</code> arrays. */
    public Object clone() throws CloneNotSupportedException {
	PHI q = (PHI) super.clone();
	q.dst = (Temp[]) dst.clone();
	q.src = (Temp[][]) src.clone();
	for (int i=0; i<q.src.length; i++)
	    q.src[i] = (Temp[]) src[i].clone();
	return q;
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer("PHI("+prev.length+"): ");
	for (int i=0; i<dst.length; i++) {
	    sb.append(dst[i].toString() + "=(");
	    for (int j=0; j<src[i].length; j++) {
		if (src[i][j]==null)
		    sb.append("null");
		else
		    sb.append(src[i][j].toString());
		if (j < src[i].length-1)
		    sb.append(",");
	    }
	    sb.append(")");
	    if (i < dst.length-1)
		sb.append("; ");
	}
	return sb.toString();
    }
}
