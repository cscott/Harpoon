// InstrSolver.java, created Wed Apr  7 22:37:59 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Util.WorkSet;

/**
 * <code>InstrSolver</code>
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: InstrSolver.java,v 1.1.2.2 1999-05-27 01:53:35 pnkfelix Exp $
 */
public final class InstrSolver  {
    
    /** Overriding default constructor to prevent instantiation. */
    private InstrSolver() {
        
    }

    /** Performs dataflow analysis on a set of <code>BasicBlock</code>.
	
	<BR> <B>requires:</B> 1. Every instruction in
	                      <code>root</code> and in the
			      <code>BasicBlock</code>s linked to by
			      <code>root</code> is an instance of an
			      <code>Instr</code>. 
			      2. <code>v</code> can visit instructions
			      of type <code>Instr</code>.
	<BR> <B>modifies:</B> <code>v</code>
	<BR> <B>effects:</B> Sends <code>v</code> into
	                     <code>root</code> and the
			     <code>BasicBlock</code>s linked to by
			     <code>root</code>, performing
			     <code>v</code>'s transfer function on 
			     each <code>BasicBlock</code> in turn,
			     tracking for when no change occurs, at
			     which point the analysis is complete.    
     */
    public static void worklistSolver(BasicBlock root, DataFlowBasicBlockVisitor v) {
	WorkSet w = new WorkSet();
	w.push(root);
	while (!w.isEmpty()) {
	    v.changed = false;
	    BasicBlock b = (BasicBlock) w.pull();
	    b.visit(v);
	    v.addSuccessors(w, b);
	}
    } 
    
}
