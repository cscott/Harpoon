// INSTANCEOF.java, created Tue Sep  1 21:09:43 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
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
 * @version $Id: INSTANCEOF.java,v 1.1.2.3 1998-12-11 22:21:05 cananian Exp $ 
 */
public class INSTANCEOF extends Quad {
    /** The <code>Temp</code> in which to store the result of the test. */
    protected Temp dst;
    /** The <code>Temp</code> to evaluate. */
    protected Temp src;
    /** The class in which <code>src</code> is tested for membership. */
    final protected HClass hclass;

    /** Creates a <code>INSTANCEOF</code> representing a typecheck test.
     * @param dst
     *        the <code>Temp</code> in which to store the result of the test.
     * @param src
     *        the <code>Temp</code> to test.
     * @param hclass
     *        the class in which <code>src</code> is tested for membership.
     */
    public INSTANCEOF(HCodeElement source,
		      Temp dst, Temp src, HClass hclass) {
	super(source);
	this.dst = dst;
	this.src = src;
	this.hclass = hclass;
	// VERIFY legality of INSTANCEOF
	Util.assert(dst!=null && src!=null && hclass!=null);
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

    public Quad rename(TempMap tm) {
	return new INSTANCEOF(this, map(tm,dst), map(tm,src), hclass);
    }
    /** Rename all used variables in this Quad according to a mapping. */
    void renameUses(TempMap tm) {
	src = tm.tempMap(src);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	return dst.toString() + " = " + 
	    src.toString() + " INSTANCEOF " + hclass.getName();
    }
}
