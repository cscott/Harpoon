// GET.java, created Wed Aug  5 07:05:59 1998 by cananian
package harpoon.IR.QuadSSA;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>GET</code> represent field access (get) operations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: GET.java,v 1.7 1998-08-26 22:01:39 cananian Exp $
 */

public class GET extends Quad {
    /** Temp in which to store the fetched field contents. */
    public Temp dst;
    /** The field desciption. */
    public HField field;
    /** Reference to the object containing the field. */
    public Temp objectref;
    /** Creates a <code>GET</code>. */
    public GET(String sourcefile, int linenumber,
	       Temp dst, HField field, Temp objectref) {
	super(sourcefile, linenumber);
	this.dst = dst;
	this.field = field;
	this.objectref = objectref;
    }
    GET(HCodeElement hce,
	Temp dst, HField field, Temp objectref) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, field, objectref);
    }
    /** Returns the Temp used by this Quad. 
     * @return the <code>objectref</code> field. */
    public Temp[] use() { return new Temp[] { objectref }; }
    /** Returns the Temp defined by this Quad.
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns human-readable representation. */
    public String toString() {
	return "GET " + 
	    field.getDeclaringClass().getName() + "." +
	    field.getName() + " of " + objectref + " into " + dst;
    }
    /** Determines whether the GET is of a static field. */
    public boolean isStatic() { 
	return Modifier.isStatic(field.getModifiers());
    }
}
