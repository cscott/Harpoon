// PPTR.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * <code>PPTR</code> is an abstract superclass of the <code>LowQuad</code>s
 * that convert object references into <code>POINTER</code> types.  We
 * allow arithmetic only on <code>POINTER</code>s.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PPTR.java,v 1.3 2002-02-26 22:45:51 cananian Exp $
 */
public abstract class PPTR extends LowQuad {
    /** The <code>Temp</code> in which to store the computed
     *  <code>POINTER</code> value.
     */
    protected final Temp dst;
    /** The <code>Temp</code> holding the object reference to
     *  convert. */
    protected final Temp objectref;
    
    /** Creates a <code>PPTR</code>. */
    public PPTR(LowQuadFactory qf, HCodeElement source,
		final Temp dst, final Temp objectref) {
	super(qf, source);
	this.dst = dst;
	this.objectref = objectref;
	Util.ASSERT(dst != null && objectref != null);
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> in which to store the computed
     *  <code>POINTER</code> value. */
    public Temp dst() { return dst; }
    /** Returns the <code>Temp</code> containing the object reference
     *  to convert into a <code>POINTER</code>. */
    public Temp objectref() { return objectref; }

    public Temp[] use() { return new Temp[] { objectref }; }
    public Temp[] def() { return new Temp[] { dst }; }
}
