// SCCBBFactory.java, created Wed Jan 12 15:51:47 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.BasicBlocks;


import java.util.Hashtable;
import java.util.Arrays;
import java.util.Iterator;

import harpoon.Analysis.BasicBlock;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;

import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.SCCTopSortedGraph;
import harpoon.Util.UComp;

/**
 * <code>SCCBBFactory</code> converts the code of a method into the
 topollogically sorted component graph of <code>BasicBlock</code>s.
 *
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: SCCBBFactory.java,v 1.1.2.6 2001-06-17 22:36:26 cananian Exp $
 */
public class SCCBBFactory implements java.io.Serializable {

    /** The <code>BBConverter</code> used to generate the BasicBlock views 
	of the methods */
    private BBConverter bbconv;

    /** Creates a <code>SCCBBFactory</code>. */
    public SCCBBFactory(BBConverter bbconv) {
	this.bbconv = bbconv;
    }

    /** Returns the underlying <code>BBConverter</code>. This is the 
	same as the one passed to the constructor of <code>this</code>
	object. */
    public BBConverter getBBConverter(){
	return bbconv;
    }

    // Navigator through the control flow graph of Basic Blocks.
    private static final SCComponent.Navigator navigator = 
	new SCComponent.Navigator(){
		public Object[] next(Object node){
		    Object[] obj = ((BasicBlock)node).getNext();
		    Arrays.sort(obj, UComp.uc);
		    return obj;
		}
		public Object[] prev(Object node){
		    Object[] obj =  ((BasicBlock)node).getPrev();
		    Arrays.sort(obj, UComp.uc);
		    return obj;
		}
	    };
    
    /** Generates the code of the method <code>hm</code> using the 
	<code>HCodeFactory</code> passed to the constructor of
	<code>this</code> object, cuts it into pieces (i.e. 
	<code>BasicBlock</code>s) and topologically sorts the
	component graph. Returns the sorted graph. */
    public SCCTopSortedGraph computeSCCBB(HMethod hm){
	BasicBlock.Factory bbf = bbconv.convert2bb(hm);
	BasicBlock bb = bbf.getRoot();

	SCComponent scc = SCComponent.buildSCC(bb,navigator);
	SCCTopSortedGraph bb_scc = SCCTopSortedGraph.topSort(scc);

	return bb_scc;
    }

}
