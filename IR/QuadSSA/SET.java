// SET.java, created Wed Aug  5 07:04:09 1998 by cananian
package harpoon.IR.QuadSSA;

import java.lang.reflect.Modifier;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>SET</code> represents field assignment-to operations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SET.java,v 1.6 1998-08-26 22:01:40 cananian Exp $
 */

public class SET extends Quad {
    /** The field description. */
    public HField field;
    /** Reference to the object containing the field. */
    public Temp objectref;
    /** Temp containing the desired new value of the field. */
    public Temp src;

    /** Creates a <code>SET</code>. */
    public SET(String sourcefile, int linenumber,
	       HField field, Temp objectref, Temp src) {
	super(sourcefile, linenumber);
	this.field = field;
	this.objectref = objectref;
	this.src = src;
    }
    SET(HCodeElement hce, HField field, Temp objectref, Temp src) {
	this(hce.getSourceFile(), hce.getLineNumber(), field, objectref, src);
    }
    /** Returns the Temps used by this Quad. 
     * @return the <code>objectref</code> and <code>src</code> fields. */
    public Temp[] use() { return new Temp[] { objectref, src }; }
    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	return "SET "+field.getDeclaringClass().getName()+"."+field.getName()+
	    " of " + objectref + " to " + src.toString();
    }
    /** Determines whether the SET is of a static field. */
    public boolean isStatic() { 
	return Modifier.isStatic(field.getModifiers());
    }
}
