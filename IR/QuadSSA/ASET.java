// ASET.java, created Wed Aug 26 19:12:32 1998 by cananian
package harpoon.IR.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;

/**
 * <code>ASET</code> represents an array element assignment.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ASET.java,v 1.8 1998-09-16 06:32:46 cananian Exp $
 * @see ANEW
 * @see AGET
 * @see ALENGTH
 */

public class ASET extends Quad {
    /** The array reference */
    public Temp objectref;
    /** The Temp holding the index of the element to get. */
    public Temp index;
    /** The new value for the array element. */
    public Temp src;

    /** Creates an <code>ASET</code> object. */
    public ASET(HCodeElement source,
		Temp objectref, Temp index, Temp src) {
	super(source);
	this.objectref = objectref;
	this.index = index;
	this.src = src;
    }

    /** Returns all the Temps used by this quad. 
     * @return the <code>objectref</code>, <code>index</code>, and 
     *         <code>src</code> fields.
     */
    public Temp[] use() { return new Temp[] { objectref, index, src }; }

    /** Rename all used variables in this Quad according to a mapping. */
    public void renameUses(TempMap tm) {
	objectref = tm.tempMap(objectref);
	index = tm.tempMap(index);
	src = tm.tempMap(src);
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    public void renameDefs(TempMap tm) { }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this quad. */
    public String toString() {
	return "ASET " + objectref + "["+index+"] = " + src;
    }
}
