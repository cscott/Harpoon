// HClassUtil.java, created Fri Sep 11 09:14:23 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import harpoon.ClassFile.*;
/**
 * <code>HClassUtil</code> contains various useful methods for dealing with
 * HClasses that do not seem to belong with the standard HClass methods.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassUtil.java,v 1.6 1998-10-11 02:37:58 cananian Exp $
 */

public abstract class HClassUtil  {
    // Only static methods.
    
    /** Count the number of dimensions of an array type.
     *  @return 0 for a non-array, n for an n-dimensional array type.
     */
    public static final int dims(HClass hc) {
	int i;
	for (i=0; hc.isArray(); i++)
	    hc = hc.getComponentType();
	return i;
    }
    /** Return the ultimate component type of an array (that is, after
     *  all array dimensions have been stripped off). 
     * @return input class <code>hc</code> if <code>hc</code> is not an
     *         array, otherwise a component class <code>c</code> where
     *         <code>c</code> is not an array. */
    public static final HClass baseClass(HClass hc) {
	while (hc.isArray())
	    hc = hc.getComponentType();
	return hc;
    }
    /** Make an n-dimensional array class from the given component class.
     *  The parameter <code>dims</code> is the number of array dimensions
     *  to add. */
    public static final HClass arrayClass(HClass hc, int dims) {
	StringBuffer sb = new StringBuffer();
	return HClass.forDescriptor(Util.repeatString("[",dims)+
				    hc.getDescriptor());
    }
    /** Create an array describing the inheritance of class hc.
     * @return an array, where element 0 is the HClass for java.lang.Object,
     *         an the last element is hc.
     */
    public static final HClass[] parents(HClass hc) {
	int len=0;
	for (HClass h=hc; h!=null; h=h.getSuperclass())
	    len++;
	HClass[] r = new HClass[len];
	for (len--; len>=0 && hc!=null; hc=hc.getSuperclass(), len--)
	    r[len] = hc;
	return r;
    }
    /** Find and return the first common superclass of a pair of classes. */
    public static final HClass commonSuper(HClass a, HClass b) {
	HClass[] A = parents(a);
	HClass[] B = parents(b);
	Util.assert(A[0]==A[0]); // should be java.lang.Object.
	int i;
	for(i=1; i<A.length && i<B.length; i++)
	    if (A[i] != B[i]) break;
	// A[i] and B[i] now point to the first *different* parent.
	return A[i-1];
    }
}
