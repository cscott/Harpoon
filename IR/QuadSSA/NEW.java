// NEW.java, created Wed Aug  5 07:08:20 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
/**
 * <code>NEW</code> represents an object creation operation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: NEW.java,v 1.2 1998-08-07 13:38:13 cananian Exp $
 */

public class NEW extends Quad {
    public HClass hclass;
    /** Creates a <code>NEW</code> object.  <code>NEW</code> creates
     *  a new instance of the class <code>hclass</code>. */
    public NEW(String sourcefile, int linenumber,
	       HClass hclass) {
        super(sourcefile, linenumber);
	this.hclass = hclass;
    }
}
