// GCInfo.java, created Wed Jan 26 09:40:41 2000 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@tesuji.lcs.mit.edu>
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
import java.util.Iterator;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A <code>GCInfo</code> object collects data needed for
 * garbage collection and makes necessary annotations to
 * the instruction stream.
 * 
 * @author  Karen K. Zee <kkz@tesuji.lcs.mit.edu>
 * @version $Id: GCInfo.java,v 1.1.2.5 2000-02-10 23:54:02 kkz Exp $
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
	protected Map liveDerivations; // maps Temps to Derivation.DList 
	protected Set liveStackOffsetLocs;
	protected Set liveMachineRegLocs;
	/** Creates a <code>GCPoint</code> object 
	    @param label
	           the <code>Label</code> identifying
		   the <code>gcPoint</code>
	    @param liveDerivations
	           a <code>Map</code> of derived pointer locations as
		   <code>Set</code>s of <code>CommonLoc</code>s to 
		   the corresponding derivation information as 
		   <code>DLoc</code>s
	    @param locations
	           a <code>Set</code> of <code>CommonLoc</code>s that
		   represent live pointers at the given GC point 
	*/
	public GCPoint(Instr gcPoint, Label label, Map liveDerivations,
		       Set locations) {
	    this.gcPoint = gcPoint;
	    this.label = label;
	    this.liveDerivations = liveDerivations;
	    filter(locations);
	}
	// Sorts the various locations by type
	private void filter(Set locations) {
	    for(Iterator it = locations.iterator(); it.hasNext(); ) {
		CommonLoc loc = (CommonLoc)it.next();
		switch(loc.kind()) {
		case StackOffsetLoc.KIND:
		    liveStackOffsetLocs.add(loc); break;
		case MachineRegLoc.KIND:
		    liveMachineRegLocs.add(loc); break;
		default:
		    Util.assert(false);
		}
	    }
	}
	/** Returns the <code>Label</code> created at that GC point */
	public Label label() { return label; }
	/** Returns the <code>Map</code> of live Temps to 
	    <code>Derivation</code>s at that GC point */
	public Map liveDerivations() { return liveDerivations; }
	/** Returns the <code>Set</code> of live
	    <code>StackOffsetLoc</code>s at that GC point */
	public Set liveStackOffsetLocs() { return liveStackOffsetLocs; }
	/** Returns the <code>Set</code> of live
	    <code>MachineRegLoc</code>s at that GC point */
	public Set liveMachineRegLocs() { return liveMachineRegLocs; }
    }
    /** 
	Derivation information stored as <code>CommonLoc</code>s.
     */
    public static class DLoc {
	/** Arrays of base pointer locations */
	public final MachineRegLoc[] regLocs;
	public final StackOffsetLoc[] stackLocs;
	/** Arrays of booleans */
	public final boolean[] regSigns;
	public final boolean[] stackSigns;
	/** Constructor. */
	public DLoc(MachineRegLoc[] regLocs, StackOffsetLoc[] stackLocs,
		    boolean[] regSigns, boolean[] stackSigns) {
	    this.regLocs = regLocs;
	    this.stackLocs = stackLocs;
	    this.regSigns = regSigns;
	    this.stackSigns = stackSigns;
	}
    }
}
