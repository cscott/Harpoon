// NOP.java, created Tue Aug 25 03:01:12 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.TempMap;
/**
 * <code>NOP</code> nodes do nothing.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NOP.java,v 1.8 1998-10-11 02:37:57 cananian Exp $
 */

public class NOP extends Quad {
    
    /** Creates a <code>NOP</code>. */
    public NOP(HCodeElement source) {
        super(source);
    }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) { }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) { }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation. */
    public String toString() { return "NOP"; }
}
