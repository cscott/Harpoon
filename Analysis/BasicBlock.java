// BasicBlock.java, created Wed Mar 10  9:00:53 1999 by jwhaley
// Copyright (C) 1998 John Whaley
// Licensed under the terms of the GNU GPL; see COPYING for details.
package harpoon.Analysis;

import harpoon.Util.Util;
import harpoon.Util.IteratorEnumerator;
import harpoon.Util.WorkSet;
import harpoon.Util.Worklist;
import harpoon.ClassFile.HCodeElement;
import harpoon.ClassFile.HCodeEdge;
import harpoon.IR.Properties.CFGrapher;

import harpoon.Analysis.DataFlow.ReversePostOrderEnumerator;

import java.util.Map;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.ArrayList;
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
 * @version $Id: BasicBlock.java,v 1.1.2.10 2000-01-09 09:17:37 pnkfelix Exp $
*/
public class BasicBlock {
    
    static protected final boolean DEBUG = false;
    static protected void db(String s) { System.out.println(s); }

    static private boolean CHECK_REP = true;

    static int BBnum = 0;
    
    HCodeElement first;
    HCodeElement last;
    CFGrapher grapher;
    Set pred_bb;
    Set succ_bb;
    protected int num;
    
    private BasicBlock root;
    private Set leaves;
    
    // each block contains a reference to a Map shared between all of
    // the blocks that it is connected to.  It provides a mapping from
    // every instr that is contained in these blocks to the BasicBlock
    // containing that instr. 
    private Map hceToBB; 

    /** Returns a <code>Map</code> from every
	<code>HCodeElement</code> that 	is contained in this
	collection of basic blocks to the <code>BasicBlock</code>
	containing that <code>HCodeElement</code>. 
    */
    public Map getHceToBB() {
	return Collections.unmodifiableMap(hceToBB);
    }


    /** BasicBlock constructor.

	<BR> <B>requires:</B> <code>f</code> is the first element of
	                      the basic block and <code>l</code> is
			      the last element of the BasicBlock.
    */
    protected BasicBlock (HCodeElement f, HCodeElement l, CFGrapher gr) {
	first = f; last = l; 
	pred_bb = new HashSet(); succ_bb = new HashSet();
	grapher = gr;
	num = BBnum++;
    }


