// HClassUtil.java, created Fri Sep 11 09:14:23 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.Linker;

/**
 * <code>HClassUtil</code> contains various useful methods for dealing with
 * HClasses that do not seem to belong with the standard HClass methods.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: HClassUtil.java,v 1.8 2002-02-26 22:47:24 cananian Exp $
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
    public static final HClass arrayClass(Linker linker, HClass hc, int dims) {
	StringBuffer sb = new StringBuffer();
	return linker.forDescriptor(Util.repeatString("[",dims)+
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
    /** Find and return the first common superclass of a pair of classes.
     *  Not valid for interface or array types --- both <code>a</code>
     *  and <code>b</code> must be primitive or simple object types.
     */
    public static final HClass commonSuper(HClass a, HClass b) {
	Util.ASSERT(!a.isInterface() && !b.isInterface());
	Util.ASSERT(!a.isArray() && !b.isArray());
	if (a.isPrimitive() || b.isPrimitive()) {
	    Util.ASSERT(a==b, "Can't merge differing primitive types. "+
			"("+a+" and "+b+")");
	    return a;
	}

	HClass[] A = parents(a);
	HClass[] B = parents(b);
	// both A[0] and B[0] should be java.lang.Object.
	Util.ASSERT(A[0]==B[0], "Hierarchy roots differ: "+A[0]+"/"+B[0]);
	int i;
	for(i=1; i<A.length && i<B.length; i++)
	    if (A[i] != B[i]) break;
	// A[i] and B[i] now point to the first *different* parent.
	return A[i-1];
    }
    /** Find and return the first common superinterface of a pair of
     *  interfaces. */
    public static final HClass commonInterface(HClass a, HClass b) {
	Util.ASSERT(a.isInterface() && b.isInterface());
	Util.ASSERT(!a.isArray() && !b.isArray());
	// this is a quick hack.
	if (a.isSuperinterfaceOf(b)) return a;
	if (b.isSuperinterfaceOf(a)) return b;
	Util.ASSERT(!a.isPrimitive()); // getLinker() won't work in this case
	return a.getLinker().forName("java.lang.Object");
    }
    /** Find a class which is a common parent of both suppied classes.
     *  Valid for array, interface, and primitive types.
     */
    public static final HClass commonParent(HClass a, HClass b) {
	if (a.isPrimitive() || b.isPrimitive()) return commonSuper(a, b);
	// note that using getLinker() is safe because neither a nor b
	// is primitive by this point.
	Util.ASSERT(a.getLinker()==b.getLinker());
	if (a.isArray() && b.isArray()) {
	    Linker linker = a.getLinker();
	    int ad = dims(a), bd = dims(b), d = (ad<bd)?ad:bd;
	    for (int i=0; i<d; i++)
		{ a=a.getComponentType(); b=b.getComponentType(); }
	    return arrayClass(linker, commonParent(a, b), d);
	}
	if (a.isInterface() && b.isInterface())
	    return commonInterface(a, b);
	if (b.isArray()) return commonParent(b,a);
	if (a.isArray()) // b is interface or object, not array.
	    if (b==b.getLinker().forName("java.lang.Cloneable") ||
		b==b.getLinker().forName("java.io.Serializable"))
		return b;
	    else return b.getLinker().forName("java.lang.Object");
	if (b.isInterface()) return commonParent(b,a);
	if (a.isInterface()) // b is object
	    if (a.isSuperinterfaceOf(b)) return a;
	    else return a.getLinker().forName("java.lang.Object");
	// both a and b are object.
	return commonSuper(a,b);
    }
}
