// AGET.java, created Wed Aug 26 19:02:57 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

/**
 * <code>AGET</code> represents an element fetch from an array object.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AGET.java,v 1.1.2.1 1998-12-01 12:36:41 cananian Exp $
 * @see ANEW
 * @see ASET
 * @see ALENGTH
 */

public class AGET extends Quad {
    /** The Temp in which to store the fetched element. */
    public Temp dst;
    /** The array reference. */
    public Temp objectref;
    /** The Temp holding the index of the element to get. */
    public Temp index;

    /** Creates an <code>AGET</code> object. */
    public AGET(HCodeElement source,
		Temp dst, Temp objectref, Temp index) {
	super(source);
	this.dst = dst;
	this.objectref = objectref;
	this.index = index;
    }

    /** Returns the Temp defined by this quad.
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns all the Temps used by this quad.
     * @return the <code>objectref</code> and <code>index</code> fields. */
    public Temp[] use() { return new Temp[] { objectref, index }; }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	objectref = tm.tempMap(objectref);
	index = tm.tempMap(index);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return dst.toString() + " = AGET " + objectref + "["+index+"]";
    }
}
