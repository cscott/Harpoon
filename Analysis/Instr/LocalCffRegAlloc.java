// LocalCffRegAlloc.java, created Wed Aug 11 12:08:57 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Backend.Generic.Code;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.RegFileInfo;
import harpoon.Backend.Generic.RegFileInfo.SpillException;
import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.DataFlow.LiveTemps;
import harpoon.Analysis.Instr.TempInstrPair;
import harpoon.Analysis.Instr.RegAlloc.FskLoad;
import harpoon.Analysis.Instr.RegAlloc.FskStore;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrEdge;
import harpoon.IR.Assem.InstrMEM;
import harpoon.Temp.Temp;
import harpoon.Temp.Label;
import harpoon.Util.LinearMap;
import harpoon.Util.LinearSet;
import harpoon.Util.CloneableIterator;
import harpoon.Util.Util;

import harpoon.Util.Collections.SetFactory;
import harpoon.Util.Collections.Factories;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.DefaultMultiMap;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.AbstractSet;
/** <code>LocalCffRegAlloc</code> performs <A
    HREF="http://lm.lcs.mit.edu/~pnkfelix/papers/hardnessLRA.ps">
    Local Register Allocation</A> for a given set of
    <code>Instr</code>s using a conservative-furthest-first algorithm.
    The papers <A 
    HREF="http://ctf.lcs.mit.edu/~pnkfelix/papers/OnLocalRegAlloc.ps.gz">
    "On Local Register Allocation"</A> and <A
    HREF="http://ctf.lcs.mit.edu/~pnkfelix/papers/hardnessLRA.ps">"Hardness and
    Algorithms for Local Register Allocation"</A> lay out the basis
    for the algorithm it uses to allocate and assign registers.
  
    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: LocalCffRegAlloc.java,v 1.1.2.47 1999-11-16 21:22:25 pnkfelix Exp $
 */
public class LocalCffRegAlloc extends RegAlloc {
    
    /** Creates a <code>LocalCffRegAlloc</code>. */
    public LocalCffRegAlloc(Code code) {
        super(code);
    }
    
    protected Code generateRegAssignment() {
	Iterator iter = BasicBlock.basicBlockIterator(rootBlock);
	
	LiveTemps liveTemps = 
	    new LiveTemps(iter, frame.getRegFileInfo().liveOnExit());

	iter = BasicBlock.basicBlockIterator(rootBlock);
	while(iter.hasNext()) {
	    BasicBlock b = (BasicBlock) iter.next();
	    localAlloc(b, liveTemps.getLiveOnExit(b));
	}
	return code;
    }

