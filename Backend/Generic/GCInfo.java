// GCInfo.java, created Wed Jan 26 09:40:41 2000 by kkz
// Copyright (C) 2000 Karen K. Zee <kkz@tesuji.lcs.mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Backend.Generic;

import harpoon.Analysis.Instr.RegAlloc.IntermediateCodeFactory;
import harpoon.Backend.Generic.Frame;
import harpoon.Backend.Generic.RegFileInfo.CommonLoc;
import harpoon.Backend.Generic.RegFileInfo.MachineRegLoc;
import harpoon.Backend.Generic.RegFileInfo.StackOffsetLoc;
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
 * @version $Id: GCInfo.java,v 1.1.2.4 2000-02-07 23:46:39 pnkfelix Exp $
 */
public abstract class GCInfo {
    // Maps methods to gc points
    protected Map m = new HashMap();
    /** Creates an <code>IntermediateCodeFactory</code> that
	prepares the code for garbage collection.
	<BR> <B>requires:</B> The <code>parentFactory</code>
	     in <code>Instr</code> form.
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
    public List gcPoints(HMethod hm) {
	return (List)m.get(hm);
    }
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
	           a <code>Map</code> of <code>Temp</code>s
		   to the corresponding 
		   <code>Derivation.DList</code>s
	    @param locations
	           a <code>Set</code> of <code>Temp</code>s
		   that implement the <code>CommonLoc</code>
		   interface
	 */
	public GCPoint(Instr gcPoint, Label label, Map liveDerivations,
		       Set locations) {
	    this.gcPoint = gcPoint;
	    this.label = label;
	    this.liveDerivations = liveDerivations;
	    filter(locations);
	}
	// Sorts the various locations by type
	// KKZ note to self: need to filter out non-pointers
	private void filter(Set locations) {
	    for(Iterator it = locations.iterator(); it.hasNext(); ) {
		CommonLoc loc = (CommonLoc)it.next(); // Temp
		if (loc.kind() == StackOffsetLoc.KIND) {
		    liveStackOffsetLocs.add(loc);
		} else if (loc.kind() == MachineRegLoc.KIND) {
		    liveMachineRegLocs.add(loc);
		} else {
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
}


