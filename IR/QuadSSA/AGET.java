// AGET.java, created Wed Aug 26 19:02:57 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;

/**
 * <code>AGET</code> represents an element fetch from an array object.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: AGET.java,v 1.2 1998-09-04 06:31:21 cananian Exp $
 * @see ANEW
 * @see ASET
 * @see ALENGTH
 */

public class AGET extends Quad {
    /** The Temp in which to store the fetched element. */
    public Temp dst;
    /** The array reference. */
    public Temp objectref;
    /** The Temp holding the index of the element to get. */
    public Temp index;

    /** Creates an <code>AGET</code> object. */
    public AGET(String sourcefile, int linenumber,
		Temp dst, Temp objectref, Temp index) {
	super(sourcefile, linenumber);
	this.dst = dst;
	this.objectref = objectref;
	this.index = index;
    }
    AGET(HCodeElement hce, Temp dst, Temp objectref, Temp index) {
	this(hce.getSourceFile(), hce.getLineNumber(), dst, objectref, index);
    }
    /** Returns the Temp defined by this quad.
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns all the Temps used by this quad.
     * @return the <code>objectref</code> and <code>index</code> fields. */
    public Temp[] use() { return new Temp[] { objectref, index }; }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return dst.toString() + " = AGET " + objectref + "["+index+"]";
    }
}
