// RegFileInfo.java, created Sat Sep 11 00:00:07 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Util.Util;
import harpoon.Temp.Temp;
import harpoon.Temp.TempFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

/** <code>RegFileInfo</code> defines an interface that general program
    analyses can call to find out architecture specific information
    about the target machine's register file. 
    
    <p> A note about <code>Temp</code>s: several types of
    <code>Temp</code>s are mentioned in this interface, and the
    differences between them are worth noting.

    <p> A Temp, as used in the rest of the compiler, is a variable
    suitable for storing some value in.  They are temporary storage
    for intermediate values.  I will refer to them as <i>Flex
    Temps</i> for the remainder of this discussion, to avoid confusion
    with other kinds of Temps.  A <i>Physical Register Temp</i> is a
    special Temp for representing a physical register in the specific
    architecture's register file.  Some kinds of Flex Temps may
    require multiple Physical Register Temps to fit in the register
    file, depending on the variable type and the types of registers
    offered by the architecture.  A <i>Virtual Register Temp</i> is a
    abstraction of a Physical Register Temp (or several of them): it
    is not any specific register in the register file, it just
    represents a location that is somewhere in the register file.

    <p> The idea is that the register allocator will first figure out
    at which points in the program the various Flex Temps will be in
    the register file, and where the various Loads and Stores for
    maintaining the state of the register file will go.  However, the
    allocator does not have to actually assign specific Physical
    Register Temps to the Flex Temps if it does want to; it can delay
    that until later in the allocation process.  This allows for Local
    and Global Allocation to work together, because Local Allocation
    can work with Virtual Register Temps, and then the Global
    Allocator can merge Virtual Registers in different Basic Blocks
    together before mapping them to Physical Register Temps.

    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: RegFileInfo.java,v 1.4 2002-04-10 03:02:44 cananian Exp $ */
public abstract class RegFileInfo {

    /** Defines function from 
	(<code>Temp</code> x DefPoint) -> 
	<code>Set</code> of <code>CommonLoc</code>.
     */
    public interface TempLocator {
	public Set locate(Temp t, harpoon.IR.Assem.Instr i);
    }
    
    /** Common super class for <code>StackOffsetLoc</code> and 
	<code>MachineRegLoc</code>.  Should only be implemented by
	<code>Temp</code> objects (is an interface to get around
	multiple inheritance problmes).
    */
    public interface CommonLoc {
	/** Returns the <code>KIND</code> of Loc <code>this</code> is.
	    <BR> <B>effects:</B> 
	         If <code>this</code> is a
		 <code>StackOffsetLoc</code>, returns
		 <code>StackOffsetLoc.KIND</code> 
		 Else <code>this</code> implicitly is a
		 <code>MachineRegLoc</code>, and returns
		 <code>MachineRegLoc.KIND</code>. 
	*/
	int kind();
    }
    
    /** Represents Stack Offset <code>Temp</code>s. */
    public interface StackOffsetLoc extends CommonLoc {
	public static int KIND = 1;
	int stackOffset();
    }

    /** Defines the upper bound on possible indexes for
	<code>MachineRegLoc</code>s. 
    */
    public int maxRegIndex() { throw new Error("Unimplemented."); }

    /** Represents Machine Register <code>Temp</code>s. */
    public interface MachineRegLoc
	extends CommonLoc, harpoon.Backend.Maps.BackendDerivation.Register {
	public static int KIND = 2;
	
	/** Returns the index of <code>this</code> in the register file.
	    <BR> <B>effects:</B> returns the abstract index of
	         <code>this</code> in the register file.  The index
		 returned may not map directly to this register's
		 position in the register file (for example, r8 may
		 have an index of "0").  Each register for a given
		 architecture will have a different index. 
		 The index is bounded as follows:
		 0 &lt;= index &lt; <code>RegFileInfo.this.maxRegIndex()</code>. 
	*/
	int regIndex();
    }

    /** Creates a <code>RegFileInfo</code>. */
    public RegFileInfo() {
        
    }
    
    /** Returns the <code>Set</code> of registers live at a method's
	exit. 
	
	<BR> <B>effects:</B> Returns an unmodifiable <code>Set</code>
	     of register <code>Temp</code>s of the registers that
	     should be considered live at the end of a method. 
    */
    public abstract Set liveOnExit();

