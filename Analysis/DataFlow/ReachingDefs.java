// ReachingDefs.java, created Wed Mar 10  9:00:54 1999 by jwhaley
// Copyright (C) 1998 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;


import harpoon.Analysis.EdgesIterator;
import harpoon.Analysis.BasicBlock;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.UseDefable;
import harpoon.Temp.Temp;
import harpoon.Util.CloneableIterator; 
import harpoon.Util.Util;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.Collections.SetFactory; 

import java.util.HashMap;
import java.util.HashSet; 
import java.util.Iterator;
import java.util.Map;
import java.util.Set; 

/** 
 * <code>ReachingDefs</code> is a <code>ForwardDataFlowBasicBlockVisitor</code>
 * for performing reaching definitions analysis on any IR that implements
 * <code>HCodeElement</code>, <code>CFGraphable</code>, and 
 * <code>UseDefable</code>.  
 *
 * @author  John Whaley <jwhaley@alum.mit.edu>
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @author  Duncan Bryce <duncan@lcs.mit.edu>
 * @version $Id: ReachingDefs.java,v 1.1.2.19 2001-06-17 22:29:21 cananian Exp $
 */
public abstract class ReachingDefs { 
    
    private static final boolean DEBUG = false; 

    

abstract static class BBVisitor extends ForwardDataFlowBasicBlockVisitor {    

    private Map bbToRdi; 

    /** 
     * Constructs a new <code>ReachingDefs</code> for <code>basicblocks</code>.
     * 
     * <BR><B>requires:</B> 
     * <OL>
     *   <LI> <code>basicBlocks</code> is an <code>Iterator</code> of 
     *        <code>BasicBlock</code>s,	       
     *   <LI> All of the instructions in <code>basicBlocks</code> implement
     *        <code>UseDefable</code>, 
     *   <LI> No element of <code>basicblocks</code> links to a
     *        <code>BasicBlock</code> not contained within 
     *        <code>basicBlocks</code>,
     *   <LI> No <code>BasicBlock</code> is repeatedly iterated
     *        by <code>basicBlocks</code> 
     * </OL>
     * <BR> <B>modifies:</B> <code>basicBlocks</code>
     * <BR> <B>effects:</B> constructs a new <code>BasicBlockVisitor</code> 
     *   and initializes internal datasets for analysis of the 
     *   <code>BasicBlock</code>s in <code>basicBlocks</code>, iterating over 
     *   all of <code>basicBlocks</code> in the process.
     *
     * @param basicBlocks <code>Iterator</code> of <code>BasicBlock</code>s 
     *                    to be analyzed.
     */	     
    public BBVisitor(Iterator basicblocks) {
	CloneableIterator blocks   = new CloneableIterator(basicblocks);
	Set               universe = findUniverse((Iterator) blocks.clone());
	SetFactory        setFact  = new BitSetFactory(universe);
	
	this.initializeGenPrsv( (Iterator)blocks.clone(), setFact ); 
	this.initializeBBtoRDI( blocks, setFact );
    }

    /** 
     * Constructs a new <code>ReachingDefs</code> for 
     * <code>basicblocks</code>.  Allows the user to specify their own
     * <code>SetFactory</code> for constructing sets of definitions in the 
     * analysis.  
     * 
     * <BR><B>requires:</B> 
     * <OL>
     *   <LI> <code>basicBlocks</code> is an <code>Iterator</code> of 
     *        <code>BasicBlock</code>s,	       
     *   <LI> All of the instructions in <code>basicBlocks</code> implement
     *        <code>UseDefable</code>, 
     *   <LI> No element of <code>basicblocks</code> links to a
     *        <code>BasicBlock</code> not contained within 
     *        <code>basicBlocks</code>,
     *   <LI> No <code>BasicBlock</code> is repeatedly iterated
     *        by <code>basicBlocks</code> 
     * </OL>
     * <BR> <B>modifies:</B> <code>basicBlocks</code>
     * <BR> <B>effects:</B> constructs a new <code>BasicBlockVisitor</code> 
     *   and initializes internal datasets for analysis of the 
     *   <code>BasicBlock</code>s in <code>basicBlocks</code>, iterating over 
     *   all of <code>basicBlocks</code> in the process.
     *
     * @param basicBlocks <code>Iterator</code> of <code>BasicBlock</code>s 
     *                    to be analyzed.
     * @param setFact     the <code>SetFactory</code> to be used in 
     *                    the construction of sets of definitions. 
     */	     
    public BBVisitor(Iterator basicblocks, SetFactory setFact) {
	CloneableIterator blocks = new CloneableIterator(basicblocks); 

	initializeBBtoRDI( blocks, setFact );
    }
    
