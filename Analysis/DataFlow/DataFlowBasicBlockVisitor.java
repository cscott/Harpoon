package harpoon.Analysis.DataFlow;

import java.util.Enumeration;
import harpoon.Util.Worklist;
import harpoon.IR.Quads.*;
/**
 * DataFlowBasicBlockVisitor
 *
 * @author  John Whaley
 */

public abstract class DataFlowBasicBlockVisitor extends BasicBlockVisitor {

  public static boolean DEBUG = false;
  public static void db(String s) { System.out.println(s); }

  /**
   * This bit is set whenever something changes.  Used to check for
   * termination.
   */
  boolean changed;

  /**
   * Add the successors of the basic block q to the worklist W,
   * performing merge operations if necessary.
   */
  public abstract void addSuccessors(Worklist W, BasicBlock q);

  /**
   * Merge operation on the from and to basic block.  Returns true if
   * the to basic block changes.
   */
  public abstract boolean merge(BasicBlock from, BasicBlock to);

}
