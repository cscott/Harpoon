// PFOFFSET.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>PFOFFSET</code> computes the <code>POINTER</code> offset 
 * needed to access a given <b>non-static</b> field. <code>PFCONST</code>
 * is used to access a static field.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PFOFFSET.java,v 1.2 2002-02-25 21:04:40 cananian Exp $
 */
public class PFOFFSET extends PCONST {
    /** The <code>HField</code> to address. */
    protected final HField field;
    
    /** Creates a <code>PFOFFSET</code> representing the <code>POINTER</code>
     *  offset needed to access a given <b>non-static</b> field.
     * @param dst
     *        the <code>Temp</code> in which to store the computed offset.
     * @param field
     *        the <code>HField</code> whose offset to compute.
     */
    public PFOFFSET(LowQuadFactory qf, HCodeElement source,
		    final Temp dst, final HField field) {
	super(qf, source, dst);
	this.field = field;
	Util.assert(field!=null && !field.isStatic());
    }
    // ACCESSOR METHODS:
    /** Returns the field whose offset is computed. */
    public HField field() { return field; }

    public int kind() { return LowQuadKind.PFOFFSET; }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PFOFFSET((LowQuadFactory)qf, this,
			    map(defMap, dst), field);
    }

    void accept(LowQuadVisitor v) { v.visit(this); }

    public String toString() {
	return dst.toString() + " = PFOFFSET " +
	    field.getDeclaringClass().getName() + "." + field.getName();
    }
}
