// PANodeVisitor.java, created Thu Jan 13 16:21:14 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

/**
 * <code>PANodeVisitor</code> is a wrapper for a function that is
 called on a <code>PANode</code>.
 There is no other way to pass a function in Java (no pointers to methods ...)
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PANodeVisitor.java,v 1.1.2.4 2001-06-17 22:30:45 cananian Exp $
 */
public interface PANodeVisitor {
    /** Visits a <code>PANode</code>. */
    public void visit(PANode node);
}
