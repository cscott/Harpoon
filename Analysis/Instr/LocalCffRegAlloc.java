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
import harpoon.Analysis.Instr.RegAlloc.FskLoad;
import harpoon.Analysis.Instr.RegAlloc.FskStore;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMOVE;
import harpoon.IR.Assem.InstrEdge;
import harpoon.IR.Assem.InstrMEM;
import harpoon.Temp.Temp;
import harpoon.Temp.Label;

import harpoon.Util.CloneableIterator;
import harpoon.Util.LinearMap;
import harpoon.Util.Util;
import harpoon.Util.FilterIterator;
import harpoon.Util.ListFactory;

import java.util.List;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Iterator;


/**
 * <code>LocalCffRegAlloc</code> performs <A
    HREF="http://lm.lcs.mit.edu/~pnkfelix/papers/hardnessLRA.ps">
    Local Register Allocation</A> for a given set of
    <code>Instr</code>s using a conservative-furthest-first algorithm.
    The papers <A 
    HREF="http://ctf.lcs.mit.edu/~pnkfelix/papers/OnLocalRegAlloc.ps.gz">
    "On Local Register Allocation"</A> and <A
    HREF="http://ctf.lcs.mit.edu/~pnkfelix/papers/hardnessLRA.ps">"Hardness and
    Algorithms for Local Register Allocation"</A> lay out the basis
    for the algorithm it uses to allocate and assign registers.
  
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: LocalCffRegAlloc.java,v 1.1.2.62 2000-01-26 06:05:39 cananian Exp $
 */
public class LocalCffRegAlloc extends RegAlloc {
    
    /** Creates a <code>LocalCffRegAlloc</code>. */
    public LocalCffRegAlloc(Code code) {
        super(code);
    }
    
    protected Code generateRegAssignment() {
	LiveTemps liveTemps = 
	    doLVA(BasicBlock.basicBlockIterator(rootBlock));
	
	Iterator blocks = 
	    BasicBlock.basicBlockIterator(rootBlock);
	while(blocks.hasNext()) {
	    BasicBlock b = (BasicBlock) blocks.next();
	    Set liveOnExit = liveTemps.getLiveOnExit(b);
	    alloc(b, liveOnExit);
	}
	
	return code;
    }


    private void alloc(BasicBlock block, Set liveOnExit) {
	(new LocalAllocator(block, liveOnExit)).alloc();
    }
    
    // can add 1 to weight later, so store MAX_VALUE - 1 at most. 
    static Integer INFINITY = new Integer( Integer.MAX_VALUE - 1 );

    class LocalAllocator {
	final BasicBlock block;
	final Set liveOnExit;
	final RegFile regfile;

	// Temp:t currently in regfile -> Index of next ref to t
	final Map evictables;

	// maps (Instr:i x Temp:t) -> 2 * index of next Instr
	//                            referencing t 
	// (only defined for t's referenced by i that 
	// have a future reference; otherwise 'null')  
	final Map nextRef;
	
	LocalAllocator(BasicBlock b, Set lvOnExit) {
	    block = b;
	    liveOnExit = lvOnExit;
	    
	    // System.out.print("Bnr");
	    nextRef = buildNextRef(b);
	    regfile = new RegFile();
	    evictables = new HashMap();
	}

	void alloc() {
	    // System.out.print("Lra");
	    
	    // FSK: first approach: preassign hardcoded registers, without
	    // accounting for liveness of Temps at all.  Later, replace
	    // this with something smarter (but still efficient!) that
	    // will only preassign hardcoded registers whose live ranges
	    // conflict with the Ref currently being assigned.
	    precolorRegfile(block, regfile);
	    
	    Iterator instrs = new FilterIterator
		(block.iterator(),
		 new FilterIterator.Filter() {
		     public boolean isElement(Object o) {
			 final Instr j = (Instr) o;
			 return !(j instanceof FskLoad ||
				  j instanceof FskStore);
		     }
		 });
	    
	    LocalAllocVisitor v = new LocalAllocVisitor();
	    while(instrs.hasNext()) {
		Instr i = (Instr) instrs.next();
		i.accept(v);
	    }
	    
	    emptyRegFile(regfile, v.last, liveOnExit);
	}
	
