// InterfaceListPointer.java, created Sat Mar 27 17:05:08 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.Util.Tuple;

/**
 * The <code>InterfaceListPointer</code> is used to representing a pointer
 * to a list of interfaces.  The pointers of this type resides inside
 * the blocks of class data allocated by the class loader.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: InterfaceListPointer.java,v 1.2 2002-02-25 21:05:57 cananian Exp $
 */
public class InterfaceListPointer extends Pointer {
  
    // Private constructor used to add two interface list pointers
    private InterfaceListPointer(InterfaceListPointer ptr, long offset) {
	this((InterfaceList)ptr.getBase(), offset + ptr.getOffset());
    }

    /** Class constructor. */
    InterfaceListPointer(InterfaceList list, long offset) {
	super(new Object[] { list, new Long(offset) });
    }

    /** Adds the specified parameter to this <code>ClazPointer</code>'s
     *  offset */
    public Pointer add(long offset) { 
	return new InterfaceListPointer(this, offset);
    }

    /** Returns true if <code>obj</code> is an 
     *  <code>InterfaceListPointer</code> which points to the same location 
     *  as this <code>InterfaceListPointer</code>.
     */
    public boolean equals(Object obj) {
	InterfaceListPointer ptr;
	if (this==obj) return true;
	if (null==obj) return false;
	try { ptr = (InterfaceListPointer)obj; }
	catch (ClassCastException ignore) { return false; }
	return ptr.getBase()==getBase() &&
	    ptr.getOffset()==getOffset();
    }

    /** Returns an <code>InterfaceList</code> representing the base of this 
     *  <code>InterfaceListPointer</code>.
     */
    public Object getBase() {
	return proj(0);
    }

    /** Returns the offset of this <code>ClazPointer</code>. */
    public long getOffset() { 
	return ((Long)proj(1)).longValue();
    }

    /** Dereferences this <code>InterfaceListPointer</code> and returns the 
     *  value it points to. */ 
    public Object getValue() { 
	return ((InterfaceList)getBase()).getInterface((int)getOffset());
    }

    /** Always returns false. */
    public boolean isConst()   { return false; }

    /** Always returns false. */
    public boolean isDerived() { return false; }

    /** Returns an integer enumeration of the kind of this Pointer.  The 
	enumerated values are public fields of the Pointer class.
    */
    public int kind() { return Pointer.IFACE_PTR; }

    /** Throws an error, as the program is not supposed to
     *  modify static class data */
    public void updateValue(Object obj) { 
	throw new Error("Can't update the value of an InterfaceListPointer!");
    }

    /** Returns a human-readable representation of this
     *  <code>InterfaceListPointer</code>.
     */
    public String toString() {
	StringBuffer sb = new StringBuffer("InterfaceListPtr <");
	sb.append(getOffset());
	sb.append(">");
	return sb.toString();
    }
}
    

