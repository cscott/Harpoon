// LiveVars.java, created Wed May 26 16:53:26 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.BasicBlockInterf;
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
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LiveVars.java,v 1.3 2002-04-02 23:39:11 salcianu Exp $ */
public class LiveVars extends Liveness {
    
    protected static final boolean DEBUG = false; 

    LiveTemps lv;

    /** Constructs a new <code>LiveVars</code>.
	Note that since the dataflow analysis is done during
	construction, this can take a while.  
	Uses the default <code>UseDefer</code> built into
	<code>hc</code> for its analysis.  Requires that elements of
	<code>hc</code> implement <code>UseDefable</code>.
	@param hc Code to be analyzed
	@param gr Represents control-flow information for hc
	@param liveOnExit Set of Temps that are live on exit from hc
     */
    public LiveVars(HCode hc, CFGrapher gr, Set liveOnExit) {
	this(hc, gr, UseDefer.DEFAULT, liveOnExit);
    }

    /** Constructs a new <code>LiveVars</code>.
	Note that since the dataflow analysis is done during
	construction, this can take a while.
	@param hc Code to be analyzed
	@param gr Represents control-flow information for hc
	@param ud Represents use/def information for elements of hc
	@param liveOnExit Set of Temps that are live on exit from hc
     */
    public LiveVars(HCode hc, CFGrapher gr, UseDefer ud, Set liveOnExit) {
	super(hc);
	BasicBlock.Factory bbFact = 
	    new BasicBlock.Factory(hc, gr);
	lv = new LiveTemps(bbFact, liveOnExit, ud);
	Solver.worklistSolve(bbFact.blockSet().iterator(), lv);
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

    protected BasicBlock.Factory bbFact;

     /** Special ctor for use by subclasses so that the system won't
	 break when calling abstract methods that require data that
	 subclasses haven't initialized yet.
     */
    protected BBVisitor(BasicBlock.Factory bbFact, boolean ignore) {
	this.bbFact = bbFact;
    }


     /** Constructs a new <code>LiveVars</code> for <code>basicblocks</code>.
	 <BR> <B>requires:</B> All of the statements in
	      <code>bbFact</code> implement <code>UseDefable</code> 
	  <BR> <B>effects:</B> constructs a new 
	       <code>BasicBlockVisitor</code> and initializes its 
	       internal datasets for analysis of the 
	       <code>BasicBlock</code>s in <code>bbFact</code>.
	  @param bbFact <code>BasicBlock.Factory</code> containing 
	                <code>BasicBlock</code>s to be analyzed. 
     */	     
    public BBVisitor(BasicBlock.Factory bbFact) {
	Set universe = findUniverse(bbFact.blockSet());
	SetFactory setFact = new BitSetFactory(universe);
	
	initializeBBtoLVI( bbFact.blockSet(), setFact );
    }

    /** Constructor for <code>LiveVars</code> that allows the user to
	pass in their own <code>SetFactory</code> for constructing
	sets of whatever variable types that are used in the analysis.  
	<BR> <B>requires:</B> <OL>
	     <LI> All <code>Temp</code>s in <code>bbFact</code> are
	          members of the universe for
		  <code>tempSetFact</code>.
	     <LI> All of the statements in <code>bbFact</code>
	          implement <code>UseDefable</code>
	     </OL>
	 <BR> <B>effects:</B> constructs a new 
	      <code>BasicBlockVisitor</code> and initializes its 
	      internal datasets for analysis of the 
	      <code>BasicBlock</code>s in <code>bbFact</code>.	
    */
    public BBVisitor(BasicBlock.Factory bbFact,
		     SetFactory tempSetFact) {
	initializeBBtoLVI( bbFact.blockSet(), tempSetFact );
    }

    protected void initializeBBtoLVI(Set blockSet, SetFactory setFact) {
	bbToLvi = new HashMap();
	Iterator blocks = blockSet.iterator();
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
    protected abstract Set findUniverse(Set blockSet);

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
    public boolean merge(BasicBlockInterf child, BasicBlockInterf parent) {
	LiveVarInfo finfo = (LiveVarInfo) bbToLvi.get(child);
	LiveVarInfo tinfo = (LiveVarInfo) bbToLvi.get(parent);
	if (DEBUG) 
	    System.out.println
		("merge( succ: "+child+" pred: "+parent+" ) \n"+
		 "pred.LiveOut: " + tinfo.lvOUT + "\n" +
		 "succ.LiveIn: " + finfo.lvIN + "\n");
	boolean rtn = tinfo.lvOUT.addAll(finfo.lvIN);
	return rtn;
    }

    /** Visit (Transfer) function.  
	<BR> LVin(bb) = ( LVout(bb) - DEF(bb) ) union USE(bb)
    */
    public void visit(BasicBlock b) {
	LiveVarInfo info = (LiveVarInfo) bbToLvi.get(b);
	
	info.lvIN = new HashSet();     if (DEBUG) System.out.print("visit("+b+")");
	info.lvIN.addAll(info.lvOUT);  if (DEBUG) System.out.print(" add:"+info.lvOUT);
	info.lvIN.removeAll(info.def); if (DEBUG) System.out.print(" rem:"+info.def);
	info.lvIN.addAll(info.use);    if (DEBUG) System.out.print(" add:"+info.use);
    }

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

    public String dump() { return dump(true); }

    public String dump(boolean dumpInstrs) {
	StringBuffer s = new StringBuffer();
	Iterator e = bbToLvi.keySet().iterator();
	while (e.hasNext()) {
	    BasicBlock bb = (BasicBlock)e.next();
	    s.append("BasicBlock " + bb);
	    s.append(" pred:"+bb.prevSet()+" succ:"+bb.nextSet());
	    LiveVarInfo lvi = (LiveVarInfo) bbToLvi.get(bb);
	    s.append("\n" + lvi);
	    if (dumpInstrs)
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
