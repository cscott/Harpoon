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
    "On Local Register Allocation"</A> as the basis for the algorithm
    it uses to allocate and assign registers.
    
    @author  Felix S Klock <pnkfelix@mit.edu>
    @version $Id: LocalCffRegAlloc.java,v 1.1.2.11 1999-05-27 23:00:27 pnkfelix Exp $ 
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
	
	// first calculate Reaching Definitions for code
	DataFlowBasicBlockVisitor reachingDefs =  new ReachingDefs(root);
	InstrSolver.worklistSolver(block, reachingDefs);
	
	// Now perform local reg alloc on each basic block
	WorkSet blocksToAnalyze = new WorkSet();
	Set finishedBlocks = new WorkSet();

	blocksToAnalyze.add(block);

	while(! blocksToAnalyze.isEmpty()) {
	    BasicBlock b = (BasicBlock) blocksToAnalyze.pull();
	    localRegAlloc(b);
	    finishedBlocks.add(b);

	    Enumeration enum = b.next();
	    while(enum.hasMoreElements()) {
		Object o = enum.nextElement();
		if (!finishedBlocks.contains(o)) {
		    blocksToAnalyze.add(o);
		}
	    }
	    
	}
	return null;
    }

    /** Performs local register allocation for <code>bb</code>. 
	<BR> <B>requires:</B> 
	     1. <code>bb</code> is a <code>BasicBlock</code> of
	        <code>Instr</code>s.

	NOTE: CHANGE THIS!  Should not be using Reaching Defs, but
	rather liveness info (IE, don't care if a Def reaches the
	block; we want to know if there is Use after bb).
    */
    private void localRegAlloc(BasicBlock bb) {
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
			int index = findFurthest((Iterator)iter.clone(), values); 
			
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
    }

    /** 
	<BR> <B>requires:</B> 
	     1. <code>iter</code> iterates through a linear list of
	                          <code>Instr</code>s. 
	     2. <code>values</code> f
	<BR> <B>modifies:</B> <code>iter</code>
	<BR> <B>effects:</B> Returns the index 'i' of the
	     <code>Temp</code>, <code>values</code>[i], which is the
	     one that 

	<BR> NOTE: to perform true CFF, we need to know which
	variables were *DEFINED* in this round, versus just being
	used.  Thus we need a more complex data structure.  For now,
	just deal without checking for clean variables.  
    */
    private int findFurthest(Iterator iter, Temp[] values) {
	int count=0;
	
	final int REDEFINED_BEFORE_USED = -1;
	final int UNUSED_AND_UNDEFINED = 0;
	// Dist definitions: 
	// (-1) means that the value will be redefined before usage
	// (0)  means that the value has not been used or redefined
	// (x) and x > 0 ;  means that the value will be used x
	//          steps in the future 
	int[] valToDist = new int[values.length];
	
	while(iter.hasNext()) {
	    Instr i = (Instr) iter.next();
	    count++;
	    Temp[] dsts = i.def();
	    Temp[] uses = i.use();
	    for (int j=0; j<values.length; j++) {
		for (int u=0; u<uses.length; u++) {
		    if (values[j] != null &&
			values[j].equals(uses[u]) &&
			valToDist[j] != REDEFINED_BEFORE_USED) {
			valToDist[j] = count;
		    }
		}
		for (int d=0; d<dsts.length; d++) {
		    if (values[j] != null && 
			values[j].equals(dsts[d]) &&
			valToDist[j] == UNUSED_AND_UNDEFINED) {
			valToDist[j] = REDEFINED_BEFORE_USED;
		    }
		}
	    }
	}
	
	// check if any slot is dead or unused
	for (int i=0; i<valToDist.length; i++) {
	    if (valToDist[i] == UNUSED_AND_UNDEFINED ||
		valToDist[i] == REDEFINED_BEFORE_USED) {
		return i;
	    }
	}

	// else find the set of furthest variables
	Set indexSet = new HashSet();
	int distance = 0;
	for (int i=0; i<valToDist.length; i++) {
	    if (valToDist[i] == distance) {
		indexSet.add(new Integer(i));
	    } else if (valToDist[i] > distance) {
		indexSet.clear();
		indexSet.add(new Integer(i));
		distance = valToDist[i];
	    }
	}
	
	// now use Conservative Furthest-First to select which
	// variable to evict 
	//
	// TODO: Fix the below code to
	//       1. Use LiveVariable Analysis
	Iterator setIter;


	// Is there a variable thats not alive?
	setIter = indexSet.iterator();
	while (setIter.hasNext()) {
	    Integer index = (Integer) setIter.next();
	    if (false) { // if (alive(values[index.intValue()])
		return index.intValue();
	    }
	}

	// Is there a variable thats clean
	setIter = indexSet.iterator();
	while (setIter.hasNext()) {
	    Integer index = (Integer) setIter.next();
	    if (false) { // if (clean(values[index.intValue()])
		return index.intValue();
	    }
	}

	// If there all are alive and dirty, choose arbitrarily
	setIter = indexSet.iterator();
	Integer index = (Integer) setIter.next();
	return index.intValue();
	
    }
}





