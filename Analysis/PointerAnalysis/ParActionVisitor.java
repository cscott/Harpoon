// ParActionVisitor.java, created Wed Feb  9 15:35:27 2000 by salcianu
// Copyright (C) 2000 Alexandru SALCIANU <salcianu@MIT.EDU>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.PointerAnalysis;

/**
 * <code>ParActionVisitor</code> is a wrapper for the functions that are
 called on a "paralel action" piece of information.
 There is no other way to pass a function in Java (no pointers to methods ...)
 * 
 * @author  Alexandru SALCIANU <salcianu@MIT.EDU>
 * @version $Id: ParActionVisitor.java,v 1.1.2.2 2000-03-30 03:05:14 salcianu Exp $
 */
interface ParActionVisitor {
    /** Visits a "parallel action" item of information of the form
	<code> load || nt2</code>. */
    public void visit_par_ld(PALoad load, PANode nt2);
    /** Visits a "parallel action" item of information of the form
	<code>sync || nt2</code>. */
    public void visit_par_sync(PASync sync, PANode nt2);
}
