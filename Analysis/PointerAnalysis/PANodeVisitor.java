// PANodeVisitor.java, created Thu Jan 13 16:21:14 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

/**
 * <code>PANodeVisitor</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PANodeVisitor.java,v 1.1.2.2 2000-01-17 23:49:03 cananian Exp $
 */
public interface PANodeVisitor {
    public void visit(PANode node);
}
