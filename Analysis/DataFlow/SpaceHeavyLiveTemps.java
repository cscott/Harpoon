// SpaceHeavyLiveTemps.java, created Wed Aug 23 17:20:37 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.Analysis.BasicBlock;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.CFGEdge;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;
import harpoon.Util.CloneableIterator; 
import harpoon.Util.Util; 
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.Collections.ReverseIterator;
import harpoon.Util.Collections.SetFactory;

import java.util.Set;
import java.util.List;
import java.util.Collection;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <code>SpaceHeavyLiveTemps</code> is an extension of
 * <code>LiveTemps</code> that keeps ALL of the results of the queries
 * that it has calculated alive.  This can lead to large space
 * overhead but this is offset by the decrease in required time for
 * repeated queries. 
 * 
 * I plan to eventually paraterize this class with a scaling factor so
 * that space can be decreased by a constant-factor at the cost of a
 * corresponding increase in recomputation-times; for now the class
 * can be treated as if the scaling factor were equal to 1.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: SpaceHeavyLiveTemps.java,v 1.5 2002-08-30 22:37:43 cananian Exp $
 */
public class SpaceHeavyLiveTemps extends LiveTemps {
    CFGrapher grapher;

    public static LiveTemps make(HCode code, Set liveOnExit) {
	BasicBlock.Factory bbf = new BasicBlock.Factory(code);
	LiveTemps lt = new SpaceHeavyLiveTemps(bbf, liveOnExit);
	Solver.worklistSolve
	    // (bbFact.preorderBlocksIter(),
	    (new ReverseIterator(bbf.postorderBlocksIter()),
	     lt);
	return lt;

    }
    
    /** Creates a <code>SpaceHeavyLiveTemps</code>. */
    public SpaceHeavyLiveTemps(BasicBlock.Factory bbf, Set liveOnExit) {
        this(bbf, liveOnExit, UseDefer.DEFAULT);
    }
    
    public SpaceHeavyLiveTemps(BasicBlock.Factory bbf, Set liveOnExit,
			       UseDefer ud) {
        this(bbf, liveOnExit, ud, CFGrapher.DEFAULT);
    }

    public SpaceHeavyLiveTemps(BasicBlock.Factory bbf, Set liveOnExit,
			       UseDefer ud, CFGrapher grapher) {
	super(bbf, liveOnExit, ud);
	this.grapher = grapher;
    }

    
    public SpaceHeavyLiveTemps(BasicBlock.Factory bbf, Set liveOnExit,
			       SetFactory tempSetFact, UseDefer ud) {
	this(bbf, liveOnExit, tempSetFact, ud, CFGrapher.DEFAULT);
    }

    public SpaceHeavyLiveTemps(BasicBlock.Factory bbf, Set liveOnExit,
			       SetFactory tempSetFact, UseDefer ud, CFGrapher grapher) {
	super(bbf, liveOnExit, tempSetFact, ud);
	this.grapher = grapher;
    }

    private HashMap hce2liveAfter = new HashMap();

    public Set getLiveAfter(HCodeElement hce) {
	if (hce2liveAfter.containsKey(hce)) {
	    return (Set) hce2liveAfter.get(hce);
	}
	
	BasicBlock bb = bbFact.getBlock(hce);
    if(bb == null) return java.util.Collections.EMPTY_SET;
	List stms = bb.statements();
	HCodeElement last = (HCodeElement) stms.get(stms.size() - 1);
	HCodeElement cfg = hce;
	while (!hce2liveAfter.containsKey(cfg)) {
	    if (cfg.equals(last)) {
		Set liveAfter = 
		    mySetFactory.makeSet(getLiveOnExit(bb));
		hce2liveAfter.put(cfg, liveAfter);
		break;
	    } else {
		Collection succC = grapher.succC(cfg);
		assert succC.size() == 1 : cfg;
		cfg = ((CFGEdge) succC.iterator().next()).toCFG();
	    }
	}
	    
	Set liveAfter = (Set) hce2liveAfter.get(cfg);
	while(!cfg.equals(hce)) {
	    Collection predC = grapher.predC(cfg);
	    assert predC.size() == 1 : cfg;
	    liveAfter = mySetFactory.makeSet(liveAfter);
	    liveAfter.removeAll(ud.defC(cfg));
	    liveAfter.addAll(ud.useC(cfg));
	    cfg = ((CFGEdge) predC.iterator().next()).fromCFG();
	    hce2liveAfter.put(cfg, liveAfter);
	}
	
	return (Set) hce2liveAfter.get(hce);
    }

}
