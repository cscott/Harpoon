// InstrGroup.java, created Fri Jan  5 11:25:20 2001 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGEdge;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.CFGraphable;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Properties.UseDefable;
import harpoon.Util.Util;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Arrays;
/**
 * <code>InstrGroup</code> collects a group of assembly instructions
 * together so that they can be viewed as a single atomic element of
 * the code.  This allows for differing levels of abstraction as
 * required by various compiler passes.  Each set of instructions
 * collected by an <code>InstrGroup</code> must collectively form a
 * single-entry single-exit region.
 * 
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: InstrGroup.java,v 1.1.2.4 2001-06-17 22:33:12 cananian Exp $ */
public class InstrGroup {
    Type type;
    Instr entry, exit;
    InstrGroup containedIn; // null ==> not contained in supergroup 

    /** Creates a <code>InstrGroup</code>. */
    public InstrGroup(Type type) {
	this.type = type;
	containedIn = null;
    }

    /** Creates a <code>InstrGroup</code> contained in
        <code>contained</code>. 
    */
    public InstrGroup(Type type, InstrGroup container) {
	this.type = type;
	containedIn = container;
    }
    
    public String toString() { 
	return "<group type:"+type.typeString+
	    ",entry:"+((entry==null)?"null":""+entry.getID())+
	    ",exit:"+((exit==null)?"null":""+exit.getID())+">";
    }

    /** Returns true if this is contained in a super group, else
	false. 
    */
    public boolean hasContainer() { return containedIn != null; }

    /** Returns the <code>InstrGroup</code> containing
	<code>this</code>, or <code>null</code> if this is not
	contained in any super group.
    */
    public InstrGroup getContainer() { return containedIn; }

    /** Returns true if this is (reflexively) contained in
	<code>group</code>.  */
    public boolean subgroupOf(InstrGroup group) {
	return (group != null) && 
	    (this == group || 
	     this.containedIn == group || 
	     (containedIn != null && 
	      containedIn.subgroupOf( group )));
    }

    /** Sets the entry point for a group of instructions.  Should be
	called once (and only once), before setExit is called.  */
    public void setEntry(Instr entry) { 
	Util.assert(this.entry == null);
	this.entry = entry; 
    }
    /** Sets the exit point for this group of instructions.  Should be
	called once (and only once).  Note that
	for a given InstrGroup(fact, a), after setExit(b) has been
	called, it should be the case for the code associated with
	<code>fact</code>: <UL>
	<LI> a dominates b
	<LI> b postdominates a
	<LI> a and b are cycle-equivalent</UL>.  */
    public void setExit(Instr exit) { 
	Util.assert(this.exit == null);
	this.exit = exit; 
    }

    /** <code>InstrGroup.GroupGrapher</code> turns an Instr -> Group map
	into a <code>CFGrapher</code>.  The predecessor and successor
	relations in <code>InstrGroup.Grapher</code> use the groups to
	abstract away the control flow and layout dictated by the
	Instrs internally, returning the entry points of each group
	when needed.  */
    static class GroupGrapher extends CFGrapher {
	private Map i2g;

	GroupGrapher(Map instrToGroup) { // local to Assem package
	    i2g = instrToGroup;
	}

	public HCodeElement[] getLastElements(HCode hcode) { 
	    HCodeElement[] hces = hcode.getLeafElements();
	    if (hces != null) { // null allowed?  has been so far...
		for(int i=0; i<hces.length; i++) {
		    InstrGroup ig = (InstrGroup) i2g.get(hces[i]);
		    if (ig != null)
			hces[i] = ig.entry;
		}
	    }
	    return hces;
	}
	public HCodeElement getFirstElement(HCode hcode) { 
	    return hcode.getRootElement();
	}

	public Collection predC(HCodeElement hc) { 
	    // get list of low-level preds
	    CFGEdge[] preds = ((Instr) hc).pred();
	    
	    // map low-level preds to starts of associated Groups
	    for(int i=0; i<preds.length; i++) {
		if (i2g.containsKey(preds[i].fromCFG())) {
		    InstrGroup ig = (InstrGroup) i2g.get(preds[i].fromCFG());
		    preds[i] = new InstrEdge(ig.entry,(Instr)preds[i].toCFG());
		}
	    }
	    return Arrays.asList(preds);
	}

