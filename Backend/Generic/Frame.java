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
import harpoon.IR.Assem.InstrLABEL;
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
 * @version $Id: Frame.java,v 1.1.2.32 1999-09-11 18:47:09 cananian Exp $
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

    /** Returns the appropriate <code>Generic.Runtime</code> for
     *  this <code>Frame</code>. */
    public abstract Runtime getRuntime();

    /** Returns the appropriate <code>InstrBuilder</code> for this
	<code>Frame</code>. 
    */
    public abstract InstrBuilder getInstrBuilder();

    /** Returns the appropriate <code>RegFileInfo</code> for this
	<code>Frame</code>.
    */
    public abstract RegFileInfo getRegFileInfo();

    /** Returns the <code>Generic.CodeGen</code> for the backend
	associated with <code>this</code>.
     */
    public abstract CodeGen getCodeGen();
}
