// Instr.java, created Mon Feb  8  0:33:19 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.CFGEdge;
import harpoon.IR.Properties.CFGraphable;
import harpoon.IR.Properties.UseDefable;
import harpoon.Temp.Label;
import harpoon.Temp.Temp;
import harpoon.Temp.TempMap;
import harpoon.Util.ArrayFactory;
import harpoon.Util.CombineIterator;
import harpoon.Util.Util;
import harpoon.Util.Default;
import harpoon.Util.UnmodifiableIterator;

import java.util.Vector;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.AbstractCollection;
import java.util.ArrayList;

/**
 * <code>Instr</code> is the primary class for representing
 * assembly-level instructions used in the Backend.* packages.
 *
 * Important Note: Most <code>Instr</code>s have only one predecessor.
 * One type of <code>Instr</code> with potentially more than one
 * predecessor is an <code>InstrLABEL</code>.  In any case, any code
 * that relies on the "only one predecessor"-invariant must check each
 * <code>Instr</code> with <code>hasMultiplePredecessors()</code>.
 * Likewise, extensions of <code>Instr</code> which are designed to
 * allow multiple predecessors must override <code>predC()</code> and
 * <code>hasMultiplePredecessors()</code>
 * 
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @author  Felix S. Klock II <pnkfelix@mit.edu>
 * @version $Id: Instr.java,v 1.3.2.2 2002-04-07 20:51:36 cananian Exp $ */
public class Instr implements HCodeElement, UseDefable, CFGraphable {
    private static boolean PRINT_UPDATES_TO_IR = false;
    private static boolean PRINT_REPLACES = false || PRINT_UPDATES_TO_IR;
    private static boolean PRINT_INSERTS = false || PRINT_UPDATES_TO_IR;
    private static boolean PRINT_REMOVES = false || PRINT_UPDATES_TO_IR;

    private final String assem; 
    private InstrFactory inf;

    private InstrGroup groupList = null;
    boolean partOf(InstrGroup.Type t) {
	InstrGroup curr = groupList;
	while(curr != null) {
	    if (curr.type == t) 
		return true;
	    curr = curr.containedIn;
	}
	return false;
    }
    /** Sets the InstrGroup sequence associated with
	<code>this</code>.  
	<BR><B>requires:</B> <code>this</code> is not 
	    associated with any group.
    */
    public void setGroup(InstrGroup seq) {
	assert groupList == null || groupList == seq : " current group: "+groupList+
		    " setGroup( "+seq+" )";
	// this actually isn't as conservative as we could be, since
	// <seq> can be null.

	groupList = seq;
    }
    /** Returns the Set of InstrGroups that this is an element of. */
    public Set getGroups() {
	HashSet set = new HashSet();
	InstrGroup curr = groupList;
	while(curr != null) {
	    set.add(curr);
	    curr = curr.containedIn;
	}
	return set;
    }
    /** Returns the entry <code>Instr</code> when the
	instructions are being viewed as collected by
	<code>type</code>.  Note that if <code>this</code> is not
	contained in any member of <code>type</code>, then
	<code>this</code> is its own group, and thus
	<code>this</code> is returned.
     */
    public Instr getEntry(InstrGroup.Type type) {
	return getRep(type, false);
    }
    /** Returns the exit <code>Instr</code> when the
	instructions are being viewed as collected by
	<code>type</code>.  Note that if <code>this</code> is not
	contained in any member of <code>type</code>, then
	<code>this</code> is its own group, and thus
	<code>this</code> is returned.
     */
    public Instr getExit(InstrGroup.Type type) {
	Instr i = getRep(type, true);
	return i;
    }
    private Instr getRep(InstrGroup.Type type, boolean retExit) {
	InstrGroup curr = groupList;
	while(curr != null) {
	    if (curr.type == type) {
		return retExit?curr.exit:curr.entry;
	    } else {
		curr = curr.containedIn;
	    }
	}
	// got here, no containing group is a member of <type>
	return this;
    }

    private final Temp[] dst;
    private final Temp[] src;

    private int hashCode;

    // for implementing HCodeElement
    private String source_file;
    private int source_line;
    private int id;

    /** The <code>Instr</code> that is output prior to
	<code>this</code>.  Should be <code>null</code> if
	<code>this</code> is the first instruction in the method or if
	<code>this</code> has not been inserted into an instruction
	stream (ie. <code>insertAt(CFGEdge)</code> has not been
	called since the last call to <code>remove()</code> or since
	construction). 
    */
    protected Instr prev;

    /** The <code>Instr</code> that is output prior to
	<code>this</code>.  Should be <code>null</code> if
	<code>this</code> is the first instruction in the method or if
	<code>this</code> has not been inserted into an instruction
	stream (ie. <code>insertAt(CFGEdge)</code> has not been
	called since the last call to <code>remove()</code> or since
	construction).
    */
    public Instr getPrev() { return prev; }