    /** Performs Conservative Furthest First for 'b'. 
	
	NOTE: current implementation actually does Furthest First;
	need to incorporate clean/dirty information into it to do
	Conservative Furthest First (which simply uses Cleaness as a
	tie breaker for spill choices that are equidistant)

	NOTE: there are some strange side-effects of handling
	multi-register Temps, especially in a generic manner (ie, to
	make room for a new two-register value, you may end up spilling
	four registers).  There may be PAPER-WORTHY stuff in here.

     */
    private void localAlloc(BasicBlock b, Set liveOnExit) {
	// nextRef: maps (Instr x Temp) -> (dist to next reference)
	Map nextRef = buildNextRef(b);

	// maps Regs -> PseudoRegs
	TwoWayMap regfile = new TwoWayMap();

	CloneableIterator instrs = new CloneableIterator(b.listIterator());

	Instr instr = null;
	while(instrs.hasNext()) {
	    instr = (Instr) instrs.next();

	    // skip any Spill Instructions
	    if (instr instanceof FskLoad ||
		instr instanceof FskStore) {
		continue; 
	    }

	    Iterator refs = getRefs(instr);
	    while(refs.hasNext()) {
		Temp ref = (Temp) refs.next();

		if (isTempRegister(ref)) continue;

		if (!regfile.inverseMap().getValues(ref).isEmpty()) {
		    // ref already has register assigned to it
		    continue;
		}

		int workOnRef = 1;
		while(workOnRef != 0) {
		    try {
			// (Step 2)
			
			Map precolor = 
			    getPrecolorMappings
			    (ref, (Iterator) instrs.clone(),
			     liveOnExit.contains(ref));
			regfile.putAll(precolor);
			
			
			CloneableIterator suggestions = 
			    new CloneableIterator
			    (frame.getRegFileInfo().suggestRegAssignment
			     (ref, Collections.unmodifiableMap(regfile)));

			Util.assert(workOnRef < 3, 
				    "Should not need to work "+
				    "on ref " + ref + " in Instr " + 
				    instr + " " + workOnRef + " times. " +
				    "Regfile: " +  regfile + 
				    "Suggestions: [" + 
				    printIter((Iterator)suggestions.clone()) + 
				    "]");

			// TODO (to improve alloc): add code here
			// eventually to scan forward and choose a
			// suggestion based on future MOVEs to/from
			// registers.  
			List regs = (List) suggestions.next();
			code.assignRegister(instr, ref, regs);
			
			if (instr.useC().contains(ref)) {
			    InstrMEM loadInstr = 
				new FskLoad(instr, "FSK-LOAD", regs, ref);
			    loadInstr.insertAt
				(new InstrEdge(instr.getPrev(), instr));
			}

			Iterator regIter = regs.iterator();
			while(regIter.hasNext()) {
			    Temp reg = (Temp) regIter.next();

			    Util.assert(regfile.get(reg) == null,
					"reg must be empty");

			    regfile.put(reg, ref);
			}

			Iterator futureInstrs = 
			    (Iterator) instrs.clone();
			while(futureInstrs.hasNext()) {
			    Instr finstr =
				(Instr) futureInstrs.next();
			    if (finstr.useC().contains(ref)) {
				code.assignRegister(finstr,ref,regs);
			    } else if (finstr.defC().contains(ref)) {
				// redefined 'ref' -- break and allow
				// for a different register to be
				// assigned to it.
				break;
			    }
			}

			workOnRef = 0; // stop working on this ref

		    } catch (SpillException s) {
			// (Step 3)
			Iterator spills = s.getPotentialSpills();
			SortedSet weightedSpills = new TreeSet();
			while(spills.hasNext()) {
			    Set cand = (Set) spills.next();
			    Iterator regs = cand.iterator();
			    
			    // right now cost of a set S is 
			    // MIN { nextRef(i, s) | s elem S }
			    // but it may be better to implement as 
			    // SUM { nextRef(i, s) | s elem S }
			    int cost=0;
			    while(regs.hasNext()) {
				Temp pseudoReg = (Temp)
				    regfile.get(regs.next());

				// if pseudoReg is null, then there is
				// no value in pseudoReg, so it costs
				// nothing to put a value into it.
				if (pseudoReg != null) {
				    Integer dist = (Integer) nextRef.get
					(new TempInstrPair(instr, pseudoReg));
				    if (dist != null && 
					dist.intValue() < cost) { // find Min
					cost = dist.intValue();
				    }
				}
			    }
			    weightedSpills.add(new WeightedSet(cand, cost));
			}
			
			// TODO: add code here to decide which
			// FurthestRef to spill (FF -> CFF)
			Set spill = (Set) weightedSpills.last();
			
			// System.out.println("Spilling " + printSet(spill));
			Iterator spillIter = spill.iterator();
			while(spillIter.hasNext()) {
			    // the set we end up spilling may be disjoint from
			    // the set we were prompted to spill,
			    // because the SpillException only
			    // accounts for making room for Load, not
			    // in properly maintaining the state of
			    // the register file
			    Temp reg = (Temp) spillIter.next();
			    Temp value = (Temp) regfile.get(reg);

			    if (value == null) {
				// no value associated with 'reg', so
				// we don't need to spill it; can go
				// straight to storing stuff in it

			    } else {
				
				spillValue(value, 
					   new InstrEdge(instr.getPrev(),
							 instr),
					   regfile);
			    
			    }
			}

			// done spilling (now we'll loop and retry reg
			// assignment)
			workOnRef++;
			// System.out.println("Finished Spill, retrying");
		    }
		}
	    }
	}
	// finished local alloc for 'b', so now we need to empty the
	// register file.  Note that after the loop finishes, 'instr'
	// is the last instruction in the series.
	
	
	Iterator vals = new ArrayList(regfile.values()).iterator();
	while(vals.hasNext()) {
	    Temp val = (Temp) vals.next();
	    
	    // don't spill dead values.
	    if (!liveOnExit.contains(val)) continue;
	    
	    // need to insert the spill in a place where we can be
	    // sure it will be executed; the easy case is where
	    // 'instr' does not redefine any temps (so we can just put 
	    // our spills BEFORE 'instr').  If 'instr' does define
	    // temps, however, then we MUST wait to spill, and then we
	    // need to see where control can flow...
	    // insert a new block solely devoted to spilling
	    InstrEdge loc;
	    if (instr.defC().isEmpty()) {
		loc = new InstrEdge(instr.getPrev(), instr);
		spillValue(val, loc, regfile);
	    } else {
		if (instr.canFallThrough) {
		    loc = new InstrEdge(instr, instr.getNext());
		    spillValue(val, loc, regfile);
		}

		// this is a tricky case (though it seems simple at
		// first glance)...so I'm going to punt it for now.
		Util.assert(instr.getTargets().isEmpty(), 
			    "Uh oh, we gotta handle the 'inserting-"+
			    "spills-after-an-instruction-with-"+
			    "multiple-targets' case");
	    }
	}
	
	//System.out.println("completed local alloc for " + b);
    }

