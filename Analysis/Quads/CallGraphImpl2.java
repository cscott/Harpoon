// CallGraphImpl.java, created Sun Oct 11 12:56:36 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.Analysis.Maps.ExactTypeMap;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.SSxReachingDefsImpl;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.CALL;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

/**
 * <code>CallGraphImpl2</code> constructs a simple directed call graph.
 * This version pays closer attention to types for a more precise
 * result.  To quantify this a bit, the standard <code>CallGraphImpl</code>
 * yields a 510-node call graph on the "Hello, World" program, compared
 * to the 370-node graph that <code>CallGraphImpl2</code> gives you.
 * <p>
 * <code>CallGraphImpl2</code> only works on SSI form.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CallGraphImpl2.java,v 1.4 2002-04-10 03:00:59 cananian Exp $
 */
public class CallGraphImpl2 implements harpoon.Analysis.CallGraph  {
    final HCodeFactory hcf;
    final ClassHierarchy ch;
    /** Creates a <code>CallGraph</code> using the specified 
     *  <code>ClassHierarchy</code>. <code>hcf</code> must be a code
     *  factory that generates quad-ssi form. */
    public CallGraphImpl2(ClassHierarchy ch, HCodeFactory hcf) {
	// this is maybe a little too draconian
	assert hcf.getCodeName()
		    .equals(harpoon.IR.Quads.QuadSSI.codename);
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
	    ExactTypeMap etm = new TypeInfo((harpoon.IR.Quads.QuadSSI)hc);
	    ReachingDefs rd = new SSxReachingDefsImpl(hc);
	    for (Iterator it=getCallSites(m, hc).iterator(); it.hasNext(); ) {
		CALL q = (CALL) it.next();
		HMethod cm = q.method();
		if (s.contains(cm)) continue; // duplicate.
		s.addAll(Arrays.asList(calls((CALL)q, rd, etm)));
	    }
	    // finally, copy result vector to retval array.
	    retval = (HMethod[]) s.toArray(new HMethod[s.size()]);
	    // and cache result.
	    cache.put(m, retval);
	}
	return retval;
    }
    final private Map cache = new HashMap();

    /** Returns a List of all the <code>CALL</code>s quads in the code 
	of <code>hm</code> (in representation <code>hc</code>). */
    private List getCallSites(final HMethod hm, final HCode hc){
	if (hc==null) return Collections.EMPTY_LIST;
	assert hc.getMethod().equals(hm);
	if (cache_cs.containsKey(hc)) return (List) cache_cs.get(hc);

	final Vector v = new Vector();
	for (Iterator it = hc.getElementsI(); it.hasNext(); ) {
	    Quad q = (Quad) it.next();
	    if (q instanceof CALL) v.add(q);
	}
	cache_cs.put(hc, Collections.unmodifiableList(v));
	return getCallSites(hm, hc);
    }
    final private Map cache_cs = new HashMap();

    /** Return a list of all possible methods called by this method at
     *  a particular call site. */
    public HMethod[] calls(final CALL cs,
			   ReachingDefs rd, ExactTypeMap etm) {
	HMethod cm = cs.method();
	// for 'special' invocations, we know the method exactly.
	if ((!cs.isVirtual()) || cs.isStatic()) return new HMethod[]{ cm };
	// find type of receiver
	Temp receiver = cs.params(0);
	Quad def = (Quad) rd.reachingDefs(cs, receiver).iterator().next();
	HClass rtype = etm.typeMap(def, receiver);
	// if the type is exact, we know the method exactly.
	if (etm.isExactType(def, receiver))
	    try {
		return new HMethod[]
		    { rtype.getMethod(cm.getName(),cm.getDescriptor()) };
	    } catch (NoSuchMethodError nsme) {//hm, no matching method.
		return new HMethod[0];
	    }
	// otherwise, compute set of methods:
	final Set s = new HashSet();
	// all methods of the type and its children are reachable.
	WorkSet W = new WorkSet();
	W.add(rtype);
	while (!W.isEmpty()) {
	    HClass c = (HClass) W.pop();
	    // if this class can be instatiated, then its
	    // implementation of the method should be added to the set.
	    if (ch.instantiatedClasses().contains(c))
		try {
		    s.add(c.getMethod(cm.getName(),
				      cm.getDescriptor()));
		} catch (NoSuchMethodError nsme) { }
	    // recurse through all children of this method's class.
	    for (Iterator it=ch.children(c).iterator(); it.hasNext(); )
		W.add(it.next());
	}
	// finally, copy result vector to retval array.
	return (HMethod[]) s.toArray(new HMethod[s.size()]);
    }

    /** Returns the set of all the methods that can be called in the 
	execution of the program. */
    public Set callableMethods(){
	return ch.callableMethods();
    }

}
