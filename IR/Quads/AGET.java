// AGET.java, created Wed Aug 26 19:02:57 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>AGET</code> represents an element fetch from an array object.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AGET.java,v 1.1.2.4 1998-12-17 21:38:35 cananian Exp $
 * @see ANEW
 * @see ASET
 * @see ALENGTH
 */
public class AGET extends Quad {
    /** The <code>Temp</code> in which to store the fetched element. */
    protected Temp dst;
    /** The array reference. */
    protected Temp objectref;
    /** The <code>Temp</code> holding the index of the element to get. */
    protected Temp index;

    /** Creates an <code>AGET</code> object representing an element
     *  fetch from an array object.
     * @param dst 
     *        the <code>Temp</code> in which to store the fetched element.
     * @param objectref
     *        the array reference.
     * @param index
     *        the <code>Temp</code> holding the index of the element to get.
     */
    public AGET(QuadFactory qf, HCodeElement source,
		Temp dst, Temp objectref, Temp index) {
	super(qf, source);
	this.dst = dst;
	this.objectref = objectref;
	this.index = index;
	// VERIFY legality of this AGET.
	Util.assert(dst!=null && objectref!=null && index!=null);
    }
    /** Returns the destination <code>Temp</code>. */
    public Temp dst() { return dst; }
    /** Returns the array reference <code>Temp</code>. */
    public Temp objectref() { return objectref; }
    /** Returns the <code>Temp</code> holding the index of the element
     *  to fetch. */
    public Temp index() { return index; }

    /** Returns the Temp defined by this quad.
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns all the Temps used by this quad.
     * @return the <code>objectref</code> and <code>index</code> fields. */
    public Temp[] use() { return new Temp[] { objectref, index }; }

    public int kind() { return QuadKind.AGET; }

    public Quad rename(QuadFactory qqf, TempMap tm) {
	return new AGET(qqf, this,
			map(tm,dst), map(tm,objectref), map(tm,index));
    }
    /** Rename all used variables in this Quad according to a mapping. */
    void renameUses(TempMap tm) {
	objectref = tm.tempMap(objectref);
	index = tm.tempMap(index);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }
    
    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return dst.toString() + " = AGET " + objectref + "["+index+"]";
    }
}
