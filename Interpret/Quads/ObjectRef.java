// ObjectRef.java, created Mon Dec 28 00:29:30 1998 by cananian
package harpoon.Interpret.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;

/**
 * <code>ObjectRef</code> is an object reference in the interpreter.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ObjectRef.java,v 1.1.2.6 1999-02-07 21:45:53 cananian Exp $
 */
class ObjectRef extends Ref {
    /** Fields in this instance of the object. */
    FieldValueList fields;
    /** Native method closure. */
    Object closure;

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
       Util.assert(closure==null, "can't clone objects with closure info.");
       return new ObjectRef(ss, type, FieldValueList.clone(fields));
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
