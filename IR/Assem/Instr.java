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

import java.util.Vector;
import java.util.List;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import java.util.AbstractCollection;

/**
 * <code>Instr</code> is the primary class for representing
 * assembly-level instructions used in the Backend.* packages.
 *
 * @author  Andrew Berkheimer <andyb@mit.edu>
 * @version $Id: Instr.java,v 1.1.2.32 1999-08-28 00:48:28 pnkfelix Exp $
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

    // for implementing HasEdges
    // contains Instrs which have Edges to this
    private Vector pred;
    // contains Instrs which this has Edges to
    private Vector succ;

    /** The <code>Instr</code> that is output prior to
	<code>this</code>.  Should be <code>null</code> iff
	<code>this</code> is the first instruction in the method. 
    */
    Instr prev;
    /** The <code>Instr</code> that is output prior to
	<code>this</code>.  Should be <code>null</code> iff
	<code>this</code> is the first instruction in the method. 
    */
    public Instr getPrev() { return prev; }

    /** The next <code>Instr</code> to output after
	<code>this</code>.  <code>next</code> can be significant
	for control flow, depending on if
	<code>this.canFallThrough</code>.  Should be <code>null</code>
	iff <code>this</code> is the last instruction in the method.
    */
    Instr next;
    /** The next <code>Instr</code> to output after
	<code>this</code>.  <code>next</code> can be significant
	for control flow, depending on if
	<code>this.canFallThrough</code>.  Should be <code>null</code>
	iff <code>this</code> is the last instruction in the method.
    */
    public Instr getNext() { return next; }

    /** Sets whether control flow can go to <code>this.next</code>.  
	Note that if 
	<code>(!this.canFallThrough) && (this.targets == null)</code>
	then <code>this</code> represents an exit point for the code
	and should be treated as such for data flow analysis, etc.
	@see Instr#next
    */
    public final boolean canFallThrough;

    /** List of target labels that <code>this</code> can branch to.
	<code>getTargets()</code> may be empty (in which case control
	flow either falls through to the <code>this.next</code> (the
	case if <code>this.canFallThrough</code>), or returns to some
	unknown <code>Instr</code> (the case for 'return'
	statements)). 
	@see Instr#canFallThrough
	@see Instr#next
	@see Instr#hasUnmodifiableTargets 
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
    private List targets;

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
	@param targets List of targets that control flow could
	potentially branch to.
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
        this.id = inf.getUniqueID();
        this.inf = inf;
        this.assem = assem; this.dst = dst; this.src = src;

	this.pred = new Vector();
	this.succ = new Vector();
	
	this.hashCode = (id<<5) + inf.getParent().getName().hashCode();
	if (inf.getMethod() != null) {
	    this.hashCode ^= inf.getMethod().hashCode(); 
	}

	this.canFallThrough = canFallThrough;
	this.targets = targets;
    }

    /** Creates an <code>Instr</code> consisting of the
	<code>String</code> <code>assem</code> and the lists of
	destinations and sources in <code>dst</code> and
	<code>src</code>. 
    */    
    public Instr(InstrFactory inf, HCodeElement source, 
		 String assem, Temp[] dst, Temp[] src) {
	this(inf, source, assem, dst, src, true, null);
    }

    /** Creates an <code>Instr</code> consisting of the String assem
     *  and the list of sources in src. The list of destinations is
     *  empty. */
    public Instr(InstrFactory inf, HCodeElement source,
		 String assem, Temp[] src) {
        this(inf, source, assem, null, src);
    }

    /** Creates an <code>Instr</code> consisting of the String assem.
     *  The lists of sources and destinations are empty. */
    public Instr(InstrFactory inf, HCodeElement source, String assem) {
        this(inf, source, assem, null, null);
    }

    // ********* INSTR METHODS ********

//      /** Creates an <code>Edge</code> which connects <code>src</code>
//       *  to <code>dest</code> */
//      public static Edge addEdge(Instr src, Instr dest) {
//  	Util.assert(src != null);
//  	Util.assert(dest != null);

//  	Edge e = new Edge(src, dest);

//  	src.succ.addElement(e);
//  	dest.pred.addElement(e);

//  	return e;
//      }

//      /** Removes an <code>Edge</code> which previously connected
//  	<code>src</code> to <code>dest</code> 
//      */
//      public static void removeEdge(Instr src, Instr dest) {
//  	Util.assert(src != null);
//  	Util.assert(dest != null);

