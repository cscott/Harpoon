// QuadClassHierarchy.java, created Sun Oct 11 13:08:31 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HConstructor;
import harpoon.ClassFile.HField;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.Linker;
import harpoon.IR.Quads.ANEW;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.CONST;
import harpoon.IR.Quads.Code;
import harpoon.IR.Quads.GET;
import harpoon.IR.Quads.HANDLER;
import harpoon.IR.Quads.INSTANCEOF;
import harpoon.IR.Quads.NEW;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
import harpoon.IR.Quads.SET;
import harpoon.IR.Quads.TYPECAST;
import harpoon.IR.Quads.TYPESWITCH;
import harpoon.Util.ArraySet;
import harpoon.Util.HClassUtil;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;

import java.lang.reflect.Modifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * <code>QuadClassHierarchy</code> computes a <code>ClassHierarchy</code>
 * of classes possibly usable starting from some root method using
 * quad form.
 * Native methods are not analyzed.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: QuadClassHierarchy.java,v 1.1.2.34 2001-11-08 00:22:52 cananian Exp $
 */

public class QuadClassHierarchy extends harpoon.Analysis.ClassHierarchy
    implements java.io.Serializable {
    private final Map children = new HashMap();
    private final Set methods = new HashSet();

    /** Returns set of all callable methods. 
	@return <code>Set</code> of <code>HMethod</code>s.
     */
    public Set callableMethods() {
	return _unmod_methods;
    }
    private final Set _unmod_methods = Collections.unmodifiableSet(methods);

    // inherit description from parent class.
    public Set children(HClass c) {
	if (children.containsKey(c))
	    return new ArraySet((HClass[]) children.get(c));
	return Collections.EMPTY_SET;
    }
    // inherit description from parent class.
    public Set classes() {
	if (_classes == null) {
	    _classes = new HashSet();
	    for (Iterator it = children.keySet().iterator(); it.hasNext(); )
		_classes.add(it.next());
	    for (Iterator it = children.values().iterator(); it.hasNext();) {
		HClass[] ch = (HClass[]) it.next();
		for (int i=0; i<ch.length; i++)
		    _classes.add(ch[i]);
	    }
	    _classes = Collections.unmodifiableSet(_classes);
	}
	return _classes;
    }
    private transient Set _classes = null;
    /** Returns the set of all classes instantiated.
	(Actually only the list of classes for which an explicit NEW is found;
	should include list of classes that are automatically created by JVM!) */ 
    public Set instantiatedClasses() {
	return _unmod_insted;
    }
    private final Set instedClasses = new HashSet();
    private final Set _unmod_insted=Collections.unmodifiableSet(instedClasses);

    /** Returns a human-readable representation of the hierarchy. */
    public String toString() {
	// not the most intuitive representation...
	StringBuffer sb = new StringBuffer("{");
	for (Iterator it = children.keySet().iterator(); it.hasNext(); ) {
	    HClass c = (HClass) it.next();
	    sb.append(c.toString());
	    sb.append("={");
	    for (Iterator it2=children(c).iterator(); it2.hasNext(); ) {
		sb.append(it2.next().toString());
		if (it2.hasNext())
		    sb.append(',');
	    }
	    sb.append('}');
	    if (it.hasNext())
		sb.append(", ");
	}
	sb.append('}');
	return sb.toString();
    }

    ////// hclass objects
    private final HMethod HMstrIntern;
    private final HMethod HMthrStart;
    private final HMethod HMthrRun;
    private QuadClassHierarchy(Linker linker) {
	HMstrIntern = linker.forName("java.lang.String")
	    .getMethod("intern",new HClass[0]);
	HMthrStart = linker.forName("java.lang.Thread")
	    .getMethod("start", new HClass[0]);
	HMthrRun = linker.forName("java.lang.Thread")
	    .getMethod("run", new HClass[0]);
    }

    /** Creates a <code>ClassHierarchy</code> of all classes
     *  reachable/usable from <code>HMethod</code>s in the <code>roots</code>
     *  <code>Collection</code>.  <code>HClass</code>es included in
     *  <code>roots</code> are guaranteed to be included in the
     *  <code>classes()</code> set of this class hierarchy, but they may
     *  not be included in the <code>instantiatedClasses</code> set
     *  (if an instantiation instruction is not found for them).  To
     *  explicitly include an instantiated class in the hierarchy, add
     *  a constructor or non-static method of that class to the
     *  <code>roots</code> <code>Collection</code>.<p> <code>hcf</code>
     *  must be a code factory that generates quad form. */
    public QuadClassHierarchy(Linker linker,
			      Collection roots, HCodeFactory hcf) {
	this(linker); // initialize hclass objects.
	if (hcf.getCodeName().equals(harpoon.IR.Quads.QuadWithTry.codename)) {
	    // add 'implicit' exceptions to root set when analyzing QuadWithTry
	    String[] implExcName = new String[] { 
		"java.lang.ArrayStoreException",
		"java.lang.NullPointerException",
		"java.lang.ArrayIndexOutOfBoundsException",
		"java.lang.NegativeArraySizeException",
		"java.lang.ArithmeticException",
		"java.lang.ClassCastException"
	    };
	    roots = new HashSet(roots); // make mutable
	    for (int i=0; i<implExcName.length; i++)
		roots.add(linker.forName(implExcName[i])
			  .getConstructor(new HClass[0]));
	}
	// state.
	final State S = new State();

	// make initial worklist from roots collection.
	for (Iterator it=roots.iterator(); it.hasNext(); ) {
	    HClass rootC; HMethod rootM; boolean instantiated;
	    // deal with the different types of objects in the roots collection
	    Object o = it.next();
	    if (o instanceof HMethod) {
		rootM = (HMethod) o;
		rootC = rootM.getDeclaringClass();
		// let's assume non-static roots have objects to go with 'em.
		instantiated = !rootM.isStatic();
	    } else { // only HMethods and HClasses in roots, so must be HClass
		rootM = null;
		rootC = (HClass) o;
		instantiated = false;
	    }
	    if (instantiated)
		discoverInstantiatedClass(S, rootC);
	    else
		discoverClass(S, rootC);
	    if (rootM!=null)
		methodPush(S, rootM);
	}

	// worklist algorithm.
	while (!S.W.isEmpty()) {
	    HMethod m = (HMethod) S.W.pull();
	    S.done.add(m); // mark us done with this method.
	    // This method should be marked as usable.
	    {
		Set s = (Set) S.classMethodsUsed.get(m.getDeclaringClass());
		Util.assert(s!=null);
		Util.assert(s.contains(m));
	    }
	    // look at the hcode for the method.
	    harpoon.IR.Quads.Code hc = (harpoon.IR.Quads.Code) hcf.convert(m);
	    if (hc==null) { // native or unanalyzable method.
		if(!m.getReturnType().isPrimitive())
		    // be safe; assume the native method can make an object
		    // of its return type.
		    discoverInstantiatedClass(S, m.getReturnType());
	    } else { // look for CALLs, NEWs, and ANEWs
		QuadVisitor qv = new QuadVisitor() {
		    public void visit(Quad q) { /* do nothing */ }
		    // creation of a (possibly-new) class
		    public void visit(ANEW q) {
			discoverInstantiatedClass(S, q.hclass());
		    }
		    public void visit(NEW q) {
			discoverInstantiatedClass(S, q.hclass());
		    }
		    public void visit(CONST q) {
			if (q.type().isPrimitive()) return;
			discoverInstantiatedClass(S, q.type());
			// string constants use intern()
			discoverMethod(S, HMstrIntern, false/*non-virtual*/);
		    }
		    // CALLs:
		    public void visit(CALL q) {
			if (q.isStatic() || !q.isVirtual())
			    discoverMethod(S, q.method(),false/*non-virtual*/);
			else
			    discoverMethod(S, q.method(),true/*virtual*/);
		    }
		    // get and set discover classes (don't instantiate, though)
		    public void visit(GET q) {
			discoverClass(S, q.field().getDeclaringClass());
		    }
		    public void visit(SET q) {
			discoverClass(S, q.field().getDeclaringClass());
		    }
		    // make sure we have the class we're testing against
		    // handy.
		    public void visit(INSTANCEOF q) {
			discoverClass(S, q.hclass());
		    }
		    public void visit(TYPECAST q) {
			discoverClass(S, q.hclass());
		    }
		    public void visit(TYPESWITCH q) {
			for (int i=0; i<q.keysLength(); i++)
			    discoverClass(S, q.keys(i));
		    }
		    public void visit(HANDLER q) {
			if (q.caughtException()==null) return;
			discoverClass(S, q.caughtException());
		    }
		};
		// iterate through quads with visitor.
		for (Iterator it = hc.getElementsI(); it.hasNext(); )
		    ((Quad) it.next()).accept(qv);
	    }
	} // END worklist.
	
	// build method table from classMethodsUsed.
	for (Iterator it = S.classMethodsUsed.values().iterator();
	     it.hasNext(); )
	    methods.addAll((Set) it.next());
	// now generate children set from classKnownChildren.
	for (Iterator it = S.classKnownChildren.keySet().iterator();
	     it.hasNext(); ) {
	    HClass c = (HClass) it.next();
	    Set s = (Set) S.classKnownChildren.get(c);
	    HClass ch[] = (HClass[]) s.toArray(new HClass[s.size()]);
	    children.put(c, ch);
	}
    }

    /* when we discover a new class nc:
        for each superclass c or superinterface i of this class,
         add all called methods of c/i to worklist of nc, if nc implements.
    */
    private void discoverClass(State S, HClass c) {
	if (S.classKnownChildren.containsKey(c)) return; // not a new class.
	// add to known-children lists.
	Util.assert(!S.classKnownChildren.containsKey(c));
	S.classKnownChildren.put(c, new HashSet());
	Util.assert(!S.classMethodsUsed.containsKey(c));
	S.classMethodsUsed.put(c, new HashSet());
	Util.assert(!S.classMethodsPending.containsKey(c));
	S.classMethodsPending.put(c, new HashSet());
	// add class initializer (if it exists) to "called" methods.
	HMethod ci = c.getClassInitializer();
	if ((ci!=null) && (!S.done.contains(ci)))
	    methodPush(S, ci);
	// mark component type of arrays
	if (c.isArray())
	    discoverClass(S, c.getComponentType());
	// work through parents (superclass and interfaces)
	for (Iterator it=parents(c).iterator(); it.hasNext(); ) {
	    HClass p = (HClass) it.next();
	    discoverClass(S, p);
	    Set knownChildren = (Set) S.classKnownChildren.get(p);
	    knownChildren.add(c); // kC non-null after discoverClass.
	}
    }
    private void discoverInstantiatedClass(State S, HClass c) {
	if (instedClasses.contains(c)) return; else instedClasses.add(c);
	discoverClass(S, c);

	// collect superclasses and interfaces.
	// new worklist.
	WorkSet sW = new WorkSet();
	// put superclass and interfaces on worklist.
	sW.addAll(parents(c));

	// first, wake up all methods of this class that were
	// pending instantiation, and clear the pending list.
	List ml = new ArrayList((Set) S.classMethodsPending.get(c));//copy list
	for (Iterator it=ml.iterator(); it.hasNext(); )
	    methodPush(S, (HMethod)it.next());

	// if instantiated,
	// add all called methods of superclasses/interfaces to worklist.
	while (!sW.isEmpty()) {
	    // pull a superclass or superinterface off the list.
	    HClass s = (HClass) sW.pop();
	    // add superclasses/interfaces of this one to local worklist
	    sW.addAll(parents(s));
	    // now add called methods of s to top-level worklist.
	    Set calledMethods = new WorkSet((Set)S.classMethodsUsed.get(s));
	    calledMethods.addAll((Set)S.classMethodsPending.get(s));
	    for (Iterator it = calledMethods.iterator(); it.hasNext(); ) {
		HMethod m = (HMethod) it.next();
		if (!isVirtual(m)) continue; // not inheritable.
		try {
		    HMethod nm = c.getMethod(m.getName(),
					     m.getDescriptor());
		    if (!S.done.contains(nm))
			methodPush(S, nm);
		} catch (NoSuchMethodError nsme) { }
	    }
	}
	// done with this class/interface.
    }
    /* when we hit a method call site (method in class c):
        add method of c and all children of c to worklist.
       if method in interface i:
        add method of c and all implementations of c.
    */
    private void discoverMethod(State S, HMethod m, boolean isVirtual) {
	if (isVirtual && S.nonvirtual.contains(m)) {
	    // this is the first virtual invocation of a previously
	    // nonvirtual method.
	    // [removing m from S.nonvirtual will be done below,
	    //  near the call to methodPush(S, nm)]
	} else if (S.done.contains(m) || S.W.contains(m)) {
	    // we've done this guy before
	    return;
	}
	discoverClass(S, m.getDeclaringClass());
	// Thread.start() implicitly causes a call to Thread.run()
	if (m.equals(HMthrStart))
	    discoverMethod(S, HMthrRun, true/*virtual*/);
	if (!isVirtual) { // short-cut for non-virtual methods.
	    S.nonvirtual.add(m);
	    methodPush(S, m);
	    return; // that's all folks.
	}
	// mark as pending in its own class if not already used.
	if (!((Set) S.classMethodsUsed.get(m.getDeclaringClass())).contains(m))
	    ((Set) S.classMethodsPending.get(m.getDeclaringClass())).add(m);
	// now add as 'used' to all instantiated children.
	// (including itself, if its own class has been instantiated)
	WorkSet cW = new WorkSet();
	cW.push(m.getDeclaringClass());
	while (!cW.isEmpty()) {
	    // pull a class from the worklist
	    HClass c = (HClass) cW.pull();
	    // see if we should add method-of-c to method worklist.
	    if (c.isInterface() || instedClasses.contains(c))
		try {
		    HMethod nm = c.getMethod(m.getName(),
					     m.getDescriptor());
		    Util.assert(isVirtual); // shouldn't be here otherwise.
		    if (!S.done.contains(nm))
			methodPush(S, nm);
		    else if (!S.nonvirtual.contains(nm))
			continue; // nothing new to discover.
		    else S.nonvirtual.remove(nm); // since we're virtual here.
		} catch (NoSuchMethodError e) { }
	    // add all children to the worklist.
	    Set knownChildren = (Set) S.classKnownChildren.get(c);
	    for (Iterator it = knownChildren.iterator(); it.hasNext(); ) 
		cW.push(it.next());
	}
	// done.
    }

    private void methodPush(State S, HMethod m) {
	Util.assert(!S.done.contains(m));
	if (S.W.contains(m)) return; // already on work list.
	// Add to worklist
	S.W.add(m);
	// mark this method as used.
	Set s1 = (Set) S.classMethodsUsed.get(m.getDeclaringClass());
	s1.add(m);
	// and no longer pending.
	Set s2 = (Set) S.classMethodsPending.get(m.getDeclaringClass());
	s2.remove(m);
    }
    // State for algorithm.
    private static class State {
	// keeps track of methods which are actually invoked at some point.
	final Map classMethodsUsed = new HashMap(); // class->set map.
	// keeps track of methods which might be called, if someone gets
	// around to instantiating an object of the proper type.
	final Map classMethodsPending = new HashMap(); // class->set map
	// keeps track of all known children of a given class.
	final Map classKnownChildren = new HashMap(); // class->set map.
	// keeps track of which methods we've done already.
	final Set done = new HashSet();
	// keeps track of methods we've only seen non-virtual invocations for.
	final Set nonvirtual = new HashSet();

	// Worklist.
	final WorkSet W = new WorkSet();
    }

    // useful utility method
    private static boolean isVirtual(HMethod m) {
	if (m.isStatic()) return false;
	if (Modifier.isPrivate(m.getModifiers())) return false;
	if (m instanceof HConstructor) return false;
	return true;
    }

    /* ALGORITHM:
       for each class:
        table of all methods used in that class.
        list of known immediate children of the class.
       when we discover a new class nc:
        for each superclass c of this class,
         add all called methods of c to worklist of nc, if nc implements.
       when we hit a method call site (method in class c):
        add method of c and all children of c to worklist.
       for each method on worklist:
        mark method as used in class.
        for each NEW: add possibly new class.
        for each CALL: add possibly new methods.
    */
}
