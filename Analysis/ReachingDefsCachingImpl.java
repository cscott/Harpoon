// ReachingDefsCachingImpl.java, created Thu Jul 13 13:37:11 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.TYPECAST;
import harpoon.Temp.Temp;


import harpoon.Util.Collections.BitSetFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;

/**
 * <code>ReachingDefsCachingImpl</code> is an extension of
 * <code>ReachingDefsImpl</code> that keeps a BasicBlock local cache
 * mapping Temp:t -> HCodeElement:h -> Set:s where s is the result of
 * calling reachingDefs(h, t).  This way repeated queries in the same 
 * <code>BasicBlock</code> as the last query don't have to iterate
 * over all the statements in the BasicBlock again.
 * 
 * @author  Felix S. Klock <pnkfelix@mit.edu>
 * @version $Id: ReachingDefsCachingImpl.java,v 1.1.2.1 2000-07-13 18:51:14 pnkfelix Exp $
 */
public class ReachingDefsCachingImpl extends ReachingDefsImpl {

    /** Tracks the last basic block a query was done on. */
    BasicBlock lastBlock = null;

    /** Maps Temp:t -> HCodeElement:h -> Set:s where s is the result
	of calling reachingDefs(h, t).
    */
    Map myCache;

    /** Creates a <code>ReachingDefsCachingImpl</code>. */
    public ReachingDefsCachingImpl(HCode hc) {
	super(hc);
    }

    /** Creates a <code>ReachingDefsCachingImpl</code>. */
    public ReachingDefsCachingImpl(HCode hc, CFGrapher c) {
	super(hc, c);
    }

    /** Creates a <code>ReachingDefsCachingImpl</code>. */
    public ReachingDefsCachingImpl(HCode hc, CFGrapher c, UseDefer ud) {
	super(hc, c, ud);
    }
    
    /** Returns the Set of <code>HCodeElement</code>s providing definitions
     *  of <code>Temp</code> <code>t</code> which reach 
     *  <code>HCodeElement</code> <code>hce</code>. 
     */
    public Set reachingDefs(HCodeElement hce, Temp t) {
	// find out which BasicBlock this HCodeElement is from
	BasicBlock b = bbf.getBlock(hce);
	if (b == lastBlock) {
	    if (myCache.keySet().contains(t)) 
		return (Set) ((Map)myCache.get(t)).get(hce);
	} else {
	    myCache = new HashMap();
	}

	HashMap hceToResults = new HashMap();
	myCache.put(t, hceToResults);

	// get the map for the BasicBlock
	Map m = (Map)cache.get(b);
	// get the BitSetFactory
	BitSetFactory bsf = (BitSetFactory)Temp_to_BitSetFactories.get(t);
	// make a copy of the in Set for the Temp
	Set results = bsf.makeSet((Set)m.get(t));
	// propagate in Set through the HCodeElements 
	// of the BasicBlock in correct order
	for(Iterator it=b.statements().iterator(); it.hasNext(); ) {
	    HCodeElement curr = (HCodeElement)it.next();
	    hceToResults.put(curr, results);

	    Collection defC = null;
	    // special treatment of TYPECAST
	    if(check_typecast && (curr instanceof TYPECAST))
		defC = Collections.singleton(((TYPECAST)curr).objectref());
	    else
		defC = ud.defC(curr);
	    if (defC.contains(t)) 
		results = bsf.makeSet(Collections.singleton(curr));
	}

	return (Set) hceToResults.get(hce);
    }
}