    /**
     * Initializes an internal mapping of <code>BasicBlock</code>s to 
     * <code>ReachingDefInfo</code>s. 
     */ 
    protected void initializeBBtoRDI(Iterator blocks, SetFactory setFact) {
	bbToRdi = new HashMap();
	
	while(blocks.hasNext()) {
	    BasicBlock bb = (BasicBlock) blocks.next();
	    ReachingDefInfo rdi = makeGenPrsv(bb, setFact);
	    bbToRdi.put(bb, rdi);
	}
    }

    /** 
     * Merge (Confluence) operator.
     * <BR> RDin(bb) = Union over (j elem Pred(bb)) of ( RDout(j) ) 
     * <code>from</code> corresponds to a member of Pred('bb').  It
     * is the responsibility of the DataFlow Equation Solver to run
     * <code>merge</code> on all of the Pred('bb')
     * 
     * <BR> This method isn't meant to be called by application code;
     *      instead, look at one of the DataFlow Equation Solvers in
     *      the <code>harpoon.Analysis.DataFlow</code> package.
     * <BR> <B>requires:</B> 
     *   <code>child</code> and <code>parent</code> are contained in 
     *   <code>this</code>
     * <BR> <B>effects:</B> 
     *   Updates the internal information associated with <code>to</code> to
     *	 include information associated with <code>from</code>. 
     */
    public boolean merge(BasicBlock from, BasicBlock to) {
	ReachingDefInfo fInfo  = (ReachingDefInfo)this.bbToRdi.get(from); 
	ReachingDefInfo tInfo  = (ReachingDefInfo)this.bbToRdi.get(to); 
	boolean         result = tInfo.rdIN.addAll(fInfo.rdOUT); 

	if (DEBUG)
	    System.out.println
		("merge( from: " + from +" to: "+ to +" ) \n" + 
		 from + ".rdOut: "+ fInfo.rdOUT + "\n" + 
		 to + ".rdIN: "+ tInfo.rdIN + "\n"); 
	
	return result;
    }
    
    /** 
     * Visit (Transfer) function.  
     * <BR> RDout(bb) = ( RDin(bb) intersection PRSV(bb) ) union GEN(bb)
     */
    public void visit(BasicBlock b) {
	ReachingDefInfo info = (ReachingDefInfo)bbToRdi.get(b); 
	info.rdOUT.clear(); 
	info.rdOUT.addAll(info.rdIN);
	info.rdOUT.retainAll(info.prsv);
	info.rdOUT.addAll(info.gen); 

	if (DEBUG) 
	    System.out.println
		("visit( " + b + "):\n " + 
		 b + ".LiveOut: " + info.rdOUT + "\n" +
		 b + ".LiveIn: " + info.rdIN + "\n");

    }

    /** 
     * Constructs a <code>Set</code> of all of the referenced definitions in 
     * <code>blocks</code>.
     * 
     * For example, in some analyses this universe will be made up of
     * all of the <code>HCodeElement</code>s referenced in
     * <code>blocks</code>.  However, for flexibility I have allowed 
     * users to define their own universe of values. 
     */
    protected abstract Set findUniverse(Iterator blocks);

