// AGET.java, created Wed Aug 26 19:02:57 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>AGET</code> represents an element fetch from an array object.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AGET.java,v 1.2 2002-02-25 21:05:11 cananian Exp $
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
    /** The component type of the referenced array. */
    protected HClass type;

    /** Creates an <code>AGET</code> object representing an element
     *  fetch from an array object.
     * @param dst 
     *        the <code>Temp</code> in which to store the fetched element.
     * @param objectref
     *        the array reference.
     * @param index
     *        the <code>Temp</code> holding the index of the element to get.
     * @param type
     *        the component type of the referenced array.
     */
    public AGET(QuadFactory qf, HCodeElement source,
		Temp dst, Temp objectref, Temp index, HClass type) {
	super(qf, source);
	this.dst = dst;
	this.objectref = objectref;
	this.index = index;
	this.type = type.isPrimitive() ? type :
	    type.getLinker().forDescriptor("Ljava/lang/Object;");
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
    /** Returns the component type of the referenced array. All
     *  non-primitive types become <code>Object</code>. */
    public HClass type() { return type; }

    /** Returns the <code>Temp</code> defined by this quad.
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns all the <code>Temp</code>s used by this quad.
     * @return the <code>objectref</code> and <code>index</code> fields. */
    public Temp[] use() { return new Temp[] { objectref, index }; }

    public int kind() { return QuadKind.AGET; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new AGET(qqf, this, map(defMap,dst),
			map(useMap,objectref), map(useMap,index), type);
    }
    /** Rename all used variables in this <code>Quad</code> according
     *  to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	objectref = tm.tempMap(objectref);
	index = tm.tempMap(index);
    }
    /** Rename all defined variables in this <code>Quad</code> according
     *  to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }
    
    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return dst.toString() + " = ("+type.getName()+") AGET " + objectref + "["+index+"]";
    }
}