    /** The next <code>Instr</code> to output after
	<code>this</code>.  <code>next</code> can be significant
	for control flow, depending on if
	<code>this.canFallThrough</code>.  Should be <code>null</code>
	if <code>this</code> is the last instruction in the method or
	if <code>this</code> has not been inserted into an instruction
	stream (ie. <code>insertAt(CFGEdge)</code> has not been
	called since the last call to <code>remove()</code> or since
	construction).
    */
    protected Instr next;

    /** The next <code>Instr</code> to output after
	<code>this</code>.  <code>next</code> can be significant
	for control flow, depending on if
	<code>this.canFallThrough</code>.  Should be <code>null</code>
	if <code>this</code> is the last instruction in the method or
	if <code>this</code> has not been inserted into an instruction
	stream (ie. <code>insertAt(CFGEdge)</code> has not been
	called since the last call to <code>remove()</code> or since
	construction).
	@see Instr#canFallThrough
    */
    public Instr getNext() { return next; }

    /** Sets whether control flow can go to <code>this.next</code>.  
	Note that if <code>
	<nobr>(!this.canFallThrough)</nobr> && 
	<nobr>(this.targets == null)</nobr>
	</code>
	then <code>this</code> represents an exit point for the code
	and should be treated as such for data flow analysis, etc.
	@see Instr#getNext
    */
    public final boolean canFallThrough;

    private List<Label> targets;
    /** List of target labels that <code>this</code> can branch to.
	<code>getTargets()</code> may be empty (in which case control
	flow either falls through to the <code>this.next</code> (the
	case if <code>this.canFallThrough</code>), or returns to some
	unknown <code>Instr</code> (the case for 'return'
	statements)). 
	@see Instr#canFallThrough
	@see Instr#getNext
	@see Instr#hasModifiableTargets 
	@return A <code>List</code> of <code>Label</code>s.
    */
    public List<Label> getTargets() {
	if (targets != null) {
	    if (this.hasModifiableTargets()) {
		return targets;
	    } else {
		return Collections.unmodifiableList(targets);
	    }
	} else {
	    // (targets == null) ==> empty list
	    return Collections.EMPTY_LIST;
	}
    }

    /** Returns the <code>InstrLABEL</code> associated with
	<code>l</code> in the <code>InstrFactory</code> for
	<code>this</code>. 
    */
    public InstrLABEL getInstrFor(Label l) {
	return inf.labelToInstrLABELmap.get(l);
    }

    /** Defines an array factory which can be used to generate
	arrays of <code>Instr</code>s. 
    */
    public static final ArrayFactory<Instr> arrayFactory =
        new ArrayFactory<Instr>() {
            public Instr[] newArray(int len) { return new Instr[len]; }
        };

    // *************** CONSTRUCTORS *****************

    /** Creates an <code>Instr</code> consisting of the
	<code>String</code> <code>assem</code> and the list of
	destinations and sources in <code>dst</code> and
	<code>src</code>.
	@param inf <code>InstrFactory</code> for <code>this</code>
	@param source <code>HCodeElement</code> that was the source
	              for <code>this</code>
	@param assem Assembly code string for <code>this</code>
	@param dst Set of <code>Temp</code>s that may be written to in
	           the execution of <code>this</code>.
	@param src Set of <code>Temp</code>s that may be read from in 
	           the execution of <code>this</code>.
	@param canFallThrough Decides whether control flow could fall
	                      to <code>this.next</code>.
	@param targets List of target <code>Label</code>s that control
	               flow could potentially branch to.  If
		       <code>targets</code> is <code>null</code>, then
		       <code>this</code> is a non-branching instruction.
    */
    public Instr(InstrFactory inf, HCodeElement source, 
		 String assem, Temp[] dst, Temp[] src,
		 boolean canFallThrough, List<Label> targets) {
	assert !assem.trim().equals("FSK-LD `d0, `s0 `d0, `s0");

        assert inf != null;
        assert assem != null;
	// assert dst!=null && src!=null : "DST and SRC should not = null";
	if (src == null) src = new Temp[0];
	if (dst == null) dst = new Temp[0];
	
        this.source_file = (source != null)?source.getSourceFile():"unknown";
	this.source_line = (source != null)?source.getLineNumber(): 0;
        this.id = inf.getUniqueID();
        this.inf = inf;

	// update tail for instrFactory
	if (inf.cachedTail == null) {
	    inf.cachedTail = this;
	}
        this.assem = assem; this.dst = dst; this.src = src;

	this.hashCode = (id<<5) + inf.hashCode();
	if (inf.getMethod() != null) {
	    this.hashCode ^= inf.getMethod().hashCode(); 
	}

	this.canFallThrough = canFallThrough;
	this.targets = targets;

	checkForNull(this.src);
	checkForNull(this.dst);
    }

