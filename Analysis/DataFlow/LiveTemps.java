// LiveTemps.java, created Mon Nov  8 23:35:55 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Analysis.BasicBlock;
import harpoon.IR.Properties.UseDefer;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.CloneableIterator; 
import harpoon.Util.ReverseIterator;
import harpoon.Util.Util; 
import harpoon.Util.Collections.SetFactory;
import harpoon.Util.Collections.BitSetFactory;

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
 * @version $Id: LiveTemps.java,v 1.1.2.25 2001-04-11 08:02:47 pnkfelix Exp $
 */
public class LiveTemps extends LiveVars.BBVisitor {
    // may be null; code using this should check
    private Set liveOnProcExit;

    protected SetFactory mySetFactory;

    // calculates use/def information for the IR passed in.
    protected UseDefer ud;

    /** Produces a default live variable analysis object and solves
	it.  Uses <code>ud</code> to define Temp references in
	elements of <code>code</code>. elements in <code>code</code>
	must implement <code>CFGraphable</code>, and
	<code>liveOnExit</code> must be a Set of Temps (mapped to by
	<code>ud</code>) that are live on exit from
	<code>code</code>. 
     */
    public static LiveTemps make(HCode code, UseDefer ud, 
				 Set liveOnExit) {
	BasicBlock.Factory bbf = new BasicBlock.Factory(code);
	LiveTemps lt = new LiveTemps(bbf, liveOnExit, ud);
	Solver.worklistSolve
	    // (bbFact.preorderBlocksIter(),
	    (new harpoon.Util.ReverseIterator(bbf.postorderBlocksIter()),
	     lt);
	return lt;
    }

    /** Produces a default live variable analysis object and solves
	it.  elements in <code>code</code> must implement
	<code>UseDefable</code>, <code>CFGraphable</code>, etc, and
	<code>liveOnExit</code> must be a Set of Temps that are live
	on exit from <code>code</code>.
     */
    public static LiveTemps make(HCode code, Set liveOnExit) {
	return make(code, UseDefer.DEFAULT, liveOnExit);
    }
    
    /** Constructs a new <code>LiveTemps</code> for <code>basicblocks</code>.
	<BR> <B>requires:</B> <OL>
	     <LI> <code>basicblocks</code> is a
	          <code>Iterator</code> of <code>BasicBlock</code>s,	       
	     <LI> <code>ud</code> contains use/def information for all
	          of all of the instructions in
	          <code>basicblocks</code>.
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
    */	     
    public LiveTemps(BasicBlock.Factory bbFact, Set liveOnProcExit,
		     UseDefer ud) {
	super(bbFact, false);
	this.ud = ud;

	// duplicating code from LiveVars.java
	Set universe = findUniverse(bbFact.blockSet());
	universe.addAll(liveOnProcExit);
	
	if (false) {
	    // FSK: cannot do this (yet) because universe is made up
	    // of Temps from different sources (backend
	    // register-temp-factory versus hmethod temp-factory),
	    // resulting in index collisions.
	    // Look into making a CompositeIndexer later that can
	    // combine the two indexers; for now go back to the
	    // built-in default.
	    mySetFactory = new BitSetFactory(universe, Temp.INDEXER);
	} else {
	    mySetFactory = new BitSetFactory(universe);
	}

	// KEY difference: set liveOnProcExit before calling initBBtoLVI
	this.liveOnProcExit = liveOnProcExit;

	initializeBBtoLVI( bbFact.blockSet(), mySetFactory );	
    }
    
    public LiveTemps(BasicBlock.Factory bbFact, Set liveOnProcExit) {
	this(bbFact, liveOnProcExit, UseDefer.DEFAULT);
    }

