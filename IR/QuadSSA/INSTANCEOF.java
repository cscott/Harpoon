// INSTANCEOF.java, created Tue Sep  1 21:09:43 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
/**
 * <code>INSTANCEOF</code> objects represent an 'instanceof' evaluation.
 * <code>INSTANCEOF</code> assigns a boolean value to a temporary after
 * evaluating whether a certain temporary is an instance of a given
 * class type.<p>
 *
 * <strong>It is a semantic error for the <code>src</code> Temp to be able to
 * have the value <code>null</code> at run-time.</strong> A separate
 * null-pointer test should always precede the <code>INSTANCEOF</code>
 * quad if src may be null at run-time.  Standard java
 * <code>instanceof</code> returns <code>true</code> given a
 * <code>null</code> source object.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INSTANCEOF.java,v 1.8 1998-10-10 03:27:24 cananian Exp $ 
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

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	src = tm.tempMap(src);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	return dst.toString() + " = " + 
	    src.toString() + " INSTANCEOF " + hclass.getName();
    }
}
