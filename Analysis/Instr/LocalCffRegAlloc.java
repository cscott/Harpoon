// LocalRegAlloc.java, created Thu Apr  8 01:02:19 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Code;
import harpoon.Analysis.DataFlow.BasicBlock;
import harpoon.Analysis.DataFlow.DataFlowBasicBlockVisitor;
import harpoon.Analysis.DataFlow.InstrSolver;
import harpoon.Analysis.DataFlow.ReachingDefs;
import harpoon.Temp.Temp;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;
import harpoon.Util.CloneableIterator;

import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Iterator;

/** <code>LocalRegAlloc</code> performs Local Register Allocation for
    a given set of <code>Instr</code>s.  It uses the
    conservative-furthest-first algorithm laid out in the paper <A
    HREF="http://lm.lcs.mit.edu/~pnkfelix/OnLocalRegAlloc.ps.gz">
    "On Local Register Allocation"</A> and <A
    HREF="http://lm.lcs.mit.edu/~pnkfelix/hardnessLRA.ps">Hardness and
    Algorithms for Local Register Allocation</A> as the basis for the
    algorithm it uses to allocate and assign registers.
    
    @author  Felix S Klock <pnkfelix@mit.edu>
    @version $Id: LocalCffRegAlloc.java,v 1.1.2.12 1999-05-28 01:51:51 pnkfelix Exp $ 
*/
public class LocalCffRegAlloc extends RegAlloc {

    
    
    /** Creates a <code>LocalRegAlloc</code>. 
	
    */
    public LocalCffRegAlloc(Frame frame, Code code) {
        super(frame, code);
    }

    /** Assigns registers in the code for <code>this</code>.
	
	<BR> <B>effects:</B> Locally allocates registers for the
	     values defined and used in the basic blocks of the code
	     in <code>this</code>.  Values will be preserved in the
	     code; all used values are loaded at the begining of the
	     basic block they're used in, and all modified values are
	     stored at the end of the basic block they're used in.
    */
    protected Code generateRegAssignment() {
	Instr root = (Instr) code.getRootElement();
	BasicBlock block = BasicBlock.computeBasicBlocks(root);
	
	// first calculate Live Variables for code
	Iterator iter = new CloneableIterator(BasicBlock.basicBlockIterator(block));
	LiveVars livevars =  new LiveVars(iter.clone());
	InstrSolver.worklistSolver(block, livevars);
	
	// Now perform local reg alloc on each basic block
	while(iter.hasNext()) {
	    BasicBlock b = (BasicBlock) iter.next();
	    localRegAlloc(b, livevars);
	}
	return null;
    }

    /** Performs local register allocation for <code>bb</code>. 
	<BR> <B>requires:</B> 
	     1. <code>bb</code> is a <code>BasicBlock</code> of
	        <code>Instr</code>s.

    */
    private void localRegAlloc(BasicBlock bb, LiveVars lv) {
	CloneableIterator iter = new CloneableIterator(bb.listIterator());
	Temp[] registers = frame.getGeneralRegisters();
	
	// if reg(i) is holding a value, values[ i ] will have that
	// value.  Else, values[ i ] will have null. 
	Temp[] values = new Temp[ registers.length ]; 

	while(iter.hasNext()) {
	    Instr instr = (Instr) iter.next();
	    
	defs:	    
	    for (int d=0; d<instr.dst.length; d++) {
		Temp dest = instr.dst[d];		
		// go through available registers
		for (int i=0; i<registers.length; i++) {
		    if (values[i] == null) { 
			// found a register to store 'dest' in  
			values[i] = dest;			
			instr.dst[d] = registers[i];
			break defs;
		    }
		} 
		// if we got this far, we didn't find a free register

		// Evict a value (storing it to memory).  Choose the
		// value based on furthest first (this is where the
		// CFF comes in) 
		
		int index = findFurthest((Iterator)iter.clone(), values);
				
		InstrMEM minstr = 
		    new InstrMEM(null, null, null, 
                        new Temp[] { values[index] }, 
                        new Temp[] { registers[index] });
		
		// TODO: now 'dest' can be stored in
		// registers[index].  Update auxillary data structures
		// accordingly. 

	    }
	    for (int u=0; u<instr.use().length; u++) {
		Temp use = instr.use()[u];		
		for (int i=0; i<registers.length; i++) {
		    if (values[i] == null) {
			values[i] = use;
			instr.src[u] = registers[i];
		    } else {
			// Evict a value (storing it to memory).
			int index = findFurthest
			    ((Iterator)iter.clone(), values, lv); 
			
			InstrMEM store = new InstrMEM (null, null, null, 
				new Temp[] { values[index] },
				new Temp[] {  registers[index] });
			
			// TODO: put 'use' into registers[index],
			// updating auxillary data structures
			// accordingly 
		    }
		}
	    }
	}
    } // end localRegAlloc(BasicBlock, LiveVars)

}