    private void checkForNull(Temp[] ts) {
	for(int i=0; i<ts.length; i++) {
	    assert ts[i] != null : "Temp index "+i+" is null in "+ this;
	}
    }

    /** Creates an <code>Instr</code> consisting of the
	<code>String</code> <code>assem</code> and the lists of
	destinations and sources in <code>dst</code> and
	<code>src</code>. 
	<code>canFallThrough</code> is set to <code>true</code> and
	<code>targets</code> is set to <code>null</code>. 
    */    
    public Instr(InstrFactory inf, HCodeElement source, 
		 String assem, Temp[] dst, Temp[] src) {
	this(inf, source, assem, dst, src, true, null);
    }

    /** Creates an <code>Instr</code> consisting of the String assem
	and the list of sources in src. The list of destinations is
	empty. 
	<code>canFallThrough</code> is set to <code>true</code> and
	<code>targets</code> is set to <code>null</code>.
    */
    public Instr(InstrFactory inf, HCodeElement source,
		 String assem, Temp[] src) {
        this(inf, source, assem, null, src);
    }

    /** Creates an <code>Instr</code> consisting of the String assem.
	The lists of sources and destinations are empty. 
	<code>canFallThrough</code> is set to <code>true</code> and
	<code>targets</code> is set to <code>null</code>.
    */
    public Instr(InstrFactory inf, HCodeElement source, String assem) {
        this(inf, source, assem, null, null);
    }

    // ********* INSTR METHODS ********

    /** Replaces <code>oldi</code> in the Instruction Stream with
	<code>newis</code>.  
	<BR> <B>requires:</B> 
             <OL>
	     <LI> <code>oldi</code> is a non-branching instruction 
	     <LI> <code>newis</code> is a <code>List</code> of
	     instructions such that the elements of <code>newis</code>
	     form a basic block. (this constraint may be weakened
	     later if necessary)  
	     </OL>
	<BR> <B>modifies:</B> <code>oldi.prev</code>, <code>oldi.next</code>
	<BR> <B>effects:</B> Modifies the <code>Instr</code>s
	     immediately dominating and succeeding <code>oldi</code>
	     as to substitute <code>newis</code> in the place of
	     <code>oldi</code>.   
    */
    public static void replaceInstrList(final Instr oldi, final List newis) {
	// System.out.println("("+oldi.prev+") "+oldi+" ("+oldi.next+")" + " -> " + newis); 
	assert oldi != null && newis != null : "Null Arguments are bad";
	assert oldi.canFallThrough &&
		    oldi.getTargets().isEmpty() : "oldi must be a nonbranching instruction.";

	// There's something wrong, either with my code or with the
	// jikes compiler, but it does CRAZY shit if i leave this in...
	//assert isLinear(newis) : "newis must be a basic block: "+pprint(newis);
	
	Instr last = oldi.prev;
	Iterator iter = newis.iterator();
	while(iter.hasNext()) { 
	    Instr i = (Instr) iter.next();
	    if (last != null) last.next = i;
	    i.prev = last;
	    last = i;
	}
	last.next = oldi.next;
	if (oldi.next != null) oldi.next.prev = last;
    }

    private static String pprint(List l) {
	String s="";
	Iterator instrs = l.iterator();
	while(instrs.hasNext()) {
	    Instr i = (Instr) instrs.next();
	    s += i.toString() + "\n";
	}
	return s;
    }

    /** Helper method to ensure that <code>instrs</code> is
	effectively a basic block. 
	Checks: each Instr 'i' in 'instrs' has only one successor
	(with regards to Control Flow) except for the last, which
	should be non-branching and have no next.
	
	This method actually requires instrs be linear right now;
	its more of a assertion macro than an actual information
	extraction method. 
    */
    private static boolean isLinear(List instrs) {
	boolean linear = true;
	Instr i = (Instr) instrs.get(0);
	Instr next = null;

	final Iterator iter = instrs.iterator();
	while(iter.hasNext()) {
	    Instr iterInstr = (Instr) iter.next();
	    
	    assert iterInstr == i : "list "+instrs+" is nonlinear";

	    int size = i.succC().size();
	    assert size >= 0 : "size should always be >= 0";
	    if (size == 0) {
		// reached the end (I hope)
		assert i.next == null : "last should have next == null";
		assert i.targets == null : "last should have targets == null";
		assert !iter.hasNext() : "last should have nothing left in iter "+iter.next();
		break;
	    }
	    /* XXX CSA: this code is broken: succC().iterator().next() will
	     * return an InstrEdge, not an Instr; not quite sure what this
	     * code was supposed to do/how to fix it. ***
	    Instr n = (Instr) i.succC().iterator().next();
	    if (i.next != n ||
		size > 1) {
		linear = false;
		//assert false : "Instr " + i + " is nonlinear";
		break;
	    }
	    */assert false : "this code is broken";
	}

	return linear;
    }
    
