// BasicBlockInterfVisitor.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

/**
 * BasicBlockInterfVisitor is a visitor class
 * implemented similarly to QuadVisitor, with the idea that we may
 * eventually have different kinds of basic blocks.
 *
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: BasicBlockInterfVisitor.java,v 1.3 2002-04-02 23:58:23 salcianu Exp $
 */

public abstract class BasicBlockInterfVisitor {

   /** Creates a <code>BasicBlockInterfVisitor</code>. */
    protected BasicBlockInterfVisitor() { }

    public abstract void visit(BasicBlockInterf bb_interf);

    /** Visit a normal basic block. */
    public void visit(BasicBlock bb) {
	visit((BasicBlockInterf) bb);
    }

    /** Visit a Factored CFG basic block. */
    public void visit(FCFGBasicBlock fcfg_bb) {
	visit((BasicBlockInterf) fcfg_bb);
    }
}
