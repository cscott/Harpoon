// Frame.java, created Fri Feb  5 05:48:12 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Temp.CloningTempMap;
import harpoon.Temp.TempFactory;

import java.util.List;
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
 * @version $Id: Frame.java,v 1.1.2.18 1999-07-28 18:22:09 duncan Exp $
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
	<code>this</code>. 
    */
    public abstract OffsetMap getOffsetMap();

    /** Returns the <code>TempFactory</code> to create new
	<code>Temp</code>s in <code>this</code>. 
    */
    public abstract TempFactory tempFactory();

    /** Returns the <code>TempFactory</code> of the register
	<code>Temp</code>s in <code>this</code>. 
    */
    public abstract TempFactory regTempFactory();

    /** Generates a new set of <code>Instr</code>s for memory traffic
	from RAM to the register file.
	@param <code>offset</code> The stack offset.  This is an
	       ordinal number, it is NOT meant to be a multiple of
	       some byte size.  This frame should perform the
	       necessary magic to turn the number into an appropriate
	       stack offset. 
	@param <code>template</code> An <code>Instr</code> to derive
	       the generated <code>List</code> from
	       <code>template</code> gives <code>this</code> the
	       ability to incorporate additional information into the
	       produced <code>List</code> of <code>Instr</code>s.   
    */ 
    public abstract List makeLoad(Temp reg, int offset, Instr template);

    /** Generates a new set of <code>Instr</code>s for memory traffic
	from the register file to RAM. 
	@param <code>offset</code> The stack offset.  This is an
	       ordinal number, it is NOT meant to be a multiple of
	       some byte size.  This frame should perform the
	       necessary magic to turn the number into an appropriate
	       stack offset. 
	@param <code>template</code> An <code>Instr</code> to derive
	       the generated <code>List</code> from
	       <code>template</code> gives <code>this</code> the
	       ability to incorporate additional information into the
	       produced <code>List</code> of <code>Instr</code>s.   
    */ 
    public abstract List makeStore(Temp reg, int offset, Instr template);

    /** Create a new Frame one level below the current one. */
    public abstract Frame newFrame(String scope);

    /** Analyzes <code>regfile</code> to find free registers that
	<code>t</code> can be assigned to.  
	<BR> <B>effects:</B> Either returns an <code>Iterator</code>
	     possible assignments (though this is not guaranteed to be
	     a complete list of all possible choices, merely the ones 
	     that this <code>Frame</code> chose to find), or throws a
	     <code>Frame.SpillException</code> with a set of possible
	     spills. 
	@param t <code>Temp</code> that needs to be assigned to a set
   	         of Registers. 
	@param regfile A mapping from Register <code>Temp</code>s to
	               NonRegister <code>Temp</code>s representing the
		       current state of the register file. 
	@return A <code>List</code> <code>Iterator</code> of Register
                <code>Temp</code>s.  Each <code>List</code> represents
		a safe place for the value in <code>t</code> to be
		stored (safe with regard to the architecture targeted
		and the type of <code>t</code>, <b>not</b> with regard
		to the current contents of <code>regfile</code> or the
		data-flow of the procedure being analyzed).  The
		elements of the <code>List</code> in the
		<code>Iterator</code> returned are ordered according
		to proper placement of the Register-bitlength words of
		the value in <code>t</code>, low-order words first.
     */
    public abstract Iterator suggestRegAssignment(Temp t, Map regfile) throws Frame.SpillException;

    /** SpillException tells a register allocator which
	<code>Temp</code>s are appropriate for spilling in order to
	allocate space for another <code>Temp</code>.  In the common
	case, <code>this.getPotentialSpills()</code> will just return
	an <code>Iterator</code> that iterates through singleton
	<code>Set</code>s for all of the registers. 
     */
    public static abstract class SpillException extends Exception {
	public SpillException() { super(); }
	public SpillException(String s) { super(s); }

	/** Returns an iterator of spill candidates. 
	    <BR> <B>effects:</B> Returns a <code>Set</code>
    	         <code>Iterator</code> of spill candidates.  Each
		 element of the <code>Iterator</code> returned
		 represents a <code>Set</code> of Register
		 <code>Temp</code>s that could be spilled to free up
		 the amount of space needed for the attempted
		 assignment in the register file.  The returned
		 <code>Iterator</code> is not guaranteed to iterate
		 through all possible <code>Set</code>s of
		 combinations of spill candidates, but should iterate
		 through a decent selection so that the Register
		 Allocator has significant freedom in selecting
		 registers to spill. 
	*/ 
	public abstract Iterator getPotentialSpills();
    }
}

