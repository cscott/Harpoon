// TYPECAST.java, created Thu Dec 31 11:12:40 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>TYPECAST</code> performs a typecast on a given variable.
 * It throws an exception if the <code>Temp</code> cannot be type cast
 * as requested.  It is similar to the bytecode <code>checkcast</code>
 * instruction.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: TYPECAST.java,v 1.3 2002-02-26 22:45:57 cananian Exp $
 */
public class TYPECAST extends NOP {
    /** The <code>Temp</code> to type cast. */
    protected Temp objectref;
    /** The class to cast to. */
    final protected HClass hclass;

    /** Creates a <code>TYPECAST</code> representing a type cast.
     * @param objectref
     *        the <code>Temp</code> to type cast.
     * @param hclass
     *        the class to cast to.
     */
    public TYPECAST(QuadFactory qf, HCodeElement source,
		    Temp objectref, HClass hclass) {
        super(qf, source);
	this.objectref = objectref;
	this.hclass = hclass;
	// VERIFY LEGALITY
	Util.ASSERT(objectref!=null && hclass!=null);
    }
    // ACCESSOR METHODS:
    /** Return the <code>Temp</code> type cast by this <code>TYPECAST</code>.*/
    public Temp objectref() { return objectref; }
    /** Return the class of this <code>TYPECAST</code>. */
    public HClass hclass() { return hclass; }

    /** Returns the <code>Temp</code>s used by this quad. */
    public Temp[] use() { return new Temp[] { objectref }; }

    public int kind() { return QuadKind.TYPECAST; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new TYPECAST(qqf, this, map(useMap,objectref), hclass);
    }

    /** Rename all used variables in this <code>Quad</code> according to
     *  a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	objectref = tm.tempMap(objectref);
    }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns a human-readable representation of this <code>Quad</code>. */
    public String toString() {
	return "TYPECAST ("+hclass.getName()+") "+objectref.toString();
    }
}
