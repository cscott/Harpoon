// RegAlloc.java, created Mon Mar 29 16:47:25 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Temp.Temp;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrVisitor;
import harpoon.IR.Properties.UseDef;
import harpoon.IR.Properties.HasEdges;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Code;
import harpoon.Analysis.UseMap;
import harpoon.Analysis.DataFlow.BasicBlock;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;
import harpoon.Util.LinearMap;


import java.util.Hashtable;
import java.util.Arrays;
import java.util.Set;
import java.util.Vector;
import java.util.List;
import java.util.ListIterator;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;


/**
 * <code>RegAlloc</code> performs Register Allocation for a set of
 * <code>Instr</code>s in a <code>Backend.Generic.Code</code>.  After
 * register allocation is completed for a set of <code>Instr</code>s,
 * the only references to non-register <code>Temp</code>s in the
 * <code>Instr</code>s will be <code>InstrMEM</code> instructions to
 * move values from the register file to data memory and vice-versa.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: RegAlloc.java,v 1.1.2.30 1999-08-25 23:48:16 pnkfelix Exp $ */
public abstract class RegAlloc  {
    
    protected Frame frame;
    protected Code code;
    protected BasicBlock rootBlock;

    private static String getSrcStr(int num) {
	String s = "`s0";
	for(int i=1; i<num; i++) {
	    s += ", `s"+i;
	}
	return s;
    }
    private static String getDstStr(int num) {
	String s = "`d0";
	for(int i=1; i<num; i++) {
	    s += ", `d"+i;
	}
	return s;
    }

    /** Class for <code>RegAlloc</code> usage in loading registers. 
	
	Note that the constructors automagically put in the
	"appropriate" `d# and `s# operands.

     */
    protected class FskLoad extends InstrMEM {
	FskLoad(InstrFactory inf, HCodeElement hce, 
		String assem, Temp dst, Temp src) {
	    super(inf, hce, assem + " `d0, `s0", 
		  new Temp[]{dst}, new Temp[]{src});
	}
	FskLoad(InstrFactory inf, HCodeElement hce, 
		String assem, List dsts, Temp src) {
	    super(inf, hce, assem + " " + 
		  getDstStr(dsts.size()) + ", `s0",
		  (Temp[])dsts.toArray(new Temp[dsts.size()]), 
		  new Temp[]{src});
	}
	// this is prolly bad (set order not specified) but its my
	// sketchy class and no one else should be using it anyway.  
	FskLoad(InstrFactory inf, HCodeElement hce,
		String assem, Set dsts, Temp src) {
	    super(inf, hce, assem + " " + 
		  getDstStr(dsts.size()) + ", `s0", 
		  (Temp[])dsts.toArray(new Temp[dsts.size()]), 
		  new Temp[]{src});
	}

    }

    /** Class for <code>RegAlloc</code> usage in spilling registers. 
	
	Note that the constructors automagically put in the
	"appropriate" `d# and `s# operands.

    */
    protected class FskStore extends InstrMEM {
	FskStore(InstrFactory inf, HCodeElement hce, 
		String assem, Temp dst, Temp src) {
	    super(inf, hce, assem, 
		  new Temp[]{dst}, new Temp[]{src});
	}
	FskStore(InstrFactory inf, HCodeElement hce, 
		String assem, Temp dst, List srcs) {
	    super(inf, hce, assem + " `d0, " +
		  getSrcStr(srcs.size()), 
		  new Temp[]{dst}, 
		  (Temp[])srcs.toArray(new Temp[srcs.size()]));
	}
	// this is prolly bad (set order not specified) but its my
	// sketchy class and no one else should be using it anyway.  
	FskStore(InstrFactory inf, HCodeElement hce,
		 String assem, Temp dst, Set srcs) {
	    super(inf, hce, assem + " `d0, " +
		  getSrcStr(srcs.size()),
		  new Temp[]{dst}, 
		  (Temp[])srcs.toArray(new Temp[srcs.size()]));
	}
    }

    /** Creates a <code>RegAlloc</code>. 
	
	<BR> <B>Design Issue:</B> should there be a RegAlloc object
	for every method, or just for every machine target?  For now
	it seems associating a new one with every method will save a
	lot of headaches.

    */
    protected RegAlloc(Code code) {
        this.frame = code.getFrame();
	this.code = code;
	HasEdges first = (HasEdges) code.getRootElement();
	rootBlock = BasicBlock.computeBasicBlocks(first);
    }
    
