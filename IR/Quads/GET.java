// GET.java, created Wed Aug  5 07:05:59 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>GET</code> represent field access (get) operations.
 * The <code>objectref</code> is null if the field is static.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: GET.java,v 1.1.2.1 1998-12-01 12:36:42 cananian Exp $
 */

public class GET extends Quad {
    /** Temp in which to store the fetched field contents. */
    public Temp dst;
    /** The field desciption. */
    public HField field;
    /** Reference to the object containing the field. <p>
     *  <code>null</code> if field is static.     */
    public Temp objectref;

    /** Creates a <code>GET</code> for a non-static field. */
    public GET(HCodeElement source,
	       Temp dst, HField field, Temp objectref) {
	super(source);
	this.dst = dst;
	this.field = field;
	this.objectref = objectref;
	if (objectref==null) Util.assert(isStatic());
    }
    /** Creates a <code>GET</code> for a static field. */
    public GET(HCodeElement source,
	       Temp dst, HField field) {
	this(source, dst, field, null);
    }

    /** Returns the Temp used by this Quad. 
     * @return the <code>objectref</code> field. */
    public Temp[] use() { 
	if (objectref==null) return new Temp[0];
	else return new Temp[] { objectref }; 
    }
    /** Returns the Temp defined by this Quad.
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	if (objectref!=null)
	    objectref = tm.tempMap(objectref);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation. */
    public String toString() {
	StringBuffer sb = new StringBuffer(dst.toString()+" = GET ");
	if (isStatic())
	    sb.append("static ");
	sb.append(field.getDeclaringClass().getName() + "." + field.getName());
	if (objectref!=null)
	    sb.append(" of " + objectref);
	return sb.toString();
    }
    /** Determines whether the GET is of a static field. */
    public boolean isStatic() { return field.isStatic(); }
}
