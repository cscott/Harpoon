// ReachingDefs.java, created Wed Mar 10  9:00:54 1999 by jwhaley
// Copyright (C) 1998 John Whaley
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.DataFlow;

/**
 * ReachingDefs
 *
 *
 * Things that I want:
 * - Mapping from Quads to stuff (preferably unique, small integers)
 *   --> use getID?  how do we know the maximum number?
 * - Mapping from Temps to stuff (preferably unique, small integers)
 *
 * @author  John Whaley
 * @author Felix Klock (pnkfelix@mit.edu)
 */

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import harpoon.Analysis.EdgesIterator;
import harpoon.Analysis.BasicBlock;

import harpoon.IR.Properties.CFGraphable;
import harpoon.IR.Properties.UseDef;
import harpoon.ClassFile.HCodeElement;
import harpoon.Temp.Temp;

import harpoon.Util.IteratorEnumerator;
import harpoon.Util.BitString;
import harpoon.Util.Util;

/** <code>ReachingDefs</code> is a
    <code>ForwardDataFlowBasicBlockVisitor</code> for performing
    Reaching Definitions Analysis on any IR that implements
    <code>HCodeElement</code>, <code>CFGraphable</code>, and
    <code>UseDef</code>.  

    @author  John Whaley
    @author  Felix S Klock <pnkfelix@mit.edu>
    @version $Id: ReachingDefs.java,v 1.1.2.14 1999-11-30 05:24:43 cananian Exp $

*/
public class ReachingDefs extends ForwardDataFlowBasicBlockVisitor {

    private static final boolean DEBUG = false;

    private Map bbToSets; // BasicBlock -> ReachingDefInfo

    private Map tempsToPrsvs; // Temp -> BitString

    // Tracks max HCodeElement ID so that we know max BitString length 
    private int maxHceID;
    
    /** ReachingDefs constructor.
	<BR> <B>requires:</B> <code>q</code> implements
	<code>HCodeElement</code> and <code>UseDef</code>.
    */
    public ReachingDefs(CFGraphable q) {
	bbToSets = new Hashtable();
	this.maxHceID = QuadSolver.getMaxID((HCodeElement) q);
	initTempsToPrsvs(q);
    }
    
    /** Initializes the map between temps and their preserve sets.  The
	preserve sets contain all quads that do NOT define the given
	temp.  

	<BR> <B>requires:</B> <code>root</code> and all
	                      <code>CFGraphable</code> linked to by
			      <code>root</code> implement 
	                      <code>HCodeElement</code> and
			      <code>UseDef</code>. 
        <BR> <B>effects:</B> <B>TODO:</B> fill in effects clause. 
    */
    void initTempsToPrsvs(CFGraphable root) {
	tempsToPrsvs = new HashMap();
	Enumeration q_en = new IteratorEnumerator(new EdgesIterator(root));
	while (q_en.hasMoreElements()) {
	    UseDef q = (UseDef)q_en.nextElement();
	    Temp[] defs = q.def();
	    for (int i=0, n=defs.length; i<n; ++i) {
		Temp t = defs[i];
		BitString bs = (BitString)tempsToPrsvs.get(t);
		if (bs == null) {
		    tempsToPrsvs.put(t, bs = new BitString(maxHceID));
		    bs.setUpTo(maxHceID);
		}
		bs.clear( ((HCodeElement)q).getID());
	    }
	}
    }

    /** Merges operation on the from and to basic block.  Returns true
	if the to basic block changes. 
	
	<BR> <B>requires:</B> The instructions in <code>f</code> and
 	                      <code>t</code> implement
			      <code>HCodeElement</code>
    */
    public boolean merge(BasicBlock f, BasicBlock t) {
	ReachingDefInfo from_info = getInfo(f);
	Util.assert(from_info != null);
	BasicBlock from = f;
	BasicBlock to   = t;
	
	boolean result = false;
	ReachingDefInfo to_info = getInfo(to);
	if (to_info == null) {
	    putInfo(to, to_info = 
		    new ReachingDefInfo(to, maxHceID, tempsToPrsvs));
	    result = true;
	}
	if (DEBUG) db("looking at in set of "+to+": "+to_info.inSet);
	if(to_info.mergePredecessor(from_info)) {
	    if (DEBUG) db("in set of "+to+" changed to "+to_info.inSet);
	    result = true;
	}
	return result;
    }
    
    /** Visit (Transfer) function.  In our case, it simply updates the
	out set.   
	
    */
    public void visit(BasicBlock bb) {
	ReachingDefInfo info = getInfo(bb);
	if (info == null) {
	    info = new ReachingDefInfo(bb, maxHceID, tempsToPrsvs);
	    putInfo(bb, info);
	}
	info.updateOutSet();
    }

    public ReachingDefInfo getInfo(BasicBlock bb) {
	return (ReachingDefInfo)bbToSets.get(bb);
    }
    
    public void putInfo(BasicBlock bb, ReachingDefInfo info) {
	bbToSets.put(bb, info);
    }
    
    public String dump() {
	StringBuffer s = new StringBuffer();
	Iterator e = bbToSets.keySet().iterator();
	while (e.hasNext()) {
	    BasicBlock bb = (BasicBlock)e.next();
	    s.append("Basic block "+bb);
	    ReachingDefInfo rdi = getInfo(bb);
	    s.append("\n"+rdi);
	}
	return s.toString();
    }
    
}
