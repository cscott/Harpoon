// Solver.java, created Mon Nov  8 22:52:13 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Analysis.BasicBlock;
import harpoon.Util.WorkSet;

import java.util.Iterator;

/**
 * <code>Solver</code> contains static methods to find the fixed point
 * of a set of data-flow equations.  It is a generalization of the
 * <code>QuadSolver</code> and <code>InstrSolver</code> that is
 * intended for general usage on any Intermediate Representation that
 * is amicable to data-flow analysis. 
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: Solver.java,v 1.1.2.4 1999-11-09 06:28:25 pnkfelix Exp $
 */
public class Solver {

    private static boolean DEBUG = false;
    private static void db(String s) { if(DEBUG)System.out.println(s); }

    /** Creates a <code>Solver</code>.  Ctor is made private so that
	there won't be any instances of Solvers running around. */
    private Solver() {
        
    }
    
    /** Performs dataflow analysis on a set of
	<code>BasicBlock</code>s.  Finds the maximum fixed point for
	the data-flow equations defined by <code>v</code>. 

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
	while (blocks.hasNext()) w.push(blocks.next());
	while (!w.isEmpty()) {
	    // v.changed = false;
	    BasicBlock b = (BasicBlock) w.pull();
	    if (DEBUG) 
		System.out.println
		    ("Visiting block " + b);
	    b.visit(v);
	    v.addSuccessors(w, b);
	}

    }
    
}
