// PHI.java, created Fri Aug  7 13:51:47 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>PHI</code> objects represent blocks of PHI functions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PHI.java,v 1.1.2.4 1998-12-11 22:21:05 cananian Exp $
 */
public class PHI extends Quad {
    /** dst[i] is the left-hand side of the i'th phi function in this block. */
    protected Temp dst[];
    /** src[i][j] is the j'th parameter to the i'th phi function in this 
     *	block. */
    protected Temp src[][];

    /** Creates a <code>PHI</code> object representing a block of
     *  phi functions.
     * @param dst
     *        the left hand sides of a phi function assignment block.
     * @param src
     *        the phi function parameters in a phi function assignment block.
     */
    public PHI(HCodeElement source,
	       Temp dst[], Temp src[][], int arity) {
        super(source, arity, 1);
	this.dst = dst;
	this.src = src;
	// VERIFY legality of PHI function.
	Util.assert(dst!=null && src!=null);
	Util.assert(arity>=0);
	Util.assert(dst.length==src.length);
	for (int i=0; i<src.length; i++)
	    Util.assert(src[i].length==arity);
	Util.assert(arity==arity());
    }
    /** Creates a <code>PHI</code> object with the specified arity.
     *  Each phi function will have <code>arity</code> arguments.
     * @param dst
     *        the left hand sides of the phi functions.
     * @param arity
     *        the number of predecessors of this quad.
     */
    public PHI(HCodeElement source,
	       Temp dst[], int arity) {
	this(source, dst, new Temp[dst.length][arity], arity);
    }
    // ACCESSOR METHODS:
    /** Returns the right hand side of the <code>nPhi</code>'th phi
     *  function assignment in the block. */
    public Temp dst(int nPhi) { return dst[nPhi]; }
    /** Returns the <code>nParam</code>'th argument of the
     *  <code>nPhi</code>'th phi function in the block. */
    public Temp src(int nPhi, int nParam) { return src[nPhi][nParam]; }
    /** Returns an array holding the arguments to the <code>nPhi</code>'th
     *  phi function in the block. */
    public Temp[] src(int nPhi)
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, src[nPhi]); }
    
    /** Returns the number of phi functions in the block. */
    public int numPhis() { return dst.length; }
    /** Returns the number of arguments each phi function has. */
    public int arity() { return prev.length; }

    /** Removes a given phi function from the block. */
    public void removePhi(int nPhi) {
	Util.assert(0<=nPhi && nPhi<numPhis());
	dst = (Temp[])   Util.shrink(Temp.arrayFactory,       dst, nPhi);
	src = (Temp[][]) Util.shrink(Temp.doubleArrayFactory, src, nPhi);
    }

    /** Remove a predecessor from this phi, reducing the arity. */
    public void removePred(int which_pred) {
	prev = (Edge[]) Util.shrink(Edge.arrayFactory, prev, which_pred);
	for (int i=0; i<dst.length; i++)
	    src[i] = (Temp[]) Util.shrink(Temp.arrayFactory,
					  src[i], which_pred);
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
    public Temp[] def()
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, dst); }
    
    public int kind() { return QuadKind.PHI; }

    public Quad rename(TempMap tm) {
	return new PHI(this, map(tm,dst), map(tm,src), arity());
    }
    /** Rename all used variables in this Quad according to a mapping. */
    void renameUses(TempMap tm) {
	for (int i=0; i<src.length; i++) {
	    for (int j=0; j<src[i].length; j++)
		src[i][j] = tm.tempMap(src[i][j]);
	}
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    void renameDefs(TempMap tm) {
	for (int i=0; i<dst.length; i++) {
	    dst[i] = tm.tempMap(dst[i]);
	}
    }

    /** Properly clone <code>src[][]</code> and <code>dst[]</code> arrays. */
    public Object clone() {
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
