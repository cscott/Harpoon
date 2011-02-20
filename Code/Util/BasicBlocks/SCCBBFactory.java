// SCCBBFactory.java, created Wed Jan 12 15:51:47 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.BasicBlocks;

import java.util.Arrays;
import java.util.Set;

import harpoon.Analysis.BasicBlockInterf;
import harpoon.Analysis.BasicBlockFactoryInterf;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeFactory;

import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.Navigator;
import harpoon.Util.Graphs.SCCTopSortedGraph;
import harpoon.Util.UComp;

/**
 * <code>SCCBBFactory</code> converts the code of a method into the
 topollogically sorted component graph of <code>BasicBlock</code>s.
 *
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: SCCBBFactory.java,v 1.3 2003-05-06 15:04:20 salcianu Exp $
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
    private static final Navigator navigator = 
	new Navigator(){
		public Object[] next(Object node){
		    Set next_bb = ((BasicBlockInterf) node).nextSet();
		    Object[] obj = next_bb.toArray(new Object[next_bb.size()]);
		    Arrays.sort(obj, UComp.uc);
		    return obj;
		}
		public Object[] prev(Object node){
		    Set prev_bb = ((BasicBlockInterf) node).prevSet();
		    Object[] obj = prev_bb.toArray(new Object[prev_bb.size()]);
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
	BasicBlockFactoryInterf bbf = bbconv.convert2bb(hm);
	BasicBlockInterf bb = bbf.getRootBBInterf();

	SCComponent scc = SCComponent.buildSCC(bb, navigator);
	SCCTopSortedGraph bb_scc = SCCTopSortedGraph.topSort(scc);

	return bb_scc;
    }

}
