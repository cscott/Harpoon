// RegAlloc.java, created Mon Mar 29 16:47:25 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Temp.Temp;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrEdge;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrVisitor;
import harpoon.IR.Properties.UseDef;
import harpoon.IR.Properties.CFGraphable;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Code;
import harpoon.Backend.Generic.RegFileInfo;
import harpoon.Backend.Generic.RegFileInfo.SpillException;
import harpoon.Backend.Generic.InstrBuilder;
import harpoon.Analysis.UseMap;
import harpoon.Analysis.BasicBlock;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.Util.Util;
import harpoon.Util.LinearMap;
import harpoon.Util.Collections.MultiMap;
import harpoon.Util.Collections.DefaultMultiMap;



import harpoon.Analysis.DataFlow.ReachingDefs;
import harpoon.Analysis.DataFlow.ForwardDataFlowBasicBlockVisitor;
import harpoon.Analysis.DataFlow.InstrSolver;

import java.util.Hashtable;
import java.util.Arrays;
import java.util.Collection;
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
 * @version $Id: RegAlloc.java,v 1.1.2.51 1999-11-30 05:24:45 cananian Exp $ */
public abstract class RegAlloc  {
    
    private static final boolean BRAIN_DEAD = false;
    private static final boolean DEBUG = true;

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
    /* protected (jdk1.1-is-stupid)*/ public class FskLoad extends InstrMEM {

	FskLoad(InstrFactory inf, Instr i, String assem, Temp dst, Temp src) {
	    super(inf, i, assem + " `d0, `s0", 
		  new Temp[]{dst}, new Temp[]{src});

	}
	FskLoad(Instr i, String assem, Temp dst, Temp src) {
	    this(i.getFactory(), i, assem, dst, src);
	}

	// Note that the order that 'dsts' will appear in is the order
	// that its iterator returns the Temps in.
	FskLoad(InstrFactory inf, Instr i, String assem, Collection dsts, Temp src) {
	    super(inf, i, assem + " " + 
		  getDstStr(dsts.size()) + ", `s0", 
		  (Temp[])dsts.toArray(new Temp[dsts.size()]), 
		  new Temp[]{src});
	}
	FskLoad(Instr i, String assem, Collection dsts, Temp src) {
	    this(i.getFactory(), i, assem, dsts, src);
	}

    }

