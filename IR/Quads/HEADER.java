// HEADER.java, created Fri Aug  7 15:19:12 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>HEADER</code> nodes are used to anchor the top end of the
 * quad graph.  They do not represent bytecode.<p>
 * The 0-edge out of the <code>HEADER</code> points to the 
 * <code>FOOTER</code> quad for the method.  The 1-edge out of the
 * <code>HEADER</code> points to the <code>Quads.METHOD</code> quad at
 * which to begin execution.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HEADER.java,v 1.3 2002-04-11 04:00:34 cananian Exp $
 * @see METHOD
 * @see FOOTER
 */
public class HEADER extends Quad {

    /** Creates a <code>HEADER</code> quad.
     */
    public HEADER(QuadFactory qf, HCodeElement source) {
        super(qf, source, 0 /* no predecessors */, 2 /* FOOTER and METHOD */);
    }
    /** Returns the <code>FOOTER</code> corresponding to this 
     *  <code>HEADER</code>. */
    public FOOTER footer() { return (FOOTER) next(0); }
    /** Returns the <code>Quads.METHOD</code> following this <code>HEADER</code>. */
    public METHOD method() { return (METHOD) next(1); }

    public int kind() { return QuadKind.HEADER; }
    
    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new HEADER(qqf, this);
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) { }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) { }

    public void accept(QuadVisitor v) { v.visit(this); }
    public <T> T accept(QuadValueVisitor<T> v) { return v.visit(this); }

    /** Returns human-readable representation of this Quad. */
    public String toString() {
	return "HEADER";
    }
}
