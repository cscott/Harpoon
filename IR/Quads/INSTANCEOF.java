// INSTANCEOF.java, created Tue Sep  1 21:09:43 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>INSTANCEOF</code> objects represent an 'instanceof' evaluation.
 * <code>INSTANCEOF</code> assigns a boolean value to a temporary after
 * evaluating whether a certain temporary is an instance of a given
 * class type.<p>
 * <strong>In quad-with-try form ONLY:</strong>
 * The <code>src</code> <code>Temp</code> may have the value
 * <code>null</code>, in which case <code>INSTANCEOF</code> evaluates to
 * <code>false</code>.
 * <strong>In all other forms the <code>src</code> <code>Temp</code>
 * should always contain a provably non-null value at runtime.</strong>
 * (An explicit null-check may need to be added prior to the 
 * <code>INSTANCEOF</code> if the value cannot be proven non-null.)
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: INSTANCEOF.java,v 1.4 2002-04-10 03:05:14 cananian Exp $ 
 */
public class INSTANCEOF extends Quad {
    /** The <code>Temp</code> in which to store the result of the test. */
    protected Temp dst;
    /** The <code>Temp</code> to evaluate. */
    protected Temp src;
    /** The class in which <code>src</code> is tested for membership. */
    final protected HClass hclass;

    /** Creates a <code>INSTANCEOF</code> representing a type check.
     * @param dst
     *        the <code>Temp</code> in which to store the result of the test.
     * @param src
     *        the <code>Temp</code> to test.
     * @param hclass
     *        the class in which <code>src</code> is tested for membership.
     */
    public INSTANCEOF(QuadFactory qf, HCodeElement source,
		      Temp dst, Temp src, HClass hclass) {
	super(qf, source);
	this.dst = dst;
	this.src = src;
	this.hclass = hclass;
	// VERIFY legality of INSTANCEOF
	assert dst!=null && src!=null && hclass!=null;
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> in which to store the result of the
     *  <code>instanceof</code> test. */
    public Temp dst() { return dst; }
    /** Returns the <code>Temp</code> to test. */
    public Temp src() { return src; }
    /** Returns the class in which <code>src</code> is tested for
     *  membership. */
    public HClass hclass() { return hclass; }

    /** Returns the <code>Temp</code>s used by this quad. */
    public Temp[] use() { return new Temp[] { src }; }
    /** Returns the <code>Temp</code>s defined by this quad. */
    public Temp[] def() { return new Temp[] { dst }; }

    public int kind() { return QuadKind.INSTANCEOF; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new INSTANCEOF(qqf, this,
			      map(defMap,dst), map(useMap,src), hclass);
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	src = tm.tempMap(src);
    }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	return dst.toString() + " = " + 
	    src.toString() + " INSTANCEOF " + hclass.getName();
    }
}