    /** spills 'val', adding the necessary store at 'loc' and updates
	the 'regfile' so that it no longer has a mapping for 'val' or
	its associated registers.
    */
    private void spillValue(Temp val, 
			    InstrEdge loc, 
			    TwoWayMap regfile) {
	Collection regs = regfile.inverseMap().getValues(val);
	
	if (regs.size() > 1) { 
	    System.out.println("\n Val: " + val + 
			       " is held in " + regs);
	}
	InstrMEM spillInstr = new FskStore(loc.to, "FSK-STORE", val, regs);
	spillInstr.insertAt(loc);
	    
	// Now remove spilled regs from regfile
	ArrayList v = new ArrayList(regs);
	Iterator regsIter = v.iterator();
	while(regsIter.hasNext()) {
	    Object reg = regsIter.next();
	    
	    Util.assert(regfile.get(reg) != RegFileInfo.PREASSIGNED,
			"RegFileInfo should not be suggesting to spill "+
			"precolored registers...");

	    regfile.remove(reg);
	}
    }

    private Map buildNextRef(BasicBlock b) {
	Map nextRef = new HashMap();

	// < should change code later to attempt to perform this O(n^2) 
	//   description in O(n) time >

	// forall(j elem instrs(b))
	//    forall(l elem pseudo-regs(b))
	//       nextRef(j, l) <- indexOf(dist to next reference to l)
	CloneableIterator instrs = new CloneableIterator(b.listIterator());
	while(instrs.hasNext()) {
	    Instr j = (Instr) instrs.next();
	    Iterator refs = getRefs(j);
	    while(refs.hasNext()) {
		Temp l = (Temp) refs.next();
		if (isTempRegister(l)) continue;
		
		Iterator remainingInstrs = (Iterator) instrs.clone();
		boolean foundRef = false;
		for(int dist=1; 
		    (!foundRef) && remainingInstrs.hasNext(); 
		    dist++) {
		    
		    Instr i2 = (Instr) remainingInstrs.next();
		    if ( i2.useC().contains(l) ||
			 i2.defC().contains(l) ) {
			TempInstrPair jXl = new TempInstrPair(j, l);
			Integer prev = (Integer) nextRef.get(jXl);

			// Invariant checks...
			if (prev != null) {
			    if (prev.intValue() == dist) {
				Util.assert(false, 
					    "shouldn't be entering jXl "+
					    "twice (same)");
			    } else {
				Util.assert(false, 
					    "shouldn't be entering jXl "+
					    "twice (diff)\n"+
					    "j: " + j + " l: " + l + "\n" +
					    "i2: " + i2);
			    }
			}

			nextRef.put(jXl, new Integer(dist));
			foundRef = true;
		    }
		}
	    }
	}

	return nextRef;
    }

    /** Searches forward in <code>instrs</code> and finds all of the
	registers that are referenced while <code>ref</code> is still
	live (ie still has a use and has not been redefined).  

	If <code>ref</code> is live beyond the end of
	<code>instrs</code>, then <code>refLiveAtEnd</code> should be
	true.  If <code>ref</code> is not live beyond the end of
	<code>instrs</code>, then <code>refLiveAtEnd</code> should be
	false.
    */
    private Map getPrecolorMappings(Temp ref, Iterator instrs, 
				    boolean refLiveAtEnd) {
	Map mappings = new HashMap();
	Map potentials = new HashMap();
	boolean killedRef = false;
	while (instrs.hasNext() && !killedRef) {
	    Instr i = (Instr) instrs.next();
	    Iterator refs = getRefs(i);
	    while (refs.hasNext()) {
		Temp preg = (Temp) refs.next();
		if (isTempRegister(preg)) {
		    potentials.put(preg, RegFileInfo.PREASSIGNED);
		}
	    }
	    if (i.useC().contains(ref)) {
		mappings.putAll(potentials);
		potentials.clear();
	    }
	    if (i.defC().contains(ref)) {
		killedRef = true;
	    }
	}
	
	// ref was never redefined ==> need to check if its live
	// past end of instrs, in which case we STILL add the
	// 'potentials' mappings.
	if (!killedRef && refLiveAtEnd) 
	    mappings.putAll(potentials);
	
	return mappings;
    }

