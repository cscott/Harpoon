// LocalRegAlloc.java, created Thu Apr  8 01:02:19 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Code;
import harpoon.Analysis.DataFlow.BasicBlock;
import harpoon.Analysis.DataFlow.DataFlowBasicBlockVisitor;
import harpoon.Analysis.DataFlow.InstrSolver;
import harpoon.Analysis.DataFlow.LiveVars;
import harpoon.Temp.Temp;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Assem.InstrMEM;
import harpoon.Util.Util;
import harpoon.Util.WorkSet;
import harpoon.Util.UniqueVector;
import harpoon.Util.CloneableIterator;
import harpoon.Util.CombineIterator;
import harpoon.Util.MaxPriorityQueue;
import harpoon.Util.BinHeapPriorityQueue;

import java.util.Set;
import java.util.Collection;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Vector;
import java.util.Stack;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Iterator;

/** <code>LocalRegAlloc</code> performs Local Register Allocation for
    a given set of <code>Instr</code>s.  It uses the
    conservative-furthest-first algorithm laid out in the paper <A
    HREF="http://lm.lcs.mit.edu/~pnkfelix/OnLocalRegAlloc.ps.gz">
    "On Local Register Allocation"</A> and <A
    HREF="http://lm.lcs.mit.edu/~pnkfelix/hardnessLRA.ps">"Hardness and
    Algorithms for Local Register Allocation"</A> as the basis for the
    algorithm it uses to allocate and assign registers.
    
    @author  Felix S Klock <pnkfelix@mit.edu>
    @version $Id: LocalCffRegAlloc.java,v 1.1.2.16 1999-06-14 07:12:05 pnkfelix Exp $ 
*/
public class LocalCffRegAlloc extends RegAlloc {

    private static final boolean DEBUG = false;
    
    /** Creates a <code>LocalRegAlloc</code>. 
	
    */
    public LocalCffRegAlloc(Frame frame, Code code) {
        super(frame, code);
    }

    /** Assigns registers in the code for <code>this</code>.
	
	<BR> <B>effects:</B> Locally allocates registers for the
	     values defined and used in the basic blocks of the code
	     in <code>this</code>.  Values will be preserved in the
	     code; all used values are loaded at the begining of the
	     basic block they're used in, and all modified values are
	     stored at the end of the basic block they're used in.
    */
    protected Code generateRegAssignment() {
	Instr root = (Instr) code.getRootElement();
	BasicBlock block = BasicBlock.computeBasicBlocks(root);
	
	// first calculate Live Variables for code
	CloneableIterator iter = 
	    new CloneableIterator(BasicBlock.basicBlockIterator(block));
	LiveVars livevars=null;
	livevars =  new LiveVars((Iterator)iter.clone());
	InstrSolver.worklistSolver(block, livevars);
	
	// Now perform local reg alloc on each basic block
	while(iter.hasNext()) {
	    BasicBlock b = (BasicBlock) iter.next();
	    localRegAlloc(b, livevars);
	}
	return code;
    }