//  	/* ADB: dangerous way of doing this, should be done better in
//           * future. XXX */
//  	Enumeration enum = src.succ.elements();
//  	while (enum.hasMoreElements()) {
//  	    HCodeEdge hce = (HCodeEdge)enum.nextElement();	
//              if (hce.to() == dest) {
//  		src.succ.removeElement(hce);
//              }
//          }
//  	enum = dest.pred.elements();
//          while (enum.hasMoreElements()) {
//  	    HCodeEdge hce = (HCodeEdge)enum.nextElement();
//              if (hce.from() == src) {
//  		dest.pred.removeElement(hce);
//              }
//          }
//      }


    // ******* THE BELOW METHODS NEED REPLACEMENT AND REVISION *********

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
	Util.assert(isLinear(newis), "newis must be a basic block");
	
	Instr next = oldi.next;
	Instr prev = oldi.prev;
	Instr newiF = (Instr) newis.get(0);
	Instr newiL = (Instr) newis.get(newis.size() - 1);

	if(prev!=null)prev.next = newiF;
	newiF.prev = prev;
	newiL.next = next;
	if(next!=null)next.prev = newiL;
	
    }

    /** Helper method to ensure that <code>instrs</code> is
	effectively a basic block. 
    */
    private static boolean isLinear(List instrs) {
	boolean linear = true;
	int index = 0;
	Instr i = (Instr) instrs.get(index);
	Instr next = null;

	while(index < instrs.size() &&
	      linear) {
	    if (! (i.canFallThrough &&
		   i.getTargets().isEmpty()) ) {
		linear = false;
	    }
	    
	    index++;
	    if (index < instrs.size()) {
		next = (Instr) instrs.get(index);
		if (next != i.next) linear = false;
		i = next;
	    } 
	}

	return linear;
    }
    
    /** Inserts <code>instr</code> at <code>edge</code>.
	<BR> <B>requires:</B> <OL>
	     <LI> <code>edge.from()</code> and <code>edge.to()</code>
	     are instances of <code>Instr</code> or one is
	     <code>null</code> and the other is an instance of
	     <code>Instr</code>. 
	     <LI> if <code>edge.from()</code> is not
	     <code>null</code>, then
	     <code>!edge.from().hasUnmodifiableTargets()</code>.  
	     <LI> <code>edge.to()</code> is a successor of
	     <code>edge.from()</code>.
	     <LI> <code>instr</code> is a non-branching instruction.
	</OL>
	<BR> <B>modifies:</B> <code>edge.from()</code>, <code>edge.to()</code>
	<BR> <B>effects:</B> changes <code>edge.from()</code> and
	     <code>edge.to()</code> so that after
	     <code>edge.from()</code> is executed, <code>instr</code>
	     will be executed and then followed by
	     <code>edge.to()</code>.
    */

    public static void insertInstrAt(Instr instr, HCodeEdge edge) {
	Util.assert(instr.getTargets().isEmpty(),
		    "instr should be nonbranching");

	if (edge.from() == null) {
	    if (edge.to() == null) {
		Util.assert(false, 
			    "edge shouldn't have null for both to and from");
	    } else {
		Instr to = (Instr) edge.to();
		
	    }
	} else {
	    Instr from = (Instr) edge.from();
	    from.next = instr;
	    instr.prev = from;
	    
	    if (edge.to() == null) {
		
	    } else {

	    }
	}
	
    }


//      /** Inserts <code>newi</code> as an Instr after <code>pre</code>,
//  	such that <code>pre</code>'s successors become
//  	<code>newi</code>'s successors, and <code>pre</code>'s only
//  	successor is <code>newi</code>. 
     
//          NOTE: Must review this method SEVERELY...it is easy to
//  	maintain it if it is merely inserting into the linear list of
//  	instrs, but if it is meant to modify the internal
//  	representation of control flow, we need to fix it and any code
//  	that uses it to do something else. 
//       */
//      public static void insertInstrAfter(Instr pre, Instr newi) {
//  	Util.assert(pre != null);
//          Util.assert(newi != null);

//  	newi.next = pre.next;
//  	pre.next = newi;
	
//          HCodeEdge[] oldsucc = pre.succ();
//  	for (int i = 0; i < oldsucc.length; i++) {
//  	    //removeEdge(pre, (Instr)oldsucc[i].to());
//  	    //addEdge(newi, (Instr)oldsucc[i].to());
//          }
//  	pre.succ = new Vector();
//  	//addEdge(pre, newi);
//      }

