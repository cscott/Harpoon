// ReachingDefsAltImpl.java, created Fri Jul 14 14:12:18 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.Util;
import harpoon.Util.Default;
import harpoon.Util.Worklist;
import harpoon.Util.WorkSet;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.TYPECAST;

import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.GenericMultiMap;

import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * <code>ReachingDefsAltImpl</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ReachingDefsAltImpl.java,v 1.1.2.8 2000-08-15 01:51:39 pnkfelix Exp $
 */
public class ReachingDefsAltImpl extends ReachingDefs {
    final private CFGrapher cfger;
    final protected BasicBlock.Factory bbf;

    // produces Set<Pair<Temp:t, HCodeElement:h>> where `h' is 
    // a definition point for `t' 
    final protected BitSetFactory bsf;
    
    // maps Temp:t -> Set:d where `bsf'-produced `d' contains all (t,x) 
    final protected Map tempToAllDefs;

    final protected Map cache = new HashMap(); // maps BasicBlocks to in Sets 
    final protected boolean check_typecast; // demand the special treatment of TYPECAST
    final protected UseDefer ud;


    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> using <code>CFGrapher.DEFAULT</code> and 
	<code>UseDefer.DEFAULT</code>.  
	This may take a while since the analysis is done at this time.
    */
    public ReachingDefsAltImpl(HCode hc) {
	this(hc, CFGrapher.DEFAULT);
    }

    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> for an IR implementing
	<code>UseDef</code> using the provided <code>CFGrapher</code>.
	This may take a while since the analysis is done at this time. 
    */
    public ReachingDefsAltImpl(HCode hc, CFGrapher cfger) {
	this(hc, cfger, UseDefer.DEFAULT);
    }
    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> using the provided 
	<code>CFGrapher</code> and <code>UseDefer</code>. This may
	take a while since the analysis is done at this time.
    */
    public ReachingDefsAltImpl(HCode hc, CFGrapher cfger, UseDefer ud) {
	super(hc);
	this.cfger = cfger;
	this.bbf = new BasicBlock.Factory(hc, cfger);
	this.ud = ud;
	// sometimes, TYPECAST need to be treated specially
	check_typecast = 
	    hc.getName().equals(harpoon.IR.Quads.QuadNoSSA.codename);
	report("Entering analyze()");
	tempToAllDefs = getDefPts();
	
	Iterator pairsets = tempToAllDefs.values().iterator();
	Set universe = new HashSet();
	while(pairsets.hasNext()) {
	    universe.addAll((Set)pairsets.next());
	}

	bsf = new BitSetFactory(universe);

	// replace HashSets with BitSets in tempToAllDefs.values()
	Iterator ts = tempToAllDefs.keySet().iterator();
	while(ts.hasNext()) {
	    Object t = ts.next();
	    tempToAllDefs.put(t, bsf.makeSet((Set)tempToAllDefs.get(t)));
	}

	analyze(tempToAllDefs);
	report("Leaving analyze()");


    }


    /** Returns the Set of <code>HCodeElement</code>s providing definitions
     *  of <code>Temp</code> <code>t</code> which reach 
     *  <code>HCodeElement</code> <code>hce</code>. 
     * Any use that is not explicitly defined is assumed to be
     * implicitly defined by the root element of the
     * <code>HCode</code> for <code>this</code>. 
     */
    public Set reachingDefs(HCodeElement hce, Temp t) {
	// report("Processing HCodeElement: "+hce+" Temp: "+t);
	// find out which BasicBlock this HCodeElement is from
	BasicBlock b = bbf.getBlock(hce);
	Util.assert(b != null, "no block" /* +" for "+hce */ );
	// report("In BasicBlock: "+b.toString());

	boolean sawIt = false;
	List stms = b.statements();
	ListIterator iter = stms.listIterator(stms.size());
	while(iter.hasPrevious()) {
	    HCodeElement curr = (HCodeElement)iter.previous();
	    if (curr == hce) {
		sawIt = true;
		break;
	    }
	}
	Util.assert(sawIt);
	
	// broke out of loop, so now we need to see if exists a
	// definition in remaining hces
	while(iter.hasPrevious()) {
	    HCodeElement curr = (HCodeElement)iter.previous();
	    
	    Collection defC = null;
	    
	    // special treatment of TYPECAST
	    if(check_typecast && (curr instanceof TYPECAST))
		defC = Collections.singleton(((TYPECAST)curr).objectref());
	    else
		defC = ud.defC(curr);
	    
	    if (defC.contains(t)) {
		// System.out.print(" I");
		return Collections.singleton(curr);
	    }
	}
	
	// if we got here, then there isn't a def in the remainder
	// of the basic block... do a lookup

	// get the map for the BasicBlock
	Record r = (Record)cache.get(b);

	// find HCodeElements associated with `t' in the IN Set
	Set results = bsf.makeSet(r.IN);
	Set defs = (Set) tempToAllDefs.get(t);
	if (defs == null) {
	    // no def for t; assume that it was defined on entry
	    defs = Collections.singleton(cfger.getFirstElement(hc));
	}
	results.retainAll(defs);

	Iterator pairs = results.iterator();
	results = new HashSet();
	while(pairs.hasNext()) {
	    results.add( ((List)pairs.next()).get(1) );
	}

	return results;
    }

