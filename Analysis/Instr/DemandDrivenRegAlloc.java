// RegAlloc.java, created Mon Mar 29 16:47:25 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Temp.Temp;
import harpoon.IR.Assem.Instr;
import harpoon.Backend.Generic.RegFileInfo;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Code;
import harpoon.Analysis.Maps.UseDefMap;
import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.Maps.Derivation;

import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.util.ListIterator;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

/** <code>DemandDrivenRegAlloc</code> performs 
    <A HREF="http://lm.lcs.mit.edu/~pnkfelix/DemandDrivenRegAlloc.pdf"> 
    Demand-Driven Register Allocation</A> for a set of
    <code>Instr</code>s in a <code>Code</code>.

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: DemandDrivenRegAlloc.java,v 1.1.2.17 2001-07-06 16:28:08 pnkfelix Exp $ 
*/
public class DemandDrivenRegAlloc extends RegAlloc {

    UseDefMap uses;

    // maps basicblocks to the variables allocated to registers
    // entering the block
    private Map bbToAllocatedMap;

    // maps basicblocks to the live variables entering the block
    private Map bbToLiveVarMap;
    
    // TODO: should probably add a local var storing info on
    // LiveVarAnalysis. 

    
    /** Creates a <code>DemandDrivenRegAlloc</code>. 
	
	<BR> <B>Design Issue:</B> should there be a RegAlloc object
	     for every method, or just for every machine target?  For
	     now it seems associating a new one with every method will
	     save a lot of headaches.

	<BR> <B>requires:</B> 
	     1. Local register allocation has been run on
	        <code>code</code> already. 
    */
    protected DemandDrivenRegAlloc(Code code) {
        super(code);
	uses = new harpoon.Analysis.UseDef( /*code*/ );

	bbToAllocatedMap = new HashMap(); // built during allocation 

	bbToLiveVarMap = findLiveVars(code); // invariant during allocation
    }
    
    /** stub implementation. */
    protected Derivation getDerivation() { return null; }
   
    protected void generateRegAssignment() {

	/* procedure for register allocation for a procedure (taken from
	   Demand-Driven Register Allocation):

	   (assumes that Local Allocation has already been performed)
	*/
	
	/*
	   2. For all loops, l. in the procedure, from innermost to
	   outermost do the following: 
	   (a) Attempt to remove loads in l: 
	      i.   Compute delta-estimates for all instructions in the
	           loop (with ComputeDeltas() ).
	      ii.  For each load of a register candidate in the loop,
	           estimate the chance that the value will reach the
		   load in a register.  This is the product of all the
		   delta-estimates for that candidate in the load's
		   register-live-range.  
	      iii. If no loads have an estimate greater than 0, then
	           quit processing loads.
	      iv.  Otherwise, remove the load with the greatest figure
	           of merit, and allocate the candidate a register
		   across its entire register-live-range.  (This may
		   require putting a load in the loop's preheader.)
	  (b) Attempt to remove stores in l:
	      i.   Compute delta-estimates for all instructions in the
	           loop (with ComputeDeltas() ).
	      ii.  For each store of a register candidate in the loop,
	           estimate the chance that the stored value will
		   reach *all* remaining reachable loads in l, and
		   those postexits in which the value is live in a
		   register.  This is the product of all the
		   delta-estimates for that candidate along all paths
		   to these loads and postexits.
	      iii. If no stores have an estimate greater than 0, then
	           quit processing stores.
	      iv.  Otherwise, remove the store with the greatest
	           figure of merit, and allocate the candidate a
		   register along all paths to the postexits.  This
		   requires putting stores in the loop's postexits.
	*/


	/*
	
	3. Remove loads that are not in any loop (Use techniques
           described in (2a) above.)
        4. Remove stores that are not in any loop (Use techniques
           described in (2b) above).

	*/


	/*

       5. Assign registers to all candidates allocated registers using 
          the Graph Coloring Framework.

	*/

	return;
    }
    

    /** Delta computing function. Taken from the paper "Demand-Driven
	Register Allocation." 
	<BR> <B>requires:</B> <code>BasicBlock</code> is a set of
	      <code>Instr</code>s. 
	<BR> <B>effects:</B> returns a 
	Map[TempInstrPair, Double] mapping 
	(instr x temp) -> deltaValue
    */
    private Map computeDeltas(BasicBlock block) {
	// initialize configuration
	
	// { variables allocated registers entering block };
	Set allocated = (Set) bbToAllocatedMap.get(block);
	
	// { live variables entering block } - allocated
	Set candidates = new HashSet();
	candidates.addAll((Set)bbToLiveVarMap.get(block));

	candidates.removeAll(allocated);
	
	// min( REGISTERS - |allocated|, |candidates| )
	int possibly = Math.min(frame.getRegFileInfo().getAllRegisters().length - 
				allocated.size(), candidates.size() );
	
  	// REGISTERS - ( |allocated| + possibly )
	int unallocated = frame.getRegFileInfo().getAllRegisters().length - 
	    (allocated.size() + possibly);

	// *** Implementation Specific local variables *** 
	
	// maps register uses in a particular instruction to the
	// variable that the register is storing.
	Map regXinstrToVarMap = new HashMap();
	

	// Iterate over instructions in order 
	
        ListIterator instrs = block.statements().listIterator();
	while(instrs.hasNext()) { 
	    Instr instr = (Instr) instrs.next();
	    double delta;
	    
	    { // BELOW: Last use of a register frees it
		Vector regsFreedByInstr = new Vector(); 
		for (int i=0; i<instr.use().length; i++) {
		    if (isRegister(instr.use()[i]) &&  
			lastUse( instr.use()[i], instr, instrs)){ 
			regsFreedByInstr.addElement(instr.use()[i]);
		    }
		}
		// for all f of { registers freed by instr }
		for (int i=0;i<regsFreedByInstr.size();i++) { 
		    Temp f = (Temp) regsFreedByInstr.get(i);
		    
		    // Let t be value held in f
		    Temp t= (Temp) 
			regXinstrToVarMap.get
			(new TempInstrPair(instr, f)) ; 
		    
		    if (t != null) {
			allocated.remove(t);
			if(true) { // if t is a live variable
			    candidates.add(t);
			    possibly++;
			} else {
			    unallocated++;
			}
		    } else {
			unallocated++;
		    }
		}
	    }


	    { // BELOW: First use of a register allocates it
		if (true) { // if instr allocated r, a register 
		    Temp v=null; // Let v be value held in r
		    allocated.add(v);
		    if (true) { // if v holds the value of variable
				// (ie not a Temporary)  
			candidates.remove(v);
		    }
		    if (possibly > candidates.size() ) {
			// Only occurs when possibly == |candidates| prior
			// to satisfying preceding conditional
			delta = 1.0;
			possibly--;
		    } else if (unallocated > 0) {
			// Allocating an empty register cannot kill
			// anything
			delta = 1.0;
			unallocated--;
		    } else {
			delta = ((double)(possibly-1)) /
			    ((double)possibly);
			possibly--;
		    }
		} else {
		    delta = 1.0;
		}
	    }
	    
	    for (;;) { // for all v elem candidates
		// deltaTable[instr][v] = delta
	    }
	}

	return null;

    }	    

    private Map findLiveVars(Code code) {
	UseDefMap useMap = new harpoon.Analysis.UseDef(/*code*/);
	return null;
    }

}

