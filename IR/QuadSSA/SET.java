// SET.java, created Wed Aug  5 07:04:09 1998 by cananian
package harpoon.IR.QuadSSA;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;
/**
 * <code>SET</code> represents field assignment-to operations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SET.java,v 1.15 1998-09-16 06:32:50 cananian Exp $
 */

public class SET extends Quad {
    /** The field description. */
    public HField field;
    /** Reference to the object containing the field. <p>
     *  <code>null</code> if the field is static.     */
    public Temp objectref;
    /** Temp containing the desired new value of the field. */
    public Temp src;

    /** Creates a <code>SET</code> for a non-static field. */
    public SET(HCodeElement source,
	       HField field, Temp objectref, Temp src) {
	super(source);
	this.field = field;
	this.objectref = objectref;
	this.src = src;
	if (objectref==null) Util.assert(isStatic());
    }
    /** Creates a <code>SET</code> for a static field. */
    public SET(HCodeElement source,
	       HField field, Temp src) {
	this(source, field, null, src);
    }

    /** Returns the Temps used by this Quad. 
     * @return the <code>objectref</code> and <code>src</code> fields. */
    public Temp[] use() { 
	if (objectref==null) return new Temp[] { src };
	else return new Temp[] { objectref, src };
    }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	if (objectref!=null)
	    objectref = tm.tempMap(objectref);
	src = tm.tempMap(src);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
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
