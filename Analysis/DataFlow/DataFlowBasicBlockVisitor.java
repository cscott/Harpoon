// DataFlowBasicBlockVisitor.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.BasicBlockInterf;
import java.util.Enumeration;
import harpoon.Util.Worklist;

import harpoon.Util.Util;

/**
 * <code>DataFlowBasicBlockVisitor</code> is a specialized
 * <code>BasicBlockVisitor</code> for performing data flow analysis on
 * a set of <code>BasicBlock</code>.
 *
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: DataFlowBasicBlockVisitor.java,v 1.2 2002-02-25 20:56:38 cananian Exp $
 */

public abstract class DataFlowBasicBlockVisitor extends harpoon.Analysis.BasicBlockInterfVisitor {

    private static boolean DEBUG = false;
    public static void db(String s) { System.out.println(s); }

    /**
     * Adds the successors of the basic block q to the worklist W,
     * performing merge operations if necessary.
     */
    public abstract void addSuccessors(Worklist W, BasicBlock q);
    
    /**
       Merges operation on the from and to basic block.  Returns true if
       the to basic block changes.
       
       <BR> <B>NOTE:</B> "changes" above refers to our knowledge about
            the basic block changing, not the contents of the basic
	    block itself, which shouldn't be modified during Analysis.
	    Thus, an appropriate "change" would be a variable being
	    added to the IN-set of 'to' during Forward Dataflow
	    Analysis.
       
    */
    public abstract boolean merge(BasicBlock from, BasicBlock to);

    
    /** Performs some transfer function on a basic block b.  
	<BR> <B>NOTE:</B> Transfer functions must be monotonic for
	dataflow analysis to terminate.
    */
    public abstract void visit(BasicBlock b);

    public void visit(BasicBlockInterf b) {
	Util.assert(false, "Unexpected BasicBlockInterf subclass");
    }
}