    /**
     * Performs initialization necessary to calculate GEN/PRSV sets.  
     * 
     * For example, this might consist of constructing a mapping of 
     * <code>Temp</code>s to <code>HCodeElement</code>s which do not define
     * them.  
     */
    protected abstract void initializeGenPrsv(Iterator blocks, SetFactory sf); 
    
    /** 
     * Initializes the GEN/PRSV information for bb and stores it in
     * the returned <code>ReachingDefInfo</code>.  An example implementation 
     * would use <code>HCodeElement</code>s to make up their
     * <code>ReachingDefInfo</code>s. 
     */
    protected abstract ReachingDefInfo makeGenPrsv(BasicBlock bb, SetFactory sf);

    /** 
     * Returns the <code>Set</code> of definitions that reach the exit of
     * <code>b</code>.
     * 
     * <BR> <B>requires:</B> A DataFlow Equation Solver has been run to 
     *   completion on the graph of <code>BasicBlock</code>s containing 
     *   <code>b</code> with <code>this</code> as the
     *   <code>DataFlowBasicBlockVisitor</code>.
     */
    public Set getReachingOnEntry(BasicBlock b) {
	ReachingDefInfo rdi = (ReachingDefInfo) bbToRdi.get(b);
	return rdi.rdIN;
    }

    /** 
     * Returns the <code>Set</code> of definitions that reach the exit of
     * <code>b</code>.
     * 
     * <BR> <B>requires:</B> A DataFlow Equation Solver has been run to 
     *   completion on the graph of <code>BasicBlock</code>s containing 
     *   <code>b</code> with <code>this</code> as the
     *   <code>DataFlowBasicBlockVisitor</code>.
     */
    public Set getReachingOnExit(BasicBlock b) {
	ReachingDefInfo rdi = (ReachingDefInfo) bbToRdi.get(b);
	return rdi.rdOUT;
    }

    public String dump() {
	StringBuffer s = new StringBuffer();
	for (Iterator i = this.bbToRdi.keySet().iterator(); i.hasNext(); ) { 
	    BasicBlock bb = (BasicBlock)i.next();
	    s.append("Basic block "+bb);
	    ReachingDefInfo rdi = (ReachingDefInfo)this.bbToRdi.get(bb);
	    s.append("\n"+rdi);
	}
	return s.toString();
    }

    // ReachingDefInfo is a record type grouping together four sets: 
    // gen(bb):  set of definitions in 'bb' which are not subsequently killed
    //           in 'bb'. 
    // prsv(bb): set of definitions in 'bb' which are not killed in 'bb'. 
    // rdIN(bb):  Union over (j elem Pred(bb)) of ( rdOUT(j) )
    // rdOUT(bb): ( rdIN(bb) intersection prsv(bb) ) union ( gen(bb) )
    // 
    protected static class ReachingDefInfo {
	public Set gen;
	public Set prsv;
	public Set rdIN; 
	public Set rdOUT; 

	ReachingDefInfo(SetFactory sf) {
	    gen   = sf.makeSet();
	    prsv  = sf.makeSet();
	    rdIN  = sf.makeSet();
	    rdOUT = sf.makeSet(); 
	}

	public String toString() {
	    StringBuffer s = new StringBuffer();
	    Iterator iter;
	    s.append("\tGEN set: " );
	    iter = gen.iterator();
	    while(iter.hasNext()) { s.append(iter.next() + " "); }
	    s.append("\n");

	    s.append("\tPRSV set: " );
	    iter = prsv.iterator();
	    while(iter.hasNext()) { s.append(iter.next() + " "); }
	    s.append("\n");

	    s.append("\tRDin set: " );
	    iter = rdIN.iterator();
	    while(iter.hasNext()) { s.append(iter.next() + " "); }
	    s.append("\n");

	    s.append("\tRDout set: " );
	    iter = rdOUT.iterator();
	    while(iter.hasNext()) { s.append(iter.next() + " "); }
	    s.append("\n");
	    
	    return s.toString();
	}
    }
    }
}

