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

    <p> Lastly, a <i>Preassign Temp</i> is an Temp representing some
    value that has no representation as a Flex Temp.  It is used for
    portions of the code where the Code Generator has inserted
    hardcoded references to Physical Register Temps (such as when it
    is setting up the arguments for a function call); we do not want
    those Registers to be used to store any other values, and we do
    not want to allow them to be suggested for spilling when the
    register file is full.  So when the Allocator is requesting a
    Register to store some Flex Temp, it should set up mappings from
    any preassigned Physical Register Temps to newly generated
    <code>PreassignTemp</code>s to represent the values that are being
    maintained by the hardcoded references.
  
    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: RegFileInfo.java,v 1.1.2.16 2000-01-26 04:47:11 pnkfelix Exp $ */
public abstract class RegFileInfo {
    
    /** Common super class for <code>StackOffsetLoc</code> and 
	<code>MachineRegLoc</code>.  Should only be implemented by
	<code>Temp</code> objects (is an interface to get around
	multiple inheritance problmes).
    */
    static interface CommonLoc {
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
    static interface StackOffsetLoc extends CommonLoc {
	public static int KIND = 1;
	int stackOffset();
    }

    /** Defines the upper bound on possible indexes for
	<code>MachineRegLoc</code>s. 
    */
    public abstract int regIndexRange();

    /** Represents Machine Register <code>Temp</code>s. */
    static interface MachineRegLoc extends CommonLoc {
	public static int KIND = 2;
	
	/** Returns the index of <code>this</code> in the register file.
	    <BR> <B>effects:</B> returns the abstract index of
	         <code>this</code> in the register file.  The index
		 returned may not map directly to this register's
		 position in the register file (for example, r8 may
		 have an index of "0").  Each register for a given
		 architecture will have a different index.  The index
		 returned is guaranteed to fall between 
		 0 and RegFileInfo.this.regIndexRange() 
	*/
	int regIndex();
    }

    private static TempFactory preassignTF = new TempFactory() {
	public String getScope() { 
	    return "private TF for RegFileInfo"; 
	}
	public String getUniqueID(String suggestion) { 
	    return "rfi"+suggestion.hashCode(); 
	}
    };

    /** <code>PreassignTemp</code> represents <code>Temp</code>s
     *  which have been pre-assigned to machine registers. */
    public static class PreassignTemp extends Temp {
	private Temp reg;
	public PreassignTemp(Temp reg) {
	    super(preassignTF);
	    this.reg = reg;
	}
	public String toString() {
	    return reg+"<preassigned>";
	}
    }
    
    /** <code>VRegAllocator</code> is a virtual register allocator.
	Register Allocators can use the returned Virtual Register
	Temps to store Temps in abstractly, without commiting to one
	particular physical register.  This way, virtual registers in
	seperate basic blocks can map to the same physical register in
	the register file.
    */
    public static abstract class VRegAllocator {
	/** Returns a Virtual Register Temp for <code>t</code>.
	    <BR> <B> effects: </B> If <code>regfile</code> has space
	         to hold a value of the type held in <code>t</code>, 
		 returns a virtual register suitable for mapping to 
		 <code>t</code> in <code>regfile</code>.  Else, throws
		 a <code>SpillException</code> indicating which
		 registers could be removed from <code>regfile</code>
		 to make room for <code>t</code>.
	*/
	public abstract Temp vreg(Temp t, Map regfile) 
	    throws RegFileInfo.SpillException; 
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
    public abstract Set callerSave();

    /** Returns the Set of registers that are callee-saved.

	<BR> <B>effects:</B> Returns an unmodifiable <code>Set</code>
	     of all of the <I>callee-saved</I> register
	     <code>Temp</code>s.  All registers in this Set are
	     assumed to be preserved during a method call, and
	     therefore it the responsibility of the method being
	     called it save them and restore them before returning if
	     it wants to use them for temporary storage. 
    */
    public abstract Set calleeSave();

    /** Returns the <code>TempFactory</code> of the register
	<code>Temp</code>s in <code>this</code>. 

	FSK: is this needed?  Why would code using RegFileInfo need
	     access to this?  Double check if its just a hack to
	     implement the same functionality offered by
	     <code>isRegister(Temp)</code> below.
    */
    public abstract TempFactory regTempFactory();

    /** Checks if <code>t</code> is a element of the register file for
	this architecture.
	<BR> <B>effects:</B> 
	     If <code>t</code> is an element of the register file, 
	     Then returns true,
	     Else returns false.
	@param t <code>Temp</code> that may be part of the register
	         file. 
    */
    public boolean isRegister(Temp t) {
	return t.tempFactory() == regTempFactory();
    }

    /** Analyzes <code>regfile</code> to find free registers that
	<code>t</code> can be assigned to.  

	FSK: Need to update this method to incorporate knowledge of
	Virtual Register Temps (perhaps it is guaranteed not to throw
	a SpillException when given a Virtual Register Temp)

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
		       NoValue object).  Registers that are
		       pre-assigned and cannot be spilled should map
		       to an instance of a RegFileInfo.PreassignTemp
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

	Note to implementors: make <b>sure</b> that you do not return
	Registers that map to <code>RegFileInfo.PreassignTemp</code>s
	in your 'potential spill' set; it could lead to preassigned 
	registers being assigned for other variables, which defeats
	the whole purpose of pre-assignment.
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

    /** Generates a new <code>VRegAllocator</code> guaranteed to
	return virtual register Temps from a set disjoint from all
	other generated <code>VRegAllocator</code>s.
    */
    public VRegAllocator allocator() {
	harpoon.Util.Util.assert(false, "make RegFileInfo.allocator() abstract and implement in all backends");
	return null;
    }

}
