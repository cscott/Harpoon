package harpoon.Analysis.DataFlow;

/**
 * BasicBlock
 *
 * @author  John Whaley
 * @author  Felix Klock (pnkfelix@mit.edu)
 */

import harpoon.Util.*;
import harpoon.ClassFile.*;
import harpoon.IR.Properties.Edges;
import java.util.Map;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class BasicBlock {
    
    static final boolean DEBUG = false;
    static void db(String s) { System.out.println(s); }

    static int BBnum = 0;
    
    Edges first;
    Edges last;
    Set pred_bb;
    Set succ_bb;
    int num;
    
    /** BasicBlock constructor.

	<BR> <B>requires:</B> <code>f</code> is the first element of
	                      the basic block and <code>l</code> is
			      the last element of the BasicBlock.
    */
    public BasicBlock (Edges f, Edges l) {
	first = f; last = l; pred_bb = new HashSet(); succ_bb = new HashSet();
	num = BBnum++;
    }
    
    /** BasicBlock generator.
	requires: All <code>HCodeEdge</code>s linked to by the set of
	          <code>Edges</code> in the code body have
		  <code>Edges</code> objects in their <code>to</code>
		  and <code>from</code> fields.
	effects:  Creates a set of BasicBlocks corresponding to the
	          blocks implicitly contained in <code>head</code> and
		  the <code>Edges</code> objects that
		  <code>head</code> points to. 
    */
    public static BasicBlock computeBasicBlocks(Edges head) {
	/* XXX not done yet */
	return null;
    }

    public Edges getFirst() { return first; }
    public Edges getLast() { return last; }
    
    public void addPredecessor(BasicBlock bb) { pred_bb.union(bb); }
    public void addSuccessor(BasicBlock bb) { succ_bb.union(bb); }
    
    public int prevLength() { return pred_bb.size(); }
    public int nextLength() { return succ_bb.size(); }
    public Enumeration prev() { return pred_bb.elements(); }
    public Enumeration next() { return succ_bb.elements(); }
    
    public Enumeration elements() {
	return new IteratorEnumerator(listIterator());
    }
    
    /** Returns an immutable <code>ListIterator</code> for the
	<code>Edges</code> within <code>this</code>. */  
    public ListIterator listIterator() {
	return new ListIterator() {
	    Edges current = first;
	    int index = 0;
	    public boolean hasNext() { return current != last; }
	    public boolean hasPrevious() { return current != first; } // correct? 
	    public int nextIndex() { return index + 1; }
	    public int previousIndex() { return index - 1; } 
	    public Object next() {
		if (current == null) throw new NoSuchElementException();
		Util.assert((current == first) || (current.pred().length == 1));
		Util.assert(current.succ().length == 1);
		Edges r = current;
		if (r == last) current = null;
		else current = (Edges) current.succ()[0].to();
		index++;
		return r;
	    }		
	    public Object previous() {
		if (current == null) throw new NoSuchElementException();
		Util.assert((current == last) || (current.succ().length == 1));
		Util.assert(current.pred().length == 1);
		Edges r = current;
		if (r == first) current = null;
		else current = (Edges) current.pred()[0].from();
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
    
    protected BasicBlock (Edges f) {
	first = f; last = null; pred_bb = new HashSet(); succ_bb = new HashSet();
	num = BBnum++;
    }
    protected void setLast (Edges l) {
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
	    System.out.println("Edges in : "+bb.pred_bb);
	    System.out.println("Edges out: "+bb.succ_bb);
	    System.out.println();
	}
    }
    
}
