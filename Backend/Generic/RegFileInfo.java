// RegFileInfo.java, created Sat Sep 11 00:00:07 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import java.util.Set;
import java.util.Map;
import java.util.Iterator;

/** <code>RegFileInfo</code> defines an interface that general program
    analyses can call to find out architecture specific information
    about the target machine's register file. 
  
    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: RegFileInfo.java,v 1.1.2.3 1999-09-11 18:40:10 cananian Exp $
 */
public abstract class RegFileInfo {
    
    /** Creates a <code>RegFileInfo</code>. */
    public RegFileInfo() {
        
    }
    
    /** Returns the Set of registers that should be considered live at
	the end of a method. 
    */
    public abstract Set liveOnExit();

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
	@exception Frame.SpillException if the register
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
	throws RegFileInfo.SpillException;

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


    /** Returns an array of <code>Temp</code>s which represent all
     *  the available registers on the machine. */
    public abstract Temp[] getAllRegisters();
    /** Returns a specific register on the machine.<BR>
     *  <code>getRegister(index)==getAllRegisters()[index]</code>
     */
    public Temp getRegister(int index) { return getAllRegisters()[index]; }

    /** Returns an array of <code>Temp</code>s for all the registers
     *  that the register allocator can feel free to play with */
    public abstract Temp[] getGeneralRegisters();
    /** Returns a specially-named Temp to use as the frame pointer. */
    public abstract Temp FP();

}
