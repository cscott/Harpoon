// QuadEnumerator.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

/**
 * QuadEnumerator
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: QuadEnumerator.java,v 1.2 2002-02-25 20:56:42 cananian Exp $
 */

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import harpoon.IR.Quads.Quad;

class QuadEnumerator implements Enumeration {

  List W; Set done;

  QuadEnumerator(Quad q) {
    W = new ArrayList();
    done = new HashSet();
    W.add(q); done.add(q);
  }

  public boolean hasMoreElements() { return !W.isEmpty(); }

  public Object nextElement() {
    Quad q = (Quad) W.remove(W.size()-1);
    for (int i=0, n=q.nextLength(); i<n; ++i) {
      Quad nq = q.next(i);
      if (!done.contains(nq)) { done.add(nq); W.add(nq); }
    }
    return q;
  }
}
