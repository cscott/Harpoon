package harpoon.Analysis.Instr;

import harpoon.Analysis.BasicBlock;
import harpoon.IR.Assem.Instr;
import harpoon.IR.Assem.InstrMEM;
import harpoon.IR.Assem.InstrVisitor;
import harpoon.Temp.Temp;
import harpoon.Util.Util;
import harpoon.Util.Collections.ListFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/** Verify uses the inherent definitions of the instruction
    stream to check that the register uses of the allocated
    instructions are coherent.
*/
class Verify extends harpoon.IR.Assem.InstrVisitor {
    LocalCffRegAlloc lra;
    RegFile regfile = new RegFile();
    Set spillUsesV, spillDefsV;
    
    final BasicBlock block;
    private Instr curr;
    
    Verify(LocalCffRegAlloc lra, BasicBlock b, 
	   Set spillUses, Set spillDefs) {
	this.lra = lra;
	this.block = b; 
	this.spillUsesV = spillUses;
	this.spillDefsV = spillDefs;
    }
    
    /** Gets the assignment for `use' out of `regfile'.  Note that
	in some cases, `use' is not directly mapped in the
	`regfile', but rather another temp that is in the same
	EqTempSet as `use' is mapped in the `regfile'.
    */
    List getAssignment(Temp use) {
	if (lra.isRegister(use)) 
	    return ListFactory.singleton(use);
	
	List regs = regfile.getAssignment(use);
	/*	
	if (regs == null) { // search for alternate
	    Iterator temps = regfile.tempSet().iterator();
	    while(temps.hasNext()) {
		Temp t = (Temp) temps.next();
		if (lra.tempSets.getRep(t) == lra.tempSets.getRep(use)) {
		    regs = regfile.getAssignment(t);
		    break;
		}
	    }
	}
	*/
	Util.assert
	    (regs != null, 
	     lra.lazyInfo("no reg assignment"+regfile,block,curr,use,false));
	return regs;
    }
    
    public void visit(final Instr i) {
	curr = i;
	visit(i, false);
    }
    /*
    public void visit(final InstrMOVE i) {
	curr = i;
	if (lra.instrsToRemove.contains(i) && 
	    !lra.isRegister(i.use()[0]) &&
	    !lra.isRegister(i.def()[0])) {

	    // System.out.println("shouldn't be here on "+i);
	    if (true) return;
	    
	} else {
	    visit((Instr)i);
	}
    }
    */
    public void visit(final Instr i, boolean multAssignAllowed) {
	Iterator uses = i.useC().iterator();
	while(uses.hasNext()) {
	    Temp use = (Temp) uses.next();
	    if (lra.isRegister(use)) continue;
	    
	    Collection codeRegs = lra.getRegs(i, use);
	    
	    List fileRegs = getAssignment(use);
	    
	    { // ASSERTION CODE
		Util.assert(codeRegs != null, "codeRegs!=null ");
		Util.assert(!fileRegs.contains(null), "no null allowed in fileRegs");
		Util.assert(!codeRegs.contains(null), "no null allowed in codeRegs");
		Util.assert(codeRegs.containsAll(fileRegs),
			    lra.lazyInfo("codeRegs incorrect; "+
				     "c:"+codeRegs+" f:"+fileRegs+" regfile:"+regfile,
				     block,i,use,false));
		Util.assert(fileRegs.containsAll(codeRegs),
			    "fileRegs incomplete: "+"c: "+codeRegs+" f: "+fileRegs);
	    } // END ASSERTION CODE
	}
	
	Iterator defs = i.defC().iterator();
	while(defs.hasNext()) {
	    final Temp def = (Temp) defs.next();
	    // def = tempSets.getRep(def);
	    Collection codeRegs = lra.getRegs(i, def);
	    Util.assert(codeRegs != null, "getRegs null for "+def+" in "+i);
	    
	    
	    if (false) {
		// check (though really just debugging my thought process...)
		Iterator redefs = codeRegs.iterator();
		while(redefs.hasNext()) {
		    final Temp r = (Temp) redefs.next();
		    Util.assert(regfile.isEmpty(r), 
				lra.lazyInfo("reg:"+r+" is not empty prior to assignment",block,i,def));
		}
	    }
	    
	    
	    assign(def, codeRegs); // , multAssignAllowed);
	}
	
    }
    
    public void visit(final InstrMEM i) {
	curr = i;
	if (i instanceof RegAlloc.SpillLoad) {
	    // regs <- temp
	    spillUsesV.add(i.use()[0]);
	    
	    Util.assert(!regfile.hasAssignment(i.use()[0]), 
			lra.lazyInfo("if we're loading, why in regfile?",
				 block, i,i.use()[0],regfile,false));
	    assign(i.use()[0], i.defC());
	} else if (i instanceof RegAlloc.SpillStore) {
	    // temp <- regs
	    spillDefsV.add(i.def()[0]);
	    
	    // this code is failing for an unknown reason; i
	    // would think that the mapping should still be
	    // present in the regfile.  taking it out for
	    // now... 
	    if (false) {
		final Temp def = i.def()[0];
		List fileRegs = regfile.getAssignment(def);
		Collection storeRegs = i.useC();
		Util.assert(fileRegs != null, 
			    lra.lazyInfo("fileRegs!=null",block,i,def));
		Util.assert(!fileRegs.contains(null),
			    "no null allowed in fileRegs");
		Util.assert(storeRegs.containsAll(fileRegs),
			    "storeRegs incomplete");
		Util.assert(fileRegs.containsAll(storeRegs),
			    "fileRegs incomplete");
	    }
	} else {
	    visit((Instr)i);
	}
    }
    
    // default assign procedure (multiple assignments not allowed)
    private void assign(Temp def, Collection c) {
	assignP(def, c, false);
    }
    
    /** assigns `def' to `c'.
	requires: `c' is a collection of register Temps
	modifies: `regfile'
	effects: 
	1. (not `multAssignAllowed') => remove all temps held
	   by registers in `c' from `regfile'.
	2. assigns `def' to the collection of registers `c' in `regfile'
    */
    private void assignP(final Temp def, Collection c, boolean multAssignAllowed) {
	if (!multAssignAllowed) {
	    // getting around multiple-assignment checker...
	    Iterator redefs = c.iterator();
	    while(redefs.hasNext()) {
		Temp r = (Temp) redefs.next();
		Temp t = regfile.getTemp(r);
		if (regfile.hasAssignment(t)) regfile.remove(t);
		
	    }
	}
	
	regfile.assign(def, new ArrayList(c));
    }
    
}
