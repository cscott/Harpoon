// ArrayRef.java, created Mon Dec 28 03:02:39 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;

/**
 * <code>ArrayRef</code> is a representation of an array reference for use
 * by the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayRef.java,v 1.1.2.7 1999-08-07 11:20:19 cananian Exp $
 */
final class ArrayRef extends Ref implements java.io.Serializable {
    /** Elements of the array (primitives or Refs) */
    final Object[] elements;

    ArrayRef(StaticState ss, HClass type, int dims[]) 
	throws InterpretedThrowable {
	super(ss, type);
	Util.assert(dims.length >= 1 && dims[0]>=0);
	this.elements = new Object[dims[0]];
	if (dims.length==1)
	    for (int i=0; i<this.elements.length; i++)
		this.elements[i] = defaultValue(type.getComponentType());
	else {
	    int ndims[] = new int[dims.length-1];
	    System.arraycopy(dims, 1, ndims, 0, ndims.length);
	    for (int i=0; i<this.elements.length; i++)
		this.elements[i] =
		    new ArrayRef(ss, type.getComponentType(), ndims);
	}
    }
    // private constructor for use by clone() method
    private ArrayRef(StaticState ss, HClass type, Object[] elements) {
        super(ss, type);
        this.elements = elements;
        Util.assert(elements!=null);
    }
    public Object clone() { // arrays can always be cloned.
       return new ArrayRef(ss, type, (Object[]) elements.clone());
    }
    /** Arrays have phantom 'length' field. (public final int) */
    Object get(HField f) {
	if (f.getName().equals("length")) return new Integer(length());
	else throw new Error("Field not found: "+f);
    }

    int length() { return this.elements.length; }
    Object get(int i) { return this.elements[i]; }
    void update(int i, Object value) { this.elements[i] = value; }

    /** For debugging. */
    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append('<');
	sb.append(HClassUtil.baseClass(type));
	sb.append('['); sb.append(elements.length); sb.append(']');
	for (int i=1; i<HClassUtil.dims(type); i++)
	    sb.append("[]");
	sb.append('>');
	return sb.toString();
    }
}
