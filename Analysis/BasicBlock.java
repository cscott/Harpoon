// BasicBlock.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Util.Util;
import harpoon.Util.IteratorEnumerator;
import harpoon.Util.WorkSet;
import harpoon.Util.Worklist;
import harpoon.IR.Properties.HasEdges;

import harpoon.Analysis.DataFlow.ReversePostOrderEnumerator;

import java.util.Map;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
   BasicBlock collects a serial list of operations.  It is designed to
   abstract away specific operation and allow the compiler to focus on
   control flow at a higher level.  (It also allows for some analysis
   within the block to operate more efficiently by taking advantage of
   the fact that the elements of a BasicBlock have a total ordering of
   execution). 

   <BR> <B>NOTE:</B> right now <code>BasicBlock</code> only guarantees
   that it preserves the Maximal Basic Block property (where the first
   element is the entry point and the last element is the exit point)
   if the graph of operations is not modified while the basic block is
   in use.  However, other pieces of code WILL modify the Beginning
   and End of a basic block (for example, the register allocator will
   add LOADs to the beginning and STOREs to the end).  Perhaps we can
   allow for some modification of the Control-Flow-Graph; check with
   group. 
 *
 * @author  John Whaley
 * @author  Felix Klock <pnkfelix@mit.edu> 
 * @version $Id: BasicBlock.java,v 1.1.2.2 1999-10-21 23:06:09 pnkfelix Exp $
*/
public class BasicBlock {
    
    static protected final boolean DEBUG = false;
    static protected void db(String s) { System.out.println(s); }

    static private boolean CHECK_REP = true;

    static int BBnum = 0;
    
    HasEdges first;
    HasEdges last;
    Set pred_bb;
    Set succ_bb;
    protected int num;
    
    private BasicBlock root;
    private Set leaves;

    /** BasicBlock constructor.

	<BR> <B>requires:</B> <code>f</code> is the first element of
	                      the basic block and <code>l</code> is
			      the last element of the BasicBlock.
    */
    protected BasicBlock (HasEdges f, HasEdges l) {
	first = f; last = l; pred_bb = new HashSet(); succ_bb = new HashSet();
	num = BBnum++;
    }


    /** BasicBlock Iterator generator.
	<BR> <B>effects:</B> returns an <code>Iterator</code> over all
	of the <code>BasicBlock</code>s linked to and from
	<code>block</code>.  This <code>Iterator</code> will return
	each <code>BasicBlock</code> no more than once.
    */
    public static Iterator basicBlockIterator(BasicBlock block) { 
	HashSet set = new HashSet();
	WorkSet todo = new WorkSet();
	set.add(block);
	todo.push(block);
	while( !todo.isEmpty() ) {
	    BasicBlock doing = (BasicBlock) todo.pull(); 
	    Enumeration enum = doing.next(); 
	    while(enum.hasMoreElements()) { 
		BasicBlock b = (BasicBlock) enum.nextElement(); 
		if (set.add(b)) todo.push(b); 
	    } 
	    enum = doing.prev();  
	    while(enum.hasMoreElements()) {
		BasicBlock b = (BasicBlock) enum.nextElement(); 
		if (set.add(b)) todo.push(b); 
	    } 
	}
	return set.iterator();
    }

    
    /** BasicBlock generator.
	<BR> <B>requires:</B> 
	     <UL>
	     <LI> 1. <code>head</code> is an appropriate entry point
	             for a basic block (I'm working on eliminating
		     this requirement, but for now its safer to keep
		     it) 
	     <LI> 2. All <code>HCodeEdge</code>s linked to by the set
	             of <code>HasEdges</code> in the code body have
		     <code>HasEdges</code> objects in their
		     <code>to</code> and <code>from</code> fields.
		     <B>NOTE:</B> this really should be an implicit
		     invariant of <code>HasEdges</code>.  Convince 
		     Scott to change it or let us change it. 
	     </UL>
	<BR> <B>effects:</B>  Creates a set of
     	     <code>BasicBlock</code>s corresponding to the blocks
	     implicitly contained in <code>head</code> and the
	     <code>HasEdges</code> objects that <code>head</code>
	     points to, and returns the <code>BasicBlock</code> that
	     <code>head</code> is an instruction in.  The
	     <code>BasicBlock</code> returned is considered to be the
	     root (entry-point) of the set of <code>BasicBlock</code>s
	     created. 
    */
    public static BasicBlock computeBasicBlocks(HasEdges head) {
	// maps HasEdges 'e' -> BasicBlock 'b' starting with 'e'
	Hashtable h = new Hashtable(); 
	// stores BasicBlocks to be processed
	Worklist w = new WorkSet();

	while (head.pred().length == 1) {
	    head = (HasEdges) head.pred()[0].from();
	}
	
	BasicBlock first = new BasicBlock(head);
	h.put(head, first);
	w.push(first);
	
	first.root = first;
	first.leaves = new HashSet();

	while(!w.isEmpty()) {
	    BasicBlock current = (BasicBlock) w.pull();
	    
	    // 'last' is our guess on which elem will be the last;
	    // thus we start with the most conservative guess
	    HasEdges last = (HasEdges) current.getFirst();
	    boolean foundEnd = false;
	    while(!foundEnd) {
		int n = last.succ().length;
		if (n == 0) {
		    foundEnd = true;
		    first.leaves.add(current); 

		} else if (n > 1) { // control flow split
		    for (int i=0; i<n; i++) {
			HasEdges e_n = (HasEdges) last.succ()[i].to();
			BasicBlock bb = (BasicBlock) h.get(e_n);
			if (bb == null) {
			    h.put(e_n, bb=new BasicBlock(e_n));
			    bb.root = first; bb.leaves = first.leaves;
			    w.push(bb);
			}
			addEdge(current, bb);
		    }
		    foundEnd = true;
		    
		} else { // one successor
		    HasEdges next = (HasEdges) last.succ()[0].to();
		    int m = next.pred().length;
		    if (m > 1) { // control flow join
			BasicBlock bb = (BasicBlock) h.get(next);
			if (bb == null) {
			    bb = new BasicBlock(next);
			    bb.root = first; bb.leaves = first.leaves;
			    h.put(next, bb);
			    w.push(bb);
			}
			addEdge(current, bb);
			foundEnd = true;
			
		    } else { // no join; update our guess
			last = next;
		    }
		} 
	    }
	    current.setLast(last);
	}

	return (BasicBlock) h.get(head);
    }

