// Frame.java, created Fri Feb  5 05:48:12 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HCode;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.TempFactory;
import harpoon.Util.ListFactory;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Iterator;

/**
 * A <code>Frame</code> encapsulates the machine-dependent information
 * needed for compilation.  <code>Frame</code>s are not intended to be
 * <i>entirely</i> machine-specific; all machines with roughly the same
 * datatypes (for example, 32-bit words) and which use the same runtime
 * implementation should be able to share most, if not all, of a
 * <code>Frame</code> implementation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @author  Felix Klock <pnkfelix@mit.edu>
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Frame.java,v 1.1.2.27 1999-09-09 00:36:20 cananian Exp $
 * @see harpoon.IR.Assem
 */
public abstract class Frame {

    /** Returns a <code>Tree.Exp</code> object which represents a pointer
     *  to a newly allocated block of memory, of the specified size.  
     *  Generates code to handle garbage collection, and OutOfMemory errors.
     */
    public abstract Exp memAlloc(Exp size);
    
    /** Returns <code>false</code> if pointers can be represented in
     *  32 bits, or <code>true</code> otherwise. */
    public abstract boolean pointersAreLong();

    /** Returns an array of <code>Temp</code>s which represent all
     *  the available registers on the machine. */
    public abstract Temp[] getAllRegisters();

    /** Returns an array of <code>Temp</code>s for all the registers
     *  that the register allocator can feel free to play with */
    public abstract Temp[] getGeneralRegisters();
    /** Returns a specially-named Temp to use as the frame pointer. */
    public abstract Temp FP();

    /** Returns a <code>Tree.Stm</code> object which contains code
     *  to move method parameters from their passing location into
     *  the Temps that the method expects them to be in. */
    public abstract Stm procPrologue(TreeFactory tf, HCodeElement src, 
                                     Temp[] paramdsts, int[] paramtypes);

    /** Returns a block of <code>Instr</code>s which adds a "sink" 
     *  instruction to specify registers that are live on procedure exit. */
    public abstract Instr procLiveOnExit(Instr body);

    /** Returns a block of <code>Instr</code>s which wraps the 
     *  method body in assembler directives and other instructions
     *  needed to initialize stack space. */
    public abstract Instr procAssemDirectives(Instr body);

    /** Returns the appropriate <code>OffsetMap</code> for
	this <code>Frame</code>. 
    */
    public abstract OffsetMap getOffsetMap();

    /** Returns the appropriate <code>Runtime</code> for
     *  this <code>Frame</code>. */
    public abstract Runtime getRuntime();

    /** Returns the <code>TempFactory</code> of the register
	<code>Temp</code>s in <code>this</code>. 
    */
    public abstract TempFactory regTempFactory();

    /** Checks if <code>t</code> is a element of the register file for
	this backend. 
    */
    public boolean isRegister(Temp t) {
	return t.tempFactory() == regTempFactory();
    }

    /** Generates a new set of <code>Instr</code>s for memory traffic
	from RAM to multiple registers in the register file.  This
	method's default implementation simply calls
	<code>makeLoad(Temp,int,Instr)</code> for each element of
	<code>regs</code> and concatenates the returned
	<code>List</code>s of <code>Instr</code>s, so architectures
	with more efficient memory-to-multiple-register operations
	should override this implementation with a better one.
	@param <code>regs</code> The target register <code>Temp</code>s
	       to hold the values that will be loaded from
	       <code>startingOffset</code> in memory.
	@param <code>startingOffset</code> The stack offset.  This is
	       an ordinal number, it is NOT meant to be a multiple of
	       some byte size.  Note that this method will load values
	       starting at <code>startingOffset</code> and go up to
	       <code>startingOffset</code> +
	       <code>regs.size()-1</code> (with the first register in
	       <code>regs</code> corresponding to
	       <code>startingOffset</code> + 0, the second to
	       <code>startingOffset</code> + 1, and so on), so code
	       planning to reference the locations corresponding to
	       the sources for the data in the register file should 
	       account for any additional offsets.
	@param <code>template</code> An <code>Instr</code> to derive
	       the generated <code>List</code> from.
	       <code>template</code> gives <code>this</code> the
	       ability to incorporate additional information into the
	       produced <code>List</code> of <code>Instr</code>s.
	@see Frame#makeLoad(Temp, int, Instr)
	@see Frame#getSize
    */ 
    public List makeLoad(List regs, int startingOffset, Instr template) { 
        ArrayList lists = new ArrayList();
	for (int i=0; i<regs.size(); i++) {
	    lists.add(makeLoad((Temp)regs.get(i), startingOffset+i, template));
	}
	return ListFactory.concatenate(lists);
    }

