// RegAlloc.java, created Mon Mar 29 16:47:25 1999 by pnkfelix
// Copyright (C) 1999 Felix S Klock <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Temp.Temp;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Properties.UseDef;
import harpoon.IR.Properties.Edges;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.Code;
import harpoon.Analysis.UseMap;
import harpoon.Analysis.DataFlow.BasicBlock;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;

import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;
import java.util.ListIterator;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;


/**
 * <code>RegAlloc</code> performs Register Allocation for a set of
 * <code>Instr</code>s in a <code>Code</code>.  After register
 * allocation is completed for a set of <code>Instr</code>s, the only
 * references to non-register <code>Temp</code>s in the
 * <code>Instr</code>s will be <code>InstrMEM</code> instructions to
 * move values from the register file to data memory and vice-versa.
 * 
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: RegAlloc.java,v 1.1.2.3 1999-04-20 19:02:13 pnkfelix Exp $ */
public abstract class RegAlloc  {
    
    protected Frame frame;
    protected Code code;
    protected BasicBlock rootBlock;

    /** Creates a <code>RegAlloc</code>. 
	
	<BR> <B>Design Issue:</B> should there be a RegAlloc object for every
	method, or just for every machine target?  For now it seems
	associating a new one with every method will save a lot of
	headaches.

    */
    protected RegAlloc(Frame frame, Code code) {
        this.frame = frame;
	this.code = code;
	Edges first = (Edges) code.getRootElement();
	rootBlock = BasicBlock.computeBasicBlocks(first);
    }
    
    /** Assigns registers in the code for <code>this</code>.
	
	<BR> <B>effects:</B> Partially or completely allocates
	     registers for the values defined and used in the code for
	     <code>this</code>.  Values will be preserved in the code;
	     any live value will be stored before its assigned
	     register is overwritten.
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
	     this method to be used in the compiler.
     */
    public static HCodeFactory codeFactory(final HCodeFactory parentFactory, 
					   final Frame frame) {
	return new HCodeFactory() {
	    HCodeFactory parent = parentFactory;
	    Frame f = frame;
	    public HCode convert(HMethod m) {
		HCode preAllocCode = parent.convert(m);
		LocalCffRegAlloc localCode = 
		    new LocalCffRegAlloc(frame, (Code) preAllocCode);
		DemandDrivenRegAlloc globalCode =
		    new DemandDrivenRegAlloc
		    (frame, localCode.generateRegAssignment());		
		return globalCode.generateRegAssignment();
	    }
	    public String getCodeName() {
		return parent.getCodeName();
	    }
	    public void clear(HMethod m) {
		parent.clear(m);
	    }
	};
    }

    /** Checks if <code>t</code> is a register (Helper method).
	<BR> <B>effects:</B> If <code>t</code> is a register for the
	     <code>frame</code> associated with <code>this</code>,
	     then returns true.  Else returns false.   
    */ 
    protected boolean isTempRegister(Temp t) {
	Temp[] allRegs = frame.getAllRegisters();
	boolean itIs = false;
	for (int i=0; i < allRegs.length; i++) {
	    if (t.equals(allRegs[i])) {
		itIs = true;
		break;
	    }
	}
	return itIs;
    }

    /** Checks if <code>i</code> is last use of <code>reg</code> in
	the block of instructions lists in <code>iter</code>.  
	
	<BR> <B>requires:</B> 
	     1. <code>i</code> is an element in <code>iter</code> 
	     2. <code>iter</code> is currently indexed at
	        <code>i</code> 
	     3. <code>reg</code> is used by <code>i</code>
	<BR> <B>effects:</B> Returns true if no instruction after
	     <code>i</code> in <code>iter</code> uses <code>reg</code>
	     before <code>reg</code> is redefined (<code>i</code>
	     redefining <code>reg</code> is sufficient).  Else returns
	     false.  
    */
    protected boolean lastUse(Temp reg, UseDef i, ListIterator iter) {
	int index = 0;
	UseDef curr = i;
	boolean r = true;
	while (iter.hasNext() && ! contained( curr.def(), reg ) ) {
	    curr = (UseDef) iter.next(); index++;
	    if (contained( curr.use(), reg )) {
		r = false;
		break;
	    }
	}
	// reset the index (to preserve state of iter)
	while (index > 0) {
	    iter.previous();
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

