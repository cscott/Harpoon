// LocalCffRegAlloc.java, created Wed Aug 11 12:08:57 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Backend.Generic.Code;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Frame.SpillException;
import harpoon.Analysis.DataFlow.BasicBlock;
import harpoon.Analysis.Instr.TempInstrPair;
import harpoon.Analysis.Instr.RegAlloc.FskLoad;
import harpoon.Analysis.Instr.RegAlloc.FskStore;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.Temp.Temp;
import harpoon.Util.LinearMap;
import harpoon.Util.LinearSet;
import harpoon.Util.CloneableIterator;
import harpoon.Util.Util;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.SortedSet;
import java.util.AbstractSet;
/**
 * <code>LocalCffRegAlloc</code>
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LocalCffRegAlloc.java,v 1.1.2.37 1999-08-18 17:52:30 pnkfelix Exp $
 */
public class LocalCffRegAlloc extends RegAlloc {
    
    /** Creates a <code>LocalCffRegAlloc</code>. */
    public LocalCffRegAlloc(Code code) {
        super(code);
    }
    
    protected Code generateRegAssignment() {
	Iterator iter = BasicBlock.basicBlockIterator(rootBlock);
	while(iter.hasNext()) {
	    BasicBlock b = (BasicBlock) iter.next();
	    localAlloc(b);
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
    private void localAlloc(BasicBlock b) {
	// (Step 1)
	// the following code is an attempt to perform this O(n^2)
	// description in O(n) time:  
	// forall(j elem instrs(b))
	//    forall(l elem pseudo-regs(b))
	//       nextRef(j, l) <- indexOf(dist to next reference to l)
	ListIterator instrs = b.listIterator();

	// nextRef: maps (Instr x Temp)->(dist to next reference)
	Map nextRef = new HashMap();
	
	// index: main index counter
	int index = 0;

	// indexUpdata: maps a Temp->delta(dist)
	// where delta(dist) is the difference from the main index
	// counter that yields the distance to the next reference.
	// To find the actual distance, do "index + delta(dist)"
	Map indexUpdata = new HashMap();

	// first go FORWARD through instrs and set up
	// indexUpdata map with initial values for all refs
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    Iterator refs = getRefs(i);
	    while(refs.hasNext()) {
		Temp ref = (Temp) refs.next();
		if (isTempRegister(ref)) continue; 
		// every body starts off with +1 (so that all temps
		// are live on exit, and have a distance of one more
		// than the end of the list)
		indexUpdata.put(ref, new Integer(1));
	    }
	}
	
	// temps: set of all refs in basic block
	Set temps = indexUpdata.keySet();

	// now go BACKWARD and build the nextRef table while
	// simultaneously updating the indexUpdata
	while(instrs.hasPrevious()) {
	    Instr i = (Instr) instrs.previous();
	    // first update nextRef
	    Iterator tempIter = temps.iterator();
	    while(tempIter.hasNext()) {
		Temp t = (Temp) tempIter.next();
		Integer mi = (Integer) indexUpdata.get(t);
		nextRef.put(new TempInstrPair(i, t), 
			    new Integer(index + mi.intValue()));
	    }

	    // second update indexUpdata
	    Iterator refs = getRefs(i);
	    while(refs.hasNext()) {
		Temp ref = (Temp) refs.next();
		if (isTempRegister(ref)) continue;
		// this will be added to a new index later, so they
		// will cancel eachother out to yield the real
		// distance. 
		indexUpdata.put(ref, new Integer(-index));
	    }
	    
	    // finally increment the index
	    index++;
	}
	
	if(true) { // debugging code 
	    Iterator instrIter = b.listIterator();
	    while(instrIter.hasNext()) {
		Instr i = (Instr) instrIter.next();
		Iterator refIter = getRefs(i);
		while(refIter.hasNext()) {
		    Temp ref = (Temp) refIter.next();
		    if (isTempRegister(ref)) continue;
		    Integer dist = (Integer) 
			nextRef.get(new TempInstrPair(i, ref));
		    Util.assert(dist != null, "Should have a "+
				"mapping from " + i + " x " + 
				ref + " to a dist for block " + b);
		}
	    }
	}

	// maps Regs -> PseudoRegs
	TwoWayMap regfile = new TwoWayMap();
	instrs = b.listIterator();
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();

	    // skip any Spill Instructions
	    if (i instanceof FskLoad ||
		i instanceof FskStore) {
		continue; 
	    }

	    Iterator refs = getRefs(i);
	    while(refs.hasNext()) {
		Temp ref = (Temp) refs.next();
		if (isTempRegister(ref)) continue;
		int workOnRef = 1;
		while(workOnRef != 0) {
		    try {
			// (Step 2)
			
			// TODO (for correctness): add code here to
			// add mappings to regfile for registers that
			// are PRECOLORED (already referenced in
			// future instructions while 'ref' is still
			// live) 
			CloneableIterator suggestions = 
			    new CloneableIterator
			    (frame.suggestRegAssignment
			     (ref, regfile));

			Util.assert(workOnRef < 3, 
				    "Should not need to work "+
				    "on ref " + ref + " in Instr " + 
				    i + " " + workOnRef + " times. " +
				    "Regfile: " +  regfile + 
				    "Suggestions: [" + 
				    printIter((Iterator)suggestions.clone()) + 
				    "]");

			// TODO (to improve alloc): add code here
			// eventually to scan forward and choose a
			// suggestion based on future MOVEs to/from
			// registers.  
			List regs = (List) suggestions.next();
			code.assignRegister(i, ref, regs);
			
			InstrMEM loadInstr = 
			    new FskLoad(i.getFactory(), i,
					"FSK-LOAD", regs, ref);
			Instr.insertInstrBefore(i, loadInstr);

			Iterator regIter = regs.iterator();
			while(regIter.hasNext()) {
			    Temp reg = (Temp) regIter.next();
			    regfile.put(reg, ref);
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
				// pseudoReg should NOT be null; Frame
				// spec says it won't tell us to spill
				// regs that are not occupied.
				Util.assert(pseudoReg != null, 
					    "pseudoReg should not be null");

				Integer dist = (Integer) nextRef.get
				    (new TempInstrPair(i, pseudoReg));
				if (dist.intValue() < cost) { // find Min
				    cost = dist.intValue();
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
			    Set regs = (Set) regfile.inverseMap().get(value); 
			    
			    Util.assert(i != null, "i should not be null");
			    Util.assert(regs != null, 
					"regs should not be null"+
					" regfile: "+regfile+
					" reg: "+reg+
					" val: "+value);

			    InstrMEM spillInstr =
				new FskStore(i.getFactory(), i, 
					     "FSK-STORE", value, regs);
			    Instr.insertInstrBefore(i, spillInstr);
			    
			    // Now remove spilled regs from regfile
			    Iterator regsIter = regs.iterator();
			    // the iterator returned relies on the
			    // internal structure of regfile, which we
			    // are modifying.  Therefore, store the
			    // elements of regsIter in a temporary
			    // vector. 
			    ArrayList v = new ArrayList(regs.size());
			    while(regsIter.hasNext()) {
				v.add(regsIter.next());
			    }
			    regsIter = v.iterator();
			    while(regsIter.hasNext()) {
				regfile.remove(regsIter.next());
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
	
	System.out.println("completed local alloc for " + b);
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
	a one-to-many map from value to keys.  The standard Map
	methods are applied to the key to value map; to access the
	reverse mapping, call inverseMap(), which returns a
	Map[value->Set[key]].

	I should probably change this to take a pair of maps in its
	constructor and then maintain the structure of the two maps
	through the methods here, just for generality, but if I go to
	that effort I'll make this part of the harpoon.Util package.
    */
    private class TwoWayMap extends LinearMap {
	Map rMap;

	TwoWayMap() {
	    rMap = new LinearMap();
	}
	
	public void clear() { super.entrySet().clear(); rMap.clear(); }
	public Set entrySet() { 
	    return Collections.unmodifiableSet(super.entrySet()); 
	}
	public Object put(Object key, Object value) {
	    Set s = (Set) rMap.get(value);
	    if (s == null) {
		s = new LinearSet();
		rMap.put(value, s);
	    }
	    s.add(key);
	    return super.put(key, value);
	}
	public Object remove(Object key) {
	    Object prev = super.remove(key);
	    Set s = (Set) rMap.get(prev);
	    Util.assert(s.remove(key), "key:"+key+
			" should have been in Set "+
			"associated with value:"+prev);
	    if (s.isEmpty()) {
		rMap.remove(prev);
	    }
	    return prev;
	}
	public Map inverseMap() {
	    return Collections.unmodifiableMap(rMap);
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
