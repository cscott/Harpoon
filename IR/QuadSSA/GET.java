// GET.java, created Wed Aug  5 07:05:59 1998 by cananian
package harpoon.IR.QuadSSA;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>GET</code> represent field access (get) operations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: GET.java,v 1.6 1998-08-24 22:37:11 cananian Exp $
 */

public class GET extends Quad {
    public HField field;
    public Temp dst, src;
    /** Creates a <code>GET</code>. */
    public GET(String sourcefile, int linenumber,
	       Temp dst, Temp src, HField field) {
	super(sourcefile, linenumber);
	this.dst = dst;
	this.src = src;
	this.field = field;
    }
    GET(HCodeElement hce,
	Temp dst, Temp src, HField field) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, src, field);
    }
    /** Returns human-readable representation. */
    public String toString() {
	return "GET " + 
	    field.getDeclaringClass().getName() + "." +
	    field.getName() + " of " + src + " into " + dst;
    }
    /** Determines whether the GET is of a static field. */
    public boolean isStatic() { 
	return Modifier.isStatic(field.getModifiers());
    }
}