    /** Constructor for LiveVars that allows the user to pass in their
	own <code>SetFactory</code> for constructing sets of the
	<code>Temp</code>s in the analysis.  
	<BR> <B>requires:</B> All <code>Temp</code>s in
	     <code>basicBlocks</code> are members of the universe for
	     <code>tempSetFact</code>.
	     
	<BR> Doc TODO: Add all of the above documentation from the
	     standard ctor.
    */
    public LiveTemps(BasicBlock.Factory bbFact,
		     Set liveOnProcExit, 
		     SetFactory tempSetFact,
		     UseDefer ud) {
	// calling "special" ctor so that I can set up
	// liveOnProcExit before calling anything else.
	super(bbFact, false);

	this.ud = ud;

	mySetFactory = tempSetFact;

	// KEY difference: set liveOnProcExit before calling initBBtoLVI
	this.liveOnProcExit = liveOnProcExit;

	initializeBBtoLVI( bbFact.blockSet(), tempSetFact );
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
	// live_before(hce) <-- 
	//      UNION( USE(hce), (live_after(hce) - DEF(hce)))
	Set liveBefore = mySetFactory.makeSet(this.getLiveAfter(hce)); 
	liveBefore.removeAll(ud.defC(hce));
	liveBefore.addAll(ud.useC(hce));
	
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
	// System.out.println("FSK: getLiveAfter called");

	BasicBlock bb = bbFact.getBlock(hce);
    Util.assert(bb != null, "Code " + hce + " is not in a bb");

	    Set liveAfter = 
		mySetFactory.makeSet(this.getLiveOnExit(bb));

	    // Starting from the last element in hce's basic block,
	    // traverse the block in reverse order, until hce is
	    // reached.  Each step updates the liveness information.

	    java.util.List stms = bb.statements();
	    // System.out.print(" M"+stms.size());
	    java.util.ListIterator iter = stms.listIterator(stms.size());
	    
	    while(iter.hasPrevious()) {
		HCodeElement current = (HCodeElement) iter.previous();
		
		// System.out.println("doing live after for "+current);

		if (hce == current) {
		    return mySetFactory.makeSet(liveAfter);
		}

		// update set for before 'current'
		liveAfter.removeAll(ud.defC(current)); 
		liveAfter.addAll(ud.useC(current)); 
	    }
	
	    throw new RuntimeException("while loop reached end!!!");
   

    }

    /** Constructs a <code>Set</code> of all of the <code>Temp</code>s
	in <code>blocks</code>.  
	<BR> <B>requires:</B> <OL>
	     <LI> <code>blocks</code> is an <code>Iterator</code> of
	          <code>BasicBlock</code>s. 
	     </OL>
	<BR> <B>modifies:</B> <code>blocks</code>
	<BR> <B>effects:</B> Iterates through all of the instructions
	     contained in each element of <code>blocks</code>, adding
	     each instruction's useC() and defC() to a universe of
	     values, returning the universe after all of the
	     instructions have been visited.
    */
    protected Set findUniverse(Set blockSet) {
	Iterator blocks = blockSet.iterator();
	HashSet temps = new HashSet();
	while(blocks.hasNext()) {
	    BasicBlock bb = (BasicBlock) blocks.next();
	    Iterator useDefs = bb.statements().iterator();
	    while(useDefs.hasNext()) {
		HCodeElement h = (HCodeElement) useDefs.next();
		temps.addAll(ud.useC(h));
		temps.addAll(ud.defC(h));
	    }
	}
	return temps;	
    }

    /** Initializes the USE/DEF information for 'bb' and stores in in
	the returned <code>LiveVarInfo</code>.
    */
    protected LiveVarInfo makeUseDef(BasicBlock bb, SetFactory sf) {
	LiveVarInfo info = new LiveVarInfo(sf);

	if (liveOnProcExit != null &&
	    bb.nextLength() == 0) {

	    // Check that last instr is a method exit point
	    // System.out.println("FSK found last bb: " + bb.getLast());

	    info.lvOUT.addAll(liveOnProcExit);
	    info.lvIN.addAll(liveOnProcExit);
	}

	Iterator instrs = bb.statements().listIterator();	
	
	while (instrs.hasNext()) {
	    HCodeElement h = (HCodeElement) instrs.next();
	    
	    // check for usage before definition, to handle the case
	    // of a <- a+1 (which should lead to a USE(a), *not* a DEF
	    
	    // USE: set of vars used in block before being defined
	    for(int i=0; i<ud.use(h).length; i++) {
		Temp t = ud.use(h)[i];
		if ( !info.def.contains(t) ) {
		    info.use.add(t);
		}
	    }	    
	    // DEF: set of vars defined in block before being used
	    for(int i=0; i<ud.def(h).length; i++) {
		Temp t = ud.def(h)[i];
		if ( !info.use.contains(t) ) {
		    info.def.add(t);
		}
	    }
	}
	return info;
    }

    /** Returns a String containing a human-readable version of the
	analysis results.
    */
    public String dumpElems() {
	StringBuffer sb = new StringBuffer();
	sb.append("\t LIVE ON EXIT:"+liveOnProcExit+"\n");
	
	sb.append("\t START LiveVarInfos:\n"+ dump(false));


	sb.append("\t FINIS LiveVarInfos");
	int j = 0;
	for(Iterator i=bbFact.getHCode().getElementsI(); i.hasNext();){
	    HCodeElement h = (HCodeElement) i.next();
	    sb.append(j+"\t"+h.toString()+"\n");
	    sb.append("\t"+"USES:"+ud.useC(h)+"\tDEFS:"+ud.defC(h));
	    sb.append("\t"+getLiveAfter(h)+"\n");
	    j++;
	}
	return sb.toString();
    }
}
