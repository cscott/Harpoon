// LocalCffRegAlloc.java, created Sat Dec 11 15:20:45 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Backend.Generic.Code;
import harpoon.Backend.Generic.RegFileInfo;
import harpoon.Backend.Generic.RegFileInfo.SpillException;
import harpoon.Analysis.Maps.Derivation;
import harpoon.Backend.Maps.BackendDerivation;
import harpoon.Analysis.BasicBlock;
import harpoon.Analysis.DataFlow.LiveTemps;
import harpoon.Analysis.ReachingDefs;
import harpoon.Analysis.ReachingDefsImpl;
import harpoon.Analysis.ReachingDefsCachingImpl;
import harpoon.Analysis.ReachingDefsAltImpl;
import harpoon.Analysis.Instr.TempInstrPair;
import harpoon.Analysis.Instr.RegAlloc.SpillLoad;
import harpoon.Analysis.Instr.RegAlloc.SpillStore;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMOVE;
import harpoon.IR.Assem.InstrEdge;
import harpoon.IR.Assem.InstrMEM;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;
import harpoon.Temp.TempMap;
import harpoon.Temp.Label;

import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeElement;

import harpoon.Util.CombineIterator;
import harpoon.Util.Util;
import harpoon.Util.FilterIterator;

import harpoon.Util.Collections.LinearSet;
import harpoon.Util.Collections.Factories;

import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.SetFactory;
import harpoon.Util.Collections.BitSetFactory;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;


/**
 * <code>LocalCffRegAlloc</code> performs <A
 *  HREF="http://lm.lcs.mit.edu/~pnkfelix/papers/hardnessLRA.ps">
 *  Local Register Allocation</A> for a given set of
 *  <code>Instr</code>s using a conservative-furthest-first algorithm.
 *  The papers <A 
 *  HREF="http://ctf.lcs.mit.edu/~pnkfelix/papers/OnLocalRegAlloc.ps.gz">
 *  "On Local Register Allocation"</A> and <A
 *  HREF="http://ctf.lcs.mit.edu/~pnkfelix/papers/hardnessLRA.ps">"Hardness and
 *  Algorithms for Local Register Allocation"</A> lay out the basis
 *  for the algorithm it uses to allocate and assign registers.
 *
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LocalCffRegAlloc.java,v 1.4 2002-04-10 02:59:47 cananian Exp $
 */
public class LocalCffRegAlloc extends RegAlloc {
    