	class LocalAllocVisitor extends harpoon.IR.Assem.InstrVisitor {
	    // last Instr that was visited that still remains in the
	    // code (thus, not a removed InstrMOVE)
	    Instr last;

	    public void visit(final Instr i) {
		Iterator refs = new FilterIterator
		    (getRefs(i), new FilterIterator.Filter() {
			public boolean isElement(Object o) {
			    return !isTempRegister((Temp) o);
			}
		    });
		
		while(refs.hasNext()) {
		    Temp t = (Temp) refs.next();
		    if (regfile.hasAssignment(t)) {
			code.assignRegister
			    (i, t, regfile.getAssignment(t));
			evictables.remove(t);
		    } else {
			
			// FSK: the benefits of this code block are
			// dubious at best, so i'm keeping it out for now 
			if (false) {
			    // regfile has mappings to dead temps; such
			    // registers won't be suggested unless we spill
			    // them here...
			    final Collection temps = 
				regfile.getRegToTemp().values();
			    
			    final Iterator tmpIter = temps.iterator();
			    
			    // System.out.println("regfile: "+regfile);
			    while(tmpIter.hasNext()) {
				Temp tt = (Temp) tmpIter.next();
				Integer X = (Integer) nextRef.get
				    (new TempInstrPair(i, tt));
				if (X == null &&
				    !liveOnExit.contains(tt) &&
				    regfile.hasAssignment(tt) &&
				    // only force cleanspills
				    regfile.isClean(tt)) { 
				    regfile.remove(tt);
				}
			    }
			}
		    
			
			Iterator suggs = getSuggestions(t, regfile, i, evictables);
			List regList = chooseSuggestion(suggs);
			code.assignRegister(i, t, regList);
			regfile.assign(t, regList);
			
			if (i.useC().contains(t)) {
			    InstrMEM load = 
				new FskLoad
				(i, "FSK-LOAD", regList, t);
			    load.insertAt(new InstrEdge(i.getPrev(), i));
			}
		    }
		    
		    Integer X = (Integer) nextRef.get(new TempInstrPair(i, t));
		    
		    if (X == null) X = INFINITY;
		    Util.assert(X.intValue() <= (Integer.MAX_VALUE - 1),
				"Excessive Weight was stored.");
		    
		    // TODO: if t is dirty then X <- X+0.5 endif
		    // (since weights are doubled, use X++;
		    if (regfile.isDirty(t)) {
			X = new Integer(X.intValue() + 1);
		    }
		    
		    evictables.put(t, X); 
		}
		
		Iterator defs = i.defC().iterator();
		while(defs.hasNext()) {
		    Temp def = (Temp) defs.next();
		    // Q: should we also mark writes to hardcoded registers?
		    if (!isTempRegister(def)) regfile.writeTo(def);
		}
		
		// finished local alloc for 'i'
		// lets verify
		Iterator refIter = getRefs(i);
		while(refIter.hasNext()) {
		    final Temp ref = (Temp) refIter.next();
		    Util.assert(isTempRegister(ref) ||
				code.registerAssigned(i, ref),
				new Object() {
			public String toString() {
			    return
				"Instr: "+i + " / " +
				code.toAssem(i) +
				" needs register "+
				"assignment for Ref: "+ref;
			}});
		}
		
		last = i;
	    }
	    
