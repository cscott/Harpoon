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
import harpoon.Util.DisjointSet;
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
 * @version $Id: LocalCffRegAlloc.java,v 1.1.2.77 2000-05-24 18:48:26 pnkfelix Exp $
 */
public class LocalCffRegAlloc extends RegAlloc {

    private static boolean TIME = false;
    private static boolean VERIFY = true;
    
    /** Creates a <code>LocalCffRegAlloc</code>. */
    public LocalCffRegAlloc(Code code) {
        super(code);
    }

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

    private String printInfo(BasicBlock block, Instr i, 
			     Temp t, Code code) {
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

	    // insert loads...
	    Iterator loads = spillLoads.iterator();
	    while(loads.hasNext()) {
		Instr s = (Instr) loads.next();
		Instr loc = (Instr) loads.next();
		if (loc == i2) {
		    sb.append(s+"\n");
		}
	    }

	    // put actual instr in
	    sb.append(toAssem(i2, code)+
		      " \t { " + i2 + 
		      " }\n");

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
	
	return "\n"+
	    sb.toString() + 
	    "temp: "+t + "\n"+
	    "instr: "+ i + "\n\n";

    }
    
    final List instrsToRemove = new java.util.LinkedList();

    
    // When writing spillCode insertion routines, be sure to check
    // that the method of insertion is correct given the control flow
    // for that Instr...

    // this List is an alternating sequence of a Load and the Instr
    // that the Load is to occur BEFORE : [ l0 i0 l1 i1 .. lN iN ]
    final List spillLoads = new java.util.LinkedList();

    // this List is an alternating sequence of a Store and the Instr
    // that the Store is to occur AFTER : [ s0 i0 s1 i1 .. sN iN ]
    final List spillStores = new java.util.LinkedList();
    
    
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

	// BasicBlock -> EqTempSets
	HashMap bb2ts = new HashMap();

	while(blocks.hasNext()) {
	    BasicBlock b = (BasicBlock) blocks.next();
	    bb2ts.put(b, buildTempSets(b));
	    Set liveOnExit = liveTemps.getLiveOnExit(b);
	    
	    alloc(b, liveOnExit);

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
	    while(blocks.hasNext()) {
		BasicBlock b = (BasicBlock) blocks.next();
		Set liveOnExit = liveTemps.getLiveOnExit(b);
		verify(b, liveOnExit, buildTempSets(b));
	    }	
	}

	Iterator remove = instrsToRemove.iterator();
	while(remove.hasNext()) {
	    Instr ir = (Instr) remove.next();
	    ir.remove();
	}
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

    Collection getRegs(Instr i, Temp t) {
	if (isTempRegister(t)) {
	    return Collections.singleton(t);
	} else if (code.registerAssigned(i,t)) {
	    return code.getRegisters(i, t);
	} else {
	    Util.assert(false, "getRegs false for "+i+" , "+t);
	    return null;
	}
    }
    
    /** Verify uses the inherent definitions of the instruction
	stream to check that the register uses of the allocated
	instructions are coherent.
    */
    class Verify extends harpoon.IR.Assem.InstrVisitor {
	RegFile regfile = new RegFile();
	final BasicBlock block;
	EqTempSets tempSets;
	Verify(BasicBlock b, EqTempSets eqts) { 
	    this.block = b; 
	    tempSets = eqts;
	}

	public void visit(final Instr i) {
	    visit(i, false);
	}

	public void visit(final InstrMOVE i) {
	    if (instrsToRemove.contains(i)) {
		List regs = regfile.getAssignment(i.use()[0]);
		Util.assert
		    (regs != null, 
		     new Object() {
			 public String toString() {
			     return "no reg assignment :\n"+
			     printInfo(block, i, i.use()[0], code);}});
		
		assign(i.def()[0], regs, true);
	    } else {
		visit((Instr)i);
	    }
	}
	
