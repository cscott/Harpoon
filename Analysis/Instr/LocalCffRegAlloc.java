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
import harpoon.Util.Default;
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
    @version $Id: LocalCffRegAlloc.java,v 1.1.2.57 2000-01-18 15:23:38 pnkfelix Exp $
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

	System.out.print(" LVA");

	harpoon.Analysis.DataFlow.Solver.worklistSolve
	    (BasicBlock.basicBlockIterator(rootBlock),
	     liveTemps);

	System.out.print(" LocalAlloc");


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
	LocalAllocator la = new LocalAllocator(b, liveOnExit);
	la.doAlloc();
    }

    private class LocalAllocator {
	// nextRef: maps (Instr x Temp) -> (dist to next reference)
	Map nextRef;

	// maps Regs -> PseudoRegs
	TwoWayMap regfile;

	// tracks our current position in the instruction stream
	CloneableIterator instrs;

	// stores previous mappings for precolored registers,
	// so that we can restore the old mappings.
	Map archivedEntries;
	
	// Instr we are currently allocating for.
	Instr instr;

	// Set of Temps that are live on exit from the basic block
	// this is allocating for
	Set liveOnExit;

	// BasicBlock were are allocting for (use only for debug)
	BasicBlock b;
	
	LocalAllocator(BasicBlock b, Set liveOnExit) {
	    nextRef = buildNextRef(b);
	    regfile = new TwoWayMap();
	    instrs = new CloneableIterator(b.listIterator());
	    archivedEntries = Default.EMPTY_MAP;
	    this.liveOnExit = liveOnExit;
	    this.b = b;
	}

	void doAlloc() {
	    while(instrs.hasNext()) {
		instr = (Instr) instrs.next();
		
		// skip any Spill Instructions
		if (instr instanceof FskLoad ||
		    instr instanceof FskStore) {
		    continue; 
		}
		
		Iterator refs = getRefs(instr);
		// Iterator uses = instr.useC().iterator();
		
		while(refs.hasNext()) {
		    Temp ref = (Temp) refs.next();

		    regfile.putAll(archivedEntries);
		    Map precolor = getPrecolorMappings
			(ref, (Iterator) instrs.clone(),
			 liveOnExit.contains(ref));
		    archivedEntries = putAllArchive(precolor, regfile);
		    
		    int workOnRef = 0;
		    while(true) {
			workOnRef++;
			Util.assert(workOnRef <= 5,
				    "should not need to work on"+
				    "Ref: "+ref+" in Instr:"+instr+
				    " more than 5 times");
			try {
			    doAlloc(ref);
			    break;
			} catch (SpillException s) {
			    chooseSpill(s.getPotentialSpills());
			}
		    }
		}
		

		if (RegAlloc.DEBUG) { 
		    // finished local alloc for 'instr'
		    // lets verify
		    Iterator refIter = getRefs(instr);
		    while(refIter.hasNext()) {
			Temp ref = (Temp) refIter.next();
			Util.assert(isTempRegister(ref) ||
				    code.registerAssigned(instr, ref),
				    "Instr: "+instr + " / " +
				    code.toAssemRegsNotNeeded(instr) +
				    " needs register "+
				    "assignment for Ref: "+ref);
				    
		    }
		}
	    }

	    // finished local alloc for 'b', so now we need to empty the
	    // register file.  Note that after the loop finishes, 'instr'
	    // is the last instruction in the series.
	    emptyRegFile();
	}


	void doAlloc(final Temp ref) throws SpillException {
	    // hard coded ref to register --> skip
	    if (isTempRegister(ref)) return;
	    
	    // ref already has register assigned to it --> skip
	    if (code.registerAssigned(instr, ref)) {
		Util.assert( !regfile.inverseMap().getValues(ref).isEmpty(),
			     new Object() { 
		    // made Lazy 'cause dumpElems is SLOW
		    public String toString() {
			return "If a ref: "+ref+" is assigned a register, it "+
			    "should have a mapping in the regfile"+
			    "\nLiveOnExit: "+ liveOnExit +
			    "\nREGFILE: "+ regfile + 
			    "\nINSTR: " +instr+" / "+code.toAssemRegsNotNeeded(instr)+
			    "\nBLOCK: \n" + b.dumpElems()+
			    "";}});
		return;
	    }
	    
	    // (Step 2)


	    /* notes: (FSK)
	       The RIGHT way to do this is not to keep scanning over
	       and over the instruction stream doing the assignment
	       and then taking it back when a spill occurs.
	       Instead, should be tracking the current state of the
	       register file during traversal.  If a Temp in this instr
	       currently has a register assignment, then do
	       code.assignReg().  Else find an assignment as below.
	       This avoids several repeated traversals.

	       However the current design of the Register File
	       doesn't allow this to extract a Register Assignment for
	       a given Temp; it *can* give the Set of Registers that
	       are allocated for the Temp, but it loses the ordering
	       information needed to actually perform an assignment.

	       So, I need to redesign the register file abstraction to
	       do this.  (Perhaps get rid of TwoWayMap and just
	       explicitly use two maps?)

	       Also, when I do redesign the regfile, I need to make
	       sure to track clean/dirty info as I go, so I can know
	       if I need to spill or not.
	     */
	    Collection prevAssignment = 
		regfile.inverseMap().getValues(ref);
	    if (!prevAssignment.isEmpty()) {
		// FSK: this is inelegant.  I shouldn't have to
		// manually remove these mappings.  Look into an
		// alternate solution.  -->  If I implement as in the
		// notes above, then I won't need to do this, at least
		// not here 
		removeMapping(ref, regfile);
	    }


	    
	    CloneableIterator suggs = 
		new CloneableIterator
		(frame.getRegFileInfo().suggestRegAssignment
		 (ref, Collections.unmodifiableMap(regfile)));
	    
	    List regs = chooseSuggestion(suggs);
	    code.assignRegister(instr, ref, regs);
	    
	    if (instr.useC().contains(ref)) {
		InstrMEM loadInstr = new FskLoad(instr, "FSK-LOAD", regs, ref);
		loadInstr.insertAt(new InstrEdge(instr.getPrev(), instr));
	    }
	    
	    putMappings(regs.iterator(), ref);
	    
	    assignFutureInstrs((Iterator)instrs.clone(), ref, regs);
	    
	    if (RegAlloc.DEBUG) {
		// verify local alloc for 'ref'
		Util.assert(isTempRegister(ref) ||
			    code.registerAssigned(instr, ref),
			    "Ref: "+ref + " / " +
			    code.toAssemRegsNotNeeded(instr) +
			    " needs register "+
			    "assignment in Instr: "+instr);
	    }
	}
	
	private void chooseSpill(Iterator spills) {
	    SortedSet weightedSpills = new TreeSet();
	    int count = 0;
	    while(spills.hasNext()) {
		count++;
		Set cand = (Set) spills.next();
		Iterator regs = cand.iterator();
		
		// right now cost of a set S is 
		// MAX { nextRef(i, s) | s elem S }
		// but it may be better to implement as 
		// SUM { nextRef(i, s) | s elem S }
		int cost=0;
		while(regs.hasNext()) {
		    Temp pseudoReg = (Temp) regfile.get(regs.next());
		    
		    // if pseudoReg is null, then there is
		    // no value in register, so it costs
		    // nothing to put a value into it.
		    if (pseudoReg != null) {
			Integer dist = (Integer) nextRef.get
			    (new TempInstrPair(instr, pseudoReg));
			if (dist != null) {
			    int c = Integer.MAX_VALUE - dist.intValue(); 
			    if (c > cost) { // find Max
				cost = c;
			    }
			} 
		    }
		}
		
		// System.out.println("Adding "+cand+" with cost "+cost);
		
		weightedSpills.add(new WeightedSet(cand, cost));
	    }
	    
	    // TODO: add code here to decide which
	    // FurthestRef to spill (FF -> CFF)
		// Unfortunately, TreeSet does NOT create a collection
		// of elements with the same cost, it just doesn't add
		// the object if one with the same cost already
		// exists.  (since TreeSet requires that the
		// Comparables stored be "consistent with equals",
		// this is not an error)
		// Need to come up with some sort of
		// OrderedMultiSet/OrderedBag abstraction, so that I
		// can do the FF -> CFF changes.
	    Set spill = (Set) weightedSpills.last();
	    
	    //System.out.println("Spilling " + printSet(spill)+
	    //			 " of "+weightedSpills.size()+"/"+
	    //			 count+" choices");

	    Iterator spillIter = spill.iterator();
	    while(spillIter.hasNext()) {
		// the set we end up spilling may be disjoint from the
		// set we were prompted to spill, because the
		// SpillException only accounts for making room for
		// Load, not in properly maintaining the state of the
		// register file
		Temp reg = (Temp) spillIter.next();
		Temp value = (Temp) regfile.get(reg);
		
		// System.out.println(reg + " maps to " + value);

		if (value == null) {
		    // no value associated with 'reg', so we don't
		    // need to spill it; can go straight to storing
		    // stuff in it
		    
		} else {
		    
		    spillValue(value, 
			       new InstrEdge(instr.getPrev(), instr),
			       regfile);
		    
		    code.removeAssignment(instr, value);
		    Iterator fInstrs = (Iterator) instrs.clone();
		    while(fInstrs.hasNext()) {
			Instr i = (Instr) fInstrs.next();
			if (code.registerAssigned(i, value)) {
			    code.removeAssignment(i, value);
			}
		    }
		}
	    }
	}
	
	private void emptyRegFile() {
	    
	    // System.out.println("live on exit from " + b + " :\n" + liveOnExit);
	    
	    // use a HashSet here because we don't want to repeat values
	    // (regfile.values() returns a Collection-view)
	    Iterator vals = (new HashSet(regfile.values())).iterator();
	    
	    while(vals.hasNext()) {
		Temp val = (Temp) vals.next();
		
		// System.out.println("dealing with " + val + " at end of " + b);
		
		// don't spill dead values.
		if (!liveOnExit.contains(val)) continue;
		
		// need to insert the spill in a place where we can be
		// sure it will be executed; the easy case is where
		// 'instr' does not redefine the 'val' (so we can just put 
		// our spills BEFORE 'instr').  If 'instr' does define
		// 'val', however, then we MUST wait to spill, and
		// then we need to see where control can flow...
		// insert a new block solely devoted to spilling
		InstrEdge loc;
		if (!instr.defC().contains(val)) {
		    loc = new InstrEdge(instr.getPrev(), instr);
		    
		    // System.out.println("end spill: " + val + " " + loc);
		    
		    spillValue(val, loc, regfile);
		} else {
		    if (instr.canFallThrough) {
			loc = new InstrEdge(instr, instr.getNext());
			
			// System.out.println("end spill: " + val + " " + loc);
			
			// This sequence of code is a little tricky; since
			// we need to add spills for the same variable at
			// multiple locations, we need to delay updating
			// the regfile until after all of the spills have
			// been added.  So we need to use
			// addSpillInstr/removeMapping instead of just
			// spillValue 
			
			addSpillInstr(val, loc, regfile);
		    }
		    
		    Util.assert(instr.getTargets().isEmpty() ||
				instr.hasModifiableTargets(),
				"We MUST be able to modify the targets "+
				" if we're going to insert a spill here");
		    Iterator targets = instr.getTargets().iterator();
		    while(targets.hasNext()) {
			Label l = (Label) targets.next();
			loc = new InstrEdge(instr, instr.getInstrFor(l));
			// System.out.println("end spill: " + val + " " + loc);
			addSpillInstr(val, loc, regfile);
		    }
		    removeMapping(val, regfile);
		}
	    }
	}

	private void putMappings(Iterator regIter, Temp ref) {
	    while(regIter.hasNext()) {
		Temp reg = (Temp) regIter.next();
		
		Util.assert(regfile.get(reg) == null,
			    "reg " + reg + " must be empty");
		
		regfile.put(reg, ref);
	    }
	}

	/** spills 'val', adding the necessary store at 'loc' and updates
	    the 'regfile' so that it no longer has a mapping for 'val' or
	    its associated registers.
	*/
	private void spillValue(Temp val, InstrEdge loc, TwoWayMap regfile) {
	    addSpillInstr(val, loc, regfile);
	    removeMapping(val, regfile);
	}
	
	/** adds a store for 'val' at 'loc', but does *NOT* update the
	    regfile. 
	*/
	private void addSpillInstr(Temp val, InstrEdge loc, TwoWayMap regfile) {
	    Collection regs = regfile.inverseMap().getValues(val);
	    
	    Util.assert(!regs.isEmpty(), 
			val + " must map to SOME registers" +
			"\n regfile:" + regfile);
	    
	    if (false && regs.size() > 1) { // debugging stuff
		System.out.println("\n Val: " + val + 
				   " is held in " + regs);
	    }
	    InstrMEM spillInstr = new FskStore(loc.to, "FSK-STORE", val, regs);
	    spillInstr.insertAt(loc);
	}
	
	/** Removes spilled regs from regfile. */
	private void removeMapping(Temp val, TwoWayMap regfile) {
	    Collection regs = regfile.inverseMap().getValues(val);
	    ArrayList v = new ArrayList(regs);
	    Iterator regsIter = v.iterator();
	    while(regsIter.hasNext()) {
		Object reg = regsIter.next();
		
		Util.assert(! (regfile.get(reg) 
			       instanceof RegFileInfo.PreassignTemp), 
			    "RegFileInfo should not be suggesting to spill "+
			    "precolored registers...");
		
		regfile.remove(reg);
	    }
	}

    }

    private static List chooseSuggestion(Iterator suggs) {
	// TODO (to improve alloc): add code here eventually to scan
	// forward and choose a suggestion based on future MOVEs
	// to/from preassigned registers.  Obviously the signature of
	// the function may need to change...
			
	// FSK: dumb chooser (just takes first suggestion)
	return (List) suggs.next(); 
    }

    /** Returns a Set[Map.Entry] of the mappings that
	have been replaced in 'target' by the mappings in 'source'.
    */
    private static Map putAllArchive(Map source, Map target) {
	Iterator entries = source.entrySet().iterator();
	Map old = new LinearMap();
	while(entries.hasNext()) {
	    Map.Entry entry = (Map.Entry) entries.next();
	    old.put(entry.getKey(), 
		    target.put(entry.getKey(), 
			       entry.getValue()));
	    
	}
	return old;
    }

    private void assignFutureInstrs(Iterator futureInstrs,
				    Temp ref, List regs) {
	while(futureInstrs.hasNext()) {
	    Instr finstr = (Instr) futureInstrs.next();
	    if (finstr.useC().contains(ref)) {
		code.assignRegister(finstr,ref,regs);
	    } else if (finstr.defC().contains(ref)) {
		// redefined 'ref' (without a use) --> break and allow
		// for a different register to be assigned to it.
		break;
	    }
	}
    }


    private Map buildNextRef(BasicBlock b) {
	Map nextRef = new HashMap();

	// < should change code later to attempt to perform this O(n^2) 
	//   description in O(n) time, if possible >

	// forall(j elem instrs(b))
	//    forall(l elem pseudo-regs(b))
	//       nextRef(j, l) <- indexOf(dist to next reference to l)
	CloneableIterator instrs = new CloneableIterator(b.listIterator());
	while(instrs.hasNext()) {
	    // clone instrs *before* doing next so that
	    // for l elem refs(j), jXl -> 0
	    Iterator rInstrs = (Iterator) instrs.clone();
	    Instr j = (Instr) instrs.next();	    
	    for(int dist=0; rInstrs.hasNext(); dist++) {
		Instr j2 = (Instr) rInstrs.next();
		Iterator refs = getRefs(j2);
		while(refs.hasNext()){
		    Temp l = (Temp) refs.next();
		    if (isTempRegister(l)) continue;
		    TempInstrPair jXl = new TempInstrPair(j,l);
		    if (nextRef.get(jXl) == null) {
			nextRef.put(jXl, new Integer(dist));
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
	// FSK: Look into redesigning this to be faster and/or produce
	// more precise results.

	Map mappings = new HashMap();
	Map potentials = new HashMap();
	boolean killedRef = false;
	while (instrs.hasNext() && !killedRef) {
	    Instr i = (Instr) instrs.next();
	    Iterator refs = getRefs(i);
	    while (refs.hasNext()) {
		Temp preg = (Temp) refs.next();
		if (isTempRegister(preg)) {
		    potentials.put(preg, new RegFileInfo.PreassignTemp(preg));
		}
	    }
	    if (i.useC().contains(ref)) {
		if (false && !potentials.entrySet().isEmpty()) {
		    System.out.println
			("\n"+i+" hasRef2 " +ref+ 
			 " --> mappings.putAll: " + potentials);
		}
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
	DefaultMultiMap rMap;

	TwoWayMap() {
	    rMap = new DefaultMultiMap
		(Factories.hashSetFactory(), 
		 Factories.hashMapFactory());
	}
	
	TwoWayMap(Map map) {
	    rMap = new DefaultMultiMap
		(Factories.hashSetFactory(), 
		 Factories.hashMapFactory());
	    putAll(map);
	}

	public void clear() { super.entrySet().clear(); rMap.clear(); }

	public Set entrySet() { 
	    return Collections.unmodifiableSet(super.entrySet()); 
	}

	/** Removes all entries that have 'val' as a value in rMap. */
	private void removeMappingsTo(Object val) {
	    Util.assert(rMap != null, "rMap should not be null");
	    Util.assert(rMap.entrySet() != null, "rMap.entrySet should not be null");
	    Iterator entries = new ArrayList(rMap.entrySet()).iterator();
	    while(entries.hasNext()) {
		Map.Entry entry = (Map.Entry) entries.next();
		Util.assert(entry != null, "map entries should never be null");
		if ( (entry.getValue()==null && val == null) ||
		     entry.getValue().equals(val)) {
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
	public boolean equals(Object o) {
	    try {
		WeightedSet ws = (WeightedSet) o;
		return (this.s.equals(ws.s) &&
		    this.weight == ws.weight);
	    } catch (ClassCastException e) {
		return false;
	    }
	}
    }

}
