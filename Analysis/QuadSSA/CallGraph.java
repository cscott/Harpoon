// CallGraph.java, created Sun Oct 11 12:56:36 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.QuadSSA;

import harpoon.ClassFile.*;
import harpoon.IR.Quads.*;
import harpoon.Util.Set;
import harpoon.Util.HashSet;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
/**
 * <code>CallGraph</code> constructs a simple directed call graph.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CallGraph.java,v 1.4.2.7 1999-09-08 16:35:19 cananian Exp $
 */

public class CallGraph  {
    final HCodeFactory hcf;
    final ClassHierarchy ch;
    /** Creates a <code>CallGraph</code> using the specified 
     *  <code>ClassHierarchy</code>. <code>hcf</code> must be a code
     *  factory that generates quad-ssi or quad-no-ssa form. */
    public CallGraph(ClassHierarchy ch, HCodeFactory hcf) {
	// this is maybe a little too draconian
	Util.assert(hcf.getCodeName()
		    .equals(harpoon.IR.Quads.QuadSSI.codename) ||
		    hcf.getCodeName()
		    .equals(harpoon.IR.Quads.QuadNoSSA.codename));
	this.ch = ch;
	this.hcf = hcf;
    }
    
    /** Return a list of all possible methods called by this method. */
    public HMethod[] calls(final HMethod m) {
	HMethod[] retval = (HMethod[]) cache.get(m);
	if (retval==null) {
	    final Set s = new HashSet();
	    final HCode hc = hcf.convert(m);
	    if (hc==null) { cache.put(m,new HMethod[0]); return calls(m); }
	    for (Enumeration e = hc.getElementsE(); e.hasMoreElements(); ) {
		Quad q = (Quad) e.nextElement();
		if (!(q instanceof CALL)) continue;
		HMethod cm = ((CALL)q).method();
		if (s.contains(cm)) continue; // duplicate.
		// for 'special' invocations, we know the class exactly.
		if ((!((CALL)q).isVirtual()) || ((CALL)q).isStatic()) {
		    s.union(cm);
		    continue;
		}
		// all methods of children of this class are reachable.
		Worklist W = new HashSet();
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

    /** Return a list of all possible methods called by this method at this call site. */
    public HMethod[] calls(final HMethod m, final CALL cs) {
	final HCode hc = hcf.convert(m);
	if (hc==null) { return new HMethod[0]; }
	HMethod cm = cs.method();
	// for 'special' invocations, we know the method exactly.
	if ((!cs.isVirtual()) || cs.isStatic()) return new HMethod[]{ cm };
	final Set s = new HashSet();
	// all methods of children of this class are reachable.
	Worklist W = new HashSet();
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
	// finally, copy result vector to retval array.
	HMethod[] retval = new HMethod[s.size()];
	s.copyInto(retval);
	return retval;
    }

}
