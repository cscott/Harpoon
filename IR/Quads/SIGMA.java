// SIGMA.java, created Mon Sep 14 03:03:50 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>SIGMA</code> functions are added where control flow splits. <p>
 * They have the form: <code>&lt;t1, t2, ..., tn&gt; = sigma(t0)</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SIGMA.java,v 1.3 2002-02-26 22:45:57 cananian Exp $
 */
public abstract class SIGMA extends Quad {
    /** dst[i][j] is the j'th element of the tuple on the left-hand side
     *  of the i'th sigma function in this block. */
    protected Temp dst[][];
    /** src[i] is the argument to the i'th sigma function in this block. */
    protected Temp src[];
    
    /** Creates a <code>SIGMA</code> representing a block of sigma 
     *  functions.
     * @param dst
     *        the elements of the tuples on the left-hand side of a sigma
     *        function assignment block.
     * @param src
     *        the arguments to the sigma functions in this block.
     */
    public SIGMA(QuadFactory qf, HCodeElement source,
		 Temp dst[][], Temp src[], int arity) {
        super(qf, source, 1, arity);
	this.dst = dst;
	this.src = src;
	Util.ASSERT(dst!=null && src!=null);
	Util.ASSERT(arity>0);
	Util.ASSERT(dst.length==src.length);
	for (int i=0; i<dst.length; i++)
	    Util.ASSERT(dst[i].length==arity);
	Util.ASSERT(arity==arity());
    }
    /** Creates a <code>SIGMA</code> object with the specified arity.
     *  Each sigma function will return a tuple with <code>arity</code>
     *  elements.
     * @param src
     *        the arguments to the sigma functions.
     * @param arity
     *        the number of successors to this quad.
     */
    public SIGMA(QuadFactory qf, HCodeElement source, Temp src[], int arity) {
	this(qf, source, new Temp[src.length][arity], src, arity);
    }
    // ACCESSOR METHODS:
    /** Returns the argument to the <code>nSigma</code>'th sigma function in
     *  the block. */
    public Temp src(int nSigma) { return src[nSigma]; }
    /** Returns the <code>nTuple</code>'th element of the tuple returned
     *  by the <code>nSigma</code>'th sigma function in the block. */
    public Temp dst(int nSigma, int nTuple) { return dst[nSigma][nTuple]; }
    /** Returns an array holding the elements of the tuple returned by
     *  the <code>nSigma</code>'th sigma function in the block. */
    public Temp[] dst(int nSigma)
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, dst[nSigma]); }
    
    public Temp[] src() {
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, src);
    }
    public Temp[][] dst() {
	return (Temp[][]) Util.safeCopy(Temp.doubleArrayFactory, dst);
    }

    /** Returns the number of sigma functions in the block. */
    public int numSigmas() { return src.length; }
    /** Returns the number of elements in the tuple returned by each 
     *  sigma function. */
    public int arity() { return next.length; }

    /** Removes a given sigma function from the block.
     *  @deprecated Does not preserve immutability. */
    public void removeSigma(int nSigma) {
	Util.ASSERT(0<=nSigma && nSigma<numSigmas());
	src = (Temp[])   Util.shrink(Temp.arrayFactory,       src, nSigma);
	dst = (Temp[][]) Util.shrink(Temp.doubleArrayFactory, dst, nSigma);
    }

    public void assign(Temp[] d, int which_succ) {
	Util.ASSERT(d.length == src.length);
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

    /* @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	for (int i=0; i<src.length; i++) {
	    src[i] = tm.tempMap(src[i]);
	}
    }
    /* @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
	for (int i=0; i<dst.length; i++) {
	    for (int j=0; j<dst[i].length; j++)
		dst[i][j] = tm.tempMap(dst[i][j]);
	}
    }

    public void accept(QuadVisitor v) { v.visit(this); }

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
