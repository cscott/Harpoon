// CodeGen.java, created Wed Jul 28 18:19:29 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HMethod;
import harpoon.ClassFile.HClass;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Tree.TreeDerivation;
import harpoon.IR.Tree.Exp;
import harpoon.IR.Tree.Print;
import harpoon.Temp.Temp;
import harpoon.Analysis.Maps.Derivation;

/**
 * <code>Generic.CodeGen</code> is a general class for specific Backends to
 * extend.  Typically a Specfile for a specific backend will be
 * designed as an extension of this class.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: CodeGen.java,v 1.1.2.15 2000-02-19 08:24:34 cananian Exp $ */
public abstract class CodeGen {

    private static boolean DEBUG = false;

    // Frame for instructions to access to get platform specific
    // variables (Register Temps, etc)   
    public final Frame frame;

    
    /** Creates a <code>Generic.CodeGen</code>. */
    public CodeGen(Frame frame) {
        this.frame = frame;
    }

    public abstract Derivation getDerivation();

    /** Emits <code>i</code> as the next instruction in the
        instruction stream.
    */	
    protected abstract Instr emit(Instr i);

    protected abstract void declare(Temp t, HClass clz);
    protected abstract void declare(Temp t, Derivation.DList dl);
    // helper method for declarations.
    protected final void declare(Temp t, TreeDerivation td, Exp e) {
	if (td==null) return; // no derivation info.
	HClass hc = td.typeMap(e);
	if (hc!=null) declare(t, hc);
	else declare(t, td.derivation(e));
    }


    /** Fixes up the procedure entry and exit code for a list of instrs, once
     *  it is known how many registers and how much stack space is used.
     */ // FIXME: is there a more abstract way to specify these 
        // quantities?   FSK: I think we should deprecate this
    public abstract Instr procFixup(HMethod hm, Instr instr, int stackspace,
				    java.util.Set usedRegisters);

    /** Fixes up the procedure entry and exit code for a list of instrs, once
     *  it is known how many registers and how much stack space is used.
     */ // FIXME: is there a more abstract way to specify these 
        // quantities?   FSK: This is what I think we should use instead
        public void procFixup(HMethod hm,
			  harpoon.Backend.Generic.Code code, 
			  int stackspace, 
			  java.util.Set usedRegisters) {
	harpoon.Util.Util.assert(false, "abstract me and implement in subclasses");
        }

    /** Creates a <code>Instr</code> list from the
	<code>IR.Tree.Code</code> <code>tree</code>.  
	<BR> <B>effects:</B> Generates and returns a two element list,
	where the first element is the head of a <code>Instr</code>s
	to execute <code>tree</code>, and the second element is the
	<code>Derivation</code> for that list of instructions.
	@return The head of a list of <code>Instr</code>s
    */
    public abstract java.util.List genCode(harpoon.IR.Tree.Code tree,
			      final harpoon.IR.Assem.InstrFactory inf); 

    /** Creates a <code>Instr</code> list from the
	<code>IR.Tree.Data</code> <code>tree</code>. 
	<BR> <B>effects:</B> Generates and returns a list of
	     <code>Instr</code>s representing the layout of
	     <code>tree</code>.
	@return The head of a list of <code>Instr</code>s
    */
    public abstract Instr genData(harpoon.IR.Tree.Data tree,
			      final harpoon.IR.Assem.InstrFactory inf); 
    
    public void debug(String s) {
	if (DEBUG) System.out.println(s);
    }

    public String prettyPrint(harpoon.IR.Tree.Tree exp) {
	return Print.print(exp);
    }
    
} 
