// PPTR.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>PPTR</code> is an abstract superclass of the <code>LowQuad</code>s
 * that convert object references into <code>POINTER</code> types.  We
 * do not allow arithmetic on object references, but we do allow
 * <code>POINTER</code> arithmetic.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PPTR.java,v 1.1.2.1 1999-01-21 05:19:16 cananian Exp $
 */
public abstract class PPTR extends LowQuad {
    /** The <code>Temp</code> in which to store the computed
     *  <code>POINTER</code> value.
     */
    protected final Temp dst;
    
    /** Creates a <code>PPTR</code>. */
    public PPTR(LowQuadFactory qf, HCodeElement source, final Temp dst) {
	super(qf, source);
	this.dst = dst;
	Util.assert(dst != null);
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> in which to store the computed
     *  <code>POINTER</code> value. */
    public Temp dst() { return dst; }

    public Temp[] def() { return new Temp[] { dst }; }
}
