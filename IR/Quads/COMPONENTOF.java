// COMPONENTOF.java, created Wed Sep  9 13:05:33 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>COMPONENTOF</code> objects implement the test needed to determine
 * if an <code>ASET</code> needs to throw an exception.  Specifically,
 * <code>COMPONENTOF</code> evaluates to boolean <code>true</code> if
 * a certain temporary is an instance of the component
 * type of a certain array, or boolean <code>false</code> otherwise.
 * The given array should <strong>never</strong> have components of primitive
 * type, as <code>COMPONENTOF</code> would always return <code>true</code> in
 * this case, given a type-safe program.<p>
 *
 * <strong>In quad-with-try form ONLY:</strong>
 * The component object to test (<code>componentref</code>) may have
 * the value null, in which case <code>COMPONENTOF</code> returns
 * <code>true</code> (it is always safe to store <code>null</code>
 * in an object array).
 * <strong>In all other forms the component object must be provably
 * non-null.</strong>  An explicit null-check may be needed prior to the
 * <code>COMPONENTOF</code> if the component object cannot be
 * proven non-null.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: COMPONENTOF.java,v 1.4 2002-04-10 03:05:14 cananian Exp $
 * @see ASET
 * @see "The Java Virtual Machine Specification"
 */
public class COMPONENTOF extends Quad {
    /** The <code>Temp</code> in which to store the result of the test. */
    protected Temp dst;
    /** The array object to test. */
    protected Temp arrayref;
    /** The component object to test. */
    protected Temp objectref;

    /** Creates a <code>COMPONENTOF</code> representing a typecheck test.
     * @param dst
     *        the <code>Temp</code> in which to store the result of the test.
     *        The <code>Temp</code> specified by <code>dst</code> gets a
     *        boolean <code>true</code> value if <code>objectref</code>
     *        contains a reference to an instance
     *        of the component type of the array in <code>arrayref</code>
     *        or any subtype; or a boolean <code>false</code> value otherwise.
     *        In quad-with-try form <strong>only</strong>, the result is also
     *        <code>true</code> if <code>objectref</code> is
     *        <code>null</code>; in all other forms this possibility
     *        is a semantic error.
     * @param arrayref
     *        the array object to test.  <strong>Should never be an
     *        array of primitive component type.</strong>
     *        The <code>Temp</code> specified by <code>arrayref</code> 
     *        <strong>should never</strong> contain the value 
     *        <code>null</code> at run-time.
     * @param objectref
     *        the component object to test.
     *        The <code>Temp</code> specified by <code>objectref</code> 
     *        <strong>may</strong> contain the value <code>null</code> 
     *        at run-time <strong>if and only if</strong> we are in
     *        quad-with-try form.
     */
    public COMPONENTOF(QuadFactory qf, HCodeElement source, 
		       Temp dst, Temp arrayref, Temp objectref) {
	super(qf, source);
	this.dst = dst;
	this.arrayref = arrayref;
	this.objectref = objectref;
	assert dst!=null && arrayref!=null && objectref!=null;
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> in which to store the result of the
     *  type check test. */
    public Temp dst() { return dst; }
    /** Returns the array reference to test. */
    public Temp arrayref() { return arrayref; }
    /** Returns the component object reference to test. */
    public Temp objectref() { return objectref; }

    /** Returns the <code>Temp</code>s used by this quad. */
    public Temp[] use() { return new Temp[] { arrayref, objectref }; }
    /** Returns the <code>Temp</code>s defined by this quad. */
    public Temp[] def() { return new Temp[] { dst }; }

    public int kind() { return QuadKind.COMPONENTOF; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new COMPONENTOF(qqf, this, map(defMap,dst), 
			       map(useMap,arrayref), map(useMap,objectref));
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	arrayref = tm.tempMap(arrayref);
	objectref = tm.tempMap(objectref);
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
	    objectref.toString() + " COMPONENTOF " + arrayref.toString();
    }
}
