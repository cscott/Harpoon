// ClassHierarchy.java, created Sun Oct 11 13:08:31 1998 by cananian
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.IR.QuadSSA.*;
import harpoon.Util.Set;
import harpoon.Util.Util;
import harpoon.Util.Worklist;

import java.util.Hashtable;
import java.util.Enumeration;
/**
 * <code>ClassHierarchy</code> computes the class hierarchy of *reachable*
 * classes; that is, classes possibly usable starting from some root method.
 * Native methods are not analyzed.
 *
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: ClassHierarchy.java,v 1.3 1998-10-12 10:11:06 cananian Exp $
 */

public class ClassHierarchy  {
    private Hashtable children = new Hashtable();

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
    /** Returns an enumeration of all reachable/usable classes. */ 
    public Enumeration classes() {
	if (_classes == null) {
	    _classes = new Set();
	    for (Enumeration e = children.keys(); e.hasMoreElements(); )
		_classes.union(e.nextElement());
	    for (Enumeration e = children.elements(); e.hasMoreElements(); ){
		HClass[] ch = (HClass[]) e.nextElement();
		for (int i=0; i<ch.length; i++)
		    _classes.union(ch[i]);
	    }
	}
	return _classes.elements();
    }
    private Set _classes = null;

    /** Returns a human-readable representation of the hierarchy. */
    public String toString() {
	// not the most intuitive representation...
	StringBuffer sb = new StringBuffer("{");
	for (Enumeration e = children.keys(); e.hasMoreElements(); ) {
	    HClass c = (HClass) e.nextElement();
	    sb.append(c.toString());
	    sb.append("={");
	    HClass cc[] = children(c);
	    for (int i=0; i<cc.length; i++) {
		sb.append(cc[i].toString());
		if (i<cc.length-1)
		    sb.append(',');
	    }
	    sb.append('}');
	    if (e.hasMoreElements())
		sb.append(", ");
	}
	sb.append('}');
	return sb.toString();
    }

    /** Creates a <code>ClassHierarchy</code> of all classes
     *  reachable/usable from method <code>root</code>. */
    public ClassHierarchy(HMethod root) {
	// state.
	Hashtable classMethodsUsed = new Hashtable(); // hash of sets.
	Hashtable classKnownChildren = new Hashtable(); // hash of sets
	Set done = new Set(); // all methods done.

	// worklist algorithm.
	Worklist W = new Set();
	methodPush(root, W, classMethodsUsed);
	discoverClass(root.getDeclaringClass(), W, done,
		      classKnownChildren, classMethodsUsed);
	while (!W.isEmpty()) {
	    HMethod m = (HMethod) W.pull();
	    done.union(m); // mark us done with this method.
	    // This method should be marked as usable.
	    {
		Set s = (Set) classMethodsUsed.get(m.getDeclaringClass());
		Util.assert(s!=null);
		Util.assert(s.contains(m));
	    }
	    // look at the hcode for the method.
	    HCode hc = m.getCode("quad-ssa");
	    if (hc==null) { // native or unanalyzable method.
		if(!m.getReturnType().isPrimitive())
		    discoverClass(m.getReturnType(), W, done,
				  classKnownChildren, classMethodsUsed);
	    } else { // look for CALLs and NEWs
		for (Enumeration e = hc.getElementsE();
		     e.hasMoreElements(); ) {
		    Quad Q = (Quad) e.nextElement();
		    if (Q instanceof NEW) {
			NEW q = (NEW) Q;
			discoverClass(q.hclass, W, done,
				      classKnownChildren, classMethodsUsed);
		    }
		    if (Q instanceof CALL) {
			CALL q = (CALL) Q;
			if (q.isSpecial)
			    discoverSpecial(q.method, W, done,
					    classKnownChildren,
					    classMethodsUsed);
			else
			    discoverMethod(q.method, W, done,
					   classKnownChildren,
					   classMethodsUsed);
		    }
		}
	    }
	} // END worklist.
	
	// now generate children set from classKnownChildren.
	for (Enumeration e = classKnownChildren.keys(); e.hasMoreElements();) {
	    HClass c = (HClass) e.nextElement();
	    Set s = (Set) classKnownChildren.get(c);
	    HClass ch[] = new HClass[s.size()];
	    s.copyInto(ch);
	    children.put(c, ch);
	}
    }

    /* when we discover a new class nc:
        for each superclass c or superinterface i of this class,
         add all called methods of c/i to worklist of nc, if nc implements.
    */
    private void discoverClass(HClass c, 
		       Worklist W, Set done, Hashtable ckc, Hashtable cmu) {
	if (ckc.containsKey(c)) return; // not a new class.
	// add to known-children lists.
	ckc.put(c, new Set());
	// new worklist.
	Worklist sW = new Set();
	// mark superclass.
	HClass su = c.getSuperclass();
	if (su!=null) { // maybe discover super class?
	    discoverClass(su, W, done, ckc, cmu);
	    sW.push(su);
	    Set knownChildren = (Set) ckc.get(su);
	    knownChildren.union(c); // kC non-null after discoverClass.
	}
	// mark interfaces
	HClass in[] = c.getInterfaces();
	for (int i=0; i<in.length; i++) {
	    discoverClass(in[i], W, done, ckc, cmu); // discover interface?
	    sW.push(in[i]);
	    Set knownChildren = (Set) ckc.get(in[i]);
	    knownChildren.union(c); // kC non-null after discoverClass.
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
	    for (Enumeration e = calledMethods.elements();
		 e.hasMoreElements(); ) {
		HMethod m = (HMethod) e.nextElement();
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
			Worklist W, Set done, Hashtable ckc, Hashtable cmu) {
	Worklist cW = new Set();
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
	    for (Enumeration e = knownChildren.elements();
		 e.hasMoreElements(); )
		cW.push(e.nextElement());
	}
	// done.
    }
    /* methods invoked with INVOKESPECIAL... */
    private void discoverSpecial(HMethod m, 
			 Worklist W, Set done, Hashtable ckc, Hashtable cmu) {
	if (!done.contains(m))
	    methodPush(m, W, cmu);
    }
    private void methodPush(HMethod m, Worklist W, Hashtable cmu) {
	// Add to worklist
	W.push(m);
	// mark this method as usable.
	Set s = (Set) cmu.get(m.getDeclaringClass());
	if (s==null) { 
	    s = new Set(); 
	    cmu.put(m.getDeclaringClass(),s);
	}
	s.union(m);
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