    /** Generates a new set of <code>Instr</code>s for memory traffic
	from multiple registers in the register file to RAM.  This
	method's default implementation simply calls
	<code>makeStore(Temp,int,Instr)</code> for each element of
	<code>regs</code> and concatenates the returned
	<code>List</code>s of <code>Instr</code>s, so architectures
	with more efficient multiple-register-to-memory operations
	should override this implementation with a better one.
	@param <code>regs</code> The register <code>Temp</code>s
	       holding the values that will be stored starting at
	       <code>startingOffset</code> in memory.
	@param <code>startingOffset</code> The stack offset.  This is
	       an ordinal number, it is NOT meant to be a multiple of
	       some byte size.  Note that this method will store values
	       starting at <code>startingOffset</code> and go up to
	       <code>startingOffset</code> +
	       <code>regs.size()-1</code> (with the first register in
	       <code>regs</code> corresponding to
	       <code>startingOffset</code> + 0, the second to
	       <code>startingOffset</code> + 1, and so on), so code
	       planning to reference the locations corresponding to
	       the targets for the data in the register file should
	       account for any additional offsets. 
	@param <code>template</code> An <code>Instr</code> to derive
	       the generated <code>List</code> from.
	       <code>template</code> gives <code>this</code> the
	       ability to incorporate additional information into the
	       produced <code>List</code> of <code>Instr</code>s.
	@see Frame#makeStore(Temp, int, Instr)
	@see Frame#getSize

    */ 
    public List makeStore(List regs, int startingOffset, Instr template) { 
        ArrayList lists = new ArrayList();
	for (int i=0; i<regs.size(); i++) {
	    lists.add(makeStore((Temp)regs.get(i), startingOffset+i, template));
	}
	return ListFactory.concatenate(lists);
    }


    /** Generates a new set of <code>Instr</code>s for memory traffic
	from RAM to one register in the register file.
	@param <code>reg</code> The target register <code>Temp</code>
	       to hold the value that will be loaded from
	       <code>offset</code> in memory. 
	@param <code>offset</code> The stack offset.  This is an
	       ordinal number, it is NOT meant to be a multiple of
	       some byte size.  This frame should perform the
	       necessary magic to turn the number into an appropriate
	       stack offset. 
	@param <code>template</code> An <code>Instr</code> to derive
	       the generated <code>List</code> from.
	       <code>template</code> gives <code>this</code> the
	       ability to incorporate additional information into the
	       produced <code>List</code> of <code>Instr</code>s.   
	@see Frame#getSize
    */ 
    protected abstract List makeLoad(Temp reg, int offset, Instr template);

    /** Generates a new set of <code>Instr</code>s for memory traffic
	from the register file to RAM. 
	@param <code>reg</code> The register <code>Temp</code> holding
	       the value that will be stored at <code>offset</code> in
	       memory. 
	@param <code>offset</code> The stack offset.  This is an
	       abstract number, it is NOT necessarily a multiple of
	       some byte size.  This frame should perform the
	       necessary magic to turn the number into an appropriate
	       stack offset. 
	@param <code>template</code> An <code>Instr</code> to derive
	       the generated <code>List</code> from
	       <code>template</code> gives <code>this</code> the
	       ability to incorporate additional information into the
	       produced <code>List</code> of <code>Instr</code>s.   
	@see Frame#getSize
    */ 
    protected abstract List makeStore(Temp reg, int offset, Instr template);

