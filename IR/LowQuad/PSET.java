// PSET.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>PSET</code> represents a <code>POINTER</code> dereference to
 * set a field or array element.  The <code>ptr</code> <code>Temp</code>
 * ought to contain a <code>POINTER</code> value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PSET.java,v 1.1.2.2 1999-09-09 21:43:00 cananian Exp $
 */
public class PSET extends LowQuad {
    /** <code>Temp</code> holding the <code>POINTER</code> value to
     * dereference and fetch. */
    protected final Temp ptr;
    /** <code>Temp</code> containing the desired new value of the
     *  field or element. */
    protected final Temp src;
    
    /** Creates a <code>PSET</code> representing a pointer dereference and
     *  store.
     * @param ptr
     *        the <code>Temp</code> holding the <code>POINTER</code> value
     *        to dereference.
     * @param src
     *        the <code>Temp</code> containing the value to put into the
     *        field or element.
     */
    public PSET(LowQuadFactory qf, HCodeElement source, Temp ptr, Temp src) {
	super(qf, source);
	this.ptr = ptr;
	this.src = src;
	Util.assert(ptr!=null && src!=null);
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> holding the <code>POINTER</code> value
     *  to dereference. */
    public Temp ptr() { return ptr; }
    /** Returns the <code>Temp</code> holding the desired new value for
     *  the dereference field or element. */
    public Temp src() { return src; }

    public int kind() { return LowQuadKind.PSET; }

    public Temp[] use() { return new Temp[] { ptr, src }; }
    public Temp[] def() { return new Temp[0]; }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PSET((LowQuadFactory)qf, this,
			map(useMap, ptr), map(useMap, src));
    }

    void accept(LowQuadVisitor v) { v.visit(this); }

    public String toString() {
	return "PSET *" + ptr.toString() + " to " + src.toString();
    }
}
