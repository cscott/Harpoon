// ClassHierarchy.java, created Sun Oct 11 13:08:31 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.WorkSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>ClassHierarchy</code> computes the class hierarchy of *reachable*
 * classes; that is, classes possibly usable starting from some root method.
 * Native methods are not analyzed.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassHierarchy.java,v 1.4.2.7 1999-08-07 02:13:42 cananian Exp $
 */

public class ClassHierarchy implements java.io.Serializable {
    private Map children = new HashMap();

    /** Returns all usable/reachable children of an HClass. */
    public HClass[] children(HClass c) {
	if (children.containsKey(c))
	    return (HClass[]) children.get(c);
	return new HClass[0];
    }
    /** Returns the parent of an HClass. */
    public HClass parent(HClass c) {
	return c.getSuperclass();
    }
    /** Returns the set of all reachable/usable classes. */ 
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
	}
	return Collections.unmodifiableSet(_classes);
    }
    private transient Set _classes = null;
    /** Returns the set of all classes instantiated.
	(Actually only the list of classes for which an explicit NEW is found;
	should include list of classes that are automatically created by JVM!) */ 
    public Set instantiatedClasses() {
	return Collections.unmodifiableSet(instedClasses);
    }
    private Set instedClasses = new HashSet();

    /** Returns a human-readable representation of the hierarchy. */
    public String toString() {
	// not the most intuitive representation...
	StringBuffer sb = new StringBuffer("{");
	for (Iterator it = children.keySet().iterator(); it.hasNext(); ) {
	    HClass c = (HClass) it.next();
	    sb.append(c.toString());
	    sb.append("={");
	    HClass cc[] = children(c);
	    for (int i=0; i<cc.length; i++) {
		sb.append(cc[i].toString());
		if (i<cc.length-1)
		    sb.append(',');
	    }
	    sb.append('}');
	    if (it.hasNext())
		sb.append(", ");
	}
	sb.append('}');
	return sb.toString();
    }

    /** Creates a <code>ClassHierarchy</code> of all classes
     *  reachable/usable from method <code>root</code>.  <code>hcf</code>
     *  must be a code factory that generates quad form. */
    public ClassHierarchy(HMethod root, HCodeFactory hcf) {
	// state.
	Map classMethodsUsed = new HashMap(); // class->set map.
	Map classKnownChildren = new HashMap(); // class->set map.
	Set done = new HashSet(); // all methods done.

	// worklist algorithm.
	Worklist W = new WorkSet();
	methodPush(root, W, classMethodsUsed);
	discoverClass(root.getDeclaringClass(), W, done,
		      classKnownChildren, classMethodsUsed);
	while (!W.isEmpty()) {
	    HMethod m = (HMethod) W.pull();
	    done.add(m); // mark us done with this method.
	    // This method should be marked as usable.
	    {
		Set s = (Set) classMethodsUsed.get(m.getDeclaringClass());
		Util.assert(s!=null);
		Util.assert(s.contains(m));
	    }
	    // look at the hcode for the method.
	    harpoon.IR.Quads.Code hc = (harpoon.IR.Quads.Code) hcf.convert(m);
	    if (hc==null) { // native or unanalyzable method.
		if(!m.getReturnType().isPrimitive())
		    discoverClass(m.getReturnType(), W, done,
				  classKnownChildren, classMethodsUsed);
	    } else { // look for CALLs and NEWs
		for (Iterator it = hc.getElementsI(); it.hasNext(); ) {
		    Quad Q = (Quad) it.next();
		    if (Q instanceof NEW) {
			NEW q = (NEW) Q;
			instedClasses.add(q.hclass());
			discoverClass(q.hclass(), W, done,
				      classKnownChildren, classMethodsUsed);
		    }
		    if (Q instanceof CALL) {
			CALL q = (CALL) Q;
			if (q.isStatic() || !q.isVirtual())
			    discoverSpecial(q.method(), W, done,
					    classKnownChildren,
					    classMethodsUsed);
			else
			    discoverMethod(q.method(), W, done,
					   classKnownChildren,
					   classMethodsUsed);
		    }
		}
	    }
	} // END worklist.
	
	// now generate children set from classKnownChildren.
	for (Iterator it = classKnownChildren.keySet().iterator();
	     it.hasNext(); ) {
	    HClass c = (HClass) it.next();
	    Set s = (Set) classKnownChildren.get(c);
	    HClass ch[] = (HClass[]) s.toArray(new HClass[s.size()]);
	    children.put(c, ch);
	}
    }

    /* when we discover a new class nc:
        for each superclass c or superinterface i of this class,
         add all called methods of c/i to worklist of nc, if nc implements.
    */
    private void discoverClass(HClass c, 
		       Worklist W, Set done, Map ckc, Map cmu) {
	if (ckc.containsKey(c)) return; // not a new class.
	// add class initializer (if it exists) to "called" methods.
	HMethod ci = c.getClassInitializer();
	if ((ci!=null) && (!done.contains(ci))) methodPush(ci, W, cmu);
	// add to known-children lists.
	ckc.put(c, new HashSet());
	// new worklist.
	Worklist sW = new WorkSet();
	// mark superclass.
	HClass su = c.getSuperclass();
	if (su!=null) { // maybe discover super class?
	    discoverClass(su, W, done, ckc, cmu);
	    sW.push(su);
	    Set knownChildren = (Set) ckc.get(su);
	    knownChildren.add(c); // kC non-null after discoverClass.
	}
	// mark interfaces
	HClass in[] = c.getInterfaces();
	for (int i=0; i<in.length; i++) {
	    discoverClass(in[i], W, done, ckc, cmu); // discover interface?
	    sW.push(in[i]);
	    Set knownChildren = (Set) ckc.get(in[i]);
	    knownChildren.add(c); // kC non-null after discoverClass.
	}

	// add all called methods of superclasses/interfaces to worklist.
	while (!sW.isEmpty()) {
	    // pull a superclass or superinterface off the list.
	    HClass s = (HClass)sW.pull();
	    // add superclasses/interfaces of this one to local worklist
	    su = s.getSuperclass();
	    if (su!=null) sW.push(su);
	    in = s.getInterfaces();
	    for (int i=0; i<in.length; i++) sW.push(in[i]);
	    // now add called methods of s to top-level worklist.
	    Set calledMethods = (Set) cmu.get(s);
	    if (calledMethods==null) continue; // no called methods?!
	    for (Iterator it = calledMethods.iterator(); it.hasNext(); ) {
		HMethod m = (HMethod) it.next();
		try {
		    HMethod nm = c.getDeclaredMethod(m.getName(),
						     m.getDescriptor());
		    if (!done.contains(nm)) methodPush(nm, W, cmu);
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
    private void discoverMethod(HMethod m, 
			Worklist W, Set done, Map ckc, Map cmu) {
	Worklist cW = new WorkSet();
	cW.push(m.getDeclaringClass());
	while (!cW.isEmpty()) {
	    // pull a class from the worklist
	    HClass c = (HClass) cW.pull();
	    // see if we should add method-of-c to method worklist.
	    try {
		HMethod nm = c.getDeclaredMethod(m.getName(),
						 m.getDescriptor());
		if (done.contains(nm)) continue; // nothing new to discover.
		methodPush(nm, W, cmu);
	    } catch (NoSuchMethodError e) { }
	    // add all children to the worklist.
	    if (!ckc.containsKey(c)) discoverClass(c,W,done,ckc,cmu);
	    Set knownChildren = (Set) ckc.get(c);
	    for (Iterator it = knownChildren.iterator(); it.hasNext(); ) 
		cW.push(it.next());
	}
	// done.
    }
    /* methods invoked with INVOKESPECIAL or INVOKESTATIC... */
    private void discoverSpecial(HMethod m, 
			 Worklist W, Set done, Map ckc, Map cmu) {
	if (!done.contains(m))
	    methodPush(m, W, cmu);
    }
    private void methodPush(HMethod m, Worklist W, Map cmu) {
	// Add to worklist
	W.push(m);
	// mark this method as usable.
	Set s = (Set) cmu.get(m.getDeclaringClass());
	if (s==null) { 
	    s = new HashSet(); 
	    cmu.put(m.getDeclaringClass(),s);
	}
	s.add(m);
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
