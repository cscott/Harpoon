// ANEW.java, created Wed Aug 26 18:42:57 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * <code>ANEW</code> represents an array creation operation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ANEW.java,v 1.6 1998-09-11 18:23:17 cananian Exp $
 * @see NEW
 * @see AGET
 * @see ASET
 * @see ALENGTH
 */

public class ANEW extends Quad {
    /** The Temp in which to store the new array reference. */
    public Temp dst;
    /** Description of array class to create. */
    public HClass hclass;
    /** Lengths of each dimension to create. */
    public Temp dims[];

    /** Creates an <code>ANEW</code> object. <code>ANEW</code> creates
     *  an array of the type and number of dimensions indicated by
     *  the <code>hclass</code> parameter.  Each entry in <code>dims</code>
     *  denotes the number of components in a particular dimension of the
     *  array.  <code>dims[0]</code> corresponds to the left-most dimension.
     *  The array class referenced by <code>hclass</code> may have more
     *  dimensions than the length of the <code>dims</code> parameter.  In
     *  that case, only the first <code>dims.length</code> dimensions of the
     *  array are created. */
    public ANEW(HCodeElement source,
		Temp dst, HClass hclass, Temp dims[]) {
        super(source);
	this.dst = dst;
	this.hclass = hclass;
	this.dims = dims;
    }

    /** Returns the Temp defined by this Quad. 
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns the Temps used by this Quad.
     * @return the <code>dims</code> field. */
    public Temp[] use() { return (Temp[]) Util.copy(dims); }

    public void visit(Visitor v) { v.visit(this); }

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
