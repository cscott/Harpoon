// PANodeVisitor.java, created Thu Jan 13 16:21:14 2000 by salcianu
// Copyright (C) 1999 Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

/**
 * <code>PANodeVisitor</code>
 * 
 * @author  Alexandru SALCIANU <salcianu@retezat.lcs.mit.edu>
 * @version $Id: PANodeVisitor.java,v 1.1.2.1 2000-01-14 20:50:59 salcianu Exp $
 */
public interface PANodeVisitor {
    public void visit(PANode node);
}
