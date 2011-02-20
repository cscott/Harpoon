// PARRAY.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

/**
 * <code>PARRAY</code> converts an array object reference into a
 * <code>POINTER</code> value that can be used to access array elements.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PARRAY.java,v 1.3 2002-04-11 04:00:21 cananian Exp $
 */
public class PARRAY extends PPTR {
    
    /** Creates a <code>PARRAY</code> representing a conversion
     *  from an array object reference into a <code>POINTER</code> that
     *  can be used to reference array elements.
     * @param dst
     *        the <code>Temp</code> in which to store the computed
     *        <code>POINTER</code>.
     * @param objectref
     *        the <code>Temp</code> holding the reference for the array
     *        object whose elements we would like to access
     */
    public PARRAY(LowQuadFactory qf, HCodeElement source,
		  final Temp dst, final Temp objectref) {
	super(qf, source, dst, objectref);
    }

    public int kind() { return LowQuadKind.PARRAY; }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PARRAY((LowQuadFactory)qf, this,
			  map(defMap, dst), map(useMap, objectref));
    }

    void accept(LowQuadVisitor v) { v.visit(this); }
    <T> T accept(LowQuadValueVisitor<T> v) { return v.visit(this); }

    public String toString() {
	return dst.toString() + " = PARRAY " + objectref.toString();
    }
}
