// PHI.java, created Fri Aug  7 13:51:47 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>PHI</code> objects represent blocks of PHI functions.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PHI.java,v 1.3 1998-08-24 19:30:02 cananian Exp $
 */

public class PHI extends Quad {
    public Temp dst[];
    public Temp src[][];
    /** Creates a <code>PHI</code> object. */
    public PHI(String sourcefile, int linenumber,
	       Temp dst[], Temp src[][]) {
        super(sourcefile,linenumber);
	this.dst = dst;
	this.src = src;
    }
    /** Creates a <code>PHI</code> object with the specified arity. */
    public PHI(String sourcefile, int linenumber,
	       Temp dst[], int arity) {
	this(sourcefile,linenumber, dst, new Temp[dst.length][arity]);
	for (int i=0; i<dst.length; i++)
	    for (int j=0; j<arity; j++)
		this.src[i][j] = null;
    }
    PHI(HCodeElement hce, Temp dst[], Temp src[][]) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, src);
    }
    PHI(HCodeElement hce, Temp dst[], int arity) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, arity);
    }
    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	return "PHI("+src[0].length+")"; // XXX
    }
}
