// SET.java, created Wed Aug  5 07:04:09 1998 by cananian
package harpoon.IR.QuadSSA;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
/**
 * <code>SET</code> represents field assignment-to operations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SET.java,v 1.8 1998-09-08 14:38:39 cananian Exp $
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
    public Temp[] use() { return new Temp[] { objectref, src }; }
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
    public boolean isStatic() { 
	return Modifier.isStatic(field.getModifiers());
    }
}
