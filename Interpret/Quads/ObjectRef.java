// ObjectRef.java, created Mon Dec 28 00:29:30 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

/**
 * <code>ObjectRef</code> is an object reference in the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ObjectRef.java,v 1.3.2.1 2002-02-27 08:36:53 cananian Exp $
 */
class ObjectRef extends Ref implements java.io.Serializable {
    /** Fields in this instance of the object. */
    FieldValueList fields;
    /** Native method closure. */
    Object closure; // must be serializable!

    /** create a new objectref with default field values.
     * @exception InterpretedThrowable
     *            if class initializer throws an exception.  */
    ObjectRef(StaticState ss, HClass type) {
	super(ss, type);
	this.fields = null; this.closure = null;
	// Initialize our fields.
	for (HClass sc=type; sc!=null; sc=sc.getSuperclass()) {
	    HField[] fl = sc.getDeclaredFields();
	    for (int i=0; i<fl.length; i++)
		if (!fl[i].isStatic())
		    update(fl[i], defaultValue(fl[i]));
	}
	// yay, done.
    }
    /** private constructor for use by the clone() method. */
    private ObjectRef(StaticState ss, HClass type, FieldValueList fields) {
	super(ss, type);
	this.fields = fields; this.closure = null;
        // no field initialization necessary.
    }
       
    Object get(HField f) {
	return FieldValueList.get(this.fields, f);
    }
    void update(HField f, Object value) {
	this.fields = FieldValueList.update(this.fields, f, value);
    }

    Object getClosure() { return closure; }
    void putClosure(Object cl) { closure = cl; }

    public Object clone() {
       assert closure==null : "can't clone objects with closure info.";
       return new ObjectRef(ss, type, FieldValueList.clone(fields));
    }
   
    /** for profiling. */
    protected int size() { // approx. object size, in bytes. (not exact!)
	HField[] hf = type.getFields();
	int size = 8; // two header words
	for (int i=0; i<hf.length; i++)
	    if (!hf[i].isStatic()) // skip static fields
		size += (hf[i].getType()==HClass.Long ||
			 hf[i].getType()==HClass.Double) ? 8 : 4;
	return size;
    }

    /** For debugging (invokes the interpreted toString() method) */
    public String toString() {
	try {
	    HMethod hm = type.getMethod("toString", new HClass[0]);
	    ObjectRef istr =
		(ObjectRef) Method.invoke(ss, hm, new Object[] { this } );
	    return ss.ref2str(istr);
	} catch (InterpretedThrowable e) {
	    return super.toString(); // nasty ObjectRef@...
	}
    }

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
}
