// SET.java, created Wed Aug  5 07:04:09 1998 by cananian
package harpoon.IR.QuadSSA;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>SET</code> represents field assignment-to operations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SET.java,v 1.5 1998-08-24 22:37:11 cananian Exp $
 */

public class SET extends Quad {
    public HField field;
    public Temp dst, src;

    /** Creates a <code>SET</code>. */
    public SET(String sourcefile, int linenumber,
	       HField field, Temp dst, Temp src) {
	super(sourcefile, linenumber);
	this.field = field;
	this.dst = dst;
	this.src = src;
    }
    SET(HCodeElement hce, HField field, Temp dst, Temp src) {
	this(hce.getSourceFile(), hce.getLineNumber(), field, dst, src);
    }
    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	return "SET "+field.getDeclaringClass().getName()+"."+field.getName()+
	    " of " + dst + " to " + src.toString();
    }
    /** Determines whether the SET is of a static field. */
    public boolean isStatic() { 
	return Modifier.isStatic(field.getModifiers());
    }
}