    public static RegAlloc.Factory FACTORY =
	new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		return new LocalCffRegAlloc(c);
	    }
	};

    static RegAlloc.Factory RD_DISABLED_FACTORY = 
	new RegAlloc.Factory() {
	    public RegAlloc makeRegAlloc(Code c) {
		return new LocalCffRegAlloc(c, false);
	    }
	};

    private static TempFactory preassignTF = new TempFactory() {
	public String getScope() { 
	    return "private TF for RegFileInfo"; 
	}
	public String getUniqueID(String suggestion) { 
	    return "rfi"+suggestion.hashCode(); 
	}
    };

    /** <code>PreassignTemp</code> represents <code>Temp</code>s
     *  which have been pre-assigned to machine registers. */
    public static class PreassignTemp extends Temp {
	private Temp reg;
	public PreassignTemp(Temp reg) {
	    super(preassignTF);
	    this.reg = reg;
	}
	public String toString() {
	    return reg+"<preassigned>";
	}
    }
    


    private static final boolean VERIFY = false;

    private static final boolean PREASSIGN_INFO = false;
    private static final boolean SPILL_INFO = false;
    private static final boolean COALESCE_MOVES = true;

    /** ASSERTDB => assertions should emit extra info when available. */
    private static final boolean ASSERTDB = false;

    Collection genRegC;
    Collection allRegC;
    Collection allRegisters;

    Collection preassignedTemps = new HashSet();

    private LiveTemps liveTemps;

    // maps Temp:c -> Temp:o where `c' was coalesced and references to
    // `c' have been replaced with references to `o'
    // maps Instr:i -> (Temp:c -> Temp:o) where `c' was coalesced in
    // the BasicBlock containing `i' and references to `c' in the
    // instructions of that BasicBlock have been replaced with
    // references to `o'  
    private Map instrToHTempMap;

    // Used for supporting Derivation information
    private ReachingDefs reachingDefs;

    // maps Temp:t -> Instr:i where `i' is removed and defined `t'
    private Map tempToRemovedInstrs;

    /** Creates a <code>LocalCffRegAlloc</code>. */
    public LocalCffRegAlloc(Code code) {
	this(code, true);
    }
    private LocalCffRegAlloc(Code code, boolean enableRD) {
        super(code);
	allRegC = frame.getRegFileInfo().getAllRegistersC();
	genRegC = frame.getRegFileInfo().getGeneralRegistersC();
	allRegisters = allRegC;
	instrToHTempMap = new HashMap();
	tempToRemovedInstrs = new HashMap();
	if (TIME) System.out.print("D");
	// reachingDefs = new ReachingDefsCachingImpl(code);
	if (enableRD) 
	    reachingDefs = new ReachingDefsAltImpl(code);
    }

    private Instr definition(Instr i, Temp t) {
	if (i.defC().contains(t) 
	    || reachingDefs == null // FSK: yuck yuck yuck
	    ) { 
	    // System.out.print(" d");
	    return i;
	} else {
	    Set defset = reachingDefs.reachingDefs(i, t);
	    Iterator defs = defset.iterator();
	    return (Instr) defs.next();
	}
    }
    
    protected Derivation getDerivation() {
	final Derivation oldD = code.getDerivation();
	return new BackendDerivation() {
	    private HCodeElement orig(HCodeElement h){
		return getBack((Instr)h);
	    }
	    private Temp orig(HCodeElement h, Temp t) {
		HTempMap coalescedTemps = (HTempMap) instrToHTempMap.get(h);
		assert coalescedTemps != null : (ASSERTDB ? "no mapping for "+h : "no mapping");
		return coalescedTemps.tempMap(t);
	    }
	    public HClass typeMap(HCodeElement hce, Temp t) {
		HCodeElement hce2 = orig(hce); 
		Temp t2 = orig(hce, t);
		return oldD.typeMap(hce2, t2);
	    }
	    public Derivation.DList derivation(HCodeElement hce, Temp t) {
		HTempMap coalescedTemps = (HTempMap)instrToHTempMap.get(hce);
		assert coalescedTemps != null : (ASSERTDB ? "no mapping for "+hce : "no mapping");
		HCodeElement hce2 = orig(hce); 
		Temp t2 = orig(hce, t);
		return Derivation.DList.rename
		    (oldD.derivation(hce2, t2), coalescedTemps);
	    }
	    public BackendDerivation.Register
		calleeSaveRegister(HCodeElement hce, Temp t) { 
		hce = orig(hce); t = orig(hce, t);
		return ((BackendDerivation)oldD).calleeSaveRegister(hce, t);
	    }
	};
    }

    final Collection instrsToRemove = new java.util.LinkedList();
    // final Collection instrsToRemove = new java.util.HashSet();

    // alternating sequence of an Instr followed by its replacement
    // (due to temp remapping...) eg [ i0 r0 i1 r1 ... iN rN ]
    final List instrsToReplace = new java.util.LinkedList();
    
    // When writing spillCode insertion routines, be sure to check
    // that the method of insertion is correct given the control flow
    // for that Instr...

    // this List is an alternating sequence of a Load and the Instr
    // that the Load is to occur BEFORE; eg. [ l0 i0 l1 i1 .. lN iN ]
    final List spillLoads = new java.util.LinkedList();

    // this List is an alternating sequence of a Store and the Instr
    // that the Store is to occur AFTER; eg. [ s0 i0 s1 i1 .. sN iN ]
    final List spillStores = new java.util.LinkedList();
    

    protected void generateRegAssignment() {
	// this needs to be first, before we insert new Instrs that
	// won't be in the block-set proper.
	fixupUnreachableCode();

	if (TIME) System.out.print("L");
	liveVariableAnalysis();
	// if (TIME) System.out.print("A");
	allocationAnalysis();
	// analysis complete; now update Instrs (which will break
	// current BasicBlock analysis results...

	if (TIME) System.out.print("S");
	insertSpillCode();

	if (TIME) System.out.print("M");
	replaceInstrs();
	if (COALESCE_MOVES) coalesceMoves();

	if (VERIFY) verifyLRA();

        // code.printNoAssem(new java.io.PrintWriter(System.out));
    }

    private void fixupUnreachableCode() {
	for(Iterator is=code.getElementsI(); is.hasNext();){ 
 	    Instr i = (Instr) is.next();
 	    if (bbFact.getBlock(i) == null) {
 		for(Iterator refs=getRefs(i); refs.hasNext();){
 		    Temp r = (Temp) refs.next();
 		    // use any old assignment, since this code MUST
 		    // be unreachable anyway.
 		    assert !code.registerAssigned(i, r);
		    code.assignRegister
 			(i, r, (List)
 			 frame.getRegFileInfo().
 			 getRegAssignments(r).iterator().next());
 		}
 		checked.add(i);
 	    }
 	}
    }

    /** includes both pseudo-regs and machine-regs for now. */
    private static Iterator getRefs(final Instr i) {
	return new CombineIterator(i.useC().iterator(), 
				   i.defC().iterator());
    }

    private void liveVariableAnalysis() {
	liveTemps = 
	    new LiveTemps(bbFact, frame.getRegFileInfo().liveOnExit());
	harpoon.Analysis.DataFlow.Solver.worklistSolve

	    // (bbFact.preorderBlocksIter(),
	    (new harpoon.Util.ReverseIterator(bbFact.postorderBlocksIter()),
	    
	    liveTemps);
    }

    // runs once / code
    private void allocationAnalysis() { 
	if (!TIME) { 
	    // old way (more space efficient, but harder to profile)
	    Iterator blocks = bbFact.blockSet().iterator();
	    while(blocks.hasNext()) {
		BasicBlock block = (BasicBlock) blocks.next();
		Set liveOnExit = liveTemps.getLiveOnExit(block);
		BlockAlloc la = new BlockAlloc(block, liveOnExit);
		la.emptyRegFile(la.alloc());
	    }
	} else { // For all below, TIME == true
	    // temporary method for profiling purposes
	    System.out.print("B"); 
	    Set blockSet = bbFact.blockSet();
	    Iterator blocks = blockSet.iterator();
	    List blockAllocList = new ArrayList(blockSet.size());
	    Instr[] lastInstrs = new Instr[blockSet.size()];
	    while(blocks.hasNext()) {
		BasicBlock block = (BasicBlock) blocks.next();
		Set liveOnExit = liveTemps.getLiveOnExit(block);
		BlockAlloc la = new BlockAlloc(block, liveOnExit);
		blockAllocList.add(la);
	    }

	    Iterator blockAllocs;

	    System.out.print("N"); 
	    blockAllocs = blockAllocList.iterator();
	    while(blockAllocs.hasNext()) {
		BlockAlloc la = (BlockAlloc) blockAllocs.next();
		la.buildNextRef();
	    }

	    System.out.print("P"); 
	    blockAllocs = blockAllocList.iterator();
	    while(blockAllocs.hasNext()) {
		BlockAlloc la = (BlockAlloc) blockAllocs.next();
		la.buildPreassignMap();
	    }

	    System.out.print("T"); 
	    blockAllocs = blockAllocList.iterator();
	    while(blockAllocs.hasNext()) {
		BlockAlloc la = (BlockAlloc) blockAllocs.next();
		la.makeTempSets();
		la.buildTempSets();
	    }

	    System.out.print("A");
	    blockAllocs = blockAllocList.iterator();
	    for(int i=0; blockAllocs.hasNext(); i++) {
		BlockAlloc la = (BlockAlloc) blockAllocs.next();
		lastInstrs[i] = la.alloc();
	    }

	    System.out.print("E");
	    blockAllocs = blockAllocList.iterator();
	    for(int i=0; blockAllocs.hasNext(); i++) {
		BlockAlloc la = (BlockAlloc) blockAllocs.next();
		la.emptyRegFile(lastInstrs[i]);
	    }
	}
    }

    private void insertSpillCode() {
	// done doing analysis on blocks, now update with spill code. 
	// (Remember to verify control flow properties)
	Iterator loads = spillLoads.iterator();
	while(loads.hasNext()) {
	    Instr load = (Instr) loads.next();
	    Instr loc = (Instr) loads.next();
	    assert loc.getPrev() != null : (ASSERTDB ? "verify control flow prop 1. "+loc:"cfp1");
	    assert loc.getPrev().getTargets().isEmpty() : (ASSERTDB ? "verify control flow prop 2. "+loc:"cfp2");
	    assert loc.getPrev().canFallThrough : (ASSERTDB ? "verify control flow prop 3. "+loc:"cfp3");
	    load.insertAt(new InstrEdge(loc.getPrev(), loc));
	}

	Iterator stores = spillStores.iterator();
	while(stores.hasNext()) {
	    Instr store = (Instr) stores.next();
	    Instr loc = (Instr) stores.next();
	    assert loc.getTargets().isEmpty() : "overconservative assertion (targets may not be empty)";
	    assert loc.canFallThrough : "overconservative assertion (loc need not fall through)";

	    assert loc.getNext() != null : (ASSERTDB ? "verify control flow prop 4. "+loc:"cfp4");
	    InstrEdge e = new InstrEdge(loc, loc.getNext());
	    store.insertAt(e);
	}

    }

    private void coalesceMoves() {
	Iterator remove = instrsToRemove.iterator();
	while(remove.hasNext()) {
	    Instr ir = (Instr) remove.next();

	    // FSK: can't handle directly remove this case: "t4 <- r0"
	    // followed by a use of t4, because the system thinks that
	    // t4 is undefined.  (This is instead handled by a
	    // replacement of the Instr with an InstrMOVEproxy)
	    
	    assert !hasRegister(expand(ir.use()[0]));
	    assert !hasRegister(expand(ir.def()[0]));
	    
	    ir.remove();
	    
	}
    }

    private void replaceInstrs() {
	Iterator replace = instrsToReplace.iterator();
	while(replace.hasNext()) {
	    Instr pre = (Instr) replace.next();
	    Instr post = (Instr) replace.next();
	    Instr.replace(pre, post);
	}
   }

    private void verifyLRA() {
	if (TIME) System.out.print("V");
	
	// after LRA is completed, load and store instructions may
	// have been inserted at block beginings/endings.
	// Therefore we need to redo BB computation.
	computeBasicBlocks();
	liveVariableAnalysis();

	Iterator blocks = bbFact.blockSet().iterator();

	Set spillUses = new HashSet();
	Set spillDefs = new HashSet();
	while(blocks.hasNext()) {
	    BasicBlock b = (BasicBlock) blocks.next();
	    Set liveOnExit = liveTemps.getLiveOnExit(b);
	    // System.out.println();
	    verify(b, liveOnExit, spillUses, spillDefs);
	}	
	
	// code.print(new java.io.PrintWriter(System.out));
	
	final Set vUses = new HashSet(spillUses);
	Set vDefs = new HashSet(spillDefs);
	vUses.removeAll(spillDefs);
	vDefs.removeAll(spillUses);
	
	assert vUses.isEmpty() : "SpillLoad of undefined Temps";
	

	assert vDefs.isEmpty() : "(overconservative) SpillStore of unused Temp";
    }
    
    boolean hasRegs(Instr i, Temp t) {
	return (isRegister(t) || code.registerAssigned(i, t));
    }

    boolean hasRegs(Instr i, Collection c) {
	Iterator iter = c.iterator();
	boolean b = true;
	while(iter.hasNext()) {
	    Temp t = (Temp) iter.next();
	    b = b && hasRegs(i, t);
	}
	return b;
    }

    Collection getRegs(Instr i, Temp t) {
	if (isRegister(t)) {
	    return Collections.singleton(t);
	} else if (code.registerAssigned(i,t)) {
	    return code.getRegisters(i, t);
	} else {
	    return null;
	}
    }
    
    private void verify(final BasicBlock block, 
			final Set liveOnExit,
			final Set spillUses,
			final Set spillDefs) {
	// includes SpillLoads and SpillStores...
	Iterator instrs = block.statements().iterator();
	Verify verify = new Verify(this, block, spillUses, spillDefs);
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    i.accept(verify);
	    assert instrToHTempMap.keySet().contains(i) : (ASSERTDB ? "missing instr:"+i:"missing instr");
	}
    }

    /** Updates `tempSets' with instructions from `b'.
	modifies: `tempSets'
	effects: incorporates move instructions in `b' into the
	         equivalency classes maintained in `tempSets'
		 The equality operation used for the construction is 
	   eq(t1, t2) = 
	         exists InstrMOVE: "t1 <- t2" in `b' 
	      && NOT (t1 is register && t2 is register)
    */
    void buildTempSets(final EqTempSets tempSets, BasicBlock b) {
	class ScanForMove extends harpoon.IR.Assem.InstrVisitor {
	    public void visit(Instr i) { /* do nothing */ }

	    public void visit(InstrMOVE i) {
		List dl = expand(i.def()[0]);
		List ul = expand(i.use()[0]);

		assert dl.size() == ul.size();
		Iterator ds = dl.iterator();
		Iterator us = ul.iterator();

		while(ds.hasNext()) {
		    Temp d = (Temp) ds.next();
		    Temp u = (Temp) us.next();
		
		    if ((! (isRegister(d) && isRegister(u))) &&
			! d.equals(u)) {
			tempSets.add(d, u);
		    }
		}
	    }
	}
	
	ScanForMove visit = new ScanForMove();
	
	List instrL = b.statements();
	Iterator instrs = instrL.iterator();
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    i.accept(visit);
	}
    }


    // can add 1 to weight later, so store MAX_VALUE - 1 at most. 
    static Integer INFINITY = new Integer( Integer.MAX_VALUE - 1 );

    class BlockAlloc {
	
	final HTempMap coalescedTemps = new HTempMap();
	final BasicBlock block;
	final Set liveOnExit;
	final RegFile regfile = new RegFile(allRegC);

	// EqTempSets for this.block
	EqTempSets tempSets;
	
	// Temp:t currently in regfile -> Index of next ref to t
	final Map evictables;
	
	// (Instr:i x Temp:t) -> Int:2 * index of next Instr w/ ref(t)
	// [only defined for t's that have a future reference; else null]
	Map nextRef;

	// Instr currently being alloc'd
	Instr curr;

	// maps Temp:t -> Set of Regs whose live regions interfere 
	//                with t's live region 
	MultiMap preassignMap;

	// maps Temp:s -> Temp:t where s := u) has been selected for
	// removal and `u' == `t' or remappedTemps.get(`u') == `t' and
	// thus all uses of `s' must be replaced by uses of `t'
	final HTempMap remappedTemps = new HTempMap();

	BlockAlloc(BasicBlock b, Set lvOnExit) {
	    block = b;
	    liveOnExit = lvOnExit;
	    evictables = new HashMap(b.statements().size());
	    if (!TIME) {
		buildNextRef();
		buildPreassignMap();
		makeTempSets();
		buildTempSets();
	    }
	}

	void buildNextRef() {
	    nextRef = buildNextRef(block);
	}
	
	void buildPreassignMap() {
	    preassignMap = buildPreassignMap(block, allRegC, liveOnExit);
	}

	void makeTempSets() {
	    tempSets = EqTempSets.make(LocalCffRegAlloc.this, false);
	}
	
	void buildTempSets() {
	    LocalCffRegAlloc.this.buildTempSets(tempSets, block);
	}

	private Map buildNextRef(BasicBlock b) {
	    // Temp:t -> the last Instr referencing t
	    Map tempToLastRef = new HashMap(b.statements().size());
	    
	    // (Temp:t, Instr:j) -> Int:index of Instr following j w/ ref(t)
	    Map nextRef = new HashMap(b.statements().size());
	    
	    Iterator instrs = b.statements().iterator();
	    for(int c=0; instrs.hasNext(); c++) {
		Instr instr = (Instr) instrs.next();
		Iterator refs = getRefs(instr);
		while(refs.hasNext()) {
		    Temp ref = (Temp) refs.next();
		    Instr last = (Instr) tempToLastRef.get(ref);
		    if (last != null) {
			assert (2*c) <= (Integer.MAX_VALUE - 1) : "IntOverflow;change numeric rep in LRA";
			nextRef.put(new TempInstrPair(last, ref), 
				    new Integer(2*c));
		    }
		    tempToLastRef.put(ref, instr);
		}
	    }

	    Iterator entries = tempToLastRef.entrySet().iterator();
	    while(entries.hasNext()) {
		Map.Entry entry = (Map.Entry) entries.next();
		
		// enforcing the storage of NULL in nextRef only 
		// for dead Temps, so that they can be distinquished.
		if (liveOnExit.contains(entry.getKey()))
		    nextRef.put(new TempInstrPair((Temp)entry.getKey(),
						  (Instr)entry.getValue()),
				INFINITY);
	    }
	    return nextRef;
	}

	/** Generates a mapping from each temp in <code>block</code>
	    to the set of preassigned registers conflicting with that
	    temp. 
	    
	    @param block      basic block to build the map from
	    @param regs       universe of register Temps that may be 
	                      preassigned
	    @param liveOnExit set of Temps (including register Temps)
	                      that are live on exit from <code>block</code> 
	*/
	MultiMap buildPreassignMap(BasicBlock block, 
				   Collection regs, Set liveOnExit) {
	    // FSK: there are a lot of hacks strewn throughout this
	    // method, but in the end we're probably STILL doing too
	    // much redundant computation.  look into fixing either
	    // preassignMap's construction (here) or changing the code
	    // that uses it...

	    List bl = block.statements();
	    // SetFactory regSetFact = 
	    //		new BitSetFactory(new LinearSet(new HashSet(regs)));
	    BitSetFactory regSetFact = 
		new BitSetFactory((new HashSet(regs)));
	    
	    MultiMap tempToRegs = new GenericMultiMap(regSetFact);
	    
	    HashSet liveTempSet = new HashSet(liveOnExit);
	    liveTempSet.removeAll(allRegC);
	    
	    Set liveRegsOnExit = new HashSet(liveOnExit);
	    liveRegsOnExit.retainAll(regs);

	    Set liveRegs = regSetFact.makeSet(liveRegsOnExit);

	    updateMapping(tempToRegs, liveTempSet, liveRegs, 
			  harpoon.Util.Default.EMPTY_MAP);

	    // doing a reverse iteration
	    ListIterator liter = bl.listIterator(bl.size());
	    while(liter.hasPrevious()) {
		Instr i = (Instr) liter.previous();

		if (PREASSIGN_INFO) 
		    System.out.println("Instr:"+i+" ");

		boolean regchanged = false;
		boolean tmpchanged = false;
		
		// consider making `exceptions' a MultiMap...
		HashMap exceptions = new HashMap(4);
		if (i instanceof InstrMOVE) {
		    List dl = expand(i.def()[0]);
		    List ul = expand(i.use()[0]);
		    
		    assert dl.size() == ul.size();
		    Iterator ds = dl.iterator();
		    Iterator us = ul.iterator();
		    
		    while (ds.hasNext()) {
			Temp d = (Temp) ds.next();
			Temp u = (Temp) us.next();
			if (isRegister(d)) {
			    exceptions.put(u, d);
			} else if (isRegister(u)) {
			    exceptions.put(d, u);
			}
		    }
		}
		
		// make new copy of live
		liveRegs = regSetFact.makeSet(liveRegs); 

		{   // liveRegs: kill (regs /\ defs)
		    Set defRegs = regSetFact.makeFullSet();
		    defRegs.retainAll(i.defC()); 
		    liveRegs.removeAll(defRegs);
		    
		    // some regs are clobbered by routines, and thus
		    // appear in a defset but are never used. We
		    // consider these to be impulse signals, and must
		    // be added to the set of conflicts accordingly
		    if (!defRegs.isEmpty()) {
			updateMapping(tempToRegs,liveTempSet,
				      defRegs,exceptions);
		    }
		}

		liveTempSet.removeAll(i.defC()); // kill defs

		Set usesNoRegs = new HashSet(i.useC());
		usesNoRegs.removeAll(allRegC);
		tmpchanged |= liveTempSet.addAll(usesNoRegs); // add uses

		{   // liveRegs: add (regs /\ uses)
		    Set useRegs = regSetFact.makeFullSet();
		    useRegs.retainAll(i.useC());
		    regchanged |= liveRegs.addAll(useRegs);
		}

		// FSK: optimization to filter debug output (but
		// should result in faster execution times...).  Need
		// to VERIFY CORRECTNESS though
		if ( regchanged ) {
		    updateMapping(tempToRegs,liveTempSet,
				  liveRegs,exceptions);
		} else if ( tmpchanged ) {
		    // hack: if only temps changed, can make iteration
		    // loop significantly smaller by only traversing
		    // differential
		    updateMapping(tempToRegs,i.useC(),
				  liveRegs,exceptions);
		}
	    }

	    return tempToRegs;
	}

	/** adds `liveRegs' to all the conflicting regs 
	    for `liveTempC', excluding the mappings in `excepts'.
	    requires: excepts is a Temp -> Temp map
	    effects:
	    for each t in `liveTempC'
	        let regs = if (t in `excepts.keySet')
		           then `liveRegs' - `excepts.get(t)'
			   else `liveRegs'
		in adds regs to `tempToRegs'.get(t)
	*/
	private void updateMapping(MultiMap tempToRegs, 
				   Collection liveTempC, 
				   Set liveRegs, Map excepts) {
	    if (liveRegs.isEmpty()) return;
	    if (liveTempC.isEmpty()) return;

	    if(false)System.out.println("\nadding "+liveRegs+" to conflicts for "+
	                        liveTempC+" excluding "+excepts);

	    Iterator titer = liveTempC.iterator();
	    while(titer.hasNext()) {
		Temp t = (Temp) titer.next();
		if (isRegister(t)) continue;

		if (excepts.containsKey(t)) {
		    Temp r = (Temp) excepts.get(t);
		    liveRegs.remove(r);
		    tempToRegs.addAll(t, liveRegs);
		    liveRegs.add(r);
		} else {
		    tempToRegs.addAll(t, liveRegs);
		}
	    }
	}

	// runs once / block
	Instr alloc() {
	    Iterator instrs = block.statements().iterator();
	    InstrAlloc allocV = new InstrAlloc();
	    while(instrs.hasNext()) {
		allocV.iloc = curr = (Instr) instrs.next();

		// Temp remapping code for `curr'
		if (instrHasRemappedTemps(curr)) {
		    Instr oldi = curr;
		    Instr newi = curr.rename(remappedTemps);
		    instrsToReplace.add(oldi);
		    instrsToReplace.add(newi);
		    back(newi, oldi);

		    curr = newi;
		    allocV.iloc = oldi;
		}

		instrToHTempMap.put(curr, coalescedTemps);
		curr.accept(allocV);
		// System.out.println(curr + " ("+allocV.iloc+")");
		if (false && TIME) System.out.print(".");
	    }

	    return allocV.iloc;
	}
	
	private boolean instrHasRemappedTemps(Instr i) {
	    Collection uses = new HashSet(i.useC());
	    Collection defs = new HashSet(i.defC());
	    uses.retainAll(remappedTemps.keySet());
	    defs.retainAll(remappedTemps.keySet());
	    return (!uses.isEmpty() || !defs.isEmpty());
	}

	class InstrAlloc extends harpoon.IR.Assem.InstrVisitor {
	    
	    // planned target location for visited Instr.  
	    Instr iloc;
	    // FSK: Totally ruins the cleanness of the visitor
	    // abstraction; this code should be rewritten using a
	    // different framework since there doesn't seem to be a
	    // clean way to incorporate these two (potentially)
	    // independent pieces of the state.


	    // filters out hardcoded refs to machine registers 
	    class MRegFilter extends FilterIterator.Filter {
		public boolean isElement(Object o) {
		    return !isRegister((Temp) o);
		}
	    }

	    // runs once / instr
	    /** modifies: `regfile', `evictables'
	     */
	    public void visit(final Instr i) {
		Map putBackLater = takeUsesOutOfEvictables(i);
		
		Iterator refs = 
		    new FilterIterator(getRefs(i), new MRegFilter()); 

		while(refs.hasNext()) {
		    Temp t = (Temp) refs.next();
		    assign(t, i, putBackLater);
		}
		
		Iterator defs = i.defC().iterator();
		while(defs.hasNext()) {
		    Temp def = (Temp) defs.next();
		    if (!isRegister(def)) {
			regfile.writeTo(def);
			// System.out.println("Dirtifying "+def+" by "+i);
		    } else /* def is a register */ {
			Temp t = regfile.getTemp(def);
			if (t != null) {
			    if (liveOnExit.contains(t)) {
				System.out.println("\tWTF?!? removing " + 
						   t + " from " + regfile
						   + " for " + i + ", "+
						   "preassigned to "+
						   preassignMap.get(t)+
						   " i defs "+new HashSet(i.defC()));
				Instr prev = iloc.getPrev();

				// FSK: update code to do something
				// smarter (and more general)
				assert prev.canFallThrough &&
					    prev.getTargets().isEmpty() &&
					    i.predC().size() == 1 : "i.getPrev is bad choice;";

				spillValue(t, prev, regfile, 5);

				// extract extra information 
				while(!prev.defC().contains(t) &&
				      prev.predC().size() == 1)
				    prev = prev.getPrev();
				if (prev.defC().contains(t))
				    System.out.println(t+" defined by "+prev);
				else
				    System.out.println(t+" not def'd in BB");
			    }
			    regfile.remove(t);
			}
		    }
		}
		
		evictables.putAll(putBackLater);

		assert hasRegs(i, i.useC()) : (ASSERTDB ? 
			    lazyInfo("uses missing reg assignment",i,null)
			    : "uses missing reg assignment");
		assert hasRegs(i, i.defC()) : (ASSERTDB ?
			    lazyInfo("defs missing reg assignment",i,null)
			    : "defs missing reg assingment");
		
		checked.add(i);
	    }

	    // runs once / instr
	    /** Removes uses of i from `evictables'.
		modifies: `evictables'
		effects: Takes any temp used by `i' out of the keyset
		         for `evictables', returning a map holding the
			 removed mappings (for later reinsertion into
			 `evictables').  This way temps used by `i'
			 will not be considered candidates for
			 eviction from the register file. 
	    */ 
	    private Map takeUsesOutOfEvictables(Instr i) {
		Map putBackLater = new HashMap(i.useC().size());
		Iterator uses = new FilterIterator(i.useC().iterator(),
						   new MRegFilter());
		while(uses.hasNext()) {
		    Temp use = (Temp) uses.next();
		    if (evictables.containsKey(use)) {
			putBackLater.put(use, evictables.get(use));
			evictables.remove(use);
		    }
		}
		return putBackLater;
	    }
	    
	    // runs once / ref / instr
	    /** Assigns `t' in `i'.
		modifies: `code', `evictables', `regfile', `putBackLater'
		effects: 
	           1. if (`t' unassigned) then finds reg assignment
		      for `t' (potentially spilling values in
		      `evictables' to make room for `t')
		   2. Updates structures to reflect assignment for `t'
		   3. if (`t' used by `i' but not in regfile) then adds LOAD `t'
		   4. if (`t' used by `i') then puts `t' into
		      `putBackLater' else puts `t' into `evictables'
	    */
	    private void assign(Temp t, Instr i, Map putBackLater) {
		if (remappedTemps.keySet().contains(t)) {
		    t = (Temp) remappedTemps.get(t);
		}

		if (regfile.hasAssignment(t)) {
		    
		    // System.out.print(" P");
		    code.assignRegister(i, t, regfile.getAssignment(t));
		    evictables.remove(t); // (`t' reinserted later)
		    
		} else { /* not already assigned */ 
		    
		    Set preassigns = addPreassignments(t);
		    Iterator suggs = 
			getSuggestions(t, regfile, i, evictables, iloc);
		    

		    List regList = chooseSuggestion(suggs, t); 

		    if (i.useC().contains(t)) {
			InstrMEM load = 
			    SpillLoad.makeLD(i, "FSK-LD", regList, t);
			// System.out.println("adding "+load+" to "+regfile);
			spillLoads.add(load);
			spillLoads.add(iloc);
			back(load, i);
			instrToHTempMap.put(load, coalescedTemps);
		    }

		    code.assignRegister(i, t, regList);

		    regfile.assign(t, regList, definition(getBack(i),t));
			
		    
		    // remove preassignments
		    Iterator preassignIter = preassigns.iterator();
		    while(preassignIter.hasNext()) {
			regfile.remove((Temp) preassignIter.next());
		    }

		}
		
		// at this point, 't' has an assignment and needs
		// to be entered into the 'evictables' pool
		Integer X = findWeight(t, i);
		    
		// don't accidentally make `t' an eviction candidate
		if (i.useC().contains(t)) {
		    putBackLater.put(t, X);
		} else { 
		    evictables.put(t, X); 
		}
	    }
	    
	    // runs once / ref / instr
	    /** Adds conflicting preassigned registers to `regfile'.
		modifies: `regfile'
		effects: 
		  let regs = values mapped to by `t' in `preassignMap'
		      tmps = new empty set of Temps 
		  in for each r in regs 
		         if r is empty in `regfile' 
			 then let p = a new PreassignTemp for r
			      in assigns p to r
			         adds p to tmps
                     returns tmps
	    */
	    private Set addPreassignments(final Temp t) {
		Collection preassigns = preassignMap.getValues(t);
		Set preassignTempSet = new HashSet(preassigns.size());
		
		assert preassigns != null : "preassignMap is missing mappings";
		Iterator preassignIter = preassigns.iterator();
		while(preassignIter.hasNext()) {
		    final Temp reg = (Temp) preassignIter.next();
		    
		    if (regfile.isEmpty(reg)) { 
			Temp preassign = new PreassignTemp(reg);
			preassignedTemps.add(preassign);

			// preassigntemps should never be spilled, so
			// their sources should never be accessed, so
			// 'null' should be safe to add...
			regfile.assign( preassign, list(reg) , null);
			preassignTempSet.add(preassign);
		    }
		}
		
		return preassignTempSet;
	    }
	    
	    /** Finds the weight of `t' when used at `i'.  
		requires: `t' is used by `i'
		          `t' has an assignment in `regfile'
		effects: let w = 2*(distance until the next use) 
		         in if `t' is dirty 
			    then returns (w+1) 
			    else returns w
	    */
	    private Integer findWeight(Temp t, Instr i) {
		Integer X = (Integer) nextRef.get(new TempInstrPair(i, t));
		    
		// null => never referenced again; effectively infinite
		if (X == null) X = INFINITY;
		    
		assert X.intValue() <= (Integer.MAX_VALUE - 1) : "Excessive Weight was stored.";
		
		assert regfile.hasAssignment(t) : (ASSERTDB ? lazyInfo("no assignment", i, t)
			    : "no assignment");
		
		if (regfile.isDirty(t)) {
		    X = new Integer(X.intValue() + 1);
		}
		
		return X;
	    }

	    // runs once / instr	    
	    public void visit(final InstrMOVE i) {
		if (!COALESCE_MOVES) {
		    super.visit(i);
		    return;
		}

		final Temp u = i.use()[0];
		final Temp d = i.def()[0];
		Map putBackLater = takeUsesOutOfEvictables(i);

		if (!isRegister(u) &&
		    !regfile.hasAssignment(u)) {
		    // load that value into a register...
		    assign(u, i, putBackLater);
		}

		assert isRegister(u) || regfile.hasAssignment(u);
		
		int choice = 0;

	    coalesceChoice: {
		    // FSK: overconservative
		    // assert u == d || !regfile.hasAssignment(d);
		    if (regfile.hasAssignment(d) && u != d) {
			// rare (due to SSI-form) but it happens.
			// System.out.println("not coalescing "+i+" (redefinition pt)");
			break coalesceChoice;
		    }
		    
		    if (isRegister(d) &&
			regfile.getAssignment(u).get(0) == d) {
			choice = 1;
			break coalesceChoice;
		    }

		    if (isRegister(u) &&
			!preassignMap.getValues(d).contains(u)) {
			choice = 2;
			break coalesceChoice;
		    } 
		    
		    if (u == d) {
			choice = 3; 
			break coalesceChoice;
		    } 
		    
		    if (isRegister(d) || isRegister(u)) {
			break coalesceChoice;
		    } 
		
		    // u and d are not registers...
		    Collection uregs = regfile.getAssignment(u);
		    Collection conflicting = new HashSet(uregs);
		    conflicting.retainAll(preassignMap.getValues(d));
		    if (conflicting.isEmpty()) {
			choice = 4;
			break coalesceChoice;
		    }
		}
		
		if (choice == 0) {
		    visit((Instr) i);
		} else {
		    assert isRegister(u) ||
				isRegister(d) ||
				tempSets.getRep(u) == tempSets.getRep(d) : (ASSERTDB ?
				"Temps "+u+" & "+d+
				" should have same rep to be coalesced"
				: "should have same rep to be coalesced"); 
		    
		    List regList = isRegister(u)?
			list(u):
			regfile.getAssignment(u);
		    code.assignRegister(i, u, regList);
		    code.assignRegister(i, d, regList);
		    
		    if (isRegister(u) || isRegister(d)) {
			instrsToReplace.add(i);
			Instr proxy;
			if (isRegister(u)) {
			    proxy = new InstrMOVEproxy(i);
			} else {
			    proxy = new InstrMOVEproxy(i);
			}
			instrsToReplace.add(proxy);
			back(proxy, i);
			checked.add(proxy);
			instrToHTempMap.put(proxy, coalescedTemps);
			if (isRegister(u)) {
			    code.assignRegister(proxy, d, list(u));
			} else {
			    assert isRegister(d);
			    code.assignRegister(proxy, u, list(d));
			}
		    } else {
			remove(i, choice);
			coalescedTemps.put(d, coalescedTemps.get(u));
			tempToRemovedInstrs.put(d, i);
		    }

		    assert !(isRegister(u) && isRegister(d));
		    
		    if (isRegister(u)) {
			regfile.assign(d, list(u), definition(getBack(i),d));
			regfile.writeTo(d);
		    } else if (isRegister(d)) {
			if (regfile.getTemp(d) == null) {
			    // FSK: can this actually EVER work???
			    regfile.assign
				(u, list(d), definition(getBack(i),u));
			} else {
			    assert regfile.getTemp(d) == u;
			}
		    } else {
			Temp t = 
			    remappedTemps.keySet().contains(u)?
			    (Temp)remappedTemps.get(u):u;
			remappedTemps.put(d, t);
		    } 
		    // System.out.println("assigning "+i.def()[0] +
		    //				       " to "+regList);
		}
		
		assert hasRegs(i, u) : (ASSERTDB ? lazyInfo("missing reg assignment",i,u)
			    : "missing reg assignment");
		
		evictables.putAll(putBackLater);
	    }
	    
	    private void remove(Instr i, int n) {
		remove(i, n, false);
	    }
	    
	    private void remove(Instr i, int n, boolean pr) {
		instrsToRemove.add(i);
		if (pr) System.out.println("removing"+n+" "+i+" rf: "+regfile);
	    }
	}
	
	// runs once / ref / instr
	/** Gets an Iterator of suggested register assignments in
	    <code>regfile</code> for <code>t</code>.  May insert
	    load/spill code before <code>i</code>.  Uses
	    <code>evictables</code> as a <code>Map</code> from
	    <code>Temp</code>s to weighted distances to decide which
	    <code>Temp</code>s to spill, and <code>iloc</code> as the
	    proxy for the location where <code>i</code> will
	    eventually be located.
	*/
	private Iterator getSuggestions(final Temp t, final RegFile regfile, 
					final Instr i, final Map evictables,
					final Instr iloc) {
	    if (false) System.out.println("getting suggestions for "+t+
			       " in "+i+" with regfile "+regfile+
			       " and evictables "+evictables);
	    
	    for(int x=0; true; x++) {
		assert x < 10 : "shouldn't have to iterate >10";

		try {

		    Iterator assns = 
			frame.getRegFileInfo().suggestRegAssignment
			(t, regfile.getRegToTemp(), preassignedTemps);

		    // Assignment Filter: remove *used* regs
		    assns = new FilterIterator
			(assns, new FilterIterator.Filter() {
			    public boolean isElement(Object o) {
				List l = new ArrayList((List) o);
				Set conflicts = new LinearSet();
				conflicts.addAll(preassignMap.getValues(t));
				conflicts.addAll(regfile.getRegToTemp().keySet());
				if (false)
				System.out.println("assignment filter, "+
						   "conflicts: "+conflicts+
						   "suggestion: "+l);

				return !l.removeAll(conflicts);
			    }
			});
		    if (assns.hasNext()) {
			return assns;
		    } else {
			throw new SpillException() {
			    public Iterator getPotentialSpills() {
				Iterator sggs;
				try {
				    sggs = frame.getRegFileInfo().
					suggestRegAssignment
					(t, regfile.getRegToTemp(), 
					 preassignedTemps);
				} catch (SpillException e) {
				    assert false : "cant happen here";
				    sggs = null;
				}

				// Spill Filter: remove *hardcoded* regs
				sggs = new FilterIterator
				    (sggs, new FilterIterator.Filter() {
					 public boolean isElement(Object o) {
					     List l = new ArrayList((List)o);
					     Set conflicts=new LinearSet
					     ((Set)preassignMap.getValues(t));
					     return !l.removeAll(conflicts);
					 }
					 public Object map(Object o) {
					     List l = (List)o;
					     Set s = new LinearSet(l.size());
					     s.addAll(l);
					     return s;
					 }
				     });

				return sggs;
			    }
			};
		    }
		} catch (SpillException s) {

		    Iterator spills = s.getPotentialSpills();
		    SortedSet weightedSpills = new TreeSet();

		    Collection preasnRegs = preassignMap.getValues(t);

		    while(spills.hasNext()) {
			boolean valid = true;
			Set cand = (Set) spills.next();
			

			HashSet temps = new HashSet(5);
			Iterator regs = cand.iterator();
			
			int cost=Integer.MAX_VALUE;
			while(regs.hasNext()) {
			    Temp reg = (Temp) regs.next();
			    Temp preg = regfile.getTemp(reg);
			    
			    // did reg previously hold a value?
			    if (preg != null) {
				temps.add(preg);
				if (!evictables.containsKey(preg)) {
				    // --> disallowed evicting preg
				    valid = false;
				    break;
				} else if (preasnRegs.contains(reg)) {
				    valid = false;
				    break;
				}
				Integer dist = (Integer) evictables.get(preg); 
				int c = dist.intValue();
				if (c < cost) { 
				    cost = c;
				}
			    }
			}
			 
			if(valid) {
			    WeightedSet ws = new WeightedSet(cand, cost);
			    ws.temps = temps;
			    weightedSpills.add(ws);
			}

			// System.out.println("Adding "+cand+" at cost "+cost);
		    }

		    assert !weightedSpills.isEmpty() : (ASSERTDB ? 
				lazyInfo("\nneed at least one spill"
					 +"\nEvictables:"+evictables 
					 ,i,t,regfile)
				: "need at least one spill");

		    WeightedSet spill = (WeightedSet) weightedSpills.first();
		    if (SPILL_INFO) 
			System.out.print("for "+t+" in "+i
					 // + " choosing to spill "+spill
					 // + " of " + weightedSpills
					 );
		    
		    Iterator spRegs = spill.iterator();

		    while(spRegs.hasNext()) {
			
			// the set we end up spilling may be disjoint from
			// the set we were prompted to spill, because the
			// SpillException only accounts for making room
			// for Load, not in properly maintaining the state
			// of the register file
			
			Temp reg = (Temp) spRegs.next();
			Temp value = (Temp) regfile.getTemp(reg);
			
			if (value == null) {
			    // no value associated with 'reg', so we don't
			    // need to spill it; can go straight to
			    // storing stuff in it
			    if (SPILL_INFO) System.out.print(" ; "+reg+" was empty");
			    
			} else { 

			    // only bother to store if value is live *and* dirty
			    if (regfile.isDirty(value)) {
				if (liveTemps.getLiveAfter(iloc).contains(value)) {
				    spillValue(value,iloc.getPrev(),regfile, 3);
				    if (SPILL_INFO) System.out.print(" ; spilling "+value);
				} else {
				    if (SPILL_INFO) System.out.print(" ; notLive "+value+" (liveAfter:"+liveTemps.getLiveAfter(iloc)+")");
				}
			    } else {
				if (SPILL_INFO) System.out.print(" ; notDirty "+value);
			    }

			    // FSK: move-coalesced temps need to be
			    // stored on a spill regardless of
			    // cleanness of `value'
			    Collection temps = new java.util.ArrayList(remappedTemps.invert().getValues(value));
			    Iterator remapped = temps.iterator();
			    while(remapped.hasNext()) {
				Temp tt = (Temp) remapped.next();
				if (liveTemps.getLiveAfter(iloc).contains(tt)) {
				    // System.out.println("spilling "+tt+" before "+iloc+", live after:"+liveTemps.getLiveAfter(iloc));
				    Instr src;
				    if(tempToRemovedInstrs.keySet().contains(tt)) { 
					src=(Instr)tempToRemovedInstrs.get(tt);
				    } else {
					src=regfile.getSource(value);
				    }

				    spillValue(tt, iloc.getPrev(),
					       regfile, src, 2);
				}
				remappedTemps.remove(tt);
			    }
			    
			    regfile.remove(value);
			    evictables.remove(value);
			}
		    }
		    if (SPILL_INFO) System.out.println();

		}
	    }
	}

	private void emptyRegFile(Instr lastInstr) {
	    // System.out.println("live on exit from " + b + " :\n" + liveOnExit);
	    Instr instr = curr;

	    // use a new Set here because we cannot modify regfile
	    // and traverse its tempSet simultaneously.
	    Set locvalSet = new LinearSet(regfile.tempSet());
	    if (SPILL_INFO) 
		System.out.println("emptying regfile:"+regfile);
	    final Iterator locvals = locvalSet.iterator();
	    while(locvals.hasNext()) {
		final Temp locval = (Temp) locvals.next();
		
		// FSK: handle spilling move equivalent temps;
		// remember that even if the source of the move is
		// clean, the "newly defined" temps (the
		// move-equivalent virtual ones) are still "dirty" and
		// need to be stored to memory.
		Iterator vals = 
		    remappedTemps.invert().getValues(locval).iterator();
		while(vals.hasNext()) {
		    final Temp val = (Temp) vals.next();
		    
		    assert !isRegister(val) : "remapped temp should not be register";
		    
		    // don't spill dead values.
		    if (liveOnExit.contains(val)) {
			if (SPILL_INFO) 
			    System.out.println("spilling "+val+
					       " 'cause its live (0)");
			chooseSpillSpot(val, instr, regfile, 
					regfile.getSource(locval), lastInstr);
		    }
		}

		if (isRegister(locval)) {
		    // don't spill register only values

		} else if (regfile.isClean(locval)) {
		    // FSK: weirdness in SpillCodePlacement occurs if
		    // I (conservatively) leave this case out, which
		    // is a sign that something's wrong.  Experiment
		    // further... 

		    // don't spill clean values. 
		    if (SPILL_INFO) 
			System.out.println("not spilling "+locval+
					   " 'cause its clean");

		} else {
		    // FSK: check and document this
		    if (liveOnExit.contains(locval)){ 
			// don't spill dead values.
			if (SPILL_INFO) 
			    System.out.println("spilling "+locval+
					       " 'cause its live ");
			chooseSpillSpot(locval, instr, regfile, lastInstr);
		    } else {
			if (false && SPILL_INFO)
			    System.out.println("SKIPPING "+locval+
					       " 'cause its dead (3)");
		    }
		}

		regfile.remove(locval);
	    }
	}

	private void chooseSpillSpot(Temp val, Instr instr, 
				     RegFile regfile, Instr iloc) {
	    chooseSpillSpot(val, instr, regfile, 
			    regfile.getSource(val), iloc);
	}

	private void chooseSpillSpot(Temp val, Instr instr, 
				     RegFile regfile, Instr src, Instr iloc) {
	    // need to insert the spill in a place where we can be
	    // sure it will be executed; the easy case is where
	    // 'instr' does not redefine the 'val' (so we can just put 
	    // our spills BEFORE 'instr').  If 'instr' does define
	    // 'val', however, then we MUST wait to spill, and
	    // then we need to see where control can flow...
	    // insert a new block solely devoted to spilling
	    
	    final Instr prev = iloc.getPrev();
	    if (!instr.defC().contains(val) &&
		prev.getTargets().isEmpty() &&
		prev.canFallThrough) {
		
		spillValue(val, prev, regfile, src, 1);
		
	    } else {
		
		spillValue(val, iloc, regfile, src, 0);
		
	    }
	}

	
	
	// runs once / ref / instr
	private List chooseSuggestion(Iterator suggs, Temp t) {
	    return chooseSuggestion(suggs, t, false);
	}

	// runs once / ref / instr	
	private List chooseSuggestion(Iterator suggs, Temp t, boolean pr) {
	    Temp reg = tempSets.getReg(t);
	    List suggL = null;
	    if (reg != null) {
		// if [ <reg> ] is in <suggs>, ret [ <reg> ]
		// else, ret arbitrary elem suggs 
		do {
		    suggL = (List) suggs.next();

		    if (false)
			System.out.println("RFI suggests "+suggL+" for "+t);

		    if (suggL.size() == 1) {
			Temp suggReg = (Temp) suggL.get(0);
			if (suggReg.equals(reg)) {
			    if (pr) {
				System.out.println
				    (" ok_Pre " + t +" ("+
				     tempSets.getRep(t) + ") to " + reg ); 
			    }
			    return suggL;
			} 
		    }
		} while (suggs.hasNext());

		// got to this point => didn't find match for <reg> 
		if (suggL.size() == 1) {
		    // insert new association (since we've been forced to 
		    // use a different reg assignment than what we preferred)
		    reg = (Temp) suggL.get(0);
		    tempSets.associate(t, reg);
		    if (pr) 
			System.out.println
			    (" badPre " + t +" ("+
			     tempSets.getRep(t) + ") to " + reg); 
		}
		return suggL;
		
	    } else {
		suggL = (List) suggs.next();

		if (false) System.out.println("RFI suggests "+suggL+" for "+t);

		if (suggL.size() == 1) {
		    reg = (Temp) suggL.get(0);
		    tempSets.associate(t, reg);
		    if (pr) 
			System.out.println
			    (" no_Pre " + t + " ("+
			     tempSets.getRep(t) + ") to " + reg ); 

		}
		return suggL;
	    }
	}
	
	/** spills `val', adding a store after `loc', but does *NOT*
	    update the regfile. 
	*/
	private void spillValue(Temp val, Instr loc, RegFile regfile,
				int thread) {
	    spillValue(val, loc, regfile, regfile.getSource(val), thread);
	}	

	/** spills `val', adding a store after `loc', but does *NOT*
	    update the regfile, and using `src' as the source for
	    `val'.
	*/
	private void spillValue(Temp val, Instr loc, RegFile regfile,
				Instr src, int thread) {
	    assert ! preassignedTemps.contains( val ) : "cannot spill Preassigned Temps";
	    assert !isRegister(val) : (ASSERTDB ? val+" should not be reg"
			: "should not be reg");

	    Collection regs = 
		regfile.getAssignment(remappedTemps.tempMap(val));

	    assert regs != null && !regs.isEmpty() : (ASSERTDB ? 
			lazyInfo("must map to a nonempty set of registers\n"+
				 "tempSets:"+tempSets,val,regfile)
			: "must map to a nonempty set of registers");
	    assert allRegs(regs);

	    InstrMEM spillInstr = SpillStore.makeST(loc, "FSK-ST", val, regs);

	    spillStores.add(spillInstr);
	    spillStores.add(loc);
	    back(spillInstr, src);
	    instrToHTempMap.put(spillInstr, coalescedTemps);
	}

	// *** helper methods for debugging within BlockAlloc ***
	private Object lazyInfo(String prefix) {
	    return lazyInfo(prefix, curr, null);
	}

	private Object lazyInfo(String prefix, Temp t) {
	    return lazyInfo(prefix, curr, t);
	}

	private Object lazyInfo(String prefix, Temp t, RegFile regfile) {
	    return lazyInfo(prefix, curr, t, regfile);
	}

	private Object lazyInfo(String prefix, Instr i, Temp t) {
	    return LocalCffRegAlloc.this.lazyInfo(prefix, block, i, t);
	}

	private Object lazyInfo(String prefix, Instr i, Temp t,
				RegFile regfile) {
	    return LocalCffRegAlloc.this.lazyInfo(prefix, block, i, t, regfile);
	}
    }


    


    // *** DEBUGGING ROUTINES ***

    // lazyInfo(..) family of methods return an object that prints out
    // the basic block in a demand driven fashion, so that we do not
    // incur the cost of constructing the string representation of the
    // basic block until we actually will need it

    Object lazyInfo(String prefix, BasicBlock b, 
		    Instr i, Temp t) {
	return lazyInfo(prefix, b, i, t, true);
    }

    Object lazyInfo(String prefix, BasicBlock b, 
		    Instr i, Temp t, RegFile regfile) {
	return lazyInfo(prefix, b, i, t, regfile, true);
    }

    Object lazyInfo(final String prefix, final BasicBlock b, 
			    final Instr i, final Temp t, 
			    final boolean auxSpillCode) {
	return new Object() {
	    public String toString() {
		return prefix+"\n"+printInfo(b, i, t, code, auxSpillCode);
	    }
	};
    }

    Object lazyInfo(final String prefix, final BasicBlock b, 
			    final Instr i, final Temp t, 
			    final RegFile regfile,
			    final boolean auxSpillCode) {
	return new Object() {
	    public String toString() {
		return prefix+"\n"+"RegFile:"+regfile+"\n"+printInfo(b, i, t, code, auxSpillCode);
	    }
	};
    }


    private String printInfo(BasicBlock block, Instr i, 
			     Temp t, Code code) {
	return printInfo(block, i, t, code, true);
    }

    private String printInfo(BasicBlock block, Instr i, 
			     Temp t, Code code, boolean auxSpillCode) {
	java.io.StringWriter swout = new java.io.StringWriter();
	java.io.PrintWriter pwout = new java.io.PrintWriter(swout);
	// code.print(pwout);

	// StringBuffer sb = new StringBuffer(swout.toString());
	StringBuffer sb = new StringBuffer();

	// inserting the loads and stores may make this routine
	// O(n^2), which could be bad...

	List blockL = block.statements();
	Iterator itr = blockL.iterator(); 
	while(itr.hasNext()) {
	    Instr i2=(Instr)itr.next();

	    if (auxSpillCode) {
		// insert loads...
		Iterator loads = spillLoads.iterator();
		while(loads.hasNext()) {
		    Instr s = (Instr) loads.next();
		    Instr loc = (Instr) loads.next();
		    if (loc == i2) {
			sb.append(s+"\n");
		    }
		}
	    }

	    // put actual instr in
	    sb.append(code.toAssem(i2)+
		      " \t { " + i2 + 
		      " }");
	    
	    // if instr is scheduled for removal, make a note of that
	    // as well
	    if (instrsToRemove.contains(i2)) sb.append("\t* R *");
	    
	    sb.append("\n");

	    if (auxSpillCode) {
		// insert stores... 
		Iterator stores = spillStores.iterator();
		while(stores.hasNext()) {
		    Instr s = (Instr) stores.next();
		    Instr loc = (Instr) stores.next();
		    if (loc == i2) {
			sb.append(s+"\n");
		    }
		}
	    }
	}
	
	return "\n"+
	    "temp: "+t + "\n"+
	    "instr: "+ i + "\n\n"+
	    sb.toString();


    }

    public List list(Temp t) {
	return Arrays.asList( new Temp[]{ t } );
    }
    public List list(Temp t1, Temp t2) {
	return Arrays.asList( new Temp[]{ t1, t2 });
    }

}