	public void visit(final Instr i, boolean multAssignAllowed) {
	    Iterator uses = i.useC().iterator();
	    while(uses.hasNext()) {
		Temp use_ = (Temp) uses.next();
		if (isTempRegister(use_)) continue;

		Collection cRegs = getRegs(i, use_);
		List fRegs = regfile.getAssignment(use_);

		/*		
		if (fRegs == null) {
		    use_ = tempSets.getRep(use_);
		    fRegs = regfile.getAssignment(use_);
		}
		*/

		final Collection codeRegs = cRegs;
		final List fileRegs = fRegs;
		
		final Temp use = use_;

		{ // ASSERTION CODE
		    Util.assert(codeRegs != null, "codeRegs!=null ");
		    Util.assert(fileRegs != null, 
				new Object() {
				    public String toString() {
					return "fileRegs!=null " + 
					printInfo(block, i, use, code)+
					"RF: "+regfile+"\n"+
					"TS: "+tempSets+"\n"+
					"PRE for " +use+ ": " +
					getPreassignFor(block).get(use); }});
		    Util.assert(!fileRegs.contains(null), "no null allowed in fileRegs");
		    Util.assert(!codeRegs.contains(null), "no null allowed in codeRegs");
		    Util.assert(codeRegs.containsAll(fileRegs),
				new Object() {
			public String toString() {
			    return printInfo(block, i, use, code)+
				"codeRegs incomplete; "+
				"c: "+codeRegs+" f: "+fileRegs; }});
		    Util.assert(fileRegs.containsAll(codeRegs),
				new Object() {
			public String toString() { 
			    return "fileRegs incomplete: "+
				"c: "+codeRegs+" f: "+fileRegs; }});
		} // END ASSERTION CODE
	    }
	    
	    Iterator defs = i.defC().iterator();
	    while(defs.hasNext()) {
		Temp def = (Temp) defs.next();
		// def = tempSets.getRep(def);
		Collection codeRegs = getRegs(i, def);
		Util.assert(codeRegs != null);
		assign(def, codeRegs, multAssignAllowed);
	    }
	    
	}
	
	public void visit(final InstrMEM i) {
	    if (i instanceof SpillLoad) {
		// regs <- temp
		assign(i.use()[0], i.defC());
	    } else if (i instanceof SpillStore) {
		// temp <- regs
		
		// this code is failing for an unknown reason; i
		// would think that the mapping should still be
		// present in the regfile.  taking it out for
		// now... 
		if (false) {
		    final Temp def = i.def()[0];
		    List fileRegs = regfile.getAssignment(def);
		    Collection storeRegs = i.useC();
		    Util.assert(fileRegs != null, 
				new Object(){
				    public String toString() {
					return "fileRegs!=null " + 
					printInfo(block, i, def, code);
				    }});
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
	    assign(def, c, false);
	}

	private void assign(Temp def, Collection c, boolean multAssignAllowed) {
	    if (!multAssignAllowed) {
		// getting around multiple-assignment checker...
		Map r2t = regfile.getRegToTemp();
		Iterator redefs = c.iterator();
		while(redefs.hasNext()) {
		    Temp t = (Temp) r2t.get(redefs.next());
		    if (regfile.hasAssignment(t))
			regfile.remove(t);
		}
	    }

	    regfile.assign(def, new ArrayList(c));
	}
    }
    
    private void verify(final BasicBlock block, final Set liveOnExit, 
			final EqTempSets eqt) {
	// includes SpillLoads and SpillStores...
	Iterator instrs = block.statements().iterator();
	Verify verify = new Verify(block, eqt);
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    i.accept(verify);
	}
	
    }
 
    // tracks a set of disjoint set of temps, and potentially
    // associates each set with a favored register temp (which itself
    // is not part of the set)
    abstract class EqTempSets {
	// Overview: an EqTempSets is a pair <S,M>, where 
	//     S is a set of disjoint sets of temps
	//     M is a mapping from disjoint sets to register temps.
	// each element of S is represented by one of its members,
	// called the Representative (or Rep for short).

	// maps set-representatives to the register associated with that
	// set.  Undefined for sets not associated with a register.
	protected HashMap repToReg = new HashMap();

	// effects: M_post = { getRep( <t> ) -> <reg> } + M
	public void associate(Temp t, Temp reg) {
	    repToReg.put(getRep(t), reg);
	}
	
	// effects: if <t> is a register temp, returns <t>.
	//          else if the set for <t> has an associated
	//                register <r>, returns <r>
	//          else returns null
	public Temp getReg(Temp t) {
	    if (isTempRegister(t)) 
		return t;
	    else {
		Temp rep = getRep(t);
		if (repToReg.containsKey(rep)) {
		    return (Temp) repToReg.get(rep);
		} else {
		    return null;
		}
	    }
	}

	// effects: if <t> is not a register, returns the set-rep for
	// the set containing <t>.  If <t> is a register, returns
	// <t>. 
	abstract Temp getRep(Temp t);