	public Collection succC(HCodeElement hc) { 
	    Instr i = (Instr) hc;
	    if (!i2g.containsKey(i)) {
		return i.succC();
	    } else {
		// get list of succs from exit of this group
		final CFGEdge[] succs = ((InstrGroup)i2g.get(i)).exit.succ();

		// remap to have group entry as start
		for(int j=0; j<succs.length; j++) {
		    succs[j] = new InstrEdge(i, (Instr) succs[j].toCFG());
		}
		return Arrays.asList(succs); 
	    }
	}
    }
    /** <code>InstrGroup.GroupUseDefer</code> turns an Instr -> Group
	map into a <code>UseDefer</code>.  It does this by performing
	a reverse data-flow analysis, accumulating definitions, along
	with uses that have definitions originating <i>outside</i> of
	the group.  

	This actually might be a problem in the long term, because the
	register allocation needs to make sure that the assignments
	given do not have conflicts <i>within</i> the group itself.
	(This probably will not be a problem in most cases, because I
	think that the Temps used within the InstrGroup will be seen
	outside of the group as part of the defs, which cannot have
	overlaps, but still need to ensure this...)  

	The need for InstrDUMMYs illustrates one facet of this
	problem...  Perhaps the semantics of InstrGroups should be
	changed so that they represent two instructions: one
	containing all defs, and the next containing all uses?
    
    */
    static class GroupUseDefer extends UseDefer {
	private Map i2g;
	private Type t;
	GroupUseDefer(Map instrToGroup, Type t) { // local to Assem package
	    i2g = instrToGroup;
	    this.t = t;
	}

	// TODO verify correctness of useC/defC.  I did
	// guess-and-check a lot here (and this wasn't even the source
	// of the problem at the time!)

	// **NOTE** defC and useC are ASYMMETRIC.  
	public Collection useC(HCodeElement hce) { 
	    Instr i=(Instr)hce;

	    if (!i2g.containsKey(i)) {
		// FSK: PUT BACK AFTER INSTR IS COMMITTED
		// Util.assert(!i.partOf(t));
		return i.useC();
	    } else {
		InstrGroup ig = ((InstrGroup)i2g.get(i));
		if(i == ig.entry) {
		    return i.useC();
		}
		Util.assert(i == ig.exit);
		// else work backwards, gathering up uses but killing
		// defined uses...
		Instr curr = ig.exit;
		Collection set = new HashSet();
		do {
		    set.addAll(curr.useC());
		    Util.assert(curr.predC().size() == 1);
		    curr = (Instr) curr.pred()[0].from();
		    set.removeAll(curr.defC()); // order here is key
		} while(curr != ig.entry);

		return set;
	    }
	}
	public Collection defC(HCodeElement hce) { 
	    Instr i = (Instr) hce;
	    if (!i2g.containsKey(i)) {
		// FSK: PUT BACK AFTER INSTR IS COMMITTED
		// Util.assert(i.partOf(t));
		return i.defC();
	    } else {
		InstrGroup ig = (InstrGroup)i2g.get(i);
		if (i == ig.exit) {
		    return Collections.EMPTY_SET;
		} 
		// else gather up all defs and pass them out
		Util.assert(i == ig.entry);
		Instr curr = ig.exit;
		Collection set = new HashSet();

		do {
		    Util.assert(curr.predC().size() == 1);
		    curr = (Instr) curr.pred()[0].from();
		    set.addAll(curr.defC());
		} while(curr != ig.entry);

		return set;
	    }
	}
    }

    /** <code>InstrGroup.Factory</code> is responsible for maintaining
	a collection of <code>InstrGroup</code>s for a given
	<code>Assem.Code</code>.  */
    public static class Type {
	private String typeString;
	private String details;
	private Type(String groupType, String details) { 
	    typeString = groupType;
	    this.details = details;
	}

	/** Creates an <code>InstrGroup</code> of the type for
	    <code>this</code> representing <code>rep</code>.  Note
	    that <code>rep</code> should be the entry point for the
	    group of instructions that will be collected by the
	    returned <code>InstrGroup</code>.  */
	public InstrGroup makeGroup() {
	    return new InstrGroup(this);
	}
	/** Creates an <code>InstrGroup</code>, contained in
	    <code>container</code> of the type for <code>this</code>
	    representing <code>rep</code>.  Note that <code>rep</code>
	    should be the entry point for the group of instructions
	    that will be collected by the returned
	    <code>InstrGroup</code>.  If container is null, then the
	    generated InstrGroup will have no containing group. */
	public InstrGroup makeGroup(InstrGroup container) {
	    return new InstrGroup(this, container);
	}
	
	public String toString() { 
	    return "InstrGroup.Type:"+typeString; 
	}
    }

    /** groups code such as local labels (1f/1:).
     */
    public static Type NO_REORDER=new Type("ord","order sensitive dependencies");
    
    /** groups code such as double word moves where we do not want to
	allow one half of the location to be assigned to hold the 2nd
	half.
    */
    public static Type AGGREGATE=new Type("agg","aggregate instructions");

    /** groups code where we cannot insert spill code (such as
	aggregates where spill code would destroy effect of virtual
	DUMMY instructions added in).  Note that it is legal to insert
	spill code BEFORE the group, just not between instructions
	contained within the group.
    */
    public static Type NO_SPILL=new Type("!sp","spill sensitive dependencies");

    /** groups code with special delay slot instructions... (usable?)
     */
    public static Type DELAY_SLOT=new Type("del","delay slot");
    
}


