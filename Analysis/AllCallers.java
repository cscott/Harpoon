// AllCallers.java, created Tue Oct 19 19:34:57 1999 by kkz
// Copyright (C) 1999 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Analysis.CallGraph;
import harpoon.Analysis.Quads.CallGraphImpl;
import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;
import java.util.Set;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Collections;

/**
 * <code>AllCallers</code> calculates the transitive closure of the dual
 * of the call graph for methods that fulfill a certain condition.
 * 
 * @author Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: AllCallers.java,v 1.5 2004-02-08 03:19:12 cananian Exp $
 */

public class AllCallers {
    final Set/*<HMethod>*/ callableMethods;
    final Hashtable g;

    /** Creates an <code>AllCallers</code> object using the specified
     *  <code>ClassHierarchy</code>. Currently, <code>hcf</code> must be a
     *  code factory that generates quad-no-ssa or quad-ssi form because
     *  the dual of the call graph is built using <code>CallGraph</code>.
     */
    public AllCallers(ClassHierarchy ch, HCodeFactory hcf) {
	this(new CallGraphImpl(ch, hcf));
    }

    /** Creates an <code>AllCallers</code> object that is the dual of
        the callgraph <code>cg</code> (of type
        <code>harpoon.Analysis.Quads.CallGraoh</code>). */
    public AllCallers(CallGraph cg) {
	this.callableMethods = cg.callableMethods();
	this.g = buildGraph(cg);
    }

    /** <code>getCallers</code> returns a <code>Set</code> that contains
     *  all indirect and direct callers of callable methods that fulfill 
     *  the predicate in the <code>select</code> method of <code>ms</code>.
     */
    public Set getCallers(MethodSet ms) {
	Worklist toadd = new WorkSet();
	for (Object mO : callableMethods) {
	    HMethod m = (HMethod) mO;
	    if (ms.select(m)) {
		toadd.push(m);
	    }
	}
	Set retval = new WorkSet();
	while(!toadd.isEmpty()) {
	    HMethod callee = (HMethod)toadd.pull();
	    retval.add(callee);
	    if (this.g.containsKey(callee)) {
		for (Object callerO : ((WorkSet)this.g.get(callee))) {
		    HMethod caller = (HMethod) callerO;
		    if (!retval.contains(caller))
			toadd.push(caller);
		}
	    }
	}
	return retval;
    }

    /** <code>MethodSet</code> defines the interface whose method
     *  <code>select</code> is used in <code>getCallers</code> as a
     *  predicate.
     */
    public static interface MethodSet {
	boolean select(HMethod hm);
    }

    /** Builds the dual of the call graph using the 
     *  <code>ClassHierarchy</code> and <code>HCodeFactory</code>
     *  with which the <code>this</code> object was created.
     *  Returns the call graph as a Hashtable.
     */
    private Hashtable buildGraph(CallGraph cg) {
	Hashtable ht = new Hashtable();
	for (Object mO : callableMethods) {
	    HMethod m = (HMethod) mO;
	    HMethod[] callees = cg.calls(m);
	    for (int i = 0; i < callees.length; i++) {
		if (!ht.containsKey(callees[i]))
		    ht.put(callees[i], new WorkSet());
		((WorkSet)ht.get(callees[i])).add(m);
	    }
	}
	return ht;
    }

    /** Returns all the direct callers of the <code>hm</code> method. */
    public HMethod[] directCallers(HMethod hm){
	WorkSet wset = (WorkSet)g.get(hm);
	if(wset==null) return new HMethod[0];
	return (HMethod[]) (wset.toArray(new HMethod[wset.size()]));
    }

    /** Returns all the direct callers of the <code>hm</code> method. */
    public Set directCallerSet(HMethod hm){
	WorkSet wset = (WorkSet)g.get(hm);
	if(wset==null) return Collections.EMPTY_SET;
	return wset;
    }

}







