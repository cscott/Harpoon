// LiveVars.java, created Wed May 26 16:53:26 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.Liveness;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;

import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;

import harpoon.Util.CloneableIterator;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.Collections.SetFactory;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <code>LiveVars</code> performs Liveness Analysis for the variables
 * in the <code>HCode</code>s passed to it.  
 *
 * <P> It attempts to do this in efficient manner by <OL>
 * <LI> grouping the statements into <code>BasicBlock</code>s.
 * <LI> using bit strings as the underlying representation for the
 *      <code>Set</code>s it works with.
 * <LI> using the dataflow analysis framework which provides
 *      termination guarantees (as long as the transfer function and
 *      lattice of values provided meet certain conditions).
 * </OL>
 *
 * <P> However, the implementation is also meant to be parameterized
 * and flexible.  So it allows the user to pass in their own
 * <code>CFGrapher</code> and <code>UseDefer</code> for the code to be
 * analyzed.
 *
 * <P> <B>NOTE:</B> I need to document what constraints there are on
 * the <code>UseDefer</code> and <code>CFGrapher</code> parameters to
 * preserve the termination guarantees of the analysis.  For the time
 * being, people who have no experience with Dataflow Analysis code
 * should avoid passing in strange <code>CFGrapher</code>s and
 * <code>UseDefer</code>s 
 *
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: LiveVars.java,v 1.1.2.18 2000-02-01 02:57:52 pnkfelix Exp $ */
public abstract class LiveVars extends Liveness {
    
    private static final boolean DEBUG = false; 

    LiveTemps lv;

    /** Constructs a new <code>LiveVars</code>.
	Note that since the dataflow analysis is done during
	construction, this can take a while.
     */
    public LiveVars(HCode hc, CFGrapher gr, UseDefer ud, Set liveOnExit) {
	super(hc);
	BasicBlock b1 = 
	    BasicBlock.computeBasicBlocks(hc.getRootElement(), gr); 
	lv = new LiveTemps(b1.blocksIterator(), liveOnExit);
	Solver.worklistSolve(b1.blocksIterator(), lv);
    }

    public Set getLiveIn(HCodeElement hce) {
	return lv.getLiveBefore(hce);
    }
    public Set getLiveOut(HCodeElement hce) {
	return lv.getLiveAfter(hce);
    }

    
    public static abstract class BBVisitor extends BackwardDataFlowBasicBlockVisitor {
    
     // maps a BasicBlock 'bb' to the LiveVarInfo associated with 'bb'
    private Map bbToLvi;

     /** Null arg ctor for use by subclasses so that the system won't
	 break when calling abstract methods that require data that
	 subclasses haven't initialized yet.
     */
    protected BBVisitor() {
    }

     /** Constructs a new <code>LiveVars</code> for <code>basicblocks</code>.
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
	       process.  Initializes the mapping between 
	       <code>HCodeElement</code>s and <code>BasicBlock</code>s. 
	  @param basicblocks <code>Iterator</code> of
		 	        <code>BasicBlock</code>s to be analyzed. 
     */	     
    public BBVisitor(Iterator basicblocks) {
	CloneableIterator blocks = new CloneableIterator(basicblocks);
	Set universe = findUniverse((Iterator) blocks.clone());
	SetFactory setFact = new BitSetFactory(universe);
	
	initializeHceToBB( (Iterator)blocks.clone() ); 
	initializeBBtoLVI( blocks, setFact );
    }

    /** Constructor for <code>LiveVars</code> that allows the user to
	pass in their own <code>SetFactory</code> for constructing
	sets of whatever variable types that are used in the analysis.  
	<BR> <B>requires:</B> All <code>Temp</code>s in
	     <code>basicblocks</code> are members of the universe for
	     <code>tempSetFact</code>.
	     
	<BR> Doc TODO: Add all of the above documentation from the
	     standard ctor.
    */
    public BBVisitor(Iterator basicblocks, 
		    SetFactory tempSetFact) {
	CloneableIterator blocks = new CloneableIterator(basicblocks); 

	initializeHceToBB( (Iterator)blocks.clone() ); 
	initializeBBtoLVI( blocks, tempSetFact );
    }

    protected void initializeBBtoLVI(Iterator blocks, SetFactory setFact) {
	bbToLvi = new HashMap();
	while(blocks.hasNext()) {
	    BasicBlock bb = (BasicBlock) blocks.next();
	    LiveVarInfo lvi = makeUseDef(bb, setFact);
	    bbToLvi.put(bb, lvi);
	}
    }

    /** Constructs a <code>Set</code> of all of the referenced
	variables in <code>blocks</code>.
        For example, in some analyses this universe will be made up of
	all of the <code>Temp</code>s referenced in
	<code>blocks</code>.  However, for flexibility I have allowed 
	users to define their own universe of values (such as
	<code>Web</code>s). 
    */
    protected abstract Set findUniverse(Iterator blocks);

