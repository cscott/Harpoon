// ASET.java, created Wed Aug 26 19:12:32 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>ASET</code> represents an array element assignment.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ASET.java,v 1.2 2002-02-25 21:05:12 cananian Exp $
 * @see ANEW
 * @see AGET
 * @see ALENGTH
 */
public class ASET extends Quad {
    /** The array reference */
    protected Temp objectref;
    /** The <code>Temp</code> holding the index of the element to get. */
    protected Temp index;
    /** The new value for the array element. */
    protected Temp src;
    /** The component type of the referenced array. */
    protected HClass type;

    /** Creates an <code>ASET</code> object representing an array element
     *  assignment.
     * @param objectref
     *        the <code>Temp</code> holding the array reference.
     * @param index
     *        the <code>Temp</code> holding the index of the element to get.
     * @param src
     *        the <code>Temp</code> holding the new value for the array
     *        element.
     * @param type
     *        the component type of the referenced array.
     */
    public ASET(QuadFactory qf, HCodeElement source,
		Temp objectref, Temp index, Temp src, HClass type) {
	super(qf, source);
	this.objectref = objectref;
	this.index = index;
	this.src = src;
	this.type = type.isPrimitive() ? type :
	    type.getLinker().forDescriptor("Ljava/lang/Object;");
	// VERIFY legality of this ASET
	Util.assert(objectref!=null && index!=null && src!=null);
    }
    // ACCESSOR FUNCTIONS:
    /** Returns the <code>Temp</code> with the array reference. */
    public Temp objectref() { return objectref; }
    /** Returns the <code>Temp</code> with the index of the element to get. */
    public Temp index() { return index; }
    /** Returns the <code>Temp</code> holding the new value for the array
     *  element. */
    public Temp src() { return src; }
    /** Returns the component type of the referenced array. All
     *  non-primitive types become <code>Object</code>. */
    public HClass type() { return type; }

    /** Returns all the Temps used by this quad. 
     * @return the <code>objectref</code>, <code>index</code>, and 
     *         <code>src</code> fields.
     */
    public Temp[] use() { return new Temp[] { objectref, index, src }; }

    public int kind() { return QuadKind.ASET; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new ASET(qqf, this, map(useMap,objectref),
			map(useMap,index), map(useMap,src), type);
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	objectref = tm.tempMap(objectref);
	index = tm.tempMap(index);
	src = tm.tempMap(src);
    }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) { }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return "ASET " + objectref + "["+index+"] = ("+type.getName()+") "+ src;
    }
}
