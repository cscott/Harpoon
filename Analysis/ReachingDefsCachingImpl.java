// ReachingDefsCachingImpl.java, created Thu Jul 13 13:37:11 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.TYPECAST;
import harpoon.Temp.Temp;

import harpoon.Util.Util;
import net.cscott.jutil.BitSetFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
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
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ReachingDefsCachingImpl.java,v 1.5 2004-02-08 03:19:12 cananian Exp $
 */
public class ReachingDefsCachingImpl<HCE extends HCodeElement>
    extends ReachingDefsAltImpl<HCE> {

    /** Tracks the last basic block a query was done on. */
    BasicBlock<HCE> lastBlock = null;

    /** Maps Temp:t -> HCodeElement:h -> Set:s where s is the result
	of calling reachingDefs(h, t).
    */
    Map<Temp,Map<HCE,Set<HCE>>> myCache;

    /** Creates a <code>ReachingDefsCachingImpl</code>. */
    public ReachingDefsCachingImpl(HCode<HCE> hc) {
	super(hc);
    }

    /** Creates a <code>ReachingDefsCachingImpl</code>. */
    public ReachingDefsCachingImpl(HCode<HCE> hc, CFGrapher<HCE> c) {
	super(hc, c);
    }

    /** Creates a <code>ReachingDefsCachingImpl</code>. */
    public ReachingDefsCachingImpl(HCode<HCE> hc, CFGrapher<HCE> c, UseDefer<HCE> ud) {
	super(hc, c, ud);
    }
    
    /** Returns the Set of <code>HCodeElement</code>s providing definitions
     *  of <code>Temp</code> <code>t</code> which reach 
     *  <code>HCodeElement</code> <code>hce</code>. 
     */
    public Set<HCE> reachingDefs(HCE hce, Temp t) {
	// find out which BasicBlock this HCodeElement is from
	BasicBlock<HCE> b = bbf.getBlock(hce);
	if (b == lastBlock) {
	    // System.out.print(" _"+b.statements().size());
	    if (myCache.keySet().contains(t)) 
		return myCache.get(t).get(hce);
	} else {
	    // System.out.print(" M"+b.statements().size());
	    myCache = new HashMap<Temp,Map<HCE,Set<HCE>>>();
	    lastBlock = b;
	}

	Map<HCE,Set<HCE>> hceToResults = new HashMap<HCE,Set<HCE>>();
	myCache.put(t, hceToResults);

	// get the map for the BasicBlock
	Record r = cache.get(b);

	// find HCodeElements associated with `t' in the IN Set
	Set results = bsf.makeSet(r.IN);
	results.retainAll( tempToAllDefs.get(t) );

	Iterator pairs = results.iterator();
	results = new HashSet();
	while(pairs.hasNext()) {
	    results.add( ((List)pairs.next()).get(1) );
	}

	// propagate in Set through the HCodeElements 
	// of the BasicBlock in correct order
	// report("Propagating...");
	for(HCE curr : b.statements()) {
	    hceToResults.put(curr, results);

	    Collection<Temp> defC = null;
	    // special treatment of TYPECAST
	    if(check_typecast && (curr instanceof TYPECAST))
		defC = Collections.singleton(((TYPECAST)curr).objectref());
	    else
		defC = ud.defC(curr);

	    if (defC.contains(t)) 
		results = Collections.singleton(curr);
	}

	return hceToResults.get(hce);
    }
}
