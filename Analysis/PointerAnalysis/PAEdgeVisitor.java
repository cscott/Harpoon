// PAEdgeVisitor.java, created Sun Feb  6 20:44:07 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.Temp.Temp;

/**
 * <code>PAEdgeVisitor</code> is a wrapper for the functions that are
 called on an edge of the form <code>&lt;var,node&gt;</code> or
 <code>&lt;node1,f,node2&gt;</code>.
 There is no other way to pass a function in Java (no pointers to methods ...)
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PAEdgeVisitor.java,v 1.1.2.4 2001-06-17 22:30:42 cananian Exp $
 */
interface  PAEdgeVisitor {
    /** Visits a <code>&lt;var,node&gt;</code> edge. */
    public void visit(Temp var, PANode node);
    /** Visits a <code>&lt;node1,f,node2&gt;</code> edge. */
    public void visit(PANode node1, String f, PANode node2);
}