    public HasEdges getFirst() { return first; }
    public HasEdges getLast() { return last; }
    
    public void addPredecessor(BasicBlock bb) { pred_bb.add(bb); }
    public void addSuccessor(BasicBlock bb) { succ_bb.add(bb); }
    
    public int prevLength() { return pred_bb.size(); }
    public int nextLength() { return succ_bb.size(); }
    public Enumeration prev() { return new IteratorEnumerator(pred_bb.iterator()); }
    public Enumeration next() { return new IteratorEnumerator(succ_bb.iterator()); }
    
    /** Returns an <code>Enumeration</code> of <code>HasEdges</code>
	within <code>this</code>.  
    */
    public Enumeration elements() {
	return new IteratorEnumerator(listIterator());
    }

    /** Returns an unmodifiable <code>Iterator</code> for the
	<code>HasEdges</code>s within <code>this</code>.
	The <code>Iterator</code> returned will iterate through the
	instructions according to their order in the program.
    */
    public Iterator iterator() {
	return listIterator();
    }

    /** Returns an unmodifiable <code>ListIterator</code> for the
	<code>HasEdges</code>s within <code>this</code>. 
	The <code>ListIterator</code> returned will iterate through the
	instructions according to their order in the program.
    */  
    public ListIterator listIterator() {
	return new ListIterator() {
	    HasEdges next = first;
	    HasEdges prev = null;
	    int index = 0;
	    public boolean hasNext() { return next != null; }
	    public boolean hasPrevious() { return prev != null; } // correct? 
	    public int nextIndex() { return index + 1; }
	    public int previousIndex() { return index - 1; } 
	    public Object next() {
		if (next == null) throw new NoSuchElementException();
		if (CHECK_REP) {
		    Util.assert((next == first) ||
				(next.pred().length == 1), 
				"BasicBlock REP error; non-first elem has only " + 
				"one predecessor\n" + BasicBlock.this.dumpElems());
		    Util.assert((next == last) ||
				(next.succ().length == 1),
				"BasicBlock REP error; non-last elem has only " + 
				"one successor\n" + BasicBlock.this.dumpElems());
		}
		prev = next; 
		if (next == last) next = null;
		else next = (HasEdges) next.succ()[0].to();
		index++;
		return prev; 
	    }		
	    public Object previous() {
		if (prev == null) throw new NoSuchElementException();
		Util.assert((prev == first) || (prev.pred().length == 1));
		Util.assert((prev == last) || (prev.succ().length == 1));
		next = prev;
		if (prev == first) prev = null;
		else prev = (HasEdges) prev.pred()[0].from();
		index--;
		return next;
	    }
	    
	    public void remove() {throw new UnsupportedOperationException();} 
	    public void set(Object o) {throw new UnsupportedOperationException();} 
	    public void add(Object o)  {throw new UnsupportedOperationException();} 
	};
    }

    /** Accept a visitor. */
    public void visit(BasicBlockVisitor v) { v.visit(this); }
    
    protected BasicBlock (HasEdges f) {
	first = f; last = null; pred_bb = new HashSet(); succ_bb = new HashSet();
	num = BBnum++;
    }

    /** Returns the root <code>BasicBlock</code>.
	<BR> <B>effects:</B> returns the <code>BasicBlock</code> that
	     is at the start of the set of <code>HasEdges</code>s
	     being analyzed. 
    */
    public BasicBlock getRoot() {
	return root;
    }

    /** Returns the leaf <code>BasicBlock</code>s.
	<BR> <B>effects:</B> returns a <code>Set</code> of
	     <code>BasicBlock</code>s that are at the ends of the
	     <code>HasEdge</code>s being analyzed.  
    */
    public Set getLeaves() {
	return Collections.unmodifiableSet(leaves);
    }

    protected void setLast (HasEdges l) {
	last = l;
	if (DEBUG) db(this+": from "+first+" to "+last);
    }
    
    public static void addEdge(BasicBlock from, BasicBlock to) {
	from.addSuccessor(to);
	to.addPredecessor(from);
	if (DEBUG) db("adding CFG edge from "+from+" to "+to);
    }
    
    public String toString() {
	return "BB"+num;
    }

    public String dumpElems() {
	CHECK_REP = false; // a hack; this method uses listIterator(),
	                   // which now calls dumpElems() (-->infinite
	                   // loop).  So we make the call to dumpElems
	                   // conditional 
	StringBuffer s = new StringBuffer();
	Iterator iter = listIterator();
	while(iter.hasNext()) {	    
	    s.append(iter.next() + "\n");
	}
	return s.toString();
    }
    
    public static void dumpCFG(BasicBlock start) {
	Enumeration e = new ReversePostOrderEnumerator(start);
	while (e.hasMoreElements()) {
	    BasicBlock bb = (BasicBlock)e.nextElement();
	    System.out.println("Basic block "+bb);
	    System.out.println("HasEdges in : "+bb.pred_bb);
	    System.out.println("HasEdges out: "+bb.succ_bb);
	    System.out.println();
	}
    }
    
}
