// CallGraph.java, created Sun Oct 11 12:56:36 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>CallGraph</code> constructs a simple directed call graph.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CallGraph.java,v 1.1.2.2 1999-10-14 19:59:04 cananian Exp $
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
	    for (Iterator it = hc.getElementsI(); it.hasNext(); ) {
		Quad q = (Quad) it.next();
		if (!(q instanceof CALL)) continue;
		HMethod cm = ((CALL)q).method();
		if (s.contains(cm)) continue; // duplicate.
		// for 'special' invocations, we know the class exactly.
		if ((!((CALL)q).isVirtual()) || ((CALL)q).isStatic()) {
		    s.add(cm);
		    continue;
		}
		// all methods of children of this class are reachable.
		WorkSet W = new WorkSet();
		W.add(cm.getDeclaringClass());
		while (!W.isEmpty()) {
		    HClass c = (HClass) W.pop();
		    // if this class overrides the method, add it to vector.
		    try {
			s.add(c.getDeclaredMethod(cm.getName(),
						    cm.getDescriptor()));
		    } catch (NoSuchMethodError nsme) { }
		    // recurse through all children of this method.
		    for (Iterator it2=ch.children(c).iterator();it2.hasNext();)
			W.add(it2.next());
		}
	    }
	    // finally, copy result vector to retval array.
	    retval = (HMethod[]) s.toArray(new HMethod[s.size()]);
	    // and cache result.
	    cache.put(m, retval);
	}
	return retval;
    }
    final private Map cache = new HashMap();

    /** Return a list of all possible methods called by this method at this call site. */
    public HMethod[] calls(final HMethod m, final CALL cs) {
	final HCode hc = hcf.convert(m);
	if (hc==null) { return new HMethod[0]; }
	HMethod cm = cs.method();
	// for 'special' invocations, we know the method exactly.
	if ((!cs.isVirtual()) || cs.isStatic()) return new HMethod[]{ cm };
	final Set s = new HashSet();
	// all methods of children of this class are reachable.
	WorkSet W = new WorkSet();
	W.add(cm.getDeclaringClass());
	while (!W.isEmpty()) {
	    HClass c = (HClass) W.pop();
	    // if this class overrides the method, add it to vector.
	    try {
		s.add(c.getDeclaredMethod(cm.getName(),
					    cm.getDescriptor()));
	    } catch (NoSuchMethodError nsme) { }
	    // recurse through all children of this method.
	    for (Iterator it=ch.children(c).iterator(); it.hasNext(); )
		W.add(it.next());
	}
	// finally, copy result vector to retval array.
	return (HMethod[]) s.toArray(new HMethod[s.size()]);
    }
}
