// ObjectRef.java, created Sat Mar 27 17:05:09 1999 by duncan
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.NAME;
import harpoon.Util.Util;

/**
 * <code>ObjectRef</code> is an object reference in the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ObjectRef.java,v 1.4 2002-04-10 03:06:00 cananian Exp $
 */
class ObjectRef extends Ref {

    private Integer      hashCode;
    private ClazPointer  classPtr;
  
    /** Fields in this instance of the object. */
    FieldValueList fields;
    /** Native method closure. */
    Object closure;

    /** create a new objectref with default field values.
     * @exception InterpretedThrowable
     *            if class initializer throws an exception.  */
    ObjectRef(StaticState ss, HClass type) {
	this(ss, type, null, null);
    }
  
    /** create a new objectref with default field values.
     * @exception InterpretedThrowable
     *            if class initializer throws an exception.  */
    ObjectRef(StaticState ss, HClass type, 
	      Integer hashCode, ClazPointer classPtr) {
	super(ss, type);
	this.fields = null; 
	this.closure = null;
	this.hashCode = hashCode==null?new Integer(this.hashCode()):hashCode;  
	this.classPtr = classPtr==null?
	    new ClazPointer(ss.map.label(type), ss, 0):
	    classPtr;

	// Initialize our fields.
	for (HClass sc=type; sc!=null; sc=sc.getSuperclass()) {
	    HField[] fl = sc.getDeclaredFields();
	    for (int i=0; i<fl.length; i++) {
		if (!fl[i].isStatic()) { update(fl[i], defaultValue(fl[i])); }
	    }
	}
	// yay, done.
    }

    // private constructor for use by the clone() method. 
    private ObjectRef(StaticState ss, HClass type, FieldValueList fields) {
	super(ss, type);
	this.fields = fields; this.closure = null;
        // no field initialization necessary.
    }

    /** Clones this <code>ObjectRef</code> */
    public Object clone() {
	assert closure==null : "can't clone objects with closure info.";
	return new ObjectRef(ss, type, FieldValueList.clone(fields));
    }
   
    /** Calls the <code>finalize()</code> method of the specified 
     *  <code>ObjectRef</code> */
    protected void finalize() throws Throwable {
	// finalize the referenced object by evaluating its finalize method.
	try {
	    HMethod hm = type.getMethod("finalize", new HClass[0]);
	    Method.invoke(ss, hm, new Object[] { this } );
	} catch (InterpretedThrowable e) {
	    // ignore.
	} catch (NoSuchMethodError e) {
	    // no finalize method.
	}
	// finalize the ref.
	super.finalize();
    }

    /** Returns the value of the specified field of this <code>ObjectRef</code>
     */
    Object get(HField f) {
	//if (DEBUG) db("Accessing field: " + f + " in " + this);
	return FieldValueList.get(this.fields, f);
    }
 
    /** Returns the native method closure of this <code>ObjectRef</code> */
    Object getClosure() { return closure; }

    /** Sets the native method closure of this <code>ObjectRef</code> */
    void putClosure(Object cl) { closure = cl; }

    /** Updates the specified field of this <code>ObjectRef</code> to have
     *  the specified value */
    void update(HField f, Object value) {
	//if (DEBUG) db("Updating field " + f + " in " + this + " to " + value);
	this.fields = FieldValueList.update(this.fields, f, value);
    }

    /** Dereferences the specified <code>FieldPointer</code> and returns
     *  the value which it points to */
    static Object get(FieldPointer ptr) {
	ObjectRef ref = (ObjectRef)ptr.getBase();
	long offset = ptr.getOffset();

	// case 1: points to hashcode.  return field as normal. 
	if (ref.ss.map.hashCodeOffset(ref.type)==offset) {
	    assert ref.hashCode!=null;
	    return ref.hashCode;
	}
	// case 2: points to classptr.  Return ClazPointer<label, 0>
	else if (ref.ss.map.clazzPtrOffset(ref.type)==offset) {
	    assert ref.classPtr != null;
	    return ref.classPtr;
	}
	// case 3: points to finalization info.  Can this happen?
	// case 4: points to normal info.  
	else {
	    return ref.get(ref.ss.getField(ptr));
	}
    }

    /** Updates the location pointed to by <code>ptr</code> to have
     *  the specified value. */
    static void update(FieldPointer ptr, Object value) {
	ObjectRef ref = (ObjectRef)ptr.getBase();
	long offset = ptr.getOffset();

	if (ref.ss.map.hashCodeOffset(ref.type)==offset) {
	    throw new Error("Hashcode field is final!");
	}
	else if (ref.ss.map.clazzPtrOffset(ref.type)==offset) {
	    throw new Error("The ClazPointer of an object is final!");
	}
	else {
	    ref.update(ref.ss.getField(ptr), value);
	}	
    }

    /** Returns a human-readable representation of this <code>ObjectRef</code>
     */
    /*    
	  public String toString() {
	  String fString;
	  if (fields==null) fString=null;
	  else fString = fields.toString();
	
	  return "ObjectRef < " + type + ", " + fString;
	  
	  }
    */
}




