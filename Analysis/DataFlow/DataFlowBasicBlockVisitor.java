package harpoon.Analysis.DataFlow;

import java.util.Enumeration;
import harpoon.Util.Worklist;
import harpoon.IR.Quads.*;
/**
 * <code>DataFlowBasicBlockVisitor</code> is a specialized
 * <code>BasicBlockVisitor<code> for performing data flow analysis on
 * a set of <code>BasicBlock</code>.
 *
 * @author John Whaley 
 */

public abstract class DataFlowBasicBlockVisitor extends BasicBlockVisitor {

    public static boolean DEBUG = false;
    public static void db(String s) { System.out.println(s); }
    
    /**
     * This bit is set whenever something changes.  Used to check for
     * termination.
     */
    protected boolean changed;
    
    /**
     * Adds the successors of the basic block q to the worklist W,
     * performing merge operations if necessary.
     */
    public abstract void addSuccessors(Worklist W, BasicBlock q);
    
    /**
     * Merges operation on the from and to basic block.  Returns true if
     * the to basic block changes.
     */
    public abstract boolean merge(BasicBlock from, BasicBlock to);

    
    /** Performs a transfer function on a basic block b.  
	<BR> <B>NOTE:</B> Transfer functions must be monotonic for
	dataflow analysis to terminate.
    */
    public abstract void visit(BasicBlock b);
}

