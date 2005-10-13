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
import net.cscott.jutil.Default;
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
 * @version $Id: QuadClassHierarchy.java,v 1.9 2005-10-13 22:14:45 salcianu Exp $
 */

public class QuadClassHierarchy extends harpoon.Analysis.ClassHierarchy
    implements java.io.Serializable {
    private final Map<HClass,HClass[]> children =
	new HashMap<HClass,HClass[]>();
    private final Set<HMethod> methods = new HashSet<HMethod>();

    /** Returns set of all callable methods. 
	@return <code>Set</code> of <code>HMethod</code>s.
     */
    public Set<HMethod> callableMethods() {
	return _unmod_methods;
    }
    private final Set<HMethod> _unmod_methods =
	Collections.unmodifiableSet(methods);

    // inherit description from parent class.
    public Set<HClass> children(HClass c) {
	if (children.containsKey(c))
	    return new ArraySet<HClass>(children.get(c));
	return Default.EMPTY_SET();
    }
    // inherit description from parent class.
    public Set<HClass> classes() {
	if (_classes == null) {
	    _classes = new HashSet<HClass>();
	    for(HClass hc : children.keySet()) {
		_classes.add(hc);
	    }
	    for(HClass[] ch : children.values()) {
		for (int i=0; i<ch.length; i++)
		    _classes.add(ch[i]);
	    }
	    _classes = Collections.unmodifiableSet(_classes);
	}
	return _classes;
    }
    private transient Set<HClass> _classes = null;
    /** Returns the set of all classes instantiated.
	(Actually only the list of classes for which an explicit NEW is found;
	should include list of classes that are automatically created by JVM!) */ 
    public Set<HClass> instantiatedClasses() {
	return _unmod_insted;
    }
    private final Set<HClass> instedClasses = new HashSet<HClass>();
    private final Set<HClass> _unmod_insted =
	Collections.unmodifiableSet(instedClasses);

    /** Returns a human-readable representation of the hierarchy. */
    public String toString() {
	// not the most intuitive representation...
	StringBuffer sb = new StringBuffer("{");
	for (Iterator<HClass> it=children.keySet().iterator(); it.hasNext(); ){
	    HClass c = it.next();
	    sb.append(c.toString());
	    sb.append("={");
	    for (Iterator<HClass> it2=children(c).iterator(); it2.hasNext(); ){
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

    // Frequently used HMethod objects' should be final, but the java
    // compiler cannot detect that this method is called only from the
    // constructor ...
    private HMethod HMstrIntern;
    private HMethod HMthrStart;
    private HMethod HMthrRun;
    /* [AS 09/28/05]: Used to be a private constructor: confusing:
       this "constructor" was radically different from the real
       constructor - it is just a convenient initializers for the real
       constructor, and should be declared as such. */
    private void initHMethods(Linker linker) {
	HMstrIntern = linker.forName("java.lang.String")
	    .getMethod("intern",new HClass[0]);
	HMthrStart = linker.forName("java.lang.Thread")
	    .getMethod("start", new HClass[0]);
	HMthrRun = linker.forName("java.lang.Thread")
	    .getMethod("run", new HClass[0]);
    }


    private void addImplicitExceptions(Linker linker, Collection roots, HCodeFactory hcf) {
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

    // make initial worklist from roots collection.
    private void initWorkList(State S, Collection roots) {
	for (Object root : roots) {
	    HClass rootC; HMethod rootM; boolean instantiated;
	    
	    // deal with the different types of objects in the roots collection
	    if (root instanceof HMethod) {
		rootM = (HMethod) root;
		rootC = rootM.getDeclaringClass();
		// let's assume non-static method roots have objects to go with 'em.
		instantiated = !rootM.isStatic();
	    } else { // only HMethods and HClasses in roots, so o must be HClass
		rootM = null;
		rootC = (HClass) root;
		// no methods of an array.  so mention of an array means
		// it is instantiated.  otherwise, it's not.
		instantiated = rootC.isArray();
	    }
	    
	    if (instantiated)
		discoverInstantiatedClass(S, rootC);
	    else
		discoverClass(S, rootC);
	    
	    if (rootM != null)
		methodPush(S, rootM);
	}
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
     *  must be a code factory that generates quad form. 

     <p>
     [AS 09/28/05]: TODO: why do we consider as instantiated the
     declaring class of a non-static method root?  What if that method
     is actually called on an instantiated SUBclass?  MORE IMPORTANT
     QUESTION: why is it important for a class to be included in the
     classes() set?
     */
    public QuadClassHierarchy(Linker linker,
			      Collection roots, HCodeFactory hcf) {
	initHMethods(linker); // initialize hclass objects.
	if (hcf.getCodeName().equals(harpoon.IR.Quads.QuadWithTry.codename)) {
	    addImplicitExceptions(linker, roots, hcf);
	}

	// state.
	final State S = new State();
	// quad visitor for processing each quad
	final QuadVisitor qv = new MyQuadVisitor(S);

	// make initial worklist from roots collection.
	initWorkList(S, roots);

	// worklist algorithm.
	while (!S.W.isEmpty()) {
	    HMethod m = S.W.pull();
	    S.done.add(m); // mark us done with this method.

	    // Safety check: this method should be marked as usable.
	    assert checkIsMarkedAsUsed(S, m);

	    // look at the hcode for the method.
	    harpoon.IR.Quads.Code hc = (harpoon.IR.Quads.Code) hcf.convert(m);

	    if (hc == null) { // native or unanalyzable method.
		if(!m.getReturnType().isPrimitive() &&
		   !m.getReturnType().isInterface())
		    // be safe; assume the native method can make an object
		    // of its return type.
		    discoverInstantiatedClass(S, m.getReturnType());
	    }
	    else { // look for CALLs, NEWs, and ANEWs
		// iterate through quads with visitor.
		for (Iterator<Quad> it = hc.getElementsI(); it.hasNext(); )
		    it.next().accept(qv);
	    }
	} // END worklist.
	
	// build method table from classMethodsUsed.
	for (Set<HMethod> set : S.classMethodsUsed.values()) {
	    methods.addAll(set);
	}

	// now generate children set from classKnownChildren.
	for (HClass c : S.classKnownChildren.keySet()) {
	    Set<HClass> s = S.classKnownChildren.get(c);
	    HClass ch[] = s.toArray(new HClass[s.size()]);
	    children.put(c, ch);
	}
    }


    // executed just for assertions; by calling this method in an
    // assertion condition, we don't pay its cost if the assertions
    // are off
    private boolean checkIsMarkedAsUsed(State S, HMethod m) {
	HClass hc = m.getDeclaringClass();
	Set<HMethod> s = S.classMethodsUsed.get(hc);
	assert s!=null : "NULL classMethodsUsed(declClass=" + hc + ") for m=" + m;
	assert s.contains(m) : "m NOT IN classMethodsUsed(declClass=" + hc + ") for m=" + m;
	return true;
    }


    // quad visitor used in the main worklist algorithm, while
    // processing the instructions of each method.
    private class MyQuadVisitor extends QuadVisitor {
	MyQuadVisitor(State S) { this.S = S; }
	private final State S;

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
	    if (q.type().getName().equals("java.lang.String"))
	    // string constants use intern()
	    discoverMethod(S,HMstrIntern,false/*non-virtual*/);
	    if (q.type().getName().equals("java.lang.Class")) {
		discoverClass(S, (HClass) q.value());
	    }
	    if (q.type().getName().equals("java.lang.reflect.Field")) {
		discoverClass(S, ((HField) q.value()).getDeclaringClass());
	    }
	    if (q.type().getName().equals("java.lang.reflect.Method")) {
		discoverClass(S, ((HMethod) q.value()).getDeclaringClass());
	    }
	}
	// CALLs:
	public void visit(CALL q) {
	    if (q.isStatic() || !q.isVirtual()) {
		discoverMethod(S, q.method(),false/*non-virtual*/);
	    }
	    else {
		discoverMethod(S, q.method(),true/*virtual*/);
	    }
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
	    for (int i=0; i<q.keysLength(); i++) {
		discoverClass(S, q.keys(i));
	    }
	}
	public void visit(HANDLER q) {
	    if (q.caughtException()==null) return;
	    discoverClass(S, q.caughtException());
	}
    };
    


    /* when we discover a new class nc:
        for each superclass c or superinterface i of this class,
         add all called methods of c/i to worklist of nc, if nc implements.
    */
    private void discoverClass(State S, HClass c) {
	if (S.classKnownChildren.containsKey(c)) return; // not a new class.

	// add to known-children lists.
	assert !S.classKnownChildren.containsKey(c);
	S.classKnownChildren.put(c, new HashSet());
	assert !S.classMethodsUsed.containsKey(c);
	S.classMethodsUsed.put(c, new HashSet());
	assert !S.classMethodsPending.containsKey(c);
	S.classMethodsPending.put(c, new HashSet());

	// add class initializer (if it exists) to "called" methods.
	HMethod ci = c.getClassInitializer();
	if ((ci!=null) && (!S.done.contains(ci)))
	    methodPush(S, ci);

	// mark component type of arrays
	if (c.isArray())
	    discoverClass(S, c.getComponentType());

	// work through parents (superclass and interfaces)
	for (Object pO : parents(c)) {
	    HClass p = (HClass) pO;
	    discoverClass(S, p);
	    Set<HClass> knownChildren = S.classKnownChildren.get(p);
	    knownChildren.add(c); // kC non-null after discoverClass.
	}
    }

    private void discoverInstantiatedClass(State S, HClass c) {
	if(!instedClasses.add(c)) {
	    // already seen class -> return
	    return;
	}
	discoverClass(S, c);

	// collect superclasses and interfaces.
	// new worklist.
	WorkSet<HClass> sW = new WorkSet<HClass>();
	// put superclass and interfaces on worklist.
	sW.addAll(parents(c));

	// first, wake up all methods of this class that were
	// pending instantiation, and clear the pending list.
	List<HMethod> ml =
	    new ArrayList<HMethod>(S.classMethodsPending.get(c));//copy list
	for (HMethod hm : ml) {
	    methodPush(S, hm);
	}

	// if instantiated,
	// add all called methods of superclasses/interfaces to worklist.
	while (!sW.isEmpty()) {
	    // pull a superclass or superinterface off the list.
	    HClass s = sW.pop();
	    // add superclasses/interfaces of this one to local worklist
	    sW.addAll(parents(s));
	    // now add called methods of s to top-level worklist.
	    Set<HMethod> calledMethods =
		new WorkSet<HMethod>(S.classMethodsUsed.get(s));
	    calledMethods.addAll(S.classMethodsPending.get(s));
	    for (HMethod m : calledMethods){
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
	if (!S.classMethodsUsed.get(m.getDeclaringClass()).contains(m))
	    S.classMethodsPending.get(m.getDeclaringClass()).add(m);
	// now add as 'used' to all instantiated children.
	// (including itself, if its own class has been instantiated)
	WorkSet<HClass> cW = new WorkSet<HClass>();
	cW.push(m.getDeclaringClass());
	while (!cW.isEmpty()) {
	    // pull a class from the worklist
	    HClass c = cW.pull();
	    // see if we should add method-of-c to method worklist.
	    if (c.isInterface() || instedClasses.contains(c))
		try {
		    HMethod nm = c.getMethod(m.getName(),
					     m.getDescriptor());
		    assert isVirtual; // shouldn't be here otherwise.
		    if (!S.done.contains(nm))
			methodPush(S, nm);
		    else if (!S.nonvirtual.contains(nm))
			continue; // nothing new to discover.
		    else S.nonvirtual.remove(nm); // since we're virtual here.
		} catch (NoSuchMethodError e) { }
	    // add all children to the worklist.
	    Set<HClass> knownChildren = S.classKnownChildren.get(c);
	    for (Iterator<HClass> it=knownChildren.iterator(); it.hasNext(); )
		cW.push(it.next());
	}
	// done.
    }

    private void methodPush(State S, HMethod m) {
	assert !S.done.contains(m);
	if (S.W.contains(m)) return; // already on work list.

	// Add to worklist
	S.W.add(m);
	// mark this method as used.
	Set<HMethod> s1 = S.classMethodsUsed.get(m.getDeclaringClass());
	s1.add(m);
	// and no longer pending.
	Set<HMethod> s2 = S.classMethodsPending.get(m.getDeclaringClass());
	s2.remove(m);
    }



    // State for the algorithm.
    private static class State {

	// for each class, keeps track of methods which are actually invoked at some point.
	final Map<HClass,Set<HMethod>> classMethodsUsed
	    = new HashMap<HClass,Set<HMethod>>(); // class->set map.

	// keeps track of methods which might be called, if someone gets
	// around to instantiating an object of the proper type.
	final Map<HClass,Set<HMethod>> classMethodsPending
	    = new HashMap<HClass,Set<HMethod>>(); // class->set map

	// keeps track of all known children of a given class.
	final Map<HClass,Set<HClass>> classKnownChildren
	    = new HashMap<HClass,Set<HClass>>(); // class->set map.

	// keeps track of which methods we've done already.
	final Set<HMethod> done = new HashSet<HMethod>();

	// keeps track of methods we've only seen non-virtual invocations for.
	final Set<HMethod> nonvirtual = new HashSet<HMethod>();

	// Worklist.
	final WorkSet<HMethod> W = new WorkSet<HMethod>();
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