//      /** Inserts <code>newi</code> as an Instr before
//  	<code>post</code>, such that <code>post</code>'s predecessors
//  	become <code>newi</code>'s predecessors, and
//  	<code>post</code>'s only predecessor is <code>newi</code>.  

//          NOTE: Must review this method SEVERELY...it is easy to
//  	maintain it if it is merely inserting into the linear list of
//  	instrs, but if it is meant to modify the internal
//  	representation of control flow, we need to fix it and any code
//  	that uses it to do something else. 
//      */
//      public static void insertInstrBefore(Instr post, Instr newi) {
//          Util.assert(post != null);
//          Util.assert(newi != null);

//  	// InstrLABELs are the only Instrs with multiple entry
//  	// points; therefore we simply won't allow them to be used
//  	// here. 
//  	Util.assert(!(post instanceof InstrLABEL), 
//  		    "InstrLABELs are bad insertion points.");

//  	HCodeEdge[] oldpred = post.pred();
//  	for (int i = 0; i < oldpred.length; i++) {
//              //removeEdge((Instr)oldpred[i].from(), post);
//  	    //addEdge((Instr)oldpred[i].from(), newi);
//  	}
//  	post.pred = new Vector();
//  	//addEdge(newi, post);
//      }

    /** Accept a visitor. */
    public void visit(InstrVisitor v) { v.visit(this); }

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
        for (int i = 0; i < len; i++) 
            if (assem.charAt(i) == '`')
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
		    if (n < src.length) 
			s.append(targets.get(n));
		    else 
			s.append("L?");
		}
		break;
		case '`': 
		    s.append('`');
		    break;
                }
            else s.append(assem.charAt(i));

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

    // ******************* HCodeElement interface

    public String getSourceFile() { return source_file; }

    public int getLineNumber() { return source_line; }

    public int getID() { return id; }

    // ******************** HasEdges interface

    public HCodeEdge[] edges() { 
	HCodeEdge[] p = (HCodeEdge[]) pred.toArray();
	HCodeEdge[] s = (HCodeEdge[]) succ.toArray();
	HCodeEdge[] e = new HCodeEdge[p.length + s.length];
	System.arraycopy(p, 0, e, 0, p.length);
	System.arraycopy(s, 0, e, p.length, s.length);
	return e;
    }
    public Collection edgeC() {
	return new AbstractCollection() {
	    public int size() { return pred.size()+succ.size(); }
	    public Iterator iterator() {
		return new CombineIterator(new Iterator[] { pred.iterator(),
							    succ.iterator() });
	    }
	};
    }

    public HCodeEdge[] pred() {
	HCodeEdge[] edges = new HCodeEdge[pred.size()];
	pred.copyInto(edges);
	return edges;
    }
    public Collection predC() {
	return Collections.unmodifiableCollection(pred);
    }

    public HCodeEdge[] succ() { 
	HCodeEdge[] edges = new HCodeEdge[succ.size()];
	succ.copyInto(edges);
        return edges;
    }
    public Collection succC() {
	return Collections.unmodifiableCollection(succ);
    }

    /** Checks whether <code>this.targets</code> is modifiable. 
	Most instructions with a list of targets allow for dynamic
	replacement of elements of the targets list.  This way, branch
	targets can be modified to allow for easy insertion of 
	arbitrary fixup code on edges between <code>Instr</code>s by
	adding new branches and labels.  

	<P> For example: <BR>
	<code> beq L0
	       ...assembly code...
	       L0:
	</code> <BR>
	can be turned into: <BR>
	<code> beq L1
	       ...assembly code...
	       L1:
	       ...fixup code...
	       b L0
	       L0:
        </code>
	For those instructions, this method returns
	<code>true</code>.  

	<P> However, some instructions (such as computed branches)
	cannot have their targets list modified in such a manner.
	These instructions should be initialized with an anonymous
	inner class that overrides this method and returns
	<code>false</code>. 

	<P> An important invariant that must be preserved (and is high
	level enough that Tree form implementors must take note of it)
	is that 
	
	<P> for all n0, n1, n2 elem of Instr such that there exists an
	edge <nobr> &lt n0, n1 &gt </nobr> and an edge <nobr> &lt n1,
	n2 &gt </nobr>, 
	n0 doesn't have modifiable targets implies the edge 
	<nobr> &lt n0, n1 &gt </nobr> dominates the edge 
	<nobr> &lt n1, n2 &gt </nobr>.
	
	<P> In other words, <code>n1</code> should have no predecessors
	other than <code>n0</code>.
     */
    public boolean hasModifiableTargets() {
	return false;
    }
}
