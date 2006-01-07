// ClassHierarchy.java, created Wed Sep  8 14:34:46 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;
import net.cscott.jutil.WorkSet;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;


/**
 * A <code>ClassHierarchy</code> enumerates reachable/usable classes
 * and callable methods.  A method is <i>callable</i> if the execution
 * of the compiled application may invoke that method (see {@link
 * #callableMethods()} for more info); these are the methods that Flex
 * absolutely has to compile.  To understand which classes are
 * <i>reachable/usable</i>, please see {@link #classes()}.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassHierarchy.java,v 1.11 2006-01-07 15:10:27 salcianu Exp $ */
public abstract class ClassHierarchy {

    /** Returns the set of all usable/reachable children of an
     *  <code>HClass</code>.  For an interface class <code>c</code>,
     *  the children include all reachable classes which implement it
     *  as well as any reachable interfaces which extend it.  For a
     *  non-interface class, children are all reachable subclasses.
     *  Note: this method deals with direct children; i.e., it doesn't
     *  return transitive (more than one level) subclassing children.
     *
     *  The result should be complementary to the result of the
     *  <code>c.parents()</code> method: the parents of any class
     *  <code>c</code> returned by <code>children(cc)</code> should
     *  include <code>cc</code>.  */
    public abstract Set<HClass> children(HClass c);


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



    /** Returns the set of all callable methods.  A Java method
	<code>m</code> is <i>callable</i> iff
	
	<ul>

	<li><code>m</code> is the <code>main</code> method of the application.

	<li><code>m</code> is invoked by the runtime code before the
	start of the main method (there may be such calls in order to
	initialize some required classes).

	<li><code>m</code> is the class initializer of a class from
	the set returned by the {@link #classes()} method; these
	classes are referenced, i.e., used, by the compiled
	application, so their class initializers ARE executed.

	<li><code>m</code> is the <code>run</code> method of a thread
	that is started by an already callable method.

	<li><code>m</code> is invoked from the an already callable method.

	</ul> */
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

	<li>Superclassing: if a class appears in <i>C</i>, then its
	superclass and all implemented interfaces (if any) appear in
	<i>C</i> too.

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


    /** Returns the set of all <i>instantiated</i> classes.  A class is
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
