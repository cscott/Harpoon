// PHI.java, created Fri Aug  7 13:51:47 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>PHI</code> objects represent blocks of PHI functions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PHI.java,v 1.15 1998-09-13 23:57:28 cananian Exp $
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

    /** Remove a predecessor from this phi.  <p>
     *  prev[which_pred] should be null. */
    public void remove(int which_pred) {
	Util.assert(prev[which_pred]==null);
	prev = (Edge[]) Util.shrink(prev, which_pred);
	for (int i=0; i<dst.length; i++)
	    src[i] = (Temp[]) Util.shrink(src[i], which_pred);
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

    /** Rename all variables in a Quad according to a mapping. */
    public void rename(TempMap tm) {
	for (int i=0; i<dst.length; i++) {
	    dst[i] = tm.tempMap(dst[i]);
	    for (int j=0; j<src[i].length; j++)
		src[i][j] = tm.tempMap(src[i][j]);
	}
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer("PHI("+prev().length+"): ");
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
