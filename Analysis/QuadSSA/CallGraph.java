// CallGraph.java, created Sun Oct 11 12:56:36 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Util.Set;
import harpoon.Util.Worklist;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
/**
 * <code>CallGraph</code> constructs a simple directed call graph.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CallGraph.java,v 1.4.2.1 1998-12-01 12:36:26 cananian Exp $
 */

public class CallGraph  {
    final ClassHierarchy ch;
    /** Creates a <code>CallGraph</code> using the specified 
     *  <code>ClassHierarchy</code>. */
    public CallGraph(ClassHierarchy ch) { this.ch = ch; }
    
    /** Return a list of all possible methods called by this method. */
    public HMethod[] calls(final HMethod m) {
	HMethod[] retval = (HMethod[]) cache.get(m);
	if (retval==null) {
	    final Set s = new Set();
	    final HCode hc = m.getCode("quad-ssa");
	    if (hc==null) {cache.put(m,new HMethod[0]); return calls(m); }
	    for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
		Quad q = (Quad) e.nextElement();
		if (!(q instanceof CALL)) continue;
		HMethod cm = ((CALL)q).method;
		if (s.contains(cm)) continue; // duplicate.
		// for 'special' invocations, we know the class exactly.
		if (((CALL)q).isSpecial) {
		    s.union(cm);
		    continue;
		}
		// all methods of children of this class are reachable.
		Worklist W = new Set();
		W.push(cm.getDeclaringClass());
		while (!W.isEmpty()) {
		    HClass c = (HClass) W.pull();
		    // if this class overrides the method, add it to vector.
		    try {
			s.union(c.getDeclaredMethod(cm.getName(),
						    cm.getDescriptor()));
		    } catch (NoSuchMethodError nsme) { }
		    // recurse through all children of this method.
		    HClass[] child = ch.children(c);
		    for (int i=0; i<child.length; i++)
			W.push(child[i]);
		}
	    }
	    // finally, copy result vector to retval array.
	    retval = new HMethod[s.size()];
	    s.copyInto(retval);
	    // and cache result.
	    cache.put(m, retval);
	}
	return retval;
    }
    final private Hashtable cache = new Hashtable();
}
