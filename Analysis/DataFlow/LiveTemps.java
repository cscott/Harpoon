// LiveTemps.java, created Mon Nov  8 23:35:55 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Analysis.BasicBlock;
import harpoon.IR.Properties.HasEdges; 
import harpoon.IR.Properties.UseDef;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.CloneableIterator; 
import harpoon.Util.Util; 
import harpoon.Util.Collections.SetFactory;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;


/**
 * <code>LiveTemps</code> is an extension of <code>LiveVars</code> for
 * performing liveness analysis on <code>Temp</code>s.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LiveTemps.java,v 1.1.2.3 1999-11-29 02:21:09 duncan Exp $
 */
public class LiveTemps extends LiveVars {
    private Map hceToBB; 
    
    /** Constructs a new <code>LiveTemps</code> for <code>basicblocks</code>.
	<BR> <B>requires:</B> <OL>
	     <LI> <code>basicblocks</code> is a
	          <code>Iterator</code> of <code>BasicBlock</code>s,	       
	     <LI> All of the instructions in
	          <code>basicblocks</code> implement
		  <code>UseDef</code>
	     <LI> No element of <code>basicblocks</code> links to a
	          <code>BasicBlock</code> not contained within
		  <code>basicblocks</code>
	     <LI> No <code>BasicBlock</code> is repeatedly iterated
	          by <code>basicblocks</code> 
		  </OL>
	 <BR> <B>modifies:</B> <code>basicblocks</code>
	 <BR> <B>effects:</B> constructs a new
	      <code>BasicBlockVisitor</code> and initializes its
	      internal datasets for analysis of the
	      <code>BasicBlock</code>s in <code>basicblocks</code>,
	      iterating over all of <code>basicblocks</code> in the
	      process.
	 @param basicblocks <code>Iterator</code> of <code>BasicBlock</code>s to be analyzed.
	 @param liveOnProcExit <code>Set</code> of <code>Temp</code>s that are live on exit from the method (for example, r0 for assembly code). 

	 <P> FSK: Note to Self: Actual SUPPORT for liveOnProcExit
	     would be nice... 
    */	     

    public LiveTemps(Iterator basicBlocks, Set liveOnProcExit) {
        super(basicBlocks); 
    }
    
    

    /** Constructor for LiveVars that allows the user to pass in their
	own <code>SetFactory</code> for constructing sets of the
	<code>Temp</code>s in the analysis.  
	<BR> <B>requires:</B> All <code>Temp</code>s in
	     <code>basicBlocks</code> are members of the universe for
	     <code>tempSetFact</code>.
	     
	<BR> Doc TODO: Add all of the above documentation from the
	     standard ctor.
	     
        <BR> FSK: Note to Self: Actual SUPPORT for liveOnProcExit would be 
	nice... 
    */
    public LiveTemps(Iterator basicBlocks, 
		     Set liveOnProcExit, 
		     SetFactory tempSetFact) {
	super( basicBlocks, tempSetFact );
    }

    /** Returns the <code>Set</code> of <code>Temp</code>s that are
	live on on entry to <code>hce</code>.
	<BR> <B>requires:</B> A DataFlow Equation Solver has been run
	     to completion on the graph of <code>BasicBlock</code>s
	     containing some block that contains <code>hce</code>,
	     with <code>this</code> as the
	     <code>DataFlowBasicBlockVisitor</code>. 
	<BR> <B>effects:</B> Returns a <code>Set</code> of
	     <code>Temp</code>s that are live on entry to
	     <code>hce</code>. 
    */
    public Set getLiveBefore(HCodeElement hce) {
	// hce must implement UseDef.  
	UseDef udHce = (UseDef)hce; 
	// live_before(hce) <-- UNION(USE(hce), (live_after(hce) - DEF(hce)))
	Set liveBefore = this.getLiveAfter(hce); 
	liveBefore.removeAll(udHce.defC()); 
	liveBefore.addAll(udHce.useC());
	
	return liveBefore; 
    }
    
