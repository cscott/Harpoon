// QuadEnumerator.java, created Sat Apr 10 10:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

/**
 * QuadEnumerator
 *
 * @author  John Whaley
 */

import harpoon.Util.*;
import java.util.Enumeration;
import harpoon.IR.Quads.*;

class QuadEnumerator implements Enumeration {

  Worklist W; Set done;

  QuadEnumerator(Quad q) {
    W = new HashSet();
    done = new HashSet();
    W.push(q); done.push(q);
  }

  public boolean hasMoreElements() { return !W.isEmpty(); }

  public Object nextElement() {
    Quad q = (Quad) W.pull();
    for (int i=0, n=q.nextLength(); i<n; ++i) {
      Quad nq = q.next(i);
      if (!done.contains(nq)) { done.union(nq); W.push(nq); }
    }
    return q;
  }
}
