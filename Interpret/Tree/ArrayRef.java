// ArrayRef.java, created Sat Mar 27 17:05:07 1999 by duncan
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

// ArrayRef.java, created Mon Dec 28 03:02:39 1998 by cananian

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.IR.Tree.NAME;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;

/**
 * <code>ArrayRef</code> is a representation of an array reference for use
 * by the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ArrayRef.java,v 1.3.2.2 2002-03-14 01:58:07 cananian Exp $
 */
final class ArrayRef extends Ref {
    /** Elements of the array (primitives or Refs) */
    final   Object[]            elements;
    private Integer             hashCode = null;
    private Integer             length   = null;
    private ClazPointer         classPtr = null;

    /** Class constructor. */
    ArrayRef(StaticState ss, HClass type, int[] dims) 
	throws InterpretedThrowable {
	this(ss, type, dims, null, null, null);
    }

    /** Class constructor. */
    ArrayRef(StaticState ss, HClass type, int[] dims, 
	     Integer length, Integer hashCode, ClazPointer classPtr) 
	throws InterpretedThrowable {
	super(ss, type);

	assert dims.length >= 1 && dims[0]>=0;
	this.elements = new Object[dims[0]];
	this.length = length==null?new Integer(dims[0]):length; 
	this.hashCode = hashCode==null?new Integer(this.hashCode()):hashCode; 
	this.classPtr = classPtr==null?
	  new ClazPointer(ss.map.label(type), ss, 0):classPtr;
	
	if (dims.length==1)
	    for (int i=0; i<this.elements.length; i++)
		this.elements[i] = defaultValue(type.getComponentType());
	else {
	    int ndims[] = new int[dims.length-1];
	    System.arraycopy(dims, 1, ndims, 0, ndims.length);
	    for (int i=0; i<this.elements.length; i++) {
	        System.err.println("*** Warning, unexpected # of dims!");
		this.elements[i] =
		    new ArrayRef(ss, type.getComponentType(), ndims);
	    }
	}
    }

    // private constructor for use by clone() method
    private ArrayRef(StaticState ss, HClass type, final Object[] elements) {
        super(ss, type);
        this.elements = elements;
        assert elements!=null;
    }

    public Object clone() { // arrays can always be cloned.
	return new ArrayRef(ss, type, (Object[]) elements.clone());
    }


    /** Returns the length of this array.  The length field can also be
     *  accesses with an <code>ArrayPointer</code> which has a base of this 
     *  <code>ArrayRef</code>, and an offset equal to the lengthOffset
     *  of the interpreter's offset map.  */
    int length() { return length.intValue(); }

    /** Returns the i'th element of the array represented by this
     *  <code>ArrayRef</code>
     */
    Object get(int i){ 
        if (this.elements.length <= i)
	    throw new Error
		("ArrayOutOfBounds exception should have been thrown");
	else
	    return this.elements[i]; 
    }

    /** Sets the i'th element of the array represented by this 
     *  <code>ArrayRef<code> to be <code>value</code>.
     */
    void update(int i, Object value) { 
      if (this.elements.length <= i) {
	  throw new Error("Should've thrown ArrayOutOfBoundsException");
      }
      else {
	  this.elements[i] = value; 
      }
    }

    /** Provides yet another method to access the length field of this 
     *  <code>ArrayRef</code>.
     */
    Object get(HField field) { 
	if (field.getName().equals("length")) return length;
	else throw new Error("Field not found: "+ field.getName());
    }

    
    /** Returns a human-readable representation of this <code>ArrayRef</code>
     */
  /*    public String toString() {
	StringBuffer sb = new StringBuffer();
	sb.append("ArrayRef: <");
	sb.append(HClassUtil.baseClass(type));
	sb.append('['); sb.append(elements.length); sb.append(']');
	for (int i=1; i<HClassUtil.dims(type); i++)
	    sb.append("[]");
	sb.append('>');
	return sb.toString();
	} */


    /** Returns the value obtained by dereferencing the specified
     *  <code>ArrayPointer</code>.  The value returned will be 
     *  in native format.  
     */
    static Object get(ArrayPointer ptr) {
	ArrayRef ref = (ArrayRef)ptr.getBase();
	long offset = ptr.getOffset();

	if (ref.ss.map.lengthOffset(ref.type)==offset) {
	    assert ref.length!=null;
	    return ref.length;
	}
	else if (ref.ss.map.hashCodeOffset(ref.type)==offset) {
	    assert ref.hashCode!=null;
	    return ref.hashCode;
	}
	else if (ref.ss.map.clazzPtrOffset(ref.type)==offset) {
	    assert ref.classPtr!=null;
	    return ref.classPtr;
	}
	else return ref.get((int)offset);
    }
	    
    /** Updates the value pointed to by <code>ptr</code>.  The 
     *  <code>value</code> parameter must be in native format.
     */
    static void update(ArrayPointer ptr, Object value) {
	ArrayRef ref = (ArrayRef)ptr.getBase();
	long offset = ptr.getOffset();

	if (ref.ss.map.lengthOffset(ref.type)==offset) {
	    throw new Error("The length field of an array is final");
	}
	else if (ref.ss.map.hashCodeOffset(ref.type)==offset) {
	    throw new Error("The hashcode field of an array is final");
	}
	else if (ref.ss.map.clazzPtrOffset(ref.type)==offset) {
	    throw new Error("The clazpointer of an array is final");
	}
	else {
	    ref.update((int)offset, value);
	}
    }
}