    /** Class for <code>RegAlloc</code> usage in spilling registers. 
	
	Note that the constructors automagically put in the
	"appropriate" `d# and `s# operands.

    */
    /* protected (jdk1.1-is-stupid)*/ public class FskStore extends InstrMEM {
	FskStore(Instr i, String assem, Temp dst, Temp src) {
	    this(i.getFactory(), i, assem, dst, src);
	}

	FskStore(InstrFactory inf, HCodeElement hce, 
		String assem, Temp dst, Temp src) {
	    super(inf, hce, assem, 
		  new Temp[]{dst}, new Temp[]{src});
	}

	// Note that the order that 'dsts' will appear in is the order
	// that its iterator returns the Temps in.
	FskStore(InstrFactory inf, HCodeElement hce,
		 String assem, Temp dst, Collection srcs) {
	    super(inf, hce, assem + " `d0, " +
		  getSrcStr(srcs.size()),
		  new Temp[]{dst}, 
		  (Temp[])srcs.toArray(new Temp[srcs.size()]));
	}

	FskStore(Instr i, String assem, Temp dst, Collection srcs) {
	    this(i.getFactory(), i, assem, dst, srcs);
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
	CFGraphable first = (CFGraphable) code.getRootElement();
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
		if (BRAIN_DEAD) {
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

		    //  globalCode = 
		    //	new DemandDrivenRegAlloc
		    //	(frame, localCode.generateRegAssignment()); 
		    
		}

		return resolveOutstandingTemps
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
        <BR> <B>modifies:</B> inHc
	<BR> <B>effects:</B> Replaces the <code>FskLoad</code> and
	     <code>FskStore</code>s with memory instructions for the
	     appropriate <code>Frame</code>.
    */
    protected static HCode resolveOutstandingTemps(final HCode inHc) {
	final Code in = (Code) inHc;
	CFGraphable first = (CFGraphable) in.getRootElement();
	BasicBlock block = BasicBlock.computeBasicBlocks(first);
	
	final MakeWebsDumb makeWebs = 
	    new MakeWebsDumb(BasicBlock.basicBlockIterator(block));
	InstrSolver.worklistSolver(BasicBlock.basicBlockIterator(block), makeWebs);
	

	class Debug {
	    private int indent;
	    Debug(int indent) {
		this.indent = indent;
	    }

	    void indent() {
		for(int i=0; i<indent; i++)
		    System.out.print(" ");
	    }

	    void print(BasicBlock bb, MakeWebsDumb mW) {
		System.out.println();
		MakeWebsDumb.WebInfo webInfo = 
		    (MakeWebsDumb.WebInfo) mW.bbInfoMap.get(bb);
		/* focusing on IN/OUT info now...
		indent(); System.out.println("--"+ bb + " USE Map[Temp, Set[Instr]] start");  
		printEntries(webInfo.use);
		indent(); System.out.println("--"+ bb + " USE Map[Temp, Set[Instr]] end");  
		
		indent(); System.out.println("--"+ bb + " DEF Map[Temp, Set[Instr]] start");  
		printEntries(webInfo.def);
		indent(); System.out.println("--"+ bb + " DEF Map[Temp, Set[Instr]] end");  
		*/

		indent(); System.out.println("--"+ bb + " IN Map[Temp, Web] start");
		printEntries(webInfo.in);
		indent(); System.out.println("--"+ bb + " IN Map[Temp, Web] end");
		
		printInstrs(bb);
		
		indent(); System.out.println("--"+ bb + " OUT Map[Temp, Web] start");
		printEntries(webInfo.out);
		indent(); System.out.println("--"+ bb + " OUT Map[Temp, Web] end");
	    }

	    void printEntries(Map map) {
		Iterator entries = map.entrySet().iterator();
		while(entries.hasNext()) {
		    Map.Entry entry = (Map.Entry) entries.next();
		    indent(); indent(); System.out.println("[ "+ entry.getKey() + ", " + entry.getValue() + " ]");
		}
	    }
	    void printInstrs(BasicBlock bb) {
		Iterator instrs = bb.listIterator();
		while(instrs.hasNext()) {
		    Instr instr = (Instr) instrs.next();
		    if (! (instr instanceof FskLoad || 
			   instr instanceof FskStore)) {
			System.out.println("(" + instr.getID() +
					   ")\t"+ in.toAssem(instr));
		    } else {
			System.out.println("(" + instr.getID() +
					   ")\t"+ instr.toString());
		    }
		}
	    }
	    
	}

	Iterator bbIter = BasicBlock.basicBlockIterator(block);
	while(bbIter.hasNext()) {
	    BasicBlock bb = (BasicBlock) bbIter.next();
	    (new Debug(10)).print(bb, makeWebs);

	    System.out.println();
	    
	}
    
	    
	return in;
    }

/*    protected HCode resolveOutstandingTemps(HCode in) {
	// This implementation is REALLY braindead.  Fix to do a
	// smarter Graph-Coloring stack offset allocator

	// Its also broken because it doesn't use getSize in
	// InstrBuilder.  I need to look into incorporating that into
	// this implementation.

	Util.assert(in != null, "Don't try to resolve Temps for null HCodes");

	class TempFinder extends InstrVisitor {
	    HashMap tempsToOffsets = new HashMap();
	    int nextOffset = 1;

	    public void visit(FskLoad m) {
		// look for non-Register Temps in use, adding
		// them to internal map
		Iterator uses = m.useC().iterator();
		while(uses.hasNext()) {
		    Temp use = (Temp) uses.next();
		    if(!isTempRegister(use) &&
		       tempsToOffsets.get(use)==null){
			tempsToOffsets.put(use, new Integer(nextOffset));
			nextOffset++;
		    }
		}
	    } 
	    public void visit(FskStore m) {
		// look for non-Register Temps in def, adding
		// them to internal map
		Iterator defs = m.defC().iterator();
		while(defs.hasNext()) {
		    Temp def = (Temp) defs.next();
		    if(!isTempRegister(def) &&
		       tempsToOffsets.get(def)==null){
			tempsToOffsets.put(def, new Integer(nextOffset)); 
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
		List instrs = frame.getInstrBuilder().makeStore(Arrays.asList(m.use()), i.intValue(), m);

		// add a comment saying which temp is being stored
		Instr first = (Instr) instrs.get(0);
		Instr.replaceInstrList(m, instrs);		
		Instr newi = new Instr(first.getFactory(),
				       first,
				       "\t@storing " + m.def()[0],
				       null, null);
		newi.insertAt(new InstrEdge(first.getPrev(), first));
		
	    }
	    
	    public void visitLoad(FskLoad m) {
		// replace all non-Register Temps with appropriate
		// stack offset locations
		Integer i = (Integer) tempsToOffsets.get(m.use()[0]);
		Util.assert(i != null, "tempsToOffsets should have a value for "+m.use()[0]);
		List instrs = frame.getInstrBuilder().makeLoad(Arrays.asList(m.def()), i.intValue(), m);

		// add a comment saying which temp is being loaded
		Instr first = (Instr) instrs.get(0);
		Instr.replaceInstrList(m, instrs);
		Instr newi = new Instr(first.getFactory(),
				       first,
				       "\t@loading " + m.use()[0],
				       null, null);
		newi.insertAt(new InstrEdge(first.getPrev(), first));
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
	    i.accept(tf);
	}
	// now tf should have a full map of Temps to needed Stack
	// Offsets.

	// System.out.println("TempsToOffsets Mapping: " + tf.tempsToOffsets);

	InstrReplacer ir = new InstrReplacer(tf.tempsToOffsets);
	instrs = in.getElementsI();
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    i.accept(ir);
	}

	return in;
    }
*/    

    /** Checks if <code>t</code> is a register (Helper method).
	<BR> <B>effects:</B> If <code>t</code> is a register for the
	     <code>frame</code> associated with <code>this</code>,
	     then returns true.  Else returns false.   
    */ 
    protected boolean isTempRegister(Temp t) {
	return frame.getRegFileInfo().isRegister(t);
        
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
	the block of instructions listed in <code>iter</code>.  
	
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
	Temp[] regs = frame.getRegFileInfo().getGeneralRegisters();
		
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
			    frame.getRegFileInfo().suggestRegAssignment(preg, regFile); 
			List regList = (List) iter.next();
			InstrMEM loadSrcs = 
			    new FskLoad(inf, null, "FSK-LOAD", regList, preg); 
			loadSrcs.insertAt(new InstrEdge(instr.getPrev(), instr));
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
			    frame.getRegFileInfo().suggestRegAssignment(preg, regFile); 
			List regList = (List) iter.next();
			InstrMEM storeDsts = 
			    new FskStore(inf, null, "FSK-STORE",
					 preg, regList);

			// NOTE: if this assertion fails, we can write
			// code to work around this requirement.  But
			// for now this implementation is simple and
			// seems to work.  
			Util.assert(instr.succC().size() == 1, 
				    "Instr: "+instr+" should have only"+
				    " one control flow successor");

			storeDsts.insertAt
			    (new InstrEdge
			     (instr, (Instr)instr.succ()[0].to()));

			// I'm not certain this code will handle 
			// "add t0, t0, t1" properly 
			code.assignRegister(instr, preg, regList);
			Iterator regIter = regList.iterator();
			while(regIter.hasNext()) {
			    Temp r = (Temp) regIter.next();
			    regFile.put(r, preg);
			}
		    }
		}
	    } catch (SpillException e) {
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
	    ((Instr)instrs.next()).accept(memVisitor);
	}
	
