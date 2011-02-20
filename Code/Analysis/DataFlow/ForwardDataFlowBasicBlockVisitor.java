// ForwardDataFlowBasicBlockVisitor.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Analysis.BasicBlockInterf;
import java.util.Iterator;
import harpoon.Util.Worklist;

/**
 * ForwardDataFlowBasicBlockVisitor
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ForwardDataFlowBasicBlockVisitor.java,v 1.4 2002-04-02 23:39:11 salcianu Exp $
 */

public abstract class ForwardDataFlowBasicBlockVisitor extends DataFlowBasicBlockVisitor {
    
    private static final boolean DEBUG = false;

  /** Performs the <i>merge</i> operation between <code>q</code> and
      its successors, readding <code>BasicBlock</code>s to
      <code>W</code> where necessary. 
      <BR> <B>effects:</B> Runs <code>merge(q, s)</code> for all
           <code>s</code> element of Successors(<code>q</code>).  
	   If the <code>merge(q, s)</code> operation returns
	   <code>true</code> for a given <code>s</code>, adds
	   <code>s</code> to <code>W</code>, indicating that
	   <code>s</code> must be revisited by <code>this</code>.
       @see #merge(BasicBlock, BasicBlock)
   */
  public void addSuccessors(Worklist W, BasicBlockInterf q) {
    if (DEBUG) db("adding successors of " + q + " to worklist");

    for (Iterator it = q.nextSet().iterator(); it.hasNext(); ) {
	BasicBlockInterf qn = (BasicBlockInterf) it.next();

	if (DEBUG) db("looking at " + q + " -> " + qn);
	
	if (merge(q, qn)) {
	    
	    if (DEBUG) db("added " + qn + " to the work queue");
	    
	    W.push(qn); // in set changed -- need to revisit target node
	}
    }
  }

}

