// BackwardDataFlowQuadVisitor.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import java.util.Enumeration;
import harpoon.Util.Worklist;
import harpoon.IR.Quads.Quad;
/**
 * BackwardDataFlowQuadVisitor
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 */

public abstract class BackwardDataFlowQuadVisitor extends DataFlowQuadVisitor {

  public void addSuccessors(Worklist W, Quad q) {
    if (DEBUG) db("adding predecessors of "+q+" to worklist");
    for (int i=0, n=q.prevLength(); i<n; ++i) {
      Quad qn = q.prev(i);
      if (DEBUG) db("looking at "+qn+" -> "+q);
      if (merge(q, qn)) {
	if (DEBUG) db("added "+qn+" to the work queue because its out set changed");
	W.push(qn); // out set changed -- need to revisit target node
      }
    }
  }

}