    /** Not implemented yet. */
    public static void insertInstrsAt(CFGEdge edge, List instrs) {
	assert false;
    }

    /** Inserts <code>this</code> at <code>edge</code>.  The purpose 
	of this insertion is to modify <I>control flow</I>, rather than just
	instruction layout.  See <code>layout(CFGEdge)</code> for
	direct modification of layout (which is less constrained than
	this method but is not intended for generic program
	transformation use.
	<BR> <B>requires:</B> <OL>
	     <LI> <code>edge.from()</code> and <code>edge.to()</code>
	          are instances of <code>Instr</code> or one is
		  <code>null</code> and the other is an instance of
		  <code>Instr</code>.  
	     <LI> if <code>edge.from()</code> is not
	          <code>null</code>, then
		  <code>edge.from().hasModifiableTargets()</code>.   
 	     <LI> if <code>edge.from()</code> and
	          <code>edge.to()</code> are not <code>null</code>,
		  then <code>edge.to()</code> is a successor of    
		  <code>edge.from()</code>.
	     <LI> <code>this</code> is a non-branching instruction
	          (ie, <code>this.targets</code> equals
		  <code>null</code> and
		  <code>this.canFallThrough</code>). 
	     <LI> <code>this</code> is not currently in an instruction
	          stream (ie <code>
		  <nobr>this.getPrev() == null</nobr></code> and <code>
		  <nobr>this.getNext() == null</nobr></code>).  This
		  is true for newly created <code>Instr</code>s and
		  for <code>Instr</code> which have just had their
		  <code>remove()</code> method called. 
	</OL>
	<BR> <B>modifies:</B> <code>edge.from()</code>, 
	     <code>edge.to()</code>, <code>this</code>
        <BR> <B>effects:</B> changes <code>edge.from()</code> and 
	     <code>edge.to()</code> so that after
	     <code>edge.from()</code> is executed, <code>this</code>
	     will be executed and then followed by
	     <code>edge.to()</code>.
        @see Instr#remove
    */
    public void insertAt(CFGEdge edge) {
	assert this.next == null &&
		    this.prev == null : "next and prev fields should be null";
	assert this.getTargets().isEmpty() &&
		    this.canFallThrough : "this should be nonbranching";
	assert edge.to() != null ||
		    edge.from() != null : "edge shouldn't have null for both to and from";

	Instr from = null, to = null;
	if (edge.from() != null) {
	    from = (Instr) edge.from();
	    assert from.targets==null || 
			from.hasModifiableTargets() : ("from: "+from+", if it branches, should have mutable target list");
	    assert edge.to() == null || 
			 from.edgeC().contains(edge) : "edge: "+edge+" should be in <from>.edges(): " + 
			 Util.print(from.edgeC());
	}
	if (edge.to() != null) {
	    to = (Instr) edge.to();
	    assert edge.from() == null || 
			 to.edgeC().contains(edge) : "edge should be in <to>.edges()";
	}
	
	// TODO: add code that will check if edge.from().next !=
	// edge.to(), in which case frame should create a new InstrLabel
	// and a new branch, and then we'll find a place to put them
	// in the code.  (add a pointer to the tail of the instruction
	// stream to InstrFactory)
	// then do: edge.from() -> newInstrLabel -> this -> 
	//          newBranch -> edge.to()
	if (from != null &&
	    from.next != edge.to()) {
	    assert this.inf != null : "InstrFactory should never be null";
	    Instr last = this.inf.getTail();
	    assert last != null : "cachedTail should not be null";
	    assert last.next == null : ("last Instr: "+last+" should really be LAST, "+
			"but it has next: " + last.next);
	    
	    // Oh shit.  How should we design a way to insert
	    // arbitrary code with a method in the *ARCHITECTURE
	    // INDEPENDANT* Instr class? 
	    assert false;
	} else { // edge.from() falls through to edge.to() 
	    layout(from, to);
	}
    }

