// GCInfo.java, created Wed Jan 26 09:40:41 2000 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@alum.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Analysis.Instr.RegAlloc.IntermediateCodeFactory;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.RegFileInfo.CommonLoc;
import harpoon.Backend.Generic.RegFileInfo.MachineRegLoc;
import harpoon.Backend.Generic.RegFileInfo.StackOffsetLoc;
import harpoon.ClassFile.HClass;
import harpoon.ClassFile.HCodeFactory;
import harpoon.ClassFile.HMethod;
import harpoon.IR.Assem.Instr;
import harpoon.Temp.Label;
import harpoon.Util.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A <code>GCInfo</code> object collects data needed for
 * garbage collection and makes necessary annotations to
 * the instruction stream.
 * 
 * @author  Karen K. Zee <kkz@alum.mit.edu>
 * @version $Id: GCInfo.java,v 1.1.2.12 2001-06-18 20:47:24 cananian Exp $
 */
public abstract class GCInfo {
    /** Creates an <code>IntermediateCodeFactory</code> that
	prepares the code for garbage collection.
	<BR> <B>requires:</B> The <code>parentFactory</code>
	     must produce code in <code>Instr</code> form.
	<BR> <B>effects:</B> Returns an 
	     <code>IntermediateCodeFactory</code> that modifies
	     the code as needed for a particular garbage
	     collection scheme and stores the necessary
	     garbage collection data in the <code>GCInfo</code>
	     object.
     */
    public abstract IntermediateCodeFactory 
	codeFactory(IntermediateCodeFactory parentFactory, Frame frame);
    /** Returns an ordered <code>List</code> of the
	<code>GCPoint</code>s in a given <code>HMethod</code>.
	Returns <code>null</code> if the <code>HMethod</code>
	has not been evaluated for garbage collection purposes.
	Returns an empty <code>List</code> if the 
	<code>HMethod</code> has been evaluated and has been
	found to not contain any GC points.
    */
    public abstract List gcPoints(HMethod hm);
    /** Returns a <code>List</code> of <code>HMethod</code>s
	with the following properties:
	- The declaring class of the <code>HMethod</code> is
	<code>HClass</code>.
	- The <code>convert</code> method of the 
	<code>IntermediateCodeFactory</code> has been invoked
	on all the <code>HMethod</code>s in the <code>List</code>. 
	The <code>IntermediateCodeFactory</code> referred
	to here is the one returned by the <code>codeFactory</code>
	method of <code>this</code>. Returns null if the given 
	<code>HClass</code> does not declare any methods on which 
	<code>convert</code> has been invoked.
	- The <code>HMethod</code>s are ordered according to the
	order in which the <code>convert</code> method was invoked.
    */
    public abstract List getOrderedMethods(HClass hc);
    /** A <code>GCPoint</code> contains information about all
     *  the live objects that the garbage collector needs to
     *  add to the root set at a particular GC point.
     */
    public static class GCPoint {
	protected Instr gcPoint;
	protected Label label;
	protected Map regDerivations;
	protected Map stackDerivations;
	protected Set liveStackOffsetLocs;
	protected Set liveMachineRegLocs;
	protected Map calleeSaved;
	/** Creates a <code>GCPoint</code> object 
	    @param label
	           the <code>Label</code> identifying
		   the <code>gcPoint</code>
	    @param liveDerivations
	           a <code>Map</code> of pointer locations as
		   <code>Set</code>s of <code>CommonLoc</code>s to 
		   the corresponding derivation information as 
		   <code>DLoc</code>s
	    @param locations
	           a <code>Set</code> of <code>CommonLoc</code>s that
		   represent live pointers at the given GC point
	    @param calleeSaved
	           a <code>Map</code> of the callee-saved
		   <code>BackendDerivation.Register</code>s to
		   the <code>WrappedStackOffsetLoc</code>s 
		   where its contents has been stored
	*/
	public GCPoint(Instr gcPoint, Label label, Map liveDerivations,
		       Set locations, Map calleeSaved) {
	    this.gcPoint = gcPoint;
	    this.label = label;
	    this.calleeSaved = calleeSaved;
	    this.liveStackOffsetLocs = new HashSet();
	    this.liveMachineRegLocs = new HashSet();
	    filter(liveDerivations);
	    filter(locations);
	}
	// Sorts derivations by location type of derived pointer
	private void filter(Map derivations) {
	    regDerivations = new HashMap();
	    stackDerivations = new HashMap();
	    for(Iterator keys=derivations.keySet().iterator(); 
		keys.hasNext(); ) {
		Set key = (Set)keys.next();
		DLoc derivation = (DLoc)derivations.get(key);
		for(Iterator it=key.iterator(); it.hasNext(); ) {
		    CommonLoc loc = (CommonLoc)it.next();
		    switch(loc.kind()) {
		    case StackOffsetLoc.KIND:
			WrappedStackOffsetLoc wsol =
			    new WrappedStackOffsetLoc((StackOffsetLoc)loc);
			stackDerivations.put(wsol, derivation); 
			break;
		    case MachineRegLoc.KIND:
			WrappedMachineRegLoc wmrl = 
			    new WrappedMachineRegLoc((MachineRegLoc)loc);
			regDerivations.put(wmrl, derivation); 
			break;
		    default:
			Util.assert(false);		    
		    }
		}
	    }
	}
	// Sorts the various locations by type
	private void filter(Set locations) {
	    for(Iterator it = locations.iterator(); it.hasNext(); ) {
		CommonLoc loc = (CommonLoc)it.next();
		switch(loc.kind()) {
		case StackOffsetLoc.KIND:
		    WrappedStackOffsetLoc wsol =
			new WrappedStackOffsetLoc((StackOffsetLoc)loc);
		    liveStackOffsetLocs.add(wsol); 
		    break;
		case MachineRegLoc.KIND:
		    WrappedMachineRegLoc wmrl = 
			new WrappedMachineRegLoc((MachineRegLoc)loc);
		    liveMachineRegLocs.add(wmrl); 
		    break;
		default:
		    Util.assert(false);
		}
	    }
	}
	/** Returns the <code>Label</code> identifying the GC point */
	public Label label() { return label; }
	/** Returns an unmodifiable <code>Map</code> of live derived 
	    pointers in <code>WrappedMachineRegLoc</code>s to the 
	    derivation information in the form of <code>DLoc</code>s 
	    for that GC point */
	public Map regDerivations() { 
	    return Collections.unmodifiableMap(regDerivations); 
	}
	/** Returns an unmodifiable <code>Map</code> of live derived 
	    pointers in <code>StackOffsetLoc</code>s to the 
	    derivation information in the form of <code>DLoc</code>s 
	    for that GC point */
	public Map stackDerivations() { 
	    return Collections.unmodifiableMap(stackDerivations); 
	}
	/** Returns an unmodifiable <code>Set</code> of live, 
	    non-derived pointers at this GC point as
	    <code>WrappedStackOffsetLoc</code>s */
	public Set liveStackOffsetLocs() { 
	    return Collections.unmodifiableSet(liveStackOffsetLocs); 
	}
	/** Returns an unmodifiable <code>Set</code> of live, 
	    non-derived pointers at this GC point as
	    <code>WrappedMachineRegLoc</code>s */
	public Set liveMachineRegLocs() { 
	    return Collections.unmodifiableSet(liveMachineRegLocs); 
	}
	/** Returns an unmodifiable <code>Map</code> of callee-saved
	    <code>BackendDerivation.Register</code>s to the
	    <code>CommonLoc</code>s where its contents has been stored */
	public Map calleeSaved() {
	    return Collections.unmodifiableMap(calleeSaved); 
	}
    }
    /** <code>WrappedMachineRegLoc</code> is a wrapper object for
	<code>MachineRegLoc</code>s that implement special
	<code>equals</code> and <code>hashCode</code> methods.
	Two <code>WrappedMachineRegLoc</code> objects are equal
	if the underlying <code>MachineRegLoc</code>s represent
	the same register.
    */
    public static class WrappedMachineRegLoc {
	protected MachineRegLoc mrl;
	/** Creates a <code>WrappedMachineRegLoc</code> object */
	public WrappedMachineRegLoc(MachineRegLoc mrl) {
	    this.mrl = mrl;
	}
	/** Returns the abstract index of underlying 
	    <code>MachineRegLoc</code> in the register file.
	    The index returned is identical to what is
	    returned by the <code>regIndex</code> method of
	    the underlying <code>MachineRegLoc</code> object.
	*/
	public int regIndex() {
	    return mrl.regIndex();
	}
	/** Compares two <code>WrappedMachineRegLoc</code> objects
	    for equality. Two <code>WrappedMachineRegLoc</code>
	    objects are equal if they represent the same register.
	*/
	public boolean equals(Object obj) {
	    try {
		return (regIndex() == ((WrappedMachineRegLoc)obj).regIndex());
	    } catch (ClassCastException cce) {
		return false;
	    }
	}
	/** Returns the hash code value for the object. Two
	    <code>WrappedMachineRegLoc</code> return the same hash 
	    code if the underlying <code>MachineRegLoc</code>
	    objects represent the same register.
	*/
	public int hashCode() {
	    // okay, i'm cheating quite a bit in this implementation
	    // but this s the simplest way I can think of to ensure 
	    // that two WrappedMachineRegLoc objects referring to the 
	    // same abstract register are equal
	    return ((new Integer(regIndex())).hashCode());
	}
    }
    /** <code>WrappedStackOffsetLoc</code> is a wrapper object for
	<code>StackOffsetLoc</code>s that implement special
	<code>equals</code> and <code>hashCode</code> methods.
	Two <code>StackOffsetLoc</code> objects are equal
	if the underlying <code>StackOffsetLoc</code>s represent
	the same stack offset.
    */
    public static class WrappedStackOffsetLoc {
	protected StackOffsetLoc sol;
	/** Creates a <code>WrappedStackOffsetLoc</code> object */
	public WrappedStackOffsetLoc(StackOffsetLoc sol) {
	    this.sol = sol;
	}
	/** Returns the abstract stack offset of underlying 
	    <code>StackOffsetLoc</code>. The absolute location
	    in memory to which a stack offset refers is
	    context-dependent. For example, the stack offset of
	    an object in one method may have the same abstract
	    value as the stack offset of another object in a
	    different method, but they do not necessarily refer
	    to the same absolute location in the stack.
	    The stack offset returned is identical to what is
	    returned by the <code>stackOffset</code> method of
	    the underlying <code>StackOffsetLoc</code> object.
	*/
	public int stackOffset() {
	    return sol.stackOffset();
	}
	/** Compares two <code>WrappedStackOffsetLoc</code> objects
	    for equality. Two <code>WrappedStackOffsetLoc</code>
	    objects are equal if they refer to the same abstract
	    stack offset.
	*/
	public boolean equals(Object obj) {
	    try {
		return (stackOffset() == 
			((WrappedStackOffsetLoc)obj).stackOffset());
	    } catch (ClassCastException cce) {
		return false;
	    }
	}
	/** Returns the hash code value for the object. Two
	    <code>WrappedStackOffsetLoc</code> return the same hash 
	    code if the underlying <code>StackOffsetLoc</code> refer
	    to the same abstract stack offset.
	*/
	public int hashCode() {
	    // okay, i'm cheating quite a bit in this implementation
	    // but this s the simplest way I can think of to ensure 
	    // that two WrappedStackOffsetLoc objects referring to the 
	    // same abstract stack offset are equal
	    return ((new Integer(stackOffset())).hashCode());
	}
    }
    /** 
	Derivation information stored as <code>CommonLoc</code>s.
     */
    public static class DLoc {
	/** Arrays of base pointer locations */
	public final WrappedMachineRegLoc[] regLocs;
	public final WrappedStackOffsetLoc[] stackLocs;
	/** Arrays of booleans */
	public final boolean[] regSigns;
	public final boolean[] stackSigns;
	/** Constructor. */
	public DLoc(WrappedMachineRegLoc[] regLocs, 
		    WrappedStackOffsetLoc[] stackLocs,
		    boolean[] regSigns, boolean[] stackSigns) {
	    this.regLocs = regLocs;
	    this.stackLocs = stackLocs;
	    this.regSigns = regSigns;
	    this.stackSigns = stackSigns;
	}
    }
}

