// LiveVars.java, created Wed May 26 16:53:26 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

import harpoon.IR.Properties.UseDef;
import harpoon.Temp.Temp;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Iterator;

/**
 * <code>LiveVars</code> performs Liveness Analysis for the variables
 * in the BasicBlocks passed to it.
 *
 * <BR> <B>NOTE:</B> This implementation is <B>very</B> inefficient and
 * space-heavy.  Look into using <code>BitString</code> as an internal
 * rep instead of <code>Set</code>s of <code>Temp</code>s.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: LiveVars.java,v 1.1.2.4 1999-06-16 17:00:57 pnkfelix Exp $
 */
public class LiveVars extends BackwardDataFlowBasicBlockVisitor {
    
    // maps a BasicBlock 'bb' to the LiveVarInfo associated with 'bb'
    private Map bbToLvi;
    
    /** Constructs a new <code>LiveVars</code> for <code>basicblocks</code>
	<BR> <B>requires:</B> 
	     <BR> 1. <code>basicblocks</code> is a
	          <code>Iterator</code> of <code>BasicBlock</code>s,	       
	     <BR> 2. All of the instructions in
	          <code>basicblocks</code> implement
		  <code>UseDef</code>
	     <BR> 3. No element of <code>basicblocks</code> links to a
	          <code>BasicBlock</code> not contained within
		  <code>basicblocks</code>
	     <BR> 4. No <code>BasicBlock</code> is repeatedly iterated
	          by <code>basicblocks</code>
	 <BR> <B>modifies:</B> <code>basicblocks</code>
	 <BR> <B>effects:</B> constructs a new
	      <code>BasicBlockVisitor</code> and initializes its
	      internal datasets for analysis of the
	      <code>BasicBlock</code>s in <code>basicblocks</code>
    */	     
    public LiveVars(Iterator basicblocks) {
	bbToLvi = new HashMap();
	while(basicblocks.hasNext()) {
	    BasicBlock bb = (BasicBlock) basicblocks.next();
	    LiveVarInfo lvi = makeUseDef(bb);
	    bbToLvi.put(bb, lvi);
	}
    }


    /** Merge (confluence) operator.
	<BR> LVout(bb) = Union over (j elem Succ(bb)) of ( LVin(j) ) 
    */
    public boolean merge(BasicBlock from, BasicBlock to) {
	LiveVarInfo finfo = (LiveVarInfo) bbToLvi.get(from);
	LiveVarInfo tinfo = (LiveVarInfo) bbToLvi.get(to);
	return tinfo.lvOUT.addAll(finfo.lvIN);
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
    
    /** Initializes the USE/DEF information for bb and stores it in
	the return LiveVarInfo.K
    */
    private LiveVarInfo makeUseDef(BasicBlock bb) {
	LiveVarInfo info = new LiveVarInfo();
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

    /** Returns the <code>Set</code> of <code>Temp</code>s that are
	live on entry to <code>b</code>
	<BR> <B>requires:</B> A DataFlow Solver has been run to
	     completion on the graph of <code>BasicBlock</code>s
	     containing <code>b</code> with <code>this</code> as the
	     <code>DataFlowBasicBlockVisitor</code>.
    */
    public Set getLiveOnEntry(BasicBlock b) {
	LiveVarInfo lvi = (LiveVarInfo) bbToLvi.get(b);
	return lvi.lvIN;
    }

    /** Returns the <code>Set</code> of <code>Temp</code>s that are
	live on exit from <code>b</code>
	<BR> <B>requires:</B> A DataFlow Solver has been run to
	     completion on the graph of <code>BasicBlock</code>s
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
	    s.append("\n -- " + bb + " INSTRS --\n" + 
		     bb.dumpElems() + 
		     " -- END " + bb + " --\n");
	}
	return s.toString();
    }

    // LiveVarInfo groups together four sets: 
    // use(bb): set of vars used in 'bb' before being defined in 'bb',
    //          if at all. 
    // def(bb): set of vars defined in 'bb' before being used in 'bb',
    //          if at all. 
    // lvIN(bb):  ( lvOUT(bb) - DEF(bb) ) U USE(bb)
    // lvOUT(bb): Union over (j elem Succ(bb)) of ( lvIN(j) )
    private static class LiveVarInfo {
	Set use;
	Set def;
	Set lvIN;
	Set lvOUT;

	LiveVarInfo() { 
	    use = new HashSet();
	    def = new HashSet();
	    lvIN = new HashSet();
	    lvOUT = new HashSet();
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

