package harpoon.Analysis.DataFlow;

import java.util.Enumeration;
import harpoon.Util.Worklist;
import harpoon.IR.Quads.*;
/**
 * ForwardDataFlowQuadVisitor
 *
 * @author  John Whaley
 */

public abstract class ForwardDataFlowQuadVisitor extends DataFlowQuadVisitor {

  public void addSuccessors(Worklist W, Quad q) {
    if (DEBUG) db("adding successors of "+q+" to worklist");
    for (int i=0, n=q.nextLength(); i<n; ++i) {
      Quad qn = q.next(i);
      if (DEBUG) db("looking at "+q+" -> "+qn);
      if (merge(q, qn)) {
	if (DEBUG) db("added "+qn+" to the work queue because its in set changed");
	W.push(qn); // in set changed -- need to revisit target node
      }
    }
  }

}

