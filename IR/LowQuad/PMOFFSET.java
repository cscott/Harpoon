// PMOFFSET.java, created Wed Jan 20 21:47:52 1999 by cananian
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
 * <code>PMOFFSET</code> computes the <code>POINTER</code> offset
 * needed to invoke a given <b>virtual</b> method. Non-virtual methods
 * are invoked via the <code>POINTER</code> obtained from
 * <code>PMCONST</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PMOFFSET.java,v 1.3.2.1 2002-02-27 08:36:27 cananian Exp $
 */
public class PMOFFSET extends PCONST {
    /** The <code>HMethod</code> to address. */
    protected final HMethod method;

    /** Creates a <code>PMOFFSET</code> representing the <code>POINTER</code>
     *  offset needed to invoke a given <b>virtual</b> method.
     * @param dst
     *        the <code>Temp</code> in which to store the computed offset.
     * @param method
     *        the <code>HMethod</code> whose offset to compute.
     */
    public PMOFFSET(LowQuadFactory qf, HCodeElement source,
		    final Temp dst, final HMethod method) {
	super(qf, source, dst);
	this.method = method;
	assert method!=null && !method.isStatic() &&
		    !(method instanceof HConstructor) &&
		    !Modifier.isPrivate(method.getModifiers());
    }
    // ACCESSOR METHODS:
    /** Returns the method whose offset is computed. */
    public HMethod method() { return method; }

    public int kind() { return LowQuadKind.PMOFFSET; }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PMOFFSET((LowQuadFactory)qf, this,
			    map(defMap, dst), method);
    }

    void accept(LowQuadVisitor v) { v.visit(this); }

    public String toString() {
	return dst.toString() + " = PMOFFSET " +
	    method.getDeclaringClass().getName()+"."+method.getName();
    }
}