    /** Performs local register allocation for <code>bb</code>. 
	<BR> <B>requires:</B> 
	     <BR>1. <code>bb</code> is a <code>BasicBlock</code> of
	            <code>Instr</code>s.
	<BR> <B>modifies:</B> <code>Instr</code>s in <code>bb</code>
	<BR> <B>effects:</B> Analyzes the <code>Instr</code>s in
	     <code>bb</code> and inserts LOAD and STORE 
	     <code>InstrMEM</code> objects at appropriate locations in
	     <code>bb</code> to ensure that 
	     <BR> 1. All <code>Temp</code>s in
	             non-<code>InstrMEM</code> objects are
		     register-<code>Temp</code>s
	     <BR> 2. All needed <code>Temp</code>s are loaded from
	             memory into registers prior to use
	     <BR> 3. All modified <code>Temp</code>s are stored to
	             memory with their final values (though
		     intermediate values may not be propagated to
		     memory)
	     <B>NOTE:</B> Current BasicBlock implementation does not
	                  support modification of the underlying
			  instruction stream while the BasicBlock is
			  in use.  Therefore, after calling this
			  method the caller should throw away 'bb' and
			  construct a new BasicBlock.

    */
    private void localRegAlloc(BasicBlock bb, LiveVars lv) {
	// The following code assumes that our Temp objects are
	// effectively the same as pseudo-registers (definition
	// below).  If this is not true, then the code is probably
	// broken.

	// Definition of pseudo-register: Pseudo-registers contain
	// Temporary Values and Constants.  No aliasing between
	// pseudo-registers is possible.  A pseudo-register is defined
	// at most once, and thus represents only one live range of a
	// variable.  Register allocation assigns pseudo-registers to
	// a set of N registers.

	CloneableIterator instrs = 
	    new CloneableIterator(bb.listIterator());

	InstrFactory inf = null;

	// Store all new memory instructions in memInstrs, then add
	// them at the end.
	class InstrAdditionMap {
	    
	    // stacks[0]: Maps Instr 'i' -> Stack[Instr] to be added
	    //            before 'i'
	    // stacks[1]: Maps Instr 'i' -> Stack[Instr] to be added
	    //            after 'i'
	    private HashMap[] stacks = new HashMap[2];
	    InstrAdditionMap() {
		stacks[0] = new HashMap();
		stacks[1] = new HashMap();
	    }
	    
	    /** Returns a Stack of Instrs to be executed before 'i'.
	     */
	    Stack getPrior(Instr i) {
		Stack pre = (Stack) stacks[0].get(i);
		if (pre == null) {
		    pre = new Stack();
		    stacks[0].put(i, pre);
		}
		return pre;
	    }
	    /** Returns a Stack of Instrs to be executed after 'i' */
	    Stack getSucceeding(Instr i) {
		Stack succ = (Stack) stacks[1].get(i);
		if (succ == null) {
		    succ = new Stack();
		    stacks[1].put(i, succ);
		}
		return succ;
	    }

	    /** Updates all of the Instrs in this, adding all of the
		instructions in 'stacks' according to whether they
		were put in stacks[0] or stacks[1]
	    */
	    void updateInstrs() {
		// add priors
		HashMap map = stacks[0];
		Iterator keys = map.keySet().iterator();
		while(keys.hasNext()) {
		    Instr k = (Instr) keys.next();
		    Stack s = (Stack) map.get(k);
		    while(!s.empty()) {
			Instr n = (Instr) s.pop();
			Instr.insertInstrBefore(k, n);
		    }
		}
		// add successors
		map = stacks[1];
		keys = map.keySet().iterator();
		while(keys.hasNext()) {
		    Instr k = (Instr) keys.next();
		    Stack s = (Stack) map.get(k);
		    while(!s.empty()) {
			Instr n = (Instr) s.pop();
			Instr.insertInstrAfter(k, n);
		    }
		}
	    }			
	}

	// memInstrs: Delays modification of the BasicBlock until
	// AFTER analysis of the BasicBlock is complete 
	InstrAdditionMap memInstrs = new InstrAdditionMap();

	// nextRef: Maps a (Instr x Value) -> Distance
	// NOTE: I multiply all values stored into next-ref
	// by 2, because the algorithm calls for adding 1/2 to a value
	// in next-ref and it seems silly to use floating point
	// numbers.  
	HashMap nextRef = new HashMap(); 


	RegToValueMap regFile = new RegToValueMap();
	if (DEBUG) System.out.println(regFile);
	MaxPriorityQueue pregPriQueue = new BinHeapPriorityQueue();

	// for all j's and l's do
	//     next-ref(j, l) := the next step with a reference to the
	//                       l'th pseudo-register requested at
	//                       step j 
	// done
	CloneableIterator jnstrs = (CloneableIterator) instrs.clone();
	int step = 0;
	while (jnstrs.hasNext()) {
	    Instr j = (Instr) jnstrs.next();
	    step++;
	    //foreach l, elem pseudo-registers requested at j	    
	    Iterator references = getReferences(j);
	    while (references.hasNext()) {
		Temp l = (Temp) references.next();
		if (!regFile.isRegister(l)) { // just pseudo-regs in nextRef
		    CloneableIterator search = 
			(CloneableIterator) instrs.clone();
		    while (search.hasNext()) {
			Instr jprime = (Instr) search.next();
			// hasRef: jprime has a reference to l
			boolean hasRef=false;
			Iterator jPRefs = getReferences(jprime);
			while (jPRefs.hasNext()) {
			    if (l.equals(jPRefs.next())) {
				hasRef = true; break;
			    }
			}
			if (hasRef) { 
			    nextRef.put(new TempInstrPair(l, j), 
					new Integer(2*step));
			    break;
			}
		    }
		}
	    }
	}
	

	// for j = 1 to r do
	jnstrs = (CloneableIterator) instrs.clone();
	while (jnstrs.hasNext()) {
	    // f := the number of pseudo registers requested at step
	    //      j, but not in the register file
	    Instr j = (Instr) jnstrs.next();
	    inf = j.getFactory();
	    Iterator references = getReferences(j);
	    UniqueVector refsV = new UniqueVector();
	    while(references.hasNext()) {
		Temp t = (Temp) references.next();
		if (regFile.isRegister(t)) {
		    refsV.addElement(t);
		}
	    }
	    int f = refsV.size();
	    if (DEBUG) System.out.print("Initial f : " + f + " ");

	    for (int i=0; i<refsV.size(); i++) {
		if (regFile.containsValue( (Temp) refsV.elementAt(i) ) ) {
		    f--;
		}
	    }

	    if (DEBUG) System.out.print("Middle f : " + f + " ");
	    
	    // f := f - (the number of empty registers) 
	    // (we get around subtracting the number of registers
	    // taken by dead pseudo-registers by making the distance
	    // in next-ref for dead values effectively infinite.)
	    f = f - regFile.numEmptyRegisters(); 
	    
	    if (DEBUG) System.out.println( "Final f : " + f);

	    // for l = 1 to f do
	    //     evict the pseudo-register DEL-MAX()
	    // done
	    for (int l = 0; l<f; l++) {
		
		Temp preg = (Temp) pregPriQueue.deleteMax();
		boolean dirty = regFile.isDirty(preg);
		Temp realReg = regFile.evict(preg);
		
		if (dirty) {
		    InstrMEM store = 
			new InstrMEM(inf, null, "FSK-STORE `d0, `s0", // change string to null later
				     new Temp[] { preg },
				     new Temp[] { realReg });

		    memInstrs.getPrior(j).push(store);
		}

		if (DEBUG) System.out.println("EVICT: Instr " + j + " " + regFile);
	    }
	    

	    // for l = 1 to the number of pseudo-registers read
	    //                  at step j do
	    
	    for (int l=0; l<j.use().length; l++) {
		// i := l'th pseudo-registers requested at step j
		Temp i = j.use()[l];

		// check that 'i' is a pseudo register (not a REAL register)
		if (regFile.isRegister(i)) continue;

		// if i is not in a register then load i else DELETE(i) 
		if (regFile.values().contains(i)) {
		    pregPriQueue.remove(i); // we need to DELETE(i)
		    // from the priority queue so we can INSERT it
		    // again later with an updated distance to its
		    // next use.
		} else {
		    Temp reg = regFile.getEmptyRegister();
		    Util.assert(reg != null, 
				"There should be an empty register in " + 
				regFile);
		    regFile.put(reg, i);
		    
		    InstrMEM load =
			new InstrMEM(inf, null, "FSK-LOAD `d0, `s0", // change string to null later
				     new Temp[] { reg },
				     new Temp[] { i });
		    
		    memInstrs.getPrior(j).push(load);

		    // TODO: replace the following for loops with some
		    // sort of cloning Temp Map in the instruction
		    // perhaps? 
		    for (int ji = 0; ji < j.dst.length; ji++) {
			if (j.dst[ji] == i) {
			    j.dst[ji] = reg;
			}
			regFile.writeTo(j.dst[ji]); // set DIRTY bit
		    }
		    for (int ji = 0; ji < j.src.length; ji++) {
			if (j.src[ji] == i) {
			    j.src[ji] = reg;
			}
		    }

		    // visit all the remaining instructions,
		    // replacing 'i' with 'reg'
		    CloneableIterator remainInstrs =
			(CloneableIterator) jnstrs.clone();
		    while (remainInstrs.hasNext()) {
			Instr instr = (Instr) remainInstrs.next();
			// by above def of pseudo-Register, we only
			// have to replace USEs in subsequent
			// instructions with the Register value; DEFs
			// are guaranteed to have new Temps associated
			// with them
			for (int ind=0; ind<instr.src.length; ind++) {
			    if (instr.src[ind].equals(i)) {
				instr.src[ind] = reg;
			    }
			}
		    }
		    

		    if (DEBUG) System.out.println("PUT: Instr " + j + " " + regFile);
		}

		// x := next-ref(j, l)
		Integer intgr = (Integer) nextRef.get(new TempInstrPair(j, i));

		Util.assert(intgr != null, "At any USE of temp i in instr j, "+
			    "there should be an entry in next-bref");
		
		// below check is old and should be unnecessary.  But WTF
		int x = (intgr==null)?Integer.MAX_VALUE-1:intgr.intValue();
		
		// if i is dirty then x := x + 1/2 end if

		// if (regFile.isDirty(i)) {
		if (isDirty(i, j, bb)) { 
		    // add 1 instead of 1/2 (all values in
		    // nextRef are doubled, see note above.
		    x = x + 1; 
		}
		
		// INSERT(i, x)
		pregPriQueue.insert(i, x);
		
	    }//done
	}//done
	
	// Now need to append a series of STOREs to the BasicBlock (so
	// that Temp locations in memory are updated

	Instr instr = (Instr) bb.getLast();

	for (int i=0; i<regFile.regs.length; i++) {
	    Temp val = regFile.get(regFile.regs[i]);
	    if (val != null && regFile.isDirty(val)) {
		InstrMEM store = 
		    new InstrMEM(inf, null, "FSK-STORE `d0, `s0", // change string to null later
				 new Temp[] { val },
				 new Temp[] { regFile.regs[i] });
		
		memInstrs.getSucceeding(instr).push(store);
	    }
	}

	// now analysis has completed, update the instruction stream.
	memInstrs.updateInstrs();
    } // end localRegAlloc(BasicBlock, LiveVars)


