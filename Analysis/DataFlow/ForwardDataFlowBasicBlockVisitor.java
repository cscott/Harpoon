package harpoon.Analysis.DataFlow;

import java.util.Enumeration;
import harpoon.Util.Worklist;

/**
 * ForwardDataFlowBasicBlockVisitor
 *
 * @author  John Whaley
 */

public abstract class ForwardDataFlowBasicBlockVisitor extends DataFlowBasicBlockVisitor {

  /**
   * Adds the successors of the basic block q to the worklist W,
   * performing merge operations if necessary.
   */
  public void addSuccessors(Worklist W, BasicBlock q) {
    if (DEBUG) db("adding successors of "+q+" to worklist");

    for (Enumeration e=q.next(); e.hasMoreElements(); ) {
      BasicBlock qn = (BasicBlock)e.nextElement();

      if (DEBUG) db("looking at "+q+" -> "+qn);

      if (merge(q, qn)) {

	if (DEBUG) db("added "+qn+" to the work queue");

	W.push(qn); // in set changed -- need to revisit target node
      }
    }
  }

}

