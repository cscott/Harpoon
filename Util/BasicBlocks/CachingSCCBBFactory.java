// CachingSCCBBFactory.java, created Wed Jan 26 17:53:17 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.BasicBlocks;

import java.util.Map;
import java.util.HashMap;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Graphs.SCCTopSortedGraph;


/**
 * <code>CachingSCCBBFactory</code> adds some caching to
 <code>SCCBBFactory</code>.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: CachingSCCBBFactory.java,v 1.1.2.3 2001-06-17 22:36:26 cananian Exp $
 */
public class CachingSCCBBFactory extends SCCBBFactory{

    // The cache of previously computed results; mapping
    //  HMethod -> SCCTopSortedGraph
    private Map cache;

    /** Creates a <code>CachingSCCBBFactory</code>. */
    public CachingSCCBBFactory(BBConverter bbconv){
	super(bbconv);
	cache = new HashMap();
    }
    
    /** Computes the topologically sorted graph of all the basic blocks from 
	the <code>hm</code> method. All the results are cached so that the
	computation occurs only once for each method (of course, unless 
	<code>clear</code> is called). */
    public SCCTopSortedGraph computeSCCBB(HMethod hm){
	if(cache.containsKey(hm))
	    return (SCCTopSortedGraph) cache.get(hm);
	SCCTopSortedGraph sccg = super.computeSCCBB(hm);
	cache.put(hm,sccg);
	return sccg;
    }

    /** Clears the cache of previously computed results. */
    public void clear(){
	cache.clear();
    }
    
}