	    public void visit(InstrMOVE i) {
		if (true) {
		    visit((Instr)i);
		    return;
		}
		
		// very simple move coalescing: if this is the last use of
		// a temp, just replace the mapping in the abstract
		// regfile instead of actually doing the move.
		Temp src = i.use()[0];
		Temp dst = i.def()[0];
		
		List regs = null;
		
		if (isTempRegister(src) &&
		    !isTempRegister(dst) &&
		    !regfile.hasAssignment(dst)

		    // FSK: removing this clause, but its abscence may
		    // break output 
		    // && !nextRef.containsKey(new TempInstrPair(i, src))
					 ) {
		    // remove the RegFileInfo.PreassignTemp
		    regfile.remove((Temp)regfile.getRegToTemp().get(src));

		    regs = harpoon.Util.ListFactory.singleton(src);
		    regfile.assign(dst, regs);
		    // System.out.println(" removing "+i+" : (Td<-Rs)"
		    //                    /* + " && !futureRef(Rs)"*/ );
		} else if (!isTempRegister(src) &&
			   !isTempRegister(dst) &&
			   regfile.hasAssignment(src) &&
			   nextRef.get(new TempInstrPair(i, src)) == null) {
		    regs = regfile.getAssignment(src);
		    regfile.remove(src);
		    // System.out.println(" removing "+i+" : (Td<-Ts) && !futureRef(Ts)");
		} else if (isTempRegister(dst) &&
			   !isTempRegister(src) &&
			   nextRef.get(new TempInstrPair(i, src)) == null &&
			   regfile.hasAssignment(src) &&
			   regfile.getAssignment(src).equals
			        (harpoon.Util.ListFactory.singleton(dst))) {
		    regfile.remove(src);
		    regs = harpoon.Util.ListFactory.singleton(dst);
		    // FSK: reassigning dst to better reflect
		    // regfile's state (may not be correct approach however)
		    dst = new RegFileInfo.PreassignTemp(dst); 
		    // System.out.println(" removing "+i+" : (Rd<-Ts) && !futureRef(Ts) && reg(Ts)==Rd");
		} else {
		    // System.out.println(" not removing "+i);
		}
		
		if(regs != null) {
		    if (regfile.hasAssignment(dst)) regfile.remove(dst);
		    
		    regfile.assign(dst, regs);
		    
		    // need to save this value if the Temp is spilled
		    regfile.writeTo(dst);
		    
		    Integer X = (Integer) nextRef.get
			(new TempInstrPair(i, dst)); 
		    if (X == null) X = INFINITY;
		    evictables.put(dst, X);
		    
		    // System.out.println("Yay, removing " + i);
		    i.remove();

		} else {
		    // couldn't remove; treat as a normal Instr
		    visit( (Instr) i);
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
	private Iterator getSuggestions(Temp t, RegFile regfile, 
					Instr i, Map evictables) {
	    for(int x=0; true; x++) {
		Util.assert(x < 5, "shouldn't have to iterate >5");

		try {
		    Iterator suggs = 
			frame.getRegFileInfo().suggestRegAssignment
			(t, regfile.getRegToTemp());
		    return suggs;
		} catch (SpillException s) {
		    Iterator spills = s.getPotentialSpills();
		    SortedSet weightedSpills = new TreeSet();
		    while(spills.hasNext()) {
			Set cand = (Set) spills.next();
			Iterator regs = cand.iterator();
		    
			int cost=Integer.MAX_VALUE;
			while(regs.hasNext()) {
			    Temp reg = (Temp) regs.next();
			    Temp preg = regfile.getTemp(reg);
			if (preg != null) {
			    Integer dist = (Integer) evictables.get(preg); 
			    Util.assert(dist != null, 
					"Alloc for "+i+" "+
					"Preg: "+preg+" should be in "+
					"evictables if it is in regfile: "+
					regfile);
			    int c = dist.intValue();
			    if (c < cost) { 
				cost = c;
			    }
			}
		    }
		    
		    weightedSpills.add(new WeightedSet(cand, cost));
		    // System.out.println("Adding "+cand+" at cost "+cost);
		}
		
		WeightedSet spill = (WeightedSet) weightedSpills.first();
		// System.out.println("Choosing to spill "+spill+
		//	       " of " + weightedSpills);
		
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
			
		    } else {
			spillValue(value, 
				   new InstrEdge(i.getPrev(), i), 
				   regfile);
			evictables.remove(value);
		    }
		}
	    }
	}
    }
    
    private void precolorRegfile(BasicBlock b, RegFile regfile) {
	Iterator instrs = b.iterator();
	Instr i=null;
	while(instrs.hasNext()) {
	    i = (Instr) instrs.next();
	    Iterator regs = 
		new FilterIterator
		(getRefs(i), new FilterIterator.Filter() {
		    public boolean isElement(Object o) {
			return isTempRegister((Temp) o);
		    }
		});
	    while(regs.hasNext()) {
		Temp reg = (Temp) regs.next();
		if (regfile.getTemp(reg) == null) 
		    regfile.assign(new RegFileInfo.PreassignTemp(reg),
				   ListFactory.singleton(reg));
	    }
	}
    }

