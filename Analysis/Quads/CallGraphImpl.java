// CallGraphImpl.java, created Sun Oct 11 12:56:36 1998 by cananian
// Copyright (C) 1998 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadKind;
import harpoon.IR.Quads.CALL;
import harpoon.IR.Quads.Code;
import harpoon.Util.Util;
import harpoon.Util.Collections.WorkSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * <code>CallGraphImpl</code> constructs a simple directed call graph.
 This is the most conservative implementation of <code>CallGraph</code>.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: CallGraphImpl.java,v 1.6 2002-04-11 04:28:49 salcianu Exp $
 */
public class CallGraphImpl extends AbstrCallGraph  {
    //final HCodeFactory hcf;
    final ClassHierarchy ch;
    /** Creates a <code>CallGraph</code> using the specified 
     *  <code>ClassHierarchy</code>. <code>hcf</code> must be a code
     *  factory that generates quad-ssi or quad-no-ssa form. */
    public CallGraphImpl(ClassHierarchy ch, HCodeFactory hcf) {
	// this is maybe a little too draconian
	assert hcf.getCodeName()
		    .equals(harpoon.IR.Quads.QuadSSI.codename) ||
		    hcf.getCodeName()
		    .equals(harpoon.IR.Quads.QuadSSA.codename) ||
		    hcf.getCodeName()
		    .equals(harpoon.IR.Quads.QuadNoSSA.codename);
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
		s.addAll(Arrays.asList(calls(m, (CALL)q)));
	    }
	    // finally, copy result vector to retval array.
	    retval = (HMethod[]) s.toArray(new HMethod[s.size()]);
	    // and cache result.
	    cache.put(m, retval);
	}
	return retval;
    }
    final private Map cache = new HashMap();

    /** Return an array containing all possible methods called by this
     *  method at a particular call site. */
    public HMethod[] calls(final HMethod m, final CALL cs) {
	HMethod cm = cs.method();
	// for 'special' invocations, we know the method exactly.
	if ((!cs.isVirtual()) || cs.isStatic()) return new HMethod[]{ cm };
	final Set s = new HashSet();
	// all methods of children of this class are reachable.
	WorkSet W = new WorkSet();
	W.add(cm.getDeclaringClass());
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
