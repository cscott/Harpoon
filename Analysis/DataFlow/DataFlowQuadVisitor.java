// DataFlowQuadVisitor.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import java.util.Enumeration;
import harpoon.Util.Worklist;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Quads.QuadVisitor;
/**
 * DataFlowQuadVisitor
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: DataFlowQuadVisitor.java,v 1.1.2.6 2001-06-17 23:06:45 cananian Exp $
 */

public abstract class DataFlowQuadVisitor extends QuadVisitor {

  public static boolean DEBUG = false;
  public static void db(String s) { System.out.println(s); }

  /**
   * This bit is set whenever something changes.  Used to check for
   * termination.
   */
  boolean changed;

  /**
   * Add the successors of the quad q to the worklist W, performing merge
   * operations if necessary.
   */
  public abstract void addSuccessors(Worklist W, Quad q);

  /**
   * Merge operation on the from and to quad.  Returns true if the to quad
   * changes.
   */
  public abstract boolean merge(Quad from, Quad to);

}
