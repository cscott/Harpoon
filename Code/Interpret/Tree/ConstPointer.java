// ConstPointer.java, created Sat Mar 27 17:05:07 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.Util.Tuple;
import harpoon.Util.Util;
import harpoon.Temp.Label;

/**
 * The <code>ConstPointer</code> class is used to represent
 * pointers that can be resolved statically.  Unlike the other pointer
 * classes, <code>ConstPointer</code>s have no offsets.  Their base
 * is simply a <code>Label</code> object.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ConstPointer.java,v 1.2 2002-02-25 21:05:50 cananian Exp $
 */
class ConstPointer extends Pointer {
    private final StaticState ss;
    
    public static final ConstPointer NULL_POINTER = 
	new ConstPointer(null, null);

    /** Class constructor. */
    ConstPointer(Label label, StaticState ss) {
	super(new Object[] { label } );
	this.ss = ss;
    }
  
    /** Throws an <code>Error</code>.  <code>ConstPointer</code>s must
     *  remain constant */
    public Pointer add(long offset) {
	throw new Error("Can't add to a ConstPointer!");
    }

    /** Returns true if <code>obj</code> is a <code>ConstPointer</code> which
     *  points to the same location as this <code>ConstPointer</code>.
     */
    public boolean equals(Object obj) {
	ConstPointer ptr;
	if (this==obj) return true;
	if (null==obj) return false;
	if (this==NULL_POINTER || obj==NULL_POINTER) return false;
	try { ptr = (ConstPointer)obj; }
	catch (ClassCastException e) { return false; }
	return ((Label)getBase()).toString().
	    equals(((Label)ptr.getBase()).toString());
    }

    /** Returns a <code>Label</code> representing the base of this 
     *  <code>ConstPointer</code>.
     */
    public Object getBase() { return (Label)proj(0); }

    /** Throws an <code>Error</code>.  Since <code>ConstPointer</code>s
     *  have no offsets, it is incorrect to try to access their offsets.
     */
    public long getOffset() { 
	throw new Error("ConstPointers have no offsets");
    }

    /** Returns the value obtained by dereferencing this 
     *  <code>ConstPointer</code>.
     */
    public Object getValue() { 
	return (this==NULL_POINTER) ?
	    Method.toNonNativeFormat(null) :
	    Method.toNonNativeFormat(ss.getValue(this));
    }

    /** Always returns true. */
    public boolean isConst()         { return true; }

    /** Always returns false. */
    public boolean isDerived()       { return false; }

    /** Returns an integer enumeration of the kind of this Pointer.  The 
	enumerated values are public fields of the Pointer class.
    */
    public int kind() { return Pointer.CONST_PTR; }
   
    /** If this <code>ConstPointer</code> points to a static field, then
     *  returns the type of this static field.  Otherwise throws an 
     *  <code>Error</code>.
     */
    public HClass getType() {
        return ss.getField(this).getType();
    }

    /** If this <code>ConstPointer</code> points to a static field, then
     *  updates the value of this static field.  Otherwise throws an
     *  <code>Error</code>.
     */
    public void updateValue(Object value) {
	ss.updateFieldValue(this, Method.toNativeFormat(value, getType()));
    }

    /** Returns a human-readable representation of this 
     *  <code>ConstPointer</code>.
     */
    public String toString() { 
	StringBuffer sb = new StringBuffer("ConstPtr: < ");
	if (this==NULL_POINTER) 
	    sb.append("null");
	else
	    sb.append(getBase().toString());
	sb.append(" >");
	return sb.toString();
    }

}



