// InstrBuilder.java, created Fri Sep 10 23:29:27 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.Temp.Temp;
import harpoon.Util.ListFactory;

import java.util.List;
import java.util.ArrayList;

/** <code>InstrBuilder</code> defines an interface that general program
    transformations can call to generate needed assembly code blocks for
    arbitrary target architectures.  
    <p>
    Many of the <code>Instr</code> optimizations need to insert new
    code to support the transformations that they make on the code.
    This class provides a set of generic <code>Instr</code> creation
    routines that each backend wil implement, to be used in creating
    the support code.
    
    @see harpoon.Analysis.Instr
    @author  Felix S. Klock II <pnkfelix@mit.edu>
    @version $Id: InstrBuilder.java,v 1.1.2.1 1999-09-11 05:43:18 pnkfelix Exp $
 */
public abstract class InstrBuilder {
    
    /** Creates a <code>InstrBuilder</code>. */
    public InstrBuilder() {
        
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

    /** Returns a new <code>InstrLABEL</code> for generating new
	arbitrary code blocks to branch to.
	@param template An <code>Instr</code> to base the generated
	                <code>InstrLABEL</code>.
			<code>template</code> should be part of the
			instruction stream that the returned
			<code>InstrLABEL</code> is intended for. 
    */
    public abstract InstrLABEL makeLabel(Instr template);


}
