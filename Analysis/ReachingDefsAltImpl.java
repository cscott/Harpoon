// ReachingDefsAltImpl.java, created Fri Jul 14 14:12:18 2000 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGrapher;
import harpoon.Temp.Temp;
import harpoon.Util.Collections.SetFactory;
import harpoon.Util.Collections.BitSetFactory;
import harpoon.Util.Collections.Factories;
import harpoon.Util.Util;
import harpoon.Util.Default;
import harpoon.Util.Worklist;
import harpoon.Util.Collections.WorkSet;
import harpoon.Util.Indexer;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Quads.TYPECAST;

import java.util.Collections;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.Set;

/**
 * <code>ReachingDefsAltImpl</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: ReachingDefsAltImpl.java,v 1.3.2.3 2002-04-07 20:22:49 cananian Exp $
 */
public class ReachingDefsAltImpl<HCE extends HCodeElement>
    extends ReachingDefs<HCE> {

    final private CFGrapher<HCE> cfger;
    final protected BasicBlock.Factory<HCE> bbf;

    // produces Set<Pair<Temp:t, HCodeElement:h>> where `h' is 
    // a definition point for `t' 
    final protected AugSetFactory bsf;
    
    // maps Temp:t -> Set:d where `bsf'-produced `d' contains all (t,x) 
    final protected Map<Temp,Set<Map.Entry<Temp,HCE>>> tempToAllDefs;

    final protected Map<BasicBlock<HCE>,Record> cache =
	new HashMap<BasicBlock<HCE>,Record>(); // maps BasicBlocks to in Sets 
    final protected boolean check_typecast; // demand the special treatment of TYPECAST
    final protected UseDefer<HCE> ud;


    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> using <code>CFGrapher.DEFAULT</code> and 
	<code>UseDefer.DEFAULT</code>.  
	This may take a while since the analysis is done at this time.
    */
    public ReachingDefsAltImpl(HCode<HCE> hc) {
	this(hc, CFGrapher.DEFAULT);
    }

    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> for an IR implementing
	<code>UseDefable</code> using the provided <code>CFGrapher</code>.
	This may take a while since the analysis is done at this time. 
    */
    public ReachingDefsAltImpl(HCode<HCE> hc, CFGrapher<HCE> cfger) {
	this(hc, cfger, UseDefer.DEFAULT);
    }
    /** Creates a <code>ReachingDefsImpl</code> object for the
	provided <code>HCode</code> using the provided 
	<code>CFGrapher</code> and <code>UseDefer</code>. This may
	take a while since the analysis is done at this time.
    */
    public ReachingDefsAltImpl(final HCode<HCE> hc, CFGrapher<HCE> cfger, final UseDefer<HCE> ud) {
	super(hc);
	this.cfger = cfger;
	this.bbf = new BasicBlock.Factory<HCE>(hc, cfger);
	this.ud = ud;
	// sometimes, TYPECAST need to be treated specially
	check_typecast = 
	    hc.getName().equals(harpoon.IR.Quads.QuadNoSSA.codename);
	report("Entering analyze() ");

	final DefPtRecord dpr = getDefPts();
	tempToAllDefs = dpr.tempToPairs;
	
	// System.out.print("constucting universe");
	Iterator<Set<Map.Entry<Temp,HCE>>> pairsets = tempToAllDefs.values().iterator();
	Set<Map.Entry<Temp,HCE>> universe = new HashSet<Map.Entry<Temp,HCE>>(tempToAllDefs.values().size());
	int totalsz = 0, numsets = 0, totsqsz = 0;
	int maxsz=0, minsz=Integer.MAX_VALUE;
	while(pairsets.hasNext()) {
	    Set<Map.Entry<Temp,HCE>> pairset = pairsets.next();
	    universe.addAll(pairset);

	    if (pairset.size() > maxsz) maxsz = pairset.size();
	    if (pairset.size() < minsz) minsz = pairset.size();
	    totalsz += pairset.size(); 
	    totsqsz += (pairset.size()*pairset.size());
	    numsets++;
	}
	

	report(" totalsz:"+totalsz +" totsqsz:"+totsqsz +
	       " maxsz:"+maxsz+" minsz:"+minsz+
	       " numsets:"+numsets +" mean sz:"+(totalsz/numsets));
	report(" numblks:"+bbf.blockSet().size()+
	       " numtmps:"+tempToAllDefs.keySet().size());
	final int meanSize = totalsz / numsets;

	report("constucting AugmentedSetFactory (uni:"+universe.size()+")");

	bsf = new AugSetFactory(universe);

	if (true) {
	    report("s/HashSet/AugSet/");
	    // replace HashSets with AugSets in tempToAllDefs.values()
	    Iterator<Map.Entry<Temp,Set<Map.Entry<Temp,HCE>>>> es = tempToAllDefs.entrySet().iterator();
	    while(es.hasNext()) {
		Map.Entry<Temp,Set<Map.Entry<Temp,HCE>>> e = es.next();
		Set<Map.Entry<Temp,HCE>> pairs = e.getValue();
		e.setValue(bsf.makeSet(pairs));
	    }
	    bsf.stats();
	
	}

	report("performing analysis");
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
    public Set<HCE> reachingDefs(HCE hce, Temp t) {
	// report("Processing HCodeElement: "+hce+" Temp: "+t);
	// find out which BasicBlock this HCodeElement is from
	BasicBlock<HCE> b = bbf.getBlock(hce);
	//assert b != null : "no block" /* +" for "+hce */;
	if(b == null) {
	    if (true) return java.util.Collections.EMPTY_SET;
       System.out.println("\nSuccC " + cfger.succC(hce));
       System.out.println("PredC " + cfger.predC(hce));
       assert false : "no block"+" for "+hce;
    }
	// report("In BasicBlock: "+b.toString());

	boolean sawIt = false;
	List<HCE> stms = b.statements();
	ListIterator<HCE> iter = stms.listIterator(stms.size());
	while(iter.hasPrevious()) {
	    HCE curr = iter.previous();
	    if (curr == hce) {
		sawIt = true;
		break;
	    }
	}
	assert sawIt;
	
	// broke out of loop, so now we need to see if exists a
	// definition in remaining hces
	while(iter.hasPrevious()) {
	    HCE curr = iter.previous();
	    
	    Collection<Temp> defC = null;
	    
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
	Record r = cache.get(b);

	// find HCodeElements associated with `t' in the IN Set
	Set<Map.Entry<Temp,HCE>> results = bsf.makeSet(r.IN);
	Set<Map.Entry<Temp,HCE>> defs = tempToAllDefs.get(t);
	if (defs == null) {
	    // no def for t
	    defs = Collections.EMPTY_SET;
	}
	results.retainAll(defs);

	Iterator<Map.Entry<Temp,HCE>> pairs = results.iterator();
	Set<HCE> results2 = new HashSet<HCE>();
	while(pairs.hasNext()) {
	    results2.add( pairs.next().getValue() );
	}

	return results2;
    }

    // do analysis
    private void analyze(Map<Temp,Set<Map.Entry<Temp,HCE>>> Temp_To_Pairs) {
	if (TIME) System.out.print("(");
	// build Gen and Kill sets
	report("Entering buildGenKillSets()");
	buildGenKillSets(Temp_To_Pairs);

	bsf.stats();
	// report("Leaving buildGenKillSets()");
	
	// solve for fixed point
	report("Entering solve()"); if (TIME) System.out.print("S");
	solve();
	// report("Leaving solve()");
	// store only essential information
	Iterator<Record> records = cache.values().iterator();
	while(records.hasNext()) {
	    Record r = records.next();
	    r.OUT = null; r.KILL = null; r.GEN = null;
	}

	if (TIME) System.out.print(")");
    }

    class DefPtRecord {
	// Temp -> Set of Pair< Temp, Defpt > >
	private Map<Temp,Set<Map.Entry<Temp,HCE>>> tempToPairs;
	// List of Pair< Temp, Defpt > 
	private ArrayList defpts;
	DefPtRecord(int mapsz) {
	    tempToPairs = new HashMap<Temp,Set<Map.Entry<Temp,HCE>>>(mapsz);
	    defpts = new ArrayList();
	}
    }

    // create a mapping of Temps to a Set of (t, defPt) Pairs, 
    // as well as a list of defPts defining more than one Temp. 
    // (the latter is to allow a more efficient indexer definition. 
    private DefPtRecord getDefPts() {
	Collection<HCE> hceL = cfger.getElements(hc);
	DefPtRecord dpr = new DefPtRecord(hceL.size());
	Map<Temp,Set<Map.Entry<Temp,HCE>>> m = dpr.tempToPairs;
	List multDefns = dpr.defpts;
	for(Iterator<HCE> it=hceL.iterator(); it.hasNext(); ) {
	    HCE hce = it.next();
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
		Set<Map.Entry<Temp,HCE>> defPts = m.get(t);
		if (defPts == null) {
		    // have not yet encountered this Temp
		    defPts = new HashSet<Map.Entry<Temp,HCE>>();
		    // add to map
		    m.put(t, defPts);
		}
		// add this definition point
		Map.Entry<Temp,HCE> pair = Default.entry(t,hce);
		defPts.add(pair);
		if (tArray.length > 1) {
		    multDefns.add(pair);
		}
	    }
	    if (false && DEBUG) {
		Collection col = ud.useC(hce);
		if (!col.isEmpty()) strbuf.append("\nUSES: ");
		for(Iterator it2 = col.iterator(); it2.hasNext(); )
		    strbuf.append(it2.next().toString() + " ");
		if (strbuf.length() > 0)
		    report(strbuf.toString());
	    }
	}
	if (false && DEBUG) {
	    StringBuffer strbuf2 = 
		new StringBuffer("Have entry for Temp(s): ");
	    for(Iterator keys = m.keySet().iterator(); keys.hasNext(); )
		strbuf2.append(keys.next()+" ");
	    report(strbuf2.toString());
	}
	return dpr;
    }

    final class Record {
	Set<Map.Entry<Temp,HCE>> IN, OUT, GEN, KILL;
	boolean haveSeen = false;
	Record() {
	    IN = bsf.makeSet();
	    OUT = bsf.makeSet();
	    GEN = bsf.makeSet();
	    KILL = bsf.makeSet();
	}
    }
    // builds a BasicBlock -> Record mapping in `cache'
    private void buildGenKillSets(Map<Temp,Set<Map.Entry<Temp,HCE>>> Temp_To_Pairs) {
	// calculate Gen and Kill sets for each basic block 
	for(Iterator<BasicBlock<HCE>> blocks=bbf.blockSet().iterator(); blocks.hasNext(); ) {
	    BasicBlock<HCE> b = blocks.next();
	    Record bitSets = new Record();
	    cache.put(b, bitSets);

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
		    Map.Entry<Temp,HCE> def = Default.entry(t, hce);
		    bitSets.GEN.add(def);
		    Set<Map.Entry<Temp,HCE>> kill = bsf.makeSet(Temp_To_Pairs.get(t));
		    kill.remove(def);
		    bitSets.KILL.addAll(kill);
		}
	    }
	}
    }
    // uses Worklist algorithm to solve for reaching definitions
    // given a BasicBlock -> Record map
    private void solve() {
	int revisits = 0;
	WorkSet worklist;

	worklist = new WorkSet(bbf.blockSet().size());
	Iterator iter = bbf.postorderBlocksIter();
	while(iter.hasNext()) {
	    worklist.addFirst(iter.next());
	}

	while(!worklist.isEmpty()) {
	    // System.out.print(worklist.size() + " ");
	    
	    // FSK is unsure of these changes (11/13/2000)
	    // BasicBlock b = (BasicBlock)worklist.pull();
	    BasicBlock b = (BasicBlock)worklist.removeFirst();
	    
	    revisits++;
	    // get all the bitSets for this BasicBlock
	    Record bitSet = cache.get(b);
	    Set oldIN, oldOUT;
	    oldIN = bsf.makeSet(bitSet.IN); // clone old in Set
	    bitSet.IN.clear();
	    for(Iterator preds=b.prevSet().iterator(); preds.hasNext(); ) {
		BasicBlock pred = (BasicBlock)preds.next();
		Record pBitSet = cache.get(pred);
		bitSet.IN.addAll(pBitSet.OUT); // union
	    }

	    // FSK is unsure of these changes (11/13/2000)
	    if (bitSet.haveSeen && oldIN.equals(bitSet.IN)) {
		// OUT is a function of IN, so if, by some miracle, IN
		// has not changed, we continue
		// System.out.print(" MIRACLE! ");
		continue;
	    }

	    oldOUT = bitSet.OUT; // keep old out Set
	    bitSet.OUT = bsf.makeSet(bitSet.IN);
	    bitSet.OUT.removeAll(bitSet.KILL);
	    bitSet.OUT.addAll(bitSet.GEN);
	    bitSet.haveSeen = true;

	    // FSK is unsure of these changes (11/13/2000)
	    // if (oldIN.equals(bitSet.IN) && oldOUT.equals(bitSet.OUT))
	    if (oldOUT.equals(bitSet.OUT)) {
		// System.out.print(" GIFT! ");
		continue;
	    }
	    for(Iterator succs=b.nextSet().iterator();succs.hasNext();){
		Object block = (BasicBlock)succs.next();
		worklist.addLast(block);
	    }
	}
	if (TIME) System.out.print("#iter:"+revisits+
				   " #bbs:"+bbf.blockSet().size());

    }
    // debugging utility
    private static final boolean DEBUG = false;
    private static void report(String str) {
	if (DEBUG) System.out.println(str+" "+new java.util.Date());
    }

    class AugSetFactory extends SetFactory {
	AugSetFactory(Set universe) { 

	    // universe = new harpoon.Util.Collections.LinearSet(universe);
	    // FSK: oog; don't do the above (BSF methods need fast
	    // universe.contains(..) method implementation

	    bitSetFact = new BitSetFactory(universe);
	    final int unisize = universe.size();
	    linToBitThreshold = unisize / 1000;
	    bitToLinThreshold = unisize / 10000;
	}

	final BitSetFactory bitSetFact;
	//final SetFactory    linSetFact = Factories.linearSetFactory;
	final SetFactory    linSetFact = 
	    new harpoon.Util.Collections.AggregateSetFactory();
	final int linToBitThreshold, bitToLinThreshold;

	int linToBitSwitches = 0;
	int bitToLinSwitches = 0;
	int startedAsBit = 0;
	int startedAsLin = 0;

	public void stats() { 
	    report("stats: "+
		   "l2b: "+linToBitSwitches+" "+
		   "b2l: "+bitToLinSwitches+" "+
		   "sal: "+startedAsLin+" "+
		   "sab: "+startedAsBit);
	}
	
	class AugSet extends java.util.AbstractSet { 
	    boolean bitSetRep;
	    Set bSet;
	    
	    // FSK:  maybe change to add HashSets for medium size
	    // (O(size-of-set) vs O(size-of-universe)

	    public AugSet(Collection c){ this(c, (c.size() > linToBitThreshold));}
	    public AugSet(Collection c, boolean useBitSetRep) {
		if (useBitSetRep) {
		    startedAsBit++;
		    bitSetRep = true;
		    if (c instanceof AugSet) {
			bSet = bitSetFact.makeSet( ((AugSet)c).bSet ); 
		    } else {
			bSet = bitSetFact.makeSet(c);
		    }
		} else {
		    startedAsLin++;
		    bitSetRep = false;
		    bSet = linSetFact.makeSet(c); 
		}
	    }
	    public boolean equals(Object o) {
		if (o instanceof AugSet) {
		    return bSet.equals( ((AugSet)o).bSet );
		} else {
		    return super.equals(o);
		}
	    }
	    public int size() { return bSet.size(); }
	    public Iterator iterator() { return bSet.iterator(); }
	    public void clear() { bSet.clear(); }
	    public boolean add(Object o) { return mayConvert(bSet.add(o)); }
	    public boolean remove(Object o){return mayConvert(bSet.remove(o));}
	    public boolean addAll(Collection c) { 
		boolean b;
		mayConvert(c.size() + bSet.size());
		if (c instanceof AugSet) {
		    // System.out.print("AU_");
		    b = bSet.addAll( ((AugSet)c).bSet );
		    // System.out.print(size());
		} else {
		    // System.out.print("SU_");
		    b = bSet.addAll(c); 
		    // System.out.print(size());
		} 
		mayConvert();
		return b; 
	    }
	    public boolean removeAll(Collection c) {
		return ((c instanceof AugSet) ?
			bSet.removeAll(((AugSet)c).bSet):
			bSet.removeAll(c));
	    }
	    public boolean retainAll(Collection c) {
		return ((c instanceof AugSet) ?
			bSet.retainAll(((AugSet)c).bSet):
			bSet.retainAll(c));
	    }
	    
	    // macro to prettify other code
	    private boolean mayConvert(boolean b) {mayConvert(); return b;}
	    private void mayConvert() { mayConvert(this.size()); }
	    private void mayConvert(int sz) {
		if (bitSetRep) {
		    if (sz < bitToLinThreshold) {
			// switch to tight rep
			bitSetRep = false;
			bitToLinSwitches++;
			bSet = linSetFact.makeSet(this);
		    } 
		} else {
		    if (sz > linToBitThreshold ) {
			// switch to bitset rep
			bitSetRep = true;
			linToBitSwitches++;
			bSet = bitSetFact.makeSet(this);
		    }
		}
	    }
	    
	}
	public Set makeSet(Collection c) {
	    return new AugSet(c);
	}
	
    }
}
