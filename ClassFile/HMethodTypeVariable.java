// HMethodTypeVariable.java, created Mon Mar 17 20:15:53 2003 by cananian
// Copyright (C) 2003 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.ClassFile;

/**
 * The <code>HMethodTypeVariable</code> interface represents a type
 * variable declared as a formal parameter to a generic method.
 * Example: <code>A</code> in <code>&lt;A&gt; compareTo(A a)</code>.
 * <p>
 * Note that implementations of this interface are free to return
 * distinct objects for the same type variable; the identity of
 * an object implementing this interface may not be used to test for
 * identity among the type variables they represent. (In other words,
 * this interface does not extend <code>ReferenceUnique</code>.)
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HMethodTypeVariable.java,v 1.1 2003-03-18 02:27:02 cananian Exp $
 * @see java.lang.reflect.MethodTypeVariable
 */
public interface HMethodTypeVariable extends HTypeVariable {
    /**
     * Returns an <code>HMethod</code> object representing the method
     * that declared the type variable represented by this object.
     * @return the method that declared the type variable.
     */
    public HMethod getDeclaringMethod();
}
