// PMETHOD.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

/**
 * <code>PMETHOD</code> converts an object reference into a
 * <code>POINTER</code> value that can be used to invoke
 * object methods.  Only necessary for <b>virtual</b> methods.
 * Non-virtual methods can be invoked via the <code>POINTER</code>
 * obtained by <code>PMCONST</code>. <br>
 * <b>NOTE THAT</b> constructors and invocations using the <code>super</code>
 * keyword are non-virtual.  See the <code>harpoon.IR.Quads.CALL</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PMETHOD.java,v 1.2 2002-02-25 21:04:40 cananian Exp $
 */
public class PMETHOD extends PPTR {

    /** Creates a <code>PMETHOD</code> representing a conversion from
     *  an object reference into a <code>POINTER</code> that
     *  can be used to invoke <b>virtual</b> methods.
     * @param dst
     *        the <code>Temp</code> in which to store the computed
     *        <code>POINTER</code>.
     * @param objectref
     *        the <code>Temp</code> holding the reference for the
     *        object whose virtual methods we would like to invoke.
     */
    public PMETHOD(LowQuadFactory qf, HCodeElement source,
		   final Temp dst, final Temp objectref) {
	super(qf, source, dst, objectref);
    }

    public int kind() { return LowQuadKind.PMETHOD; }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PMETHOD((LowQuadFactory)qf, this,
			   map(defMap, dst), map(useMap, objectref));
    }

    void accept(LowQuadVisitor v) { v.visit(this); }

    public String toString() {
	return dst.toString() + " = PMETHOD " + objectref.toString();
    }
}