    /** Assigns registers in the code for <code>this</code>.
	
	<BR> <B>effects:</B> Partially or completely allocates
	     registers for the values defined and used in the code for
	     <code>this</code>.  Values will be preserved in the code;
	     any live value will be stored before its assigned
	     register is overwritten.  
	     <BR> Loads and Stores in general
	     are added in the form of <code>FskLoad</code>s and
	     <code>FskStore</code>s; the main <code>RegAlloc</code>
	     class will use <code>resolveOutstandingTemps(HCode</code> 
	     to replace these "fake" loads and stores with frame
	     specified Memory instructions.

	@see RegAlloc#resolveOutstandingTemps(HCode)
    */
    protected abstract Code generateRegAssignment();

    
    /** Returns the root of the <code>BasicBlock</code> hierarchy for
	the <code>Code</code> associated with <code>this</code>.
    */
    protected BasicBlock getBasicBlocks() {
	return rootBlock;
    }


    /** Creates a register-allocating <code>HCodeFactory</code> for
	"instr" form.
	<BR> <B>requires:</B> <code>parentFactory</code> produces code
	     in a derivative of "instr" form.
	<BR> <B>effects:</B> Produces an <code>HCodeFactory</code>
	     which allocates registers in the code produced by
	     <code>parentFactory</code> using the machine properties
	     specified in <code>frame</code>.

	<!-- (Design Note is (temporarily?) outdated; we've gone From
	     Smart To Dumb, and we're loving every minute of it.)

	     <BR> <B>DESIGN NOTE:</B> This method relies on the subclasses
	     of <code>RegAlloc</code> to perform actual allocation.
	     This causes a cycle in our module dependency graph,
	     which, while not strictly illegal, tends to be a sign of
	     a design flaw. Consider moving the code factory generator
	     out of the <code>RegAlloc</code> class into a seperate
	     class to get rid of the cycle.  In the meantime, any new
	     <code>RegAlloc</code> subclasses can be incorporated into
	     this method to be used in the compiler.  Perhaps should
	     also design a way to parameterize which
	     <code>RegAlloc</code> subclasses will be used. 
	  -->
     */
    public static HCodeFactory codeFactory(final HCodeFactory parentFactory, 
					   final Frame frame) {
	return new HCodeFactory() {
	    HCodeFactory parent = parentFactory;
	    Frame f = frame;
	    public HCode convert(HMethod m) {
		HCode preAllocCode = parent.convert(m);
		
		if (preAllocCode == null) {
		    return null;
		}

		RegAlloc localCode, globalCode;
		if (false) {
		    // very dumb (but correct) reg alloc
		    localCode = 
			new BrainDeadLocalAlloc((Code) preAllocCode);
		} else {
		    localCode = 
			new LocalCffRegAlloc((Code) preAllocCode);
		}

		if (true) {
		    // no global reg alloc
		    globalCode = localCode;
		} else {
		    /*
		      globalCode = 
			new DemandDrivenRegAlloc
			(frame, localCode.generateRegAssignment()); 
		    */
		}

		return globalCode.resolveOutstandingTemps
		    ( globalCode.generateRegAssignment() );
	    }
	    public String getCodeName() {
		return parent.getCodeName();
	    }
	    public void clear(HMethod m) {
		parent.clear(m);
	    }
	};
    }

