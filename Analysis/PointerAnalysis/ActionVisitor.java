// ActionVisitor.java, created Wed Feb  9 15:26:32 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

/**
 * <code>ActionVisitor</code> is a wrapper for the functions that are
 called on an action. There is no other way to
 pass a function in Java (no pointers to methods ...)
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: ActionVisitor.java,v 1.2 2002-02-25 20:58:38 cananian Exp $
 */
interface ActionVisitor {
    /** Visits a <code>ld</code> action. */
    public void visit_ld(PALoad load);
    /** Visits a <code>sync</code> action. */
    public void visit_sync(PASync sync);
}

