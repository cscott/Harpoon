// AllCallers.java, created Tue Oct 19 19:34:57 1999 by kkz
// Copyright (C) 1999  Karen K. Zee <kkzee@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.ClassHierarchy;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.WorkSet;
import java.util.Set;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * <code>AllCallers</code>
 * 
 * @author Karen K. Zee <kkzee@alum.mit.edu>
 * @version $Id: AllCallers.java,v 1.1.2.1 1999-10-20 03:18:29 kkz Exp $
 */

public class AllCallers {
    final HCodeFactory hcf;
    final ClassHierarchy ch;
    final Hashtable g;

    /** Creates a <code>AllCallers</code> object using the specified
     *  <code>ClassHierarchy</code>. Currently, <code>hcf</code> must be a
     *  code factory that generates quad-no-ssa-form.
     */
    public AllCallers(ClassHierarchy ch, HCodeFactory hcf) {
	this.hcf = hcf;
	this.ch = ch;
	this.g = buildGraph();
    }

    public Set getCallers(MethodSet ms) {
	Worklist toadd = new WorkSet();
	for (Iterator cm = this.ch.callableMethods().iterator(); 
	     cm.hasNext(); ) {
	    HMethod m = (HMethod)cm.next();
	    if (ms.select(m)) {
		toadd.push(m);
	    }
	}
	Set retval = new WorkSet();
	while(!toadd.isEmpty()) {
	    HMethod callee = (HMethod)toadd.pull();
	    retval.add(callee);
	    if (this.g.containsKey(callee)) {
		for (Iterator callers = ((WorkSet)this.g.get(callee)).iterator(); 
		     callers.hasNext(); ) {
		    HMethod caller = (HMethod)callers.next();
		    if (!retval.contains(caller))
			toadd.push(caller);
		}
	    }
	}
	return retval;
    }

    public static interface MethodSet {
	boolean select(HMethod hm);
    }

    /** Builds a directed graph and returns it as a <code>Hashtable</code>
     *  using the <code>ClassHierarchy</code> and <code>HCodeFactory</code>
     *  with which the <code>this</code> object was created.
     */
    private Hashtable buildGraph() {
	Hashtable ht = new Hashtable();
	CallGraph cg = new CallGraph(this.ch, this.hcf);
	for (Iterator cm = this.ch.callableMethods().iterator(); 
	     cm.hasNext(); ) {
	    HMethod m = (HMethod)cm.next();
	    HMethod[] callees = cg.calls(m);
	    for (int i = 0; i < callees.length; i++) {
		if (!ht.containsKey(callees[i]))
		    ht.put(callees[i], new WorkSet());
		((WorkSet)ht.get(callees[i])).add(m);
	    }
	}
	return ht;
    }
}