	return code;
    }

}

/** Visits <code>BasicBlock</code>s of <code>Instr</code>s and
    uses the <code>FskLoad</code> and <code>FskStore</code>
    instructions to construct <code>Web</code>s for this method,
    These webs will need to be run through a merging dataflow
    analysis pass.  This is effectively ReachingDefs, but I
    couldn't figure out how to easily adapt Whaley's version of
    ReachingDefs to my needs (note to FSK, either figure out
    Whaley's version or rewrite it in a form thats at least as
    useful as the LiveVars class) (also note to FSK: the current
    implementation is soft and flabby (space and time
    inefficient); look into really fixing up ReachingDefs and
    LiveVars to be fast AND easy-to-use)
*/
class MakeWebsDumb extends ForwardDataFlowBasicBlockVisitor {
    /** struct class: 
	'in' maps a Temp to a Web that is defined somewhere above
	     this block. 
	'out' maps a Temp to a Web that is defined somewhere above
	      or within this block (note that the Web defined IN
	      this block is a distinct object from the one defined
	      ABOVE this block)
	'use' maps a Temp to the Set of Instrs that refer to it up
	      until (and not including) that Temp's LAST definition
	      in the block
	'def' maps a Temp to the Set of Instrs from that Temp's
	      LAST definition in the basic block through all of its
	      subsequent uses in the block
    */
    class WebInfo {
	MultiMap.Factory mmf = new MultiMap.Factory();
	
	MultiMap in = new DefaultMultiMap(); // Map[Temp, [Web] ]
	MultiMap out = new DefaultMultiMap(); // Map[Temp, [Web] ]
	MultiMap use = new DefaultMultiMap(new MySetFactory(), // Map[Temp, [Instr] ]
					harpoon.Util.Collections.Factories.hashMapFactory());
	MultiMap def = new DefaultMultiMap(new MySetFactory(), // Map[Temp, [Instr] ]
					harpoon.Util.Collections.Factories.hashMapFactory());
	