    /** Returns the Set of registers that are caller-saved.
	
	<BR> <B>effects:</B> Returns an unmodifiable <code>Set</code>
	     of all of the <I>caller-saved</I> register
	     <code>Temp</code>s.  Any register in this Set can be used
	     arbitrarily in a method call, and therefore it is the
	     responsibility of the caller of a method to save them if
	     it wants them preserved. 
    */
    // public abstract Set callerSave();

    /** Returns the Set of registers that are callee-saved.

	<BR> <B>effects:</B> Returns an unmodifiable <code>Set</code>
	     of all of the <I>callee-saved</I> register
	     <code>Temp</code>s.  All registers in this Set are
	     assumed to be preserved during a method call, and
	     therefore it the responsibility of the method being
	     called it save them and restore them before returning if
	     it wants to use them for temporary storage. 
    */
    // public abstract Set calleeSave();

    /** Checks if <code>t</code> is a element of the register file for
	this architecture.
	<BR> <B>effects:</B> 
	     If <code>t</code> is an element of the register file, 
	     Then returns true,
	     Else returns false.
	@param t <code>Temp</code> that may be part of the register
	         file. 
    */
    public abstract boolean isRegister(Temp t); 


    /** Returns the number of slots that <code>t</code>'s assigned
	register sequence occupies in the register file.  

	<p> The default implementation returns 1; subclasses should
	override to account for how their respective architectures
	handle multi-register temps (such as longs or doubles).  
	In any case, should always return an integer greater than 
	zero. 
    */
    public int occupancy(Temp t) { return 1; }
    
    /** Returns the degree of conflict that <code>b</code> inflicts
	upon <code>a</code> if the two <code>Temp</code>s interfere.
    
	<p> The default implementation returns 1; subclasses should
	override to account for how their respect architectures
	constrain the assignment of multi-register temps (such as
	longs or doubles).  
	
	<p>Also, if the two registers can never be assigned to the
	same register bank, then it is valid for this method to return
	<code>0</code>.
	
	<p> For advice on choosing an appropriate number to return,
	see the paper "Coloring Register Pairs" by Briggs, Cooper, and
	Torczon.  The mappings recommended by that paper are
	(double,single) => 2 and (double,double) => 2 [or 3].
    
    */
    public int pressure(Temp a, Temp b) { return 1; }
    
    /** Returns a List of Reg that can hold <code>needy</code>, given
	that the Regs in <code>occupied</code> are not available to
	hold <code>needy</code>.  

	<p> Note that the returned List is not a list of possible 
	assignments, but rather a single assignment that may span more 
        than one register.   
	Thus the length of the returned List should equal 
	<code>this.occupies(needy)</code>.
	
	<p> Returns null if no assignment is available in the situation
	where all registers in <code>occupied</code> are in use.
    */
    public List assignment(Temp needy, Collection occupied) {
	assert false : "abstract and implement in subclasses";
	return null;
    }
    
    /** Returns the Regs that can never hold <code>t</code>.  
	
	<p> This method is used to increase the degree of
	<code>Temps</code> which have limited assignments in the
	register file.  For example, if t is a <code>Temp</code> that
	can only be assigned to a certain register bank, this method
	will return a <code>Collection</code> containing all of the
	registers in the other register banks.
    */
    public Collection illegal(Temp t) { 
	return Collections.EMPTY_SET;
    }
    
    /** Returns all of the available registers on this architecture. */
    public Collection allRegs() { return getAllRegistersC(); }

    /** Produces a mutable <code>Set</code> of register assignments
	that can hold <code>t</code>.  FSK: experimental method.
	<BR> <B>requires:</B> t is not a physical register Temp.
	<BR> <B>effects:</B> Returns a <code>Set</code> of possible
	     register assignments for <code>t</code>, where each
	     assignment is an unmodifiable <code>List</code> of
	     Register <code>Temp</code>s.  The elements of each
	     <code>List</code> are ordered according to proper
	     placement of the Register-bitlength words of the value in
	     <code>t</code>, low-order words first.   Every list
	     returned will have the same length.
	     The Set returned may be a <code>SortedSet</code>, in
	     which case the earlier assignments are favored over later
	     ones. 
    */
    public Set getRegAssignments(Temp t) { 
	assert false : "abstract and implement in subclasses";
	return null;
    }

