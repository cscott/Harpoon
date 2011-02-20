// CachingSCCLBBFactory.java, created Thu Mar 23 19:51:21 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.LightBasicBlocks;

import java.util.Map;
import java.util.HashMap;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Graphs.SCCTopSortedGraph;


/**
 * <code>CachingSCCLBBFactory</code> adds some caching to
 <code>SCCLBBFactory</code>.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: CachingSCCLBBFactory.java,v 1.2 2002-02-25 21:09:33 cananian Exp $
 */
public class CachingSCCLBBFactory extends SCCLBBFactory
    implements java.io.Serializable {
    
    // The cache of previously computed results; mapping
    //  HMethod -> SCCTopSortedGraph
    private Map cache;

    /** Creates a <code>CachingSCCLBBFactory</code>. */
    public CachingSCCLBBFactory(LBBConverter lbbconv){
	super(lbbconv);
        cache = new HashMap();
    }

    /** Computes the topologically sorted graph of all the light basic blocks
	of the <code>hm</code> method. All the results are cached so that the
        computation occurs only once for each method (of course, unless 
        <code>clear</code> is called). */
    public SCCTopSortedGraph computeSCCLBB(HMethod hm){
        if(cache.containsKey(hm))
            return (SCCTopSortedGraph) cache.get(hm);
        SCCTopSortedGraph sccg = super.computeSCCLBB(hm);
        cache.put(hm,sccg);
        return sccg;
    }

    /** Clears the cache of previously computed results. */
    public void clear(){
        cache.clear();
    }
    
}
