// PCONST.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

/**
 * <code>PCONST</code> is an abstract superclass of the <code>LowQuad</code>s
 * that encode symbolic offsets or constants for array elements, fields, and
 * methods.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PCONST.java,v 1.4 2002-04-10 03:04:57 cananian Exp $
 */
public abstract class PCONST extends LowQuad {
    /** The <code>Temp</code> in which to store the offset or constant. */
    protected final Temp dst;
    
    /** Creates a <code>PCONST</code>. */
    public PCONST(LowQuadFactory qf, HCodeElement source, final Temp dst) {
	super(qf, source);
	this.dst = dst;
	assert dst != null;
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> in which to store the offset or
     *  constant. */
    public Temp dst() { return dst; }

    public Temp[] def() { return new Temp[] { dst }; }
}
