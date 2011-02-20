// Pointer.java, created Sat Mar 27 17:05:09 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
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
 * @version $Id: Pointer.java,v 1.2 2002-02-25 21:06:01 cananian Exp $
 */
abstract class Pointer extends Tuple {
    public static final int  ARRAY_PTR = 0;
    public static final int   CLAZ_PTR = 1;
    public static final int  CONST_PTR = 2;
    public static final int  FIELD_PTR = 3;
    public static final int  IFACE_PTR = 4;
    public static final int STRING_PTR = 5;
    public static final int  UNDEF_PTR = 6;
    

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

    /** Returns an integer enumeration of the kind of this Pointer.  The 
	enumerated values are public fields of the <code>Pointer</code> class.
    */
    abstract public int kind();

    /** Updates the value at the location pointed to by this 
     *  <code>Pointer</code>. */
    abstract void           updateValue(Object obj);
}





