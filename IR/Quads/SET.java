// SET.java, created Wed Aug  5 07:04:09 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>SET</code> represents field assignment-to operations.
 * The <code>objectref</code> is null if the field is static.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SET.java,v 1.1.2.3 1998-12-11 22:21:05 cananian Exp $
 */
public class SET extends Quad {
    /** The field description. */
    final protected HField field;
    /** Reference to the object containing the field. <p>
     *  <code>null</code> if the field is static.     */
    protected Temp objectref;
    /** <code>Temp</code> containing the desired new value of the field. */
    protected Temp src;

    /** Creates a <code>SET</code> representing a field assignment
     *  operation.
     * @param field
     *        the description of the field to set.
     * @param objectref
     *        <code>null</code> for static fields, or a <code>Temp</code>
     *        referencing the object containing the field otherwise.
     * @param src
     *        the <code>Temp</code> containing the value to put into
     *        the field.
     */
    public SET(HCodeElement source,
	       HField field, Temp objectref, Temp src) {
	super(source);
	this.field = field;
	this.objectref = objectref;
	this.src = src;
	// VERIFY legality of SET
	Util.assert(field!=null && src!=null);
	if (isStatic())
	    Util.assert(objectref==null);
	else
	    Util.assert(objectref!=null);
    }
    /** Returns the description of the field to set. */
    public HField field() { return field; }
    /** Returns <code>null</code> if the <code>SET</code> is on a static
     *  field, or the <code>Temp</code> containing the field to set
     *  otherwise. */
    public Temp objectref() { return objectref; }
    /** Returns the <code>Temp</code> containing the desired new value 
     *  of the field. */
    public Temp src() { return src; }

    /** Returns the Temps used by this Quad. 
     * @return the <code>objectref</code> and <code>src</code> fields. */
    public Temp[] use() { 
	if (objectref==null) return new Temp[] { src };
	else return new Temp[] { objectref, src };
    }

    public int kind() { return QuadKind.SET; }

    public Quad rename(TempMap tm) {
	return new SET(this, field, map(tm,objectref), map(tm,src));
    }
    /** Rename all used variables in this Quad according to a mapping. */
    void renameUses(TempMap tm) {
	if (objectref!=null)
	    objectref = tm.tempMap(objectref);
	src = tm.tempMap(src);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    void renameDefs(TempMap tm) {
    }
    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer("SET ");
	if (isStatic()) sb.append("static ");
	sb.append(field.getDeclaringClass().getName()+"."+field.getName());
	if (objectref!=null) sb.append(" of " + objectref);
	sb.append(" to " + src.toString());
	return sb.toString();
    }
    /** Determines whether the SET is of a static field. */
    public boolean isStatic() { return field.isStatic(); }
}
