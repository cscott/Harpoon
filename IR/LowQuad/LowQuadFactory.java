// LowQuadFactory.java, created Wed Jan 20 22:14:25 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

/**
 * <code>LowQuadFactory</code> is a trivial subclass of
 * <code>QuadFactory</code> which assigns unique numbers to the
 * <code>Quad</code>s and <code>LowQuad</code>s in LowQuad form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: LowQuadFactory.java,v 1.2 2002-02-25 21:04:40 cananian Exp $
 */
public abstract class LowQuadFactory extends harpoon.IR.Quads.QuadFactory {
    // no new methods.

    public String toString() {
	return "LowQuadFactory["+getParent().toString()+"]";
    }
}