    /** Removes <code>this</code> from its current place in the
	instruction layout.
	<BR> <B>requires:</B> <code>this</code> has a current location
	     in the instruction layout.
	     (ie <code>insertAt(CFGEdge)</code> or
	     <code>layout(Instr, Instr)</code> has been called 
	     since the last time <code>remove()</code> was called, or
	     since construction if <code>remove()</code> has never
	     been called.)
	<BR> <B>modifies:</B> <code>this</code>,
             <code>this.prev</code>, <code>this.next</code> 
	<BR> <B>effects:</B> removes <code>this</code> from its
	     current instruction stream, updating the
	     preceeding and succeeding <code>Instr</code>s accordingly
	     (preceeding and succeeding according to instruction
	     layout, not according to control flow) and setting the
	     <code>this.prev</code> and <code>this.next</code> fields
	     to <code>null</code>. 
	@see Instr#insertAt
	@see Instr#layout
    */    
    public void remove() {
	assert ! this.hasMultiplePredecessors();

	assert ((harpoon.Backend.Generic.Code)
		     inf.getParent()).getRootElement() != this : "Do not remove root element.";

	if (PRINT_REMOVES) System.out.println("removing Instr:"+this);

	for(Iterator groups=getGroups().iterator(); groups.hasNext();){
	    InstrGroup group = (InstrGroup) groups.next();
	    assert group.entry != this : "Can't handle group component removes yet (entry)";
	    assert group.exit != this : "Can't handle group component removes yet (exit)";
	}


	if (this.next != null) {
	    // remove ref to this in this.next
	    this.next.prev = this.prev; 
	}
	if (this.prev != null) {
	    // remove ref to this in this.prev
	    this.prev.next = this.next;
	}


	this.next = null;
	this.prev = null;
	/* remove mappings in inf.labelToBranchingInstrsSetMap */
	if (this.targets!=null) {
	    for (Iterator<Label> it=this.targets.iterator(); it.hasNext(); ) {
		Label l = it.next();
		((Set)inf.labelToBranches.get(l)).remove(this);
	    }
	}

    }

    /** Places <code>this</code> in the instruction layout between
	<code>from</code> and <code>to</code>.
	<BR> <B>requires:</B> <OL>
	     <LI> if <code>from</code> is not null, then
	          <code>from.getNext()</code> equals <code>to</code>. 
	     <LI> if <code>to</code> is not null, then
	          <code>to.getPrev()</code> equals <code>from</code>.
	     <LI> <code>this</code> is not currently in an instruction
	          stream (ie <code><nobr> this.getPrev() == null
		  </nobr></code> and <code><nobr> this.getNext() ==
		  null) </nobr></code>. 
		  This is true for newly created <code>Instr</code>s
		  and for <code>Instr</code> which have just had their 
		  <code>remove()</code> method called.
        </OL>
        <BR> <B>modifies:</B> <code>from</code>, <code>to</code>
        <BR> <B>effects:</B> Inserts <code>this</code> into the
	     instruction stream in between <code>from</code> and
	     <code>to</code>.  If <code>from</code> is null, then
	     <code>this</code> is laid out as the beginning of the
	     Instruction stream.  If <code>to</code> is null, then
	     <code>this</code> is laid out as the end of the
	     Instruction stream.
    */
    public void layout(Instr from, Instr to) { 
	assert this.next == null &&
		    this.prev == null : "next and prev fields should be null";
	
	if (PRINT_INSERTS) System.out.println("inserting Instr:"+this);

	if (to != null &&
	    from != null) {
	    assert to.prev == from &&
			from.next == to : "to should follow from in the instruction layout "+
			"if they already exist";
	}
	
	if (from!=null) from.next = this;
	this.prev = from;
	this.next = to;
	if (to!=null) to.prev = this;

	/* add this to inf.labelToBranchingInstrSetMap */
 	if (this.targets != null) {
	    Iterator<Label> titer = this.targets.iterator();
	    while(titer.hasNext()) {
		Label l = titer.next();
		((Set)inf.labelToBranches.
		 get(l)).add(this);
	    }
	}

	// choose group of whichever group is in the SURROUNDING area
	// (ie higher in the nested scopes)
	if (from != null && to != null) {
	    if (from.groupList != null && 
		from.groupList.subgroupOf( to.groupList )) {
		this.setGroup( to.groupList );
		// System.out.println("FSK: setGroup(to) code called (Instr.java) on "+this);
	    } else if (to.groupList != null &&
		       to.groupList.subgroupOf( from.groupList )) {
		this.setGroup( from.groupList );
		// System.out.println("FSK: setGroup(from) code called (Instr.java) on "+this);
	    }

	    // Else we're inserting an instruction inbetween two
	    // instructions which don't have a group in common, in
	    // which case we shouldn't assign a group to the new
	    // instruction at all.
	}
    }

    /* Replaces <code>inOld</code> with <code>inNew</code> in the
     * instruction layout. */
    public static void replace(Instr inOld, Instr inNew) {
	assert inNew.next==null && inNew.prev==null : "newI has a spot";
	assert inOld.next!=null || inOld.prev!=null : "oldI has no loc";

	if (PRINT_REPLACES) System.out.println("replacing Instr:"+inOld+" with Instr:"+inNew);

	inNew.setGroup(inOld.groupList);
	inOld.groupList = null;
	if (inNew.groupList != null) {
	    if (inNew.groupList.entry == inOld) inNew.groupList.entry = inNew;
	    if (inNew.groupList.exit == inOld) inNew.groupList.exit = inNew;
	    inNew.inf.addGroup( inNew.groupList );
	}

	inNew.layout(inOld, inOld.getNext());
	inOld.remove();
    }

