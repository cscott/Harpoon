// InstrGroup.java, created Fri Jan  5 11:25:20 2001 by pnkfelix
// Copyright (C) 2000 Felix S. Klock II <pnkfelix@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCode;
import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGEdge;
import harpoon.IR.Properties.CFGrapher;
import harpoon.IR.Properties.CFGraphable;
import harpoon.IR.Properties.UseDefer;
import harpoon.IR.Properties.UseDefable;
import harpoon.Temp.Temp;
import harpoon.Util.Default;
import harpoon.Util.Util;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
/**
 * <code>InstrGroup</code> collects a group of assembly instructions
 * together so that they can be viewed as a single atomic element of
 * the code.  This allows for differing levels of abstraction as
 * required by various compiler passes.  Each set of instructions
 * collected by an <code>InstrGroup</code> must collectively form a
 * single-entry single-exit region.
 *
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: InstrGroup.java,v 1.5 2004-02-07 21:28:47 cananian Exp $ */
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
	assert this.entry == null;
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
	assert this.exit == null;
	this.exit = exit; 
    }

    /** <code>InstrGroup.GroupGrapher</code> turns an Instr -> Group map
	into a <code>CFGrapher</code>.  The predecessor and successor
	relations in <code>InstrGroup.Grapher</code> use the groups to
	abstract away the control flow and layout dictated by the
	Instrs internally, returning the entry points of each group
	when needed.  */
    static class GroupGrapher extends CFGrapher<Instr> {
	private Map<Instr,InstrGroup> i2g;

	GroupGrapher(Map<Instr,InstrGroup> instrToGroup) { // local to Assem package
	    i2g = instrToGroup;
	}

	public Instr[] getLastElements(HCode<Instr> hcode) { 
	    // FSK: is this even necessary?  It was w/o exit mapping,
	    // but w/ exit mapping, I think that this loop is just an
	    // identity transform.  Should look into it later.
	    Instr[] hces = hcode.getLeafElements();
	    if (hces != null) { // null allowed?  has been so far...
		for(int i=0; i<hces.length; i++) {
		    InstrGroup ig = i2g.get(hces[i]);
		    if (ig != null)
			hces[i] = ig.exit;
		}
	    }
	    return hces;
	}
	public Instr getFirstElement(HCode<Instr> hcode) { 
	    return hcode.getRootElement();
	}

	public List<HCodeEdge<Instr>> predC(Instr i) {
	    if (!i2g.containsKey(i)) {
		return /*Collections.*/unmodifiableList(i.predC());
	    } else {
		InstrGroup ig = i2g.get(i);
		if (i == ig.exit) {
		    return Collections.singletonList /* XXX: CAST DUE TO BUG */
			((HCodeEdge<Instr>)new InstrEdge(ig.entry, ig.exit));
		} else {
		    assert i == ig.entry;
		    return /*Collections.*/unmodifiableList(i.predC());
		}
	    }
	}

	public List<HCodeEdge<Instr>> succC(Instr i) { 
	    if (!i2g.containsKey(i)) {
		return /*Collections.*/unmodifiableList(i.succC());
	    } else {
		InstrGroup ig = i2g.get(i);
		if (i == ig.entry) {
		    return Collections.singletonList /* XXX:CAST DUE TO BUG */
			((HCodeEdge<Instr>)new InstrEdge(ig.entry, ig.exit));
		} else {
		    assert i == ig.exit;
		    return /*Collections.*/unmodifiableList(i.succC());
		}
	    }
	}
	/**XXX:BUG IN JAVAC, fixed in 1.5.0*/
	private List<HCodeEdge<Instr>> unmodifiableList(final List<InstrEdge> l) {
	    return new AbstractList<HCodeEdge<Instr>>() {
		public int size() { return l.size(); }
		public HCodeEdge<Instr> get(int i) { return l.get(i); }
	    };
	}
    }
    /** <code>InstrGroup.GroupUseDefer</code> turns an Instr -> Group
	map into a <code>UseDefer</code>.  It does this by performing
	a reverse data-flow analysis, accumulating definitions, along
	with uses that have definitions originating <i>outside</i> of
	the group.  
	<p>
	The produced <code>UseDefer</code> then maps all of the temps
	used by the set of instrs: (group \ {entry}) as coming from
	the exit of the group, and all of the temps defined by the
	entire group as coming from the entry of the group.  This is a
	conservative view; it ensures that the resulting register
	assignments will be distinct, even when they sometimes did not
	need to be.
    */
    static class GroupUseDefer extends UseDefer<Instr> {
	private Map<Instr,InstrGroup> i2g;
	private Type t;
	GroupUseDefer(Map<Instr,InstrGroup> instrToGroup, Type t) { // local to Assem package
	    i2g = instrToGroup;
	    this.t = t;
	}

	// **NOTE** defC and useC are ASYMMETRIC.  
	public Collection<Temp> useC(Instr i) { 
	    if (!i2g.containsKey(i)) {
		assert !i.partOf(t) : ("instr:"+i+" grp type:"+t);
		return i.useC();
	    } else {
		InstrGroup ig = i2g.get(i);
		if(i == ig.entry) {
		    return i.useC();
		}
		assert i == ig.exit;
		// else work backwards, gathering up uses but killing
		// defined uses...
		Instr curr = ig.exit;
		Collection<Temp> set = new HashSet<Temp>();
		do {
		    assert curr.getGroups().contains(ig);
		    set.addAll(curr.useC());
		    assert curr.predC().size() == 1;
		    curr = (Instr) curr.pred()[0].from();
		    set.removeAll(curr.defC()); // order here is key
		} while(curr != ig.entry);

		return set;
	    }
	}
	public Collection<Temp> defC(Instr i) { 
	    if (!i2g.containsKey(i)) {
		assert !i.partOf(t);
		return i.defC();
	    } else {
		InstrGroup ig = i2g.get(i);
		if (i == ig.exit) {
		    return Collections.EMPTY_SET;
		} 
		// else gather up all defs and pass them out
		assert i == ig.entry;
		Instr curr = ig.exit;
		
		// start with defs in last instr (shifting them to the
		// start of the group)
		Collection<Temp> set = new HashSet<Temp>( curr.defC() );

		do {
		    assert curr.getGroups().contains(ig);
		    assert curr.predC().size() == 1;
		    curr = (Instr) curr.pred()[0].from();
		    set.addAll(curr.defC());
		} while(curr != ig.entry);

		return set;
	    }
	}
    }

    /** <code>InstrGroup.Type</code> is associated with a collection
	of <code>InstrGroup</code>s for a given
	<code>Assem.Code</code>.  Each <code>InstrGroup.Type</code> is
	meant to be used as a way to extract abstract <i>views</i> of
	the <code>Assem.Code</code>.
    
    */
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


