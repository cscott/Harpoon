package harpoon.Interpret.Tree;

import harpoon.Util.Tuple;

/**
 * The <code>Pointer</code> class is used to represent
 * all pointers in the Tree interpreter.  Pointers in the Tree interpreter
 * are represented by a base, plus some optional offset.  The base can 
 * be a <code>Label</code>, an <code>ArrayRef</code>, or an 
 * <code>ObjectRef</code>. 
 *
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: Pointer.java,v 1.1.2.1 1999-03-27 22:05:09 duncan Exp $
 */
abstract class Pointer extends Tuple {

    protected Pointer(Object[] tuple) {
	super(tuple);
    }

    /** Adds the specified offset to the offset of this <code>Pointer</code>,
     *  and returns the new <code>Pointer</code>. */
    abstract public Pointer add(long offset);

    /** Returns the base of this <code>Pointer</code>. */
    abstract public Object  getBase();

    /** Returns the offset of this <code>Pointer</code>. */
    abstract public long    getOffset();

    /** Dereferences this <code>Pointer</code> and returns the resulting value.
     */
    abstract Object         getValue();

    /** Returns true if this <code>Pointer</code> is a pointer constant. */
    abstract public boolean isConst();

    /** Returns true if this <code>Pointer</code> is a derived pointer. */
    abstract public boolean isDerived();

    /** Updates the value at the location pointed to by this 
     *  <code>Pointer</code>. */
    abstract void           updateValue(Object obj);
}





