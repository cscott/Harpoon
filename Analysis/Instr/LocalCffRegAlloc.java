// LocalCffRegAlloc.java, created Sat Dec 11 15:20:45 1999 by pnkfelix
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
import harpoon.Analysis.Instr.RegAlloc.SpillLoad;
import harpoon.Analysis.Instr.RegAlloc.SpillStore;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMOVE;
import harpoon.IR.Assem.InstrEdge;
import harpoon.IR.Assem.InstrMEM;
import harpoon.Temp.Temp;
import harpoon.Temp.Label;

import harpoon.Util.CombineIterator;
import harpoon.Util.CloneableIterator;
import harpoon.Util.UnmodifiableIterator;
import harpoon.Util.LinearMap;
import harpoon.Util.Util;
import harpoon.Util.FilterIterator;

import harpoon.Util.Collections.LinearSet;
import harpoon.Util.Collections.Factories;
import harpoon.Util.Collections.ListFactory;
import harpoon.Util.Collections.GenericMultiMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.SetFactory;
import harpoon.Util.Collections.BitSetFactory;

import java.util.Arrays;
import java.util.List;
import java.util.AbstractSet;
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
 * @version $Id: LocalCffRegAlloc.java,v 1.1.2.81 2000-06-09 23:21:07 pnkfelix Exp $
 */
public class LocalCffRegAlloc extends RegAlloc {

    private static boolean TIME = false;
    private static boolean VERIFY = true;

    private static boolean SPILL_INFO = false;
    
    /** Creates a <code>LocalCffRegAlloc</code>. */
    public LocalCffRegAlloc(Code code) {
        super(code);
	tempSets = EqTempSets.make(this, true);
	Iterator blocks = bbFact.blockSet().iterator();

	while(blocks.hasNext()) {
	    BasicBlock b = (BasicBlock) blocks.next();
	    buildTempSets(b);
	}

	// System.out.println("EqTempSets : " + tempSets);

	tempSets.lock();
    }
    
    // final List instrsToRemove = new java.util.LinkedList();
    final Collection instrsToRemove = new java.util.HashSet();
    
    // When writing spillCode insertion routines, be sure to check
    // that the method of insertion is correct given the control flow
    // for that Instr...

    // this List is an alternating sequence of a Load and the Instr
    // that the Load is to occur BEFORE; eg. [ l0 i0 l1 i1 .. lN iN ]
    final List spillLoads = new java.util.LinkedList();

    // this List is an alternating sequence of a Store and the Instr
    // that the Store is to occur AFTER; eg. [ s0 i0 s1 i1 .. sN iN ]
    final List spillStores = new java.util.LinkedList();
    
    // each method is associated with an EqTempSets (NOT each
    // BasicBlock) 
    final EqTempSets tempSets;
    
    // FSK: FOR TEMPORARY DEBUGGING USE *ONLY*; REMOVE ASAP!
    // Maps first NON-InstrMEM elem of a BasicBlock <bb> to the
    // preassignMap for <bb>  
    final Map bbToPreassignMap = new HashMap();
    Map getPreassignFor(BasicBlock bb) {
	    return (Map) bbToPreassignMap.get(getFirstNonInstrMEM(bb));
	}
    void putPreassign(BasicBlock bb, Map preassign) {
	    bbToPreassignMap.put(getFirstNonInstrMEM(bb), preassign);
    }
    Instr getFirstNonInstrMEM(BasicBlock bb) {
	Iterator instrs = bb.statements().iterator();
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    if ( ! ( i instanceof InstrMEM )  ){
		return i;
	    } 
	}
	
