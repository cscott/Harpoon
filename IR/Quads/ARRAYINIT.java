// ARRAYINIT.java, created Fri Dec 11 07:06:49 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.*;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>ARRAYINIT</code> represents an array initialization operation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ARRAYINIT.java,v 1.1.2.3 1998-12-17 21:38:35 cananian Exp $
 */
public class ARRAYINIT extends Quad {
    /** The array reference to initialize. */
    protected Temp objectref;
    /** The component type. */
    protected HClass type;
    /** The array initializers. */
    protected Object[] value;

    /** Creates a <code>ARRAYINIT</code> representing an array initializer.
     *  The values in the <code>value</code> array are stored in sequential
     *  elements of the array referenced by <code>objectref</code> starting
     *  at element 0.
     * @param objectref
     *        the array to initialize.
     * @param type
     *        the component type of the array.
     * @param value
     *        the values to store in the array.
     */
    public ARRAYINIT(QuadFactory qf, HCodeElement source,
		     Temp objectref, HClass type, Object[] value) {
        super(qf, source);
	this.objectref = objectref;
	this.type = type;
	this.value = value;
	// VERIFY legality of ARRAYINIT.
	Util.assert(objectref!=null && type!=null && value!=null);
	Util.assert(type.isPrimitive() && type!=HClass.Void);
    }
    /** Returns the <code>Temp</code> referencing the array to be
     *  initialized. */
    public Temp objectref() { return objectref; }
    /** Returns the component type of the array to be initialized. */
    public HClass type() { return type; }
    /** Returns the array initializers. */
    public Object[] value() { return (Object[]) value.clone(); }

    /** Returns the <code>Temp</code> used by this <code>Quad</code>.
     * @return the <code>objectref</code> field.
     */
    public Temp[] use() { return new Temp[] { objectref }; }

    public int kind() { return QuadKind.ARRAYINIT; }

    public Quad rename(QuadFactory qqf, TempMap tm) {
	return new ARRAYINIT(qqf, this, map(tm,objectref), type, 
			     (Object[]) value.clone());
    }
    /** Rename all defined variables in this Quad according to a mapping. */
    void renameUses(TempMap tm) {
	objectref = tm.tempMap(objectref);
    }

    public void visit(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append(objectref.toString());
	sb.append("[] = ARRAYINIT (");
	sb.append(type.getName());
	sb.append(") { ");
	for (int i=0; i<value.length; i++) {
	    if (type.equals(HClass.forClass(String.class)))
		sb.append("\""+Util.escape(value.toString())+"\"");
	    else
		sb.append(value.toString());
	    if (i < value.length-1)
		sb.append(", ");
	}
	sb.append(" }");
	return sb.toString();
    }
}
