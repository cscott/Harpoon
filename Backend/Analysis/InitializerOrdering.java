// InitializerOrdering.java, created Thu Oct 14 12:42:50 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Analysis;

import harpoon.Analysis.CallGraph;
import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HInitializer;
import harpoon.ClassFile.HMethod;
import net.cscott.jutil.UniqueVector;
import harpoon.Util.Util;
import net.cscott.jutil.WorkSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
/**
 * <code>InitializerOrdering</code> computes a topological sort of
 * the static initializer call graph designed to ensure that
 * classes are initialized in the correct order.  Cycles in
 * the initializer call graph will emit warnings; executing the
 * static initializers in the order given is not guaranteed
 * to produce the same results as executing them on demand
 * in that case.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: InitializerOrdering.java,v 1.5 2004-02-08 01:57:24 cananian Exp $
 */
public class InitializerOrdering {
    public final List sorted;
    /** Creates a <code>InitializerOrdering</code>. */
    public InitializerOrdering(ClassHierarchy ch, CallGraph cg) {
	List _sorted = new ArrayList();
	Set touched = new HashSet(), added = new HashSet();
        for (Iterator it=ch.classes().iterator(); it.hasNext(); ) {
	    HClass hc = (HClass) it.next();
	    if (!added.contains(hc))
		examineClass(ch, cg, hc, touched, added, _sorted);
	}
	this.sorted = Collections.unmodifiableList(_sorted);
    }
    private void examineClass(ClassHierarchy ch, CallGraph cg, HClass c,
			      Set touched, Set added, List _sorted) {
	assert ch.classes().contains(c) : ("Being examined, although it's not in hierarchy:"+c);
	assert !added.contains(c);
	assert !touched.contains(c);
	touched.add(c);
	// first, all superclass initializers must be called.
	for (HClass hcp=c.getSuperclass(); hcp!=null; hcp=hcp.getSuperclass())
	    if (!touched.contains(hcp))
		examineClass(ch, cg, hcp, touched, added, _sorted);
	    else if (!added.contains(hcp))
		warnCircle(hcp);
	// now, all methods called by this static initializer must be examined
	HMethod m = c.getClassInitializer();
	if (m!=null && !touched.contains(m))
	    examineMethod(ch, cg, c, m, touched, added, _sorted);
	// okay, all dependencies have been added, so now add this initializer
	if (m!=null) _sorted.add(m);
	added.add(c);
    }
    private void examineMethod(ClassHierarchy ch, CallGraph cg, 
			       HClass initClass, HMethod m,
			       Set touched, Set added, List _sorted) {
	assert ch.callableMethods().contains(m) : ("Being examined, although it's not callable:"+m);
	assert !touched.contains(m);
	touched.add(m);
	// first check that the class containing this method has been init'ed.
	// this class is *being* init'ed if m is an initializer, so skip then.
	HClass hc = m.getDeclaringClass();
	if (hc != initClass) { // we're already initializing this class.
	    if (!touched.contains(hc))
		examineClass(ch, cg, hc, touched, added, _sorted);
	    else if (!added.contains(hc))
		warnCircle(hc);
	}
	// now recursively invoke all the methods which this guy calls.
	HMethod[] deps = cg.calls(m);
	for (int i=0; i<deps.length; i++) {
	    if (!touched.contains(deps[i]))// could be recursive functions here
		examineMethod(ch, cg, initClass, deps[i],
			      touched, added, _sorted);
	}
	// XXX: ACCESSED FIELDS ALSO TRIGGER INITIALIZERS!
	// okay, done looking at this method.
    }
    private void warnCircle(HClass hc) {
	System.err.println("WARNING: circular dependency on " +
			   hc.getName() + " in static initializers.");
    }
}
