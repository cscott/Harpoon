// ClassHierarchy.java, created Wed Sep  8 14:34:46 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.Util.ArraySet;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
/**
 * A <code>ClassHierarchy</code> enumerates reachable/usable classes
 * and methods.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassHierarchy.java,v 1.1.2.5 2000-12-17 20:01:58 cananian Exp $
 */
public abstract class ClassHierarchy {
    // tree of callable classes
    /** Returns all usable/reachable children of an <code>HClass</code>.
     *  For an interface class <code>c</code>, the children include all
     *  reachable classes which implement it as well as any reachable
     *  interfaces which extend it.  For a non-interface class, children
     *  are all reachable subclasses.
     *	@return <code>Set</code> of <code>HClass</code>es.
     */
    public abstract Set children(HClass c);
    /** Return the parents of an <code>HClass</code>.
     *  The parents of a class <code>c</code> are its superclass and 
     *  interfaces.  The results should be complementary to the 
     *  <code>children()</code> method:
     *  <code>parent(c)</code> of any class <code>c</code> returned
     *  by <code>children(cc)</code> should include <code>cc</code>.
     */
    public final Set parents(HClass c) {
	// odd inheritance properties:
	//  interfaces: all instances of an interface are also instances of
	//              java.lang.Object, so root all interfaces there.
	//  arrays: Integer[][]->Number[][]->Object[][]->Object[]->Object
	//      but also Set[][]->Collection[][]->Object[][]->Object[]->Object
	//      (i.e. interfaces are just as rooted here)
	// note every use of c.getLinker() below is safe because c is
	// guaranteed non-primitive in every context.
	HClass base = HClassUtil.baseClass(c); int dims=HClassUtil.dims(c);
	HClass su = base.getSuperclass();
	HClass[] interfaces = base.getInterfaces();
	boolean isObjArray = c.getDescriptor().endsWith("[Ljava/lang/Object;");
	// root interface inheritance hierarchy at Object.
	if (interfaces.length==0 && base.isInterface())
	    su = c.getLinker().forName("java.lang.Object");// c not prim.
	// create return value array.
	HClass[] parents = new HClass[interfaces.length +
				      ((su!=null || isObjArray) ? 1 : 0)];
	int n=0;
	if (su!=null)
	    parents[n++] = HClassUtil.arrayClass(c.getLinker(),//c not prim.
						 su, dims);
	for (int i=0; i<interfaces.length; i++)
	    parents[n++] = HClassUtil.arrayClass(c.getLinker(),//c not prim.
						 interfaces[i], dims);
	// don't forget Object[][]->Object[]->Object
	// (but remember also Object[][]->Cloneable->Object)
	if (isObjArray)
	    parents[n++] = HClassUtil.arrayClass(c.getLinker(), base, dims-1);
	// okay, done.  Did we size the array correctly?
	Util.assert(n==parents.length);
	// okay, return as Set.
	return new ArraySet(parents);
    }
    /** Returns a set of methods in the hierarchy (not necessary reachable
     *  methods) which override the given method.  The set is only one
     *  level deep; invoke children() again on each member of the returned
     *  set to find the rest of the possible overriding methods.  Note
     *  however that interface methods may introduce some imprecision:
     *  in particular, for some hm2 in children(hm1) (where hm1 is an
     *  interface method), hm2.getDeclaringClass() may not implement
     *  hm1.getDeclaringClass().   For example, ListIterator.next() may
     *  be implemented by A, but B (a subclass of A which doesn't override
     *  A.next()) may be the class which implements ListIterator. */
    public final Set overrides(HMethod hm) {
	return overrides(hm.getDeclaringClass(), hm, false);
    }
    /** Returns the set of methods, excluding <code>hm</code>, declared
     *  in classes which are instances of <code>hc</code>, which override
     *  <code>hm</code>.  If <code>all</code> is true, returns all such
     *  methods in the class hierarchy; otherwise returns only the methods
     *  which *immediately* override <code>hm</code>. */
    public Set overrides(HClass hc, HMethod hm, boolean all) {
	// non-virtual methods have no overrides.
	if (hm.isStatic() || Modifier.isPrivate(hm.getModifiers()) ||
	    hm instanceof HConstructor) return Collections.EMPTY_SET;
	// determine overrides for virtual methods.
	Set result = new WorkSet();
	WorkSet ws = new WorkSet(this.children(hc));
	while (!ws.isEmpty()) {
	    HClass hcc = (HClass) ws.pop();
	    // note we don't catch MethodNotFoundError 'cuz we should find hm.
	    HMethod hmm = hcc.getMethod(hm.getName(), hm.getDescriptor());
	    if (!hm.equals(hmm)) {
		// this is an overriding method!
		result.add(hmm);
		if (all) result.addAll(overrides(hcc, hmm, all));
	    } else
		// keep looking for subclasses that declare method:
		// add all subclasses of this one to the worklist.
		ws.addAll(this.children(hcc));
	}
	return result;
    }

    // other methods.
    /** Returns set of all callable methods. 
     *	@return <code>Set</code> of <code>HMethod</code>s.
     */
    public abstract Set callableMethods();
    /** Returns the set of all reachable/usable classes.
     *  If any method in a class is callable (including static methods),
     *  then the class will be a member of the returned set.
     *  @return <code>Set</code> of <code>HClass</code>es.
     */
    public abstract Set classes();
    /** Returns the set of all *instantiated* classes.
     *  This is a subset of the set returned by the <code>classes()</code>
     *  method.  A class is included in the return set only if an
     *  object of that type is at some point created.
     */
    public abstract Set instantiatedClasses();
}