    /** Returns the <code>Set</code> of <code>Temp</code>s that are
	live on exit from <code>hce</code>.
	<BR> <B>requires:</B> A DataFlow Equation Solver has been run
	     to completion on the graph of <code>BasicBlock</code>s
	     containing some block that contains <code>hce</code>,
	     with <code>this</code> as the
	     <code>DataFlowBasicBlockVisitor</code>.
	<BR> <B>effects:</B> Returns a <code>Set</code> of
	     <code>Temp</code>s that are live on exit from
	     <code>hce</code>. 
    */
    public Set getLiveAfter(HCodeElement hce) {
	Util.assert(this.hceToBB.containsKey(hce)); 

	BasicBlock bb        = (BasicBlock)this.hceToBB.get(hce); 
	Set        liveAfter = this.getLiveOnExit(bb); 
	HasEdges   current   = bb.getLast(); 

	// Starting from the last element in hce's basic block, traverse
	// the block in reverse order, until hce is reached.  Each step 
	// updates the liveness information.
	for (; current != hce; current = (HasEdges)current.pred()[0].from()) {
	    UseDef udCurrent = (UseDef)current;
	    liveAfter.addAll(udCurrent.useC()); 
	    liveAfter.removeAll(udCurrent.defC()); 
	}

	return liveAfter; 
    }

    /** Constructs a <code>Set</code> of all of the <code>Temp</code>s
	in <code>blocks</code>.  
	<BR> <B>requires:</B> <OL>
	     <LI> <code>blocks</code> is an <code>Iterator</code> of
	          <code>BasicBlock</code>s. 
	     <LI> All of the instructions in each element of
	          <code>blocks</code> implement <code>UseDef</code>. 
	     </OL>
	<BR> <B>modifies:</B> <code>blocks</code>
	<BR> <B>effects:</B> Iterates through all of the instructions
	     contained in each element of <code>blocks</code>, adding
	     each instruction's useC() and defC() to a universe of
	     values, returning the universe after all of the
	     instructions have been visited.
    */
    protected Set findUniverse(Iterator blocks) {
	HashSet temps = new HashSet();
	while(blocks.hasNext()) {
	    BasicBlock bb = (BasicBlock) blocks.next();
	    Iterator useDefs = bb.iterator();
	    while(useDefs.hasNext()) {
		UseDef useDef = (UseDef) useDefs.next();
		temps.addAll(useDef.useC());
		temps.addAll(useDef.defC());
	    }
	}
	return temps;	
    }

    /** Initializes the mapping of <code>HCodeElement</code>s to 
	<code>BasicBlocks</code>.  
	
	@param basicblocks  an <code>Iterator</code> of the basic blocks to be
	                    analyzed. 
    */
    protected final void initializeHceToBB(Iterator blocks) { 
	if (blocks.hasNext()) { 
	    this.hceToBB = ((BasicBlock)blocks.next()).getHceToBB();
	} else { 
	    this.hceToBB = new HashMap();
	}
    }

    /** Initializes the USE/DEF information for 'bb' and stores in in
	the returned <code>LiveVarInfo</code>.
    */
    protected LiveVarInfo makeUseDef(BasicBlock bb, SetFactory sf) {
	LiveVarInfo info = new LiveVarInfo(sf);
	Iterator instrs = bb.listIterator();	
	
	while (instrs.hasNext()) {
	    UseDef ud = (UseDef) instrs.next();
	    
	    // check for usage before definition, to handle the case
	    // of a <- a+1 (which should lead to a USE(a), *not* a DEF
	    
	    // USE: set of vars used in block before being defined
	    for(int i=0; i<ud.use().length; i++) {
		Temp t = ud.use()[i];
		if ( !info.def.contains(t) ) {
		    info.use.add(t);
		}
	    }	    
	    // DEF: set of vars defined in block before being used
	    for(int i=0; i<ud.def().length; i++) {
		Temp t = ud.def()[i];
		if ( !info.use.contains(t) ) {
		    info.def.add(t);
		}
	    }
	}
	return info;
    }
}
