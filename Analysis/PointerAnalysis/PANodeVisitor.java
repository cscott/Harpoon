// PANodeVisitor.java, created Thu Jan 13 16:21:14 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

/**
 * <code>PANodeVisitor</code> is a wrapper for a function that is
 called on a <code>PANode</code>.
 There is no other way to pass a function in Java (no pointers to methods ...)
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PANodeVisitor.java,v 1.1.2.3 2000-02-12 23:05:21 salcianu Exp $
 */
public interface PANodeVisitor {
    /** Visits a <code>PANode</code>. */
    public void visit(PANode node);
}
