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
import harpoon.Util.Util;

import java.util.Iterator;
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
 * @version $Id: LocalCffRegAlloc.java,v 1.1.2.33 1999-08-12 20:44:51 pnkfelix Exp $
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
	return null;
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
	//    forall(l elem pseudo-regs(j))
	//       nextRef(j, l) <- indexOf(dist to next reference to l)
	Iterator instrs = b.listIterator();
	Map nextRef = new HashMap();
	Map tempToMutIntUpdata = new HashMap();
	int index=0;
	while(instrs.hasNext()) {
	    index++;
	    Instr i = (Instr) instrs.next();

	    Iterator refs = getRefs(i);
	    while(refs.hasNext()) {
		Temp ref = (Temp) refs.next();
		if (isTempRegister(ref)) continue; 
		MutInt lastRef = (MutInt) tempToMutIntUpdata.get(ref);
		if (lastRef != null) {
		    lastRef.i += index;
		    // don't need to remove lastRef from Updata, it
		    // will be implicitly removed later in
		    // Updata.put(ref, dist)
		}
		MutInt dist = new MutInt(-index);
		nextRef.put(new TempInstrPair(i, ref), dist);
		tempToMutIntUpdata.put(ref, dist);
	    }
	}
	
	// maps Regs -> PseudoRegs
	TwoWayMap regfile = new TwoWayMap();
	instrs = b.listIterator();
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    Iterator refs = getRefs(i);
	    while(refs.hasNext()) {
		Temp ref = (Temp) refs.next();
		if (isTempRegister(ref)) continue;
		boolean workOnRef = true;
		while(workOnRef) {
		    try {
			// (Step 2)
			
			// TODO (for correctness): add code here to
			// add mappings to regfile for registers that
			// are PRECOLORED (already referenced in
			// future instructions while 'ref' is still
			// live) 
			Iterator suggestions = 
			    frame.suggestRegAssignment
			    (ref, regfile);

			// TODO (to improve alloc): add code here
			// eventually to scan forward and choose a
			// suggestion based on future MOVEs to/from
			// registers.  
			List regs = (List) suggestions.next();
			code.assignRegister(i, ref, regs);
			
			InstrMEM loadInstr = 
			    new FskLoad(i.getFactory(), null,
					"FSK-LOAD", regs, ref);
			Instr.insertInstrBefore(i, loadInstr);

			Iterator regIter = regs.iterator();
			while(regIter.hasNext()) {
			    Temp reg = (Temp) regIter.next();
			    regfile.put(reg, ref);
			}
			workOnRef = false; // stop working on this ref

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
				MutInt dist = (MutInt) nextRef.get
				    (new TempInstrPair(i, pseudoReg));
				if (dist.i >= 0 && // neg -> no refs
				    dist.i < cost) { // find Min
				    cost = dist.i;
				}
			    }
			    weightedSpills.add(new WeightedSet(cand, cost));
			}
			
			// TODO: add code here to decide which
			// FurthestRef to spill (FF -> CFF)
			Set spill = (Set) weightedSpills.last();
			
			while(!spill.isEmpty()) {
			    // the set we end up spilling may be disjoint from
			    // the set we were prompted to spill,
			    // because the SpillException only
			    // accounts for making room for Load, not
			    // in properly maintaining the state of
			    // the register file
			    Temp reg = (Temp) spill.iterator().next(); 
			    Temp value = (Temp) regfile.get(reg);
			    Set regs = (Set)
				regfile.inverseMap().get(value); 
			    Iterator regsIter = regs.iterator();

			    InstrMEM spillInstr =
				new FskStore(i.getFactory(), null, 
					     "FSK-STORE", value, regs);
			    Instr.insertInstrBefore(i, spillInstr);
			}
			 
			// done spilling (now we'll loop and retry reg
			// assignment)
		    }
		}
	    }
	}
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
		s.add(key);
		rMap.put(value, s);
	    }
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

    /** mutable object wrapping around int. */
    private class MutInt {
	int i;
	MutInt(int i) { this.i = i; }
    }
}