    private String printSet(Set set) {
	String s = "{ ";
	s += printIter(set.iterator()) + "}";
	return s;
    }
    
    private String printIter(Iterator iter) {
	String s = "";
	while(iter.hasNext()) {
	    s += ""+iter.next()+" ";
	}
	return s;
    }


    /** includes both pseudo-regs and machine-regs for now. */
    private Iterator getRefs(final Instr i) {
	// silly to hard code?  Are >5 refs possible?
	final ArrayList l = new ArrayList(5); 
	for (int j=0; j < i.use().length; j++) {
	    l.add(i.use()[j]);
	}
	for (int j=0; j < i.def().length; j++) {
	    if (!l.contains(i.def()[j])) l.add(i.def()[j]);
	}
	return l.iterator();
    }
    
    /** TwoWayMap maintains a many-to-one map from keys to values, and
	a one-to-many map from values to keys.  The standard Map
	methods are applied to the key->value map; to access the
	reverse mapping, call inverseMap(), which returns a
	MultiMap mapping from each value to a collection of keys. 

	I should probably change this to take a MapFactory and a
	MultiMapFactory in its constructor and then maintain the
	structure of the two maps through the methods here, just for
	generality, but if I go to that effort I'll make this part of
	the harpoon.Util.Collections package. 
    */
    private class TwoWayMap extends LinearMap {
	MultiMap rMap;

	TwoWayMap() {
	    rMap = new DefaultMultiMap
		(Factories.hashSetFactory(), 
		 Factories.hashMapFactory());
	}
	
	public void clear() { super.entrySet().clear(); rMap.clear(); }

	public Set entrySet() { 
	    return Collections.unmodifiableSet(super.entrySet()); 
	}

	/** Removes all entries that have 'val' as a value in rMap. */
	private void removeMappingsTo(Object val) {

	    Iterator entries = new ArrayList(rMap.entrySet()).iterator();
	    while(entries.hasNext()) {
		Map.Entry entry = (Map.Entry) entries.next();
		if (entry.getValue().equals(val)) {
		    // should be able to just directly remove 'entry'
		    // from 'entries' and have rMap updated
		    // accordingly, but the MultiMap class does not
		    // support that functionality yet...

		    rMap.removeAll
			(entry.getKey(), Collections.singleton(val));

		    //System.out.println("rMap: Removing entry " + entry);
		}


	    }
	}

	public Object put(Object key, Object value) {
	    removeMappingsTo(key);
	    rMap.add(value, key);
	    return super.put(key, value);
	}

	public void putAll(Map map) {
	    Iterator entries = map.entrySet().iterator();
	    while(entries.hasNext()) {
		Map.Entry entry = (Map.Entry) entries.next();
		put(entry.getKey(), entry.getValue());
	    }

	    /* Iterator keys = map.keySet().iterator();
	       while(keys.hasNext()) { removeMappingsTo(keys.next()); }
	       super.putAll(map);
	    */
	}

	public Object remove(Object key) {
	    removeMappingsTo(key);
	    return super.remove(key);
	}

	public MultiMap inverseMap() {
	    return rMap;
	}

	public String toString() {
	    String s = "[ ";
	    Iterator entries = entrySet().iterator();
	    while(entries.hasNext()){
		Map.Entry entry = (Map.Entry) entries.next();
		s += "(" + entry.getKey() + ", " + 
		    entry.getValue() + ") ";
	    }
	    s += "]";
	    return s;
	}

    }

    /** wrapper around set with an associated weight. */
    private class WeightedSet extends AbstractSet implements Comparable {
	private Set s; 
	int weight;
	WeightedSet(Set s, int i) {
	    this.s = s;
	    this.weight = i;
	}
	public int compareTo(Object o) {
	    WeightedSet s = (WeightedSet) o;
	    return (s.weight - this.weight);
	} 
	public int size() { return s.size(); }
	public Iterator iterator() { return s.iterator(); }
    }

}
