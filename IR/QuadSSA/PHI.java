// PHI.java, created Fri Aug  7 13:51:47 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>PHI</code> objects represent blocks of PHI functions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PHI.java,v 1.5 1998-09-03 01:21:57 cananian Exp $
 */

public class PHI extends Quad {
    public Temp dst[];
    public Temp src[][];
    /** Creates a <code>PHI</code> object. */
    public PHI(String sourcefile, int linenumber,
	       Temp dst[], Temp src[][], int arity) {
        super(sourcefile,linenumber, arity, 1);
	this.dst = dst;
	this.src = src;
    }
    /** Creates a <code>PHI</code> object with the specified arity. */
    public PHI(String sourcefile, int linenumber,
	       Temp dst[], int arity) {
	this(sourcefile,linenumber, dst, new Temp[dst.length][arity], arity);
	for (int i=0; i<dst.length; i++)
	    for (int j=0; j<arity; j++)
		this.src[i][j] = null;
    }
    PHI(HCodeElement hce, Temp dst[], Temp src[][], int arity) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, src, arity);
    }
    PHI(HCodeElement hce, Temp dst[], int arity) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, arity);
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
    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	return "PHI("+src[0].length+")"; // XXX
    }
}
