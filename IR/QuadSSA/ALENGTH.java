// ALENGTH.java, created Wed Aug 26 18:58:09 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;

/**
 * <code>ALENGTH</code> represents an array length query.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ALENGTH.java,v 1.1 1998-08-26 23:06:28 cananian Exp $
 * @see ANEW
 * @see AGET
 * @see ASET
 */

public class ALENGTH extends Quad {
    /** The Temp in which to store the array length. */
    public Temp dst;
    /** The array reference to query. */
    public Temp objectref;
    
    /** Creates a <code>ALENGTH</code>. */
    public ALENGTH(String sourcefile, int linenumber,
		   Temp dst, Temp objectref) {
	super(sourcefile, linenumber);
	this.dst = dst;
	this.objectref = objectref;
    }
    ALENGTH(HCodeElement hce, Temp dst, Temp objectref) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, objectref);
    }
    /** Returns the Temp defined by this Quad. 
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns the Temp used by this Quad.
     * @return the <code>objectref</code> field. */
    public Temp[] use() { return new Temp[] { objectref }; }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return "ALENGTH " + dst + ", " + objectref;
    }
}
