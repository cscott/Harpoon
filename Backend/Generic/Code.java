// Code.java, created Tue Feb 16 22:25:11 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.IR.Assem.Instr;
import harpoon.Temp.Temp;
import harpoon.Util.Util;

import java.util.Collection;
import java.util.List;

/**
 * <code>Generic.Code</code> is an abstract superclass of codeviews
 * which use <code>Instr</code>s.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Code.java,v 1.1.2.39 2000-01-26 06:04:54 cananian Exp $
 */
public abstract class Code extends harpoon.IR.Assem.Code {

    protected Code(harpoon.IR.Tree.Code treeCode) {
	super(treeCode.getMethod(), treeCode.getFrame());
	this.instrs = this.frame.getCodeGen().gen(treeCode, this.inf);
	Util.assert(instrs != null);
    }
    
    public abstract String getName();

    // allow code-gen to get/reset the protected instrs field
    // CSA: I DON'T LIKE THIS!
    Instr getInstrs() { return instrs; }
    void setInstrs(Instr instrs) { this.instrs = instrs; }
    // CSA: END DISLIKE

    /** Returns all of the Register <code>Temp</code>s that
	<code>val</code> maps to in <code>i</code>.
	<BR> <B>requires:</B> <OL>
	      <LI> <code>val</code> must be a <code>Temp</code> that
	           is an element of <code>i.defC()</code> or
		   <code>i.useC()</code>
	      <LI> <code>registerAssigned(i, val)</code> must be true
	<BR> <B>effects:</B> Returns a <code>Collection</code> of the
	     Register <code>Temp</code>s that are assigned to
	     <code>val</code> in <code>i</code>.  Every member of the
	     <code>Collection</code> returned will be a valid Register
	     for this architecture. 
    */
    public Collection getRegisters(Instr i, Temp val) {
	Util.assert(false, "Make abstract and implement in backends");
	return null;
    }
    
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
