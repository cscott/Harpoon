// BasicBlockVisitor.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

/**
 * BasicBlockVisitor
 *
 * Implemented similarly to QuadVisitor, with the idea that we may
 * eventually have different kinds of basic blocks.
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: BasicBlockVisitor.java,v 1.1.2.2 2001-06-17 22:28:34 cananian Exp $
 */

public abstract class BasicBlockVisitor  {
  protected BasicBlockVisitor() { }

  /** Visit a basic block b. */
  public abstract void visit(BasicBlock b);
}