    private boolean isDirty(Temp value, Instr instr, BasicBlock bb) {
	boolean dirty = false;
	Iterator pastInstrs = bb.listIterator();
	while(pastInstrs.hasNext()) {
	    Instr i = (Instr) pastInstrs.next();
	    Util.assert(i != null, "Iterator.next() should never return null");
	    if (i == instr) { // only iterate up to 'instr'
		break;
	    } else {
		Temp[] def = i.def();
		for (int index=0; index<def.length; index++) {
		    Util.assert(def[index] != null, 
				"Temp[] returned by " + i + " .def() " + 
				"should not contain null");
		    if (def[index].equals(value)) {
			dirty = true;
		    }
		}
	    }
	}
	return dirty;
    }
    
    /** Returns <code>true</code> if <code>value</code> is alive. 
	<BR> <B>requires:</B> 
	     1. <code>instrs</code> corresponds to a sublist of the
	        instructions in <code>bb</code> from some point in
		<code>bb</code> to the end of <code>bb</code> 
	     2. <code>lv</code> has an entry for <code>bb</code>
	<BR> <B>effects:</B> Returns <code>true</code> if
	     <code>value</code> is referenced in <code>instrs</code>
	     or if <code>value</code> is live-on-exit according to
	     <code>lv</code>'s analysis of <code>bb</code>.
     */
    private boolean stillAlive(Temp value, CloneableIterator instrs, 
			       BasicBlock bb, LiveVars lv) {
	Set liveOnExit = lv.getLiveOnExit(bb);
	if (liveOnExit.contains(value)) {
	    return true;
	} else {
	    // may still be live; check future instruction
	    // references
	    CloneableIterator futureInstrs = (CloneableIterator) instrs.clone();
	    while (futureInstrs.hasNext()) {
		Instr future = (Instr) futureInstrs.next();
		Iterator frefs = getReferences(future);
		while(frefs.hasNext()) {
		    Temp ftemp = (Temp) frefs.next();
		    if (ftemp.equals(value)) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

    /** Returns an <code>Iterator</code> of <code>Temp</code>s
	representing the references made in <code>i</code>.
	Repeated references to the same variable are repeated in
	the iteration.
    */
    private Iterator getReferences(Instr i) {
	// the below object may be better suited (more efficient) but
	// I'm not certain of its correctness and at this point I
	// don't want to waste time debugging it.
	class RefIterator {
	    private Instr instr;
	    private Temp next;
	    private int index;
	    private boolean onDef;
	    RefIterator(Instr j) {
		instr = j;
		if (instr.def().length != 0) {
		    next = instr.def()[0];
		    index=0;
		    onDef=true;
		} else if (instr.use().length != 0) {
		    next = instr.use()[0];
		    index=0;
		    onDef=false;
		} else {
		    next = null;
		}
	    }
	    public boolean hasNext() {
		return (next != null);
	    }
	    public Object next() {
		Object rtrn = null;
		if (next == null) {
		    throw new NoSuchElementException();
		} else {
		    rtrn = next;
		    index++;
		    if (onDef) {
			// bounds check
			if (index == instr.def().length) {
			    if (instr.use().length != 0) {
				index = 0;
				onDef = false;
				next = instr.use()[index];
			    } else {
				next = null;
			    }
			} else {
			    next = instr.def()[index];
			}
		    } else { // onUse
			// bounds check
			if (index == instr.use().length) {
			    next = null; // no more refs
			} else {
			    next = instr.use()[index];
			}
		    }
		}
		return rtrn;
	    }
	    public void remove() {
		throw new UnsupportedOperationException();
	    } 
	}


	// return new RefIterator(j);
	return new 
	    CombineIterator
	    ( new Iterator[] 
	      { Arrays.asList( i.def() ).iterator() ,
		    Arrays.asList( i.use() ).iterator() } );

    }

    class RegToValueMap {
	// if 'REGISTER( i )' (which is stored in regs[ i ] ) is holding
	// a value, vals[ i ] will have that value.  Else, vals[ i ]
	// will be set to null.
	Temp[] regs;
	Temp[] vals;
	boolean[] dirty; // tracks dirty bit for regs


	/** Constructs a new RegToValueMap, with all of the registers
	    mapped to no value.
	*/
	public RegToValueMap() {
	    regs = frame.getGeneralRegisters();
	    vals = new Temp[ regs.length ];
	    dirty = new boolean[ regs.length ]; 
	}
	
	/** Returns the value associated with <code>reg</code>
	    <BR> <B> requires: </B> <code>reg</code> is one of the
	         General Registers for the frame of the outer
		 <code>LocalCffRegAlloc</code>.
	    <BR> <B> effects: </B> returns the <code>Temp</code> value
	         currently associated with <code>reg</code> or
		 <code>null</code> if no <code>Temp</code> is
		 associated with <code>reg</code>
	*/
	public Temp get(Temp reg) {
	    for (int i=0; i<regs.length; i++) {
		if (regs[i].equals(reg)) {
		    return vals[i];
		}
	    }
	    return null;
	}
	
	/** Associates <code>reg</code> with <code>value</code>.
	    <BR> <B> requires: </B> <code>reg</code> is one of the
	         General Registers for the frame of the outer
		 <code>LocalCffRegAlloc</code>.
	    <BR> <B> effects: </B> Associates <code>reg</code> to
	         <code>value</code>, discarding any previous
		 association of <code>reg</code> to some
		 <code>Temp</code> 
	*/
	public void put(Temp reg, Temp value) {
	    for (int i=0; i<regs.length; i++) {
		if (regs[i].equals(reg)) {
		    vals[i] = value;
		    dirty[i] = false;
		}
	    }
	}
	
	/** Sets <code>t</code> as "dirty".
	 */
	public void writeTo(Temp t) {
	    for (int i=0; i<regs.length; i++) {
		if (vals[i] == t || regs[i] == t) {
		    dirty[i] = true;
		}
	    }
	}
	    
	/** Identifies <code>t</code> as "clean" or "dirty".
	    <BR> <B>requires:</B> <code>t</code> is present either in
	         <code>this.regs</code> or <code>this.vals</code>  
	    <BR> <B>effects:</B> Searches for the index <code>i</code>
	         of <code>t</code> in <code>this.regs</code> and 
		 <code>this.vals</code>, and returns
		 <code>this.dirty[i]</code> 
	*/
	public boolean isDirty(Temp t) {
	    for (int i=0; i<regs.length; i++) {
		if ((vals[i] != null && vals[i].equals(t)) || 
		    (regs[i] != null && regs[i].equals(t))) {
		    return dirty[i];
		}
	    }
	    Util.assert(false, "RegToValueMap.isDirty(t) requires " + 
			"that 't' is currently in the map");
	    return false; 
	}

	/** Identifies <code>t</code> as a register or memory
	    location.
	*/
	public boolean isRegister(Temp t) {
	    for (int i=0; i<regs.length; i++) {
		if (regs[i].equals(t)) {
		    return true;
		}
	    }
	    return false;
	}
		    

	/** Evicts <code>value</code> from <code>this</code>.
	    <BR> <B>requires:</B> <code>value</code> is mapped to by
	         some key in <code>this</code>
	    <BR> <B>effects:</B> finds the <code>Temp</code> key
	         mapping to <code>value</code>, maps the key to no
		 value, and returns the key.
	*/
	    public Temp evict(Temp value) {
	    for (int i=0; i<vals.length; i++) {
		if (vals[i] != null &&
		    vals[i].equals(value)) {
		    vals[i] = null;
		    dirty[i] = false;
		    return regs[i];
		}
	    }
	    return null;
	}
	

	/** Returns a <code>Temp</code> representing a register
	    suitable for storing a value in. 
	    <BR> <B> effects: </B> Returns a <code>Temp</code> that is
	    mapped to <code>null</code> in <code>this</code>, or
	    <code>null</code> if no such <code>Temp</code> exists.
	*/
	public Temp getEmptyRegister() {
	    for (int i=0; i<regs.length; i++) {
		if (vals[i] == null) {
		    return regs[i];
		}
	    }
	    return null;
	}
	
	/** Returns true if <code>this</code> contains some register
	    that maps to <code>value</code>.
	*/
	public boolean containsValue(Temp value) {
	    for (int i=0; i<vals.length; i++) {
		if (vals[i] != null &&
		    vals[i].equals(value)) {
		    return true;
		}
	    }
	    return false;
	}
	
	/** Returns the number of registers in <code>this</code> that
	    map to no value.
	*/
	public int numEmptyRegisters() {
	    int count = 0;
	    for (int i=0; i<vals.length; i++) {
		if (vals[i] == null) {
		    count++;
		}
	    }
	    return count;
	}
	
	/** Returns a collection view of the values contained in this
	    map.
	*/
	public Collection values() {
	    Vector v = new Vector();
	    for(int i=0; i<vals.length; i++) {
		if (vals[i] != null) v.add(vals[i]);
	    }
	    return v;
	}

	public String toString() {
	    String s = "RegFile[ ";
	    for (int i=0; i<regs.length; i++) {
		s += "{ " + vals[i] + 
		    (dirty[i]?"Dirty":"Clean") + " } ";
	        // if (i == regs.length/2) s += "\n";
	    }
	    s += "]";
	    return s;
	}

    }

}





