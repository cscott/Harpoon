// AppelRegAlloc.java, created Sun Apr  8 10:56:00 2001 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Backend.Generic.Code;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.UseDefer;
import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.DataFlow.LiveTemps;

import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;

import harpoon.Util.Util;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;

public class Check {

    public static 
	void liveSetsAreConsistent( Code code, BasicBlock.Factory bbf,
				    CFGrapher cfger, UseDefer uder,
				    LiveTemps liveTemps , Set initSet ) {
	for(Iterator bbs=bbf.blocksIterator(); bbs.hasNext();) {
	    BasicBlock bb = (BasicBlock) bbs.next();
	    List stms = bb.statements();
	    Instr lastStm = (Instr) stms.get( stms.size() - 1 );
	    Set liveAfter = liveTemps.getLiveAfter( lastStm );
	    HashSet decreasingLiveAfter = new HashSet( liveAfter );
	    decreasingLiveAfter.removeAll( initSet );
	    Collection succs = (Collection) cfger.succElemC(lastStm);
	    for(Object succO : succs) {
		Instr succ = (Instr) succO;
		HashSet liveBefore = new HashSet(liveTemps.getLiveBefore( succ ));
		decreasingLiveAfter.removeAll( liveBefore );
		liveBefore.removeAll( liveAfter );
		if( ! liveBefore.isEmpty() ){
		    die( bbf, code,
			 "liveIn(succ:"+succ+"):"+liveTemps.getLiveBefore( succ )+
			 " not subset of liveOut(pred:"+lastStm+"):"+
			 liveTemps.getLiveAfter( lastStm ));
		}
	    }
	    
	    if(! decreasingLiveAfter.isEmpty() ) {

		System.out.println( "pred:"+lastStm );
		System.out.println( "succC(pred):"+cfger.succElemC( lastStm ));
		System.out.println( "liveOut(pred):"+liveTemps.getLiveAfter( lastStm ));
		System.out.println();
		die( bbf, code, 
		     "liveOut(pred:"+lastStm+
		     ") not union of liveIn(succs); "+
		     "missing:"+decreasingLiveAfter);
	    }
	}
    }
	
    
    public static 
	void allLiveVarsHaveDefs( Code code, BasicBlock.Factory bbf, 
				  CFGrapher cfger, UseDefer uder,
				  ReachingDefs rdefs, LiveTemps liveTemps ) {
	for(Iterator bbs=bbf.blocksIterator(); bbs.hasNext();) {
	    BasicBlock bb = (BasicBlock) bbs.next();
	    List stms = bb.statements();
	    Instr lastStm = (Instr) stms.get( stms.size() - 1 );
	    Set s = liveTemps.getLiveAfter( lastStm );

	    Collection succs = (Collection) cfger.succElemC(lastStm);
	    
	    for(Object tO : s){
		Temp t = (Temp) tO;
				
		// N.B. reaching-defs( lastStm, t ) alone would *NOT*
		// be correct here, because we're getting the temps
		// that are live AFTER lastStm, which means we want
		// lastStm to be included as a possible reaching-def.
		// But we don't want to do reaching-defs on the
		// successors on their own, because we want to ensure
		// that this particular flow-of-control is handled.
		
		if ( !uder.defC(lastStm).contains(t) &&
		     rdefs.reachingDefs( lastStm, t ).isEmpty() ){

		    System.out.println( liveTemps.dumpElems() );
		    System.out.println();
		    
		    die( bbf, code, 
			 "Temp: "+t+" has no defs that reach ["+lastStm+
			 "] though it is marked as live-after-there.");
		}
	    }
	}
    }

    private static 
	void die( BasicBlock.Factory bbf, Code code , String message ) {
	bbf.dumpCFG();
	System.out.println();
	code.printPreallocatedCode();
	assert false : message;
    }
}