    private void emptyRegFile(RegFile regfile, Instr instr, 
			      Set liveOnExit) {
	// System.out.println("live on exit from " + b + " :\n" + liveOnExit);
	
	// use a new HashSet here because we don't want to repeat values
	// (regToTemp.values() returns a Collection-view)
	Iterator vals = 
	    (new HashSet(regfile.getRegToTemp().values())).iterator();
	
	while(vals.hasNext()) {
	    Temp val = (Temp) vals.next();
	    
	    // System.out.println("dealing with " + val + " at end of " + b);
	    
	    // don't spill dead values.
	    if (!liveOnExit.contains(val)) {
		regfile.remove(val);
		continue;
	    }
	    
	    
	    // don't spill clean values.
	    if (regfile.isClean(val)) {
		regfile.remove(val);
		continue;
	    }
	    
	    // need to insert the spill in a place where we can be
	    // sure it will be executed; the easy case is where
	    // 'instr' does not redefine the 'val' (so we can just put 
	    // our spills BEFORE 'instr').  If 'instr' does define
	    // 'val', however, then we MUST wait to spill, and
	    // then we need to see where control can flow...
	    // insert a new block solely devoted to spilling
	    InstrEdge loc;
	    if (!instr.defC().contains(val) &&
		instr.getPrev().getTargets().isEmpty()) {

		loc = new InstrEdge(instr.getPrev(), instr);
		
		// System.out.println("end spill: " + val + " " + loc);
		
		spillValue(val, loc, regfile);
	    } else {
		if (instr.canFallThrough) {
		    Util.assert(instr.getNext() != null, 
				instr.getPrev() + 
				" b4 Instr: "+instr+
				" .getNext() != null"); 
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
		regfile.remove(val);
	    }
	}
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

	Iterator instrs = b.iterator();
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
	    if (liveOnExit.contains(entry.getKey()))
		nextRef.put(new TempInstrPair((Temp)entry.getKey(),
					      (Instr)entry.getValue()),
			    INFINITY);
			
	}
	return nextRef;
    }


    private List chooseSuggestion(Iterator suggs) {
	// TODO (to improve alloc): add code here eventually to scan
	// forward and choose a suggestion based on future MOVEs
	// to/from preassigned registers.  Obviously the signature of
	// the function may need to change...
			
	// FSK: dumb chooser (just takes first suggestion)
	return (List) suggs.next(); 

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


    /** spills 'val', adding a store if necessary at 'loc' and updates
	the 'regfile' so that it no longer has a mapping for 'val' or
	its associated registers.
    */
    private void spillValue(Temp val, InstrEdge loc, RegFile regfile) {
	if (regfile.isDirty(val)) {
	    Util.assert(! (val instanceof RegFileInfo.PreassignTemp),
			"cannot spill Preassigned Temps");
	    addSpillInstr(val, loc, regfile);
	}
	regfile.remove(val);
    }
    
    /** adds a store for 'val' at 'loc', but does *NOT* update the
	regfile. 
    */
    private void addSpillInstr(Temp val, InstrEdge loc, RegFile regfile) {
	Collection regs = regfile.getAssignment(val);
	Util.assert(regs != null, val+ " must have an assignment in "+
		                       "regfile to be spilled");
	Util.assert(!regs.isEmpty(), 
		    val + " must map to SOME registers" +
		    "\n regfile:" + regfile);

	InstrMEM spillInstr = new FskStore(loc.to, "FSK-STORE", val, regs);
	spillInstr.insertAt(loc);
    }
    }

    private LiveTemps doLVA(Iterator blocks) {
	LiveTemps liveTemps = 
	    new LiveTemps(blocks, frame.getRegFileInfo().liveOnExit());
	harpoon.Analysis.DataFlow.Solver.worklistSolve
	    (BasicBlock.basicBlockIterator(rootBlock),
	     liveTemps);
	return liveTemps;
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
	public String toString() { 
	    return "<Set:"+s.toString()+",Weight:"+weight+">"; 
	}
    }

}