    /** Create a new <code>Instr</code> identical to the receiver, but
     *  with all <code>Temp</code>s renamed according to the given
     *  mappings.  The new <code>Instr</code> will have no edges, and
     *  will come from the <code>InstrFactory</code> for
     *  <code>this</code>. 
     */
    public final Instr rename(TempMap tempmap) {
	return this.rename(tempmap, tempmap);
    }

    /** Create a new <code>Instr</code> identical to the receiver, but
     *  with all <code>Temp</code>s renamed according to the given
     *  mappings.  The new <code>Instr</code> will have no edges, and
     *  will come from the <code>InstrFactory</code> for
     *  <code>this</code>. 
     */
    public final Instr rename(TempMap defMap, TempMap useMap) {
	return this.rename(this.inf, defMap, useMap);
    }

    /** Create a new <code>Instr</code> identical to the receiver, but
     *  with all <code>Temp</code>s renamed according to the given
     *  mappings.  The new <code>Instr</code> will have no edges, and
     *  will come from the specified <code>InstrFactory</code>. 
     *
     * FSK: I don't think this "no edges" part of the specification
     * holds anymore (it was written before we revised the
     * control-flow representation in the Instrs...  
     */
    public Instr rename(InstrFactory inf, TempMap defMap, TempMap useMap) {
	return new Instr(inf, this, getAssem(),
			 map(defMap,dst), map(useMap,src),
			 this.canFallThrough, getTargets());
    }

    /** Accept a visitor. */
    public void accept(InstrVisitor v) { v.visit(this); }

    /** Returns the <code>InstrFactory</code> that generated this. */
    public InstrFactory getFactory() { return inf; }
    // shouldn't this return inf.clone()???????

    /** Returns the hashcode for this. */
    public int hashCode() { return hashCode; }

    public String getAssem() { return assem; }

    // ********* INTERFACE IMPLEMENTATIONS and SUPERCLASS OVERRIDES

    // ******************** Object overrides
 
    /** Returns a string representation of the <code>Instr</code>.  
	Note that while in the common case the <code>String</code>
	returned will match the executable assembly code for the
	<code>Instr</code>, this is not guaranteed.  To produce
	executable assembly in all cases, use
	<code>IR.Assem.Code.toAssem(Instr i)</code>.
    */
    public String toString() {
        int len = assem.length();
        StringBuffer s = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
	    switch(assem.charAt(i)) {
            case '`':
		if (i<len-1) switch (assem.charAt(++i)) {
		case 'd': { 
		    int n = Character.digit(assem.charAt(++i), 10);
		    if (n < dst.length) 
			s.append(dst[n]);
		    else 
			s.append("d?");
		}
		break;
		case 's': {
		    int n = Character.digit(assem.charAt(++i), 10);
		    if (n < src.length) 
			s.append(src[n]);
		    else 
			s.append("s?");
		}
		break;
		case 'L': {
		    int n = Character.digit(assem.charAt(++i), 10);
		    if (n < targets.size()) 
			s.append(targets.get(n));
		    else 
			s.append("L?");
		}
		break;
		case '`': 
		    s.append('`');
		    break;
                }
		break;
	    case '\n':
		// the below should still be valid, but its causing problems for global labels.
		// so I'm putting back the original, hard-to-read toString format.
		//s.append("\\n ");
		s.append("\n");
		break;
	    default:
		s.append(assem.charAt(i));
	    }
	}

