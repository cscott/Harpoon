// OffsetMap.java, created Thu Jan 14 22:17:20 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Interpret.Tree;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HClass;
import harpoon.Temp.Label;

import java.util.Set;

/**
 * An <code>OffsetMap</code> maps an <code>HField</code> or an 
 * <code>HMethod</code> to an offset in bytes.  It also reports the
 * total size of an <code>HClass</code> object.
 * 
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: OffsetMap.java,v 1.2 2002-02-25 21:06:01 cananian Exp $
 */
abstract class OffsetMap { // use an abstract class, if we can.

    /** Maps an <code>HClass</code> to an offset (in bytes).  
     *  Returns the offset from an object reference of the class pointer */
    public abstract int clazzPtrOffset(HClass hc);

    /** If returns the offset from the clazz pointer of the component type
     *  of <code>hc</code>.  This location in memory will contain 
     *  <code>null</code> if <code>hc</code> is not an array.  If 
     *  <code>hc</code> is an array, contains a pointer to the 
     *  component type of the array. */
    public abstract int componentTypeOffset(HClass hc);

    /** Returns the size (in bytes) of the display information.  
     */
    public abstract int displaySize();

    /** Maps an <code>HClass</code> to an offset (in bytes).  
     *  If hc is an array type, returns the offset of the
     *  array's 0th element. */
    public abstract int elementsOffset(HClass hc);

    /** Maps an <code>HClass</code> to an offset (in bytes).  
     *  Returns the offset of the object's first field. */
    public abstract int fieldsOffset(HClass hc);

    /** Maps an <code>HClass</code> to an offset (in bytes).
     *  Returns the offset from an object reference at which the hashcode
     *  is stored. */
    public abstract int hashCodeOffset(HClass hc);

    /** Returns the offset from the class pointer of the list of interfaces
     *  implemented by the specified class
     */
    public abstract int interfaceListOffset(HClass hc);

    /** Maps an <code>HClass</code> to an offset (in bytes).  
     *  If <code>hc</code> is an array type, returns the offset from 
     *  an object reference of the array's length field. */
    public abstract int lengthOffset(HClass hc); 

    /** Maps an <code>HClass</code> to an offset (in bytes).
     *  Returns the offset from the class pointer of the specified
     *  class.  This will be some function of the class's depth in
     *  the class hierarchy. */
    public abstract int offset(HClass hc);

    /** Maps a non-static <code>HField</code> to an offset (in bytes).
     *  If the field is inlined using type 1 inlining (which preserves
     *  the class pointer) then the specified offset points just after the
     *  class descriptor, in the same place a normal object pointer points.
     *  If the field in inlined using type 2 inlining (which omits the
     *  class pointer) then the specified offset points to the first field
     *  of the object. */
    public abstract int offset(HField hf);

    /** Maps a non-static <code>HMethod</code> to an offset (in bytes).
     *  This method must work for interface methods as well as class methods.*/
    public abstract int offset(HMethod hm);

    /** Maps an <code>HClass</code> to a size (in bytes). */
    public abstract int size(HClass hc);

    /** Returns the size (in bytes) of a word in the architecture represented
     *  by this <code>OffsetMap</code>. */
    public abstract int wordsize();
}



