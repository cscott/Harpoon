// Code.java, created Tue Feb 16 22:25:11 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;
import harpoon.Temp.Label;
import harpoon.Util.Util;
import harpoon.Analysis.Maps.Derivation;

import java.util.Collection;
import java.util.List;

/**
 * <code>Generic.Code</code> is an abstract superclass of codeviews
 * which use <code>Instr</code>s.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Code.java,v 1.1.2.53 2001-06-05 04:03:24 pnkfelix Exp $
 */
public abstract class Code extends harpoon.IR.Assem.Code {

    public void printPreallocatedCode() {
	myPrint(new java.io.PrintWriter(System.out),false,true,new PrintCallback());
    }
    public void printPreallocatedCode(PrintCallback callback) {
	myPrint(new java.io.PrintWriter(System.out),false,true,callback);
    }

    public static boolean PEEPHOLE_OPTIMIZATIONS = true;

    private Derivation derivation;
    
    /** Generates a new <code>Generic.Code</code> from 
	another <code>Generic.Code</code>, <code>code</code>, with
	<code>i</code> as the root instruction (instead of whatever
	root was used in <code>code</code>, and <code>codeName</code>
	as the value that would be returned by a call
	<code>getName()</code>.  (the codeName argument is a hack to
	get around a dependency problem in the constructor for
	<code>Assem.Code</code>.
    */
    protected Code(Code code, Instr i, Derivation d, String codeName) {
	super(code.getMethod(), code.getFrame(), codeName);
	this.instrs = i;
	this.derivation = d;
    }

    protected Code(harpoon.IR.Tree.Code treeCode) {
	super(treeCode.getMethod(), treeCode.getFrame());
	List pair = this.frame.getCodeGen().
	            genCode(treeCode, this.inf);
	this.instrs = (Instr) pair.get(0);
	this.derivation = (Derivation) pair.get(1);
	Util.assert(instrs != null);
	Util.assert(derivation != null);
    }

    public Derivation getDerivation() {
	return derivation;
    }

    /** Returns false if <code>instr</code> cannot be safely deleted.
	<BR> <B>effects:</B> If it is safe to remove
	     <code>instr</code> from <code>this</code> stream of
	     instructions without changing the semantic meaning of the
	     program, then this <b>might</b> return true.  
	     Otherwise, returns false. 
    */
    public boolean isSafeToRemove(Instr instr) {
	
	if (instr.isMove()) { 
	    
	    // Eliminate MOVEs where [src] = [dst]
	    Util.assert(instr.use().length == 1, "moves have single use!");
	    Util.assert(instr.def().length == 1, "moves have single def!");
	    if (getRegisters(instr, instr.use()[0]). equals (getRegisters(instr, instr.def()[0]))) {
		return true;
	    }


	} else if (instr.isJump()) { 
	    
	    // Eliminate JUMPs to immediate successor in layout
	    Util.assert(!instr.canFallThrough);
	    Util.assert(instr.getTargets().size() == 1);
	    Label l = (Label) instr.getTargets().get(0);
	    if (instr.getInstrFor(l).equals( instr.getNext() )) {
		return true;
	    }
	    
	}


	return false;
    }

    /** Overrides superclass implementation of toAssem to return an empty string if <code>instr</code> 
	can be safely eliminated from output.  */
    public String toAssem(Instr instr) {
	if (PEEPHOLE_OPTIMIZATIONS && 
	    isSafeToRemove(instr)) {
	    
	    return "";

	} else {
	    
	    return super.toAssem(instr);

	}
    }

    public abstract String getName();

    /** Returns all of the Register <code>Temp</code>s that
	<code>val</code> maps to in <code>i</code>.
	<BR> <B>requires:</B> <OL>
	      <LI> <code>val</code> must be a <code>Temp</code> that
	           is an element of <code>i.defC()</code> or
		   <code>i.useC()</code>
	      <LI> (val is not Register for this architecture) => 
	           <code>registerAssigned(i, val)</code> is true
	<BR> <B>effects:</B> Returns a <code>Collection</code> of the
	     Register <code>Temp</code>s that are assigned to
	     <code>val</code> in <code>i</code>.  Every member of the
	     <code>Collection</code> returned will be a valid Register
	     for this architecture. 
    */
    public abstract List getRegisters(Instr i, Temp val);
    
    /** Assigns a register to a <code>Temp</code> in <code>i</code>.
	<BR> <B>modifies:</B> <code>i</code> (FSK: potentially at least)
	<BR> <B>effects:</B> creates a mapping 
	<BR> NOTE: This is only an experimental method; only FSK
	should be using it until he makes sure that it implies no
	design flaws. 

	<P> FSK: Flaw 1 -- if there are multiple references to
	<code>pseudoReg</code> in <code>i</code>, like a := a + 1,
	then this method is too general; it does not allow us to put
	a's def in a different register from its use.  Now, since
	we're using SSI form at a high level, I don't know if we'll
	ever encounter code like that (depends on how Tree->Instr form
	is performed), but 
	<BR> (1.) I don't like <b>relying</b> on SSI to catch
	          undocumented problems like this implicitly, 
	<BR> (2.) we could, in theory, try to use this backend with a  
	          non-SSI front end
	<BR> The other issue here is I don't know when allowing the
	flexibility of having different registers for a's def and use
	will buy us anything... 
	UPDATE: it does buy us something:
	since it allows for smaller webs w/o move instructions.  we
	can get around this problem relatively cheaply by maintaining
	not just a Instr x Temp -> List<Reg> mapping, but instead two
	mappings: 
	   Instr x Use -> List<Reg> 
	   Instr x Def -> List<Reg>.
	i will implement this after preliminary Global Register 
	Allocation is working. 
	 
     */
    public abstract void assignRegister(Instr i,
					Temp pseudoReg, 
					List regs);

    /** Checks if <code>pseudoReg</code> has been assigned to some
	registers in <code>i</code>.
	<BR> <B>requires:</B> 
	      <code>val</code> must be a <code>Temp</code> that
	      is an element of <code>i.defC()</code> or
	      <code>i.useC()</code>
	<BR> <B>effects:</B> 
	     If <code>pseudoReg</code> has been assigned
	     to some <code>List</code> of registers in <code>i</code>
	     and <code>removeAssignment(i, pseudoReg)</code> has not
	     been called since, returns <code>true</code>.  
	     Else returns <code>false</code>.
     */
    public abstract boolean registerAssigned(Instr i, Temp pseudoReg);

    public void removeAssignment(Instr i, Temp pseudoReg) {
	Util.assert(false, "override and implement Code.removeAssignment"+
		    " (which should be abstract but since its an "+
		    "experimental method I don't want have add it "+
		    "to all the other code yet)");
    }

}
