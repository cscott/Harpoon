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
 * @version $Id: INSTANCEOF.java,v 1.3 1998-09-11 17:13:57 cananian Exp $
 */

public class INSTANCEOF extends Quad {
    /** The temp in which to store the result of the evaluation. */
    public Temp dst;
    /** The temp to evaluate. */
    public Temp src;
    /** The class in which <code>src</code> is tested for membership. */
    public HClass hclass;

    /** Creates a <code>INSTANCEOF</code>. */
    public INSTANCEOF(HCodeElement source,
		      Temp dst, Temp src, HClass hclass) {
	super(source);
	this.dst = dst;
	this.src = src;
	this.hclass = hclass;
    }

    /** Returns the <code>Temp</code>s used by this quad. */
    public Temp[] use() { return new Temp[] { src }; }
    /** Returns the <code>Temp</code>s defined by this quad. */
    public Temp[] def() { return new Temp[] { dst }; }

    public void accept(Visitor v) { v.visit(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	return dst.toString() + " = " + 
	    src.toString() + " INSTANCEOF " + hclass.getName();
    }
}
