// BasicBlockVisitor.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

/**
 * BasicBlockVisitor
 *
 * Implemented similarly to QuadVisitor, with the idea that we may
 * eventually have different kinds of basic blocks.
 * @author John Whaley
 * @version $Id: BasicBlockVisitor.java,v 1.1.2.1 1999-09-20 16:06:22 pnkfelix Exp $
 */

public abstract class BasicBlockVisitor  {
  protected BasicBlockVisitor() { }

  /** Visit a basic block b. */
  public abstract void visit(BasicBlock b);
}