    /** BasicBlock Iterator generator.
	<BR> <B>effects:</B> returns an <code>Iterator</code> over all
	of the <code>BasicBlock</code>s linked to and from
	<code>block</code>.  This <code>Iterator</code> will return
	each <code>BasicBlock</code> no more than once.
    */
    public static Iterator basicBlockIterator(BasicBlock block) { 
	ArrayList lst = new ArrayList();
	WorkSet todo = new WorkSet();
	lst.add(block);
	todo.push(block);
	while( !todo.isEmpty() ) {
	    BasicBlock doing = (BasicBlock) todo.pull(); 
	    Enumeration enum = doing.next(); 
	    while(enum.hasMoreElements()) { 
		BasicBlock b = (BasicBlock) enum.nextElement(); 
		if (!lst.contains(b)) {
		    lst.add(b);
		    todo.push(b);
		}
	    } 
	    enum = doing.prev();  
	    while(enum.hasMoreElements()) {
		BasicBlock b = (BasicBlock) enum.nextElement(); 
		if (!lst.contains(b)) {
		    lst.add(b);
		    todo.push(b); 
		}
	    } 
	}
	return lst.iterator();
    }

    
    /** BasicBlock generator.
	<BR> <B>requires:</B> 
	      <code>head</code> is an appropriate entry point for a
	      basic block (I'm working on eliminating this
	      requirement, but for now its safer to keep it)

	<BR> <B>effects:</B>  Creates a set of
	     <code>BasicBlock</code>s corresponding to the blocks
	     implicitly contained in <code>head</code> and the
	     <code>HCodeElement</code> objects that <code>head</code>
	     points to, and returns the <code>BasicBlock</code> that
	     <code>head</code> is an instruction in.  The
	     <code>BasicBlock</code> returned is considered to be the
	     root (entry-point) of the set of <code>BasicBlock</code>s
	     created.
    */
    public static BasicBlock computeBasicBlocks(HCodeElement head,
						final CFGrapher gr) {
	// maps from every hce 'h' -> BasicBlock 'b' such that 'b'
	// contains 'h' 
	HashMap hceToBB = new HashMap();

	// maps HCodeElement 'e' -> BasicBlock 'b' starting with 'e'
	Hashtable h = new Hashtable(); 
	// stores BasicBlocks to be processed
	Worklist w = new WorkSet();

	while (gr.pred(head).length == 1) {
	    head = gr.pred(head)[0].from();
	}
	
	BasicBlock first = new BasicBlock(head, gr);
	first.hceToBB = hceToBB;
	h.put(head, first);
	hceToBB.put(head, first);
	w.push(first);
	
	first.root = first;
	first.leaves = new HashSet();

	while(!w.isEmpty()) {
	    BasicBlock current = (BasicBlock) w.pull();
	    
	    // 'last' is our guess on which elem will be the last;
	    // thus we start with the most conservative guess
	    HCodeElement last = current.getFirst();
	    boolean foundEnd = false;
	    while(!foundEnd) {
		int n = gr.succC(last).size();
		if (n == 0) {
		    foundEnd = true;
		    first.leaves.add(current); 

		} else if (n > 1) { // control flow split
		    for (int i=0; i<n; i++) {
			HCodeElement e_n = gr.succ(last)[i].to();
			BasicBlock bb = (BasicBlock) h.get(e_n);
			if (bb == null) {
			    h.put(e_n, bb=new BasicBlock(e_n, gr));
			    bb.hceToBB = hceToBB;
			    hceToBB.put(e_n, bb);
			    bb.root = first; bb.leaves = first.leaves;
			    w.push(bb);
			}
			addEdge(current, bb);
		    }
		    foundEnd = true;
		    
		} else { // one successor
		    Util.assert(n == 1, "must have one successor");
		    HCodeElement next = gr.succ(last)[0].to();
		    int m = gr.predC(next).size();
		    if (m > 1) { // control flow join
			BasicBlock bb = (BasicBlock) h.get(next);
			if (bb == null) {
			    bb = new BasicBlock(next, gr);
			    bb.hceToBB = hceToBB;
			    bb.root = first; bb.leaves = first.leaves;
			    h.put(next, bb);
			    hceToBB.put(next, bb);
			    w.push(bb);
			}
			addEdge(current, bb);
			foundEnd = true;
			
		    } else { // no join; update our guess
			hceToBB.put(next, current);
			last = next;
		    }
		} 
	    }
	    current.setLast(last);
	}

	if (false) { // check that all reachables are included in our BasicBlocks somewhere.
	    HashSet checked = new HashSet();

	    ArrayList todo = new ArrayList();
	    todo.add(head);

	    while(!todo.isEmpty()) {
		HCodeElement hce =(HCodeElement) todo.remove(0);
 		BasicBlock bb = (BasicBlock) hceToBB.get(hce);
		Util.assert(bb != null, "hce "+hce+" should map to some BB");
		boolean missing = true;
		for(Iterator elems = bb.iterator(); missing && elems.hasNext(); ) {
		    HCodeElement o = (HCodeElement) elems.next();
		    if (hce.equals(o)) missing = false;
		}
		Util.assert(!missing, 
			    "hce "+hce+" should be somewhere in "+
			    "BB "+bb);
		checked.add(hce);
		Iterator predEdges = gr.predC(hce).iterator();
		while(predEdges.hasNext()) {
		    HCodeEdge edge = (HCodeEdge) predEdges.next();
		    if (!checked.contains(edge.from()) &&
			!todo.contains(edge.from())) {
			todo.add(edge.from());
		    } 
		}
		Iterator succEdges = gr.succC(hce).iterator();
		while(succEdges.hasNext()) {
		    HCodeEdge edge = (HCodeEdge) succEdges.next();
		    if (!checked.contains(edge.to()) &&
			!todo.contains(edge.to())) {
			todo.add(edge.to());
		    } 
		}
	    }
	}

	return (BasicBlock) h.get(head);
    }

