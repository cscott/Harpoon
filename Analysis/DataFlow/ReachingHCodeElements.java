// ReachingHCodeElements.java, created Fri Dec  3  1:47:33 1999 by duncan
// Copyright (C) 1998 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Analysis.BasicBlock; 
import harpoon.ClassFile.HCodeElement; 
import harpoon.IR.Properties.CFGraphable; 
import harpoon.IR.Properties.UseDefable; 
import harpoon.Temp.Temp; 
import harpoon.Util.Util; 
import harpoon.Util.Collections.SetFactory; 

import java.util.HashMap; 
import java.util.HashSet; 
import java.util.Iterator; 
import java.util.Map; 
import java.util.Set; 

/**
 * <code>ReachingHCodeElements</code> is an extension of 
 * <code>ReachingDefs</code> for performing reaching definitions analysis on 
 * <code>HCodeElements</code>s.  
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @author  Duncan Bryce <duncan@lcs.mit.edu> 
 * @version $Id: ReachingHCodeElements.java,v 1.1.2.9 2001-01-13 21:45:18 cananian Exp $ 
 */
public class ReachingHCodeElements extends ReachingDefs.BBVisitor { 
    private BasicBlock.Factory bbfactory;
    private Map tempsToPrsvs;
    private Set universe; 
    private Map rdCache = new HashMap(); 

    
    /** 
     * Constructs a new <code>ReachingHCodeElements</code> for 
     * the basic blocks in the supplied <code>BasicBlock.Factory</code>.
     * 
     * <BR><B>requires:</B> 
     * <OL>
     *   <LI> <code>bbfactory</code> is a valid
     *        <code>BasicBlock.Factory</code>.
     *   <LI> All of the instructions in <code>basicBlocks</code> implement
     *        <code>UseDefable</code>, 
     * </OL>
     * <BR> <B>effects:</B> constructs a new <code>BasicBlockVisitor</code> 
     *   and initializes internal datasets for analysis of the 
     *   <code>BasicBlock</code>s in <code>bbfactory</code>.
     *
     * @param bbfactory <code>BasicBlock.Factory</code> of
     *                  <code>BasicBlock</code>s to be analyzed.
     */	     
    public ReachingHCodeElements(BasicBlock.Factory bbfactory) {
        super(bbfactory.blocksIterator());
	this.bbfactory = bbfactory;
    }
    
    /** 
     * Constructs a new <code>ReachingHCodeElements</code> for 
     * the basic blocks in the supplied <code>BasicBlock.Factory</code>.
     * Allows the user to specify their own <code>SetFactory</code> for
     * constructing sets of <code>HCodeElements</code> in the analysis.  
     * 
     * <BR><B>requires:</B> 
     * <OL>
     *   <LI> <code>bbfactory</code> is a valid
     *        <code>BasicBlock.Factory</code>.
     *   <LI> All of the instructions in <code>basicBlocks</code> implement
     *        <code>UseDefable</code>, 
     *   <LI> All of the <code>HCodeElements</code> in <code>basicBlocks</code>
     *        which have non-empty def sets are members of the universe of
     *        <code>setFact</code>. 
     * </OL>
     * <BR> <B>effects:</B> constructs a new <code>BasicBlockVisitor</code> 
     *   and initializes internal datasets for analysis of the 
     *   <code>BasicBlock</code>s in <code>bbfactory</code>.
     *
     * @param bbfactory <code>BasicBlock.Factory</code> of
     *                  <code>BasicBlock</code>s to be analyzed.
     * @param setFact     the <code>SetFactory</code> to be used in 
     *                    the construction of sets of 
     *                    <code>HCodeElements</code>. 
     */	     
    public ReachingHCodeElements(BasicBlock.Factory bbfactory,
				 SetFactory setFact) {
	super(bbfactory.blocksIterator(), setFact);
	this.bbfactory = bbfactory;
    }


    /** 
     * Constructs a <code>Set</code> of all of the <code>HCodeElement</code>s
     * in <code>blocks</code> which have non-empty def sets. 
     * 
     * <BR><B>requires:</B> 
     * <OL>
     *   <LI> <code>blocks</code> is an <code>Iterator</code> of
     *        <code>BasicBlock</code>s. 
     *   <LI> All of the instructions in each element of <code>blocks</code> 
     *        implement <code>UseDefable</code>. 
     * </OL>
     * <BR> <B>modifies:</B> <code>blocks</code>
     * <BR> <B>effects:</B> 
     *   Iterates through all of the instructions contained in each element 
     *   of <code>blocks</code>, adding each instruction which has a non-empty
     *   def set to a universe of values, returning the universe after all of 
     *   the instructions have been visited.  Internally maintains a reference
     *   to this computed dataset. 
     */
    protected Set findUniverse(Iterator blocks) { 
	this.universe = new HashSet();

	for (; blocks.hasNext(); ) {
	    BasicBlock bb = (BasicBlock) blocks.next();
	    Iterator hces = bb.statements().iterator(); 
	    while(hces.hasNext()) { 
		UseDefable udNext = (UseDefable)hces.next(); 
		if (udNext.def().length > 0) { 
		    this.universe.add(udNext); 
		}
	    }
	}
	return this.universe; 
    }

