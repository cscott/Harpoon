package harpoon.Interpret.Tree;

import harpoon.ClassFile.HClass;
import harpoon.IR.Tree.NAME;
import harpoon.Temp.Label;
import harpoon.Util.Tuple;

/**
 * The <code>UndefinedPointer</code> class represents a pointer to a 
 * value for which the type is not known.  It is an error to access 
 * the value of this pointer until it is converted to a pointer of another
 * type.
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: UndefinedPointer.java,v 1.1.2.2 1999-06-28 18:57:35 duncan Exp $
 */
public class UndefinedPointer extends Pointer {

    // Private constructor used to add two UndefinedPointers 
    private UndefinedPointer(UndefinedPointer ptr, long offset) {
	this((UndefinedRef)ptr.getBase(), ptr.getOffset() + offset);
    }

    /** Class constructor */
    public UndefinedPointer(UndefinedRef ref, long offset) {
	super(new Object[] { ref, new Long(offset) });
    }

    /** Adds the specified offset to the offset of this 
     *  <code>UndefinedPointer</code> and returns the resulting pointer. */
    public Pointer add(long offset) {
	return new UndefinedPointer(this, offset);
    }

    /** Returns an <code>UndefinedRef</code> object representing the 
     *  base of this <code>UndefinedPointer</code>. */
    public Object getBase() {
	return ((UndefinedRef)proj(0));
    }

    /** Returns the offset of this <code>UndefinedPointer</code>. */
    public long getOffset() {
	return ((Long)proj(1)).longValue();
    }
    
    /** Throws an error. */
    Object getValue() {
	throw new Error("Can't get value of an UndefinedPointer!");
    }

    /** Always returns false. */
    public boolean isConst()          { return false; }

    /** Always returns false. */
    public boolean isDerived()        { return false; }

    /** Returns an integer enumeration of the kind of this Pointer.  The 
	enumerated values are public fields of the Pointer class.
    */
    public int kind() { return Pointer.UNDEF_PTR; }

    /** Returns a human-readable representation of this 
     *  <code>UndefinedPointer</code>. */
    public String toString() { 
	StringBuffer sb = new StringBuffer("UndefPtr: < ");
	sb.append(getOffset());
	sb.append(" >");
	return sb.toString();
    }

    /** Updates the value at the location pointed to by this
     *  <code>UndefinedPointer</code> to have the specified value. */
    void updateValue(Object value) {
	UndefinedRef.update(this, value);
    }
}

