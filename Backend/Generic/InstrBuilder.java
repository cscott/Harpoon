// InstrBuilder.java, created Fri Sep 10 23:29:27 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrLABEL;
import harpoon.Temp.Temp;
import harpoon.Temp.Label;
import harpoon.Util.Collections.ListFactory;

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
    @version $Id: InstrBuilder.java,v 1.3.2.1 2002-02-27 08:34:29 cananian Exp $
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
	@param <code>startingOffset</code> The stack offset
	       (zero-indexed).  This is 
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
	@see InstrBuilder#makeLoad(Temp, int, Instr)
	@see InstrBuilder#getSize
    */ 
    public List makeLoad(List regs, int startingOffset, Instr template) { 
        ArrayList list = new ArrayList();
	Instr last=null, curr=null;
	for (int i=0; i<regs.size(); i++) {
	    List cl = makeLoad((Temp)regs.get(i), startingOffset+i, template);
	    curr = (Instr) cl.get(0);
	    curr.layout(last, curr.getNext());
	    list.addAll(cl);
	    last = (Instr) cl.get(cl.size() - 1);
	}
	return list;
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
	@param <code>startingOffset</code> The stack offset
	       (zero-indexed).  This is 
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
	@see InstrBuilder#makeStore(Temp, int, Instr)
	@see InstrBuilder#getSize

    */ 
    public List makeStore(List regs, int startingOffset, Instr template) { 
        ArrayList list = new ArrayList();
	Instr last=null, curr=null;
	for (int i=0; i<regs.size(); i++) {
	    List cl = makeStore((Temp)regs.get(i), startingOffset+i, template);
	    curr = (Instr) cl.get(0);
	    Instr next = curr.getNext();
	    curr.remove(); // FSK: being safe; removing before relayouting
	    curr.layout(last, next);
	    list.addAll(cl);
	    last = (Instr) cl.get(cl.size() - 1);
	}
	
	// System.out.println("store: "+list);
	
	return list;
    }

    /** Generates a new set of <code>Instr</code>s for memory traffic
	from RAM to one register in the register file.
	@param <code>reg</code> The target register <code>Temp</code>
	       to hold the value that will be loaded from
	       <code>offset</code> in memory. 
	@param <code>offset</code> The stack offset (zero-indexed).
	       This is an 
	       ordinal number, it is NOT meant to be a multiple of
	       some byte size.  This frame should perform the
	       necessary magic to turn the number into an appropriate
	       stack offset. 
	@param <code>template</code> An <code>Instr</code> to derive
	       the generated <code>List</code> from.
	       <code>template</code> gives <code>this</code> the
	       ability to incorporate additional information into the
	       produced <code>List</code> of <code>Instr</code>s.   
	@see InstrBuilder#getSize
    */ 
    protected abstract List makeLoad(Temp reg, int offset, Instr template);

    /** Generates a new set of <code>Instr</code>s for memory traffic
	from the register file to RAM. 
	@param <code>reg</code> The register <code>Temp</code> holding
	       the value that will be stored at <code>offset</code> in
	       memory. 
	@param <code>offset</code> The stack offset (zero-indexed).
	       This is an 
	       abstract number, it is NOT necessarily a multiple of
	       some byte size.  This frame should perform the
	       necessary magic to turn the number into an appropriate
	       stack offset. 
	@param <code>template</code> An <code>Instr</code> to derive
	       the generated <code>List</code> from
	       <code>template</code> gives <code>this</code> the
	       ability to incorporate additional information into the
	       produced <code>List</code> of <code>Instr</code>s.   
	@see InstrBuilder#getSize
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
	    <code>t2</code>, either 
	    ( <code>offset(t2)</code> is greater than or equal to
	    <code>offset(t1) + getSize(t1)</code> )
	    OR
	    ( <code>offset(t1)</code> is greater than or equal to
	    <code>offset(t2) + getSize(t2)</code> ).

	<BR> The default implementation simply returns 1; subclasses
	     should override this and check for double word temps, etc.
        @see InstrBuilder#makeLoad
        @see InstrBuilder#makeStore
    */
    public int getSize(Temp temp) {
	return 1;
    }

    /** Returns a new <code>InstrLABEL</code> for generating new
	arbitrary code blocks to branch to.
	@param template An <code>Instr</code> to base the generated
	                <code>InstrLABEL</code>.
			<code>template</code> should be part of the
			instruction stream that the returned
			<code>InstrLABEL</code> is intended for. 
    */
    public InstrLABEL makeLabel(Label l, Instr template) {
	assert false : "abstract method";
	return null;
    }
}
