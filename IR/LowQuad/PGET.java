// PGET.java, created Wed Jan 20 21:47:52 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.LowQuad;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>PGET</code> represents a <code>POINTER</code> dereference to
 * get a field or array element.  The <code>ptr</code> <code>Temp</code>
 * ought to contain a <code>POINTER</code> value.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: PGET.java,v 1.2 2002-02-25 21:04:40 cananian Exp $
 */
public class PGET extends LowQuad {
    /** <code>Temp</code> in which to store the fetched field or array
     *  element. */
    protected final Temp dst;
    /** <code>Temp</code> holding the <code>POINTER</code> value to
     *  dereference and fetch. */
    protected final Temp ptr;
    /** The type of the object we are fetching.  This may be
     *  a sub-integer type, and thus disagree with the type of
     *  <code>dst</code>.  For non-primitive types, this may be
     *  simply <code>Object</code>; use a typemap and the <code>dst</code>
     *  field if you need accurate non-primitive types. */
    protected final HClass type;
    
    /** Creates a <code>PGET</code> representing a pointer dereference and
     *  fetch.
     * @param dst
     *        the <code>Temp</code> in which to store the fetched field or
     *        element.
     * @param ptr
     *        the <code>Temp</code> holding the <code>POINTER</code> value
     *        to dereference.
     * @param type
     *        the type of the object we are fetching; possibly a sub-integer
     *        type.  Not necessarily precise if non-primitive.
     */
    public PGET(LowQuadFactory qf, HCodeElement source,
		Temp dst, Temp ptr, HClass type) {
	super(qf, source);
	this.dst = dst;
	this.ptr = ptr;
	this.type= type;
	Util.assert(dst!=null && ptr!=null && type!=null);
    }
    // ACCESSOR METHODS:
    /** Returns the <code>Temp</code> in which to store the fetched field or
     *  array element. */
    public Temp dst() { return dst; }
    /** Returns the <code>Temp</code> holding the <code>POINTER</code> value
     *  to dereference and fetch. */
    public Temp ptr() { return ptr; }
    /** Returns the type of the field or array element we are fetching.
     *  Not necessarily precise if non-primitive.  May be a sub-integer
     *  type. */
    public HClass type() { return type; }

    public int kind() { return LowQuadKind.PGET; }

    public Temp[] use() { return new Temp[] { ptr }; }
    public Temp[] def() { return new Temp[] { dst }; }

    public harpoon.IR.Quads.Quad rename(harpoon.IR.Quads.QuadFactory qf,
					TempMap defMap, TempMap useMap) {
	return new PGET((LowQuadFactory)qf, this,
			map(defMap, dst), map(useMap, ptr), type);
    }

    void accept(LowQuadVisitor v) { v.visit(this); }

    public String toString() {
	String r = dst.toString() + " = PGET *" + ptr.toString();
	if (type.isPrimitive()) r+=" ("+type.toString()+")";
	return r;
    }
}
