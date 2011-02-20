// ANEW.java, created Wed Aug 26 18:42:57 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>ANEW</code> represents an array creation operation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ANEW.java,v 1.5 2002-04-11 04:00:28 cananian Exp $
 * @see NEW
 * @see AGET
 * @see ASET
 * @see ALENGTH
 */
public class ANEW extends Quad {
    /** The <code>Temp</code> in which to store the new array reference. */
    protected Temp dst;
    /** Description of array class to create. */
    final protected HClass hclass;
    /** Lengths of each dimension to create. */
    protected Temp dims[];

    /** Creates an <code>ANEW</code> object. <code>ANEW</code> creates
     *  an array of the type and number of dimensions indicated by
     *  the <code>hclass</code> parameter.  Each entry in <code>dims</code>
     *  denotes the number of components in a particular dimension of the
     *  array.  <code>dims[0]</code> corresponds to the left-most dimension.
     *  The array class referenced by <code>hclass</code> may have more
     *  dimensions than the length of the <code>dims</code> parameter.  In
     *  that case, only the first <code>dims.length</code> dimensions of the
     *  array are created. 
     * @param dst
     *        the <code>Temp</code> in which to store the new array
     *        reference.
     * @param hclass
     *        the array class to create.
     * @param dims
     *        <code>Temp</code>s holding the length of each array
     *        dimension.
     */
    public ANEW(QuadFactory qf, HCodeElement source,
		Temp dst, HClass hclass, Temp dims[]) {
        super(qf, source);
	this.dst = dst;
	this.hclass = hclass;
	this.dims = dims;
	// VERIFY legality of this ANEW.
	assert dst!=null && hclass!=null && dims!=null;
	assert hclass.isArray();
	assert dims.length>0;
	for (int i=0; i<dims.length; i++)
	    assert dims[i]!=null;
    }
    // ACCESSOR FUNCTIONS:
    /** Returns the destination <code>Temp</code>. */
    public Temp dst() { return dst; }
    /** Returns the array class this <code>ANEW</code> will create. */
    public HClass hclass() { return hclass; }
    /** Returns an array of <code>Temp</code>s holding the length of
     *  each array dimension. */
    public Temp[] dims()
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, dims); }
    /** Returns a particular element of the <code>dims</code> array. */
    public Temp dims(int i) { return dims[i]; }
    /** Returns the length of the <code>dims</code> array. */
    public int dimsLength() { return dims.length; }

    /** Returns the Temp defined by this Quad. 
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns the Temps used by this Quad.
     * @return the <code>dims</code> field. */
    public Temp[] use() 
    { return (Temp[]) Util.safeCopy(Temp.arrayFactory, dims); }

    public int kind() { return QuadKind.ANEW; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new ANEW(qqf, this,
			map(defMap,dst), hclass, map(useMap, dims));
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	for (int i=0; i<dims.length; i++)
	    dims[i] = tm.tempMap(dims[i]);
    }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void accept(QuadVisitor v) { v.visit(this); }
    public <T> T accept(QuadValueVisitor<T> v) { return v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	HClass hc = hclass;
	int d,i;
	for (d = 0; hc.isArray(); d++)
	    hc = hc.getComponentType();
	StringBuffer sb = new StringBuffer(dst.toString() + " = ANEW ");
	sb.append(hc.getName());
	for (i = 0; i<dims.length; i++)
	    sb.append("["+dims[i]+"]");
	for (; i<d; i++)
	    sb.append("[]");
	return sb.toString();
    }
}
