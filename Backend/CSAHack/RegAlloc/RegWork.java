/*
 * RegAlloc/RegWork.java
 *  collections of sets used by the register allocator.
 *   
 * C. Scott Ananian, cananian@princeton.edu, COS320
 */

package harpoon.Backend.CSAHack.RegAlloc;

import harpoon.Backend.CSAHack.Graph.Node;
import harpoon.Backend.CSAHack.Graph.NodeList;
import harpoon.Temp.TempList;

import java.util.Hashtable;
import java.util.Stack;

abstract class RegWork { // make all of these inner classes.
static class Worklist {
  Set simplify  = new Set("simplifyWorklist");
  Set freeze	= new Set("freezeWorklist");
  Set spill	= new Set("spillWorklist");
}

static class NodeSet {
  Set precolored = new Set("precolored");
  Set initial    = new Set("initial");
  Worklist worklist = new Worklist();
  Set spilled	 = new Set("spilled");
  Set coalesced	 = new Set("coalesced");
  Set colored	 = new Set("colored");
  Stack selectStack= new Stack();

  int correctsize;

  NodeSet(TempList registers, InterferenceGraph ig) {
//    precolored.live = initial.live = worklist.simplify.live =
//      worklist.freeze.live = worklist.spill.live = spilled.live = 
//	coalesced.live = colored.live = ig;
    // initialize sets. (only precolored and initial are non-empty)
    for (NodeList nl = ig.nodes(); nl!=null; nl=nl.tail)
      initial.add(nl.head);
    for ( ; registers!= null; registers=registers.tail) {
      Node n = ig.tnode(registers.head);
      if (n!=null) {
	precolored.add(n);
	initial.remove(n);
      }
    }
    correctsize = size();
  }

  int size() {
    return precolored.size() + initial.size() + spilled.size() + coalesced.size() + colored.size() + selectStack.size() + worklist.simplify.size() + worklist.freeze.size() + worklist.spill.size();
  }
  void check() {
    if (size() != correctsize) throw new Error("Size invariance violated");
  }
}

static class Moves { 
  Set coalesced		= new Set("coalescedMoves");
  Set constrained	= new Set("constrainedMoves");
  Set frozen		= new Set("frozenMoves");
  Set worklist		= new Set("worklistMoves");
  Set active		= new Set("activeMoves");

  Moves(harpoon.Backend.CSAHack.FlowGraph.FlowGraph fg) {
    for (NodeList nl = fg.nodes(); nl!=null; nl=nl.tail)
      if (fg.isMove(nl.head))
	worklist.add(nl.head);
  }
}

} // end RegWork
