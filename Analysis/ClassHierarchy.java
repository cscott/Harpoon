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
import net.cscott.jutil.WorkSet;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
/**
 * A <code>ClassHierarchy</code> enumerates reachable/usable classes
 * and methods.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassHierarchy.java,v 1.7 2005-10-14 19:00:56 salcianu Exp $
 */
public abstract class ClassHierarchy {
    // tree of callable classes
    /** Returns the set of all usable/reachable children of an
     *  <code>HClass</code>.  For an interface class <code>c</code>,
     *  the children include all reachable classes which implement it
     *  as well as any reachable interfaces which extend it.  For a
     *  non-interface class, children are all reachable subclasses.
     *  Note: this method deals with direct children; i.e., it doesn't
     *  return transitive (more than one level) subclassing children.
     *
     *  <p>[AS 09/28/05]: TODO: DOCUMENT: what does "reachable" mean?
     *  It's not "transitive" (see above Note).  Here is a tentative
     *  definition: a class is reachable iff (1) it is instantiated,
     *  or (2) it is the superclass of a reachable class.  Is this OK?
     * */
    public abstract Set<HClass> children(HClass c);

    /** Return the parents of an <code>HClass</code>.
     *  The parents of a class <code>c</code> are its superclass and 
     *  interfaces.  The results should be complementary to the 
     *  <code>children()</code> method:
     *  <code>parent(c)</code> of any class <code>c</code> returned
     *  by <code>children(cc)</code> should include <code>cc</code>.
     */
    public final Set<HClass> parents(HClass c) {
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
	boolean isPrimArray = c.isArray() && base.isPrimitive();
	// root interface inheritance hierarchy at Object.
	if (interfaces.length==0 && base.isInterface())
	    su = c.getLinker().forName("java.lang.Object");// c not prim.
	// create return value array.
	HClass[] parents = new HClass[interfaces.length +
				      ((su!=null || isObjArray || isPrimArray)
				       ? 1 : 0)];
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
	// also!  int[] -> Object.
	if (isPrimArray) // c not prim.
	    parents[n++] = c.getLinker().forName("java.lang.Object");
	// okay, done.  Did we size the array correctly?
	assert n==parents.length;
	// okay, return as Set.
	return new ArraySet<HClass>(parents);
    }

    /** Returns a set of methods in the hierarchy (not necessary
     *  reachable methods) which override the given method
     *  <code>hm</code>.  The set does not include <code>hm</code> and
     *  is only one level deep; invoke <code>overrides()</code> again
     *  on each member of the returned set to find the rest of the
     *  possible overriding methods.
     *
     *  <p>Note however that interface methods may introduce some
     *  imprecision: in particular, for some hm2 in overrides(hm1)
     *  (where hm1 is an interface method), hm2.getDeclaringClass()
     *  may not implement the interface hm1.getDeclaringClass().  For
     *  example, <code>ListIterator.next()</code> may be implemented
     *  by class <code>A</code> that does not implement the full
     *  <code>ListIterator</code> interface; class <code>B</code> (a
     *  subclass of <code>A</code> which does not override
     *  <code>A.next()</code>) may be the class which implements
     *  <code>ListIterator</code>. */
    public final Set<HMethod> overrides(HMethod hm) {
	return overrides(hm.getDeclaringClass(), hm, false);
    }

    /** Returns the set of methods, excluding <code>hm</code>, declared
     *  in classes which are instances of <code>hc</code>, which override
     *  <code>hm</code>.  If <code>all</code> is true, returns all such
     *  methods in the class hierarchy; otherwise returns only the methods
     *  which *immediately* override <code>hm</code>. */
    public Set<HMethod> overrides(HClass hc, HMethod hm, boolean all) {
	// non-virtual, private and constructor methods have no overrides.
	if (hm.isStatic() || Modifier.isPrivate(hm.getModifiers()) ||
	    hm instanceof HConstructor) return Collections.<HMethod>emptySet();

	// determine overrides for virtual methods.
	Set<HMethod> result = new WorkSet<HMethod>();
	WorkSet<HClass> ws = new WorkSet<HClass>(this.children(hc));
	while (!ws.isEmpty()) {
	    HClass hcc = ws.pop();
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
    public abstract Set<HMethod> callableMethods();


    /** Returns the set of all reachable/usable classes.  

	<p>More technically, this method returns the smallest set
	<i>C</i> that satisfies the following rules:

	<ul>

	<li><i>C</i> contains all classes that are referenced from the
	callable methods ({@link #callableMethods}).  This includes:

	<ul>

	<li>Instantiated classes (i.e., the set returned by
	<code>classes()</code> includes the set returned by
	<code>instantiatedClasses()</code>).

	<li>Declaring classes for callable static methods.

	<li>Declaring classes for accessed fields.

	<li>Classes that appear in class casts or in
	<code>instanceof</code> tests (including the implicit tests
	that appear at the beginning of exception handlers).

	</ul>

	<li><i>C</i> is closed with respect to

	<ul>

	<li>Superclassing: if a class appears in this <i>C</i>, than
	its superclass and all implemented interfaces (if any) appear
	in <i>C</i> too.

	<li>Array element relation: if the classs <code>T[]></code>
	appears in <i>C</i>, <code>T</code> appears in <i>C</i> too.

	</ul>

	</ul>

	Intuitively, these are the classes Flex will generate code
	for.  E.g., if a static method is called, the PreciseC backend
	of Flex will generate a <code>.c</code> file for its declaring
	class, including the code for that static method (but not
	including the code for other method that are not invoked by
	the compiled application). */
    public abstract Set<HClass> classes();


    /** Returns the set of all *instantiated* classes.  A class is
	included in this set only if an instance of this class may be
	created during the execution of the compiled application.
	This is a subset of the set returned by the
	<code>classes()</code> method.  It includes:

	<ul>

	<li>Classes instantiated by the code of the Java callable
	methods ({@link #callableMethods()}), using an instruction
	like <code>NEW</code> or <code>ANEW</code> (array new).

	<li>Classes instantiated by the native code that runs before
	the main method is invoked, or by the native methods
	(transitively) invoked by the callable Java methods.  Flex
	should be informed about this classes by one of the external
	<i>root</i> property files.

	</ul> */
    public abstract Set<HClass> instantiatedClasses();
}
