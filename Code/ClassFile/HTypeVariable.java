// HTypeVariable.java, created Mon Mar 17 20:04:41 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * The <code>HTypeVariable</code> interface represents a type variable
 * declared as a formal parameter to a generic class, interface, or
 * method.  Example: <code>A</code> in <code>Collection&lt;A&gt;</code>.
 * This interface embodies commonality among all type variables.
 * Every actual type variable supports one of the two subinterfaces
 * <code>HMethodTypeVariable</code> or <code>HClassTypeVariable</code>.
 * <p>
 * Note that implementations of this interface are free to return
 * distinct objects for the same type variable; the identity of
 * an objects implementing this interface may not be used to test for
 * identity among the type variables they represent. (In other words,
 * this interface does not extend <code>ReferenceUnique</code>.)
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HTypeVariable.java,v 1.1 2003-03-18 02:27:02 cananian Exp $
 * @see java.lang.reflect.TypeVariable
 */
public interface HTypeVariable extends HType {
    /**
     * Returns an array with <code>HType</code> objects representing the
     * declared bound(s) of the type variable represented by this object.
     * If no bound is explicitly declared, than the array contains one
     * element, a <code>HClass</code> object representing
     * <code>java.lang.Object</code>.
     * @return the declared bounds for this type variable.
     */
    public HType[] getBounds();
    /**
     * Returns the name of this type variable.
     * @return the name of the type variable represented by this object.
     */
    public String getName();
}
