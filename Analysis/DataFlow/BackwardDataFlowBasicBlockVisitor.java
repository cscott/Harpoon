// BackwardDataFlowBasicBlockVisitor.java, created Wed May 26 16:33:58 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Analysis.BasicBlockInterf;
import java.util.Iterator;
import net.cscott.jutil.WorkSet;

/**
 * <code>BackwardDataFlowBasicBlockVisitor</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: BackwardDataFlowBasicBlockVisitor.java,v 1.6 2004-02-08 03:19:21 cananian Exp $
 */
public abstract class BackwardDataFlowBasicBlockVisitor 
    extends DataFlowBasicBlockVisitor {

    private static final boolean DEBUG = false;

    /** Performs the <i>merge</i> operation between <code>q</code> and
	its predecessors, readding <code>BasicBlock</code>s to
	<code>W</code> where necessary.
	<BR> <B>effects:</B> Runs <code>merge(q, p)</code> for all
	     <code>p</code> element of Predecessors(<code>q</code>).
	     If the <code>merge(q, p)</code> operation returns
	     <code>true</code> for a given <code>p</code>, adds
	     <code>p</code> to <code>W</code>, indicating that
	     <code>p</code> must be revisited by <code>this</code>.
       @see #merge(BasicBlock, BasicBlock)
    */
    public void addSuccessors(WorkSet W, BasicBlockInterf q) {
	for (Object parentO : q.prevSet()) {
	    BasicBlockInterf parent = (BasicBlockInterf) parentO;
	    if (DEBUG) 
		System.out.println
		    ("addSucc: merging from: " + q + 
		     " to: " + parent);
	    if (merge(q, parent)) {
		W.add(parent);
	    }
	    if (DEBUG) 
		System.out.println
		    ("addSucc: worklist: " + W);
	}
    }
    
}
