// LocalRegAlloc.java, created Thu Apr  8 01:02:19 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Code;
import harpoon.Analysis.DataFlow.BasicBlock;
import harpoon.Temp.Temp;
import harpoon.IR.Assem.Instr;
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
    HREF="http://dimacs.rutgers.edu/TechnicalReports/abstracts/1997/97-33.html">
    "On Local Register Allocation"</A> as the basis for the algorithm
    it uses to allocate and assign registers.
    
    @author  Felix S Klock <pnkfelix@mit.edu>
    @version $Id: LocalCffRegAlloc.java,v 1.1.2.1 1999-04-20 19:49:01 pnkfelix Exp $ 
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
	// cloning to pervent mutation of original code passed in.
	try {
	    Code newCode = (Code) code.clone(code.getMethod());
	} catch (CloneNotSupportedException e) {
	    Util.assert (false, 
			 "Wasn't able to clone method" + 
			 " for register allocation");
	}
	Instr root = (Instr) code.getRootElement();
	BasicBlock block = BasicBlock.computeBasicBlocks(root);
	
	// // first calculate Reaching Definitions for code
	// BasicBlockVisitor reachingDefs =  new ReachingDefs(root);
	// InstrSolver.worklistSolver(block, reachingDefs);
	
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

	// if reg is holding a value, values[regIndex] will have that
	// value.  Else, values will have null. 
	Temp[] values = new Temp[ registers.length ]; 

	while(iter.hasNext()) {
	    Instr instr = (Instr) iter.next();
	    
	defs:	    
	    for (int d=0; d<instr.def().length; d++) {
		Temp dest = instr.def()[d];		
		for (int i=0; i<registers.length; i++) {
		    if (values[i] == null) {
			values[i] = dest;
			
			// TODO: generate a new Instr here with <dest> 
			// replaced by registers[i]
			
			break defs;
		    }
		} 
		// Evict a value (storing it to memory).
		// Choose the value based on furthest first
		// (this is where the CFF comes in)
		
		int index = findFurthest((Iterator)iter.clone(), values);
		

		// TODO: make a new InstrMEM 
		// storing registers[index] -> values[index]
		
		// TODO: generate a new Instr
		
		
		
		
		for (int u=0; u<instr.use().length; u++) {
		    Temp use = instr.use()[u];		
		    for (int i=0; i<registers.length; i++) {
			if (values[i] == null) {
			    values[i] = use;
			    // TODO: generate a new Instr here with <use> 
			    // replaced by registers[i]
			} else {
			    // Evict a value (storing it to memory).
			    // Choose the value based on furthest first
			    // (this is where the CFF comes in)
			}
		    }
		}
	    }
	}
    }

    /** 
	<BR> <B>requires:</B> 
	     1. <code>iter</code> is an <code>Iterator</code> of
	        <code>Instr</code>s.
	     2. <code>values</code> f
	<BR> <B>modifies:</B> <code>iter</code>



	NOTE: to perform true CFF, we need to know which variables
	were *DEFINED* in this round, versus just being used.  Thus we
	need a more complex data structure.  For now, just deal
	without checking for clean variables.
     */
    private int findFurthest(Iterator iter, Temp[] values) {
	int count=0;
	
	// Dist definitions: 
	// (-1) means that the value will be redefined before usage
	// (0)  means that the value has not been used or redefined
	// (#)  implying # > 0,  means that the value will be used #
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
			valToDist[j] != -1) {
			valToDist[j] = count;
		    }
		}
		for (int d=0; d<dsts.length; d++) {
		    if (values[j] != null && 
			values[j].equals(dsts[d]) &&
			valToDist[j] == 0) {
			valToDist[j] = -1;
		    }
		}
	    }
	}
	
	// check if any slot is dead or unused
	for (int i=0; i<valToDist.length; i++) {
	    if (valToDist[i] <= 0) {
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





