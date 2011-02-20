// ReachingDefsImpl.java, created Wed Feb  9 16:35:43 2000 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.Util;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.TYPECAST;
import harpoon.Util.Default;

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
 * @author  Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: ReachingDefsImpl.java,v 1.4 2002-04-10 02:58:48 cananian Exp $
 */
public class ReachingDefsImpl<HCE extends HCodeElement> extends ReachingDefs<HCE> {
    final private CFGrapher<HCE> cfger;
    final protected BasicBlock.Factory<HCE> bbf;
    final protected Map<Temp,BitSetFactory<HCE>> Temp_to_BitSetFactories =
	new HashMap<Temp,BitSetFactory<HCE>>();
    final protected Map<BasicBlock<HCE>,Map<Temp,Set<HCE>[]>> cache =
	new HashMap<BasicBlock<HCE>,Map<Temp,Set<HCE>[]>>(); // maps BasicBlocks to in/out Sets 
    final protected boolean check_typecast; // demand the special treatment of TYPECAST
    final protected UseDefer<HCE> ud;
    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> for an IR implementing
	<code>UseDefable</code> using the provided <code>CFGrapher</code>.
	This may take a while since the analysis is done at this time. 
    */
    public ReachingDefsImpl(HCode<HCE> hc, CFGrapher<HCE> cfger) {
	this(hc, cfger, UseDefer.DEFAULT);
    }
    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> using the provided 
	<code>CFGrapher</code> and <code>UseDefer</code>. This may
	take a while since the analysis is done at this time.
    */
    public ReachingDefsImpl(HCode<HCE> hc, CFGrapher<HCE> cfger, UseDefer<HCE> ud) {
	super(hc);
	this.cfger = cfger;
	this.bbf = new BasicBlock.Factory<HCE>(hc, cfger);
	this.ud = ud;
	// sometimes, TYPECAST need to be treated specially
	check_typecast = 
	    hc.getName().equals(harpoon.IR.Quads.QuadNoSSA.codename);
	analyze();
    }
    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> using <code>CFGrapher.DEFAULT</code>.
	This may take a while since the analysis is done at this time.
    */
    public ReachingDefsImpl(HCode<HCE> hc) {
	this(hc, CFGrapher.DEFAULT);
    }
    /** Returns the Set of <code>HCodeElement</code>s providing definitions
     *  of <code>Temp</code> <code>t</code> which reach 
     *  <code>HCodeElement</code> <code>hce</code>. Returns the empty
     *  Set if the given <code>HCodeElement</code> is unreachable. */
    public Set<HCE> reachingDefs(HCE hce, Temp t) {
	// find out which BasicBlock this HCodeElement is from
	BasicBlock<HCE> b = bbf.getBlock(hce);
	if (b == null) {
	    // dead code, no definitions reach
	    return java.util.Collections.EMPTY_SET;
	}
	// get the map for the BasicBlock
	Map<Temp,Set<HCE>[]> m = cache.get(b);
	// get the BitSetFactory
	BitSetFactory<HCE> bsf = Temp_to_BitSetFactories.get(t);
	assert m.get(t) != null : t.toString();
	// make a copy of the in Set for the Temp
	Set<HCE> results = bsf.makeSet(m.get(t)[IN]);
	// propagate in Set through the HCodeElements 
	// of the BasicBlock in correct order
	for(Iterator<HCE> it=b.statements().iterator(); it.hasNext(); ) {
	    HCE curr = it.next();
	    if (curr == hce) return results;
	    Collection<Temp> defC = null;
	    // special treatment of TYPECAST
	    if(check_typecast && (curr instanceof TYPECAST))
		defC = Collections.singleton(((TYPECAST)curr).objectref());
	    else
		defC = ud.defC(curr);
	    if (defC.contains(t)) 
		results = bsf.makeSet(Collections.singleton(curr));
	}
	assert false;
	return null; // should never happen
    }
    // do analysis
    private void analyze() {
	final Map Temp_to_DefPts = getDefPts();
	getBitSets(Temp_to_DefPts);
	
	// build Gen and Kill sets
	buildGenKillSets(Temp_to_DefPts);

	// solve for fixed point
	solve();
	// store only essential information
	for(Iterator<BasicBlock<HCE>> it=cache.keySet().iterator(); it.hasNext();) {
	    BasicBlock<HCE> b = it.next();
	    Map<Temp,Set<HCE>[]> m = cache.get(b);
	    for(Iterator<Temp> temps=m.keySet().iterator(); temps.hasNext();) {
		Temp t = temps.next();
		Set<HCE>[] results = m.get(t);
		m.put(t, new Set<HCE>[] { results[IN] } );
	    }
	}
    }
    // create a mapping of Temps to a Set of possible definition points
    private Map<Temp,Set<HCE>> getDefPts() {
	Map<Temp,Set<HCE>> m = new HashMap<Temp,Set<HCE>>();
	for(Iterator<HCE> it=cfger.getElements(hc).iterator(); it.hasNext();) {
	    HCE hce = it.next();
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
		Set<HCE> defPts = m.get(t);
		if (defPts == null) {
		    // have not yet encountered this Temp
		    defPts = new HashSet<HCE>();
		    // add to map
		    m.put(t, defPts);
		}
		// add this definition point
		defPts.add(hce);
	    }
	    /* for debugging purposes only */
	    Collection<Temp> col = ud.useC(hce);
	    if (!col.isEmpty()) strbuf.append("\nUSES: ");
	    for(Iterator<Temp> it2 = col.iterator(); it2.hasNext(); )
		strbuf.append(it2.next().toString() + " ");
	    if (strbuf.length() > 0)
		report(strbuf.toString());

	}
	StringBuffer strbuf2 = new StringBuffer("Have entry for Temp(s): ");
	for(Iterator<Temp> keys = m.keySet().iterator(); keys.hasNext(); )
	    strbuf2.append(keys.next()+" ");
	report(strbuf2.toString());
	return m;
    }
    // create a mapping of Temps to BitSetFactories
    // using a mapping of Temps to Sets of definitions points
    private void getBitSets(Map<Temp,Set<HCE>> input) {
	for(Iterator<Temp> it=input.keySet().iterator(); it.hasNext(); ) {
	    Temp t = it.next();
	    BitSetFactory<HCE> bsf = new BitSetFactory<HCE>(input.get(t));
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
    private void buildGenKillSets(Map<Temp,Set<HCE>> DefPts) {
	// calculate Gen and Kill sets for each basic block 
	for(Iterator<BasicBlock<HCE>> blocks=bbf.blockSet().iterator(); blocks.hasNext(); ) {
	    BasicBlock<HCE> b = blocks.next();
	    Map<Temp,Set<HCE>[]> Temp_to_BitSets =
		new HashMap<Temp,Set<HCE>[]>();
	    // iterate through the instructions in the basic block
	    for(Iterator<HCE> it=b.statements().iterator(); it.hasNext(); ) {
		HCE hce = it.next();
		Temp[] tArray = null;
		// special treatment of TYPECAST
		if(check_typecast && (hce instanceof TYPECAST))
		    tArray = new Temp[]{((TYPECAST)hce).objectref()};
		else
		    tArray = ud.def(hce);
		for(int i=0; i < tArray.length; i++) {
		    Temp t = tArray[i];
		    BitSetFactory<HCE> bsf 
			= Temp_to_BitSetFactories.get(t);
		    Set<HCE>[] bitSets =new Set<HCE>[4]; // 0 is Gen, 1 is Kill
		    bitSets[GEN] = bsf.makeSet(Collections.singleton(hce));
		    Set<HCE> kill = new HashSet<HCE>(DefPts.get(t));
		    kill.remove(hce);
		    bitSets[KILL] = bsf.makeSet(kill);
		    Temp_to_BitSets.put(t, bitSets);
		}
		for(Iterator<Temp> temps=DefPts.keySet().iterator(); 
		    temps.hasNext(); ) {
		    Temp t = temps.next();
		    Set<HCE>[] bitSets = Temp_to_BitSets.get(t);
		    BitSetFactory<HCE> bsf = Temp_to_BitSetFactories.get(t);
		    if (bitSets == null) {
			bitSets = new Set<HCE>[4];
			Temp_to_BitSets.put(t, bitSets);
			bitSets[GEN] = bsf.makeSet(Default.EMPTY_SET());
			bitSets[KILL] = bsf.makeSet(Default.EMPTY_SET());
		    }
		    bitSets[IN] = bsf.makeSet(Default.EMPTY_SET()); //in
		    bitSets[OUT] = bsf.makeSet(Default.EMPTY_SET()); //out
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
	WorkSet<BasicBlock<HCE>> worklist;
	if (true) {
	    worklist = new WorkSet<BasicBlock<HCE>>(blockSet.size());
	    Iterator<BasicBlock<HCE>> iter = bbf.postorderBlocksIter();
	    while(iter.hasNext()) {
		worklist.push(iter.next());
	    }
	} else {
	    worklist = new WorkSet<BasicBlock<HCE>>(blockSet);
	}

	while(!worklist.isEmpty()) {
	    BasicBlock<HCE> b = worklist.pull();
	    revisits++;
	    // get all the bitSets for this BasicBlock
	    Map<Temp,Set<HCE>[]> bitSets = cache.get(b);
	    for(Iterator<Temp> it=bitSets.keySet().iterator(); it.hasNext();) {
		Temp t = it.next();
		Set<HCE>[] bitSet = bitSets.get(t);
		BitSetFactory bsf = Temp_to_BitSetFactories.get(t);
		Set[] old = new Set[2];
		old[IN] = bsf.makeSet(bitSet[IN]); // clone old in Set
		bitSet[IN].clear();
		for(Iterator<BasicBlock<HCE>> preds=b.prevSet().iterator(); preds.hasNext(); ) {
		    BasicBlock<HCE> pred = preds.next();
		    Set<HCE>[] pBitSet = cache.get(pred).get(t);
		    bitSet[IN].addAll(pBitSet[OUT]); // union
		}
		old[OUT] = bitSet[OUT]; // keep old out Set
		bitSet[OUT] = bsf.makeSet(bitSet[IN]);
		bitSet[OUT].removeAll(bitSet[KILL]);
		bitSet[OUT].addAll(bitSet[GEN]);
		if (old[IN].equals(bitSet[IN]) && old[OUT].equals(bitSet[OUT]))
		    continue;
		for(Iterator<BasicBlock<HCE>> succs=b.nextSet().iterator();
		    succs.hasNext();){
		    BasicBlock<HCE> block = succs.next();
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





