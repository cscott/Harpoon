package harpoon.Analysis.DataFlow;

/**
 * QuadBasicBlock
 *
 * @author  John Whaley
 * @author  Felix Klock (pnkfelix@mit.edu)
 */

import harpoon.Util.*;
import harpoon.IR.Quads.*;
import java.util.Map;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.NoSuchElementException;

public class QuadBasicBlock extends BasicBlock{
    
    public QuadBasicBlock(Quad s, Quad e) {
	super(s, e);
    }

    private QuadBasicBlock (Quad f) {
	super(f);
    }

    public Enumeration quads() {
	return this.elements();
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
	BasicBlock first = new QuadBasicBlock(head, head);
	h.put(head, first);

    Quad qf = head.next(1);
    BasicBlock second = new QuadBasicBlock(qf);
    h.put(qf, second);
    addEdge(first, second);
    Util.assert(qf.nextLength() == 1);
    
    Worklist W = new HashSet();
    W.push(second);

    // loop
    while (!W.isEmpty()) {
      BasicBlock current = (BasicBlock)W.pull();
      Quad q = (Quad) current.getFirst();
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
	      h.put(q_n, bb = new QuadBasicBlock(q_n));
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
	    h.put(qn, bb = new QuadBasicBlock(qn));
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
    return "QBB"+num;
  }

}