    /** Transforms Temp references in 'in' into appropriate offsets
	from the Stack Pointer in the Memory. 
        <BR> <B>modifies:</B> in
	<BR> <B>effects:</B> Replaces the <code>FskLoad</code> and
	     <code>FskStore</code>s with memory instructions for the
	     appropriate <code>Frame</code>.
    */
    protected HCode resolveOutstandingTemps(HCode in) {
	// This implementation is REALLY braindead.  Fix to do a
	// smarter Graph-Coloring stack offset allocator

	Util.assert(in != null, "Don't try to resolve Temps for null HCodes");

	class TempFinder extends InstrVisitor {
	    HashMap tempsToOffsets = new HashMap();
	    int nextOffset = 1;

	    public void visit(FskLoad m) {
		// look for non-Register Temps in use, adding
		// them to internal map
		for(int i=0; i<m.use().length; i++){
		    if(!isTempRegister(m.use()[i]) &&
		       tempsToOffsets.get(m.use()[i])==null){
			tempsToOffsets.put
			    (m.use()[i], new Integer(nextOffset));
			nextOffset++;
		    }
		}
	    } 
	    public void visit(FskStore m) {
		// look for non-Register Temps in def, adding
		// them to internal map
		for(int i=0; i<m.def().length; i++){
		    if(!isTempRegister(m.def()[i]) &&
		       tempsToOffsets.get(m.def()[i])==null){
			tempsToOffsets.put
			    (m.def()[i], new Integer(nextOffset));
			nextOffset++;
		    }
		}
	    } 


	    public void visit(Instr m) {
		try { visit((FskStore) m); return;
		} catch(ClassCastException e) { }
		try { visit((FskLoad) m); return;
		} catch(ClassCastException e) { }
		

		// can no longer check Instr directly for registers,
		// because register association is done indirectly in
		// the Code now
	    }
	}
	
	// Not sure how to handle multiple Temp references in one
	// InstrMEM...for now will assume that there is only one
	// memory references per InstrMEM...
	class InstrReplacer extends InstrVisitor {
	    HashMap tempsToOffsets;
	    InstrReplacer(HashMap t2o) {
		tempsToOffsets = t2o; 
	    }
 	    
	    
	    // Make these SMARTER: get rid of requirement that Loads
	    // and Stores have only one references to memory (to
	    // allow for StrongARMs ldm* instructions : starting doing
	    // this, but I think TempFinder above relies on some parts
	    // of it, at least when assigning offsets.  Check over
	    // this. 
	    public void visitStore(FskStore m) {
		// replace all non-Register Temps with appropriate
		// stack offset locations
		Integer i = (Integer) tempsToOffsets.get(m.def()[0]);
		Util.assert(i != null, "tempsToOffsets should have a value for "+m.def()[0]);
		List instrs = frame.makeStore(Arrays.asList(m.use()), i.intValue(), m);

		// add a comment saying which temp is being stored
		Instr first = (Instr) instrs.get(0);
		Instr.replaceInstrList(m, instrs);		
		Instr.insertInstrBefore(first, 
					new Instr(first.getFactory(),
						  first,
						  "\t@storing " + m.def()[0],
						  null, null));
	    }
	    
	    public void visitLoad(FskLoad m) {
		// replace all non-Register Temps with appropriate
		// stack offset locations
		Integer i = (Integer) tempsToOffsets.get(m.use()[0]);
		Util.assert(i != null, "tempsToOffsets should have a value for "+m.use()[0]);
		List instrs = frame.makeLoad(Arrays.asList(m.def()), i.intValue(), m);

		// add a comment saying which temp is being loaded
		Instr first = (Instr) instrs.get(0);
		Instr.replaceInstrList(m, instrs);
		Instr.insertInstrBefore(first, 
					new Instr(first.getFactory(),
						  first,
						  "\t@loading " + m.use()[0],
						  null, null));
	    }
	    
	    public void visit(Instr i) {
		// do nothing 
	    }

	    public void visit(InstrMEM i) {
		Util.assert(i != null, "InstrMEM should not be null");
		try { visitStore((FskStore) i); return; 
		} catch(ClassCastException e) { }
		try { visitLoad((FskLoad) i); return;
		} catch(ClassCastException e) { }
		    //Do nothing
	    }
	
	}

	TempFinder tf = new TempFinder();
	Iterator instrs = in.getElementsI();
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    i.visit(tf);
	}
	// now tf should have a full map of Temps to needed Stack
	// Offsets.

	// System.out.println("TempsToOffsets Mapping: " + tf.tempsToOffsets);

