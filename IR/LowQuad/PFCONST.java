// PFCONST.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>PFCONST</code> computes the <code>POINTER</code> constant
 * needed to access a given <b>static</b> field. <code>PFIELD</code> and
 * <code>PFOFFSET</code> must be used together to access a non-static field.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PFCONST.java,v 1.4 2002-04-10 03:04:57 cananian Exp $
 */
public class PFCONST extends PCONST {
    /** The <code>HField</code> to address. */
    protected final HField field;
    
    /** Creates a <code>PFCONST</code> representing the <code>POINTER</code>
     *  constant needed to access a given <b>static</b> field.
     * @param dst
     *        the <code>Temp</code> in which to store the computed
     *        <code>POINTER</code> constant.
     * @param field
     *        the <code>HField</code> whose <code>POINTER</code> constant
     *        to compute.
     */
    public PFCONST(LowQuadFactory qf, HCodeElement source,
		    final Temp dst, final HField field) {
	super(qf, source, dst);
	this.field = field;
	assert field!=null && field.isStatic();
    }
    // ACCESSOR METHODS:
    /** Returns the field whose <code>POINTER</code> constant is computed. */
    public HField field() { return field; }

    public int kind() { return LowQuadKind.PFCONST; }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PFCONST((LowQuadFactory)qf, this,
			    map(defMap, dst), field);
    }

    void accept(LowQuadVisitor v) { v.visit(this); }

    public String toString() {
	return dst.toString() + " = PFCONST " +
	    field.getDeclaringClass().getName() + "." + field.getName();
    }
}