    /** Analyzes <code>regfile</code> to find free registers that
	<code>t</code> can be assigned to.  
	(FSK: Need to update this method to incorporate knowledge of
	Virtual Register Temps (perhaps it is guaranteed not to throw
	a SpillException when given a Virtual Register Temp))
	<BR> <B>effects:</B> Either returns an <code>Iterator</code>
	     of possible assignments (though this is not guaranteed to
	     be a complete list of all possible choices, merely the
	     ones that this <code>RegFileInfo</code> chose to find), or
	     throws a <code>RegFileInfo.SpillException</code> with a set of
	     possible spills. 
	<P> Note to implementors: Resist the urge to generate an
	<code>Iterator</code> that produces <B>all</B> possible
	assignments in series. 
	In general, register allocation algorithms need to, at some
	point, construct an interference graph to represent how the
	registers should be assigned.  Such a graph would contain a
	node for each register assignment (and put interference edges
	between assignments that were not allowed for a given 
	<code>Temp</code>), which means that even if your
	<code>Iterator</code> did not keep all of the assignments 
	in memory at once, code that USES your <code>Iterator</code>
	may very well do so.  Thus, while there may be many
	assignments for a <code>Temp</code> that occupies 
	more than one register, and it is possible to write an 
	<code>Iterator</code> that produces all such assignments, such
	an <code>Iterator</code> would cause an time/space explosion
	when applied to a decently sized register file.  
	<BR> Also, realize that it is not enough to ensure that any
	one of the set 
	{ of possible <code>Iterator</code>s that may be returned }
	traverses a reasonably small subset of the assignment space;
	you must ensure that the UNION of all possible traversals is
	of a reasonable size.  This is because the interference graph
	that is constructed will not be built from just one Suggestion
	Iterator, but rather from an Suggestion Iterator 
	<B>for each</B> variable that is given a register assignment.

	@param t <code>Temp</code> that needs to be assigned to a set
   	         of Registers. 
	@param regfile A mapping from Register <code>Temp</code>s to
	               NonRegister <code>Temp</code>s representing the
		       current state of the register file.  Empty
		       Register <code>Temp</code>s should simply not
		       have an entry in <code>regfile</code> (as
		       opposed to the alternative of mapping to some
		       NoValue object).  
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
	@exception RegFileInfo.SpillException if the register
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
    public Iterator suggestRegAssignment(Temp t, Map regfile,
					 Collection preassignedTemps) 
	throws RegFileInfo.SpillException {
	assert false; return null;
    }

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


    /** Returns an array of <code>Temp</code>s which represent all
     *  the available registers on the machine. */
    public abstract Temp[] getAllRegisters();

    /** Returns a Collection of <code>Temp</code>s which represent all
     *  the available registers on the machine. */
    public Collection getAllRegistersC() {
	return Arrays.asList(getAllRegisters());
    }

    /** Returns a List of the Component Temps that compose
	<code>t</code>.  If <code>t</code> is not a Composite Temp
	(ie, it maps directly to a single register in the Register
	File) then the singleton List <code>[ t ]</code> is returned.
	
	Note that the default implementation assumes that
	<code>t</code> is not a Composite Temp; architectures with
	Composite Temps should override and properly implement this
	method. 
    */
    public List expand(Temp t) {
	return harpoon.Util.Collections.ListFactory.singleton(t);
    }

    /** Returns a specific register on the machine.<BR>
     *  <code>getRegister(index)==getAllRegisters()[index]</code>
     */
    public Temp getRegister(int index) { return getAllRegisters()[index]; }


    // FSK: these two methods are probably unneeded with the General
    // Register Allocator, and may encourage register allocation
    // implementations to take a "dangerous" approach... look into
    // deprecating it (or localizing it to
    // Backend.StrongARM.RegFileInfo)

    /** Returns an array of <code>Temp</code>s for all the registers
     *  that the register allocator can feel free to play with */
    public abstract Temp[] getGeneralRegisters();

    /** Returns a Collection of <code>Temp</code>s for all the registers
     *  that the register allocator can feel free to play with */
    public Collection getGeneralRegistersC() {
	return Arrays.asList(getGeneralRegisters());
    }

}