    /** Merge (Confluence) operator.
	<BR> LVout(bb) = Union over (j elem Succ(bb)) of ( LVin(j) ) 
	<BR> <code>parent</code> corresponds to 'bb' above, while
	<code>child</code> corresponds to a member of Succ('bb').  It
	is the responsibility of the DataFlow Equation Solver to run
	<code>merge</code> on all of the Succ('bb')
	<BR> This method isn't meant to be called by application code;
	     instead, look at one of the DataFlow Equation Solvers in
	     the <code>harpoon.Analysis.DataFlow</code> package.
	<BR> <B>requires:</B> <code>child</code> and <code>parent</code>
	                      are contained in <code>this</code>
	<BR> <B>effects:</B> Updates the internal information
                             associated with <code>to</code> to
			     include information associated with
			     <code>from</code>
	@see harpoon.Analysis.DataFlow.InstrSolver
    */
    public boolean merge(BasicBlock child, BasicBlock parent) {
	LiveVarInfo finfo = (LiveVarInfo) bbToLvi.get(child);
	LiveVarInfo tinfo = (LiveVarInfo) bbToLvi.get(parent);
	boolean rtn = tinfo.lvOUT.addAll(finfo.lvIN);
	if (DEBUG) 
	    System.out.println
		("merge( succ: "+child+" pred: "+parent+" ) \n"+
		 "pred.LiveOut: " + tinfo.lvOUT + "\n" +
		 "succ.LiveIn: " + finfo.lvIN + "\n");
	return rtn;
    }

    /** Visit (Transfer) function.  
	<BR> LVin(bb) = ( LVout(bb) - DEF(bb) ) union USE(bb)
    */
    public void visit(BasicBlock b) {
	LiveVarInfo info = (LiveVarInfo) bbToLvi.get(b);

	info.lvIN = new HashSet();
	info.lvIN.addAll(info.lvOUT);
	info.lvIN.removeAll(info.def);
	info.lvIN.addAll(info.use);
    }

    /** Initializes the mapping of <code>HCodeElement</code>s to 
	<code>BasicBlocks</code>.  Implementations interested in this mapping
	should override this method. 
	
	@param basicblocks  an <code>Iterator</code> of the basic blocks to be
	                    analyzed. 
    */
    protected void initializeHceToBB(Iterator basicblocks) { }
    
    /** Initializes the USE/DEF information for bb and stores it in
	the returned LiveVarInfo.  An example implementation would
	use <code>Temp</code>s to make up their
	<code>LiveVarInfo</code>s. 
    */
    protected abstract LiveVarInfo makeUseDef(BasicBlock bb, SetFactory sf);

    /** Returns the <code>Set</code> of variables that are
	live on entry to <code>b</code>.
	<BR> <B>requires:</B> A DataFlow Solver has been run to
	     completion on the graph of <code>BasicBlock</code>s
	     containing <code>b</code> with <code>this</code> as the
	     <code>DataFlowBasicBlockVisitor</code>.
    */
    public Set getLiveOnEntry(BasicBlock b) {
	LiveVarInfo lvi = (LiveVarInfo) bbToLvi.get(b);
	return lvi.lvIN;
    }

    /** Returns the <code>Set</code> of variables that are
	live on exit from <code>b</code>.
	<BR> <B>requires:</B> A DataFlow Equation Solver has been run
             to completion on the graph of <code>BasicBlock</code>s 
	     containing <code>b</code> with <code>this</code> as the
	     <code>DataFlowBasicBlockVisitor</code>.
    */
    public Set getLiveOnExit(BasicBlock b) {
	LiveVarInfo lvi = (LiveVarInfo) bbToLvi.get(b);
	return lvi.lvOUT;
    }

    public String dump() {
	StringBuffer s = new StringBuffer();
	Iterator e = bbToLvi.keySet().iterator();
	while (e.hasNext()) {
	    BasicBlock bb = (BasicBlock)e.next();
	    s.append("BasicBlock " + bb);
	    LiveVarInfo lvi = (LiveVarInfo) bbToLvi.get(bb);
	    s.append("\n" + lvi);
	    s.append(" -- " + bb + " INSTRS --\n" + 
		     bb.dumpElems() + 
		     " -- END " + bb + " --\n\n");
	}
	return s.toString();
    }

    // LiveVarInfo is a record type grouping together four sets: 
    // use(bb): set of vars used in 'bb' before being defined in 'bb',
    //          if at all. 
    // def(bb): set of vars defined in 'bb' before being used in 'bb',
    //          if at all. 
    // lvIN(bb):  ( lvOUT(bb) - DEF(bb) ) U USE(bb)
    // lvOUT(bb): Union over (j elem Succ(bb)) of ( lvIN(j) )
    protected static class LiveVarInfo {
	public Set use; 
	public Set def;
	public Set lvIN;
	public Set lvOUT;

	public LiveVarInfo(SetFactory sf) { 
	    use = sf.makeSet();
	    def = sf.makeSet();
	    lvIN = sf.makeSet();
	    lvOUT = sf.makeSet();
	}
	
	public String toString() {
	    StringBuffer s = new StringBuffer();
	    Iterator iter;
	    s.append("\tUSE set: " );
	    iter = use.iterator();
	    while(iter.hasNext()) { s.append(iter.next() + " "); }
	    s.append("\n");

	    s.append("\tDEF set: " );
	    iter = def.iterator();
	    while(iter.hasNext()) { s.append(iter.next() + " "); }
	    s.append("\n");

	    s.append("\tLVin set: " );
	    iter = lvIN.iterator();
	    while(iter.hasNext()) { s.append(iter.next() + " "); }
	    s.append("\n");

	    s.append("\tLVout set: " );
	    iter = lvOUT.iterator();
	    while(iter.hasNext()) { s.append(iter.next() + " "); }
	    s.append("\n");
	    
	    return s.toString();
	}
    }
}

}