	// option operation that makes the sets in this immutable.
        void lock() { return; }

	// requires: <t1> or <t2> is not a register 
	// modifies: this
	// effects: puts <t1> and <t2> in the same equivalence class,
	//          unifying all the temps in the two equivalence
	//          classes for <t1> and <t2>
	//          unless:
	//     - one of the temps is a register and the equivalence
	//       set for the other temp already has a register 
	//       => no modification to this
        void add(Temp t1, Temp t2) {
	    Util.assert( (!isTempRegister(t1)) ||
			 (!isTempRegister(t2)) , 
			 t1 + " or " + t2 + " must be non-register");

	    Temp rep1 = getRep(t1);
	    Temp rep2 = getRep(t2);

	    if (isTempRegister(t1)) {
		if ( repToReg.containsKey(rep2) ) { 
		    return;
		} else { 
		    repToReg.put(rep2, t1); 
		}
	    } else if (isTempRegister(t2)) {
		if (repToReg.containsKey(rep1) ) { 
		    return;
		} else { 
		    repToReg.put(rep1, t2); 
		}
	    } else {
		union(t1, t2);
		
		Temp newRep = getRep(t1);
		
		if (repToReg.containsKey(rep1)) {
		    Temp reg = (Temp) repToReg.get(rep1);
		    repToReg.remove(rep1);
		    repToReg.put(newRep, reg);
		}
		if (repToReg.containsKey(rep2)) {
		    Temp reg = (Temp) repToReg.get(rep2);
		    repToReg.remove(rep2);
		    repToReg.put(newRep, reg);
		}
	    }
	}
	
	// requires: <t1> and <t2> are not registers
	// modifies: this
	// effects: unifies the equivalence classes for t1 and t2,
	//          removing the old equivalence sets and creating a
	//          new one whose rep is either t1 or t2.
	protected abstract void union(Temp t1, Temp t2);
	    
    }

    // (inefficient implementation with useful toString)
    class EqTempSets1 extends EqTempSets {
	private Map tempToSet = new HashMap();
	private Map setToRep = new HashMap();
	private List sets = new ArrayList();

	private boolean locked = false;

	void lock() {
	    tempToSet = Collections.unmodifiableMap(tempToSet);
	    setToRep = Collections.unmodifiableMap(setToRep);
	    sets = Collections.unmodifiableList(sets);
	    locked = true;
	}

	Temp getRep(Temp t) {
	    if (isTempRegister(t)) return t;
	    Set s = (Set) tempToSet.get(t);
	    if (s == null) {
		if (locked) {
		    if (setToRep.containsKey(t))
			return (Temp) setToRep.get(t);
		    else 
			return t;
		}
		s = new HashSet();
		sets.add(s);
		s.add(t);
		tempToSet.put(t, s);
		setToRep.put(s, t);
		return t;
	    } else {
		return (Temp) setToRep.get(s);
	    }
	}
	protected void union(Temp t1, Temp t2) {
	    Set s1 = (Set) tempToSet.get(t1);
	    if (s1 == null) s1 = new HashSet();
	    Set s2 = (Set) tempToSet.get(t2);
	    if (s2 == null) s2 = new HashSet();

	    HashSet s3 = new HashSet(s1);
	    s3.addAll(s2);
	    Iterator iter = new CombineIterator(s1.iterator(), s2.iterator());
	    while(iter.hasNext()) {
		Temp t = (Temp) iter.next();
		tempToSet.put(t, s3);
	    }
	    setToRep.remove(s1);  sets.remove(s1);
	    setToRep.remove(s2);  sets.remove(s2);
	    setToRep.put(s3, t1); sets.add(s3);
	}
	public String toString() {
	    StringBuffer sb = new StringBuffer();
	    Iterator iter = sets.iterator();	
	    int i=0;
	    while(iter.hasNext()) {
		i++;
		Set s = (Set) iter.next();
		sb.append("< Set"+i+": ");
		sb.append(s.toString());
		Object rep = setToRep.get(s);
		sb.append(", Rep:" + rep);
		sb.append(", Reg:" + repToReg.get(rep)+" >");
		if (iter.hasNext()) sb.append(" ");
	    }
	    return sb.toString();
	}
    }

    // efficient implementation that will replace instances of
    // EqTempSets1 once the code using EqTempSets1 has been debugged
    // completely and EqTempSets1.toString() is not needed. 
    class EqTempSets2 extends EqTempSets {
	private DisjointSet dss = new DisjointSet();