        return s.toString();
    }

    // ******************** UseDefable Interface

    /** Returns the <code>Temp</code>s used by this <code>Instr</code>. */
    public final Temp[] use() { 
	Collection<Temp> u = useC();
	return u.toArray(new Temp[u.size()]);
    }

    /** Returns the <code>Temp</code>s defined by this <code>Instr</code>. */
    public final Temp[] def() { 
	Collection<Temp> d = defC();
	return d.toArray(new Temp[d.size()]);
    }
    public Collection<Temp> useC() { return Collections.unmodifiableList(Arrays.asList(src)); }
    public Collection<Temp> defC() { return Collections.unmodifiableList(Arrays.asList(dst)); }

    // ******************* HCodeElement interface

    public String getSourceFile() { return source_file; }

    public int getLineNumber() { return source_line; }

    public int getID() { return id; }

    // ******************** CFGraphable interface

    /** Returns the <I>control flow</I> edges of <code>this</code>.
	Note that this returns edges according to <I>control flow</I>, not in
	terms of instruction layout.  Look at <code>getNext()</code>
	and <code>getPrev()</code> for information on instruction
	layout. 
    */
    public InstrEdge[] edges() { 
	Collection<InstrEdge> c = edgeC();
	return c.toArray(new InstrEdge[c.size()]);
    }
    /** Returns the <I>control flow</I> edges of <code>this</code>.
	Note that this returns edges according to <I>control flow</I>, not in
	terms of instruction layout.  Look at <code>getNext()</code>
	and <code>getPrev()</code> for information on instruction
	layout. 
    */
    public Collection<InstrEdge> edgeC() {
	return new AbstractCollection<InstrEdge>() {
	    public int size() { return predC().size()+succC().size(); }
	    public Iterator<InstrEdge> iterator() {
		return new CombineIterator<InstrEdge>
		    (new Iterator<InstrEdge>[] { predC().iterator(),
						     succC().iterator() });
	    }
	};
    }

    /** Returns the <I>control flow</I> predecessors of <code>this</code>.
	Note that this returns edges according to <I>control flow</I>, not in
	terms of instruction layout.  Look at <code>getNext()</code>
	and <code>getPrev()</code> for information on instruction
	layout.  
	
	Uses <code>predC()</code> to get the necessary information.
    */
    public InstrEdge[] pred() {
	Collection<InstrEdge> c = predC();
	InstrEdge[] edges = new InstrEdge[c.size()];
	return c.toArray(edges);
    }
    /** Returns the <I>control flow</I> predecessors of <code>this</code>.
	Note that this returns edges according to <I>control flow</I>, not in
	terms of instruction layout.  Look at <code>getNext()</code>
	and <code>getPrev()</code> for information on instruction
	layout. 
    */
    public Collection<InstrEdge> predC() {
	assert !this.hasMultiplePredecessors() : "should not call Instr.predC() if instr"+
		    "has multiple predecessors...override method";
	return new AbstractCollection<InstrEdge>(){
	    public int size() {
		if ((prev != null) && prev.canFallThrough) {
		    return 1;
		} else {
		    return 0;
		}
	    }
	    public Iterator<InstrEdge> iterator() {
		if ((prev != null) && prev.canFallThrough) {
		    return Default.singletonIterator
			(new InstrEdge(prev, Instr.this));
		} else {
		    return Default.nullIterator;
		}
	    }
	};
    }

    /** Returns the <I>control flow</I> successors of <code>this</code>.
	Note that this returns edges according to <I>control flow</I>, not in
	terms of instruction layout.  Look at <code>getNext()</code>
	and <code>getPrev()</code> for information on instruction
	layout. 
    */
    public InstrEdge[] succ() { 
	Collection<InstrEdge> c = succC();
	InstrEdge[] edges = new InstrEdge[c.size()];
	return c.toArray(edges);
    }
    /** Returns the <I>control flow</I> successors of <code>this</code>.
	Note that this returns edges according to <I>control flow</I>, not in
	terms of instruction layout.  Look at <code>getNext()</code>
	and <code>getPrev()</code> for information on instruction
	layout. 
    */
    public Collection<InstrEdge> succC() {
	return new AbstractCollection<InstrEdge>() {
	    public int size() {
		int total=0;
		if (canFallThrough && (next != null)) {
		    total++;
		} 
		if (targets!=null) {
		    total += targets.size();
		}
		return total;
	    }
	    public Iterator<InstrEdge> iterator() {
		return new CombineIterator<InstrEdge>
		    (new Iterator<InstrEdge>[] {

			// first iterator: fall to next?
			(((next!=null)&&canFallThrough)?
			 Default.singletonIterator
			  (new InstrEdge(Instr.this,next)):
			 Default.nullIterator),

			// second iterator: branch to targets?
                        ((targets!=null)?
			 new UnmodifiableIterator<InstrEdge>(){
			    Iterator<Label> titer = targets.iterator();
			    public boolean hasNext() {
				return titer.hasNext();
			    }
			    public InstrEdge next() {
				return new InstrEdge
				    (Instr.this, 
				     inf.labelToInstrLABELmap.get
				     (titer.next())); 
			    }
			}:Default.nullIterator)

                    });
	    }
	};
    }

    /** Checks whether <code>this.targets</code> is modifiable. 
	Most instructions with a list of targets allow for dynamic
	replacement of elements of the targets list.  This way, branch
	targets can be modified to allow for easy insertion of 
	arbitrary fixup code on edges between <code>Instr</code>s by
	adding new branches and labels.  

	<P> For example: <BR><code>
	<TABLE CELLSPACING=0 CELLPADDING=0>
	<TR><TD> [<I> code block 1 </I>] 
	<TR><TD><code>beq L0</code></TD></TR>
	<TR><TD>[<I> code block 2 </I>]</TD></TR>
	<TR><TD><code>L0:</code></TD></TR>
	<TR><TD>[<I> code block 3, which does not fall through</I>]</TD></TR>
	</TABLE>
	</code><BR>
	can be turned into: <BR>
	<TABLE CELLSPACING=0 CELLPADDING=0>
	<TR><TD>[<I> code block 1 </I>]</TD></TR>
	<TR><TD><code>beq L1</code></TD></TR>
	<TR><TD>[<I> code block 2 </I>]</TD></TR>
	<TR><TD><code>L0:</code></TD></TR>
	<TR><TD>[<I> code block 3, which does not fall through </I>]</TD></TR>
	<TR><TD><code>L1:</code></TD></TR>
	<TR><TD>[<I> fixup code prefixing block 3 </I>]</TD></TR>
	<TR><TD><code>b L0</code></TD></TR>
	</TABLE>
        </code><BR>
	For such instructions, this method returns
	<code>true</code>.  

	<P> However, some instructions (such as computed branches)
	cannot have their targets list modified in such a manner  
	(the only way to safely insert code between blocks is to first
	ensure that a given computed branch is the only branch that
	jumps to the target label, and then insert the fixup code at
	the point of the label). 
	
	<P> Such instructions should be initialized with an anonymous 
	inner class that overrides this method and returns
	<code>false</code>.  

	<P> An important invariant that must be preserved (and is high
	level enough that Tree form implementors must take note of it)
	is that 
	
	<P> for all <I>n0</I>, <I>n1</I>, <I>n2</I> elem of Instr such
	that there exists an edge <nobr> &lt <I>n0</I>, <I>n1</I> &gt
	</nobr> and an edge <nobr> &lt <I>n1</I>, <I>n2</I> &gt
	</nobr>,  <I>n0</I> doesn't have modifiable targets implies
	the edge <nobr> &lt <I>n0</I>, <I>n1</I> &gt </nobr> dominates
	the edge <nobr> &lt <I>n1</I>, <I>n2</I> &gt </nobr>. 
	
	<P> In other words, <I>n1</I> should have no predecessors
	other than <I>n0</I>. 
     */
    public boolean hasModifiableTargets() {
	return true;
    }
    
    /** Checks if <code>this</code> has multiple predecessors.
	Most <code>Instr</code>s have either zero or one
	predecessors.  Any <code>Instr</code>s that can have more than
	one predecessor should override this method to return true. 
    */
    protected boolean hasMultiplePredecessors() {
	return false;
    }

    // ----------------------------------------------------
    // Useful for temp renaming.  Exported only to subclasses.
    protected final static Temp map(TempMap tm, Temp t) {
	return (t==null)?null:(tm==null)?t:tm.tempMap(t);
    }
    protected final static Temp[] map(TempMap tm, Temp[] ta) {
	Temp[] r = new Temp[ta.length];
	for (int i=0; i<r.length; i++)
	    r[i] = map(tm, ta[i]);
	return r;
    }
    protected final static Temp[][] map(TempMap tm, Temp[][] taa) {
	Temp[][] r = new Temp[taa.length][];
	for (int i=0; i<r.length; i++)
	    r[i] = map(tm, taa[i]);
	return r;
    }

    /** Clones this, mutating the assembly string.
	<BR> <B>requires:</B> newAssem != null
	<BR> <B>effects:</B> returns cloneMutateAssem(inf, newAssem)
    */
    public final Instr cloneMutateAssem(String newAssem) {
	return cloneMutateAssem(this.inf, newAssem);
    }

    /** Clones this, mutating the assembly string.
	<BR> <B>requires:</B> newAssem != null
	<BR> <B>effects:</B> Returns a new Instr object with the same
	     compiler-visible high level properties as
	     <code>this</code> (use/def, isMove, etc), but instead of
	     having the assembly-string of this, it has
	     <code>newAssem</code> as its assembly string.  The
	     generated instr will not have a a place in the
	     instruction layout; it is the responsiblity of the caller
	     to subsequently call Instr.replace to swap
	     <code>this</code> and the returned <code>Instr</code>.
    */
    public Instr cloneMutateAssem(InstrFactory inf, String newAssem) {
	return new Instr(inf, this, newAssem, 
			 this.dst, this.src, this.canFallThrough,
			 this.targets);
    }

    /** Returns true if this is a Move.
	<BR> <B>effects:</B> Returns true if the only effect of
	executing this instruction is to copy a set of source
	<code>Temp</code>s to a set of destination <code>Temp</code>s. 
    */
    public boolean isMove() { return false; }

    /** Returns true if this is an unconditional Jump.
	<BR> <B>effects:</B> Returns true if the only effect of
	executing this instruction is to shift control-flow to one
	target <code>Label</code> with no side-effects.
    */
    public boolean isJump() { return false; }
    
    
    /** Returns true if this is a dummy instruction to express
	register allocation constraints.
	<BR> <B>effects:</B> Returns true if this instruction has no
	     effect in itself, but instead is an artificial use of
	     some <code>Temp</code>(s) inserted as a note to the
	     register allocator not to insert a <em>Spill-Load</em>
	     before this instruction.
    */
    public boolean isDummy() { return false; }

    /** Returns true if this is a directive instruction that is meant
	to represent data stored in the code segment, not actual
	executable code.
    */
    public boolean isDirective() { return false; } 

    /** Returns true if this is a label instruction that is meant
	to mark a location in the code segment, not actual
	executable code.
    */
    public boolean isLabel() { return false; } 

}
