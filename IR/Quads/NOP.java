// NOP.java, created Tue Aug 25 03:01:12 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.TempMap;

/**
 * <code>NOP</code> nodes do nothing.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NOP.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 */
public class NOP extends Quad {
    
    /** Creates a <code>NOP</code>. */
    public NOP(QuadFactory qf, HCodeElement source) {
        super(qf, source);
    }

    public int kind() { return QuadKind.NOP; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new NOP(qqf, this);
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) { }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) { }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation. */
    public String toString() { return "NOP"; }
}
