// PSET.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HClass;
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
 * @version $Id: PSET.java,v 1.3 2002-02-26 22:45:51 cananian Exp $
 */
public class PSET extends LowQuad {
    /** <code>Temp</code> holding the <code>POINTER</code> value to
     * dereference and store to. */
    protected final Temp ptr;
    /** <code>Temp</code> containing the desired new value of the
     *  field or element. */
    protected final Temp src;
    /** The type of the object we are storing.  This may be
     *  a sub-integer type, and thus disagree with the type of
     *  <code>src</code>.  For non-primitive types, this may be
     *  simply <code>Object</code>; use a typemap and the <code>src</code>
     *  field if you need accurate non-primitive types. */
    protected final HClass type;
    
    /** Creates a <code>PSET</code> representing a pointer dereference and
     *  store.
     * @param ptr
     *        the <code>Temp</code> holding the <code>POINTER</code> value
     *        to dereference.
     * @param src
     *        the <code>Temp</code> containing the value to put into the
     *        field or element.
     * @param type
     *        the type of the object we are storing; possibly a sub-integer
     *        type.  Not necessarily precise if non-primitive.
     */
    public PSET(LowQuadFactory qf, HCodeElement source,
		Temp ptr, Temp src, HClass type) {
	super(qf, source);
	this.ptr = ptr;
	this.src = src;
	this.type= type;
	Util.ASSERT(ptr!=null && src!=null && type!=null);
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> holding the <code>POINTER</code> value
     *  to dereference. */
    public Temp ptr() { return ptr; }
    /** Returns the <code>Temp</code> holding the desired new value for
     *  the dereference field or element. */
    public Temp src() { return src; }
    /** Returns the type of the field or array element we are storing.
     *  Not necessarily precise if non-primitive.  May be a sub-integer
     *  type. */
    public HClass type() { return type; }

    public int kind() { return LowQuadKind.PSET; }

    public Temp[] use() { return new Temp[] { ptr, src }; }
    public Temp[] def() { return new Temp[0]; }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PSET((LowQuadFactory)qf, this,
			map(useMap, ptr), map(useMap, src), type);
    }

    void accept(LowQuadVisitor v) { v.visit(this); }

    public String toString() {
	String r = "PSET *" + ptr.toString() + " to " + src.toString();
	if (type.isPrimitive()) r+=" ("+type.toString()+")";
	return r;
    }
}
