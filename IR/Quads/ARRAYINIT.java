// ARRAYINIT.java, created Fri Dec 11 07:06:49 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.Util;

/**
 * <code>ARRAYINIT</code> represents an array initialization operation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ARRAYINIT.java,v 1.4 2002-04-10 03:05:14 cananian Exp $
 */
public class ARRAYINIT extends Quad {
    /** The array reference to initialize. */
    protected Temp objectref;
    /** The component type. */
    protected HClass type;
    /** The starting index for the initializers. */
    protected int offset;
    /** The array initializers. Elements must be instances of the type
     *  wrapper. */
    protected Object[] value;

    /** Creates a <code>ARRAYINIT</code> representing an array initializer.
     *  The values in the <code>value</code> array are stored in sequential
     *  elements of the array referenced by <code>objectref</code> starting
     *  at element 0.
     * @param objectref
     *        the array to initialize.
     * @param offset
     *        the starting index for the initializers.
     * @param type
     *        the component type of the array.
     * @param value
     *        the values to store in the array.
     */
    public ARRAYINIT(QuadFactory qf, HCodeElement source,
		     Temp objectref, int offset, HClass type, Object[] value) {
        super(qf, source);
	this.objectref = objectref;
	this.type = type;
	this.offset = offset;
	this.value = value;
	// VERIFY legality of ARRAYINIT.
	assert objectref!=null && type!=null && value!=null;
	assert type.isPrimitive() && type!=HClass.Void;
	/*assert offset>=0; // legal, it just throws an exception. */
    }
    /** Returns the <code>Temp</code> referencing the array to be
     *  initialized. */
    public Temp objectref() { return objectref; }
    /** Returns the component type of the array to be initialized. */
    public HClass type() { return type; }
    /** Returns the starting offset of the initializers. */
    public int offset() { return offset; }
    /** Returns the array initializers. */
    public Object[] value() { return (Object[]) value.clone(); }

    /** Returns the <code>Temp</code> used by this <code>Quad</code>.
     * @return the <code>objectref</code> field.
     */
    public Temp[] use() { return new Temp[] { objectref }; }

    public int kind() { return QuadKind.ARRAYINIT; }

    public Quad rename(QuadFactory qqf, TempMap defMap, TempMap useMap) {
	return new ARRAYINIT(qqf, this, map(useMap,objectref), offset, type, 
			     (Object[]) value.clone());
    }
    /** Rename all defined variables in this Quad according to a mapping.
     * @deprecated does not preserve immutability. */
    void renameUses(TempMap tm) {
	objectref = tm.tempMap(objectref);
    }

    public void accept(QuadVisitor v) { v.visit(this); }

    /** Returns human-readable representation of this quad. */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append(objectref.toString());
	sb.append("["+offset+"-"+(offset+value.length-1)+"]");
	sb.append(" = ARRAYINIT (");
	sb.append(type.getName());
	sb.append(") { ");
	for (int i=0; i<value.length; i++) {
	    if (type.getName().equals("java.lang.String"))
		sb.append("\""+Util.escape(value[i].toString())+"\"");
	    else if (type.equals(HClass.Char))
		sb.append("\'"+Util.escape(value[i].toString())+"\'");
	    else
		sb.append(value[i].toString());
	    if (i < value.length-1)
		sb.append(", ");
	}
	sb.append(" }");
	return sb.toString();
    }
}
