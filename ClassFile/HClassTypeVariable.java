// HClassTypeVariable.java, created Mon Mar 17 20:10:58 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * The <code>HClassTypeVariable</code> interface represents a type variable
 * declared as a formal parameter to a generic class or interface.
 * Example: <code>A</code> in <code>Collection&lt;A&gt;</code>.
 * <p>
 * Note that implementations of this interface are free to return
 * distinct objects for the same type variable; the identity of
 * an object implementing this interface may not be used to test for
 * identity among the type variables they represent. (In other words,
 * this interface does not extend <code>ReferenceUnique</code>.)
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassTypeVariable.java,v 1.1 2003-03-18 02:27:02 cananian Exp $
 * @see java.lang.reflect.ClassTypeVariable
 */
public interface HClassTypeVariable extends HTypeVariable {
    /**
     * Returns an <code>HClass</code> object representing the class or
     * interface that declared the type variable represented by this
     * object.
     * @return the class or interface that declared the type variable.
     */
    public HClass getDeclaringClass();
}
