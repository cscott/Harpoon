// GET.java, created Wed Aug  5 07:05:59 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HField;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>GET</code> represent field access (get) operations.
 * The <code>objectref</code> is null if the field is static.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: GET.java,v 1.4 2002-04-10 03:05:14 cananian Exp $
 */
public class GET extends Quad {
    /** <code>Temp</code> in which to store the fetched field contents. */
    protected Temp dst;
    /** The field description. */
    final protected HField field;
    /** Reference to the object containing the field. <p>
     *  <code>null</code> if field is static.     */
    protected Temp objectref;

    /** Creates a <code>GET</code> representing a field access.
     * @param dst
     *        the <code>Temp</code> in which to store the fetched field.
     * @param field
     *        the field description.
     * @param objectref
     *        the <code>Temp</code> referencing the object
     *        containing the specified field, if the field is not static.
     *        For static fields, <code>objectref</code> is <code>null</code>.
     */
    public GET(QuadFactory qf, HCodeElement source,
	       Temp dst, HField field, Temp objectref) {
	super(qf, source);
	this.dst = dst;
	this.field = field;
	this.objectref = objectref;
	// VERIFY legality of GET
	assert dst!=null && field!=null;
	if (isStatic())
	    assert objectref==null;
	else
	    assert objectref!=null;
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> in which to store the fetched field. */
    public Temp dst() { return dst; }
    /** Returns the field to fetch. */
    public HField field() { return field; }
    /** Returns the object containing the specified field, or 
     *  <code>null</code> if the field is static. */
    public Temp objectref() { return objectref; }

    /** Returns the Temp used by this Quad. 
     * @return the <code>objectref</code> field. */
    public Temp[] use() { 
	if (objectref==null) return new Temp[0];
	else return new Temp[] { objectref }; 
    }
    /** Returns the Temp defined by this Quad.
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }

    public int kind() { return QuadKind.GET; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new GET(qqf, this,
		       map(defMap,dst), field, map(useMap,objectref));
    }
    /** Rename all used variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	if (objectref!=null)
	    objectref = tm.tempMap(objectref);
    }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void accept(QuadVisitor v) { v.visit(this); }

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
