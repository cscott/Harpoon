// Instr.java, created Mon Feb  8  0:33:19 1999 by andyb
// Copyright (C) 1999 Andrew Berkheimer <andyb@mit.edu>
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.IR.Assem;

import harpoon.ClassFile.HCodeEdge;
import harpoon.ClassFile.HCodeElement;
import harpoon.IR.Properties.HasEdges;
import harpoon.IR.Properties.UseDef;
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
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.AbstractCollection;

/**
 * <code>Instr</code> is the primary class for representing
 * assembly-level instructions used in the Backend.* packages.
 *
 * Important Note: Most <code>Instr</code>s have only one
 * predecessor.  One type of <code>Instr</code> with more than
 * one predecessor is an <code>InstrLABEL</code>.  In any case, any
 * code that relies on the "only one predecessor"-invariant must
 * check each <code>Instr</code> with
 * <code>hasMultiplePredecessors()</code>.  Likewise, extensions of
 * <code>Instr</code> which are designed to allow multiple predecessors
 * must override <code>predC()</code> and
 * <code>hasMultiplePredecessors()</code>   
 * 
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @author  Felix S Klock <pnkfelix@mit.edu>
 * @version $Id: Instr.java,v 1.1.2.56 1999-10-31 19:54:29 cananian Exp $
 */
public class Instr implements HCodeElement, UseDef, HasEdges {
    private String assem;
    private InstrFactory inf;

    private Temp[] dst;
    private Temp[] src;

    private int hashCode;

    // for implementing HCodeElement
    private String source_file;
    private int source_line;
    private int id;

    /** The <code>Instr</code> that is output prior to
	<code>this</code>.  Should be <code>null</code> if
	<code>this</code> is the first instruction in the method or if
	<code>this</code> has not been inserted into an instruction
	stream (ie. <code>insertAt(HCodeEdge)</code> has not been
	called since the last call to <code>remove()</code> or since
	construction). 
    */
    protected Instr prev;

