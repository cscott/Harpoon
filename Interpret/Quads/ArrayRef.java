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
 * @version $Id: ArrayRef.java,v 1.4 2002-04-10 03:05:50 cananian Exp $
 */
final class ArrayRef extends Ref implements java.io.Serializable {
    /** Elements of the array (primitives or Refs) */
    final Object[] elements;

    ArrayRef(StaticState ss, HClass type, int dims[]) 
	throws InterpretedThrowable {
	super(ss, type);
	assert dims.length >= 1 && dims[0]>=0;
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
        assert elements!=null;
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

    /** for profiling. */
    protected int size() { // approx. array size, in bytes.
	HClass ct = type.getComponentType();
	int elsize;
	if (!ct.isPrimitive()) elsize = 4; // for 32-bit archs, at least.
	else if (ct==HClass.Boolean || ct==HClass.Byte) elsize = 1;
	else if (ct==HClass.Char || ct==HClass.Short) elsize = 2;
	else if (ct==HClass.Int || ct==HClass.Float) elsize = 4;
	else if (ct==HClass.Double || ct==HClass.Long) elsize = 8;
	else throw new Error("ugh: what kind of primitive type is "+ct+"?");
	return 8 + elsize*length();
    }

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
