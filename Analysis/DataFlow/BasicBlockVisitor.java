package harpoon.Analysis.DataFlow;

/**
 * BasicBlockVisitor
 *
 * Implemented similarly to QuadVisitor, with the idea that we may
 * eventually have different kinds of basic blocks.
 */

public abstract class BasicBlockVisitor  {
  protected BasicBlockVisitor() { }

  /** Visit a basic block b. */
  public abstract void visit(BasicBlock b);
}
