// ALENGTH.java, created Wed Aug 26 18:58:09 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>ALENGTH</code> represents an array length query.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ALENGTH.java,v 1.3.2.1 2002-02-27 08:36:32 cananian Exp $
 * @see ANEW
 * @see AGET
 * @see ASET
 */
public class ALENGTH extends Quad {
    /** The <code>Temp</code> in which to store the array length. */
    protected Temp dst;
    /** The array reference to query. */
    protected Temp objectref;
    
    /** Creates a <code>ALENGTH</code> representing an array length
     *  query.
     * @param dst
     *        the <code>Temp</code> in which to store the array length.
     * @param objectref
     *        the <code>Temp</code> holding the array reference to query.
     */
    public ALENGTH(QuadFactory qf, HCodeElement source,
		   Temp dst, Temp objectref) {
	super(qf, source);
	this.dst = dst;
	this.objectref = objectref;
	// VERIFY legality of this ALENGTH
	assert dst!=null && objectref!=null;
    }
    /** Returns the destination <code>Temp</code>. */
    public Temp dst() { return dst; }
    /** Returns the <code>Temp</code> holding the array reference to query. */
    public Temp objectref() { return objectref; }

    /** Returns the <code>Temp</code> defined by this Quad. 
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns the <code>Temp</code> used by this Quad.
     * @return the <code>objectref</code> field. */
    public Temp[] use() { return new Temp[] { objectref }; }

    public int kind() { return QuadKind.ALENGTH; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new ALENGTH(qqf, this,
			   map(defMap,dst), map(useMap,objectref));
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	objectref = tm.tempMap(objectref);
    }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return dst.toString() + " = ALENGTH " + objectref;
    }
}
