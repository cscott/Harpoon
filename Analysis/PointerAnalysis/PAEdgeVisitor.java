// PAEdgeVisitor.java, created Sun Feb  6 20:44:07 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

import harpoon.Temp.Temp;

/**
 * <code>PAEdgeVisitor</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: PAEdgeVisitor.java,v 1.1.2.1 2000-02-07 02:01:17 salcianu Exp $
 */
interface  PAEdgeVisitor {
    public void visit(Temp var,PANode node);    
    public void visit(PANode node1, String f, PANode node2);
}