	Util.assert(false, 
		    "should never get here"+
		    " (except perhaps if we"+
		    " encounter empty bb's (!!) )");
	return null;
    }
   
    protected void generateRegAssignment() {
	LiveTemps liveTemps = doLVA(bbFact);
	Iterator blocks = bbFact.blockSet().iterator();
	while(blocks.hasNext()) {
	    BasicBlock b = (BasicBlock) blocks.next();
	    Set liveOnExit = liveTemps.getLiveOnExit(b);

	    // FSK: total hack to try to make the compiler be smarter
	    // about liveness ( MAY NOT BE NEEDED!!! )
	    HashSet newLiveOnExit = new HashSet(liveOnExit);
	    Iterator temps = liveOnExit.iterator();
	    while(temps.hasNext()) {
		newLiveOnExit.add
		    (tempSets.getRep((Temp) temps.next()));
	    }

	    // System.out.println("LiveOnExit: " + newLiveOnExit);	    

	    alloc(b, newLiveOnExit);
	    if (TIME) System.out.print("#");
	}

	// done doing analysis on blocks, now update with spill code. 
	// (Remember to verify control flow properties)
	Iterator loads = spillLoads.iterator();
	while(loads.hasNext()) {
	    Instr load = (Instr) loads.next();
	    Instr loc = (Instr) loads.next();
	    Util.assert(loc.getPrev().getTargets().isEmpty(), "verify control flow prop.");
	    Util.assert(loc.getPrev().canFallThrough, "verify control flow prop.");
	    load.insertAt(new InstrEdge(loc.getPrev(), loc));
	}

	Iterator stores = spillStores.iterator();
	while(stores.hasNext()) {
	    Instr store = (Instr) stores.next();
	    Instr loc = (Instr) stores.next();
	    Util.assert(loc.getTargets().isEmpty(), 
			"overconservative assertion (targets may not be empty)");
	    Util.assert(loc.canFallThrough,
			"overconservative assertion (loc need not fall through)");
	    InstrEdge e = new InstrEdge(loc, loc.getNext());
	    store.insertAt(e);
	}
	
	if (TIME) System.out.print("V");

	if (VERIFY) {
	    // after LRA is completed, load and store instructions may
	    // have been inserted at block beginings/endings.
	    // Therefore we need to redo BB computation.
	    computeBasicBlocks();
	    liveTemps = doLVA(bbFact);
	    blocks = bbFact.blockSet().iterator();

	    Set spillUses = new HashSet();
	    Set spillDefs = new HashSet();
	    while(blocks.hasNext()) {
		BasicBlock b = (BasicBlock) blocks.next();
		Set liveOnExit = liveTemps.getLiveOnExit(b);
		// System.out.println("preassign for "+b+" : "+getPreassignFor(b));
		// System.out.println();
		verify(b, liveOnExit, spillUses, spillDefs);
	    }	


	    // code.print(new java.io.PrintWriter(System.out));

	    Set vUses = new HashSet(spillUses);
	    Set vDefs = new HashSet(spillDefs);
	    
	    vUses.removeAll(spillDefs);
	    vDefs.removeAll(spillUses);
	    
	    Util.assert(vUses.isEmpty(),
			"Spill Load of undefined Temps: "+vUses);

	    // This assertion may be overconservative, but then again,
	    // its DUMB to store temps that are never loaded...
	    Util.assert(true || vDefs.isEmpty(),
			"Spill Store of undefined Temps: "+vDefs);
	    
	}

	// InstrMOVEs not removed until AFTER verification...
	Iterator remove = instrsToRemove.iterator();
	while(remove.hasNext()) {
	    Instr ir = (Instr) remove.next();
	    ir.remove();
	}

	// code.print(new java.io.PrintWriter(System.out));
    }


    private void alloc(BasicBlock block, Set liveOnExit) {
	if (TIME) System.out.print("B"); 
	LocalAllocator la = new LocalAllocator(block, liveOnExit);
	if (TIME) System.out.print("L");
	la.alloc();
    }

    boolean hasRegs(Instr i, Temp t) {
	return (isTempRegister(t) ||
		code.registerAssigned(i, t));
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
	if (isTempRegister(t)) {
	    return Collections.singleton(t);
	} else if (code.registerAssigned(i,t)) {
	    return code.getRegisters(i, t);
	} else {
	    return null;
	}
    }
    
    /** Verify uses the inherent definitions of the instruction
	stream to check that the register uses of the allocated
	instructions are coherent.
    */
    class Verify extends harpoon.IR.Assem.InstrVisitor {
	RegFile regfileV = new RegFile();
	Set spillUsesV, spillDefsV;

	final BasicBlock block;
	private Instr curr;

	Verify(BasicBlock b, Set spillUses, Set spillDefs) {
	    this.block = b; 
	    this.spillUsesV = spillUses;
	    this.spillDefsV = spillDefs;
	}

	/** Gets the assignment for `use' out of `regfile'.  Note that
	    in some cases, `use' is not directly mapped in the
	    `regfile', but rather another temp that is in the same
	    EqTempSet as `use' is mapped in the `regfile'.
	*/
	List getAssignment(Temp use) {
	    if (isTempRegister(use)) 
		return ListFactory.singleton(use);

	    List regs = regfileV.getAssignment(use);
	    
	    if (regs == null) { // search for alternate
		Iterator temps = regfileV.tempSet().iterator();
		while(temps.hasNext()) {
		    Temp t = (Temp) temps.next();
		    if (tempSets.getRep(t) == tempSets.getRep(use)) {
			regs = regfileV.getAssignment(t);
			break;
		    }
		}
	    }
	    
	    Util.assert
		(regs != null, 
		 lazyInfo("no reg assignment",block,curr,use,code,false));
	    return regs;
	}

	public void visit(final Instr i) {
	    curr = i;
	    visit(i, false);
	}

	public void visit(final InstrMOVE i) {
	    curr = i;
	    if (instrsToRemove.contains(i)) {
		List regs = getAssignment(i.use()[0]);
		assign(i.def()[0], regs); // mult assign?
	    } else {
		visit((Instr)i);
	    }
	}
	
	public void visit(final Instr i, boolean multAssignAllowed) {
	    Iterator uses = i.useC().iterator();
	    while(uses.hasNext()) {
		Temp use = (Temp) uses.next();
		if (isTempRegister(use)) continue;

		Collection codeRegs = getRegs(i, use);

		List fileRegs = getAssignment(use);

		{ // ASSERTION CODE
		    Util.assert(codeRegs != null, "codeRegs!=null ");
		    Util.assert(!fileRegs.contains(null), "no null allowed in fileRegs");
		    Util.assert(!codeRegs.contains(null), "no null allowed in codeRegs");
		    Util.assert(codeRegs.containsAll(fileRegs),
				lazyInfo("codeRegs incorrect; "+
					 "c:"+codeRegs+" f:"+fileRegs+" regfile:"+regfileV,
					 block,i,use,code,false));
		    Util.assert(fileRegs.containsAll(codeRegs),
				"fileRegs incomplete: "+"c: "+codeRegs+" f: "+fileRegs);
		} // END ASSERTION CODE
	    }
	    
	    Iterator defs = i.defC().iterator();
	    while(defs.hasNext()) {
		final Temp def = (Temp) defs.next();
		// def = tempSets.getRep(def);
		Collection codeRegs = getRegs(i, def);
		Util.assert(codeRegs != null);
		

		if (false) {
		    // check (though really just debugging my thought process...)
		    Iterator redefs = codeRegs.iterator();
		    while(redefs.hasNext()) {
			final Temp r = (Temp) redefs.next();
			Util.assert(regfileV.isEmpty(r), 
				    lazyInfo("reg:"+r+" is not empty prior to assignment",block,i,def,code));
		    }
		}


		assign(def, codeRegs); // , multAssignAllowed);
	    }
	    
	}
	
	public void visit(final InstrMEM i) {
	    curr = i;
	    if (i instanceof SpillLoad) {
		// regs <- temp
		spillUsesV.add(i.use()[0]);

		Util.assert(!regfileV.hasAssignment(i.use()[0]), 
			    lazyInfo("if we're loading, why in regfile?",
				     block, i,i.use()[0],code,regfileV,false));
		assign(i.use()[0], i.defC());
	    } else if (i instanceof SpillStore) {
		// temp <- regs
		spillDefsV.add(i.def()[0]);

		// this code is failing for an unknown reason; i
		// would think that the mapping should still be
		// present in the regfile.  taking it out for
		// now... 
		if (false) {
		    final Temp def = i.def()[0];
		    List fileRegs = regfileV.getAssignment(def);
		    Collection storeRegs = i.useC();
		    Util.assert(fileRegs != null, 
				lazyInfo("fileRegs!=null",block,i,def,code));
		    Util.assert(!fileRegs.contains(null),
				"no null allowed in fileRegs");
		    Util.assert(storeRegs.containsAll(fileRegs),
				"storeRegs incomplete");
		    Util.assert(fileRegs.containsAll(storeRegs),
				"fileRegs incomplete");
		}
	    } else {
		visit((Instr)i);
	    }
	}

	// default assign procedure (multiple assignments not allowed)
	private void assign(Temp def, Collection c) {
	    assignP(def, c, false);
	}

	/** assigns `def' to `c'.
	    requires: `c' is a collection of register Temps
	    modifies: `regfile'
	    effects: 
	       1. (not `multAssignAllowed') => remove all temps held
	                by registers in `c' from `regfile'.
	       2. assigns `def' to the collection of registers `c' in `regfile'
	*/
	private void assignP(final Temp def, Collection c, boolean multAssignAllowed) {
	    if (!multAssignAllowed) {
		// getting around multiple-assignment checker...
		Iterator redefs = c.iterator();
		while(redefs.hasNext()) {
		    Temp r = (Temp) redefs.next();
		    Temp t = regfileV.getTemp(r);
		    if (regfileV.hasAssignment(t)) regfileV.remove(t);

		}
	    }

	    regfileV.assign(def, new ArrayList(c));
	}
    }
    
    private void verify(final BasicBlock block, 
			final Set liveOnExit,
			final Set spillUses,
			final Set spillDefs) {
	// includes SpillLoads and SpillStores...
	Iterator instrs = block.statements().iterator();
	Verify verify = new Verify(block, spillUses, spillDefs);
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    i.accept(verify);
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
    void buildTempSets(BasicBlock b) {
	class ScanForMove extends harpoon.IR.Assem.InstrVisitor {
	    public void visit(Instr i) { 
		/* do nothing */ 
	    }
	    public void visit(InstrMOVE i) {
		Temp d = i.def()[0], u = i.use()[0];
		
		if ((! (isTempRegister(d) && isTempRegister(u))) &&
		    ! d.equals(u)) {
		    tempSets.add(d, u);
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

    class LocalAllocator {

	final BasicBlock block;
	final Set liveOnExit;
	final RegFile regfile = new RegFile();
	
	// Temp:t currently in regfile -> Index of next ref to t
	final Map evictables = new HashMap();
	
	// maps (Instr:i x Temp:t) -> 2 * index of next Instr
	//                            referencing t
	// (only defined for t's referenced by i that 
	//  have a future reference; otherwise null)  
	final Map nextRef;

	// maps reference Temps to location Temps (ie the Temp
	// referred to in the code to the Temp that is represented in
	// the regfile)
	final Map referToLoc = new HashMap();
	
	// inverse of `referToLoc'
	final MultiMap locToRefers = new GenericMultiMap();

	Instr curr;

	private Temp getLoc(Temp refer) {
	    if (false) {
		Temp t = (Temp) referToLoc.get(refer);
		if (t == null) 
		    return tempSets.getRep(refer);
		else 
		    return tempSets.getRep(t);
	    } else {
		return tempSets.getRep(refer);
	    }
	}

	private void addRefer(final Temp refer, final Temp loc) {
	    Util.assert(!referToLoc.keySet().contains(refer),
			lazyInfo("referToLoc shouldn't already have key:"+refer,loc));
	    
	    referToLoc.put(refer, loc);
	    locToRefers.add(loc, refer);
	}

	// maps Temp:t -> Set of Regs 
	//      whose live regions interfere with t's live region
	final MultiMap preassignMap;

	

	LocalAllocator(BasicBlock b, Set lvOnExit) {
	    block = b;
	    liveOnExit = lvOnExit;
	    
	    nextRef = buildNextRef(b);

	    // System.out.println(block + " has liveOnExit:"+ lvOnExit);

	    preassignMap = 
		buildPreassignMap(block, 
				  frame.getRegFileInfo().getAllRegistersC(),
				  lvOnExit);

	    // System.out.println(block + " has preassignMap:"+preassignMap);
	    // System.out.println();

	    putPreassign(block, preassignMap);
	}

	private Map buildNextRef(BasicBlock b) {
	    // forall(j elem instrs(b))
	    //    forall(l elem pseudo-regs(j))
	    //       nextRef(j, l) <- indexOf(dist to next reference to l)
	    
	    // Temp:t -> the last Instr referencing t
	    Map tempToLastRef = new HashMap();
	    
	    // Instr:i -> the index of i in 'b'
	    Map instrToIndex = new HashMap();
	    
	    // (Temp:t x Instr:j) -> index of Instr following j referencing t
	    Map nextRef = new HashMap();
	    
	    Iterator instrs = b.statements().iterator();
	    int c=0;
	    while(instrs.hasNext()) {
		Instr instr = (Instr) instrs.next();
		instrToIndex.put(instr, new Integer(c));
		Iterator refs = getRefs(instr);
		while(refs.hasNext()) {
		    Temp ref = (Temp) refs.next();
		    Instr last = (Instr) tempToLastRef.get(ref);
		    if (last != null) {
			Util.assert((2*c) <= (Integer.MAX_VALUE - 1),
				    "IntOverflow;use another numeric rep in LRA");
			nextRef.put(new TempInstrPair(last, ref), 
				    new Integer(2*c));
		    }
		    tempToLastRef.put(ref, instr);
		}
		c++;
	    }
	    
	    Iterator entries = tempToLastRef.entrySet().iterator();
	    
	    while(entries.hasNext()) {
		Map.Entry entry = (Map.Entry) entries.next();
		
		// enforcing the storage of NULL in nextRef for dead
		// Temps, so that they can be distinquished.
		if (liveOnExit.contains(entry.getKey()) ||
		    liveOnExit.contains(getLoc((Temp)entry.getKey())))
		    nextRef.put(new TempInstrPair((Temp)entry.getKey(),
						  (Instr)entry.getValue()),
				INFINITY);
		
	    }
	    return nextRef;
	}

	/* purely a debugging hack. this methods and all references to
	   it (like several other methods in this class) should be
	   eliminated ASAP. -FSK
	*/
	boolean setContainsR9(Set set) {
	    String s = set.toString();
	    return (s.indexOf("r9") != -1);
	}

	/** Generates a mapping from each temp in <code>block</code>
	    to the set of preassigned registers conflicting with that
	    temp. 
	    
	    @param block      basic block to build the map from
	    @param regs       universe of register Temps that may be preassigned
	    @param liveOnExit set of Temps (including register Temps) that are live on exit from <block>
	*/
	MultiMap buildPreassignMap(BasicBlock block, Collection regs, Set liveOnExit) {
	    List bl = block.statements();
	    SetFactory regSetFact = 
		new BitSetFactory(new LinearSet(new HashSet(regs)));
	    
	    MultiMap tempToRegs = 
		new GenericMultiMap(regSetFact, Factories.hashMapFactory());

	    HashSet liveTemps = new HashSet(liveOnExit);
	    
	    final Set allRegs = regSetFact.makeSet(regs);

	    Set liveRegsOnExit = new HashSet(liveOnExit);
	    liveRegsOnExit.retainAll(regs);

	    Set liveRegs = regSetFact.makeSet(liveRegsOnExit);

	    if (setContainsR9(liveRegs)) 
		throw new RuntimeException(printInfo(block, null, null, code));
	    
	    updateMapping(tempToRegs, liveTemps, liveRegs,null,null);

	    // doing a reverse iteration
	    ListIterator liter = bl.listIterator(bl.size());
	    while(liter.hasPrevious()) {
		Instr i = (Instr) liter.previous();

		Temp exT = null, exR = null;
		if (i instanceof InstrMOVE) {
		    if (isTempRegister(i.def()[0])) {
			exR = i.def()[0];
			exT = i.use()[0];
		    } else if (isTempRegister(i.use()[0])) {
			exT = i.def()[0];
			exR = i.use()[0];
		    }
		} 
		
		
		// make new copy of live
		liveRegs = regSetFact.makeSet(liveRegs); 

		{   // liveRegs: kill (regs /\ defs)
		    Set defRegs = regSetFact.makeSet(allRegs);
		    defRegs.retainAll(i.defC()); 
		    liveRegs.removeAll(defRegs);
		    
		    // some regs are clobbered by routines, and thus
		    // appear in a defset but are never used.  We
		    // consider these to be impulse signals, and must
		    // be added to the set of conflicts accordingly
		    if (!defRegs.isEmpty()) {
			updateMapping(tempToRegs,liveTemps,defRegs,exT,exR);
		    }
		}

		liveTemps.removeAll(i.defC());	// kill defs
		liveTemps.addAll(i.useC());  // add uses

		{   // liveRegs: add (regs /\ uses)
		    Set useRegs = regSetFact.makeSet(allRegs);
		    useRegs.retainAll(i.useC());
		    liveRegs.addAll(useRegs);
		}

		if (setContainsR9(liveRegs)) 
		    throw new RuntimeException(printInfo(block, i, null, code));

		
		
		updateMapping(tempToRegs,liveTemps,liveRegs,exT,exR);

	    }

	    return tempToRegs;
	}

	/** For each t in `liveTemps', 
	    adds `liveRegs' to all the conflicting regs for t, 
	    excluding the mapping (`exceptT' -> `exceptR'). 
	    requires: (exceptR == null) ==> (exceptT == null)
	    effects:
	    for each t in `liveTemps'
	        let regs = if (t == `exceptT')
		           then `liveRegs' - `exceptR'
			   else `liveRegs'
		in adds regs to `tempToRegs'.get(t)
	*/
	private void updateMapping(MultiMap tempToRegs, Set liveTemps, 
				   Set liveRegs, Temp exceptT, Temp exceptR) {
	    // System.out.println("adding "+liveRegs+" to conflicts for "+
	    //                    liveTemps+" excluding ("+exceptT+" -> "+exceptR);

	    if (liveRegs.isEmpty()) return;
	    Iterator titer = liveTemps.iterator();
	    while(titer.hasNext()) {
		Temp t = (Temp) titer.next();
		if (t == exceptT) {
		    liveRegs.remove(exceptR);
		    tempToRegs.addAll(t, liveRegs);
		    liveRegs.add(exceptR);
		} else {
		    tempToRegs.addAll(t, liveRegs);
		}
	    }
	}

	void alloc() {

	    Iterator instrs = new FilterIterator
		(block.statements().iterator(),
		 new FilterIterator.Filter() {
		     public boolean isElement(Object o) {
			 final Instr j = (Instr) o;
			 return !(j instanceof SpillLoad ||
				  j instanceof SpillStore);
		     }});
	    
	    LocalAllocVisitor allocV = new LocalAllocVisitor();
	    while(instrs.hasNext()) {
		Instr i = (Instr) instrs.next();
		if (TIME) System.out.print(".");
		curr = i;
		i.accept(allocV);
	    }
	    
	    emptyRegFile(regfile, allocV.last, liveOnExit);


	    // System.out.println();
	    // System.out.println();
	}

	class LocalAllocVisitor extends harpoon.IR.Assem.InstrVisitor {
	    // last Instr that was visited that still remains in the
	    // code (thus, not a removed InstrMOVE).  Used when
	    // emptying the register file post-allocation
	    Instr last;

	    // filters out hardcoded refs to machine registers 
	    class MRegFilter extends FilterIterator.Filter {
		public boolean isElement(Object o) {
		    return !isTempRegister((Temp) o);
		}
	    }

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
		    if (!isTempRegister(def)) {
			regfile.writeTo(getLoc(def));
		    } else {
			Temp t = regfile.getTemp(def);
			if (t != null) {
			    if (liveOnExit.contains(t) ||
				liveOnExit.contains(getLoc(t))) {
				System.out.println("\tWTF?!? removing " + 
						   t + " from " + regfile
						   + " for " + i);
				Instr prev = i.getPrev();

				// FSK: update code to do something
				// smarter (and more general)
				Util.assert(prev.canFallThrough &&
					    prev.getTargets().isEmpty() &&
					    i.predC().size() == 1,
					    "i.getPrev is bad choice;");

				spillValue(t, prev, regfile);
			    }
			    regfile.remove(t);
			}
		    }
		}
		
		evictables.putAll(putBackLater);

		Util.assert(hasRegs(i, i.useC()),
			    lazyInfo("uses missing reg assignment",i,null));
		Util.assert(hasRegs(i, i.defC()),
			    lazyInfo("defs missing reg assignment", i, null));
		
		last = i;
	    }

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
		Map putBackLater = new HashMap();
		Iterator uses = new FilterIterator(i.useC().iterator(),
						   new MRegFilter());
		while(uses.hasNext()) {
		    Temp use = getLoc((Temp) uses.next());
		    if (evictables.containsKey(use)) {
			putBackLater.put(use, evictables.get(use));
			evictables.remove(use);
		    }
		}
		return putBackLater;
	    }

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
		if (regfile.hasAssignment(getLoc(t))) {
		    
		    code.assignRegister(i, t, regfile.getAssignment(getLoc(t)));
		    evictables.remove(getLoc(t)); // (`t' reinserted later)

		} else { /* not already assigned */ 
		    
		    Set preassigns = addPreassignments(t);
		    Iterator suggs = getSuggestions(t, regfile, i, evictables);
		    List regList = chooseSuggestion(suggs, t); 

		    code.assignRegister(i, t, regList);
		    regfile.assign(getLoc(t), regList);
		    
		    if (i.useC().contains(t)) {
			InstrMEM load = SpillLoad.makeLD(i, "FSK-LD", regList, getLoc(t));
			spillLoads.add(load);
			spillLoads.add(i);
		    }

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
		    putBackLater.put(getLoc(t), X);
		} else { 
		    evictables.put(getLoc(t), X); 
		}
	    }

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
		Set preassignTempSet = new HashSet();
		
		Util.assert(preassigns != null,
			    "preassignMap is missing mappings");
		Iterator preassignIter = preassigns.iterator();
		while(preassignIter.hasNext()) {
		    final Temp reg = (Temp) preassignIter.next();
		    
		    if (regfile.isEmpty(reg)) { 
			Temp preassign = new RegFileInfo.PreassignTemp(reg);
			regfile.assign( preassign, 
					Arrays.asList( new Temp[]{ reg }));
			preassignTempSet.add(preassign);
		    }
		}

		return preassignTempSet;
	    }

	    /** Finds the weight of `t' when used at `i'.  
		requires: `t' is used by `i'
		          `getLoc(t)' has an assignment in `regfile'
		effects: let w = 2*(distance until the next use) 
		         in if `t' is dirty 
			    then returns (w+1) 
			    else returns w
	    */
	    private Integer findWeight(Temp t, Instr i) {
		Integer X = (Integer) nextRef.get(new TempInstrPair(i, t));
		    
		// null => never referenced again; effectively infinite
		if (X == null) X = INFINITY;
		    
		Util.assert(X.intValue() <= (Integer.MAX_VALUE - 1),
			    "Excessive Weight was stored.");
		
		Util.assert(regfile.hasAssignment(getLoc(t)),
			    lazyInfo("no assignment for "+
				     "getLoc("+t+"):"+getLoc(t)
				     , i, t));

		if (regfile.isDirty(getLoc(t))) {
		    X = new Integer(X.intValue() + 1);
		}
		
		return X;
	    }

	    
	    public void visit(final InstrMOVE i) {
		final Temp u = i.use()[0];
		final Temp d = i.def()[0];
		Map putBackLater = takeUsesOutOfEvictables(i);

		if (!isTempRegister(u) &&
		    !regfile.hasAssignment(getLoc(u))) {
		    // load that value into a register...
		    assign(u, i, putBackLater);
		} 
		
		List ul = resolve(u);
		List dl = resolve(d);
		int choice = 0;

		if ( ul.equals(dl) &&
		     !regfile.hasAssignment(getLoc(d)) &&
		     !preassignMap.contains(d, ul.get(0))) {
		    choice = 1;
		} else if (!(isTempRegister(d) ||
			     regfile.hasAssignment(getLoc(d))) &&
			   !isTempRegister(u) &&
			   !preassignMap.contains(d, ul.get(0))) { 
		    choice = 2;
		} else if (u == d) {
		    // weird, but it actually happens
		    choice = 3;
		}

		
		if (choice != 0) {
		    Util.assert(tempSets.getRep(u) ==
				tempSets.getRep(d),
				"Temps "+u+" & "+d+" should have same rep to be coalesced"); 
		    Util.assert(regfile.hasAssignment(getLoc(u)), 
				lazyInfo("use:"+u+" or getLoc(use):"+
					 getLoc(u)+" should have an assignment at this point",i,u));

		    List regList = regfile.getAssignment(getLoc(u));
		    code.assignRegister(i, u, regList);
		    code.assignRegister(i, d, regList);
		    remove(i, choice);


		    if (u != d) addRefer(d, getLoc(u));

		    // System.out.println("assigning "+i.def()[0] +
		    //				       " to "+regList);
		} else {
		    visit((Instr) i);
		    if(getRegs(i,i.use()[0]).equals(getRegs(i,i.def()[0]))) {
			remove(i, 4);
		    }		
		}
		
		Util.assert(hasRegs(i, u),
			    lazyInfo("missing reg assignment",i,u));

		evictables.putAll(putBackLater);
	    }

	    private void remove(Instr i, int n) {
		remove(i, n, (n!=3)&&(n!=4));
	    }

	    private void remove(Instr i, int n, boolean pr) {
		instrsToRemove.add(i);
		if (pr) System.out.println("removing"+n+" "+i+" rf: "+regfile);
	    }

	    public List resolve(Temp t) {
		if (regfile.hasAssignment(getLoc(t))) {
		    return regfile.getAssignment(getLoc(t));
		} else {
		    return java.util.Arrays.asList(new Temp[]{ t });
		} 
	    }
	}

	/** Gets an Iterator of suggested register assignments in
	    <code>regfile</code> for <code>t</code>.  May insert
	    load/spill code before <code>i</code>.  Uses
	    <code>evictables</code> as a <code>Map</code> from
	    <code>Temp</code>s to weighted distances to decide which
	    <code>Temp</code>s to spill.  
	*/
	private Iterator getSuggestions(final Temp t, final RegFile regfile, 
					final Instr i, final Map evictables) {
	    if (false) System.out.println("getting suggestions for "+t+
			       " in "+i+" with regfile "+regfile+
			       " and evictables "+evictables);
	    
	    for(int x=0; true; x++) {
		Util.assert(x < 10, "shouldn't have to iterate >10");

		try {

		    Iterator suggs = 
			frame.getRegFileInfo().suggestRegAssignment
			(t, regfile.getRegToTemp());
		    return suggs;
		} catch (SpillException s) {

		    Iterator spills = s.getPotentialSpills();
		    SortedSet weightedSpills = new TreeSet();

		    Collection preasnRegs = preassignMap.getValues(t);

		    final Map trackSpills = new HashMap();

		    while(spills.hasNext()) {
			boolean valid = true;
			Set cand = (Set) spills.next();
			

			HashSet temps = new HashSet();
			Iterator regs = cand.iterator();
			
			int cost=Integer.MAX_VALUE;
			while(regs.hasNext()) {
			    Temp reg = (Temp) regs.next();
			    Temp preg = regfile.getTemp(reg);
			    
			    trackSpills.put(cand, "Allowed");

			    // did reg previously hold a value?
			    if (preg != null) {
				temps.add(preg);
				if (!evictables.containsKey(preg)) {
				    // --> disallowed evicting preg
				    valid = false;
				    trackSpills.put(cand, "psuedo:"+preg+" for "+cand+" not in Evictables");
				    break;
				} else if (preasnRegs.contains(reg)) {
				    valid = false;
				    trackSpills.put(cand, "reg:"+reg+" for "+cand+" has conflicting preassignment"); 
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

		    Util.assert(!weightedSpills.isEmpty(), 
				lazyInfo("\nneed at least one spill"
					 +" of \n"+trackSpills+"\nEvictables:"+evictables
					 ,i,t,regfile));

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
			    if (SPILL_INFO) System.out.print(" ; spilling "+value);
			    spillValue(value,i.getPrev(),regfile);
			    regfile.remove(value);
			    evictables.remove(value);
			}
		    }
		    if (SPILL_INFO) System.out.println();

		}
	    }
	}
	
	private void emptyRegFile(RegFile regfile, final Instr instr, 
				  Set liveOnExit) {
	    // System.out.println("live on exit from " + b + " :\n" + liveOnExit);
	    
	    // use a new Set here because we cannot modify regfile
	    // and traverse its tempSet simultaneously.
	    Set locvalSet = new LinearSet(regfile.tempSet());
	    if (SPILL_INFO) 
		System.out.println("emptying regfile:"+regfile);
	    final Iterator locvals = locvalSet.iterator();
	    while(locvals.hasNext()) {
		final Temp locval = (Temp) locvals.next();
		
		// handle spilling move equivalent temps
		Iterator vals = locToRefers.getValues(locval).iterator();
		while(vals.hasNext()) {
		    final Temp val = (Temp) vals.next();
		    
		    Util.assert(!isTempRegister(val), 
				"move sets should not contain registers");
		    
		    // don't spill dead values.
		    if (liveOnExit.contains(val)) {
			// don't spill dead values.
			if (SPILL_INFO) 
			    System.out.println("spilling "+val+
					       " 'cause its live (0)");
			chooseSpillSpot(val, instr, regfile);
		    }
		}
		


		if (isTempRegister(locval)) {
		    // don't spill register only values

		} else if (regfile.isClean(locval)) {
		    // FSK: weirdness in SpillCodePlacement occurs if
		    // I (conservatively) leave this case out, which
		    // is a sign that something's wrong.  Experiement
		    // further... 

		    // don't spill clean values. 
		    if (SPILL_INFO) 
			System.out.println("not spilling "+locval+
					   " 'cause its clean");

		} else {
		    // FSK: check and document this; implications of
		    // getLoc() et.al are unclear at best.
		    if (liveOnExit.contains(locval)){ 
			// don't spill dead values.
			if (false && SPILL_INFO) 
			    System.out.println("spilling "+locval+
					       " 'cause its live (1)");
			chooseSpillSpot(locval, instr, regfile);
		    } else if (liveOnExit.contains(getLoc(locval))) {
			// don't spill dead values.
			// FSK: This case seems to be useless; all
			// tests show that spills are caught in case
			// (1) above or case (3) below...
			if (SPILL_INFO) 
			    System.out.println("spilling "+locval+
					       " 'cause its live (2)");
			chooseSpillSpot(locval, instr, regfile);
		    } else {
			if (false && SPILL_INFO)
			    System.out.println("SKIPPING "+locval+
					       " 'cause its dead (3)");
		    }
		}

		regfile.remove(locval);
	    }
	}

	private void chooseSpillSpot(Temp val, Instr instr, RegFile regfile) {
	    // need to insert the spill in a place where we can be
	    // sure it will be executed; the easy case is where
	    // 'instr' does not redefine the 'val' (so we can just put 
	    // our spills BEFORE 'instr').  If 'instr' does define
	    // 'val', however, then we MUST wait to spill, and
	    // then we need to see where control can flow...
	    // insert a new block solely devoted to spilling
	    
	    Instr loc;
	    final Instr prev = instr.getPrev();
	    if (!instr.defC().contains(val) &&
		prev.getTargets().isEmpty() &&
		prev.canFallThrough) {
		
		spillValue(val, prev, regfile);
		
	    } else {
		
		spillValue(val, instr, regfile);
		
		// Something like this code will have to be used
		// where the spill code is acutally inserted, but
		// its no longer necessary here. 
		if (false) {
		    if (instr.canFallThrough) {
			Util.assert(instr.getNext() != null, 
				    instr.getPrev() + 
				    " before Instr: ("+instr+
				    ") .getNext() != null"); 
			
				// System.out.println("weird spill: " + val + " " + loc);
				// This sequence of code is a little tricky; since
				// we need to add spills for the same variable at
				// multiple locations, we need to delay updating
				// the regfile until after all of the spills have
				// been added.  So we need to use
				// addSpillInstr/removeMapping instead of just
				// spillValue 
			
			spillValue(val, instr, regfile);
		    }
		    
		    Util.assert(instr.getTargets().isEmpty() ||
				instr.hasModifiableTargets(),
				"We MUST be able to modify the targets "+
				" if we're going to insert a spill here");
		    Iterator targets = instr.getTargets().iterator();
		    while(targets.hasNext()) {
			Label l = (Label) targets.next();
			loc = null; // new InstrEdge(instr, instr.getInstrFor(l));
			System.out.println("labelled spill: " + val + " " + loc);
			spillValue(val, loc, regfile);
		    }
		}
	    }
	}

	
	
	private List chooseSuggestion(Iterator suggs, Temp t) {
	    return chooseSuggestion(suggs, t, false);
	}
	
	private List chooseSuggestion(Iterator suggs, Temp t, boolean pr) {
	    Temp reg = tempSets.getReg(t);
	    List suggL = null;
	    if (reg != null) {
		// if [ <reg> ] is in <suggs>, ret [ <reg> ]
		// else, ret arbitrary elem suggs 
		do {
		    suggL = (List) suggs.next();
		    
		    if (suggL.size() == 1) {
			Temp suggReg = (Temp) suggL.get(0);
			if (suggReg.equals(reg)) {
			    if (pr) {
				System.out.println(" ok_Pre " + t +
						   " ("+tempSets.getRep(t) + 
						   ") to " + reg
						   // +"\n"+tempSets
						   ); 
			    }
			    return suggL;
			} 
		    }
		} while (suggs.hasNext());

		// got to this point => didn't find match for <reg> 
		if (suggL.size() == 1) {
		    // insert new association (since we've been forced
		    // to use a different reg assignment than what we preferred)
		    reg = (Temp) suggL.get(0);
		    tempSets.associate(t, reg);
		    if (pr) 
			System.out.println(" badPre " + t +
					   " ("+tempSets.getRep(t) + 
					   ") to " + reg
					   // +"\n"+tempSets
					   ); 

		}


		return suggL;
		
	    } else {
		suggL = (List) suggs.next();
		if (suggL.size() == 1) {
		    reg = (Temp) suggL.get(0);
		    tempSets.associate(t, reg);
		    if (pr) 
			System.out.println(" no_Pre " + t +
					   " ("+tempSets.getRep(t) + 
					   ") to " + reg
					   // +"\n"+tempSets
					   ); 

		}
		return suggL;
	    }
	}
	
	/** includes both pseudo-regs and machine-regs for now. */
	private Iterator getRefs(final Instr i) {
	    return new CombineIterator(i.useC().iterator(), 
				       i.defC().iterator());
	}
	

	
	/** spills `val', adding a store after `loc', but does *NOT*
	    update the regfile. 
	*/
	private void spillValue(Temp val, Instr loc, RegFile regfile) {
	    Util.assert(! (val instanceof RegFileInfo.PreassignTemp),
			"cannot spill Preassigned Temps");
	    Util.assert(!isTempRegister(val), val+" should not be reg");

	    Collection regs = regfile.getAssignment(getLoc(val));
	    Util.assert(regs != null, 
			lazyInfo("must map to a set of registers\n"+
				 "tempSets:"+tempSets,val,regfile)); 
	    Util.assert(!regs.isEmpty(), 
			lazyInfo("must map to non-empty set of registers",val,regfile));

	    InstrMEM spillInstr = SpillStore.makeST(loc, "FSK-ST", getLoc(val), regs);

	    spillStores.add(spillInstr);
	    spillStores.add(loc);
	}

	// *** helper methods for debugging within LocalAllocator ***
	private Object lazyInfo(String prefix, Temp t) {
	    return lazyInfo(prefix, curr, t);
	}

	private Object lazyInfo(String prefix, Temp t, RegFile regfile) {
	    return lazyInfo(prefix, curr, t, regfile);
	}

	private Object lazyInfo(String prefix, Instr i, Temp t) {
	    return LocalCffRegAlloc.this.lazyInfo(prefix, block, i, t, code);
	}

	private Object lazyInfo(String prefix, Instr i, Temp t,
				RegFile regfile) {
	    return LocalCffRegAlloc.this.lazyInfo(prefix, block, i, t, code, regfile);
	}
    }

    private LiveTemps doLVA(BasicBlock.Factory bbFact) {
	LiveTemps liveTemps = 
	    new LiveTemps(bbFact, frame.getRegFileInfo().liveOnExit());
	harpoon.Analysis.DataFlow.Solver.worklistSolve
	    (bbFact.blockSet().iterator(), liveTemps);
	return liveTemps;
    }

    
    /** wrapper around set with an associated weight. */
    private class WeightedSet extends AbstractSet implements Comparable {
	private Set s; 
	Set temps;
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
	public String toString() { 
	    return "<Set:"+s.toString()+
		",Weight:"+weight+
		(temps==null?"":(",Temps:"+temps))+">"; 
	}
    }


    // *** DEBUGGING ROUTINES ***

    private static String toAssem(Instr i, Code code) {
	// SpillLoads and SpillStores do not put the appropriate
	// suffixes on Temps (because they are just
	// placeholders for actual load and store
	// instructions) so we don't attempt to convert them
	// to assembly form
	if (i instanceof SpillLoad ||
	    i instanceof SpillStore) {
	    return i.toString();
	} else {
	    return code.toAssem(i);
	}
    }


    // lazyInfo(..) family of methods return an object that prints out
    // the basic block in a demand driven fashion, so that we do not
    // incur the cost of constructing the string representation of the
    // basic block until we actually will need it

    private Object lazyInfo(String prefix, BasicBlock b, 
			    Instr i, Temp t, Code code) {
	return lazyInfo(prefix, b, i, t, code, true);
    }

    private Object lazyInfo(String prefix, BasicBlock b, 
			    Instr i, Temp t, Code code, 
			    RegFile regfile) {
	return lazyInfo(prefix, b, i, t, code, regfile, true);
    }

    private Object lazyInfo(final String prefix, final BasicBlock b, 
			    final Instr i, final Temp t, final Code code, 
			    final boolean auxSpillCode) {
	return new Object() {
	    public String toString() {
		return prefix+"\n"+printInfo(b, i, t, code, auxSpillCode);
	    }
	};
    }

    private Object lazyInfo(final String prefix, final BasicBlock b, 
			    final Instr i, final Temp t, final Code code, 
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
	    sb.append(toAssem(i2, code)+
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


}
