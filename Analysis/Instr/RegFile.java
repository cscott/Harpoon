// RegFile.java, created Wed Dec  8 16:24:41 1999 by pnkfelix
// Copyright (C) 1999 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis.Instr;

import harpoon.Temp.Temp;
import harpoon.Util.LinearMap;
import harpoon.Util.Util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;
import java.util.Iterator;

/**
 * <code>RegFile</code> is an abstraction for a register file found in
 * most processor architectures for storing working sets of data.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: RegFile.java,v 1.1.2.4 2000-01-13 17:54:17 pnkfelix Exp $
 */
class RegFile {
    
    private Map regToTmp; // Map[ Reg -> Temp ]
    private Map tmpToRegLst; // Map[ Temp -> List<Reg> ]

    private Set dirtyTemps; // Set of all Temps that have been written

    /** Creates a <code>RegFile</code>. */
    public RegFile() {
        regToTmp = new HashMap();
	tmpToRegLst = new HashMap();
	dirtyTemps = new HashSet();
    } 

    /** Marks the pseudo register <code>preg</code> as dirty. 
	<BR> <B>requires:</B> <code>preg</code> currently is assigned
	                      in <code>this</code>.
        <BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> marks <code>preg</code> as dirty.
	     <code>preg</code> will remain dirty until it is removed
	     from <code>this</code>.
     */
    public void writeTo(Temp preg) {
	Util.assert(hasAssignment(preg),
		    "temp: "+preg+" should have an assignment in "+this);
	dirtyTemps.add(preg);
    }

    /** Returns whether the pseudo register is clean.
	<BR> <B>requires:</B> <code>preg</code> currently is assigned
	                      in <code>this</code>.
	<BR> <B>effects:</B> if the pseudo register <code>preg</code>
	     has been marked as dirty since last being assigned in
	     <code>this</code>, returns False.  Else returns True.
    */
    public boolean isClean(Temp preg) {
	return !isDirty(preg);
    }

    /** Returns whether the pseudo register is dirty.
	<BR> <B>requires:</B> <code>preg</code> currently is assigned
	                      in <code>this</code>.
	<BR> <B>effects:</B> if pseudo register <code>preg</code> has
	     been marked as dirty since last being assigned in
	     <code>this</code>, returns True.  Else returns False.
    */
    public boolean isDirty(Temp preg) {
	Util.assert(hasAssignment(preg),
		    "temp: "+preg+" should have an assignment in "+this);
	return dirtyTemps.contains(preg);
    }

    public String toString() {
	return regToTmp.toString();
    }
    
    /** Returns a snapshot of the current mapping from registers to
	pseudoregisters.
	<BR> <B>effects:</B> Returns an immutable <code>Map</code> of
	     Register <code>Temp</code>s to Pseudo-Register
	     <code>Temp</code>s, representing the current state of the
	     register file.  The <code>Map</code> returned is suitable
	     for use with RegFileInfo.
    */
    public Map getRegToTemp() {
	// we create a new HashMap so that changes to the current
	// Register File are not reflected in the returned Map, and we
	// make it unmodifiable so that it remains immutable.
	return Collections.unmodifiableMap(new HashMap(regToTmp));
    }

    /** Returns the pseudo-register associated with <code>reg</code>.
	If there is no pseudo-register associated with
	<code>reg</code>, returns <code>null</code>.
     */
    public Temp getTemp(Temp reg) {
	return (Temp) regToTmp.get(reg);
    }
    
    /** Assigns <code>pseudoReg</code> to <code>regs</code>.
	<BR> <B>requires:</B> <OL>
  	     <LI> All elements of <code>regs</code> are not currently 
	          associated with any pseudo register in
		  <code>this</code>. 
	     <LI> <code>pseudoReg</code> does not currently have an
	          assignment in <code>this</code>.
	     </OL>
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> Creates an association between
   	     <code>pseudoReg</code> and all of the elements of
	     <code>regs</code>.
	<BR> Note: Requirement 2 may seem strange, as its not
 	     strictly illegal in a compiler to assign pseudo registers
	     multiple times.  But the alternative would require making 
	     tmpToRegLst a MultiMap, the semantics of remove(pseudoReg)
	     would have been ugly, and theres no place i know of where
	     it would make sense to assign preg multiple times.
	     AHA!  There is ONE: The pseudoReg PREASSIGN.  Hmmm.  Have
	     to think about this one...
     */
    public void assign(final Temp pseudoReg, final List regs) { 
	final Iterator regIter = regs.iterator();
	while (regIter.hasNext()) {
	    Temp reg = (Temp) regIter.next();
	    Util.assert(!regToTmp.containsKey(reg), 
			"non-empty reg: "+reg); 
	    regToTmp.put(reg, pseudoReg);
	}

	Util.assert(!tmpToRegLst.containsKey(pseudoReg), 
		    "dont assign pregs more than once");
	tmpToRegLst.put(pseudoReg, regs);
    } 

    /** Checks if <code>pseudoReg</code> has an register assignment in
	<code>this</code>.
	<BR> <B>effects:</B> If <code>pseudoReg</code> currently maps
	     to some <code>List</code> of registers in
	     <code>this</code>, returns <code>true</code>.
	     Else returns <code>false</code>.
     */
    public boolean hasAssignment(final Temp pseudoReg) {  
	return tmpToRegLst.containsKey(pseudoReg);
    } 
    
    /** Returns the current assignment for <code>pseudoReg</code>.
	<BR> <B>requires:</B> <code>pseudoReg</code> current has an
	     assignment in <code>this</code>. 
	<BR> <B>effects:</B> Returns the <code>List</code> of register
	     <code>Temp</code>s currently associated with
	     <code>pseudoReg</code> in <code>this</code>.
     */
    public List getAssignment(final Temp pseudoReg) { 
	return (List) tmpToRegLst.get(pseudoReg);
    }  
    
    /** Removes the assignment for <code>pseudoReg</code>.
	<BR> <B>requires:</B> <code>pseudoReg</code> currently has an
	     assignment in <code>this</code>.
	<BR> <B>modifies:</B> <code>this</code>
	<BR> <B>effects:</B> removes all associations in
	     <code>this</code> between <code>pseudoReg</code> and
	     register <code>Temp</code>s.
    */
    public void remove(final Temp pseudoReg) {
	List regs = (List) tmpToRegLst.remove(pseudoReg);
	final Iterator regIter = regs.iterator();
	while(regIter.hasNext()) {
	    Temp reg = (Temp) regIter.next();
	    Object prev = regToTmp.remove(reg);
	    Util.assert(prev.equals(pseudoReg), 
			"reg: "+reg+" should have mapped to "+
			pseudoReg+" instead of "+prev);
	}
	dirtyTemps.remove(pseudoReg);
    }
} 
