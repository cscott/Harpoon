// GET.java, created Wed Aug  5 07:05:59 1998 by cananian
package harpoon.IR.QuadSSA;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
/**
 * <code>GET</code> represent field access (get) operations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: GET.java,v 1.9 1998-09-03 19:16:23 cananian Exp $
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
    public GET(String sourcefile, int linenumber,
	       Temp dst, HField field, Temp objectref) {
	super(sourcefile, linenumber);
	this.dst = dst;
	this.field = field;
	this.objectref = objectref;
	if (objectref==null) Util.assert(isStatic());
    }
    /** Creates a <code>GET</code> for a static field. */
    public GET(String sourcefile, int linenumber,
	       Temp dst, HField field) {
	this(sourcefile, linenumber, dst, field, null);
    }
    GET(HCodeElement hce, Temp dst, HField field, Temp objectref) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, field, objectref);
    }
    GET(HCodeElement hce, Temp dst, HField field) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, field);
    }

    /** Returns the Temp used by this Quad. 
     * @return the <code>objectref</code> field. */
    public Temp[] use() { return new Temp[] { objectref }; }
    /** Returns the Temp defined by this Quad.
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }
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
    public boolean isStatic() { 
	return Modifier.isStatic(field.getModifiers());
    }
}
