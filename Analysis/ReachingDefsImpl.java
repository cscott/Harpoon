// ReachingDefsImpl.java, created Wed Feb  9 16:35:43 2000 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@tesuji.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.WorkSet;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.TYPECAST;

import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
/**
 * <code>ReachingDefsImpl</code> defines an implementation
 * for analyzing reaching definitions. Since results are
 * cached, a new <code>ReachingDefsImpl</code> should be
 * created if the code has been modified.
 * 
 * @author  Karen K. Zee <kkz@tesuji.lcs.mit.edu>
 * @version $Id: ReachingDefsImpl.java,v 1.1.2.15 2001-01-13 21:44:50 cananian Exp $
 */
public class ReachingDefsImpl extends ReachingDefs {
    final private CFGrapher cfger;
    final protected BasicBlock.Factory bbf;
    final protected Map Temp_to_BitSetFactories = new HashMap();
    final protected Map cache = new HashMap(); // maps BasicBlocks to in Sets 
    final protected boolean check_typecast; // demand the special treatment of TYPECAST
    final protected UseDefer ud;
    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> for an IR implementing
	<code>UseDefable</code> using the provided <code>CFGrapher</code>.
	This may take a while since the analysis is done at this time. 
    */
    public ReachingDefsImpl(HCode hc, CFGrapher cfger) {
	this(hc, cfger, UseDefer.DEFAULT);
    }
    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> using the provided 
	<code>CFGrapher</code> and <code>UseDefer</code>. This may
	take a while since the analysis is done at this time.
    */
    public ReachingDefsImpl(HCode hc, CFGrapher cfger, UseDefer ud) {
	super(hc);
	this.cfger = cfger;
	this.bbf = new BasicBlock.Factory(hc, cfger);
	this.ud = ud;
	// sometimes, TYPECAST need to be treated specially
	check_typecast = 
	    hc.getName().equals(harpoon.IR.Quads.QuadNoSSA.codename);
	report("Entering analyze()");
	analyze();
	report("Leaving analyze()");
    }
    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> using <code>CFGrapher.DEFAULT</code>.
	This may take a while since the analysis is done at this time.
    */
    public ReachingDefsImpl(HCode hc) {
	this(hc, CFGrapher.DEFAULT);
    }
    /** Returns the Set of <code>HCodeElement</code>s providing definitions
     *  of <code>Temp</code> <code>t</code> which reach 
     *  <code>HCodeElement</code> <code>hce</code>. */
    public Set reachingDefs(HCodeElement hce, Temp t) {
	report("Processing HCodeElement: "+hce+" Temp: "+t);
	// find out which BasicBlock this HCodeElement is from
	BasicBlock b = bbf.getBlock(hce);
	Util.assert(b != null, "no block for "+hce);
	report("In BasicBlock: "+b.toString());
	// get the map for the BasicBlock
	Map m = (Map)cache.get(b);
	report("Got map for the BasicBlock");
	// get the BitSetFactory
	BitSetFactory bsf = (BitSetFactory)Temp_to_BitSetFactories.get(t);
	report("Got BitSetFactory");
	Util.assert(m.get(t) != null, t.toString());
	// make a copy of the in Set for the Temp
	Set results = bsf.makeSet((Set)m.get(t));
	// propagate in Set through the HCodeElements 
	// of the BasicBlock in correct order
	report("Propagating...");
	for(Iterator it=b.statements().iterator(); it.hasNext(); ) {
	    HCodeElement curr = (HCodeElement)it.next();
	    if (curr == hce) return results;
	    Collection defC = null;
	    // special treatment of TYPECAST
	    if(check_typecast && (curr instanceof TYPECAST))
		defC = Collections.singleton(((TYPECAST)curr).objectref());
	    else
		defC = ud.defC(curr);
	    if (defC.contains(t)) 
		results = bsf.makeSet(Collections.singleton(curr));
	}
	Util.assert(false);
	return null; // should never happen
    }
    // do analysis
    private void analyze() {
	report("Entering getDefPts()");
	final Map Temp_to_DefPts = getDefPts();
	report("Leaving getDefPts()");
	report("Entering getDefPts()");
	getBitSets(Temp_to_DefPts);
	report("Leaving getDefPts()");
	
	// build Gen and Kill sets
	report("Entering buildGenKillSets()");
	buildGenKillSets(Temp_to_DefPts);
	report("Leaving buildGenKillSets()");

	// solve for fixed point
	report("Entering solve()");
	solve();
	report("Leaving solve()");
	// store only essential information
	for(Iterator it=cache.keySet().iterator(); it.hasNext(); ) {
	    BasicBlock b = (BasicBlock)it.next();
	    Map m = (Map)cache.get(b);
	    for(Iterator temps=m.keySet().iterator(); temps.hasNext(); ) {
		Temp t = (Temp)temps.next();
		Set[] results = (Set[])m.get(t);
		m.put(t, results[IN]);
	    }
	}
    }
    // create a mapping of Temps to a Set of possible definition points
    private Map getDefPts() {
	Map m = new HashMap();
	for(Iterator it=hc.getElementsI(); it.hasNext(); ) {
	    HCodeElement hce = (HCodeElement)it.next();
	    StringBuffer strbuf = new StringBuffer();
	    Temp[] tArray = null;
	    report("Getting defs in: "+hce+" (defC:"+new java.util.ArrayList
		   (ud.defC(hce))+")");
	    // special treatment of TYPECAST
	    if(check_typecast && (hce instanceof TYPECAST)) {
		strbuf.append("TYPECAST: ");
		tArray = new Temp[]{((TYPECAST)hce).objectref()};
	    } else {
		tArray = ud.def(hce);
		if (tArray.length > 0)
		    strbuf.append("DEFINES: ");
	    }
	    for(int i=0; i < tArray.length; i++) {
		Temp t = tArray[i];
		strbuf.append(t+" ");
		Set defPts = (Set)m.get(t);
		if (defPts == null) {
		    // have not yet encountered this Temp
		    defPts = new HashSet();
		    // add to map
		    m.put(t, defPts);
		}
		// add this definition point
		defPts.add(hce);
	    }
	    /* for debugging purposes only */
	    Collection col = ud.useC(hce);
	    if (!col.isEmpty()) strbuf.append("\nUSES: ");
	    for(Iterator it2 = col.iterator(); it2.hasNext(); )
		strbuf.append(it2.next().toString() + " ");
	    if (strbuf.length() > 0)
		report(strbuf.toString());

	}
	StringBuffer strbuf2 = new StringBuffer("Have entry for Temp(s): ");
	for(Iterator keys = m.keySet().iterator(); keys.hasNext(); )
	    strbuf2.append(keys.next()+" ");
	report(strbuf2.toString());
	return m;
    }
    // create a mapping of Temps to BitSetFactories
    // using a mapping of Temps to Sets of definitions points
    private void getBitSets(Map input) {
	for(Iterator it=input.keySet().iterator(); it.hasNext(); ) {
	    Temp t = (Temp)it.next();
	    BitSetFactory bsf = new BitSetFactory((Set)input.get(t));
	    Temp_to_BitSetFactories.put(t, bsf);
	}
    }
    private final int IN = 0;
    private final int OUT = 1;
    private final int GEN = 2;
    private final int KILL = 3;
    // return a mapping of BasicBlocks to a mapping of Temps to
    // an array of bitsets where the indices are organized as follows:
    // 0 - gen Set
    // 1 - kill Set
    // 2 - in Set
    // 3 - out Set
    private void buildGenKillSets(Map DefPts) {
	// calculate Gen and Kill sets for each basic block 
	for(Iterator blocks=bbf.blockSet().iterator(); blocks.hasNext(); ) {
	    BasicBlock b = (BasicBlock)blocks.next();
	    Map Temp_to_BitSets = new HashMap();
	    // iterate through the instructions in the basic block
	    for(Iterator it=b.statements().iterator(); it.hasNext(); ) {
		HCodeElement hce = (HCodeElement)it.next();
		Temp[] tArray = null;
		// special treatment of TYPECAST
		if(check_typecast && (hce instanceof TYPECAST))
		    tArray = new Temp[]{((TYPECAST)hce).objectref()};
		else
		    tArray = ud.def(hce);
		for(int i=0; i < tArray.length; i++) {
		    Temp t = tArray[i];
		    BitSetFactory bsf 
			= (BitSetFactory)Temp_to_BitSetFactories.get(t);
		    Set[] bitSets = new Set[4]; // 0 is Gen, 1 is Kill
		    bitSets[GEN] = bsf.makeSet(Collections.singleton(hce));
		    Set kill = new HashSet((Set)DefPts.get(t));
		    kill.remove(hce);
		    bitSets[KILL] = bsf.makeSet(kill);
		    Temp_to_BitSets.put(t, bitSets);
		}
		for(Iterator temps=DefPts.keySet().iterator(); 
		    temps.hasNext(); ) {
		    Temp t = (Temp)temps.next();
		    Set[] bitSets = (Set[])Temp_to_BitSets.get(t);
		    BitSetFactory bsf = 
			(BitSetFactory)Temp_to_BitSetFactories.get(t);
		    if (bitSets == null) {
			bitSets = new Set[4];
			Temp_to_BitSets.put(t, bitSets);
			bitSets[GEN] = bsf.makeSet(Collections.EMPTY_SET);
			bitSets[KILL] = bsf.makeSet(Collections.EMPTY_SET);
		    }
		    bitSets[IN] = bsf.makeSet(Collections.EMPTY_SET); //in
		    bitSets[OUT] = bsf.makeSet(Collections.EMPTY_SET); //out
		}
	    }
	    cache.put(b, Temp_to_BitSets);
	}
    }
    // uses Worklist algorithm to solve for reaching definitions
    // given a map of BasicBlocks to Maps of Temps to arrays of bit Sets
    private void solve() {
	int revisits = 0;
	Set blockSet = bbf.blockSet();
	WorkSet worklist;
	if (true) {
	    worklist = new WorkSet(blockSet.size());
	    Iterator iter = bbf.postorderBlocksIter();
	    while(iter.hasNext()) {
		worklist.push(iter.next());
	    }
	} else {
	    worklist = new WorkSet(blockSet);
	}

	while(!worklist.isEmpty()) {
	    BasicBlock b = (BasicBlock)worklist.pull();
	    revisits++;
	    // get all the bitSets for this BasicBlock
	    Map bitSets = (Map)cache.get(b);
	    for(Iterator it=bitSets.keySet().iterator(); it.hasNext(); ) {
		Temp t = (Temp)it.next();
		Set[] bitSet = (Set[])bitSets.get(t);
		BitSetFactory bsf = 
		    (BitSetFactory)Temp_to_BitSetFactories.get(t);
		Set[] old = new Set[2];
		old[IN] = bsf.makeSet(bitSet[IN]); // clone old in Set
		bitSet[IN].clear();
		for(Iterator preds=b.prevSet().iterator(); preds.hasNext(); ) {
		    BasicBlock pred = (BasicBlock)preds.next();
		    Set[] pBitSet = (Set[])((Map)cache.get(pred)).get(t);
		    bitSet[IN].addAll(pBitSet[OUT]); // union
		}
		old[OUT] = bitSet[OUT]; // keep old out Set
		bitSet[OUT] = bsf.makeSet(bitSet[IN]);
		bitSet[OUT].removeAll(bitSet[KILL]);
		bitSet[OUT].addAll(bitSet[GEN]);
		if (old[IN].equals(bitSet[IN]) && old[OUT].equals(bitSet[OUT]))
		    continue;
		for(Iterator succs=b.nextSet().iterator();succs.hasNext();){
		    Object block = (BasicBlock)succs.next();
		    worklist.addLast(block);
		}
	    }
	}
	if (TIME) System.out.print("(r:"+revisits+"/"+blockSet.size()+")");

    }
    // debugging utility
    private final boolean DEBUG = false;
    private void report(String str) {
	if (DEBUG) System.out.println(str);
    }
}





