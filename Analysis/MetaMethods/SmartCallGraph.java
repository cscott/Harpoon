// SmartCallGraph.java, created Tue Mar 21 15:32:43 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.MetaMethods;

import java.util.Vector;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;

import harpoon.Analysis.Quads.CallGraph;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Quads.CALL;
import harpoon.Analysis.MetaMethods.MetaMethod;
import harpoon.Analysis.MetaMethods.MetaCallGraph;
import harpoon.Analysis.PointerAnalysis.Relation;

/**
 * <code>SmartCallGraph</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: SmartCallGraph.java,v 1.1.2.1 2000-03-21 22:28:44 salcianu Exp $
 */
public class SmartCallGraph implements CallGraph {
    
    /** Creates a <code>SmartCallGraph</code>. */
    public SmartCallGraph(MetaCallGraph mcg) {
	compute(mcg);
    }

    // empty array to return in case of no callee
    private final HMethod[] empty_array = new HMethod[0];

    // cache to keep the association caller -> calees
    // Generic programming type: Map<HMethod,HMethod[]>
    final private Map hm2callees = new HashMap();
    /** Returns an array containing all possible methods called by
	method <code>m</code>. If <code>hm</code> doesn't call any 
	method, return an array of length <code>0</code>. */
    public final HMethod[] calls(final HMethod hm) {
	HMethod[] retval = (HMethod[]) hm2callees.get(hm);
	if(retval == null)
	    return empty_array;
	return retval;
    }

    // cache to keep the association caller -> call_site -> callees
    // Generic programming type: Map<HMethod, Map<CALL, HMethod[]>>
    final private Map hm2cs2callees = new HashMap();
    /** Returns an array containing  all possible methods called by 
	method <code>m</code> at the call site <code>cs</code>.
	If there is no known callee for the call site <code>cs>/code>, or if 
	<code>cs</code> doesn't belong to the code of <code>hm</code>,
	return an array pof length <code>0</code>. */
    public final HMethod[] calls(final HMethod hm, final CALL cs) {
	Map cs2callees = (Map) hm2cs2callees.get(hm);
	if(cs2callees == null)
	    return empty_array;
	HMethod[] retval = (HMethod[]) cs2callees.get(cs);
	if(retval == null)
	    return empty_array;
	return retval;
    }

    /** Returns a list of all the <code>CALL</code>s quads in the code 
	of <code>hm</code>. */
    public CALL[] getCallSites(final HMethod hm){
	Map map = (Map) hm2cs2callees.get(hm);
	if(map == null)
	    return new CALL[0];
	Set css = map.keySet();
	return (CALL[]) css.toArray(new CALL[css.size()]);
    }

    // Does the main computation: fill the hm2callees and hm2cs2callees
    // structures using the info from mcg.
    private final void compute(final MetaCallGraph mcg){
	final Relation split = mcg.getSplitRelation();

	for(Iterator ithm = split.keySet().iterator(); ithm.hasNext(); ){
	    HMethod hm = (HMethod) ithm.next();
	    // vc stores all the callees of hm
	    Vector vc = new Vector();
	    // map stores the association cs -> callees (cs is from
	    // the code of hm)
	    Map map = new HashMap();

	    for(Iterator itmm = split.getValues(hm); itmm.hasNext(); ){

		MetaMethod mm = (MetaMethod) itmm.next();

		Iterator itcs = mcg.getCallSites(mm).iterator();
		while(itcs.hasNext()){
		    CALL cs = (CALL) itcs.next();
		    // vc_cs stores all the callers at site cs
		    Vector vc_cs = new Vector();

		    // get the meta-method whih are called at cs
		    MetaMethod[] callees = mcg.getCallees(mm,cs);

		    for(int i = 0; i < callees.length; i++){
			HMethod hm_callee = callees[i].getHMethod();
			vc.add(hm_callee);
			vc_cs.add(hm_callee);
		    }

		    map.put(cs, 
		       (HMethod[]) vc_cs.toArray(new HMethod[vc_cs.size()]));
		}
	    }

	    hm2callees.put(hm, (HMethod[])vc.toArray(new HMethod[vc.size()])); 
	    hm2cs2callees.put(hm, map);
	}
    }

}
