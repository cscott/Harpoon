// CachingLiveTemps.java, created Sat Jul 29 15:22:48 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
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
 * <code>CachingLiveTemps</code> is an extension of
 * <code>LiveTemps</code> that keeps a cache of the recent results
 * that it calculated.  The cache keeps the last few basic blocks
 * accessed, along with the results of liveness analysis on all of the
 * statements in the basic blocks.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CachingLiveTemps.java,v 1.1.2.4 2001-06-17 22:29:19 cananian Exp $
 */
public class CachingLiveTemps extends LiveTemps {

    public static LiveTemps make(HCode code, Set liveOnExit) {
	BasicBlock.Factory bbf = new BasicBlock.Factory(code);
	LiveTemps lt = new CachingLiveTemps(bbf, liveOnExit);
	Solver.worklistSolve
	    // (bbFact.preorderBlocksIter(),
	    (new harpoon.Util.ReverseIterator(bbf.postorderBlocksIter()),
	     lt);
	return lt;
    }
    
    /** Creates a <code>CachingLiveTemps</code>. */
    public CachingLiveTemps(BasicBlock.Factory bbf, Set liveOnExit) {
        super(bbf, liveOnExit);
    }

    public CachingLiveTemps(BasicBlock.Factory bbf, Set liveOnExit,
			    UseDefer ud) {
        super(bbf, liveOnExit, ud);
    }
    
    public CachingLiveTemps(BasicBlock.Factory bbf, Set liveOnExit,
			    SetFactory tempSetFact, UseDefer ud) {
	super(bbf, liveOnExit, tempSetFact, ud);
    }

    public String cachePerformance() {
	return "HITS: "+hits+" MISSES: "+misses+" SETUP: "+setup;
    }
    
    private int hits = 0, misses = 0, setup = 0;


    private static final int CACHE_SIZE = 20;
    private int nextPt = 0;
    private BasicBlock[] lastBBs = new BasicBlock[CACHE_SIZE];
    // a cache of results for the getLiveAfter(HCodeElement) method
    // each index corresponds to the basic block in lastBBs
    private HashMap hce2liveAfters[] = new HashMap[CACHE_SIZE];

    private HashSet blocksSeenBefore = new HashSet();

    private HashMap addToCache(BasicBlock bb) {
	if (blocksSeenBefore.contains(bb)) {
	    misses++;
	} else {
	    setup++;
	    blocksSeenBefore.add(bb);
	}

	if (lastBBs[ nextPt ] != null) {
	    hce2liveAfters[ nextPt ].clear();
	} else {
	    hce2liveAfters[ nextPt ] = new HashMap(bb.statements().size());
	}
	lastBBs[ nextPt ] = bb;
	final HashMap hce2liveAfter = hce2liveAfters[ nextPt ];
	nextPt = (nextPt == lastBBs.length-1)?0:nextPt+1;


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
	    
	    hce2liveAfter.put(current, mySetFactory.makeSet(liveAfter));

	    // update set for before 'current'
	    liveAfter.removeAll(ud.defC(current)); 
	    liveAfter.addAll(ud.useC(current)); 
	}
	
	return hce2liveAfter;
    }
    
    public Set getLiveAfter(HCodeElement hce) {
	BasicBlock bb = bbFact.getBlock(hce);

	for(int i=0; i < lastBBs.length; i++) {
	    if (lastBBs[i] == bb) {
		hits++;
		return (Set) hce2liveAfters[i].get(hce);
	    }
	}

	return (Set) addToCache( bb ).get(hce);
    }
}
