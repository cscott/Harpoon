// ALENGTH.java, created Wed Aug 26 18:58:09 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

/**
 * <code>ALENGTH</code> represents an array length query.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ALENGTH.java,v 1.8 1998-09-16 06:32:46 cananian Exp $
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
    public ALENGTH(HCodeElement source,
		   Temp dst, Temp objectref) {
	super(source);
	this.dst = dst;
	this.objectref = objectref;
    }

    /** Returns the Temp defined by this Quad. 
     * @return the <code>dst</code> field. */
    public Temp[] def() { return new Temp[] { dst }; }
    /** Returns the Temp used by this Quad.
     * @return the <code>objectref</code> field. */
    public Temp[] use() { return new Temp[] { objectref }; }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	objectref = tm.tempMap(objectref);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) {
	dst = tm.tempMap(dst);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return dst.toString() + " = ALENGTH " + objectref;
    }
}
