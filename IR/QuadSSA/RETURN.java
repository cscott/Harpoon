// RETURN.java, created Wed Aug  5 06:46:49 1998
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
/**
 * <code>RETURN</code> objects indicate a method return, with an
 * optional return value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: RETURN.java,v 1.8 1998-09-09 23:02:49 cananian Exp $
 */

public class RETURN extends Quad {
    /** Return value. <code>null</code> if there is no return value. */
    public Temp retval;
    /** Creates a <code>RETURN</code>. */
    public RETURN(HCodeElement source, Temp retval) {
	super(source, 1, 1 /* one successor, the footer node. */);
	this.retval = retval;
    }
    /** Creates a <code>RETURN</code> with does not return a value. */
    public RETURN(HCodeElement source) {
	this(source, null);
    }

    /** Returns all the Temps used by this Quad. */
    public Temp[] use() {
	if (retval==null) return new Temp[0];
	else return new Temp[] { retval }; 
    }
    /** Returns a human-readable representation of this Quad. */
    public String toString() {
	if (retval==null) return "RETURN";
	return "RETURN " + retval.toString();
    }
}