	class MySetFactory extends harpoon.Util.Collections.SetFactory {
	    public Set makeSet(java.util.Collection c) {
		return new java.util.HashSet(c) {
		    /** temporarily overriding this.toString() to
			give dense description of Set's contents. */  
		    public String toString() {
			StringBuffer str = new StringBuffer("{ ");
			Iterator iter = iterator();
			while(iter.hasNext()) {
			    Instr i = (Instr) iter.next();
			    str.append( i.getID() );
			    if (iter.hasNext()) str.append(", ");
			}
			str.append(" }");
			return str.toString();
		    }		    
		};
	    }
	}

	
	void foundLoad(RegAlloc.FskLoad instr) {
	    Iterator uses = instr.useC().iterator();
	    while(uses.hasNext()) {
		Temp t = (Temp) uses.next();

		Util.assert(t != null, "No nulls for Temps");
		
		if ((def.getValues(t)).isEmpty()) {
		    // if it uses a variable defined in
		    // another block, then add to USE
		    use.add(t, instr);
		} else {
		    // put it in the DEF set; we'll move the DEF
		    // set into the USE set later if we have to.
		    def.add(t, instr);
		}
	    }
	}
		
	void foundStore(RegAlloc.FskStore instr) {
	    Iterator defs = instr.defC().iterator();
	    while(defs.hasNext()) {
		Temp t = (Temp) defs.next();

		Util.assert(t != null, "No nulls for Temps");
		
		if (!(def.getValues(t)).isEmpty()) {
		    // We have seen a DEF for t in this block
		    // before; need to move all those instrs over
		    // before putting this in the DEF set
		    Iterator instrs =
			(def.getValues(t)).iterator();
		    while(instrs.hasNext()) {
			use.add(t, instrs.next());
		    }
		}
		def.add(t, instr);
	    }
	}
    }
    
    HashMap bbInfoMap;
    
    MakeWebsDumb(Iterator basicBlocks) {
	bbInfoMap = new HashMap();
	
	// initialize USE/DEF info
	while(basicBlocks.hasNext()) {
	    BasicBlock bb = (BasicBlock) basicBlocks.next();
	    WebInfo info = new WebInfo();
	    bbInfoMap.put(bb, info);
	    
	    ListIterator instrs = bb.listIterator();
	    while(instrs.hasNext()) {
		Instr instr = (Instr) instrs.next();
		if (instr instanceof RegAlloc.FskLoad) {
		    // LOAD FROM MEM
		    info.foundLoad((RegAlloc.FskLoad) instr);
		} else if (instr instanceof RegAlloc.FskStore) { 
		    // STORE TO MEM
		    info.foundStore((RegAlloc.FskStore) instr);
		}
	    }		
	}
    }
    
    public boolean merge(BasicBlock from, BasicBlock to) {

	WebInfo fromInfo = (WebInfo) bbInfoMap.get(from);
	WebInfo toInfo = (WebInfo) bbInfoMap.get(to);
	
	// FSK: can't just use putAll(); need to track if a change
	// FSK: occurred. 
	// toInfo.in.putAll(fromInfo.out);
	boolean changed = false;
	Iterator keys = fromInfo.out.keySet().iterator();
	while(keys.hasNext()) {
	    Object key = keys.next();
	    java.util.Collection newVals = fromInfo.out.getValues(key);
	    changed = changed || toInfo.in.addAll(key, newVals);
	}

	// System.out.println("\t\t\tMerging from " + from +
	//		   " to " + to + ":" + 
	//		   (changed?"changed":"nochange")); 
	
	return changed;
    }
    
    public void visit(BasicBlock b) {
	// System.out.println("\t\t\tVisiting " + b);

	WebInfo webInfo = (WebInfo) bbInfoMap.get(b);
	
	Iterator inEntries = webInfo.in.entrySet().iterator();
	while(inEntries.hasNext()) {
	    Map.Entry entry = (Map.Entry) inEntries.next();
	    Temp t = (Temp) entry.getKey();
	    Web web = (Web) entry.getValue();
	    Iterator instrs = webInfo.use.getValues(t).iterator();

	    // System.out.println("\t\t\t\t IN -> OUT : [" + 
	    //		       web + ", "+b+"]" );

	    while(instrs.hasNext()) {
		web.refs.add(instrs.next());
	    }
	    
	    webInfo.out.put(t, web);
	}
	
	Iterator defKeys = webInfo.def.keySet().iterator();
	while(defKeys.hasNext()) {
	    Temp t = (Temp) defKeys.next();
	    Set instrs = (Set) webInfo.def.getValues(t);

	    Web web = new Web(t, instrs);
	    
	    // Note that this will replace any web in OUT that was
	    // in IN with the last defined web (with the same temp) in
	    // this Basic Block (if one exists).  This is the correct
	    // behavior. 
	    webInfo.out.put(t, web);

	}
    }
}