    /** The <code>Instr</code> that is output prior to
	<code>this</code>.  Should be <code>null</code> if
	<code>this</code> is the first instruction in the method or if
	<code>this</code> has not been inserted into an instruction
	stream (ie. <code>insertAt(HCodeEdge)</code> has not been
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
	stream (ie. <code>insertAt(HCodeEdge)</code> has not been
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
	stream (ie. <code>insertAt(HCodeEdge)</code> has not been
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

    private List targets;
    /** List of target labels that <code>this</code> can branch to.
	<code>getTargets()</code> may be empty (in which case control
	flow either falls through to the <code>this.next</code> (the
	case if <code>this.canFallThrough</code>), or returns to some
	unknown <code>Instr</code> (the case for 'return'
	statements)). 
	@see Instr#canFallThrough
	@see Instr#getNext
	@see Instr#hasModifiableTargets 
    */
    public List getTargets() {
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

    /** Defines an array factory which can be used to generate
	arrays of <code>Instr</code>s. 
    */
    public static final ArrayFactory arrayFactory =
        new ArrayFactory() {
            public Object[] newArray(int len) { return new Instr[len]; }
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
		 boolean canFallThrough, List targets) {
        Util.assert(inf != null);
        Util.assert(assem != null);
	// Util.assert(dst!=null && src!=null, "DST and SRC should not = null");
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
	if (targets != null) {
	    // add this to inf.labelToBranchingInstrSetMap
	    Iterator titer = targets.iterator();
	    while(titer.hasNext()) {
		Label l = (Label) titer.next();
		((Set)inf.labelToBranches.
		 get(l)).add(this);
	    }
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
    public static void replaceInstrList(Instr oldi, List newis) {
	Util.assert(oldi != null && newis != null, "Null Arguments are bad");
	Util.assert(oldi.canFallThrough &&
		    oldi.getTargets().isEmpty(), 
		    "oldi must be a nonbranching instruction.");
	Util.assert(isLinear(newis), "newis must be a basic block: " +
		    pprint(newis));
	
	Instr next = oldi.next;
	Instr prev = oldi.prev;
	Instr newiF = (Instr) newis.get(0);
	Instr newiL = (Instr) newis.get(newis.size() - 1);

	if(prev!=null)prev.next = newiF;
	newiF.prev = prev;
	newiL.next = next;
	if(next!=null)next.prev = newiL;
	
	String s = "";
	Instr i;
	if (prev != null) { 
	    i = prev;
	} else {
	    s += "null | ";
	    i = newiF;
	}
	while(i != null && 
	      i != next) {
	    s += i.toString() + " | ";
	    // if (i.next == null) System.out.println("next is null for " + i);
	    i = i.next;
	}
	if (i != null) {
	    s += i.toString();
	} else {
	    s += "null";
	}

	//System.out.println("Changed \n" + prev +" "+ oldi +" "+ next +"\n to " + s);
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
    */
    private static boolean isLinear(List instrs) {
	boolean linear = true;
	int index = 0;
	Instr i = (Instr) instrs.get(index);
	Instr next = null;

	while(true) {
	    int size = i.succC().size();
	    Util.assert(size >= 0, "size should always be >= 0");
	    if (size == 0) {
		// reached the end (I hope)
		Util.assert(i.next == null &&
			    i.targets == null,
			    "last instr should have next==targets==null");
		break;
	    }
	    Instr n = (Instr) i.succC().iterator().next();
	    if (i.next != n ||
		size > 1) {
		linear = false;
		//Util.assert(false,"Instr " + i + " is nonlinear");
		break;
	    }
	}

	return linear;
    }
    
    /** Not implemented yet. */
    public static void insertInstrsAt(HCodeEdge edge, List instrs) {
	
    }

    /** Inserts <code>this</code> at <code>edge</code>.  The purpose 
	of this insertion is to modify <I>control flow</I>, rather than just
	instruction layout.  See <code>layout(HCodeEdge)</code> for
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
    public void insertAt(HCodeEdge edge) {
	Util.assert(this.next == null &&
		    this.prev == null, 
		    "next and prev fields should be null");
	Util.assert(this.getTargets().isEmpty() &&
		    this.canFallThrough,
		    "this should be nonbranching");
	Util.assert(edge.to() != null ||
		    edge.from() != null, 
		    "edge shouldn't have null for both to and from");

	Instr from = null, to = null;
	if (edge.from() != null) {
	    from = (Instr) edge.from();
	    Util.assert(from.targets==null || 
			from.hasModifiableTargets(), 
			"from: "+from+", if it branches, should have mutable target list");
	    Util.assert( edge.to() == null || 
			 from.edgeC().contains(edge),
			 "edge: "+edge+" should be in <from>.edges(): " + 
			 Util.print(from.edgeC()));
	}
	if (edge.to() != null) {
	    to = (Instr) edge.to();
	    Util.assert( edge.from() == null || 
			 to.edgeC().contains(edge),
			 "edge should be in <to>.edges()");
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
	    Util.assert(this.inf != null, "InstrFactory should never be null");
	    Instr last = this.inf.getTail();
	    Util.assert(last != null, "cachedTail should not be null");
	    Util.assert(last.next == null, "last Instr: "+last+" should really be LAST, "+
			"but it has next: " + last.next);
	    
	    // Oh shit.  How should we design a way to insert
	    // arbitrary code with a method in the *ARCHITECTURE
	    // INDEPENDANT* Instr class? 
	    
	} else { // edge.from() falls through to edge.to() 
	    layout(from, to);
	}
    }

    /** Removes <code>this</code> from its current place in the
	instruction layout.
	<BR> <B>requires:</B> <code>this</code> has a current location
	     in the instruction layout.
	     (ie <code>insertAt(HCodeEdge)</code> or
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
    }

    /** Places <code>this</code> in the instruction layout between
	<code>from</code> and <code>to</code>.
	<BR> <B>requires:</B> <OL>
	     <LI> <code>from</code> and <code>to</code> are each
    	          instances of <code>Instr</code> or null
 	     <LI> if <code>from</code> and <code>to</code> are not
	          <code>null</code>, then <code>from.getNext()</code>
		  equals <code>to</code> and <code>to.getPrev()</code>
		  equals <code>from</code>.
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
	     <code>to</code>.
    */
    public void layout(Instr from, Instr to) { 
	Util.assert(this.next == null &&
		    this.prev == null, 
		    "next and prev fields should be null");
	if (to != null &&
	    from != null) {
	    Util.assert(to.prev == from &&
			from.next == to,
			"to should follow from in the instruction layout "+
			"if they already exist");
	}
	
	if(from!=null)from.next = this;
	this.prev = from;
	this.next = to;
	if(to!=null)to.prev = this;
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
	<code>Backend.Generic.Code.toAssem(Instr i)</code>.
    */
    public String toString() {
        StringBuffer s = new StringBuffer();
        int len = assem.length();
        for (int i = 0; i < len; i++) {
	    switch(assem.charAt(i)) {
            case '`':
                switch (assem.charAt(++i)) {
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

    // ******************** UseDef Interface

    /** Returns the <code>Temp</code>s used by this <code>Instr</code>. */
    public Temp[] use() { 
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, src); 
    }

    /** Returns the <code>Temp</code>s defined by this <code>Instr</code>. */
    public Temp[] def() { 
	return (Temp[]) Util.safeCopy(Temp.arrayFactory, dst);
    }
    public Collection useC() { return Collections.unmodifiableList(Arrays.asList(use())); }
    public Collection defC() { return Collections.unmodifiableList(Arrays.asList(def())); }

    // ******************* HCodeElement interface

    public String getSourceFile() { return source_file; }

    public int getLineNumber() { return source_line; }

    public int getID() { return id; }

    // ******************** HasEdges interface

    /** Returns the control flow edges of <code>this</code>.
	Note that this returns edges according to <I>control flow</I>, not in
	terms of instruction layout.  Look at <code>getNext()</code>
	and <code>getPrev()</code> for information on instruction
	layout. 
    */
    public HCodeEdge[] edges() { 
	Collection c = edgeC();
	return (HCodeEdge[]) c.toArray(new InstrEdge[c.size()]);
    }
    /** Returns the <I>control flow</I> edges of <code>this</code>.
	Note that this returns edges according to <I>control flow</I>, not in
	terms of instruction layout.  Look at <code>getNext()</code>
	and <code>getPrev()</code> for information on instruction
	layout. 
    */
    public Collection edgeC() {
	return new AbstractCollection() {
	    public int size() { return predC().size()+succC().size(); }
	    public Iterator iterator() {
		return new CombineIterator(new Iterator[] { predC().iterator(),
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
    public HCodeEdge[] pred() {
	Collection c = predC();
	HCodeEdge[] edges = new HCodeEdge[c.size()];
	return (HCodeEdge[]) c.toArray(edges);
    }
    /** Returns the <I>control flow</I> predecessors of <code>this</code>.
	Note that this returns edges according to <I>control flow</I>, not in
	terms of instruction layout.  Look at <code>getNext()</code>
	and <code>getPrev()</code> for information on instruction
	layout. 
    */
    public Collection predC() {
	Util.assert(!this.hasMultiplePredecessors(),
		    "should not call Instr.predC() if instr"+
		    "has multiple predecessors...override method");
	return new AbstractCollection(){
	    public int size() {
		if ((prev != null) && prev.canFallThrough) {
		    return 1;
		} else {
		    return 0;
		}
	    }
	    public Iterator iterator() {
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
    public HCodeEdge[] succ() { 
	Collection c = succC();
	HCodeEdge[] edges = new HCodeEdge[c.size()];
	return (HCodeEdge[]) c.toArray(edges);
    }
    /** Returns the <I>control flow</I> successors of <code>this</code>.
	Note that this returns edges according to <I>control flow</I>, not in
	terms of instruction layout.  Look at <code>getNext()</code>
	and <code>getPrev()</code> for information on instruction
	layout. 
    */
    public Collection succC() {
	return new AbstractCollection() {
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
	    public Iterator iterator() {
		return new CombineIterator
		    (new Iterator[] {

			// first iterator: fall to next?
			(((next!=null)&&canFallThrough)?
			 Default.singletonIterator
			  (new InstrEdge(Instr.this,next)):
			 Default.nullIterator),

			// second iterator: branch to targets?
                        ((targets!=null)?
			 new UnmodifiableIterator(){
			    Iterator titer = targets.iterator();
			    public boolean hasNext() {
				return titer.hasNext();
			    }
			    public Object next() {
				return new InstrEdge
				    (Instr.this, 
				     (Instr) inf.labelToInstrLABELmap.get
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
}
