// BasicBlock.java, created Mon Mar 15 16:08:50 1999 by pnkfelix
// Copyright (C) 1998 John Whaley
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Quads;

/**
 * BasicBlock
 *
 * @author  John Whaley
 * @author  Felix Klock (pnkfelix@mit.edu)
 */

import harpoon.Util.HashSet;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.IR.Quads.HEADER;
import harpoon.IR.Quads.Quad;
import harpoon.IR.Properties.CFGrapher;
import java.util.Map;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class BasicBlock extends harpoon.Analysis.BasicBlock{
    
    public BasicBlock(Quad s, Quad e) {
	super(s, e, CFGrapher.DEFAULT);
    }

    private BasicBlock (Quad f) {
	super(f, CFGrapher.DEFAULT);
    }

    public Enumeration quads() {
	return this.elements();
    }

    public static void addEdge(BasicBlock from, BasicBlock to) {
	from.addSuccessor(to);
	to.addPredecessor(from);
	if (DEBUG) db("adding CFG edge from "+from+" to "+to);
    }
   
    /**
     * Returns something that maps starting quads to their basic blocks. 
     */ 
    public static Map computeBasicBlocks(HEADER head) {
	
	Hashtable h = new Hashtable();
	
	// set stuff up.
	BasicBlock first = new BasicBlock(head, head);
	h.put(head, first);
	
	Quad qf = head.next(1);
	BasicBlock second = new BasicBlock(qf);
	h.put(qf, second);
	addEdge(first, second);
	Util.assert(qf.nextLength() == 1);
	
	Worklist W = new HashSet();
	W.push(second);
	
	// loop
	while (!W.isEmpty()) {
	    BasicBlock current = (BasicBlock)W.pull();
	    Quad q = (Quad) current.getFirst();
	    if (DEBUG) db("now in BB "+current);
	    for (;;) {
		int n = q.nextLength();
		if (DEBUG) db("looking at "+q);
		if (n <= 0) break; // end of method
		if (n > 1) { // control flow split
		    if (DEBUG) db("control flow split, size "+n);
		    for (int i=0; i<n; ++i) {
			Quad q_n = q.next(i);
			BasicBlock bb = (BasicBlock)h.get(q_n);
			if (bb == null) {
			    h.put(q_n, bb = new BasicBlock(q_n));
			    W.push(bb);
			    if (DEBUG) db("added "+bb);
			}
			addEdge(current, bb);
		    }
		    break;
		}
		Quad qn = q.next(0);
		int m = qn.prevLength();
		if (m > 1) { // control flow join
		    if (DEBUG) db("control flow join at "+qn+", size "+m);
		    BasicBlock bb = (BasicBlock)h.get(qn);
		    if (bb == null) {
			h.put(qn, bb = new BasicBlock(qn));
			W.push(bb);
			if (DEBUG) db("added "+bb);
		    }
		    addEdge(current, bb);
		    break;
		}
		q = qn;
	    }
	    current.setLast(q);
	}
	
	if (DEBUG) db("finished computing CFG");
	
	return h;
    }
    
    public String toString() {
	return "QBB"+num;
    }
    
}
