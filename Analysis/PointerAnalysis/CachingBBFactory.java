// CachingBBFactory.java, created Wed Jan 12 15:51:47 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Hashtable;

import harpoon.Analysis.BasicBlock;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Properties.CFGrapher;


/**
 * <code>CachingBBFactory</code> provides caching for the  
 * constructions of <code>BasicBlock</code>s and a convenient
 * method that is passing directly from <code>HMethod</code>
 * to <code>BasicBlock</code>s.
 *
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: CachingBBFactory.java,v 1.1.2.1 2000-01-14 20:50:59 salcianu Exp $
 */
public class CachingBBFactory {
    
    /** The cache of previously computed results; mapping
     *  <code>HMethod</code> -> <code>BasicBlock</code> */
    private Hashtable cache;

    /** The <code>HCodeFactory</code> used to generate the code
     *  of the methods */
    private HCodeFactory hcf;

    /** Creates a <code>CachingBBFactory</code>. */
    public CachingBBFactory(HCodeFactory _hcf) {
        cache = new Hashtable();
	hcf   = _hcf;
    }

    /** Generates the code of the method <code>hm</code> using the 
     *  <code>HCodeFactory</code> passed to the constructor of
     *  <code>this</code> onject and cut it into pieces (i.e. 
     *  <code>BasicBlock</code>s). All the results are cached. */
    public BasicBlock computeBasicBlocks(HMethod hm){

	System.out.println("Computing the basic blocks for " + hm);

	if(!cache.containsKey(hm)){
	    
	    System.out.println("Computation NOW -> new result");

	    BasicBlock bb = BasicBlock.computeBasicBlocks(
		hcf.convert(hm).getRootElement(),
		CFGrapher.DEFAULT);
	    cache.put(hm,bb);
	    return bb;
	}

	System.out.println("Old computation");

	return (BasicBlock)cache.get(hm);
    }
    
    public void clear(){
	cache.clear();
    }
}