    // do analysis
    private void analyze(Map Temp_To_Pairs) {
	// build Gen and Kill sets
	// report("Entering buildGenKillSets()");
	buildGenKillSets(Temp_To_Pairs);
	// report("Leaving buildGenKillSets()");

	// solve for fixed point
	// report("Entering solve()");
	solve();
	// report("Leaving solve()");
	// store only essential information
	Iterator records = cache.values().iterator();
	while(records.hasNext()) {
	    Record r = (Record) records.next();
	    r.OUT = null; r.KILL = null; r.GEN = null;
	}
    }

    // create a mapping of Temps to a Set of (t, defPt) Pairs 
    private Map getDefPts() {
	Map m = new HashMap();
	for(Iterator it=hc.getElementsI(); it.hasNext(); ) {
	    HCodeElement hce = (HCodeElement)it.next();
	    StringBuffer strbuf = new StringBuffer();
	    Temp[] tArray = null;
	    //report("Getting defs in: "+hce+" (defC:"+new java.util.ArrayList(ud.defC(hce))+")");
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
		defPts.add(Default.pair(t,hce));
		
	    }
	    if (DEBUG) {
		Collection col = ud.useC(hce);
		if (!col.isEmpty()) strbuf.append("\nUSES: ");
		for(Iterator it2 = col.iterator(); it2.hasNext(); )
		    strbuf.append(it2.next().toString() + " ");
		if (strbuf.length() > 0)
		    report(strbuf.toString());
	    }
	}
	if (DEBUG) {
	    StringBuffer strbuf2 = 
		new StringBuffer("Have entry for Temp(s): ");
	    for(Iterator keys = m.keySet().iterator(); keys.hasNext(); )
		strbuf2.append(keys.next()+" ");
	    report(strbuf2.toString());
	}
	return m;
    }

    // makes the singleton set { (t,h) }
    private Set makeSet(Temp t, HCodeElement h) {
	Set s = bsf.makeSet();
	List p = Default.pair(t, h);
	s.add(p);
	return s;
    }
    final class Record {
	Set IN, OUT, GEN, KILL;
	Record() {
	    IN = bsf.makeSet();
	    OUT = bsf.makeSet();
	    GEN = bsf.makeSet();
	    KILL = bsf.makeSet();
	}
    }
    // builds a BasicBlock -> Record mapping in `cache'
    private void buildGenKillSets(Map Temp_To_Pairs) {
	// calculate Gen and Kill sets for each basic block 
	for(Iterator blocks=bbf.blockSet().iterator(); blocks.hasNext(); ) {
	    BasicBlock b = (BasicBlock)blocks.next();
	    Record bitSets = new Record();
	    cache.put(b, bitSets);

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
		    bitSets.GEN.addAll(makeSet(t, hce));
		    Set kill = bsf.makeSet((Set)Temp_To_Pairs.get(t));
		    kill.remove(hce);
		    bitSets.KILL.addAll(bsf.makeSet(kill));
		}
	    }
	}
    }
    // uses Worklist algorithm to solve for reaching definitions
    // given a BasicBlock -> Record map
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
	    Record bitSet = (Record)cache.get(b);
	    Set oldIN, oldOUT;
	    oldIN = bsf.makeSet(bitSet.IN); // clone old in Set
	    bitSet.IN.clear();
	    for(Iterator preds=b.prevSet().iterator(); preds.hasNext(); ) {
		BasicBlock pred = (BasicBlock)preds.next();
		Record pBitSet = (Record) cache.get(pred);
		bitSet.IN.addAll(pBitSet.OUT); // union
	    }
	    oldOUT = bitSet.OUT; // keep old out Set
	    bitSet.OUT = bsf.makeSet(bitSet.IN);
	    bitSet.OUT.removeAll(bitSet.KILL);
	    bitSet.OUT.addAll(bitSet.GEN);
	    if (oldIN.equals(bitSet.IN) && oldOUT.equals(bitSet.OUT))
		continue;
	    for(Iterator succs=b.nextSet().iterator();succs.hasNext();){
		Object block = (BasicBlock)succs.next();
		worklist.push(block);
	    }
	}
	if (TIME) System.out.print("(r:"+revisits+"/"+blockSet.size()+")");

    }
    // debugging utility
    private static final boolean DEBUG = false;
    private void report(String str) {
	if (DEBUG) System.out.println(str);
    }

}
