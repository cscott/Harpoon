// Frame.java, created Fri Feb  5 05:48:12 1999 by cananian
// Copyright (C) 1999 C. Scott Ananian <cananian@alumni.princeton.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Temp.Temp;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Assem.InstrList;
import harpoon.IR.Assem.InstrFactory;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Stm;
import harpoon.IR.Tree.TreeFactory;
import harpoon.Backend.Maps.OffsetMap;
import harpoon.Temp.TempFactory;

/**
 * A <code>Frame</code> encapsulates the machine-dependent information
 * needed for compilation.
 * 
 * @author  C. Scott Ananian <cananian@alumni.princeton.edu>
 * @version $Id: Frame.java,v 1.1.2.10 1999-02-26 23:22:28 andyb Exp $
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
                                     Temp[] paramdsts);

    /** Returns an <code>InstrList</code> which adds a "sink" 
     *  instruction to specify registers that are live on procedure exit. */
    public abstract InstrList procLiveOnExit(InstrList body);

    /** Returns an <code>InstrList</code> which wraps the 
     *  method body in assembler directives and other instructions
     *  needed to initialize stack space. */
    public abstract InstrList procAssemDirectives(InstrList body);

    /** Returns the appropriate offset map for this frame */
    public abstract OffsetMap getOffsetMap();

    /** Returns the TempFactory to create new Temps in this Frame */
    public abstract TempFactory tempFactory();

    /** Returns the TempFactory of the register Temps in this Frame */
    public abstract TempFactory regTempFactory();
    
    /** Create a new Frame one level below the current one. */
    public abstract Frame newFrame(String scope);
}
