// SIGMA.java, created Mon Sep 14 03:03:50 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>SIGMA</code> functions are added where control flow splits. <p>
 * They have the form: <code>&lt;t1, t2, ..., tn&gt; = sigma(t0)</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SIGMA.java,v 1.1.2.2 1998-12-09 00:54:03 cananian Exp $
 */

public abstract class SIGMA extends Quad {
    public Temp dst[][];
    public Temp src[];
    
    /** Creates a <code>SIGMA</code>. */
    public SIGMA(HCodeElement source, Temp dst[][], Temp src[], int arity) {
        super(source, 1, arity);
	this.dst = dst;
	this.src = src;
    }
    public SIGMA(HCodeElement source, Temp src[], int arity) {
	this(source, new Temp[src.length][arity], src, arity);
    }

    public void assign(Temp[] d, int which_succ) {
	Util.assert(d.length == src.length);
	for (int i=0; i < d.length; i++)
	    dst[i][which_succ] = d[i];
    }

    public Temp[] use()
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, src); }

    public Temp[] def() {
	int n=0;
	for (int i=0; i<dst.length; i++)
	    n+=dst[i].length;
	Temp[] d = new Temp[n];
	n=0;
	for (int i=0; i<dst.length; i++) {
	    System.arraycopy(dst[i], 0, d, n, dst[i].length);
	    n+=dst[i].length;
	}
	return d;
    }
    public void renameUses(TempMap tm) {
	for (int i=0; i<src.length; i++) {
	    src[i] = tm.tempMap(src[i]);
	}
    }
    public void renameDefs(TempMap tm) {
	for (int i=0; i<dst.length; i++) {
	    for (int j=0; j<dst[i].length; j++)
		dst[i][j] = tm.tempMap(dst[i][j]);
	}
    }

    /** Properly clone <code>dst[][]</code> and <code>src[]</code> arrays. */
    public Object clone() {
	SIGMA q = (SIGMA) super.clone();
	q.dst = (Temp[][]) dst.clone();
	q.src = (Temp[]) src.clone();
	for (int i=0; i<q.dst.length; i++)
	    q.dst[i] = (Temp[]) dst[i].clone();
	return q;
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    public String toString() {
	StringBuffer sb = new StringBuffer("SIGMA("+next().length+"): ");
	for (int i=0; i<src.length; i++) {
	    sb.append("<");
	    for (int j=0; j<dst[i].length; j++) {
		sb.append(dst[i][j].toString());
		if (j < dst[i].length - 1)
		    sb.append(", ");
	    }
	    sb.append(">=");
	    sb.append(src[i].toString());
	    if (i < src.length-1)
		sb.append("; ");
	}
	return sb.toString();
    }
}
