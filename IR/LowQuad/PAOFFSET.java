// PAOFFSET.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>PAOFFSET</code> computes the <code>POINTER</code> offset
 * needed to access a given array element.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PAOFFSET.java,v 1.3 2002-02-26 22:45:51 cananian Exp $
 */
public class PAOFFSET extends PCONST {
    /** The array type. */
    protected final HClass arrayType;
    /** The index into the array. */
    protected final Temp index;

    /** Creates a <code>PAOFFSET</code> representing the <code>POINTER</code>
     *  offset needed to access a given array element.
     * @param dst
     *        the <code>Temp</code> in which to store the computed offset.
     * @param arrayType
     *        the type of the array the element is in.
     * @param index
     *        the index of the array element to address.
     */
    public PAOFFSET(LowQuadFactory qf, HCodeElement source,
		    final Temp dst, final HClass arrayType, final Temp index) {
	super(qf, source, dst);
	this.arrayType = arrayType;
	this.index = index;
	Util.ASSERT(arrayType!=null && arrayType.isArray(),
		    "arrayType ("+arrayType+") is not valid.");
	Util.ASSERT(index!=null);
    }
    /** Returns the array type. */
    public HClass arrayType() { return arrayType; }
    /** Returns the <code>Temp</code> holding the index of the array element
     *  to address. */
    public Temp index() { return index; }

    public int kind() { return LowQuadKind.PAOFFSET; }

    public Temp[] use() { return new Temp[] { index }; }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PAOFFSET((LowQuadFactory)qf, this,
			    map(defMap, dst), arrayType, map(useMap, index));
    }

    void accept(LowQuadVisitor v) { v.visit(this); }

    public String toString() {
	HClass hc = arrayType;
	int d;
	for (d=0; hc.isArray(); d++)
	    hc = hc.getComponentType();
	StringBuffer sb = new StringBuffer();
	sb.append(dst.toString() + " = PAOFFSET ");
	sb.append(hc.getName());
	for (int i=0; i<d-1; i++)
	    sb.append("[]");
	sb.append("["+index+"]");
	return sb.toString();
    }
}
