// INSTANCEOF.java, created Tue Sep  1 21:09:43 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>INSTANCEOF</code> objects represent an 'instanceof' evaluation.
 * <code>INSTANCEOF</code> assigns a boolean value to a temporary after
 * evaluating whether a certain temporary is an instance of a given
 * class type.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INSTANCEOF.java,v 1.1 1998-09-02 01:22:47 cananian Exp $
 */

public class INSTANCEOF extends Quad {
    /** The temp in which to store the result of the evaluation. */
    public Temp dst;
    /** The temp to evaluate. */
    public Temp src;
    /** The class in which <code>src</code> is tested for membership. */
    public HClass hclass;

    /** Creates a <code>INSTANCEOF</code>. */
    public INSTANCEOF(String sourcefile, int linenumber,
		      Temp dst, Temp src, HClass hclass) {
	super(sourcefile, linenumber);
	this.dst = dst;
	this.src = src;
	this.hclass = hclass;
    }
    INSTANCEOF(HCodeElement hce, Temp dst, Temp src, HClass hclass) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, src, hclass);
    }
    /** Returns the <code>Temp</code>s used by this quad. */
    public Temp[] use() { return new Temp[] { src }; }
    /** Returns the <code>Temp</code>s defined by this quad. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	return dst.toString() + " = " + 
	    src.toString() + " INSTANCEOF " + hclass.getName();
    }
}