    /**
     * Initializes a mapping of temps to the Set of <code>HCodeElement</code>s
     * which  do not define them.  
     *
     * <BR><B>Requires:</B>
     * <OL>
     *   <LI> <code>blocks</code> is an <code>Iterator</code> of 
     *        <code>BasicBlock</code>s.  
     *   <LI> All of the instructions in each element of <code>blocks</code>
     *        implement <code>UseDefable</code>. 
     *   <LI> All of the <code>HCodeElements</code> in <code>basicBlocks</code>
     *        which have non-empty def sets are members of the universe of
     *        <code>setFact</code>. 
     */ 
    protected void initializeGenPrsv(Iterator blocks, SetFactory sf) { 
	this.tempsToPrsvs = new HashMap();

	// Update the universe to use a compatible set factory. 
	// This is usually much more efficient. 
	this.universe = sf.makeSet(this.universe); 
	
	while (blocks.hasNext()) { 
	    BasicBlock bb = (BasicBlock)blocks.next(); 
	    Iterator useDefs = bb.statements().iterator(); 
	    while(useDefs.hasNext()) { 
		UseDefable udNext = (UseDefable)useDefs.next(); 
		Temp[] defs   = udNext.def();
		for (int i=0, n=defs.length; i<n; ++i) {
		    Temp t    = defs[i];
		    Set  prsv = (Set)tempsToPrsvs.get(t); 
		    if (prsv == null) { 
			tempsToPrsvs.put(t, prsv = sf.makeSet(this.universe));
		    }
		    prsv.remove(udNext); 
		}
	    }
	}
    }
	
    /** 
     * Initializes the GEN/PRSV information for 'bb' and stores in in
     * the returned <code>ReachingDefInfo</code>.
     * 
     * <BR><B>Requires:</B> 
     *   <code>initializeGenPrsv</code> has already been called on this
     *   <code>ReachingHCodeElements</code> object, and <code>bb</code> was
     *   one of the blocks in the <code>Iterator</code> parameter to the last
     *   such invocation.  
     */
     protected ReachingDefInfo makeGenPrsv(BasicBlock bb, SetFactory sf) { 
	ReachingDefInfo info = new ReachingDefInfo(sf); 

	info.prsv.addAll(this.universe); 

	Iterator useDefs = bb.statements().iterator(); 
	while(useDefs.hasNext()) {
	    UseDefable udNext = (UseDefable) useDefs.next(); 
	    Temp[] defs   = udNext.def();
	    for (int i=0, n=defs.length; i<n; ++i) {
		Temp t    = defs[i];
		Set  prsv = (Set)tempsToPrsvs.get(t);
		info.prsv.retainAll(prsv); 
		info.gen.retainAll(prsv); 
		info.gen.add(udNext); 
	    }
	}
	return info; 
    }

    /** 
     * Returns the <code>Set</code> of <code>HCodeElements</code>s 
     * which represent a definition that reaches the point directly before
     * <code>hce</code>.  If <code>hce</code> represents more than one
     * definition, all definitions which it represents must reach this point.
     * Because of this, the <code>ReachingHCodeElements</code> class is
     * most useful for intermediate representations in which each
     * <code>HCodeElement</code> can represent only 1 definition. 
     * 
     * <BR><B>requires:</B> 
     * A DataFlow Equation Solver has been run to completion on the graph 
     * of <code>BasicBlock</code>s containing some block that contains 
     * <code>hce</code>, with <code>this</code> as the	
     * <code>DataFlowBasicBlockVisitor</code>. 
     * 	
     * <BR> <B>effects:</B> 
     * Returns a <code>Set</code> of <code>Temp</code>s that are live on 
     * entry to <code>hce</code>. 
     */
    public Set getReachingBefore(HCodeElement hce) {

	if (!this.rdCache.containsKey(hce)) { 
	    BasicBlock  bb          = bbfactory.getBlock(hce);
	    Set         reachBefore = new HashSet(); 

	    reachBefore.addAll(this.getReachingOnEntry(bb)); 

	    // Starting from the first element in hce's basic block, traverse
	    // the block until hce is reached.  Each step updates the
	    // reaching def information.
	    Iterator i = bb.statements().iterator(); 
	    while(i.hasNext()) { 
		UseDefable udCurrent = (UseDefable)i.next();
		this.rdCache.put(udCurrent, reachBefore);
		Temp[] defs = udCurrent.def(); 
		for (int n=0; n<defs.length; n++) { 
		    reachBefore.retainAll
			((Set)this.tempsToPrsvs.get(defs[n])); 
		    reachBefore.add((HCodeElement)udCurrent); 
		}
	    }
	}

	return (Set)this.rdCache.get(hce); 
    }

    /** 
     * Returns the <code>Set</code> of <code>HCodeElements</code>s 
     * which represent a definition that reaches the point directly after 
     * <code>hce</code>.  If <code>hce</code> represents more than one
     * definition, all definitions which it represents must reach this point.
     * Because of this, the <code>ReachingHCodeElements</code> class is
     * most useful for intermediate representations in which each
     * <code>HCodeElement</code> can represent only 1 definition. 
     * 
     * <BR><B>requires:</B> 
     * A DataFlow Equation Solver has been run to completion on the graph 
     * of <code>BasicBlock</code>s containing some block that contains 
     * <code>hce</code>, with <code>this</code> as the	
     * <code>DataFlowBasicBlockVisitor</code>. 
     * 	
     * <BR> <B>effects:</B> 
     * Returns a <code>Set</code> of <code>Temp</code>s that are live on 
     * entry to <code>hce</code>. 
     */
    public Set getReachingAfter(HCodeElement hce) { 
	Set reachAfter = this.getReachingBefore(hce); 
	
	Temp[] defs = ((UseDefable)hce).def(); 
	for (int i=0; i<defs.length; i++) { 
	    reachAfter.retainAll((Set)this.tempsToPrsvs.get(defs[i])); 
	}
	reachAfter.add(hce); 
	
	return reachAfter; 
    }
}


