// LowQuad.java, created Tue Jan 19 21:17:59 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

/**
 * The <code>LowQuad</code> interface identifies subclasses of
 * <code>harpoon.IR.Quads.Quad</code> as being members of the 
 * <code>LowQuad</code> representation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LowQuad.java,v 1.2 2002-02-25 21:04:40 cananian Exp $
 */
public abstract class LowQuad extends harpoon.IR.Quads.Quad {
    public LowQuad(LowQuadFactory qf, HCodeElement source) {
	super(qf, source);
    }

    // force visitor classes to be of type LowQuadVisitor.
    public void accept(harpoon.IR.Quads.QuadVisitor v) {
	accept( (LowQuadVisitor)v );
    }
    /** Accept a visitor. */
    abstract void accept(LowQuadVisitor v);
}
