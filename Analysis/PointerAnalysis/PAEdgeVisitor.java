// PAEdgeVisitor.java, created Sun Feb  6 20:44:07 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.Temp.Temp;

/**
 * <code>PAEdgeVisitor</code> is a wrapper for the functions that are
 called on an edge of the form <code>&lt;var,node&gt;</code> or
 <code>&lt;node1,f,node2&gt;</code>.
 There is no other way to pass a function in Java (no pointers to methods ...)
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PAEdgeVisitor.java,v 1.1.2.2 2000-02-12 23:05:20 salcianu Exp $
 */
interface  PAEdgeVisitor {
    /** Visits a <code>&lt;var,node&gt;</code> edge. */
    public void visit(Temp var,PANode node);
    /** Visits a <code>&lt;node1,f,node2&gt;</code> edge. */
    public void visit(PANode node1, String f, PANode node2);
}
