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
     */
    public BasicBlock (Edges f, Edges l) {
	first = f; last = l; pred_bb = new HashSet(); succ_bb = new HashSet();
	num = BBnum++;
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
	return new Enumeration() {
	    Edges current = first;
	    public boolean hasMoreElements() { return current != last; }
	    public Object nextElement() {
		if (current == null) throw new NoSuchElementException();
		Util.assert((current == first) || (current.pred().length == 1));
		Util.assert(current.succ().length == 1);
		Edges r = current;
		if (r == last) current = null;
		else current = (Edges) current.succ()[0].to();
		return r;
	    }
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
