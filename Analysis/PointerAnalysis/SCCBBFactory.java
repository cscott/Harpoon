// SCCBBFactory.java, created Wed Jan 12 15:51:47 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;


import java.util.Hashtable;

import harpoon.Analysis.BasicBlock;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;
import harpoon.IR.Properties.CFGrapher;


/**
 * <code>SCCBBFactory</code> provides caching for the  
 * constructions of <code>BasicBlock</code>s and a convenient
 * method that is passing directly from <code>HMethod</code>
 * to <code>BasicBlock</code>s.
 *
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: SCCBBFactory.java,v 1.1.2.1 2000-01-27 06:24:11 salcianu Exp $
 */
public class SCCBBFactory {
    
    /** The cache of previously computed results; mapping
     *  <code>HMethod</code> -> <code>BasicBlock</code> */
    private Hashtable cache;

    /** The <code>HCodeFactory</code> used to generate the code 
	of the methods */
    private HCodeFactory hcf;

    /** Creates a <code>CachingBBFactory</code>. */
    public SCCBBFactory(HCodeFactory hcf) {
	this.hcf = hcf;
    }

    /** Generates the code of the method <code>hm</code> using the 
     * <code>HCodeFactory</code> passed to the constructor of
     * <code>this</code> object and cut it into pieces (i.e. 
     * <code>BasicBlock</code>s). */
    public SCCTopSortedGraph computeSCCBB(HMethod hm){
	HCode hcode = hcf.convert(hm);
	BasicBlock bb = BasicBlock.computeBasicBlocks(
			     hcode.getRootElement(),
			     CFGrapher.DEFAULT);

	SCComponent.Navigator navigator = 
	    new SCComponent.Navigator(){
		    public Object[] next(Object node){
			return ((BasicBlock)node).getNext();
		    }
		    public Object[] prev(Object node){
			return ((BasicBlock)node).getPrev();
		    }
		};

	SCComponent scc = SCComponent.buildSCC(bb,navigator);
	SCCTopSortedGraph bb_scc = SCCTopSortedGraph.topSort(scc);

	return bb_scc;
    }

}
