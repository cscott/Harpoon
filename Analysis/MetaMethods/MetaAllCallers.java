// MetaAllCallers.java, created Sun Mar 12 22:30:17 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MetaMethods;

import java.util.Iterator;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import harpoon.Analysis.PointerAnalysis.Relation;

/**
 * <code>MetaAllCallers</code> is the dual of <code>MetaCallGraph</code>.
 It computes the callers (<code>MetaMethod</code>s) of each meta-method from the
 program. Note that the bulk of the computation is done in 
 <code>MetaCallGraph</code>, this class just inverts the edges of a
 precomputed graph.
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: MetaAllCallers.java,v 1.1.2.1 2000-03-18 01:55:14 salcianu Exp $
 */
public class MetaAllCallers {

    /** Creates a <code>MetaAllCallers</code>. Receives as parameter
	the <code>MetaCallGraph</code> which computes the possible callees
	of each meta-method. */
    public MetaAllCallers(MetaCallGraph mcg) {
	process(mcg);
    }

    // Map<MetaMethod,MetaMethod[]>
    private Map callers_cmpct = null;

    private final MetaMethod[] empty_array = new MetaMethod[0];

    /** Returns the callers of the meta method <code>mm_callee</code>.
	The returned set is a set of <code>MetaMethod</code>s. */
    public  MetaMethod[] getCallers(MetaMethod mm_callee){
	MetaMethod[] retval = (MetaMethod[]) callers_cmpct.get(mm_callee);
	if(retval == null)
	    retval = empty_array;
	return retval;
    }

    // fills the relation callers, by simply inverting the information
    // from the attached MetaCallGraph.
    private void process(MetaCallGraph mcg){
	// keeps the callers of each meta-method
	// Relation<MetaMethod mm_callee, MetaMethod mm_caller>.
	Relation callers = new Relation();

	Iterator it_callers = mcg.getAllMetaMethods().iterator();
	while(it_callers.hasNext()){
	    MetaMethod mm_caller = (MetaMethod) it_callers.next();
	    MetaMethod[] mms = mcg.getCallees(mm_caller);
	    for(int i = 0 ; i < mms.length ; i++){
		MetaMethod mm_callee = mms[i];
		callers.add(mm_callee,mm_caller);
	    }
	}

	compact(callers);
	callers = null; // enable the GC
    }

    // Converts the information from the big format (Relation) into a smaller
    // one (array based)
    private void compact(Relation callers){
	callers_cmpct = new HashMap();
	for(Iterator it = callers.keySet().iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
	    Set set = callers.getValuesSet(mm);
	    MetaMethod[] mms = 
		(MetaMethod[]) set.toArray(new MetaMethod[set.size()]);
	    callers_cmpct.put(mm,mms);
	}
    }

}
