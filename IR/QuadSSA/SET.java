// SET.java, created Wed Aug  5 07:04:09 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>SET</code> represents field assignment-to operations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SET.java,v 1.4 1998-08-24 19:30:03 cananian Exp $
 */

public class SET extends Quad {
    public HField dst;
    public Temp src;

    /** Creates a <code>SET</code>. */
    public SET(String sourcefile, int linenumber,
	       HField dst, Temp src) {
	super(sourcefile, linenumber);
	this.dst = dst;
	this.src = src;
    }
    SET(HCodeElement hce, HField dst, Temp src) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, src);
    }
    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	return "SET "+dst.getDeclaringClass().getName()+"."+dst.getName()+
	    " to " + src.toString();
    }
}