    public HCodeElement getFirst() { return first; }
    public HCodeElement getLast() { return last; }
    
    public void addPredecessor(BasicBlock bb) { pred_bb.add(bb); }
    public void addSuccessor(BasicBlock bb) { succ_bb.add(bb); }
    
    public int prevLength() { return pred_bb.size(); }
    public int nextLength() { return succ_bb.size(); }
    public Enumeration prev() { return new IteratorEnumerator(pred_bb.iterator()); }
    public Enumeration next() { return new IteratorEnumerator(succ_bb.iterator()); }
    
    /** Returns an <code>Enumeration</code> of
	<code>HCodeElement</code>s within <code>this</code>.  
    */
    public Enumeration elements() {
	return new IteratorEnumerator(listIterator());
    }

    /** Returns an unmodifiable <code>Iterator</code> for the
	<code>HCodeElement</code>s within <code>this</code>.  The
	<code>Iterator</code> returned will iterate through the
	instructions according to their order in the program.  
    */
    public Iterator iterator() {
	return listIterator();
    }

    /** Returns an unmodifiable <code>ListIterator</code> for the
	<code>HCodeElement</code>s within <code>this</code>.  The
	<code>ListIterator</code> returned will iterate through the
	instructions according to their order in the program.
    */
    public ListIterator listIterator() {
	return new ListIterator() {
	    HCodeElement next = first;
	    HCodeElement prev = null;
	    int index = 0;
	    public boolean hasNext() { return next != null; }
	    public boolean hasPrevious() { return prev != null; } // correct? 
	    public int nextIndex() { return index + 1; }
	    public int previousIndex() { return index - 1; } 
	    public Object next() {
		if (next == null) throw new NoSuchElementException();
		if (CHECK_REP) {
		    Util.assert((next == first) ||
				(grapher.pred(next).length == 1), 
				new Util.LazyString() {
			public String eval() {
			    return "BasicBlock REP error; non-first elem has only " + 
				"one predecessor\n" +
				BasicBlock.this.dumpElems();
			}
		    });
		    Util.assert((next == last) ||
				(grapher.succ(next).length == 1),
				new Util.LazyString() {
			public String eval() {
			    return "BasicBlock REP error; non-last elem has only " + 
				"one successor\n" + BasicBlock.this.dumpElems();
			}
		    });
		}
		prev = next; 
		if (next == last) next = null;
		else next = grapher.succ(next)[0].to();
		index++;
		return prev; 
	    }		
	    public Object previous() {
		if (prev == null) throw new NoSuchElementException();
		Util.assert((prev == first) || (grapher.pred(prev).length == 1));
		Util.assert((prev == last) || (grapher.succ(prev).length == 1));
		next = prev;
		if (prev == first) prev = null;
		else prev = grapher.pred(prev)[0].from();
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
    
    protected BasicBlock (HCodeElement f, CFGrapher gr) {
	first = f; last = null; 
	pred_bb = new HashSet(); succ_bb = new HashSet();
	grapher = gr;
	num = BBnum++;
    }

    /** Returns the root <code>BasicBlock</code>.
	<BR> <B>effects:</B> returns the <code>BasicBlock</code> that
	     is at the start of the set of <code>HCodeElement</code>s
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

    protected void setLast (HCodeElement l) {
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
	    System.out.println("HCodeElement in : "+bb.pred_bb);
	    System.out.println("HCodeElement out: "+bb.succ_bb);
	    System.out.println();
	}
    }
    
}
