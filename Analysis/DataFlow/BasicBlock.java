package harpoon.Analysis.DataFlow;

/**
 * BasicBlock
 *
 * @author  John Whaley
 */

import harpoon.Util.*;
import harpoon.IR.Quads.*;
import java.util.Map;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class BasicBlock {

  static final boolean DEBUG = false;
  static void db(String s) { System.out.println(s); }

  static int BBnum = 0;

  Quad first;
  Quad last;
  Set pred_bb;
  Set succ_bb;
  int num;

  public BasicBlock (Quad f, Quad l) {
    first = f; last = l; pred_bb = new HashSet(); succ_bb = new HashSet();
    num = BBnum++;
  }

  public Quad getFirst() { return first; }
  public Quad getLast() { return last; }

  public void addPredecessor(BasicBlock bb) { pred_bb.union(bb); }
  public void addSuccessor(BasicBlock bb) { succ_bb.union(bb); }

  public int prevLength() { return pred_bb.size(); }
  public int nextLength() { return succ_bb.size(); }
  public Enumeration prev() { return pred_bb.elements(); }
  public Enumeration next() { return succ_bb.elements(); }

  public Enumeration quads() {
    return new Enumeration() {
      Quad current = first;
      public boolean hasMoreElements() { return current != last; }
      public Object nextElement() {
	if (current == null) throw new NoSuchElementException();
	Util.assert((current == first) || (current.prevLength() == 1));
	Util.assert(current.nextLength() == 1);
	Quad r = current;
	if (r == last) current = null;
	else current = current.next(0);
	return r;
      }
    };
  }

  /** Accept a visitor. */
  public void visit(BasicBlockVisitor v) { v.visit(this); }

  private BasicBlock (Quad f) {
    first = f; last = null; pred_bb = new HashSet(); succ_bb = new HashSet();
    num = BBnum++;
  }
  private void setLast (Quad l) {
    last = l;
    if (DEBUG) db(this+": from "+first+" to "+last);
  }

  public static void addEdge(BasicBlock from, BasicBlock to) {
    from.addSuccessor(to);
    to.addPredecessor(from);
    if (DEBUG) db("adding CFG edge from "+from+" to "+to);
  }

  /**
   * Returns something that maps starting quads to their basic blocks.
   */
  public static Map computeBasicBlocks(HEADER head) {

    Hashtable h = new Hashtable();

    // set stuff up.
    BasicBlock first = new BasicBlock(head, head);
    h.put(head, first);

    Quad qf = head.next(1);
    BasicBlock second = new BasicBlock(qf);
    h.put(qf, second);
    addEdge(first, second);
    Util.assert(qf.nextLength() == 1);
    
    Worklist W = new HashSet();
    W.push(second);

    // loop
    while (!W.isEmpty()) {
      BasicBlock current = (BasicBlock)W.pull();
      Quad q = current.getFirst();
      if (DEBUG) db("now in BB "+current);
      for (;;) {
	int n = q.nextLength();
	if (DEBUG) db("looking at "+q);
	if (n <= 0) break; // end of method
	if (n > 1) { // control flow split
	  if (DEBUG) db("control flow split, size "+n);
	  for (int i=0; i<n; ++i) {
	    Quad q_n = q.next(i);
	    BasicBlock bb = (BasicBlock)h.get(q_n);
	    if (bb == null) {
	      h.put(q_n, bb = new BasicBlock(q_n));
	      W.push(bb);
	      if (DEBUG) db("added "+bb);
	    }
	    addEdge(current, bb);
	  }
	  break;
	}
	Quad qn = q.next(0);
	int m = qn.prevLength();
	if (m > 1) { // control flow join
	  if (DEBUG) db("control flow join at "+qn+", size "+m);
	  BasicBlock bb = (BasicBlock)h.get(qn);
	  if (bb == null) {
	    h.put(qn, bb = new BasicBlock(qn));
	    W.push(bb);
	    if (DEBUG) db("added "+bb);
	  }
	  addEdge(current, bb);
	  break;
	}
	q = qn;
      }
      current.setLast(q);
    }

    if (DEBUG) db("finished computing CFG");

    return h;
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
