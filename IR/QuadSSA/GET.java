// GET.java, created Wed Aug  5 07:05:59 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>GET</code> represent field access (get) operations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: GET.java,v 1.3 1998-08-20 22:43:20 cananian Exp $
 */

public class GET extends Quad {
    public HField src;
    public Temp dst;
    /** Creates a <code>GET</code>. */
    public GET(String sourcefile, int linenumber,
	       Temp dst, HField src) {
	super(sourcefile, linenumber);
	this.dst = dst;
	this.src = src;
    }
    /** Returns human-readable representation. */
    public String toString() {
	return "GET " + 
	    src.getDeclaringClass().getName() + "." +
	    src.getName() + " into " + dst;
    }
}
