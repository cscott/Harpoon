// MetaAllCallers.java, created Sun Mar 12 22:30:17 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MetaMethods;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import harpoon.Analysis.PointerAnalysis.PAWorkList;

import harpoon.Util.DataStructs.Relation;
import harpoon.Util.DataStructs.LightRelation;

import harpoon.Util.Util;

/**
 * <code>MetaAllCallers</code> is the dual of <code>MetaCallGraph</code>.
 It computes the callers (<code>MetaMethod</code>s) of each meta-method from the
 program. Note that the bulk of the computation is done in 
 <code>MetaCallGraph</code>, this class just inverts the edges of a
 precomputed graph.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: MetaAllCallers.java,v 1.3.2.1 2002-02-27 08:31:50 cananian Exp $
 */
public class MetaAllCallers implements java.io.Serializable {

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

    /** Returns the meta-methods that transitively call the meta-method
	<code>mm_callee</code>. Simply the transitive closure of 
	<code>getCallers</code>. */
    public MetaMethod[] getTransCallers(MetaMethod mm_callee){
	Set callers  = new HashSet();
	PAWorkList W = new PAWorkList();
	W.add(mm_callee);
	while(!W.isEmpty()){
	    MetaMethod mm_work = (MetaMethod) W.remove();
	    MetaMethod[] dc = getCallers(mm_work);
	    for(int i = 0; i < dc.length; i++)
		if(callers.add(dc[i])) W.add(dc[i]);
	}
	return (MetaMethod[]) callers.toArray(new MetaMethod[callers.size()]);
    }

    // fills the relation callers, by simply inverting the information
    // from the attached MetaCallGraph.
    private void process(MetaCallGraph mcg){
	// keeps the callers of each meta-method
	// Relation<MetaMethod mm_callee, MetaMethod mm_caller>.
	Relation callers = new LightRelation();

	Iterator it_callers = mcg.getAllMetaMethods().iterator();
	while(it_callers.hasNext()){
	    MetaMethod mm_caller = (MetaMethod) it_callers.next();
	    MetaMethod[] mms = mcg.getCallees(mm_caller);
	    for(int i = 0 ; i < mms.length ; i++){
		MetaMethod mm_callee = mms[i];
		assert (mm_caller != null) && (mm_callee != null) : "mm_caller = " + mm_caller +
			    "i = " + i + "/" + mms.length;
		callers.add(mm_callee, mm_caller);
	    }
	}

	compact(callers);
	callers = null; // enable the GC
    }

    // Converts the information from the big format (Relation) into a smaller
    // one (array based)
    private void compact(Relation callers){
	callers_cmpct = new HashMap();
	for(Iterator it = callers.keys().iterator(); it.hasNext(); ){
	    MetaMethod mm = (MetaMethod) it.next();
	    Set set = callers.getValues(mm);
	    MetaMethod[] mms = 
		(MetaMethod[]) set.toArray(new MetaMethod[set.size()]);
	    callers_cmpct.put(mm,mms);
	}
    }

}
