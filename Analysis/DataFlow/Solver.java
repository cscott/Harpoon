// Solver.java, created Mon Nov  8 22:52:13 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.DataFlow.ReversePostOrderEnumerator; 
import harpoon.Util.Collections.WorkSet;


import java.util.Enumeration; 
import java.util.Iterator;

/**
 * <code>Solver</code> contains static methods to find the fixed point
 * of a set of data-flow equations.  It is a generalization of the
 * <code>QuadSolver</code> and <code>InstrSolver</code> that is
 * intended for general usage on any Intermediate Representation that
 * is amicable to data-flow analysis. 
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: Solver.java,v 1.1.2.8 2001-11-08 00:21:19 cananian Exp $
 */
public class Solver {

    private static boolean DEBUG = false;
    private static void db(String s) { if(DEBUG)System.out.println(s); }
    static final boolean TIME = false;

    /** Creates a <code>Solver</code>.  Ctor is made private so that
	there won't be any instances of Solvers running around. */
    private Solver() {
        
    }
    
    /** Performs dataflow analysis on a set of
	<code>BasicBlock</code>s.  Finds the maximum fixed point for
	the data-flow equations defined by <code>v</code>. 

	FSK: it seems to me that the <code>blocks</code> are added to
	     a stack and then popped off for visitation, which means
	     that if you want to visit the blocks in say Reverse
	     Postorder, then <code>blocks</code> must iterate over
	     them in *Normal* Postorder.  This seems viciously
	     unintuitive... perhaps the Solver interface should be
	     revised to offer more control over the traversal... (on
	     the other hand, the current implementation doesn't seem
	     to be performing that shabbily to me, so perhaps is
	     better to leave well enough alone...)

	<BR> <B>requires:</B> <OL>
	     <LI> The elements of <code>blocks</code> are
	          <code>BasicBlock</code>s.
	     <LI> <code>v</code> can safely visit the elements of
    	          <code>blocks</code>. 
	     </OL>
	<BR> <B>modifies:</B> <code>v</code>, <code>blocks</code>
	<BR> <B>effects:</B> 
	     Sends <code>v</code> into the elements of
	     <code>blocks</code> and the 'successors' of the elements
	     of <code>blocks</code> (where 'successors' is defined by
	     <code>v</code>'s addSuccessors() method).
	     On each visitation, performs <code>v</code>'s transfer
	     function on each <code>BasicBlock</code> in turn,
	     tracking for when no change occurs to the 'successors',
	     at which point analysis is complete.
	     This method guarantees that all of the
	     <code>BasicBlock</code>s. in <code>blocks</code> will be
	     visited  by <code>v</code> at least once. 
    */
    public static void worklistSolve(Iterator blocks,
				     DataFlowBasicBlockVisitor v) {
	WorkSet w = new WorkSet();
	int total = 0;
	int count = 0;
	while (blocks.hasNext()) {
	    w.push(blocks.next());
	    total++;
	}
	while (!w.isEmpty()) {
	    // v.changed = false;
	    BasicBlock b = (BasicBlock) w.pull();
	    if (TIME) count++;
	    if (DEBUG) 
		System.out.println
		    ("Visiting block " + b);
	    b.accept(v);
	    v.addSuccessors(w, b);
	}
	if (TIME) System.out.print("(s:"+count+"/"+total+")");

    }

    public static void forwardRPOSolve(BasicBlock root,
				       DataFlowBasicBlockVisitor v) { 
	ReversePostOrderEnumerator rpo = new ReversePostOrderEnumerator(root);
	boolean changed = false;
	do {
	    changed = false;

	    ReversePostOrderEnumerator iter = rpo.copy();
	    while (iter.hasMoreElements()) {
		BasicBlock q = (BasicBlock)iter.next();
		if (DEBUG) db("visiting: "+q);
		q.accept(v);
		for (Enumeration e=q.next(); e.hasMoreElements(); ) {
		    BasicBlock qn = (BasicBlock)e.nextElement();
		    if (DEBUG) db("doing edge "+q+" -> "+qn);
		    changed = v.merge(q, qn); 
		}
	    }

	} while (changed);
    }
    
}
