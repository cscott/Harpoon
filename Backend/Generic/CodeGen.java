// CodeGen.java, created Wed Jul 28 18:19:29 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HMethod;
import harpoon.IR.Tree.Code;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Tree.Print;

/**
 * <code>Generic.CodeGen</code> is a general class for specific Backends to
 * extend.  Typically a Specfile for a specific backend will be
 * designed as an extension of this class.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CodeGen.java,v 1.1.2.3 1999-10-26 01:47:54 pnkfelix Exp $ */
public abstract class CodeGen {

    private static boolean DEBUG = false;

    // Frame for instructions to access to get platform specific
    // variables (Register Temps, etc)   
    protected final Frame frame;
    
    /** Creates a <code>Generic.CodeGen</code>. */
    public CodeGen(Frame frame) {
        this.frame = frame;
    }
    
    /** Fixes up the procedure entry and exit code for a list of instrs, once
     *  it is known how many registers and how much stack space is used.
     */ // FIXME: is there a more abstract way to specify these quantities?
    public abstract Instr procFixup(HMethod hm, Instr instr, int stackspace,
				    java.util.Set usedRegisters);

    /** Creates a <code>Instr</code> list from the
	<code>IR.Tree.Code</code> <code>tree</code>. 
	<BR> <B>effects:</B> Generates and returns a list of
	     <code>Instr</code>s to execute <code>tree</code>.
	@return The head of a list of <code>Instr</code>s
    */
    public abstract Instr gen(harpoon.IR.Tree.Code tree,
			      final harpoon.IR.Assem.InstrFactory inf); 

    /** Creates a <code>Instr</code> list from the
	<code>IR.Tree.Data</code> <code>tree</code>. 
	<BR> <B>effects:</B> Generates and returns a list of
	     <code>Instr</code>s representing the layout of
	     <code>tree</code>.
	@return The head of a list of <code>Instr</code>s
    */
    public abstract Instr gen(harpoon.IR.Tree.Data tree,
			      final harpoon.IR.Assem.InstrFactory inf); 
    
    public void debug(String s) {
	if (DEBUG) System.out.println(s);
    }

    public String prettyPrint(harpoon.IR.Tree.Tree exp) {
	return Print.print(exp);
    }
    
} 
