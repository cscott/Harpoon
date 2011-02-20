// FieldPointer.java, created Sat Mar 27 17:05:07 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.Util.Tuple;

/**
 * The <code>FieldPointer</code> class represents a pointer to an 
 * <code>ObjectRef</code> plus some offset.  This pointer can be dereferenced 
 * with <code>getValue()</code>, and the value at this location can be 
 * modified with <code>updateValue()</code>.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: FieldPointer.java,v 1.2 2002-02-25 21:05:50 cananian Exp $
 */
class FieldPointer extends Pointer {
    private boolean isDerived;

    // Private constructor used to add two FieldPointers.
    private FieldPointer(FieldPointer base, long offset) {
	this((ObjectRef)base.getBase(), base.getOffset() + offset, true);
    }

    // Private constructor used to clone a FieldPointer.
    private FieldPointer(ObjectRef base, long offset, boolean isDerived) {
	super(new Object[] { base, new Long(offset) });
	this.isDerived = isDerived;
    }

    /** Class constructor. */
    FieldPointer(ObjectRef base, long offset) {
	this(base, offset, false);
    }

    /** Adds the specified parameter to this <code>ArrayPointer</code>'s
     *  offset. */
    public Pointer add(long offset) {
	return new FieldPointer(this, offset);
    }

    /** Returns true if <code>obj</code> is a <code>FieldPointer</code>
     *  that points to the same location as this <code>FieldPointer</code>.
     */
    public boolean equals(Object obj) {
	FieldPointer ptr;
	if (this==obj) return true;
	if (null==obj) return false;
	try { ptr = (FieldPointer)obj; }
	catch (ClassCastException ignore) { return false; }
	return (getBase()==ptr.getBase()) &&
	    (getOffset()==ptr.getOffset());
    }
  
    /** Returns an <code>ArrayRef</code> representing the base of this 
     *  <code>ArrayPointer</code>. */
    public Object getBase()   { return (ObjectRef)proj(0); }

    /** Returns the offset of this <code>ArrayPointer</code> */
    public long   getOffset() { return ((Long)proj(1)).longValue(); }

    /** Returns the value obtained by dereferencing this 
     *  <code>FieldPointer</code>.  This value is in non-native format.
     */
    Object getValue() {
	//return Method.toNonNativeFormat(((ObjectRef)getBase()).get(this));
	return Method.toNonNativeFormat(ObjectRef.get(this));
    }

    /** Returns an integer enumeration of the kind of this Pointer.  The 
	enumerated values are public fields of the Pointer class.
    */
    public int kind() { return Pointer.FIELD_PTR; }

    /** Sets the value at the memory location specified by this 
     *  <code>FieldPointer</code> to the specified parameter.  
     *  This value should be in non-native format.
     */
    void updateValue(Object value) {
	ObjectRef objref = (ObjectRef)getBase();
	HClass type = objref.ss.getField(this).getType();
	if (Debug.DEBUG) 
	    Debug.db("Calling FieldValue.update: " + value + ", " + type);
	ObjectRef.update(this, Method.toNativeFormat(value, type));
    }

    /** Always returns false. */
    public boolean isConst()         { return false; }

    /** Returns true if this <code>ArrayPointer</code> is derived. */
    public boolean isDerived()       { return isDerived; }

    /** Returns a human-readable representation of this 
     *  <code>ArrayPointer</code>
     */
    public String toString() { 
	StringBuffer sb = new StringBuffer("FieldPtr --> < ");
	sb.append(((ObjectRef)getBase()).toString());
	sb.append(" , ");
	sb.append(getOffset());
	sb.append(" >");
	return sb.toString();
    }
}






