// SCCLBBFactory.java, created Thu Mar 23 19:37:33 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Util.LightBasicBlocks;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCode;
import harpoon.Util.Graphs.SCComponent;
import harpoon.Util.Graphs.SCCTopSortedGraph;


/**
 * <code>SCCLBBFactory</code> computes the topologically sorted component
 graph of the light basic blocks containing the code of a method.
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: SCCLBBFactory.java,v 1.1.2.8 2001-06-17 22:37:13 cananian Exp $
 */
public class SCCLBBFactory implements java.io.Serializable {

    /** The <code>LBBConverter</code> used to generate the 
	LightBasicBlock views of the methods */
    private LBBConverter lbbconv;
    
    /** Creates a <code>SCCLBBFactory</code>. */
    public SCCLBBFactory(LBBConverter lbbconv) {
        this.lbbconv = lbbconv;
    }

    /** Returns the underlying <code>LBBConverter</code>. This is the 
        same as the one passed to the constructor of <code>this</code>
        object. */
    public LBBConverter getLBBConverter(){
        return lbbconv;
    }

    private static final SCComponent.Navigator navigator = 
	new SCComponent.Navigator() {
		public Object[] next(Object node) {
		    return ((LightBasicBlock) node).getNextLBBs();
		}
		public Object[] prev(Object node) {
		    return ((LightBasicBlock) node).getPrevLBBs();
		}
	    };

    /** Generates the code of the method <code>hm</code> using the 
	<code>HCodeFactory</code> passed to the constructor of
	<code>this</code> object, cut it into pieces (i.e. 
	<code>LightBasicBlock</code>s), build the strongly connected componnets
	of <code>LightBasicBlock</code>s and sort them topologically.
	Returns the sorted graph. */
    public SCCTopSortedGraph computeSCCLBB(HMethod hm){

        LightBasicBlock.Factory lbbf = lbbconv.convert2lbb(hm);
        LightBasicBlock lbb = lbbf.getRoot();

        SCComponent scc = SCComponent.buildSCC(lbb, navigator);
        SCCTopSortedGraph lbb_scc = SCCTopSortedGraph.topSort(scc);

        return lbb_scc;
    }

}