	InstrReplacer ir = new InstrReplacer(tf.tempsToOffsets);
	instrs = in.getElementsI();
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    i.visit(ir);
	}

	return in;
    }
    

    /** Checks if <code>t</code> is a register (Helper method).
	<BR> <B>effects:</B> If <code>t</code> is a register for the
	     <code>frame</code> associated with <code>this</code>,
	     then returns true.  Else returns false.   
    */ 
    protected boolean isTempRegister(Temp t) {
	return frame.isRegister(t);
        
	// Temp[] allRegs = frame.getAllRegisters();
	// boolean itIs = false;
	// for (int i=0; i < allRegs.length; i++) {
	//    if (t.equals(allRegs[i])) {
	//	itIs = true;
	//	break;
	//    }
	// }
	// return itIs;
    }

    /** Checks if <code>i</code> is last use of <code>reg</code> in
	the block of instructions lists in <code>iter</code>.  
	
	<BR> <B>requires:</B> 
	     <BR> 1. <code>i</code> is an element in <code>iter</code> 
	     <BR> 2. <code>iter</code> is an <code>Iterator</code> of
	             a linear series of <code>Instr</code>s in the
		     order that they will be executed.
	     <BR> 3. <code>iter</code> is currently indexed at
	             <code>i</code> 
	<BR> <B>modifies:</B> <code>iter</code>
	<BR> <B>effects:</B> 
             <BR> 1. Returns true if no instruction after
	             <code>i</code> in <code>iter</code> uses
		     <code>reg</code> before <code>reg</code> is
		     redefined (<code>i</code> redefining
		     <code>reg</code> is sufficient).  Else returns
		     false. 
	     <BR> 2. <code>iter</code> is left at an undetermined
	             index. 
    */
    protected static boolean lastUse(Temp reg, UseDef i, Iterator iter) {
	UseDef curr = i;
	boolean r = true;
	while (iter.hasNext() && ! contained( curr.def(), reg ) ) {
	    curr = (UseDef) iter.next();
	    if (contained( curr.use(), reg )) {
		r = false;
		break;
	    }
	}
	return r;
    } 

    private static boolean contained(Object[] array, Object o) {
	boolean yes = false;
	for (int i=0; i<array.length; i++) {
	    if (array[i] == o) {
		yes = true;
		break;
	    }
	}
	return yes;
    }

}

class BrainDeadLocalAlloc extends RegAlloc {
    BrainDeadLocalAlloc(Code code) {
	super(code);
    }
	
    class BrainDeadInstrVisitor extends InstrVisitor {
	Temp[] regs = frame.getGeneralRegisters();
		
	public void visit(Instr instr) {
	    InstrFactory inf = instr.getFactory();
	    
	    try {
		// in this (dumb) model, each instruction will
		// load all uses and store all defs, so we can
		// treat the register file as being empty for each
		// instruction
		
		Map regFile = new LinearMap();

		// load srcs
		for(int i=0; i<instr.use().length; i++) {
		    Temp preg = instr.use()[i];
		    if (!isTempRegister(preg)) {
			Iterator iter =
			    frame.suggestRegAssignment(preg, regFile); 
			List regList = (List) iter.next();
			InstrMEM loadSrcs = 
			    new FskLoad(inf, null, "FSK-LOAD", 
					regList, preg); 
			Instr.insertInstrBefore(instr, loadSrcs);
			code.assignRegister(instr, preg, regList);
			Iterator regIter = regList.iterator();
			while(regIter.hasNext()) {
			    Temp r = (Temp) regIter.next();
			    regFile.put(r, preg);
			}
		    }
		}
		// store dsts
		for(int i=0; i<instr.def().length; i++) {
		    Temp preg = instr.def()[i];
		    if(!isTempRegister(preg)) {
			Iterator iter =
			    frame.suggestRegAssignment(preg, regFile); 
			List regList = (List) iter.next();
			InstrMEM storeDsts = 
			    new FskStore(inf, null, "FSK-STORE",
					 preg, regList);
			Instr.insertInstrAfter(instr, storeDsts);
			// I'm not certain this code will handle "add
			// t0, t0, t1" properly 
			code.assignRegister(instr, preg, regList);
			Iterator regIter = regList.iterator();
			while(regIter.hasNext()) {
			    Temp r = (Temp) regIter.next();
			    regFile.put(r, preg);
			}
		    }
		}
	    } catch (Frame.SpillException e) {
		// actually...this doesn't necessarily imply that we
		// have to fail; if a TwoWordTemp can't be fitted, we
		// could potentially shift the register files contents
		// around to make room for it.   While we STILL
		// shouldn't ever encounter this problem (after all,
		// we have an empty register file and usually at most
		// three pseudo registers to assign (potentially six
		// though, with double worded operands)) I should give
		// some thought on how to handle this case 
		
		Util.assert(false, "Either a TwoWordTemp screwed us, or "+
			    "One Instr uses/defines more "+
			    "registers than "+frame+" can accomidate "+
			    "in Register File!"); 
	    }
	}
	
    }
    
    /** For each instruction:
	1. Load every use from memory into the register file.
	2. Execute the instruction
	3. Store every dest from the register file
	regFile will be clean in between each instruction in this
	(very dumb) allocation strategy. 
    */
    protected Code generateRegAssignment() {
	Iterator instrs = code.getElementsI();
	InstrVisitor memVisitor = new BrainDeadInstrVisitor();
	
	while(instrs.hasNext()) {
	    ((Instr)instrs.next()).visit(memVisitor);
	}
	
	return code;
    }
}
