// TreeSolver.java, created Tue Jul 27 17:53:34 1999 by duncan
// Copyright (C) 1998 Duncan Bryce <duncan@lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import harpoon.IR.Properties.HasEdges;
import harpoon.ClassFile.HCodeElement;
import harpoon.Util.Worklist;
import harpoon.Util.Set;
import harpoon.Util.HashSet;
import harpoon.Util.Util;
import harpoon.Util.IteratorEnumerator;
import harpoon.Analysis.EdgesIterator;

/** 
 * A blatant rip-off of Whaley's <code>QuadSolver</code> class, used to 
 * solve data flow equations (baby).  As Felix pointed out, there is
 * probably not much of a need for separate quad and tree solver classes.
 * Ultimately they should probably be combined into a 
 * <code>BasicBlockSolver</code> class. 
 * 
 * @author Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: TreeSolver.java,v 1.1.2.5 1999-08-05 05:12:40 duncan Exp $
 */
public abstract class TreeSolver {

    public static boolean DEBUG = false;
    public static void db(String s) { System.out.println(s); }


    public static void forward_rpo_solver(BasicBlock root, 
					  DataFlowBasicBlockVisitor v) {

	ReversePostOrderEnumerator rpo = new ReversePostOrderEnumerator(root);
	boolean changed = false;
	do {
	    changed = false;

	    ReversePostOrderEnumerator iter = rpo.copy();
	    while (iter.hasMoreElements()) {
		BasicBlock q = (BasicBlock)iter.next();
		if (DEBUG) db("visiting: "+q);
		q.visit(v);
		for (Enumeration e=q.next(); e.hasMoreElements(); ) {
		    BasicBlock qn = (BasicBlock)e.nextElement();
		    if (DEBUG) db("doing edge "+q+" -> "+qn);
		    changed = v.merge(q, qn); 
		}
	    }

	} while (changed);

    }

    public static void worklist_solver(BasicBlock root, 
				       DataFlowBasicBlockVisitor v) {

	Worklist W = new HashSet();
	W.push(root);
	while (!W.isEmpty()) {
	    //v.changed = false;
	    BasicBlock q = (BasicBlock) W.pull();
	    if (DEBUG) db("visiting: "+q);
	    q.visit(v);
	    v.addSuccessors(W, q);
	}
    }

    public static int getMaxID(HCodeElement root) {
	int max = 0;
	for (Iterator i = new EdgesIterator((HasEdges)root); i.hasNext();) { 
	    int id = ((HCodeElement)(i.next())).getID();
	    Util.assert(id >= 0);
	    if (id > max) max = id;
	}
	if (DEBUG) db("max tree ID is "+max);
	return max;
    }
}
