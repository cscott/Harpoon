// ClazPointer.java, created Sat Mar 27 17:05:07 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.Util.Tuple;
import harpoon.Temp.Label;

/**
 * The <code>ClazPointer</code> is used to representing a pointer
 * to static class data.  
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ClazPointer.java,v 1.2 2002-02-25 21:05:50 cananian Exp $
 */
class ClazPointer extends Pointer {
    private boolean isDerived;
    private /*final*/ StaticState ss;

    // Private constructor used to add two ClazPointers.
    private ClazPointer(ClazPointer base, long offset) {
	this((Label)base.getBase(), base.ss, base.getOffset() + offset, true);
    }

    // Private constructor used to clone a ClazPointer.
    private ClazPointer(Label base, final StaticState ss, 
			long offset, boolean isDerived) {
	super(new Object[] { base, new Long(offset) });
	this.ss = ss;
	this.isDerived = isDerived;
    }

    /** Class constructor */
    ClazPointer(Label base, final StaticState ss, long offset) {
	this(base, ss, offset, false);
    }
    
    /** Adds the specified parameter to this <code>ClazPointer</code>'s
     *  offset */
    public Pointer add(long offset) {
	return new ClazPointer(this, offset);
    }

    /** Returns true if <code>obj</code> is a <code>ClazPointer</code> which
     *  points to the same location as this <code>ClazPointer</code>.
     */
    public boolean equals(Object obj) {
	ClazPointer ptr;
	if (this==obj) return true;
	if (null==obj) return false;
	try { ptr = (ClazPointer)obj; }
	catch (ClassCastException ignore) { return false; }
	return (((Label)getBase()).toString().equals
		(((Label)ptr.getBase()).toString())) &&
	    getOffset()==ptr.getOffset();
    }

    /** Returns a <code>Label</code> representing the base of this 
     *  <code>ConstPointer</code>.
     */
    public Object getBase()   { return (Label)proj(0); }

    /** Returns the offset of this <code>ClazPointer</code>. */
    public long getOffset() { return ((Long)proj(1)).longValue(); }

    /** Dereferences this <code>ClazPointer</code> and returns the value
     *  it points to. */ 
    public Object getValue() { return ss.getValue(this); }

    /** Always returns false. */
    public boolean isConst()         { return false; }

    /** Returns true if this <code>ClazPointer</code> has been derived from 
     *  another <code>ClazPointer</code> */
    public boolean isDerived()       { return isDerived; }

    /** Returns an integer enumeration of the kind of this Pointer.  The 
	enumerated values are public fields of the Pointer class.
    */
    public int kind() { return Pointer.CLAZ_PTR; }

    /** Throws an error, as the program is not supposed to
     *  modify static class data */
    public void updateValue(Object obj) { 
	throw new Error("Can't modify claz data!");
    }

    /** Returns a human-readable representation of this 
     *  <code>ClazPointer</code> */
    public String toString() { 
	StringBuffer sb = new StringBuffer("ClazPtr: < ");
	sb.append(getBase().toString());
	sb.append(" , ");
	sb.append(getOffset());
	sb.append(" >");
	return sb.toString();
    }
}





