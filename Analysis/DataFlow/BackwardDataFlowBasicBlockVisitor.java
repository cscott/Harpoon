// BackwardDataFlowBasicBlockVisitor.java, created Wed May 26 16:33:58 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import java.util.Enumeration;
import harpoon.Util.Worklist;

/**
 * <code>BackwardDataFlowBasicBlockVisitor</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: BackwardDataFlowBasicBlockVisitor.java,v 1.1.2.1 1999-05-27 22:58:59 pnkfelix Exp $
 */
public abstract class BackwardDataFlowBasicBlockVisitor extends DataFlowBasicBlockVisitor {

    /** Adds the successors of the basic block q to the worklist W,
	performing merge operations if necessary.
    */
    public void addSuccessors(Worklist W, BasicBlock q) {
	for (Enumeration e=q.prev(); e.hasMoreElements(); ) {
	    BasicBlock qn = (BasicBlock) e.nextElement();
	    if (merge(q, qn)) {
		W.push(qn);
	    }
	}
    }
    
}
