package harpoon.Analysis.DataFlow;

/**
 * BasicBlock
 *
 * @author  John Whaley
 * @author  Felix Klock (pnkfelix@mit.edu)
 */

import harpoon.Util.*;
import harpoon.ClassFile.*;
import harpoon.IR.Properties.HasEdges;
import java.util.Map;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class BasicBlock {
    
    static final boolean DEBUG = false;
    static void db(String s) { System.out.println(s); }

    static int BBnum = 0;
    
    HasEdges first;
    HasEdges last;
    Set pred_bb;
    Set succ_bb;
    int num;
    
    /** BasicBlock constructor.

	<BR> <B>requires:</B> <code>f</code> is the first element of
	                      the basic block and <code>l</code> is
			      the last element of the BasicBlock.
    */
    public BasicBlock (HasEdges f, HasEdges l) {
	first = f; last = l; pred_bb = new HashSet(); succ_bb = new HashSet();
	num = BBnum++;
    }
    
    /** BasicBlock generator.
	<BR> <B>requires:</B> All <code>HCodeEdge</code>s linked to by
	     the set of <code>HasEdges</code> in the code body have
	     <code>HasEdges</code> objects in their <code>to</code> and
	     <code>from</code> fields.  <B>NOTE:</B> this really
	     should be an implicit invariant of <code>HasEdges</code>.
	     Convince Scott to change it or let us change it.
	<BR> <B>effects:</B>  Creates a set of BasicBlocks
	     corresponding to the blocks implicitly contained in
	     <code>head</code> and the <code>HasEdges</code> objects that
	     <code>head</code> points to, and returns the
	     <code>BasicBlock</code> that <code>head</code> is the
	     first instruction in. 
    */
    public static BasicBlock computeBasicBlocks(HasEdges head) {
	Hashtable h = new Hashtable();
	Worklist w = new WorkSet();

	BasicBlock first = new BasicBlock(head);
	h.put(head, first);
	w.push(first);
	
	while(!w.isEmpty()) {
	    BasicBlock current = (BasicBlock) w.pull();
	    HasEdges e = (HasEdges) current.getFirst();
	    for (;;) {
		int n = e.succ().length;
		if (n == 0) 
		    break; // end of method
		else if (n > 1) { // control flow split
		    for (int i=0; i<n; i++) {
			HasEdges e_n = (HasEdges) e.succ()[i];
			BasicBlock bb = (BasicBlock) h.get(e_n);
			if (bb == null) {
			    h.put(e_n, bb=new BasicBlock(e_n));
			    w.push(bb);
			}
			addEdge(current, bb);
		    }
		    break;
		} else {
		    HasEdges en = (HasEdges) e.succ()[0];
		    int m = en.pred().length;
		    if (m > 1) { // control flow join
			BasicBlock bb = (BasicBlock) h.get(en);
			if (bb == null) {
			    h.put(en, bb = new BasicBlock(en));
			    w.push(bb);
			}
			addEdge(current, bb);
			break;
		    } else {
			e = en;
		    }
		} 
	    }
	    current.setLast(e);
	}
	return (BasicBlock) h.get(head);
    }

    public HasEdges getFirst() { return first; }
    public HasEdges getLast() { return last; }
    
    public void addPredecessor(BasicBlock bb) { pred_bb.union(bb); }
    public void addSuccessor(BasicBlock bb) { succ_bb.union(bb); }
    
    public int prevLength() { return pred_bb.size(); }
    public int nextLength() { return succ_bb.size(); }
    public Enumeration prev() { return pred_bb.elements(); }
    public Enumeration next() { return succ_bb.elements(); }
    
    /** Returns an <code>Enumeration</code> of <code>HasEdges</code>
	within <code>this</code>.  
    */
    public Enumeration elements() {
	return new IteratorEnumerator(listIterator());
    }
    
    /** Returns an immutable <code>ListIterator</code> for the
	<code>HasEdges</code> within <code>this</code>. */  
    public ListIterator listIterator() {
	return new ListIterator() {
	    HasEdges current = first;
	    int index = 0;
	    public boolean hasNext() { return current != last; }
	    public boolean hasPrevious() { return current != first; } // correct? 
	    public int nextIndex() { return index + 1; }
	    public int previousIndex() { return index - 1; } 
	    public Object next() {
		if (current == null) throw new NoSuchElementException();
		Util.assert((current == first) || (current.pred().length == 1));
		Util.assert(current.succ().length == 1);
		HasEdges r = current;
		if (r == last) current = null;
		else current = (HasEdges) current.succ()[0].to();
		index++;
		return r;
	    }		
	    public Object previous() {
		if (current == null) throw new NoSuchElementException();
		Util.assert((current == last) || (current.succ().length == 1));
		Util.assert(current.pred().length == 1);
		HasEdges r = current;
		if (r == first) current = null;
		else current = (HasEdges) current.pred()[0].from();
		index--;
		return r;
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
