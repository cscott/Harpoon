// PMCONST.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

import java.lang.reflect.Modifier;
/**
 * <code>PMCONST</code> computes the <code>POINTER</code> constant
 * needed to invoke a given <b>non-virtual</b> method.  Virtual methods
 * are invoked using <code>PMETHOD</code> and <code>PMOFFSET</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PMCONST.java,v 1.4 2002-04-10 03:04:57 cananian Exp $
 */
public class PMCONST extends PCONST {
    /** The <code>HMethod</code> to address. */
    protected final HMethod method;

    /** Creates a <code>PMCONST</code> representing the <code>POINTER</code>
     *  constant needed to invoke a given <b>non-virtual</b> method.
     * @param dst
     *        the <code>Temp</code> in which to store the computed
     *        <code>POINTER</code> constant.
     * @param method
     *        the <code>HMethod</code> whose <code>POINTER</code>
     *        constant to compute.
     */
    public PMCONST(LowQuadFactory qf, HCodeElement source,
		    final Temp dst, final HMethod method) {
	super(qf, source, dst);
	this.method = method;
	assert method!=null; // can't really check better than this.
    }
    // ACCESSOR METHODS:
    /** Returns the method whose offset is computed. */
    public HMethod method() { return method; }

    public int kind() { return LowQuadKind.PMCONST; }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PMCONST((LowQuadFactory)qf, this,
			   map(defMap, dst), method);
    }

    void accept(LowQuadVisitor v) { v.visit(this); }

    public String toString() {
	return dst.toString() + " = PMCONST " +
	    method.getDeclaringClass().getName()+"."+method.getName();
    }
}
