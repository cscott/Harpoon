// ActionVisitor.java, created Wed Feb  9 15:26:32 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

/**
 * <code>ActionVisitor</code> is a wrapper for the functions that are
 called on an action. There is no other way to
 pass a function in Java (no pointers to methods ...)
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: ActionVisitor.java,v 1.1.2.1 2000-02-10 00:42:34 salcianu Exp $
 */
interface ActionVisitor {
    /** Visits a <code>ld</code> action. */
    public void visit_ld(PALoad load);
    /** Visits a <code>sync</code> action of the form \
	<code>&lt;sync,n,nt&gt;</code>. */
    public void visit_sync(PANode n, PANode nt);
}

