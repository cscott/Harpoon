// AllCallersImpl.java, created Wed Aug 10 10:05:34 2005 by salcianu
// Copyright (C) 2003 Alexandru Salcianu <salcianu@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PA2.AllocSync;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.Analysis.ClassHierarchy;

import jpaul.DataStructs.Relation3;
import jpaul.DataStructs.Relation3MapRelImpl;

/**
 * <code>AllCallersImpl</code>
 * 
 * @author  Alexandru Salcianu <salcianu@alum.mit.edu>
 * @version $Id: AllCallersImpl.java,v 1.1 2005-08-16 22:41:57 salcianu Exp $
 */
class AllCallersImpl implements AllCallers {

    public AllCallersImpl(ClassHierarchy ch, CallGraph cg) {
	for(HMethod caller : ch.callableMethods()) {
	    for(CALL cs : cg.getCallSites(caller)) {
		HMethod[] callees = cg.calls(caller, cs);
		if(callees.length == 1) {
		    monoCALLs.add(cs);
		}
		for(HMethod callee : callees) {
		    rel3.add(callee, caller, cs);
		}
	    }
	}
    }
    
    private final Relation3<HMethod,HMethod,CALL> rel3 = 
	new Relation3MapRelImpl<HMethod,HMethod,CALL>();
    private final Set<CALL> monoCALLs = 
	new HashSet<CALL>();


    public Collection<HMethod> getCallers(HMethod callee) {
	return rel3.get2ndValues(callee);
    }

    public Collection<CALL> getCALLs(HMethod caller, HMethod callee) {
	return rel3.get3rdValues(callee, caller);
    }

    public boolean monoCALL(CALL cs) {
	return monoCALLs.contains(cs);
    }

}