	Temp getRep(Temp t) {
	    if (isTempRegister(t))
		return t;
	    else 
		return (Temp) dss.find(t);
	}

	protected void union(Temp t1, Temp t2) {
	    dss.union(t1, t2);
	}
    }

    /** Constructs an EqTempSets for `b'. The equality operation used
	for the construction is 
	eq(t1, t2) = 
	if exists InstrMOVE: "t1 <- t2" in `b' then True else False
	
     */
    EqTempSets buildTempSets(BasicBlock b) {
	final EqTempSets tempSets = new EqTempSets1();
	class ScanForMove extends harpoon.IR.Assem.InstrVisitor {
	    public void visit(Instr i) { 
		/* do nothing */ 
	    }
	    public void visit(InstrMOVE i) {
		Temp d = i.def()[0], u = i.use()[0];
		if (!d.equals(u)) {
		    tempSets.add(d, u);
		}

		// strictly speaking, the above code WILL fail when
		// <d> and <u> are both registers.  But we don't seem
		// to encounter that in practice, since it could only
		// be the result of a hard coded mov from one register
		// to another.  Still, we should either change the
		// semantics of EqTempSets.add(Temp, Temp) or change
		// this code accordingly.
	    }
	}
	
	ScanForMove visit = new ScanForMove();
	
	List instrL = b.statements();
	ListIterator instrs = instrL.listIterator(instrL.size());
	while(instrs.hasPrevious()) {
	    Instr i = (Instr) instrs.previous();
	    i.accept(visit);
	    
	    
	}

	tempSets.lock();

	return tempSets;
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
	//  have a future reference; otherwise null)  
	final Map nextRef;

	// tracks sets of equivalent temps (eq. temps are ones that
	// are used in InstrMOVEs)
	final EqTempSets tempSets;

	// maps Temp:t -> Set of Regs 
	//      whose live regions interfere with t's live region
	final MultiMap preassignMap;

	LocalAllocator(BasicBlock b, Set lvOnExit) {
	    block = b;
	    liveOnExit = lvOnExit;
	    
	    nextRef = buildNextRef(b);
	    regfile = new RegFile();
	    evictables = new HashMap();

	    // Scan through Block, building up a EqTempSets based on
	    // moves 
	    tempSets = buildTempSets(block);

	    // System.out.println("TempSets: " + tempSets);
	    // System.out.println();
	    
	    preassignMap = 
		buildPreassignMap(block, 
				  frame.getRegFileInfo().getAllRegistersC(),
				  lvOnExit);
	    Util.assert(preassignMap != null);
	    putPreassign(block, preassignMap);
	    
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
	    

	    // doing a reverse iteration
	    ListIterator liter = bl.listIterator(bl.size());
	    while(liter.hasPrevious()) {
		Instr i = (Instr) liter.previous();

		// update mapping
		updateMapping(tempToRegs, liveTemps, liveRegs);
		
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
		    // (this impl. may be excessively inefficient
		    // though) 
		    if (!defRegs.isEmpty()) {
			updateMapping(tempToRegs, liveTemps, defRegs); 
			// if (defRegs.size() > 3) System.out.println("DEFupdate: " + defRegs + " for " + liveTemps);
		    }
		}

		// liveTemps: kill defs
		liveTemps.removeAll(i.defC());

		// liveTemps: add uses
		liveTemps.addAll(i.useC());

		{   // liveRegs: add (regs /\ uses)
		    Set useRegs = regSetFact.makeSet(allRegs);
		    useRegs.retainAll(i.useC());
		    liveRegs.addAll(useRegs);
		}

		if (setContainsR9(liveRegs)) 
		    throw new RuntimeException(printInfo(block, i, null, code));


	    }

	    // duplicate update mapping here to handle fencepost
	    // issues? 

	    return tempToRegs;
	}

