// SET.java, created Wed Aug  5 07:04:09 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>SET</code> represents field assignment-to operations.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: SET.java,v 1.2 1998-08-07 13:38:13 cananian Exp $
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
}