    /** Returns the size of <code>temp</code> on the stack.
	<BR><B>effects:</B> Calculates the size that a value of the
	    type of <code>temp</code> would have on the stack (in
	    terms of the abstract number used for calculating stack
	    offsets in <code>makeLoad()</code> and
	    <code>makeStore()</code>).  
	<BR> When constructing loads and stores, the register allocator
	    should ensure that live values do not overlap on the
	    stack.  Thus, given two temps <code>t1</code> and
	    <code>t2</code>, <code>offset(t2)</code> should be at
	    least <code>offset(t1) + getSize(t1)</code>
	<BR> The default implementation simply returns 1; subclasses
	     should override this and check for double word temps, etc.
        @see Frame#makeLoad
        @see Frame#makeStore
    */
    public int getSize(Temp temp) {
	return 1;
    }

    /** Analyzes <code>regfile</code> to find free registers that
	<code>t</code> can be assigned to.  
	<BR> <B>effects:</B> Either returns an <code>Iterator</code>
	     of possible assignments (though this is not guaranteed to
	     be a complete list of all possible choices, merely the
	     ones that this <code>Frame</code> chose to find), or
	     throws a <code>Frame.SpillException</code> with a set of
	     possible spills. 
	@param t <code>Temp</code> that needs to be assigned to a set
   	         of Registers. 
	@param regfile A mapping from Register <code>Temp</code>s to
	               NonRegister <code>Temp</code>s representing the
		       current state of the register file.  Empty
		       Register <code>Temp</code>s should simply not
		       have an entry in <code>regfile</code> (as
		       opposed to the alternative of mapping to some
		       NoValue object)
	@return A <code>List</code> <code>Iterator</code> of Register
	        <code>Temp</code>s.  The <code>Iterator</code> is
	        guaranteed to have at least one element.  Each
	        <code>List</code> represents a safe place for the
	        value in <code>t</code> to be stored (safe with regard
	        to the architecture targeted and the type of
	        <code>t</code>, <b>not</b> with regard to the current
	        contents of <code>regfile</code> or the data-flow of
	        the procedure being analyzed).  The elements of each
	        <code>List</code> in the <code>Iterator</code>
	        returned are ordered according to proper placement of
	        the Register-bitlength words of the value in
	        <code>t</code>, low-order words first.  
	@exception <code>Frame.SpillException</code> if the register
	           file represented by <code>regfile</code> does not
		   have any Register <code>Temp</code>s free to hold a
		   new value of the type of <code>t</code>.  This
		   exception will contain the necessary information to
		   spill some set of registers.  After spilling, a 
		   second call to <code>suggestRegAssignment()</code>
		   can not throw an exception, as long as no new
		   values have been loaded into the register file
		   since the point of spilling.
    */
    public abstract Iterator suggestRegAssignment(Temp t, Map regfile) 
	throws Frame.SpillException;

    /** SpillException tells a register allocator which
	<code>Temp</code>s are appropriate for spilling in order to
	allocate space for another <code>Temp</code>.  

	In the common case, <code>this.getPotentialSpills()</code>
	will just return an <code>Iterator</code> that iterates
	through singleton <code>Set</code>s for all of the registers.  
	
	It is the responsibility of the register allocator to actually
	decide which set of registers to spill, to generate the code
	to save the values within said registers to their appropriate
	memory locations, and to generate the code to put those values
	back in the register file when they are needed next.
    */
    public static abstract class SpillException extends Exception {
	public SpillException() { super(); }
	public SpillException(String s) { super(s); }

	/** Returns an iterator of spill candidates. 
	    <BR> <B>effects:</B> Returns a <code>Set</code>
    	         <code>Iterator</code> of spill candidates.  Each
		 element of the <code>Iterator</code> returned
		 represents a mutable <code>Set</code> of Register
		 <code>Temp</code>s that could be spilled to free up
		 the amount of space needed for the attempted
		 assignment in the register file.  The returned
		 <code>Iterator</code> is not guaranteed to iterate
		 through all possible <code>Set</code>s of
		 combinations of spill candidates, but should iterate
		 through a decent selection so that the Register
		 Allocator has significant freedom in selecting
		 registers to spill. 

	   <BR> NOTE: consider making the Sets returned immutable...
	*/ 
	public abstract Iterator getPotentialSpills();
    }

    /** Returns the <code>GenericCodeGen</code> for the backend
	associated with <code>this</code>.
     */
    public abstract GenericCodeGen codegen();

    /** Returns the Set of registers that should be considered live at
	the end of a method. 
    */
    public abstract Set liveOnExit();
    
}

