// ForwardDataFlowQuadVisitor.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import java.util.Enumeration;
import harpoon.Util.Worklist;
import harpoon.IR.Quads.Quad;
/**
 * ForwardDataFlowQuadVisitor
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ForwardDataFlowQuadVisitor.java,v 1.1.2.6 2001-06-17 23:06:45 cananian Exp $
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