	/** For each t elem liveTemps, adds liveRegs to the set of
	    regs that t conflicts with.
	*/
	private void updateMapping(MultiMap tempToRegs, Set liveTemps, 
				   Set liveRegs) {
	    if (liveRegs.isEmpty()) return;

	    // update mapping
	    Iterator titer = liveTemps.iterator();
	    while(titer.hasNext()) {
		Temp t = (Temp) titer.next();
		tempToRegs.addAll(t, liveRegs);
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

	    public void visit(Instr i) {
		Map putBackLater = takeOutUses(i);
		
		Iterator refs = 
		    new FilterIterator(getRefs(i), new MRegFilter()); 

		while(refs.hasNext()) {
		    Temp t = (Temp) refs.next();
		    assign(t, i, putBackLater);
		}
		
		Iterator defs = i.defC().iterator();
		while(defs.hasNext()) {
		    Temp def = (Temp) defs.next();
		    if (!isTempRegister(def)) regfile.writeTo(def);
		}
		
		evictables.putAll(putBackLater);

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
	    private Map takeOutUses(Instr i) {
		Map putBackLater = new HashMap();
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

	    /** Assigns `t' in `i'.
		modifies: `code', `evictables', `regfile', `putBackLater'
		effects: 
	           1. (`t' unassigned) => finds reg assignment for `t'
		   2. Updates structures to reflect assignment for `t'
		   3. (`t' used but not in regfile) => adds LOAD `t'
		   4. Puts `t' into `evictables', with updated Weight
	    */
	    private void assign(Temp t, Instr i, Map putBackLater) {

		if (regfile.hasAssignment(t)) {
		    
		    code.assignRegister(i, t, regfile.getAssignment(t));
		    evictables.remove(t); // (`t' reinserted later)

		} else { /* not already assigned */ 
		    
		    Set preassigns = addPreassignments(t);
		    Iterator suggs = getSuggestions(t, regfile, i, evictables);
		    List regList = chooseSuggestion(suggs, t); 

		    code.assignRegister(i, t, regList);
		    regfile.assign(t, regList);
			
		    if (i.useC().contains(t)) {
			InstrMEM load = new SpillLoad(i, "FSK-LOAD", regList, t);
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
		    putBackLater.put(t, X);
		} else { 
		    evictables.put(t, X); 
		}
	    }

	    /** Finds the weight of `t' when used at `i'.  
		requires: `t' is used by `i'
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
		
		if (regfile.isDirty(t)) {
		    X = new Integer(X.intValue() + 1);
		}
		
		return X;
	    }

	    /** Adds conflicting preassigned registers to `regfile'.
		modifies: `regfile'
		effects: 
		  let regs = values mapped to by `t' in `preassignMap'
		      tmps = new empty set of Temps 
		  in for each r in regs 
		         constructs a new PreassignTemp:p for r
			 assigns p to r in `regfile'
			 adds p to tmps
                     returns tmps
	    */
	    private Set addPreassignments(Temp t) {
		Collection preassigns = preassignMap.getValues(t);
		Set preassignTempSet = new HashSet();
		
		Util.assert(preassigns != null,
			    "preassignMap is missing mappings");
		Iterator preassignIter = preassigns.iterator();
		while(preassignIter.hasNext()) {
		    Temp reg = (Temp) preassignIter.next();
		    Temp preassign = new RegFileInfo.PreassignTemp(reg);
		    regfile.assign( preassign, 
				    Arrays.asList( new Temp[]{ reg }));
		    preassignTempSet.add(preassign);
		}

		return preassignTempSet;
	    }

	    
	    public void visit(final InstrMOVE i) {
		List ul = resolve(i.use()[0]);
		List dl = resolve(i.def()[0]);
		int choice = 0;
		
		if ( ul.equals(dl) &&
		     regfile.hasAssignment(i.use()[0]) &&
		     !regfile.hasAssignment(i.def()[0]) &&
		     !preassignMap.contains(i.def()[0], ul.get(0))) {
		    choice = 1;
		} else if (regfile.hasAssignment(i.use()[0]) &&
			   !(isTempRegister(i.def()[0]) ||
			     regfile.hasAssignment(i.def()[0])) &&
			   !isTempRegister(i.use()[0]) &&
			   !preassignMap.contains(i.def()[0], ul.get(0))) { 
		    choice = 2;
		} 
		
		if (choice != 0) {
		    List regList = regfile.getAssignment(i.use()[0]);
		    Util.assert
			(regList != null, 
			 new Object() {
			     public String toString() {
				 return 
				 printInfo(block, i, i.use()[0], code)+
				 "regList != null for "+i.use()[0];
			     }});
		    
		    code.assignRegister(i, i.use()[0], regList); // good?		    
		    code.assignRegister(i, i.def()[0], regList); // good?		    
		    remove(i, choice);

		    regfile.assign(i.def()[0], regList);
		    // System.out.println("assigning "+i.def()[0] +
		    //				       " to "+regList);
		} else {
		    visit((Instr) i);
		    return;
		}
	    }

	    private void remove(Instr i, int n) {
		remove(i, n, false);
	    }

	    private void remove(Instr i, int n, boolean pr) {
		instrsToRemove.add(i);
		if (pr) System.out.println("removing"+n+" "+i+" rf: "+regfile);
	    }

	    public List resolve(Temp t) {
		if (regfile.hasAssignment(t)) {
		    return regfile.getAssignment(t);
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
	private Iterator getSuggestions(Temp t, RegFile regfile, 
					final Instr i, Map evictables) {
	    if (false) System.out.println("getting suggestions for "+t+
			       " in "+i+" with regfile "+regfile+
			       " and evictables "+evictables);
	    
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


		    Set trackSpills = new HashSet();
		    while(spills.hasNext()) {
			boolean valid = true;
			Set cand = (Set) spills.next();
			
			trackSpills.add(cand);

			HashSet temps = new HashSet();
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
				    if (false) 
					System.out.println("aha "+preg+
							   " was disallowed");
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
				"\nneed at least one spill of \n"+
				trackSpills+"\n from "+evictables+
				"\nRegFile:"+regfile+"\n"+
				tempSets + "\n"+
				printInfo(block, i, t, code));

		    WeightedSet spill = (WeightedSet) weightedSpills.first();
		    if (false) System.out.println("for "+t+" in "+i+
				       " choosing to spill "+spill+
				       " of " + weightedSpills);
		    
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
				       i.getPrev(),
				       regfile);
			    evictables.remove(value);
			}
		    }
		}
	    }
	}
	
	private void precolorRegfile(BasicBlock b, RegFile regfile) {
	    Iterator instrs = b.statements().iterator();
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
	
	private void emptyRegFile(RegFile regfile, final Instr instr, 
				  Set liveOnExit) {
	    // System.out.println("live on exit from " + b + " :\n" + liveOnExit);
	    
	    // use a new HashSet here because we don't want to repeat values
	    // (regToTemp.values() returns a Collection-view)
	    Iterator vals = 
		(new HashSet(regfile.getRegToTemp().values())).iterator();
	    
	    while(vals.hasNext()) {
		final Temp val = (Temp) vals.next();
		
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
		
		// don't spill register only values
		if (isTempRegister(val)) {
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

		Instr loc; // was InstrEdge
		final Instr prev = instr.getPrev();
		if (!instr.defC().contains(val) &&
		    prev.getTargets().isEmpty()) {
		    Util.assert(prev.canFallThrough,
				new Object () {
			public String toString() {
			    return "control flow doesn't flow from "+
				prev + " to " + instr + " ; can't "+
				" insert spill\n"+
				printInfo(block, instr, val, code);
			}
		    });
		    // System.out.println("end spill: " + val + " " + loc);
		    
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
			    
			    addSpillInstr(val, instr, regfile);
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
			    addSpillInstr(val, loc, regfile);
			}

			regfile.remove(val);
		    }
		    // end of unneeded code block
		    
		    
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
		if (liveOnExit.contains(entry.getKey()))
		    nextRef.put(new TempInstrPair((Temp)entry.getKey(),
						  (Instr)entry.getValue()),
				INFINITY);
		
	    }
	    return nextRef;
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
	
	
	/** spills 'val', adding a store if necessary after 'loc' and updates
	    the 'regfile' so that it no longer has a mapping for 'val' or
	    its associated registers.
	*/
	private void spillValue(Temp val, Instr loc, RegFile regfile) {
	    if (regfile.isDirty(val)) {
		Util.assert(! (val instanceof RegFileInfo.PreassignTemp),
			    "cannot spill Preassigned Temps");
		addSpillInstr(val, loc, regfile);
	    }
	    regfile.remove(val);
	}
	
	/** adds a store for 'val' after 'loc', but does *NOT* update the
	    regfile. 
	*/
	private void addSpillInstr(Temp val, Instr loc, RegFile regfile) {
	    Collection regs = regfile.getAssignment(val);
	    Util.assert(regs != null, val+ " must have an assignment in "+
			"regfile to be spilled");
	    Util.assert(!regs.isEmpty(), 
			val + " must map to SOME registers" +
			"\n regfile:" + regfile);
	    
	    Util.assert(!isTempRegister(val), val+" should not be reg");
	    InstrMEM spillInstr = new SpillStore(loc, "FSK-STORE", val, regs);

	    spillStores.add(spillInstr);
	    spillStores.add(loc);
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

}
